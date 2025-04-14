package org.violetmoon.quark.content.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.util.BlockPropertyUtil;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.RenderLayerRegistry;

public class CorundumClusterBlock extends ZetaBlock implements SimpleWaterloggedBlock {

	public final CorundumBlock base;

	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	protected static VoxelShape northAabb;
	protected static VoxelShape southAabb;
	protected static VoxelShape eastAabb;
	protected static VoxelShape westAabb;
	protected static VoxelShape upAabb;
	protected static VoxelShape downAabb;

	static {
		int yBound = 7;
		int xBound = 3;
		upAabb = Block.box(xBound, 0.0D, xBound, 16 - xBound, yBound, 16 - xBound);
		downAabb = Block.box(xBound, 16 - yBound, xBound, 16 - xBound, 16.0D, 16 - xBound);
		northAabb = Block.box(xBound, xBound, 16 - yBound, 16 - xBound, 16 - xBound, 16.0D);
		southAabb = Block.box(xBound, xBound, 0.0D, 16 - xBound, 16 - xBound, yBound);
		eastAabb = Block.box(0.0D, xBound, xBound, yBound, 16 - xBound, 16 - xBound);
		westAabb = Block.box(16 - yBound, xBound, xBound, 16.0D, 16 - xBound, 16 - xBound);
	}

	public CorundumClusterBlock(CorundumBlock base) {
		super(Quark.ZETA.registryUtil.inheritQuark(base, "%s_cluster"), base.getModule(),
				BlockPropertyUtil.copyPropertySafe(base)
						.sound(SoundType.AMETHYST_CLUSTER));

		this.base = base;
		base.cluster = this;

		registerDefaultState(defaultBlockState().setValue(FACING, Direction.DOWN).setValue(WATERLOGGED, false));

		ZetaModule module = base.getModule();
		if(module == null) //auto registration below this line
			return;

		module.zeta().renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT);
		setCreativeTab(CreativeModeTabs.COLORED_BLOCKS);
	}

	@NotNull
	@Override
	public VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		Direction direction = state.getValue(FACING);
		return switch(direction) {
		case NORTH -> northAabb;
		case SOUTH -> southAabb;
		case EAST -> eastAabb;
		case WEST -> westAabb;
		case DOWN -> downAabb;
		default -> upAabb;
		};
	}

	@Override
	public void neighborChanged(@NotNull BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos, boolean isMoving) {
		if(!canSurvive(state, worldIn, pos))
			worldIn.destroyBlock(pos, true);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		Direction dir = state.getValue(FACING);
		BlockPos off = pos.relative(dir.getOpposite());
		BlockState offState = worldIn.getBlockState(off);
		return offState.isFaceSturdy(worldIn, off, dir);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
	}

	@NotNull
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@NotNull
	@Override
	public BlockState updateShape(BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos facingPos) {
		if(state.getValue(WATERLOGGED)) {
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}

		return super.updateShape(state, facing, facingState, level, pos, facingPos);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return type == PathComputationType.WATER && state.getFluidState().is(FluidTags.WATER);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

}
