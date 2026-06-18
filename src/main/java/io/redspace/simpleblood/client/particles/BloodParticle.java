package io.redspace.simpleblood.client.particles;

import io.redspace.simpleblood.decal_behavior.DecalDirection;
import io.redspace.simpleblood.decal_behavior.DecalType;
import io.redspace.simpleblood.registry.ParticleRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3fc;

import static net.minecraft.world.level.ClipContext.Block.VISUAL;
import static net.minecraft.world.level.ClipContext.Fluid.NONE;


public class BloodParticle extends SingleQuadParticle {
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
        super(level, xCoord, yCoord, zCoord, xd, yd, zd, spriteSet.first());
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
        this.setSprite(spriteSet.get(this.random));

        this.rCol = BloodParticleOptions.red(color);
        this.gCol = BloodParticleOptions.green(color);
        this.bCol = BloodParticleOptions.blue(color);

        this.scaleTransition = 1f + (float) Math.random();
        this.mirrored = this.random.nextBoolean();
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
                this.level.addParticle(new BloodGroundParticleOptions(this.color, this.getQuadSize(0.0F)), true, false, groundLevel.x, groundLevel.y, groundLevel.z, 0.0D, 0.0D, 0.0D);
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
    protected SingleQuadParticle.Layer getLayer() {
        return underwater ? SingleQuadParticle.Layer.TRANSLUCENT : SingleQuadParticle.Layer.OPAQUE;
    }

    @Override
    public void extract(@NotNull QuadParticleRenderState renderState, @NotNull Camera camera, float partialTick) {
        if (this.decalDirection != DecalDirection.OMNIDIRECTIONAL) {
            Vector3fc lv = camera.leftVector();
            Vec3 left = new Vec3(lv.x(), lv.y(), lv.z());
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
        super.extract(renderState, camera, partialTick);
    }

    @Override
    protected float getU0() {
        return this.mirrored ? this.spriteU1() : this.spriteU0();
    }

    @Override
    protected float getU1() {
        return this.mirrored ? this.spriteU0() : this.spriteU1();
    }

    protected float spriteU0() {
        return super.getU0();
    }

    protected float spriteU1() {
        return super.getU1();
    }


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
                                       double dx, double dy, double dz, RandomSource random) {
            return new BloodParticle(level, x, y, z, this.sprites, this.decalType, this.decalDirection, ParticleRegistry.DEFAULT_BLOOD_COLOR, 1f, dx, dy, dz);
        }

        @Override
        public Particle create(BloodParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new BloodParticle(level, x, y, z, this.sprites, this.decalType, this.decalDirection, options.color(), options.scale(), dx, dy, dz);
        }
    }
}
