package fr.xacraft.entity;

import fr.xacraft.item.ItemStack;
import fr.xacraft.world.World;
import org.joml.Vector3f;

public class ItemEntity extends Entity {

    private ItemStack itemStack;

    public ItemEntity(
            ItemStack itemStack,
            Vector3f position,
            Vector3f boundingBox,
            World world
        ) {
        super(
                position,
                boundingBox,
                world
        );
        this.itemStack = itemStack;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }
    
    @Override
    public void update() {
        super.update();
    }
    
    public void render() {

    }
}
