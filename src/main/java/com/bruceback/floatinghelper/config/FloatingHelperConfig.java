package com.bruceback.floatinghelper.config;

public class FloatingHelperConfig {
    public static final int DEFAULT_MARGIN = 12;
    public static final int DEFAULT_WIDTH = 72;
    public static final int DEFAULT_HEIGHT = 72;
    public static final int MIN_SIZE = 24;

    public boolean showOnTitleScreen = true;
    public int x = -1;
    public int y = DEFAULT_MARGIN;
    public int width = DEFAULT_WIDTH;
    public int height = DEFAULT_HEIGHT;
    public double relativeX = -1.0D;
    public double relativeY = -1.0D;
    public boolean mirrored = false;

    public int textX = -1;
    public int textY = -1;
    public double textRelativeX = -1.0D;
    public double textRelativeY = -1.0D;
}
