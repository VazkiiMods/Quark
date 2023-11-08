package org.violetmoon.quark.content.management.module;

import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.config.Config;
import org.violetmoon.quark.base.handler.GeneralConfig;
import org.violetmoon.quark.base.handler.SimilarBlockTypeHandler;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.base.network.message.ScrollOnBundleMessage;
import org.violetmoon.quark.content.management.client.screen.HeldShulkerBoxScreen;
import org.violetmoon.quark.content.management.inventory.HeldShulkerBoxContainer;
import org.violetmoon.quark.content.management.inventory.HeldShulkerBoxMenu;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.client.event.play.ZRenderTooltip;
import org.violetmoon.zeta.client.event.play.ZScreen;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;
import org.violetmoon.zeta.util.ItemNBTHelper;
import org.violetmoon.zeta.util.RegistryUtil;

@ZetaLoadModule(category = "management")
public class ExpandedItemInteractionsModule extends ZetaModule {

	@Config
	public static boolean enableArmorInteraction = true;
	@Config(flag = "shulker_box_interaction")
	public static boolean enableShulkerBoxInteraction = true;
	@Config(flag = "lava_interaction")
	public static boolean enableLavaInteraction = true;
	@Config
	public static boolean allowOpeningShulkerBoxes = true;
	@Config(flag = "allow_rotating_bundles")
	public static boolean allowRotatingBundles = true;

	@Hint("lava_interaction") Item lava_bucket = Items.LAVA_BUCKET;
	@Hint(value = "allow_rotating_bundles", key = "rotating_bundles") Item bundle = Items.BUNDLE;
	@Hint(value = "shulker_box_interaction", key = "shulker_box_right_click")
	List<Item> shulkers;

	private static boolean staticEnabled = false;

	public static MenuType<HeldShulkerBoxMenu> heldShulkerBoxMenuType;

	@LoadEvent
	public final void register(ZRegister event) {
		heldShulkerBoxMenuType = IForgeMenuType.create(HeldShulkerBoxMenu::fromNetwork);
		Quark.ZETA.registry.register(heldShulkerBoxMenuType, "held_shulker_box", Registry.MENU_REGISTRY);
	}

	@LoadEvent
	public final void clientSetup(ZClientSetup event) {
		MenuScreens.register(heldShulkerBoxMenuType, HeldShulkerBoxScreen::new);
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = enabled;

		shulkers = RegistryUtil.massRegistryGet(GeneralConfig.shulkerBoxes, Registry.ITEM);
	}

	public static boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
		if (!staticEnabled || action == ClickAction.PRIMARY)
			return false;

		ItemStack stackAt = slot.getItem();
		if (enableShulkerBoxInteraction && shulkerOverride(stack, stackAt, slot, action, player, false)) {
			if (player.containerMenu != null)
				player.containerMenu.slotsChanged(slot.container);
			return true;
		}

		return false;
	}

	public static boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack incoming, Slot slot, ClickAction action, Player player, SlotAccess accessor) {
		if (!staticEnabled || action == ClickAction.PRIMARY)
			return false;

		if (enableLavaInteraction && lavaBucketOverride(stack, incoming, slot, action, player))
			return true;

		if (enableArmorInteraction && armorOverride(stack, incoming, slot, action, player, false))
			return true;

		return enableShulkerBoxInteraction && shulkerOverride(stack, incoming, slot, action, player, true);
	}

	public static void scrollOnBundle(ServerPlayer player, int containerId, int stateId, int slotNum, double scrollDelta) {
		if (!staticEnabled || !allowRotatingBundles)
			return;

		if (-0.1 <= scrollDelta && scrollDelta <= 0.1) return;

		player.resetLastActionTime();
		if (player.containerMenu.containerId == containerId) {
			if (player.isSpectator()) {
				player.containerMenu.sendAllDataToRemote();
			} else if (!player.containerMenu.stillValid(player)) {
				Quark.LOG.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
			} else {
				boolean flag = stateId != player.containerMenu.getStateId();
				player.containerMenu.suppressRemoteUpdates();

				Slot under = player.containerMenu.getSlot(slotNum);
				if (under != null) {
					ItemStack underStack = under.getItem();
					rotateBundle(underStack, scrollDelta);
				}

				player.containerMenu.resumeRemoteUpdates();
				if (flag) {
					player.containerMenu.broadcastFullState();
				} else {
					player.containerMenu.broadcastChanges();
				}
			}
		}
	}

	private static void rotateBundle(ItemStack stack, double scrollDelta) {
		if (stack.is(Items.BUNDLE)) {
			CompoundTag tag = stack.getTag();
			if (tag != null) {
				ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
				if (items.size() > 1) {
					ListTag rotatedItems = new ListTag();
					if (scrollDelta < 0) {
						rotatedItems.add(items.get(items.size() - 1));
						for (int i = 0; i < items.size() - 1; i++)
							rotatedItems.add(items.get(i));
					} else {
						for (int i = 1; i < items.size(); i++)
							rotatedItems.add(items.get(i));
						rotatedItems.add(items.get(0));
					}
					tag.put("Items", rotatedItems);
				}
			}
		}
	}

	private static boolean armorOverride(ItemStack stack, ItemStack incoming, Slot slot, ClickAction action, Player player, boolean simulate) {
		if (incoming.isEmpty()) {
			//disallow stacks with more than one since it would prevent from de stacking
			if (stack.getCount() >1) return false;
			EquipmentSlot equipSlot = null;

			if (stack.getItem() instanceof ArmorItem armor) {
				equipSlot = armor.getSlot();
			} else if (stack.getItem() instanceof ElytraItem)
				equipSlot = EquipmentSlot.CHEST;

			if (equipSlot != null) {
				ItemStack currArmor = player.getItemBySlot(equipSlot);

				if (slot.mayPickup(player) && slot.mayPlace(currArmor))
					if (currArmor.isEmpty() || (!EnchantmentHelper.hasBindingCurse(currArmor) && currArmor != stack)) {
						int index = slot.getSlotIndex();
						if (index < slot.container.getContainerSize()) {
							if (!simulate) {
								player.setItemSlot(equipSlot, stack.copy());

								slot.container.setItem(index, currArmor.copy());
								slot.onQuickCraft(stack, currArmor);
							}
							return true;
						}
					}
			}
		}

		return false;
	}

	private static boolean canTrashItem(ItemStack stack, ItemStack incoming, Slot slot, Player player) {
		return stack.getItem() == Items.LAVA_BUCKET
				&& !incoming.isEmpty()
				&& !player.getAbilities().instabuild
				&& slot.allowModification(player)
				&& slot.mayPlace(stack)
				&& !incoming.getItem().isFireResistant()
				&& !SimilarBlockTypeHandler.isShulkerBox(incoming);
	}

	public static boolean lavaBucketOverride(ItemStack stack, ItemStack incoming, Slot slot, ClickAction action, Player player) {
		if (canTrashItem(stack, incoming, slot, player)) {

			incoming.setCount(0);
			if (!player.level.isClientSide)
				player.level.playSound(null, player.blockPosition(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.25F, 2F + (float) Math.random());

			return true;
		}

		return false;
	}

	public static boolean canOpenShulkerBox(ItemStack stack, ItemStack incoming, Slot slot, Player player) {
		return incoming.isEmpty() &&
				allowOpeningShulkerBoxes &&
				(!player.hasContainerOpen() || player.containerMenu instanceof InventoryMenu) &&
				slot.container == player.getInventory() &&
				SimilarBlockTypeHandler.isShulkerBox(stack) &&
				slot.mayPickup(player);
	}

	private static boolean shulkerOverride(ItemStack shulkerStack, ItemStack incoming, Slot slot, ClickAction action, Player player, boolean isStackedOnMe) {
		//sanity check since some mods like to ignore max shulkerStack size...
		if (shulkerStack.getCount() != 1) return false;

		if (isStackedOnMe && canOpenShulkerBox(shulkerStack, incoming, slot, player)) {
			int lockedSlot = slot.getSlotIndex();
			if(player instanceof ServerPlayer splayer) {
				HeldShulkerBoxContainer container = new HeldShulkerBoxContainer(splayer, lockedSlot);

				NetworkHooks.openScreen(splayer, container, buf -> buf.writeInt(lockedSlot));
			}
			else
				player.playSound(SoundEvents.SHULKER_BOX_OPEN, 1F, 1F);

			return true;
		}

		if (!incoming.isEmpty() && tryAddToShulkerBox(player, shulkerStack, incoming, slot, true, true, isStackedOnMe) != null) {
			ItemStack finished = tryAddToShulkerBox(player, shulkerStack, incoming, slot, false, isStackedOnMe, isStackedOnMe);

			if (finished != null) {
				if (isStackedOnMe) {
					player.playSound(SoundEvents.SHULKER_BOX_OPEN, 0.7F, 1.5F);
					slot.set(finished);
				}
				return true;
			}
		}

		return false;
	}

	public static BlockEntity getShulkerBoxEntity(ItemStack shulkerBox) {
		CompoundTag cmp = ItemNBTHelper.getCompound(shulkerBox, "BlockEntityTag", false);
		if (cmp.contains("LootTable"))
			return null;

		BlockEntity te = null;
		cmp = cmp.copy();
		cmp.putString("id", "minecraft:shulker_box");
		if (shulkerBox.getItem() instanceof BlockItem) {
			Block shulkerBoxBlock = Block.byItem(shulkerBox.getItem());
			BlockState defaultState = shulkerBoxBlock.defaultBlockState();
			if (shulkerBoxBlock instanceof EntityBlock) {
				te = ((EntityBlock) shulkerBoxBlock).newBlockEntity(BlockPos.ZERO, defaultState);
				if (te != null)
					te.load(cmp);
			}
		}

		return te;
	}

	private static ItemStack tryAddToShulkerBox(Player player, ItemStack shulkerBox, ItemStack stack, Slot slot, boolean simulate, boolean useCopy, boolean allowDump) {
		if (!SimilarBlockTypeHandler.isShulkerBox(shulkerBox) || !slot.mayPickup(player))
			return null;

		BlockEntity tile = getShulkerBoxEntity(shulkerBox);

		if (tile != null) {
			LazyOptional<IItemHandler> handlerHolder = tile.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
			if (handlerHolder.isPresent()) {
				IItemHandler handler = handlerHolder.orElseGet(EmptyHandler::new);
				if (SimilarBlockTypeHandler.isShulkerBox(stack) && allowDump) {
					BlockEntity otherShulker = getShulkerBoxEntity(stack);
					if (otherShulker != null) {
						LazyOptional<IItemHandler> otherHolder = otherShulker.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
						if (otherHolder.isPresent()) {
							IItemHandler otherHandler = otherHolder.orElseGet(EmptyHandler::new);
							boolean any = false;
							for (int i = 0; i < otherHandler.getSlots(); i++) {
								ItemStack inserting = otherHandler.extractItem(i, 64, true);
								if (!inserting.isEmpty()) {
									ItemStack result = ItemHandlerHelper.insertItem(handler, inserting, true);
									if (result.isEmpty() || result.getCount() != inserting.getCount()) {
										if (simulate) {
											return shulkerBox;
										} else {
											ItemHandlerHelper.insertItem(handler, otherHandler.extractItem(i, inserting.getCount() - result.getCount(), false), false);

											any = true;
										}
									}
								}
							}

							if (any) {
								ItemStack workStack = useCopy ? shulkerBox.copy() : shulkerBox;

								ItemNBTHelper.setCompound(workStack, "BlockEntityTag", tile.saveWithId());
								ItemNBTHelper.setCompound(stack, "BlockEntityTag", otherShulker.saveWithId());

								if (slot.mayPlace(workStack))
									return workStack;
							}
						}
					}
				}
				ItemStack result = ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
				boolean did = result.isEmpty() || result.getCount() != stack.getCount();

				if (did) {
					ItemStack workStack = useCopy ? shulkerBox.copy() : shulkerBox;
					if (!simulate)
						stack.setCount(result.getCount());

					ItemNBTHelper.setCompound(workStack, "BlockEntityTag", tile.saveWithId());

					if (slot.mayPlace(workStack))
						return workStack;
				}
			}
		}

		return null;
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends ExpandedItemInteractionsModule {
		@PlayEvent
		public void gatherTooltip(ZRenderTooltip.GatherComponents.Low event) {
			if (!enableArmorInteraction && (!enableShulkerBoxInteraction || !allowOpeningShulkerBoxes))
				return;

			Minecraft mc = Minecraft.getInstance();
			Screen gui = mc.screen;
			if (mc.player != null && gui instanceof AbstractContainerScreen<?> containerGui && containerGui.getMenu().getCarried().isEmpty()) {
				Slot under = containerGui.getSlotUnderMouse();
				if (containerGui instanceof CreativeModeInventoryScreen creativeGui && creativeGui.getSelectedTab() != CreativeModeTab.TAB_INVENTORY.getId())
					return;

				if (under != null) {
					ItemStack underStack = under.getItem();

					if (event.getItemStack() == underStack)
						if(enableArmorInteraction && armorOverride(underStack, ItemStack.EMPTY, under, ClickAction.SECONDARY, mc.player, true))
							event.getTooltipElements().add(Either.left(Component.translatable("quark.misc.equip_armor").withStyle(ChatFormatting.YELLOW)));

						else if(enableShulkerBoxInteraction && canOpenShulkerBox(underStack, ItemStack.EMPTY, under, mc.player))
							event.getTooltipElements().add(Either.left(Component.translatable("quark.misc.open_shulker").withStyle(ChatFormatting.YELLOW)));
				}
			}
		}

		@PlayEvent
		public void onDrawScreen(ZScreen.Render.Post event) {
			Minecraft mc = Minecraft.getInstance();
			Screen gui = mc.screen;
			if (mc.player != null && gui instanceof AbstractContainerScreen<?> containerGui) {
				ItemStack held = containerGui.getMenu().getCarried();
				if (!held.isEmpty()) {
					Slot under = containerGui.getSlotUnderMouse();

					if (under != null) {
						ItemStack underStack = under.getItem();

						int x = event.getMouseX();
						int y = event.getMouseY();
						if (enableLavaInteraction && canTrashItem(underStack, held, under, mc.player)) {
							gui.renderComponentTooltip(event.getPoseStack(), List.of(Component.translatable("quark.misc.trash_item").withStyle(ChatFormatting.RED)), x, y);
						} else if (enableShulkerBoxInteraction && tryAddToShulkerBox(mc.player, underStack, held, under, true, true, true) != null) {
							gui.renderComponentTooltip(event.getPoseStack(), List.of(Component.translatable(
									SimilarBlockTypeHandler.isShulkerBox(held) ? "quark.misc.merge_shulker_box" : "quark.misc.insert_shulker_box"
							).withStyle(ChatFormatting.YELLOW)), x, y, underStack);
						} else if (enableShulkerBoxInteraction && SimilarBlockTypeHandler.isShulkerBox(underStack)) {
							gui.renderComponentTooltip(event.getPoseStack(), gui.getTooltipFromItem(underStack), x, y, underStack);
						}
					}

				}
			}
		}

		@PlayEvent
		public void onScroll(ZScreen.MouseScrolled.Pre event) {
			if (!allowRotatingBundles)
				return;

			Minecraft mc = Minecraft.getInstance();
			Screen gui = mc.screen;

			double scrollDelta = event.getScrollDelta();

			if (mc.player != null && gui instanceof AbstractContainerScreen<?> containerGui) {
				Slot under = containerGui.getSlotUnderMouse();
				if (under != null) {
					ItemStack underStack = under.getItem();
					if (underStack.is(Items.BUNDLE)) {
						CompoundTag tag = underStack.getTag();
						if (tag != null) {
							ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
							if (items.size() > 1) {
								var menu = containerGui.getMenu();
								event.setCanceled(true);
								if (scrollDelta < -0.1 || scrollDelta > 0.1) {
									rotateBundle(underStack, scrollDelta);
									QuarkNetwork.sendToServer(new ScrollOnBundleMessage(menu.containerId, menu.getStateId(), under.index, scrollDelta));
								}
							}
						}
					}
				}
			}
		}
	}
}
