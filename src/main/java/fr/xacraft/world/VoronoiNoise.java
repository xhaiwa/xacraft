package fr.xacraft.world;

import java.util.Random;

public class VoronoiNoise {
    private Random random;
    private long seed;

    public VoronoiNoise(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    public static class VoronoiResult {
        public float value;
        public float distance;

        public VoronoiResult(float value, float distance) {
            this.value = value;
            this.distance = distance;
        }
    }

    public VoronoiResult getVoronoi(float x, float z) {
        int cellX = (int) Math.floor(x);
        int cellZ = (int) Math.floor(z);

        float minDist = Float.MAX_VALUE;
        float closestValue = 0.0f;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int neighborX = cellX + dx;
                int neighborZ = cellZ + dz;

                float[] point = getRandomPoint(neighborX, neighborZ);
                float pointX = neighborX + point[0];
                float pointZ = neighborZ + point[1];

                float dist = distance(x, z, pointX, pointZ);

                if (dist < minDist) {
                    minDist = dist;
                    closestValue = point[2];
                }
            }
        }

        return new VoronoiResult(closestValue, minDist);
    }

    private float[] getRandomPoint(int cellX, int cellZ) {
        long hash = seed;
        hash = hash * 31 + cellX;
        hash = hash * 31 + cellZ;

        Random cellRandom = new Random(hash);

        float offsetX = cellRandom.nextFloat();
        float offsetZ = cellRandom.nextFloat();
        float value = cellRandom.nextFloat();

        return new float[] { offsetX, offsetZ, value };
    }

    private float distance(float x1, float z1, float x2, float z2) {
        float dx = x2 - x1;
        float dz = z2 - z1;
        return (float) Math.sqrt(dx * dx + dz * dz);
    }
}
