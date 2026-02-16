package fr.xacraft.entity;

import fr.xacraft.render.Camera;

public interface IEntityPlayable {
    void handleInput(float deltaTime);
    void handleMouse(float xOffset, float yOffset);
    void jump();
    Camera getCamera();
}
