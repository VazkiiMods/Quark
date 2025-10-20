package org.violetmoon.quark.content.world.module;

import net.minecraft.world.level.block.Block;
import org.violetmoon.quark.base.util.BlockPropertyUtil;
import org.violetmoon.quark.base.util.QuarkWorldGenWeights;
import org.violetmoon.quark.content.world.undergroundstyle.PermafrostStyle;
import org.violetmoon.quark.content.world.undergroundstyle.base.UndergroundStyleConfig;
import org.violetmoon.quark.content.world.undergroundstyle.base.UndergroundStyleGenerator;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.block.OldMaterials;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.config.type.CompoundBiomeConfig;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZCommonSetup;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.util.Hint;
import org.violetmoon.zeta.world.WorldGenHandler;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.List;

@ZetaLoadModule(category = "world")
public class PermafrostModule extends ZetaModule {
	@Config
	public UndergroundStyleConfig generationSettings = UndergroundStyleConfig.styleBuilder()
		.style(new PermafrostStyle())
		.biomes(CompoundBiomeConfig.fromBiomeReslocs(false, "minecraft:frozen_peaks"))
		.rarity(2)
		.horizontalSize(72)
		.verticalSize(15)
		.horizontalVariation(22)
		.verticalVariation(4)
		.minYLevel(105)
		.maxYLevel(140)
		.build();

	@Hint
	public static ZetaBlock permafrost;

    public static List<Block> blocks = new ArrayList<>();

	@LoadEvent
	public final void register(ZRegister event) {
        CreativeTabManager.startChain(CreativeModeTabs.BUILDING_BLOCKS, false,true,Blocks.DEEPSLATE);
		permafrost = (ZetaBlock) new ZetaBlock("permafrost", this,
				OldMaterials.stone()
						.mapColor(MapColor.COLOR_LIGHT_BLUE)
						.requiresCorrectToolForDrops()
						.strength(1.5F, 10F)
						.sound(SoundType.STONE))
				.setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, Blocks.DEEPSLATE, true);

        ZetaBlock permafrostBricks = (ZetaBlock) new ZetaBlock("permafrost_bricks", this,
                BlockPropertyUtil.copyPropertySafe(permafrost)).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
		CreativeTabManager.endChain();

        event.getVariantRegistry().addSlabStairsWall(permafrost, CreativeModeTabs.BUILDING_BLOCKS);
        event.getVariantRegistry().addSlabStairsWall(permafrostBricks, CreativeModeTabs.BUILDING_BLOCKS);

        ((PermafrostStyle) generationSettings.style).setBlock(permafrost.defaultBlockState());
        blocks.add(permafrost); //0
        blocks.add(permafrostBricks); //1
	}

	@LoadEvent
	public final void setup(ZCommonSetup event) {
		WorldGenHandler.addGenerator(this,
			new UndergroundStyleGenerator(generationSettings, "permafrost"),
			GenerationStep.Decoration.UNDERGROUND_DECORATION,
			QuarkWorldGenWeights.UNDERGROUND_BIOMES
		);
	}

}
