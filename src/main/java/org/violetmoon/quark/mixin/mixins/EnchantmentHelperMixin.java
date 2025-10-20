package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.tools.module.AncientTomesModule;
import org.violetmoon.quark.content.tweaks.module.GoldToolsHaveFortuneModule;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

	@ModifyReturnValue(method = "getItemEnchantmentLevel", at = @At("RETURN"))
	private static int getActualEnchantmentLevel(int original, @Local(ordinal = 0, argsOnly = true) Holder<Enchantment> holder, @Local(ordinal = 0, argsOnly = true) ItemStack stack) {
		return GoldToolsHaveFortuneModule.getActualEnchantmentLevel(holder, stack, original);
	}

	@Inject(method = "getComponentType", at = @At("HEAD"), cancellable = true)
	private static void getAncientTomeEnchantments(ItemStack stack, CallbackInfoReturnable<DataComponentType<ItemEnchantments>> callbackInfoReturnable) {
		Holder<Enchantment> enchant = AncientTomesModule.getTomeEnchantment(stack);

		if(enchant != null && stack != null) {
			callbackInfoReturnable.setReturnValue(DataComponents.ENCHANTMENTS);
		}
	}
}
