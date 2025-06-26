package org.violetmoon.quark.content.world.module;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.util.QuarkWorldGenWeights;
import org.violetmoon.quark.content.world.gen.FallenLogGenerator;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.DimensionConfig;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.world.WorldGenHandler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;

@ZetaLoadModule(category = "world")
public class FallenLogsModule extends ZetaModule {

	@Config
	public static DimensionConfig dimensions = new DimensionConfig(false, "minecraft:overworld");

	@Config(description = "Percentage of fallen logs spawning as hollow. Requires Hollow Logs Module to be enabled")
	public static double hollowChance = 0.7;
	
	@Config
	public static int rarity = 5;

	@Config(description = "chance for logs to spawn on water")
	public static double onWaterChance = 0.1;
	
	@Config
	public static int sparseBiomeRarity = 12;
	
	@Config(description = "Tags that define which biomes can have which wood types")
	public static List<String> biomeTags = Arrays.asList(
			"has_fallen_acacia=minecraft:acacia_log",
            "has_fallen_birch=minecraft:birch_log",
            "has_fallen_cherry=minecraft:cherry_log",
            "has_fallen_dark_oak=minecraft:dark_oak_log",
            "has_fallen_jungle=minecraft:jungle_log",
            "has_fallen_mangrove=minecraft:mangrove_log",
            "has_fallen_oak=minecraft:oak_log",
            "has_fallen_spruce=minecraft:spruce_log");

	public static Map<TagKey<Biome>, Block> blocksPerTag = new HashMap<>();
	
	public static TagKey<Biome> reducedLogsTag;
	public static TagKey<Block> canSpawnOnTag;

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		reducedLogsTag = Quark.asTagKey(Registries.BIOME, "has_lower_fallen_tree_density");
		canSpawnOnTag = Quark.asTagKey(Registries.BLOCK, "fallen_log_can_spawn_on");
		WorldGenHandler.addGenerator(this, new FallenLogGenerator(dimensions), Decoration.TOP_LAYER_MODIFICATION, QuarkWorldGenWeights.FALLEN_LOGS);
	}
	
	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		blocksPerTag.clear();
		for(String s : biomeTags) {
			String[] toks = s.split("=");
			
			String k = toks[0];
			String v = toks[1];
			
			TagKey<Biome> tag = Quark.asTagKey(Registries.BIOME, (k));
			Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(v));
			
			if(block == null)
				throw new IllegalArgumentException("Block " + v + " doesn't exist");
			blocksPerTag.put(tag, block);
		}
	}

}
