package com.jojostands;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

/** kind: 0 = modo, 1 = mob escolhido, 2 = bloco escolhido, 3 = dar item do stand. */
public record StandPayload(int kind, String value) implements CustomPayload {
    public static final CustomPayload.Id<StandPayload> ID = new CustomPayload.Id<>(JojoStands.id("stand"));
    public static final PacketCodec<RegistryByteBuf, StandPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, StandPayload::kind,
            PacketCodecs.STRING, StandPayload::value,
            StandPayload::new);

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
}
