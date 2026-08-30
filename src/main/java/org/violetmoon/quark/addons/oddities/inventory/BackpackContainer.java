package org.violetmoon.quark.addons.oddities.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.module.BackpackModule;

public class BackpackContainer extends SimpleContainer {
    private final ItemStack sourceStack;

    public BackpackContainer(ItemStack sourceStack) {
        super(27);
        this.sourceStack = sourceStack;
        ItemContainerContents contents = sourceStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        contents.copyInto(this.getItems());
    }

    @Override
    public boolean canPlaceItem(int slot, @NotNull ItemStack stack) {
        return BackpackModule.backpackBlockedTag == null || !stack.is(BackpackModule.backpackBlockedTag);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        this.sourceStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
    }
}
