package fr.xacraft.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private Vector3f position;
    private Vector3f rotation;
    private float ratio;
    private float fov;
    private float zNear;
    private float zFar;

    private Matrix4f projection;
    private Matrix4f view;

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
        this.projection = new Matrix4f().perspective(this.fov, ratio, zNear, zFar);
        this.view = new Matrix4f();

        updateViewMatrix();
    }

    public void update() {
        this.updateViewMatrix();
    }

    public void updateViewMatrix() {
        this.view.identity()
                .rotateX(this.rotation.x)
                .rotateY(this.rotation.y)
                .rotateZ(this.rotation.z)
                .translate(-this.position.x,
                            -this.position.y,
                            -this.position.z);
    }

    public Matrix4f getView() {
        return this.view;
    }

    public Matrix4f getProjection() {
        return this.projection;
    }
}
