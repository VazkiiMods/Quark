package org.violetmoon.quark.content.experimental.module;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.violetmoon.quark.content.experimental.hax.PseudoAccessorMerchantOffer;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorMerchantOffer;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.play.entity.living.ZLivingConversion;
import org.violetmoon.zeta.event.play.entity.living.ZLivingTick;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "experimental", enabledByDefault = false)
public class VillagerRerollingReworkModule extends ZetaModule {

	public static final String TAG_VILLAGER_SEED = "quark:MerchantInitialSeed";
	public static final String TAG_ITEMS_REROLLED_TODAY = "quark:RerolledItemsToday";
	public static final String TAG_TRADE_TIER = "quark:tier";

	public static boolean staticEnabled;

	@Config(
		description = "If enabled, the first two trades a villager generates for a profession will always be the same for a given villager.\n" +
				"This prevents repeatedly placing down a job site block to reroll the villager's trades."
	)
	public static boolean seedInitialVillagerTrades = true;

	@Config(description = """
		Set to 0 to disable the chance to reroll trades when restocking.
		It's possible for a trade to not restock even when the chance is 1. This happens when the rerolled trade is one the villager already has.
		This chance only guarantees a reroll will be attempted.""")
	@Config.Min(0)
	@Config.Max(1)
	public static double chanceToRerollWhenRestocking = 0.25;

	@Config(
		description = "Set to 0 to disable the chance to reroll trades when restocking. Set to -1 to allow unlimited rerolling.\n" +
				"Trades earlier in the list will restock first."
	)
	public static int maximumRestocksPerDay = 3;

	@Config(
		description = "If enabled, villagers will reroll when they restock, rather than when they begin work for the day.\n" +
				"If disabled, players can prevent rerolling by ensuring the villager isn't out of stock on their last restock of the day."
	)
	public static boolean rerollOnAnyRestock = false;

	@Config(description = "If enabled, villagers will be able to reroll any trade that has been used AT ALL since the last restock.")
	public static boolean rerollEvenIfNotOutOfStock = false;

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}

	@PlayEvent
	public void assignSeedIfUnassigned(ZLivingTick event) {
		if (!(event.getEntity() instanceof LivingEntity entity)) return;
		if(canUseSeededRandom(entity)) {
			CompoundTag persistent = entity.getPersistentData();

			if(!persistent.contains(TAG_VILLAGER_SEED, Tag.TAG_LONG))
				persistent.putLong(TAG_VILLAGER_SEED, entity.getRandom().nextLong());
		}
	}

	@PlayEvent
	public void keepSeedOnConversion(ZLivingConversion.Post event) {
		LivingEntity original = event.getEntity();
		LivingEntity outcome = event.getOutcome();
		if(canUseSeededRandom(original) || canUseSeededRandom(outcome)) {
			CompoundTag persistent = original.getPersistentData();
			if(persistent.contains(TAG_VILLAGER_SEED, Tag.TAG_LONG))
				outcome.getPersistentData().putLong(TAG_VILLAGER_SEED, persistent.getLong(TAG_VILLAGER_SEED));
		}
	}

	public static void clearRerolls(Villager villager) {
		villager.getPersistentData().remove(TAG_ITEMS_REROLLED_TODAY);
	}

	public static void attemptToReroll(Villager villager) {
		if(!staticEnabled || maximumRestocksPerDay == 0 || chanceToRerollWhenRestocking == 0)
			return;

		int restocks = villager.getPersistentData().getInt(TAG_ITEMS_REROLLED_TODAY);
		if(restocks >= maximumRestocksPerDay && maximumRestocksPerDay > 0)
			return;

		MerchantOffers offers = villager.getOffers();

		for(int i = 0; i < offers.size(); i++) {
			MerchantOffer offer = offers.get(i);
			if((rerollEvenIfNotOutOfStock && offer.getUses() > 0) || offer.isOutOfStock()) {
				MerchantOffer rerolled = attemptToReroll(villager, offer);

				if(rerolled != null) {
					boolean foundEquivalent = false; // We avoid duplicate trades...
					for(MerchantOffer otherOffer : offers) {

						if(ItemStack.isSameItemSameComponents(otherOffer.getBaseCostA(), rerolled.getBaseCostA()) &&
								ItemStack.isSameItemSameComponents(otherOffer.getCostB(), rerolled.getCostB()) &&
								ItemStack.isSameItemSameComponents(otherOffer.getResult(), rerolled.getResult())) {
							foundEquivalent = true;
							break;
						}
					}

					if(!foundEquivalent) {
						rerolled.addToSpecialPriceDiff(offer.getSpecialPriceDiff());
						((AccessorMerchantOffer) rerolled).quark$setRewardExp(rerolled.shouldRewardExp() && offer.shouldRewardExp());

						restocks++;

						offers.set(i, rerolled);

						if(restocks >= maximumRestocksPerDay && maximumRestocksPerDay > 0)
							break;
					}
				}
			}
		}

		if(maximumRestocksPerDay > 0)
			villager.getPersistentData().putInt(TAG_ITEMS_REROLLED_TODAY, restocks);
	}

	public static MerchantOffer attemptToReroll(Villager villager, MerchantOffer original) {
		if(((PseudoAccessorMerchantOffer) original).quark$getTier() > VillagerData.MAX_VILLAGER_LEVEL)
			return null;

		if(villager.getRandom().nextDouble() >= chanceToRerollWhenRestocking)
			return null;

		int tier = ((PseudoAccessorMerchantOffer) original).quark$getTier();
		if(tier >= 0 && tier <= VillagerData.MAX_VILLAGER_LEVEL) {
			VillagerData data = villager.getVillagerData();
			var trades = VillagerTrades.TRADES.get(data.getProfession());
			if(trades != null && !trades.isEmpty()) {
				var listings = trades.get(tier);
				if(listings != null && listings.length > 0) {
					var listing = listings[villager.getRandom().nextInt(listings.length)];
					MerchantOffer newOffer = listing.getOffer(villager, villager.getRandom());
					if(newOffer != null) {
						((PseudoAccessorMerchantOffer) newOffer).quark$setTier(tier);
						return newOffer;
					}
				}
			}
		}

		return null;
	}

	public static boolean canUseSeededRandom(LivingEntity villager) {
		return staticEnabled && seedInitialVillagerTrades && villager instanceof VillagerDataHolder;
	}

	public static boolean shouldUseSeededRandom(LivingEntity villager, MerchantOffers offers) {
		return canUseSeededRandom(villager) && offers.isEmpty();
	}

	public static RandomSource seededRandomForVillager(AbstractVillager villager) {
		long seed = villager.getPersistentData().getLong(TAG_VILLAGER_SEED);

		return RandomSource.create(seed);
	}
}
