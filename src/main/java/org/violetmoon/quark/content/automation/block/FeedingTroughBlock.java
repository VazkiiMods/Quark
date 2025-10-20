package org.violetmoon.quark.content.automation.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.common.util.DeferredSoundType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.content.automation.block.be.FeedingTroughBlockEntity;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;

/**
 * @author WireSegal
 *         Created at 9:39 AM on 9/20/19.
 */
public class FeedingTroughBlock extends ZetaBlock implements EntityBlock {

	private static final SoundType WOOD_WITH_PLANT_STEP = new DeferredSoundType(1.0F, 1.0F, () -> SoundEvents.WOOD_BREAK, () -> SoundEvents.GRASS_STEP, () -> SoundEvents.WOOD_PLACE, () -> SoundEvents.WOOD_HIT, () -> SoundEvents.WOOD_FALL);

	public static final BooleanProperty FULL = BooleanProperty.create("full");

	public static final VoxelShape CUBOID_SHAPE = box(0, 0, 0, 16, 8, 16);
	public static final VoxelShape EMPTY_SHAPE = Shapes.join(CUBOID_SHAPE, box(2, 2, 2, 14, 8, 14), BooleanOp.ONLY_FIRST);
	public static final VoxelShape FULL_SHAPE = Shapes.join(CUBOID_SHAPE, box(2, 6, 2, 14, 8, 14), BooleanOp.ONLY_FIRST);
	public static final VoxelShape ANIMAL_SHAPE = box(0, 0, 0, 16, 24, 16);

	public FeedingTroughBlock(String regname, @Nullable ZetaModule module, Properties properties) {
		super(regname, module, properties);
		registerDefaultState(defaultBlockState().setValue(FULL, false));

		if(module == null) //auto registration below this line
			return;
		setCreativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS, Blocks.COMPOSTER, true);
	}

	@NotNull
	@Override
	public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		Entity entity = context instanceof EntityCollisionContext ecc ? ecc.getEntity() : null;
		if(entity instanceof Animal)
			return ANIMAL_SHAPE;

		return EMPTY_SHAPE;
	}

	@NotNull
	@Override
	public VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
		return CUBOID_SHAPE;
	}

	@NotNull
	@Override
	public VoxelShape getShape(BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return state.getValue(FULL) ? FULL_SHAPE : EMPTY_SHAPE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FULL);
	}

	@Override
	public SoundType getSoundType(BlockState state, LevelReader level, BlockPos pos, @Nullable Entity entity) {
		if(state.getValue(FULL)) return WOOD_WITH_PLANT_STEP;
		return super.getSoundType(state, level, pos, entity);
	}

	@Override
	public void fallOn(Level level, @NotNull BlockState state, @NotNull BlockPos pos, @NotNull Entity entity, float distance) {
		if(level.getBlockState(pos).getValue(FULL))
			entity.causeFallDamage(distance, 0.2F, level.damageSources().fall());
		else
			super.fallOn(level, state, pos, entity, distance);
	}

	@Override
	public void onRemove(BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			BlockEntity tile = world.getBlockEntity(pos);
			if(tile instanceof FeedingTroughBlockEntity f) {
				Containers.dropContents(world, pos, f);
				world.updateNeighbourForOutputSignal(pos, this);
			}

			super.onRemove(state, world, pos, newState, isMoving);
		}
	}

	@Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(@NotNull BlockState state, Level world, @NotNull BlockPos pos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(world.getBlockEntity(pos));
	}

	@NotNull
	@Override
	public InteractionResult useWithoutItem(@NotNull BlockState state, Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult trace) {
		if(world.isClientSide)
			return InteractionResult.SUCCESS;
		else {
			MenuProvider container = this.getMenuProvider(state, world, pos);
			if(container != null)
				player.openMenu(container);

			return InteractionResult.CONSUME;
		}
	}

	@Override
	public boolean triggerEvent(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, int id, int param) {
		super.triggerEvent(state, world, pos, id, param);
		BlockEntity tile = world.getBlockEntity(pos);
		return tile != null && tile.triggerEvent(id, param);
	}

	@Override
	@Nullable
	public MenuProvider getMenuProvider(@NotNull BlockState state, Level world, @NotNull BlockPos pos) {
		BlockEntity tile = world.getBlockEntity(pos);
		return tile instanceof MenuProvider m ? m : null;
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new FeedingTroughBlockEntity(pos, state);
	}

}
