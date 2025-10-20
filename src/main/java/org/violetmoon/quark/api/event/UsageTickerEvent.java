package org.violetmoon.quark.api.event;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class UsageTickerEvent extends Event implements ICancellableEvent {

	public final EquipmentSlot slot;
	public final ItemStack currentStack;
	public final ItemStack currentRealStack;
	public final int currentCount;
	public final Pass pass;
	public final Player player;

	public UsageTickerEvent(EquipmentSlot slot, ItemStack currentStack, ItemStack currentRealStack, int currentCount, boolean isRender, Player player) {
		this.slot = slot;
		this.currentStack = currentStack;
		this.currentRealStack = currentRealStack;
		this.currentCount = currentCount;
		this.pass = (isRender ? Pass.RENDERING : Pass.LOGICAL);
		this.player = player;
	}

	public static enum Pass {
		LOGICAL, RENDERING
	}

	public static class GetStack extends UsageTickerEvent {

		private ItemStack resultStack;

		public GetStack(EquipmentSlot slot, ItemStack currentStack, ItemStack currentRealStack, int currentCount, boolean isRender, Player player) {
			super(slot, currentStack, currentRealStack, currentCount, isRender, player);

			resultStack = currentStack;
		}

		public ItemStack getResultStack() {
			return resultStack;
		}

		public void setResultStack(ItemStack resultStack) {
			this.resultStack = resultStack;
		}

	}

	public static class GetCount extends UsageTickerEvent {

		private int resultCount;

		public GetCount(EquipmentSlot slot, ItemStack currentStack, ItemStack currentRealStack, int currentCount, boolean isRender, Player player) {
			super(slot, currentStack, currentRealStack, currentCount, isRender, player);

			resultCount = currentCount;
		}

		public int getResultCount() {
			return resultCount;
		}

		public void setResultCount(int resultCount) {
			this.resultCount = resultCount;
		}
	}
}
