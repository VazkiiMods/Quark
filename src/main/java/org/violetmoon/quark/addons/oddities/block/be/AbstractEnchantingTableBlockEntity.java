package org.violetmoon.quark.addons.oddities.block.be;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.zeta.util.MiscUtil;
import org.violetmoon.zeta.util.SimpleInventoryBlockEntity;

import java.util.Random;

public abstract class AbstractEnchantingTableBlockEntity extends SimpleInventoryBlockEntity implements Nameable {

	public int tickCount;
	public float pageFlip, pageFlipPrev, flipT, flipA, bookSpread, bookSpreadPrev, bookRotation, bookRotationPrev, tRot;

	private static final Random rand = new Random();
	private Component customName;

	public AbstractEnchantingTableBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
	}

	@Override
	public int getContainerSize() {
		return 3;
	}

	@Override
	public boolean isAutomationEnabled() {
		return false;
	}

	@Override
	protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.saveAdditional(tag, provider);

		if(hasCustomName())
			tag.putString("CustomName", Component.Serializer.toJson(customName, provider));
	}

	@Override
	protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
		super.loadAdditional(tag, provider);

		if(tag.contains("CustomName", 8))
			customName = Component.Serializer.fromJson(tag.getString("CustomName"), provider);
	}

	public void tick() {
		performVanillaUpdate();
	}

	private void performVanillaUpdate() {
		this.bookSpreadPrev = this.bookSpread;
		this.bookRotationPrev = this.bookRotation;
		Player entityplayer = this.level.getNearestPlayer((this.worldPosition.getX() + 0.5F), (this.worldPosition.getY() + 0.5F), (this.worldPosition.getZ() + 0.5F), 3.0D, false);

		if(entityplayer != null) {
			double d0 = entityplayer.getX() - (this.worldPosition.getX() + 0.5F);
			double d1 = entityplayer.getZ() - (this.worldPosition.getZ() + 0.5F);
			this.tRot = (float) Mth.atan2(d1, d0);
			this.bookSpread += 0.1F;

			if(this.bookSpread < 0.5F || rand.nextInt(40) == 0) {
				float f1 = this.flipT;

				do {
					this.flipT += (rand.nextInt(4) - rand.nextInt(4));
				} while(!(f1 != this.flipT));
			}
		} else {
			this.tRot += 0.02F;
			this.bookSpread -= 0.1F;
		}

		while(this.bookRotation >= (float) Math.PI) {
			this.bookRotation -= ((float) Math.PI * 2F);
		}

		while(this.bookRotation < -(float) Math.PI) {
			this.bookRotation += ((float) Math.PI * 2F);
		}

		while(this.tRot >= (float) Math.PI) {
			this.tRot -= ((float) Math.PI * 2F);
		}

		while(this.tRot < -(float) Math.PI) {
			this.tRot += ((float) Math.PI * 2F);
		}

		float f2 = this.tRot - this.bookRotation;

		while(f2 >= Math.PI)
			f2 -= (Math.PI * 2F);

		while(f2 < -Math.PI)
			f2 += (Math.PI * 2F);

		this.bookRotation += f2 * 0.4F;
		this.bookSpread = Mth.clamp(this.bookSpread, 0.0F, 1.0F);
		++this.tickCount;
		this.pageFlipPrev = this.pageFlip;
		float f = (this.flipT - this.pageFlip) * 0.4F;
		f = Mth.clamp(f, -0.2F, 0.2F);
		this.flipA += (f - this.flipA) * 0.9F;
		this.pageFlip += this.flipA;
	}

	public void dropItem(int i) {
		ItemStack stack = getItem(i);
		if(!stack.isEmpty())
			Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
	}

	@NotNull
	@Override
	public Component getName() {
		return hasCustomName() ? customName : Component.translatable("container.enchant");
	}

	@Override
	public boolean hasCustomName() {
		return customName != null;
	}

	public void setCustomName(Component customNameIn) {
		customName = customNameIn;
	}

	@Override
	public void inventoryChanged(int i) {
        sync();
	}

	@Override
	protected boolean needsToSyncInventory() {
		return true;
	}

	@Override
	public void sync() {
		MiscUtil.syncTE(this);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

}
