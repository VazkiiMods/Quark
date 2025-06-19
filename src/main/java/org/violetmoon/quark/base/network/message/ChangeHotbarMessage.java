package org.violetmoon.quark.base.network.message;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.violetmoon.quark.base.network.QuarkNetwork;

public record ChangeHotbarMessage(int bar) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, ChangeHotbarMessage> STREAM_CODEC = ByteBufCodecs.INT
			.map(ChangeHotbarMessage::new, ChangeHotbarMessage::bar);

	public void swap(Container inv, int slot1, int slot2) {
		ItemStack stack1 = inv.getItem(slot1);
		ItemStack stack2 = inv.getItem(slot2);
		inv.setItem(slot2, stack1);
		inv.setItem(slot1, stack2);
	}

	@Override
	public void handle(ServerPlayer player) {
		if(bar > 0 && bar <= 3)
			for(int i = 0; i < 9; i++)
				swap(player.getInventory(), i, i + bar * 9);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.CHANGE_HOTBAR_MESSAGE;
	}
}
