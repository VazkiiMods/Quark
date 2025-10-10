package org.violetmoon.quark.mixin.mixins.accessor;

import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.sounds.Music;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.Builder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;

@Mixin(OverworldBiomes.class)
public interface AccessorOverworldBiomes {
	@Invoker("biome")
	static Biome quark$biome(
			boolean hasPercipitation,
			float temperature,
			float downfall,
			Builder mobSpawnSettings,
			BiomeGenerationSettings.Builder generationSettings,
			@Nullable Music backgroundMusic
	) {
		throw new AssertionError();
	}

	@Invoker("globalOverworldGeneration")
	static void quark$globalOverworldGeneration(BiomeGenerationSettings.Builder generationSettings) {
		throw new AssertionError();
	}
}
