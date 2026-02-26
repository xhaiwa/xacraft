package fr.xacraft.item;

import fr.xacraft.block.BlockType;

public class Item {

    private String id;
    private int maxStackSize;

    public Item(String id, int maxStackSize) {
        this.id = id;
        this.maxStackSize = maxStackSize;
    }

    public String getId() {
        return id;
    }

    public int getMaxStackSize() {
        return maxStackSize;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id='" + id + '\'' +
                ", maxStackSize=" + maxStackSize +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Item item = (Item) o;

        return id.equals(item.id);
    }

    public boolean isBlockItem() {
        return switch (this.id) {
            case "xacraft:stone", "xacraft:grass", "xacraft:dirt", "xacraft:cobblestone",
                 "xacraft:sand", "xacraft:snow", "xacraft:oak_log", "xacraft:oak_leaves" -> true;
            default -> false;
        };
    }

    public BlockType getBlockType() {
        try {
            return switch (this.id) {
                case "xacraft:stone" -> BlockType.STONE;
                case "xacraft:grass" -> BlockType.GRASS;
                case "xacraft:dirt" -> BlockType.DIRT;
                case "xacraft:cobblestone" -> BlockType.COBBLESTONE;
                case "xacraft:sand" -> BlockType.SAND;
                case "xacraft:snow" -> BlockType.SNOW;
                case "xacraft:oak_log" -> BlockType.OAK_LOG;
                case "xacraft:oak_leaves" -> BlockType.OAK_LEAVES;
                default -> throw new IllegalArgumentException("Item is not a block item");
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
