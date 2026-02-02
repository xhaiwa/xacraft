package fr.xacraft.block;

import fr.xacraft.world.Location;

public class Block {
    private Location location;
    protected BlockType blockType;

    public Block(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
