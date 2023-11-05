package vazkii.quark.content.experimental.client.screen;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.opengl.GL11;
import vazkii.quark.content.experimental.module.VariantSelectorModule;

import java.util.ArrayList;
import java.util.List;

public class VariantSelectorScreen extends Screen {

	float timeIn = 0;
	int slotSelected = -1;

	final Minecraft mc;
	final ItemStack stack;
	final KeyMapping key;
	final String currentVariant;
	final List<String> variants;

	final List<DrawStack> drawStacks = new ArrayList<>();

	public VariantSelectorScreen(ItemStack stack, KeyMapping key, String currentVariant, List<String> variants) {
		super(Component.empty());
		mc = Minecraft.getInstance();
		this.stack = stack;
		this.key = key;
		this.currentVariant = currentVariant;
		this.variants = variants;
	}

	@Override
	public void render(PoseStack ms, int mx, int my, float delta) {
		super.render(ms, mx, my, delta);

		timeIn += delta;

		int x = width / 2;
		int y = height / 2;
		int maxRadius = 50;

		int segments = variants.size() + 1;
		float degPer = (float) Math.PI * 2 / segments;

		// ensure the boring one is always at the bottom
		float pad = - ((float) Math.PI / segments) + ((float) Math.PI / 2);
		double angle = mouseAngle(x, y, mx, my);
		double dist = (x - mx) * (x - mx) + (y - my) * (y - my); 

		// loop angle around to ensure the last bit is accessible
		if(angle < pad)
			angle = Math.PI * 2 + pad;

		slotSelected = -1;

		Block block = Blocks.COBBLESTONE;
		if(stack.getItem() instanceof BlockItem bi)
			block = bi.getBlock();

		Tesselator tess = Tesselator.getInstance();
		BufferBuilder buf = tess.getBuilder();

		RenderSystem.disableCull();
		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		drawStacks.clear();
		buf.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

		for(int seg = 0; seg < segments; seg++) {
			String variant = seg == 0 ? "" : variants.get(seg - 1);
			boolean rightVariant = variant.equals(currentVariant);

			Block variantBlock = VariantSelectorModule.getVariantForBlock(block, variant);
			boolean variantExists = seg == 0 || (variantBlock != block);

			float start = seg * degPer + pad;
			float end = (seg + 1) * degPer + pad;

			boolean mouseInSector = variantExists && start < angle && angle < end && dist > 64;
			float radius = Math.max(0F, Math.min((timeIn - ((float) seg * 6F / (float) segments)) * 40F, (float) maxRadius));

			if(mouseInSector || rightVariant)
				radius *= 1.1f;

			int gs = 0x39;
			if(seg % 2 == 0)
				gs += 0x29;

			int r = gs;
			int g = gs ;
			int b = gs;
			int a = 0x33;

			if(variantExists) {
				g += 0x22;
				a = 0x99;
			}

			if(seg == 0)
				buf.vertex(x, y, 0).color(r, g, b, a).endVertex();

			if(mouseInSector) {
				slotSelected = seg;
				r = 0x00;
				g = b = 0xAA;
			} 
			else if(rightVariant) {
				r = b = 0x00;
				g = 0xAA;
			}

			float sxp = x + Mth.cos(start) * radius;
			float syp = y + Mth.sin(start) * radius;
			float exp = x + Mth.cos(end) * radius;
			float eyp = y + Mth.sin(end) * radius;

			buf.vertex(sxp, syp, 0).color(r, g, b, a).endVertex();
			buf.vertex(exp, eyp, 0).color(r, g, b, a).endVertex();

			float center = (seg + 0.5f) * degPer + pad;
			float cxp = x + Mth.cos(center) * radius;
			float cyp = y + Mth.sin(center) * radius;

			ItemStack variantStack = variantExists ? new ItemStack(variantBlock) : ItemStack.EMPTY;
			double mod = 0.6;
			int xdp = (int) ((cxp - x) * mod + x);
			int ydp = (int) ((cyp - y) * mod + y);
			drawStacks.add(new DrawStack(variantStack, xdp - 8, ydp - 8));
		}
		tess.end();

		RenderSystem.enableTexture();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		
		for(DrawStack ds : drawStacks) {
			if(ds.stack().isEmpty())
				mc.font.draw(ms, "?", ds.x() + 6, ds.y() + 3, 0x99FFFFFF);
			else 
				mc.getItemRenderer().renderGuiItem(ds.stack(), ds.x(), ds.y());
		}
		RenderSystem.disableBlend();
	}

	@Override
	public void tick() {
		super.tick();
		if (!isKeyDown(key)) {
			mc.setScreen(null);
			
			if(slotSelected == -1 && timeIn < 10)
				slotSelected = 0;
			
			if(slotSelected != -1) {
				String variant = slotSelected == 0 ? "" : variants.get(slotSelected - 1);
				VariantSelectorModule.Client.setClientVariant(variant, true);
				mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
			}
		}

		ImmutableSet<KeyMapping> set = ImmutableSet.of(mc.options.keyUp, mc.options.keyLeft, mc.options.keyDown, mc.options.keyRight, mc.options.keyShift, mc.options.keySprint, mc.options.keyJump);
		for (KeyMapping k : set) {
			KeyMapping.set(k.getKey(), isKeyDown(k));
		}
	}

	public boolean isKeyDown(KeyMapping keybind) {
		InputConstants.Key key = keybind.getKey();
		if (key.getType() == InputConstants.Type.MOUSE) {
			return keybind.isDown();
		}
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key.getValue());
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private static double mouseAngle(int x, int y, int mx, int my) {
		return (Mth.atan2(my - y, mx - x) + Math.PI * 2) % (Math.PI * 2);
	}

	private record DrawStack(ItemStack stack, int x, int y) {};

}
