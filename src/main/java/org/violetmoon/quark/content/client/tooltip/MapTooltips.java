package org.violetmoon.quark.content.client.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.violetmoon.quark.content.client.module.ImprovedTooltipsModule;
import org.violetmoon.zeta.client.event.play.ZGatherTooltipComponents;

import javax.annotation.Nonnull;
import java.util.List;

public class MapTooltips {

	private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");

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
		public void renderImage(@Nonnull Font font, int tooltipX, int tooltipY, @Nonnull PoseStack pose, @Nonnull ItemRenderer itemRenderer, int something) {
			Minecraft mc = Minecraft.getInstance();

			MapItemSavedData mapdata = MapItem.getSavedData(stack, mc.level);
			Integer mapID = MapItem.getMapId(stack);

			if (mapdata == null)
				return;

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, RES_MAP_BACKGROUND);

			int pad = 7;
			int size = 135 + pad;
			float scale = 0.5F;

			pose.pushPose();
			pose.translate(tooltipX + 3, tooltipY + 3, 500);
			pose.scale(scale, scale, 1F);
			RenderSystem.enableBlend();

			GuiComponent.blit(pose, -pad, -pad, 0, 0, size, size, size, size);

			BufferBuilder buffer = Tesselator.getInstance().getBuilder();
			MultiBufferSource.BufferSource immediateBuffer = MultiBufferSource.immediate(buffer);
			mc.gameRenderer.getMapRenderer().render(pose, immediateBuffer, mapID, mapdata, true, 240);
			immediateBuffer.endBatch();
			pose.popPose();
		}

		@Override
		public int getHeight() {
			Minecraft mc = Minecraft.getInstance();
			MapItemSavedData mapdata = MapItem.getSavedData(stack, mc.level);
			return mapdata != null ? 75 : 0;
		}

		@Override
		public int getWidth(@Nonnull Font font) {
			return 72;
		}
	}


}
