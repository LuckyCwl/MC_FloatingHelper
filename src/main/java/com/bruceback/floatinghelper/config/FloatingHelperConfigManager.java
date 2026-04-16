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

        migrateLegacyLayout(config);
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

    public static void ensureValidBounds(FloatingUiLayoutConfig target, int screenWidth, int screenHeight) {
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

    public static void updateRelativePosition(FloatingUiLayoutConfig target, int screenWidth, int screenHeight) {
        int maxX = Math.max(0, screenWidth - target.width);
        int maxY = Math.max(0, screenHeight - target.height);
        target.x = Math.max(0, Math.min(target.x, maxX));
        target.y = Math.max(0, Math.min(target.y, maxY));
        target.relativeX = maxX == 0 ? 0.0D : (double) target.x / maxX;
        target.relativeY = maxY == 0 ? 0.0D : (double) target.y / maxY;
    }

    public static void applyRelativePosition(FloatingUiLayoutConfig target, int screenWidth, int screenHeight) {
        int maxX = Math.max(0, screenWidth - target.width);
        int maxY = Math.max(0, screenHeight - target.height);
        target.relativeX = clampRelative(target.relativeX);
        target.relativeY = clampRelative(target.relativeY);
        target.x = maxX == 0 ? 0 : (int) Math.round(target.relativeX * maxX);
        target.y = maxY == 0 ? 0 : (int) Math.round(target.relativeY * maxY);
    }

    public static void updateTextRelativePosition(FloatingHelperConfig target, int screenWidth, int screenHeight, int textWidth, int textHeight) {
        int maxX = Math.max(0, screenWidth - textWidth);
        int maxY = Math.max(0, screenHeight - textHeight);
        target.textX = Math.max(0, Math.min(target.textX, maxX));
        target.textY = Math.max(0, Math.min(target.textY, maxY));
        target.textRelativeX = maxX == 0 ? 0.0D : (double) target.textX / maxX;
        target.textRelativeY = maxY == 0 ? 0.0D : (double) target.textY / maxY;
    }

    public static int resolveTextX(FloatingHelperConfig target, int screenWidth, int textWidth, int defaultX) {
        int maxX = Math.max(0, screenWidth - textWidth);
        if (!isValidRelative(target.textRelativeX)) {
            if (target.textX < 0) {
                target.textX = Math.max(0, Math.min(defaultX, maxX));
            }
            target.textX = Math.max(0, Math.min(target.textX, maxX));
            return target.textX;
        }

        target.textRelativeX = clampRelative(target.textRelativeX);
        return maxX == 0 ? 0 : (int) Math.round(target.textRelativeX * maxX);
    }

    public static int resolveTextY(FloatingHelperConfig target, int screenHeight, int textHeight, int defaultY) {
        int maxY = Math.max(0, screenHeight - textHeight);
        if (!isValidRelative(target.textRelativeY)) {
            if (target.textY < 0) {
                target.textY = Math.max(0, Math.min(defaultY, maxY));
            }
            target.textY = Math.max(0, Math.min(target.textY, maxY));
            return target.textY;
        }

        target.textRelativeY = clampRelative(target.textRelativeY);
        return maxY == 0 ? 0 : (int) Math.round(target.textRelativeY * maxY);
    }

    public static Path getConfigDir() {
        return CONFIG_DIR;
    }

    private static void migrateLegacyLayout(FloatingHelperConfig target) {
        if (target.titleUi == null) {
            target.titleUi = new FloatingUiLayoutConfig();
        }

        if (target.inGameUi == null) {
            target.inGameUi = new FloatingUiLayoutConfig();
        }

        boolean hasLegacyLayout = target.x >= 0 || isValidRelative(target.relativeX) || isValidRelative(target.relativeY)
                || target.width != FloatingHelperConfig.DEFAULT_WIDTH
                || target.height != FloatingHelperConfig.DEFAULT_HEIGHT
                || target.mirrored;

        if (hasLegacyLayout) {
            if (isUnset(target.titleUi)) {
                copyLegacyLayout(target, target.titleUi);
            }

            if (isUnset(target.inGameUi)) {
                copyLegacyLayout(target, target.inGameUi);
            }
        }
    }

    private static void copyLegacyLayout(FloatingHelperConfig source, FloatingUiLayoutConfig target) {
        target.x = source.x;
        target.y = source.y;
        target.width = source.width;
        target.height = source.height;
        target.relativeX = source.relativeX;
        target.relativeY = source.relativeY;
        target.mirrored = source.mirrored;
    }

    private static boolean isUnset(FloatingUiLayoutConfig layout) {
        return layout.x < 0
                && !isValidRelative(layout.relativeX)
                && !isValidRelative(layout.relativeY)
                && layout.width == FloatingHelperConfig.DEFAULT_WIDTH
                && layout.height == FloatingHelperConfig.DEFAULT_HEIGHT
                && !layout.mirrored;
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
