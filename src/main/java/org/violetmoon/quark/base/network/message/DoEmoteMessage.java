package org.violetmoon.quark.base.network.message;

import io.netty.buffer.ByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.violetmoon.quark.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.content.tweaks.client.emote.EmoteHandler;

import java.util.UUID;

public record DoEmoteMessage(String emote, UUID playerUUID, int tier) implements ClientboundPacketPayload {
	public static final StreamCodec<ByteBuf, DoEmoteMessage> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.STRING_UTF8, DoEmoteMessage::emote,
		UUIDUtil.STREAM_CODEC, DoEmoteMessage::playerUUID,
		ByteBufCodecs.INT, DoEmoteMessage::tier,
	    DoEmoteMessage::new
	);

	@Override
	@OnlyIn(Dist.CLIENT)
	public void handle(LocalPlayer localPlayer) {
		Level world = Minecraft.getInstance().level;
		Player player = world.getPlayerByUUID(playerUUID);
		EmoteHandler.putEmote(player, emote, tier);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.DO_EMOTE_MESSAGE;
	}
}
