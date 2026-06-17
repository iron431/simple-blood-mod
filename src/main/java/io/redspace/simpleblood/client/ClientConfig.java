package io.redspace.simpleblood.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue GROUND_DECAL_DURATION_MULTIPLIER = BUILDER
            .comment("Multiplier for how long blood ground decals remain visible.")
            .defineInRange("groundDecalDurationMultiplier", 1.0, 0.1, 10.0);

    public static final ModConfigSpec.BooleanValue ALLOW_GRAPHIC_PARTICLES = BUILDER
            .comment("Whether blood particles marked as graphics are allowed to spawn.")
            .define("allowGraphicParticles", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

}
