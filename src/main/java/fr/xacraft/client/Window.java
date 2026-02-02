package fr.xacraft.client;

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
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        // Create the window
        glfwWindow = glfwCreateWindow(this.width,
                                  this.height,
                                  this.title,
                                  NULL,
                                  NULL);
        if ( glfwWindow == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Callback pour fermer la fenetre, se lance a chaque appuie sur la touche
        glfwSetKeyCallback(glfwWindow, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true);
        });

        glfwMakeContextCurrent(glfwWindow);
        // Vsync
        glfwSwapInterval(1);

        glfwShowWindow(glfwWindow);
    }

    public long getGlfwWindow() {
        return this.glfwWindow;
    }

    public void destroyWindow() {
        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(this.glfwWindow);
        glfwDestroyWindow(this.glfwWindow);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(glfwWindow);
    }

    public void swapBuffers() {
        glfwSwapBuffers(glfwWindow);
    }
}
