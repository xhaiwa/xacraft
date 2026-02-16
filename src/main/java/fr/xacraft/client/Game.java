package fr.xacraft.client;

import fr.xacraft.entity.EntityPlayer;
import fr.xacraft.render.Renderer;
import fr.xacraft.render.UIRenderer;
import fr.xacraft.world.World;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Game {
    private Window window;
    private World world;
    private Renderer renderer;
    private UIRenderer uiRenderer;
    private EntityPlayer player;

    private double lastMouseX = 400;
    private double lastMouseY = 300;
    private boolean firstMouse = true;

    private boolean mouseCaptured = true;

    private static final double TICK_RATE = 20.0;
    private static final double NS_PER_TICK = 1_000_000_000.0 / TICK_RATE;

    private int tps = 0;
    private int fps = 0;

    public void init() {
        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        GLFWErrorCallback.createPrint(System.err).set();
        this.window = Window.getInstance();
        this.renderer = Renderer.getInstance();
        this.renderer.init();
        this.uiRenderer = UIRenderer.getInstance();
        this.uiRenderer.init("src/main/resources/fonts/Monocraft.ttf", 34.f);
        this.world = new World();
        this.player = new EntityPlayer(
                new Vector3f(0, 80, 5),
                new Vector3f(0.6f, 1.8f, 0.6f),
                this.world);

        for (int i = 0; i < 50; i++) {
            this.world.updateChunks(this.player.getCamera().getPosition());
        }

        glfwSetInputMode(window.getGlfwWindow(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public void loop() {
        glClearColor(0.5f, 0.7f, 1.0f, 1.0f);

        long lastTime = System.nanoTime();
        double delta = 0.0;

        long lastSecond = System.nanoTime();
        int frameCount = 0;
        int tickCount = 0;

        while (!this.window.shouldClose()) {
            long now = System.nanoTime();
            long elapsed = now - lastTime;
            delta += elapsed / NS_PER_TICK;
            lastTime = now;

            while (delta >= 1.0) {
                tick();
                tickCount++;
                delta--;
            }

            float alpha = (float) delta;

            render(alpha);
            frameCount++;

            if (System.nanoTime() - lastSecond >= 1_000_000_000) {
                fps = frameCount;
                tps = tickCount;
                frameCount = 0;
                tickCount = 0;
                lastSecond = System.nanoTime();
            }

            glfwPollEvents();
        }

        this.close();
    }

    private void tick() {
        processKeyboard();

        player.update();
        world.updateChunks(this.player.getRenderPosition());
    }

    private void render(float alpha) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        processMouse();
        player.getCamera().saveRotation();
        player.interpolate(alpha);

        renderer.render(player.getCamera(), world);

        uiRenderer.drawText("Xacraft pre-alpha", 10.f, 1 * 32.f, 0.5f,
                new float[]{1.f, 1.f, 1.f, 1.f});
        uiRenderer.drawText("FPS: " + fps + " | TPS: " + tps, 10.f, 2 * 32.f, 0.5f,
                new float[]{1.f, 1.f, 1.f, 1.f});

        Vector3f pos = player.getCamera().getPosition();
        uiRenderer.drawText(String.format("X: %.1f Y: %.1f Z: %.1f", pos.x, pos.y, pos.z),
                10.f, 3 * 32.f, 0.5f, new float[]{1.f, 1.f, 1.f, 1.f});

        window.swapBuffers();
    }

    public void close() {
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void processKeyboard() {
        long windowHandle = window.getGlfwWindow();

        player.handleInput(0);

        if (glfwGetKey(windowHandle, GLFW_KEY_TAB) == GLFW_PRESS) {
            mouseCaptured = !mouseCaptured;

            if (mouseCaptured) {
                glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
            } else {
                glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMouse() {
        if (!mouseCaptured) return;
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

        player.handleMouse((float) xOffset, (float) yOffset);
    }
}
