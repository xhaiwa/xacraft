package fr.xacraft.block;

public enum BlockType {
    AIR(true, false, -1),
    STONE(false, true, -1);

    private boolean transparent;
    private boolean solid;
    private int blockId;

    BlockType(boolean transparent, boolean solid, int blockId) {
        this.transparent = transparent;
        this.solid = solid;
        this.blockId = blockId;
    }

    public boolean isTransparent() {
        return this.transparent;
    }

    public boolean isSolid() {
        return this.solid;
    }

    public int getBlockId() {
        return this.blockId;
    }
}
