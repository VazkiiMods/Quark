package org.violetmoon.quark.integration.terrablender;

import com.mojang.datafixers.util.Pair;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.OverworldBiomeBuilder;

import org.violetmoon.zeta.module.ZetaModule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractUndergroundBiomeHandler {

	protected List<UndergroundBiomeDesc> undergroundBiomeDescs = new ArrayList<>(2);

	public void registerUndergroundBiome(ZetaModule module, ResourceLocation id, Climate.ParameterPoint climate) {
		registerUndergroundBiome(new UndergroundBiomeDesc(module, id, climate));
	}

	public void registerUndergroundBiome(UndergroundBiomeDesc desc) {
		undergroundBiomeDescs.add(desc);
	}

	protected void addUndergroundBiomesTo(Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer) {
		for(UndergroundBiomeDesc desc : undergroundBiomeDescs)
			if(desc.module().isEnabled())
				consumer.accept(Pair.of(desc.climateParameterPoint(), desc.resourceKey()));
	}

	/**
	 * This gets hooked up to the vanilla method OverworldBiomeBuilder#addUndergroundBiomes.
	 * Calling the Consumer will effectively, add biomes to the `minecraft:overworld`
	 * MultiNoiseBiomeSourceParameterList.Preset.
	 */
	public abstract void modifyVanillaOverworldPreset(OverworldBiomeBuilder builder, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> consumer);

	public record UndergroundBiomeDesc(ZetaModule module, ResourceLocation id, Climate.ParameterPoint climateParameterPoint) {
		public ResourceKey<Biome> resourceKey() {
			return ResourceKey.create(Registries.BIOME, id);
		}
	}
}
