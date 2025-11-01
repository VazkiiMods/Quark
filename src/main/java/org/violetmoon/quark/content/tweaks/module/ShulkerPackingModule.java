package org.violetmoon.quark.content.tweaks.module;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.violetmoon.quark.mixin.mixins.accessor.AccessorBaseContainerBlockEntity;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.play.entity.player.ZRightClickBlock;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;

@ZetaLoadModule(category = "tweaks")
public class ShulkerPackingModule extends ZetaModule {

	@Hint
	Item shulker_shell = Items.SHULKER_SHELL;

	@PlayEvent // It's like fedex but eldritch
	public void callFedEnd(ZRightClickBlock event) {
		BlockPos pos = event.getHitVec().getBlockPos();
		Player player = event.getPlayer();

		if(player.isShiftKeyDown()) {
			ItemStack mainHand = player.getMainHandItem();
			ItemStack offHand = player.getOffhandItem();
			if(mainHand.is(Items.SHULKER_SHELL) && offHand.is(Items.SHULKER_SHELL)) {
				Level level = player.level();
				BlockState state = level.getBlockState(pos);
				if(state.is(Tags.Blocks.CHESTS) && !state.is(Blocks.ENDER_CHEST)) {
					event.setCanceled(true);
					event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide()));
					if(level.isClientSide())
						return;

					ItemStack newShulkerBox = new ItemStack(Blocks.SHULKER_BOX);
					ShulkerBoxBlockEntity shulkerBoxData = new ShulkerBoxBlockEntity(pos, Blocks.SHULKER_BOX.defaultBlockState());

					BlockEntity be = level.getBlockEntity(pos);
					if(be instanceof Container container && container.getContainerSize() == shulkerBoxData.getContainerSize()) {
                        NonNullList<ItemStack> items = NonNullList.create();
						for(int i = 0; i < container.getContainerSize(); i++) {
							ItemStack inSlot = container.getItem(i);
							if(shulkerBoxData.canPlaceItemThroughFace(i, inSlot, null)) {
								//shulkerBoxData.setItem(i, inSlot);
                                items.add(i, inSlot);
								container.setItem(i, ItemStack.EMPTY);
							}
						}

						if(be instanceof Nameable nameable && nameable.hasCustomName()) {
							Component component = nameable.getCustomName();
							if(component != null) {
								((AccessorBaseContainerBlockEntity)shulkerBoxData).setName(component);
								newShulkerBox.set(DataComponents.CUSTOM_NAME, component);
							}
						}

						level.destroyBlock(pos, false, player);

						level.playSound(null, pos, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 1f, 1f);

						player.awardStat(Stats.ITEM_USED.get(Items.SHULKER_SHELL), 2);
						if(!player.getAbilities().instabuild) {
							mainHand.shrink(1);
							offHand.shrink(1);
						}

						newShulkerBox.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(shulkerBoxData.saveWithFullMetadata(level.registryAccess())));
                        newShulkerBox.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
                        ItemHandlerHelper.giveItemToPlayer(player, newShulkerBox, player.getInventory().selected);
					}
				}
			}
		}
	}
}
