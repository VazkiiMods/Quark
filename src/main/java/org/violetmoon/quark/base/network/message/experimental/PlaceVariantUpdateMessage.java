package org.violetmoon.quark.base.network.message.experimental;

import io.netty.buffer.ByteBuf;
import org.violetmoon.quark.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.content.experimental.module.VariantSelectorModule;

public record PlaceVariantUpdateMessage(String variant) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, PlaceVariantUpdateMessage> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
			.map(PlaceVariantUpdateMessage::new, PlaceVariantUpdateMessage::variant);

	@Override
	public void handle(ServerPlayer player) {
		VariantSelectorModule.setSavedVariant(player, variant);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.PLACE_VARIANT_UPDATE_MESSAGE;
	}
}
