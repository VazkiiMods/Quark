package org.violetmoon.quark.addons.oddities.block.be;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.addons.oddities.block.CrateBlock;
import org.violetmoon.quark.addons.oddities.inventory.CrateMenu;
import org.violetmoon.quark.addons.oddities.module.CrateModule;
import org.violetmoon.quark.base.handler.SortingHandler;

import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
public class CrateBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    boolean needsUpdate = true;
    private int numPlayersUsing;
    private int[] visibleSlots = new int[0];

    public int displayTotal = 0;
    public int displaySlots = 0;

    private int cachedTotal = -1;

    private NonNullList<ItemStack> items = NonNullList.withSize(640, ItemStack.EMPTY);

    ContainerData crateData = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? displayTotal : displaySlots;
        }

        @Override
        public void set(int index, int value) {

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
        List<ItemStack> stacks = new ArrayList<>(this.items);
        SortingHandler.mergeStacks(stacks);

        for(ItemStack stack : stacks)
            if(!stack.isEmpty())
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CrateBlockEntity be) {
        be.tick();
    }

    public void tick() {
        this.recalculate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.merge(ContainerHelper.saveAllItems(tag, items, provider));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, provider);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction dir) {
        return true;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack stack, @Nullable Direction dir) {
        return getSlotLimit(index) > 0;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return super.canPlaceItem(slot, stack);
    }

    @Override
    public int[] getSlotsForFace(@NotNull Direction direction) {
        int slotCount = items.size();
        if(visibleSlots.length != slotCount) {
            visibleSlots = new int[slotCount];
            for(int i = 0; i < slotCount; i++)
                visibleSlots[i] = i;
        }
        return visibleSlots;
    }


    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    public void setItem(int slot, ItemStack stack) {
        ItemStack oldItem = getItem(slot);
        super.setItem(slot, stack);

        changeTotal(oldItem, stack);
    }

    @NotNull
    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack oldStack = ContainerHelper.removeItem(items, slot, count);
        ItemStack newStack = getItem(slot);
        changeTotal(oldStack, newStack);

        return oldStack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = super.removeItemNoUpdate(slot);
        changeTotal(stack, getItem(slot));
        return stack;
    }

    public ItemStack removeItem(int slot) {
        return removeItem(slot, 64);
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    private void changeTotal(ItemStack oldStack, ItemStack newStack) {
        int diff = newStack.getCount() - oldStack.getCount();
        if(diff != 0)
            changeTotal(diff);
    }

    private void changeTotal(int change) {
        cachedTotal = getTotal() + change;
        needsUpdate = true;
    }

    public int getTotal() {
        if(cachedTotal != -1)
            return cachedTotal;

        int count = 0;
        for(ItemStack stack : items)
            count += stack.getCount();

        cachedTotal = count;
        return count;
    }

    public void refreshCachedTotal() {
        cachedTotal = -1;
        getTotal();
    }

    public int getSlotLimit(int slot) {
        ItemStack stackInSlot = getItem(slot);
        int total = getTotal();
        return Mth.clamp(stackInSlot.getCount() + CrateModule.maxItems - total, 0, 64);
    }


    @Override
    protected Component getDefaultName() {
        return Component.translatable(CrateModule.crate.getDescriptionId());
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(@NotNull NonNullList<ItemStack> list) {
        this.items = list;
        cachedTotal = -1;
    }


    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inv) {
        return new CrateMenu(id, inv, this, crateData);
    }

    public void recalculate() {
        needsUpdate = false;

        displayTotal = 0;
        displaySlots = 0;

        NonNullList<ItemStack> newStacks = NonNullList.withSize(CrateModule.maxItems, ItemStack.EMPTY);
        int idx = 0;
        for(ItemStack stack : items) {
            if(!stack.isEmpty()) {
                newStacks.set(idx, stack);
                displayTotal += stack.getCount();
                displaySlots++;
                idx++;
            }
        }

        items = newStacks;
        cachedTotal = -1;
    }

    public void clearContent() {
        needsUpdate = false;
        items = NonNullList.withSize(CrateModule.maxItems, ItemStack.EMPTY);
        displayTotal = 0;
        displaySlots = 0;
    }

    // THE VANILLA COPY

    @Override
    public boolean stillValid(@NotNull Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void startOpen(Player player) {
        if (!player.isSpectator()) {
            if (this.numPlayersUsing < 0) {
                this.numPlayersUsing = 0;
            }

            ++this.numPlayersUsing;
            BlockState blockstate = this.getBlockState();
            boolean isOpen  = blockstate.getValue(CrateBlock.PROPERTY_OPEN);
            if (!isOpen) {
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
        int x = this.worldPosition.getX();
        int y = this.worldPosition.getY();
        int z = this.worldPosition.getZ();
        this.numPlayersUsing = calculatePlayersUsing(this.getLevel(), this, x, y, z);
        if (this.numPlayersUsing > 0)
            this.scheduleTick();
    }

    public static int calculatePlayersUsing(Level world, BaseContainerBlockEntity container, int x, int y, int z) {
        int playersUsing = 0;

        for (Player playerentity : world.getEntitiesOfClass(Player.class, new AABB((float) x - 5.0F, (float) y - 5.0F, (float) z - 5.0F, (float) (x + 1) + 5.0F, (float) (y + 1) + 5.0F, (float) (z + 1) + 5.0F))) {
            if (playerentity.containerMenu instanceof CrateMenu) {
                Container iinventory = ((CrateMenu) playerentity.containerMenu).crate;
                if (iinventory == container) {
                    ++playersUsing;
                }
            }
        }

        return playersUsing;
    }

    @Override
    public void stopOpen(Player player) {
        if (!player.isSpectator()) {
            --this.numPlayersUsing;
        }

        if (numPlayersUsing <= 0) {
            BlockState blockState = this.getBlockState();
            if (!blockState.is(CrateModule.crate)) {
                this.setRemoved();
                return;
            }

            boolean isOpen = blockState.getValue(CrateBlock.PROPERTY_OPEN);
            if (isOpen) {
                this.playSound(blockState, SoundEvents.BARREL_CLOSE);
                level.gameEvent(player, GameEvent.CONTAINER_OPEN, worldPosition);
                this.setOpenProperty(blockState, false);
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
        double x = (double) this.worldPosition.getX() + 0.5D;
        double y = (double) this.worldPosition.getY() + 0.5D;
        double z = (double) this.worldPosition.getZ() + 0.5D;
        this.level.playSound(null, x, y, z, sound, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
    }
}
