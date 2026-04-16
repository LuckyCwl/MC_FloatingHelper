package com.bruceback.floatinghelper.renderer;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class FloatingIconWidget {
    private static final Identifier UI_TEXTURE = Identifier.of("floatinghelper", "textures/gui/yc_ui.png");
    private static final Identifier SIDEBAR_TEXTURE = Identifier.of("floatinghelper", "textures/gui/yc_sidebar.png");

    private FloatingIconWidget() {
    }

    public static void reloadTexture() {
        // The mod now always uses the built-in textures.
    }

    public static void render(DrawContext context, int x, int y, int width, int height, boolean mirrored, boolean collapsedToSidebar) {
        Identifier texture = collapsedToSidebar ? SIDEBAR_TEXTURE : UI_TEXTURE;

        if (!mirrored) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, width, height, width, height);
            return;
        }

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x + width, y);
        context.getMatrices().scale(-1.0F, 1.0F);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, 0, 0, 0.0F, 0.0F, width, height, width, height);
        context.getMatrices().popMatrix();
    }
}
