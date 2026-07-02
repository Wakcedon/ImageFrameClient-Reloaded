package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundAcknowledgement(long id) implements CustomPacketPayload {

    public static final Type<ClientboundAcknowledgement> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("imageframe", "clientbound_ack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAcknowledgement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, ClientboundAcknowledgement::id,
            ClientboundAcknowledgement::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

}
