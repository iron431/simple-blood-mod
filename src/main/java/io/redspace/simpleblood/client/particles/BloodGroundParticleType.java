package io.redspace.simpleblood.client.particles;

import com.mojang.serialization.Codec;
import net.minecraft.core.particles.ParticleType;

public class BloodGroundParticleType extends ParticleType<BloodGroundParticleOptions> {
    public BloodGroundParticleType(boolean overrideLimiter) {
        super(overrideLimiter, BloodGroundParticleOptions.DESERIALIZER);
    }

    @Override
    public Codec<BloodGroundParticleOptions> codec() {
        return BloodGroundParticleOptions.CODEC;
    }
}
