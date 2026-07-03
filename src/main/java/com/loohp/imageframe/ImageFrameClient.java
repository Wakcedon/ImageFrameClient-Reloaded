package com.loohp.imageframe;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.loohp.imageframe.configuration.Configuration;
import com.loohp.imageframe.configuration.ServerPerConfig;
import com.loohp.imageframe.handler.ClientPayloadHandler;
import com.loohp.imageframe.handler.ServerPayloadHandler;
import com.loohp.imageframe.object.AnimatedTexture;
import com.loohp.imageframe.object.ImageMapData;
import com.loohp.imageframe.object.MultipartHdMapInfo;
import com.loohp.imageframe.payload.*;
import com.loohp.imageframe.util.ImageCache;
import com.loohp.imageframe.util.ImageUtil;
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
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Mod("imageframeclient")
public class ImageFrameClient {

    public static final Logger LOGGER = LoggerFactory.getLogger("imageframeclient");
    public static ImageFrameClient MOD;

    private final AtomicBoolean currentServerSupported = new AtomicBoolean(false);
    private final Int2ObjectMap<DynamicTexture> loadedHdTextures = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<Optional<ImageMapData>> imageMapData = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<AnimatedTexture> animatedTextures = new Int2ObjectOpenHashMap<>();
    private final ConcurrentMap<Integer, byte[]> rawImageData = new ConcurrentHashMap<>();
    private final Cache<Integer, MultipartHdMapInfo> pendingMultipart = CacheBuilder.newBuilder()
            .expireAfterAccess(Duration.of(10, ChronoUnit.SECONDS)).build();

    private static Path gameDir;

    public static Path getGameDirectory() {
        if (gameDir == null) {
            if (FMLEnvironment.dist.isClient()) {
                gameDir = Minecraft.getInstance().gameDirectory.toPath();
            } else {
                gameDir = Path.of(".");
            }
        }
        return gameDir;
    }

    public ImageFrameClient(IEventBus modBus, ModContainer container) {
        MOD = this;
        LOGGER.info("Hello world from ImageFrame Client Reloaded!");

        Configuration.init(container);
        ServerPerConfig.init();

        modBus.addListener(RegisterPayloadHandlersEvent.class, this::registerPayloads);

        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingOut.class, event -> {
            imageMapData.clear();
            for (int mapId : new IntOpenHashSet(loadedHdTextures.keySet())) {
                DynamicTexture tex = loadedHdTextures.remove(mapId);
                if (tex != null) tex.close();
            }
            for (int mapId : new IntOpenHashSet(animatedTextures.keySet())) {
                AnimatedTexture ani = animatedTextures.remove(mapId);
                if (ani != null) ani.close();
            }
            rawImageData.clear();
            currentServerSupported.set(false);
        });

        NeoForge.EVENT_BUS.addListener(ClientPlayerNetworkEvent.LoggingIn.class, event -> {
            if (event.getConnection() != null) {
                String ip = event.getConnection().getRemoteAddress().toString();
                ServerPerConfig.setCurrentServer(ip);
            }
        });

        if (FMLEnvironment.dist.isClient()) {
            NeoForge.EVENT_BUS.addListener(RegisterClientCommandsEvent.class, this::registerClientCommands);
            NeoForge.EVENT_BUS.addListener(ClientTickEvent.Pre.class, this::onClientTick);
        }
    }

    private void onClientTick(ClientTickEvent.Pre event) {
        if (animatedTextures.isEmpty()) return;
        for (var entry : animatedTextures.int2ObjectEntrySet()) {
            int mapId = entry.getIntKey();
            DynamicTexture tex = loadedHdTextures.get(mapId);
            if (tex != null) {
                entry.getValue().tick(tex);
            }
        }
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        ServerPayloadHandler.register(event);

        if (FMLEnvironment.dist.isClient()) {
            ClientPayloadHandler.register(event);
        }

        var registrar = event.registrar("1");

        registrar.playToServer(ServerboundAcknowledgement.TYPE, ServerboundAcknowledgement.STREAM_CODEC,
                (payload, ctx) -> {});
        registrar.playToServer(ServerboundHdImageRequest.TYPE, ServerboundHdImageRequest.STREAM_CODEC,
                (payload, ctx) -> {});
        registrar.playToServer(ServerboundImageMapDetailsRequest.TYPE, ServerboundImageMapDetailsRequest.STREAM_CODEC,
                (payload, ctx) -> {});
    }

    private void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(net.minecraft.commands.Commands.literal("ifc").executes(ctx -> {
            Minecraft.getInstance().setScreen(new com.loohp.imageframe.gui.ImageManagerScreen());
            return 1;
        }));
    }

    public void onServerAcknowledged(ClientboundAcknowledgement payload) {
        PacketDistributor.sendToServer(new ServerboundAcknowledgement(payload.id()));
        currentServerSupported.set(true);
        if (Configuration.NOTIFY_WHEN_SERVER_SUPPORTS.get()) {
            SystemToast.add(
                    Minecraft.getInstance().getToasts(),
                    SystemToast.SystemToastId.UNSECURE_SERVER_WARNING,
                    Component.translatable("imageframeclient.message.server_supported.title")
                            .withStyle(ChatFormatting.GOLD),
                    Component.translatable("imageframeclient.message.server_supported.description")
            );
        }
    }

    public void onHdImageResponse(ClientboundHdImageResponse payload) {
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
                    rawImageData.put(mapId, data);
                    if (isGif(data)) {
                        registerAnimatedTexture(mapId, data);
                    } else {
                        NativeImage image = NativeImage.read(data);
                        registerHdTexture(mapId, image);
                    }
                }
            } else {
                DynamicTexture removed = loadedHdTextures.remove(mapId);
                if (removed != null) removed.close();
                AnimatedTexture aniRemoved = animatedTextures.remove(mapId);
                if (aniRemoved != null) aniRemoved.close();
                rawImageData.remove(mapId);
                ImageCache.removeTexture(mapId);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to process HD image response", e);
        }
    }

    public void onHdImageMultipart(ClientboundHdImageMultipartResponse payload) {
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
                    byte[] complete = info.complete();
                    rawImageData.put(mapId, complete);
                    if (isGif(complete)) {
                        registerAnimatedTexture(mapId, complete);
                    } else {
                        NativeImage image = NativeImage.read(complete);
                        registerHdTexture(mapId, image);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to process multipart HD image response", e);
        }
    }

    public void onImageUpdated(ClientboundImageUpdatedSignal payload) {
        for (int index : payload.indexes()) {
            imageMapData.remove(index);
        }
        for (int mapId : payload.mapIds()) {
            DynamicTexture tex = loadedHdTextures.remove(mapId);
            if (tex != null) tex.close();
            AnimatedTexture ani = animatedTextures.remove(mapId);
            if (ani != null) ani.close();
            rawImageData.remove(mapId);
            ImageCache.removeTexture(mapId);
        }
    }

    public void onImageMapDetails(ClientboundImageMapDetailsResponse payload) {
        if (payload.width() > 0 && payload.height() > 0) {
            imageMapData.put(payload.index(),
                    Optional.of(new ImageMapData(payload.width(), payload.height(), payload.mapIds())));
        }
    }

    private boolean isGif(byte[] data) {
        return data.length > 6
                && data[0] == (byte) 'G'
                && data[1] == (byte) 'I'
                && data[2] == (byte) 'F'
                && data[3] == (byte) '8';
    }

    private void registerAnimatedTexture(int mapId, byte[] data) throws IOException {
        AnimatedTexture existing = animatedTextures.remove(mapId);
        if (existing != null) existing.close();
        DynamicTexture oldTex = loadedHdTextures.remove(mapId);
        if (oldTex != null) oldTex.close();

        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
        if (!readers.hasNext()) {
            LOGGER.warn("No GIF reader available, using static fallback for map {}", mapId);
            NativeImage fallback = NativeImage.read(data);
            registerHdTexture(mapId, fallback);
            return;
        }
        ImageReader reader = readers.next();
        try (ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            reader.setInput(iis);
            int numFrames = reader.getNumImages(true);
            List<NativeImage> frames = new ArrayList<>(numFrames);
            int[] delays = new int[numFrames];
            for (int i = 0; i < numFrames; i++) {
                BufferedImage bi = reader.read(i);
                NativeImage frame = ImageUtil.fromBufferedImage(bi);
                NativeImage resized = ImageUtil.resizeBicubic(frame,
                        Configuration.MAX_IMAGE_SIZE.get().getMaxSize());
                frame.close();
                frames.add(resized);
                delays[i] = parseGifDelay(reader, i);
            }
            AnimatedTexture ani = new AnimatedTexture(frames, delays);
            animatedTextures.put(mapId, ani);

            NativeImage firstFrame = frames.getFirst();
            DynamicTexture tex = new DynamicTexture(firstFrame);
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath("imageframe", "hdmap_" + mapId);
            Minecraft.getInstance().getTextureManager().register(location, tex);
            loadedHdTextures.put(mapId, tex);
            ImageCache.saveTexture(mapId, firstFrame);
            LOGGER.info("Registered animated texture for map {} with {} frames", mapId, numFrames);
        } finally {
            reader.dispose();
        }
    }

    private int parseGifDelay(ImageReader reader, int frameIndex) {
        try {
            var meta = reader.getImageMetadata(frameIndex).getAsTree("javax_imageio_gif_image_extension");
            if (meta instanceof IIOMetadataNode node) {
                var child = node.getFirstChild();
                if (child != null && child.getAttributes() != null
                        && child.getAttributes().getNamedItem("delayTime") != null) {
                    return Math.max(1, Integer.parseInt(
                            child.getAttributes().getNamedItem("delayTime").getNodeValue()) * 10);
                }
            }
        } catch (Exception ignored) {}
        return 100;
    }

    private void registerHdTexture(int mapId, NativeImage image) {
        DynamicTexture existing = loadedHdTextures.remove(mapId);
        if (existing != null) existing.close();

        NativeImage resized = ImageUtil.resizeBicubic(image, Configuration.MAX_IMAGE_SIZE.get().getMaxSize());
        DynamicTexture tex = new DynamicTexture(resized);
        ResourceLocation location = ResourceLocation.fromNamespaceAndPath("imageframe", "hdmap_" + mapId);
        Minecraft.getInstance().getTextureManager().register(location, tex);
        loadedHdTextures.put(mapId, tex);
        ImageCache.saveTexture(mapId, resized);
    }

    public byte[] getRawImageData(int mapId) {
        byte[] data = rawImageData.get(mapId);
        if (data == null) {
            Path cached = ImageCache.getCacheDir().resolve("map_" + mapId + ".png");
            if (Files.isRegularFile(cached)) {
                try { return Files.readAllBytes(cached); } catch (IOException ignored) {}
            }
        }
        return data;
    }

    public DynamicTexture getHdTexture(int mapId) {
        DynamicTexture tex = loadedHdTextures.get(mapId);
        if (tex == null) {
            if (animatedTextures.containsKey(mapId)) return null;
            NativeImage cached = ImageCache.loadCachedTexture(mapId);
            if (cached != null) {
                tex = new DynamicTexture(cached);
                ResourceLocation loc = ResourceLocation.withDefaultNamespace("map/" + mapId);
                Minecraft.getInstance().getTextureManager().register(loc, tex);
                loadedHdTextures.put(mapId, tex);
            }
        }
        return tex;
    }

    public void requestHdMap(int mapId) {
        if (!loadedHdTextures.containsKey(mapId) && !animatedTextures.containsKey(mapId) && currentServerSupported.get()) {
            PacketDistributor.sendToServer(new ServerboundHdImageRequest(mapId));
        }
    }

    public void clearLoadedHdMaps() {
        loadedHdTextures.clear();
        for (var ani : animatedTextures.values()) ani.close();
        animatedTextures.clear();
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
