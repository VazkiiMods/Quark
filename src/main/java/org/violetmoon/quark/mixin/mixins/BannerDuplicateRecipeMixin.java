package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.crafting.BannerDuplicateRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.content.tweaks.module.MoreBannerLayersModule;

@Mixin(BannerDuplicateRecipe.class)
public class BannerDuplicateRecipeMixin {

	@ModifyExpressionValue(method = "matches(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/world/level/Level;)Z", at = @At(value = "CONSTANT", args = "intValue=6"))
	public int getLimitMatches(int original) {
		return MoreBannerLayersModule.getLimit(original);
	}

	@ModifyExpressionValue(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "CONSTANT", args = "intValue=6"))
	public int getLimitAssemble(int original) {
		return MoreBannerLayersModule.getLimit(original);
	}

}
