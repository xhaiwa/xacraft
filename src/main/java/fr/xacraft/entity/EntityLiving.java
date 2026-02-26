package fr.xacraft.entity;

import fr.xacraft.world.World;
import org.joml.Vector3f;

public class EntityLiving extends Entity {

    private float health;
    private float damage;
    private boolean isMob;

    public EntityLiving(Vector3f position,
                        Vector3f boudingBox,
                        float health,
                        float damage,
                        boolean isMob,
                        World world) {
        super(position, boudingBox, world);
        this.health = health;
        this.damage = damage;
        this.isMob = isMob;
    }

    @Override
    public void update() {
        super.update();
        if (isMob)
            updateAi();
    }

    public void updateAi() {

    }

    public float getHealth() {
        return this.health;
    }
}
