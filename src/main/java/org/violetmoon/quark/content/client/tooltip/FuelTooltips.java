package org.violetmoon.quark.content.client.tooltip;

import java.util.Locale;

import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.handler.ClientUtil;
import org.violetmoon.quark.content.client.module.ImprovedTooltipsModule;
import org.violetmoon.zeta.client.event.play.ZGatherTooltipComponents;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.violetmoon.zeta.event.play.ZFurnaceFuelBurnTime;

public class FuelTooltips {

	public static void makeTooltip(ZGatherTooltipComponents event) {
		ItemStack stack = event.getItemStack();
		if(!stack.isEmpty()) {
			Screen screen = Minecraft.getInstance().screen;
			if(screen != null && screen instanceof AbstractFurnaceScreen<?>) {
				int count = Quark.ZETA.itemExtensions.get(stack).getBurnTimeZeta(stack, RecipeType.SMELTING);
				if(count == 0){
					//TODO make this not private
					count = Quark.ZETA.fuel.fuelValues.get(stack.getItem());
				}

				if(count > 0) {
					Font font = Minecraft.getInstance().font;

					String time = getDisplayString(count);
					event.getTooltipElements().add(Either.right(new FuelComponent(stack, 18 + font.width(time), count)));
				}
			}
		}
	}

	private static String getDisplayString(int count) {
		float items = (float) count / (float) Math.max(1, ImprovedTooltipsModule.fuelTimeDivisor);
		String time = String.format(Locale.ROOT, ((items - (int) items) == 0) ? "x%.0f" : "x%.1f", items);
		return time;
	}

	public record FuelComponent(ItemStack stack, int width, int count) implements ClientTooltipComponent, TooltipComponent {
		@Override
		public void renderImage(@NotNull Font font, int tooltipX, int tooltipY, @NotNull GuiGraphics guiGraphics) {
			PoseStack pose = guiGraphics.pose();

			pose.pushPose();
			pose.translate(tooltipX, tooltipY, 500);

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			guiGraphics.blit(ClientUtil.GENERAL_ICONS, 1, 1, 0, 128, 13, 13, 256, 256);

			String time = getDisplayString(count);
			guiGraphics.drawString(font, time, 16, 5, 0xffb600, true);

			pose.popPose();
		}

		@Override
		public int getHeight() {
			return 18;
		}

		@Override
		public int getWidth(@NotNull Font font) {
			return width;
		}
	}

}
