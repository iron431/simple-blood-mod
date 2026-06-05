package io.redspace.simpleblood.client;

import io.redspace.simpleblood.client.particles.BloodEmitterParticle;
import io.redspace.simpleblood.client.particles.BloodGroundParticle;
import io.redspace.simpleblood.client.particles.BloodParticle;
import io.redspace.simpleblood.client.particles.SpriteSheetBloodParticle;
import io.redspace.simpleblood.registry.ParticleRegistry;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

public class ClientEvents {
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        BloodEmitterParticle.clearVariantProviders();

        event.registerSpriteSet(ParticleRegistry.BLOOD_PARTICLE.get(), sprites ->
                BloodEmitterParticle.registerVariantProvider(new BloodParticle.Provider(sprites)));
        event.registerSpriteSet(ParticleRegistry.BLOOD_GROUND_PARTICLE.get(), BloodGroundParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPAT_1.get(), sprites ->
                BloodEmitterParticle.registerVariantProvider(new SpriteSheetBloodParticle.Provider(sprites, 4, 32)));
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPAT_2.get(), sprites ->
                BloodEmitterParticle.registerVariantProvider(new SpriteSheetBloodParticle.Provider(sprites, 4, 32)));
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPURT_2.get(), sprites ->
                BloodEmitterParticle.registerVariantProvider(new SpriteSheetBloodParticle.Provider(sprites, 3, 32)));
        event.registerSpriteSet(ParticleRegistry.BLOOD_SWIPE_1.get(), sprites ->
                BloodEmitterParticle.registerVariantProvider(new SpriteSheetBloodParticle.Provider(sprites, 7, 32)));
        event.registerSpriteSet(ParticleRegistry.BLOOD_SWIPE_2.get(), sprites ->
                BloodEmitterParticle.registerVariantProvider(new SpriteSheetBloodParticle.Provider(sprites, 8, 32)));
        event.registerSpriteSet(ParticleRegistry.BLOOD_EMITTER.get(), sprites -> new BloodEmitterParticle.Provider());
    }
}
