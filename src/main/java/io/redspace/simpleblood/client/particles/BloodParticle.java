package io.redspace.simpleblood.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.simpleblood.registry.ParticleRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static net.minecraft.world.level.ClipContext.Block.*;
import static net.minecraft.world.level.ClipContext.Fluid.NONE;

public class BloodParticle extends TextureSheetParticle {
    private final boolean mirrored;
    float scaleTransition;

    public BloodParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double xd, double yd, double zd) {

        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        this.xd = xd;
        this.yd = yd * 2 + .05f;
        this.zd = zd;
        this.quadSize *= 1f + (float) Math.random();
        this.scale(2.5f);
        this.lifetime = 30 + (int) (Math.random() * 10);
        this.gravity = 1.0F;
        this.pickSprite(spriteSet);

        this.rCol = ParticleRegistry.BLOOD_COLOR.x;
        this.gCol = ParticleRegistry.BLOOD_COLOR.y;
        this.bCol = ParticleRegistry.BLOOD_COLOR.z;

        this.scaleTransition = 1f + (float) Math.random();
        this.mirrored = level.random.nextBoolean();

    }


    @Override
    public void tick() {
        super.tick();
        if (this.onGround) {
            Vec3 groundLevel = level.clip(new ClipContext(this.getPos().add(0, 0.6, 0), this.getPos(), VISUAL, NONE, CollisionContext.empty())).getLocation();
            this.level.addParticle(ParticleRegistry.BLOOD_GROUND_PARTICLE.get(), groundLevel.x, groundLevel.y, groundLevel.z, this.getQuadSize(0.0F), 0.0D, 0.0D);
            this.remove();
        }
    }

    @Override
    public float getQuadSize(float partialTick) {
        float scaleMult = (this.age + partialTick) > scaleTransition ? 1f : (this.age + partialTick) / (scaleTransition * 2f) + .5f;
        return super.getQuadSize(partialTick) * scaleMult;
    }

    protected void renderRotatedQuad(VertexConsumer buffer, Quaternionf quaternion, float x, float y, float z, float partialTicks) {
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
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        public Particle createParticle(SimpleParticleType particleType, ClientLevel level,
                                       double x, double y, double z,
                                       double dx, double dy, double dz) {
            return new BloodParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
