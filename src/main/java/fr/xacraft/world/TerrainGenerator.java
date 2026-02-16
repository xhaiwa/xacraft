package fr.xacraft.world;

import fr.xacraft.block.Block;
import fr.xacraft.block.BlockType;

public class TerrainGenerator {
    private PerlinNoise continentNoise;
    private PerlinNoise baseNoise;
    private PerlinNoise detailNoise1;
    private PerlinNoise detailNoise2;
    private PerlinNoise mountainNoise;
    private PerlinNoise ridgeNoise;
    private PerlinNoise peaksNoise;
    private PerlinNoise temperatureNoise;
    private PerlinNoise humidityNoise;
    private VoronoiNoise biomeVoronoi;

    private long seed;

    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.continentNoise = new PerlinNoise(seed);
        this.baseNoise = new PerlinNoise(seed + 1000);
        this.detailNoise1 = new PerlinNoise(seed + 2000);
        this.detailNoise2 = new PerlinNoise(seed + 3000);
        this.mountainNoise = new PerlinNoise(seed + 4000);
        this.ridgeNoise = new PerlinNoise(seed + 5000);
        this.peaksNoise = new PerlinNoise(seed + 6000);
        this.temperatureNoise = new PerlinNoise(seed + 7000);
        this.humidityNoise = new PerlinNoise(seed + 8000);
        this.biomeVoronoi = new VoronoiNoise(seed + 9000);
    }

    public void generateChunk(Block[][][] blocks, int chunkX, int chunkZ) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                float continent = continentNoise.octavePerlin(
                        worldX * 0.0002f, worldZ * 0.0002f, 4, 0.6f);
                continent = (continent + 1.0f) * 0.5f;

                float base = baseNoise.octavePerlin(
                        worldX * 0.004f, worldZ * 0.004f, 6, 0.6f);
                base = (base + 1.0f) * 0.5f;

                float detail1 = detailNoise1.octavePerlin(
                        worldX * 0.01f, worldZ * 0.01f, 5, 0.55f);
                detail1 = (detail1 + 1.0f) * 0.5f;

                float detail2 = detailNoise2.octavePerlin(
                        worldX * 0.03f, worldZ * 0.03f, 4, 0.5f);
                detail2 = (detail2 + 1.0f) * 0.5f;

                float mountain = mountainNoise.octavePerlin(
                        worldX * 0.003f, worldZ * 0.003f, 7, 0.7f);
                mountain = (mountain + 1.0f) * 0.5f;
                mountain = (float) Math.pow(mountain, 4.0);

                float ridge = ridgeNoise.octavePerlin(
                        worldX * 0.008f, worldZ * 0.008f, 6, 0.6f);
                ridge = 1.0f - Math.abs(ridge);
                ridge = (float) Math.pow(ridge, 3.0);

                float peaks = peaksNoise.octavePerlin(
                        worldX * 0.02f, worldZ * 0.02f, 5, 0.55f);
                peaks = 1.0f - Math.abs(peaks);
                peaks = (float) Math.pow(peaks, 4.0);

                float heightValue = base * 0.5f + detail1 * 0.3f + detail2 * 0.2f;

                if (mountain > 0.2f) {
                    float mountainInfluence = (mountain - 0.2f) / 0.8f;

                    heightValue = heightValue * (1.0f - mountainInfluence * 0.8f) +
                            mountain * mountainInfluence * 1.2f +
                            ridge * mountainInfluence * 0.6f +
                            peaks * mountainInfluence * 0.4f;
                }

                heightValue = heightValue * 0.6f + continent * 0.4f;

                int height = (int)(heightValue * 240.0f) - 54;



                height = Math.max(30, Math.min(200, height));

                float temperature = getVoronoiValue(worldX, worldZ, temperatureNoise, 0.0003f);
                float humidity = getVoronoiValue(worldX, worldZ, humidityNoise, 0.0004f);

                BiomeType biome = getBiome(temperature, humidity, height);

                generateColumn(blocks, x, z, height, biome);
            }
        }
    }

    private float getVoronoiValue(int worldX, int worldZ, PerlinNoise noise, float scale) {
        VoronoiNoise.VoronoiResult voronoi = biomeVoronoi.getVoronoi(worldX * scale, worldZ * scale);

        float perlin = noise.octavePerlin(worldX * scale, worldZ * scale, 3, 0.5f);
        perlin = (perlin + 1.0f) * 0.5f;

        return voronoi.value * 0.7f + perlin * 0.3f;
    }

    private void generateColumn(Block[][][] blocks, int x, int z, int height, BiomeType biome) {
        for (int y = 0; y < 255; y++) {
            if (y == 0) {
                blocks[x][y][z] = new Block(BlockType.STONE);
            } else if (y < height - 4) {
                blocks[x][y][z] = new Block(BlockType.STONE);
            } else if (y < height) {
                blocks[x][y][z] = new Block(biome.getFillerBlock());
            } else if (y == height) {
                blocks[x][y][z] = new Block(biome.getTopBlock());
            } else if (y <= 62 && height <= 62) {
                blocks[x][y][z] = new Block(BlockType.WATER);
            } else {
                blocks[x][y][z] = new Block(BlockType.AIR);
            }
        }
    }

    private BiomeType getBiome(float temperature, float humidity, float height) {
        if (height <= 58) return BiomeType.OCEAN;
        if (height <= 64) return BiomeType.BEACH;

        if (height > 120) {
            if (temperature < 0.25f) return BiomeType.SNOW_MOUNTAINS;
            return BiomeType.MOUNTAINS;
        }

        if (height > 100) {
            if (temperature < 0.35f) return BiomeType.SNOW_MOUNTAINS;
            return BiomeType.MOUNTAINS;
        }

        if (height > 85) {
            if (temperature < 0.3f) return BiomeType.SNOW_MOUNTAINS;
            if (humidity < 0.4f) return BiomeType.MOUNTAINS;
            return BiomeType.FOREST;
        }

        if (temperature < 0.25f) return BiomeType.SNOW_MOUNTAINS;
        if (temperature > 0.75f && humidity < 0.25f) return BiomeType.DESERT;
        if (temperature > 0.65f && humidity < 0.45f) return BiomeType.PLAINS;
        if (temperature > 0.7f && humidity > 0.7f) return BiomeType.FOREST;
        if (humidity > 0.6f && temperature > 0.35f) return BiomeType.FOREST;

        return BiomeType.PLAINS;
    }
}

/*

package fr.xacraft.world;

import fr.xacraft.block.Block;
import fr.xacraft.block.BlockType;

public class TerrainGenerator {
    private PerlinNoise continentNoise;
    private PerlinNoise baseNoise;
    private PerlinNoise detailNoise1;
    private PerlinNoise detailNoise2;
    private PerlinNoise mountainNoise;
    private PerlinNoise ridgeNoise;
    private PerlinNoise peaksNoise;
    private PerlinNoise temperatureNoise;
    private PerlinNoise humidityNoise;
    private VoronoiNoise biomeVoronoi;

    private PerlinNoise caveNoise;
    private PerlinNoise spaghettiCaveNoise;
    private PerlinNoise noodleCaveNoise;
    private PerlinNoise densityNoise;

    private long seed;

    public TerrainGenerator(long seed) {
        this.seed = seed;
        this.continentNoise = new PerlinNoise(seed);
        this.baseNoise = new PerlinNoise(seed + 1000);
        this.detailNoise1 = new PerlinNoise(seed + 2000);
        this.detailNoise2 = new PerlinNoise(seed + 3000);
        this.mountainNoise = new PerlinNoise(seed + 4000);
        this.ridgeNoise = new PerlinNoise(seed + 5000);
        this.peaksNoise = new PerlinNoise(seed + 6000);
        this.temperatureNoise = new PerlinNoise(seed + 7000);
        this.humidityNoise = new PerlinNoise(seed + 8000);
        this.biomeVoronoi = new VoronoiNoise(seed + 9000);

        this.caveNoise = new PerlinNoise(seed + 10000);
        this.spaghettiCaveNoise = new PerlinNoise(seed + 11000);
        this.noodleCaveNoise = new PerlinNoise(seed + 12000);
        this.densityNoise = new PerlinNoise(seed + 13000);
    }

    public void generateChunk(Block[][][] blocks, int chunkX, int chunkZ) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkX * 16 + x;
                int worldZ = chunkZ * 16 + z;

                float continent = continentNoise.octavePerlin(
                        worldX * 0.0002f, worldZ * 0.0002f, 4, 0.6f);
                continent = (continent + 1.0f) * 0.5f;

                float base = baseNoise.octavePerlin(
                        worldX * 0.004f, worldZ * 0.004f, 6, 0.6f);
                base = (base + 1.0f) * 0.5f;

                float detail1 = detailNoise1.octavePerlin(
                        worldX * 0.01f, worldZ * 0.01f, 5, 0.55f);
                detail1 = (detail1 + 1.0f) * 0.5f;

                float detail2 = detailNoise2.octavePerlin(
                        worldX * 0.03f, worldZ * 0.03f, 4, 0.5f);
                detail2 = (detail2 + 1.0f) * 0.5f;

                float mountain = mountainNoise.octavePerlin(
                        worldX * 0.003f, worldZ * 0.003f, 7, 0.7f);
                mountain = (mountain + 1.0f) * 0.5f;
                mountain = (float) Math.pow(mountain, 4.0);

                float ridge = ridgeNoise.octavePerlin(
                        worldX * 0.008f, worldZ * 0.008f, 6, 0.6f);
                ridge = 1.0f - Math.abs(ridge);
                ridge = (float) Math.pow(ridge, 3.0);

                float peaks = peaksNoise.octavePerlin(
                        worldX * 0.02f, worldZ * 0.02f, 5, 0.55f);
                peaks = 1.0f - Math.abs(peaks);
                peaks = (float) Math.pow(peaks, 4.0);

                float heightValue = base * 0.5f + detail1 * 0.3f + detail2 * 0.2f;

                if (mountain > 0.2f) {
                    float mountainInfluence = (mountain - 0.2f) / 0.8f;
                    heightValue = heightValue * (1.0f - mountainInfluence * 0.8f) +
                            mountain * mountainInfluence * 1.2f +
                            ridge * mountainInfluence * 0.6f +
                            peaks * mountainInfluence * 0.4f;
                }

                heightValue = heightValue * 0.6f + continent * 0.4f;
                int height = (int)(heightValue * 240.0f) - 54;
                height = Math.max(30, Math.min(200, height));

                float temperature = getVoronoiValue(worldX, worldZ, temperatureNoise, 0.0003f);
                float humidity = getVoronoiValue(worldX, worldZ, humidityNoise, 0.0004f);
                BiomeType biome = getBiome(temperature, humidity, height);

                generateColumn3D(blocks, x, z, worldX, worldZ, height, biome);
            }
        }
    }

    private void generateColumn3D(Block[][][] blocks, int x, int z, int worldX, int worldZ,
                                  int surfaceHeight, BiomeType biome) {

        for (int y = 0; y < 255; y++) {
            if (y > surfaceHeight + 15) {
                blocks[x][y][z] = new Block(BlockType.AIR);
                continue;
            }

            float density = getDensity(worldX, y, worldZ, surfaceHeight);

            float caveValue = getCaveValue(worldX, y, worldZ);

            if (caveValue > 0.5f && y > 5 && y < surfaceHeight - 5 && y > 64) {
                density = -1.0f;
            }

            if (y == 0) {
                blocks[x][y][z] = new Block(BlockType.STONE);
            } else if (density > 0) {
                blocks[x][y][z] = getSolidBlock(y, surfaceHeight, biome);
            } else {
                if (y <= 62) {
                    blocks[x][y][z] = new Block(BlockType.WATER);
                } else {
                    blocks[x][y][z] = new Block(BlockType.AIR);
                }
            }
        }
    }

    private float getDensity(int worldX, int y, int worldZ, int surfaceHeight) {
        float squash = 0.3f;

        float density3D = densityNoise.octavePerlin3D(
                worldX * 0.01f,
                y * 0.01f * squash,
                worldZ * 0.01f,
                4, 0.5f);

        float distanceFromSurface = y - surfaceHeight;
        float heightGradient;

        if (distanceFromSurface > 0) {
            heightGradient = -distanceFromSurface * 0.15f;
        } else {
            heightGradient = -distanceFromSurface * 0.08f;
        }

        float finalDensity = heightGradient + density3D * 0.15f;

        if (y < 10) {
            finalDensity += (10 - y) * 0.3f;
        }

        return finalDensity;
    }

    private float getCaveValue(int worldX, int y, int worldZ) {
        float cheese = caveNoise.octavePerlin3D(
                worldX * 0.008f,
                y * 0.012f,
                worldZ * 0.008f,
                3, 0.5f);
        cheese = Math.abs(cheese);

        float spaghetti1 = spaghettiCaveNoise.octavePerlin3D(
                worldX * 0.015f,
                y * 0.02f,
                worldZ * 0.015f,
                3, 0.5f);

        float spaghetti2 = spaghettiCaveNoise.octavePerlin3D(
                worldX * 0.015f + 1000,
                y * 0.02f + 1000,
                worldZ * 0.015f + 1000,
                3, 0.5f);

        float spaghetti = Math.max(Math.abs(spaghetti1), Math.abs(spaghetti2));

        float noodle = noodleCaveNoise.octavePerlin3D(
                worldX * 0.025f,
                y * 0.03f,
                worldZ * 0.025f,
                2, 0.4f);
        noodle = Math.abs(noodle);

        float caveValue = 0;

        if (y > 20 && y < 120 && cheese < 0.12f) {
            caveValue = 1.0f;
        }

        if (spaghetti < 0.08f) {
            caveValue = Math.max(caveValue, 0.8f);
        }

        if (noodle < 0.05f) {
            caveValue = Math.max(caveValue, 0.6f);
        }

        return caveValue;
    }

    private Block getSolidBlock(int y, int surfaceHeight, BiomeType biome) {
        int depthFromSurface = surfaceHeight - y;

        if (y < 5) {
            return new Block(BlockType.STONE);
        }

        if (depthFromSurface <= 0) {
            return new Block(biome.getTopBlock());
        }

        if (depthFromSurface <= 4) {
            return new Block(biome.getFillerBlock());
        }

        return new Block(BlockType.STONE);
    }

    private float getVoronoiValue(int worldX, int worldZ, PerlinNoise noise, float scale) {
        VoronoiNoise.VoronoiResult voronoi = biomeVoronoi.getVoronoi(worldX * scale, worldZ * scale);
        float perlin = noise.octavePerlin(worldX * scale, worldZ * scale, 3, 0.5f);
        perlin = (perlin + 1.0f) * 0.5f;
        return voronoi.value * 0.7f + perlin * 0.3f;
    }

    private BiomeType getBiome(float temperature, float humidity, float height) {
        if (height <= 58) return BiomeType.OCEAN;
        if (height <= 64) return BiomeType.BEACH;

        if (height > 120) {
            if (temperature < 0.25f) return BiomeType.SNOW_MOUNTAINS;
            return BiomeType.MOUNTAINS;
        }

        if (height > 100) {
            if (temperature < 0.35f) return BiomeType.SNOW_MOUNTAINS;
            return BiomeType.MOUNTAINS;
        }

        if (height > 85) {
            if (temperature < 0.3f) return BiomeType.SNOW_MOUNTAINS;
            if (humidity < 0.4f) return BiomeType.MOUNTAINS;
            return BiomeType.FOREST;
        }

        if (temperature < 0.25f) return BiomeType.SNOW_MOUNTAINS;
        if (temperature > 0.75f && humidity < 0.25f) return BiomeType.DESERT;
        if (temperature > 0.65f && humidity < 0.45f) return BiomeType.PLAINS;
        if (temperature > 0.7f && humidity > 0.7f) return BiomeType.FOREST;
        if (humidity > 0.6f && temperature > 0.35f) return BiomeType.FOREST;

        return BiomeType.PLAINS;
    }
}
*/