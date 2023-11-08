package org.violetmoon.quark.addons.oddities.client.screen;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.violetmoon.quark.addons.oddities.inventory.EnchantmentMatrix.Piece;

import javax.annotation.Nonnull;

public class MatrixEnchantingPieceList extends ObjectSelectionList<MatrixEnchantingPieceList.PieceEntry> {

	private final MatrixEnchantingScreen parent;
	private final int listWidth;

	public MatrixEnchantingPieceList(MatrixEnchantingScreen parent, int listWidth, int listHeight, int top, int bottom, int entryHeight) {
		super(parent.getMinecraft(), listWidth, listHeight, top, bottom, entryHeight);
		this.listWidth = listWidth;
		this.parent = parent;
	}

	@Override
	protected int getScrollbarPosition() {
		return getLeft() + this.listWidth - 5;
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
	public void render(@Nonnull PoseStack stack, int mouseX, int mouseY, float partialTicks) {
		int i = this.getScrollbarPosition();
		int j = i + 6;

		fill(stack, getLeft(), getTop(), getLeft() + getWidth() + 1, getTop() + getHeight(), 0xFF2B2B2B);

		Window main = parent.getMinecraft().getWindow();
		int res = (int) main.getGuiScale();
		RenderSystem.enableScissor(getLeft() * res, (main.getGuiScaledHeight() - getBottom()) * res, getWidth() * res, getHeight() * res);
		renderList(stack, mouseX, mouseY, partialTicks);
		RenderSystem.disableScissor();

		renderScroll(stack, i, j);
	}

	protected int getMaxScroll2() {
		return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
	}

	private void renderScroll(PoseStack stack, int i, int j) {
		int j1 = this.getMaxScroll2();
		if (j1 > 0) {
			int k1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
			k1 = Mth.clamp(k1, 32, this.y1 - this.y0 - 8);
			int l1 = (int)this.getScrollAmount() * (this.y1 - this.y0 - k1) / j1 + this.y0;
			if (l1 < this.y0) {
				l1 = this.y0;
			}

			fill(stack, i, y1, j, y0, 0xFF000000);
			fill(stack, i, (l1 + k1), j, l1, 0xFF818181);
			fill(stack, i, (l1 + k1 - 1), j - 1, l1, 0xFFc0c0c0);
		}
	}

	@Override
	protected void renderBackground(@Nonnull PoseStack stack) {
		// NO-OP
	}

	protected class PieceEntry extends ObjectSelectionList.Entry<PieceEntry> {

		private final Piece piece;
		private final int index;

		PieceEntry(Piece piece, int index) {
			this.piece = piece;
			this.index = index;
		}

		@Override
		public void render(@Nonnull PoseStack stack, int entryIdx, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hover, float partialTicks) {
			if(mouseX > left && mouseY > top && mouseX <= (left + entryWidth) && mouseY <= (top + entryHeight))
				parent.hoveredPiece = piece;

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, MatrixEnchantingScreen.BACKGROUND);

			stack.pushPose();
			stack.translate(left + (listWidth - 7) / 2f, top + entryHeight / 2f, 0);
			stack.scale(0.5F, 0.5F, 0.5F);
			stack.translate(-8, -8, 0);
			parent.renderPiece(stack, piece, 1F);
			stack.popPose();
		}

		@Override
		public boolean mouseClicked(double x, double y, int button) {
			parent.selectedPiece = index;
			setSelected(this);
			return false;
		}

		@Nonnull
		@Override
		public Component getNarration() {
			return Component.literal("");
		}

	}

}
