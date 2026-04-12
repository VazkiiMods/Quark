package org.violetmoon.quark.mixin.mixins.client.variants;

import net.minecraft.client.renderer.entity.ArmadilloRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.violetmoon.quark.content.client.module.VariantAnimalTexturesModule;

@Mixin(ArmadilloRenderer.class)
public class ArmadilloRendererMixin {
    @Inject(method = "getTextureLocation(Lnet/minecraft/world/entity/animal/armadillo/Armadillo;)Lnet/minecraft/resources/ResourceLocation;", at = @At("HEAD"), cancellable = true)
    private void overrideTexture(Armadillo armadillo, CallbackInfoReturnable<ResourceLocation> cir) {
        ResourceLocation loc = VariantAnimalTexturesModule.Client.getArmadilloTexture(armadillo);
        if(loc != null)
            cir.setReturnValue(loc);
    }
}
