package com.bruceback.floatinghelper.screen;

import com.bruceback.floatinghelper.config.FloatingHelperConfig;
import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
import com.bruceback.floatinghelper.config.FloatingUiLayoutConfig;
import com.bruceback.floatinghelper.dialog.FloatingHelperTitleDialog;
import com.bruceback.floatinghelper.dialog.FloatingHelperTitleDialog.TextLayout;
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
    private final LayoutMode layoutMode;
    private DragMode dragMode = DragMode.NONE;
    private int dragOffsetX;
    private int dragOffsetY;
    private int anchorX;
    private int anchorY;
    private TextLayout textLayout;

    public FloatingHelperLayoutScreen(Screen parent, FloatingHelperConfig workingCopy, LayoutMode layoutMode) {
        super(Text.literal(layoutMode.title));
        this.parent = parent;
        this.workingCopy = workingCopy;
        this.layoutMode = layoutMode;
    }

    @Override
    protected void init() {
        FloatingHelperConfigManager.ensureValidBounds(selectedLayout(), width, height);
        clampBounds();
        int buttonWidth = 72;
        int buttonGap = 8;
        int totalWidth = buttonWidth * 4 + buttonGap * 3;
        int buttonLeft = (width - totalWidth) / 2;
        int buttonY = height - 28;

        addDrawableChild(ButtonWidget.builder(Text.literal("重置"), button -> {
                    FloatingUiLayoutConfig layout = selectedLayout();
                    layout.width = FloatingHelperConfig.DEFAULT_WIDTH;
                    layout.height = FloatingHelperConfig.DEFAULT_HEIGHT;
                    layout.x = width - layout.width - FloatingHelperConfig.DEFAULT_MARGIN;
                    layout.y = FloatingHelperConfig.DEFAULT_MARGIN;
                    layout.relativeX = -1.0D;
                    layout.relativeY = -1.0D;

                    if (layoutMode == LayoutMode.TITLE) {
                        workingCopy.textX = -1;
                        workingCopy.textY = -1;
                        workingCopy.textRelativeX = -1.0D;
                        workingCopy.textRelativeY = -1.0D;
                    }

                    FloatingHelperConfigManager.updateRelativePosition(layout, width, height);
                })
                .dimensions(buttonLeft, buttonY, buttonWidth, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(mirrorButtonText(), button -> {
                    selectedLayout().mirrored = !selectedLayout().mirrored;
                    button.setMessage(mirrorButtonText());
                })
                .dimensions(buttonLeft + buttonWidth + buttonGap, buttonY, buttonWidth, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("保存"), button -> {
                    clampBounds();
                    FloatingHelperConfigManager.updateRelativePosition(selectedLayout(), width, height);
                    if (layoutMode == LayoutMode.TITLE && textLayout != null) {
                        FloatingHelperConfigManager.updateTextRelativePosition(workingCopy, width, height, textLayout.textWidth(), textLayout.textHeight());
                    }
                    FloatingHelperConfigManager.update(workingCopy);
                    client.setScreen(parent);
                })
                .dimensions(buttonLeft + (buttonWidth + buttonGap) * 2, buttonY, buttonWidth, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("取消"), button -> client.setScreen(parent))
                .dimensions(buttonLeft + (buttonWidth + buttonGap) * 3, buttonY, buttonWidth, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, width, height, 0x88000000);

        FloatingUiLayoutConfig layout = selectedLayout();
        boolean effectiveMirrored = layout.mirrored ^ shouldAutoMirror(layout);
        FloatingIconWidget.render(context, layout.x, layout.y, layout.width, layout.height, effectiveMirrored);
        drawBorder(context, layout.x - 1, layout.y - 1, layout.width + 2, layout.height + 2, 0xFFFF4D4D);

        if (layoutMode == LayoutMode.TITLE) {
            FloatingHelperTitleDialog.render(context, textRenderer, workingCopy, width, height);
            textLayout = FloatingHelperTitleDialog.getLayout(textRenderer, workingCopy, width, height);
            drawBorder(context, textLayout.x() - 2, textLayout.y() - 2, textLayout.textWidth() + 4, textLayout.textHeight() + 4, 0xFF64E764);
        } else {
            textLayout = null;
        }

        drawHandle(context, layout.x, layout.y, mouseX, mouseY);
        drawHandle(context, layout.x + layout.width, layout.y, mouseX, mouseY);
        drawHandle(context, layout.x, layout.y + layout.height, mouseX, mouseY);
        drawHandle(context, layout.x + layout.width, layout.y + layout.height, mouseX, mouseY);

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal(layoutMode.description), width / 2, 32, 0xE0E0E0);
        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (click.button() == 0) {
            int mouseX = (int) click.x();
            int mouseY = (int) click.y();
            dragMode = detectDragMode(mouseX, mouseY);

            if (dragMode == DragMode.MOVE) {
                FloatingUiLayoutConfig layout = selectedLayout();
                dragOffsetX = mouseX - layout.x;
                dragOffsetY = mouseY - layout.y;
                return true;
            }

            if (dragMode == DragMode.MOVE_TEXT && textLayout != null) {
                dragOffsetX = mouseX - textLayout.x();
                dragOffsetY = mouseY - textLayout.y();
                return true;
            }

            if (dragMode != DragMode.NONE) {
                FloatingUiLayoutConfig layout = selectedLayout();
                anchorX = layout.x + (dragMode == DragMode.RESIZE_NW || dragMode == DragMode.RESIZE_SW ? layout.width : 0);
                anchorY = layout.y + (dragMode == DragMode.RESIZE_NW || dragMode == DragMode.RESIZE_NE ? layout.height : 0);
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
            FloatingUiLayoutConfig layout = selectedLayout();
            layout.x = mouseX - dragOffsetX;
            layout.y = mouseY - dragOffsetY;
            clampBounds();
            return true;
        }

        if (dragMode == DragMode.MOVE_TEXT && textLayout != null) {
            workingCopy.textX = mouseX - dragOffsetX;
            workingCopy.textY = mouseY - dragOffsetY;
            int maxX = Math.max(0, width - textLayout.textWidth());
            int maxY = Math.max(0, height - textLayout.textHeight());
            workingCopy.textX = MathHelper.clamp(workingCopy.textX, 0, maxX);
            workingCopy.textY = MathHelper.clamp(workingCopy.textY, 0, maxY);
            FloatingHelperConfigManager.updateTextRelativePosition(workingCopy, width, height, textLayout.textWidth(), textLayout.textHeight());
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
        FloatingUiLayoutConfig layout = selectedLayout();
        int newX = layout.x;
        int newY = layout.y;
        int newWidth = layout.width;
        int newHeight = layout.height;

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

        layout.x = newX;
        layout.y = newY;
        layout.width = Math.max(FloatingHelperConfig.MIN_SIZE, newWidth);
        layout.height = Math.max(FloatingHelperConfig.MIN_SIZE, newHeight);
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
        FloatingUiLayoutConfig layout = selectedLayout();

        if (isOverHandle(mouseX, mouseY, layout.x, layout.y)) {
            return DragMode.RESIZE_NW;
        }

        if (isOverHandle(mouseX, mouseY, layout.x + layout.width, layout.y)) {
            return DragMode.RESIZE_NE;
        }

        if (isOverHandle(mouseX, mouseY, layout.x, layout.y + layout.height)) {
            return DragMode.RESIZE_SW;
        }

        if (isOverHandle(mouseX, mouseY, layout.x + layout.width, layout.y + layout.height)) {
            return DragMode.RESIZE_SE;
        }

        if (layoutMode == LayoutMode.TITLE
                && textLayout != null
                && mouseX >= textLayout.x() && mouseX <= textLayout.x() + textLayout.textWidth()
                && mouseY >= textLayout.y() && mouseY <= textLayout.y() + textLayout.textHeight()) {
            return DragMode.MOVE_TEXT;
        }

        if (mouseX >= layout.x && mouseX <= layout.x + layout.width
                && mouseY >= layout.y && mouseY <= layout.y + layout.height) {
            return DragMode.MOVE;
        }

        return DragMode.NONE;
    }

    private boolean isOverHandle(int mouseX, int mouseY, int centerX, int centerY) {
        return mouseX >= centerX - HANDLE_SIZE && mouseX <= centerX + HANDLE_SIZE
                && mouseY >= centerY - HANDLE_SIZE && mouseY <= centerY + HANDLE_SIZE;
    }

    private void clampBounds() {
        FloatingUiLayoutConfig layout = selectedLayout();
        layout.width = MathHelper.clamp(layout.width, FloatingHelperConfig.MIN_SIZE, width);
        layout.height = MathHelper.clamp(layout.height, FloatingHelperConfig.MIN_SIZE, height);
        layout.x = MathHelper.clamp(layout.x, 0, Math.max(0, width - layout.width));
        layout.y = MathHelper.clamp(layout.y, 0, Math.max(0, height - layout.height));
    }

    private Text mirrorButtonText() {
        return Text.literal(selectedLayout().mirrored ? "镜像：开" : "镜像：关");
    }

    private boolean shouldAutoMirror(FloatingUiLayoutConfig layout) {
        int centerX = layout.x + layout.width / 2;
        return centerX > width / 2;
    }

    private FloatingUiLayoutConfig selectedLayout() {
        return layoutMode == LayoutMode.TITLE ? workingCopy.titleUi : workingCopy.inGameUi;
    }

    public enum LayoutMode {
        TITLE("编辑主界面 UI", "红框拖动主界面人物，绿框拖动主界面文字。"),
        IN_GAME("编辑游戏内 UI", "这里只编辑游戏内 yc_ui，不显示主界面文字。");

        private final String title;
        private final String description;

        LayoutMode(String title, String description) {
            this.title = title;
            this.description = description;
        }
    }

    private enum DragMode {
        NONE,
        MOVE,
        MOVE_TEXT,
        RESIZE_NW,
        RESIZE_NE,
        RESIZE_SW,
        RESIZE_SE
    }
}
