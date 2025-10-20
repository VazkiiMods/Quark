package org.violetmoon.quark.addons.oddities.inventory.slot;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.inventory.BackpackContainer;
import org.violetmoon.quark.addons.oddities.module.BackpackModule;

public class BackpackSlot extends CachedItemHandlerSlot {

	public BackpackSlot(BackpackContainer container, int index, int xPosition, int yPosition) {
		super(container, index, xPosition, yPosition);
	}

	@Override
	public boolean mayPlace(@NotNull ItemStack stack) {
		return super.mayPlace(stack) && !stack.is(BackpackModule.backpackBlockedTag);
	}

}
