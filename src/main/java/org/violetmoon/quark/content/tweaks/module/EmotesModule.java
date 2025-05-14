package org.violetmoon.quark.content.tweaks.module;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import org.lwjgl.glfw.GLFW;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.handler.ContributorRewardHandler;
import org.violetmoon.quark.base.network.message.RequestEmoteMessage;
import org.violetmoon.quark.content.tweaks.client.emote.*;
import org.violetmoon.quark.content.tweaks.client.screen.NotButton;
import org.violetmoon.zeta.client.event.load.ZClientSetup;
import org.violetmoon.zeta.client.event.load.ZKeyMapping;
import org.violetmoon.zeta.client.event.play.ZInput;
import org.violetmoon.zeta.client.event.play.ZRenderGuiOverlay;
import org.violetmoon.zeta.client.event.play.ZRenderLiving;
import org.violetmoon.zeta.client.event.play.ZRenderTick;
import org.violetmoon.zeta.client.event.play.ZScreen;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.event.load.ZConfigChanged;
import org.violetmoon.zeta.event.load.ZModulesReady;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import java.io.File;
import java.util.*;

import aurelienribon.tweenengine.Tween;

@ZetaLoadModule(category = "tweaks")
public class EmotesModule extends ZetaModule {

	private static final Set<String> DEFAULT_EMOTE_NAMES = ImmutableSet.of(
			"no",
			"yes",
			"wave",
			"salute",
			"cheer",
			"clap",
			"think",
			"point",
			"shrug",
			"headbang",
			"weep",
			"facepalm");

	private static final Set<String> PATREON_EMOTES = ImmutableSet.of(
			"dance",
			"tpose",
			"dab",
			"jet",
			"exorcist",
			"zombie");

	public static final int EMOTE_BUTTON_WIDTH = 25;
	public static final int EMOTES_PER_ROW = 3;

	@Config(description = "The enabled default emotes. Remove from this list to disable them. You can also re-order them, if you feel like it.")
	public static List<String> enabledEmotes = Lists.newArrayList(DEFAULT_EMOTE_NAMES);

	@Config(description = "The list of Custom Emotes to be loaded.\nWatch the tutorial on Custom Emotes to learn how to make your own: https://youtu.be/ourHUkan6aQ")
	public static List<String> customEmotes = Lists.newArrayList();

	@Config(description = "Enable this to make custom emotes read the file every time they're triggered so you can edit on the fly.\nDO NOT ship enabled this in a modpack, please.")
	public static boolean customEmoteDebug = false;

	@Config
	public static int buttonShiftX = 0;
	@Config
	public static int buttonShiftY = 0;

	public static boolean emotesVisible = false;
	public static File emotesDir;

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends EmotesModule {

		public static CustomEmoteIconResourcePack resourcePack;

		private static Map<KeyMapping, String> emoteKeybinds;

		private static NotButton emoteToggleButton;
		private static List<NotButton> emoteButtons = new ArrayList<>();

		@LoadEvent
		public void onReady(ZModulesReady e) {
			Minecraft mc = Minecraft.getInstance();
			if(mc == null)
				return; // Mojang datagen has no client instance available

			emotesDir = new File(mc.gameDirectory, "/config/quark_emotes");
			if(!emotesDir.exists())
				emotesDir.mkdirs();

			//todo: Fixme or something idk - Siuolplex
			/*
			mc.getResourcePackRepository().addPackFinder(new RepositorySource() {
				@Override
				public void loadPacks(@NotNull Consumer<Pack> packConsumer, @NotNull Pack.PackConstructor packInfoFactory) {
					Client.resourcePack = new CustomEmoteIconResourcePack();
			
					String name = "quark:emote_resources";
					Pack t = Pack.create(name, true, () -> Client.resourcePack, packInfoFactory, Pack.Position.TOP, tx->tx);
					packConsumer.accept(t);
				}
			});*/
		}

		@LoadEvent
		public final void clientSetup(ZClientSetup event) {
			Tween.registerAccessor(HumanoidModel.class, ModelAccessor.INSTANCE);
		}

		@LoadEvent
		public void registerKeybinds(ZKeyMapping event) {
			int sortOrder = 0;

			Client.emoteKeybinds = new HashMap<>();
			for(String s : DEFAULT_EMOTE_NAMES)
				Client.emoteKeybinds.put(event.init("quark.emote." + s, null, QuarkClient.EMOTE_GROUP, sortOrder++), s);
			for(String s : PATREON_EMOTES)
				Client.emoteKeybinds.put(event.init("quark.keybind.patreon_emote." + s, null, QuarkClient.EMOTE_GROUP, sortOrder++), s);
		}

		@LoadEvent
		public void configChanged(ZConfigChanged e) {
			EmoteHandler.clearEmotes();

			for(String s : enabledEmotes) {
				if(DEFAULT_EMOTE_NAMES.contains(s))
					EmoteHandler.addEmote(s);
			}

			for(String s : PATREON_EMOTES)
				EmoteHandler.addEmote(s);

			for(String s : customEmotes)
				EmoteHandler.addCustomEmote(s);
		}

		//For some god damn reason it is impossible to get "whether the mouse is down" outside of a screen.
		//this is a very obscure piece of information it Definitely makes sense to encapsulate this as tightly as possible for no reason
		// THank you Mojang <33333333333 i love writing MODS!!
		boolean theMouseIsPressedNowThankYou = false;
		@PlayEvent
		public void mouseStupidAwfulBad(ZInput.MouseButton event) {
			if(event.getButton() == GLFW.GLFW_MOUSE_BUTTON_1) {
				if(event.getAction() == GLFW.GLFW_PRESS)
					theMouseIsPressedNowThankYou = true;
				else if(event.getAction() == GLFW.GLFW_RELEASE)
					theMouseIsPressedNowThankYou = false;
			}
		}

		@PlayEvent
		public void initGui(ZScreen.Init.Post event) {
			if(event.getScreen() instanceof ChatScreen) {
				emoteButtons.clear();

				Window window = Minecraft.getInstance().getWindow();
				int windowWidth = window.getGuiScaledWidth();
				int windowHeight = window.getGuiScaledHeight();

				Map<Integer, List<EmoteDescriptor>> descriptorSorting = new TreeMap<>();

				for(EmoteDescriptor desc : EmoteHandler.emoteMap.values()) {
					if(desc.getTier() <= ContributorRewardHandler.localPatronTier) {
						List<EmoteDescriptor> descriptors = descriptorSorting.computeIfAbsent(desc.getTier(), k -> new LinkedList<>());

						descriptors.add(desc);
					}
				}

				int rows = 0;
				int row = 0;
				int tierRow, rowPos;

				Minecraft mc = Minecraft.getInstance();
				boolean expandDown = mc.options.showSubtitles().get();

				Set<Integer> keys = descriptorSorting.keySet();
				for(int tier : keys) {
					List<EmoteDescriptor> descriptors = descriptorSorting.get(tier);
					if(descriptors != null) {
						rows += descriptors.size() / 3;
						if(descriptors.size() % 3 != 0)
							rows++;
					}
				}

				int buttonX = buttonShiftX;
				int buttonY = (expandDown ? 2 : windowHeight - 40) + buttonShiftY;

				for(int tier : keys) {
					rowPos = 0;
					tierRow = 0;
					List<EmoteDescriptor> descriptors = descriptorSorting.get(tier);
					if(descriptors != null) {
						for(EmoteDescriptor desc : descriptors) {
							int rowSize = Math.min(descriptors.size() - tierRow * EMOTES_PER_ROW, EMOTES_PER_ROW);

							int x = buttonX + windowWidth - 2 - (EMOTE_BUTTON_WIDTH * (EMOTES_PER_ROW + 1)) + (((rowPos + 1) * 2 + EMOTES_PER_ROW - rowSize) * EMOTE_BUTTON_WIDTH / 2 + 1);
							int y = buttonY + (EMOTE_BUTTON_WIDTH * (rows - row)) * (expandDown ? 1 : -1);

							NotButton button = new NotButton(
								x, y,
								EMOTE_BUTTON_WIDTH - 1, EMOTE_BUTTON_WIDTH - 1,
								desc,
								() -> QuarkClient.ZETA_CLIENT.sendToServer(new RequestEmoteMessage(desc.getRegistryName()))
							);

							emoteButtons.add(button);

							if(++rowPos == EMOTES_PER_ROW) {
								tierRow++;
								row++;
								rowPos = 0;
							}
						}
					}
					if(rowPos != 0)
						row++;
				}

				emoteToggleButton = new NotButton(
					buttonX + windowWidth - 2 - EMOTE_BUTTON_WIDTH * EMOTES_PER_ROW, buttonY,
					EMOTE_BUTTON_WIDTH * EMOTES_PER_ROW, 20,
					Component.translatable("quark.gui.button.emotes"),
					() -> emotesVisible ^= true
				);

				event.getScreen().renderables.add((gfx, mouseX, mouseY, partialTicks) -> {
					emoteToggleButton.draw(gfx, mouseX, mouseY, theMouseIsPressedNowThankYou);
					if(emotesVisible)
						for(NotButton b : emoteButtons)
							b.draw(gfx, mouseX, mouseY, theMouseIsPressedNowThankYou);
				});
			}
		}

		@PlayEvent
		public void onKeyInput(ZInput.Key event) {
			Minecraft mc = Minecraft.getInstance();
			if(mc.isWindowActive()) {
				for(KeyMapping key : Client.emoteKeybinds.keySet()) {
					if(key.isDown()) {
						String emote = Client.emoteKeybinds.get(key);
						QuarkClient.ZETA_CLIENT.sendToServer(new RequestEmoteMessage(emote));
						return;
					}
				}
			}
		}

		@PlayEvent
		public void drawCrosshair(ZRenderGuiOverlay.Crosshair.Post event) {
			Minecraft mc = Minecraft.getInstance();
			Window res = event.getWindow();
			GuiGraphics guiGraphics = event.getGuiGraphics();
			PoseStack stack = guiGraphics.pose();
			EmoteBase emote = EmoteHandler.getPlayerEmote(mc.player);
			if(emote != null && emote.timeDone < emote.totalTime) {
				ResourceLocation resource = emote.desc.texture;
				int x = res.getGuiScaledWidth() / 2 - 16;
				int y = res.getGuiScaledHeight() / 2 - 60;
				float transparency = 1F;
				float tween = 5F;

				if(emote.timeDone < tween)
					transparency = emote.timeDone / tween;
				else if(emote.timeDone > emote.totalTime - tween)
					transparency = (emote.totalTime - emote.timeDone) / tween;

				stack.pushPose();
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, transparency);

				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();

				guiGraphics.blit(resource, x, y, 0, 0, 32, 32, 32, 32);
				RenderSystem.enableBlend();

				String name = I18n.get(emote.desc.getTranslationKey());
				guiGraphics.drawString(mc.font, name, res.getGuiScaledWidth() / 2f - mc.font.width(name) / 2f, y + 34, 0xFFFFFF + (((int) (transparency * 255F)) << 24), true);
				stack.popPose();
			}
		}

		@PlayEvent
		public void renderTick(ZRenderTick event) {
			EmoteHandler.onRenderTick(Minecraft.getInstance());
		}

		@PlayEvent
		public void preRenderLiving(ZRenderLiving.PreHighest event) {
			if(event.getEntity() instanceof Player player)
				EmoteHandler.preRender(event.getPoseStack(), player);
		}

		@PlayEvent
		public void postRenderLiving(ZRenderLiving.PostLowest event) {
			if(event.getEntity() instanceof Player player)
				EmoteHandler.postRender(event.getPoseStack(), player);
		}

	}

}
