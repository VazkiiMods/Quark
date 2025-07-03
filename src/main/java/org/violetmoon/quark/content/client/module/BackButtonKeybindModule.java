package org.violetmoon.quark.content.client.module;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Type;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.controls.KeyBindsScreen;
import net.minecraft.client.resources.language.I18n;

import org.lwjgl.glfw.GLFW;

import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.zeta.client.event.load.ZKeyMapping;
import org.violetmoon.zeta.client.event.play.ZScreen;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.util.List;
import java.util.Objects;

@ZetaLoadModule(category = "client")
public class BackButtonKeybindModule extends ZetaModule {

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends BackButtonKeybindModule {

		private KeyMapping backKey;
		private List<GuiEventListener> listeners;

		private InputConstants.Key lastKey; //for optionsScreenHack

		@LoadEvent
		public void registerKeybinds(ZKeyMapping event) {
			//TODO ZETA: dunno if this predicate works lol
			backKey = event.initMouse("quark.keybind.back", 4, QuarkClient.MISC_GROUP, (key) -> key.getType() != Type.MOUSE || key.getValue() != 0);
		}

		@PlayEvent
		public void openGui(ZScreen.Init.Pre event) {
			lastKey = backKey.getKey();
			listeners = event.getListenersList();
		}

		@PlayEvent
		public void onKeyInput(ZScreen.KeyPressed.Post event) {
			if(backKey.getKey().getType() == Type.KEYSYM && event.getKeyCode() == backKey.getKey().getValue())
				clicc();
		}

		@PlayEvent
		public void onMouseInput(ZScreen.MouseButtonPressed.Post event) {
			if(keybindsScreenHack(event))
				return;

			int btn = event.getButton();
			if(backKey.getKey().getType() == Type.MOUSE && btn != GLFW.GLFW_MOUSE_BUTTON_LEFT && btn == backKey.getKey().getValue())
				clicc();
		}

		private boolean keybindsScreenHack(ZScreen evt) {
			if(!(evt.getScreen() instanceof KeyBindsScreen))
				return false; //not applicable

			//If the player is on the keybinds screen and the back button has changed since we last looked, ignore the back press.
			//This prevents rebinding the back key to a mouse button instantly kicking you out of the keybinds menu. (#4671)
			//For some reason this hack doesn't need to be applied to keyboard buttons? Only mouse clicks.
			boolean different = !Objects.equals(lastKey, backKey.getKey());
			lastKey = backKey.getKey();
			return different;
		}

		private void clicc() {
			ImmutableSet<String> buttons = ImmutableSet.of(
					I18n.get("gui.back"),
					I18n.get("gui.done"),
					I18n.get("gui.cancel"),
					I18n.get("gui.toTitle"),
					I18n.get("gui.toMenu"),
					I18n.get("quark.gui.config.save"));

			// Iterate this way to ensure we match the more important back buttons first
			for(String b : buttons)
				for(GuiEventListener listener : listeners) {
					if(listener instanceof Button w) {
                        if(w.getMessage().getString().equals(b) && w.visible && w.active) {
							w.onClick(0, 0);
							return;
						}
					}
				}

			Minecraft mc = Minecraft.getInstance();
			if(mc.level != null && mc.screen != null)
				mc.screen.onClose();
		}

	}

}
