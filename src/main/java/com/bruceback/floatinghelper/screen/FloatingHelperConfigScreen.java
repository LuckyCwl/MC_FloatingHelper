package com.bruceback.floatinghelper.screen;

import com.bruceback.floatinghelper.config.FloatingHelperConfig;
import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.text.Text;

public class FloatingHelperConfigScreen extends Screen {
    private final Screen parent;

    public FloatingHelperConfigScreen(Screen parent) {
        super(Text.literal("FloatingHelper"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        FloatingHelperConfig config = FloatingHelperConfigManager.get();
        int panelWidth = 300;
        int left = (width - panelWidth) / 2;
        int top = height / 2 - 52;

        clearChildren();

        CyclingButtonWidget<Boolean> showButton = CyclingButtonWidget.onOffBuilder(config.showOnTitleScreen)
                .build(left, top, panelWidth, 20, Text.literal("显示 / 隐藏"), (button, value) -> {
                    FloatingHelperConfig updatedConfig = copyOf(FloatingHelperConfigManager.get());
                    updatedConfig.showOnTitleScreen = value;
                    FloatingHelperConfigManager.update(updatedConfig);
                });
        addDrawableChild(showButton);

        addDrawableChild(ButtonWidget.builder(Text.literal("编辑位置与大小"), button ->
                        client.setScreen(new FloatingHelperLayoutScreen(this, copyOf(FloatingHelperConfigManager.get()))))
                .dimensions(left, top + 30, panelWidth, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("完成"), button -> close())
                .dimensions(left, top + 60, panelWidth, 20)
                .build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        context.fill(0, 0, width, height, 0x88000000);

        int panelWidth = 300;
        int panelHeight = 112;
        int left = (width - panelWidth) / 2;
        int top = height / 2 - 56;

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 18, 0xFFFFFF);
        context.fill(left - 8, top - 8, left + panelWidth + 8, top + panelHeight + 8, 0xAA000000);
        drawBorder(context, left - 8, top - 8, panelWidth + 16, panelHeight + 16, 0xFF6B6B6B);
        context.drawTextWithShadow(textRenderer, Text.literal("Mod Menu 配置"), left, top - 18, 0xE0E0E0);
        context.drawTextWithShadow(textRenderer, Text.literal("悬浮人物会在主界面和游戏内显示。"), left, top + 2, 0xC8C8C8);
        context.drawTextWithShadow(textRenderer, Text.literal("主界面的绿色提示文字会跟随 yc_ui 的位置、大小和朝向。"), left, top + 14, 0xC8C8C8);

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void close() {
        if (client != null) {
            client.setScreen(parent);
            return;
        }

        super.close();
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.drawHorizontalLine(x, x + width - 1, y, color);
        context.drawHorizontalLine(x, x + width - 1, y + height - 1, color);
        context.drawVerticalLine(x, y, y + height - 1, color);
        context.drawVerticalLine(x + width - 1, y, y + height - 1, color);
    }

    private static FloatingHelperConfig copyOf(FloatingHelperConfig source) {
        FloatingHelperConfig copy = new FloatingHelperConfig();
        copy.showOnTitleScreen = source.showOnTitleScreen;
        copy.x = source.x;
        copy.y = source.y;
        copy.width = source.width;
        copy.height = source.height;
        copy.relativeX = source.relativeX;
        copy.relativeY = source.relativeY;
        copy.mirrored = source.mirrored;
        return copy;
    }
}
