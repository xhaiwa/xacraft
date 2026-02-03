package fr.xacraft.client;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL30.*;

public class Game {
    private Window window;

    private int vaoId;
    private int vboId;
    private float[] vertexBufferData;

    public Game() {
    }

    public void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");
        this.window = Window.getInstance();

        GL.createCapabilities();

        // VAO + VBO + Vertex
        this.vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        this.vertexBufferData = new float[] {
                -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                0.0f,  1.0f, 0.0f,
        };

        this.vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, this.vertexBufferData, GL_STATIC_DRAW);
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
            this.render();
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

    public void render() {
        glEnableVertexAttribArray(0);
        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glDrawArrays(GL_TRIANGLES, 0, 3);
        glDisableVertexAttribArray(0);
    }
}
