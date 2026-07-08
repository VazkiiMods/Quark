package org.violetmoon.quark.integration.lootr.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.Block;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.integration.lootr.LootrIntegration;
import org.violetmoon.zeta.client.SimpleWithoutLevelRenderer;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.client.event.load.ZRegisterClientExtension;
import org.violetmoon.zeta.client.extensions.IZetaClientItemExtensions;
import org.violetmoon.zeta.event.bus.LoadEvent;

public class ClientLootrIntegration implements IClientLootrIntegration {

	public static final LootrIntegration LOOTR_INTEGRATION = (LootrIntegration) Quark.LOOTR_INTEGRATION;

	@Override
	public void clientSetup(ZClientSetup event) {
		BlockEntityRenderers.register(LOOTR_INTEGRATION.chestTEType, ctx -> new LootrVariantChestRenderer<>(ctx, false));
		BlockEntityRenderers.register(LOOTR_INTEGRATION.trappedChestTEType, ctx -> new LootrVariantChestRenderer<>(ctx, true));
	}

	@LoadEvent
	public void setItemExtensions(ZRegisterClientExtension event) {
		for (Block b : LOOTR_INTEGRATION.lootrRegularChests) {
			event.registerItem(new IZetaClientItemExtensions() {
				@Override
				public BlockEntityWithoutLevelRenderer getBEWLR() {
					return new SimpleWithoutLevelRenderer(LOOTR_INTEGRATION.chestTEType, b.defaultBlockState());
				}
			}, b.asItem());
		}

		for (Block b : LOOTR_INTEGRATION.lootrTrappedChests) {
			event.registerItem(new IZetaClientItemExtensions() {
				@Override
				public BlockEntityWithoutLevelRenderer getBEWLR() {
					return new SimpleWithoutLevelRenderer(LOOTR_INTEGRATION.trappedChestTEType, b.defaultBlockState());
				}
			}, b.asItem());
		}
	}
}
