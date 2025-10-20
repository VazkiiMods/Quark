package org.violetmoon.quark.addons.oddities.block.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.addons.oddities.block.be.PipeBlockEntity;
import org.violetmoon.quark.addons.oddities.module.PipesModule;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.RenderLayerRegistry;
import org.violetmoon.zeta.util.MiscUtil;

import java.util.HashSet;
import java.util.Set;

public abstract class BasePipeBlock extends ZetaBlock implements EntityBlock {

	protected static final BooleanProperty DOWN = BlockStateProperties.DOWN;
	protected static final BooleanProperty UP = BlockStateProperties.UP;
	protected static final BooleanProperty NORTH = BlockStateProperties.NORTH;
	protected static final BooleanProperty SOUTH = BlockStateProperties.SOUTH;
	protected static final BooleanProperty WEST = BlockStateProperties.WEST;
	protected static final BooleanProperty EAST = BlockStateProperties.EAST;

	protected static BooleanProperty property(Direction direction) {
		return switch(direction) {
		case DOWN -> DOWN;
		case UP -> UP;
		case NORTH -> NORTH;
		case SOUTH -> SOUTH;
		case WEST -> WEST;
		case EAST -> EAST;
		};
	}

	protected BasePipeBlock(String name, SoundType soundType, @Nullable ZetaModule module){
		this(name,module, Block.Properties.of()
						.instrument(NoteBlockInstrument.HAT)
						.strength(3F, 10F)
						.sound(soundType)
						.noOcclusion()
		);
	}

	protected BasePipeBlock(String name,  @Nullable ZetaModule module, BlockBehaviour.Properties properties) {
		super(name, module, properties);

		registerDefaultState(getDefaultPipeState());

		if(module == null) //auto registration below this line
			return;

		setCreativeTab(CreativeModeTabs.REDSTONE_BLOCKS);
		module.zeta().renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT);
	}

	public BlockState getDefaultPipeState() {
		return defaultBlockState()
				.setValue(DOWN, false).setValue(UP, false)
				.setValue(NORTH, false).setValue(SOUTH, false)
				.setValue(WEST, false).setValue(EAST, false);
	}

	boolean isPipeWaterlogged(BlockState state) {
		return false;
	}

	public boolean allowsFullConnection(PipeBlockEntity.ConnectionType conn) {
		return conn.isSolid;
	}

	@Override
	public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {

		// fix pipes if they're ruined
		if(stack.getItem() == Items.STICK) {
			Set<BlockPos> found = new HashSet<>();
			boolean fixedAny = false;

			Set<BlockPos> candidates = new HashSet<>();
			Set<BlockPos> newCandidates = new HashSet<>();

			candidates.add(pos);
			do {
				for(BlockPos cand : candidates) {
					for(Direction d : Direction.values()) {
						BlockPos offPos = cand.relative(d);
						BlockState offState = worldIn.getBlockState(offPos);
						if(offState.getBlock() == this && !candidates.contains(offPos) && !found.contains(offPos))
							newCandidates.add(offPos);
					}

					BlockState curr = worldIn.getBlockState(cand);
					BlockState target = getTargetState(worldIn, cand);
					if(!target.equals(curr)) {
						fixedAny = true;
						worldIn.setBlock(cand, target, 2 | 4);
					}
				}

				found.addAll(candidates);
				candidates = newCandidates;
				newCandidates = new HashSet<>();
			} while(!candidates.isEmpty());

			if(fixedAny)
				return ItemInteractionResult.sidedSuccess(worldIn.isClientSide);
		}

		return super.useItemOn(stack, state, worldIn, pos, player, handIn, hit);
	}

	@SuppressWarnings("deprecation") //Don't need the Forge extension
	@Override
	public BlockState rotate(BlockState state, Rotation direction) {
		BlockState newState = state;
		for(Direction dir : Direction.values())
			newState = newState.setValue(property(dir), state.getValue(property(direction.rotate(dir))));
		return newState;
	}

	@NotNull
	@Override
	public BlockState mirror(@NotNull BlockState state, @NotNull Mirror mirror) {
		BlockState newState = state;
		for(Direction dir : Direction.values())
			newState = newState.setValue(property(dir), state.getValue(property(mirror.mirror(dir))));
		return newState;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		for(var d : Direction.values()) {
			// Very ugly. I'm too lazy to figure out a better solution. Checking the given dir would work for normal pipes but not for encased ones
			PipeBlockEntity.ConnectionType type = PipeBlockEntity.computeConnectionTo(level, pos, d);
			boolean fullConnection = allowsFullConnection(type);
			state = state.setValue(MiscUtil.directionProperty(d), fullConnection);
		}
		// This also shouldn't be here but again gives issues with encased pipes
		if(level.getBlockEntity(pos) instanceof PipeBlockEntity tile) {
			tile.refreshVisualConnections();
		}
		return state;
	}

	@Override
	public void neighborChanged(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos,
								@NotNull Block pNeighborBlock, @NotNull BlockPos pNeighborPos, boolean pMovedByPiston) {
		super.neighborChanged(pState, pLevel, pPos, pNeighborBlock, pNeighborPos, pMovedByPiston);
		if(pLevel.getBlockEntity(pPos) instanceof PipeBlockEntity tile) {
			tile.refreshVisualConnections();
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return getTargetState(context.getLevel(), context.getClickedPos());
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
		refreshVisualConnections(level, pos);
		super.setPlacedBy(level, pos, state, entity, stack);
	}

	@Override
	public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston) {
		refreshVisualConnections(pLevel, pPos);
	}

	public void refreshVisualConnections(Level pLevel, BlockPos pPos) {
		if (pLevel.getBlockEntity(pPos) instanceof PipeBlockEntity tile) {
			tile.refreshVisualConnections();
		}
	}

	// Update all connections in blockstate
	protected BlockState getTargetState(LevelAccessor level, BlockPos pos) {
		BlockState newState = defaultBlockState();

		for(Direction facing : Direction.values()) {
			PipeBlockEntity.ConnectionType type = PipeBlockEntity.computeConnectionTo(level, pos, facing);

			newState = newState.setValue(MiscUtil.directionProperty(facing), allowsFullConnection(type));
		}

		return newState;
	}

	public static boolean isConnected(BlockState state, Direction side) {
		return state.getValue(MiscUtil.directionProperty(side));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(UP, DOWN, NORTH, SOUTH, WEST, EAST);
	}

	@Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(@NotNull BlockState blockState, Level worldIn, @NotNull BlockPos pos) {
		BlockEntity tile = worldIn.getBlockEntity(pos);
		if(tile instanceof PipeBlockEntity pipe)
			return pipe.getComparatorOutput();
		return 0;
	}

	@Override
	public void onRemove(@NotNull BlockState state, Level worldIn, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
		BlockEntity be = worldIn.getBlockEntity(pos);

		if(be instanceof PipeBlockEntity pipe)
			pipe.dropAllItems();

		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new PipeBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level world, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
		return createTickerHelper(type, PipesModule.blockEntityType, PipeBlockEntity::tick);
	}
}
