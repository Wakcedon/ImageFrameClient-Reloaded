package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundImageDeleteAck(boolean success, String message) implements CustomPacketPayload {

    public static final Type<ClientboundImageDeleteAck> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("imageframe", "clientbound_delete_ack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundImageDeleteAck> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ClientboundImageDeleteAck::success,
            ByteBufCodecs.STRING_UTF8, ClientboundImageDeleteAck::message,
            ClientboundImageDeleteAck::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

}
