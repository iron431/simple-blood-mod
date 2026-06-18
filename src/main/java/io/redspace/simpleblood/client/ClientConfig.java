package io.redspace.simpleblood.client;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ClientConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec.DoubleValue GROUND_DECAL_DURATION_MULTIPLIER = BUILDER
            .comment("Multiplier for how long blood ground decals remain visible.")
            .defineInRange("groundDecalDurationMultiplier", 1.0, 0.1, 10.0);

    public static final ForgeConfigSpec.BooleanValue ALLOW_GRAPHIC_PARTICLES = BUILDER
            .comment("Whether blood particles marked as graphics are allowed to spawn.")
            .define("allowGraphicParticles", true);

    public static final ForgeConfigSpec SPEC = BUILDER.build();

}
