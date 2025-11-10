package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ServerboundAcknowledgement(long id) implements CustomPayload {

    public static final Id<ServerboundAcknowledgement> ID = new Id<>(Identifier.of("imageframe", "serverbound_ack"));

    public static final PacketCodec<RegistryByteBuf, ServerboundAcknowledgement> CODEC = PacketCodec.tuple(
            PacketCodecs.LONG, ServerboundAcknowledgement::id,
            ServerboundAcknowledgement::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

}