package org.violetmoon.quark.addons.oddities.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.addons.oddities.block.be.CrateBlockEntity;
import org.violetmoon.quark.addons.oddities.module.CrateModule;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;

public class CrateBlock extends ZetaBlock implements EntityBlock {

	public static final BooleanProperty PROPERTY_OPEN = BlockStateProperties.OPEN;

	public CrateBlock(@Nullable ZetaModule module) {
		super("crate", module, Properties.ofFullCopy(Blocks.BARREL));
		registerDefaultState(stateDefinition.any().setValue(PROPERTY_OPEN, false));

		if(module == null) //auto registration below this line
			return;
		setCreativeTab(CreativeModeTabs.FUNCTIONAL_BLOCKS, Blocks.BARREL, false);
	}

	@Override
	public boolean hasAnalogOutputSignal(@NotNull BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos) {
		BlockEntity be = world.getBlockEntity(pos);
		if(be instanceof CrateBlockEntity crate) {
			return (int) (Math.floor((crate.displayTotal * 14.0) / crate.getContainerSize()) + (crate.displayTotal > 0 ? 1 : 0));
		}
		return 0;
	}

	@Nullable
	@Override
	public MenuProvider getMenuProvider(@NotNull BlockState state, Level level, @NotNull BlockPos pos) {
		BlockEntity be = level.getBlockEntity(pos);
		return be instanceof MenuProvider provider ? provider : null;
	}

	@NotNull
	@Override
	public InteractionResult useWithoutItem(@NotNull BlockState state, Level worldIn, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
		if(worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			BlockEntity be = worldIn.getBlockEntity(pos);
			if(be instanceof CrateBlockEntity crate) {
				if(player instanceof ServerPlayer serverPlayer)
					serverPlayer.openMenu(crate, pos);

				PiglinAi.angerNearbyPiglins(player, true);
			}

			return InteractionResult.CONSUME;
		}
	}

	@Override
	public void setPlacedBy(@NotNull Level worldIn, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		if(stack.getComponents().has(DataComponents.CUSTOM_NAME)) {
			BlockEntity tileentity = worldIn.getBlockEntity(pos);
			if(tileentity instanceof CrateBlockEntity crate)
				crate.setComponents(stack.getComponents());
		}
	}

	@Override
	public void tick(@NotNull BlockState state, ServerLevel worldIn, @NotNull BlockPos pos, @NotNull RandomSource rand) {
		BlockEntity tileentity = worldIn.getBlockEntity(pos);
		if(tileentity instanceof CrateBlockEntity)
			((CrateBlockEntity) tileentity).crateTick();
	}

	@Override
	public void onRemove(BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
		if(!state.is(newState.getBlock())) {
			BlockEntity tileentity = worldIn.getBlockEntity(pos);

			if(tileentity instanceof CrateBlockEntity crate) {
				crate.spillTheTea();
			}
		}

		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PROPERTY_OPEN);
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new CrateBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level world, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
		return createTickerHelper(type, CrateModule.blockEntityType, CrateBlockEntity::tick);
	}

}
