package org.violetmoon.quark.base.network.message;

import io.netty.buffer.ByteBuf;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.content.tweaks.module.SimpleHarvestModule;

public record HarvestMessage(BlockPos pos, InteractionHand hand) implements ServerboundPacketPayload {
	public static final StreamCodec<ByteBuf, HarvestMessage> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, HarvestMessage::pos,
		CatnipStreamCodecs.HAND, HarvestMessage::hand,
	    HarvestMessage::new
	);

	@Override
	public void handle(ServerPlayer player) {
		if (player != null) {
			BlockHitResult pick = Item.getPlayerPOVHitResult(player.level(), player, ClipContext.Fluid.ANY);
			SimpleHarvestModule.click(player, hand, pos, pick);
		}
	}

	@Override
	public PacketTypeProvider getTypeProvider() {
		return QuarkNetwork.HARVEST_MESSAGE;
	}
}
