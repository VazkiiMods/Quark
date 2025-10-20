package org.violetmoon.quark.mixin.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ThrownTridentRenderer;
import net.minecraft.world.entity.projectile.ThrownTrident;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.violetmoon.quark.content.tools.module.ColorRunesModule;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorAbstractArrow;

@Mixin(ThrownTridentRenderer.class)
public class ThrownTridentRendererMixin {

	@Inject(method = "render(Lnet/minecraft/world/entity/projectile/ThrownTrident;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"))
	private void setColorRuneTargetStack(ThrownTrident entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, CallbackInfo callbackInfo) {
		ColorRunesModule.setTargetStack(((AccessorAbstractArrow)entityIn).quark$getPickupItem());
	}
}
