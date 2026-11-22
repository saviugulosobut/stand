package com.jojostands;

import net.minecraft.item.Item;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

public final class Fx {
    public static final Vector3f GOLD = new Vector3f(1.0f, 0.78f, 0.10f);
    public static final Vector3f RED = new Vector3f(0.90f, 0.02f, 0.05f);

    private Fx() {}

    public static Vector3f colorFor(Item item) {
        if (item == JojoStands.GOLDEN_EXPERIENCE) return GOLD;
        if (item == JojoStands.KING_CRIMSON) return RED;
        return null;
    }

    public static void burst(ServerWorld w, Vec3d pos, Vector3f color, int count) {
        w.spawnParticles(new DustParticleEffect(color, 1.6f), pos.x, pos.y, pos.z, count, 0.5, 0.6, 0.5, 0.02);
    }

    public static void ring(ServerWorld w, Vec3d c, Vector3f color, double r, int n) {
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            w.spawnParticles(new DustParticleEffect(color, 1.0f), c.x + Math.cos(a) * r, c.y, c.z + Math.sin(a) * r, 1, 0, 0, 0, 0);
        }
    }
}
