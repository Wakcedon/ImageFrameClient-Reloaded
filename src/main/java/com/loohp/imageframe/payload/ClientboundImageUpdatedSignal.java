package com.loohp.imageframe.payload;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ClientboundImageUpdatedSignal(IntSet indexes, IntSet mapIds) implements CustomPacketPayload {

    public static final Type<ClientboundImageUpdatedSignal> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("imageframe", "clientbound_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundImageUpdatedSignal> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(IntOpenHashSet::new, ByteBufCodecs.INT), ClientboundImageUpdatedSignal::indexes,
            ByteBufCodecs.collection(IntOpenHashSet::new, ByteBufCodecs.INT), ClientboundImageUpdatedSignal::mapIds,
            ClientboundImageUpdatedSignal::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

}
