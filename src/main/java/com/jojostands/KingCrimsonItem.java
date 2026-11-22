package com.jojostands;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
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
    public static final int COOLDOWN = 200;        // 10 s
    public static final int TIME_ERASE_TICKS = 100; // 5 s
    public static final int EPITAPH_TICKS = 100;    // 5 s

    public KingCrimsonItem(Settings settings) { super(settings); }

    @Override
    public boolean hasGlint(ItemStack stack) { return true; }

    /** Clique = Apagar Tempo | Shift + clique = Epitafio */
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack);
        if (player.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
        ServerWorld sw = (ServerWorld) world;
        ServerPlayerEntity sp = (ServerPlayerEntity) player;

        if (player.isSneaking()) epitaph(sw, sp); else timeErase(sw, sp);
        player.getItemCooldownManager().set(this, COOLDOWN);
        return TypedActionResult.success(stack);
    }

    private static void timeErase(ServerWorld sw, ServerPlayerEntity p) {
        for (LivingEntity e : sw.getEntitiesByClass(LivingEntity.class, p.getBoundingBox().expand(40),
                x -> x != p && x.isAlive())) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, TIME_ERASE_TICKS, 255, false, false));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, TIME_ERASE_TICKS, 255, false, false));
            if (e instanceof MobEntity m) m.setTarget(null);
        }
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, TIME_ERASE_TICKS, 2, false, false));
        sw.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1f, 0.5f);
        Fx.burst(sw, p.getPos().add(0, 1, 0), Fx.RED, 60);
        p.sendMessage(Text.literal("§cKing Crimson: o tempo foi apagado!"), true);
    }

    private static void epitaph(ServerWorld sw, ServerPlayerEntity p) {
        StandManager.get(p).epitaphUntil = sw.getTime() + EPITAPH_TICKS;
        for (LivingEntity e : sw.getEntitiesByClass(LivingEntity.class, p.getBoundingBox().expand(32),
                x -> x != p && x.isAlive())) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, EPITAPH_TICKS, 0, false, false));
        }
        sw.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_BEACON_ACTIVATE, SoundCategory.PLAYERS, 1f, 0.6f);
        Fx.burst(sw, p.getPos().add(0, 1.5, 0), Fx.RED, 30);
        p.sendMessage(Text.literal("§cEpitafio: voce viu o futuro! O proximo golpe sera evitado."), true);
    }
}
