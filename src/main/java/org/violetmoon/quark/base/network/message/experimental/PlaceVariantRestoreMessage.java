package org.violetmoon.quark.base.network.message.experimental;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.content.experimental.module.VariantSelectorModule;

public record PlaceVariantRestoreMessage(String variant) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, PlaceVariantRestoreMessage> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
			.map(PlaceVariantRestoreMessage::new, PlaceVariantRestoreMessage::variant);

	@Override
	public void handle(LocalPlayer player) {
		VariantSelectorModule.Client.setClientVariant(variant, false);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.PLACE_VARIANT_RESTORE_MESSAGE;
	}
}
