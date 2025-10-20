package org.violetmoon.quark.base.network.message;

import io.netty.buffer.ByteBuf;
import org.violetmoon.quark.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.violetmoon.quark.base.handler.InventoryTransferHandler;
import org.violetmoon.quark.base.network.QuarkNetwork;

public record InventoryTransferMessage(boolean smart, boolean restock) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, InventoryTransferMessage> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.BOOL, InventoryTransferMessage::smart,
		ByteBufCodecs.BOOL, InventoryTransferMessage::restock,
	    InventoryTransferMessage::new
	);

	@Override
	public void handle(ServerPlayer player) {
		InventoryTransferHandler.transfer(player, restock, smart);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.INVENTORY_TRANSFER_MESSAGE;
	}
}
