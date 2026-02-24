package fr.xacraft.item;

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
}
