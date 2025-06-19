package org.violetmoon.quark.content.tweaks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.content.tweaks.module.DyeableItemFramesModule;

public class DyedItemFrame extends ItemFrame {

	private static final String TAG_COLOR = "q_color";
	private static final String TAG_GLOW = "q_glow";

	private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(DyedItemFrame.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Boolean> DATA_GLOW = SynchedEntityData.defineId(DyedItemFrame.class, EntityDataSerializers.BOOLEAN);

	public DyedItemFrame(EntityType<? extends DyedItemFrame> p_149607_, Level p_149608_) {
		super(p_149607_, p_149608_);
	}

	public DyedItemFrame(Level level, BlockPos pos, Direction direction, int color, boolean glow) {
		super(DyeableItemFramesModule.entityType, level, pos, direction);

		getEntityData().set(DATA_COLOR, color);
		getEntityData().set(DATA_GLOW, glow);
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_COLOR, 0);
		builder.define(DATA_GLOW, false);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag cmp) {
		super.addAdditionalSaveData(cmp);

		cmp.putInt(TAG_COLOR, getColor());
		cmp.putBoolean(TAG_GLOW, isGlow());
	}

	@Override
	public void readAdditionalSaveData(CompoundTag cmp) {
		super.readAdditionalSaveData(cmp);

		getEntityData().set(DATA_COLOR, cmp.getInt(TAG_COLOR));
		getEntityData().set(DATA_GLOW, cmp.getBoolean(TAG_GLOW));
	}

	public Integer getColor() {
		return getEntityData().get(DATA_COLOR);
	}

	public boolean isGlow() {
		return getEntityData().get(DATA_GLOW);
	}

	@Override
	public SoundEvent getRemoveItemSound() {
		return isGlow() ? SoundEvents.GLOW_ITEM_FRAME_REMOVE_ITEM : super.getRemoveItemSound();
	}

	@Override
	public SoundEvent getBreakSound() {
		return isGlow() ? SoundEvents.GLOW_ITEM_FRAME_BREAK : super.getRemoveItemSound();
	}

	@Override
	public SoundEvent getPlaceSound() {
		return isGlow() ? SoundEvents.GLOW_ITEM_FRAME_PLACE : super.getRemoveItemSound();
	}

	@Override
	public SoundEvent getAddItemSound() {
		return isGlow() ? SoundEvents.GLOW_ITEM_FRAME_ADD_ITEM : super.getRemoveItemSound();
	}

	@Override
	public SoundEvent getRotateItemSound() {
		return isGlow() ? SoundEvents.GLOW_ITEM_FRAME_ROTATE_ITEM : super.getRemoveItemSound();
	}

	@Override
	protected ItemStack getFrameItemStack() {
		ItemStack stack = new ItemStack(isGlow() ? Items.GLOW_ITEM_FRAME : Items.ITEM_FRAME);
		Quark.ZETA.dyeables.applyDye(stack, new DyedItemColor(getColor(), false));

		return stack;
	}

}
