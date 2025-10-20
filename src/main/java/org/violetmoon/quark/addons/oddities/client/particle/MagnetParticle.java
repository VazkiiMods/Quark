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
import org.joml.Quaternionf;
import org.joml.Vector3f;
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
    public void render(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        Vec3 vec3 = pRenderInfo.getPosition();
        float x = (float) (Mth.lerp(pPartialTicks, this.xo, this.x) - vec3.x());
        float y = (float) (Mth.lerp(pPartialTicks, this.yo, this.y) - vec3.y());
        float z = (float) (Mth.lerp(pPartialTicks, this.zo, this.z) - vec3.z());
        Quaternionf quaternionf;
        if (this.roll == 0.0F) {
            quaternionf = pRenderInfo.rotation();
        } else {
            quaternionf = new Quaternionf(pRenderInfo.rotation());
            quaternionf.rotateZ(Mth.lerp(pPartialTicks, this.oRoll, this.roll));
        }

        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float size = this.getQuadSize(pPartialTicks);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            float xWob = Mth.lerp(pPartialTicks, this.xWobbleO, this.xWobble);
            float yWob = Mth.lerp(pPartialTicks, this.yWobbleO, this.yWobble);
            vector3f.add(xWob, yWob, 0);
            //vector3f.add(3 * Mth.sin((this.age + pPartialTicks) / 14f), 3 * Mth.cos((this.age + pPartialTicks) / 14f), 0);
            vector3f.rotate(quaternionf);
            vector3f.mul(size);
            vector3f.add(x, y, z);
        }

        float f6 = this.getU0();
        float f7 = this.getU1();
        float f4 = this.getV0();
        float f5 = this.getV1();
        float al = Mth.lerp(pPartialTicks, this.alphaO, this.alpha);
        int j = this.getLightColor(pPartialTicks);
        pBuffer.addVertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).setUv(f7, f5).setColor(this.rCol, this.gCol, this.bCol, al).setLight(j);
        pBuffer.addVertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).setUv(f7, f4).setColor(this.rCol, this.gCol, this.bCol, al).setLight(j);
        pBuffer.addVertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).setUv(f6, f4).setColor(this.rCol, this.gCol, this.bCol, al).setLight(j);
        pBuffer.addVertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).setUv(f6, f5).setColor(this.rCol, this.gCol, this.bCol, al).setLight(j);
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
        public Particle createParticle(SimpleParticleType pType, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            MagnetParticle particle = new MagnetParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
