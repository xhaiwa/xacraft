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

    private double lastMouseX = 400;
    private double lastMouseY = 300;
    private boolean firstMouse = true;

    public void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        GLFWErrorCallback.createPrint(System.err).set();
        this.window = Window.getInstance();
        this.renderer = Renderer.getInstance();
        this.renderer.init();
        this.camera = new Camera(new Vector3f(0, 21, 5),
                                new Vector3f(0, 0, 0),
                                16 / 9.f,
                                70.f,
                                0.1f,
                                1000.f);

        glfwSetInputMode(window.getGlfwWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.

        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        float lastFrame = 0.f;

        while (!this.window.shouldClose()) {
            float currentFrame = (float) glfwGetTime();
            float deltaTime = currentFrame - lastFrame;
            lastFrame = currentFrame;

            System.out.println(1.f / deltaTime);

            processKeyboard(deltaTime);
            processMouse();
            camera.update();

            this.update();
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
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

    private void processKeyboard(float deltaTime) {
        long windowHandle = window.getGlfwWindow();

        if (glfwGetKey(windowHandle, GLFW_KEY_W) == GLFW_PRESS) {
            camera.move(GLFW_KEY_W, deltaTime);
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_S) == GLFW_PRESS) {
            camera.move(GLFW_KEY_S, deltaTime);
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_A) == GLFW_PRESS) {
            camera.move(GLFW_KEY_A, deltaTime);
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_D) == GLFW_PRESS) {
            camera.move(GLFW_KEY_D, deltaTime);
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_SPACE) == GLFW_PRESS) {
            camera.move(GLFW_KEY_SPACE, deltaTime);
        }
        if (glfwGetKey(windowHandle, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
            camera.move(GLFW_KEY_LEFT_SHIFT, deltaTime);
        }

        // ESC pour quitter
        if (glfwGetKey(windowHandle, GLFW_KEY_ESCAPE) == GLFW_PRESS) {
            glfwSetWindowShouldClose(windowHandle, true);
        }
    }

    private void processMouse() {
        long windowHandle = window.getGlfwWindow();

        double[] xpos = new double[1];
        double[] ypos = new double[1];
        glfwGetCursorPos(windowHandle, xpos, ypos);

        if (firstMouse) {
            lastMouseX = xpos[0];
            lastMouseY = ypos[0];
            firstMouse = false;
        }

        double xOffset = xpos[0] - lastMouseX;
        double yOffset = lastMouseY - ypos[0];

        lastMouseX = xpos[0];
        lastMouseY = ypos[0];

        camera.rotate((float) xOffset, (float) yOffset);
    }
}
