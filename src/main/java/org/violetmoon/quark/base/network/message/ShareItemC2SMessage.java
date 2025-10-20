package org.violetmoon.quark.base.network.message;

import net.neoforged.neoforge.network.PacketDistributor;
import org.violetmoon.quark.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.content.management.module.ItemSharingModule;

// The client, requesting "hey I'd like to share this item"
public record ShareItemC2SMessage(ItemStack toShare) implements ServerboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ShareItemC2SMessage> STREAM_CODEC = ItemStack.STREAM_CODEC
			.map(ShareItemC2SMessage::new, ShareItemC2SMessage::toShare);

	@Override
	public void handle(ServerPlayer player) {
		MinecraftServer server = player.getServer();
		if(server == null)
			return;

		if(!ItemSharingModule.canShare(player.getUUID(), server))
			return;

		Component senderName = player.getDisplayName();

		PacketDistributor.sendToAllPlayers(new ShareItemS2CMessage(player.getUUID(), senderName, toShare));
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.SHARE_ITEM_C2S_MESSAGE;
	}
}
