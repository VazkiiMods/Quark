package vazkii.quark.content.tweaks.module;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.zeta.util.ItemNBTHelper;
import vazkii.quark.base.module.LoadModule;
import vazkii.quark.base.module.QuarkModule;
import vazkii.quark.base.module.hint.Hint;

@LoadModule(category = "tweaks", hasSubscriptions = true)
public class ShulkerPackingModule extends QuarkModule {

	@Hint Item shulker_shell = Items.SHULKER_SHELL;
	
	@SubscribeEvent // It's like fedex but eldritch
	public void callFedEnd(PlayerInteractEvent.RightClickBlock event) {
		BlockPos pos = event.getHitVec().getBlockPos();
		Player player = event.getEntity();

		if (player.isShiftKeyDown()) {
			ItemStack mainHand = player.getMainHandItem();
			ItemStack offHand = player.getOffhandItem();
			if (mainHand.is(Items.SHULKER_SHELL) && offHand.is(Items.SHULKER_SHELL)) {
				Level level = player.getLevel();
				BlockState state = level.getBlockState(pos);
				if (state.is(Tags.Blocks.CHESTS) && !state.is(Blocks.ENDER_CHEST)) {
					event.setCanceled(true);
					event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
					if (level.isClientSide())
						return;

					ItemStack newShulkerBox = new ItemStack(Blocks.SHULKER_BOX);
					ShulkerBoxBlockEntity shulkerBoxData = new ShulkerBoxBlockEntity(pos, Blocks.SHULKER_BOX.defaultBlockState());

					BlockEntity be = level.getBlockEntity(pos);
					if (be instanceof Container container && container.getContainerSize() == shulkerBoxData.getContainerSize()) {
						for (int i = 0; i < container.getContainerSize(); i++) {
							ItemStack inSlot = container.getItem(i);
							if (shulkerBoxData.canPlaceItemThroughFace(i, inSlot, null)) {
								shulkerBoxData.setItem(i, inSlot);
								container.setItem(i, ItemStack.EMPTY);
							}
						}

						if (be instanceof Nameable nameable && nameable.hasCustomName()) {
							Component component = nameable.getCustomName();
							if (component != null) {
								shulkerBoxData.setCustomName(component);
								newShulkerBox.setHoverName(component);
							}
						}

						level.destroyBlock(pos, false, player);

						level.playSound(null, pos, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 1f, 1f);

						player.awardStat(Stats.ITEM_USED.get(Items.SHULKER_SHELL), 2);
						if (!player.getAbilities().instabuild) {
							mainHand.shrink(1);
							offHand.shrink(1);
						}

						ItemNBTHelper.setCompound(newShulkerBox, "BlockEntityTag", shulkerBoxData.saveWithFullMetadata());

						ItemHandlerHelper.giveItemToPlayer(player, newShulkerBox, player.getInventory().selected);
					}
				}
			}
		}
	}
}
