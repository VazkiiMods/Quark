package org.violetmoon.quark.addons.oddities.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL13;
import org.violetmoon.quark.addons.oddities.module.MagnetsModule;
import org.violetmoon.quark.base.Quark;

import java.util.function.Supplier;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.PARTICLE;

// Im putting here some stuff idk where else to put
@EventBusSubscriber(modid = "quark", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class MagnetParticleRenderType {

    //I think this just makes it so stuff with low alpha isn't cutoff
    private static Supplier<ShaderInstance> PARTICLE_SHADER = GameRenderer::getParticleShader;
    public static final ParticleRenderType ADDITIVE_TRANSLUCENCY = new ParticleRenderType() {

        @Override
        public @NotNull BufferBuilder begin(Tesselator tesselator, TextureManager manager) {
            Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
            RenderSystem.activeTexture(GL13.GL_TEXTURE2);
            RenderSystem.activeTexture(GL13.GL_TEXTURE0);

            RenderSystem.depthMask(false);
            RenderSystem.setShader(PARTICLE_SHADER);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            return tesselator.begin(VertexFormat.Mode.QUADS, PARTICLE);
        }

        @Override
        public String toString() {
            return "PARTICLE_SHEET_ADDITIVE_TRANSLUCENT";
        }
    };

    // On fabric don't add this if its too much. just leave on forge. Shader just makes particles a tiny bit nicer as it doesnt cut off alpha
    @SubscribeEvent
    public static void registerShader(RegisterShadersEvent event) {
        try {
            ShaderInstance translucentParticleShader = new ShaderInstance(event.getResourceProvider(),
                    Quark.asResource("particle_no_alpha_cutoff"), DefaultVertexFormat.POSITION_TEX);

            event.registerShader(translucentParticleShader, s -> PARTICLE_SHADER = () -> s);

        } catch (Exception e) {
            Quark.LOG.error("Failed to parse shader: " + e);
        }
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MagnetsModule.attractorParticle, MagnetParticle.Provider::new);
        event.registerSpriteSet(MagnetsModule.repulsorParticle, MagnetParticle.Provider::new);
    }
}
