package com.loohp.imageframe;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.loohp.imageframe.configuration.Configuration;
import com.loohp.imageframe.object.ImageMapData;
import com.loohp.imageframe.object.MultipartHdMapInfo;
import com.loohp.imageframe.payload.ClientboundAcknowledgement;
import com.loohp.imageframe.payload.ClientboundHdImageMultipartResponse;
import com.loohp.imageframe.payload.ClientboundHdImageResponse;
import com.loohp.imageframe.payload.ClientboundImageMapDetailsResponse;
import com.loohp.imageframe.payload.ClientboundImageUpdatedSignal;
import com.loohp.imageframe.payload.ServerboundAcknowledgement;
import com.loohp.imageframe.payload.ServerboundHdImageRequest;
import com.loohp.imageframe.payload.ServerboundImageMapDetailsRequest;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod("imageframeclient")
public class ImageFrameClient {

    public static final Logger LOGGER = LoggerFactory.getLogger("imageframeclient");
    public static ImageFrameClient MOD;

    private final AtomicBoolean currentServerSupported = new AtomicBoolean(false);
    private final Int2ObjectMap<DynamicTexture> loadedHdTextures = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Optional<ImageMapData>> imageMapData = new Int2ObjectOpenHashMap<>();
    private final Cache<Integer, MultipartHdMapInfo> pendingMultipart = CacheBuilder.newBuilder().expireAfterAccess(Duration.of(10, ChronoUnit.SECONDS)).build();

    public ImageFrameClient(IEventBus modBus, ModContainer container) {
        MOD = this;
        LOGGER.info("Hello world from ImageFrame Client Reloaded!");

        Configuration.init(container);

        modBus.addListener(RegisterPayloadHandlersEvent.class, this::registerPayloads);

        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> {
            imageMapData.clear();
            for (int mapId : new IntOpenHashSet(loadedHdTextures.keySet())) {
                DynamicTexture tex = loadedHdTextures.remove(mapId);
                if (tex != null) {
                    tex.close();
                }
            }
            currentServerSupported.set(false);
        });
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(ServerboundAcknowledgement.TYPE, ServerboundAcknowledgement.STREAM_CODEC, (payload, context) -> {});
        registrar.playToServer(ServerboundHdImageRequest.TYPE, ServerboundHdImageRequest.STREAM_CODEC, (payload, context) -> {});
        registrar.playToServer(ServerboundImageMapDetailsRequest.TYPE, ServerboundImageMapDetailsRequest.STREAM_CODEC, (payload, context) -> {});

        registrar.playToClient(ClientboundAcknowledgement.TYPE, ClientboundAcknowledgement.STREAM_CODEC, (payload, context) -> {
            PacketDistributor.sendToServer(new ServerboundAcknowledgement(payload.id()));
            currentServerSupported.set(true);
            if (Configuration.NOTIFY_WHEN_SERVER_SUPPORTS.get()) {
                SystemToast.add(
                        Minecraft.getInstance().getToasts(),
                        SystemToast.SystemToastId.UNSECURE_SERVER_WARNING,
                        Component.translatable("imageframeclient.message.server_supported.title").withStyle(ChatFormatting.GOLD),
                        Component.translatable("imageframeclient.message.server_supported.description")
                );
            }
        });

        registrar.playToClient(ClientboundHdImageResponse.TYPE, ClientboundHdImageResponse.STREAM_CODEC, (payload, context) -> {
            if (!Configuration.USE_NATIVE_RES_MAP_IMAGES.get()) return;
            try {
                int mapId = payload.mapId();
                if (payload.requestAccepted()) {
                    Optional<Integer> opt = payload.multipart();
                    byte[] data = payload.data();
                    if (opt.isPresent()) {
                        MultipartHdMapInfo info = new MultipartHdMapInfo();
                        info.put(0, data);
                        pendingMultipart.put(opt.get(), info);
                    } else if (data.length > 0) {
                        registerHdTexture(mapId, NativeImage.read(data));
                    }
                } else {
                    DynamicTexture removed = loadedHdTextures.remove(mapId);
                    if (removed != null) removed.close();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to process HD image response", e);
            }
        });

        registrar.playToClient(ClientboundHdImageMultipartResponse.TYPE, ClientboundHdImageMultipartResponse.STREAM_CODEC, (payload, context) -> {
            if (!Configuration.USE_NATIVE_RES_MAP_IMAGES.get()) return;
            try {
                int mapId = payload.mapId();
                int multipartId = payload.multipart();
                MultipartHdMapInfo info = pendingMultipart.getIfPresent(multipartId);
                if (info != null) {
                    byte[] data = payload.data();
                    int index = payload.index();
                    if (data.length > 0) {
                        info.put(index, data);
                    }
                    if (payload.end()) {
                        info.setLastIndex(index);
                    }
                    if (info.isCompleted()) {
                        pendingMultipart.invalidate(multipartId);
                        registerHdTexture(mapId, NativeImage.read(info.complete()));
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Failed to process multipart HD image response", e);
            }
        });

        registrar.playToClient(ClientboundImageUpdatedSignal.TYPE, ClientboundImageUpdatedSignal.STREAM_CODEC, (payload, context) -> {
            for (int index : payload.indexes()) {
                imageMapData.remove(index);
            }
            for (int mapId : payload.mapIds()) {
                DynamicTexture tex = loadedHdTextures.remove(mapId);
                if (tex != null) tex.close();
            }
        });

        registrar.playToClient(ClientboundImageMapDetailsResponse.TYPE, ClientboundImageMapDetailsResponse.STREAM_CODEC, (payload, context) -> {
            if (payload.width() > 0 && payload.height() > 0) {
                imageMapData.put(payload.index(), Optional.of(new ImageMapData(payload.width(), payload.height(), payload.mapIds())));
            }
        });
    }

    private void registerHdTexture(int mapId, NativeImage image) {
        DynamicTexture existing = loadedHdTextures.remove(mapId);
        if (existing != null) existing.close();

        NativeImage resized = resizeToPreference(image);
        DynamicTexture tex = new DynamicTexture(resized);
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("imageframe", "hdmap_" + mapId);
        Minecraft.getInstance().getTextureManager().register(location, tex);
        loadedHdTextures.put(mapId, tex);
    }

    private NativeImage resizeToPreference(NativeImage src) {
        int size = Configuration.MAX_IMAGE_SIZE.get().getMaxSize();
        if (src.getWidth() <= size) {
            return src;
        }
        NativeImage dst = new NativeImage(size, size, false);
        src.resizeSubRectTo(0, 0, src.getWidth(), src.getHeight(), dst);
        return dst;
    }

    public DynamicTexture getHdTexture(int mapId) {
        return loadedHdTextures.get(mapId);
    }

    public ResourceLocation getHdTextureLocation(int mapId) {
        DynamicTexture tex = loadedHdTextures.get(mapId);
        return tex != null ? ResourceLocation.withDefaultNamespace("map/" + mapId) : null;
    }

    public void requestHdMap(int mapId) {
        if (!loadedHdTextures.containsKey(mapId) && currentServerSupported.get()) {
            PacketDistributor.sendToServer(new ServerboundHdImageRequest(mapId));
        }
    }

    public void clearLoadedHdMaps() {
        loadedHdTextures.clear();
    }

    public ImageMapData getOrRequestImageMapData(int index) {
        Optional<ImageMapData> result = imageMapData.get(index);
        if (result == null) {
            if (currentServerSupported.get()) {
                PacketDistributor.sendToServer(new ServerboundImageMapDetailsRequest(index));
                imageMapData.put(index, Optional.empty());
            }
            return null;
        }
        return result.orElse(null);
    }
}
