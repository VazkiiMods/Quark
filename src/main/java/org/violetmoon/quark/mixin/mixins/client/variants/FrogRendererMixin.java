package org.violetmoon.quark.mixin.mixins.client.variants;

import net.minecraft.client.renderer.entity.FrogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Frog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.client.module.VariantAnimalTexturesModule;

@Mixin(FrogRenderer.class)
public class FrogRendererMixin {
    @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/animal/frog/Frog;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private void overrideTexture(Frog frog, CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation loc = VariantAnimalTexturesModule.Client.getFrogTexture(frog);
        if(loc != null)
            cir.setReturnValue(loc);
    }
}
