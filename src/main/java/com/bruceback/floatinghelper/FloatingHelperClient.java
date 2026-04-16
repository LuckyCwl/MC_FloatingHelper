package com.bruceback.floatinghelper;

import com.bruceback.floatinghelper.renderer.FloatingIconWidget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.TitleScreen;

public class FloatingHelperClient implements ClientModInitializer {
    private static final int ICON_SIZE = 28;
    private static final int MARGIN = 12;

    @Override
    public void onInitializeClient() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                ScreenEvents.afterRender(screen).register((currentScreen, drawContext, mouseX, mouseY, tickDelta) -> {
                    int x = currentScreen.width - ICON_SIZE - MARGIN;
                    int y = MARGIN;
                    FloatingIconWidget.render(drawContext, x, y, ICON_SIZE);
                });
            }
        });
    }
}
