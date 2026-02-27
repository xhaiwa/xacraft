package fr.xacraft.world;

import fr.xacraft.block.Block;
import fr.xacraft.block.BlockType;
import fr.xacraft.render.TextureAtlas;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Chunk {
    private Block[][][] block;
    private Vector2i pos;

    private List<Float> opaqueMeshes, opaqueUvs, opaqueLights;
    private List<Float> waterMeshes, waterUvs, waterLights;

    private int opaqueVaoId, opaqueVboId, opaqueUvVboId, opaqueLightVboId;
    private int waterVaoId, waterVboId, waterUvVboId, waterLightVboId;

    private World world;
    private boolean meshReady = false;
    private TerrainGenerator terrainGenerator;
    private int opaqueVertexCount, waterVertexCount;

    public Chunk(Vector2i pos, World world, TerrainGenerator generator) {
        this.block = new Block[16][255][16];
        this.pos = pos;

        this.opaqueVaoId = glGenVertexArrays();
        this.waterVaoId = glGenVertexArrays();

        this.opaqueVboId = glGenBuffers();
        this.opaqueLightVboId = glGenBuffers();
        this.opaqueUvVboId = glGenBuffers();

        this.waterVboId = glGenBuffers();
        this.waterLightVboId = glGenBuffers();
        this.waterUvVboId = glGenBuffers();

        this.terrainGenerator = generator;
        this.world = world;
    }

    private boolean shouldRenderFace(int x, int y, int z, BlockType currentBlockType) {
        if (x < 0 || x >= 16 || y < 0 || y >= 255 || z < 0 || z >= 16) {
            int chunkX = this.pos.x;
            int chunkZ = this.pos.y;

            if (x < 0) { chunkX--; x = 15; }
            else if (x >= 16) { chunkX++; x = 0; }
            if (z < 0) { chunkZ--; z = 15; }
            else if (z >= 16) { chunkZ++; z = 0; }

            return world.shouldRenderFace(chunkX, chunkZ, x, y, z, currentBlockType);
        }

        BlockType neighborType = this.block[x][y][z].getBlockType();

        return neighborType == BlockType.AIR ||
                (currentBlockType != neighborType && neighborType != BlockType.AIR);
    }

    public void cleanup() {
        if (opaqueVaoId != 0) glDeleteVertexArrays(opaqueVaoId);
        if (opaqueVboId != 0) glDeleteBuffers(opaqueVboId);
        if (opaqueLightVboId != 0) glDeleteBuffers(opaqueLightVboId);
        if (opaqueUvVboId != 0) glDeleteBuffers(opaqueUvVboId);

        if (waterVaoId != 0) glDeleteVertexArrays(waterVaoId);
        if (waterVboId != 0) glDeleteBuffers(waterVboId);
        if (waterLightVboId != 0) glDeleteBuffers(waterLightVboId);
        if (waterUvVboId != 0) glDeleteBuffers(waterUvVboId);

        opaqueMeshes = opaqueUvs = opaqueLights = null;
        waterMeshes = waterUvs = waterLights = null;
        block = null;
        meshReady = false;
    }

    private boolean isBlockSolid(int x, int y, int z) {
        if (x >= 0 && x < 16 && y >= 0 && y < 255 && z >= 0 && z < 16) {
            BlockType type = this.block[x][y][z].getBlockType();
            return type != BlockType.AIR && type != BlockType.WATER;
        }

        int chunkX = this.pos.x;
        int chunkZ = this.pos.y;

        if (x < 0) { chunkX--; x = 15; }
        else if (x >= 16) { chunkX++; x = 0; }
        if (z < 0) { chunkZ--; z = 15; }
        else if (z >= 16) { chunkZ++; z = 0; }

        return world.isBlockSolid(chunkX, chunkZ, x, y, z);
    }

    private float calculateVertexAO(boolean side1, boolean side2, boolean corner) {
        if (side1 && side2) return 0.25f;
        int count = (side1 ? 1 : 0) + (side2 ? 1 : 0) + (corner ? 1 : 0);
        return 1.0f - (count * 0.25f);
    }

    public Block[][][] getBlock() {
        return this.block;
    }

    public void generateChunk() {
        terrainGenerator.generateChunk(this.block, this.pos.x, this.pos.y);
    }

    public void addUpFace(int x, int y, int z, TextureAtlas atlas,
                          List<Float> meshes, List<Float> uvs, List<Float> lights) {
        BlockType blockType = this.block[x][y][z].getBlockType();
        float y0 = (blockType == BlockType.WATER) ? y + 0.875f : y + 1.0f;
        int textureIndex = blockType.getTopTexture();
        float[] texUVs = atlas.getUVs(textureIndex);

        float u0 = texUVs[0], v0 = texUVs[1];
        float u1 = texUVs[2], v1 = texUVs[3];
        float u2 = texUVs[4], v2 = texUVs[5];
        float u3 = texUVs[6], v3 = texUVs[7];

        boolean s00_1 = isBlockSolid(x - 1, y + 1, z);
        boolean s00_2 = isBlockSolid(x, y + 1, z - 1);
        boolean c00 = isBlockSolid(x - 1, y + 1, z - 1);
        float ao00 = calculateVertexAO(s00_1, s00_2, c00);

        boolean s10_1 = isBlockSolid(x + 1, y + 1, z);
        boolean s10_2 = isBlockSolid(x, y + 1, z - 1);
        boolean c10 = isBlockSolid(x + 1, y + 1, z - 1);
        float ao10 = calculateVertexAO(s10_1, s10_2, c10);

        boolean s01_1 = isBlockSolid(x - 1, y + 1, z);
        boolean s01_2 = isBlockSolid(x, y + 1, z + 1);
        boolean c01 = isBlockSolid(x - 1, y + 1, z + 1);
        float ao01 = calculateVertexAO(s01_1, s01_2, c01);

        boolean s11_1 = isBlockSolid(x + 1, y + 1, z);
        boolean s11_2 = isBlockSolid(x, y + 1, z + 1);
        boolean c11 = isBlockSolid(x + 1, y + 1, z + 1);
        float ao11 = calculateVertexAO(s11_1, s11_2, c11);

        meshes.add((float) x); meshes.add(y0); meshes.add((float) z);
        uvs.add(u0); uvs.add(v0);
        meshes.add((float) x); meshes.add(y0); meshes.add((float) z + 1);
        uvs.add(u3); uvs.add(v3);
        meshes.add((float) x + 1); meshes.add(y0); meshes.add((float) z);
        uvs.add(u1); uvs.add(v1);

        lights.add(1.0f * ao00); lights.add(1.0f * ao00); lights.add(1.0f * ao00);
        lights.add(1.0f * ao01); lights.add(1.0f * ao01); lights.add(1.0f * ao01);
        lights.add(1.0f * ao10); lights.add(1.0f * ao10); lights.add(1.0f * ao10);

        meshes.add((float) x + 1); meshes.add(y0); meshes.add((float) z);
        uvs.add(u1); uvs.add(v1);
        meshes.add((float) x); meshes.add(y0); meshes.add((float) z + 1);
        uvs.add(u3); uvs.add(v3);
        meshes.add((float) x + 1); meshes.add(y0); meshes.add((float) z + 1);
        uvs.add(u2); uvs.add(v2);

        lights.add(1.0f * ao10); lights.add(1.0f * ao10); lights.add(1.0f * ao10);
        lights.add(1.0f * ao01); lights.add(1.0f * ao01); lights.add(1.0f * ao01);
        lights.add(1.0f * ao11); lights.add(1.0f * ao11); lights.add(1.0f * ao11);
    }

    public void generateMesh(TextureAtlas atlas) {
        if (this.block == null) return;

        this.opaqueMeshes = new ArrayList<>();
        this.opaqueUvs = new ArrayList<>();
        this.opaqueLights = new ArrayList<>();

        this.waterMeshes = new ArrayList<>();
        this.waterUvs = new ArrayList<>();
        this.waterLights = new ArrayList<>();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 255; y++) {
                for (int z = 0; z < 16; z++) {
                    BlockType type = this.block[x][y][z].getBlockType();

                    if (type == BlockType.AIR) continue;

                    List<Float> targetMesh = (type == BlockType.WATER) ? waterMeshes : opaqueMeshes;
                    List<Float> targetUvs = (type == BlockType.WATER) ? waterUvs : opaqueUvs;
                    List<Float> targetLights = (type == BlockType.WATER) ? waterLights : opaqueLights;

                    if (y + 1 >= 255 || shouldRenderFace(x, y + 1, z, type))
                        addUpFace(x, y, z, atlas, targetMesh, targetUvs, targetLights);

                    if (y - 1 < 0 || shouldRenderFace(x, y - 1, z, type))
                        addDownFace(x, y, z, atlas, targetMesh, targetUvs, targetLights);

                    if (shouldRenderFace(x + 1, y, z, type))
                        addEastFace(x, y, z, atlas, targetMesh, targetUvs, targetLights);

                    if (shouldRenderFace(x - 1, y, z, type))
                        addWestFace(x, y, z, atlas, targetMesh, targetUvs, targetLights);

                    if (shouldRenderFace(x, y, z + 1, type))
                        addNorthFace(x, y, z, atlas, targetMesh, targetUvs, targetLights);

                    if (shouldRenderFace(x, y, z - 1, type))
                        addSouthFace(x, y, z, atlas, targetMesh, targetUvs, targetLights);
                }
            }
        }

        opaqueVertexCount = opaqueMeshes.size() / 3;
        waterVertexCount = waterMeshes.size() / 3;
        meshReady = true;
    }

    public void addDownFace(int x, int y, int z, TextureAtlas atlas,
                            List<Float> meshes, List<Float> uvs, List<Float> lights) {
        float y0 = y;
        BlockType blockType = this.block[x][y][z].getBlockType();
        int textureIndex = blockType.getBottomTexture();
        float[] texUVs = atlas.getUVs(textureIndex);

        float u0 = texUVs[0], v0 = texUVs[1];
        float u1 = texUVs[2], v1 = texUVs[3];
        float u2 = texUVs[4], v2 = texUVs[5];
        float u3 = texUVs[6], v3 = texUVs[7];

        boolean s00_1 = isBlockSolid(x - 1, y - 1, z);
        boolean s00_2 = isBlockSolid(x, y - 1, z - 1);
        boolean c00 = isBlockSolid(x - 1, y - 1, z - 1);
        float ao00 = calculateVertexAO(s00_1, s00_2, c00);

        boolean s10_1 = isBlockSolid(x + 1, y - 1, z);
        boolean s10_2 = isBlockSolid(x, y - 1, z - 1);
        boolean c10 = isBlockSolid(x + 1, y - 1, z - 1);
        float ao10 = calculateVertexAO(s10_1, s10_2, c10);

        boolean s01_1 = isBlockSolid(x - 1, y - 1, z);
        boolean s01_2 = isBlockSolid(x, y - 1, z + 1);
        boolean c01 = isBlockSolid(x - 1, y - 1, z + 1);
        float ao01 = calculateVertexAO(s01_1, s01_2, c01);

        boolean s11_1 = isBlockSolid(x + 1, y - 1, z);
        boolean s11_2 = isBlockSolid(x, y - 1, z + 1);
        boolean c11 = isBlockSolid(x + 1, y - 1, z + 1);
        float ao11 = calculateVertexAO(s11_1, s11_2, c11);

        meshes.add((float) x); meshes.add(y0); meshes.add((float) z);
        uvs.add(u0); uvs.add(v0);
        meshes.add((float) x + 1); meshes.add(y0); meshes.add((float) z);
        uvs.add(u1); uvs.add(v1);
        meshes.add((float) x); meshes.add(y0); meshes.add((float) z + 1);
        uvs.add(u3); uvs.add(v3);

        lights.add(0.5f * ao00); lights.add(0.5f * ao00); lights.add(0.5f * ao00);
        lights.add(0.5f * ao10); lights.add(0.5f * ao10); lights.add(0.5f * ao10);
        lights.add(0.5f * ao01); lights.add(0.5f * ao01); lights.add(0.5f * ao01);

        meshes.add((float) x + 1); meshes.add(y0); meshes.add((float) z);
        uvs.add(u1); uvs.add(v1);
        meshes.add((float) x + 1); meshes.add(y0); meshes.add((float) z + 1);
        uvs.add(u2); uvs.add(v2);
        meshes.add((float) x); meshes.add(y0); meshes.add((float) z + 1);
        uvs.add(u3); uvs.add(v3);

        lights.add(0.5f * ao10); lights.add(0.5f * ao10); lights.add(0.5f * ao10);
        lights.add(0.5f * ao11); lights.add(0.5f * ao11); lights.add(0.5f * ao11);
        lights.add(0.5f * ao01); lights.add(0.5f * ao01); lights.add(0.5f * ao01);
    }

    public void addNorthFace(int x, int y, int z, TextureAtlas atlas,
                             List<Float> meshes, List<Float> uvs, List<Float> lights) {
        float z0 = z + 1;
        BlockType blockType = this.block[x][y][z].getBlockType();
        int textureIndex = blockType.getNorthTexture();
        float[] texUVs = atlas.getUVs(textureIndex);

        float u0 = texUVs[0], v0 = texUVs[1];
        float u1 = texUVs[2], v1 = texUVs[3];
        float u2 = texUVs[4], v2 = texUVs[5];
        float u3 = texUVs[6], v3 = texUVs[7];

        boolean s00_1 = isBlockSolid(x - 1, y, z + 1);
        boolean s00_2 = isBlockSolid(x, y - 1, z + 1);
        boolean c00 = isBlockSolid(x - 1, y - 1, z + 1);
        float ao00 = calculateVertexAO(s00_1, s00_2, c00);

        boolean s10_1 = isBlockSolid(x + 1, y, z + 1);
        boolean s10_2 = isBlockSolid(x, y - 1, z + 1);
        boolean c10 = isBlockSolid(x + 1, y - 1, z + 1);
        float ao10 = calculateVertexAO(s10_1, s10_2, c10);

        boolean s01_1 = isBlockSolid(x - 1, y, z + 1);
        boolean s01_2 = isBlockSolid(x, y + 1, z + 1);
        boolean c01 = isBlockSolid(x - 1, y + 1, z + 1);
        float ao01 = calculateVertexAO(s01_1, s01_2, c01);

        boolean s11_1 = isBlockSolid(x + 1, y, z + 1);
        boolean s11_2 = isBlockSolid(x, y + 1, z + 1);
        boolean c11 = isBlockSolid(x + 1, y + 1, z + 1);
        float ao11 = calculateVertexAO(s11_1, s11_2, c11);

        meshes.add((float) x); meshes.add((float) y); meshes.add(z0);
        uvs.add(u0); uvs.add(v2);
        meshes.add((float) x + 1); meshes.add((float) y); meshes.add(z0);
        uvs.add(u2); uvs.add(v2);
        meshes.add((float) x + 1); meshes.add((float) y + 1); meshes.add(z0);
        uvs.add(u2); uvs.add(v0);

        lights.add(0.8f * ao00); lights.add(0.8f * ao00); lights.add(0.8f * ao00);
        lights.add(0.8f * ao10); lights.add(0.8f * ao10); lights.add(0.8f * ao10);
        lights.add(0.8f * ao11); lights.add(0.8f * ao11); lights.add(0.8f * ao11);

        meshes.add((float) x); meshes.add((float) y); meshes.add(z0);
        uvs.add(u0); uvs.add(v2);
        meshes.add((float) x + 1); meshes.add((float) y + 1); meshes.add(z0);
        uvs.add(u2); uvs.add(v0);
        meshes.add((float) x); meshes.add((float) y + 1); meshes.add(z0);
        uvs.add(u0); uvs.add(v0);

        lights.add(0.8f * ao00); lights.add(0.8f * ao00); lights.add(0.8f * ao00);
        lights.add(0.8f * ao11); lights.add(0.8f * ao11); lights.add(0.8f * ao11);
        lights.add(0.8f * ao01); lights.add(0.8f * ao01); lights.add(0.8f * ao01);
    }

    public void addSouthFace(int x, int y, int z, TextureAtlas atlas,
                             List<Float> meshes, List<Float> uvs, List<Float> lights) {
        float z0 = z;
        BlockType blockType = this.block[x][y][z].getBlockType();
        int textureIndex = blockType.getSouthTexture();
        float[] texUVs = atlas.getUVs(textureIndex);

        float u0 = texUVs[0], v0 = texUVs[1];
        float u1 = texUVs[2], v1 = texUVs[3];
        float u2 = texUVs[4], v2 = texUVs[5];
        float u3 = texUVs[6], v3 = texUVs[7];

        boolean s00_1 = isBlockSolid(x - 1, y, z - 1);
        boolean s00_2 = isBlockSolid(x, y - 1, z - 1);
        boolean c00 = isBlockSolid(x - 1, y - 1, z - 1);
        float ao00 = calculateVertexAO(s00_1, s00_2, c00);

        boolean s10_1 = isBlockSolid(x + 1, y, z - 1);
        boolean s10_2 = isBlockSolid(x, y - 1, z - 1);
        boolean c10 = isBlockSolid(x + 1, y - 1, z - 1);
        float ao10 = calculateVertexAO(s10_1, s10_2, c10);

        boolean s01_1 = isBlockSolid(x - 1, y, z - 1);
        boolean s01_2 = isBlockSolid(x, y + 1, z - 1);
        boolean c01 = isBlockSolid(x - 1, y + 1, z - 1);
        float ao01 = calculateVertexAO(s01_1, s01_2, c01);

        boolean s11_1 = isBlockSolid(x + 1, y, z - 1);
        boolean s11_2 = isBlockSolid(x, y + 1, z - 1);
        boolean c11 = isBlockSolid(x + 1, y + 1, z - 1);
        float ao11 = calculateVertexAO(s11_1, s11_2, c11);

        meshes.add((float) x); meshes.add((float) y); meshes.add(z0);
        uvs.add(u2); uvs.add(v2);
        meshes.add((float) x + 1); meshes.add((float) y + 1); meshes.add(z0);
        uvs.add(u0); uvs.add(v0);
        meshes.add((float) x + 1); meshes.add((float) y); meshes.add(z0);
        uvs.add(u0); uvs.add(v2);

        lights.add(0.8f * ao00); lights.add(0.8f * ao00); lights.add(0.8f * ao00);
        lights.add(0.8f * ao11); lights.add(0.8f * ao11); lights.add(0.8f * ao11);
        lights.add(0.8f * ao10); lights.add(0.8f * ao10); lights.add(0.8f * ao10);

        meshes.add((float) x); meshes.add((float) y); meshes.add(z0);
        uvs.add(u2); uvs.add(v2);
        meshes.add((float) x); meshes.add((float) y + 1); meshes.add(z0);
        uvs.add(u2); uvs.add(v0);
        meshes.add((float) x + 1); meshes.add((float) y + 1); meshes.add(z0);
        uvs.add(u0); uvs.add(v0);

        lights.add(0.8f * ao00); lights.add(0.8f * ao00); lights.add(0.8f * ao00);
        lights.add(0.8f * ao01); lights.add(0.8f * ao01); lights.add(0.8f * ao01);
        lights.add(0.8f * ao11); lights.add(0.8f * ao11); lights.add(0.8f * ao11);
    }

    public void addEastFace(int x, int y, int z, TextureAtlas atlas,
                            List<Float> meshes, List<Float> uvs, List<Float> lights) {
        float x0 = x + 1;
        BlockType blockType = this.block[x][y][z].getBlockType();
        int textureIndex = blockType.getEastTexture();
        float[] texUVs = atlas.getUVs(textureIndex);

        float u0 = texUVs[0], v0 = texUVs[1];
        float u1 = texUVs[2], v1 = texUVs[3];
        float u2 = texUVs[4], v2 = texUVs[5];
        float u3 = texUVs[6], v3 = texUVs[7];

        boolean s00_1 = isBlockSolid(x + 1, y, z - 1);
        boolean s00_2 = isBlockSolid(x + 1, y - 1, z);
        boolean c00 = isBlockSolid(x + 1, y - 1, z - 1);
        float ao00 = calculateVertexAO(s00_1, s00_2, c00);

        boolean s10_1 = isBlockSolid(x + 1, y, z + 1);
        boolean s10_2 = isBlockSolid(x + 1, y - 1, z);
        boolean c10 = isBlockSolid(x + 1, y - 1, z + 1);
        float ao10 = calculateVertexAO(s10_1, s10_2, c10);

        boolean s01_1 = isBlockSolid(x + 1, y, z - 1);
        boolean s01_2 = isBlockSolid(x + 1, y + 1, z);
        boolean c01 = isBlockSolid(x + 1, y + 1, z - 1);
        float ao01 = calculateVertexAO(s01_1, s01_2, c01);

        boolean s11_1 = isBlockSolid(x + 1, y, z + 1);
        boolean s11_2 = isBlockSolid(x + 1, y + 1, z);
        boolean c11 = isBlockSolid(x + 1, y + 1, z + 1);
        float ao11 = calculateVertexAO(s11_1, s11_2, c11);

        meshes.add(x0); meshes.add((float) y); meshes.add((float) z);
        uvs.add(u2); uvs.add(v2);
        meshes.add(x0); meshes.add((float) y + 1); meshes.add((float) z);
        uvs.add(u2); uvs.add(v0);
        meshes.add(x0); meshes.add((float) y + 1); meshes.add((float) z + 1);
        uvs.add(u0); uvs.add(v0);

        lights.add(0.6f * ao00); lights.add(0.6f * ao00); lights.add(0.6f * ao00);
        lights.add(0.6f * ao01); lights.add(0.6f * ao01); lights.add(0.6f * ao01);
        lights.add(0.6f * ao11); lights.add(0.6f * ao11); lights.add(0.6f * ao11);

        meshes.add(x0); meshes.add((float) y); meshes.add((float) z);
        uvs.add(u2); uvs.add(v2);
        meshes.add(x0); meshes.add((float) y + 1); meshes.add((float) z + 1);
        uvs.add(u0); uvs.add(v0);
        meshes.add(x0); meshes.add((float) y); meshes.add((float) z + 1);
        uvs.add(u0); uvs.add(v2);

        lights.add(0.6f * ao00); lights.add(0.6f * ao00); lights.add(0.6f * ao00);
        lights.add(0.6f * ao11); lights.add(0.6f * ao11); lights.add(0.6f * ao11);
        lights.add(0.6f * ao10); lights.add(0.6f * ao10); lights.add(0.6f * ao10);
    }

    public void addWestFace(int x, int y, int z, TextureAtlas atlas,
                            List<Float> meshes, List<Float> uvs, List<Float> lights) {
        float x0 = x;
        BlockType blockType = this.block[x][y][z].getBlockType();
        int textureIndex = blockType.getWestTexture();
        float[] texUVs = atlas.getUVs(textureIndex);

        float u0 = texUVs[0], v0 = texUVs[1];
        float u1 = texUVs[2], v1 = texUVs[3];
        float u2 = texUVs[4], v2 = texUVs[5];
        float u3 = texUVs[6], v3 = texUVs[7];

        boolean s00_1 = isBlockSolid(x - 1, y, z - 1);
        boolean s00_2 = isBlockSolid(x - 1, y - 1, z);
        boolean c00 = isBlockSolid(x - 1, y - 1, z - 1);
        float ao00 = calculateVertexAO(s00_1, s00_2, c00);

        boolean s10_1 = isBlockSolid(x - 1, y, z + 1);
        boolean s10_2 = isBlockSolid(x - 1, y - 1, z);
        boolean c10 = isBlockSolid(x - 1, y - 1, z + 1);
        float ao10 = calculateVertexAO(s10_1, s10_2, c10);

        boolean s01_1 = isBlockSolid(x - 1, y, z - 1);
        boolean s01_2 = isBlockSolid(x - 1, y + 1, z);
        boolean c01 = isBlockSolid(x - 1, y + 1, z - 1);
        float ao01 = calculateVertexAO(s01_1, s01_2, c01);

        boolean s11_1 = isBlockSolid(x - 1, y, z + 1);
        boolean s11_2 = isBlockSolid(x - 1, y + 1, z);
        boolean c11 = isBlockSolid(x - 1, y + 1, z + 1);
        float ao11 = calculateVertexAO(s11_1, s11_2, c11);

        meshes.add(x0); meshes.add((float) y); meshes.add((float) z);
        uvs.add(u0); uvs.add(v2);
        meshes.add(x0); meshes.add((float) y); meshes.add((float) z + 1);
        uvs.add(u2); uvs.add(v2);
        meshes.add(x0); meshes.add((float) y + 1); meshes.add((float) z + 1);
        uvs.add(u2); uvs.add(v0);

        lights.add(0.6f * ao00); lights.add(0.6f * ao00); lights.add(0.6f * ao00);
        lights.add(0.6f * ao10); lights.add(0.6f * ao10); lights.add(0.6f * ao10);
        lights.add(0.6f * ao11); lights.add(0.6f * ao11); lights.add(0.6f * ao11);

        meshes.add(x0); meshes.add((float) y); meshes.add((float) z);
        uvs.add(u0); uvs.add(v2);
        meshes.add(x0); meshes.add((float) y + 1); meshes.add((float) z + 1);
        uvs.add(u2); uvs.add(v0);
        meshes.add(x0); meshes.add((float) y + 1); meshes.add((float) z);
        uvs.add(u0); uvs.add(v0);

        lights.add(0.6f * ao00); lights.add(0.6f * ao00); lights.add(0.6f * ao00);
        lights.add(0.6f * ao11); lights.add(0.6f * ao11); lights.add(0.6f * ao11);
        lights.add(0.6f * ao01); lights.add(0.6f * ao01); lights.add(0.6f * ao01);
    }


    public void uploadToGpu() {
        if (!meshReady) return;

        if (!opaqueMeshes.isEmpty()) {
            glBindVertexArray(opaqueVaoId);

            glBindBuffer(GL_ARRAY_BUFFER, opaqueVboId);
            glBufferData(GL_ARRAY_BUFFER, toFloatArray(opaqueMeshes), GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            glBindBuffer(GL_ARRAY_BUFFER, opaqueLightVboId);
            glBufferData(GL_ARRAY_BUFFER, toFloatArray(opaqueLights), GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            glBindBuffer(GL_ARRAY_BUFFER, opaqueUvVboId);
            glBufferData(GL_ARRAY_BUFFER, toFloatArray(opaqueUvs), GL_STATIC_DRAW);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(2);
        }

        if (!waterMeshes.isEmpty()) {
            glBindVertexArray(waterVaoId);

            glBindBuffer(GL_ARRAY_BUFFER, waterVboId);
            glBufferData(GL_ARRAY_BUFFER, toFloatArray(waterMeshes), GL_STATIC_DRAW);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(0);

            glBindBuffer(GL_ARRAY_BUFFER, waterLightVboId);
            glBufferData(GL_ARRAY_BUFFER, toFloatArray(waterLights), GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(1);

            glBindBuffer(GL_ARRAY_BUFFER, waterUvVboId);
            glBufferData(GL_ARRAY_BUFFER, toFloatArray(waterUvs), GL_STATIC_DRAW);
            glVertexAttribPointer(2, 2, GL_FLOAT, false, 0, 0);
            glEnableVertexAttribArray(2);
        }

        glBindVertexArray(0);
    }

    private float[] toFloatArray(List<Float> list) {
        float[] arr = new float[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    public void render() {
        if (opaqueVertexCount == 0) return;
        glBindVertexArray(opaqueVaoId);
        glDrawArrays(GL_TRIANGLES, 0, opaqueVertexCount);
        glBindVertexArray(0);
    }

    public void renderWater() {
        if (waterVertexCount == 0) return;
        glBindVertexArray(waterVaoId);
        glDrawArrays(GL_TRIANGLES, 0, waterVertexCount);
        glBindVertexArray(0);
    }

    public boolean hasWater() {
        return waterVertexCount > 0;
    }

    public Vector2i getPos() {
        return this.pos;
    }

    public boolean isMeshReady() {
        return meshReady;
    }

    public float[][] getHeightDataNormalized() {
        float[][] heightData = new float[16][16];
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int y;
                for (y = 254; y >= 0; y--) {
                    if (block[x][y][z].getBlockType() != BlockType.AIR) break;
                }
                heightData[x][z] = y / 254.0f; // Normalize to [-1, 1]
            }
        }
        return heightData;
    }
}
