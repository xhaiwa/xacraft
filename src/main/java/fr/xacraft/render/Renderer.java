package fr.xacraft.render;

import fr.xacraft.client.Window;
import fr.xacraft.shader.Shader;
import fr.xacraft.shader.ShaderProgram;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class Renderer {
    private static Renderer instance;

    private ShaderProgram program;
    private Window window;

    private int vaoId;
    private int vboId;
    private float[] vertexBufferData;

    public Renderer() {
        this.window = Window.getInstance();
    }

    public static Renderer getInstance() {
        if (Renderer.instance == null)
            Renderer.instance = new Renderer();
        return Renderer.instance;
    }

    public void init() {

        // Load Shader
        Shader vertexShader = Shader.loadShader(GL_VERTEX_SHADER,
                "src/main/java/fr/xacraft/shader/vertex.glsl");
        Shader fragmentShader = Shader.loadShader(GL_FRAGMENT_SHADER,
                "src/main/java/fr/xacraft/shader/fragment.glsl");

        this.program = new ShaderProgram(vertexShader.getID(),
                fragmentShader.getID());

        // VAO + VBO + Vertex
        this.vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId);
        this.vertexBufferData = new float[] {
                -1.0f, -1.0f, 0.0f,
                1.0f, -1.0f, 0.0f,
                0.0f,  1.0f, 0.0f,
        };

        this.vboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, this.vertexBufferData, GL_STATIC_DRAW);
        glEnableVertexAttribArray(0);
    }

    public void render(Camera camera) {
        this.program.use();

        int viewLoc = glGetUniformLocation(this.program.getID(), "view");
        int projLoc = glGetUniformLocation(this.program.getID(), "projection");
        glUniformMatrix4fv(viewLoc, false, camera.getView().get(new float[16]));
        glUniformMatrix4fv(projLoc, false, camera.getProjection().get(new float[16]));

        glBindBuffer(GL_ARRAY_BUFFER, this.vboId);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glDrawArrays(GL_TRIANGLES, 0, 3);
    }
}
