package com.bruceback.floatinghelper.renderer;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class FloatingIconWidget {
    private static final Identifier UI_TEXTURE = Identifier.of("floatinghelper", "textures/gui/yc_ui.png");

    private FloatingIconWidget() {
    }

    public static void reloadTexture() {
        // The mod now always uses the built-in yc_ui texture.
    }

    public static void render(DrawContext context, int x, int y, int width, int height, boolean mirrored) {
        if (!mirrored) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, UI_TEXTURE, x, y, 0.0F, 0.0F, width, height, width, height);
            return;
        }

        context.drawTexturedQuad(UI_TEXTURE, x, y, x + width, y + height, 1.0F, 0.0F, 0.0F, 1.0F);
    }
}
