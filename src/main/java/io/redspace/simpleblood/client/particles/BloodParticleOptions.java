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

public record BloodParticleOptions(int color, float scale, boolean isGraphic) implements ParticleOptions {
    public BloodParticleOptions(int color) {
        this(color, 1f, true);
    }

    public static final Codec<BloodParticleOptions> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("color").forGetter(BloodParticleOptions::color),
                    Codec.FLOAT.optionalFieldOf("scale", 1f).forGetter(BloodParticleOptions::scale),
                    Codec.BOOL.optionalFieldOf("is_graphic", true).forGetter(BloodParticleOptions::isGraphic)
            ).apply(instance, BloodParticleOptions::new)
    );

    public static final ParticleOptions.Deserializer<BloodParticleOptions> DESERIALIZER = new ParticleOptions.Deserializer<>() {
        @Override
        public @NotNull BloodParticleOptions fromCommand(@NotNull ParticleType<BloodParticleOptions> type, @NotNull StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            int color = reader.readInt();
            reader.expect(' ');
            float scale = reader.readFloat();
            reader.expect(' ');
            boolean isGraphic = reader.readBoolean();
            return new BloodParticleOptions(color, scale, isGraphic);
        }

        @Override
        public @NotNull BloodParticleOptions fromNetwork(@NotNull ParticleType<BloodParticleOptions> type, @NotNull FriendlyByteBuf buf) {
            return new BloodParticleOptions(buf.readVarInt(), buf.readFloat(), buf.readBoolean());
        }
    };

    public static float red(int color) {
        return ((color >> 16) & 0xFF) / 255f;
    }

    public static float green(int color) {
        return ((color >> 8) & 0xFF) / 255f;
    }

    public static float blue(int color) {
        return (color & 0xFF) / 255f;
    }

    @Override
    public ParticleType<?> getType() {
        return ParticleRegistry.BLOOD_EMITTER.get();
    }

    @Override
    public void writeToNetwork(FriendlyByteBuf buf) {
        buf.writeVarInt(color);
        buf.writeFloat(scale);
        buf.writeBoolean(isGraphic);
    }

    @Override
    public @NotNull String writeToString() {
        return color + " " + scale + " " + isGraphic;
    }
}
