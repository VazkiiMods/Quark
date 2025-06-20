package org.violetmoon.quark.base.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.content.tweaks.module.DoubleDoorOpeningModule;
import org.violetmoon.quark.catnip.net.base.ServerboundPacketPayload;

public record DoubleDoorMessage(BlockPos pos) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, DoubleDoorMessage> STREAM_CODEC = BlockPos.STREAM_CODEC
			.map(DoubleDoorMessage::new, DoubleDoorMessage::pos);

	private Level extractWorld(ServerPlayer entity) {
		return entity == null ? null : entity.level();
	}

	@Override
	public void handle(ServerPlayer serverPlayer) {
		Quark.ZETA.modules.get(DoubleDoorOpeningModule.class)
				.openBlock(extractWorld(serverPlayer), serverPlayer, pos);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.DOUBLE_DOOR_MESSAGE;
	}
}
