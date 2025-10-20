package org.violetmoon.quark.content.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;

public class VariantBookshelfBlock extends ZetaBlock {

	private final boolean flammable;

	public VariantBookshelfBlock(String type, @Nullable ZetaModule module, boolean flammable, SoundType sound) {
		super(type + "_bookshelf", module, Block.Properties.ofFullCopy(Blocks.BOOKSHELF).sound(sound));
		this.flammable = flammable;

		if(module == null) //auto registration below this line
			return;
		setCreativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS, Blocks.BOOKSHELF, false);
	}

	@Override
	public boolean isFlammableZeta(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return flammable;
	}

	@Override
	public float getEnchantPowerBonusZeta(BlockState state, LevelReader world, BlockPos pos) {
		return 1;
	}
}
