package io.redspace.simpleblood.client;

import io.redspace.simpleblood.IronsSimpleBloodMod;
import io.redspace.simpleblood.client.particles.BloodEmitterParticle;
import io.redspace.simpleblood.client.particles.BloodGroundParticle;
import io.redspace.simpleblood.client.particles.BloodParticle;
import io.redspace.simpleblood.client.particles.SpriteSheetBloodParticle;
import io.redspace.simpleblood.decal_behavior.DecalDirection;
import io.redspace.simpleblood.decal_behavior.DecalType;
import io.redspace.simpleblood.registry.ParticleRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = IronsSimpleBloodMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {
    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        List<BloodEmitterParticle.VariantFactory> variants = new ArrayList<>();

        event.registerSpriteSet(ParticleRegistry.BLOOD_GROUND_PARTICLE.get(), BloodGroundParticle.Provider::new);

        event.registerSpriteSet(ParticleRegistry.BLOOD_PARTICLE.get(), sprites -> {
            var provider = new BloodParticle.Provider(sprites, DecalType.GENERIC, DecalDirection.OMNIDIRECTIONAL);
            variants.add(provider);
            return provider;
        });
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPAT_1.get(), sprites -> {
            var provider = new SpriteSheetBloodParticle.Provider(sprites, 4, 32, DecalType.PIERCING, DecalDirection.OMNIDIRECTIONAL);
            variants.add(provider);
            return provider;
        });
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPAT_2.get(), sprites -> {
            var provider = new SpriteSheetBloodParticle.Provider(sprites, 4, 32, DecalType.PIERCING, DecalDirection.OMNIDIRECTIONAL);
            variants.add(provider);
            return provider;
        });
        event.registerSpriteSet(ParticleRegistry.BLOOD_SPURT_2.get(), sprites -> {
            var provider = new SpriteSheetBloodParticle.Provider(sprites, 3, 32, DecalType.GENERIC, DecalDirection.RIGHT);
            variants.add(provider);
            return provider;
        });
        event.registerSpriteSet(ParticleRegistry.BLOOD_SWIPE_1.get(), sprites -> {
            var provider = new SpriteSheetBloodParticle.Provider(sprites, 7, 32, DecalType.SLASHING, DecalDirection.RIGHT);
            variants.add(provider);
            return provider;
        });
        event.registerSpriteSet(ParticleRegistry.BLOOD_SWIPE_2.get(), sprites -> {
            var provider = new SpriteSheetBloodParticle.Provider(sprites, 8, 32, DecalType.SLASHING, DecalDirection.RIGHT);
            variants.add(provider);
            return provider;
        });

        event.registerSpriteSet(ParticleRegistry.BLOOD_EMITTER.get(), sprites -> new BloodEmitterParticle.Provider(variants));
    }
}
