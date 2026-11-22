package com.jojostands;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class StandManager {
    private static final Map<UUID, StandData> DATA = new HashMap<>();
    public static final String TAG_PREFIX = "gx_owner:";

    private StandManager() {}

    public static StandData get(PlayerEntity p) {
        return DATA.computeIfAbsent(p.getUuid(), k -> new StandData());
    }

    /** O dono fica salvo numa command tag do mob, entao sobrevive a reiniciar o mundo. */
    public static UUID getOwner(Entity e) {
        for (String t : e.getCommandTags()) {
            if (t.startsWith(TAG_PREFIX)) {
                try { return UUID.fromString(t.substring(TAG_PREFIX.length())); }
                catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }

    public static void makeAlly(MobEntity mob, PlayerEntity owner) {
        mob.addCommandTag(TAG_PREFIX + owner.getUuid());
        mob.setPersistent();
        mob.setTarget(null);
    }
}
