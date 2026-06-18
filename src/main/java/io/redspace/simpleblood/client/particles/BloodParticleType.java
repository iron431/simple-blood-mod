package io.redspace.simpleblood.client.particles;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public class BloodParticleType extends ParticleType<BloodParticleOptions> {
    public BloodParticleType(boolean overrideLimiter) {
        super(overrideLimiter, BloodParticleOptions.DESERIALIZER);
    }

    @Override
    public Codec<BloodParticleOptions> codec() {
        return BloodParticleOptions.CODEC;
    }
}
