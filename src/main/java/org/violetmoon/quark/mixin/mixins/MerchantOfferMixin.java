package org.violetmoon.quark.mixin.mixins;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.experimental.hax.PseudoAccessorMerchantOffer;
import org.violetmoon.quark.content.tools.module.AncientTomesModule;

@Mixin(MerchantOffer.class)
public class MerchantOfferMixin implements PseudoAccessorMerchantOffer {

	// Does not need to be synced
	@Unique
	private int quark$tier;

	@Override
	public int quark$getTier() {
		return quark$tier;
	}

	@Override
	public void quark$setTier(int tier) {
		this.quark$tier = tier;
	}

	/*@Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
	private void setTierWhenConstructed(CompoundTag tag, CallbackInfo ci) {
		if(tag.contains(VillagerRerollingReworkModule.TAG_TRADE_TIER, Tag.TAG_ANY_NUMERIC))
			tier = tag.getInt(VillagerRerollingReworkModule.TAG_TRADE_TIER);
		else
			tier = -1;
	}

	@Inject(method = "<init>(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;IIIFI)V", at = @At("RETURN"))
	private void setTierWhenConstructed(ItemStack baseCostA, ItemStack costB, ItemStack result, int uses, int maxUses, int xp, float priceMultiplier, int demand, CallbackInfo ci) {
		tier = VillagerData.MAX_VILLAGER_LEVEL + 1; // Tier will be set in AbstractVillager#addOffersFromItemListings. If it isn't set, this marks a trade as non-rerollable
	}

	@ModifyReturnValue(method = "createTag", at = @At("RETURN"))
	private CompoundTag addTierToTag(CompoundTag tag) {
	    if(tier >= 0)
			tag.putInt(VillagerRerollingReworkModule.TAG_TRADE_TIER, tier);
		return tag;
	}
	*/

    @Inject(method = "satisfiedBy", at = @At("HEAD"), cancellable = true)
    private void isRequiredItem(ItemStack playerOfferA, ItemStack playerOfferB, CallbackInfoReturnable<Boolean> cir) {
        MerchantOffer offer = (MerchantOffer) (Object) this;
        if (AncientTomesModule.matchWildcardEnchantedBook(offer, playerOfferA, playerOfferB)) {
            cir.setReturnValue(true);
        }
    }
}