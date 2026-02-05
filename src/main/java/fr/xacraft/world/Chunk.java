package fr.xacraft.world;

import fr.xacraft.block.Block;
import fr.xacraft.block.BlockType;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    /**
     * Block [x][y][z]
     */
    private Block[][][] block;
    private List<Float> meshes;

    public Chunk() {
        this.block = new Block[16][255][16];
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

    public void addUpFace(int x, int y, int z)  {
        meshes.add((float) x); meshes.add((float) y + 1); meshes.add((float) z);
        meshes.add((float) x + 1); meshes.add((float) y + 1); meshes.add((float) z);
        meshes.add((float) x); meshes.add((float) y + 1); meshes.add((float) z + 1);

        meshes.add((float) x + 1); meshes.add((float) y + 1); meshes.add((float) z);
        meshes.add((float) x); meshes.add((float) y + 1); meshes.add((float) z + 1);
        meshes.add((float) x + 1); meshes.add((float) y + 1); meshes.add((float) z + 1);
    }

    public void generateMesh() {
        this.meshes = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 255; y++) {
                for (int z = 0; z < 16; z++) {
                    if (this.block[x][y][z].getBlockType() != BlockType.AIR) {
                        addUpFace(x, y, z);
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

    public boolean isAir(int x, int y, int z) {
        return this.block[x][y][z].getBlockType() == BlockType.AIR;
    }
}
