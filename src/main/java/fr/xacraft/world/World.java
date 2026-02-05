package fr.xacraft.world;

import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class World {
    private List<Chunk> chunks;

    public World() {
        this.chunks = new ArrayList<>();
        generateChunks();
    }

    public void generateChunks() {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Vector2i pos = new Vector2i(i, j);
                Chunk chunk = new Chunk(pos);
                chunk.generateFlatChunk();
                chunk.generateMesh();
                chunks.add(chunk);
            }
        }
    }

    public void render() {
        for (Chunk chunk : chunks) {
            chunk.render();
        }
    }
}
