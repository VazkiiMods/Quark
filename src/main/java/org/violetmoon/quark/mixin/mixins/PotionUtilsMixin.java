package org.violetmoon.quark.mixin.mixins;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.content.client.hax.PseudoAccessorItemStack;
import org.violetmoon.quark.content.client.module.ImprovedTooltipsModule;

import java.util.Collections;
import java.util.List;

@Mixin(value = PotionUtils.class)
public class PotionUtilsMixin {

	@Unique
	private static ItemStack stackActingOn;

	@Inject(method = "addPotionTooltip(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;F)V", at = @At("HEAD"))
	private static void setActingStack(ItemStack stack, List<Component> components, float durationMultiplier, CallbackInfo ci) {
		stackActingOn = stack;
	}

	@Inject(method = "addPotionTooltip(Lnet/minecraft/world/item/ItemStack;Ljava/util/List;F)V", at = @At("RETURN"))
	private static void clearActingStack(ItemStack stack, List<Component> components, float durationMultiplier, CallbackInfo ci) {
		stackActingOn = null;
	}

	@ModifyVariable(method = "addPotionTooltip(Ljava/util/List;Ljava/util/List;F)V", at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z", ordinal = 1, shift = At.Shift.BEFORE), ordinal = 2)
	private static List<Pair<Attribute, AttributeModifier>> overrideAttributeTooltips(List<Pair<Attribute, AttributeModifier>> attributes, List<MobEffectInstance> mobEffects) {
		if(stackActingOn != null && ImprovedTooltipsModule.shouldHideAttributes()) {
			((PseudoAccessorItemStack) (Object) stackActingOn).quark$capturePotionAttributes(attributes);
			return Collections.emptyList();
		}
		return attributes;
	}
}
