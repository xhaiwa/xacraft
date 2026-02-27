package fr.xacraft.world;

import fr.xacraft.block.Block;
import fr.xacraft.block.BlockType;
import fr.xacraft.block.Blocks;

public enum BiomeType {
    OCEAN(Blocks.WATER, Blocks.STONE, 45),
    BEACH(Blocks.SAND, Blocks.STONE, 64),
    PLAINS(Blocks.GRASS, Blocks.DIRT, 67),
    FOREST(Blocks.GRASS, Blocks.DIRT, 72),
    DESERT(Blocks.SAND, Blocks.STONE, 68),
    MOUNTAINS(Blocks.STONE, Blocks.STONE, 100),
    SNOW_MOUNTAINS(Blocks.SNOW, Blocks.STONE, 120);

    private Block topBlock;
    private Block fillerBlock;
    private int baseHeight;

    BiomeType(Block topBlock, Block fillerBlock, int baseHeight) {
        this.topBlock = topBlock;
        this.fillerBlock = fillerBlock;
        this.baseHeight = baseHeight;
    }

    public Block getTopBlock() {
        return topBlock;
    }

    public Block getFillerBlock() {
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
