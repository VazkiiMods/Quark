package org.violetmoon.zeta.client.config.definition;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ForgeSlider;
import org.jetbrains.annotations.Nullable;
import org.violetmoon.quark.base.config.type.inputtable.RGBAColorConfig;
import org.violetmoon.zeta.client.ZetaClient;
import org.violetmoon.zeta.client.config.screen.AbstractSectionInputScreen;
import org.violetmoon.zeta.client.config.widget.PencilButton;
import org.violetmoon.zeta.config.ChangeSet;
import org.violetmoon.zeta.config.SectionDefinition;
import org.violetmoon.zeta.config.ValueDefinition;

public class RGBClientDefinition implements ClientDefinitionExt<SectionDefinition> {
	protected final ValueDefinition<Double> r;
	protected final ValueDefinition<Double> g;
	protected final ValueDefinition<Double> b;
	protected final @Nullable ValueDefinition<Double> a;

	public RGBClientDefinition(SectionDefinition def) {
		r = def.getValue("R", Double.class);
		g = def.getValue("G", Double.class);
		b = def.getValue("B", Double.class);
		a = def.getValue("A", Double.class);

		//need at least R, G, and B; A is optional
		Preconditions.checkNotNull(r, "need an 'R' value in this section");
		Preconditions.checkNotNull(g, "need an 'G' value in this section");
		Preconditions.checkNotNull(b, "need an 'B' value in this section");
	}

	@Override
	public String getSubtitle(ChangeSet changes, SectionDefinition def) {
		double r = changes.get(this.r);
		double g = changes.get(this.g);
		double b = changes.get(this.b);

		if(this.a == null)
			return String.format("[%.1f, %.1f, %.1f]", r, g, b);

		double a = changes.get(this.a);
		return String.format("[%.1f, %.1f, %.1f, %.1f]", r, g, b, a);
	}

	@Override
	public void addWidgets(ZetaClient zc, Screen parent, ChangeSet changes, SectionDefinition def, Consumer<AbstractWidget> widgets) {
		Screen newScreen = new RGBInputScreen(zc, parent, changes, def);
		widgets.accept(new PencilButton(zc, 230, 3, b1 -> Minecraft.getInstance().setScreen(newScreen)));
	}

	class RGBInputScreen extends AbstractSectionInputScreen {
		protected ForgeSlider rslide;
		protected ForgeSlider gslide;
		protected ForgeSlider bslide;
		protected @Nullable ForgeSlider aslide;

		public RGBInputScreen(ZetaClient zc, Screen parent, ChangeSet changes, SectionDefinition def) {
			super(zc, parent, changes, def);
		}

		@Override
		protected void init() {
			super.init();

			int w = 100;
			int p = 12;
			int x = width / 2 - 110;
			int y = 55;

			rslide = addRenderableWidget(makeSliderPlease(x, y     , w - p, 20, r, "R =", 0xFF0000));
			gslide = addRenderableWidget(makeSliderPlease(x, y + 25, w - p, 20, g, "G = ", 0x00FF00));
			bslide = addRenderableWidget(makeSliderPlease(x, y + 50, w - p, 20, b, "B = ", 0x0077FF));
			if(a != null)
				aslide = addRenderableWidget(makeSliderPlease(x, y + 75, w - p, 20, a, "A = ", 0xFFFFFF));

			forceUpdateWidgets();
		}

		@Override
		protected void forceUpdateWidgets() {
			rslide.setValue(changes.get(r));
			gslide.setValue(changes.get(g));
			bslide.setValue(changes.get(b));

			if(aslide != null)
				aslide.setValue(changes.get(a));
		}

		private static final Component EMPTY = Component.empty();

		private ForgeSlider makeSliderPlease(int x, int y, int width, int height, ValueDefinition<Double> binding, String label, int labelColor) {
			//TODO: AbstractSliderButton instead
			return new ForgeSlider(x, y + 50, width, height, EMPTY, EMPTY, 0f, 1f, 0, 0, 1, false) {
				@Override
				protected void applyValue() {
					setValue(snap(this));
					changes.set(binding, getValue());
				}

				@Override
				public void render(@Nonnull PoseStack mstack, int mouseX, int mouseY, float partialTicks) {
					super.render(mstack, mouseX, mouseY, partialTicks);

					//draw the current value
					String displayVal = String.format("%.2f", getValue());
					int valueColor = changes.isDirty(binding) ? ChatFormatting.GOLD.getColor() : 0xFFFFFF;
					font.drawShadow(mstack, displayVal, x + (float) (getWidth() / 2 - font.width(displayVal) / 2), y + 6, valueColor);

					//draw a label
					font.drawShadow(mstack, label, x - 20, y + 5, labelColor);
				}
			};
		}

		@Override
		public void tick() {
			updateButtonStatus(true); //color inputs are always valid
		}

		@Override
		public void render(@Nonnull PoseStack mstack, int mouseX, int mouseY, float partialTicks) {
			renderBackground(mstack);

			super.render(mstack, mouseX, mouseY, partialTicks);

			int titleLeft = width / 2;
			drawCenteredString(mstack, font, Component.literal(ext.getGuiDisplayName(changes, def)).withStyle(ChatFormatting.BOLD), titleLeft, 20, 0xFFFFFF);
			//drawCenteredString(mstack, font, Component.literal(element.getGuiDisplayName()), titleLeft, 30, 0xFFFFFF); //TODO

			//TODO: text

			int cx = width / 2 + 20;
			int cy = 55;
			int size = 95;
			int color = RGBAColorConfig.forColor(
				rslide.getValue(),
				gslide.getValue(),
				bslide.getValue(),
				aslide == null ? 1 : aslide.getValue()
			).getColor();

			//checkerboard
			fill(mstack, cx - 1, cy - 1, cx + size + 1, cy + size + 1, 0xFF000000);
			fill(mstack, cx, cy, cx + size, cy + size, 0xFF999999);
			fill(mstack, cx, cy, cx + size / 2, cy + size / 2, 0xFF666666);
			fill(mstack, cx + size / 2, cy + size / 2, cx + size, cy + size, 0xFF666666);

			//color
			fill(mstack, cx, cy, cx + size, cy + size, color);
		}

		private double snap(ForgeSlider s) {
			double val = s.getValue();
			val = snap(val, 0.0, s);
			val = snap(val, 0.25, s);
			val = snap(val, 0.5, s);
			val = snap(val, 0.75, s);
			val = snap(val, 1.0, s);
			return val;
		}

		private double snap(double val, double target, ForgeSlider s) {
			if(Math.abs(val - target) < 0.02) {
				s.setValue(target);
				return target;
			}
			return val;
		}
	}
}
