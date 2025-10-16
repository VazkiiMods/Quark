package org.violetmoon.quark.content.building.module;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

import org.violetmoon.quark.content.building.block.MudBrickLatticeBlock;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.block.ZetaPillarBlock;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

import java.util.ArrayList;
import java.util.List;

@ZetaLoadModule(category = "building")
public class MoreMudBlocksModule extends ZetaModule {
	public static List<Block> blocks = new ArrayList<>();

	@LoadEvent
	public final void register(ZRegister event) {
		BlockBehaviour.Properties props = Properties.ofFullCopy(Blocks.MUD_BRICKS);

		CreativeTabManager.startChain(CreativeModeTabs.BUILDING_BLOCKS, false, false, Blocks.MUD_BRICK_STAIRS);
		Block carved = new ZetaBlock("carved_mud_bricks", this, props).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS, Blocks.MUD_BRICK_STAIRS, false);
		blocks.add(carved);
		Block pillar = new ZetaPillarBlock("mud_pillar", this, props).setCreativeTab(CreativeModeTabs.BUILDING_BLOCKS);
		blocks.add(pillar);
		Block lattice = new MudBrickLatticeBlock(this, props);
		blocks.add(lattice);
		CreativeTabManager.endChain();
	}

}
