package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.Optional;

public record ClientboundHdImageResponse(int mapId, boolean requestAccepted, byte[] data, Optional<Integer> multipart) implements CustomPayload {

    public static final Id<ClientboundHdImageResponse> ID = new Id<>(Identifier.of("imageframe", "clientbound_hd_image"));

    public static final PacketCodec<RegistryByteBuf, ClientboundHdImageResponse> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ClientboundHdImageResponse::mapId,
            PacketCodecs.BOOLEAN, ClientboundHdImageResponse::requestAccepted,
            PacketCodecs.BYTE_ARRAY, ClientboundHdImageResponse::data,
            PacketCodecs.optional(PacketCodecs.INTEGER), ClientboundHdImageResponse::multipart,
            ClientboundHdImageResponse::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

}