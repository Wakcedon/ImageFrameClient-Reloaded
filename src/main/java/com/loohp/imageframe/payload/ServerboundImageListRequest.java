package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundImageListRequest() implements CustomPacketPayload {

    public static final Type<ServerboundImageListRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("imageframe", "serverbound_image_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundImageListRequest> STREAM_CODEC = StreamCodec.unit(new ServerboundImageListRequest());

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

}
