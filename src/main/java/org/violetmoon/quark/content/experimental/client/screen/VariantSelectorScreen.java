package org.violetmoon.quark.content.experimental.client.screen;

import com.google.common.collect.ImmutableSet;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import org.violetmoon.quark.content.experimental.module.VariantSelectorModule;

import java.util.ArrayList;
import java.util.List;

public class VariantSelectorScreen extends Screen {

    private float timeIn = 0;
    private int slotSelected = -1;

    private final Minecraft mc;
    private final KeyMapping key;
    private final String currentVariant;
    private final List<Pair<String, @Nullable Block>> variants;

    private final List<DrawStack> drawStacks = new ArrayList<>();

    public VariantSelectorScreen(Block originalBlock, KeyMapping key, String currentVariant, List<String> visibleVariants) {
        super(Component.empty());
        this.mc = Minecraft.getInstance();
        this.key = key;
        this.currentVariant = currentVariant;

        this.variants = new ArrayList<>();
        this.variants.add(Pair.of("", originalBlock));
        for(String v : visibleVariants){
            Block variantBlock = VariantSelectorModule.getVariantBlockFromOriginal(originalBlock, v);
            variants.add(Pair.of(v, variantBlock));
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mx, int my, float delta) {
        super.render(guiGraphics, mx, my, delta);

        timeIn += delta;

        int x = width / 2;
        int y = height / 2;
        int maxRadius = 50;

        int segments = variants.size()  ;
        float degPer = (float) Math.PI * 2 / segments;

        // ensure the boring one is always at the bottom
        float pad = -((float) Math.PI / segments) + ((float) Math.PI / 2);
        double angle = mouseAngle(x, y, mx, my);
        double dist = (x - mx) * (x - mx) + (y - my) * (y - my);
        int activeSegment = (dist > 64)
            // converting to int floors towards 0 so negative values would be problematic, just add PI*2
            ? (int) (((float) Math.PI * 2 + angle - pad) / degPer) % segments
            : -1; // too close to activate

        // loop angle around to ensure the last bit is accessible
        if (angle < pad)
            angle = Math.PI * 2 + pad;

        slotSelected = -1;

        Tesselator tess = Tesselator.getInstance();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder buf = tess.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        drawStacks.clear();

        for (int seg = 0; seg < segments; seg++) {
            //we need 1 extra
            var pair = variants.get(seg);
            String variant = pair.getFirst();
            Block variantBlock = pair.getSecond();
            boolean variantExists = variantBlock != null;

            boolean rightVariant = variant.equals(currentVariant);

            float start = seg * degPer + pad;
            float end = (seg + 1) * degPer + pad;

            boolean mouseInSector = variantExists && activeSegment == seg;
            float radius = Math.max(0F, Math.min((timeIn - ((float) seg * 6F / (float) segments)) * 40F, (float) maxRadius));

            if (mouseInSector || rightVariant)
                radius *= 1.1f;

            if (!variantExists)
                radius *= 0.9f;

            int gs = 0x39;
            if (seg % 2 == 0)
                gs += 0x29;

            int r = gs;
            int g = gs;
            int b = gs;
            int a = 0x44;

            if (variantExists) {
                g += 0x22;
                a = 0x99;
            } else {
                r /= 4;
                g /= 4;
                b /= 4;
            }

            if (seg == 0)
                buf.addVertex(x, y, 0).setColor(r, g, b, a);

            if (mouseInSector) {
                slotSelected = seg;
                r = 0x00;
                g = b = 0xAA;
            } else if (rightVariant) {
                r = b = 0x00;
                g = 0xAA;
            }

            float sxp = x + Mth.cos(start) * radius;
            float syp = y + Mth.sin(start) * radius;
            float exp = x + Mth.cos(end) * radius;
            float eyp = y + Mth.sin(end) * radius;

            buf.addVertex(sxp, syp, 0).setColor(r, g, b, a);
            buf.addVertex(exp, eyp, 0).setColor(r, g, b, a);

            float center = (seg + 0.5f) * degPer + pad;
            float cxp = x + Mth.cos(center) * radius;
            float cyp = y + Mth.sin(center) * radius;

            ItemStack variantStack = variantExists ? new ItemStack(variantBlock) : ItemStack.EMPTY;
            double mod = 0.6;
            int xdp = (int) ((cxp - x) * mod + x);
            int ydp = (int) ((cyp - y) * mod + y);
            drawStacks.add(new DrawStack(variantStack, xdp - 8, ydp - 8));
        }

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        BufferUploader.drawWithShader(buf.buildOrThrow());

        for (DrawStack ds : drawStacks) {
            if (!ds.stack().isEmpty())
                guiGraphics.renderItem(ds.stack(), ds.x(), ds.y());
        }

        RenderSystem.disableBlend();
    }

    @Override
    public void tick() {
        super.tick();
        if (!isKeyDown(key)) {
            mc.setScreen(null);

            if (slotSelected == -1 && timeIn < 10)
                slotSelected = 0;

            if (slotSelected != -1) {
                String variant = variants.get(slotSelected).getFirst();
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

    private record DrawStack(ItemStack stack, int x, int y) {
    }

    ;

}
