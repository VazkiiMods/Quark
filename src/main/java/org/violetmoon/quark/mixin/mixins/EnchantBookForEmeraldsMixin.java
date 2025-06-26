package org.violetmoon.quark.mixin.mixins;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import org.violetmoon.quark.content.experimental.module.EnchantmentsBegoneModule;

import java.util.List;

@Mixin(VillagerTrades.EnchantBookForEmeralds.class)
public class EnchantBookForEmeraldsMixin {

	@ModifyVariable(method = "getOffer", at = @At("STORE"))
	private ItemStack filterBegoneFromTrades(ItemStack itemStack) {
		return EnchantmentsBegoneModule.begoneEnchantmentsFromItem(itemStack);
	}
}
