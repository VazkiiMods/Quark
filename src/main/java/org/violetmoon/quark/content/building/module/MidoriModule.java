package org.violetmoon.quark.content.building.module;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.block.ZetaPillarBlock;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZLoadComplete;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.item.ZetaItem;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

import java.util.ArrayList;
import java.util.List;

@ZetaLoadModule(category = "building")
public class MidoriModule extends ZetaModule {
	public static List<Block> blocks = new ArrayList<>();

	public static Item moss_paste;

	@LoadEvent
	public final void register(ZRegister event) {
		moss_paste = new ZetaItem("moss_paste", this, new Item.Properties()).setCreativeTab(CreativeModeTabs.INGREDIENTS, Items.CLAY_BALL, false);

		Block.Properties props = Block.Properties.of()
				.mapColor(MapColor.COLOR_LIGHT_GREEN)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.requiresCorrectToolForDrops()
				.strength(1.5F, 6.0F);

		CreativeTabManager.startChain(CreativeModeTabs.BUILDING_BLOCKS, false, true, Blocks.COAL_BLOCK);
		Block midori = new ZetaBlock("midori_block", this, props).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, Blocks.COAL_BLOCK, true);
		blocks.add(midori); //0
		Block pillar = new ZetaPillarBlock("midori_pillar", this, props).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
		blocks.add(pillar); //1
		event.getVariantRegistry().addSlabAndStairs((IZetaBlock) midori, null);
		CreativeTabManager.endChain();
	}

	@LoadEvent
	public void loadComplete(ZLoadComplete event) {
		event.enqueueWork(() -> {
			ComposterBlock.COMPOSTABLES.put(moss_paste, 0.5F);
		});
	}

}
