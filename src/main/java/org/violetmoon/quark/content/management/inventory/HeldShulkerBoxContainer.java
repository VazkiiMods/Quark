package org.violetmoon.quark.content.management.inventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.violetmoon.quark.base.handler.SimilarBlockTypeHandler;
import org.violetmoon.quark.content.management.module.ExpandedItemInteractionsModule;

import java.util.ArrayList;
import java.util.List;

public class HeldShulkerBoxContainer implements Container, MenuProvider {

	public final Player player;
	public final ItemStack stack;
	public final ShulkerBoxBlockEntity be;
	public final int slot;

	public HeldShulkerBoxContainer(Player player, int slot) {
		this.player = player;
		this.slot = slot;

		stack = player.getInventory().getItem(slot);
		ShulkerBoxBlockEntity gotBe = null;

		if(SimilarBlockTypeHandler.isShulkerBox(stack)) {
			BlockEntity tile = ExpandedItemInteractionsModule.getShulkerBoxEntity(stack, player.level().registryAccess());
			if(tile instanceof ShulkerBoxBlockEntity shulker) {
                gotBe = shulker;
            }
        }

        if (stack.has(DataComponents.CONTAINER) && gotBe != null) {
            for (int i = 0; i < stack.get(DataComponents.CONTAINER).getSlots(); i++) {
                gotBe.setItem(i, stack.get(DataComponents.CONTAINER).getStackInSlot(i));
            }
        }

        be = gotBe;
	}

	@Override
	public AbstractContainerMenu createMenu(int containerID, Inventory playerInv, Player player) {
		return new HeldShulkerBoxMenu(containerID, playerInv, this, slot);
	}

	@Override
	public Component getDisplayName() {
		return be.getDisplayName();
	}

	@Override
	public void clearContent() {
		be.clearContent();
	}

	@Override
	public int getContainerSize() {
		return be.getContainerSize();
	}

	@Override
	public boolean isEmpty() {
		return be.isEmpty();
	}

	@Override
	public ItemStack getItem(int slot) {
		return be.getItem(slot);
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		return be.removeItem(slot, amount);
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return be.removeItemNoUpdate(slot);
	}

	@Override
	public void setItem(int slot, ItemStack itemStack) {
		be.setItem(slot, itemStack);
	}

	@Override
	public void setChanged() {
		be.setChanged();
        List<ItemStack> stacks = new ArrayList<>();
        for (int i = 0; i < be.getContainerSize(); i++) {
            stacks.add(this.getItem(i));
        }
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(stacks));
        //stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(be.saveWithId(be.getLevel().registryAccess())));
		//ItemNBTHelper.setCompound(stack, "BlockEntityTag", be.saveWithId());
	}

	@Override
	public boolean stillValid(Player player) {
		return stack != null && player == this.player && player.getInventory().getItem(slot) == stack;
	}

}
