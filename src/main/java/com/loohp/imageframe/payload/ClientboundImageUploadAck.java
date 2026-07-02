package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundImageUploadAck(boolean success, String message) implements CustomPacketPayload {

    public static final Type<ClientboundImageUploadAck> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("imageframe", "clientbound_upload_ack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundImageUploadAck> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundImageUploadAck::success,
            ByteBufCodecs.STRING_UTF8, ClientboundImageUploadAck::message,
            ClientboundImageUploadAck::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

}
