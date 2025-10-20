package org.violetmoon.quark.content.automation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.violetmoon.quark.content.automation.module.IronRodModule;
import org.violetmoon.zeta.api.ICollateralMover;
import org.violetmoon.zeta.block.IZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.CreativeTabManager;
import org.violetmoon.zeta.registry.RenderLayerRegistry;
import org.violetmoon.zeta.util.BooleanSuppliers;

import java.util.function.BooleanSupplier;

public class IronRodBlock extends EndRodBlock implements ICollateralMover, IZetaBlock {

	private final ZetaModule module;
	private BooleanSupplier enabledSupplier = BooleanSuppliers.TRUE;

	public static final BooleanProperty CONNECTED = BooleanProperty.create("connected");

	public IronRodBlock(ZetaModule module) {
		super(Block.Properties.of()
				.mapColor(DyeColor.GRAY)
				.strength(5F, 10F)
				.sound(SoundType.METAL)
				.noOcclusion()
				.forceSolidOn());

		module.zeta().registry.registerBlock(this, "iron_rod", true);
		CreativeTabManager.addToTab(CreativeModeTabs.REDSTONE_BLOCKS, this);

		module.zeta().renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT);

		this.module = module;
	}

	@Nullable
	@Override
	public ZetaModule getModule() {
		return module;
	}

	@Override
	public IronRodBlock setCondition(BooleanSupplier enabledSupplier) {
		this.enabledSupplier = enabledSupplier;
		return this;
	}

	@Override
	public boolean doesConditionApply() {
		return enabledSupplier.getAsBoolean();
	}

	@Override
	protected void createBlockStateDefinition(@NotNull Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(CONNECTED);
	}

	@Override
	public boolean isCollateralMover(Level world, BlockPos source, Direction moveDirection, BlockPos pos) {
		return moveDirection == world.getBlockState(pos).getValue(FACING) &&
				!world.getBlockState(pos.relative(moveDirection)).is(IronRodModule.ironRodImmuneTag);
	}

	@Override
	public MoveResult getCollateralMovement(Level world, BlockPos source, Direction moveDirection, Direction side, BlockPos pos) {
		return side == moveDirection && !world.getBlockState(pos.relative(side)).is(IronRodModule.ironRodImmuneTag) ? MoveResult.BREAK : MoveResult.SKIP;
	}

	@Override
	public void animateTick(@NotNull BlockState stateIn, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull RandomSource rand) {
		// NO-OP
	}

}
