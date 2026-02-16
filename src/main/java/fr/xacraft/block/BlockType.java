package fr.xacraft.block;

public enum BlockType {
    AIR(true, false, -1, -1, -1, -1, -1, -1),
    STONE(false, true, 1, 1, 1, 1, 1, 1),
    GRASS(false, true, 3, 2, 4, 4, 4, 4),
    DIRT(false, true, 2, 2, 2, 2, 2, 2),
    COBBLESTONE(false, true, 5, 5, 5, 5, 5, 5),
    WATER(true, false, 6, 6, 6, 6, 6, 6),
    SAND(false, true, 7, 7, 7, 7, 7, 7),
    SNOW(false, true, 8, 8, 8, 8, 8, 8);

    private boolean transparent;
    private boolean solid;
    private int topTexture;
    private int bottomTexture;
    private int northTexture;
    private int southTexture;
    private int eastTexture;
    private int westTexture;

    BlockType(boolean transparent, boolean solid,
              int topTexture, int bottomTexture,
              int northTexture, int southTexture,
              int eastTexture, int westTexture) {
        this.transparent = transparent;
        this.solid = solid;
        this.topTexture = topTexture;
        this.bottomTexture = bottomTexture;
        this.northTexture = northTexture;
        this.southTexture = southTexture;
        this.eastTexture = eastTexture;
        this.westTexture = westTexture;
    }

    public boolean isTransparent() {
        return this.transparent;
    }

    public boolean isSolid() {
        return this.solid;
    }

    public int getTopTexture() {
        return topTexture;
    }

    public int getBottomTexture() {
        return bottomTexture;
    }

    public int getNorthTexture() {
        return northTexture;
    }

    public int getSouthTexture() {
        return southTexture;
    }

    public int getEastTexture() {
        return eastTexture;
    }

    public int getWestTexture() {
        return westTexture;
    }
}
