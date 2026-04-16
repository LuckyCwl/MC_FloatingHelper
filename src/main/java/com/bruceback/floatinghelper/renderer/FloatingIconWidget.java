package com.bruceback.floatinghelper.renderer;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class FloatingIconWidget {
    private static final Identifier BUILTIN_TEXTURE = Identifier.of("floatinghelper", "textures/gui/icon.png");

    private FloatingIconWidget() {
    }

    public static void reloadTexture() {
        // The mod now always uses the built-in icon texture.
    }

    public static void render(DrawContext context, int x, int y, int width, int height) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, BUILTIN_TEXTURE, x, y, 0.0F, 0.0F, width, height, width, height);
    }
}
