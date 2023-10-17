package vazkii.quark.addons.oddities.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import vazkii.quark.base.util.InventoryIIH;
import vazkii.quark.addons.oddities.inventory.slot.BackpackSlot;
import vazkii.quark.addons.oddities.inventory.slot.CachedItemHandlerSlot;
import vazkii.quark.addons.oddities.module.BackpackModule;

import javax.annotation.Nonnull;

public class BackpackMenu extends InventoryMenu {

	public BackpackMenu(int windowId, Player player) {
		super(player.getInventory(), !player.level.isClientSide, player);
		this.containerId = windowId;

		Inventory inventory = player.getInventory();
		for(Slot slot : slots)
			if (slot.container == inventory && slot.getSlotIndex() < inventory.getContainerSize() - 5)
				slot.y += 58;

		Slot anchor = slots.get(9);
		int left = anchor.x;
		int top = anchor.y - 58;

		ItemStack backpack = inventory.armor.get(2);
		if(backpack.getItem() == BackpackModule.backpack) {
			InventoryIIH inv = new InventoryIIH(backpack);

			for(int i = 0; i < 3; ++i)
				for(int j = 0; j < 9; ++j) {
					int k = j + i * 9;
					addSlot(new BackpackSlot(inv, k, left + j * 18, top + i * 18));
				}
		}
	}

	public static BackpackMenu fromNetwork(int windowId, Inventory playerInventory, FriendlyByteBuf buf) {
		return new BackpackMenu(windowId, playerInventory.player);
	}

	@Nonnull
	@Override
	public ItemStack quickMoveStack(@Nonnull Player playerIn, int index) {
		final int topSlots = 8;
		final int invStart = topSlots + 1;
		final int invEnd = invStart + 27;
		final int hotbarStart = invEnd;
		final int hotbarEnd = hotbarStart + 9;
		final int shieldSlot = hotbarEnd;
		final int backpackStart = shieldSlot + 1;
		final int backpackEnd = backpackStart + 27;

		ItemStack baseStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack stack = slot.getItem();
			baseStack = stack.copy();
			EquipmentSlot slotType = Mob.getEquipmentSlotForItem(stack);
			int equipIndex = topSlots - (slotType == null ? 0 : slotType.getIndex());

			if (index < invStart || index == shieldSlot) { // crafting and armor slots
				ItemStack target = null;
				if(!this.moveItemStackTo(stack, invStart, hotbarEnd, false) && !this.moveItemStackTo(stack, backpackStart, backpackEnd, false))
					target = ItemStack.EMPTY;

				if(target != null)
					return target;
				else if(index == 0) // crafting result
					slot.onQuickCraft(stack, baseStack);
			}

			else if(slotType != null && slotType.getType() == Type.ARMOR && !this.slots.get(equipIndex).hasItem()) { // shift clicking armor
				if(!this.moveItemStackTo(stack, equipIndex, equipIndex + 1, false))
					return ItemStack.EMPTY;
			}

			else if (slotType != null && slotType == EquipmentSlot.OFFHAND && !this.slots.get(shieldSlot).hasItem()) { // shift clicking shield
				if(!this.moveItemStackTo(stack, shieldSlot, shieldSlot + 1, false))
					return ItemStack.EMPTY;
			}

			else if (index < invEnd) {
				if (!this.moveItemStackTo(stack, backpackStart, backpackEnd, false) && !this.moveItemStackTo(stack, hotbarStart, hotbarEnd, false))
					return ItemStack.EMPTY;
			}

			else if(index < hotbarEnd) {
				if(!this.moveItemStackTo(stack, invStart, invEnd, false) && !this.moveItemStackTo(stack, backpackStart, backpackEnd, false))
					return ItemStack.EMPTY;
			}

			else if(!this.moveItemStackTo(stack, hotbarStart, hotbarEnd, false) && !this.moveItemStackTo(stack, invStart, invEnd, false))
				return ItemStack.EMPTY;

			if (stack.isEmpty())
				slot.set(ItemStack.EMPTY);
			else slot.setChanged();

			if (stack.getCount() == baseStack.getCount())
				return ItemStack.EMPTY;

			slot.onTake(playerIn, stack);
			if(index == 0)
				playerIn.drop(stack, false);
		}

		return baseStack;
	}

	// Shamelessly stolen from CoFHCore because KL is awesome
	// and was like yeah just take whatever you want lol
	// https://github.com/CoFH/CoFHCore/blob/d4a79b078d257e88414f5eed598d57490ec8e97f/src/main/java/cofh/core/util/helpers/InventoryHelper.java
	@Override
	public boolean moveItemStackTo(ItemStack stack, int start, int length, boolean r) {
		boolean successful = false;
		int i = !r ? start : length - 1;
		int iterOrder = !r ? 1 : -1;

		Slot slot;
		ItemStack existingStack;

		if(stack.isStackable()) while (stack.getCount() > 0 && (!r && i < length || r && i >= start)) {
			slot = slots.get(i);

			existingStack = slot.getItem();

			if (!existingStack.isEmpty()) {
				int maxStack = Math.min(stack.getMaxStackSize(), slot.getMaxStackSize());
				int rmv = Math.min(maxStack, stack.getCount());

				if (slot.mayPlace(cloneStack(stack, rmv)) && existingStack.getItem().equals(stack.getItem()) && ItemStack.tagMatches(stack, existingStack)) {
					int existingSize = existingStack.getCount() + stack.getCount();

					if (existingSize <= maxStack) {
						stack.setCount(0);
						existingStack.setCount(existingSize);
						slot.set(existingStack);
						successful = true;
					} else if (existingStack.getCount() < maxStack) {
						stack.shrink(maxStack - existingStack.getCount());
						existingStack.setCount(maxStack);
						slot.set(existingStack);
						successful = true;
					}
				}
			}
			i += iterOrder;
		}
		if(stack.getCount() > 0) {
			i = !r ? start : length - 1;
			while(stack.getCount() > 0 && (!r && i < length || r && i >= start)) {
				slot = slots.get(i);
				existingStack = slot.getItem();

				if(existingStack.isEmpty()) {
					int maxStack = Math.min(stack.getMaxStackSize(), slot.getMaxStackSize());
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

	@Override
	public void clicked(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull Player player) {
		CachedItemHandlerSlot.cache(this);
		super.clicked(slotId, dragType, clickTypeIn, player);
		CachedItemHandlerSlot.applyCache(this);
	}

	private static ItemStack cloneStack(ItemStack stack, int size) {
		if(stack.isEmpty())
			return ItemStack.EMPTY;

		ItemStack copy = stack.copy();
		copy.setCount(size);
		return copy;
	}

	public static void saveCraftingInventory(Player player) {
		CraftingContainer crafting = ((InventoryMenu) player.containerMenu).getCraftSlots();
		for(int i = 0; i < crafting.getContainerSize(); i++) {
			ItemStack stack = crafting.getItem(i);
			if(!stack.isEmpty() && !player.addItem(stack))
				player.drop(stack, false);
		}
	}

	@Override
	public @Nonnull MenuType<?> getType() {
		return BackpackModule.menyType;
	}

}
