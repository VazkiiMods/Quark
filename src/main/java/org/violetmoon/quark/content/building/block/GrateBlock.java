package org.violetmoon.quark.content.building.block;

import it.unimi.dsi.fastutil.doubles.Double2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.api.ICrawlSpaceBlock;
import org.violetmoon.zeta.block.SimpleFluidloggedBlock;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.RenderLayerRegistry;

public class GrateBlock extends ZetaBlock implements SimpleFluidloggedBlock, ICrawlSpaceBlock {
	private static final VoxelShape TRUE_SHAPE = box(0, 15, 0, 16, 16, 16);
	private static final Double2ObjectArrayMap<VoxelShape> WALK_BLOCK_CACHE = new Double2ObjectArrayMap<>();

	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty LAVALOGGED = BooleanProperty.create("lavalogged");

	public GrateBlock(@Nullable ZetaModule module) {
		super("grate", module,
				Block.Properties.of()
						.strength(5, 10)
						.sound(SoundType.METAL)
						.isValidSpawn((what, huh, idk, hoh) -> false)
						.lightLevel(state -> state.getValue(LAVALOGGED) ? 15 : 0)
						.noOcclusion());

		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false).setValue(LAVALOGGED, false));

		if(module == null) //auto registration below this line
			return;
		module.zeta().renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT);
		setCreativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS, Blocks.CHAIN, false);
	}

	private static VoxelShape createNewBox(double stepHeight) {
		return box(0, 15, 0, 16, 17 + 16 * stepHeight, 16);
	}

	@Override
	public boolean hasDynamicShape() {
		return true;
	}

	@Override
	public boolean canCrawl(Level level, BlockState state, BlockPos pos, Direction direction) {
		return true;
	}

	@Override
	public double crawlHeight(Level level, BlockState state, BlockPos pos, Direction direction) {
		return 0;
	}

	@Override
	public boolean isLog(ServerPlayer sp, BlockState state, BlockPos pos, Direction direction) {
		return false;
	}

	@NotNull
	@Override
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return TRUE_SHAPE;
	}

	private static VoxelShape getCachedShape(double stepHeight) {
		return WALK_BLOCK_CACHE.computeIfAbsent(stepHeight, GrateBlock::createNewBox);
	}

	@Override
	public boolean collisionExtendsVerticallyZeta(BlockState state, BlockGetter level, BlockPos pos, Entity collidingEntity) {
		if(collidingEntity instanceof Animal || collidingEntity instanceof WaterAnimal)
			if(!(collidingEntity instanceof Animal animal && animal.getLeashHolder() != null))
				return !(collidingEntity instanceof WaterAnimal waterAnimal && waterAnimal.getLeashHolder() != null);
		return false;
	}

	@NotNull
	@Override
	public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		Entity entity = context instanceof EntityCollisionContext ? ((EntityCollisionContext) context).getEntity() : null;

		if(entity != null) {
			if(entity instanceof ItemEntity || entity instanceof ExperienceOrb)
				return Shapes.empty();

			boolean preventedType = entity instanceof Animal || entity instanceof WaterAnimal;
			boolean leashed = (entity instanceof Animal animal && animal.getLeashHolder() != null) ||
					(entity instanceof WaterAnimal waterAnimal && waterAnimal.getLeashHolder() != null);

			boolean onGrate = world.getBlockState(entity.blockPosition().offset(0, -1, 0)).getBlock() instanceof GrateBlock;

			if(preventedType && !leashed && !onGrate) {
                LivingEntity livingEntity = (LivingEntity) entity;
                AttributeInstance stepHeight = livingEntity.getAttribute(Attributes.STEP_HEIGHT);
                if (stepHeight != null) {
                    return getCachedShape(stepHeight.getValue());
                }
            }

			return TRUE_SHAPE;
		}

		return TRUE_SHAPE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Fluid fluidAt = context.getLevel().getFluidState(context.getClickedPos()).getType();
		BlockState state = defaultBlockState();
		return acceptsFluid(fluidAt) ? withFluid(state, fluidAt) : state;
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	public boolean propagatesSkylightDown(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
		return fluidContained(state) == Fluids.EMPTY;
	}

	@Override
	public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block updatedBlock, @NotNull BlockPos neighbor, boolean isMoving) {
		super.neighborChanged(state, level, pos, updatedBlock, neighbor, isMoving);
		if(!pos.below().equals(neighbor)) {
			BlockState neighborState = level.getBlockState(neighbor);
			if(neighborState.getFluidState().is(FluidTags.WATER) && fluidContained(state).isSame(Fluids.LAVA)) {
				level.destroyBlock(pos, true);
				level.setBlock(pos, EventHooks.fireFluidPlaceBlockEvent(level, pos, neighbor, Blocks.OBSIDIAN.defaultBlockState()), 3);
				level.levelEvent(LevelEvent.LAVA_FIZZ, pos, 0);
			}
		}
	}

	@NotNull
	@Override
	public BlockState updateShape(@NotNull BlockState state, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockPos facingPos) {
		if(state.getValue(LAVALOGGED) && state.getValue(WATERLOGGED))
			state = withFluid(state, Fluids.WATER);

		Fluid fluid = fluidContained(state);
		if(fluid != Fluids.EMPTY)
			level.scheduleTick(pos, fluid, fluid.getTickDelay(level));

		return super.updateShape(state, facing, facingState, level, pos, facingPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED, LAVALOGGED);
	}

	@NotNull
	@Override
	public FluidState getFluidState(@NotNull BlockState state) {
		FluidState contained = fluidContained(state).defaultFluidState();
		if(contained.hasProperty(BlockStateProperties.FALLING))
			contained = contained.setValue(BlockStateProperties.FALLING, false);
		return contained;
	}

	@Override
	public boolean acceptsFluid(@NotNull Fluid fluid) {
		return fluid == Fluids.WATER || fluid == Fluids.LAVA;
	}

	@NotNull
	@Override
	public BlockState withFluid(@NotNull BlockState state, @NotNull Fluid fluid) {
		return state
				.setValue(WATERLOGGED, fluid == Fluids.WATER)
				.setValue(LAVALOGGED, fluid == Fluids.LAVA);
	}

	@NotNull
	@Override
	public Fluid fluidContained(@NotNull BlockState state) {
		if(state.getValue(WATERLOGGED))
			return Fluids.WATER;
		else if(state.getValue(LAVALOGGED))
			return Fluids.LAVA;
		else
			return Fluids.EMPTY;
	}
}
