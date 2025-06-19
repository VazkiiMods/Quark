package org.violetmoon.quark.base.network.message;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.ContributorRewardHandler;
import org.violetmoon.quark.base.network.QuarkNetwork;

public record RequestEmoteMessage(String emote) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, RequestEmoteMessage> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
			.map(RequestEmoteMessage::new, RequestEmoteMessage::emote);

	@Override
	public void handle(ServerPlayer player) {
		if (player != null)
			Quark.ZETA.network.sendToAllPlayers(
					new DoEmoteMessage(emote, player.getUUID(), ContributorRewardHandler.getTier(player)),
					player.server);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.REQUEST_EMOTE_MESSAGE;
	}
}
