package org.violetmoon.quark.base.network.message;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.content.tweaks.module.LockRotationModule;
import org.violetmoon.quark.content.tweaks.module.LockRotationModule.LockProfile;

public record SetLockProfileMessage(LockProfile profile) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, SetLockProfileMessage> STREAM_CODEC = LockProfile.STREAM_CODEC
			.map(SetLockProfileMessage::new, SetLockProfileMessage::profile);

	@Override
	public void handle(ServerPlayer player) {
		LockRotationModule.setProfile(player, profile);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.SET_LOCK_PROFILE_MESSAGE;
	}
}
