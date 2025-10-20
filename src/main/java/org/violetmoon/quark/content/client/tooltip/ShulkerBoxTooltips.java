package org.violetmoon.quark.content.client.tooltip;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.handler.SimilarBlockTypeHandler;
import org.violetmoon.quark.content.client.module.ChestSearchingModule;
import org.violetmoon.quark.content.client.module.ImprovedTooltipsModule;
import org.violetmoon.zeta.client.event.play.ZGatherTooltipComponents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class ShulkerBoxTooltips {

	public static final ResourceLocation WIDGET_RESOURCE = Quark.asResource("textures/misc/shulker_widget.png");

	public static void makeTooltip(ZGatherTooltipComponents event) {
		ItemStack stack = event.getItemStack();
		if(SimilarBlockTypeHandler.isShulkerBox(stack) && stack.has(DataComponents.CONTAINER)) {
			ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
			if (contents.nonEmptyStream().toList().isEmpty()) {
				return;
			}

			ClientPacketListener listener = Minecraft.getInstance().getConnection();
			List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();
			List<Either<FormattedText, TooltipComponent>> tooltipCopy = new ArrayList<>(tooltip);

			for(int i = 1; i < tooltipCopy.size(); i++) {
				Either<FormattedText, TooltipComponent> either = tooltipCopy.get(i);
				if(either.left().isPresent() && either.left().get() instanceof MutableComponent component) {
					String s = either.left().get().getString();
					if (component.getContents() instanceof TranslatableContents translatableContents && translatableContents.getKey().contains("container.shulkerBox"))
						tooltip.remove(either);
				}
			}

			if(!ImprovedTooltipsModule.shulkerBoxRequireShift || Screen.hasShiftDown())
				tooltip.add(1, Either.right(new ShulkerComponent(stack)));
			if(ImprovedTooltipsModule.shulkerBoxRequireShift && !Screen.hasShiftDown())
				tooltip.add(1, Either.left(Component.translatable("quark.misc.shulker_box_shift")));
		}
	}

	public record ShulkerComponent(ItemStack stack) implements ClientTooltipComponent, TooltipComponent {

		private static final int[][] TARGET_RATIOS = new int[][] {
				{ 1, 1 },
				{ 9, 3 },
				{ 9, 5 },
				{ 9, 6 },
				{ 9, 8 },
				{ 9, 9 },
				{ 12, 9 }
		};

		private static final int CORNER = 5;
		private static final int BUFFER = 1;
		private static final int EDGE = 18;

		@Override
		public void renderImage(@NotNull Font font, int tooltipX, int tooltipY, @NotNull GuiGraphics guiGraphics) {
			Minecraft mc = Minecraft.getInstance();

			PoseStack pose = guiGraphics.pose();

			if(stack.has(DataComponents.CONTAINER)) {
				ItemContainerContents contents = stack.get(DataComponents.CONTAINER);

				ItemStack currentBox = stack;
				int currentX = tooltipX;
				int currentY = tooltipY - 1;

                int size = Math.toIntExact(contents.nonEmptyStream().count());
				int[] dims = {Math.min(size, 9), 1 + (size-1) / 9};
				for (int[] testAgainst : TARGET_RATIOS) {
					if (testAgainst[0] * testAgainst[1] == size) {
						dims = testAgainst;
						break;
					}
				}

				int texWidth = CORNER * 2 + EDGE * dims[0];
				int right = currentX + texWidth;
				Window window = mc.getWindow();
				if (right > window.getGuiScaledWidth())
					currentX -= (right - window.getGuiScaledWidth());

				pose.pushPose();
				pose.translate(0, 0, 700);

				int color = -1;

				if (ImprovedTooltipsModule.shulkerBoxUseColors && ((BlockItem) currentBox.getItem()).getBlock() instanceof ShulkerBoxBlock boxBlock) {
					DyeColor dye = boxBlock.getColor();
					if (dye != null) {
						color = dye.getTextureDiffuseColor();
					}
				}

				renderTooltipBackground(guiGraphics, mc, pose, currentX, currentY, dims[0], dims[1], color);

                Iterator<ItemStack> stackIterator = contents.nonEmptyItems().iterator();
				for (int i = 0; i < size; i++) {
					ItemStack itemstack = stackIterator.next();

					int xp = currentX + 6 + ((i) % 9) * 18;
					int yp = currentY + 6 + ((i) / 9) * 18;

					guiGraphics.renderItem(itemstack, xp, yp);
					guiGraphics.renderItemDecorations(mc.font, itemstack, xp, yp);

					if (!Quark.ZETA.modules.get(ChestSearchingModule.class).namesMatch(itemstack)) {
						RenderSystem.disableDepthTest();
						guiGraphics.fill(xp, yp, xp + 16, yp + 16, 0xAA000000);
                        RenderSystem.enableDepthTest();
					}
				}

				pose.popPose();
			}
		}

		public static void renderTooltipBackground(GuiGraphics guiGraphics, Minecraft mc, PoseStack matrix, int x, int y, int width, int height, int color) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, WIDGET_RESOURCE);
			RenderSystem.setShaderColor(((color & 0xFF0000) >> 16) / 255f,
					((color & 0x00FF00) >> 8) / 255f,
					(color & 0x0000FF) / 255f, 1f);

			guiGraphics.blit(WIDGET_RESOURCE, x, y,
					0, 0,
					CORNER, CORNER, 256, 256);
			guiGraphics.blit(WIDGET_RESOURCE, x + CORNER + EDGE * width, y + CORNER + EDGE * height,
					CORNER + BUFFER + EDGE + BUFFER, CORNER + BUFFER + EDGE + BUFFER,
					CORNER, CORNER, 256, 256);
			guiGraphics.blit(WIDGET_RESOURCE, x + CORNER + EDGE * width, y,
					CORNER + BUFFER + EDGE + BUFFER, 0,
					CORNER, CORNER, 256, 256);
			guiGraphics.blit(WIDGET_RESOURCE, x, y + CORNER + EDGE * height,
					0, CORNER + BUFFER + EDGE + BUFFER,
					CORNER, CORNER, 256, 256);
			for(int row = 0; row < height; row++) {
				guiGraphics.blit(WIDGET_RESOURCE, x, y + CORNER + EDGE * row,
						0, CORNER + BUFFER,
						CORNER, EDGE, 256, 256);
				guiGraphics.blit(WIDGET_RESOURCE, x + CORNER + EDGE * width, y + CORNER + EDGE * row,
						CORNER + BUFFER + EDGE + BUFFER, CORNER + BUFFER,
						CORNER, EDGE, 256, 256);
				for(int col = 0; col < width; col++) {
					if(row == 0) {
						guiGraphics.blit(WIDGET_RESOURCE, x + CORNER + EDGE * col, y,
								CORNER + BUFFER, 0,
								EDGE, CORNER, 256, 256);
						guiGraphics.blit(WIDGET_RESOURCE, x + CORNER + EDGE * col, y + CORNER + EDGE * height,
								CORNER + BUFFER, CORNER + BUFFER + EDGE + BUFFER,
								EDGE, CORNER, 256, 256);
					}

					guiGraphics.blit(WIDGET_RESOURCE, x + CORNER + EDGE * col, y + CORNER + EDGE * row,
							CORNER + BUFFER, CORNER + BUFFER,
							EDGE, EDGE, 256, 256);
				}
			}
			RenderSystem.setShaderColor(1,1,1,1);
		}

		@Override
		public int getHeight() {
			if (stack.isEmpty() || !stack.has(DataComponents.CONTAINER))
				return 0;
			else {
				ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
				return 11 + (1 + (Math.toIntExact((contents.nonEmptyStream().count())) - 1) / 9) * 18;
			}
		}

		//171 max
		@Override
		public int getWidth(@NotNull Font font) {
			if (stack.isEmpty() || !stack.has(DataComponents.CONTAINER))
				return 0;
			else {
				ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
				return 9 + Math.min(Math.toIntExact(contents.nonEmptyStream().count()), 9) * 18;
			}
		}
	}

}
