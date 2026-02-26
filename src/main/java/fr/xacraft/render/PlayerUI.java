package fr.xacraft.render;

import fr.xacraft.client.Game;
import fr.xacraft.client.Window;
import fr.xacraft.entity.EntityPlayer;

public class PlayerUI {

    private UIRenderer uiRenderer;
    private EntityPlayer player;
    private int barTextureId;
    private int iconsTextureId;
    private int widgetTextureId;

    public PlayerUI(UIRenderer uiRenderer, EntityPlayer player) {
        this.uiRenderer = uiRenderer;
        this.player = player;
        this.barTextureId = this.uiRenderer.loadTexture("src/main/resources/textures/bar.png", true);
        this.iconsTextureId = this.uiRenderer.loadTexture("src/main/resources/textures/icons.png", true);
        this.widgetTextureId = this.uiRenderer.loadTexture("src/main/resources/textures/widgets.png", true);
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
                Window.getInstance().getWidth() / 2.f - 182 + slotOffset - 1,
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

    public void render() {
        this.renderItemBar();
        this.renderCurrentSlot();
        this.renderCrosshair();
        this.renderHealth();
    }
}
