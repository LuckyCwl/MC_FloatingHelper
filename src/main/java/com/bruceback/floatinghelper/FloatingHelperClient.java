package com.bruceback.floatinghelper;

import com.bruceback.floatinghelper.config.FloatingHelperConfig;
import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
import com.bruceback.floatinghelper.renderer.FloatingIconWidget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.TitleScreen;

public class FloatingHelperClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        FloatingHelperConfigManager.load();
        FloatingIconWidget.reloadTexture();

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                ScreenEvents.afterRender(screen).register((currentScreen, drawContext, mouseX, mouseY, tickDelta) -> {
                    FloatingHelperConfig config = FloatingHelperConfigManager.get();

                    if (!config.showOnTitleScreen) {
                        return;
                    }

                    FloatingHelperConfigManager.ensureValidBounds(currentScreen.width, currentScreen.height);
                    FloatingIconWidget.render(drawContext, config.x, config.y, config.width, config.height);
                });
            }
        });
    }
}
