package org.violetmoon.quark.base.network.message;

import net.createmod.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.content.management.module.ItemSharingModule;

import java.util.UUID;

public record ShareItemS2CMessage(UUID senderUuid, Component senderName, ItemStack stack) implements ClientboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, ShareItemS2CMessage> STREAM_CODEC = StreamCodec.composite(
		UUIDUtil.STREAM_CODEC, ShareItemS2CMessage::senderUuid,
		ComponentSerialization.STREAM_CODEC, ShareItemS2CMessage::senderName,
	    ItemStack.STREAM_CODEC, ShareItemS2CMessage::stack,
	    ShareItemS2CMessage::new
	);

	@Override
	public void handle(LocalPlayer player) {
		if (Minecraft.getInstance().isBlocked(senderUuid))
			return;

		Minecraft.getInstance().gui.getChat().addMessage(
				Component.translatable("chat.type.text", senderName, ItemSharingModule.createStackComponent(stack)),
				null,
				new GuiMessageTag(0xDEB483, null, null, "Quark shared item")
		);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.SHARE_ITEM_S2C_MESSAGE;
	}
}
