package fr.xacraft.render;

import fr.xacraft.block.BlockType;
import fr.xacraft.client.Game;
import fr.xacraft.client.Window;
import fr.xacraft.entity.EntityPlayer;
import fr.xacraft.shader.Shader;
import fr.xacraft.shader.ShaderProgram;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30;

import static org.lwjgl.opengl.GL20.*;

public class PlayerUI {

    private UIRenderer uiRenderer;
    private EntityPlayer player;
    private int barTextureId;
    private int iconsTextureId;
    private int widgetTextureId;
    private int atlasTextureId;

    private TextureAtlas textureAtlas;
    private IsoCubeMesh isoCubeMesh;
    private IsoCubeFBO isoCubeFBO;

    private Matrix4f orthoProjection;
    private Matrix4f view;
    private ShaderProgram itemShader;

    public PlayerUI(UIRenderer uiRenderer, EntityPlayer player) {
        this.uiRenderer = uiRenderer;
        this.player = player;
        this.barTextureId = this.uiRenderer.loadTexture("src/main/resources/textures/bar.png", true);
        this.iconsTextureId = this.uiRenderer.loadTexture("src/main/resources/textures/icons.png", true);
        this.widgetTextureId = this.uiRenderer.loadTexture("src/main/resources/textures/widgets.png", true);
        this.atlasTextureId = this.uiRenderer.loadTexture("src/main/resources/textures/atlas.png", true);
        this.orthoProjection = new Matrix4f().ortho(0, Window.getInstance().getWidth(),
                Window.getInstance().getHeight(), 0,
                -100f, 100f);
        this.view = new Matrix4f().identity();
        int vertexShader = Shader.loadShader(GL30.GL_VERTEX_SHADER, "src/main/resources/shaders/isometric.vert").getID();
        int fragmentShader = Shader.loadShader(GL30.GL_FRAGMENT_SHADER, "src/main/resources/shaders/isometric.frag").getID();
        this.itemShader = new ShaderProgram(vertexShader, fragmentShader);
        this.textureAtlas = new TextureAtlas("src/main/resources/textures/atlas.png");
        this.isoCubeMesh = new IsoCubeMesh();
        this.isoCubeMesh.init();
        this.isoCubeFBO = new IsoCubeFBO(1024);
    }

    public void renderItemBar() {
        uiRenderer.drawTexturedQuad(Window.getInstance().getWidth() / 2.f - 182,
                Window.getInstance().getHeight() - 22 * 2,
                182,
                22,
                2,
                barTextureId,
                new float[]{0.f, 0.f, 1.f, 0.f, 1.f, 1.f, 0.f, 1.f},
                new float[]{1.f, 1.f, 1.f, 1.f});
    }

    public void renderCrosshair() {
        uiRenderer.drawTexturedQuad(Window.getInstance().getWidth() / 2.f - 8,
                Window.getInstance().getHeight() / 2.f - 8,
                16,
                16,
                2,
                iconsTextureId,
                new float[]{
                        0.f, 0.f,
                        0.0625f, 0.f,
                        0.0625f, 0.0625f,
                        0.f, 0.0625f
                },
                new float[]{1.f, 1.f, 1.f, 1.f});
    }

    public void renderHealth() {
        int health = (int) player.getHealth();
        for (int i = 0; i < 10; i++) {
            uiRenderer.drawTexturedQuad(
                    Window.getInstance().getWidth() / 2.f + i * 18 - 180,
                    Window.getInstance().getHeight() - 22 * 2 - 18,
                    9,
                    9,
                    2.f,
                    iconsTextureId,
                    new float[]{
                            16/256.f, 0/256.f,
                            25/256.f, 0/256.f,
                            25/256.f, 9/256.f,
                            16/256.f, 9/256.f
                    },
                    new float[]{1.f, 1.f, 1.f, 1.f}
            );
        }

        for (int i = 0; i < health / 2; i++) {
            uiRenderer.drawTexturedQuad(
                    Window.getInstance().getWidth() / 2.f + i * 18 - 180,
                    Window.getInstance().getHeight() - 22 * 2 - 18,
                    9,
                    9,
                    2.f,
                    iconsTextureId,
                    new float[]{
                            52/256.f, 0/256.f,
                            61/256.f, 0/256.f,
                            61/256.f, 9/256.f,
                            52/256.f, 9/256.f
                    },
                    new float[]{1.f, 1.f, 1.f, 1.f}
            );
        }
    }

    public void renderCurrentSlot() {
        int slotOffset = Game.currentSlot * 40;

        uiRenderer.drawTexturedQuad(
                Window.getInstance().getWidth() / 2.f - 182 + slotOffset - 2,
                Window.getInstance().getHeight() - 22 * 2 - 1,
                24,
                24,
                2.f,
                widgetTextureId,
                new float[] {
                        0.f / 256, 22.f / 256,
                        0.f / 256, 46.f / 256,
                        24.f / 256, 46.f / 256,
                        24.f / 256, 22.f / 256
                },
                new float[] {1.f, 1.f, 1.f, 1.f}
        );
    }

    public void renderBlockInSlot(int slot, BlockType blockType) {
        int topIndex   = blockType.getTopTexture();
        int westIndex  = blockType.getWestTexture();
        int southIndex = blockType.getSouthTexture();

        float[] uvTop   = textureAtlas.getUVs(topIndex);
        float[] uvLeft  = textureAtlas.getUVs(westIndex);
        float[] uvRight = textureAtlas.getUVs(southIndex);

        isoCubeFBO.bind();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        int fboSize = isoCubeFBO.getSize();
        Matrix4f fboProj = new Matrix4f().ortho(-1.2f, 1.2f, -1.2f, 1.2f, -10f, 10f);
        Matrix4f fboView = new Matrix4f().identity();
        Matrix4f model = new Matrix4f()
                .rotateX((float) -Math.atan(1.0 / Math.sqrt(2)))
                .rotateY((float) Math.toRadians(-135))
                .scale(1f);

        int pid = itemShader.getID();
        itemShader.use();

        float[] buf = new float[16];
        model.get(buf);
        glUniformMatrix4fv(glGetUniformLocation(pid, "model"), false, buf);
        fboProj.get(buf);
        glUniformMatrix4fv(glGetUniformLocation(pid, "projection"), false, buf);
        fboView.get(buf);
        glUniformMatrix4fv(glGetUniformLocation(pid, "view"), false, buf);
        glUniform1i(glGetUniformLocation(pid, "textureSampler"), 0);

        textureAtlas.bind();
        isoCubeMesh.updateAndDraw(uvTop, uvLeft, uvRight);

        glDisable(GL_DEPTH_TEST);
        isoCubeFBO.unbind(
                Window.getInstance().getWidth(),
                Window.getInstance().getHeight()
        );

        float slotX = Window.getInstance().getWidth() / 2.f - 182 + slot * 40 + 4 - 4;
        float slotY = Window.getInstance().getHeight() - 42;

        uiRenderer.drawTexturedQuad(
                slotX, slotY,
                fboSize / 2.f, fboSize / 2.f,
                0.08f,
                isoCubeFBO.getColorTextureId(),
                new float[]{ 0f,1f, 1f,1f, 1f,0f, 0f,0f },
                new float[]{ 1f, 1f, 1f, 1f }
        );
    }


    public void render() {
        this.renderItemBar();
        this.renderCurrentSlot();
        this.renderCrosshair();
        this.renderHealth();
        this.renderBlockInSlot(0, BlockType.GRASS);
    }
}
