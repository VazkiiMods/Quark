package org.violetmoon.quark.addons.oddities.inventory;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.block.be.CrateBlockEntity;
import org.violetmoon.quark.addons.oddities.module.CrateModule;
import org.violetmoon.quark.base.network.message.oddities.ScrollCrateMessage;

public class CrateMenu extends AbstractContainerMenu {

	public final CrateBlockEntity crate;
	public final Inventory playerInv;

	public static final int numRows = 6;
	public static final int numCols = 9;
	public static final int displayedSlots = numCols * numRows;
	public final int totalSlots;

	public int scroll = 0;
	private final ContainerData crateData;

	public CrateMenu(int id, Inventory inv, CrateBlockEntity crate) {
		this(id, inv, crate, new SimpleContainerData(2));
	}

	public CrateMenu(int id, Inventory inv, CrateBlockEntity crate, ContainerData data) {
		super(CrateModule.menuType, id);
		crate.startOpen(inv.player);

		this.crate = crate;
		this.playerInv = inv;
		this.crateData = data;

		int rowHeight = (numRows - 4) * 18;

		totalSlots = crate.getContainerSize();

		for(int index = 0; index < totalSlots; ++index)
			addSlot(new CrateSlot(crate, index, 8 + (index % numCols) * 18, 18 + (index / numCols) * 18));

		for(int yIndex = 0; yIndex < 3; ++yIndex)
			for(int xIndex = 0; xIndex < 9; ++xIndex)
				addSlot(new Slot(inv, xIndex + yIndex * 9 + 9, 8 + xIndex * 18, 103 + yIndex * 18 + rowHeight));

		for(int index = 0; index < 9; ++index)
			addSlot(new Slot(inv, index, 8 + index * 18, 161 + rowHeight));

		addDataSlots(crateData);
	}

	public int getTotal() {
		return crateData.get(0);
	}

	public int getStackCount() {
		return crateData.get(1);
	}

	@NotNull
	@Override
	public ItemStack quickMoveStack(@NotNull Player playerIn, int index) {
		ItemStack activeStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if(slot != null && slot.hasItem()) {
			ItemStack stackInSlot = slot.getItem();
			activeStack = stackInSlot.copy();

			if(index < totalSlots) {
				if(!this.moveItemStackTo(stackInSlot, totalSlots, slots.size(), true))
					return ItemStack.EMPTY;
			} else if(!this.moveItemStackTo(stackInSlot, 0, totalSlots, false))
				return ItemStack.EMPTY;

			if(stackInSlot.isEmpty())
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
		}

		return activeStack;
	}

	// Shamelessly stolen from CoFHCore because KL is awesome
	// and was like yeah just take whatever you want lol
	// https://github.com/CoFH/CoFHCore/blob/d4a79b078d257e88414f5eed598d57490ec8e97f/src/main/java/cofh/core/util/helpers/InventoryHelper.java
	@Override
	public boolean moveItemStackTo(ItemStack stack, int start, int length, boolean reverse) {
        boolean successful = false;
        int i = reverse ? (length - 1) : start;
        int iterOrder = reverse ? -1 : 1;

        Slot slot;
        ItemStack existingStack;

        // First pass, try to merge
        if(stack.isStackable())
            while(!stack.isEmpty() && (reverse ? i >= start : i < length)) {
                slot = slots.get(i);

                existingStack = slot.getItem();

                if(!existingStack.isEmpty()) {
                    int maxStack = Math.min(Math.min(stack.getMaxStackSize(), slot.getMaxStackSize()), slot instanceof CrateSlot ? CrateModule.maxItems - crate.getTotal() : 64);
                    int rmv = Math.min(maxStack, stack.getCount());

                    if(slot.mayPlace(cloneStack(stack, rmv)) && existingStack.getItem().equals(stack.getItem()) && ItemStack.isSameItemSameComponents(stack, existingStack)) {
                        int existingSize = existingStack.getCount() + stack.getCount();
                        ItemStack existingStackCopy = existingStack.copy();

                        if(existingSize <= maxStack) {
                            stack.setCount(0);
                            existingStackCopy.setCount(existingSize);
                            slot.set(existingStackCopy);
                            successful = true;
                        } else if(existingStackCopy.getCount() < maxStack) {
                            stack.shrink(maxStack - existingStackCopy.getCount());

                            existingStackCopy.setCount(maxStack);
                            slot.set(existingStackCopy);
                            successful = true;
                        }
                    }
                }
                i += iterOrder;
            }

        // Second pass, after marged, if any remaining, try to insert into empty slots
        if(!stack.isEmpty()) {
            i = reverse ? (length - 1) : start;
            while(!stack.isEmpty() && (!reverse && i < length || reverse && i >= start) && (!(slots.get(i) instanceof CrateSlot) || CrateModule.maxItems - crate.getTotal() > 0)) {
                slot = slots.get(i);
                existingStack = slot.getItem();

                if(existingStack.isEmpty()) {
                    int maxStack = Math.min(Math.min(stack.getMaxStackSize(), slot.getMaxStackSize()), slot instanceof CrateSlot ? CrateModule.maxItems - crate.getTotal() : 64);
                    int rmv = Math.min(maxStack, stack.getCount());

                    if(slot.mayPlace(cloneStack(stack, rmv))) {
                        existingStack = stack.split(rmv);
                        slot.set(existingStack);
                        successful = true;
                    }
                }
                i += iterOrder;
            }
        }

        return successful;


	}

	private static ItemStack cloneStack(ItemStack stack, int size) {
		if(stack.isEmpty())
			return ItemStack.EMPTY;

		ItemStack copy = stack.copy();
		copy.setCount(size);
		return copy;
	}

	public static CrateMenu fromNetwork(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
		BlockPos pos = buf.readBlockPos();
		CrateBlockEntity te = (CrateBlockEntity) playerInventory.player.level().getBlockEntity(pos);
		return new CrateMenu(windowId, playerInventory, te);
	}

	@Override
	public boolean stillValid(@NotNull Player playerIn) {
		return crate.stillValid(playerIn);
	}

	@Override
	public void removed(@NotNull Player playerIn) {
		super.removed(playerIn);
		crate.stopOpen(playerIn);
	}

	public void scroll(boolean down, boolean packet) {
		boolean did = false;

		if(down) {
			int maxScroll = (getStackCount() / numCols) * numCols;

			int target = scroll + numCols;
			if(target <= maxScroll) {
				scroll = target;
				did = true;

				for(Slot slot : slots)
					if(slot instanceof CrateSlot)
						slot.y -= 18;
			}
		} else {
			int target = scroll - numCols;
			if(target >= 0) {
				scroll = target;
				did = true;

				for(Slot slot : slots)
					if(slot instanceof CrateSlot)
						slot.y += 18;
			}
		}

		if(did) {
			broadcastChanges();

			if(packet)
				PacketDistributor.sendToServer(new ScrollCrateMessage(down));
		}
	}

	private class CrateSlot extends Slot {
		public CrateSlot(Container container, int index, int xPosition, int yPosition) {
			super(container, index, xPosition, yPosition);
		}

		@Override
		public void setChanged() {
			crate.setChanged();
		}

		@Override
		public boolean isActive() {
			int index = getSlotIndex();
			return index >= scroll && index < scroll + displayedSlots;
		}

        //todo: Could be made a mixin for better cross-mod support
        @Override
        public ItemStack safeInsert(ItemStack stack, int increment) {
            if (!stack.isEmpty() && this.mayPlace(stack)) {
                ItemStack itemstack = this.getItem();
                int amount = Math.max(Math.min(Math.min(increment, stack.getCount()), CrateModule.maxItems - ((CrateBlockEntity)container).getTotal()), 0);
                if (itemstack.isEmpty()) {
                    this.setByPlayer(stack.split(amount));
                } else if (ItemStack.isSameItemSameComponents(itemstack, stack)) {
                    stack.shrink(amount);
                    itemstack.grow(amount);
                    this.setByPlayer(itemstack);
                }

                ((CrateBlockEntity)container).refreshCachedTotal();
                return stack;
            } else {
                return stack;
            }
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return super.mayPlace(stack) && ((CrateBlockEntity)container).getSlotLimit(index) > 0;
        }
    }
}
