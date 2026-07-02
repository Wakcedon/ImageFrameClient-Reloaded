package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ServerboundImageUpload(String name, int width, int height, byte[] pngData) implements CustomPacketPayload {

    public static final Type<ServerboundImageUpload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("imageframe", "serverbound_image_upload"));

    private static final int MAX_UPLOAD_SIZE = 50 * 1024 * 1024;

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundImageUpload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ServerboundImageUpload::name,
            ByteBufCodecs.VAR_INT, ServerboundImageUpload::width,
            ByteBufCodecs.VAR_INT, ServerboundImageUpload::height,
            ByteBufCodecs.byteArray(MAX_UPLOAD_SIZE), ServerboundImageUpload::pngData,
            ServerboundImageUpload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

}
