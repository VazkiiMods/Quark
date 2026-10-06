package org.violetmoon.quark.content.world.block;

import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.content.world.module.GlimmeringWealdModule;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;

public class HugeGlowShroomBlock extends HugeMushroomBlock implements IZetaBlock {

	private final ZetaModule module;
	private final boolean glowing;

	public HugeGlowShroomBlock(String name, ZetaModule module, final boolean glowing) {
		super(Block.Properties.ofFullCopy(Blocks.RED_MUSHROOM_BLOCK)
				.lightLevel(b -> glowing ? 12 : 0)
				.hasPostProcess((a, b, c) -> glowing).emissiveRendering((a, b, c) -> glowing)
				.randomTicks()
				.noOcclusion());

		this.module = module;
		this.glowing = glowing;

		this.module.zeta().registry.registerBlock(this, name, true);
		CreativeTabManager.addToTab(CreativeModeTabs.NATURAL_BLOCKS, this);
	}

	@Override
	public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return false;
	}

	@Override
	public int getFlammabilityZeta(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return 0;
	}

	@Override
	public int getFireSpreadSpeedZeta(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return 0;
	}

	@Override
	public void animateTick(@NotNull BlockState stateIn, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull RandomSource rand) {
		super.animateTick(stateIn, worldIn, pos, rand);

		BlockState below = worldIn.getBlockState(pos.below());
		if(glowing && rand.nextInt(10) == 0 && (below.isAir() || below.getBlock() == GlimmeringWealdModule.glow_shroom_ring))
			worldIn.addParticle(ParticleTypes.END_ROD, pos.getX() + rand.nextDouble(), pos.getY(), pos.getZ() + rand.nextDouble(), 0, -0.05 - Math.random() * 0.05, 0);
	}

	@Override
	public ZetaModule getModule() {
		return module;
	}

	@Override
	public IZetaBlock setCondition(BooleanSupplier condition) {
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return true;
	}

}
