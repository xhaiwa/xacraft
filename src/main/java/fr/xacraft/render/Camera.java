package fr.xacraft.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private Vector3f position;
    private Vector3f rotation;
    private float ratio;
    private float fov;
    private float zNear;
    private float zFar;

    private Matrix4f projection;
    private Matrix4f view;

    private float movementSpeed = 30.0f;
    private float mouseSensitivity = 0.15f;

    private Vector3f previousRotation;
    private Vector3f renderRotation;

    public Camera(Vector3f position,
                  Vector3f rotation,
                  float ratio,
                  float fov,
                  float zNear,
                  float zFar) {
        this.position = position;
        this.rotation = rotation;
        this.ratio = ratio;
        this.fov = fov;
        this.zNear = zNear;
        this.zFar = zFar;

        this.previousRotation = new Vector3f(rotation);
        this.renderRotation = new Vector3f(rotation);

        this.projection = new Matrix4f().perspective((float)Math.toRadians(this.fov), ratio, zNear, zFar);
        this.view = new Matrix4f();

        updateViewMatrix();
    }

    public void update() {
        this.updateViewMatrix();
    }

    public void updateViewMatrix() {
        this.view.identity()
                .rotateX(this.renderRotation.x)
                .rotateY(this.renderRotation.y)
                .rotateZ(this.renderRotation.z)
                .translate(-this.position.x,
                        -this.position.y,
                        -this.position.z);
    }

    public void move(int direction, float deltaTime) {
        float velocity = movementSpeed * deltaTime;

        Vector3f forward = getForward();
        Vector3f right = getRight();

        switch (direction) {
            case GLFW_KEY_W:
                position.add(forward.mul(velocity));
                break;
            case GLFW_KEY_S:
                position.sub(forward.mul(velocity));
                break;
            case GLFW_KEY_D:
                position.sub(right.mul(velocity));
                break;
            case GLFW_KEY_A:
                position.add(right.mul(velocity));
                break;
            case GLFW_KEY_SPACE:
                position.y += velocity;
                break;
            case GLFW_KEY_LEFT_SHIFT:
                position.y -= velocity;
                break;
        }
    }

    public void saveRotation() {
        previousRotation.set(rotation);
    }

    public void rotate(float xOffset, float yOffset) {
        xOffset *= mouseSensitivity;
        yOffset *= mouseSensitivity;

        rotation.y += (float) Math.toRadians(xOffset);
        rotation.x += (float) Math.toRadians(yOffset);

        if (rotation.x > Math.toRadians(89.0f)) {
            rotation.x = (float) Math.toRadians(89.0f);
        }
        if (rotation.x < Math.toRadians(-89.0f)) {
            rotation.x = (float) Math.toRadians(-89.0f);
        }
    }

    public void interpolateRotation(float alpha) {
        renderRotation.x = previousRotation.x + (rotation.x - previousRotation.x) * alpha;
        renderRotation.y = previousRotation.y + (rotation.y - previousRotation.y) * alpha;
        renderRotation.z = previousRotation.z + (rotation.z - previousRotation.z) * alpha;
    }

    public Vector3f getForward() {
        float yaw = rotation.y;
        float pitch = rotation.x;

        return new Vector3f(
                (float) Math.sin(yaw) * (float) Math.cos(pitch),
                (float) -Math.sin(pitch),
                (float) -Math.cos(yaw) * (float) Math.cos(pitch)
        ).normalize();
    }

    public Vector3f getRight() {
        float yaw = rotation.y - (float) Math.toRadians(90.0f);

        return new Vector3f(
                (float) Math.sin(yaw),
                0,
                (float) -Math.cos(yaw)
        ).normalize();
    }

    public Matrix4f getView() {
        return this.view;
    }

    public Matrix4f getProjection() {
        return this.projection;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public void setFov(float fov) {
        this.fov = fov;
    }

    public float getFov() {
        return this.fov;
    }
}
