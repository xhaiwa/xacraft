package fr.xacraft.render;

import fr.xacraft.client.Window;
import fr.xacraft.shader.Shader;
import fr.xacraft.shader.ShaderProgram;
import fr.xacraft.world.Chunk;
import fr.xacraft.world.World;
import org.joml.Matrix4f;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static fr.xacraft.settings.Settings.renderDistance;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

public class Renderer {
    private static Renderer instance;

    private ShaderProgram program;
    private Window window;
    private Frustum frustum;

    public Renderer() {
        this.window = Window.getInstance();
        this.frustum = new Frustum();
    }

    public static Renderer getInstance() {
        if (Renderer.instance == null)
            Renderer.instance = new Renderer();
        return Renderer.instance;
    }

    public void init() {
        Shader vertexShader = Shader.loadShader(GL_VERTEX_SHADER,
                "src/main/resources/shaders/vertex.glsl");
        Shader fragmentShader = Shader.loadShader(GL_FRAGMENT_SHADER,
                "src/main/resources/shaders/fragment.glsl");

        this.program = new ShaderProgram(vertexShader.getID(),
                fragmentShader.getID());

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);
    }

    public void render(Camera camera, World world) {
        this.program.use();
        world.getAtlas().bind();

        int textureLoc = glGetUniformLocation(this.program.getID(), "textureSampler");
        glUniform1i(textureLoc, 0);

        frustum.update(camera.getProjection(), camera.getView());

        int viewLoc = glGetUniformLocation(this.program.getID(), "view");
        int projLoc = glGetUniformLocation(this.program.getID(), "projection");
        int modelLoc = glGetUniformLocation(this.program.getID(), "model");

        glUniformMatrix4fv(viewLoc, false, camera.getView().get(new float[16]));
        glUniformMatrix4fv(projLoc, false, camera.getProjection().get(new float[16]));

        Vector3f camPos = camera.getPosition();
        int camChunkX = (int) Math.floor(camPos.x / 16.f);
        int camChunkZ = (int) Math.floor(camPos.z / 16.f);

        List<Chunk> visibleChunks = new ArrayList<>();
        for (Chunk chunk : world.getChunks()) {
            Vector2i chunkPos = chunk.getPos();
            int dx = Math.abs(chunkPos.x - camChunkX);
            int dz = Math.abs(chunkPos.y - camChunkZ);

            if (dx > renderDistance || dz > renderDistance) continue;
            if (!frustum.isChunkInFrustum(chunkPos.x, chunkPos.y)) continue;

            visibleChunks.add(chunk);
        }

        glDisable(GL_BLEND);

        for (Chunk chunk : visibleChunks) {
            Matrix4f model = new Matrix4f().identity()
                    .translate(chunk.getPos().x * 16.0f, 0, chunk.getPos().y * 16.0f);
            glUniformMatrix4fv(modelLoc, false, model.get(new float[16]));
            chunk.render();
        }

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);

        List<Chunk> waterChunks = new ArrayList<>();
        for (Chunk chunk : visibleChunks) {
            if (chunk.hasWater()) {
                waterChunks.add(chunk);
            }
        }

        waterChunks.sort(new Comparator<Chunk>() {
            @Override
            public int compare(Chunk c1, Chunk c2) {
                float dist1 = distanceSquared(c1.getPos(), camChunkX, camChunkZ);
                float dist2 = distanceSquared(c2.getPos(), camChunkX, camChunkZ);
                return Float.compare(dist2, dist1);
            }
        });

        for (Chunk chunk : waterChunks) {
            Matrix4f model = new Matrix4f().identity()
                    .translate(chunk.getPos().x * 16.0f, 0, chunk.getPos().y * 16.0f);
            glUniformMatrix4fv(modelLoc, false, model.get(new float[16]));
            chunk.renderWater();
        }

        glDepthMask(true);
        glDisable(GL_BLEND);
    }

    private float distanceSquared(Vector2i chunkPos, int camChunkX, int camChunkZ) {
        float dx = chunkPos.x - camChunkX;
        float dz = chunkPos.y - camChunkZ;
        return dx * dx + dz * dz;
    }
}