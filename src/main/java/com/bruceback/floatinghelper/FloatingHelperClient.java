package com.bruceback.floatinghelper;

import com.bruceback.floatinghelper.config.FloatingHelperConfig;
import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
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
                    renderFloatingIcon(drawContext, currentScreen.width, currentScreen.height);

                    if (FloatingHelperConfigManager.get().showOnTitleScreen) {
                        FloatingHelperTitleDialog.render(drawContext, client.textRenderer, currentScreen.width, currentScreen.height);
                    }
                });

                ScreenMouseEvents.afterMouseClick(screen).register((currentScreen, click, doubleClick) -> {
                    if (FloatingHelperConfigManager.get().showOnTitleScreen
                            && isInsideFloatingIcon(click.x(), click.y(), currentScreen.width, currentScreen.height)) {
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

            renderFloatingIcon(drawContext, client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
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

    private static void renderFloatingIcon(DrawContext drawContext, int screenWidth, int screenHeight) {
        FloatingHelperConfig config = FloatingHelperConfigManager.get();

        if (!config.showOnTitleScreen) {
            return;
        }

        FloatingHelperConfigManager.ensureValidBounds(screenWidth, screenHeight);
        boolean effectiveMirrored = config.mirrored ^ shouldAutoMirror(config, screenWidth);
        FloatingIconWidget.render(drawContext, config.x, config.y, config.width, config.height, effectiveMirrored);
    }

    private static boolean shouldAutoMirror(FloatingHelperConfig config, int screenWidth) {
        int centerX = config.x + config.width / 2;
        return centerX > screenWidth / 2;
    }

    private static boolean isInsideFloatingIcon(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        FloatingHelperConfig config = FloatingHelperConfigManager.get();
        FloatingHelperConfigManager.ensureValidBounds(screenWidth, screenHeight);
        return mouseX >= config.x
                && mouseX <= config.x + config.width
                && mouseY >= config.y
                && mouseY <= config.y + config.height;
    }
}
