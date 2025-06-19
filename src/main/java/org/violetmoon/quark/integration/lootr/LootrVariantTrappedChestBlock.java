package org.violetmoon.quark.integration.lootr;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import noobanidus.mods.lootr.common.block.entity.LootrChestBlockEntity;
import noobanidus.mods.lootr.neoforge.config.ConfigManager;
import noobanidus.mods.lootr.util.ChestUtil;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.content.building.block.VariantTrappedChestBlock;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.registry.IZetaBlockItemProvider;

import java.util.function.Supplier;

/**
 * Copy of
 * https://github.com/noobanidus/Lootr/blob/ded29b761ebf271f53a1b976cf859e0f4bfc8d60/src/main/java/noobanidus/mods/lootr/block/LootrVariantTrappedChestBlock.java
 * All modifications are made purely to integrate with VariantTrappedChestBlock/quark
 */
public class LootrVariantTrappedChestBlock extends VariantTrappedChestBlock implements IZetaBlockItemProvider {
	public LootrVariantTrappedChestBlock(String type, ZetaModule module, Supplier<BlockEntityType<? extends ChestBlockEntity>> supplier, Properties properties) {
		super("lootr", type, module, supplier, properties.strength(2.5f));
	}

	// BEGIN LOOTR COPY

	@Override
	public float getExplosionResistance() {
		if(ConfigManager.BLAST_IMMUNE.get()) {
			return Float.MAX_VALUE;
		} else if(ConfigManager.BLAST_RESISTANT.get()) {
			return 16.0f;
		} else {
			return super.getExplosionResistance();
		}
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState state) {
		return new LootrVariantTrappedChestBlockEntity(pPos, state); // Modified
	}

	@Override
	public boolean isSignalSource(BlockState pState) {
		return true;
	}

	@Override
	public int getSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
		return Mth.clamp(LootrChestBlockEntity.getOpenCount(pBlockAccess, pPos), 0, 15);
	}

	@Override
	public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
		return pSide == Direction.UP ? pBlockState.getSignal(pBlockAccess, pPos, pSide) : 0;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult trace) {
		if(player.isShiftKeyDown()) {
			ChestUtil.handleLootSneak(this, world, pos, player);
		} else if(!ChestBlock.isChestBlockedAt(world, pos)) {
			ChestUtil.handleLootChest(this, world, pos, player);
		}
		return InteractionResult.sidedSuccess(world.isClientSide);
	}

	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		if(stateIn.getValue(WATERLOGGED)) {
			worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
		}

		return stateIn;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AABB;
	}

	@Override
	@Nullable
	public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
		return null;
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? LootrChestBlockEntity::lootrLidAnimateTick : null;
	}

	// END LOOTR COPY

	@Override
	public BlockItem provideItemBlock(Block block, LootrVariantChestBlock.Item.Properties props) {
		return new LootrVariantChestBlock.Item(block, props, true);
	}
}
