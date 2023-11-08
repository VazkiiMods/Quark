package org.violetmoon.quark.content.building.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.violetmoon.quark.content.building.module.VariantChestsModule;

import javax.annotation.Nonnull;

public class VariantTrappedChestBlockEntity extends VariantChestBlockEntity {

	public VariantTrappedChestBlockEntity(BlockPos pos, BlockState state) {
		super(VariantChestsModule.trappedChestTEType, pos, state);
	}

	@Override
	protected void signalOpenCount(@Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState state, int prevOpenCount, int openCount) {
		super.signalOpenCount(world, pos, state, prevOpenCount, openCount);
		if (prevOpenCount != openCount) {
			Block block = state.getBlock();
			world.updateNeighborsAt(pos, block);
			world.updateNeighborsAt(pos.below(), block);
		}
	}

}
