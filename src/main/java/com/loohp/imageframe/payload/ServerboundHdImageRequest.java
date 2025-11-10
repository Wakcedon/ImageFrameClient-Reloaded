package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ServerboundHdImageRequest(int mapId) implements CustomPayload {

    public static final Id<ServerboundHdImageRequest> ID = new Id<>(Identifier.of("imageframe", "serverbound_hd_image"));

    public static final PacketCodec<RegistryByteBuf, ServerboundHdImageRequest> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ServerboundHdImageRequest::mapId,
            ServerboundHdImageRequest::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

}