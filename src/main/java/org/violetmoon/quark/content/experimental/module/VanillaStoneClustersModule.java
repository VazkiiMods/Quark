package org.violetmoon.quark.content.experimental.module;

import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.neoforged.neoforge.common.Tags;
import org.violetmoon.quark.base.util.QuarkWorldGenWeights;
import org.violetmoon.quark.content.world.config.BigStoneClusterConfig;
import org.violetmoon.quark.content.world.gen.BigStoneClusterGenerator;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.DimensionConfig;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZGatherHints;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.BooleanSuppliers;
import org.violetmoon.zeta.world.WorldGenHandler;

@ZetaLoadModule(
		category = "experimental", enabledByDefault = false,
		description = "Big Stone Clusters for Granite, Diorite, and Andesite, like Quark pre-1.18")
public class VanillaStoneClustersModule extends ZetaModule {

	@SafeVarargs
	private static BigStoneClusterConfig.Builder<?> bob(TagKey<Biome>... tags) {
		return BigStoneClusterConfig.stoneBuilder()
			.dimensions(DimensionConfig.overworld(false))
			.horizontalSize(14).verticalSize(14)
			.horizontalVariation(9).verticalVariation(9)
			.rarity(4)
			.minYLevel(20)
			.maxYLevel(80)
			.biomeAllow(tags);
	} //identical to BigStoneClusters

	@Config
	public static BigStoneClusterConfig granite = bob(BiomeTags.IS_MOUNTAIN).build();
	@Config
	public static BigStoneClusterConfig diorite = bob(Tags.Biomes.IS_SAVANNA, Tags.Biomes.IS_JUNGLE, Tags.Biomes.IS_MUSHROOM).build();
	@Config
	public static BigStoneClusterConfig andesite = bob(BiomeTags.IS_FOREST).build();

	public static boolean staticEnabled;

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		add(granite, Blocks.GRANITE);
		add(diorite, Blocks.DIORITE);
		add(andesite, Blocks.ANDESITE);
	}

	@LoadEvent
	public void addAdditionalHints(ZGatherHints event) {
		//add hints here maybe?
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();
	}

	private void add(BigStoneClusterConfig config, Block block) {
		WorldGenHandler.addGenerator(this, new BigStoneClusterGenerator(config, block.defaultBlockState(), BooleanSuppliers.TRUE), Decoration.UNDERGROUND_DECORATION, QuarkWorldGenWeights.BIG_STONE_CLUSTERS);
	}

}
