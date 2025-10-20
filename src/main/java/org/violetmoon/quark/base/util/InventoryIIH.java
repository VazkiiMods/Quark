package org.violetmoon.quark.base.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

// formerly from AutoRegLib
public class InventoryIIH implements IItemHandlerModifiable {

	private final IItemHandlerModifiable iih;
	final ItemStack stack;

	public InventoryIIH(ItemStack stack) {
		this.stack = stack;
		Optional<IItemHandler> opt = Optional.ofNullable(stack.getCapability(Capabilities.ItemHandler.ITEM, null));

		if(opt.isPresent()) {
			IItemHandler handler = opt.orElse(null);
			iih = handler instanceof IItemHandlerModifiable ? (IItemHandlerModifiable) handler : null;
		} else
			iih = null;

		if(iih == null)
			throw new RuntimeException("Can't load InventoryIIH without a proper IItemHandlerModifiable");
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		iih.setStackInSlot(slot, stack);
	}

	@Override
	public int getSlots() {
		return iih.getSlots();
	}

	@NotNull
	@Override
	public ItemStack getStackInSlot(int slot) {
		return iih.getStackInSlot(slot);
	}

	@NotNull
	@Override
	public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
		return iih.insertItem(slot, stack, simulate);
	}

	@NotNull
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		return iih.extractItem(slot, amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return iih.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return iih.isItemValid(slot, stack);
	}
}
