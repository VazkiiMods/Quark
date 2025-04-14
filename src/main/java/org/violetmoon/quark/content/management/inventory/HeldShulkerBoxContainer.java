package org.violetmoon.quark.content.management.inventory;

import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import org.violetmoon.quark.base.handler.SimilarBlockTypeHandler;
import org.violetmoon.quark.content.management.module.ExpandedItemInteractionsModule;

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
			if(tile instanceof ShulkerBoxBlockEntity shulker)
				gotBe = shulker;
		}

		be = gotBe;
	}

	@Override
	public AbstractContainerMenu createMenu(int p_39954_, Inventory p_39955_, Player p_39956_) {
		return new HeldShulkerBoxMenu(p_39954_, p_39955_, this, slot);
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
	public ItemStack getItem(int p_18941_) {
		return be.getItem(p_18941_);
	}

	@Override
	public ItemStack removeItem(int p_18942_, int p_18943_) {
		return be.removeItem(p_18942_, p_18943_);
	}

	@Override
	public ItemStack removeItemNoUpdate(int p_18951_) {
		return be.removeItemNoUpdate(p_18951_);
	}

	@Override
	public void setItem(int p_18944_, ItemStack p_18945_) {
		be.setItem(p_18944_, p_18945_);
	}

	@Override
	public void setChanged() {
		be.setChanged();
		//ItemNBTHelper.setCompound(stack, "BlockEntityTag", be.saveWithId());
	}

	@Override
	public boolean stillValid(Player player) {
		return stack != null && player == this.player && player.getInventory().getItem(slot) == stack;
	}

}
