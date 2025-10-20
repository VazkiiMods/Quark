package org.violetmoon.quark.base.client.config;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.Quark;

public class SocialButton extends Button {

	public static final ResourceLocation SOCIAL_ICONS = Quark.asResource("textures/gui/social_icons.png");

	private final int textColor;
	private final int socialId;

	public SocialButton(int x, int y, Component text, int textColor, int socialId, OnPress onClick) {
		super(Button.builder(Component.literal(""), onClick).size(20, 20).pos(x, y));
		this.textColor = textColor;
		this.socialId = socialId;

		setTooltip(Tooltip.create(text));
	}

	public SocialButton(int x, int y, Component text, int textColor, int socialId, String url) {
		this(x, y, text, textColor, socialId, b -> Util.getPlatform().openUri(url));
	}

	@Override
	public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

		int u = socialId * 20;
		int v = isHovered ? 20 : 0;

		guiGraphics.blit(SOCIAL_ICONS, getX(), getY(), u, v, 20, 20, 128, 64);
	}

	@Override
	public int getFGColor() {
		return textColor;
	}

}
