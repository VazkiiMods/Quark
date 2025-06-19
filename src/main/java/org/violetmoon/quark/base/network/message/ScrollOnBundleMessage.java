package org.violetmoon.quark.base.network.message;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.content.management.module.ExpandedItemInteractionsModule;

public record ScrollOnBundleMessage(int containerId, int stateId, int slotNum, double scrollDelta) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, ScrollOnBundleMessage> STREAM_CODEC = StreamCodec.composite(
	    ByteBufCodecs.INT, ScrollOnBundleMessage::containerId,
		ByteBufCodecs.INT, ScrollOnBundleMessage::stateId,
		ByteBufCodecs.INT, ScrollOnBundleMessage::slotNum,
		ByteBufCodecs.DOUBLE, ScrollOnBundleMessage::scrollDelta,
	    ScrollOnBundleMessage::new
	);

	@Override
	public void handle(ServerPlayer player) {
		ExpandedItemInteractionsModule.scrollOnBundle(player, containerId, stateId, slotNum, scrollDelta);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.SCROLL_ON_BUNDLE_MESSAGE;
	}
}
