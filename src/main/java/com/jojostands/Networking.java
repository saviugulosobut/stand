package com.jojostands;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class Networking {
    private Networking() {}

    public static void register() {
        PayloadTypeRegistry.playC2S().register(StandPayload.ID, StandPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(StandPayload.ID,
                (payload, context) -> handle(payload, context.player()));
    }

    private static void handle(StandPayload payload, ServerPlayerEntity p) {
        StandData d = StandManager.get(p);
        switch (payload.kind()) {
            case 0 -> {
                try { d.mode = Math.floorMod(Integer.parseInt(payload.value()), 5); }
                catch (NumberFormatException ignored) {}
            }
            case 1 -> {
                Identifier id = Identifier.tryParse(payload.value());
                if (id != null) Registries.ENTITY_TYPE.getOrEmpty(id)
                        .filter(StandRules::isAllowedMob).ifPresent(t -> d.mob = t);
            }
            case 2 -> {
                Identifier id = Identifier.tryParse(payload.value());
                if (id != null) Registries.BLOCK.getOrEmpty(id)
                        .filter(StandRules::isAllowedBlock).ifPresent(b -> d.block = b);
            }
            case 3 -> {
                Item item = payload.value().equals("king_crimson") ? JojoStands.KING_CRIMSON : JojoStands.GOLDEN_EXPERIENCE;
                if (p.getInventory().count(item) == 0) p.giveItemStack(new ItemStack(item));
                p.sendMessage(Text.literal("Stand equipado!"), true);
            }
            default -> {}
        }
    }
}
