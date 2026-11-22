package com.jojostands.client;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public final class Panels {
    private Panels() {}

    /** Painel escuro com borda colorida e titulo. */
    public static void draw(DrawContext c, TextRenderer tr, int x, int y, int w, int h, int border, String title) {
        c.fillGradient(x, y, x + w, y + h, 0xF0141420, 0xF0050508);
        c.drawBorder(x, y, w, h, border);
        c.drawBorder(x + 3, y + 3, w - 6, h - 6, (border & 0x00FFFFFF) | 0x55000000);
        c.fill(x + 12, y + 28, x + w - 12, y + 29, border);
        c.drawCenteredTextWithShadow(tr, Text.literal(title), x + w / 2, y + 12, border);
    }
}
