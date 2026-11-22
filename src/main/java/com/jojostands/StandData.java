package com.jojostands;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.util.math.BlockPos;

/** Estado do stand de cada jogador (fica na memoria do servidor). */
public class StandData {
    public int mode = 0;
    public EntityType<?> mob = EntityType.ZOMBIE;
    public Block block = Blocks.GOLD_BLOCK;
    public BlockPos target = null;   // lugar para onde os aliados vao (null = seguir o dono)
    public long epitaphUntil = 0;    // tick ate quando o Epitafio esta ativo
}
