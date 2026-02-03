package fr.xacraft.block;

import fr.xacraft.world.Location;

public class Block {
    private Location location;
    protected BlockType blockType;

    public Block(Location location, BlockType blockType) {
        this.location = location;
        this.blockType = blockType;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public BlockType getBlockType() {
        return this.blockType;
    }
}
