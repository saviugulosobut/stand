package com.jojostands;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class JojoStands implements ModInitializer {
    public static final String MOD_ID = "jojostands";
    public static Item GOLDEN_EXPERIENCE;
    public static Item KING_CRIMSON;

    public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }

    @Override
    public void onInitialize() {
        GOLDEN_EXPERIENCE = Registry.register(Registries.ITEM, id("golden_experience"),
                new GoldenExperienceItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));
        KING_CRIMSON = Registry.register(Registries.ITEM, id("king_crimson"),
                new KingCrimsonItem(new Item.Settings().maxCount(1).rarity(Rarity.EPIC)));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(e -> {
            e.add(GOLDEN_EXPERIENCE);
            e.add(KING_CRIMSON);
        });

        Networking.register();
        StandEvents.register();
    }
}
