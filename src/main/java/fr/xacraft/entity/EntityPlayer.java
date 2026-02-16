package fr.xacraft.entity;

import fr.xacraft.client.Window;
import fr.xacraft.render.Camera;
import fr.xacraft.world.World;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class EntityPlayer extends EntityLiving implements IEntityPlayable {

    private Camera camera;
    private float movementSpeed;

    private boolean isJumping = false;
    private float jumpStartTime = 0f;
    private float jumpStartY = 0f;
    private int jumpFrameCount = 0;

    public EntityPlayer(Vector3f position,
                        Vector3f boudingBox,
                        World world) {
        super(position,
                boudingBox,
                20.f,
                2.f,
                false,
                world);

        this.camera = new Camera(
                position.add(0, 1.6f, 0, new Vector3f()),
                new Vector3f(0, 0, 0),
                16 / 9.f, 70.f, 0.1f, 1000.f
        );

        this.movementSpeed = 4.317f;
    }

    @Override
    public void update() {

        if (isJumping) {
            jumpFrameCount++;
            float currentTime = (float) glfwGetTime();
            float elapsedTime = currentTime - jumpStartTime;
            float heightGained = position.y - jumpStartY;

            if (onGround && motion.y <= 0) {
                float totalTime = currentTime - jumpStartTime;
                isJumping = false;
            }
        }

        super.update();
    }

    @Override
    public void handleInput(float deltaTime) {
        float acceleration = 0.15f;

        Vector3f forward = camera.getForward();
        Vector3f right = camera.getRight();

        forward.y = 0;
        forward.normalize();
        right.y = 0;
        right.normalize();

        Vector3f inputDirection = new Vector3f();

        long glfwWindow = Window.getInstance().getGlfwWindow();

        if (glfwGetKey(glfwWindow, GLFW_KEY_W) == GLFW_PRESS)
            inputDirection.add(forward);
        if (glfwGetKey(glfwWindow, GLFW_KEY_S) == GLFW_PRESS)
            inputDirection.sub(forward);
        if (glfwGetKey(glfwWindow, GLFW_KEY_A) == GLFW_PRESS)
            inputDirection.add(right);
        if (glfwGetKey(glfwWindow, GLFW_KEY_D) == GLFW_PRESS)
            inputDirection.sub(right);

        if (inputDirection.lengthSquared() > 0) {
            inputDirection.normalize();

            this.motion.x += inputDirection.x * acceleration;
            this.motion.z += inputDirection.z * acceleration;

            float horizontalSpeed = (float) Math.sqrt(
                    this.motion.x * this.motion.x +
                            this.motion.z * this.motion.z
            );

            float maxSpeed = 0.215f;
            if (horizontalSpeed > maxSpeed) {
                float scale = maxSpeed / horizontalSpeed;
                this.motion.x *= scale;
                this.motion.z *= scale;
            }
        }

        if (glfwGetKey(glfwWindow, GLFW_KEY_SPACE) == GLFW_PRESS)
            this.jump();
    }

    @Override
    public void handleMouse(float xOffset, float yOffset) {
        camera.rotate(xOffset, yOffset);
    }

    @Override
    public void jump() {
        if (onGround && !isJumping) {
            isJumping = true;
            jumpStartTime = (float) glfwGetTime();
            jumpStartY = position.y;
            jumpFrameCount = 0;

            this.motion.y = 0.42f;
        }
    }

    @Override
    public void interpolate(float alpha) {
        super.interpolate(alpha);
        camera.interpolateRotation(alpha);
    }

    @Override
    public Camera getCamera() {
        this.camera.setPosition(
                this.renderPosition.x,
                this.renderPosition.y + 1.62f,
                this.renderPosition.z
        );

        camera.update();
        return this.camera;
    }
}
