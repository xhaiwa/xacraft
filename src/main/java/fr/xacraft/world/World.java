package fr.xacraft.world;

import fr.xacraft.block.BlockType;
import fr.xacraft.entity.Entity;
import fr.xacraft.render.TextureAtlas;
import org.joml.Vector2i;
import org.joml.Vector3f;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static fr.xacraft.settings.Settings.renderDistance;
import static fr.xacraft.world.NoiseUtils.toGrayImage;

public class World {
    //private List<Chunk> chunks;
    private Map<Vector2i, Chunk> chunkMap;
    private int lastCamChunkX = Integer.MAX_VALUE;
    private int lastCamChunkZ = Integer.MAX_VALUE;
    private TextureAtlas atlas;
    private TerrainGenerator terrainGenerator;

    private List<Entity> entities = new ArrayList<>();

    private Queue<Vector2i> chunksToGenerate = new LinkedList<>();
    private Queue<Chunk> chunksToMesh = new LinkedList<>();
    private Queue<Chunk> chunksToUpload = new LinkedList<>();
    private Set<Vector2i> chunksInQueue = new HashSet<>();

    private static final int MAX_CHUNKS_GENERATE_PER_FRAME = 1;
    private static final int MAX_CHUNKS_MESH_PER_FRAME = 1;
    private static final int MAX_CHUNKS_UPLOAD_PER_FRAME = 4;

    private static final int UNLOAD_DISTANCE = renderDistance + 4;

    public World() {
        //this.chunks = new ArrayList<>();
        this.chunkMap = new HashMap<>();
        this.atlas = new TextureAtlas("src/main/resources/textures/atlas.png");
        this.terrainGenerator = new TerrainGenerator(0l);
    }

    public void updateChunks(Vector3f cameraPos) {
        int camChunkX = (int) Math.floor(cameraPos.x / 16.0f);
        int camChunkZ = (int) Math.floor(cameraPos.z / 16.0f);

        unloadDistantChunks(camChunkX, camChunkZ);

        if (camChunkX != lastCamChunkX || camChunkZ != lastCamChunkZ) {
            lastCamChunkX = camChunkX;
            lastCamChunkZ = camChunkZ;

            for (int r = 0; r <= renderDistance; r++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.abs(dx) == r || Math.abs(dz) == r) {
                            int chunkX = camChunkX + dx;
                            int chunkZ = camChunkZ + dz;
                            Vector2i pos = new Vector2i(chunkX, chunkZ);

                            if (!chunkMap.containsKey(pos) && !chunksInQueue.contains(pos)) {
                                chunksToGenerate.add(pos);
                                chunksInQueue.add(pos);
                            }
                        }
                    }
                }
            }
        }

        int generated = 0;
        while (!chunksToGenerate.isEmpty() && generated < MAX_CHUNKS_GENERATE_PER_FRAME) {
            Vector2i pos = chunksToGenerate.poll();
            chunksInQueue.remove(pos);

            Chunk chunk = new Chunk(pos, this, terrainGenerator);
            chunk.generateChunk();
            chunkMap.put(pos, chunk);
            //chunks.add(chunk);
            chunksToMesh.add(chunk);

            generated++;
        }

        int meshed = 0;
        List<Chunk> toRetry = new ArrayList<>();

        while (!chunksToMesh.isEmpty() && meshed < MAX_CHUNKS_MESH_PER_FRAME) {
            Chunk chunk = chunksToMesh.poll();
            Vector2i pos = chunk.getPos();

            boolean hasNeighbors =
                    chunkMap.containsKey(new Vector2i(pos.x - 1, pos.y)) &&
                            chunkMap.containsKey(new Vector2i(pos.x + 1, pos.y)) &&
                            chunkMap.containsKey(new Vector2i(pos.x, pos.y - 1)) &&
                            chunkMap.containsKey(new Vector2i(pos.x, pos.y + 1));

            if (hasNeighbors) {
                chunk.generateMesh(atlas);
                chunksToUpload.add(chunk);

                regenerateNeighbor(pos.x - 1, pos.y);
                regenerateNeighbor(pos.x + 1, pos.y);
                regenerateNeighbor(pos.x, pos.y - 1);
                regenerateNeighbor(pos.x, pos.y + 1);
            } else {
                toRetry.add(chunk);
            }

            meshed++;
        }

        chunksToMesh.addAll(toRetry);

        int uploaded = 0;
        while (!chunksToUpload.isEmpty() && uploaded < MAX_CHUNKS_UPLOAD_PER_FRAME) {
            Chunk chunk = chunksToUpload.poll();
            chunk.uploadToGpu();
            uploaded++;
        }
    }

    private void unloadDistantChunks(int camChunkX, int camChunkZ) {
        Iterator<Map.Entry<Vector2i, Chunk>> iterator = chunkMap.entrySet().iterator();
        int unloaded = 0;

        while (iterator.hasNext()) {
            Map.Entry<Vector2i, Chunk> entry = iterator.next();
            Vector2i pos = entry.getKey();

            int dx = Math.abs(pos.x - camChunkX);
            int dz = Math.abs(pos.y - camChunkZ);

            if (dx > UNLOAD_DISTANCE || dz > UNLOAD_DISTANCE) {
                Chunk chunk = entry.getValue();
                chunk.cleanup();
                //chunks.remove(chunk);
                iterator.remove();
                unloaded++;
            }
        }
    }

    private void regenerateNeighbor(int chunkX, int chunkZ) {
        Vector2i pos = new Vector2i(chunkX, chunkZ);
        Chunk chunk = chunkMap.get(pos);

        if (chunk != null && chunk.isMeshReady()) {
            chunk.generateMesh(atlas);
            chunksToUpload.add(chunk);
        }
    }

    /*
    public List<Chunk> getChunks() {
        return chunks;
    }
    */

    public Chunk getChunk(int chunkX, int chunkZ) {
        return chunkMap.get(new Vector2i(chunkX, chunkZ));
    }

    public Map<Vector2i, Chunk> getChunkMap() {
        return chunkMap;
    }

    public boolean isBlockSolid(int chunkX, int chunkZ, int localX, int localY, int localZ) {
        Chunk chunk = getChunk(chunkX, chunkZ);

        if (chunk == null) {
            return false;
        }

        if (localY < 0 || localY >= 255) {
            return false;
        }

        BlockType type = chunk.getBlock()[localX][localY][localZ].getBlockType();
        return type != BlockType.AIR && type != BlockType.WATER;
    }

    public boolean shouldRenderFace(int chunkX, int chunkZ, int x, int y, int z, BlockType currentType) {
        Chunk chunk = getChunk(chunkX, chunkZ);
        if (chunk == null) return true;

        BlockType neighborType = chunk.getBlock()[x][y][z].getBlockType();

        return neighborType == BlockType.AIR ||
                (currentType != neighborType && neighborType != BlockType.AIR);
    }

    public TextureAtlas getAtlas() {
        return this.atlas;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    public void image() {
        float[][] heightMap = new float[16 * 100][16 * 100];

        for (int x = 0; x < 100; x++) {
            for (int z = 0; z < 100; z++) {
                Chunk chunk = new Chunk(new Vector2i(x, z), this, terrainGenerator);
                chunk.generateChunk();
                float[][] chunkHeight = chunk.getHeightDataNormalized();
                for (int lx = 0; lx < 16; lx++)
                    for (int lz = 0; lz < 16; lz++)
                        heightMap[x * 16 + lx][z * 16 + lz] = chunkHeight[lx][lz];
            }
            if (x % 1 == 0) {
                System.out.println("Generated " + x + " chunks");
            }
        }

        float min = Float.MAX_VALUE, max = -Float.MAX_VALUE;
        for (float[] row : heightMap)
            for (float v : row) {
                if (v < min) min = v;
                if (v > max) max = v;
            }

        float range = max - min;
        for (int i = 0; i < heightMap.length; i++)
            for (int j = 0; j < heightMap[i].length; j++)
                heightMap[i][j] = range > 0 ? (heightMap[i][j] - min) / range : 0.5f;


        BufferedImage image = toGrayImage(heightMap);
        System.out.println(Arrays.deepToString(heightMap));
        File output = new File("/home/xhaiwa/Documents/world.jpg");
        try {
            ImageIO.write(image, "jpg", output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
