package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ServerboundHdImageRequest(int mapId) implements CustomPacketPayload {

    public static final Type<ServerboundHdImageRequest> ID = new Type<>(Identifier.fromNamespaceAndPath("imageframe", "serverbound_hd_image"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundHdImageRequest> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundHdImageRequest::mapId,
            ServerboundHdImageRequest::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return ID; }

}