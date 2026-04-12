package org.violetmoon.quark.mixin.mixins.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.JukeboxSong;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import org.violetmoon.quark.base.Quark;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Unique
    private TagKey<JukeboxSong> ambientSounds = TagKey.create(Registries.JUKEBOX_SONG, Quark.asResource("ambient"));

	@ModifyExpressionValue(
		method = "playJukeboxSong(Lnet/minecraft/core/Holder;Lnet/minecraft/core/BlockPos;)V",
		remap = false,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;forJukeboxSong(Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/client/resources/sounds/SimpleSoundInstance;")
    )
	public SimpleSoundInstance playStreamingMusic(SimpleSoundInstance original, @Local(ordinal = 0) Holder<JukeboxSong> song) {
        if (song.is(ambientSounds)) {
            return new SimpleSoundInstance(song.value().soundEvent().value().getLocation(),
                    SoundSource.RECORDS,
                    4.0F,
                    1.0F,
                    SoundInstance.createUnseededRandom(),
                    true,
                    0,
                    SoundInstance.Attenuation.LINEAR, original.getX(), original.getY(), original.getZ(), false);
        } else return original;
	}
}

