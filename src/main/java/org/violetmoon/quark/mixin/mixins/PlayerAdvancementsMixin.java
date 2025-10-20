package org.violetmoon.quark.mixin.mixins;

import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.violetmoon.quark.base.config.QuarkGeneralConfig;

@Mixin(AdvancementVisibilityEvaluator.class)
public class PlayerAdvancementsMixin {

	@ModifyConstant(method = "evaluateVisibility(Lnet/minecraft/advancements/AdvancementNode;Ljava/util/function/Predicate;Lnet/minecraft/server/advancements/AdvancementVisibilityEvaluator$Output;)V", constant = @Constant(intValue = 2))
	private static int visibility(int curr) {
		return QuarkGeneralConfig.advancementVisibilityDepth;
	}

}
