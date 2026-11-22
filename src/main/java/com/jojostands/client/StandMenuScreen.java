package com.jojostands.client;

import com.jojostands.StandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Menu principal: escolha do stand. Adicione mais botoes aqui quando criar novos stands. */
public class StandMenuScreen extends Screen {
    private static final int W = 240, H = 204;

    public StandMenuScreen() { super(Text.literal("Stands")); }

    @Override
    protected void init() {
        int x = (width - W) / 2 + 20, y = (height - H) / 2, bw = W - 40;
        addDrawableChild(ButtonWidget.builder(Text.literal("§cKing Crimson"), b -> {
            ClientPlayNetworking.send(new StandPayload(3, "king_crimson"));
            close();
        }).dimensions(x, y + 42, bw, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("§6Golden Experience"), b -> {
            ClientPlayNetworking.send(new StandPayload(3, "golden_experience"));
            close();
        }).dimensions(x, y + 70, bw, 22).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("§6Menu do Golden Experience"),
                b -> client.setScreen(new GoldenMenuScreen(this))).dimensions(x, y + 104, bw, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("§bComo usar"),
                b -> client.setScreen(new HelpScreen(this))).dimensions(x, y + 130, bw, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Fechar"), b -> close())
                .dimensions(x, y + 168, bw, 20).build());
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        super.renderBackground(c, mx, my, delta);
        Panels.draw(c, textRenderer, (width - W) / 2, (height - H) / 2, W, H, 0xFFFFD700, "✦ JOJO STANDS ✦");
    }
}
