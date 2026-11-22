package com.jojostands;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.*;

/**
 * Apagar Tempo do King Crimson.
 * - Congela mobs, jogadores e projeteis ao redor por 20 s (so voce se mexe).
 * - Todo dano que voce causa nos congelados fica ACUMULADO e e aplicado de uma vez quando o tempo volta.
 */
public final class TimeErase {
    public static final int DURATION_TICKS = 400;      // 20 s
    public static final double RADIUS = 48;
    public static final float DAMAGE_BONUS = 1.25f;    // bonus sobre o dano acumulado
    private static final String TAG = "kc_frozen";      // mob teve a IA desligada por nos
    private static final String TAG_NG = "kc_ng";       // gravidade desligada por nos

    private static final class Session {
        long endTick;
        final Set<UUID> frozen = new HashSet<>();
        final Map<UUID, Float> damage = new LinkedHashMap<>();
        final Map<UUID, Vec3d> velocity = new HashMap<>();

        float total() {
            float t = 0;
            for (float v : damage.values()) t += v;
            return t * DAMAGE_BONUS;
        }
    }

    private static final Map<UUID, Session> SESSIONS = new HashMap<>();
    private static final Set<UUID> FROZEN = new HashSet<>();

    private TimeErase() {}

    public static boolean isActive(ServerPlayerEntity p) { return SESSIONS.containsKey(p.getUuid()); }
    public static boolean isFrozen(Entity e) { return FROZEN.contains(e.getUuid()); }

    public static void start(ServerWorld w, ServerPlayerEntity p) {
        Session s = new Session();
        s.endTick = w.getTime() + DURATION_TICKS;
        SESSIONS.put(p.getUuid(), s);
        freezeNearby(w, p, s);

        p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, DURATION_TICKS, 2, false, false));
        p.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, DURATION_TICKS, 1, false, false));
        w.playSound(null, p.getBlockPos(), SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.PLAYERS, 1.5f, 0.4f);
        Vec3d c = p.getPos().add(0, 0.2, 0);
        for (int r = 2; r <= 16; r += 2) Fx.ring(w, c, Fx.RED, r, r * 8);
        Fx.burst(w, p.getPos().add(0, 1, 0), Fx.RED, 80);
        p.sendMessage(Text.literal("§cKing Crimson: O TEMPO FOI APAGADO!"), true);
    }

    public static void tick(ServerWorld w, ServerPlayerEntity p) {
        Session s = SESSIONS.get(p.getUuid());
        if (s == null) return;
        long left = s.endTick - w.getTime();
        if (left <= 0) { end(w, p); return; }

        for (UUID id : s.frozen) {
            Entity e = w.getEntity(id);
            if (e != null && !e.isRemoved() && !(e instanceof PlayerEntity)) e.setVelocity(Vec3d.ZERO);
        }

        if (left % 10 == 0) {
            freezeNearby(w, p, s);
            for (UUID id : s.frozen) {
                Entity e = w.getEntity(id);
                if (e != null) {
                    w.spawnParticles(new DustParticleEffect(Fx.RED, 0.8f), e.getX(), e.getY() + e.getHeight() / 2, e.getZ(), 2, 0.2, 0.3, 0.2, 0);
                }
            }
            p.sendMessage(Text.literal("§c⏱ Tempo apagado: " + (left / 20) + "s  §7| §fDano acumulado: §c"
                    + String.format("%.1f", s.total())), true);
        }
        if (left % 4 == 0) {
            Fx.ring(w, p.getPos().add(0, 0.1 + (left % 20) / 10.0, 0), Fx.RED, 1.2, 12);
            w.spawnParticles(ParticleTypes.CRIMSON_SPORE, p.getX(), p.getY() + 1, p.getZ(), 4, 0.5, 0.8, 0.5, 0);
        }
    }

    private static boolean canFreeze(Entity x, ServerPlayerEntity p) {
        if (x == p || x.isRemoved() || x.isSpectator()) return false;
        if (x instanceof ProjectileEntity pr) return pr.getOwner() != p;
        if (!(x instanceof LivingEntity) || x instanceof ArmorStandEntity || !x.isAlive()) return false;
        return !p.getUuid().equals(StandManager.getOwner(x)); // seus aliados continuam se mexendo
    }

    private static void freezeNearby(ServerWorld w, ServerPlayerEntity p, Session s) {
        for (Entity e : w.getEntitiesByClass(Entity.class, p.getBoundingBox().expand(RADIUS), x -> canFreeze(x, p))) {
            if (s.frozen.add(e.getUuid())) freeze(e, s);
            else if (e instanceof PlayerEntity pe) lock(pe);
        }
    }

    private static void freeze(Entity e, Session s) {
        FROZEN.add(e.getUuid());
        if (e instanceof ProjectileEntity) {
            s.velocity.put(e.getUuid(), e.getVelocity());
            e.addCommandTag(TAG);
        }
        if (e instanceof PlayerEntity pe) {
            lock(pe);
            return;
        }
        if (!e.hasNoGravity()) { e.setNoGravity(true); e.addCommandTag(TAG_NG); }
        if (e instanceof MobEntity m) {
            if (!m.isAiDisabled()) { m.setAiDisabled(true); m.addCommandTag(TAG); }
            m.setTarget(null);
        }
        e.setVelocity(Vec3d.ZERO);
    }

    private static void lock(PlayerEntity pe) {
        pe.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 30, 255, false, false));
        pe.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 30, 255, false, false));
        pe.addStatusEffect(new StatusEffectInstance(StatusEffects.MINING_FATIGUE, 30, 255, false, false));
    }

    private static void unfreeze(Entity e, Session s) {
        if (e.getCommandTags().contains(TAG_NG)) { e.removeCommandTag(TAG_NG); e.setNoGravity(false); }
        if (e.getCommandTags().contains(TAG)) {
            e.removeCommandTag(TAG);
            if (e instanceof MobEntity m) m.setAiDisabled(false);
        }
        if (s != null) {
            Vec3d v = s.velocity.get(e.getUuid());
            if (v != null) e.setVelocity(v);
        }
        if (e instanceof PlayerEntity pe) {
            pe.removeStatusEffect(StatusEffects.SLOWNESS);
            pe.removeStatusEffect(StatusEffects.WEAKNESS);
            pe.removeStatusEffect(StatusEffects.MINING_FATIGUE);
        }
    }

    /** Termina o Apagar Tempo e aplica TODO o dano acumulado de uma vez. */
    public static void end(ServerWorld w, ServerPlayerEntity p) {
        Session s = SESSIONS.remove(p.getUuid());
        if (s == null) return;

        for (UUID id : s.frozen) {
            Entity e = w.getEntity(id);
            if (e != null) unfreeze(e, s);
            FROZEN.remove(id);
        }
        p.removeStatusEffect(StatusEffects.SPEED);
        p.removeStatusEffect(StatusEffects.JUMP_BOOST);

        float dealt = 0;
        int hits = 0;
        for (Map.Entry<UUID, Float> en : s.damage.entrySet()) {
            if (w.getEntity(en.getKey()) instanceof LivingEntity le && le.isAlive()) {
                float dmg = en.getValue() * DAMAGE_BONUS;
                le.timeUntilRegen = 0;
                le.damage(w.getDamageSources().indirectMagic(p, p), dmg);
                Fx.burst(w, le.getPos().add(0, le.getHeight() / 2, 0), Fx.RED, 50);
                w.spawnParticles(ParticleTypes.CRIT, le.getX(), le.getY() + le.getHeight() / 2, le.getZ(), 30, 0.4, 0.5, 0.4, 0.3);
                dealt += dmg;
                hits++;
            }
        }
        w.playSound(null, p.getBlockPos(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.PLAYERS, 1.2f, 0.8f);
        Fx.ring(w, p.getPos().add(0, 0.2, 0), Fx.RED, 3, 40);
        p.sendMessage(Text.literal("§cO tempo voltou! §fDano acumulado aplicado: §c" + String.format("%.1f", dealt)
                + " §7(" + hits + " alvo(s))"), true);
    }

    /** Chamado quando voce bate num congelado: guarda o dano em vez de aplicar. */
    public static void record(ServerPlayerEntity attacker, Entity victim, float amount) {
        Session s = SESSIONS.get(attacker.getUuid());
        if (s == null || !s.frozen.contains(victim.getUuid())) return;
        s.damage.merge(victim.getUuid(), amount, Float::sum);
        if (victim.getWorld() instanceof ServerWorld w) {
            w.spawnParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getHeight() / 2, victim.getZ(), 8, 0.3, 0.4, 0.3, 0.1);
        }
        attacker.sendMessage(Text.literal("§c+" + String.format("%.1f", amount) + " §fDano acumulado: §c"
                + String.format("%.1f", s.total())), true);
    }

    /** Limpeza de seguranca: entidade carregada com marca de congelamento sem sessao ativa. */
    public static void onLoad(Entity e) {
        if (FROZEN.contains(e.getUuid())) return;
        if (e.getCommandTags().contains(TAG) || e.getCommandTags().contains(TAG_NG)) unfreeze(e, null);
    }

    public static void endAll(MinecraftServer server) {
        for (UUID id : new ArrayList<>(SESSIONS.keySet())) {
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(id);
            if (p != null) end(p.getServerWorld(), p);
        }
        clear();
    }

    public static void clear() {
        SESSIONS.clear();
        FROZEN.clear();
    }
                      }
