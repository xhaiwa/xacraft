package fr.xacraft.client;

import org.lwjgl.*;
import static org.lwjgl.glfw.GLFW.*;

public class Main {

    private Game game;

    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        this.game = new Game();

        init();
        loop();

    }

    private void init() {
        this.game.init();
    }

    private void loop() {
        this.game.loop();
    }

    public static void main(String[] args) {
        new Main().run();
    }
}