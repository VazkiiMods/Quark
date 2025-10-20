package org.violetmoon.quark.content.world.block;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.zeta.block.ZetaBushBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.MiscUtil;

import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GlowLichenGrowthBlock extends ZetaBushBlock implements BonemealableBlock {

	protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);

	public GlowLichenGrowthBlock(@Nullable ZetaModule module) {
		super("glow_lichen_growth", module, CreativeModeTabs.NATURAL_BLOCKS,
				Properties.ofFullCopy(Blocks.GLOW_LICHEN)
						.randomTicks()
						.lightLevel(s -> 8));
	}

	@Override
	public void animateTick(@NotNull BlockState stateIn, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull RandomSource rand) {
		super.animateTick(stateIn, worldIn, pos, rand);

		// spreading
		for(int i = 0; i < 10; i++)
			worldIn.addParticle(ParticleTypes.MYCELIUM,
					pos.getX() + (Math.random() - 0.5) * 5 + 0.5,
					pos.getY() + (Math.random() - 0.5) * 8 + 0.5,
					pos.getZ() + (Math.random() - 0.5) * 5 + 0.5,
					0, 0, 0);

		// focused
		worldIn.addParticle(ParticleTypes.MYCELIUM,
				pos.getX() + (Math.random() - 0.5) * 0.4 + 0.5,
				pos.getY() + (Math.random() - 0.5) * 0.3 + 0.3,
				pos.getZ() + (Math.random() - 0.5) * 0.4 + 0.5,
				0, 0, 0);
	}

	@NotNull
	@Override
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
		return state.isFaceSturdy(world, pos, Direction.UP);
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader reader, BlockPos pos, BlockState state) {
		for(Direction dir : MiscUtil.HORIZONTALS)
			if(canSpread(reader, pos.relative(dir)))
				return true;

		return false;
	}

	@Override
	public boolean isBonemealSuccess(@NotNull Level world, @NotNull RandomSource random, @NotNull BlockPos pos, @NotNull BlockState state) {
		return true;
	}

	@Override
	public void performBonemeal(@NotNull ServerLevel world, @NotNull RandomSource rand, @NotNull BlockPos pos, @NotNull BlockState state) {
		List<Direction> list = Lists.newArrayList(MiscUtil.HORIZONTALS);
		Collections.shuffle(list);
		for(Direction dir : list) {
			BlockPos offPos = pos.relative(dir);
			if(canSpread(world, offPos)) {
				world.setBlock(offPos, state, 3);
				return;
			}
		}
	}

	private boolean canSpread(BlockGetter world, BlockPos pos) {
		BlockPos below = pos.below();
		return world.getBlockState(pos).isAir() && mayPlaceOn(world.getBlockState(below), world, below);
	}

}
