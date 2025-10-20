package org.violetmoon.quark.content.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.content.client.module.ImprovedTooltipsModule;
import org.violetmoon.zeta.client.event.play.ZGatherTooltipComponents;

import java.util.List;

public class MapTooltips {

	private static final ResourceLocation RES_MAP_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/map/map_background.png");

	public static void makeTooltip(ZGatherTooltipComponents event) {
		ItemStack stack = event.getItemStack();
		if(!stack.isEmpty() && stack.getItem() instanceof MapItem) {
			List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();

			if(!ImprovedTooltipsModule.mapRequireShift || Screen.hasShiftDown())
				tooltip.add(1, Either.right(new MapComponent(stack)));
			else if(ImprovedTooltipsModule.mapRequireShift && !Screen.hasShiftDown())
				tooltip.add(1, Either.left(Component.translatable("quark.misc.map_shift")));
		}
	}

	public record MapComponent(ItemStack stack) implements ClientTooltipComponent, TooltipComponent {
		@Override
		public void renderImage(@NotNull Font font, int tooltipX, int tooltipY, @NotNull GuiGraphics guiGraphics) {
			Minecraft mc = Minecraft.getInstance();

			PoseStack pose = guiGraphics.pose();

			MapItemSavedData mapdata = MapItem.getSavedData(stack, mc.level);
			MapId mapID = stack.get(DataComponents.MAP_ID);

			if(mapdata == null)
				return;

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			int pad = 7;
			int size = 135 + pad;
			float scale = 0.5F;

			pose.pushPose();
			pose.translate(tooltipX + 3, tooltipY + 3, 500);
			pose.scale(scale, scale, 1F);
			RenderSystem.enableBlend();

			guiGraphics.blit(RES_MAP_BACKGROUND, -pad, -pad, 0, 0, size, size, size, size);
			
			pose.translate(0, 0, 1);
			mc.gameRenderer.getMapRenderer().render(pose, guiGraphics.bufferSource(), mapID, mapdata, true, 240);
		}

		@Override
		public int getHeight() {
			Minecraft mc = Minecraft.getInstance();
			MapItemSavedData mapdata = MapItem.getSavedData(stack, mc.level);
			return mapdata != null ? 75 : 0;
		}

		@Override
		public int getWidth(@NotNull Font font) {
			return 72;
		}
	}

}
