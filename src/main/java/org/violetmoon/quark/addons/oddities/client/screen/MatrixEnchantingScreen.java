package org.violetmoon.quark.addons.oddities.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.block.be.MatrixEnchantingTableBlockEntity;
import org.violetmoon.quark.addons.oddities.inventory.EnchantmentMatrix;
import org.violetmoon.quark.addons.oddities.inventory.EnchantmentMatrix.Piece;
import org.violetmoon.quark.addons.oddities.inventory.MatrixEnchantingMenu;
import org.violetmoon.quark.addons.oddities.module.MatrixEnchantingModule;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.handler.ClientUtil;
import org.violetmoon.quark.base.network.message.oddities.MatrixEnchanterOperationMessage;
import org.violetmoon.quark.catnip.animation.AnimationTickHolder;

import java.util.LinkedList;
import java.util.List;

public class MatrixEnchantingScreen extends AbstractContainerScreen<MatrixEnchantingMenu> {

	public static final ResourceLocation BACKGROUND = Quark.asResource("textures/misc/matrix_enchanting.png");

	protected final Inventory playerInv;
	protected final MatrixEnchantingTableBlockEntity enchanter;

	protected Button plusButton;
	protected MatrixEnchantingPieceList pieceList;
	protected Piece hoveredPiece;

	protected int selectedPiece = -1;
	protected int gridHoverX, gridHoverY;
	protected List<Integer> listPieces = null;

	public MatrixEnchantingScreen(MatrixEnchantingMenu container, Inventory inventory, Component component) {
		super(container, inventory, component);
		this.playerInv = inventory;
		this.enchanter = container.enchanter;
	}

	@Override
	public void init() {
		super.init();

		selectedPiece = -1;
		addRenderableWidget(plusButton = new MatrixEnchantingPlusButton(leftPos + 86, topPos + 63, this::add));
		pieceList = new MatrixEnchantingPieceList(this, 28, 64, topPos + 11, topPos + 75, 22);
		pieceList.setX(leftPos + 139);
		addRenderableWidget(pieceList);
		updateButtonStatus();

		pieceList.refresh();
		enchanter.updateEnchantPower();
	}

	@Override
	public void containerTick() {
		super.containerTick();
		updateButtonStatus();

		if(enchanter.matrix == null) {
			selectedPiece = -1;
			pieceList.refresh();
		}

		if(enchanter.clientMatrixDirty) {
			pieceList.refresh();
			enchanter.clientMatrixDirty = false;
		}
	}

	@Override
	protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
		Minecraft mc = getMinecraft();
		PoseStack pose = guiGraphics.pose();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		int i = leftPos;
		int j = topPos;
		guiGraphics.blit(BACKGROUND, i, j, 0, 0, imageWidth, imageHeight);

		if(enchanter.charge > 0 && MatrixEnchantingModule.chargePerLapis > 0) {
			int maxHeight = 18;
			int barHeight = (int) (((float) enchanter.charge / MatrixEnchantingModule.chargePerLapis) * maxHeight);
			guiGraphics.blit(BACKGROUND, i + 7, j + 32 + maxHeight - barHeight, 50, 176 + maxHeight - barHeight, 4, barHeight);
		}

		pieceList.render(guiGraphics, mouseX, mouseY, partialTicks);

		boolean showCost = enchanter.matrix != null
				&& enchanter.matrix.canGeneratePiece(enchanter.influences, enchanter.bookshelfPower, enchanter.enchantability)
				&& !mc.player.getAbilities().instabuild;

		String text = ""+enchanter.bookshelfPower;
		int x = i + 50;
		int y = j + 55;
		
		if(enchanter.bookshelfPower > 0) {
			pose.pushPose();
			guiGraphics.renderItem(new ItemStack(Items.BOOK), x, y);
			pose.translate(0, 0, 200);
	
			x -= font.width(text) / 2;
	
			drawBorderedText(guiGraphics, text, x + 3, y + 6, 0xc8ff8f);
			pose.popPose();
		}

		if(showCost) {
			int xpCost = enchanter.matrix.getNewPiecePrice();
			int xpMin = enchanter.matrix.getMinXpLevel(enchanter.bookshelfPower);
			boolean has = enchanter.matrix.validateXp(mc.player, enchanter.bookshelfPower);
			
			x = i + 71;
			y = j + 56;
			text = String.valueOf(xpCost);
			
			guiGraphics.blit(BACKGROUND, x, y, 0, imageHeight, 10, 10);

			if(!has && mc.player.experienceLevel < xpMin) {
				text = I18n.get("quark.gui.enchanting.min", xpMin);
				x += 4;
			}

			x -= font.width(text) / 2;
			drawBorderedText(guiGraphics, text, x + 2, y + 5, has ? 0xc8ff8f : 0xff8f8f);
		}
	}

	private void drawBorderedText(GuiGraphics guiGraphics, String text, int x, int y, int color) {
		guiGraphics.drawString(font, text, x - 1, y, 0, false);
		guiGraphics.drawString(font, text, x + 1, y, 0, false);
		guiGraphics.drawString(font, text, x, y + 1, 0, false);
		guiGraphics.drawString(font, text, x, y - 1, 0, false);
		guiGraphics.drawString(font, text, x, y, color, false);
	}

	@Override
	protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
		int color = ClientUtil.getGuiTextColor("matrix_enchanting");

		guiGraphics.drawString(font, enchanter.getDisplayName().getString(), 12, 5, color, false);
		guiGraphics.drawString(font, playerInv.getDisplayName().getString(), 8, imageHeight - 96 + 2, color, false);

		if(enchanter.matrix != null) {
			boolean needsRefresh = listPieces == null;
			listPieces = enchanter.matrix.benchedPieces;
			if(needsRefresh)
				pieceList.refresh();
			renderMatrixGrid(guiGraphics, enchanter.matrix);
		}
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		if(hoveredPiece != null) {
			List<Component> tooltip = new LinkedList<>();
			tooltip.add(Component.translatable(Enchantment.getFullname(hoveredPiece.enchant,hoveredPiece.level).getString().replaceAll("\\u00A7.", "")).withStyle(ChatFormatting.GOLD));

			if(hoveredPiece.influence > 0)
				tooltip.add(Component.translatable("quark.gui.enchanting.influence", (int) (hoveredPiece.influence * MatrixEnchantingModule.influencePower * 100)).withStyle(ChatFormatting.GRAY));
			else if(hoveredPiece.influence < 0)
				tooltip.add(Component.translatable("quark.gui.enchanting.dampen", (int) (hoveredPiece.influence * MatrixEnchantingModule.influencePower * 100)).withStyle(ChatFormatting.GRAY));

			int max = hoveredPiece.getMaxXP();
			if(max > 0)
				tooltip.add(Component.translatable("quark.gui.enchanting.upgrade", hoveredPiece.xp, max).withStyle(ChatFormatting.GRAY));

			if(gridHoverX == -1) {
				tooltip.add(Component.literal(""));
				tooltip.add(Component.translatable("quark.gui.enchanting.left_click").withStyle(ChatFormatting.GRAY));
				tooltip.add(Component.translatable("quark.gui.enchanting.right_click").withStyle(ChatFormatting.GRAY));
			} else if(selectedPiece != -1) {
				Piece p = getPiece(selectedPiece);
				if(p != null && p.enchant == hoveredPiece.enchant && hoveredPiece.level < hoveredPiece.enchant.value().getMaxLevel()) {
					tooltip.add(Component.literal(""));
					tooltip.add(Component.translatable("quark.gui.enchanting.merge").withStyle(ChatFormatting.GRAY));
				}
			}

			guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY); // renderTooltip
		} else
			renderTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		int gridMouseX = (int) (mouseX - leftPos - 86);
		int gridMouseY = (int) (mouseY - topPos - 11);

		gridHoverX = gridMouseX < 0 ? -1 : gridMouseX / 10;
		gridHoverY = gridMouseY < 0 ? -1 : gridMouseY / 10;
		if(gridHoverX < 0 || gridHoverX > 4 || gridHoverY < 0 || gridHoverY > 4) {
			gridHoverX = -1;
			gridHoverY = -1;
			hoveredPiece = null;
		} else if(enchanter.matrix != null) {
			int hover = enchanter.matrix.matrix[gridHoverX][gridHoverY];
			hoveredPiece = getPiece(hover);
		}

		pieceList.mouseMoved(gridMouseX, gridMouseY);

		super.mouseMoved(mouseX, mouseY);
	}

	@Override
	public boolean mouseDragged(double p_97752_, double p_97753_, int p_97754_, double p_97755_, double p_97756_) {
		pieceList.mouseDragged(p_97752_, p_97753_, p_97754_, p_97755_, p_97756_);

		return super.mouseDragged(p_97752_, p_97753_, p_97754_, p_97755_, p_97756_);
	}

	@Override
	public boolean mouseReleased(double p_97812_, double p_97813_, int p_97814_) {
		pieceList.mouseReleased(p_97812_, p_97813_, p_97814_);

		return super.mouseReleased(p_97812_, p_97813_, p_97814_);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);

		pieceList.mouseClicked(mouseX, mouseY, mouseButton);

		if(enchanter.matrix == null)
			return true;

		if(mouseButton == 0 && gridHoverX != -1) { // left click
			int hover = enchanter.matrix.matrix[gridHoverX][gridHoverY];

			if(selectedPiece != -1) {
				if(hover == -1)
					place(selectedPiece, gridHoverX, gridHoverY);
				else
					merge(selectedPiece);
			} else {
				remove(hover);
				if(!hasShiftDown())
					selectedPiece = hover;
			}
		} else if(mouseButton == 1 && selectedPiece != -1) {
			rotate(selectedPiece);
		}

		return true;
	}

	private void renderMatrixGrid(GuiGraphics guiGraphics, EnchantmentMatrix matrix) {
		PoseStack stack = guiGraphics.pose();

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, BACKGROUND);

		stack.pushPose();
		stack.translate(86, 11, 0);

		for(int i : matrix.placedPieces) {
			Piece piece = getPiece(i);
			if(piece != null) {
				stack.pushPose();
				stack.translate(piece.x * 10, piece.y * 10, 0);
				renderPiece(guiGraphics, piece, 1F);
				stack.popPose();
			}
		}

		if(selectedPiece != -1 && gridHoverX != -1) {
			Piece piece = getPiece(selectedPiece);
			if(piece != null && !(hoveredPiece != null && piece.enchant == hoveredPiece.enchant && hoveredPiece.level < hoveredPiece.enchant.value().getMaxLevel())) {
				stack.pushPose();
				stack.translate(gridHoverX * 10, gridHoverY * 10, 0);

				float a = 0.2F;
				if(matrix.canPlace(piece, gridHoverX, gridHoverY))
					a = (float) ((Math.sin(AnimationTickHolder.getTicks() + Minecraft.getInstance().getTimer().getGameTimeDeltaTicks() * 0.2) + 1) * 0.4 + 0.4);
				renderPiece(guiGraphics, piece, a);
				stack.popPose();
			}
		}

		if(hoveredPiece == null && gridHoverX != -1)
			renderHover(guiGraphics, gridHoverX, gridHoverY);

		stack.popPose();
	}

	protected void renderPiece(GuiGraphics guiGraphics, Piece piece, float a) {
		float r = ((piece.color >> 16) & 0xFF) / 255F;
		float g = ((piece.color >> 8) & 0xFF) / 255F;
		float b = (piece.color & 0xFF) / 255F;

		boolean hovered = hoveredPiece == piece;

		for(int[] block : piece.blocks)
			renderBlock(guiGraphics, block[0], block[1], piece.type, r, g, b, a, hovered);
	}

	private void renderBlock(GuiGraphics guiGraphics, int x, int y, int type, float r, float g, float b, float a, boolean hovered) {
		RenderSystem.setShaderColor(r, g, b, a);
		guiGraphics.blit(BACKGROUND, x * 10, y * 10, 11 + type * 10, imageHeight, 10, 10);
		if(hovered)
			renderHover(guiGraphics, x, y);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}

	private void renderHover(GuiGraphics guiGraphics, int x, int y) {
		guiGraphics.fill(x * 10, y * 10, x * 10 + 10, y * 10 + 10, 0x66FFFFFF);
	}

	public void add(Button button) {
		send(MatrixEnchantingTableBlockEntity.OPER_ADD, 0, 0, 0);
	}

	public void place(int id, int x, int y) {
		send(MatrixEnchantingTableBlockEntity.OPER_PLACE, id, x, y);
		selectedPiece = -1;
		click();
	}

	public void remove(int id) {
		send(MatrixEnchantingTableBlockEntity.OPER_REMOVE, id, 0, 0);
	}

	public void rotate(int id) {
		send(MatrixEnchantingTableBlockEntity.OPER_ROTATE, id, 0, 0);
	}

	public void merge(int id) {
		int hover = enchanter.matrix.matrix[gridHoverX][gridHoverY];
		Piece p = getPiece(hover);
		Piece p1 = getPiece(selectedPiece);
		if(p != null && p1 != null && p.enchant == p1.enchant && p.level < p.enchant.value().getMaxLevel()) {
			send(MatrixEnchantingTableBlockEntity.OPER_MERGE, hover, id, 0);
			selectedPiece = -1;
			click();
		}
	}

	private void send(int operation, int arg0, int arg1, int arg2) {
		MatrixEnchanterOperationMessage message = new MatrixEnchanterOperationMessage(operation, arg0, arg1, arg2);
		PacketDistributor.sendToServer(message);
	}

	private void click() {
		getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
	}

	private void updateButtonStatus() {
		plusButton.active = (enchanter.matrix != null
				&& (getMinecraft().player.getAbilities().instabuild || enchanter.charge > 0)
				&& enchanter.matrix.validateXp(getMinecraft().player, enchanter.bookshelfPower)
				&& enchanter.matrix.canGeneratePiece(enchanter.influences, enchanter.bookshelfPower, enchanter.enchantability));
	}

	protected Piece getPiece(int id) {
		EnchantmentMatrix matrix = enchanter.matrix;
		if(matrix != null)
			return matrix.pieces.get(id);

		return null;
	}

}
