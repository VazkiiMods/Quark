package org.violetmoon.quark.content.management.module;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.opengl.GL11;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.network.message.ChangeHotbarMessage;
import org.violetmoon.zeta.client.event.load.ZKeyMapping;
import org.violetmoon.zeta.client.event.play.ZClientTick;
import org.violetmoon.zeta.client.event.play.ZInput;
import org.violetmoon.zeta.client.event.play.ZRenderGuiOverlay;
import org.violetmoon.zeta.config.Config;
import org.violetmoon.zeta.event.bus.LoadEvent;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

@ZetaLoadModule(category = "management")
public class HotbarChangerModule extends ZetaModule {
	private static final ResourceLocation HOTBAR = ResourceLocation.withDefaultNamespace("textures/gui/sprites/hud/hotbar.png");

	private static final int MAX_HEIGHT = 90;

	public static float height = 0;
	public static float oldHeight = 0;
	public static int currentHeldItem = -1;
	public static boolean animating;
	public static boolean keyDown;
	public static boolean hotbarChangeOpen, shifting;

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends HotbarChangerModule {
		private static KeyMapping changeHotbarKey;

		@Config
		public double animationTime = 7; //time in ticks

		@LoadEvent
		public void registerKeybinds(ZKeyMapping event) {
			changeHotbarKey = event.init("quark.keybind.change_hotbar", "z", QuarkClient.MISC_GROUP);
		}

		@PlayEvent
		public void onMouseInput(ZInput.MouseButton event) {
			acceptInput(-1);
		}

		@PlayEvent
		public void onKeyInput(ZInput.Key event) {
			acceptInput(event.getKey());
		}

		@PlayEvent
		public void hudHeathPre(ZRenderGuiOverlay.Pre event) {
			boolean shouldShift = event.getLayerName().equals(VanillaGuiLayers.EXPERIENCE_BAR);
			if (shouldShift && !Minecraft.getInstance().options.hideGui) {
				float shift = -getRealHeight(event.getPartialTick().getGameTimeDeltaTicks()) + 22;
				if (shift < 0) {
					event.getGuiGraphics().pose().translate(0, shift, 0);
					shifting = true;
				}
			}
		}

		// Why?
		@PlayEvent
		public void hudDebugTextPre(ZRenderGuiOverlay.Pre event) {
			boolean shouldShift = event.getLayerName().equals(VanillaGuiLayers.DEBUG_OVERLAY) || event.getLayerName().equals(VanillaGuiLayers.EFFECTS);
			if (shouldShift && !Minecraft.getInstance().options.hideGui) {
				hudOverlay(event);
			}
		}

		// What?
		@PlayEvent
		public void hudPotionIconsPre(ZRenderGuiOverlay.Pre event) {

		}

		public void hudOverlay(ZRenderGuiOverlay event) {
			float shift = -getRealHeight(event.getPartialTick().getGameTimeDeltaTicks()) + 22;
			if(shifting) {
				event.getGuiGraphics().pose().translate(0, -shift, 0);
				shifting = false;
			}
		}

		@PlayEvent
		public void hudPost(ZRenderGuiOverlay.Pre event) {
			Minecraft mc = Minecraft.getInstance();
			if (event.getLayerName().equals(VanillaGuiLayers.HOTBAR) && !Minecraft.getInstance().options.hideGui) {
				if (height <= 0)
					return;

				Player player = mc.player;
				GuiGraphics guiGraphics = event.getGuiGraphics();
				PoseStack matrix = guiGraphics.pose();

				matrix.pushPose();
				matrix.translate(0, 0, -500);
				RenderSystem.enableDepthTest();

				Window res = event.getWindow();
				float realHeight = getRealHeight(event.getPartialTick().getGameTimeDeltaTicks());
				float xStart = res.getGuiScaledWidth() / 2f - 91;
				float yStart = res.getGuiScaledHeight() - realHeight;

				RenderSystem.enableBlend();
				RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				for (int i = 0; i < 3; i++) {
					matrix.pushPose();
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.75F);
					matrix.translate(xStart, yStart + i * 21, 0);
					guiGraphics.blit(HOTBAR, 0, 0, 0, 0, 182, 22, 182, 22);
					matrix.popPose();
				}

				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

				for (int i = 0; i < 3; i++) {
					String draw = Integer.toString(i + 1);
					KeyMapping key = mc.options.keyHotbarSlots[i];
					if (!key.isUnbound()) {
						draw = key.getTranslatedKeyMessage().getString();
					}

					draw = ChatFormatting.BOLD + draw;

					guiGraphics.drawString(mc.font, draw, xStart - mc.font.width(draw) - 2, yStart + i * 21 + 7, 0xFFFFFF, true);
				}

				for (int i = 0; i < 27; i++) {
					ItemStack invStack = player.getInventory().getItem(i + 9);
					int x = (int) (xStart + (i % 9) * 20 + 3);
					int y = (int) (yStart + (i / 9) * 21 + 3);

					guiGraphics.renderItem(invStack, x, y);
					guiGraphics.renderItemDecorations(mc.font, invStack, x, y);
				}
				matrix.popPose();
			}
		}

		@PlayEvent
		public void clientTick(ZClientTick.End event) {
			Player player = Minecraft.getInstance().player;

			if(player != null) {
				Inventory inventory = player.getInventory();

				if(currentHeldItem != -1 && inventory.selected != currentHeldItem) {
					inventory.selected = currentHeldItem;
					currentHeldItem = -1;
				}
			}

			if(hotbarChangeOpen && oldHeight < 1) {
				oldHeight = height;
				height += 1/(float)animationTime;
				height = Mth.clamp(height, 0, 1);
				animating = true;
			} else if(!hotbarChangeOpen && oldHeight > 0) {
				oldHeight = height;
				height -= 1/(float)animationTime;
				height = Mth.clamp(height, 0, 1);
				animating = true;
			} else
				animating = false;
		}

		private void acceptInput(int currInput) {
			Minecraft mc = Minecraft.getInstance();
			boolean down = changeHotbarKey.isDown();
			boolean wasDown = keyDown;
			keyDown = down;
			if(mc.isWindowActive()) {
				if(down && !wasDown)
					hotbarChangeOpen = !hotbarChangeOpen;
				else if(hotbarChangeOpen)
					for(int i = 0; i < 3; i++)
						if(isKeyDownOrFallback(mc.options.keyHotbarSlots[i], 49 + i, currInput)) {
							PacketDistributor.sendToServer(new ChangeHotbarMessage(i + 1));
							hotbarChangeOpen = false;
							currentHeldItem = mc.player.getInventory().selected;
							return;
						}

			}
		}

		private boolean isKeyDownOrFallback(KeyMapping key, int input, int currInput) {
			if(key.isUnbound())
				return currInput != -1 && input == currInput;

			return key.isDown();
		}

		private float getRealHeight(float part) {
			return Mth.lerp(part, oldHeight, height) * MAX_HEIGHT;
		}
	}
}
