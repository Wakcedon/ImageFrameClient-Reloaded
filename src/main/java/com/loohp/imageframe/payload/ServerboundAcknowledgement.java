package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundAcknowledgement(long id) implements CustomPacketPayload {

    public static final Type<ServerboundAcknowledgement> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("imageframe", "serverbound_ack"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundAcknowledgement> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, ServerboundAcknowledgement::id,
            ServerboundAcknowledgement::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

}
