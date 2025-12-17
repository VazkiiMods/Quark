package org.violetmoon.quark.datagen.groups;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.biome.MobSpawnSettings.SpawnerData;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.quark.content.mobs.module.StonelingsModule;
import org.violetmoon.quark.content.world.module.GlimmeringWealdModule;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorOverworldBiomes;

public class QuarkBiomes {
	public static void bootstrap(BootstrapContext<Biome> ctx) {
		HolderGetter<PlacedFeature> placedFeatureLookup = ctx.lookup(Registries.PLACED_FEATURE);
		HolderGetter<ConfiguredWorldCarver<?>> configuredCarverLookup = ctx.lookup(Registries.CONFIGURED_CARVER);
		
		ctx.register(GlimmeringWealdModule.BIOME_KEY, glimmeringWeald(placedFeatureLookup, configuredCarverLookup));
	}

	private static Biome glimmeringWeald(HolderGetter<PlacedFeature> placedFeatures, HolderGetter<ConfiguredWorldCarver<?>> worldCarvers) {
		MobSpawnSettings.Builder mobSpawnSettingsBuilder = new MobSpawnSettings.Builder();
		BiomeDefaultFeatures.commonSpawns(mobSpawnSettingsBuilder);
		mobSpawnSettingsBuilder.addSpawn(MobCategory.CREATURE, new SpawnerData(StonelingsModule.stonelingType, 200, 1, 4));
		BiomeGenerationSettings.Builder biomeGenerationSettingsBuilder = new BiomeGenerationSettings.Builder(placedFeatures, worldCarvers);
		AccessorOverworldBiomes.quark$globalOverworldGeneration(biomeGenerationSettingsBuilder);
        BiomeDefaultFeatures.addForestFlowers(biomeGenerationSettingsBuilder);
        BiomeDefaultFeatures.addDefaultOres(biomeGenerationSettingsBuilder, true);
		BiomeDefaultFeatures.addDefaultSoftDisks(biomeGenerationSettingsBuilder);
        BiomeDefaultFeatures.addOtherBirchTrees(biomeGenerationSettingsBuilder);
        BiomeDefaultFeatures.addDefaultFlowers(biomeGenerationSettingsBuilder);
        BiomeDefaultFeatures.addForestGrass(biomeGenerationSettingsBuilder);
		BiomeDefaultFeatures.addDefaultMushrooms(biomeGenerationSettingsBuilder);
		BiomeDefaultFeatures.addDefaultExtraVegetation(biomeGenerationSettingsBuilder);
		biomeGenerationSettingsBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, GlimmeringWealdModule.GLOW_SHROOMS_FEATURE);
		biomeGenerationSettingsBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_DECORATION, GlimmeringWealdModule.GLOW_SHROOMS_EXTRAS_FEATURE);
		Music music = Musics.createGameMusic(Holder.direct(QuarkSounds.MUSIC_GLIMMERING_WEALD));
		return AccessorOverworldBiomes.quark$biome(true, 0.8F, 0.4F, mobSpawnSettingsBuilder, biomeGenerationSettingsBuilder, music);
	}
}
