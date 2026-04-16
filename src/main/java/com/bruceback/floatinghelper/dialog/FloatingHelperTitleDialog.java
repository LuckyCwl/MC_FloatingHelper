package com.bruceback.floatinghelper.dialog;

import com.bruceback.floatinghelper.config.FloatingHelperConfig;
import com.bruceback.floatinghelper.config.FloatingHelperConfigManager;
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
    private static final Identifier BUTTON_TEXTURE = Identifier.ofVanilla("widget/button");
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
    private static final int OUTER_PADDING = 6;
    private static final int TEXT_PADDING_X = 12;
    private static final int TEXT_PADDING_Y = 6;
    private static final int LINE_SPACING = 2;
    private static final int MAX_LINES = 2;
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
        context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BUTTON_TEXTURE, layout.x(), layout.y(), layout.width(), layout.height());

        int lineHeight = textRenderer.fontHeight + LINE_SPACING;
        int textY = layout.y() + Math.max(TEXT_PADDING_Y, (layout.height() - layout.lines().size() * lineHeight) / 2);

        for (int i = 0; i < layout.lines().size(); i++) {
            int lineWidth = textRenderer.getWidth(layout.lines().get(i));
            int textX = layout.x() + (layout.width() - lineWidth) / 2;
            context.drawTextWithShadow(textRenderer, layout.lines().get(i), textX, textY + i * lineHeight, 0xFFFFFF);
        }
    }

    private static DialogLayout buildLayout(TextRenderer textRenderer, FloatingHelperConfig config, int screenWidth, int screenHeight) {
        FloatingHelperConfigManager.ensureValidBounds(config, screenWidth, screenHeight);

        boolean iconOnRight = isIconOnRight(config, screenWidth);
        int minDialogWidth = Math.max(96, Math.round(config.width * 1.55F));
        int maxDialogWidth = Math.max(minDialogWidth, Math.min(Math.round(screenWidth * 0.52F), Math.round(config.width * 4.6F)));
        int wrapWidth = Math.max(72, maxDialogWidth - TEXT_PADDING_X * 2);
        List<OrderedText> lines = textRenderer.wrapLines(Text.literal(currentMessage), wrapWidth);
        int dialogWidth = computeDialogWidth(textRenderer, lines, minDialogWidth, maxDialogWidth);

        while (lines.size() > MAX_LINES && dialogWidth < maxDialogWidth) {
            dialogWidth = Math.min(maxDialogWidth, dialogWidth + 20);
            wrapWidth = Math.max(72, dialogWidth - TEXT_PADDING_X * 2);
            lines = textRenderer.wrapLines(Text.literal(currentMessage), wrapWidth);
        }

        int lineHeight = textRenderer.fontHeight + LINE_SPACING;
        int dialogHeight = Math.max(20, lines.size() * lineHeight + TEXT_PADDING_Y * 2);

        int anchorX = iconOnRight
                ? config.x + Math.round(config.width * 0.72F) - dialogWidth
                : config.x + Math.round(config.width * 0.28F);
        int x = MathHelper.clamp(anchorX, OUTER_PADDING, Math.max(OUTER_PADDING, screenWidth - dialogWidth - OUTER_PADDING));

        int anchorY = config.y - dialogHeight - Math.max(6, Math.round(config.height * 0.08F));
        int y = MathHelper.clamp(anchorY, OUTER_PADDING, Math.max(OUTER_PADDING, screenHeight - dialogHeight - OUTER_PADDING));

        return new DialogLayout(x, y, dialogWidth, dialogHeight, lines);
    }

    private static int computeDialogWidth(TextRenderer textRenderer, List<OrderedText> lines, int minDialogWidth, int maxDialogWidth) {
        int widestLine = 0;

        for (OrderedText line : lines) {
            widestLine = Math.max(widestLine, textRenderer.getWidth(line));
        }

        return MathHelper.clamp(widestLine + TEXT_PADDING_X * 2, minDialogWidth, maxDialogWidth);
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

    private record DialogLayout(int x, int y, int width, int height, List<OrderedText> lines) {
    }
}
