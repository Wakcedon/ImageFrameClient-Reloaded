package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record ClientboundHdImageResponse(int mapId, boolean requestAccepted, byte[] data, Optional<Integer> multipart) implements CustomPacketPayload {

    public static final Type<ClientboundHdImageResponse> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("imageframe", "clientbound_hd_image"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundHdImageResponse> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ClientboundHdImageResponse::mapId,
            ByteBufCodecs.BOOL, ClientboundHdImageResponse::requestAccepted,
            ByteBufCodecs.BYTE_ARRAY, ClientboundHdImageResponse::data,
            ByteBufCodecs.optional(ByteBufCodecs.INT), ClientboundHdImageResponse::multipart,
            ClientboundHdImageResponse::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

}
