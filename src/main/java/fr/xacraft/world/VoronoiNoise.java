package fr.xacraft.world;

public class VoronoiNoise {
    private final long seed;

    public VoronoiNoise(long seed) {
        this.seed = seed;
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

        // Rayon étendu à 2 pour éviter les artefacts aux bords
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
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

        // Normalisation approximative (distance max ~= 1.5 en espace cellule)
        return new VoronoiResult(closestValue, Math.min(minDist / 1.5f, 1.0f));
    }

    private float[] getRandomPoint(int cellX, int cellZ) {
        // Hash robuste style xxHash / Squirrel3 — évite les collisions
        long hash = seed;
        hash ^= (long) cellX * 0x9E3779B97F4A7C15L;
        hash ^= (long) cellZ * 0x6C62272E07BB0142L;
        hash ^= (hash >>> 30);
        hash *= 0xBF58476D1CE4E5B9L;
        hash ^= (hash >>> 27);
        hash *= 0x94D049BB133111EBL;
        hash ^= (hash >>> 31);

        // Extraction de 3 floats [0, 1) depuis le hash sans instancier Random
        float offsetX = (float) ((hash & 0xFFFFFFL) / (double) 0x1000000L);
        float offsetZ = (float) (((hash >> 24) & 0xFFFFFFL) / (double) 0x1000000L);
        float value   = (float) (((hash >> 48) & 0xFFFFL)   / (double) 0x10000L);

        return new float[]{ offsetX, offsetZ, value };
    }

    private float distance(float x1, float z1, float x2, float z2) {
        float dx = x2 - x1;
        float dz = z2 - z1;
        return (float) Math.sqrt(dx * dx + dz * dz);
    }
}
