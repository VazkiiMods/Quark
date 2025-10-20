package org.violetmoon.quark.base.network.message.oddities;

import io.netty.buffer.ByteBuf;
import org.violetmoon.quark.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.violetmoon.quark.addons.oddities.inventory.BackpackMenu;
import org.violetmoon.quark.base.network.QuarkNetwork;

public record HandleBackpackMessage(boolean open) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, HandleBackpackMessage> STREAM_CODEC = ByteBufCodecs.BOOL
			.map(HandleBackpackMessage::new, HandleBackpackMessage::open);

	@Override
	public void handle(ServerPlayer player) {
		if(open) {
			ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
			if(stack.getItem() instanceof MenuProvider && player.containerMenu != null) {
				ItemStack holding = player.containerMenu.getCarried().copy();
				player.containerMenu.setCarried(ItemStack.EMPTY);
				player.openMenu((MenuProvider) stack.getItem());
				player.containerMenu.setCarried(holding);
			}
		} else {
			if(player.containerMenu != null) {
				ItemStack holding = player.containerMenu.getCarried();
				player.containerMenu.setCarried(ItemStack.EMPTY);

				BackpackMenu.saveCraftingInventory(player);
				player.containerMenu = player.inventoryMenu;
				player.inventoryMenu.setCarried(holding);
			}
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.HANDLE_BACKPACK_MESSAGE;
	}
}
