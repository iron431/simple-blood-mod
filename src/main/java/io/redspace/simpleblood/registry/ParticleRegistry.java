package io.redspace.simpleblood.registry;

import io.redspace.simpleblood.IronsSimpleBloodMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, IronsSimpleBloodMod.MODID);

    public static void register(IEventBus eventBus) {
        PARTICLE_TYPES.register(eventBus);
    }

    public static final Vector3f BLOOD_COLOR = new Vector3f(0.5f, 0f, 0.05f);
    public static final Supplier<SimpleParticleType> BLOOD_PARTICLE = PARTICLE_TYPES.register("blood", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> BLOOD_GROUND_PARTICLE = PARTICLE_TYPES.register("blood_ground", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> BLOOD_SPAT_1 = PARTICLE_TYPES.register("blood_spat_1", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> BLOOD_SPAT_2 = PARTICLE_TYPES.register("blood_spat_2", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> BLOOD_SPURT_2 = PARTICLE_TYPES.register("blood_spurt_2", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> BLOOD_SWIPE_1 = PARTICLE_TYPES.register("blood_swipe_1", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> BLOOD_SWIPE_2 = PARTICLE_TYPES.register("blood_swipe_2", () -> new SimpleParticleType(false));
    public static final Supplier<SimpleParticleType> BLOOD_EMITTER = PARTICLE_TYPES.register("blood_emitter", () -> new SimpleParticleType(false));
}
