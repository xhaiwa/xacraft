package fr.xacraft.structure;

import fr.xacraft.block.Block;
import fr.xacraft.block.BlockType;
import java.util.Random;

public class Tree {
    public static void generate(Block[][][] block, int worldX, int worldY, int worldZ) {
        Random rand = new Random();
        int height = 4 + rand.nextInt(3);
        int maxX = block.length;
        int maxY = block[0].length;
        int maxZ = block[0][0].length;

        for (int y = 0; y < height; y++) {
            placeBlock(block, worldX, worldY + y, worldZ, BlockType.OAK_LOG, maxX, maxY, maxZ);
        }

        int topLog = worldY + height - 1;

        placeLeaf(block, worldX, topLog + 2, worldZ, maxX, maxY, maxZ);

        for (int dx = -1; dx <= 1; dx++)
            for (int dz = -1; dz <= 1; dz++)
                placeLeaf(block, worldX + dx, topLog + 1, worldZ + dz, maxX, maxY, maxZ);

        for (int layer = 0; layer >= -1; layer--) {
            int by = topLog + layer;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
                    if (dx == 0 && dz == 0) continue;
                    placeLeaf(block, worldX + dx, by, worldZ + dz, maxX, maxY, maxZ);
                }
            }
        }

        int by = topLog - 2;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                boolean isCoin = Math.abs(dx) == 1 && Math.abs(dz) == 1;
                if (isCoin && rand.nextInt(4) != 0) continue;
                placeLeaf(block, worldX + dx, by, worldZ + dz, maxX, maxY, maxZ);
            }
        }
    }

    private static void placeLeaf(Block[][][] block, int x, int y, int z,
                                  int maxX, int maxY, int maxZ) {
        if (x >= 0 && x < maxX && y >= 0 && y < maxY && z >= 0 && z < maxZ)
            if (block[x][y][z] == null)
                block[x][y][z] = new Block(BlockType.OAK_LEAVES);
    }

    private static void placeBlock(Block[][][] block, int x, int y, int z,
                                   BlockType type, int maxX, int maxY, int maxZ) {
        if (x >= 0 && x < maxX && y >= 0 && y < maxY && z >= 0 && z < maxZ)
            block[x][y][z] = new Block(type);
    }
}
