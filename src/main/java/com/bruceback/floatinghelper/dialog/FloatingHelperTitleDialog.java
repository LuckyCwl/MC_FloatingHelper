package com.bruceback.floatinghelper.dialog;

import com.bruceback.floatinghelper.config.FloatingHelperConfig;
import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class FloatingHelperTitleDialog {
    private static final List<String> MESSAGES = List.of(
            "我是余丞，一个永远守候在像素世界的NPC。",
            "杨桃，好久不见啊，没想到你还会回来。",
            "下次一起去看电影吧，就看你当年期盼那部续作。",
            "你看我从未离开，这场最后的续集，我们依然在一起。",
            "对了，给你点的蛋糕你去拿了吗？好想再吃一次老式蛋糕啊。",
            "你觉得我对你的爱，是一见钟情，还是日久生情？",
            "许久未见。最近很忙吗？",
            "杨桃，我更爱你。"
    );
    private static final Style TEXT_STYLE = Style.EMPTY.withBold(true).withFormatting(Formatting.GREEN);
    private static final Random RANDOM = new Random();
    private static final List<String> remainingMessages = new ArrayList<>();
    private static final int SCREEN_MARGIN = 6;
    private static final long FADE_DURATION_MS = 1000L;
    private static final int MAX_LINES = 4;

    private static String currentMessage = MESSAGES.getFirst();
    private static long messageShownAt = Util.getMeasuringTimeMs();

    private FloatingHelperTitleDialog() {
    }

    public static void resetForTitleScreen() {
        refillMessages();
        currentMessage = remainingMessages.removeFirst();
        messageShownAt = Util.getMeasuringTimeMs();
    }

    public static void advanceMessage() {
        if (remainingMessages.isEmpty()) {
            refillMessages();
        }

        currentMessage = remainingMessages.removeFirst();
        messageShownAt = Util.getMeasuringTimeMs();
    }

    public static void render(DrawContext context, TextRenderer textRenderer, int screenWidth, int screenHeight) {
        render(context, textRenderer, FloatingHelperConfigManager.get(), screenWidth, screenHeight);
    }

    public static void render(DrawContext context, TextRenderer textRenderer, FloatingHelperConfig config, int screenWidth, int screenHeight) {
        TextLayout layout = getLayout(textRenderer, config, screenWidth, screenHeight);
        float fadeProgress = MathHelper.clamp((float) (Util.getMeasuringTimeMs() - messageShownAt) / FADE_DURATION_MS, 0.0F, 1.0F);
        int alpha = Math.max(20, Math.round(255.0F * fadeProgress));
        int color = (alpha << 24) | 0x57D65A;

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(layout.x(), layout.y() - (1.0F - fadeProgress) * 4.0F);
        matrices.scale(layout.scale(), layout.scale());

        int lineHeight = textRenderer.fontHeight + 2;

        for (int i = 0; i < layout.lines().size(); i++) {
            OrderedText line = layout.lines().get(i);
            int lineWidth = textRenderer.getWidth(line);
            int lineX = Math.max(0, (layout.textWidth() - lineWidth) / 2);
            context.drawText(textRenderer, line, lineX, i * lineHeight, color, false);
        }

        matrices.popMatrix();
    }

    public static TextLayout getLayout(TextRenderer textRenderer, FloatingHelperConfig config, int screenWidth, int screenHeight) {
        IconBounds icon = resolveIconBounds(config, screenWidth, screenHeight);
        float scale = MathHelper.clamp(icon.height() / 96.0F, 0.78F, 1.45F);
        int lineHeight = textRenderer.fontHeight + 2;
        List<OrderedText> lines = wrapMessageLines(textRenderer, scale, icon.width(), screenWidth);

        int widestLine = 0;
        for (OrderedText line : lines) {
            widestLine = Math.max(widestLine, textRenderer.getWidth(line));
        }

        int textWidth = Math.max(1, Math.round(widestLine * scale));
        int textHeight = Math.max(1, Math.round(lines.size() * lineHeight * scale));
        int defaultX = buildDefaultX(icon, textWidth, screenWidth);
        int defaultY = buildDefaultY(icon, textHeight, screenHeight);
        int x = FloatingHelperConfigManager.resolveTextX(config, screenWidth, textWidth, defaultX);
        int y = FloatingHelperConfigManager.resolveTextY(config, screenHeight, textHeight, defaultY);

        x = MathHelper.clamp(x, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenWidth - textWidth - SCREEN_MARGIN));
        y = MathHelper.clamp(y, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenHeight - textHeight - SCREEN_MARGIN));
        return new TextLayout(lines, scale, x, y, textWidth, textHeight);
    }

    private static int buildDefaultX(IconBounds icon, int textWidth, int screenWidth) {
        boolean iconOnRight = icon.centerX() > screenWidth / 2;
        int horizontalGap = Math.max(14, Math.round(icon.width() * 0.18F));
        int x = iconOnRight
                ? icon.x() - textWidth - horizontalGap
                : icon.x() + icon.width() + horizontalGap;
        return MathHelper.clamp(x, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenWidth - textWidth - SCREEN_MARGIN));
    }

    private static int buildDefaultY(IconBounds icon, int textHeight, int screenHeight) {
        int verticalGap = Math.max(10, Math.round(icon.height() * 0.14F));
        int y = icon.y() - textHeight - verticalGap;
        return MathHelper.clamp(y, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenHeight - textHeight - SCREEN_MARGIN));
    }

    private static List<OrderedText> wrapMessageLines(TextRenderer textRenderer, float scale, int iconWidth, int screenWidth) {
        String message = currentMessage
                .replace("，", "，\n")
                .replace("？", "？\n");

        int maxLineWidth = Math.max(92, Math.min(Math.round(iconWidth * 3.0F), Math.round(screenWidth * 0.42F)));
        int wrapWidth = Math.max(72, Math.round(maxLineWidth / scale));
        List<OrderedText> lines = wrapPreservingManualBreaks(textRenderer, message, wrapWidth);

        while (lines.size() > MAX_LINES && maxLineWidth < Math.round(screenWidth * 0.55F)) {
            maxLineWidth += 24;
            wrapWidth = Math.max(72, Math.round(maxLineWidth / scale));
            lines = wrapPreservingManualBreaks(textRenderer, message, wrapWidth);
        }

        return lines;
    }

    private static List<OrderedText> wrapPreservingManualBreaks(TextRenderer textRenderer, String message, int wrapWidth) {
        List<OrderedText> lines = new ArrayList<>();
        String[] segments = message.split("\\n", -1);

        for (String segment : segments) {
            Text styledSegment = Text.literal(segment).fillStyle(TEXT_STYLE);
            List<OrderedText> wrapped = textRenderer.wrapLines(styledSegment, wrapWidth);
            if (wrapped.isEmpty()) {
                wrapped = List.of(Text.literal("").fillStyle(TEXT_STYLE).asOrderedText());
            }
            lines.addAll(wrapped);
        }

        return lines;
    }

    private static IconBounds resolveIconBounds(FloatingHelperConfig config, int screenWidth, int screenHeight) {
        int width = MathHelper.clamp(config.width, FloatingHelperConfig.MIN_SIZE, Math.max(FloatingHelperConfig.MIN_SIZE, screenWidth));
        int height = MathHelper.clamp(config.height, FloatingHelperConfig.MIN_SIZE, Math.max(FloatingHelperConfig.MIN_SIZE, screenHeight));
        int maxX = Math.max(0, screenWidth - width);
        int maxY = Math.max(0, screenHeight - height);

        int x;
        int y;
        if (isValidRelative(config.relativeX) && isValidRelative(config.relativeY)) {
            x = maxX == 0 ? 0 : (int) Math.round(MathHelper.clamp((float) config.relativeX, 0.0F, 1.0F) * maxX);
            y = maxY == 0 ? 0 : (int) Math.round(MathHelper.clamp((float) config.relativeY, 0.0F, 1.0F) * maxY);
        } else {
            x = config.x < 0 ? Math.max(0, screenWidth - width - FloatingHelperConfig.DEFAULT_MARGIN) : config.x;
            y = config.y < 0 ? Math.max(0, screenHeight - height - FloatingHelperConfig.DEFAULT_MARGIN) : config.y;
        }

        x = MathHelper.clamp(x, 0, maxX);
        y = MathHelper.clamp(y, 0, maxY);
        return new IconBounds(x, y, width, height);
    }

    private static boolean isValidRelative(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value) && value >= 0.0D && value <= 1.0D;
    }

    private static void refillMessages() {
        remainingMessages.clear();
        remainingMessages.addAll(MESSAGES);
        Collections.shuffle(remainingMessages, RANDOM);
    }

    private record IconBounds(int x, int y, int width, int height) {
        private int centerX() {
            return x + width / 2;
        }
    }

    public record TextLayout(List<OrderedText> lines, float scale, int x, int y, int textWidth, int textHeight) {
    }
}
