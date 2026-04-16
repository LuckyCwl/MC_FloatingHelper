package com.bruceback.floatinghelper;

import com.bruceback.floatinghelper.config.FloatingHelperConfig;
import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
import com.bruceback.floatinghelper.renderer.FloatingIconWidget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;

public class FloatingHelperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FloatingHelperConfigManager.load();
        FloatingIconWidget.reloadTexture();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                ScreenEvents.afterRender(screen).register((currentScreen, drawContext, mouseX, mouseY, tickDelta) -> {
                    renderFloatingIcon(drawContext, currentScreen.width, currentScreen.height);
                });
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null || client.world == null) {
                return;
            }

            renderFloatingIcon(drawContext, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        });
    }

    private static void renderFloatingIcon(net.minecraft.client.gui.DrawContext drawContext, int screenWidth, int screenHeight) {
        FloatingHelperConfig config = FloatingHelperConfigManager.get();

        if (!config.showOnTitleScreen) {
            return;
        }

        FloatingHelperConfigManager.ensureValidBounds(screenWidth, screenHeight);
        FloatingIconWidget.render(drawContext, config.x, config.y, config.width, config.height);
    }
}
