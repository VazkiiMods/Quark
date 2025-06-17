package org.violetmoon.quark.mixin.mixins.client;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.violetmoon.quark.content.tools.client.render.GlintRenderTypes;

@Mixin(RenderBuffers.class)
public class RenderBuffersMixin {

	// TODO: Move to RegisterRenderBuffersEvent for Neoforge

	@Inject(method = "lambda$new$0", at = @At("TAIL"))
	private static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map, RenderType type, CallbackInfo ci) {
		GlintRenderTypes.addGlintTypes(map);
	}
}
