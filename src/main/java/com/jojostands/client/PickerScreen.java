package com.jojostands.client;

import com.jojostands.StandPayload;
import com.jojostands.StandRules;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Lista com busca para escolher o mob ou o bloco. */
public class PickerScreen extends Screen {
    public enum Kind { MOB, BLOCK }

    private static final int W = 260, H = 262, ROWS = 8;
    private final Screen parent;
    private final Kind kind;
    private final List<Identifier> all = new ArrayList<>();
    private final List<ButtonWidget> rows = new ArrayList<>();
    private TextFieldWidget search;

    public PickerScreen(Screen parent, Kind kind) {
        super(Text.literal(kind == Kind.MOB ? "Escolher mob" : "Escolher bloco"));
        this.parent = parent;
        this.kind = kind;
        if (kind == Kind.MOB) {
            Registries.ENTITY_TYPE.stream().filter(StandRules::isAllowedMob)
                    .map(Registries.ENTITY_TYPE::getId).sorted().forEach(all::add);
        } else {
            Registries.BLOCK.stream().filter(StandRules::isAllowedBlock)
                    .map(Registries.BLOCK::getId).sorted().forEach(all::add);
        }
    }

    private String nameOf(Identifier id) {
        return (kind == Kind.MOB ? Registries.ENTITY_TYPE.get(id).getName() : Registries.BLOCK.get(id).getName()).getString();
    }

    @Override
    protected void init() {
        rows.clear();
        int x = (width - W) / 2 + 15, y = (height - H) / 2, bw = W - 30;
        search = new TextFieldWidget(textRenderer, x, y + 38, bw, 18, Text.literal("Buscar"));
        search.setPlaceholder(Text.literal("§7Digite para buscar..."));
        search.setChangedListener(s -> refresh());
        addDrawableChild(search);
        setInitialFocus(search);
        addDrawableChild(ButtonWidget.builder(Text.literal("Voltar"), b -> client.setScreen(parent))
                .dimensions(x, y + 232, bw, 20).build());
        refresh();
    }

    private void refresh() {
        for (ButtonWidget w : rows) remove(w);
        rows.clear();
        int x = (width - W) / 2 + 15, y = (height - H) / 2, bw = W - 30;
        String f = search.getText().toLowerCase().trim();
        int shown = 0;
        for (Identifier id : all) {
            if (shown >= ROWS) break;
            String name = nameOf(id);
            if (!f.isEmpty() && !name.toLowerCase().contains(f) && !id.getPath().contains(f)) continue;
            ButtonWidget btn = ButtonWidget.builder(Text.literal(name), b -> {
                ClientPlayNetworking.send(new StandPayload(kind == Kind.MOB ? 1 : 2, id.toString()));
                if (kind == Kind.MOB) ClientState.mobName = name; else ClientState.blockName = name;
                client.setScreen(parent);
            }).dimensions(x, y + 62 + shown * 21, bw, 20).build();
            addDrawableChild(btn);
            rows.add(btn);
            shown++;
        }
    }

    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        super.renderBackground(c, mx, my, delta);
        Panels.draw(c, textRenderer, (width - W) / 2, (height - H) / 2, W, H, 0xFFFFD700,
                kind == Kind.MOB ? "ESCOLHER MOB" : "ESCOLHER BLOCO");
    }
}
