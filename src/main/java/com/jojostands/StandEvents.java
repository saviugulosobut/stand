package com.jojostands;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.List;
import java.util.UUID;

public final class StandEvents {
    private static final ThreadLocal<Boolean> REFLECTING = ThreadLocal.withInitial(() -> false);

    private StandEvents() {}

    public static void register() {
        ServerTickEvents.END_WORLD_TICK.register(StandEvents::tickWorld);
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(StandEvents::allowDamage);
    }

    private static void tickWorld(ServerWorld world) {
        long t = world.getTime();
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (t % 3 == 0) aura(world, p);
            if (t % 5 == 0) tickAllies(world, p);
        }
    }

    /** Aura do stand: so muda a cor (dourado / vermelho) enquanto voce segura o item. */
    private static void aura(ServerWorld world, ServerPlayerEntity p) {
        Vector3f color = Fx.colorFor(p.getMainHandStack().getItem());
        if (color == null) return;
        Vec3d back = Vec3d.fromPolar(0, p.getYaw()).multiply(-0.9);
        world.spawnParticles(new DustParticleEffect(color, 1.5f),
                p.getX() + back.x, p.getY() + 1.1, p.getZ() + back.z, 8, 0.35, 0.7, 0.35, 0.0);
    }

    /** Aliados: seguem o dono / lugar marcado, defendem o dono e nunca o atacam. */
    private static void tickAllies(ServerWorld world, ServerPlayerEntity p) {
        UUID id = p.getUuid();
        List<MobEntity> allies = world.getEntitiesByClass(MobEntity.class, p.getBoundingBox().expand(64),
                m -> m.isAlive() && id.equals(StandManager.getOwner(m)));
        if (allies.isEmpty()) return;

        StandData d = StandManager.get(p);
        List<HostileEntity> hostiles = world.getEntitiesByClass(HostileEntity.class, p.getBoundingBox().expand(16),
                e -> e.isAlive() && !(e instanceof Angerable) && StandManager.getOwner(e) == null);

        LivingEntity hitMe = (p.age - p.getLastAttackedTime() < 100 && valid(p.getAttacker(), p)) ? p.getAttacker() : null;
        LivingEntity hitByMe = (p.age - p.getLastAttackTime() < 100 && valid(p.getAttacking(), p)) ? p.getAttacking() : null;

        for (MobEntity m : allies) {
            m.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 100, 0, false, false));

            LivingEntity cur = m.getTarget();
            if (cur != null && (cur == p || !cur.isAlive() || StandManager.getOwner(cur) != null)) {
                m.setTarget(null);
                cur = null;
            }
            if (cur == null) {
                LivingEntity pick = hitMe != null ? hitMe : hitByMe;
                if (pick == null) {
                    double best = Double.MAX_VALUE;
                    for (HostileEntity h : hostiles) {
                        if (h == m) continue;
                        double dd = m.squaredDistanceTo(h);
                        if (dd < best) { best = dd; pick = h; }
                    }
                }
                if (pick != null && pick != m) { m.setTarget(pick); cur = pick; }
            }
            if (cur == null) follow(m, p, d);
        }
    }

    private static boolean valid(LivingEntity e, ServerPlayerEntity p) {
        return e != null && e.isAlive() && e != p && StandManager.getOwner(e) == null;
    }

    private static void follow(MobEntity m, ServerPlayerEntity p, StandData d) {
        Vec3d dest = d.target != null ? Vec3d.ofBottomCenter(d.target) : p.getPos();
        double dist = m.getPos().distanceTo(dest);
        if (dist > 32) {
            m.refreshPositionAndAngles(dest.x + 1, dest.y, dest.z, m.getYaw(), m.getPitch());
            m.getNavigation().stop();
        } else if (dist > (d.target != null ? 2.0 : 5.0)) {
            m.getNavigation().startMovingTo(dest.x, dest.y, dest.z, 1.2);
        } else {
            m.getNavigation().stop();
        }
    }

    private static boolean allowDamage(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity.getWorld() instanceof ServerWorld world)) return true;
        Entity attacker = source.getAttacker();

        // ---- Dono: aliados nunca o machucam + Epitafio ----
        if (entity instanceof ServerPlayerEntity p) {
            if (attacker != null && p.getUuid().equals(StandManager.getOwner(attacker))) return false;
            StandData d = StandManager.get(p);
            if (d.epitaphUntil > world.getTime() && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                d.epitaphUntil = 0;
                p.sendMessage(Text.literal("§cEpitafio: voce previu o ataque e desviou!"), true);
                Fx.burst(world, p.getPos().add(0, 1, 0), Fx.RED, 30);
                return false;
            }
            return true;
        }

        // ---- Aliado: reflete o dano em quem atacar ----
        UUID owner = StandManager.getOwner(entity);
        if (owner != null) {
            if (attacker == null) return true; // queda, afogamento etc. continuam valendo
            if (owner.equals(attacker.getUuid())) return attacker.isSneaking(); // dono + shift pode matar o aliado
            if (StandManager.getOwner(attacker) != null) return false;          // aliados nao brigam entre si
            if (!REFLECTING.get()) {
                REFLECTING.set(true);
                try { attacker.damage(world.getDamageSources().magic(), amount); }
                finally { REFLECTING.set(false); }
            }
            Fx.burst(world, entity.getPos().add(0, entity.getHeight() / 2, 0), Fx.GOLD, 15);
            return false;
        }
        return true;
    }
                }
