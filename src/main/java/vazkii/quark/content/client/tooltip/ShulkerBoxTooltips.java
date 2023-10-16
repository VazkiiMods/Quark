package vazkii.quark.content.client.tooltip;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import vazkii.zeta.util.ItemNBTHelper;
import vazkii.quark.base.handler.SimilarBlockTypeHandler;
import vazkii.quark.content.client.module.ChestSearchingModule;
import vazkii.quark.content.client.module.ImprovedTooltipsModule;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ShulkerBoxTooltips {

	public static final ResourceLocation WIDGET_RESOURCE = new ResourceLocation("quark", "textures/misc/shulker_widget.png");

	@OnlyIn(Dist.CLIENT)
	public static void makeTooltip(RenderTooltipEvent.GatherComponents event) {
		ItemStack stack = event.getItemStack();
		if(SimilarBlockTypeHandler.isShulkerBox(stack)) {
			CompoundTag cmp = ItemNBTHelper.getCompound(stack, "BlockEntityTag", false);

			if(cmp.contains("LootTable"))
				return;

			if(!cmp.contains("id"))
				return;

			BlockEntity te = BlockEntity.loadStatic(BlockPos.ZERO, ((BlockItem) stack.getItem()).getBlock().defaultBlockState(), cmp);
			if (te != null && te.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
				List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();
				List<Either<FormattedText, TooltipComponent>> tooltipCopy = new ArrayList<>(tooltip);

				for (int i = 1; i < tooltipCopy.size(); i++) {
					Either<FormattedText, TooltipComponent> either = tooltipCopy.get(i);
					if(either.left().isPresent()) {
						String s = either.left().get().getString();
						if (!s.startsWith("\u00a7") || s.startsWith("\u00a7o"))
							tooltip.remove(either);
					}
				}

				if(!ImprovedTooltipsModule.shulkerBoxRequireShift || Screen.hasShiftDown())
					tooltip.add(1, Either.right(new ShulkerComponent(stack)));
				if(ImprovedTooltipsModule.shulkerBoxRequireShift && !Screen.hasShiftDown())
					tooltip.add(1, Either.left(Component.translatable("quark.misc.shulker_box_shift")));
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	public record ShulkerComponent(ItemStack stack) implements ClientTooltipComponent, TooltipComponent {

		private static final int[][] TARGET_RATIOS = new int[][]{
				{1, 1},
				{9, 3},
				{9, 5},
				{9, 6},
				{9, 8},
				{9, 9},
				{12, 9}
		};

		private static final int CORNER = 5;
		private static final int BUFFER = 1;
		private static final int EDGE = 18;


		@Override
		public void renderImage(@Nonnull Font font, int tooltipX, int tooltipY, @Nonnull PoseStack pose, @Nonnull ItemRenderer itemRenderer, int something) {
			Minecraft mc = Minecraft.getInstance();

			CompoundTag cmp = ItemNBTHelper.getCompound(stack, "BlockEntityTag", true);
			if (cmp != null) {
				if (cmp.contains("LootTable"))
					return;

				if (!cmp.contains("id")) {
					cmp = cmp.copy();
					cmp.putString("id", "minecraft:shulker_box");
				}
				BlockEntity te = BlockEntity.loadStatic(BlockPos.ZERO, ((BlockItem) stack.getItem()).getBlock().defaultBlockState(), cmp);
				if (te != null) {
					if (te instanceof RandomizableContainerBlockEntity randomizable)
						randomizable.setLootTable(null, 0);

					LazyOptional<IItemHandler> handler = te.getCapability(ForgeCapabilities.ITEM_HANDLER, null);
					handler.ifPresent((capability) -> {
						ItemStack currentBox = stack;
						int currentX = tooltipX;
						int currentY = tooltipY - 1;

						int size = capability.getSlots();
						int[] dims = {Math.min(size, 9), Math.max(size / 9, 1)};
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
								float[] colorComponents = dye.getTextureDiffuseColors();
								color = ((int) (colorComponents[0] * 255) << 16) |
										((int) (colorComponents[1] * 255) << 8) |
										(int) (colorComponents[2] * 255);
							}
						}

						renderTooltipBackground(mc, pose, currentX, currentY, dims[0], dims[1], color);

						ItemRenderer render = mc.getItemRenderer();

						for (int i = 0; i < size; i++) {
							ItemStack itemstack = capability.getStackInSlot(i);
							int xp = currentX + 6 + (i % 9) * 18;
							int yp = currentY + 6 + (i / 9) * 18;

							if (!itemstack.isEmpty()) {
								render.renderAndDecorateItem(itemstack, xp, yp);
								render.renderGuiItemDecorations(mc.font, itemstack, xp, yp);
							}

							if (!ChestSearchingModule.namesMatch(itemstack)) {
								RenderSystem.disableDepthTest();
								GuiComponent.fill(pose, xp, yp, xp + 16, yp + 16, 0xAA000000);
							}
						}

						pose.popPose();
					});

				}
			}
		}

		public static void renderTooltipBackground(Minecraft mc, PoseStack matrix, int x, int y, int width, int height, int color) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, WIDGET_RESOURCE);
			RenderSystem.setShaderColor(((color & 0xFF0000) >> 16) / 255f,
					((color & 0x00FF00) >> 8) / 255f,
					(color & 0x0000FF) / 255f, 1f);

			GuiComponent.blit(matrix, x, y,
					0, 0,
					CORNER, CORNER, 256, 256);
			GuiComponent.blit(matrix, x + CORNER + EDGE * width, y + CORNER + EDGE * height,
					CORNER + BUFFER + EDGE + BUFFER, CORNER + BUFFER + EDGE + BUFFER,
					CORNER, CORNER, 256, 256);
			GuiComponent.blit(matrix, x + CORNER + EDGE * width, y,
					CORNER + BUFFER + EDGE + BUFFER, 0,
					CORNER, CORNER, 256, 256);
			GuiComponent.blit(matrix, x, y + CORNER + EDGE * height,
					0, CORNER + BUFFER + EDGE + BUFFER,
					CORNER, CORNER, 256, 256);
			for (int row = 0; row < height; row++) {
				GuiComponent.blit(matrix, x, y + CORNER + EDGE * row,
						0, CORNER + BUFFER,
						CORNER, EDGE, 256, 256);
				GuiComponent.blit(matrix, x + CORNER + EDGE * width, y + CORNER + EDGE * row,
						CORNER + BUFFER + EDGE + BUFFER, CORNER + BUFFER,
						CORNER, EDGE, 256, 256);
				for (int col = 0; col < width; col++) {
					if (row == 0) {
						GuiComponent.blit(matrix, x + CORNER + EDGE * col, y,
								CORNER + BUFFER, 0,
								EDGE, CORNER, 256, 256);
						GuiComponent.blit(matrix, x + CORNER + EDGE * col, y + CORNER + EDGE * height,
								CORNER + BUFFER, CORNER + BUFFER + EDGE + BUFFER,
								EDGE, CORNER, 256, 256);
					}

					GuiComponent.blit(matrix, x + CORNER + EDGE * col, y + CORNER + EDGE * row,
							CORNER + BUFFER, CORNER + BUFFER,
							EDGE, EDGE, 256, 256);
				}
			}
		}

		@Override
		public int getHeight() {
			return 65;
		}

		@Override
		public int getWidth(@Nonnull Font font) {
			return 171;
		}
	}

}
