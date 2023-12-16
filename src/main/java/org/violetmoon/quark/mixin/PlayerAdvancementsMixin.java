package org.violetmoon.quark.mixin;

import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import org.violetmoon.quark.base.handler.GeneralConfig;

@Mixin(AdvancementVisibilityEvaluator.class)
public class PlayerAdvancementsMixin {

	@ModifyConstant(method = "evaluateVisibility(Lnet/minecraft/advancements/Advancement;Ljava/util/function/Predicate;Lnet/minecraft/server/advancements/AdvancementVisibilityEvaluator$Output;)V", constant = @Constant(intValue = 2))
	private static int visibility(int curr) {
		return GeneralConfig.advancementVisibilityDepth;
	}

}
