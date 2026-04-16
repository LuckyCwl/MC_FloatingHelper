package com.bruceback.floatinghelper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FloatingHelperConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve("floatinghelper");
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve("floatinghelper.json");
    private static FloatingHelperConfig config = new FloatingHelperConfig();

    private FloatingHelperConfigManager() {
    }

    public static void load() {
        try {
            Files.createDirectories(CONFIG_DIR);

            if (Files.exists(CONFIG_FILE)) {
                try (Reader reader = Files.newBufferedReader(CONFIG_FILE)) {
                    FloatingHelperConfig loaded = GSON.fromJson(reader, FloatingHelperConfig.class);
                    config = loaded != null ? loaded : new FloatingHelperConfig();
                }
            } else {
                config = new FloatingHelperConfig();
                save();
            }
        } catch (IOException exception) {
            config = new FloatingHelperConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_DIR);

            try (Writer writer = Files.newBufferedWriter(CONFIG_FILE)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static FloatingHelperConfig get() {
        return config;
    }

    public static void update(FloatingHelperConfig newConfig) {
        config = newConfig;
        save();
    }

    public static void ensureValidBounds(int screenWidth, int screenHeight) {
        if (config.width < FloatingHelperConfig.MIN_SIZE) {
            config.width = FloatingHelperConfig.DEFAULT_WIDTH;
        }

        if (config.height < FloatingHelperConfig.MIN_SIZE) {
            config.height = FloatingHelperConfig.DEFAULT_HEIGHT;
        }

        if (config.x < 0) {
            config.x = screenWidth - config.width - FloatingHelperConfig.DEFAULT_MARGIN;
        }

        if (config.y < 0) {
            config.y = FloatingHelperConfig.DEFAULT_MARGIN;
        }

        config.x = Math.max(0, Math.min(config.x, Math.max(0, screenWidth - config.width)));
        config.y = Math.max(0, Math.min(config.y, Math.max(0, screenHeight - config.height)));
    }

    public static Path getConfigDir() {
        return CONFIG_DIR;
    }
}
