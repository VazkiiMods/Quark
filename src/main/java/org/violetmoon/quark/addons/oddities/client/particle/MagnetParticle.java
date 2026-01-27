package org.violetmoon.quark.addons.oddities.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.violetmoon.quark.addons.oddities.block.MagnetBlock;

import java.util.List;

public class MagnetParticle extends TextureSheetParticle {

    private static final double MAXIMUM_COLLISION_VELOCITY_SQUARED = Mth.square(100.0D);

    private float xWobble = 0;
    private float xWobbleO = 0;
    private float yWobble = 0;
    private float yWobbleO = 0;
    private float alphaO = 0;


    public MagnetParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.xd = pXSpeed;
        this.yd = pYSpeed;
        this.zd = pZSpeed;
        this.lifetime = 33;
        this.friction = 1;
        this.setSize(0.01f, 0.01f);
        this.alpha = 0;
        this.updateAlpha();
    }

    private void updateAlpha() {
        this.alphaO = this.alpha;
        int offset = 1;
        //alpha with fade in and fade out. No lepr. Other particles never lerp colors for some reason...
        float t = (this.age + offset) / (float) (this.lifetime + 1 + offset);
        this.setAlpha(0.6f * (1 - Mth.square(2 * t - 1)));
    }

    @Override
    public float getQuadSize(float partialTicks) {
        float t = (this.age + partialTicks) / (float) (this.lifetime + 1);
        return this.quadSize * (0.6f + (1 - Mth.square(2 * t - 1)) * 0.4f);
    }

    //same as render function just witn jitter
    @Override
    public void render(VertexConsumer buffer, Camera renderInfo, float partialTicks) {
        super.render(buffer, renderInfo, partialTicks);
    }

    @Override
    public int getLightColor(float pPartialTick) {
        int i = super.getLightColor(pPartialTick);
        int k = i >> 16 & 255;
        return 240 | k << 16;
    }

    @Override
    public void tick() {
        super.tick();
        updateAlpha();

        float wobbleAmount = 0.12f;
        this.xWobbleO = this.xWobble;
        this.yWobbleO = this.yWobble;
        this.xWobble = random.nextFloat() * wobbleAmount;
        this.yWobble = random.nextFloat() * wobbleAmount;
    }


    //Just so we can delete when we touch any block
    @Override
    public void move(double pX, double pY, double pZ) {
        if (this.hasPhysics && (pX != 0.0D || pY != 0.0D || pZ != 0.0D) && pX * pX + pY * pY + pZ * pZ < MAXIMUM_COLLISION_VELOCITY_SQUARED) {
            Vec3 moveDir = new Vec3(pX, pY, pZ);
            Vec3 vec3 = Entity.collideBoundingBox(null, moveDir, this.getBoundingBox(), this.level, List.of());
            if (moveDir.distanceToSqr(vec3) > 0.000000001 &&
                    !(level.getBlockState(BlockPos.containing(x, y, z)).getBlock() instanceof MagnetBlock)) {
                //discard when collide with any block but a magnet
                this.remove();
                return;
            }
        }

        if (pX != 0.0D || pY != 0.0D || pZ != 0.0D) {
            this.setBoundingBox(this.getBoundingBox().move(pX, pY, pZ));
            this.setLocationFromBoundingbox();
        }


        this.onGround = pY != pY && pY < 0.0D;
        if (pX != pX) {
            this.xd = 0.0D;
        }

        if (pZ != pZ) {
            this.zd = 0.0D;
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return MagnetParticleRenderType.ADDITIVE_TRANSLUCENCY;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel clientLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            MagnetParticle particle = new MagnetParticle(clientLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            particle.pickSprite(sprite);
            return particle;
        }
    }
}
