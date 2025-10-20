package org.violetmoon.quark.addons.oddities.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.violetmoon.quark.addons.oddities.block.be.MagnetizedBlockBlockEntity;
import org.violetmoon.quark.addons.oddities.module.MagnetsModule;
import org.violetmoon.zeta.block.OldMaterials;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.Collections;
import java.util.List;

/**
 * @author WireSegal
 *         Created at 3:05 PM on 2/26/20.
 */
public class MovingMagnetizedBlock extends ZetaBlock implements EntityBlock {
	public static final DirectionProperty FACING = PistonHeadBlock.FACING;

	public MovingMagnetizedBlock(@Nullable ZetaModule module) {
		super("magnetized_block", module, OldMaterials.piston().strength(-1.0F).dynamicShape().noLootTable().noOcclusion());
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@NotNull
	@Override
	public RenderShape getRenderShape(@NotNull BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public void onRemove(BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull BlockState newState, boolean isMoving) {
		if(state.getBlock() != newState.getBlock()) {
			MagnetizedBlockBlockEntity tile = getMagnetTileEntity(worldIn, pos);
			if(tile != null)
				tile.clearMagnetTileEntity();
		}
	}

	@Override
	public boolean useShapeForLightOcclusion(@NotNull BlockState state) {
		return true;
	}

	@NotNull
	@Override
	public InteractionResult useWithoutItem(@NotNull BlockState state, Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
		if (!level.isClientSide && level.getBlockEntity(pos) == null) {
			level.removeBlock(pos, false);
			return InteractionResult.CONSUME;
		} else {
			return InteractionResult.PASS;
		}
	}

	@Override
	@NotNull
	public List<ItemStack> getDrops(@NotNull BlockState state, @NotNull LootParams.Builder builder) {
		MagnetizedBlockBlockEntity tile = this.getMagnetTileEntity(builder.getLevel(), BlockPos.containing(builder.getParameter(LootContextParams.ORIGIN))); // origin
		return tile == null ? Collections.emptyList() : tile.getMagnetState().getDrops(builder);
	}

	@Override
	@NotNull
	public VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	@NotNull
	public VoxelShape getCollisionShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
		MagnetizedBlockBlockEntity tile = this.getMagnetTileEntity(worldIn, pos);
		return tile != null ? tile.getCollisionShape(worldIn, pos) : Shapes.empty();
	}

	@Nullable
	private MagnetizedBlockBlockEntity getMagnetTileEntity(BlockGetter world, BlockPos pos) {
		BlockEntity tile = world.getBlockEntity(pos);
		return tile instanceof MagnetizedBlockBlockEntity ? (MagnetizedBlockBlockEntity) tile : null;
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		return ItemStack.EMPTY;
	}

	@Override
	@NotNull
	public BlockState rotate(@NotNull BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	@NotNull
	public BlockState mirror(@NotNull BlockState state, Mirror mirrorIn) {
		return rotate(state, mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType type) {
		return false;
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return null;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level world, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
		return createTickerHelper(type, MagnetsModule.magnetizedBlockType, MagnetizedBlockBlockEntity::tick);
	}
}
