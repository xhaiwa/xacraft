package fr.xacraft.shader;

import static org.lwjgl.opengl.GL30.*;

public class ShaderProgram {

    private int id;

    public ShaderProgram(int vertexShader, int fragmentShader) {
        this.id = glCreateProgram();
        glAttachShader(this.id, vertexShader);
        glAttachShader(this.id, fragmentShader);
        glLinkProgram(this.id);
    }

    public int getID() {
        return this.id;
    }

    public void use() {
        glUseProgram(this.id);
    }
}
