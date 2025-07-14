package org.violetmoon.quark.base.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.content.tweaks.module.LockRotationModule;
import org.violetmoon.quark.content.tweaks.module.LockRotationModule.LockProfile;

public record SetLockProfileMessage(LockProfile profile) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, SetLockProfileMessage> STREAM_CODEC = LockProfile.STREAM_CODEC
			.map(SetLockProfileMessage::new, SetLockProfileMessage::profile);

	public static final StreamCodec<FriendlyByteBuf, SetLockProfileMessage> OPTIONAL_STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SetLockProfileMessage decode(@NotNull FriendlyByteBuf byteBuf) {
			SetLockProfileMessage message;

			if (byteBuf.readBoolean()) try {
				message = STREAM_CODEC.decode(byteBuf);
			} catch (NullPointerException nullPointerException) {
				message = new SetLockProfileMessage(null);
			}
			else {
				message = new SetLockProfileMessage(null);
			}


			return message;
		}

        @Override
        public void encode(@NotNull FriendlyByteBuf byteBuf, @NotNull SetLockProfileMessage setLockProfileMessage) {
			byteBuf.writeBoolean(setLockProfileMessage.profile != null);
			if (setLockProfileMessage.profile != null) STREAM_CODEC.encode(byteBuf, setLockProfileMessage);
        }
    };

	@Override
	public void handle(ServerPlayer player) {
		LockRotationModule.setProfile(player, profile);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.SET_LOCK_PROFILE_MESSAGE;
	}
}
