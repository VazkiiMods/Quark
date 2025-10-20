package org.violetmoon.quark.content.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.violetmoon.zeta.block.ZetaBushBlock;
import org.violetmoon.zeta.module.ZetaModule;

public class GlowShroomBlock extends ZetaBushBlock implements BonemealableBlock {

	protected static final VoxelShape SHAPE = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 11.0D);

	public GlowShroomBlock(@Nullable ZetaModule module) {
		super("glow_shroom", module, null,
				Properties.ofFullCopy(Blocks.RED_MUSHROOM)
						.randomTicks()
						.lightLevel(s -> 10));
	}

	@Override
	public void animateTick(@NotNull BlockState stateIn, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull RandomSource rand) {
		super.animateTick(stateIn, worldIn, pos, rand);

		if(rand.nextInt(12) == 0 && worldIn.getBlockState(pos.above()).isAir())
			worldIn.addParticle(ParticleTypes.END_ROD,
					pos.getX() + 0.4 + rand.nextDouble() * 0.2,
					pos.getY() + 0.5 + rand.nextDouble() * 0.1,
					pos.getZ() + 0.4 + rand.nextDouble() * 0.2,
					(Math.random() - 0.5) * 0.04,
					(1 + Math.random()) * 0.02,
					(Math.random() - 0.5) * 0.04);
	}

	@NotNull
	@Override
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean mayPlaceOn(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
		return state.getBlock() == Blocks.DEEPSLATE;
	}

	@Override
	public boolean isValidBonemealTarget(LevelReader reader, BlockPos pos, BlockState state) {
		return true;
	}

	@Override
	public boolean isBonemealSuccess(@NotNull Level world, RandomSource random, @NotNull BlockPos pos, @NotNull BlockState state) {
		return random.nextFloat() < 0.4D;
	}

	@Override
	public void performBonemeal(@NotNull ServerLevel world, @NotNull RandomSource random, @NotNull BlockPos pos, @NotNull BlockState state) {
		HugeGlowShroomBlock.place(world, random, pos);
	}

}
