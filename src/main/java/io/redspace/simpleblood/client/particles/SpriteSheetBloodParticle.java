package io.redspace.simpleblood.client.particles;

import io.redspace.simpleblood.decal_behavior.DecalDirection;
import io.redspace.simpleblood.decal_behavior.DecalType;
import io.redspace.simpleblood.registry.ParticleRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
            DecalType decalType,
            DecalDirection decalDirection,
            int color,
            float scale,
            double xd,
            double yd,
            double zd
    ) {
        super(level, x, y, z, spriteSet, decalType, decalDirection, color, scale, xd, yd, zd);
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
    public static class Provider implements ParticleProvider<SimpleParticleType>, BloodEmitterParticle.VariantFactory {
        private final SpriteSet sprites;
        private final int frameCount;
        private final int textureSize;
        private final DecalType decalType;
        private final DecalDirection decalDirection;

        public Provider(SpriteSet sprites, int frameCount, int textureSize, DecalType decalType, DecalDirection decalDirection) {
            this.sprites = sprites;
            this.frameCount = frameCount;
            this.textureSize = textureSize;
            this.decalType = decalType;
            this.decalDirection = decalDirection;
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
            return new SpriteSheetBloodParticle(
                    level, x, y, z, this.sprites, this.frameCount, this.textureSize, this.decalType, this.decalDirection,
                    ParticleRegistry.DEFAULT_BLOOD_COLOR, 1f, dx, dy, dz
            );
        }

        @Override
        public Particle create(BloodParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new SpriteSheetBloodParticle(
                    level, x, y, z, this.sprites, this.frameCount, this.textureSize, this.decalType, this.decalDirection,
                    options.color(), options.scale(), dx, dy, dz
            );
        }

    }
}
