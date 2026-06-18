package io.redspace.simpleblood.client.particles;

import net.minecraft.client.Camera;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.world.phys.Vec3;


public class BloodDecalParticleGroup extends ParticleGroup<BloodGroundParticle> {
    private final BloodDecalRenderState renderState = new BloodDecalRenderState();

    public BloodDecalParticleGroup(ParticleEngine engine) {
        super(engine);
    }

    @Override
    public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float partialTickTime) {
        for (BloodGroundParticle particle : this.particles) {
            Vec3 pos = particle.getPos();
            if (frustum.pointInFrustum(pos.x, pos.y, pos.z)) {
                particle.emit(this.renderState, camera, partialTickTime);
            }
        }
        return this.renderState;
    }
}
