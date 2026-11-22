package com.jojostands.client;

import com.jojostands.StandPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class GoldenMenuScreen extends Screen {
    private static final int W = 270, H = 262;
    private final Screen parent;

    public GoldenMenuScreen(Screen parent) {
        super(Text.literal("Golden Experience"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = (width - W) / 2 + 15, y = (height - H) / 2, bw = W - 30;
        for (int i = 0; i < ClientState.MODES.length; i++) {
            final int mode = i;
            String label = (ClientState.mode == i ? "§e▶ " : "§f") + ClientState.MODES[i];
            addDrawableChild(ButtonWidget.builder(Text.literal(label), b -> {
                ClientState.mode = mode;
                ClientPlayNetworking.send(new StandPayload(0, String.valueOf(mode)));
                clearAndInit();
            }).dimensions(x, y + 38 + i * 24, bw, 20).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("§6Mob: §f" + ClientState.mobName),
                b -> client.setScreen(new PickerScreen(this, PickerScreen.Kind.MOB)))
                .dimensions(x, y + 168, bw, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("§6Bloco: §f" + ClientState.blockName),
                b -> client.setScreen(new PickerScreen(this, PickerScreen.Kind.BLOCK)))
                .dimensions(x, y + 192, bw, 20).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar"), b -> client.setScreen(parent))
                .dimensions(x, y + 222, bw, 20).build());
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        super.renderBackground(c, mx, my, delta);
        Panels.draw(c, textRenderer, (width - W) / 2, (height - H) / 2, W, H, 0xFFFFD700, "✦ GOLDEN EXPERIENCE ✦");
    }
}
