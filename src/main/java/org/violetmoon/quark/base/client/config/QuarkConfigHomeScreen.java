package org.violetmoon.quark.base.client.config;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.NotNull;

import org.violetmoon.quark.base.Quark;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.config.QuarkGeneralConfig;
import org.violetmoon.quark.base.handler.ContributorRewardHandler;
import org.violetmoon.zeta.client.config.screen.ZetaConfigHomeScreen;

import java.util.Iterator;
import java.util.List;

public class QuarkConfigHomeScreen extends ZetaConfigHomeScreen {

	private static final CubeMap CUBE_MAP = new CubeMap(Quark.asResource("textures/misc/panorama/panorama"));
	private static final PanoramaRenderer PANORAMA = new PanoramaRenderer(CUBE_MAP);
	private float time;

	public QuarkConfigHomeScreen(Screen parent) {
		super(QuarkClient.ZETA_CLIENT, parent);
	}

	@Override
	protected void init() {
		super.init();

		List<Integer> socialButtonPlacements = centeredRow(width / 2, 20, 5, 5);
		Iterator<Integer> iter = socialButtonPlacements.iterator();
		addRenderableWidget(new SocialButton(iter.next(), height - 55, Component.translatable("quark.gui.config.social.website"), 0x48ddbc, 0, "https://quarkmod.net"));
		addRenderableWidget(new SocialButton(iter.next(), height - 55, Component.translatable("quark.gui.config.social.discord"), 0x7289da, 1, "https://discord.gg/vm"));
		addRenderableWidget(new SocialButton(iter.next(), height - 55, Component.translatable("quark.gui.config.social.patreon"), 0xf96854, 2, "https://patreon.com/vazkii"));
		addRenderableWidget(new SocialButton(iter.next(), height - 55, Component.translatable("quark.gui.config.social.forum"), 0xb650d8, 3, "https://forum.violetmoon.org"));
		addRenderableWidget(new SocialButton(iter.next(), height - 55, Component.translatable("quark.gui.config.social.twitter"), 0x1da1f2, 4, "https://twitter.com/VazkiiMods"));
	}

	//annoyingly it's not passed to renderBackground
	//protected float partialTicks;

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		time += partialTick;

		Minecraft mc = Minecraft.getInstance();

		if(mc.level == null) {
			float spin = 1f;
			float blur = 0.85F;

			if (time < 20F && !QuarkGeneralConfig.disableQMenuEffects) {
				spin += (20F - time);
				blur = (time / 20F) * 0.75F + 0.1F;
			}

			PANORAMA.render(graphics, this.width, this.height, blur, partialTick);
		} else {
			super.renderBackground(graphics, mouseX, mouseY, partialTick);
		}

		int boxWidth = 400;
		graphics.fill(width / 2 - boxWidth / 2, 0, width / 2 + boxWidth / 2, this.height, 0x66000000);
		graphics.fill(width / 2 - boxWidth / 2 - 1, 0, width / 2 - boxWidth / 2, this.height, 0x66999999); // nice
		graphics.fill(width / 2 + boxWidth / 2, 0, width / 2 + boxWidth / 2 + 1, this.height, 0x66999999);
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		//this.partialTicks = partialTicks;
		super.render(guiGraphics, mouseX, mouseY, partialTicks);

		guiGraphics.drawCenteredString(font, I18n.get("quark.gui.config.subheader1", ChatFormatting.LIGHT_PURPLE, ContributorRewardHandler.featuredPatron, ChatFormatting.RESET), width / 2, 28, 0x9EFFFE);
		guiGraphics.drawCenteredString(font, I18n.get("quark.gui.config.subheader2"), width / 2, 38, 0x9EFFFE);
	}
}
