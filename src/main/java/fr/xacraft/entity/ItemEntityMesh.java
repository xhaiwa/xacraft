package fr.xacraft.entity;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class ItemEntityMesh {
    private int vao, vbo;
    private static final int STRIDE = 5;
    private static final int VERTEX_COUNT = 36;

    private float[] vertices = new float[STRIDE * VERTEX_COUNT]; // positiom, uv

    // TODO: Finish this for item entity system

    public void init() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) STRIDE * VERTEX_COUNT * Float.BYTES, GL_DYNAMIC_DRAW);

        int stride = Float.BYTES * STRIDE;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
    }
}
