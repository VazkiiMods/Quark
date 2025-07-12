package org.violetmoon.quark.content.experimental.module;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.violetmoon.zeta.client.event.play.ZRenderGuiOverlay;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "experimental", enabledByDefault = false)
public class AdjustableChatModule extends ZetaModule {
	@Config
	public static int horizontalShift = 0;
	@Config
	public static int verticalShift = 0;

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends AdjustableChatModule {

		@PlayEvent
		public void pre(ZRenderGuiOverlay.Pre event) {
			if (event.getLayerName().equals(VanillaGuiLayers.CHAT) && !Minecraft.getInstance().options.hideGui) {
				event.getGuiGraphics().pose().translate(horizontalShift, verticalShift, 0);
			}
		}

		@PlayEvent
		public void post(ZRenderGuiOverlay.Post event) {
			if (event.getLayerName().equals(VanillaGuiLayers.CHAT) && !Minecraft.getInstance().options.hideGui) {
				event.getGuiGraphics().pose().translate(-horizontalShift, -verticalShift, 0);
			}
		}
	}
}
