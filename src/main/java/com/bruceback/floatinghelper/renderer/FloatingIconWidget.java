package com.bruceback.floatinghelper.renderer;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public final class FloatingIconWidget {
    private static final Identifier ICON_TEXTURE = Identifier.of("floatinghelper", "textures/gui/icon.png");

    private FloatingIconWidget() {
    }

    public static void render(DrawContext context, int x, int y, int size) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, ICON_TEXTURE, x, y, 0.0F, 0.0F, size, size, size, size);
    }
}
