package fr.xacraft.item;

public class ItemStack {

    private Item item;
    private int count;

    public ItemStack(Item item, int count) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (count < 0 || count > item.getMaxStackSize()) {
            throw new IllegalArgumentException("Count must be between 0 and " + item.getMaxStackSize());
        }
        this.item = item;
        this.count = count;
    }

    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ItemStack add(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to add cannot be negative");
        }
        return new ItemStack(item, count + amount);
    }

    public ItemStack remove(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount to remove cannot be negative");
        }
        int newCount = count - amount;
        if (newCount < 0) {
            throw new IllegalArgumentException("Resulting count cannot be negative");
        }
        return new ItemStack(item, newCount);
    }

    @Override
    public String toString() {
        return "ItemStack{" +
                "item=" + item +
                ", count=" + count +
                '}';
    }
}
