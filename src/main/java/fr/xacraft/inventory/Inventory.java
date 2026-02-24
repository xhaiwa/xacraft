package fr.xacraft.inventory;

import fr.xacraft.item.ItemStack;

public class Inventory {
        private ItemStack[] slots;

        public Inventory(int size) {
            this.slots = new ItemStack[size];
        }

        public ItemStack getSlot(int index) {
            if (index < 0 || index >= slots.length) {
                throw new IndexOutOfBoundsException("Invalid inventory slot index: " + index);
            }
            return slots[index];
        }

        public void setSlot(int index, ItemStack itemStack) {
            if (index < 0 || index >= slots.length) {
                throw new IndexOutOfBoundsException("Invalid inventory slot index: " + index);
            }
            this.slots[index] = itemStack;
        }

        public int getSize() {
            return slots.length;
        }
}
