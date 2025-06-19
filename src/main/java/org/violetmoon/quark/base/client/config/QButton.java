package org.violetmoon.quark.base.client.config;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import net.createmod.catnip.animation.AnimationTickHolder;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.QuarkClient;
import org.violetmoon.quark.base.client.handler.ClientUtil;
import org.violetmoon.quark.base.config.QuarkGeneralConfig;
import org.violetmoon.quark.base.handler.ContributorRewardHandler;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public class QButton extends Button {

	private static final int ORANGE = 1;
	private static final int PURPLE = 2;
	private static final int RAINBOW = 3;
	private static final int QUARK = 4;

	private static final List<Celebration> CELEBRATIONS = new ArrayList<>();
	static {
		celebrate("quark", 21, Month.MARCH, QUARK);
		celebrate("vm", 29, Month.APRIL, PURPLE);
		celebrate("minecraft", 18, Month.NOVEMBER, ORANGE);

		celebrate("vns", 9, Month.APRIL, ORANGE);
		celebrate("vazkii", 22, Month.NOVEMBER, ORANGE);
		celebrate("wire", 23, Month.SEPTEMBER, ORANGE);
		celebrate("anb", 6, Month.JUNE, ORANGE);
		celebrate("kame", 5, Month.NOVEMBER, ORANGE);
		celebrate("adrian", 4, Month.MAY, ORANGE);
		celebrate("train", 16, Month.AUGUST, ORANGE);
		celebrate("zemmy", 9, Month.JUNE, ORANGE);
		celebrate("mat", 7, Month.FEBRUARY, ORANGE);

		celebrate("iad", 6, Month.APRIL, RAINBOW);
		celebrate("iad2", 26, Month.OCTOBER, RAINBOW);
		celebrate("idr", 8, Month.NOVEMBER, RAINBOW);
		celebrate("ld", 8, Month.OCTOBER, RAINBOW);
		celebrate("lvd", 26, Month.APRIL, RAINBOW);
		celebrate("ncod", 11, Month.OCTOBER, RAINBOW);
		celebrate("nbpd", 14, Month.JULY, RAINBOW);
		celebrate("ppad", 24, Month.MAY, RAINBOW);
		celebrate("tdr", 20, Month.NOVEMBER, RAINBOW);
		celebrate("tdv", 31, Month.MARCH, RAINBOW);
		celebrate("zdd", 1, Month.MARCH, RAINBOW);

		celebrate("afd", 1, Month.APRIL, QUARK);
		celebrate("wwd", 3, Month.MARCH, PURPLE);
		celebrate("hw", 31, Month.OCTOBER, ORANGE);
		celebrate("xmas", 25, Month.DECEMBER, PURPLE);
		celebrate("iwd", 8, Month.MARCH, PURPLE);
		celebrate("wpld", 5, Month.MAY, PURPLE);
		celebrate("iyd", 12, Month.AUGUST, PURPLE);
		celebrate("hrd", 9, Month.DECEMBER, PURPLE);
		celebrate("ny", 1, 3, Month.JANUARY, PURPLE);

		celebrate("edballs", 28, Month.APRIL, ORANGE);
		celebrate("doyouremember", 21, Month.SEPTEMBER, ORANGE);

		// Order is important, ensure mutli day ones are at the bottom
		celebrate("pm", 1, 30, Month.JUNE, RAINBOW);
		celebrate("baw", 16, 22, Month.SEPTEMBER, RAINBOW);
		celebrate("taw", 13, 19, Month.NOVEMBER, RAINBOW);
	}

	private static void celebrate(String name, int day, Month month, int tier) {
		celebrate(name, day, day, month, tier);
	}

	private static void celebrate(String name, int day, int end, Month month, int tier) {
		CELEBRATIONS.add(new Celebration(day, month.getValue(), (end - day), tier, name));
	}

	private final boolean gay;
	private Celebration celebrating;
	private boolean showBubble;

	public QButton(int x, int y) {
		super(Button.builder(Component.literal("q"), QButton::click).size(20, 20).pos(x, y));

		Calendar calendar = Calendar.getInstance();
		int month = calendar.get(Calendar.MONTH) + 1;
		int day = calendar.get(Calendar.DATE);

		gay = month == 6;

		for(Celebration c : CELEBRATIONS)
			if(c.running(day, month)) {
				celebrating = c;
				setTooltip(Tooltip.create(Component.translatable("quark.gui.celebration." + celebrating.name)));
				break;
			}

		showBubble = !getQuarkMarkerFile().exists();
	}

	@Override
	public int getFGColor() {
		return gay ? Color.HSBtoRGB((AnimationTickHolder.getTicks() + Minecraft.getInstance().getTimer().getGameTimeDeltaTicks() / 200F), 1F, 1F) : 0x48DDBC;
	}

	@Override
	public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);

		int iconIndex = Math.min(4, ContributorRewardHandler.localPatronTier);
		if(celebrating != null) {
			iconIndex = celebrating.tier;
		}

		if(iconIndex > 0) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

			int rx = getX() - 2;
			int ry = getY() - 2;

			int w = 9;
			int h = 9;

			int v = 26;

			if(celebrating != null) {
				rx -= 3;
				ry -= 2;
				w = 10;
				h = 10;
				v = 44;
			}

			int u = 256 - iconIndex * w;

			guiGraphics.blit(ClientUtil.GENERAL_ICONS, rx, ry, u, v, w, h);
		}

		if(showBubble && QuarkGeneralConfig.enableOnboarding) {
			Font font = Minecraft.getInstance().font;
			int cy = getY() - 2;
			if(AnimationTickHolder.getTicks() + Minecraft.getInstance().getTimer().getGameTimeDeltaTicks() % 20 > 10)
				cy++;

			ClientUtil.drawChatBubble(guiGraphics, getX() + 16, cy, font, I18n.get("quark.misc.configure_quark_here"), alpha, true);
		}
	}

	private static File getQuarkMarkerFile() {
		return new File(Minecraft.getInstance().gameDirectory, ".qmenu_opened.marker");
	}

	public static void click(Button b) {
		if(b instanceof QButton qb && qb.showBubble)
			try {
				getQuarkMarkerFile().createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}

		Minecraft.getInstance().setScreen(new QuarkConfigHomeScreen(Minecraft.getInstance().screen));
	}

	private record Celebration(int day, int month, int len, int tier, String name) {

		// AFAIK none of the ones I'm tracking pass beyond a month so this
		// lazy check is fine
		public boolean running(int day, int month) {
			return this.month == month && (this.day >= day && this.day <= (day + len));
		}
	}

}
