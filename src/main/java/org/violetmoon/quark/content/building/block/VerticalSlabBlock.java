package org.violetmoon.quark.content.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.zeta.block.ext.IZetaBlockExtensions;
import org.violetmoon.zeta.registry.IZetaBlockColorProvider;
import org.violetmoon.zeta.registry.IZetaItemColorProvider;
import org.violetmoon.zeta.util.handler.FuelHandler.ICustomWoodFuelValue;

import java.util.function.Supplier;

/**
 * Base extensible class in case mods want to add their own slabs
 */
public class VerticalSlabBlock extends Block implements SimpleWaterloggedBlock, IZetaBlockColorProvider, IZetaBlockExtensions, ICustomWoodFuelValue {

	public static final EnumProperty<VerticalSlabType> TYPE = EnumProperty.create("type", VerticalSlabType.class);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public final Supplier<Block> parent;

	public VerticalSlabBlock(Supplier<Block> parent, Properties properties) {
		super(properties);
		this.parent = parent;
		registerDefaultState(defaultBlockState().setValue(TYPE, VerticalSlabType.NORTH).setValue(WATERLOGGED, false));
	}

	@Override
	public boolean isFlammableZeta(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		BlockState parentState = parent.get().defaultBlockState();
		return Quark.ZETA.blockExtensions.get(parentState).isFlammableZeta(parentState, world, pos, face);
	}

	@Override
	public int getFlammabilityZeta(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		BlockState parentState = parent.get().defaultBlockState();
		return Quark.ZETA.blockExtensions.get(parentState).getFlammabilityZeta(parentState, world, pos, face);
	}

	@SuppressWarnings("deprecation") //IForgeBlock
	@NotNull
	@Override
	public BlockState rotate(BlockState state, @NotNull Rotation rot) {
		return state.getValue(TYPE) == VerticalSlabType.DOUBLE ? state : state.setValue(TYPE, VerticalSlabType.fromDirection(rot.rotate(state.getValue(TYPE).direction)));
	}

	@SuppressWarnings("deprecation") //IForgeBlock
	@NotNull
	@Override
	public BlockState mirror(BlockState state, @NotNull Mirror mirrorIn) {
		VerticalSlabType type = state.getValue(TYPE);
		if(type == VerticalSlabType.DOUBLE || mirrorIn == Mirror.NONE)
			return state;

		if((mirrorIn == Mirror.LEFT_RIGHT && type.direction.getAxis() == Axis.Z)
				|| (mirrorIn == Mirror.FRONT_BACK && type.direction.getAxis() == Axis.X))
			return state.setValue(TYPE, VerticalSlabType.fromDirection(state.getValue(TYPE).direction.getOpposite()));

		return state;
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return state.getValue(TYPE) != VerticalSlabType.DOUBLE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TYPE, WATERLOGGED);
	}

	@NotNull
	@Override
	public VoxelShape getShape(BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return state.getValue(TYPE).shape;
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos blockpos = context.getClickedPos();
		BlockState blockstate = context.getLevel().getBlockState(blockpos);
		if(blockstate.getBlock() == this)
			return blockstate.setValue(TYPE, VerticalSlabType.DOUBLE).setValue(WATERLOGGED, false);

		FluidState fluid = context.getLevel().getFluidState(blockpos);
		BlockState retState = defaultBlockState().setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
		Direction direction = getDirectionForPlacement(context);
		VerticalSlabType type = VerticalSlabType.fromDirection(direction);

		return retState.setValue(TYPE, type);
	}

	private Direction getDirectionForPlacement(BlockPlaceContext context) {
		Direction direction = context.getClickedFace();
		if(direction.getAxis() != Axis.Y)
			return direction;

		BlockPos pos = context.getClickedPos();
		Vec3 vec = context.getClickLocation().subtract(new Vec3(pos.getX(), pos.getY(), pos.getZ())).subtract(0.5, 0, 0.5);
		double angle = Math.atan2(vec.x, vec.z) * -180.0 / Math.PI;
		return Direction.fromYRot(angle).getOpposite();
	}

	@Override
	public boolean canBeReplaced(BlockState state, @NotNull BlockPlaceContext useContext) {
		ItemStack itemstack = useContext.getItemInHand();
		VerticalSlabType slabtype = state.getValue(TYPE);
		return slabtype != VerticalSlabType.DOUBLE && itemstack.getItem() == this.asItem() &&
				(useContext.replacingClickedOnBlock() && (useContext.getClickedFace() == slabtype.direction && getDirectionForPlacement(useContext) == slabtype.direction) ||
						(!useContext.replacingClickedOnBlock() && useContext.getClickedFace() != slabtype.direction));
	}

	@NotNull
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public boolean placeLiquid(@NotNull LevelAccessor worldIn, @NotNull BlockPos pos, BlockState state, @NotNull FluidState fluidStateIn) {
		return state.getValue(TYPE) != VerticalSlabType.DOUBLE && SimpleWaterloggedBlock.super.placeLiquid(worldIn, pos, state, fluidStateIn);
	}

	@Override
	public boolean canPlaceLiquid(@Nullable Player player, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, BlockState state, @NotNull Fluid fluidIn) {
		return state.getValue(TYPE) != VerticalSlabType.DOUBLE && SimpleWaterloggedBlock.super.canPlaceLiquid(player, worldIn, pos, state, fluidIn);
	}

	@NotNull
	@Override
	public BlockState updateShape(@NotNull BlockState stateIn, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor worldIn, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
		if(stateIn.getValue(WATERLOGGED))
			worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));

		return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return type == PathComputationType.WATER && state.getFluidState().is(FluidTags.WATER);
	}

	@Override
	public @Nullable String getBlockColorProviderName() {
		return parent.get() instanceof IZetaBlockColorProvider prov ? prov.getBlockColorProviderName() : null;
	}

	@Override
	public @Nullable String getItemColorProviderName() {
		return parent.get() instanceof IZetaItemColorProvider prov ? prov.getItemColorProviderName() : null;
	}
	
	@Override
	public int getBurnTimeInTicksWhenWooden() {
		return 150;
	}

	public enum VerticalSlabType implements StringRepresentable {
		NORTH(Direction.NORTH),
		SOUTH(Direction.SOUTH),
		WEST(Direction.WEST),
		EAST(Direction.EAST),
		DOUBLE(null);

		private final String name;
		public final Direction direction;
		public final VoxelShape shape;

		VerticalSlabType(Direction direction) {
			this.name = direction == null ? "double" : direction.getSerializedName();
			this.direction = direction;

			if(direction == null)
				shape = Shapes.block();
			else {
				double min = 0;
				double max = 8;
				if(direction.getAxisDirection() == AxisDirection.NEGATIVE) {
					min = 8;
					max = 16;
				}

				if(direction.getAxis() == Axis.X)
					shape = Block.box(min, 0, 0, max, 16, 16);
				else
					shape = Block.box(0, 0, min, 16, 16, max);
			}
		}

		@Override
		public String toString() {
			return name;
		}

		@NotNull
		@Override
		public String getSerializedName() {
			return name;
		}

		public static VerticalSlabType fromDirection(Direction direction) {
			for(VerticalSlabType type : VerticalSlabType.values())
				if(type.direction != null && direction == type.direction)
					return type;

			return null;
		}

	}

}
