package com.jojostands.client;

import com.jojostands.JojoStands;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class JojoStandsClient implements ClientModInitializer {
    public static KeyBinding MENU_KEY;

    @Override
    public void onInitializeClient() {
        MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.jojostands.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G, "key.categories.jojostands"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (MENU_KEY.wasPressed()) {
                if (client.currentScreen == null && client.player != null) {
                    client.setScreen(new StandMenuScreen());
                }
            }
        });

        // HUD: mostra como usar enquanto voce segura o item do stand
        HudRenderCallback.EVENT.register((ctx, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.options.hudHidden || mc.currentScreen != null) return;
            Item held = mc.player.getMainHandStack().getItem();
            List<String> lines;
            if (held == JojoStands.KING_CRIMSON) {
                lines = List.of("§cKing Crimson",
                        "§fClique: §7Apagar Tempo (5 s)",
                        "§fShift + clique: §7Epitafio (5 s)",
                        "§fRecarga: §710 s   §fG: §7menu");
            } else if (held == JojoStands.GOLDEN_EXPERIENCE) {
                lines = List.of("§6Golden Experience",
                        "§fModo: §e" + ClientState.MODES[ClientState.mode],
                        "§7" + ClientState.USAGE[ClientState.mode],
                        "§fMob: §7" + ClientState.mobName + "  §fBloco: §7" + ClientState.blockName,
                        "§fG: §7menu");
            } else return;

            boolean cooling = mc.player.getItemCooldownManager().getCooldownProgress(held, 0f) > 0;
            int y = 6;
            for (String l : lines) {
                ctx.drawTextWithShadow(mc.textRenderer, Text.literal(l), 6, y, 0xFFFFFF);
                y += 11;
            }
            if (cooling) ctx.drawTextWithShadow(mc.textRenderer, Text.literal("§cRecarregando..."), 6, y, 0xFFFFFF);
        });
    }
}
