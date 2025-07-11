package org.violetmoon.quark.content.client.module;

import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.violetmoon.quark.base.client.handler.ClientUtil;
import org.violetmoon.zeta.client.event.play.ZRenderGuiOverlay;
import org.violetmoon.zeta.event.bus.PlayEvent;
import org.violetmoon.zeta.module.ZetaLoadModule;
import org.violetmoon.zeta.module.ZetaModule;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

@ZetaLoadModule(category = "client")
public class ElytraIndicatorModule extends ZetaModule {

	public int getArmorLimit(int curr) {
		return curr;
	}

	@ZetaLoadModule(clientReplacement = true)
	public static class Client extends ElytraIndicatorModule {

		private int shift = 0;

		@PlayEvent
		public void hudPre(ZRenderGuiOverlay.Pre event) {
			if(!event.shouldDrawSurvivalElements() || !event.getLayerName().equals(VanillaGuiLayers.ARMOR_LEVEL))
				return;

			Minecraft mc = Minecraft.getInstance();
			Player player = mc.player;
			ItemStack itemstack = player.getItemBySlot(EquipmentSlot.CHEST);

			if(zeta().itemExtensions.get(itemstack).canElytraFlyZeta(itemstack, player)) {
				int armor = player.getArmorValue();
				shift = (armor >= 20 ? 0 : 9);

				GuiGraphics guiGraphics = event.getGuiGraphics();
				PoseStack pose = guiGraphics.pose();
				Window window = event.getWindow();

				pose.translate(shift, 0, 0);

				pose.pushPose();
				pose.translate(0, 0, 100);
				RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

				int x = window.getGuiScaledWidth() / 2 - 100;
				int y = window.getGuiScaledHeight() - event.getLeftHeight();
				guiGraphics.blit(ClientUtil.GENERAL_ICONS, x, y, 184, 35, 9, 9, 256, 256);

				pose.popPose();
			}
		}

		@PlayEvent
		public void hudPost(ZRenderGuiOverlay.Post event) {
			if(event.getLayerName().equals(VanillaGuiLayers.ARMOR_LEVEL) && shift != 0) {
				event.getGuiGraphics().pose().translate(-shift, 0, 0);
				shift = 0;
			}
		}

		@Override
		public int getArmorLimit(int curr) {
			if(!isEnabled())
				return curr;
			return curr;
			//return 20 - ((shift / 9) * 2);
		}

	}

}
