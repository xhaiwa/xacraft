package fr.xacraft.client;

import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    private static Window instance;

    private int width;
    private int height;
    private String title;

    private long glfwWindow;

    public Window(int width, int height, String title) {
        this.width = width;
        this.height = height;
        this.title = title;
        this.init();
    }

    public static Window getInstance() {
        if (instance == null) {
            instance = new Window(1280, 720, "xacraft");
        }
        return instance;
    }

    public void init() {
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        glfwWindow = glfwCreateWindow(this.width,
                                  this.height,
                                  this.title,
                                  NULL,
                                  NULL);
        if ( glfwWindow == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        glfwMakeContextCurrent(glfwWindow);
        glfwSwapInterval(0);
        GL.createCapabilities();

        glfwShowWindow(glfwWindow);
    }

    public long getGlfwWindow() {
        return this.glfwWindow;
    }

    public void destroyWindow() {
        glfwFreeCallbacks(this.glfwWindow);
        glfwDestroyWindow(this.glfwWindow);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(glfwWindow);
    }

    public void swapBuffers() {
        glfwSwapBuffers(glfwWindow);
    }

    public void setTitle(String title) {
        glfwSetWindowTitle(this.glfwWindow, title);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }
}
