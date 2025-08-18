package org.violetmoon.quark.mixin.mixins;

import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldGenRegion.class)
public class WorldGenRegionMixin {
	// 1.20.1 -> 1.21.1 stopped checking the createChunk parameter, lol
	@Inject(method = "getChunk(IILnet/minecraft/world/level/chunk/status/ChunkStatus;Z)Lnet/minecraft/world/level/chunk/ChunkAccess;", at = @At(value = "NEW", target = "(Ljava/lang/String;)Ljava/lang/IllegalStateException;"), cancellable = true)
	private void quark$fixCreateParamaterBeingIgnored(int p_9514_, int p_9515_, ChunkStatus p_331853_, boolean createChunk, CallbackInfoReturnable<ChunkAccess> cir) {
		if (!createChunk) {
			cir.setReturnValue(null);
		}
	}
}
