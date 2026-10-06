package com.modgen.flowclient.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

public final class FlowButton extends ButtonWidget {
    private final BooleanSupplier selected;
    private final IntSupplier accent;
    private final boolean toggle;
    public FlowButton(int x, int y, int width, int height, String label, Runnable action, BooleanSupplier selected, IntSupplier accent, boolean toggle) {
        // Fully qualify Text: ButtonWidget's inherited nested Text type shadows an import.
        super(x, y, width, height, net.minecraft.text.Text.literal(label), b -> action.run(), DEFAULT_NARRATION_SUPPLIER);
        this.selected = selected;
        this.accent = accent;
        this.toggle = toggle;
    }
    public static void round(DrawContext context, int x, int y, int width, int height, int color) {
        if (width < 4 || height < 4) return;
        context.fill(x + 3, y, x + width - 3, y + height, color);
        context.fill(x + 1, y + 1, x + width - 1, y + height - 1, color);
        context.fill(x, y + 3, x + width, y + height - 3, color);
    }
    // In 1.21.11 renderWidget is final; custom button content belongs in drawIcon.
    @Override protected void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean on = selected.getAsBoolean();
        boolean hover = isHovered() || isFocused();
        int x = getX(), y = getY(), w = getWidth(), h = getHeight();
        if (on && com.modgen.flowclient.client.FlowClientClient.MODULES.enabled("Flow+"))
            round(context, x - 1, y - 1, w + 2, h + 2, 0x554F386D);
        round(context, x, y, w, h, on ? accent.getAsInt() : hover ? 0xFF625274 : 0xFF30283F);
        round(context, x + 1, y + 1, w - 2, h - 2, on && !toggle ? 0xFF503477 : hover ? 0xFF262037 : 0xFF141323);
        var renderer = MinecraftClient.getInstance().textRenderer;
        String label = renderer.trimToWidth(getMessage().getString(), Math.max(0, w - (toggle ? 39 : 14)));
        context.drawText(renderer, label, x + 7, y + (h - 8) / 2, on ? 0xFFF1E6FF : 0xFFBEB9D2, false);
        if (toggle) {
            round(context, x + w - 29, y + (h - 10) / 2, 23, 10, on ? accent.getAsInt() : 0xFF29253D);
            round(context, x + w - (on ? 15 : 27), y + (h - 8) / 2, 8, 8, 0xFFF5F0FF);
        }
    }
}
