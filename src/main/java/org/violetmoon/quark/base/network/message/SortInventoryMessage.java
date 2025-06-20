package org.violetmoon.quark.base.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.violetmoon.quark.base.handler.SortingHandler;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.catnip.net.base.ServerboundPacketPayload;

public record SortInventoryMessage(boolean forcePlayer) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, SortInventoryMessage> STREAM_CODEC = ByteBufCodecs.BOOL
			.map(SortInventoryMessage::new, SortInventoryMessage::forcePlayer);

	@Override
	public void handle(ServerPlayer player) {
		SortingHandler.sortInventory(player, forcePlayer);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.SORT_INVENTORY_MESSAGE;
	}
}
