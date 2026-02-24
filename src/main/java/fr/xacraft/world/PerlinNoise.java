package fr.xacraft.world;

import java.util.Random;

public class PerlinNoise {
    private static final int TABLE_SIZE = 256;
    private int[] perm;
    private float[][] gradientX;
    private float[][] gradientY;

    private static final float[][] GRAD3 = {
            { 1, 1, 0}, {-1, 1, 0}, { 1,-1, 0}, {-1,-1, 0},
            { 1, 0, 1}, {-1, 0, 1}, { 1, 0,-1}, {-1, 0,-1},
            { 0, 1, 1}, { 0,-1, 1}, { 0, 1,-1}, { 0,-1,-1}
    };

    public PerlinNoise(long seed) {
        this.gradientX = new float[TABLE_SIZE][TABLE_SIZE];
        this.gradientY = new float[TABLE_SIZE][TABLE_SIZE];
        this.perm = new int[TABLE_SIZE * 2];
        generateGradients(seed);
        generatePermutationTable(seed);
    }

    private void generateGradients(long seed) {
        Random random = new Random(seed);
        for (int y = 0; y < TABLE_SIZE; y++) {
            for (int x = 0; x < TABLE_SIZE; x++) {
                double angle = random.nextDouble() * 2.0 * Math.PI;
                gradientX[y][x] = (float) Math.cos(angle);
                gradientY[y][x] = (float) Math.sin(angle);
            }
        }
    }

    private void generatePermutationTable(long seed) {
        Random random = new Random(seed * 6364136223846793005L + 1442695040888963407L);
        for (int i = 0; i < TABLE_SIZE; i++) {
            perm[i] = i;
        }
        for (int i = TABLE_SIZE - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int tmp = perm[i];
            perm[i] = perm[j];
            perm[j] = tmp;
        }
        for (int i = 0; i < TABLE_SIZE; i++) {
            perm[TABLE_SIZE + i] = perm[i];
        }
    }

    private float fade(float t) {
        return t * t * t * (t * (t * 6.0f - 15.0f) + 10.0f);
    }

    private float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    private float grad3(int hash, float x, float y, float z) {
        float[] g = GRAD3[hash % 12];
        return g[0] * x + g[1] * y + g[2] * z;
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
        int gridX = ((ix % TABLE_SIZE) + TABLE_SIZE) % TABLE_SIZE;
        int gridY = ((iy % TABLE_SIZE) + TABLE_SIZE) % TABLE_SIZE;

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
        return perlin3D((float) x, (float) y, (float) z);
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
            result += perlin3D(
                    (float)(x * frequency * scaleX),
                    (float)(y * frequency * scaleY),
                    (float)(z * frequency * scaleX)
            ) * amplitude;

            frequency *= 2.0;
            amplitude *= 0.5;
        }

        return result;
    }

    private float perlin3D(float x, float y, float z) {
        int xi = (int) Math.floor(x) & (TABLE_SIZE - 1);
        int yi = (int) Math.floor(y) & (TABLE_SIZE - 1);
        int zi = (int) Math.floor(z) & (TABLE_SIZE - 1);

        float xf = x - (float) Math.floor(x);
        float yf = y - (float) Math.floor(y);
        float zf = z - (float) Math.floor(z);

        float u = fade(xf);
        float v = fade(yf);
        float w = fade(zf);

        int a  = perm[xi]     + yi;
        int aa = perm[a]      + zi;
        int ab = perm[a + 1]  + zi;
        int b  = perm[xi + 1] + yi;
        int ba = perm[b]      + zi;
        int bb = perm[b + 1]  + zi;

        float res = lerp(
                lerp(
                        lerp(grad3(perm[aa],     xf,       yf,       zf),
                             grad3(perm[ba],     xf - 1f,  yf,       zf),      u),
                        lerp(grad3(perm[ab],     xf,       yf - 1f,  zf),
                             grad3(perm[bb],     xf - 1f,  yf - 1f,  zf),      u),
                        v),
                lerp(
                        lerp(grad3(perm[aa + 1], xf,       yf,       zf - 1f),
                             grad3(perm[ba + 1], xf - 1f,  yf,       zf - 1f), u),
                        lerp(grad3(perm[ab + 1], xf,       yf - 1f,  zf - 1f),
                             grad3(perm[bb + 1], xf - 1f,  yf - 1f,  zf - 1f), u),
                        v),
                w);

        return res;
    }
}
