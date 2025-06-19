package org.violetmoon.quark.base.handler;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.api.IQuarkButtonAllowed;
import org.violetmoon.quark.api.ITransferManager;
import org.violetmoon.quark.api.QuarkCapabilities;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.config.QuarkGeneralConfig;
import org.violetmoon.quark.content.management.module.EasyTransferingModule;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class InventoryTransferHandler {

	public static void transfer(Player player, boolean isRestock, boolean smart) {
		if(!Quark.ZETA.modules.isEnabled(EasyTransferingModule.class) || player.isSpectator() || !accepts(player.containerMenu, player))
			return;

		//		if(!useContainer && !player.getEntityWorld().getWorldInfo().getGameRulesInstance().getBoolean(StoreToChests.GAME_RULE)) {
		//			disableClientDropoff(player);
		//			return;
		//		}

		Transfer transfer = isRestock ? new Restock(player, smart) : new Transfer(player, smart);
		transfer.execute();
	}

	//	public static void disableClientDropoff(PlayerEntity player) {
	//		if(player instanceof ServerPlayerEntity)
	//			NetworkHandler.INSTANCE.sendTo(new MessageDisableDropoffClient(), (ServerPlayerEntity) player);
	//	}
	//
	//	public static IItemHandler getInventory(PlayerEntity player, World world, BlockPos pos) {
	//		TileEntity te = world.getTileEntity(pos);
	//
	//		if(te == null)
	//			return null;
	//
	//		boolean accept = isValidChest(player, te);
	//		if(accept) {
	//			Supplier<IItemHandler> supplier = () -> {
	//				IItemHandler innerRet = te.getCapability(ForgeCapabilities.ITEM_HANDLER, null).orElse(null);
	//				if(innerRet == null && te instanceof IInventory)
	//					innerRet = new InvWrapper((IInventory) te);
	//
	//				return innerRet;
	//			};
	//
	//			if(hasProvider(te))
	//				return getProvider(te).getTransferItemHandler(supplier);
	//			else return supplier.get();
	//		}
	//
	//		return null;
	//	}

	private static boolean hasProvider(Object te) {
		return false /*te instanceof BlockEntity be && Quark.ZETA.capabilityManager.hasCapability(QuarkCapabilities.TRANSFER, be)*/;
	}

	private static ITransferManager getProvider(Object te) {
		return null /*Quark.ZETA.capabilityManager.getCapability(QuarkCapabilities.TRANSFER, (BlockEntity) te)*/;
	}

	public static boolean accepts(AbstractContainerMenu container, Player player) {
		//extracting from the crafting table with the button dups items
		if (container instanceof CraftingMenu || (player.level().isClientSide() && container instanceof CreativeModeInventoryScreen.ItemPickerMenu))
			return false;

		if(hasProvider(container))
			return getProvider(container).acceptsTransfer(player);

		return container instanceof IQuarkButtonAllowed || (container.slots.size() - player.getInventory().items.size() >= QuarkGeneralConfig.chestButtonSlotTarget);
	}

	public static class Transfer {

		public final Player player;
		public final boolean smart;
		//		public final boolean useContainer;

		public final List<Pair<IItemHandler, Double>> itemHandlers = new ArrayList<>();

		public Transfer(Player player, boolean smart/*, boolean useContainer*/) {
			this.player = player;
			//			this.useContainer = useContainer;
			this.smart = smart;
		}

		public void execute() {
			locateItemHandlers();

			if(itemHandlers.isEmpty())
				return;

			if(smart)
				smartTransfer();
			else
				roughTransfer();

			player.inventoryMenu.broadcastChanges();
			//			if(useContainer)
			player.containerMenu.broadcastChanges();
		}

		public void smartTransfer() {
			transfer((stack, handler) -> {
				int slots = handler.getSlots();
				for(int i = 0; i < slots; i++) {
					ItemStack stackAt = handler.getStackInSlot(i);
					if(stackAt.isEmpty())
						continue;

					boolean itemEqual = stack.getItem() == stackAt.getItem();
					boolean damageEqual = stack.getDamageValue() == stackAt.getDamageValue();
					boolean nbtEqual = ItemStack.isSameItemSameComponents(stackAt, stack);

					if(itemEqual && damageEqual && nbtEqual)
						return true;

					if(stack.isDamageableItem() && stack.getMaxStackSize() == 1 && itemEqual && nbtEqual)
						return true;
				}

				return false;
			});
		}

		public void roughTransfer() {
			transfer((stack, handler) -> true);
		}

		public void locateItemHandlers() {
			//			if(useContainer) {
			AbstractContainerMenu c = player.containerMenu;
			for(Slot s : c.slots) {
				Container inv = s.container;
				if(inv != player.getInventory()) {
					itemHandlers.add(Pair.of(ContainerWrapper.provideWrapper(s, c), 0.0));
					break;
				}
			}
			//			} else {
			//				BlockPos playerPos = player.getPosition();
			//				int range = 6;
			//
			//				for(int i = -range; i < range * 2 + 1; i++)
			//					for(int j = -range; j < range * 2 + 1; j++)
			//						for(int k = -range; k < range * 2 + 1; k++) {
			//							BlockPos pos = playerPos.add(i, j, k);
			//							findHandler(pos);
			//						}
			//
			//				itemHandlers.sort(Comparator.comparingDouble(Pair::getRight));
			//			}
		}

		//		public void findHandler(BlockPos pos) {
		//			IItemHandler handler = getInventory(player, player.getEntityWorld(), pos);
		//			if(handler != null)
		//				itemHandlers.add(Pair.of(handler, player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)));
		//		}

		public void transfer(TransferPredicate predicate) {
			Inventory inv = player.getInventory();

			for(int i = Inventory.getSelectionSize(); i < inv.items.size(); i++) {
				ItemStack stackAt = inv.getItem(i);

				if(!stackAt.isEmpty()/* && !FavoriteItems.isItemFavorited(stackAt)*/) {
					ItemStack ret = insert(stackAt, predicate);
					if(!ItemStack.matches(stackAt, ret))
						inv.setItem(i, ret);
				}
			}
		}

		public ItemStack insert(ItemStack stack, TransferPredicate predicate) {
			ItemStack ret = stack.copy();
			for(Pair<IItemHandler, Double> pair : itemHandlers) {
				IItemHandler handler = pair.getLeft();
				ret = insertInHandler(handler, ret, predicate);
				if(ret.isEmpty())
					return ItemStack.EMPTY;
			}

			return ret;
		}

		public ItemStack insertInHandler(IItemHandler handler, final ItemStack stack, TransferPredicate predicate) {
			if(predicate.test(stack, handler)) {
				ItemStack retStack = ItemHandlerHelper.insertItemStacked(handler, stack, false);
				if(!retStack.isEmpty())
					retStack = retStack.copy();
				else
					return retStack;

				return retStack;
			}

			return stack;
		}

	}

	public static class Restock extends Transfer {

		public Restock(Player player, boolean filtered) {
			super(player, filtered);
		}

		@Override
		public void transfer(TransferPredicate predicate) {
			IItemHandler inv = itemHandlers.getFirst().getLeft();
			IItemHandler playerInv = new PlayerInvWrapper(player.getInventory());

			for(int i = inv.getSlots() - 1; i >= 0; i--) {
				ItemStack stackAt = inv.extractItem(i, inv.getStackInSlot(i).getCount(), true);

				if(!stackAt.isEmpty()) {
					ItemStack copy = stackAt.copy();
					ItemStack ret = insertInHandler(playerInv, copy, predicate);

					if(!ItemStack.matches(stackAt, ret)) {
						inv.extractItem(i, stackAt.getCount() - ret.getCount(), false);
					}
				}
			}
		}
	}

	public static class PlayerInvWrapper extends InvWrapper {

		public PlayerInvWrapper(Container inv) {
			super(inv);
		}

		@NotNull
		@Override
		public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			if(stack.isEmpty())
				stack = stack.copy();

			return super.insertItem(slot, stack, simulate);
		}

		@Override
		public int getSlots() {
			return super.getSlots() - 5;
		}

	}

	public static class ContainerWrapper extends InvWrapper {

		private final AbstractContainerMenu container;

		public static IItemHandler provideWrapper(Slot slot, AbstractContainerMenu container) {
			if(slot instanceof SlotItemHandler slotItemHandler) {
				IItemHandler handler = slotItemHandler.getItemHandler();
				if(hasProvider(handler)) {
					return getProvider(handler).getTransferItemHandler(() -> handler);
				} else {
					return handler;
				}
			} else {
				return provideWrapper(slot.container, container);
			}
		}

		public static IItemHandler provideWrapper(Container inv, AbstractContainerMenu container) {
			if(hasProvider(inv))
				return getProvider(inv).getTransferItemHandler(() -> new ContainerWrapper(inv, container));
			return new ContainerWrapper(inv, container);
		}

		private ContainerWrapper(Container inv, AbstractContainerMenu container) {
			super(inv);
			this.container = container;
		}

		@NotNull
		@Override
		public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
			Slot containerSlot = getSlot(slot);
			if(containerSlot == null || !containerSlot.mayPlace(stack))
				return stack;

			return super.insertItem(slot, stack, simulate);
		}

		private Slot getSlot(int slotId) {
			Container inv = getInv();
			for(Slot slot : container.slots)
				if(slot.container == inv && slot.getSlotIndex() == slotId)
					return slot;

			return null;
		}

	}

	public interface TransferPredicate extends BiPredicate<ItemStack, IItemHandler> {
	}

}
