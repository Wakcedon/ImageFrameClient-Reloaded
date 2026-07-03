package com.loohp.imageframe.handler;

import com.loohp.imageframe.payload.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public final class ClientPayloadHandler {

    public static volatile List<ImageInfo> pendingImageList;
    public static volatile String pendingToastMessage;

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1").optional();

        registrar.playToClient(ClientboundAcknowledgement.TYPE, ClientboundAcknowledgement.STREAM_CODEC,
                ClientPayloadHandler::handleAcknowledgement);

        registrar.playToClient(ClientboundHdImageResponse.TYPE, ClientboundHdImageResponse.STREAM_CODEC,
                ClientPayloadHandler::handleHdImageResponse);

        registrar.playToClient(ClientboundHdImageMultipartResponse.TYPE, ClientboundHdImageMultipartResponse.STREAM_CODEC,
                ClientPayloadHandler::handleHdImageMultipart);

        registrar.playToClient(ClientboundImageUpdatedSignal.TYPE, ClientboundImageUpdatedSignal.STREAM_CODEC,
                ClientPayloadHandler::handleImageUpdated);

        registrar.playToClient(ClientboundImageMapDetailsResponse.TYPE, ClientboundImageMapDetailsResponse.STREAM_CODEC,
                ClientPayloadHandler::handleImageMapDetails);

        registrar.playToClient(ClientboundImageListResponse.TYPE, ClientboundImageListResponse.STREAM_CODEC,
                ClientPayloadHandler::handleImageList);

        registrar.playToClient(ClientboundImageUploadAck.TYPE, ClientboundImageUploadAck.STREAM_CODEC,
                ClientPayloadHandler::handleUploadAck);

        registrar.playToClient(ClientboundImageDeleteAck.TYPE, ClientboundImageDeleteAck.STREAM_CODEC,
                ClientPayloadHandler::handleDeleteAck);
    }

    private static void handleAcknowledgement(ClientboundAcknowledgement payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.loohp.imageframe.ImageFrameClient.MOD.onServerAcknowledged(payload));
    }

    private static void handleHdImageResponse(ClientboundHdImageResponse payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.loohp.imageframe.ImageFrameClient.MOD.onHdImageResponse(payload));
    }

    private static void handleHdImageMultipart(ClientboundHdImageMultipartResponse payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.loohp.imageframe.ImageFrameClient.MOD.onHdImageMultipart(payload));
    }

    private static void handleImageUpdated(ClientboundImageUpdatedSignal payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.loohp.imageframe.ImageFrameClient.MOD.onImageUpdated(payload));
    }

    private static void handleImageMapDetails(ClientboundImageMapDetailsResponse payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> com.loohp.imageframe.ImageFrameClient.MOD.onImageMapDetails(payload));
    }

    private static void handleImageList(ClientboundImageListResponse payload, IPayloadContext ctx) {
        pendingImageList = List.copyOf(payload.images());
    }

    private static void handleUploadAck(ClientboundImageUploadAck payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Component msg = Component.literal(payload.message());
            if (payload.success()) {
                pendingToastMessage = payload.message();
            }
            Minecraft.getInstance().player.displayClientMessage(msg, false);
        });
    }

    private static void handleDeleteAck(ClientboundImageDeleteAck payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Component msg = Component.literal(payload.message());
            Minecraft.getInstance().player.displayClientMessage(msg, false);
            pendingToastMessage = payload.message();
        });
    }

    private ClientPayloadHandler() {}
}
