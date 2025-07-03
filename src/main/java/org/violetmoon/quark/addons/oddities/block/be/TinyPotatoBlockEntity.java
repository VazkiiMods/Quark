package org.violetmoon.quark.addons.oddities.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.addons.oddities.block.TinyPotatoBlock;
import org.violetmoon.quark.addons.oddities.module.TinyPotatoModule;
import org.violetmoon.quark.addons.oddities.util.TinyPotatoInfo;
import org.violetmoon.quark.base.handler.QuarkSounds;
import org.violetmoon.zeta.util.MiscUtil;
import org.violetmoon.zeta.util.SimpleInventoryBlockEntity;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TinyPotatoBlockEntity extends SimpleInventoryBlockEntity implements Nameable {

	public static final String TAG_NAME = "name";
	public static final String TAG_ANGRY = TinyPotatoBlock.ANGRY;
	private static final int JUMP_EVENT = 0;

	public int jumpTicks = 0;
	public Component name = Component.literal("");
	private int soundCd = 0;
	public boolean angry = false;

	private static final Map<String, String> GENDER = new HashMap<>();
	private static final Map<String, SoundEvent> SOUNDS = new HashMap<>();

	static {
		GENDER.put("girlstater", "daughter");
		GENDER.put("lesbiabtater", "daughter");
		GENDER.put("lesbiamtater", "daughter");
		GENDER.put("lesbiantater", "daughter");
		GENDER.put("lesbitater", "daughter");
		GENDER.put("lessbientater", "daughter");

		GENDER.put("agendertater", "child");
		GENDER.put("enbytater", "child");
		GENDER.put("nbtater", "child");
		GENDER.put("nonbinarytater", "child");
		GENDER.put("robotater", "child");
		GENDER.put("wiretater", "child");
		GENDER.put("eutrotater", "child");
		GENDER.put("bob", "child");
		GENDER.put("snences", "child");

		GENDER.put("genderfluidtater", "child");
		GENDER.put("taterfluid", "child");
		GENDER.put("eggtater", "child");
		GENDER.put("tategg", "child");
		GENDER.put("transtater", "child");

		GENDER.put("manytater", "children");
		GENDER.put("pluraltater", "children");
		GENDER.put("snorps", "children");
		GENDER.put("systater", "children");
		GENDER.put("systemtater", "children");

		// The best gender
		GENDER.put("tomater", "tomato");

		SOUNDS.put("shia labeouf", QuarkSounds.BLOCK_POTATO_DO_IT);
		SOUNDS.put("joe biden", QuarkSounds.BLOCK_POTATO_SODA);
		SOUNDS.put("yungnickyoung", QuarkSounds.BLOCK_POTATO_YUNG);
	}

	public TinyPotatoBlockEntity(BlockPos pos, BlockState state) {
		super(TinyPotatoModule.blockEntityType, pos, state);
	}

	public void interact(Player player, InteractionHand hand, ItemStack stack, Direction side) {
		int index = side.get3DDataValue();
		if(index >= 0) {
			ItemStack stackAt = getItem(index);
			if(!stackAt.isEmpty() && stack.isEmpty()) {
				player.setItemInHand(hand, stackAt);
				setItem(index, ItemStack.EMPTY);
			} else if(!stack.isEmpty()) {
				ItemStack copy = stack.split(1);

				if(stack.isEmpty()) {
					player.setItemInHand(hand, stackAt);
				} else if(!stackAt.isEmpty()) {
					player.getInventory().placeItemBackInInventory(stackAt);
				}

				setItem(index, copy);
			}
		}

		if(level != null && !level.isClientSide) {
			jump();

			if(hasCustomName()) {
				TinyPotatoInfo info = TinyPotatoInfo.fromComponent(name);

				String checkName = info.name().toLowerCase(Locale.ROOT).trim();
				if(SOUNDS.containsKey(checkName) && soundCd == 0) {
					SoundEvent playSound = SOUNDS.get(checkName);
					soundCd = 20;
					level.playSound(null, worldPosition, playSound, SoundSource.BLOCKS, 1F, 1F);
				}
			}

			ItemStack tater = ItemStack.EMPTY;
			boolean manyTater = false;
			for(int i = 0; i < getContainerSize(); i++) {
				ItemStack stackAt = getItem(i);
				if(!stackAt.isEmpty() && stackAt.is(TinyPotatoModule.tiny_potato.asItem())) {
					if(tater.isEmpty())
						tater = stackAt;
					else {
						manyTater = true;
						break;
					}
				}
			}
			if(!tater.isEmpty()) {
				String taterGender = manyTater ? "children" : "son";
				if(tater.getComponents().has(DataComponents.CUSTOM_NAME) && !manyTater) {
					TinyPotatoInfo info = TinyPotatoInfo.fromComponent(tater.getHoverName());
					taterGender = GENDER.getOrDefault(info.name(), taterGender);
				}
				if(player instanceof ServerPlayer serverPlayer)
					serverPlayer.sendSystemMessage(Component.translatable("quark.misc.my_" + taterGender), true);
			}
		}
	}

	private void jump() {
		if(level != null && jumpTicks == 0) {
			level.blockEvent(getBlockPos(), getBlockState().getBlock(), JUMP_EVENT, 20);
		}
	}

	@Override
	public boolean triggerEvent(int id, int param) {
		if(id == JUMP_EVENT) {
			jumpTicks = param;
			return true;
		} else {
			return super.triggerEvent(id, param);
		}
	}

	public static void commonTick(Level level, BlockPos pos, BlockState state, TinyPotatoBlockEntity self) {
		if(self.jumpTicks > 0) {
			self.jumpTicks--;
		}

		if(!level.isClientSide) {
			if(level.random.nextInt(100) == 0) {
				self.jump();
			}
			if(self.soundCd > 0) {
				self.soundCd--;
			}
		}
	}

	@Override
	public void inventoryChanged(int i) {
		sync();
	}

	@Override
	public void setChanged() {
		super.setChanged();
		if(level != null && !level.isClientSide) {
			sync();
		}
	}

	@Override
	public void sync() {
		MiscUtil.syncTE(this);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public void readSharedNBT(CompoundTag tag, HolderLookup.Provider provider) {
		super.readSharedNBT(tag, provider);
		name = Component.Serializer.fromJson(tag.getString(TAG_NAME), provider);
		angry = tag.getBoolean(TAG_ANGRY);
	}

	@Override
	public void writeSharedNBT(CompoundTag tag, HolderLookup.Provider provider) {
		super.writeSharedNBT(tag, provider);
		tag.putString(TAG_NAME, Component.Serializer.toJson(name, provider));
		tag.putBoolean(TAG_ANGRY, angry);
	}

	@Override
	public int getContainerSize() {
		return 6;
	}

	@Override
	public int getMaxStackSize() {
		return 1;
	}

	@Override
	public boolean canPlaceItem(int slot, @NotNull ItemStack itemstack) {
		return this.getItem(slot).isEmpty();
	}

	@Override
	public void startOpen(@NotNull Player player) {

	}

	@Override
	public void stopOpen(@NotNull Player player) {

	}

	@NotNull
	@Override
	public Component getName() {
		return Component.translatable(TinyPotatoModule.tiny_potato.getDescriptionId());
	}

	@Nullable
	@Override
	public Component getCustomName() {
		return name.getString().isEmpty() ? null : name;
	}

	@NotNull
	@Override
	public Component getDisplayName() {
		if(hasCustomName()) {
			Component customName = getCustomName();
			if(customName != null)
				return customName;
		}
		return getName();
	}
}
