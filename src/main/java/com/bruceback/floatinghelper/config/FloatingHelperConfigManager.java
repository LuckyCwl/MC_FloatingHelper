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
        ensureValidBounds(config, screenWidth, screenHeight);
    }

    public static void ensureValidBounds(FloatingHelperConfig target, int screenWidth, int screenHeight) {
        if (target.width < FloatingHelperConfig.MIN_SIZE) {
            target.width = FloatingHelperConfig.DEFAULT_WIDTH;
        }

        if (target.height < FloatingHelperConfig.MIN_SIZE) {
            target.height = FloatingHelperConfig.DEFAULT_HEIGHT;
        }

        target.width = Math.min(target.width, Math.max(FloatingHelperConfig.MIN_SIZE, screenWidth));
        target.height = Math.min(target.height, Math.max(FloatingHelperConfig.MIN_SIZE, screenHeight));

        int maxX = Math.max(0, screenWidth - target.width);
        int maxY = Math.max(0, screenHeight - target.height);

        if (!isValidRelative(target.relativeX) || !isValidRelative(target.relativeY)) {
            if (target.x < 0) {
                target.x = Math.max(0, screenWidth - target.width - FloatingHelperConfig.DEFAULT_MARGIN);
            }

            if (target.y < 0) {
                target.y = Math.max(0, screenHeight - target.height - FloatingHelperConfig.DEFAULT_MARGIN);
            }

            target.x = Math.max(0, Math.min(target.x, maxX));
            target.y = Math.max(0, Math.min(target.y, maxY));
            updateRelativePosition(target, screenWidth, screenHeight);
        }

        applyRelativePosition(target, screenWidth, screenHeight);
    }

    public static void updateRelativePosition(FloatingHelperConfig target, int screenWidth, int screenHeight) {
        int maxX = Math.max(0, screenWidth - target.width);
        int maxY = Math.max(0, screenHeight - target.height);
        target.x = Math.max(0, Math.min(target.x, maxX));
        target.y = Math.max(0, Math.min(target.y, maxY));
        target.relativeX = maxX == 0 ? 0.0D : (double) target.x / maxX;
        target.relativeY = maxY == 0 ? 0.0D : (double) target.y / maxY;
    }

    public static void applyRelativePosition(FloatingHelperConfig target, int screenWidth, int screenHeight) {
        int maxX = Math.max(0, screenWidth - target.width);
        int maxY = Math.max(0, screenHeight - target.height);
        target.relativeX = clampRelative(target.relativeX);
        target.relativeY = clampRelative(target.relativeY);
        target.x = maxX == 0 ? 0 : (int) Math.round(target.relativeX * maxX);
        target.y = maxY == 0 ? 0 : (int) Math.round(target.relativeY * maxY);
    }

    public static Path getConfigDir() {
        return CONFIG_DIR;
    }

    private static boolean isValidRelative(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && value >= 0.0D && value <= 1.0D;
    }

    private static double clampRelative(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 0.0D;
        }

        return Math.max(0.0D, Math.min(value, 1.0D));
    }
}
