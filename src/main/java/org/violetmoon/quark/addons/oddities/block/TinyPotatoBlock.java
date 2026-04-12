package org.violetmoon.quark.addons.oddities.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.addons.oddities.block.be.TinyPotatoBlockEntity;
import org.violetmoon.quark.addons.oddities.item.TinyPotatoBlockItem;
import org.violetmoon.quark.addons.oddities.module.TinyPotatoModule;
import org.violetmoon.quark.base.components.QuarkDataComponents;
import org.violetmoon.zeta.block.OldMaterials;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.IZetaBlockItemProvider;

/**
 * @author WireSegal
 *         Created at 10:16 AM on 3/14/22.
 */
public class TinyPotatoBlock extends ZetaBlock implements SimpleWaterloggedBlock, EntityBlock, IZetaBlockItemProvider {

	public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private static final VoxelShape SHAPE = box(6, 0, 6, 10, 6, 10);

	public static final String ANGRY = "angery";

	public static boolean isAngry(ItemStack stack) {
		return Boolean.TRUE.equals(stack.get(QuarkDataComponents.IS_ANGRY));
	}

	public TinyPotatoBlock(@Nullable ZetaModule module) {
		super("tiny_potato", module,
				OldMaterials.wool().strength(0.25F));
		registerDefaultState(defaultBlockState()
				.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH));

		if(module == null) //auto registration below this line
			return;
		setCreativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS);
	}

	@Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
	}

	@Override
	protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HORIZONTAL_FACING, WATERLOGGED);
	}

	@NotNull
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public void onRemove(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
		if(!state.is(newState.getBlock())) {
			BlockEntity be = world.getBlockEntity(pos);
			if(be instanceof TinyPotatoBlockEntity inventory) {
				Containers.dropContents(world, pos, inventory);
			}
			super.onRemove(state, world, pos, newState, isMoving);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		ItemStack stack = super.getCloneItemStack(level, pos, state);
		BlockEntity be = level.getBlockEntity(pos);
		if(be instanceof TinyPotatoBlockEntity tater) {
			if(tater.hasCustomName())
				stack.set(DataComponents.CUSTOM_NAME, tater.getCustomName());

			if(tater.angry)
				stack.set(QuarkDataComponents.IS_ANGRY, true);
		}
		return stack;
	}

	@NotNull
	@Override
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext ctx) {
		return SHAPE;
	}


	@NotNull
	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		BlockEntity be = world.getBlockEntity(pos);
		if(be instanceof TinyPotatoBlockEntity tater) {
			tater.interact(player, hand, player.getItemInHand(hand), hit.getDirection());

			if(player instanceof ServerPlayer sp)
				TinyPotatoModule.patPotatoTrigger.trigger(sp);

			if(world instanceof ServerLevel serverLevel) {
				AABB box = SHAPE.bounds();
				serverLevel.sendParticles(ParticleTypes.HEART, pos.getX() + box.minX + Math.random() * (box.maxX - box.minX), pos.getY() + box.maxY, pos.getZ() + box.minZ + Math.random() * (box.maxZ - box.minZ), 1, 0, 0, 0, 0);
			}
		}
		return ItemInteractionResult.sidedSuccess(world.isClientSide);
	}

	@NotNull
	@Override
	public BlockState getStateForPlacement(@NotNull BlockPlaceContext ctx) {
		return defaultBlockState()
				.setValue(HORIZONTAL_FACING, ctx.getHorizontalDirection().getOpposite())
				.setValue(WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER);
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
	public void setPlacedBy(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity living, ItemStack stack) {
		boolean hasCustomName = stack.has(DataComponents.CUSTOM_NAME);
		boolean isAngry = isAngry(stack);
		if(hasCustomName || isAngry) {
			BlockEntity be = world.getBlockEntity(pos);
			if(be instanceof TinyPotatoBlockEntity tater) {
				if(hasCustomName)
					tater.name = stack.getHoverName();
				tater.angry = isAngry(stack);
			}
		}
	}

	@Override
	public boolean triggerEvent(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, int id, int param) {
		super.triggerEvent(state, world, pos, id, param);
		BlockEntity tile = world.getBlockEntity(pos);
		return tile != null && tile.triggerEvent(id, param);
	}

	@Override
	public BlockItem provideItemBlock(Block block, Item.Properties properties) {
		return new TinyPotatoBlockItem(block, properties);
	}

	@NotNull
	@Override
	public RenderShape getRenderShape(@NotNull BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@NotNull
	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new TinyPotatoBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
		return createTickerHelper(type, TinyPotatoModule.blockEntityType, TinyPotatoBlockEntity::commonTick);
	}
}
