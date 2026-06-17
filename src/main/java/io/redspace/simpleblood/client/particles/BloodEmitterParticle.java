package io.redspace.simpleblood.client.particles;

import io.redspace.simpleblood.client.ClientConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class BloodEmitterParticle {
    @FunctionalInterface
    public interface VariantFactory {
        Particle create(BloodParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz);
    }

    private BloodEmitterParticle() {
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<BloodParticleOptions> {
        private final List<VariantFactory> variants;

        public Provider(List<VariantFactory> variants) {
            this.variants = variants;
        }

        @Override
        public Particle createParticle(
                @NotNull BloodParticleOptions options,
                ClientLevel level,
                double x,
                double y,
                double z,
                double dx,
                double dy,
                double dz
        ) {
            if (options.isGraphic() && !ClientConfig.ALLOW_GRAPHIC_PARTICLES.get()) {
                return null;
            }
            return variants.get(level.random.nextInt(variants.size()))
                    .create(options, level, x, y, z, dx, dy, dz);
        }
    }
}
