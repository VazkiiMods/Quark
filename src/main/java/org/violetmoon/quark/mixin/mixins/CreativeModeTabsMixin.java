package org.violetmoon.quark.mixin.mixins;

import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.core.Holder;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.content.experimental.module.EnchantmentsBegoneModule;

@Mixin(CreativeModeTabs.class)
public class CreativeModeTabsMixin {

	@ModifyExpressionValue(method = "generateEnchantmentBookTypesOnlyMaxLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/HolderLookup;listElements()Ljava/util/stream/Stream;", ordinal = 0))
	private static Stream<Holder<Enchantment>> quark$filterEnchantments(Stream<Holder<Enchantment>> in) {
		return in.filter(ench -> !EnchantmentsBegoneModule.shouldBegone(ench));
	}

	@ModifyExpressionValue(method = "generateEnchantmentBookTypesAllLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/HolderLookup;listElements()Ljava/util/stream/Stream;", ordinal = 0))
	private static Stream<Holder<Enchantment>> quark$filterEnchantments2(Stream<Holder<Enchantment>> in) {
		return in.filter(ench -> !EnchantmentsBegoneModule.shouldBegone(ench));
	}

}
