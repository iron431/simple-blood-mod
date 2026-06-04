package io.redspace.simpleblood.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class BloodEmitterParticle {
    private static final int SPAT_FRAMES = 4;
    private static final int SPURT_FRAMES = 3;
    private static final int SWIPE_1_FRAMES = 7;
    private static final int SWIPE_2_FRAMES = 8;
    private static final int VARIANT_COUNT = 6;

    private BloodEmitterParticle() {
    }

    @OnlyIn(Dist.CLIENT)
    public static final class Sprites {
        public static SpriteSet blood;
        public static SpriteSet spat1;
        public static SpriteSet spat2;
        public static SpriteSet spurt2;
        public static SpriteSet swipe1;
        public static SpriteSet swipe2;

        private Sprites() {
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
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
            return switch (level.random.nextInt(VARIANT_COUNT)) {
                case 0 -> new BloodParticle(level, x, y, z, Sprites.blood, dx, dy, dz);
                case 1 -> new SpriteSheetBloodParticle(level, x, y, z, Sprites.spat1, SPAT_FRAMES, dx, dy, dz);
                case 2 -> new SpriteSheetBloodParticle(level, x, y, z, Sprites.spat2, SPAT_FRAMES, dx, dy, dz);
                case 3 -> new SpriteSheetBloodParticle(level, x, y, z, Sprites.spurt2, SPURT_FRAMES, dx, dy, dz);
                case 4 -> new SpriteSheetBloodParticle(level, x, y, z, Sprites.swipe1, SWIPE_1_FRAMES, dx, dy, dz);
                default -> new SpriteSheetBloodParticle(level, x, y, z, Sprites.swipe2, SWIPE_2_FRAMES, dx, dy, dz);
            };
        }
    }
}
