package fr.xacraft.render;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

public class Frustum {
    private final FrustumIntersection frustum;

    public Frustum() {
        this.frustum = new FrustumIntersection();
    }

    public void update(Matrix4f projection, Matrix4f view) {
        Matrix4f combined = new Matrix4f();
        projection.mul(view, combined);
        frustum.set(combined);
    }

    public boolean isChunkInFrustum(float chunkX, float chunkZ) {
        float minX = chunkX * 16.0f;
        float minY = 0.0f;
        float minZ = chunkZ * 16.0f;
        float maxX = minX + 16.0f;
        float maxY = 255.0f;
        float maxZ = minZ + 16.0f;

        return frustum.testAab(minX, minY, minZ, maxX, maxY, maxZ);
    }
}
