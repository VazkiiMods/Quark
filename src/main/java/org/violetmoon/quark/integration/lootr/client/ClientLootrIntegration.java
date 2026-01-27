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

	private final LootrIntegration real = (LootrIntegration) Quark.LOOTR_INTEGRATION;

	@Override
	public void clientSetup(ZClientSetup event) {
		BlockEntityRenderers.register(real.chestTEType, ctx -> new LootrVariantChestRenderer<>(ctx, false));
		BlockEntityRenderers.register(real.trappedChestTEType, ctx -> new LootrVariantChestRenderer<>(ctx, true));
	}

	@LoadEvent
	public void setItemExtensions(ZRegisterClientExtension event) {
		for (Block b : real.lootrRegularChests) {
			event.registerItem(new IZetaClientItemExtensions() {
				@Override
				public BlockEntityWithoutLevelRenderer getBEWLR() {
					return new SimpleWithoutLevelRenderer(real.chestTEType, b.defaultBlockState());
				}
			}, b.asItem());
		}

		for (Block b : real.lootrTrappedChests) {
			event.registerItem(new IZetaClientItemExtensions() {
				@Override
				public BlockEntityWithoutLevelRenderer getBEWLR() {
					return new SimpleWithoutLevelRenderer(real.trappedChestTEType, b.defaultBlockState());
				}
			}, b.asItem());
		}
	}
}
