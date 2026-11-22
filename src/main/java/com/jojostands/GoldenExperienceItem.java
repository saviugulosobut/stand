package com.jojostands;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class GoldenExperienceItem extends Item {
    /** Recarga (em ticks, 20 = 1 s) ao criar/invocar mobs. */
    public static final int MOB_COOLDOWN = 200;   // 10 s
    public static final int OTHER_COOLDOWN = 10;  // 0,5 s

    private static final List<EntityType<?>> ANIMALS = List.of(EntityType.RABBIT, EntityType.CHICKEN,
            EntityType.PIG, EntityType.COW, EntityType.SHEEP, EntityType.FROG);
    private static final List<net.minecraft.block.Block> FLOWERS = List.of(Blocks.DANDELION, Blocks.POPPY,
            Blocks.AZURE_BLUET, Blocks.OXEYE_DAISY, Blocks.CORNFLOWER);

    public GoldenExperienceItem(Settings settings) { super(settings); }

    @Override
    public boolean hasGlint(ItemStack stack) { return true; }

    // Clique no AR
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient) return TypedActionResult.success(stack);
        ServerWorld sw = (ServerWorld) world;
        ServerPlayerEntity sp = (ServerPlayerEntity) player;
        if (player.getItemCooldownManager().isCoolingDown(this)) return TypedActionResult.fail(stack);
        StandData d = StandManager.get(player);

        switch (d.mode) {
            case 0 -> { // curar
                player.heal(6f);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 100, 1));
                Fx.burst(sw, player.getPos().add(0, 1, 0), Fx.GOLD, 25);
                sp.sendMessage(Text.literal("§6Golden Experience: energia de vida!"), true);
                player.getItemCooldownManager().set(this, OTHER_COOLDOWN);
            }
            case 2 -> { // invocar mob protetor na frente
                Vec3d front = player.getPos().add(player.getRotationVector().multiply(2, 0, 2));
                spawnAlly(sw, sp, d.mob, BlockPos.ofFloored(front));
                player.getItemCooldownManager().set(this, MOB_COOLDOWN);
            }
            case 4 -> {
                if (player.isSneaking()) {
                    d.target = null;
                    sp.sendMessage(Text.literal("§6Aliados vao te seguir."), true);
                } else {
                    sp.sendMessage(Text.literal("§6Clique num bloco para mandar os aliados la (shift + clique no ar = seguir)."), true);
                }
            }
            default -> sp.sendMessage(Text.literal("§6Clique em um bloco."), true);
        }
        return TypedActionResult.success(stack);
    }

    // Clique num BLOCO
    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        World world = ctx.getWorld();
        if (world.isClient) return ActionResult.SUCCESS;
        ServerWorld sw = (ServerWorld) world;
        if (!(ctx.getPlayer() instanceof ServerPlayerEntity p)) return ActionResult.PASS;
        if (p.getItemCooldownManager().isCoolingDown(this)) return ActionResult.FAIL;

        BlockPos pos = ctx.getBlockPos();
        BlockState state = sw.getBlockState(pos);
        StandData d = StandManager.get(p);
        boolean changeable = !state.isAir() && state.getHardness(sw, pos) >= 0;

        switch (d.mode) {
            case 0 -> { // criar vida
                if (BoneMealItem.useOnFertilizable(new ItemStack(Items.BONE_MEAL), sw, pos)) {
                    sw.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5, 15, .4, .4, .4, 0);
                } else if (changeable) {
                    if (sw.random.nextBoolean()) {
                        sw.setBlockState(pos, Blocks.GRASS_BLOCK.getDefaultState());
                        if (sw.isAir(pos.up())) sw.setBlockState(pos.up(), FLOWERS.get(sw.random.nextInt(FLOWERS.size())).getDefaultState());
                    } else {
                        sw.setBlockState(pos, Blocks.AIR.getDefaultState());
                        ANIMALS.get(sw.random.nextInt(ANIMALS.size())).spawn(sw, pos, SpawnReason.MOB_SUMMONED);
                    }
                    Fx.burst(sw, Vec3d.ofCenter(pos), Fx.GOLD, 25);
                } else return ActionResult.FAIL;
                p.getItemCooldownManager().set(this, OTHER_COOLDOWN);
            }
            case 1 -> { // bloco -> mob aliado
                if (!changeable) return ActionResult.FAIL;
                sw.setBlockState(pos, Blocks.AIR.getDefaultState());
                spawnAlly(sw, p, d.mob, pos);
                p.getItemCooldownManager().set(this, MOB_COOLDOWN);
            }
            case 2 -> { // invocar mob em cima do bloco
                spawnAlly(sw, p, d.mob, pos.up());
                p.getItemCooldownManager().set(this, MOB_COOLDOWN);
            }
            case 3 -> { // bloco -> outro bloco
                if (!changeable) return ActionResult.FAIL;
                sw.setBlockState(pos, d.block.getDefaultState());
                Fx.burst(sw, Vec3d.ofCenter(pos), Fx.GOLD, 25);
                p.getItemCooldownManager().set(this, OTHER_COOLDOWN);
            }
            case 4 -> { // mandar aliados para la
                d.target = pos.up();
                Fx.burst(sw, Vec3d.ofBottomCenter(pos.up()), Fx.GOLD, 20);
                p.sendMessage(Text.literal("§6Aliados vao para esse lugar."), true);
                p.getItemCooldownManager().set(this, OTHER_COOLDOWN);
            }
            default -> {}
        }
        return ActionResult.SUCCESS;
    }

    private static void spawnAlly(ServerWorld sw, ServerPlayerEntity owner, EntityType<?> type, BlockPos pos) {
        Entity e = type.spawn(sw, pos, SpawnReason.MOB_SUMMONED);
        if (e instanceof MobEntity m) {
            StandManager.makeAlly(m, owner);
            Fx.burst(sw, m.getPos().add(0, m.getHeight() / 2, 0), Fx.GOLD, 30);
        }
    }
}
