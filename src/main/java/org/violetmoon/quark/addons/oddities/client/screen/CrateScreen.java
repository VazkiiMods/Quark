package org.violetmoon.quark.addons.oddities.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.addons.oddities.inventory.CrateMenu;
import org.violetmoon.quark.addons.oddities.module.CrateModule;
import org.violetmoon.quark.api.IQuarkButtonAllowed;
import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.client.handler.ClientUtil;
import org.violetmoon.quark.base.client.handler.InventoryButtonHandler;
import org.violetmoon.quark.base.client.handler.InventoryButtonHandler.ButtonTargetType;
import org.violetmoon.quark.content.client.module.ChestSearchingModule;

import java.util.List;

public class CrateScreen extends AbstractContainerScreen<CrateMenu> implements IQuarkButtonAllowed {
	private static final ResourceLocation TEXTURE = Quark.asResource("textures/gui/crate.png");

	private int lastScroll;
	private int scrollOffs;
	private boolean scrolling;

	private List<Rect2i> extraAreas;

	public CrateScreen(CrateMenu container, Inventory inv, Component component) {
		super(container, inv, component);

		int inventoryRows = CrateMenu.numRows;
		imageHeight = 114 + inventoryRows * 18;
		inventoryLabelY = imageHeight - 94;
	}

	@Override
	protected void init() {
		super.init();

		int xOffset = (width - imageWidth) / 2;
		int yOffset = (height - imageHeight) / 2;
		extraAreas = Lists.newArrayList(new Rect2i(xOffset + imageWidth, yOffset, 23, 136));
	}

	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(guiGraphics, mouseX, mouseY, partialTicks);
		super.render(guiGraphics, mouseX, mouseY, partialTicks);
		renderTooltip(guiGraphics, mouseX, mouseY);
	}

	private boolean canScroll() {
		return (menu.getStackCount() / CrateMenu.numCols) > 0;
	}

	private float getPxPerScroll() {
		return 95F / ((float)(menu.getStackCount() / CrateMenu.numCols));
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) { // TODO: Fix later, likely incorrect
		menu.scroll(scrollY < 0, true);
		lastScroll = scrollOffs = Math.round((float)(menu.scroll / CrateMenu.numCols) * getPxPerScroll());
		return true;
	}

	@Override
	public boolean mouseClicked(double p_98531_, double p_98532_, int p_98533_) {
		if(p_98533_ == 0 && insideScrollbar(p_98531_, p_98532_)) {
			scrolling = canScroll();

			return true;
		}

		return super.mouseClicked(p_98531_, p_98532_, p_98533_);
	}

	protected boolean insideScrollbar(double mouseX, double mouseY) {
		int left = leftPos + 175;
		int top = topPos + 18;
		int right = left + 14;
		int bottom = top + 112;

		return mouseX >= left && mouseY >= top && mouseX < right && mouseY < bottom;
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if(scrolling) {
			int top = topPos + 18;

			double relative = mouseY - top - 6;
			if(relative < 0)
				relative = 0;
			else if(relative > 95)
				relative = 95;

			scrollOffs = (int) relative;

			float diff = (float) (scrollOffs - lastScroll);
			float pixelsNeeded = getPxPerScroll();

			while(Math.abs(diff) >= pixelsNeeded) {
				boolean up = diff > 0;

				menu.scroll(up, true);
				lastScroll = Math.round((float)(menu.scroll / CrateMenu.numCols) * pixelsNeeded);
				diff = (float) (scrollOffs - lastScroll);
			}

			return true;
		}

		else
			return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseReleased(double p_98622_, double p_98623_, int p_98624_) {
		if(p_98624_ == 0)
			scrolling = false;

		return super.mouseReleased(p_98622_, p_98623_, p_98624_);
	}

	@Override
	protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTicks, int x, int y) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		int i = (width - imageWidth) / 2;
		int j = (height - imageHeight) / 2;
		guiGraphics.blit(TEXTURE, i, j, 0, 0, imageWidth + 20, imageHeight);

		int maxScroll = (menu.getStackCount() / CrateMenu.numCols) * CrateMenu.numCols;

		int u = 232 + (maxScroll == 0 ? 12 : 0);
		int by = j + 18 + scrollOffs;
		guiGraphics.blit(TEXTURE, i + imageWidth, by, u, 0, 12, 15);

		if(!Quark.ZETA.modules.get(ChestSearchingModule.class).searchBarShown()) {
			String s = menu.getTotal() + "/" + CrateModule.maxItems;

			int color = ClientUtil.getGuiTextColor("crate_count");
			guiGraphics.drawString(font, s, i + this.imageWidth - font.width(s) - 8 - InventoryButtonHandler.getActiveButtons(ButtonTargetType.CONTAINER_INVENTORY).size() * 12, j + 6, color, false);
		}

        guiGraphics.drawString(font, "lastScroll: " + lastScroll, 4, 4, 0xFFFFFF);
        guiGraphics.drawString(font, "scrollOffs: " + scrollOffs, 4, 16, 0xFFFFFF);
        guiGraphics.drawString(font, "menu.scroll: " + menu.scroll, 4, 28, 0xFFFFFF);
    }

	@Override
	protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
		int color = ClientUtil.getGuiTextColor("crate_count");

		guiGraphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, color, false);
		guiGraphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, color, false);
	}
}
