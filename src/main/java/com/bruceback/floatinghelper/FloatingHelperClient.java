package com.bruceback.floatinghelper;

import com.bruceback.floatinghelper.config.FloatingHelperConfig;
import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
import com.bruceback.floatinghelper.renderer.FloatingIconWidget;
import com.bruceback.floatinghelper.screen.FloatingHelperConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class FloatingHelperClient implements ClientModInitializer {
    private static final KeyBinding.Category KEY_CATEGORY = KeyBinding.Category.create(Identifier.of("floatinghelper", "controls"));

    private KeyBinding retractKey;
    private KeyBinding snapSidebarKey;
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
        retractKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.floatinghelper.retract",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                KEY_CATEGORY
        ));

        snapSidebarKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.floatinghelper.snap_sidebar",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
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

    private void registerKeyHandlers() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (retractKey.wasPressed()) {
                toggleSidebar(client);
            }

            while (snapSidebarKey.wasPressed()) {
                snapToNearestSidebar(client);
            }

            while (openConfigKey.wasPressed()) {
                openConfigScreen(client);
            }
        });
    }

    private static void toggleSidebar(MinecraftClient client) {
        FloatingHelperConfig config = FloatingHelperConfigManager.get();
        ScreenSize size = getScreenSize(client);
        FloatingHelperConfigManager.ensureValidBounds(config, size.width(), size.height());
        config.collapsedToSidebar = !config.collapsedToSidebar;
        FloatingHelperConfigManager.update(config);
    }

    private static void snapToNearestSidebar(MinecraftClient client) {
        FloatingHelperConfig config = FloatingHelperConfigManager.get();
        ScreenSize size = getScreenSize(client);
        FloatingHelperConfigManager.ensureValidBounds(config, size.width(), size.height());
        config.collapsedToSidebar = true;
        FloatingHelperConfigManager.snapToNearestSide(config, size.width(), size.height());
        FloatingHelperConfigManager.update(config);
    }

    private static void openConfigScreen(MinecraftClient client) {
        if (client.currentScreen instanceof FloatingHelperConfigScreen) {
            return;
        }

        client.setScreen(new FloatingHelperConfigScreen(client.currentScreen));
    }

    private static ScreenSize getScreenSize(MinecraftClient client) {
        if (client.currentScreen != null) {
            return new ScreenSize(client.currentScreen.width, client.currentScreen.height);
        }

        return new ScreenSize(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight());
    }

    private static void renderFloatingIcon(DrawContext drawContext, int screenWidth, int screenHeight) {
        FloatingHelperConfig config = FloatingHelperConfigManager.get();

        if (!config.showOnTitleScreen) {
            return;
        }

        FloatingHelperConfigManager.ensureValidBounds(screenWidth, screenHeight);
        FloatingIconWidget.render(
                drawContext,
                config.x,
                config.y,
                config.width,
                config.height,
                config.mirrored,
                config.collapsedToSidebar
        );
    }

    private record ScreenSize(int width, int height) {
    }
}
