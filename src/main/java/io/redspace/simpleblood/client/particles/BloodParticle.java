package io.redspace.simpleblood.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.simpleblood.client.ClientConfig;
import io.redspace.simpleblood.decal_behavior.DecalDirection;
import io.redspace.simpleblood.decal_behavior.DecalType;
import io.redspace.simpleblood.registry.ParticleRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static net.minecraft.world.level.ClipContext.Block.*;
import static net.minecraft.world.level.ClipContext.Fluid.NONE;

public class BloodParticle extends TextureSheetParticle {
    private final DecalType decalType;
    private final DecalDirection decalDirection;
    private final int color;
    float scaleTransition;
    private boolean mirrored;
    private boolean underwater;

    public BloodParticle(
            ClientLevel level,
            double xCoord,
            double yCoord,
            double zCoord,
            SpriteSet spriteSet,
            DecalType decalType,
            DecalDirection decalDirection,
            int color,
            float scale,
            double xd,
            double yd,
            double zd
    ) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);
        this.decalType = decalType;
        this.decalDirection = decalDirection;
        this.color = color;
        this.xd = xd;
        this.yd = yd * 1.5 + .15f;
        this.zd = zd;
        this.quadSize *= 1f + (float) Math.random();
        this.scale(scale * 2.5f);
        this.lifetime = 100 + (int) (Math.random() * 40);
        this.gravity = 1.5F;
        this.pickSprite(spriteSet);

        this.rCol = BloodParticleOptions.red(color);
        this.gCol = BloodParticleOptions.green(color);
        this.bCol = BloodParticleOptions.blue(color);

        this.scaleTransition = 1f + (float) Math.random();
        this.mirrored = level.random.nextBoolean();
        if (!level.getFluidState(BlockPos.containing(x, y, z)).isEmpty()) {
            this.underwater = true;
            this.xd *= 0.5f;
            this.yd *= 0.5f;
            this.zd *= 0.5f;
            this.gravity *= .1f;
        }
    }

    public DecalType getDecalType() {
        return this.decalType;
    }

    public DecalDirection getDecalDirection() {
        return this.decalDirection;
    }

    @Override
    public void tick() {
        super.tick();
        if (underwater) {
            this.gravity *= .99f;
        }
        if (this.onGround) {
            if (alpha > 0.5) {
                // prevent low-life underwater particles from emitting ground particle
                Vec3 groundLevel = level.clip(new ClipContext(this.getPos().add(0, 0.6, 0), this.getPos(), VISUAL, NONE, CollisionContext.empty())).getLocation();
                this.level.addParticle(new BloodGroundParticleOptions(this.color), true, groundLevel.x, groundLevel.y, groundLevel.z, this.getQuadSize(0.0F), 0.0D, 0.0D);
            }
            this.remove();
        }
    }

    @Override
    public float getQuadSize(float partialTick) {
        float scaleMult = (this.age + partialTick) > scaleTransition ? 1f : (this.age + partialTick) / (scaleTransition * 2f) + .5f;
        return super.getQuadSize(partialTick) * scaleMult;
    }

    @Override
    public void render(@NotNull VertexConsumer buffer, @NotNull Camera renderInfo, float partialTicks) {
        if (this.decalDirection != DecalDirection.OMNIDIRECTIONAL) {
            Vec3 left = new Vec3(renderInfo.getLeftVector());
            Vec3 horizontalVelocity = new Vec3(xd, 0, zd);
            double dot = left.dot(horizontalVelocity.normalize());
            if (Math.abs(dot) > 0.1) {
                boolean facingRight = dot < 0;
                this.mirrored = facingRight ^ this.decalDirection == DecalDirection.RIGHT;
            }
        }
        if (underwater) {
            alpha -= 0.005f;
            scale(1.005f);
            if (alpha < .1) {
                remove();
                return;
            }
        }
        super.render(buffer, renderInfo, partialTicks);
    }

    @Override
    protected void renderRotatedQuad(@NotNull VertexConsumer buffer, @NotNull Quaternionf quaternion, float x, float y, float z, float partialTicks) {
        float f = this.getQuadSize(partialTicks);
        float f1 = this.getU0();
        float f2 = this.getU1();
        float f3 = this.getV0();
        float f4 = this.getV1();
        if (this.mirrored) {
            float tmp = f1;
            f1 = f2;
            f2 = tmp;
        }
        int i = this.getLightColor(partialTicks);
        this.renderVertex(buffer, quaternion, x, y, z, 1.0F, -1.0F, f, f2, f4, i);
        this.renderVertex(buffer, quaternion, x, y, z, 1.0F, 1.0F, f, f2, f3, i);
        this.renderVertex(buffer, quaternion, x, y, z, -1.0F, 1.0F, f, f1, f3, i);
        this.renderVertex(buffer, quaternion, x, y, z, -1.0F, -1.0F, f, f1, f4, i);
    }

    private void renderVertex(
            VertexConsumer buffer,
            Quaternionf quaternion,
            float x,
            float y,
            float z,
            float xOffset,
            float yOffset,
            float quadSize,
            float u,
            float v,
            int packedLight
    ) {
        Vector3f vector3f = new Vector3f(xOffset, yOffset, 0.0F).rotate(quaternion).mul(quadSize).add(x, y, z);
        buffer.addVertex(vector3f.x(), vector3f.y(), vector3f.z())
                .setUv(u, v)
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
                .setLight(packedLight);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return underwater ? ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT : ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType>, BloodEmitterParticle.VariantFactory {
        private final SpriteSet sprites;
        private final DecalType decalType;
        private final DecalDirection decalDirection;

        public Provider(SpriteSet spriteSet, DecalType decalType, DecalDirection decalDirection) {
            this.sprites = spriteSet;
            this.decalType = decalType;
            this.decalDirection = decalDirection;
        }

        @Override
        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new BloodParticle(level, x, y, z, this.sprites, this.decalType, this.decalDirection, ParticleRegistry.DEFAULT_BLOOD_COLOR, 1f, dx, dy, dz);
        }

        @Override
        public Particle create(BloodParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new BloodParticle(level, x, y, z, this.sprites, this.decalType, this.decalDirection, options.color(), options.scale(), dx, dy, dz);
        }
    }
}
