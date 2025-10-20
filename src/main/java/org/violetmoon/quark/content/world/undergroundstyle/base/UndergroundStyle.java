package org.violetmoon.quark.content.world.undergroundstyle.base;

import net.minecraft.core.registries.Registries;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.world.undergroundstyle.base.UndergroundStyleGenerator.Context;
import org.violetmoon.zeta.util.MiscUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class UndergroundStyle {

	private static final TagKey<Block> UNDERGROUND_BIOME_REPLACEABLE = Quark.asTagKey(Registries.BLOCK, "underground_biome_replaceable");

	public boolean canReplace(BlockState state) {
		return state.canBeReplaced() || state.is(UNDERGROUND_BIOME_REPLACEABLE);
	}

	public final void fill(Context context, BlockPos pos) {
		LevelAccessor world = context.world;
		BlockState state = world.getBlockState(pos);

		if(state.getDestroySpeed(world, pos) == -1)
			return;

		if(isFloor(world, pos, state))
			fillFloor(context, pos, state);
		else if(isCeiling(world, pos, state))
			fillCeiling(context, pos, state);
		else if(isWall(world, pos, state))
			fillWall(context, pos, state);
		else if(isInside(state))
			fillInside(context, pos, state);
	}

	public abstract void fillFloor(Context context, BlockPos pos, BlockState state);
	public abstract void fillCeiling(Context context, BlockPos pos, BlockState state);
	public abstract void fillWall(Context context, BlockPos pos, BlockState state);
	public abstract void fillInside(Context context, BlockPos pos, BlockState state);

	//nb. checking isSolidRender so that air doesn't count as a floor or wall etc

	public boolean isFloor(LevelAccessor world, BlockPos pos, BlockState state) {
		if(!state.isSolidRender(world, pos) || !canReplace(state))
			return false;

		BlockPos upPos = pos.above();
		return world.isEmptyBlock(upPos) || world.getBlockState(upPos).canBeReplaced();
	}

	public boolean isCeiling(LevelAccessor world, BlockPos pos, BlockState state) {
		if(!state.isSolidRender(world, pos) || !canReplace(state))
			return false;

		BlockPos downPos = pos.below();
		return world.isEmptyBlock(downPos) || world.getBlockState(downPos).canBeReplaced();
	}

	public boolean isWall(LevelAccessor world, BlockPos pos, BlockState state) {
		if(!state.isSolidRender(world, pos) || !canReplace(state))
			return false;

		return isBorder(world, pos);
	}

	public Direction getBorderSide(LevelAccessor world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		for(Direction facing : MiscUtil.HORIZONTALS) {
			BlockPos offsetPos = pos.relative(facing);
			BlockState stateAt = world.getBlockState(offsetPos);

			if(state != stateAt && world.isEmptyBlock(offsetPos) || stateAt.canBeReplaced())
				return facing;
		}

		return null;
	}

	public boolean isBorder(LevelAccessor world, BlockPos pos) {
		return getBorderSide(world, pos) != null;
	}

	public boolean isInside(BlockState state) {
		return state.is(UNDERGROUND_BIOME_REPLACEABLE);
	}

}
