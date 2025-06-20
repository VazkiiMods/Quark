package org.violetmoon.quark.base.network.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import org.violetmoon.quark.base.network.QuarkNetwork;
import org.violetmoon.quark.catnip.net.base.ServerboundPacketPayload;
import org.violetmoon.quark.content.tweaks.module.SimpleHarvestModule;

public record HarvestMessage(BlockPos pos, InteractionHand hand) implements ServerboundPacketPayload {
	private static final StreamCodec<ByteBuf, InteractionHand> HAND_STREAM_CODEC = ByteBufCodecs.BOOL.map(
			value -> value ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
			hand -> hand == InteractionHand.MAIN_HAND
	);
	
	public static final StreamCodec<ByteBuf, HarvestMessage> STREAM_CODEC = StreamCodec.composite(
	    BlockPos.STREAM_CODEC, HarvestMessage::pos,
		HAND_STREAM_CODEC, HarvestMessage::hand,
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
