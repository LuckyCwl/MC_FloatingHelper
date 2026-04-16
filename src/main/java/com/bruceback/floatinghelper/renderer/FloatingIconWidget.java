package com.bruceback.floatinghelper.renderer;

import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FloatingIconWidget {
    private static final Identifier BUILTIN_TEXTURE = Identifier.of("floatinghelper", "textures/gui/icon.png");
    private static final Identifier CUSTOM_TEXTURE = Identifier.of("floatinghelper", "dynamic/custom_icon");
    private static Identifier activeTexture = BUILTIN_TEXTURE;
    private static NativeImageBackedTexture dynamicTexture;

    private FloatingIconWidget() {
    }

    public static void reloadTexture() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client == null) {
            return;
        }

        if (dynamicTexture != null) {
            dynamicTexture.close();
            client.getTextureManager().destroyTexture(CUSTOM_TEXTURE);
            dynamicTexture = null;
        }

        String customPath = FloatingHelperConfigManager.get().customImagePath;

        if (customPath == null || customPath.isBlank()) {
            activeTexture = BUILTIN_TEXTURE;
            return;
        }

        Path path = Path.of(customPath);

        if (!Files.exists(path)) {
            activeTexture = BUILTIN_TEXTURE;
            return;
        }

        try {
            BufferedImage bufferedImage = ImageIO.read(path.toFile());

            if (bufferedImage == null) {
                activeTexture = BUILTIN_TEXTURE;
                return;
            }

            NativeImage image = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), true);

            for (int y = 0; y < bufferedImage.getHeight(); y++) {
                for (int x = 0; x < bufferedImage.getWidth(); x++) {
                    image.setColorArgb(x, y, bufferedImage.getRGB(x, y));
                }
            }

            dynamicTexture = new NativeImageBackedTexture(() -> "floatinghelper-custom-icon", image);
            client.getTextureManager().registerTexture(CUSTOM_TEXTURE, dynamicTexture);
            activeTexture = CUSTOM_TEXTURE;
        } catch (IOException exception) {
            activeTexture = BUILTIN_TEXTURE;
        }
    }

    public static void render(DrawContext context, int x, int y, int width, int height) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, activeTexture, x, y, 0.0F, 0.0F, width, height, width, height);
    }
}
