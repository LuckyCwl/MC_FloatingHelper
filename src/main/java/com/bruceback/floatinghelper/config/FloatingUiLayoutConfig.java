package com.bruceback.floatinghelper.config;

public class FloatingUiLayoutConfig {
    public int x = -1;
    public int y = FloatingHelperConfig.DEFAULT_MARGIN;
    public int width = FloatingHelperConfig.DEFAULT_WIDTH;
    public int height = FloatingHelperConfig.DEFAULT_HEIGHT;
    public double relativeX = -1.0D;
    public double relativeY = -1.0D;
    public boolean mirrored = false;

    public FloatingUiLayoutConfig copy() {
        FloatingUiLayoutConfig copy = new FloatingUiLayoutConfig();
        copy.x = x;
        copy.y = y;
        copy.width = width;
        copy.height = height;
        copy.relativeX = relativeX;
        copy.relativeY = relativeY;
        copy.mirrored = mirrored;
        return copy;
    }
}
