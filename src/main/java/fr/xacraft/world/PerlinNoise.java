package fr.xacraft.world;

import java.util.Random;

public class PerlinNoise {
    private static final int GRID_SIZE = 256;
    private float[][] gradientX;
    private float[][] gradientY;

    public PerlinNoise(long seed) {
        this.gradientX = new float[GRID_SIZE][GRID_SIZE];
        this.gradientY = new float[GRID_SIZE][GRID_SIZE];
        generateGradients(seed);
    }

    private void generateGradients(long seed) {
        Random random = new Random(seed);
        for (int y = 0; y < GRID_SIZE; y++) {
            for (int x = 0; x < GRID_SIZE; x++) {
                double angle = random.nextDouble() * 2.0 * Math.PI;
                gradientX[y][x] = (float) Math.cos(angle);
                gradientY[y][x] = (float) Math.sin(angle);
            }
        }
    }

    private float smoothstep(float w) {
        if (w <= 0.0f) return 0.0f;
        if (w >= 1.0f) return 1.0f;
        return w * w * (3.0f - 2.0f * w);
    }

    private float interpolate(float a0, float a1, float w) {
        return a0 + (a1 - a0) * smoothstep(w);
    }

    private float dotGridGradient(int ix, int iy, float x, float y) {
        int gridX = ((ix % GRID_SIZE) + GRID_SIZE) % GRID_SIZE;
        int gridY = ((iy % GRID_SIZE) + GRID_SIZE) % GRID_SIZE;

        float dx = x - ix;
        float dy = y - iy;

        return (dx * gradientX[gridY][gridX] + dy * gradientY[gridY][gridX]);
    }

    public float perlin(float x, float y) {
        int x0 = (int) Math.floor(x);
        int x1 = x0 + 1;
        int y0 = (int) Math.floor(y);
        int y1 = y0 + 1;

        float sx = x - x0;
        float sy = y - y0;

        float n0 = dotGridGradient(x0, y0, x, y);
        float n1 = dotGridGradient(x1, y0, x, y);
        float ix0 = interpolate(n0, n1, sx);

        n0 = dotGridGradient(x0, y1, x, y);
        n1 = dotGridGradient(x1, y1, x, y);
        float ix1 = interpolate(n0, n1, sx);

        return interpolate(ix0, ix1, sy);
    }

    public double perlin(double x, double y, double z) {
        float fx = (float) x;
        float fy = (float) (y + z * 0.333);

        return perlin(fx, fy);
    }

    public float octavePerlin(float x, float y, int octaves, float persistence) {
        float total = 0.0f;
        float frequency = 1.0f;
        float amplitude = 1.0f;
        float maxValue = 0.0f;

        for (int i = 0; i < octaves; i++) {
            total += perlin(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2.0f;
        }

        return total / maxValue;
    }

    public float octavePerlin3D(float x, float y, float z, int octaves, float persistence) {
        float total = 0.0f;
        float frequency = 1.0f;
        float amplitude = 1.0f;
        float maxValue = 0.0f;

        for (int i = 0; i < octaves; i++) {
            total += perlin3D(x * frequency, y * frequency, z * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= persistence;
            frequency *= 2.0f;
        }

        return total / maxValue;
    }

    public double octavePerlin(double x, double y, double z,
                               int octaves, double scaleX, double scaleY) {
        double result = 0.0;
        double amplitude = 1.0;
        double frequency = 1.0;

        for (int i = 0; i < octaves; i++) {
            result += perlin(
                    x * frequency * scaleX,
                    y * frequency * scaleY,
                    z * frequency * scaleX
            ) * amplitude;

            frequency *= 2.0;
            amplitude *= 0.5;
        }

        return result;
    }

    private float perlin3D(float x, float y, float z) {
        int xi = (int) Math.floor(x) & (GRID_SIZE - 1);
        int yi = (int) Math.floor(y) & (GRID_SIZE - 1);
        int zi = (int) Math.floor(z) & (GRID_SIZE - 1);

        float xf = x - (float) Math.floor(x);
        float yf = y - (float) Math.floor(y);
        float zf = z - (float) Math.floor(z);

        float n000 = dotGridGradient(xi, yi, xf, yf);
        float n100 = dotGridGradient(xi + 1, yi, xf - 1.0f, yf);
        float n010 = dotGridGradient(xi, yi + 1, xf, yf - 1.0f);
        float n110 = dotGridGradient(xi + 1, yi + 1, xf - 1.0f, yf - 1.0f);

        int zOffset = (int)(z * 17.0f);
        float n001 = dotGridGradient((xi + zOffset) & (GRID_SIZE - 1), yi, xf, yf);
        float n101 = dotGridGradient((xi + 1 + zOffset) & (GRID_SIZE - 1), yi, xf - 1.0f, yf);
        float n011 = dotGridGradient((xi + zOffset) & (GRID_SIZE - 1), yi + 1, xf, yf - 1.0f);
        float n111 = dotGridGradient((xi + 1 + zOffset) & (GRID_SIZE - 1), yi + 1, xf - 1.0f, yf - 1.0f);

        float x1 = interpolate(n000, n100, xf);
        float x2 = interpolate(n010, n110, xf);
        float y1 = interpolate(x1, x2, yf);

        float x3 = interpolate(n001, n101, xf);
        float x4 = interpolate(n011, n111, xf);
        float y2 = interpolate(x3, x4, yf);

        return interpolate(y1, y2, zf);
    }
}
