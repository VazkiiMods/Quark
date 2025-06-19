package org.violetmoon.quark.addons.oddities.client.screen;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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
		super(parent.getMinecraft(), listWidth, listHeight, top, bottom);
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
		int i = this.getScrollbarPosition();
		int j = i + 6;

		guiGraphics.fill(getX(), getY(), getX() + getWidth() + 1, getY() + getHeight(), 0xFF2B2B2B);

		Window main = parent.getMinecraft().getWindow();
		int res = (int) main.getGuiScale();
		RenderSystem.enableScissor(getX() * res, (main.getGuiScaledHeight() - getBottom()) * res, getWidth() * res, getHeight() * res);
		renderListItems(guiGraphics, mouseX, mouseY, partialTicks);
		RenderSystem.disableScissor();
		renderScroll(guiGraphics, i, j);
	}

	protected int getMaxScroll2() {
		return Math.max(0, this.getMaxPosition() - (this.getHeight() - this.getY() - 4));
	}

	private void renderScroll(GuiGraphics guiGraphics, int i, int j) {
		int j1 = this.getMaxScroll2();
		if(j1 > 0) {
			int k1 = (int) ((float) ((this.getHeight() - this.getY()) * (this.getHeight() - this.getY())) / (float) this.getMaxPosition());
			k1 = Mth.clamp(k1, 32, this.getHeight() - this.getY() - 8);
			int l1 = (int) this.getScrollAmount() * (this.getHeight() -  this.getY() - k1) / j1 + this.getY();
			if(l1 < this.getY()) {
				l1 = this.getY();
			}

			guiGraphics.fill(i, getHeight(), j, getY(), 0xFF000000);
			guiGraphics.fill(i, (l1 + k1), j, l1, 0xFF818181);
			guiGraphics.fill(i, (l1 + k1 - 1), j - 1, l1, 0xFFc0c0c0);
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
