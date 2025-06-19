package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import org.violetmoon.quark.content.experimental.module.EnchantmentsBegoneModule;

@Mixin(Enchantment.class)
public class EnchantmentMixin {

	@ModifyReturnValue(method = "canEnchant", at = @At("RETURN"))
	private boolean canApply(boolean prev, ItemStack stack) {
		Enchantment self = (Enchantment) (Object) this;
		return !EnchantmentsBegoneModule.shouldBegone(Holder.direct(self));
	}
}
