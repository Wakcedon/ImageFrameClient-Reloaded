package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ServerboundImageMapDetailsRequest(int index) implements CustomPayload {

    public static final Id<ServerboundImageMapDetailsRequest> ID = new Id<>(Identifier.of("imageframe", "serverbound_imagemap_details"));

    public static final PacketCodec<RegistryByteBuf, ServerboundImageMapDetailsRequest> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ServerboundImageMapDetailsRequest::index,
            ServerboundImageMapDetailsRequest::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

}