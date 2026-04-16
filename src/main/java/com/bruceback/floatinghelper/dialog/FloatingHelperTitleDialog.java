package com.bruceback.floatinghelper.dialog;

import com.bruceback.floatinghelper.config.FloatingHelperConfig;
import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class FloatingHelperTitleDialog {
    private static final Identifier DIALOG_TEXTURE = Identifier.of("floatinghelper", "textures/gui/dialog_box.png");
    private static final List<String> MESSAGES = List.of(
            "我是余丞，一个永远守候在像素世界的NPC。",
            "杨桃，好久不见啊，没想到你还会回来。",
            "下次一起去看电影吧~，就看你当年期盼那部续作。",
            "你看我从未离开，这场最后的续集，我们依然在一起。",
            "对了，给你点的蛋糕你去拿了吗？好想再吃一次老式蛋糕啊~",
            "你觉得我对你的爱，是一见钟情，还是日久生情？",
            "许久未见。最近很忙吗？",
            "杨桃，我更爱你。"
    );
    private static final int OUTER_PADDING = 4;
    private static final int TEXT_PADDING_X = 12;
    private static final int TEXT_PADDING_Y = 7;
    private static final int MAX_LINES = 3;
    private static final Random RANDOM = new Random();
    private static final List<String> remainingMessages = new ArrayList<>();
    private static String currentMessage = MESSAGES.getFirst();

    private FloatingHelperTitleDialog() {
    }

    public static void resetForTitleScreen() {
        refillMessages();
        currentMessage = remainingMessages.removeFirst();
    }

    public static void advanceMessage() {
        if (remainingMessages.isEmpty()) {
            refillMessages();
        }

        currentMessage = remainingMessages.removeFirst();
    }

    public static void render(DrawContext context, TextRenderer textRenderer, int screenWidth, int screenHeight) {
        render(context, textRenderer, FloatingHelperConfigManager.get(), screenWidth, screenHeight);
    }

    public static void render(DrawContext context, TextRenderer textRenderer, FloatingHelperConfig config, int screenWidth, int screenHeight) {
        DialogLayout layout = buildLayout(textRenderer, config, screenWidth, screenHeight);
        renderBackground(context, layout);
        renderText(context, textRenderer, layout);
    }

    public static boolean isInsideDialog(double mouseX, double mouseY, int screenWidth, int screenHeight) {
        DialogLayout layout = buildLayout(MinecraftClient.getInstance().textRenderer, FloatingHelperConfigManager.get(), screenWidth, screenHeight);
        return mouseX >= layout.x()
                && mouseX <= layout.x() + layout.width()
                && mouseY >= layout.y()
                && mouseY <= layout.y() + layout.height();
    }

    private static DialogLayout buildLayout(TextRenderer textRenderer, FloatingHelperConfig config, int screenWidth, int screenHeight) {
        FloatingHelperConfigManager.ensureValidBounds(config, screenWidth, screenHeight);

        boolean iconOnRight = isIconOnRight(config, screenWidth);
        float textScale = MathHelper.clamp(config.height / 92.0F, 0.7F, 1.45F);
        int dialogHeight = Math.max(30, Math.round(config.height * 0.38F));
        int minDialogWidth = Math.max(72, Math.round(config.width * 1.1F));
        int maxDialogWidth = Math.max(minDialogWidth, Math.min(Math.round(screenWidth * 0.42F), Math.round(config.width * 3.8F)));

        int wrapWidth = Math.max(48, Math.round((maxDialogWidth - TEXT_PADDING_X * 2) / textScale));
        List<OrderedText> lines = textRenderer == null
                ? List.of()
                : textRenderer.wrapLines(Text.literal(currentMessage), wrapWidth);

        int rawTextWidth = textRenderer == null ? minDialogWidth : textRenderer.getWidth(currentMessage);
        int desiredWidth = Math.round(rawTextWidth * textScale) + TEXT_PADDING_X * 2;
        int dialogWidth = MathHelper.clamp(desiredWidth, minDialogWidth, maxDialogWidth);

        if (textRenderer != null) {
            wrapWidth = Math.max(48, Math.round((dialogWidth - TEXT_PADDING_X * 2) / textScale));
            lines = textRenderer.wrapLines(Text.literal(currentMessage), wrapWidth);

            while (lines.size() > MAX_LINES && dialogWidth < maxDialogWidth) {
                dialogWidth = Math.min(maxDialogWidth, dialogWidth + 18);
                wrapWidth = Math.max(48, Math.round((dialogWidth - TEXT_PADDING_X * 2) / textScale));
                lines = textRenderer.wrapLines(Text.literal(currentMessage), wrapWidth);
            }

            int scaledTextHeight = Math.round(lines.size() * (textRenderer.fontHeight + 2) * textScale);
            dialogHeight = Math.max(dialogHeight, scaledTextHeight + TEXT_PADDING_Y * 2);
        }

        int baseX = iconOnRight
                ? config.x + Math.round(config.width * 0.58F) - dialogWidth
                : config.x + Math.round(config.width * 0.42F);
        int x = MathHelper.clamp(baseX, OUTER_PADDING, Math.max(OUTER_PADDING, screenWidth - dialogWidth - OUTER_PADDING));

        int baseY = config.y - Math.round(dialogHeight * 0.62F);
        int y = MathHelper.clamp(baseY, OUTER_PADDING, Math.max(OUTER_PADDING, screenHeight - dialogHeight - OUTER_PADDING));

        return new DialogLayout(x, y, dialogWidth, dialogHeight, iconOnRight, textScale, lines);
    }

    private static void renderBackground(DrawContext context, DialogLayout layout) {
        if (!layout.mirrored()) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, DIALOG_TEXTURE, layout.x(), layout.y(), 0.0F, 0.0F, layout.width(), layout.height(), layout.width(), layout.height());
            return;
        }

        context.drawTexturedQuad(DIALOG_TEXTURE, layout.x(), layout.y(), layout.x() + layout.width(), layout.y() + layout.height(), 1.0F, 0.0F, 0.0F, 1.0F);
    }

    private static void renderText(DrawContext context, TextRenderer textRenderer, DialogLayout layout) {
        if (layout.lines().isEmpty()) {
            return;
        }

        float textScale = layout.textScale();
        int lineHeight = textRenderer.fontHeight + 2;
        int contentHeight = layout.lines().size() * lineHeight;
        int textX = layout.x() + TEXT_PADDING_X;
        int textY = layout.y() + Math.max(TEXT_PADDING_Y, (layout.height() - Math.round(contentHeight * textScale)) / 2);

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(textX, textY);
        matrices.scale(textScale, textScale);

        for (int i = 0; i < layout.lines().size(); i++) {
            context.drawTextWithShadow(textRenderer, layout.lines().get(i), 0, i * lineHeight, 0xFFF6EEDC);
        }

        matrices.popMatrix();
    }

    private static boolean isIconOnRight(FloatingHelperConfig config, int screenWidth) {
        int centerX = config.x + config.width / 2;
        return centerX > screenWidth / 2;
    }

    private static void refillMessages() {
        remainingMessages.clear();
        remainingMessages.addAll(MESSAGES);
        Collections.shuffle(remainingMessages, RANDOM);
    }

    private record DialogLayout(int x, int y, int width, int height, boolean mirrored, float textScale, List<OrderedText> lines) {
    }
}
