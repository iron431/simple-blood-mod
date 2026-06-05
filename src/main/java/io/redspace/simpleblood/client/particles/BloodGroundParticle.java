package io.redspace.simpleblood.client.particles;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.redspace.simpleblood.registry.ParticleRegistry;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class BloodGroundParticle extends TextureSheetParticle {
    private static final Vector3f ROTATION_VECTOR = Util.make(new Vector3f(0.5F, 0.5F, 0.5F), Vector3f::normalize);
    private static final Vector3f TRANSFORM_VECTOR = new Vector3f(-1.0F, -1.0F, 0.0F);
    private static final float DEGREES_90 = Mth.PI / 2f;

    public BloodGroundParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, double scale, double yd, double zd) {

        super(level, xCoord, yCoord, zCoord, 0.0D, yd, zd);

        this.xd = 0.0D;
        this.yd = yd;
        this.zd = zd;
        this.quadSize = (1.5f + (float) Math.random() * 0.25f) * readScale(scale);
        this.lifetime = 200 + (int) (Math.random() * 200);
        this.gravity = 1.0F;
        this.pickSprite(spriteSet);

        this.rCol = ParticleRegistry.BLOOD_COLOR.x;
        this.gCol = ParticleRegistry.BLOOD_COLOR.y;
        this.bCol = ParticleRegistry.BLOOD_COLOR.z;
    }

    private static float readScale(double scale) {
        return scale > 0.0D ? (float) scale : 1.0F;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        this.alpha = 1.0F - Mth.clamp(((float) this.age + partialTick - 90) / (float) this.lifetime, 0.2F, .7F);
        float quadSize = this.getQuadSize(partialTick);
        if (this.age + partialTick <= SPLAT_IN_TIME) {
            quadSize *= (this.age + partialTick) / (SPLAT_IN_TIME * 2f) + .5f;
        }
        this.renderRotatedParticle(buffer, camera, partialTick, quadSize, (quat) -> {
            quat.mul(Axis.YP.rotation(-(float) Math.PI));
            quat.mul(Axis.XP.rotation(DEGREES_90));
        });
    }

    private static final float SPLAT_IN_TIME = 1.5f;
    private static final float MAX_PROJECTION_HEIGHT = 2.0f;

    private void renderRotatedParticle(VertexConsumer pConsumer, Camera camera, float partialTick, float quadSize, Consumer<Quaternionf> pQuaternion) {
        Vec3 cameraPos = camera.getPosition();
        float localX = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float localY = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y()) + 0.01f + .005f * (this.age / (float) this.lifetime);
        float localZ = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());
        Quaternionf quaternion = (new Quaternionf()).setAngleAxis(0.0F, ROTATION_VECTOR.x(), ROTATION_VECTOR.y(), ROTATION_VECTOR.z());

        pQuaternion.accept(quaternion);
        quaternion.transform(TRANSFORM_VECTOR);

        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};


        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternion);
            vector3f.mul(quadSize * 0.5f); // vector is a 2x2 plane, cut in half
            vector3f.add(localX, localY, localZ);
        }

        Vec3 worldExtentMin = new Vec3(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        Vec3 worldExtentMax = new Vec3(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
        for (Vector3f corner : avector3f) {
            Vec3 worldCorner = cameraPos.add(corner.x(), corner.y(), corner.z());
            worldExtentMin = new Vec3(
                    Math.min(worldExtentMin.x, worldCorner.x),
                    Math.min(worldExtentMin.y, worldCorner.y),
                    Math.min(worldExtentMin.z, worldCorner.z)
            );
            worldExtentMax = new Vec3(
                    Math.max(worldExtentMax.x, worldCorner.x),
                    Math.max(worldExtentMax.y, worldCorner.y),
                    Math.max(worldExtentMax.z, worldCorner.z)
            );
        }
        int light = this.getLightColor(partialTick);
        double centerX = Mth.lerp(partialTick, this.xo, this.x);
        double centerY = Mth.lerp(partialTick, this.yo, this.y);
        double centerZ = Mth.lerp(partialTick, this.zo, this.z);
        int startY = Mth.floor(centerY + 1.0D);
        int endY = Mth.floor(centerY - MAX_PROJECTION_HEIGHT);

        int minBlockX = BlockPos.containing(worldExtentMin).getX();
        int maxBlockX = BlockPos.containing(worldExtentMax).getX();
        int minBlockZ = BlockPos.containing(worldExtentMin).getZ();
        int maxBlockZ = BlockPos.containing(worldExtentMax).getZ();

        for (int blockX = minBlockX; blockX <= maxBlockX; blockX++) {
            for (int blockZ = minBlockZ; blockZ <= maxBlockZ; blockZ++) {
                this.renderColumnDecal(pConsumer, camera, blockX, blockZ, startY, endY, centerX, centerY, centerZ, worldExtentMin, worldExtentMax, quadSize, light);
            }
        }
    }

    private void renderColumnDecal(
            VertexConsumer buffer,
            Camera camera,
            int blockX,
            int blockZ,
            int startY,
            int endY,
            double centerX,
            double centerY,
            double centerZ,
            Vec3 worldExtentMin,
            Vec3 worldExtentMax,
            float quadSize,
            int light
    ) {
        BlockPos.MutableBlockPos columnPos = new BlockPos.MutableBlockPos();

        for (int y = startY; y >= endY; y--) {
            columnPos.set(blockX, y, blockZ);
            BlockPos surfacePos = columnPos.below();
            BlockState blockState = this.level.getBlockState(surfacePos);
            if (blockState.getRenderShape() == RenderShape.INVISIBLE) {
                continue;
            }

            VoxelShape shape = blockState.getShape(this.level, surfacePos);
            if (shape.isEmpty()) {
                continue;
            }

            AABB bounds = shape.bounds();
            double surfaceTopY = surfacePos.getY() + bounds.maxY;
            if (surfaceTopY > centerY + 0.25D) {
                continue;
            }
            float drop = (float) (centerY - surfaceTopY);
            float alphaMultiplier = Mth.lerp( Mth.clamp(drop / MAX_PROJECTION_HEIGHT, 0.0F, 1.0F), 1.0F, 0.25F);
            if (this.renderBlockDecal(
                    buffer,
                    camera,
                    surfacePos,
                    bounds,
                    (float) surfaceTopY,
                    centerX,
                    centerZ,
                    worldExtentMin,
                    worldExtentMax,
                    quadSize,
                    light,
                    alphaMultiplier
            )) {
                return;
            }
        }
    }

    private boolean renderBlockDecal(
            VertexConsumer buffer,
            Camera camera,
            BlockPos surfacePos,
            AABB bounds,
            float surfaceTopY,
            double centerX,
            double centerZ,
            Vec3 worldExtentMin,
            Vec3 worldExtentMax,
            float quadSize,
            int light,
            float alphaMultiplier
    ) {
        float minX = surfacePos.getX() + (float) bounds.minX;
        float maxX = surfacePos.getX() + (float) bounds.maxX;
        float minZ = surfacePos.getZ() + (float) bounds.minZ;
        float maxZ = surfacePos.getZ() + (float) bounds.maxZ;
        float surfaceY = surfaceTopY + 0.005625F;

        if (minX < worldExtentMin.x) {
            minX = (float) worldExtentMin.x;
        }
        if (maxX > worldExtentMax.x) {
            maxX = (float) worldExtentMax.x;
        }
        if (minZ < worldExtentMin.z) {
            minZ = (float) worldExtentMin.z;
        }
        if (maxZ > worldExtentMax.z) {
            maxZ = (float) worldExtentMax.z;
        }
        if (minX >= maxX || minZ >= maxZ) {
            return false;
        }

        float u0 = this.getU0();
        float u1 = this.getU1();
        float v0 = this.getV0();
        float v1 = this.getV1();
        float halfSize = quadSize * 0.5F;
        Vec3 cameraPos = camera.getPosition();
        Vec2[] corners = new Vec2[]{
                new Vec2(minX, minZ),
                new Vec2(minX, maxZ),
                new Vec2(maxX, maxZ),
                new Vec2(maxX, minZ),
        };

        for (Vec2 corner : corners) {
            float offsetX = corner.x - (float) centerX;
            float offsetZ = corner.y - (float) centerZ;
            float u = (offsetX / (2.0F * halfSize) + 0.5F) * (u1 - u0) + u0;
            float v = (offsetZ / (2.0F * halfSize) + 0.5F) * (v1 - v0) + v0;
            this.makeCornerVertex(
                    buffer,
                    new Vector3f(
                            corner.x - (float) cameraPos.x,
                            surfaceY - (float) cameraPos.y,
                            corner.y - (float) cameraPos.z
                    ),
                    u,
                    v,
                    light,
                    alphaMultiplier
            );
        }
        return true;
    }

    private void makeCornerVertex(VertexConsumer pConsumer, Vector3f pVertex, float pU, float pV, int pPackedLight, float alphaMultiplier) {
        pConsumer.addVertex(pVertex.x(), pVertex.y(), pVertex.z())
                .setColor(this.rCol, this.gCol, this.bCol, this.alpha * alphaMultiplier)
                .setUv(pU, pV)
                .setLight(pPackedLight);
    }

    @NotNull
    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
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
            return new BloodGroundParticle(level, x, y, z, this.sprites, dx, dy, dz);
        }
    }
}
