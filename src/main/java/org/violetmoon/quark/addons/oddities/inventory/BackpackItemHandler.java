package org.violetmoon.quark.addons.oddities.inventory;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

public class BackpackItemHandler extends InvWrapper {

	public BackpackItemHandler(ItemStack backpack) {
		super(new BackpackContainer(backpack));
	}

	@Override
	public void setStackInSlot(int slot, @NotNull ItemStack stack) {
		super.setStackInSlot(slot, stack);
		getInv().setChanged();
	}
}
