package org.violetmoon.quark.mixin.mixins.client.variants;

import net.minecraft.client.renderer.entity.AllayRenderer;
import net.minecraft.client.renderer.entity.CowRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.allay.Allay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.client.module.VariantAnimalTexturesModule;

@Mixin(AllayRenderer.class)
public class AllayRendererMixin {
    @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/animal/allay/Allay;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private void overrideTexture(Allay allay, CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation loc = VariantAnimalTexturesModule.Client.getAllayTexture(allay);
        if(loc != null)
            cir.setReturnValue(loc);
    }
}
