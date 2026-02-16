package fr.xacraft.render;

import fr.xacraft.client.Window;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class UIRenderer {

    private static UIRenderer instance;

    private static final int BITMAP_WIDTH = 512;
    private static final int BITMAP_HEIGHT = 512;
    private static final int FIRST_CHAR = 32;
    private static final int LAST_CHAR = 126;
    private static final int NUM_CHARS = LAST_CHAR - FIRST_CHAR + 1;

    private int textureId;
    private int vao, vbo;
    private int shaderProgram;

    private STBTTBakedChar.Buffer charData;
    private float fontHeight;

    public void init(String fontPath, float fontSize) {
        this.fontHeight = fontSize;

        ByteBuffer ttfData = loadFont(fontPath);

        ByteBuffer bitmap = BufferUtils.createByteBuffer(BITMAP_WIDTH * BITMAP_HEIGHT);

        charData = STBTTBakedChar.malloc(NUM_CHARS);
        int result = stbtt_BakeFontBitmap(ttfData, fontSize, bitmap,
                BITMAP_WIDTH, BITMAP_HEIGHT, FIRST_CHAR, charData);

        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, BITMAP_WIDTH, BITMAP_HEIGHT,
                0, GL_RED, GL_UNSIGNED_BYTE, bitmap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        vao = glGenVertexArrays();
        vbo = glGenBuffers();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, 6 * 4 * 4, GL_DYNAMIC_DRAW);

        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 4, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * 4, 2 * 4);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        createShader();
    }

    private ByteBuffer loadFont(String fontPath) {
        try (FileChannel fc = FileChannel.open(Paths.get(fontPath))) {
            ByteBuffer buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
            while (fc.read(buffer) != -1);
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load font: " + fontPath, e);
        }
    }

    public void drawText(String text, float x, float y, float scale, float[] color) {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);

        glUseProgram(shaderProgram);
        glBindTexture(GL_TEXTURE_2D, textureId);
        glBindVertexArray(vao);

        int colorLoc = glGetUniformLocation(shaderProgram, "textColor");
        glUniform4f(colorLoc, color[0], color[1], color[2], color[3]);

        int projLoc = glGetUniformLocation(shaderProgram, "projection");

        FloatBuffer projMat = BufferUtils.createFloatBuffer(16);
        createOrthoMatrix(projMat, 0, Window.getInstance().getWidth(),
                Window.getInstance().getHeight(), 0);
        glUniformMatrix4fv(projLoc, false, projMat);

        int charsDrawn = 0;
        try (MemoryStack stack = stackPush()) {
            FloatBuffer xPos = stack.floats(x);
            FloatBuffer yPos = stack.floats(y);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);

                if (c == '\n') {
                    yPos.put(0, yPos.get(0) + fontHeight);
                    xPos.put(0, x);
                    continue;
                }

                if (c < FIRST_CHAR || c > LAST_CHAR) continue;

                stbtt_GetBakedQuad(charData, BITMAP_WIDTH, BITMAP_HEIGHT,
                        c - FIRST_CHAR, xPos, yPos, quad, true);

                float x0 = quad.x0() * scale;
                float y0 = quad.y0() * scale;
                float x1 = quad.x1() * scale;
                float y1 = quad.y1() * scale;

                float[] vertices = {
                        x0, y0, quad.s0(), quad.t0(),
                        x1, y0, quad.s1(), quad.t0(),
                        x1, y1, quad.s1(), quad.t1(),

                        x0, y0, quad.s0(), quad.t0(),
                        x1, y1, quad.s1(), quad.t1(),
                        x0, y1, quad.s0(), quad.t1()
                };

                glBindBuffer(GL_ARRAY_BUFFER, vbo);
                glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);
                glDrawArrays(GL_TRIANGLES, 0, 6);
                charsDrawn++;
            }
        }

        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
        glUseProgram(0);

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glDisable(GL_BLEND);

        int error = glGetError();
        if (error != GL_NO_ERROR) {
            System.err.println("OpenGL error: " + error);
        }
    }

    public float getTextWidth(String text, float scale) {
        try (MemoryStack stack = stackPush()) {
            FloatBuffer xPos = stack.floats(0);
            FloatBuffer yPos = stack.floats(0);
            STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c >= FIRST_CHAR && c <= LAST_CHAR) {
                    stbtt_GetBakedQuad(charData, BITMAP_WIDTH, BITMAP_HEIGHT,
                            c - FIRST_CHAR, xPos, yPos, quad, true);
                }
            }

            return xPos.get(0) * scale;
        }
    }

    private void createShader() {
        String vertexShader =
                "#version 330 core\n" +
                        "layout (location = 0) in vec2 aPos;\n" +
                        "layout (location = 1) in vec2 aTexCoord;\n" +
                        "out vec2 TexCoord;\n" +
                        "uniform mat4 projection;\n" +
                        "void main() {\n" +
                        "    gl_Position = projection * vec4(aPos, 0.0, 1.0);\n" +
                        "    TexCoord = aTexCoord;\n" +
                        "}\n";

        String fragmentShader =
                "#version 330 core\n" +
                        "in vec2 TexCoord;\n" +
                        "out vec4 FragColor;\n" +
                        "uniform sampler2D fontTexture;\n" +
                        "uniform vec4 textColor;\n" +
                        "void main() {\n" +
                        "    float alpha = texture(fontTexture, TexCoord).r;\n" +
                        "    FragColor = vec4(textColor.rgb, textColor.a * alpha);\n" +
                        "}\n";

        int vertexId = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexId, vertexShader);
        glCompileShader(vertexId);
        checkShaderErrors(vertexId);

        int fragmentId = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentId, fragmentShader);
        glCompileShader(fragmentId);
        checkShaderErrors(fragmentId);

        shaderProgram = glCreateProgram();
        glAttachShader(shaderProgram, vertexId);
        glAttachShader(shaderProgram, fragmentId);
        glLinkProgram(shaderProgram);
        checkProgramErrors(shaderProgram);

        glDeleteShader(vertexId);
        glDeleteShader(fragmentId);

        glUseProgram(shaderProgram);
        int texLoc = glGetUniformLocation(shaderProgram, "fontTexture");
        glUniform1i(texLoc, 0);
        glUseProgram(0);
    }


    private void createOrthoMatrix(FloatBuffer buffer, float left, float right,
                                   float bottom, float top) {
        buffer.put(new float[] {
                2.0f / (right - left), 0, 0, 0,
                0, 2.0f / (top - bottom), 0, 0,
                0, 0, -1, 0,
                -(right + left) / (right - left), -(top + bottom) / (top - bottom), 0, 1
        });
        buffer.flip();
    }

    private void checkShaderErrors(int shader) {
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader compilation failed: " +
                    glGetShaderInfoLog(shader));
        }
    }

    private void checkProgramErrors(int program) {
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Program linking failed: " +
                    glGetProgramInfoLog(program));
        }
    }

    public void cleanup() {
        charData.free();
        glDeleteTextures(textureId);
        glDeleteVertexArrays(vao);
        glDeleteBuffers(vbo);
        glDeleteProgram(shaderProgram);
    }

    public static UIRenderer getInstance() {
        if (instance == null)
            instance = new UIRenderer();
        return instance;
    }
}
