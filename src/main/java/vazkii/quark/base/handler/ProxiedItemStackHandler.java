package vazkii.quark.base.handler;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;
import vazkii.zeta.util.ItemNBTHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author WireSegal
 * Created at 4:27 PM on 12/15/18.
 */
public class ProxiedItemStackHandler implements IItemHandler, IItemHandlerModifiable, ICapabilityProvider {

	protected final ItemStack stack;
	protected final String key;
	protected final int size;

	public ProxiedItemStackHandler(ItemStack stack) {
		this(stack, "Inventory", 1);
	}

	public ProxiedItemStackHandler(ItemStack stack, String key) {
		this(stack, key, 1);
	}

	public ProxiedItemStackHandler(ItemStack stack, int size) {
		this(stack, "Inventory", size);
	}

	public ProxiedItemStackHandler(ItemStack stack, String key, int size) {
		this.stack = stack;
		this.key = key;
		this.size = size;
	}

	private ListTag getStackList() {
		ListTag list = ItemNBTHelper.getList(stack, key, 10, true);
		if (list == null)
			ItemNBTHelper.setList(stack, key, list = new ListTag());

		while (list.size() < size)
			list.add(new CompoundTag());

		return list;
	}

	private void writeStack(int index, @Nonnull ItemStack stack) {
		getStackList().set(index, stack.serializeNBT());
		onContentsChanged(index);
	}

	private ItemStack readStack(int index) {
		return ItemStack.of(getStackList().getCompound(index));
	}

	@Override
	public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
		validateSlotIndex(slot);
		writeStack(slot, stack);
		onContentsChanged(slot);
	}

	@Override
	public int getSlots() {
		return size;
	}

	@Override
	@Nonnull
	public ItemStack getStackInSlot(int slot) {
		validateSlotIndex(slot);
		return readStack(slot);
	}

	@Override
	@Nonnull
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return ItemStack.EMPTY;

		validateSlotIndex(slot);

		ItemStack existing = readStack(slot);

		int limit = getStackLimit(slot, stack);

		if (!existing.isEmpty()) {
			if (!ItemHandlerHelper.canItemStacksStack(stack, existing))
				return stack;

			limit -= existing.getCount();
		}

		if (limit <= 0)
			return stack;

		boolean reachedLimit = stack.getCount() > limit;

		if (!simulate)
			writeStack(slot, reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, limit) : stack);

		return reachedLimit ? ItemHandlerHelper.copyStackWithSize(stack, stack.getCount() - limit) : ItemStack.EMPTY;
	}

	@Override
	@Nonnull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (amount == 0)
			return ItemStack.EMPTY;

		validateSlotIndex(slot);

		ItemStack existing = readStack(slot);

		if (existing.isEmpty())
			return ItemStack.EMPTY;

		int toExtract = Math.min(amount, existing.getMaxStackSize());

		if (existing.getCount() <= toExtract) {
			if (!simulate)
				writeStack(slot, ItemStack.EMPTY);

			return existing;
		} else {
			if (!simulate)
				writeStack(slot, ItemHandlerHelper.copyStackWithSize(existing, existing.getCount() - toExtract));

			return ItemHandlerHelper.copyStackWithSize(existing, toExtract);
		}
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
		return Math.min(getSlotLimit(slot), stack.getMaxStackSize());
	}

	@Override
	public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
		return true;
	}

	protected void validateSlotIndex(int slot) {
		if (slot < 0 || slot >= size)
			throw new RuntimeException("Slot " + slot + " not in valid range - [0," + size + ")");
	}

	protected void onContentsChanged(int slot) {
		// NO-OP
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
		return ForgeCapabilities.ITEM_HANDLER.orEmpty(capability, LazyOptional.of(() -> this));
	}
}
