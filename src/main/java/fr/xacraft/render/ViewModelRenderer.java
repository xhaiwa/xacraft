package fr.xacraft.render;

import fr.xacraft.client.Window;
import fr.xacraft.shader.Shader;
import fr.xacraft.shader.ShaderProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class ViewModelRenderer {

    private ShaderProgram handShader;
    private TextureAtlas steveAtlas;

    private HandMesh handMesh;

    private int vao, vbo;

    public ViewModelRenderer() {
        int vertexShader = Shader.loadShader(GL_VERTEX_SHADER, "src/main/resources/shaders/hand.vert").getID();
        int fragmentShader = Shader.loadShader(GL_FRAGMENT_SHADER, "src/main/resources/shaders/hand.frag").getID();

        this.handShader = new ShaderProgram(vertexShader, fragmentShader);
        this.steveAtlas = new TextureAtlas("src/main/resources/textures/steve.png");
        this.handMesh = new HandMesh();
        this.vao = glGenVertexArrays();
        this.vbo = glGenBuffers();
        this.upload();
    }

    public void upload() {
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, handMesh.getVertices(), GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void render(float alpha) {
        glClear(GL_DEPTH_BUFFER_BIT);


        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(70.0f),
                Window.getInstance().getWidth() / (float) Window.getInstance().getHeight(),
                0.01f,
                10.f);

        Matrix4f view = new Matrix4f().translate(ViewModelTransform.transform);

        Matrix4f model = new Matrix4f()
                .rotateX((float) Math.toRadians(ViewModelTransform.rotation.x))
                .rotateY((float) Math.toRadians(ViewModelTransform.rotation.y))
                .rotateZ((float) Math.toRadians(ViewModelTransform.rotation.z))
                .scale(1f);

        handShader.use();
        steveAtlas.bind();
        float[] buf = new float[16];
        projection.get(buf);
        glUniformMatrix4fv(glGetUniformLocation(handShader.getID(), "projection"), false, buf);
        view.get(buf);
        glUniformMatrix4fv(glGetUniformLocation(handShader.getID(), "view"), false, buf);
        model.get(buf);
        glUniformMatrix4fv(glGetUniformLocation(handShader.getID(), "model"), false, buf);

        glUniform1i(glGetUniformLocation(handShader.getID(), "textureSampler"), 0);
        glBindVertexArray(vao);
        glDrawArrays(GL_TRIANGLES, 0, handMesh.getVertices().length / 5);
        glBindVertexArray(0);

        steveAtlas.unbind();
    }
}
