package fr.xacraft.world;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class NoiseUtils {

    public static void continentImage(long seed) {
        PerlinNoise noise = new PerlinNoise(seed);
        float[][] data = new float[1000][1000];
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                data[i][j] = NoiseUtils.sampleNormalized(noise, i, j, 0.00015f, 5, 0.55f);
            }
        }
        BufferedImage image = toGrayImage(data);
        System.out.println(Arrays.deepToString(data));
        File output = new File("/home/xhaiwa/Documents/continental.jpg");
        try {
            ImageIO.write(image, "jpg", output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void detailImage1(long seed) {
        PerlinNoise noise = new PerlinNoise(seed);
        float[][] data = new float[1000][1000];
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                data[i][j] = NoiseUtils.sampleNormalized(noise, i, j, 0.012f, 5, 0.5f);
            }
        }
        BufferedImage image = toGrayImage(data);
        System.out.println(Arrays.deepToString(data));
        File output = new File("/home/xhaiwa/Documents/detail1.jpg");
        try {
            ImageIO.write(image, "jpg", output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static BufferedImage toGrayImage(float[][] data) {
        int height = data.length;
        int width = data[0].length;

        BufferedImage image = new BufferedImage(
                width, height, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                float v = data[y][x];

                v = Math.max(-1f, Math.min(1f, v));

                int gray = (int) ((v + 1f) * 127.5f);
                int rgb = (gray << 16) | (gray << 8) | gray;

                image.setRGB(x, y, rgb);
            }
        }
        return image;
    }

    private static float sampleNormalized(PerlinNoise noise, int wx, int wz,
                                    float scale, int octaves, float persistence) {
        float v = noise.octavePerlin(wx * scale, wz * scale, octaves, persistence);
        return v;
    }
}
