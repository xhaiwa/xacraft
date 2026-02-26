package fr.xacraft.render;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class IsoCubeMesh {
    private int vao, vbo;
    private static final int STRIDE = 6;
    private static final int VERTEX_COUNT = 18;
    private float[] vertices = new float[VERTEX_COUNT * STRIDE];

    public void init() {
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, (long) VERTEX_COUNT * STRIDE * Float.BYTES, GL_DYNAMIC_DRAW);

        int stride = STRIDE * Float.BYTES;
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(2, 1, GL_FLOAT, false, stride, 5 * Float.BYTES);
        glEnableVertexAttribArray(2);

        glBindVertexArray(0);
    }

    public void updateAndDraw(float[] uvTop, float[] uvLeft, float[] uvRight) {
        buildVertices(uvTop, uvLeft, uvRight);

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);

        FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.length);
        fb.put(vertices).flip();
        glBufferSubData(GL_ARRAY_BUFFER, 0, fb);

        glDrawArrays(GL_TRIANGLES, 0, VERTEX_COUNT);
        glBindVertexArray(0);
    }

    private void buildVertices(float[] uvT, float[] uvL, float[] uvR) {
        int i = 0;
        i = face(i, new float[][]{
                {-0.5f,  0.5f, -0.5f,  uvT[0], uvT[1]},
                { 0.5f,  0.5f, -0.5f,  uvT[2], uvT[3]},
                { 0.5f,  0.5f,  0.5f,  uvT[4], uvT[5]},
                {-0.5f,  0.5f, -0.5f,  uvT[0], uvT[1]},
                { 0.5f,  0.5f,  0.5f,  uvT[4], uvT[5]},
                {-0.5f,  0.5f,  0.5f,  uvT[6], uvT[7]},
        }, 1.0f);
        i = face(i, new float[][]{
                {-0.5f,  0.5f,  0.5f,  uvL[0], uvL[1]},
                {-0.5f, -0.5f,  0.5f,  uvL[0], uvL[5]},
                {-0.5f, -0.5f, -0.5f,  uvL[2], uvL[5]},
                {-0.5f,  0.5f,  0.5f,  uvL[0], uvL[1]},
                {-0.5f, -0.5f, -0.5f,  uvL[2], uvL[5]},
                {-0.5f,  0.5f, -0.5f,  uvL[2], uvL[1]},
        }, 0.5f);
        i = face(i, new float[][]{
                { 0.5f,  0.5f,  0.5f,  uvR[0], uvR[1]},
                { 0.5f, -0.5f,  0.5f,  uvR[0], uvR[5]},
                {-0.5f, -0.5f,  0.5f,  uvR[2], uvR[5]},
                { 0.5f,  0.5f,  0.5f,  uvR[0], uvR[1]},
                {-0.5f, -0.5f,  0.5f,  uvR[2], uvR[5]},
                {-0.5f,  0.5f,  0.5f,  uvR[2], uvR[1]},
        }, 0.7f);
    }

    private int face(int i, float[][] verts, float shade) {
        for (float[] v : verts) {
            vertices[i++] = v[0]; vertices[i++] = v[1]; vertices[i++] = v[2];
            vertices[i++] = v[3]; vertices[i++] = v[4];
            vertices[i++] = shade;
        }
        return i;
    }
}
