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
    private static final Random RANDOM = new Random();
    private static final List<String> remainingMessages = new ArrayList<>();
    private static final Style TEXT_STYLE = Style.EMPTY.withBold(true).withFormatting(Formatting.GREEN);
    private static final long FADE_DURATION_MS = 1000L;
    private static final float BASE_ROTATION = -0.22F;
    private static final int MAX_LINES = 2;
    private static final int SCREEN_MARGIN = 6;

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
        Text message = Text.literal(currentMessage).fillStyle(TEXT_STYLE);
        TextLayout layout = buildLayout(textRenderer, config, screenWidth, screenHeight, message);
        float fadeProgress = MathHelper.clamp((float) (Util.getMeasuringTimeMs() - messageShownAt) / FADE_DURATION_MS, 0.0F, 1.0F);
        int alpha = Math.max(16, Math.round(255.0F * fadeProgress));
        int color = (alpha << 24) | 0x57D65A;

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(layout.centerX(), layout.centerY() - (1.0F - fadeProgress) * 4.0F);
        matrices.rotate(layout.rotation());
        matrices.scale(layout.scale(), layout.scale());

        int lineHeight = textRenderer.fontHeight + 2;
        int startY = -Math.round(layout.height() / 2.0F);

        for (int i = 0; i < layout.lines().size(); i++) {
            OrderedText line = layout.lines().get(i);
            int lineWidth = textRenderer.getWidth(line);
            int lineX = -Math.round(lineWidth / 2.0F);
            context.drawTextWithShadow(textRenderer, line, lineX, startY + i * lineHeight, color);
        }

        matrices.popMatrix();
    }

    private static TextLayout buildLayout(TextRenderer textRenderer, FloatingHelperConfig config, int screenWidth, int screenHeight, Text message) {
        FloatingHelperConfigManager.ensureValidBounds(config, screenWidth, screenHeight);

        boolean iconOnRight = isIconOnRight(config, screenWidth);
        float scale = MathHelper.clamp(config.height / 96.0F, 0.78F, 1.45F);
        int maxLineWidth = Math.max(84, Math.min(Math.round(config.width * 3.0F), Math.round(screenWidth * 0.38F)));
        List<OrderedText> lines = textRenderer.wrapLines(message, Math.round(maxLineWidth / scale));

        while (lines.size() > MAX_LINES && maxLineWidth < Math.round(screenWidth * 0.5F)) {
            maxLineWidth += 18;
            lines = textRenderer.wrapLines(message, Math.round(maxLineWidth / scale));
        }

        if (lines.size() > MAX_LINES) {
            lines = new ArrayList<>(lines.subList(0, MAX_LINES));
        }

        int widestLine = 0;
        for (OrderedText line : lines) {
            widestLine = Math.max(widestLine, textRenderer.getWidth(line));
        }

        int lineHeight = textRenderer.fontHeight + 2;
        int blockWidth = Math.round(widestLine * scale);
        int blockHeight = Math.round(lines.size() * lineHeight * scale);
        int horizontalGap = Math.max(12, Math.round(config.width * 0.18F));
        int verticalGap = Math.max(10, Math.round(config.height * 0.12F));

        int left = iconOnRight
                ? config.x - blockWidth - horizontalGap
                : config.x + config.width + horizontalGap;
        int top = config.y - blockHeight - verticalGap;

        left = MathHelper.clamp(left, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenWidth - blockWidth - SCREEN_MARGIN));
        top = MathHelper.clamp(top, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenHeight - blockHeight - SCREEN_MARGIN));

        // Keep the text outside of the character bounds even after clamping to the screen.
        if (intersects(left, top, blockWidth, blockHeight, config.x, config.y, config.width, config.height)) {
            top = Math.max(SCREEN_MARGIN, config.y - blockHeight - verticalGap - Math.round(config.height * 0.18F));
        }

        if (intersects(left, top, blockWidth, blockHeight, config.x, config.y, config.width, config.height)) {
            left = iconOnRight
                    ? Math.max(SCREEN_MARGIN, config.x - blockWidth - horizontalGap - Math.round(config.width * 0.2F))
                    : Math.min(screenWidth - blockWidth - SCREEN_MARGIN, config.x + config.width + horizontalGap + Math.round(config.width * 0.2F));
        }

        left = MathHelper.clamp(left, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenWidth - blockWidth - SCREEN_MARGIN));
        top = MathHelper.clamp(top, SCREEN_MARGIN, Math.max(SCREEN_MARGIN, screenHeight - blockHeight - SCREEN_MARGIN));

        float centerX = left + blockWidth / 2.0F;
        float centerY = top + blockHeight / 2.0F;
        float rotation = iconOnRight ? -BASE_ROTATION : BASE_ROTATION;
        return new TextLayout(lines, scale, blockWidth, blockHeight, centerX, centerY, rotation);
    }

    private static boolean intersects(int leftA, int topA, int widthA, int heightA, int leftB, int topB, int widthB, int heightB) {
        return leftA < leftB + widthB
                && leftA + widthA > leftB
                && topA < topB + heightB
                && topA + heightA > topB;
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

    private record TextLayout(List<OrderedText> lines, float scale, int width, int height, float centerX, float centerY, float rotation) {
    }
}
