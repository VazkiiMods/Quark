package org.violetmoon.quark.base.network.message.oddities;

import io.netty.buffer.ByteBuf;
import org.violetmoon.quark.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;

import org.violetmoon.quark.addons.oddities.block.be.MatrixEnchantingTableBlockEntity;
import org.violetmoon.quark.addons.oddities.inventory.MatrixEnchantingMenu;
import org.violetmoon.quark.base.network.QuarkNetwork;

public record MatrixEnchanterOperationMessage(int operation, int arg0, int arg1, int arg2) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, MatrixEnchanterOperationMessage> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, MatrixEnchanterOperationMessage::operation,
		ByteBufCodecs.INT, MatrixEnchanterOperationMessage::arg0,
		ByteBufCodecs.INT, MatrixEnchanterOperationMessage::arg1,
		ByteBufCodecs.INT, MatrixEnchanterOperationMessage::arg2,
	    MatrixEnchanterOperationMessage::new
	);

	@Override
	public void handle(ServerPlayer player) {
		AbstractContainerMenu container = player.containerMenu;

		if (container instanceof MatrixEnchantingMenu matrixMenu) {
			MatrixEnchantingTableBlockEntity enchanter = matrixMenu.enchanter;
			enchanter.onOperation(player, operation, arg0, arg1, arg2);
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.MATRIX_ENCHANTER_OPERATION_MESSAGE;
	}
}
