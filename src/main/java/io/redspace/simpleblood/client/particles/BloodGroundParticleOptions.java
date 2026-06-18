package io.redspace.simpleblood.client.particles;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.simpleblood.registry.ParticleRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

public record BloodGroundParticleOptions(int color, float scale) implements ParticleOptions {
    public BloodGroundParticleOptions(int color) {
        this(color, 1f);
    }

    public static final Codec<BloodGroundParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.optionalFieldOf("color", ParticleRegistry.DEFAULT_BLOOD_COLOR).forGetter(BloodGroundParticleOptions::color),
                    Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(BloodGroundParticleOptions::scale)
            ).apply(instance, BloodGroundParticleOptions::new)
    );

    public static final ParticleOptions.Deserializer<BloodGroundParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public @NotNull BloodGroundParticleOptions fromCommand(@NotNull ParticleType<BloodGroundParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int color = reader.readInt();
            reader.expect(' ');
            float scale = reader.readFloat();
            return new BloodGroundParticleOptions(color, scale);
        }

        @Override
        public @NotNull BloodGroundParticleOptions fromNetwork(@NotNull ParticleType<BloodGroundParticleOptions> type, @NotNull FriendlyByteBuf buf) {
            return new BloodGroundParticleOptions(buf.readVarInt(), buf.readFloat());
        }
    };

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.BLOOD_GROUND_PARTICLE.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(color);
        buf.writeFloat(scale);
    }

    @Override
    public @NotNull String writeToString() {
        return color + " " + scale;
    }
}
