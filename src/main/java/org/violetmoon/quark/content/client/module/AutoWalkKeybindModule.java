package org.violetmoon.quark.content.client.module;

import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.config.Config;
import org.violetmoon.zeta.client.event.load.ZKeyMapping;
import org.violetmoon.zeta.client.event.play.ZInput;
import org.violetmoon.zeta.client.event.play.ZInputUpdate;
import org.violetmoon.zeta.client.event.play.ZRenderGuiOverlay;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.player.Player;

@ZetaLoadModule(category = "client")
public class AutoWalkKeybindModule extends ZetaModule {

	@Config public static boolean drawHud = true;
	@Config public static int hudHeight = 10;

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends AutoWalkKeybindModule {

		private KeyMapping keybind;

		private boolean autorunning;
		private boolean hadAutoJump;
		private boolean shouldAccept;

		@LoadEvent
		public void registerKeybinds(ZKeyMapping event) {
			keybind = event.init("quark.keybind.autorun", null, QuarkClient.MISC_GROUP);
		}

		@PlayEvent
		public void onMouseInput(ZInput.MouseButton event) {
			acceptInput();
		}

		@PlayEvent
		public void onKeyInput(ZInput.Key event) {
			acceptInput();
		}

		@PlayEvent
		public void drawHUD(ZRenderGuiOverlay.Hotbar event) {
			if(drawHud && autorunning) {
				String message = I18n.get("quark.misc.autowalking");

				Minecraft mc = Minecraft.getInstance();
				int w = mc.font.width("OoO" + message + "oOo");

				Window window = event.getWindow();
				int x = (window.getGuiScaledWidth() - w) / 2;
				int y = hudHeight;

				String displayMessage = message;
				int dots = (QuarkClient.ticker.ticksInGame / 10) % 2;
				switch(dots) {
					case 0 -> displayMessage = "OoO " + message + " oOo";
					case 1 -> displayMessage = "oOo " + message + " OoO";
				}

				mc.font.drawShadow(event.getPoseStack(), displayMessage, x, y, 0xFFFFFFFF);
			}
		}

		private void acceptInput() {
			Minecraft mc = Minecraft.getInstance();

			OptionInstance<Boolean> opt = mc.options.autoJump();
			if(mc.options.keyUp.isDown()) {
				if(autorunning)
					opt.set(hadAutoJump);

				autorunning = false;
			} else {
				if(keybind.isDown()) {
					if(shouldAccept) {
						shouldAccept = false;
						Player player = mc.player;
						float height = player.getStepHeight();

						autorunning = !autorunning;

						if(autorunning) {
							hadAutoJump = opt.get();

							if(height < 1)
								opt.set(true);
						} else opt.set(hadAutoJump);
					}
				} else shouldAccept = true;
			}
		}

		@PlayEvent
		public void onInput(ZInputUpdate event) {
			Minecraft mc = Minecraft.getInstance();
			if(mc.player != null && autorunning) {
				Input input = event.getInput();
				input.up = true;
				input.forwardImpulse = ((LocalPlayer) event.getEntity()).isMovingSlowly() ? 0.3F : 1F;
			}
		}

	}

}
