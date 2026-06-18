package io.redspace.simpleblood.client.particles;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.ParticleGroupRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * custom render state for 26.1.2, because single-quads are now strictly enforeced as single quad particles
 */
public class BloodDecalRenderState implements ParticleGroupRenderState, SubmitNodeCollector.ParticleGroupRenderer {
    public static final ParticleRenderType TERRAIN_DECAL = new ParticleRenderType("simpleblood:terrain_decal");

    private final Map<SingleQuadParticle.Layer, Storage> layers = new HashMap<>();
    private int vertexCount;

    public void addVertex(SingleQuadParticle.Layer layer, float x, float y, float z, float u, float v, int color, int lightCoords) {
        this.layers.computeIfAbsent(layer, ignored -> new Storage()).add(x, y, z, u, v, color, lightCoords);
        this.vertexCount++;
    }

    @Override
    public void clear() {
        this.layers.values().forEach(Storage::clear);
        this.vertexCount = 0;
    }

    @Override
    public boolean isEmpty() {
        return this.vertexCount == 0;
    }

    @Override
    public QuadParticleRenderState.@Nullable PreparedBuffers prepare(ParticleFeatureRenderer.ParticleBufferCache cachedBuffer, boolean translucent) {
        if (this.isEmpty()) {
            return null;
        }

        try (ByteBufferBuilder builder = ByteBufferBuilder.exactlySized(this.vertexCount * DefaultVertexFormat.PARTICLE.getVertexSize())) {
            BufferBuilder bufferBuilder = new BufferBuilder(builder, VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            Map<SingleQuadParticle.Layer, QuadParticleRenderState.PreparedLayer> preparedLayers = new HashMap<>();
            int offset = 0;

            for (Map.Entry<SingleQuadParticle.Layer, Storage> entry : this.layers.entrySet()) {
                if (entry.getKey().translucent() == translucent) {
                    Storage storage = entry.getValue();
                    storage.forEach((x, y, z, u, v, color, lightCoords) ->
                            bufferBuilder.addVertex(x, y, z).setUv(u, v).setColor(color).setLight(lightCoords));
                    int verts = storage.count();
                    if (verts > 0) {
                        preparedLayers.put(entry.getKey(), new QuadParticleRenderState.PreparedLayer(offset, verts / 4 * 6));
                    }
                    offset += verts;
                }
            }

            MeshData mesh = bufferBuilder.build();
            if (mesh != null) {
                cachedBuffer.write(mesh.vertexBuffer());
                RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS).getBuffer(mesh.drawState().indexCount());
                GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                        .writeTransform(RenderSystem.getModelViewMatrix(), new Vector4f(1.0F, 1.0F, 1.0F, 1.0F), new Vector3f(), new Matrix4f());
                return new QuadParticleRenderState.PreparedBuffers(mesh.drawState().indexCount(), dynamicTransforms, preparedLayers);
            }
        }

        return null;
    }

    @Override
    public void render(
            QuadParticleRenderState.PreparedBuffers preparedBuffers,
            ParticleFeatureRenderer.ParticleBufferCache bufferCache,
            RenderPass renderPass,
            TextureManager textureManager
    ) {
        RenderSystem.AutoStorageIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        renderPass.setVertexBuffer(0, bufferCache.get());
        renderPass.setIndexBuffer(indexBuffer.getBuffer(preparedBuffers.indexCount()), indexBuffer.type());
        renderPass.setUniform("DynamicTransforms", preparedBuffers.dynamicTransforms());

        for (Map.Entry<SingleQuadParticle.Layer, QuadParticleRenderState.PreparedLayer> entry : preparedBuffers.layers().entrySet()) {
            renderPass.setPipeline(entry.getKey().pipeline());
            AbstractTexture texture = textureManager.getTexture(entry.getKey().textureAtlasLocation());
            renderPass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
            renderPass.drawIndexed(entry.getValue().vertexOffset(), 0, entry.getValue().indexCount(), 1);
        }
    }

    @Override
    public void submit(SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        if (this.vertexCount > 0) {
            submitNodeCollector.submitParticleGroup(this);
        }
    }

    @FunctionalInterface

    private interface VertexConsumerFn {
        void consume(float x, float y, float z, float u, float v, int color, int lightCoords);
    }


    private static class Storage {
        private static final int FLOATS_PER_VERTEX = 5;
        private static final int INTS_PER_VERTEX = 2;
        private int capacity = 4096;
        private float[] floatValues = new float[capacity * FLOATS_PER_VERTEX];
        private int[] intValues = new int[capacity * INTS_PER_VERTEX];
        private int currentVertexIndex;

        public void add(float x, float y, float z, float u, float v, int color, int lightCoords) {
            if (this.currentVertexIndex >= this.capacity) {
                this.grow();
            }
            int findex = this.currentVertexIndex * FLOATS_PER_VERTEX;
            this.floatValues[findex++] = x;
            this.floatValues[findex++] = y;
            this.floatValues[findex++] = z;
            this.floatValues[findex++] = u;
            this.floatValues[findex] = v;
            int iindex = this.currentVertexIndex * INTS_PER_VERTEX;
            this.intValues[iindex++] = color;
            this.intValues[iindex] = lightCoords;
            this.currentVertexIndex++;
        }

        public void forEach(VertexConsumerFn consumer) {
            for (int vertex = 0; vertex < this.currentVertexIndex; vertex++) {
                int findex = vertex * FLOATS_PER_VERTEX;
                int iindex = vertex * INTS_PER_VERTEX;
                consumer.consume(
                        this.floatValues[findex++],
                        this.floatValues[findex++],
                        this.floatValues[findex++],
                        this.floatValues[findex++],
                        this.floatValues[findex],
                        this.intValues[iindex++],
                        this.intValues[iindex]
                );
            }
        }

        public void clear() {
            this.currentVertexIndex = 0;
        }

        public int count() {
            return this.currentVertexIndex;
        }

        private void grow() {
            this.capacity *= 2;
            this.floatValues = Arrays.copyOf(this.floatValues, this.capacity * FLOATS_PER_VERTEX);
            this.intValues = Arrays.copyOf(this.intValues, this.capacity * INTS_PER_VERTEX);
        }
    }
}
