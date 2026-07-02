package com.loohp.imageframe.payload;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record ClientboundImageListResponse(List<ImageInfo> images) implements CustomPacketPayload {

    public static final Type<ClientboundImageListResponse> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("imageframe", "clientbound_image_list"));

    private static final StreamCodec<RegistryFriendlyByteBuf, ImageInfo> IMAGE_INFO_STREAM = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, ImageInfo::name,
            ByteBufCodecs.VAR_INT, ImageInfo::width,
            ByteBufCodecs.VAR_INT, ImageInfo::height,
            ByteBufCodecs.VAR_LONG, ImageInfo::fileSize,
            ImageInfo::new
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundImageListResponse> STREAM_CODEC = StreamCodec.of(
            (buf, val) -> {
                List<ImageInfo> list = val.images();
                ByteBufCodecs.VAR_INT.encode(buf, list.size());
                for (ImageInfo info : list) {
                    IMAGE_INFO_STREAM.encode(buf, info);
                }
            },
            buf -> {
                int len = ByteBufCodecs.VAR_INT.decode(buf);
                List<ImageInfo> list = new ArrayList<>(len);
                for (int i = 0; i < len; i++) {
                    list.add(IMAGE_INFO_STREAM.decode(buf));
                }
                return new ClientboundImageListResponse(list);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

}
