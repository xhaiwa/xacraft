package fr.xacraft.entity;

import fr.xacraft.physics.AABB;
import fr.xacraft.world.World;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Entity {
    protected Vector3f position;
    protected Vector3f motion;
    protected Vector3f boundingBox;
    protected boolean onGround;
    protected World world;

    protected Vector3f previousPosition;
    protected Vector3f renderPosition;

    public Entity(Vector3f position, Vector3f boundingBox, World world) {
        this.position = position;
        this.motion = new Vector3f();
        this.boundingBox = boundingBox;
        this.onGround = false;
        this.world = world;

        this.previousPosition = new Vector3f(position);
        this.renderPosition = new Vector3f(position);
    }

    public void update() {
        previousPosition.set(position);

        if (!onGround) {
            this.motion.y -= 0.08f;
        }

        if (this.motion.y < -3.92f) {
            this.motion.y = -3.92f;
        }

        handleCollision();

        if (onGround) {
            this.motion.x *= 0.546f;
            this.motion.z *= 0.546f;
        } else {
            this.motion.x *= 0.91f;
            this.motion.z *= 0.91f;
        }
    }

    public void interpolate(float alpha) {
        renderPosition.x = previousPosition.x + (position.x - previousPosition.x) * alpha;
        renderPosition.y = previousPosition.y + (position.y - previousPosition.y) * alpha;
        renderPosition.z = previousPosition.z + (position.z - previousPosition.z) * alpha;
    }

    private void handleCollision() {
        List<AABB> blockAABBs = getCollidingBlocks(new Vector3f(0, motion.y, 0));

        float originalMotionY = motion.y;
        for (AABB blockAABB : blockAABBs) {
            motion.y = calculateYOffset(blockAABB, motion.y);
        }

        position.y += motion.y;

        if (Math.abs(motion.y - originalMotionY) > 0.001f) {
            if (originalMotionY < 0) {
                onGround = true;
            }
            motion.y = 0;
        } else {
            onGround = false;
        }

        blockAABBs = getCollidingBlocks(new Vector3f(motion.x, 0, 0));

        for (AABB blockAABB : blockAABBs) {
            motion.x = calculateXOffset(blockAABB, motion.x);
        }

        position.x += motion.x;

        blockAABBs = getCollidingBlocks(new Vector3f(0, 0, motion.z));

        for (AABB blockAABB : blockAABBs) {
            motion.z = calculateZOffset(blockAABB, motion.z);
        }

        position.z += motion.z;
    }

    private List<AABB> getCollidingBlocks(Vector3f motion) {
        List<AABB> list = new ArrayList<>();

        AABB entityAABB = new AABB(position, boundingBox);
        AABB expanded = entityAABB.expand(motion.x, motion.y, motion.z);

        int minX = (int) Math.floor(expanded.minX);
        int minY = (int) Math.floor(expanded.minY);
        int minZ = (int) Math.floor(expanded.minZ);
        int maxX = (int) Math.ceil(expanded.maxX);
        int maxY = (int) Math.ceil(expanded.maxY);
        int maxZ = (int) Math.ceil(expanded.maxZ);

        for (int x = minX; x <= maxX; x++) {
            for (int y = Math.max(0, minY); y <= Math.min(254, maxY); y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    if (isBlockSolidAt(x, y, z)) {
                        list.add(new AABB(x, y, z, x + 1, y + 1, z + 1));
                    }
                }
            }
        }

        return list;
    }

    private boolean isBlockSolidAt(int x, int y, int z) {
        int chunkX = Math.floorDiv(x, 16);
        int chunkZ = Math.floorDiv(z, 16);
        int localX = Math.floorMod(x, 16);
        int localZ = Math.floorMod(z, 16);

        return world.isBlockSolid(chunkX, chunkZ, localX, y, localZ);
    }

    private float calculateYOffset(AABB blockAABB, float offsetY) {
        AABB entityAABB = new AABB(position, boundingBox);

        if (blockAABB.maxX > entityAABB.minX && blockAABB.minX < entityAABB.maxX &&
                blockAABB.maxZ > entityAABB.minZ && blockAABB.minZ < entityAABB.maxZ) {

            if (offsetY < 0 && entityAABB.minY >= blockAABB.maxY) {
                float newOffset = blockAABB.maxY - entityAABB.minY;
                if (newOffset > offsetY) {
                    return newOffset;
                }
            }

            if (offsetY > 0 && entityAABB.maxY <= blockAABB.minY) {
                float newOffset = blockAABB.minY - entityAABB.maxY;
                if (newOffset < offsetY) {
                    return newOffset;
                }
            }
        }

        return offsetY;
    }

    private float calculateXOffset(AABB blockAABB, float offsetX) {
        AABB entityAABB = new AABB(position, boundingBox);

        if (blockAABB.maxY > entityAABB.minY && blockAABB.minY < entityAABB.maxY &&
                blockAABB.maxZ > entityAABB.minZ && blockAABB.minZ < entityAABB.maxZ) {

            if (offsetX > 0 && entityAABB.maxX <= blockAABB.minX) {
                float newOffset = blockAABB.minX - entityAABB.maxX;
                if (newOffset < offsetX) {
                    return newOffset;
                }
            }

            if (offsetX < 0 && entityAABB.minX >= blockAABB.maxX) {
                float newOffset = blockAABB.maxX - entityAABB.minX;
                if (newOffset > offsetX) {
                    return newOffset;
                }
            }
        }

        return offsetX;
    }

    private float calculateZOffset(AABB blockAABB, float offsetZ) {
        AABB entityAABB = new AABB(position, boundingBox);

        if (blockAABB.maxX > entityAABB.minX && blockAABB.minX < entityAABB.maxX &&
                blockAABB.maxY > entityAABB.minY && blockAABB.minY < entityAABB.maxY) {

            if (offsetZ > 0 && entityAABB.maxZ <= blockAABB.minZ) {
                float newOffset = blockAABB.minZ - entityAABB.maxZ;
                if (newOffset < offsetZ) {
                    return newOffset;
                }
            }

            if (offsetZ < 0 && entityAABB.minZ >= blockAABB.maxZ) {
                float newOffset = blockAABB.maxZ - entityAABB.minZ;
                if (newOffset > offsetZ) {
                    return newOffset;
                }
            }
        }

        return offsetZ;
    }

    public void jump() {
        if (onGround) {
            this.motion.y = 0.42f;
        }
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public Vector3f getRenderPosition() {
        return this.renderPosition;
    }

    public Vector3f getMotion() {
        return this.motion;
    }

    public boolean isOnGround() {
        return this.onGround;
    }
}
