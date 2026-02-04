package fr.xacraft.client;

import fr.xacraft.render.Camera;
import fr.xacraft.render.Renderer;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;

public class Game {
    private Window window;
    private Renderer renderer;
    private Camera camera;

    public void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        GLFWErrorCallback.createPrint(System.err).set();
        this.window = Window.getInstance();
        this.renderer = Renderer.getInstance();
        this.renderer.init();
        this.camera = new Camera(new Vector3f(0, 0, 5),
                                new Vector3f(0, 0, 0),
                                16 / 9.f,
                                70.f,
                                0.1f,
                                1000.f);
    }

    public void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while (!this.window.shouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            this.update();
            this.renderer.render(this.camera);
            this.window.swapBuffers();
            glfwPollEvents();
        }

        this.close();
    }

    public void close() {
        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void update() {

    }
}
