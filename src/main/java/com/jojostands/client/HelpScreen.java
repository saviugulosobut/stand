package com.jojostands.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Tela "Como usar". */
public class HelpScreen extends Screen {
    private static final int W = 300, H = 250;
    private static final String[] KC = {
            "§cKing Crimson §7(recarga: 1 s)",
            "",
            "§fClique direito §7- Apagar Tempo (20 s):",
            "§7  tudo em 48 blocos congela; so voce se move",
            "§7  (Speed + pulo). Clique de novo: encerra.",
            "§fDano acumulado: §7golpes nos congelados nao",
            "§7  machucam na hora; tudo explode (+25%)",
            "§7  quando o tempo volta.",
            "",
            "§fShift + clique direito §7- Epitafio (20 s):",
            "§7  todos ao redor brilham, todo ataque e",
            "§7  evitado e o atacante fica lento.",
            "§7Aura: §cvermelha §7enquanto segura o item."
    };
    private static final String[] GE = {
            "§6Golden Experience",
            "§7Escolha o modo no menu do Golden Experience:",
            "",
            "§f1. Criar vida §7- bloco vira planta/animal;",
            "§7   clique no ar cura voce.",
            "§f2. Bloco → Mob §7- o bloco vira o mob escolhido",
            "§7   e ele vira seu aliado (recarga 10 s).",
            "§f3. Invocar mob §7- mob aparece e te protege.",
            "§f4. Bloco → Bloco §7- troca pelo bloco escolhido.",
            "§f5. Comandar §7- clique num bloco: aliados vao;",
            "§7   Shift + clique no ar: voltam a te seguir.",
            "",
            "§7Aliados te seguem, defendem, nunca te atacam",
            "§7e refletem o dano de quem os atacar.",
            "§7Shift + bater no aliado = remove ele.",
            "§7Aura: §6dourada §7enquanto segura o item."
    };
    private final Screen parent;
    private boolean golden = false;

    public HelpScreen(Screen parent) {
        super(Text.literal("Como usar"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int x = (width - W) / 2 + 15, y = (height - H) / 2, half = (W - 36) / 2;
        addDrawableChild(ButtonWidget.builder(Text.literal("§cKing Crimson"), b -> golden = false)
                .dimensions(x, y + 36, half, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("§6Golden Experience"), b -> golden = true)
                .dimensions(x + half + 6, y + 36, half, 18).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar"), b -> client.setScreen(parent))
                .dimensions(x, y + H - 30, W - 30, 20).build());
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        super.renderBackground(c, mx, my, delta);
        int x = (width - W) / 2, y = (height - H) / 2;
        Panels.draw(c, textRenderer, x, y, W, H, golden ? 0xFFFFD700 : 0xFFD01030, "COMO USAR");
        String[] lines = golden ? GE : KC;
        int ty = y + 62;
        for (String l : lines) {
            c.drawTextWithShadow(textRenderer, Text.literal(l), x + 16, ty, 0xFFFFFF);
            ty += 11;
        }
    }
}
