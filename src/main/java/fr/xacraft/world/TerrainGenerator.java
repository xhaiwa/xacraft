package fr.xacraft.world;

import fr.xacraft.block.Block;
import fr.xacraft.block.BlockType;
import fr.xacraft.structure.Tree;

public class TerrainGenerator {
    private PerlinNoise continentNoise;
    private PerlinNoise erosionNoise;
    private PerlinNoise baseNoise;
    private PerlinNoise detailNoise1;
    private PerlinNoise detailNoise2;
    private PerlinNoise mountainNoise;
    private PerlinNoise ridgeNoise;
    private PerlinNoise ridgeNoise2;
    private PerlinNoise peaksNoise;
    private PerlinNoise terraceNoise;

    private PerlinNoise temperatureNoise;
    private PerlinNoise humidityNoise;
    private VoronoiNoise biomeVoronoi;

    private PerlinNoise densityNoise;
    private PerlinNoise spaghettiCaveNoise1;
    private PerlinNoise spaghettiCaveNoise2;
    private PerlinNoise noodleCaveNoise;
    private PerlinNoise cheeseCaveNoise;
    private PerlinNoise caveMaskNoise;

    private long seed;

    private static final int SEA_LEVEL = 72;

    public TerrainGenerator(long seed) {
        this.seed = seed;

        NoiseUtils.image(seed);

        this.continentNoise  = new PerlinNoise(seed);
        this.erosionNoise    = new PerlinNoise(seed + 500);
        this.baseNoise       = new PerlinNoise(seed + 1000);
        this.detailNoise1    = new PerlinNoise(seed + 2000);
        this.detailNoise2    = new PerlinNoise(seed + 3000);
        this.mountainNoise   = new PerlinNoise(seed + 4000);
        this.ridgeNoise      = new PerlinNoise(seed + 5000);
        this.ridgeNoise2     = new PerlinNoise(seed + 5500);
        this.peaksNoise      = new PerlinNoise(seed + 6000);
        this.terraceNoise    = new PerlinNoise(seed + 6500);

        this.temperatureNoise = new PerlinNoise(seed + 7000);
        this.humidityNoise    = new PerlinNoise(seed + 8000);
        this.biomeVoronoi     = new VoronoiNoise(seed + 9000);

        this.densityNoise        = new PerlinNoise(seed + 10000);
        this.spaghettiCaveNoise1 = new PerlinNoise(seed + 12000);
        this.spaghettiCaveNoise2 = new PerlinNoise(seed + 13000);
        this.noodleCaveNoise     = new PerlinNoise(seed + 14000);
        this.cheeseCaveNoise     = new PerlinNoise(seed + 15000);
        this.caveMaskNoise       = new PerlinNoise(seed + 16000);
    }

    public void generateChunk(Block[][][] blocks, int chunkX, int chunkZ) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                int surfaceHeight = computeSurfaceHeight(worldX, worldZ);

                float temperature = getVoronoiValue(worldX, worldZ, temperatureNoise, 0.003f);
                float humidity    = getVoronoiValue(worldX, worldZ, humidityNoise, 0.004f);
                BiomeType biome   = getBiome(temperature, humidity, surfaceHeight);

                generateColumn3D(blocks, x, z, worldX, worldZ, surfaceHeight, biome);
            }
        }
    }

    private int computeSurfaceHeight(int worldX, int worldZ) {
        float continent = sampleNormalized(continentNoise, worldX, worldZ, 0.0045f, 9, 0.55f);

        float erosion = sampleNormalized(erosionNoise, worldX, worldZ, 0.009f, 9, 0.5f);

        float base = sampleNormalized(baseNoise, worldX, worldZ, 0.003f, 6, 0.55f);

        float detail1 = sampleNormalized(detailNoise1, worldX, worldZ, 0.012f, 5, 0.5f);
        float detail2 = sampleNormalized(detailNoise2, worldX, worldZ, 0.035f, 4, 0.45f);

        float mountainRaw = sampleNormalized(mountainNoise, worldX, worldZ, 0.002f, 6, 0.65f);
        float mountainFactor = (float) Math.pow(mountainRaw, 2.5);

        float ridge1 = ridgeNoise.octavePerlin(worldX * 0.005f, worldZ * 0.005f, 6, 0.6f);
        ridge1 = 1.0f - Math.abs(ridge1);
        ridge1 = (float) Math.pow(ridge1, 2.0);

        float ridge2 = ridgeNoise2.octavePerlin(worldX * 0.007f, worldZ * 0.007f, 5, 0.55f);
        ridge2 = 1.0f - Math.abs(ridge2);
        ridge2 = (float) Math.pow(ridge2, 2.5);

        float ridgeCombined = Math.max(ridge1 * 0.7f, ridge2 * 0.6f) + ridge1 * ridge2 * 0.8f;

        float peaks = peaksNoise.octavePerlin(worldX * 0.015f, worldZ * 0.015f, 5, 0.5f);
        peaks = 1.0f - Math.abs(peaks);
        peaks = (float) Math.pow(peaks, 3.0);

        float terrace = sampleNormalized(terraceNoise, worldX, worldZ, 0.006f, 3, 0.5f);

        float heightValue = base * 0.45f + detail1 * 0.3f + detail2 * 0.15f + continent * 0.1f;

        float erosionInfluence = smoothstep(erosion, 0.4f, 0.7f);

        if (mountainFactor > 0.08f) {
            float mInfluence = smoothstep(mountainFactor, 0.08f, 0.6f) * erosionInfluence;

            float mountainHeight = ridgeCombined * 0.5f + peaks * 0.3f + mountainFactor * 0.2f;

            float terraceStrength = mInfluence * 0.15f;
            float terraced = applyTerrace(mountainHeight, 6.0f, terrace * terraceStrength);

            heightValue = lerp(heightValue, terraced + heightValue * 0.3f, mInfluence * 0.85f);
        }

        heightValue = heightValue * 0.65f + continent * 0.35f;

        int height = (int) (heightValue * 200.0f) - 20;

        return height;
    }

    private void generateColumn3D(Block[][][] blocks, int x, int z, int worldX, int worldZ,
                                  int surfaceHeight, BiomeType biome) {

        boolean[] solid = new boolean[255];
        boolean[] cave = new boolean[255];

        for (int y = 0; y < 255; y++) {
            if (y > surfaceHeight + 20) {
                solid[y] = false;
                cave[y] = false;
                continue;
            }

            float density = getDensity(worldX, y, worldZ, surfaceHeight);
            boolean isCave = isCaveAt(worldX, y, worldZ, surfaceHeight);

            if (isCave) {
                density = -1.0f;
            }

            solid[y] = (y == 0) || (density > 0);
            cave[y] = isCave;
        }

        for (int y = 0; y < 255; y++) {
            if (solid[y]) {
                blocks[x][y][z] = cave[y] ? new Block(BlockType.AIR) : new Block(BlockType.STONE);
                if (blocks[x][y][z].getBlockType() == BlockType.AIR) continue;
                if (y == surfaceHeight - 1) {
                    blocks[x][y][z] = new Block(biome.getTopBlock());
                    if ((int) (Math.random() * 500) == 10
                            && (biome == BiomeType.FOREST || biome == BiomeType.PLAINS)) {
                        Tree.generate(blocks, x, y + 1, z);
                        blocks[x][y][z] = new Block(BlockType.DIRT);
                    }
                } else if (y < surfaceHeight - 1 && y >= surfaceHeight - 5 - 1) {
                    blocks[x][y][z] = new Block(biome.getFillerBlock());
                }
            } else {
                if (y <= SEA_LEVEL && !cave[y] && surfaceHeight - 1 <= SEA_LEVEL) {
                    blocks[x][y][z] = new Block(BlockType.WATER);
                } else {
                    if (blocks[x][y][z] == null)
                        blocks[x][y][z] = new Block(BlockType.AIR);
                }
            }
        }
    }

    private float getDensity(int worldX, int y, int worldZ, int surfaceHeight) {
        float density3D = densityNoise.octavePerlin3D(
                worldX * 0.008f,
                y * 0.006f,
                worldZ * 0.008f,
                4, 0.5f);

        float distFromSurface = y - surfaceHeight;
        float heightGradient;

        if (distFromSurface > 0) {
            heightGradient = -distFromSurface * 0.25f;
        } else if (distFromSurface > -5) {
            heightGradient = -distFromSurface * 0.35f;
        } else if (distFromSurface > -20) {
            heightGradient = 1.75f + (-distFromSurface - 5) * 0.06f;
        } else {
            heightGradient = 2.65f + (-distFromSurface - 20) * 0.03f;
        }

        float depthRatio = Math.max(0, Math.min(1, -distFromSurface / 25.0f));
        float noiseInfluence = depthRatio * 0.3f;

        float finalDensity = heightGradient + density3D * noiseInfluence;

        if (y < 5) {
            finalDensity += (5 - y) * 0.5f;
        }

        return finalDensity;
    }


    private boolean isCaveAt(int worldX, int y, int worldZ, int surfaceHeight) {
        if (y == 0) return false;
        float caveMask = sampleNormalized(caveMaskNoise, worldX, worldZ, 0.0008f, 3, 0.5f);
        if (caveMask < 0.4f) return false;

        float spaghetti1 = spaghettiCaveNoise1.octavePerlin3D(
                worldX * 0.015f,
                y * 0.02f,
                worldZ * 0.015f,
                3, 0.5f);

        float spaghetti2 = spaghettiCaveNoise2.octavePerlin3D(
                worldX * 0.015f,
                y * 0.02f,
                worldZ * 0.015f,
                3, 0.5f);

        boolean spaghettiCave = (Math.abs(spaghetti1) < 0.03f)
                && (Math.abs(spaghetti2) < 0.03f);

        float noodle1 = noodleCaveNoise.octavePerlin3D(
                worldX * 0.025f,
                y * 0.03f,
                worldZ * 0.025f,
                2, 0.4f);

        float noodle2 = noodleCaveNoise.octavePerlin3D(
                worldX * 0.025f + 500,
                y * 0.03f + 500,
                worldZ * 0.025f + 500,
                2, 0.4f);

        boolean noodleCave = (Math.abs(noodle1) < 0.02f)
                && (Math.abs(noodle2) < 0.02f);

        boolean cheeseCave = false;
        if (y > 10 && y < surfaceHeight - 12 && caveMask > 0.6f) {
            float cheese = cheeseCaveNoise.octavePerlin3D(
                    worldX * 0.008f,
                    y * 0.012f,
                    worldZ * 0.008f,
                    3, 0.5f);
            cheese = Math.abs(cheese);
            cheeseCave = (cheese < 0.04f);
        }

        return spaghettiCave || noodleCave || cheeseCave;
    }

    private BiomeType getBiome(float temperature, float humidity, int height) {
        if (height <= 55) return BiomeType.OCEAN;
        if (height <= SEA_LEVEL + 2) return BiomeType.BEACH;

        if (height > 130) {
            return (temperature < 0.35f) ? BiomeType.SNOW_MOUNTAINS : BiomeType.MOUNTAINS;
        }

        if (height > 105) {
            if (temperature < 0.3f) return BiomeType.SNOW_MOUNTAINS;
            return BiomeType.MOUNTAINS;
        }

        if (height > 88) {
            if (temperature < 0.25f) return BiomeType.SNOW_MOUNTAINS;
            if (humidity < 0.45f) return BiomeType.MOUNTAINS;
            return BiomeType.FOREST;
        }

        if (temperature < 0.2f) return BiomeType.SNOW_MOUNTAINS;
        if (temperature > 0.72f && humidity < 0.28f) return BiomeType.DESERT;
        if (humidity > 0.55f && temperature > 0.3f) return BiomeType.FOREST;
        if (temperature > 0.6f && humidity < 0.5f) return BiomeType.PLAINS;

        return BiomeType.PLAINS;
    }

    private float getVoronoiValue(int worldX, int worldZ, PerlinNoise noise, float scale) {
        VoronoiNoise.VoronoiResult voronoi = biomeVoronoi.getVoronoi(worldX * scale, worldZ * scale);
        float perlin = noise.octavePerlin(worldX * scale, worldZ * scale, 3, 0.5f);
        perlin = (perlin + 1.0f) * 0.5f;
        return voronoi.value * 0.7f + perlin * 0.3f;
    }

    private float sampleNormalized(PerlinNoise noise, int wx, int wz,
                                   float scale, int octaves, float persistence) {
        float v = noise.octavePerlin(wx * scale, wz * scale, octaves, persistence);
        return (v + 1.0f) * 0.5f;
    }

    private float smoothstep(float value, float edge0, float edge1) {
        float t = Math.max(0, Math.min(1, (value - edge0) / (edge1 - edge0)));
        return t * t * (3.0f - 2.0f * t);
    }

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private float applyTerrace(float height, float steps, float strength) {
        if (strength <= 0.001f) return height;
        float terraced = (float) Math.round(height * steps) / steps;
        return lerp(height, terraced, strength);
    }
}
