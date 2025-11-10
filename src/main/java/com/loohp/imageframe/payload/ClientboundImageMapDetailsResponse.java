package com.loohp.imageframe.payload;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClientboundImageMapDetailsResponse(int index, int width, int height, IntList mapIds) implements CustomPayload {

    public static final Id<ClientboundImageMapDetailsResponse> ID = new Id<>(Identifier.of("imageframe", "clientbound_imagemap_details"));

    public static final PacketCodec<RegistryByteBuf, ClientboundImageMapDetailsResponse> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, ClientboundImageMapDetailsResponse::index,
            PacketCodecs.INTEGER, ClientboundImageMapDetailsResponse::width,
            PacketCodecs.INTEGER, ClientboundImageMapDetailsResponse::height,
            PacketCodecs.collection(IntArrayList::new, PacketCodecs.INTEGER), ClientboundImageMapDetailsResponse::mapIds,
            ClientboundImageMapDetailsResponse::new
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

}