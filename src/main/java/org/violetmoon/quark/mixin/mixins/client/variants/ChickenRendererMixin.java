package org.violetmoon.quark.mixin.mixins.client.variants;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Chicken;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.violetmoon.quark.content.client.module.VariantAnimalTexturesModule;
import org.violetmoon.quark.content.tweaks.module.GrabChickensModule;

@Mixin(ChickenRenderer.class)
public class ChickenRendererMixin {

	@Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/animal/Chicken;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
	private void overrideTexture(Chicken chicken, CallbackInfoReturnable<ResourceLocation> cir) {
		ChickenRenderer render = (ChickenRenderer) ((Object) this);
		ChickenModel<Chicken> model = render.getModel();
		GrabChickensModule.Client.setRenderChickenFeetStatus(chicken, model);

		ResourceLocation loc = VariantAnimalTexturesModule.Client.getChickenTexture(chicken);
		if(loc != null)
			cir.setReturnValue(loc);
	}
}
