package fr.xacraft.render;

import fr.xacraft.client.Window;
import fr.xacraft.shader.Shader;
import fr.xacraft.shader.ShaderProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL20.*;

public class ViewModelRenderer {

    private ShaderProgram handShader;

    public ViewModelRenderer() {
        int vertexShader = Shader.loadShader(GL_VERTEX_SHADER, "shaders/hand.vert").getID();
        int fragmentShader = Shader.loadShader(GL_FRAGMENT_SHADER, "shaders/hand.frag").getID();
        handShader = new ShaderProgram(vertexShader, fragmentShader);
    }

    public void render(float alpha) {
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(70.0f),
                Window.getInstance().getWidth() / (float) Window.getInstance().getHeight(),
                0.01f,
                10.f);

        Matrix4f view = new Matrix4f()
                .rotateX((float) Math.toRadians(-10.0f * alpha))
                .rotateY((float) Math.toRadians(10.0f * alpha))
                .translate(0.5f, -0.5f, -1.0f);

        Matrix4f model = new Matrix4f().identity();

        handShader.use();
        float[] buf = new float[16];
        projection.get(buf);
        glUniformMatrix4fv(glGetUniformLocation(handShader.getID(), "projection"), false, buf);
        view.get(buf);
        glUniformMatrix4fv(glGetUniformLocation(handShader.getID(), "view"), false, buf);
        model.get(buf);
        glUniformMatrix4fv(glGetUniformLocation(handShader.getID(), "model"), false, buf);
    }
}
