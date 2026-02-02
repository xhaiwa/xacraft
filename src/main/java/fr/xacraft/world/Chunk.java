package fr.xacraft.world;

import fr.xacraft.block.Block;

public class Chunk {
    /**
     * Block [x][y][z]
     */
    private Block[][][] block;

    public Chunk(Location location) {
        this.block = new Block[16][255][16];
    }

    public Block[][][] getBlock() {
        return this.block;
    }
}
