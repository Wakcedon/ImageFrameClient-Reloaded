package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientboundHdImageMultipartResponse(int mapId, int multipart, int index, byte[] data, boolean end) implements CustomPayload {

    public static final Id<ClientboundHdImageMultipartResponse> ID = new Id<>(Identifier.of("imageframe", "clientbound_hd_image_multi"));

    public static final PacketCodec<RegistryByteBuf, ClientboundHdImageMultipartResponse> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ClientboundHdImageMultipartResponse::mapId,
            PacketCodecs.INTEGER, ClientboundHdImageMultipartResponse::multipart,
            PacketCodecs.INTEGER, ClientboundHdImageMultipartResponse::index,
            PacketCodecs.BYTE_ARRAY, ClientboundHdImageMultipartResponse::data,
            PacketCodecs.BOOLEAN, ClientboundHdImageMultipartResponse::end,
            ClientboundHdImageMultipartResponse::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

}