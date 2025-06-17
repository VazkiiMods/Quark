package org.violetmoon.quark.mixin.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.content.tweaks.module.AutomaticRecipeUnlockModule;

import java.util.Map;

@Mixin(value = ServerAdvancementManager.class, priority = 1001)
public class ServerAdvancementManagerMixin {

	@Shadow private Map<ResourceLocation, AdvancementHolder> advancements;

	@Inject(
		method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
		at = @At(value = "INVOKE",
				target = "Lnet/minecraft/advancements/AdvancementTree;<init>()V"),
				require = 0)
	private void removeRecipeAdvancements(Map<ResourceLocation, JsonElement> map, ResourceManager manager, ProfilerFiller filler, CallbackInfo ci, @Local ImmutableMap.Builder<ResourceLocation, AdvancementHolder> builder) {
			AutomaticRecipeUnlockModule.removeRecipeAdvancements(this.advancements);
	}
}
