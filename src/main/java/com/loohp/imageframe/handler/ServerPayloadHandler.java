package com.loohp.imageframe.handler;

import com.loohp.imageframe.ImageFrameClient;
import com.loohp.imageframe.payload.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class ServerPayloadHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("imageframe-server");
    private static final String IMAGES_DIR = "imageframeclient" + java.io.File.separator + "images";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToServer(ServerboundImageListRequest.TYPE, ServerboundImageListRequest.STREAM_CODEC,
                ServerPayloadHandler::handleImageList);

        registrar.playToServer(ServerboundImageUpload.TYPE, ServerboundImageUpload.STREAM_CODEC,
                ServerPayloadHandler::handleImageUpload);

        registrar.playToServer(ServerboundImageDelete.TYPE, ServerboundImageDelete.STREAM_CODEC,
                ServerPayloadHandler::handleImageDelete);
    }

    private static Path getImagesDir() {
        return Path.of(IMAGES_DIR);
    }

    private static Path getImageFrameDir() {
        Path ifDir = Path.of("plugins", "ImageFrame", "images");
        return Files.isDirectory(ifDir) ? ifDir : null;
    }

    private static List<ImageInfo> listImages() {
        List<Path> dirs = new ArrayList<>();
        Path ourDir = getImagesDir();
        if (Files.isDirectory(ourDir)) dirs.add(ourDir);
        Path ifDir = getImageFrameDir();
        if (ifDir != null && !ifDir.equals(ourDir)) dirs.add(ifDir);

        List<ImageInfo> result = new ArrayList<>();
        for (Path dir : dirs) {
            try (Stream<Path> files = Files.list(dir)) {
                files.filter(p -> p.getFileName().toString().endsWith(".png"))
                     .forEach(p -> {
                         try {
                             String name = p.getFileName().toString().replaceAll("\\.png$", "");
                             long size = Files.size(p);
                             result.add(new ImageInfo(name, -1, -1, size));
                         } catch (IOException e) {
                             LOGGER.warn("Failed to read image file {}", p, e);
                         }
                     });
            } catch (IOException e) {
                LOGGER.warn("Failed to list images in {}", dir, e);
            }
        }
        return result;
    }

    private static void handleImageList(ServerboundImageListRequest payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            List<ImageInfo> images = listImages();
            ctx.reply(new ClientboundImageListResponse(List.copyOf(images)));
        });
    }

    private static void handleImageUpload(ServerboundImageUpload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            String name = sanitizeName(payload.name());
            if (name.isEmpty()) {
                ctx.reply(new ClientboundImageUploadAck(false, "Invalid image name"));
                return;
            }
            if (payload.width() <= 0 || payload.width() > 256) {
                ctx.reply(new ClientboundImageUploadAck(false, "Width must be 1-256"));
                return;
            }
            if (payload.height() <= 0 || payload.height() > 256) {
                ctx.reply(new ClientboundImageUploadAck(false, "Height must be 1-256"));
                return;
            }
            try {
                Files.createDirectories(getImagesDir());
                Path target = getImagesDir().resolve(name + ".png");
                if (Files.exists(target)) {
                    ctx.reply(new ClientboundImageUploadAck(false, "An image with that name already exists"));
                    return;
                }
                Files.write(target, payload.pngData());

                Path ifDir = getImageFrameDir();
                if (ifDir != null) {
                    Files.createDirectories(ifDir);
                    Files.copy(target, ifDir.resolve(name + ".png"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                }

                ctx.reply(new ClientboundImageUploadAck(true, "Image '" + name + "' uploaded successfully"));
                LOGGER.info("Image '{}' uploaded ({}x{} tiles, {} bytes)", name, payload.width(), payload.height(), payload.pngData().length);
            } catch (IOException e) {
                LOGGER.error("Failed to upload image '{}'", name, e);
                ctx.reply(new ClientboundImageUploadAck(false, "Failed to save image: " + e.getMessage()));
            }
        });
    }

    private static void handleImageDelete(ServerboundImageDelete payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            String name = sanitizeName(payload.name());
            if (name.isEmpty()) {
                ctx.reply(new ClientboundImageDeleteAck(false, "Invalid image name"));
                return;
            }
            boolean deleted = false;
            try {
                Path ourFile = getImagesDir().resolve(name + ".png");
                if (Files.deleteIfExists(ourFile)) deleted = true;

                Path ifDir = getImageFrameDir();
                if (ifDir != null) {
                    Path ifFile = ifDir.resolve(name + ".png");
                    if (Files.deleteIfExists(ifFile)) deleted = true;
                }

                if (deleted) {
                    ctx.reply(new ClientboundImageDeleteAck(true, "Image '" + name + "' deleted"));
                    LOGGER.info("Image '{}' deleted", name);
                } else {
                    ctx.reply(new ClientboundImageDeleteAck(false, "Image '" + name + "' not found"));
                }
            } catch (IOException e) {
                LOGGER.error("Failed to delete image '{}'", name, e);
                ctx.reply(new ClientboundImageDeleteAck(false, "Failed to delete: " + e.getMessage()));
            }
        });
    }

    private static String sanitizeName(String name) {
        String s = name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return s.length() > 64 ? s.substring(0, 64) : s;
    }

    private ServerPayloadHandler() {}
}
