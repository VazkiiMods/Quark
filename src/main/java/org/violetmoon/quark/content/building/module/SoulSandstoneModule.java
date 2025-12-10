package org.violetmoon.quark.content.building.module;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;

import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.block.OldMaterials;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

import java.util.ArrayList;
import java.util.List;

@ZetaLoadModule(category = "building")
public class SoulSandstoneModule extends ZetaModule {
    public static List<Block> blocks = new ArrayList<>();

	@LoadEvent
	public final void register(ZRegister event) {
		Block.Properties props = OldMaterials.stone().mapColor(MapColor.COLOR_BROWN)
				.requiresCorrectToolForDrops()
				.strength(0.8F);

        CreativeTabManager.startChain(CreativeModeTabs.BUILDING_BLOCKS, false,true,Blocks.SEA_LANTERN);
        Block soulSandstone = new ZetaBlock("soul_sandstone", this, props).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
		Block chiseledSoulSandstone = new ZetaBlock("chiseled_soul_sandstone", this, props).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
		Block cutSoulSandstone = new ZetaBlock("cut_soul_sandstone", this, props).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
        Block smoothSoulSandstone = new ZetaBlock("smooth_soul_sandstone", this, props).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
		CreativeTabManager.endChain();

        event.getVariantRegistry().addSlabStairsWall((IZetaBlock) soulSandstone, CreativeModeTabs.BUILDING_BLOCKS);
        event.getVariantRegistry().addSlab((IZetaBlock) cutSoulSandstone, CreativeModeTabs.BUILDING_BLOCKS);
        event.getVariantRegistry().addSlabAndStairs((IZetaBlock) smoothSoulSandstone, CreativeModeTabs.BUILDING_BLOCKS);

        blocks.add(soulSandstone); //0
        blocks.add(chiseledSoulSandstone); //1
        blocks.add(cutSoulSandstone); //2
        blocks.add(smoothSoulSandstone); //3
	}

}
