package fr.xacraft.physics;

import org.joml.Vector3f;

public class AABB {
    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    public AABB(Vector3f position, Vector3f size) {
        this.minX = position.x - size.x / 2.0f;
        this.minY = position.y;
        this.minZ = position.z - size.z / 2.0f;
        this.maxX = position.x + size.x / 2.0f;
        this.maxY = position.y + size.y;
        this.maxZ = position.z + size.z / 2.0f;
    }

    public AABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public boolean intersects(AABB other) {
        return this.maxX > other.minX && this.minX < other.maxX &&
                this.maxY > other.minY && this.minY < other.maxY &&
                this.maxZ > other.minZ && this.minZ < other.maxZ;
    }

    public AABB offset(float x, float y, float z) {
        return new AABB(
                minX + x, minY + y, minZ + z,
                maxX + x, maxY + y, maxZ + z
        );
    }

    public AABB expand(float x, float y, float z) {
        float newMinX = x < 0 ? minX + x : minX;
        float newMinY = y < 0 ? minY + y : minY;
        float newMinZ = z < 0 ? minZ + z : minZ;
        float newMaxX = x > 0 ? maxX + x : maxX;
        float newMaxY = y > 0 ? maxY + y : maxY;
        float newMaxZ = z > 0 ? maxZ + z : maxZ;

        return new AABB(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }
}
