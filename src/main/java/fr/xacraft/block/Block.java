package fr.xacraft.block;

public class Block {
    protected BlockType blockType;

    public Block(BlockType blockType) {
        this.blockType = blockType;
    }

    public BlockType getBlockType() {
        return this.blockType;
    }

    public void setBlockType(BlockType blockType) {
        this.blockType = blockType;
    }
}
