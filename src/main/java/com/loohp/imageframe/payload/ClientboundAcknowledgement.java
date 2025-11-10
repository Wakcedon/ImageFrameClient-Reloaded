package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientboundAcknowledgement(long id) implements CustomPayload {

    public static final Id<ClientboundAcknowledgement> ID = new Id<>(Identifier.of("imageframe", "clientbound_ack"));

    public static final PacketCodec<RegistryByteBuf, ClientboundAcknowledgement> CODEC = PacketCodec.tuple(
            PacketCodecs.LONG, ClientboundAcknowledgement::id,
            ClientboundAcknowledgement::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

}