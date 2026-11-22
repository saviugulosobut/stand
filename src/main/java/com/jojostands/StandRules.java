package com.jojostands;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Items;

import java.util.Set;

/** Quais mobs/blocos o Golden Experience pode usar. Edite aqui para liberar/bloquear. */
public final class StandRules {
    private static final Set<EntityType<?>> EXTRA_MOBS = Set.of(
            EntityType.VILLAGER, EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.WANDERING_TRADER);

    private static final Set<Block> BANNED_BLOCKS = Set.of(
            Blocks.BEDROCK, Blocks.BARRIER, Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK,
            Blocks.REPEATING_COMMAND_BLOCK, Blocks.STRUCTURE_BLOCK, Blocks.JIGSAW, Blocks.LIGHT,
            Blocks.END_PORTAL_FRAME);

    private StandRules() {}

    public static boolean isAllowedMob(EntityType<?> t) {
        if (t == EntityType.ENDER_DRAGON) return false;
        if (!t.isSummonable()) return false;
        if (t.getSpawnGroup() != SpawnGroup.MISC) return true;
        return EXTRA_MOBS.contains(t);
    }

    public static boolean isAllowedBlock(Block b) {
        return b.asItem() != Items.AIR && !BANNED_BLOCKS.contains(b);
    }
}
