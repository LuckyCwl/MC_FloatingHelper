package com.bruceback.floatinghelper.screen;

import com.bruceback.floatinghelper.config.FloatingHelperConfig;
import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
import com.bruceback.floatinghelper.renderer.FloatingIconWidget;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class FloatingHelperLayoutScreen extends Screen {
    private static final int HANDLE_SIZE = 8;

    private final Screen parent;
    private final FloatingHelperConfig workingCopy;
    private DragMode dragMode = DragMode.NONE;
    private int dragOffsetX;
    private int dragOffsetY;
    private int anchorX;
    private int anchorY;

    public FloatingHelperLayoutScreen(Screen parent, FloatingHelperConfig workingCopy) {
        super(Text.literal("编辑 FloatingHelper 布局"));
        this.parent = parent;
        this.workingCopy = workingCopy;
    }

    @Override
    protected void init() {
        FloatingHelperConfigManager.ensureValidBounds(width, height);
        clampBounds();

        addDrawableChild(ButtonWidget.builder(Text.literal("重置"), button -> {
                    workingCopy.width = FloatingHelperConfig.DEFAULT_WIDTH;
                    workingCopy.height = FloatingHelperConfig.DEFAULT_HEIGHT;
                    workingCopy.x = width - workingCopy.width - FloatingHelperConfig.DEFAULT_MARGIN;
                    workingCopy.y = FloatingHelperConfig.DEFAULT_MARGIN;
                })
                .dimensions(width / 2 - 154, height - 28, 100, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("保存"), button -> {
                    clampBounds();
                    FloatingHelperConfigManager.update(workingCopy);
                    client.setScreen(parent);
                })
                .dimensions(width / 2 - 50, height - 28, 100, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("取消"), button -> client.setScreen(parent))
                .dimensions(width / 2 + 54, height - 28, 100, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderBackground(context, mouseX, mouseY, deltaTicks);
        context.fill(0, 0, width, height, 0x88000000);

        FloatingIconWidget.render(context, workingCopy.x, workingCopy.y, workingCopy.width, workingCopy.height);
        drawBorder(context, workingCopy.x - 1, workingCopy.y - 1, workingCopy.width + 2, workingCopy.height + 2, 0xFFFF4D4D);

        drawHandle(context, workingCopy.x, workingCopy.y, mouseX, mouseY);
        drawHandle(context, workingCopy.x + workingCopy.width, workingCopy.y, mouseX, mouseY);
        drawHandle(context, workingCopy.x, workingCopy.y + workingCopy.height, mouseX, mouseY);
        drawHandle(context, workingCopy.x + workingCopy.width, workingCopy.y + workingCopy.height, mouseX, mouseY);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("拖拽图案移动，拖拽红色角点缩放"), width / 2, 32, 0xE0E0E0);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (click.button() == 0) {
            int mouseX = (int) click.x();
            int mouseY = (int) click.y();
            dragMode = detectDragMode(mouseX, mouseY);

            if (dragMode == DragMode.MOVE) {
                dragOffsetX = mouseX - workingCopy.x;
                dragOffsetY = mouseY - workingCopy.y;
                return true;
            }

            if (dragMode != DragMode.NONE) {
                anchorX = workingCopy.x + (dragMode == DragMode.RESIZE_NW || dragMode == DragMode.RESIZE_SW ? workingCopy.width : 0);
                anchorY = workingCopy.y + (dragMode == DragMode.RESIZE_NW || dragMode == DragMode.RESIZE_NE ? workingCopy.height : 0);
                return true;
            }
        }

        return super.mouseClicked(click, doubleClick);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        if (click.button() != 0 || dragMode == DragMode.NONE) {
            return super.mouseDragged(click, deltaX, deltaY);
        }

        int mouseX = (int) click.x();
        int mouseY = (int) click.y();

        if (dragMode == DragMode.MOVE) {
            workingCopy.x = mouseX - dragOffsetX;
            workingCopy.y = mouseY - dragOffsetY;
            clampBounds();
            return true;
        }

        resizeFromCorner(mouseX, mouseY);
        clampBounds();
        return true;
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (click.button() == 0) {
            dragMode = DragMode.NONE;
        }

        return super.mouseReleased(click);
    }

    private void resizeFromCorner(int mouseX, int mouseY) {
        int newX = workingCopy.x;
        int newY = workingCopy.y;
        int newWidth = workingCopy.width;
        int newHeight = workingCopy.height;

        switch (dragMode) {
            case RESIZE_NW -> {
                newX = Math.min(mouseX, anchorX - FloatingHelperConfig.MIN_SIZE);
                newY = Math.min(mouseY, anchorY - FloatingHelperConfig.MIN_SIZE);
                newWidth = anchorX - newX;
                newHeight = anchorY - newY;
            }
            case RESIZE_NE -> {
                newY = Math.min(mouseY, anchorY - FloatingHelperConfig.MIN_SIZE);
                newWidth = Math.max(FloatingHelperConfig.MIN_SIZE, mouseX - anchorX);
                newHeight = anchorY - newY;
                newX = anchorX;
            }
            case RESIZE_SW -> {
                newX = Math.min(mouseX, anchorX - FloatingHelperConfig.MIN_SIZE);
                newWidth = anchorX - newX;
                newHeight = Math.max(FloatingHelperConfig.MIN_SIZE, mouseY - anchorY);
                newY = anchorY;
            }
            case RESIZE_SE -> {
                newX = anchorX;
                newY = anchorY;
                newWidth = Math.max(FloatingHelperConfig.MIN_SIZE, mouseX - anchorX);
                newHeight = Math.max(FloatingHelperConfig.MIN_SIZE, mouseY - anchorY);
            }
            default -> {
                return;
            }
        }

        workingCopy.x = newX;
        workingCopy.y = newY;
        workingCopy.width = Math.max(FloatingHelperConfig.MIN_SIZE, newWidth);
        workingCopy.height = Math.max(FloatingHelperConfig.MIN_SIZE, newHeight);
    }

    private void drawHandle(DrawContext context, int centerX, int centerY, int mouseX, int mouseY) {
        int x = centerX - HANDLE_SIZE / 2;
        int y = centerY - HANDLE_SIZE / 2;
        boolean hovered = mouseX >= x && mouseX <= x + HANDLE_SIZE && mouseY >= y && mouseY <= y + HANDLE_SIZE;
        int color = hovered ? 0xFFFF8080 : 0xFFFF4D4D;
        context.fill(x, y, x + HANDLE_SIZE, y + HANDLE_SIZE, color);
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.drawHorizontalLine(x, x + width - 1, y, color);
        context.drawHorizontalLine(x, x + width - 1, y + height - 1, color);
        context.drawVerticalLine(x, y, y + height - 1, color);
        context.drawVerticalLine(x + width - 1, y, y + height - 1, color);
    }

    private DragMode detectDragMode(int mouseX, int mouseY) {
        if (isOverHandle(mouseX, mouseY, workingCopy.x, workingCopy.y)) {
            return DragMode.RESIZE_NW;
        }

        if (isOverHandle(mouseX, mouseY, workingCopy.x + workingCopy.width, workingCopy.y)) {
            return DragMode.RESIZE_NE;
        }

        if (isOverHandle(mouseX, mouseY, workingCopy.x, workingCopy.y + workingCopy.height)) {
            return DragMode.RESIZE_SW;
        }

        if (isOverHandle(mouseX, mouseY, workingCopy.x + workingCopy.width, workingCopy.y + workingCopy.height)) {
            return DragMode.RESIZE_SE;
        }

        if (mouseX >= workingCopy.x && mouseX <= workingCopy.x + workingCopy.width
                && mouseY >= workingCopy.y && mouseY <= workingCopy.y + workingCopy.height) {
            return DragMode.MOVE;
        }

        return DragMode.NONE;
    }

    private boolean isOverHandle(int mouseX, int mouseY, int centerX, int centerY) {
        return mouseX >= centerX - HANDLE_SIZE && mouseX <= centerX + HANDLE_SIZE
                && mouseY >= centerY - HANDLE_SIZE && mouseY <= centerY + HANDLE_SIZE;
    }

    private void clampBounds() {
        workingCopy.width = MathHelper.clamp(workingCopy.width, FloatingHelperConfig.MIN_SIZE, width);
        workingCopy.height = MathHelper.clamp(workingCopy.height, FloatingHelperConfig.MIN_SIZE, height);
        workingCopy.x = MathHelper.clamp(workingCopy.x, 0, Math.max(0, width - workingCopy.width));
        workingCopy.y = MathHelper.clamp(workingCopy.y, 0, Math.max(0, height - workingCopy.height));
    }

    private enum DragMode {
        NONE,
        MOVE,
        RESIZE_NW,
        RESIZE_NE,
        RESIZE_SW,
        RESIZE_SE
    }
}
