package org.violetmoon.quark.mixin.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.violetmoon.quark.content.world.module.GlimmeringWealdModule;

import java.util.List;

// Some mods call VanillaRegistries.createLookup which for some reason throw some exception with modded biomes.
// This is a terrible temporary fix until the real issue is found
@Mixin(RegistrySetBuilder.BuildState.class)
public class RegistrySetBuilderHackMixin {
    @WrapOperation(method = "reportNotCollectedHolders",
            at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0)
    )
    public <E> boolean quark$preventInvalidGWBiome(List<RuntimeException> instance, E error, Operation<Boolean> original, @Local ResourceKey<Object> resourcekey) {
        if (resourcekey.location().equals(GlimmeringWealdModule.BIOME_NAME))
            return false;
        // This isn't wrong, but the mcdev plugin thinks it is due to the generic E here
        // so just ignore the error or smt idk
        return original.call(instance, error);
    }
}
