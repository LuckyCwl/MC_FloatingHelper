package com.bruceback.floatinghelper.screen;

import com.bruceback.floatinghelper.config.FloatingHelperConfig;
import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
import com.bruceback.floatinghelper.renderer.FloatingIconWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FloatingHelperConfigScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget pathField;
    private FloatingHelperConfig workingCopy;

    public FloatingHelperConfigScreen(Screen parent) {
        super(Text.literal("FloatingHelper"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        workingCopy = copyOf(FloatingHelperConfigManager.get());

        int panelWidth = 320;
        int left = (width - panelWidth) / 2;
        int top = 48;
        int rowWidth = panelWidth;

        CyclingButtonWidget<Boolean> showButton = CyclingButtonWidget.onOffBuilder(workingCopy.showOnTitleScreen)
                .build(left, top, rowWidth, 20, Text.literal("主界面显示"), (button, value) -> workingCopy.showOnTitleScreen = value);
        addDrawableChild(showButton);

        addDrawableChild(ButtonWidget.builder(Text.literal("编辑位置与大小"), button -> client.setScreen(new FloatingHelperLayoutScreen(this, workingCopy)))
                .dimensions(left, top + 28, rowWidth, 20)
                .tooltip(Tooltip.of(Text.literal("打开遮罩编辑层，直接拖拽位置和尺寸")))
                .build());

        pathField = new TextFieldWidget(textRenderer, left, top + 64, rowWidth - 84, 20, Text.literal("图案路径"));
        pathField.setMaxLength(512);
        pathField.setText(workingCopy.customImagePath == null ? "" : workingCopy.customImagePath);
        pathField.setChangedListener(value -> workingCopy.customImagePath = value.trim());
        addSelectableChild(pathField);

        addDrawableChild(ButtonWidget.builder(Text.literal("上传"), button -> browseForImage())
                .dimensions(left + rowWidth - 78, top + 64, 78, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("使用内置图案"), button -> {
                    workingCopy.customImagePath = "";
                    pathField.setText("");
                    FloatingIconWidget.reloadTexture();
                })
                .dimensions(left, top + 92, rowWidth, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("保存"), button -> {
                    FloatingHelperConfigManager.update(copyOf(workingCopy));
                    FloatingIconWidget.reloadTexture();
                    client.setScreen(parent);
                })
                .dimensions(left, height - 52, 154, 20)
                .build());

        addDrawableChild(ButtonWidget.builder(Text.literal("取消"), button -> client.setScreen(parent))
                .dimensions(left + 166, height - 52, 154, 20)
                .build());

        setInitialFocus(pathField);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        renderBackground(context, mouseX, mouseY, deltaTicks);

        int panelWidth = 320;
        int panelHeight = 182;
        int left = (width - panelWidth) / 2;
        int top = 36;

        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 18, 0xFFFFFF);
        context.fill(left - 8, top - 8, left + panelWidth + 8, top + panelHeight + 8, 0xAA000000);
        drawBorder(context, left - 8, top - 8, panelWidth + 16, panelHeight + 16, 0xFF6B6B6B);
        context.drawTextWithShadow(textRenderer, Text.literal("配置界面"), left, top - 18, 0xE0E0E0);
        context.drawTextWithShadow(textRenderer, Text.literal("自定义图案"), left, top + 52, 0xE0E0E0);
        context.drawTextWithShadow(textRenderer, Text.literal("右侧预览"), left + 210, top + 120, 0xE0E0E0);

        super.render(context, mouseX, mouseY, deltaTicks);
        pathField.render(context, mouseX, mouseY, deltaTicks);

        int previewSize = 72;
        int previewX = left + 224;
        int previewY = top + 132;
        context.fill(previewX - 6, previewY - 6, previewX + previewSize + 6, previewY + previewSize + 6, 0x44000000);
        drawBorder(context, previewX - 6, previewY - 6, previewSize + 12, previewSize + 12, 0xFF8A8A8A);
        FloatingIconWidget.render(context, previewX, previewY, previewSize, previewSize);
        context.drawTextWithShadow(textRenderer, Text.literal("拖拽与缩放在编辑层中进行"), left, top + 124, 0xA0A0A0);
    }

    private void drawBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.drawHorizontalLine(x, x + width - 1, y, color);
        context.drawHorizontalLine(x, x + width - 1, y + height - 1, color);
        context.drawVerticalLine(x, y, y + height - 1, color);
        context.drawVerticalLine(x + width - 1, y, y + height - 1, color);
    }

    private void browseForImage() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("选择 FloatingHelper 图案");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "gif", "bmp"));

        int result = chooser.showOpenDialog(null);

        if (result != JFileChooser.APPROVE_OPTION || chooser.getSelectedFile() == null) {
            return;
        }

        Path selectedPath = chooser.getSelectedFile().toPath();
        Path configImagePath = FloatingHelperConfigManager.getConfigDir().resolve("custom-icon.png");

        try {
            Files.createDirectories(FloatingHelperConfigManager.getConfigDir());
            BufferedImage image = ImageIO.read(selectedPath.toFile());

            if (image == null) {
                return;
            }

            ImageIO.write(image, "png", configImagePath.toFile());
            workingCopy.customImagePath = configImagePath.toString();
            pathField.setText(workingCopy.customImagePath);
            FloatingHelperConfigManager.update(copyOf(workingCopy));
            FloatingIconWidget.reloadTexture();
        } catch (IOException ignored) {
        }
    }

    private static FloatingHelperConfig copyOf(FloatingHelperConfig source) {
        FloatingHelperConfig copy = new FloatingHelperConfig();
        copy.showOnTitleScreen = source.showOnTitleScreen;
        copy.x = source.x;
        copy.y = source.y;
        copy.width = source.width;
        copy.height = source.height;
        copy.customImagePath = source.customImagePath;
        return copy;
    }
}
