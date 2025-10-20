package org.violetmoon.quark.catnip.net.base;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public non-sealed interface ClientboundPacketPayload extends BasePacketPayload {
	/**
	 * Called on the main client thread.
	 * Make sure that implementations are also annotated, or else servers may crash.
	 */
	@OnlyIn(Dist.CLIENT)
	void handle(LocalPlayer player);

	default void handleInternal(Player player) {
		if (player instanceof LocalPlayer localPlayer)
			handle(localPlayer);
	}
}
