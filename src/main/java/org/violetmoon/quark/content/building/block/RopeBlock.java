package org.violetmoon.quark.content.building.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.content.automation.module.PistonsMoveTileEntitiesModule;
import org.violetmoon.quark.content.building.module.RopeModule;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.item.ZetaBlockItem;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.IZetaBlockItemProvider;
import org.violetmoon.zeta.registry.RenderLayerRegistry;

public class RopeBlock extends ZetaBlock implements IZetaBlockItemProvider, SimpleWaterloggedBlock {

	private static final VoxelShape SHAPE = box(6, 0, 6, 10, 16, 10);

	public static BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public RopeBlock(String regname, @Nullable ZetaModule module, Properties properties) {
		super(regname, module, properties);

		registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));

		if(module == null) //auto registration below this line
			return;
		module.zeta().renderLayerRegistry.put(this, RenderLayerRegistry.Layer.CUTOUT);
		setCreativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS, Blocks.CHAIN, true);
	}

	@Override
	public BlockItem provideItemBlock(Block block, Item.Properties properties) {
		return new ZetaBlockItem(block, properties) {
			@Override
			public boolean doesSneakBypassUseZeta(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
				return world.getBlockState(pos).getBlock() instanceof RopeBlock;
			}
		};
	}

	@NotNull
	@Override
	public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return Shapes.empty();
	}

	@NotNull
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
		return !state.getValue(WATERLOGGED);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
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
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {
		if (player.getMainHandItem().getItem() != asItem() && pullUp(level, pos)) {
			if(!player.getAbilities().instabuild) {
				if(!player.addItem(new ItemStack(this)))
					player.drop(new ItemStack(this), false);
			}
			level.playSound(null, pos, soundType.getBreakSound(), SoundSource.BLOCKS, 0.5F, 1F);
			return InteractionResult.sidedSuccess(level.isClientSide);
		}
		return InteractionResult.PASS;
	}

	@NotNull
	@Override
	public ItemInteractionResult useItemOn(ItemStack stack, @NotNull BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
		if(hand == InteractionHand.MAIN_HAND) {
			if(stack.getItem() == asItem() && !player.isDiscrete()) {
				if(pullDown(worldIn, pos)) {
					if(!player.getAbilities().instabuild)
						stack.shrink(1);

					worldIn.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS, 0.5F, 1F);
					return ItemInteractionResult.sidedSuccess(worldIn.isClientSide);
				}
			} else if(FluidUtil.getFluidHandler(stack).isPresent()) { //TODO: Forge extension
				FluidActionResult interact = FluidUtil.tryPickUpFluid(stack, player, worldIn, getBottomPos(worldIn, pos), Direction.UP);
				if(interact.success) {
					stack.shrink(1);
					if(!player.addItem(interact.result))
						player.drop(interact.result, false);
				}
				
				return interact.success ? ItemInteractionResult.sidedSuccess(worldIn.isClientSide) : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			} else if(stack.getItem() == Items.GLASS_BOTTLE) {
				BlockPos bottomPos = getBottomPos(worldIn, pos);
				BlockState stateAt = worldIn.getBlockState(bottomPos);
				if(stateAt.getFluidState().is(Fluids.WATER)) {
					Vec3 playerPos = player.position();
					worldIn.playSound(player, playerPos.x, playerPos.y, playerPos.z, SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
					stack.shrink(1);
					ItemStack bottleStack = PotionContents.createItemStack(Items.POTION, Potions.WATER);
					player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

					if(stack.isEmpty())
						player.setItemInHand(hand, bottleStack);
					else if(!player.addItem(bottleStack))
						player.drop(bottleStack, false);

					return ItemInteractionResult.sidedSuccess(worldIn.isClientSide);
				}
				return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
			} else {
				if(pullUp(worldIn, pos)) {
					if(!player.getAbilities().instabuild) {
						if(!player.addItem(new ItemStack(this)))
							player.drop(new ItemStack(this), false);
					}

					worldIn.playSound(null, pos, soundType.getBreakSound(), SoundSource.BLOCKS, 0.5F, 1F);
					return ItemInteractionResult.sidedSuccess(worldIn.isClientSide);
				}
			}
		}
		return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	public boolean pullUp(Level world, BlockPos pos) {
		BlockPos basePos = pos;

		while(true) {
			pos = pos.below();
			BlockState state = world.getBlockState(pos);
			if(state.getBlock() != this)
				break;
		}

		BlockPos ropePos = pos.above();
		if(ropePos.equals(basePos))
			return false;

		world.setBlockAndUpdate(ropePos, world.getBlockState(ropePos).getFluidState().createLegacyBlock());
		moveBlock(world, pos, ropePos);

		return true;
	}

	public boolean pullDown(Level world, BlockPos pos) {
		boolean can;
		boolean endRope = false;
		boolean wasAirAtEnd = false;

		do {
			pos = pos.below();
			if(!world.isInWorldBounds(pos))
				return false;

			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			if(block == this)
				continue;

			if(endRope) {
				can = wasAirAtEnd || world.isEmptyBlock(pos) || state.canBeReplaced();
				break;
			}

			endRope = true;
			wasAirAtEnd = world.isEmptyBlock(pos) || world.getBlockState(pos).getBlock() instanceof LiquidBlock;
		} while(true);

		if(can) {
			BlockPos ropePos = pos.above();
			moveBlock(world, ropePos, pos);

			BlockState ropePosState = world.getBlockState(ropePos);

			if(world.isEmptyBlock(ropePos) || ropePosState.canBeReplaced()) {
				world.setBlockAndUpdate(ropePos, defaultBlockState().setValue(WATERLOGGED, ropePosState.getFluidState().getType() == Fluids.WATER));
				return true;
			}
		}

		return false;
	}

	private BlockPos getBottomPos(Level worldIn, BlockPos pos) {
		Block block = this;
		while(block == this) {
			pos = pos.below();
			BlockState state = worldIn.getBlockState(pos);
			block = state.getBlock();
		}

		return pos;

	}

	// mojang tag pls
	private boolean isIllegalBlock(Block block) {
		return block == Blocks.OBSIDIAN || block == Blocks.CRYING_OBSIDIAN || block == Blocks.RESPAWN_ANCHOR;
	}

	private void moveBlock(Level world, BlockPos srcPos, BlockPos dstPos) {
        if (world.isClientSide)
            return;

		BlockState state = world.getBlockState(srcPos);
        //if (!state.getFluidState().is(Fluids.EMPTY)) return;

		Block block = state.getBlock();

		if(state.getDestroySpeed(world, srcPos) == -1 || !state.canSurvive(world, dstPos) || state.isAir() || state.getBlock() instanceof LiquidBlock ||
				state.getPistonPushReaction() != PushReaction.NORMAL || isIllegalBlock(block))
			return;

		BlockEntity tile = world.getBlockEntity(srcPos);
		if(tile != null) {
			if(RopeModule.forceEnableMoveTileEntities ? PistonsMoveTileEntitiesModule.shouldMoveTE(state) : PistonsMoveTileEntitiesModule.shouldMoveTE(true, state))
				return;

			tile.setRemoved();
		}



		FluidState fluidState = world.getFluidState(srcPos);
		world.setBlockAndUpdate(srcPos, fluidState.createLegacyBlock());

		BlockState nextState = Block.updateFromNeighbourShapes(state, world, dstPos);
		if(nextState.getProperties().contains(BlockStateProperties.WATERLOGGED))
			nextState = nextState.setValue(BlockStateProperties.WATERLOGGED, world.getFluidState(dstPos).getType() == Fluids.WATER);

        world.setBlockAndUpdate(dstPos, nextState);

        // You gotta write the block entity data AFTER you move the block.
        if(tile != null) {
			BlockEntity target = BlockEntity.loadStatic(dstPos, nextState, tile.saveWithFullMetadata(world.registryAccess()).copy(), world.registryAccess());
			if(target != null) {
				world.setBlockEntity(target);
                //world.setBlockAndUpdate(dstPos, state);
				target.setChanged();
			}
		}

        nextState.handleNeighborChanged(world, dstPos, nextState.getBlock(), srcPos, true);

        world.updateNeighborsAt(dstPos, state.getBlock());
        for (Entity entity : world.getEntities(null, new AABB(srcPos.above()))) {
            entity.teleportRelative(0 ,dstPos.getY() - srcPos.getY(), 0);
        }

    }

	@Override
	public boolean canSurvive(@NotNull BlockState state, LevelReader worldIn, BlockPos pos) {
		BlockPos upPos = pos.above();
		BlockState upState = worldIn.getBlockState(upPos);
		return upState.getBlock() == this || upState.isFaceSturdy(worldIn, upPos, Direction.DOWN);
	}

	@Override
	public void neighborChanged(BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos, boolean isMoving) {
		if(!state.canSurvive(worldIn, pos)) {
			worldIn.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(worldIn.getBlockState(pos)));
			dropResources(state, worldIn, pos);
			worldIn.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
		}
	}

	@Override
	public boolean isLadderZeta(BlockState state, LevelReader world, BlockPos pos, LivingEntity entity) {
		return true;
	}

	@NotNull
	@Override
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return SHAPE;
	}

	@Override
	public int getFlammabilityZeta(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return 30;
	}

	@Override
	public int getFireSpreadSpeedZeta(BlockState state, BlockGetter world, BlockPos pos, Direction face) {
		return 60;
	}

}
