package com.jojostands;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class KingCrimsonItem extends Item {
    public static final int COOLDOWN = 20;          // 1 s
    public static final int EPITAPH_TICKS = 400;    // 20 s

    public KingCrimsonItem(Settings settings) { super(settings); }

    @Override
    public boolean hasGlint(ItemStack stack) { return true; }

    /** Clique = Apagar Tempo (clique de novo encerra) | Shift + clique = Epitafio (shift de novo encerra) */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack);
        if (player.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
        ServerWorld sw = (ServerWorld) world;
        ServerPlayerEntity sp = (ServerPlayerEntity) player;

        if (player.isSneaking()) {
            if (epitaphActive(sw, sp)) endEpitaph(sp); else startEpitaph(sw, sp);
        } else {
            if (TimeErase.isActive(sp)) TimeErase.end(sw, sp); else TimeErase.start(sw, sp);
        }
        player.getItemCooldownManager().set(this, COOLDOWN);
        return TypedActionResult.success(stack);
    }

    // ---------------- Epitafio ----------------
    public static boolean epitaphActive(ServerWorld w, ServerPlayerEntity p) {
        return StandManager.get(p).epitaphUntil > w.getTime();
    }

    private static void startEpitaph(ServerWorld sw, ServerPlayerEntity p) {
        StandManager.get(p).epitaphUntil = sw.getTime() + EPITAPH_TICKS;
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, EPITAPH_TICKS, 0, false, false));
        sw.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1.2f, 0.6f);
        Fx.ring(sw, p.getPos().add(0, 0.2, 0), Fx.RED, 2, 30);
        Fx.burst(sw, p.getPos().add(0, 1.5, 0), Fx.RED, 40);
        p.sendMessage(Text.literal("§cEpitafio: voce ve o futuro! Todo ataque sera evitado por 20 s."), true);
    }

    private static void endEpitaph(ServerPlayerEntity p) {
        StandManager.get(p).epitaphUntil = 0;
        p.removeStatusEffect(StatusEffects.SPEED);
        p.sendMessage(Text.literal("§cEpitafio encerrado."), true);
    }

    public static void tickEpitaph(ServerWorld w, ServerPlayerEntity p) {
        long left = StandManager.get(p).epitaphUntil - w.getTime();
        if (left <= 0) return;
        if (left % 20 == 0) {
            for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, p.getBoundingBox().expand(32),
                    x -> x != p && x.isAlive())) {
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 40, 0, false, false));
            }
        }
        if (left % 4 == 0) Fx.ring(w, p.getPos().add(0, 2.2, 0), Fx.RED, 0.5, 8);
        if (left % 10 == 0 && !TimeErase.isActive(p)) {
            p.sendMessage(Text.literal("§cEpitafio: " + (left / 20) + "s"), true);
        }
    }
}
