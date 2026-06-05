package io.redspace.simpleblood.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class BloodEmitterParticle {
    private static final List<ParticleProvider<SimpleParticleType>> VARIANT_PROVIDERS = new ArrayList<>();

    private BloodEmitterParticle() {
    }

    public static void clearVariantProviders() {
        VARIANT_PROVIDERS.clear();
    }

    public static <T extends ParticleProvider<SimpleParticleType>> T registerVariantProvider(T provider) {
        VARIANT_PROVIDERS.add(provider);
        return provider;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(
                @NotNull SimpleParticleType particleType,
                ClientLevel level,
                double x,
                double y,
                double z,
                double dx,
                double dy,
                double dz
        ) {
            return VARIANT_PROVIDERS.get(level.random.nextInt(VARIANT_PROVIDERS.size()))
                    .createParticle(particleType, level, x, y, z, dx, dy, dz);
        }
    }
}
