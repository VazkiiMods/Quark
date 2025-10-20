package org.violetmoon.quark.content.management.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.violetmoon.quark.api.ISortingLockedSlots;
import org.violetmoon.quark.content.management.module.ExpandedItemInteractionsModule;

public class HeldShulkerBoxMenu extends AbstractContainerMenu implements ISortingLockedSlots {

	private final Container container;
	private final Player player;
	public final int blockedSlot;

	public HeldShulkerBoxMenu(int containerID, Inventory playerInventory, int blockedSlot) {
		this(containerID, playerInventory, new SimpleContainer(27), blockedSlot);
	}

	public HeldShulkerBoxMenu(int containerID, Inventory playerInventory, FriendlyByteBuf buf) {
		this(containerID, playerInventory, buf.readInt());
	}

    // Fun fact! Slots are order-sensitive! The more you know.
	public HeldShulkerBoxMenu(int containerID, Inventory playerInventory, Container container, int blockedSlot) {
		super(ExpandedItemInteractionsModule.heldShulkerBoxMenuType, containerID);
		checkContainerSize(container, 27);
		this.container = container;
		this.player = playerInventory.player;
		this.blockedSlot = blockedSlot;
		container.startOpen(player);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + row * 9;
                this.addSlot(new ShulkerBoxSlot(container, slot, 8 + column * 18, 18 + row * 18));
            }
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = column + row * 9 + 9;
                if (slot != blockedSlot)
                    this.addSlot(new Slot(playerInventory, slot, 8 + column * 18, 84 + row * 18));
            }
        }

		for (int slot = 0; slot < 9; ++slot) {
			if (slot != blockedSlot) {
				this.addSlot(new Slot(playerInventory, slot, 8 + slot * 18, 142));
			}
		}
	}

	@Override
	public boolean stillValid(Player checkedPlayer) {
		return this.container.stillValid(checkedPlayer);
	}

	@Override
	public ItemStack quickMoveStack(Player checkedPlayer, int slotIndex) {
		ItemStack copy = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);
		if (slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			copy = stack.copy();
			if (slotIndex < this.container.getContainerSize()) {
				if (!this.moveItemStackTo(stack, this.container.getContainerSize(), this.slots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if(!this.moveItemStackTo(stack, 0, this.container.getContainerSize(), false)) {
				return ItemStack.EMPTY;
			}

			if(stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}
		}
		return copy;
	}

	@Override
	public void suppressRemoteUpdates() {
		super.suppressRemoteUpdates();
		player.inventoryMenu.suppressRemoteUpdates();
	}

	@Override
	public void resumeRemoteUpdates() {
		super.resumeRemoteUpdates();
		player.inventoryMenu.resumeRemoteUpdates();
	}

	@Override
	public void removed(Player checkedPlayer) {
		super.removed(checkedPlayer);
		this.container.stopOpen(checkedPlayer);
	}

	@Override
	public int[] getSortingLockedSlots(boolean sortingPlayerInventory) {
		return sortingPlayerInventory ? new int[] { blockedSlot } : null;
	}
}
