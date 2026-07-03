package com.loohp.imageframe.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class PayloadUtil {

    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> lenient(StreamCodec<RegistryFriendlyByteBuf, T> inner) {
        return new StreamCodec<>() {
            @Override
            public T decode(RegistryFriendlyByteBuf buf) {
                T value = inner.decode(buf);
                buf.skipBytes(buf.readableBytes());
                return value;
            }
            @Override
            public void encode(RegistryFriendlyByteBuf buf, T value) {
                inner.encode(buf, value);
            }
        };
    }

    private PayloadUtil() {}
}
