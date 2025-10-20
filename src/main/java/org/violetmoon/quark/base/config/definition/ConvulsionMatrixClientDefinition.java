package org.violetmoon.quark.base.config.definition;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import org.jetbrains.annotations.NotNull;
import org.violetmoon.quark.base.config.type.ConvulsionMatrixConfig;
import org.violetmoon.zeta.client.ZetaClient;
import org.violetmoon.zeta.client.config.definition.ClientDefinitionExt;
import org.violetmoon.zeta.client.config.screen.AbstractSectionInputScreen;
import org.violetmoon.zeta.client.config.widget.PencilButton;
import org.violetmoon.zeta.config.ChangeSet;
import org.violetmoon.zeta.config.SectionDefinition;
import org.violetmoon.zeta.config.ValueDefinition;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConvulsionMatrixClientDefinition implements ClientDefinitionExt<SectionDefinition>{
	public ConvulsionMatrixClientDefinition(ConvulsionMatrixConfig cfg, SectionDefinition def) {
		this.params = cfg.params;

		r = def.getValueErased("R", List.class);
		g = def.getValueErased("G", List.class);
		b = def.getValueErased("B", List.class);

		Preconditions.checkNotNull(r, "need an 'R' value in this section");
		Preconditions.checkNotNull(g, "need an 'G' value in this section");
		Preconditions.checkNotNull(b, "need an 'B' value in this section");
	}

	private final ConvulsionMatrixConfig.Params params;

	private final ValueDefinition<List<Double>> r;
	private final ValueDefinition<List<Double>> g;
	private final ValueDefinition<List<Double>> b;

	@Override
	public String getSubtitle(ChangeSet changes, SectionDefinition def) {
		List<Double> r_ = changes.getExactSizeCopy(r, 3, 0d);
		List<Double> g_ = changes.getExactSizeCopy(g, 3, 0d);
		List<Double> b_ = changes.getExactSizeCopy(b, 3, 0d);

		return Stream.of(r_, g_, b_)
			.flatMap(List::stream)
			.map(d -> String.format(Locale.ROOT, "%.1f", d))
			.collect(Collectors.joining(", ", "[", "]"));
	}

	@Override
	public void addWidgets(ZetaClient zc, Screen parent, ChangeSet changes, SectionDefinition def, Consumer<AbstractWidget> widgets) {
		Screen newScreen = new ConvulsionMatrixInputScreen(zc, parent, changes, def);
		widgets.accept(new PencilButton(zc, 230, 3, b1 -> Minecraft.getInstance().setScreen(newScreen)));
	}

	class ConvulsionMatrixInputScreen extends AbstractSectionInputScreen {
		ExtendedSlider[] sliders = new ExtendedSlider[9];

		public ConvulsionMatrixInputScreen(ZetaClient zc, Screen parent, ChangeSet changes, SectionDefinition def) {
			super(zc, parent, changes, def);
		}

		protected List<Double> with(ValueDefinition<List<Double>> def, int index, double value) {
			List<Double> copy = changes.getExactSizeCopy(def, 3, 0d);
			copy.set(index, value);
			return copy;
		}

		@SuppressWarnings({"PointlessArithmeticExpression", "ConstantValue"}) //table :(
		@Override
		protected void init() {
			super.init();

			int w = 70;
			int p = 12;
			int x = width / 2 - 33;
			int y = 55;

			sliders[0] = addRenderableWidget(makeSliderPlease(x + w * 0, y + 25 * 0, w - p, 20, r, 0));
			sliders[1] = addRenderableWidget(makeSliderPlease(x + w * 1, y + 25 * 0, w - p, 20, r, 1));
			sliders[2] = addRenderableWidget(makeSliderPlease(x + w * 2, y + 25 * 0, w - p, 20, r, 2));
			sliders[3] = addRenderableWidget(makeSliderPlease(x + w * 0, y + 25 * 1, w - p, 20, g, 0));
			sliders[4] = addRenderableWidget(makeSliderPlease(x + w * 1, y + 25 * 1, w - p, 20, g, 1));
			sliders[5] = addRenderableWidget(makeSliderPlease(x + w * 2, y + 25 * 1, w - p, 20, g, 2));
			sliders[6] = addRenderableWidget(makeSliderPlease(x + w * 0, y + 25 * 2, w - p, 20, b, 0));
			sliders[7] = addRenderableWidget(makeSliderPlease(x + w * 1, y + 25 * 2, w - p, 20, b, 1));
			sliders[8] = addRenderableWidget(makeSliderPlease(x + w * 2, y + 25 * 2, w - p, 20, b, 2));

			int i = 0;
			for(Map.Entry<String, double[]> entry : params.presetMap.entrySet()) {
				String name = entry.getKey();
				double[] preset = entry.getValue();
				addRenderableWidget(Button.builder(Component.literal(name), __ -> setFromArray(preset)).size(w - p, 20).pos(x + (w * i), y + 115).build());
				i++;
			}

			forceUpdateWidgets();
		}

		@Override
		protected void forceUpdateWidgets() {
			setFromList(Stream.of(changes.getExactSizeCopy(r, 3, 0d), changes.getExactSizeCopy(g, 3, 0d), changes.getExactSizeCopy(b, 3, 0d))
				.flatMap(List<Double>::stream)
				.toList());
		}

		private static final Component EMPTY = Component.empty();

		private ExtendedSlider makeSliderPlease(int x, int y, int width, int height, ValueDefinition<List<Double>> binding, int bindingIndex) {
			return new ExtendedSlider(x, y, width, height, EMPTY, EMPTY, 0d, 2d, 0d, 0, 1, false) {
				@Override
				public void setValue(double value) {
					super.setValue(value);
					changes.set(binding, with(binding, bindingIndex, value));
				}

				@Override
				protected void applyValue() {
					setValue(snap(this));
				}

				@Override
				public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
					Minecraft mc = Minecraft.getInstance();
					super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);

					String displayVal = String.format(Locale.ROOT, "%.2f", getValue());
					int valueColor = changes.isDirty(binding) ? ChatFormatting.GOLD.getColor() : 0xFFFFFF;
					guiGraphics.drawString(mc.font, displayVal, x + (getWidth() / 2 - font.width(displayVal) / 2), y + 6, valueColor);
				}
			};
		}

		@Override
		public void tick() {
			updateButtonStatus(true); //always valid
		}

		@Override
		public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
			PoseStack mstack = guiGraphics.pose();
			renderBackground(guiGraphics, mouseX, mouseY, partialTicks);

			super.render(guiGraphics, mouseX, mouseY, partialTicks);

			int x = width / 2 - 203;
			int y = 10;
			int size = 60;

			int titleLeft = width / 2 + 66;
			guiGraphics.drawCenteredString(font, Component.literal(params.name).withStyle(ChatFormatting.BOLD), titleLeft, 20, 0xFFFFFF);
			guiGraphics.drawCenteredString(font, Component.literal("Presets"), titleLeft, 155, 0xFFFFFF);

			//copied from the original
			int sliders = 0;
			for (Renderable renderable : renderables) {
				if (renderable instanceof ExtendedSlider slider) {
					switch (sliders) {
						case 0 -> {
							guiGraphics.drawString(font, "R =", slider.getX() - 20, slider.getY() + 5, 0xFF0000);
							guiGraphics.drawString(font, "R", slider.getX() + (slider.getWidth() / 2 - 2), slider.getY() - 12, 0xFF0000);
						}
						case 1 -> guiGraphics.drawString(font, "G", slider.getX() + (slider.getWidth() / 2 - 2), slider.getY() - 12, 0x00FF00);
						case 2 -> guiGraphics.drawString(font, "B", slider.getX() + (slider.getWidth() / 2 - 2), slider.getY() - 12, 0x0077FF);
						case 3 -> guiGraphics.drawString(font, "G =", slider.getX() - 20, slider.getY() + 5, 0x00FF00);
						case 6 -> guiGraphics.drawString(font, "B =", slider.getX() - 20, slider.getY() + 5, 0x0077FF);
					}
					if ((sliders % 3) != 0)
						guiGraphics.drawString(font, "+", slider.getX() - 9, slider.getY() + 5, 0xFFFFFF);

					sliders++;
				}
			}

			//color preview
			String[] biomes = params.biomeNames;
			int[] colors = params.testColors;
			int[] folliageColors = params.folliageTestColors;
			boolean renderFolliage = params.shouldDisplayFolliage();
			double[] matrix = getToDoubleArray();

			for (int i = 0; i < biomes.length; i++) {
				String name = biomes[i];
				int color = colors[i];

				int convolved = ConvulsionMatrixConfig.convolve(matrix, color);

				int folliage, convolvedFolliage = 0;
				if (renderFolliage) {
					folliage = folliageColors[i];
					convolvedFolliage = ConvulsionMatrixConfig.convolve(matrix, folliage);
				}

				int cx = x + (i % 2) * (size + 5);
				int cy = y + (i / 2) * (size + 5);

				guiGraphics.fill(cx - 1, cy - 1, cx + size + 1, cy + size + 1, 0xFF000000);
				guiGraphics.fill(cx, cy, cx + size, cy + size, convolved);
				guiGraphics.fill(cx + size / 2 - 1, cy + size / 2 - 1, cx + size, cy + size, 0x22000000);

				if (renderFolliage)
					guiGraphics.fill(cx + size / 2, cy + size / 2, cx + size, cy + size, convolvedFolliage);

				guiGraphics.drawString(font, name, cx + 2, cy + 2, 0x55000000);

				if (renderFolliage) {
					guiGraphics.renderItem(new ItemStack(Items.OAK_SAPLING), cx + size - 18, cy + size - 16);
					mstack.pushPose();
					mstack.translate(0, 0, 999);
					guiGraphics.fill(cx + size / 2, cy + size / 2, cx + size, cy + size, convolvedFolliage & 0x55FFFFFF);
					mstack.popPose();
				}
			}
		}

		protected void setFromArray(double[] values) {
			for(int i = 0; i < 9; i++) {
				sliders[i].setValue(values[i]);
			}
		}

		protected void setFromList(List<Double> values) {
			for(int i = 0; i < 9; i++) {
				sliders[i].setValue(values.get(i));
			}
		}

		protected double[] getToDoubleArray() {
			double[] values = new double[9];
			for(int i = 0; i < 9; i++) {
				values[i] = sliders[i].getValue();
			}
			return values;
		}

		private double snap(ExtendedSlider slider) {
			double val = slider.getValue();
			val = snap(val, 0.5, slider);
			val = snap(val, 1.0, slider);
			val = snap(val, 1.5, slider);
			return val;
		}

		private double snap(double val, double correct, ExtendedSlider slider) {
			if(Math.abs(val - correct) < 0.02) {
				slider.setValue(correct);
				return correct;
			}
			return val;
		}
	}
}
