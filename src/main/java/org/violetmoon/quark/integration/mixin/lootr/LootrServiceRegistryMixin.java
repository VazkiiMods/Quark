package org.violetmoon.quark.integration.mixin.lootr;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import noobanidus.mods.lootr.common.api.replacement.BlockReplacementMap;
import noobanidus.mods.lootr.common.impl.LootrServiceRegistry;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.integration.lootr.LootrIntegration;


@Debug(export = true)
@Mixin(value = LootrServiceRegistry.class, remap = false)
// "Waaa waaaa I dont wanna make services I wanna write mid code" - Siuol
public class LootrServiceRegistryMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnoobanidus/mods/lootr/common/api/replacement/BlockReplacementMap;sort()V"), remap = false)
    void addReplacementsToMap(BlockReplacementMap instance) {
        if (Quark.LOOTR_INTEGRATION instanceof LootrIntegration integration) {
            integration.populate(instance);
        }
    }
}
