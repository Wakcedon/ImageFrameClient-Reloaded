package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundHdImageRequest(int mapId) implements CustomPacketPayload {

    public static final Type<ServerboundHdImageRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("imageframe", "serverbound_hd_image"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundHdImageRequest> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ServerboundHdImageRequest::mapId,
            ServerboundHdImageRequest::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

}
