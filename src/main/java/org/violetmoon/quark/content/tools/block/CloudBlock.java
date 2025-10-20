package org.violetmoon.quark.content.tools.block;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.content.tools.block.be.CloudBlockEntity;
import org.violetmoon.quark.content.tools.module.BottledCloudModule;
import org.violetmoon.zeta.block.ZetaBlock;
import org.violetmoon.zeta.module.ZetaModule;

public class CloudBlock extends ZetaBlock implements EntityBlock {

	public CloudBlock(@Nullable ZetaModule module) {
		super("cloud", module,
				Block.Properties.of()
						.mapColor(MapColor.CLAY)
						.sound(SoundType.WOOL)
						.strength(0)
						.noOcclusion()
						.noCollission()
		);
	}

	@NotNull
	@Override
	public PushReaction getPistonPushReaction(@NotNull BlockState state) {
		return PushReaction.BLOCK;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		if(stack.getItem() == Items.GLASS_BOTTLE) {
			fillBottle(player, player.getInventory().selected);
			level.removeBlock(pos, false);
			return ItemInteractionResult.sidedSuccess(level.isClientSide);
		}

		if(stack.getItem() instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();

			UseOnContext context = new UseOnContext(player, hand, new BlockHitResult(new Vec3(0.5F, 1F, 0.5F), result.getDirection(), pos, false));
			BlockPlaceContext bcontext = new BlockPlaceContext(context);

			BlockState stateToPlace = block.getStateForPlacement(bcontext);
			if(stateToPlace != null && stateToPlace.canSurvive(level, pos)) {
				level.setBlockAndUpdate(pos, stateToPlace);
				level.playSound(player, pos, stateToPlace.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1F, 1F);

				if(!player.getAbilities().instabuild) {
					stack.shrink(1);
					fillBottle(player, 0);
				}
				return ItemInteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		return ItemInteractionResult.FAIL;
	}

	@SuppressWarnings("deprecation")
	@Override
	public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
		return new ItemStack(BottledCloudModule.bottled_cloud);
	}

	private void fillBottle(Player player, int startIndex) {
		Inventory inv = player.getInventory();
		for(int i = startIndex; i < inv.getContainerSize(); i++) {
			ItemStack stackInSlot = inv.getItem(i);
			if(stackInSlot.getItem() == Items.GLASS_BOTTLE) {
				stackInSlot.shrink(1);

				ItemStack give = new ItemStack(BottledCloudModule.bottled_cloud);
				if(!player.addItem(give))
					player.drop(give, false);
				return;
			}
		}
	}

	@Override
	public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
		return new CloudBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level world, @NotNull BlockState state, @NotNull BlockEntityType<T> type) {
		return createTickerHelper(type, BottledCloudModule.blockEntityType, CloudBlockEntity::tick);
	}
}
