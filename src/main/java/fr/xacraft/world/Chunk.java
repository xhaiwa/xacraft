package fr.xacraft.world;

import fr.xacraft.block.Block;
import fr.xacraft.block.BlockType;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    /**
     * Block [x][y][z]
     */
    private Block[][][] block;

    private Vector2i pos;

    private List<Float> meshes;
    private List<Float> lights;

    public Chunk(Vector2i pos) {
        this.block = new Block[16][255][16];
        this.pos = new Vector2i(pos.x * 16, pos.y * 16);
    }

    public Block[][][] getBlock() {
        return this.block;
    }

    public void generateFlatChunk() {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 255; y++) {
                for (int z = 0; z < 16; z++) {
                    if (y <= 20) {
                        this.block[x][y][z] = new Block(BlockType.STONE);
                    } else {
                        this.block[x][y][z] = new Block(BlockType.AIR);
                    }
                }
            }
        }
    }

    public void addUpFace(int x, int y, int z) {
        float y0 = y + 1;
        float px = pos.x;
        float pz = pos.y;

        meshes.add((float) x + px);     meshes.add(y0); meshes.add((float) z + pz);
        meshes.add((float) x + 1 + px); meshes.add(y0); meshes.add((float) z + pz);
        meshes.add((float) x + px);     meshes.add(y0); meshes.add((float) z + 1 + pz);

        meshes.add((float) x + 1 + px); meshes.add(y0); meshes.add((float) z + pz);
        meshes.add((float) x + px);     meshes.add(y0); meshes.add((float) z + 1 + pz);
        meshes.add((float) x + 1 + px); meshes.add(y0); meshes.add((float) z + 1 + pz);

        for (int i = 0; i < 6; i++) {
            lights.add(1.0f); lights.add(1.0f); lights.add(1.0f);
        }
    }

    public void addDownFace(int x, int y, int z) {
        float y0 = y;
        float px = pos.x;
        float pz = pos.y;

        meshes.add((float) x + px);     meshes.add(y0); meshes.add((float) z + pz);
        meshes.add((float) x + px);     meshes.add(y0); meshes.add((float) z + 1 + pz);
        meshes.add((float) x + 1 + px); meshes.add(y0); meshes.add((float) z + pz);

        meshes.add((float) x + 1 + px); meshes.add(y0); meshes.add((float) z + pz);
        meshes.add((float) x + px);     meshes.add(y0); meshes.add((float) z + 1 + pz);
        meshes.add((float) x + 1 + px); meshes.add(y0); meshes.add((float) z + 1 + pz);

        for (int i = 0; i < 6; i++) {
            lights.add(0.5f); lights.add(0.5f); lights.add(0.5f);
        }
    }

    public void addNorthFace(int x, int y, int z) {
        float z0 = z + 1;
        float px = pos.x;
        float pz = pos.y;


        meshes.add((float) x + px);     meshes.add((float) y);     meshes.add(z0 + pz);
        meshes.add((float) x + 1 + px); meshes.add((float) y);     meshes.add(z0 + pz);
        meshes.add((float) x + 1 + px); meshes.add((float) y + 1); meshes.add(z0 + pz);

        meshes.add((float) x + px);     meshes.add((float) y);     meshes.add(z0 + pz);
        meshes.add((float) x + 1 + px); meshes.add((float) y + 1); meshes.add(z0 + pz);
        meshes.add((float) x + px);     meshes.add((float) y + 1); meshes.add(z0 + pz);

        for (int i = 0; i < 6; i++) {
            lights.add(0.8f); lights.add(0.8f); lights.add(0.8f);
        }
    }

    public void addSouthFace(int x, int y, int z) {
        float z0 = z;
        float px = pos.x;
        float pz = pos.y;


        meshes.add((float) x + px);     meshes.add((float) y);     meshes.add(z0 + pz);
        meshes.add((float) x + 1 + px); meshes.add((float) y + 1); meshes.add(z0 + pz);
        meshes.add((float) x + 1 + px); meshes.add((float) y);     meshes.add(z0 + pz);

        meshes.add((float) x + px);     meshes.add((float) y);     meshes.add(z0 + pz);
        meshes.add((float) x + px);     meshes.add((float) y + 1); meshes.add(z0 + pz);
        meshes.add((float) x + 1 + px); meshes.add((float) y + 1); meshes.add(z0 + pz);

        for (int i = 0; i < 6; i++) {
            lights.add(0.8f); lights.add(0.8f); lights.add(0.8f);
        }
    }

    public void addEastFace(int x, int y, int z) {
        float x0 = x + 1;
        float px = pos.x;
        float pz = pos.y;


        meshes.add(x0 + px); meshes.add((float) y);     meshes.add((float) z + pz);
        meshes.add(x0 + px); meshes.add((float) y);     meshes.add((float) z + 1 + pz);
        meshes.add(x0 + px); meshes.add((float) y + 1); meshes.add((float) z + 1 + pz);

        meshes.add(x0 + px); meshes.add((float) y);     meshes.add((float) z + pz);
        meshes.add(x0 + px); meshes.add((float) y + 1); meshes.add((float) z + 1 + pz);
        meshes.add(x0 + px); meshes.add((float) y + 1); meshes.add((float) z + pz);

        for (int i = 0; i < 6; i++) {
            lights.add(0.6f); lights.add(0.6f); lights.add(0.6f);
        }
    }

    public void addWestFace(int x, int y, int z) {
        float x0 = x;
        float px = pos.x;
        float pz = pos.y;


        meshes.add(x0 + px); meshes.add((float) y);     meshes.add((float) z + pz);
        meshes.add(x0 + px); meshes.add((float) y + 1); meshes.add((float) z + 1 + pz);
        meshes.add(x0 + px); meshes.add((float) y);     meshes.add((float) z + 1 + pz);

        meshes.add(x0 + px); meshes.add((float) y);     meshes.add((float) z + pz);
        meshes.add(x0 + px); meshes.add((float) y + 1); meshes.add((float) z + pz);
        meshes.add(x0 + px); meshes.add((float) y + 1); meshes.add((float) z + 1 + pz);

        for (int i = 0; i < 6; i++) {
            lights.add(0.6f); lights.add(0.6f); lights.add(0.6f);
        }
    }

    public void generateMesh() {
        this.meshes = new ArrayList<>();
        this.lights = new ArrayList<>();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 255; y++) {
                for (int z = 0; z < 16; z++) {
                    if (this.block[x][y][z].getBlockType() != BlockType.AIR) {
                        if (y + 1 >= 255
                                || this.block[x][y + 1][z].getBlockType() == BlockType.AIR)
                            addUpFace(x, y, z);
                        if (y - 1 < 0
                                || this.block[x][y - 1][z].getBlockType() == BlockType.AIR)
                            addDownFace(x, y, z);
                        if (x + 1 >= 16
                                || this.block[x + 1][y][z].getBlockType() == BlockType.AIR)
                            addEastFace(x, y, z);
                        if (x - 1 < 0
                                || this.block[x - 1][y][z].getBlockType() == BlockType.AIR)
                            addWestFace(x, y, z);
                        if (z + 1 >= 16
                                || this.block[x][y][z + 1].getBlockType() == BlockType.AIR)
                            addNorthFace(x, y, z);
                        if (z - 1 < 0
                                || this.block[x][y][z - 1].getBlockType() == BlockType.AIR)
                            addSouthFace(x, y, z);
                    }
                }
            }
        }
    }

    public float[] getMeshesData() {
        float[] data = new float[meshes.size()];
        for (int i = 0; i < meshes.size(); i++) {
            data[i] = meshes.get(i);
        }
        return data;
    }

    public int getVertexCount() {
        return meshes.size() / 3;
    }

    public float[] getLightData() {
        float[] data = new float[lights.size()];
        for (int i = 0; i < lights.size(); i++) {
            data[i] = lights.get(i);
        }
        return data;
    }

    public int getLightCount() {
        return lights.size() / 3;
    }

    public boolean isAir(int x, int y, int z) {
        return this.block[x][y][z].getBlockType() == BlockType.AIR;
    }
}
