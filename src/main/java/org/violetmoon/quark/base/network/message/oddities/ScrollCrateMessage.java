package org.violetmoon.quark.base.network.message.oddities;

import io.netty.buffer.ByteBuf;
import org.violetmoon.quark.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.violetmoon.quark.addons.oddities.inventory.CrateMenu;
import org.violetmoon.quark.base.network.QuarkNetwork;

public record ScrollCrateMessage(boolean down) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, ScrollCrateMessage> STREAM_CODEC = ByteBufCodecs.BOOL
			.map(ScrollCrateMessage::new, ScrollCrateMessage::down);

	@Override
	public void handle(ServerPlayer player) {
		AbstractContainerMenu container = player.containerMenu;

		if (container instanceof CrateMenu crate)
			crate.scroll(down, false);
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.SCROLL_CRATE_MESSAGE;
	}
}
