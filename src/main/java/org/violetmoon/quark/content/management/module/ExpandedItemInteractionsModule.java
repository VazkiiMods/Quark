package org.violetmoon.quark.content.management.module;

import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.component.BundleContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.config.QuarkGeneralConfig;
import org.violetmoon.quark.base.handler.SimilarBlockTypeHandler;
import org.violetmoon.quark.base.network.message.ScrollOnBundleMessage;
import org.violetmoon.quark.content.management.client.screen.HeldShulkerBoxScreen;
import org.violetmoon.quark.content.management.inventory.HeldShulkerBoxContainer;
import org.violetmoon.quark.content.management.inventory.HeldShulkerBoxMenu;
import org.violetmoon.quark.mixin.mixins.client.accessor.AccessorCustomCreativeSlot;
import org.violetmoon.quark.mixin.mixins.client.accessor.AccessorMenuScreens;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.client.event.play.ZRenderTooltip;
import org.violetmoon.zeta.client.event.play.ZScreen;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZRegister;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;
import org.violetmoon.zeta.util.Hint;
import org.violetmoon.zeta.util.RegistryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

	@Hint("lava_interaction")
	Item lava_bucket = Items.LAVA_BUCKET;
	@Hint(value = "allow_rotating_bundles", key = "rotating_bundles")
	Item bundle = Items.BUNDLE;
	@Hint(value = "shulker_box_interaction", key = "shulker_box_right_click")
	List<Item> shulkers;

	private static boolean staticEnabled = false;

	public static MenuType<HeldShulkerBoxMenu> heldShulkerBoxMenuType;

	@LoadEvent
	public final void register(ZRegister event) {
		heldShulkerBoxMenuType = IMenuTypeExtension.create(HeldShulkerBoxMenu::new);
		Quark.ZETA.registry.register(heldShulkerBoxMenuType, "held_shulker_box", Registries.MENU);
	}

	@LoadEvent
	public final void configChanged(ZConfigChanged event) {
		staticEnabled = isEnabled();

		shulkers = RegistryUtil.massRegistryGet(QuarkGeneralConfig.shulkerBoxes, BuiltInRegistries.ITEM);
	}

	public static boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
		if(!staticEnabled || action == ClickAction.PRIMARY)
			return false;

		ItemStack stackAt = slot.getItem();
		if(enableShulkerBoxInteraction && shulkerOverride(stack, stackAt, slot, action, player, false)) {
			if(player.containerMenu != null)
				player.containerMenu.slotsChanged(slot.container);
			return true;
		}

		return false;
	}

	public static boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack incoming, Slot slot, ClickAction action, Player player, SlotAccess accessor) {
		if(!staticEnabled || action == ClickAction.PRIMARY)
			return false;

		if(enableLavaInteraction && lavaBucketOverride(stack, incoming, slot, action, player))
			return true;

		if(enableArmorInteraction && armorOverride(stack, incoming, slot, action, player, false))
			return true;

		return enableShulkerBoxInteraction && shulkerOverride(stack, incoming, slot, action, player, true);
	}

	public static void scrollOnBundle(ServerPlayer player, int containerId, int stateId, int slotNum, double scrollDelta) {
		if(!staticEnabled || !allowRotatingBundles)
			return;

		if(-0.1 <= scrollDelta && scrollDelta <= 0.1)
			return;

		player.resetLastActionTime();
		if(player.containerMenu.containerId == containerId) {
			if(player.isSpectator()) {
				player.containerMenu.sendAllDataToRemote();
			} else if(!player.containerMenu.stillValid(player)) {
				Quark.LOG.debug("Player {} interacted with invalid menu {}", player, player.containerMenu);
			} else {
				boolean flag = stateId != player.containerMenu.getStateId();
				player.containerMenu.suppressRemoteUpdates();

				Slot under = player.containerMenu.getSlot(slotNum);
				if(under != null) {
					ItemStack underStack = under.getItem();
					rotateBundle(underStack, scrollDelta);
				}

				player.containerMenu.resumeRemoteUpdates();
				if(flag) {
					player.containerMenu.broadcastFullState();
				} else {
					player.containerMenu.broadcastChanges();
				}
			}
		}
	}

	private static void rotateBundle(ItemStack stack, double scrollDelta) {
		if(stack.is(Items.BUNDLE)) {
			BundleContents bundleContents = stack.get(DataComponents.BUNDLE_CONTENTS);
			if(bundleContents != null) {
				List<ItemStack> items = (List<ItemStack>) bundleContents.items();
				if(items.size() > 1) {
					List<ItemStack> rotatedItems = new ArrayList<>();
					if(scrollDelta < 0) {
						rotatedItems.add(items.get(items.size() - 1));
						for(int i = 0; i < items.size() - 1; i++)
							rotatedItems.add(items.get(i));
					} else {
						for(int i = 1; i < items.size(); i++)
							rotatedItems.add(items.get(i));
						rotatedItems.add(items.get(0));
					}
					stack.set(DataComponents.BUNDLE_CONTENTS, new BundleContents(rotatedItems));
				}
			}
		}
	}

	private static boolean armorOverride(ItemStack stack, ItemStack incoming, Slot slot, ClickAction action, Player player, boolean simulate) {
		if(incoming.isEmpty() && !player.isSpectator()) {
			//disallow stacks with more than one since it would prevent from de stacking
			if(stack.getCount() > 1)
				return false;
			EquipmentSlot equipSlot = null;

			if(stack.getItem() instanceof ArmorItem armor) {
				equipSlot = armor.getEquipmentSlot();
			} else if(stack.getItem() instanceof ElytraItem)
				equipSlot = EquipmentSlot.CHEST;

			if(equipSlot != null) {
				ItemStack currArmor = player.getItemBySlot(equipSlot);

                if (slot.mayPickup(player)) {
                    if (slot.mayPlace(currArmor) || (currArmor.isEmpty())) {
                        if (currArmor.isEmpty() || (!(EnchantmentHelper.getTagEnchantmentLevel(player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.BINDING_CURSE), currArmor) > 0)
								&& currArmor != stack)) {
                            if (!simulate) {
                                player.setItemSlot(equipSlot, stack.copy());
                                if ((currArmor.isEmpty())) slot.remove(1); // Added to allow people to grab from Forge component slots while not wearing an item. God Forge is weird.
                                else slot.set(currArmor.copy());

                                slot.onQuickCraft(stack, currArmor);
								slot.onTake(player, stack);
                            }
                            return true;
                        }
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
				&& !incoming.has(DataComponents.FIRE_RESISTANT)
				&& !SimilarBlockTypeHandler.isShulkerBox(incoming);
	}

	public static boolean lavaBucketOverride(ItemStack stack, ItemStack incoming, Slot slot, ClickAction action, Player player) {
		if(canTrashItem(stack, incoming, slot, player)) {

			incoming.setCount(0);
			if(!player.level().isClientSide)
				player.level().playSound(null, player.blockPosition(), SoundEvents.LAVA_EXTINGUISH, SoundSource.PLAYERS, 0.25F, 2F + (float) Math.random());

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
		if(shulkerStack.getCount() != 1)
			return false;

		if(isStackedOnMe && canOpenShulkerBox(shulkerStack, incoming, slot, player)) {
			int lockedSlot = slot.getSlotIndex();
			if(player instanceof ServerPlayer splayer) {
				HeldShulkerBoxContainer container = new HeldShulkerBoxContainer(splayer, lockedSlot);
				player.openMenu(container, packet -> packet.writeInt(lockedSlot));
			} else
				player.playSound(SoundEvents.SHULKER_BOX_OPEN, 1F, 1F);

			return true;
		}

		if(!incoming.isEmpty() && tryAddToShulkerBox(player, shulkerStack, incoming, slot, true, true, isStackedOnMe) != null) {
			ItemStack finished = tryAddToShulkerBox(player, shulkerStack, incoming, slot, false, isStackedOnMe, isStackedOnMe);

			if(finished != null) {
				if(isStackedOnMe) {
					player.playSound(SoundEvents.SHULKER_BOX_OPEN, 0.7F, 1.5F);
					slot.set(finished);
				}
				return true;
			}
		}

		return false;
	}

	public static BlockEntity getShulkerBoxEntity(ItemStack shulkerBox, RegistryAccess access) {
        CompoundTag cmp;
        if (shulkerBox.has(DataComponents.BLOCK_ENTITY_DATA)) {
            cmp = shulkerBox.get(DataComponents.BLOCK_ENTITY_DATA).copyTag();
        } else {
            cmp = new CompoundTag();
        }

		if(cmp.contains("LootTable"))
			return null;

		BlockEntity te = null;
		cmp = cmp.copy();
		cmp.putString("id", "minecraft:shulker_box");
		if(shulkerBox.getItem() instanceof BlockItem) {
			Block shulkerBoxBlock = Block.byItem(shulkerBox.getItem());
			BlockState defaultState = shulkerBoxBlock.defaultBlockState();
			if(shulkerBoxBlock instanceof EntityBlock) {
				te = ((EntityBlock) shulkerBoxBlock).newBlockEntity(BlockPos.ZERO, defaultState);
				if(te != null)
					te.loadWithComponents(cmp, access);
			}
		}

		return te;
	}

	private static ItemStack tryAddToShulkerBox(Player player, ItemStack shulkerBox, ItemStack stack, Slot slot, boolean simulate, boolean useCopy, boolean allowDump) {
		if(!SimilarBlockTypeHandler.isShulkerBox(shulkerBox) || !slot.mayPickup(player))
			return null;

		BlockEntity tile = getShulkerBoxEntity(shulkerBox, player.level().registryAccess());

		if (tile != null) {
			Optional<IItemHandler> handlerHolder = Optional.ofNullable(shulkerBox.getCapability(Capabilities.ItemHandler.ITEM));
			if(handlerHolder.isPresent() && handlerHolder.orElse(new ItemStackHandler()) instanceof IItemHandler handler) {
				if(SimilarBlockTypeHandler.isShulkerBox(stack) && allowDump) {
					BlockEntity otherShulker = getShulkerBoxEntity(stack, player.level().registryAccess());
					if(otherShulker != null) {
						Optional<IItemHandler> otherHolder = Optional.ofNullable(stack.getCapability(Capabilities.ItemHandler.ITEM));
						if(otherHolder.isPresent() && otherHolder.orElse(new ItemStackHandler()) instanceof IItemHandler otherHandler) {
							boolean any = false;
							for(int i = 0; i < otherHandler.getSlots(); i++) {
								ItemStack inserting = otherHandler.extractItem(i, 64, true);
								if(!inserting.isEmpty()) {
									ItemStack result = ItemHandlerHelper.insertItem(handler, inserting, true);
									if(result.isEmpty() || result.getCount() != inserting.getCount()) {
										if(simulate) {
											return shulkerBox;
										} else {
											ItemHandlerHelper.insertItem(handler, otherHandler.extractItem(i, inserting.getCount() - result.getCount(), false), false);

											any = true;
										}
									}
								}
							}

							if(any) {
								ItemStack workStack = useCopy ? shulkerBox.copy() : shulkerBox;

								workStack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tile.saveWithId(player.level().registryAccess())));
								stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(otherShulker.saveWithId(player.level().registryAccess())));

								if(slot.mayPlace(workStack))
									return workStack;
							}
						}
					}
				}
				ItemStack result = ItemHandlerHelper.insertItem(handler, stack.copy(), simulate);
				boolean did = result.isEmpty() || result.getCount() != stack.getCount();

				if(did) {
					ItemStack workStack = useCopy ? shulkerBox.copy() : shulkerBox;
					if(!simulate)
						stack.setCount(result.getCount());

					workStack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tile.saveWithId(player.level().registryAccess())));

					if(slot.mayPlace(workStack))
						return workStack;
				}
			}
		}

		return null;
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends ExpandedItemInteractionsModule {

		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			AccessorMenuScreens.invokeRegister(heldShulkerBoxMenuType, HeldShulkerBoxScreen::new);
		}

		@PlayEvent
		public void gatherTooltip(ZRenderTooltip.GatherComponents.Low event) {
			if(!enableArmorInteraction && (!enableShulkerBoxInteraction || !allowOpeningShulkerBoxes))
				return;

			Minecraft mc = Minecraft.getInstance();
			Screen gui = mc.screen;
			if(mc.player != null && gui instanceof AbstractContainerScreen<?> containerGui && containerGui.getMenu().getCarried().isEmpty()) {
				Slot under = containerGui.getSlotUnderMouse();
				if(under == null || under instanceof AccessorCustomCreativeSlot)
					return;

				ItemStack underStack = under.getItem();

				if(event.getItemStack() == underStack)
					if(enableArmorInteraction && armorOverride(underStack, ItemStack.EMPTY, under, ClickAction.SECONDARY, mc.player, true))
						event.getTooltipElements().add(Either.left(Component.translatable("quark.misc.equip_armor").withStyle(ChatFormatting.YELLOW)));

					else if(enableShulkerBoxInteraction && canOpenShulkerBox(underStack, ItemStack.EMPTY, under, mc.player))
						event.getTooltipElements().add(Either.left(Component.translatable("quark.misc.open_shulker").withStyle(ChatFormatting.YELLOW)));
			}
		}

		@PlayEvent
		public void onDrawScreen(ZScreen.Render.Post event) {
			Minecraft mc = Minecraft.getInstance();
			Screen gui = mc.screen;
			GuiGraphics guiGraphics = event.getGuiGraphics();

			if(mc.player != null && gui instanceof AbstractContainerScreen<?> containerGui) {
				ItemStack held = containerGui.getMenu().getCarried();
				if(!held.isEmpty()) {
					Slot under = containerGui.getSlotUnderMouse();
					if(under == null || under instanceof AccessorCustomCreativeSlot)
						return;

					ItemStack underStack = under.getItem();

					int x = event.getMouseX();
					int y = event.getMouseY();
					if(enableLavaInteraction && canTrashItem(underStack, held, under, mc.player)) {
						guiGraphics.renderComponentTooltip(mc.font, List.of(Component.translatable("quark.misc.trash_item").withStyle(ChatFormatting.RED)), x, y);
					} else if(enableShulkerBoxInteraction && tryAddToShulkerBox(mc.player, underStack, held, under, true, true, true) != null) {
						guiGraphics.renderComponentTooltip(mc.font, List.of(Component.translatable(
								SimilarBlockTypeHandler.isShulkerBox(held) ? "quark.misc.merge_shulker_box" : "quark.misc.insert_shulker_box"
						).withStyle(ChatFormatting.YELLOW)), x, y, underStack);
					} else if(enableShulkerBoxInteraction && SimilarBlockTypeHandler.isShulkerBox(underStack)) {
						guiGraphics.renderComponentTooltip(mc.font, Screen.getTooltipFromItem(mc, underStack), x, y, underStack);
					}

				}
			}
		}

		@PlayEvent
		public void onScroll(ZScreen.MouseScrolled.Pre event) {
			if(!allowRotatingBundles)
				return;

			Minecraft mc = Minecraft.getInstance();
			Screen gui = mc.screen;

			double scrollDelta = event.getScrollDeltaY();

			if(mc.player != null && gui instanceof AbstractContainerScreen<?> containerGui) {
				Slot under = containerGui.getSlotUnderMouse();
				if(under == null || under instanceof AccessorCustomCreativeSlot)
					return;

				ItemStack underStack = under.getItem();
				if(underStack.is(Items.BUNDLE)) {
					BundleContents bundleContents = underStack.get(DataComponents.BUNDLE_CONTENTS);

					if(bundleContents != null) {
						List<ItemStack> items = (List<ItemStack>) bundleContents.items();
						if(items.size() > 1) {
							var menu = containerGui.getMenu();
							event.setCanceled(true);
							if(scrollDelta < -0.1 || scrollDelta > 0.1) {
								rotateBundle(underStack, scrollDelta);
								PacketDistributor.sendToServer(new ScrollOnBundleMessage(menu.containerId, menu.getStateId(), under.index, scrollDelta));
							}
						}
					}
				}
			}
		}
	}
}
