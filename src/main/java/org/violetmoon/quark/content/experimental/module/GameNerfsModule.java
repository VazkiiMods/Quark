package org.violetmoon.quark.content.experimental.module;

import com.mojang.serialization.Dynamic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.gossip.GossipContainer;
import net.minecraft.world.entity.ai.gossip.GossipType;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.util.ItemEnchantsUtil;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.play.ZAnvilUpdate;
import org.violetmoon.zeta.event.play.ZItemTooltip;
import org.violetmoon.zeta.event.play.entity.ZEntityMobGriefing;
import org.violetmoon.zeta.event.play.entity.living.ZLivingDrops;
import org.violetmoon.zeta.event.play.entity.living.ZLivingTick;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.*;
import java.util.function.Predicate;

@ZetaLoadModule(category = "experimental", enabledByDefault = false)
public class GameNerfsModule extends ZetaModule {

	private static final String TAG_TRADES_ADJUSTED = "quark:zombie_trades_adjusted";

	@Config(
		description = "Makes Mending act like the Unmending mod\n"
				+ "https://www.curseforge.com/minecraft/mc-mods/unmending"
	)
	public static boolean nerfMending = true;

	@Config(
		name = "No Nerf for Mending II", description = "Makes Mending II still work even if mending is nerfed.\n" +
				"If you want Mending II, disable the sanity check on Ancient Tomes and add minecraft:mending to the tomes."
	)
	public static boolean noNerfForMendingTwo = false;

	@Config(description = "Resets all villager discounts when zombified to prevent reducing prices to ridiculous levels")
	public static boolean nerfVillagerDiscount = true;

	@Config(description = "Makes Iron Golems not drop Iron Ingots")
	public static boolean disableIronFarms = true;

	@Config(description = "Makes Boats not glide on ice")
	public static boolean disableIceRoads = true;

	@Config(description = "Makes Sheep not drop Wool when killed")
	public static boolean disableWoolDrops = true;

	@Config(description = "Disables mob griefing for only specific entities")
	public static boolean enableSelectiveMobGriefing = true;

	@Config(description = "Force Elytra to only work in specific dimensions")
	public static boolean enableDimensionLockedElytra = true;

	@Config(description = "Makes falling blocks not able to be duped via dimension crossing")
	public static boolean disableFallingBlockDupe = true;

	@Config(description = "Fixes several piston physics exploits, most notably including TNT duping")
	public static boolean disablePistonPhysicsExploits = true;

	@Config(description = "Fixes mushroom growth being able to replace blocks")
	public static boolean disableMushroomBlockRemoval = true;

	@Config(description = "Makes tripwire hooks unable to be duplicated")
	public static boolean disableTripwireHookDupe = true;

	@Config(description = "Makes villages spawn less often when close to spawn")
	public static boolean villageSpawnNerf = false;

	@Config(description = "Distance at which villages will spawn as normal. Effect scales linearly from world spawn")
	public static int villageSpawnNerfDistance = 7000;


	@Config
	public static List<String> nonGriefingEntities = Arrays.asList("minecraft:creeper", "minecraft:enderman");

	@Config
	public static List<String> elytraAllowedDimensions = List.of("minecraft:the_end");

	private static boolean staticEnabled;

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}

	// Source for this magic number is the ice-boat-nerf mod
	// https://gitlab.com/supersaiyansubtlety/ice_boat_nerf/-/blob/master/src/main/java/net/sssubtlety/ice_boat_nerf/mixin/BoatEntityMixin.java
	public static float getBoatFriction(float glide) {
		return (staticEnabled && disableIceRoads) ? 0.45F : glide;
	}

	public static boolean canEntityUseElytra(LivingEntity entity, boolean prev) {
		if(!prev)
			return false;
		if(!staticEnabled || !enableDimensionLockedElytra)
			return true;

		Level level = entity.level();
		String dim = level.dimension().location().toString();
		return elytraAllowedDimensions.contains(dim);
	}

	public static boolean stopFallingBlocksDuping() {
		return staticEnabled && disableFallingBlockDupe;
	}

	public static boolean stopPistonPhysicsExploits() {
		return staticEnabled && disablePistonPhysicsExploits;
	}

	public static boolean shouldMushroomsUseTreeReplacementLogic() {
		return staticEnabled && disableMushroomBlockRemoval;
	}

	public static boolean shouldTripwireHooksCheckForAir() {
		return staticEnabled && disableTripwireHookDupe;
	}

	@PlayEvent
	public void onMobGriefing(ZEntityMobGriefing event) {
		if(!enableSelectiveMobGriefing || event.getEntity() == null) return;

		String name = BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()).toString();
		if(nonGriefingEntities.contains(name))
			event.setCanGrief(false);
	}

	public static Predicate<ItemStack> limitMendingItems(Predicate<ItemStack> base, RegistryAccess access) {
		if(!staticEnabled || !nerfMending) return base;

		if (noNerfForMendingTwo) {
			Holder<Enchantment> mending = access.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING);
			return (stack) -> base.test(stack) && Quark.ZETA.itemExtensions.get(stack).getEnchantmentLevelZeta(stack, mending) > 1;
		}
		return (stack) -> false;
	}

	private boolean hasMending(ItemStack stack, Holder<Enchantment> mending) {
		int mendingLevel = 0;
        if (stack.has(DataComponents.STORED_ENCHANTMENTS)) {
            mendingLevel = stack.get(DataComponents.STORED_ENCHANTMENTS).getLevel(mending);
        } else if (stack.has(DataComponents.ENCHANTMENTS)) {
            mendingLevel = stack.get(DataComponents.ENCHANTMENTS).getLevel(mending);
        }
		return mendingLevel > 0 && (!noNerfForMendingTwo || mendingLevel < 2);
	}

	@PlayEvent
	public void onAnvilUpdate(ZAnvilUpdate event) {
		if (!nerfMending) return;

		Holder<Enchantment> mending = event.getPlayer().level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING);
		ItemStack left = event.getLeft();
		ItemStack right = event.getRight();
		ItemStack output = event.getOutput();

		if (output.isEmpty() && (left.isEmpty() || right.isEmpty())) return;

		boolean isMended = false;

		if (hasMending(left, mending) || hasMending(right, mending)) {
			if ((left.getItem().equals(right.getItem())) || (right.getItem().equals(Items.ENCHANTED_BOOK))) {
				isMended = true;
			}
		}

		if (isMended) {
			if (output.isEmpty()) {
				output = left.copy();
			}

			ItemEnchantments enchLeft = Optional.ofNullable(output.get(DataComponents.ENCHANTMENTS)).orElse(ItemEnchantments.EMPTY);
			ItemEnchantments.Mutable toApply = new ItemEnchantments.Mutable(enchLeft);
			ItemEnchantments enchRight = Optional.ofNullable(right.get(DataComponents.ENCHANTMENTS)).orElse(ItemEnchantments.EMPTY);

			for(Holder<Enchantment> enchantment : enchRight.keySet()) {
				if(enchantment.value().canEnchant(output)) {
					int level = enchRight.getLevel(enchantment);
					if(toApply.keySet().contains(enchantment)) {
						int levelPresent = toApply.getLevel(enchantment);
						if(level > levelPresent)
							toApply.set(enchantment, level);
						else if(level == levelPresent && enchantment.value().getMaxLevel() > level)
							toApply.set(enchantment, level + 1);
					} else {
						toApply.set(enchantment, level);
					}
				}
			}

            toApply.removeIf(enchantmentHolder -> enchantmentHolder.is(mending));

            output.set(DataComponents.ENCHANTMENTS, toApply.toImmutable());


            output.set(DataComponents.REPAIR_COST, 0);
			if (output.isDamageableItem()) {
				output.setDamageValue(0);
			}

			event.setOutput(output);
			event.setCost(5);
		}
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends GameNerfsModule {

		@PlayEvent
		public void onTooltip(ZItemTooltip event) {
			if (!nerfMending) return;

			Component itemgotmodified = Component.translatable("quark.misc.repaired").withStyle(ChatFormatting.YELLOW);
			if (Optional.ofNullable(event.getItemStack().get(DataComponents.REPAIR_COST)).orElse(0) > 0) {
				event.getToolTip().add(itemgotmodified);
			}
		}
	}

	@PlayEvent
	public void onTick(ZLivingTick event) {
		if(nerfVillagerDiscount && event.getEntity().getType() == EntityType.ZOMBIE_VILLAGER && !event.getEntity().getPersistentData().contains(TAG_TRADES_ADJUSTED)) {
			ZombieVillager zombie = (ZombieVillager) event.getEntity();

			Tag gossipsNbt = zombie.gossips;

			GossipContainer manager = new GossipContainer();
			manager.update(new Dynamic<>(NbtOps.INSTANCE, gossipsNbt));

			for(UUID uuid : manager.gossips.keySet()) {
				GossipContainer.EntityGossips gossips = manager.gossips.get(uuid);
				gossips.remove(GossipType.MAJOR_POSITIVE);
				gossips.remove(GossipType.MINOR_POSITIVE);
			}

			zombie.gossips = manager.store(NbtOps.INSTANCE);

			zombie.getPersistentData().putBoolean(TAG_TRADES_ADJUSTED, true);
		}
	}

	@PlayEvent
	public void onLoot(ZLivingDrops event) {
		if(disableIronFarms && event.getEntity().getType() == EntityType.IRON_GOLEM)
			event.getDrops().removeIf(e -> e.getItem().getItem() == Items.IRON_INGOT);

		if(disableWoolDrops && event.getEntity().getType() == EntityType.SHEEP)
			event.getDrops().removeIf(e -> e.getItem().is(ItemTags.WOOL));
	}

}
