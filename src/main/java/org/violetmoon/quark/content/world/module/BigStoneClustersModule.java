package org.violetmoon.quark.content.world.module;

import com.google.common.collect.Lists;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep.Decoration;
import net.neoforged.neoforge.common.Tags;
import org.violetmoon.quark.base.util.QuarkWorldGenWeights;
import org.violetmoon.quark.content.world.config.AirStoneClusterConfig;
import org.violetmoon.quark.content.world.config.BigStoneClusterConfig;
import org.violetmoon.quark.content.world.gen.BigStoneClusterGenerator;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.CompoundBiomeConfig;
import org.violetmoon.zeta.config.type.DimensionConfig;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZGatherHints;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.BooleanSuppliers;
import org.violetmoon.zeta.world.WorldGenHandler;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

@ZetaLoadModule(category = "world")
public class BigStoneClustersModule extends ZetaModule {

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
	}

	@Config
	public static BigStoneClusterConfig calcite = bob(BiomeTags.IS_MOUNTAIN).build();
	@Config
	public static BigStoneClusterConfig limestone = bob(Tags.Biomes.IS_SWAMP, BiomeTags.IS_OCEAN).build();
	@Config
	public static BigStoneClusterConfig jasper = bob(BiomeTags.IS_BADLANDS, Tags.Biomes.IS_SANDY).build();
	@Config
	public static BigStoneClusterConfig shale = bob(Tags.Biomes.IS_SNOWY).build();

	@Config
	public static AirStoneClusterConfig myalite = AirStoneClusterConfig.airStoneBuilder()
		.generateInAir(true)
		.dimensions(DimensionConfig.end(false))
		.biomes(CompoundBiomeConfig.fromBiomeReslocs(false, "minecraft:end_highlands"))
		.horizontalSize(20)
		.verticalSize(40)
		.horizontalVariation(6)
		.verticalVariation(10)
		.rarity(100)
		.minYLevel(58)
		.maxYLevel(62)
		.build();

	@Config(
		description = "Blocks that stone clusters can replace. If you want to make it so it only replaces in one dimension,\n"
				+ "do \"block|dimension\", as we do for netherrack and end stone by default."
	)
	public static List<String> blocksToReplace = Lists.newArrayList(
			"minecraft:stone", "minecraft:andesite", "minecraft:diorite", "minecraft:granite",
			"minecraft:netherrack|minecraft:the_nether", "minecraft:end_stone|minecraft:the_end",
			"quark:marble", "quark:limestone", "quark:jasper", "quark:slate");

	public static BiPredicate<Level, Block> blockReplacePredicate = (w, b) -> false;

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		add(calcite, Blocks.CALCITE, BooleanSuppliers.TRUE);

		add(limestone, NewStoneTypesModule.limestoneBlock, () -> NewStoneTypesModule.enabledWithLimestone);
		add(jasper, NewStoneTypesModule.jasperBlock, () -> NewStoneTypesModule.enabledWithJasper);
		add(shale, NewStoneTypesModule.shaleBlock, () -> NewStoneTypesModule.enabledWithShale);
		add(myalite, NewStoneTypesModule.myaliteBlock, () -> NewStoneTypesModule.enabledWithMyalite);
	}

	@PlayEvent
	public void addAdditionalHints(ZGatherHints event) {
		if(calcite.enabled)
			event.hintItem(Items.CALCITE, zeta());
	}

	private void add(BigStoneClusterConfig config, Block block, BooleanSupplier condition) {
		WorldGenHandler.addGenerator(this, new BigStoneClusterGenerator(config, block.defaultBlockState(), condition), Decoration.UNDERGROUND_DECORATION, QuarkWorldGenWeights.BIG_STONE_CLUSTERS);
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		blockReplacePredicate = (b, w) -> false;

		for(String s : blocksToReplace) {
			String bname = s;
			String dimension = null;

			if(bname.contains("|")) {
				String[] toks = bname.split("\\|");
				bname = toks[0];
				dimension = toks[1];
			}

			String dimFinal = dimension;
			BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(bname)).ifPresent(blockObj -> {
				if(blockObj != Blocks.AIR) {
					if(dimFinal == null)
						blockReplacePredicate = blockReplacePredicate.or((w, b) -> blockObj == b);
					else {
						blockReplacePredicate = blockReplacePredicate.or((w, b) -> {
							if(blockObj != b)
								return false;
							if(w == null)
								return false;

							return w.dimension().location().toString().equals(dimFinal);
						});
					}
				}
			});
		}
	}

}
