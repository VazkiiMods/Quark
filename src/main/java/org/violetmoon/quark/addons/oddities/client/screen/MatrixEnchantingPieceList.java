package org.violetmoon.quark.addons.oddities.client.screen;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.inventory.EnchantmentMatrix.Piece;

public class MatrixEnchantingPieceList extends ObjectSelectionList<MatrixEnchantingPieceList.PieceEntry> {

	private final MatrixEnchantingScreen parent;
	private final int listWidth;

	public MatrixEnchantingPieceList(MatrixEnchantingScreen parent, int listWidth, int listHeight, int top, int bottom, int entryHeight) {
		super(parent.getMinecraft(), listWidth, listHeight, top, entryHeight);
		this.listWidth = listWidth;
		this.parent = parent;
	}

	@Override
	protected int getScrollbarPosition() {
		return getX() + this.listWidth - 5;
	}

	@Override
	public int getRowWidth() {
		return this.listWidth;
	}

	public void refresh() {
		clearEntries();

		if(parent.listPieces != null)
			for(int i : parent.listPieces) {
				Piece piece = parent.getPiece(i);
				if(piece != null)
					addEntry(new PieceEntry(piece, i));
			}
	}

	@Override
	public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		int scrollbarStartX = this.getScrollbarPosition();
		int scrollbarEndX = scrollbarStartX + 6;

		guiGraphics.fill(getX(), getY(), getX() + getWidth() + 1, getY() + getHeight(), 0xFF2B2B2B);

		Window main = parent.getMinecraft().getWindow();
		int res = (int) main.getGuiScale();
		RenderSystem.enableScissor(getX() * res, (main.getGuiScaledHeight() - getBottom()) * res, getWidth() * res, getHeight() * res);
		setScrollAmount(Math.min(this.getScrollAmount(), getMaxScroll()));
		renderListItems(guiGraphics, mouseX, mouseY, partialTicks);
		RenderSystem.disableScissor();
		renderScroll(guiGraphics, scrollbarStartX, scrollbarEndX);
	}

	protected int getMaxScroll2() {
		return Math.max(0, this.getMaxPosition() - (this.getHeight() - this.getY() - 4));
	}

	/**
	 * The method that renders the scrollbar. Note that you do NOT send in the y-pos or the height of the scrollbar. Thats all calculated here.
	 * @param guiGraphics GuiGraphics object, needed to actually render stuff.
	 * @param scrollbarStartX The starting x-pos of the scrollbar.
	 * @param scrollbarEndX The ending x-pos of the scrollbar.
	 */
	private void renderScroll(GuiGraphics guiGraphics, int scrollbarStartX, int scrollbarEndX) {
		int maxScrollHeight = this.getMaxScroll();
		if(maxScrollHeight > 0) {
			int diff = (this.getY() - this.getHeight());
			int scrollbarSize = ((this.getHeight())*(this.getHeight()))/(this.getMaxPosition());
			scrollbarSize = Math.clamp(scrollbarSize, 0, this.getHeight());
			//Lerp func. A+(B-A)*T for the uninitiated. Goes between the highest and lowest ypos the scrollbar can be
			int scrollbarYPos = (int) ((this.getY() + scrollbarSize) + (this.getHeight() - scrollbarSize) * (getScrollAmount() / getMaxScroll()));

			guiGraphics.drawString(this.parent.getMinecraft().font, "getY: " + this.getY(),4,4,0xFFFFFF);
			guiGraphics.drawString(this.parent.getMinecraft().font, "getHeight: " + this.getHeight(),4,16,0xFFFFFF);
			guiGraphics.drawString(this.parent.getMinecraft().font, "maxScrollHeight: " + maxScrollHeight,4,28,0xFFFFFF);
			guiGraphics.drawString(this.parent.getMinecraft().font, "scrollbarStartX: " + scrollbarStartX,4,40,0xFFFFFF);
			guiGraphics.drawString(this.parent.getMinecraft().font, "scrollbarEndX: " + scrollbarEndX,4,52,0xFFFFFF);
			guiGraphics.drawString(this.parent.getMinecraft().font, "scrollbarSize: " + scrollbarSize,4,64,0xFFFFFF);
			guiGraphics.drawString(this.parent.getMinecraft().font, "scrollbarYPos: " + scrollbarYPos,4,76,0xFFFFFF);
			guiGraphics.drawString(this.parent.getMinecraft().font, "maxPos: " + getMaxPosition(),4,88,0xFFFFFF);
			guiGraphics.drawString(this.parent.getMinecraft().font, "scrollAmount: " + getScrollAmount(),4,100,0xFFFFFF);

			guiGraphics.fill(scrollbarStartX, this.getY()+getHeight(), scrollbarEndX, getY(), 0xFF000000);
			guiGraphics.fill(scrollbarStartX, (scrollbarYPos - scrollbarSize), scrollbarEndX, scrollbarYPos, 0xFF818181);
			guiGraphics.fill(scrollbarStartX, (scrollbarYPos - scrollbarSize), scrollbarEndX - 1, scrollbarYPos, 0xFFc0c0c0);
		}
	}

	protected class PieceEntry extends ObjectSelectionList.Entry<PieceEntry> {

		private final Piece piece;
		private final int index;

		PieceEntry(Piece piece, int index) {
			this.piece = piece;
			this.index = index;
		}

		@Override
		public void render(@NotNull GuiGraphics guiGraphics, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hover, float partialTicks) {
			PoseStack stack = guiGraphics.pose();

			if(mouseX > left && mouseY > top && mouseX <= (left + entryWidth) && mouseY <= (top + entryHeight))
				parent.hoveredPiece = piece;

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, MatrixEnchantingScreen.BACKGROUND);

			stack.pushPose();
			stack.translate(left + (listWidth - 7) / 2f, top + entryHeight / 2f, 0);
			stack.scale(0.5F, 0.5F, 0.5F);
			stack.translate(-8, -8, 0);
			parent.renderPiece(guiGraphics, piece, 1F);
			stack.popPose();
		}

		@Override
		public boolean mouseClicked(double x, double y, int button) {
			parent.selectedPiece = index;
			setSelected(this);
			return false;
		}

		@NotNull
		@Override
		public Component getNarration() {
			return Component.literal("");
		}

	}

}
