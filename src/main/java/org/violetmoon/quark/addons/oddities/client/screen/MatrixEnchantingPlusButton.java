package org.violetmoon.quark.addons.oddities.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

public class MatrixEnchantingPlusButton extends Button {

	public MatrixEnchantingPlusButton(int x, int y, OnPress onPress) {
		super(Button.builder(Component.literal(""), onPress).size(50, 12).pos(x, y));
	}

	@Override
	public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		boolean hovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + width && mouseY < getY() + height;
		if(!visible)
			return;

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		int u = 0;
		int v = 177;

		if(!active)
			v += 12;
		else if(hovered)
			v += 24;

		guiGraphics.blit(MatrixEnchantingScreen.BACKGROUND, getX(), getY(), u, v, width, height);
	}

}
