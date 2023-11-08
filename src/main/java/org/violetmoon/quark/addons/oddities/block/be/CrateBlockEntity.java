package org.violetmoon.quark.addons.oddities.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import org.violetmoon.quark.addons.oddities.block.CrateBlock;
import org.violetmoon.quark.addons.oddities.capability.CrateItemHandler;
import org.violetmoon.quark.addons.oddities.inventory.CrateMenu;
import org.violetmoon.quark.addons.oddities.module.CrateModule;

import javax.annotation.Nonnull;

public class CrateBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {

	private int numPlayersUsing;

	private int[] visibleSlots = new int[0];

	protected final ContainerData crateData = new ContainerData() {
		@Override
		public int get(int index) {
			CrateItemHandler handler = itemHandler();
			return index == 0 ? handler.displayTotal : handler.displaySlots;
		}

		@Override
		public void set(int index, int value) {
			// NO-OP
		}

		@Override
		public int getCount() {
			return 2;
		}
	};

	public CrateBlockEntity(BlockPos pos, BlockState state) {
		super(CrateModule.blockEntityType, pos, state);
	}

	public void spillTheTea() {
		itemHandler().spill(level, worldPosition);
	}

	public static void tick(Level level, BlockPos pos, BlockState state, CrateBlockEntity be) {
		be.tick();
	}

	public void tick() {
		itemHandler().recalculate();
	}

	@Override
	protected void saveAdditional(@Nonnull CompoundTag compound) {
		super.saveAdditional(compound);

		compound.merge(itemHandler().serializeNBT());
	}

	@Override
	public void load(@Nonnull CompoundTag nbt) {
		super.load(nbt);

		itemHandler().deserializeNBT(nbt);
	}

	public CrateItemHandler itemHandler() {
		LazyOptional<IItemHandler> handler = getCapability(ForgeCapabilities.ITEM_HANDLER);
		if (handler.isPresent() && handler.orElse(new EmptyHandler()) instanceof CrateItemHandler crateHandler)
			return crateHandler;

		// Should never happen, but just to prevent null-pointers
		return new CrateItemHandler();
	}

	@Nonnull
	@Override
	public ItemStack getItem(int slot) {
		return itemHandler().getStackInSlot(slot);
	}

	@Nonnull
	@Override
	public ItemStack removeItemNoUpdate(int slot) {
		return itemHandler().extractItem(slot, 64, true);
	}

	@Override
	public void setItem(int slot, @Nonnull ItemStack stack) {
		itemHandler().setStackInSlot(slot, stack);
	}

	@Nonnull
	@Override
	public ItemStack removeItem(int slot, int count) {
		return itemHandler().extractItem(slot, 64, true);
	}

	@Override
	public int getContainerSize() {
		return itemHandler().getSlots();
	}

	@Override
	public void clearContent() {
		itemHandler().clear();
	}

	@Override
	public boolean isEmpty() {
		return itemHandler().isEmpty();
	}

	@Override
	public boolean canTakeItemThroughFace(int index, @Nonnull ItemStack stack, @Nonnull Direction dir) {
		return true;
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, @Nonnull ItemStack stack, Direction dir) {
		return itemHandler().getSlotLimit(index) > 0;
	}

	@Nonnull
	@Override
	public int[] getSlotsForFace(@Nonnull Direction dir) {
		int slotCount = itemHandler().getSlots();
		if (visibleSlots.length != slotCount) {
			visibleSlots = new int[slotCount];
			for (int i = 0; i < slotCount; i++) visibleSlots[i] = i;
		}
		return visibleSlots;
	}

	@Nonnull
	@Override
	protected Component getDefaultName() {
		return Component.translatable(CrateModule.crate.getDescriptionId());
	}

	@Nonnull
	@Override
	protected AbstractContainerMenu createMenu(int id, @Nonnull Inventory player) {
		return new CrateMenu(id, player, this, crateData);
	}

	@Nonnull
	@Override
	protected IItemHandler createUnSidedHandler() {
		return new CrateItemHandler();
	}

	// Vaniller copy =========================

	@Override
	public boolean stillValid(@Nonnull Player player) {
		if (this.level.getBlockEntity(this.worldPosition) != this) {
			return false;
		} else {
			return !(player.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) > 64.0D);
		}
	}

	@Override
	public void startOpen(Player player) {
		if (!player.isSpectator()) {
			if (this.numPlayersUsing < 0) {
				this.numPlayersUsing = 0;
			}

			++this.numPlayersUsing;
			BlockState blockstate = this.getBlockState();
			boolean flag = blockstate.getValue(CrateBlock.PROPERTY_OPEN);
			if (!flag) {
				this.playSound(blockstate, SoundEvents.BARREL_OPEN);
				level.gameEvent(player, GameEvent.CONTAINER_OPEN, worldPosition);
				this.setOpenProperty(blockstate, true);
			}

			this.scheduleTick();
		}
	}

	private void scheduleTick() {
		this.level.scheduleTick(this.getBlockPos(), this.getBlockState().getBlock(), 5);
	}

	public void crateTick() {
		int i = this.worldPosition.getX();
		int j = this.worldPosition.getY();
		int k = this.worldPosition.getZ();
		this.numPlayersUsing = calculatePlayersUsing(this.level, this, i, j, k);
		if (this.numPlayersUsing > 0)
			this.scheduleTick();
	}

	public static int calculatePlayersUsing(Level world, BaseContainerBlockEntity container, int x, int y, int z) {
		int i = 0;

		for(Player playerentity : world.getEntitiesOfClass(Player.class, new AABB((float)x - 5.0F, (float)y - 5.0F, (float)z - 5.0F, (float)(x + 1) + 5.0F, (float)(y + 1) + 5.0F, (float)(z + 1) + 5.0F))) {
			if (playerentity.containerMenu instanceof CrateMenu) {
				Container iinventory = ((CrateMenu)playerentity.containerMenu).crate;
				if (iinventory == container) {
					++i;
				}
			}
		}

		return i;
	}

	@Override
	public void stopOpen(Player player) {
		if (!player.isSpectator()) {
			--this.numPlayersUsing;
		}

		if(numPlayersUsing <= 0 ){
			BlockState blockstate = this.getBlockState();
			if (!blockstate.is(CrateModule.crate)) {
				this.setRemoved();
				return;
			}

			boolean flag = blockstate.getValue(CrateBlock.PROPERTY_OPEN);
			if (flag) {
				this.playSound(blockstate, SoundEvents.BARREL_CLOSE);
				level.gameEvent(player, GameEvent.CONTAINER_OPEN, worldPosition);
				this.setOpenProperty(blockstate, false);
			}
		}
	}

	private void setOpenProperty(BlockState state, boolean open) {
		BlockPos pos = this.getBlockPos();
		BlockState prev = level.getBlockState(pos);
		if (prev.is(state.getBlock()))
			level.setBlock(pos, state.setValue(CrateBlock.PROPERTY_OPEN, open), 3);
	}

	private void playSound(BlockState state, SoundEvent sound) {
		double d0 = (double)this.worldPosition.getX() + 0.5D;
		double d1 = (double)this.worldPosition.getY() + 0.5D;
		double d2 = (double)this.worldPosition.getZ() + 0.5D;
		this.level.playSound(null, d0, d1, d2, sound, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
	}

}
