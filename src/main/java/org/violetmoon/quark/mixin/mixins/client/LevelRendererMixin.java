package org.violetmoon.quark.mixin.mixins.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.item.JukeboxSong;

import net.minecraft.world.level.block.entity.JukeboxBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.violetmoon.quark.base.item.QuarkMusicDiscItem;
import org.violetmoon.quark.content.tools.module.AmbientDiscsModule;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

	@Shadow @Nullable private ClientLevel level;

	@Inject(
		method = "Lnet/minecraft/client/renderer/LevelRenderer;playJukeboxSong(Lnet/minecraft/core/Holder;Lnet/minecraft/core/BlockPos;)V",
		remap = false,
		at = @At(value = "JUMP", ordinal = 1),
		cancellable = true
	)
	public void playStreamingMusic(Holder<JukeboxSong> song, BlockPos pos, CallbackInfo ci) {

		if(level.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox && jukebox.getTheItem().getItem() instanceof QuarkMusicDiscItem quarkDisc && AmbientDiscsModule.Client.playAmbientSound(quarkDisc, pos))
			ci.cancel();
	}

}
