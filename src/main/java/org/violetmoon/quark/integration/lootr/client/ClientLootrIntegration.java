package org.violetmoon.quark.integration.lootr.client;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.Block;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.integration.lootr.LootrIntegration;
import org.violetmoon.zeta.client.SimpleWithoutLevelRenderer;
import org.violetmoon.zeta.client.event.load.ZClientSetup;

public class ClientLootrIntegration implements IClientLootrIntegration {

	private final LootrIntegration real = (LootrIntegration) Quark.LOOTR_INTEGRATION;

	@Override
	public void clientSetup(ZClientSetup event) {
		BlockEntityRenderers.register(real.chestTEType, ctx -> new LootrVariantChestRenderer<>(ctx, false));
		BlockEntityRenderers.register(real.trappedChestTEType, ctx -> new LootrVariantChestRenderer<>(ctx, true));

		//for(Block b : real.lootrRegularChests) QuarkClient.ZETA_CLIENT.setBlockEntityWithoutLevelRenderer(b.asItem(), new SimpleWithoutLevelRenderer(real.chestTEType, b.defaultBlockState()));
		//for(Block b : real.lootrTrappedChests) QuarkClient.ZETA_CLIENT.setBlockEntityWithoutLevelRenderer(b.asItem(), new SimpleWithoutLevelRenderer(real.trappedChestTEType, b.defaultBlockState()));
	}

}
