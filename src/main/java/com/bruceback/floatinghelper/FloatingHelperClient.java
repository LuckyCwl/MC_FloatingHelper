package com.bruceback.floatinghelper;

import com.bruceback.floatinghelper.config.FloatingHelperConfig;
import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
import com.bruceback.floatinghelper.config.FloatingUiLayoutConfig;
import com.bruceback.floatinghelper.dialog.FloatingHelperTitleDialog;
import com.bruceback.floatinghelper.renderer.FloatingIconWidget;
import com.bruceback.floatinghelper.screen.FloatingHelperConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class FloatingHelperClient implements ClientModInitializer {
    private static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(Identifier.of("floatinghelper", "controls"));

    private KeyBinding toggleVisibilityKey;
    private KeyBinding openConfigKey;

    @Override
    public void onInitializeClient() {
        FloatingHelperConfigManager.load();
        FloatingIconWidget.reloadTexture();
        registerKeyBindings();
        registerRenderers();
        registerKeyHandlers();
    }

    private void registerKeyBindings() {
        toggleVisibilityKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.floatinghelper.toggle_visibility",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                KEY_CATEGORY
        ));

        openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.floatinghelper.open_config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                KEY_CATEGORY
        ));
    }

    private void registerRenderers() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen) {
                FloatingHelperTitleDialog.resetForTitleScreen();

                ScreenEvents.afterRender(screen).register((currentScreen, drawContext, mouseX, mouseY, tickDelta) -> {
                    renderTitleUi(drawContext, currentScreen.width, currentScreen.height);

                    if (FloatingHelperConfigManager.get().showOnTitleScreen) {
                        FloatingHelperTitleDialog.render(drawContext, client.textRenderer, FloatingHelperConfigManager.get(), currentScreen.width, currentScreen.height);
                    }
                });

                ScreenMouseEvents.afterMouseClick(screen).register((currentScreen, click, doubleClick) -> {
                    if (FloatingHelperConfigManager.get().showOnTitleScreen
                            && isInsideFloatingIcon(click.x(), click.y(), currentScreen.width, currentScreen.height, FloatingHelperConfigManager.get().titleUi)) {
                        FloatingHelperTitleDialog.advanceMessage();
                    }

                    return true;
                });
            }
        });

        HudRenderCallback.EVENT.register((drawContext, tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();

            if (client.player == null || client.world == null) {
                return;
            }

            renderInGameUi(drawContext, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
        });
    }

    private void registerKeyHandlers() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleVisibilityKey.wasPressed()) {
                toggleFloatingHelperVisibility();
            }

            while (openConfigKey.wasPressed()) {
                openConfigScreen(client);
            }
        });
    }

    private static void toggleFloatingHelperVisibility() {
        FloatingHelperConfig config = FloatingHelperConfigManager.get();
        config.showOnTitleScreen = !config.showOnTitleScreen;
        FloatingHelperConfigManager.update(config);
    }

    private static void openConfigScreen(MinecraftClient client) {
        if (client.currentScreen instanceof FloatingHelperConfigScreen) {
            return;
        }

        client.setScreen(new FloatingHelperConfigScreen(client.currentScreen));
    }

    private static void renderTitleUi(DrawContext drawContext, int screenWidth, int screenHeight) {
        FloatingHelperConfig config = FloatingHelperConfigManager.get();

        if (!config.showOnTitleScreen) {
            return;
        }

        renderLayout(drawContext, config.titleUi, screenWidth, screenHeight);
    }

    private static void renderInGameUi(DrawContext drawContext, int screenWidth, int screenHeight) {
        FloatingHelperConfig config = FloatingHelperConfigManager.get();

        if (!config.showOnTitleScreen) {
            return;
        }

        renderLayout(drawContext, config.inGameUi, screenWidth, screenHeight);
    }

    private static void renderLayout(DrawContext drawContext, FloatingUiLayoutConfig layout, int screenWidth, int screenHeight) {
        FloatingHelperConfigManager.ensureValidBounds(layout, screenWidth, screenHeight);
        boolean effectiveMirrored = layout.mirrored ^ shouldAutoMirror(layout, screenWidth);
        FloatingIconWidget.render(drawContext, layout.x, layout.y, layout.width, layout.height, effectiveMirrored);
    }

    private static boolean shouldAutoMirror(FloatingUiLayoutConfig layout, int screenWidth) {
        int centerX = layout.x + layout.width / 2;
        return centerX > screenWidth / 2;
    }

    private static boolean isInsideFloatingIcon(double mouseX, double mouseY, int screenWidth, int screenHeight, FloatingUiLayoutConfig layout) {
        FloatingHelperConfigManager.ensureValidBounds(layout, screenWidth, screenHeight);
        return mouseX >= layout.x
                && mouseX <= layout.x + layout.width
                && mouseY >= layout.y
                && mouseY <= layout.y + layout.height;
    }
}
