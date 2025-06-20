package org.violetmoon.quark.base.network.message;

import org.violetmoon.quark.catnip.net.base.ClientboundPacketPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.violetmoon.quark.base.network.QuarkNetwork;

public record UpdateTridentMessage(int tridentID, ItemStack stack) implements ClientboundPacketPayload {
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTridentMessage> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, UpdateTridentMessage::tridentID, 
	    ItemStack.STREAM_CODEC, UpdateTridentMessage::stack,
		UpdateTridentMessage::new
	);

	@Override
	public void handle(LocalPlayer player) {
		Level level = Minecraft.getInstance().level;
		if (level != null) {
			Entity entity = level.getEntity(tridentID);
			if (entity instanceof ThrownTrident trident) {
				trident.pickupItemStack = stack;
			}
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.UPDATE_TRIDENT_MESSAGE;
	}
}
