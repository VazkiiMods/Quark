/*
 * The Cool MIT License (CMIT)
 *
 * Copyright (c) 2023 Emi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, as long as the person is cool, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * The person is cool.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.violetmoon.quark.content.automation.block.be;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import org.violetmoon.quark.content.automation.block.CrafterBlock;
import org.violetmoon.quark.content.automation.inventory.CrafterMenu;
import org.violetmoon.quark.content.automation.module.CrafterModule;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CrafterBlockEntity extends BaseContainerBlockEntity implements CraftingContainer, WorldlyContainer {
	private static final DispenseItemBehavior BEHAVIOR = new CraftDispenseBehavior();
	public final NonNullList<ItemStack> stacks = NonNullList.withSize(9, ItemStack.EMPTY);
	public final ResultContainer result = new ResultContainer();
	public final boolean[] blocked = new boolean[9];
	public final ContainerData delegate;
	private boolean didInitialScan = false;


	public CrafterBlockEntity(BlockPos pos, BlockState state) {
		super(CrafterModule.blockEntityType, pos, state);
		delegate = new ContainerData() {

			@Override
			public int get(int index) {
				int res = level.getBlockState(pos).getValue(CrafterBlock.POWER) == CrafterBlock.PowerState.TRIGGERED ? 1 : 0;
				for (int i = 0; i < 9; i++) {
					if (blocked[i]) {
						res |= 1 << (i + 1);
					}
				}
				return res;
			}

			@Override
			public void set(int index, int value) {
			}

			@Override
			public int getCount() {
				return 1;
			}
		};
	}

	@Override
	protected void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		ListTag list = new ListTag();
		for (boolean b : blocked) {
			list.add(ByteTag.valueOf(b));
		}
		nbt.put("Blocked", list);
		ContainerHelper.saveAllItems(nbt, stacks);
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		if (nbt.contains("Blocked")) {
			ListTag list = nbt.getList("Blocked", Tag.TAG_BYTE);
			for (int i = 0; i < list.size() && i < 9; i++) {
				blocked[i] = ((ByteTag) list.get(i)).getAsByte() != 0;
			}
		}
		ContainerHelper.loadAllItems(nbt, stacks);
	}
	public boolean insert_or_drop(ItemStack outputStack)
	{
		if (itemStack.isEmpty()) {
			return false;
		}
	//	BlockSource blockSource = new BlockSource(sw, worldPosition, this.getBlockState(), null);
		BlockSource blockSource = new BlockSourceImpl(sw, worldPosition);
		Direction direction = this.getBlockState().getValue(CrafterBlock.FACING);
		Container inventory = HopperBlockEntity.getContainerAt(level, worldPosition.relative(direction));
		if (inventory == null) {
			BEHAVIOR.dispense(blockSource, outputStack);
		} else {
			if (!hasSpace(inventory, direction, outputStack)) {
				return false;
			}
			if (inventory instanceof CrafterBlockEntity) {
				int count = output.getCount();
				for (int i = 0; i < count; i++) {
					ItemStack is = outputStack.copy();
					is.setCount(1);
					HopperBlockEntity.addItem(result, inventory, is, direction.getOpposite());
				}
			} else {
				HopperBlockEntity.addItem(result, inventory, outputStack, direction.getOpposite());
			}
		}
		return true;
	}
	public void craft() {
		if (level instanceof ServerLevel sw) {
			update();;
			if(insert_or_drop(result.getItem(0)))
				takeItems();
				update();
			}
		}
	}

	private static IntStream getAvailableSlots(Container inventory, Direction side) {
		return inventory instanceof WorldlyContainer ? IntStream.of(((WorldlyContainer)inventory).getSlotsForFace(side)) : IntStream.range(0, inventory.getContainerSize());
	}

	public boolean hasSpace(Container inv, Direction dir, ItemStack stack) {
		IntStream stream = getAvailableSlots(inv, dir);
		int inserted = 0;
		int slotMax = Math.min(stack.getMaxStackSize(), inv.getMaxStackSize());
		if (CrafterModule.useEmiLogic && inv instanceof CrafterBlockEntity) {
			slotMax = 1;
		}
		
		for (int i : stream.toArray()) {
			if (inv instanceof WorldlyContainer si && !si.canPlaceItemThroughFace(i, stack, dir)) {
				continue;
			}
			ItemStack is = inv.getItem(i);
			if (is.isEmpty()) {
				inserted += slotMax;
			} else if (ItemStack.isSameItemSameTags(is, stack) && is.getCount() < slotMax) {
				inserted += slotMax - is.getCount();
			}
			if (inserted >= stack.getCount()) {
				return true;
			}
		}
		return false;
	}

	public void takeItems() {
		NonNullList<ItemStack> defaultedList = level.getRecipeManager().getRemainingItemsFor(RecipeType.CRAFTING, this, level);

		if(level instanceof ServerLevel serverLevel) {
			for(int i = 0; i < defaultedList.size(); ++i) {
				ItemStack itemInCrafter = this.getItem(i);
				ItemStack remainingItem = defaultedList.get(i);
				
				if(remainingItem.isEmpty())
					itemInCrafter.shrink(1);
				else {
					insert_or_drop(remainingItem);
					itemInCrafter.shrink(itemInCrafter.getCount());
				}
			}
		}

		update();
	}

	public int getComparatorOutput() {
		int out = 0;
		for (int i = 0; i < 9; i++) {
			if (blocked[i] || !getItem(i).isEmpty()) {
				out++;
			}
		}
		return out;
	}

	public static void tick(Level world, BlockPos pos, BlockState state, CrafterBlockEntity be) {
		if(!be.didInitialScan && !world.isClientSide) {
			be.update();
			be.didInitialScan = true;
		}
	}

	public static ItemStack getResult(Level world, CraftingContainer craftingInventory) {
		Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftingInventory, world);
		if (optional.isPresent()) {
			CraftingRecipe craftingRecipe = optional.get();
			ItemStack stack = craftingRecipe.assemble(craftingInventory, world.registryAccess());
			if (stack.isItemEnabled(world.enabledFeatures())) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	}

	public void update() {
		ItemStack stack = CrafterBlockEntity.getResult(level, this);
		result.setItem(0, stack);
		level.updateNeighbourForOutputSignal(worldPosition, CrafterModule.block);
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}

	@Override
	public ItemStack getItem(int slot) {
		return stacks.get(slot);
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : stacks) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack removeItem(int slot, int amount) {
		ItemStack stack = ContainerHelper.removeItem(stacks, slot, amount);
		if (!stack.isEmpty()) {
			this.setChanged();
		}
		update();
		return stack;
	}

	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		ItemStack stack = ContainerHelper.takeItem(stacks, slot);
		update();
		return stack;
	}

	@Override
	public void setItem(int slot, ItemStack stack) {
		stacks.set(slot, stack);
		update();
	}

	@Override
	public int getContainerSize() {
		return 9;
	}

	@Override
	public void clearContent() {
		for (int i = 0; i < getContainerSize(); i++) {
			setItem(i, ItemStack.EMPTY);
		}
	}

	@Override
	protected AbstractContainerMenu createMenu(int syncId, Inventory playerInventory) {
		return new CrafterMenu(syncId, playerInventory, (it) -> new TransientCraftingContainer(it, 3, 3, stacks), result, delegate, ContainerLevelAccess.create(level, getBlockPos()));
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("block.quark.crafter");
	}

	@Override
	public void fillStackedContents(StackedContents finder) {
		for(ItemStack itemstack : stacks) {
			finder.accountSimpleStack(itemstack);
		}
	}

	@Override
	public int getWidth() {
		return 3;
	}

	@Override
	public int getHeight() {
		return 3;
	}

	@Override
	public List<ItemStack> getItems() {
		return stacks;
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return true;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction dir) {
		return canPlaceItem(slot, stack);
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		ItemStack stackInSlot = getItem(slot);
		boolean allowed = stackInSlot.isEmpty();

		if(!CrafterModule.useEmiLogic && !allowed) {
			int min = 999;
			for(int i = 0; i < 9; i++) {
				if(blocked[i])
					continue;

				ItemStack testStack = getItem(i);
				if(testStack.isEmpty() || ItemStack.isSameItemSameTags(stackInSlot, testStack))
					min = Math.min(min, testStack.getCount());
			}

			return stackInSlot.getCount() == min;
		}

		boolean blockedSlot = blocked[slot];
		boolean powered = level.getBlockState(worldPosition).getValue(CrafterBlock.POWER).powered();

		return allowed && !blockedSlot && (CrafterModule.allowItemsWhilePowered || !powered);
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		int ct = 0;
		for(boolean bl : blocked)
			if(!bl)
				ct++;

		int[] ret = new int[ct];
		int i = 0;
		for(int j = 0; j < blocked.length; j++)
			if(!blocked[j]) {
				ret[i] = j;
				i++;
			}

		return ret;
	}

	private static class CraftDispenseBehavior implements DispenseItemBehavior {

		@Override
		public final ItemStack dispense(BlockSource blockSource, ItemStack itemStack) {
			ItemStack itemStack2 = this.dispenseSilently(blockSource, itemStack);
			this.playSound(blockSource);
			this.spawnParticles(blockSource, blockSource.getBlockState().getValue(CrafterBlock.FACING));
			return itemStack2;
		}

		protected ItemStack dispenseSilently(BlockSource pointer, ItemStack stack) {
			Direction direction = pointer.getBlockState().getValue(CrafterBlock.FACING);
			Position position = getOutputLocation(pointer);
			spawnItem(pointer.getLevel(), stack, 6, direction, position);
			return stack;
		}

		public static void spawnItem(Level world, ItemStack stack, int speed, Direction side, Position pos) {
			double d = pos.x();
			double e = pos.y();
			double f = pos.z();
			if (side.getAxis() == Axis.Y) {
				e -= 0.125;
			} else {
				e -= 0.15625;
			}

			ItemEntity itemEntity = new ItemEntity(world, d, e, f, stack);
			double g = world.random.nextDouble() * 0.1 + 0.2;
			itemEntity.setDeltaMovement(world.random.triangle((double)side.getStepX() * g, 0.0172275 * (double)speed), world.random.triangle(0.2, 0.0172275 * (double)speed), world.random.triangle((double)side.getStepZ() * g, 0.0172275 * (double)speed));
			world.addFreshEntity(itemEntity);
		}

		protected void playSound(BlockSource pointer) {
			pointer.getLevel().levelEvent(1000, pointer.getPos(), 0);
		}

		protected void spawnParticles(BlockSource pointer, Direction side) {
			pointer.getLevel().levelEvent(2000, pointer.getPos(), side.get3DDataValue());
		}

		private static Position getOutputLocation(BlockSource pointer) {
			Direction direction = pointer.getBlockState().getValue(CrafterBlock.FACING);
			return pointer.getPos().getCenter().add(0.7 * (double)direction.getStepX(), 0.7 * (double)direction.getStepY(), 0.7 * (double)direction.getStepZ());
		}
	}
}
