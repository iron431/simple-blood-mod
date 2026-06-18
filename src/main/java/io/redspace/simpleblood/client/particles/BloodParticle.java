package io.redspace.simpleblood.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.simpleblood.decal_behavior.DecalDirection;
import io.redspace.simpleblood.decal_behavior.DecalType;
import io.redspace.simpleblood.registry.ParticleRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static net.minecraft.world.level.ClipContext.Block.VISUAL;
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
                Vec3 groundLevel = level.clip(new ClipContext(this.getPos().add(0, 0.6, 0), this.getPos(), VISUAL, NONE, (net.minecraft.world.entity.Entity) null)).getLocation();
                this.level.addParticle(new BloodGroundParticleOptions(this.color, this.getQuadSize(0.0F)), true, groundLevel.x, groundLevel.y, groundLevel.z, 0.0D, 0.0D, 0.0D);
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
        renderRotatedQuad(buffer, renderInfo, partialTicks);
    }

    public void renderRotatedQuad(VertexConsumer pBuffer, Camera pRenderInfo, float pPartialTicks) {
        Vec3 vec3 = pRenderInfo.getPosition();
        float f = (float) (Mth.lerp((double) pPartialTicks, this.xo, this.x) - vec3.x());
        float f1 = (float) (Mth.lerp((double) pPartialTicks, this.yo, this.y) - vec3.y());
        float f2 = (float) (Mth.lerp((double) pPartialTicks, this.zo, this.z) - vec3.z());
        Quaternionf quaternionf;
        if (this.roll == 0.0F) {
            quaternionf = pRenderInfo.rotation();
        } else {
            quaternionf = new Quaternionf(pRenderInfo.rotation());
            quaternionf.rotateZ(Mth.lerp(pPartialTicks, this.oRoll, this.roll));
        }
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f3 = this.getQuadSize(pPartialTicks);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternionf);
            vector3f.mul(f3);
            vector3f.add(f, f1, f2);
        }

        float f6 = this.getU0();
        float f7 = this.getU1();
        if (this.mirrored) {
            float tmp = f6;
            f6 = f7;
            f7 = tmp;
        }
        float f4 = this.getV0();
        float f5 = this.getV1();
        int j = this.getLightColor(pPartialTicks);
        pBuffer.vertex((double) avector3f[0].x(), (double) avector3f[0].y(), (double) avector3f[0].z()).uv(f7, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double) avector3f[1].x(), (double) avector3f[1].y(), (double) avector3f[1].z()).uv(f7, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double) avector3f[2].x(), (double) avector3f[2].y(), (double) avector3f[2].z()).uv(f6, f4).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
        pBuffer.vertex((double) avector3f[3].x(), (double) avector3f[3].y(), (double) avector3f[3].z()).uv(f6, f5).color(this.rCol, this.gCol, this.bCol, this.alpha).uv2(j).endVertex();
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
        buffer.vertex(vector3f.x(), vector3f.y(), vector3f.z())
                .uv(u, v)
                .color(this.rCol, this.gCol, this.bCol, this.alpha)
                .uv2(packedLight)
                .endVertex();
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
