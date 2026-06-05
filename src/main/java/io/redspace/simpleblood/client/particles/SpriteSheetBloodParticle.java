package io.redspace.simpleblood.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpriteSheetBloodParticle extends BloodParticle {
    private final int frameCount;
    private static final int TICKS_PER_FRAME = 3;

    public SpriteSheetBloodParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            SpriteSet spriteSet,
            int frameCount,
            int textureSize,
            double xd,
            double yd,
            double zd
    ) {
        super(level, x, y, z, spriteSet, xd, yd, zd);
        this.frameCount = frameCount;
        this.setSprite(spriteSet.get(0, 1));
        this.quadSize *= textureSize / 16f;
    }

    private float animPercent(int offset) {
        return Math.min((Math.min(age / TICKS_PER_FRAME, frameCount - 1) + offset) / ((float) frameCount), 10);
    }

    @Override
    protected float getU0() {
        float u0 = super.getU0();
        float u1 = super.getU1();
        return Mth.lerp(animPercent(0), u0, u1);
    }

    @Override
    protected float getU1() {
        float u0 = super.getU0();
        float u1 = super.getU1();
        return Mth.lerp(animPercent(1), u0, u1);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final int frameCount;
        private final int textureSize;

        public Provider(SpriteSet sprites, int frameCount, int textureSize) {
            this.sprites = sprites;
            this.frameCount = frameCount;
            this.textureSize = textureSize;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType particleType,
                ClientLevel level,
                double x,
                double y,
                double z,
                double dx,
                double dy,
                double dz
        ) {
            return new SpriteSheetBloodParticle(level, x, y, z, this.sprites, this.frameCount, this.textureSize, dx, dy, dz);
        }
    }
}
