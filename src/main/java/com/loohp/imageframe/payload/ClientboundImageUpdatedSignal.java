package com.loohp.imageframe.payload;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientboundImageUpdatedSignal(IntSet indexes, IntSet mapIds) implements CustomPayload {

    public static final Id<ClientboundImageUpdatedSignal> ID = new Id<>(Identifier.of("imageframe", "clientbound_update"));

    public static final PacketCodec<RegistryByteBuf, ClientboundImageUpdatedSignal> CODEC = PacketCodec.tuple(
            PacketCodecs.collection(IntOpenHashSet::new, PacketCodecs.INTEGER), ClientboundImageUpdatedSignal::indexes,
            PacketCodecs.collection(IntOpenHashSet::new, PacketCodecs.INTEGER), ClientboundImageUpdatedSignal::mapIds,
            ClientboundImageUpdatedSignal::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

}