package fr.xacraft.world;

import fr.xacraft.block.BlockType;

public enum BiomeType {
    OCEAN(BlockType.WATER, BlockType.STONE, 45),
    BEACH(BlockType.SAND, BlockType.STONE, 64),
    PLAINS(BlockType.GRASS, BlockType.DIRT, 67),
    FOREST(BlockType.GRASS, BlockType.DIRT, 72),
    DESERT(BlockType.SAND, BlockType.STONE, 68),
    MOUNTAINS(BlockType.STONE, BlockType.STONE, 100),
    SNOW_MOUNTAINS(BlockType.SNOW, BlockType.STONE, 120);

    private BlockType topBlock;
    private BlockType fillerBlock;
    private int baseHeight;

    BiomeType(BlockType topBlock, BlockType fillerBlock, int baseHeight) {
        this.topBlock = topBlock;
        this.fillerBlock = fillerBlock;
        this.baseHeight = baseHeight;
    }

    public BlockType getTopBlock() {
        return topBlock;
    }

    public BlockType getFillerBlock() {
        return fillerBlock;
    }

    public int getBaseHeight() {
        return baseHeight;
    }

    public static BiomeType getBiome(float temperature, float humidity, float height) {
        if (height < 0.35f) {
            return OCEAN;
        }

        if (height < 0.4f) {
            return BEACH;
        }

        if (height > 0.75f && temperature < 0.3f) {
            return SNOW_MOUNTAINS;
        }

        if (height > 0.7f) {
            return MOUNTAINS;
        }

        if (temperature > 0.7f && humidity < 0.3f) {
            return DESERT;
        }

        if (humidity > 0.6f) {
            return FOREST;
        }

        return PLAINS;
    }
}
