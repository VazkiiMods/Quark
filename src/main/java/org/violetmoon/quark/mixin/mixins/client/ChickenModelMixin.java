package org.violetmoon.quark.mixin.mixins.client;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(ChickenModel.class)
public class ChickenModelMixin implements ModelRootAccess {
    private ModelPart quark$root = new ModelPart(List.of(), Map.of());

    @Inject(method = "<init>", at = @At("RETURN"))
    public void addRoot(ModelPart root, CallbackInfo ci) {
        quark$root = root;
    }

    @Override
    public ModelPart getRoot() {
        return quark$root;
    }
}
