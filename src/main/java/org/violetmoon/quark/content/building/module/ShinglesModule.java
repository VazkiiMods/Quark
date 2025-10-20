package org.violetmoon.quark.content.building.module;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ZetaLoadModule(category = "building")
public class ShinglesModule extends ZetaModule {
	public static List<Block> blocks = new ArrayList<>();

	public static Map<DyeColor, IZetaBlock> blockMap = new HashMap<>(); //datagen only

	@LoadEvent
	public final void register(ZRegister event) {
		add(event, "", Blocks.TERRACOTTA);

		add(event, "white_", Blocks.WHITE_TERRACOTTA);
		add(event, "orange_", Blocks.ORANGE_TERRACOTTA);
		add(event, "magenta_", Blocks.MAGENTA_TERRACOTTA);
		add(event, "light_blue_", Blocks.LIGHT_BLUE_TERRACOTTA);
		add(event, "yellow_", Blocks.YELLOW_TERRACOTTA);
		add(event, "lime_", Blocks.LIME_TERRACOTTA);
		add(event, "pink_", Blocks.PINK_TERRACOTTA);
		add(event, "gray_", Blocks.GRAY_TERRACOTTA);
		add(event, "light_gray_", Blocks.LIGHT_GRAY_TERRACOTTA);
		add(event, "cyan_", Blocks.CYAN_TERRACOTTA);
		add(event, "purple_", Blocks.PURPLE_TERRACOTTA);
		add(event, "blue_", Blocks.BLUE_TERRACOTTA);
		add(event, "brown_", Blocks.BROWN_TERRACOTTA);
		add(event, "green_", Blocks.GREEN_TERRACOTTA);
		add(event, "red_", Blocks.RED_TERRACOTTA);
		add(event, "black_", Blocks.BLACK_TERRACOTTA);
	}

	private void add(ZRegister event, String name, Block parent) {
		IZetaBlock block = event.getVariantRegistry().addSlabAndStairs((IZetaBlock) new ZetaBlock(name + "shingles", this, Block.Properties.ofFullCopy(parent)).setCreativeTab(CreativeModeTabs.COLORED_BLOCKS, parent, false), CreativeModeTabs.COLORED_BLOCKS);

		blocks.add(block.getBlock());

		if(!name.isEmpty() && Utils.isDevEnv()){
			int lastIndex = name.lastIndexOf("_");
			String newString = name.substring(0, lastIndex) + name.substring(lastIndex + 1);
			blockMap.put(DyeColor.byName(newString, null), block);
		}
	}

}
