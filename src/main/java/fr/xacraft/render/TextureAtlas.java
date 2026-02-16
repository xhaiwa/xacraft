package fr.xacraft.render;

import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class TextureAtlas {
    private int textureId;
    private int atlasWidth = 32;
    private int atlasHeight = 32;
    private float textureSize;

    public TextureAtlas(String path) {
        this.textureSize = 1.0f / atlasWidth;
        this.textureId = loadTexture(path);
    }

    private int loadTexture(String path) {
        try {
            BufferedImage image = ImageIO.read(new File(path));
            int width = image.getWidth();
            int height = image.getHeight();

            BufferedImage imageWithAlpha = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            imageWithAlpha.getGraphics().drawImage(image, 0, 0, null);

            int[] pixels = new int[width * height];
            imageWithAlpha.getRGB(0, 0, width, height, pixels, 0, width);

            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int pixel = pixels[y * width + x];
                    buffer.put((byte) ((pixel >> 16) & 0xFF));
                    buffer.put((byte) ((pixel >> 8) & 0xFF));
                    buffer.put((byte) (pixel & 0xFF));
                    buffer.put((byte) ((pixel >> 24) & 0xFF));
                }
            }

            buffer.flip();

            int textureID = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureID);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

            System.out.println("✅ Texture loaded: " + path + " (" + width + "x" + height + ") with ALPHA");

            return textureID;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load texture: " + path, e);
        }
    }

    public void bind() {
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public float[] getUVs(int textureIndex) {
        int x = textureIndex % atlasWidth;
        int y = textureIndex / atlasWidth;

        float u0 = x * textureSize;
        float v0 = y * textureSize;
        float u1 = u0 + textureSize;
        float v1 = v0 + textureSize;

        return new float[]{u0, v0, u1, v0, u1, v1, u0, v1};
    }

    public int getTextureId() {
        return textureId;
    }
}
