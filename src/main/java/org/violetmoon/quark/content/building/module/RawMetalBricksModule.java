package org.violetmoon.quark.content.building.module;

import net.minecraft.world.level.block.Block;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import java.util.ArrayList;
import java.util.List;

@ZetaLoadModule(category = "building")
public class RawMetalBricksModule extends ZetaModule {
	public static List<Block> blocks = new ArrayList<>();

	@LoadEvent
	public final void register(ZRegister event) {
        IZetaBlock iron = (IZetaBlock) new ZetaBlock("raw_iron_bricks", this, Properties.ofFullCopy(Blocks.RAW_IRON_BLOCK)).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, Blocks.IRON_BLOCK, false);
		blocks.add(iron.getBlock());
        event.getVariantRegistry().addSlabStairsWall(iron, CreativeModeTabs.BUILDING_BLOCKS);
		IZetaBlock gold = (IZetaBlock) new ZetaBlock("raw_gold_bricks", this, Properties.ofFullCopy(Blocks.RAW_GOLD_BLOCK)).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, Blocks.GOLD_BLOCK, false);
		blocks.add(gold.getBlock());
        event.getVariantRegistry().addSlabStairsWall(gold, CreativeModeTabs.BUILDING_BLOCKS);
		IZetaBlock copper = (IZetaBlock) new ZetaBlock("raw_copper_bricks", this, Properties.ofFullCopy(Blocks.RAW_COPPER_BLOCK)).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, Blocks.COPPER_BLOCK, true);
		blocks.add(copper.getBlock());
        event.getVariantRegistry().addSlabStairsWall(copper, CreativeModeTabs.BUILDING_BLOCKS);

		//ImmutableSet.of(iron, gold, copper).forEach(what -> event.getVariantRegistry().addSlabStairsWall(what, CreativeModeTabs.BUILDING_BLOCKS));
	}

}
