package com.loohp.imageframe.gui;

import com.loohp.imageframe.handler.ClientPayloadHandler;
import com.loohp.imageframe.payload.ImageInfo;
import com.loohp.imageframe.payload.ServerboundImageDelete;
import com.loohp.imageframe.payload.ServerboundImageListRequest;
import com.loohp.imageframe.payload.ServerboundImageUpload;
import com.loohp.imageframe.util.ImageCache;
import com.loohp.imageframe.util.ImageUtil;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ImageManagerScreen extends Screen {

    private static final Logger LOGGER = LoggerFactory.getLogger("imageframe-gui");
    private static final int LIST_WIDTH_RATIO = 40;
    private static final long TIMEOUT_MS = 5000;

    private List<ImageInfo> images = List.of();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private boolean loading = true;
    private String statusMessage = "";
    private long lastRequestTime;

    private Button uploadBtn;
    private Button deleteBtn;
    private Button refreshBtn;
    private Button clearCacheBtn;
    private EditBox widthInput;
    private EditBox heightInput;

    private BufferedImage pendingImage;
    private String pendingFileName;
    private int rotation;
    private boolean flipH;
    private boolean flipV;

    public ImageManagerScreen() {
        super(Component.translatable("imageframeclient.gui.title"));
    }

    @Override
    protected void init() {
        int listW = width * LIST_WIDTH_RATIO / 100;
        int btnY = height - 30;

        uploadBtn = addRenderableWidget(Button.builder(
                Component.translatable("imageframeclient.gui.upload"),
                btn -> openFileChooser()
        ).bounds(10, btnY, 70, 20).build());

        deleteBtn = addRenderableWidget(Button.builder(
                Component.translatable("imageframeclient.gui.delete"),
                btn -> deleteSelected()
        ).bounds(85, btnY, 60, 20).build());

        refreshBtn = addRenderableWidget(Button.builder(
                Component.translatable("imageframeclient.gui.refresh"),
                btn -> refreshList()
        ).bounds(150, btnY, 60, 20).build());

        clearCacheBtn = addRenderableWidget(Button.builder(
                Component.literal("Clear Cache"),
                btn -> {
                    ImageCache.clearCache();
                    setStatus("Cache cleared");
                }
        ).bounds(215, btnY, 70, 20).build());

        addRenderableWidget(Button.builder(
                Component.translatable("imageframeclient.gui.close"),
                btn -> onClose()
        ).bounds(width - 70, btnY, 60, 20).build());

        int editX = 390;
        addRenderableWidget(Button.builder(
                Component.literal("R"),
                btn -> rotate90()
        ).bounds(editX, btnY, 20, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("H"),
                btn -> { flipH = !flipH; setStatus(flipH ? "Flipped H" : "Flip H off"); }
        ).bounds(editX + 22, btnY, 20, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("V"),
                btn -> { flipV = !flipV; setStatus(flipV ? "Flipped V" : "Flip V off"); }
        ).bounds(editX + 44, btnY, 20, 20).build());

        addRenderableWidget(Button.builder(
                Component.literal("X"),
                btn -> clearTransform()
        ).bounds(editX + 66, btnY, 20, 20).build());

        widthInput = addRenderableWidget(new EditBox(font, 250, btnY, 40, 20,
                Component.translatable("imageframeclient.gui.width")));
        widthInput.setValue("1");
        widthInput.setFilter(s -> s.matches("\\d*") && (!s.isEmpty() && Integer.parseInt(s) <= 256));

        heightInput = addRenderableWidget(new EditBox(font, 340, btnY, 40, 20,
                Component.translatable("imageframeclient.gui.height")));
        heightInput.setValue("1");
        heightInput.setFilter(s -> s.matches("\\d*") && (!s.isEmpty() && Integer.parseInt(s) <= 256));

        refreshList();
    }

    private void rotate90() {
        if (pendingImage != null) {
            rotation = (rotation + 90) % 360;
            setStatus("Rotation: " + rotation + "\u00B0");
        }
    }

    private void clearTransform() {
        rotation = 0;
        flipH = false;
        flipV = false;
        setStatus("Transform reset");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);

        int listW = width * LIST_WIDTH_RATIO / 100;

        graphics.fill(0, 0, listW, height - 35, 0x44000000);

        if (loading) {
            graphics.drawCenteredString(font, Component.translatable("imageframeclient.gui.loading"),
                    listW / 2, height / 2 - 20, 0xFFFFFF);
        } else if (!statusMessage.isEmpty()) {
            graphics.drawCenteredString(font, Component.literal(statusMessage),
                    listW / 2, height / 2 - 20, 0xFFFF55);
        } else {
            renderImageList(graphics, listW, mouseX, mouseY);
        }

        if (selectedIndex >= 0 && selectedIndex < images.size()) {
            renderImageDetail(graphics, listW + 10, 10, width - listW - 20, mouseX, mouseY);
        }

        graphics.drawCenteredString(font, title, width / 2, 5, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("imageframeclient.gui.tiles_label"), 235, btnLabelY(), 0xAAAAAA);

        if (pendingImage != null) {
            int previewX = listW + 10;
            int previewY = 110;
            int previewSize = Math.min(width - listW - 30, 128);
            renderImagePreview(graphics, pendingImage, previewX, previewY, previewSize, mouseX, mouseY);
            graphics.drawString(font, Component.literal("Preview: " + pendingFileName),
                    previewX, previewY - 10, 0xAAAAAA);
        }

        super.render(graphics, mouseX, mouseY, delta);
    }

    private void renderImagePreview(GuiGraphics graphics, BufferedImage image, int x, int y, int maxSize, int mx, int my) {
        BufferedImage transformed = applyTransform(image);
        int w = transformed.getWidth();
        int h = transformed.getHeight();
        float scale = Math.min((float) maxSize / w, (float) maxSize / h);
        int sw = Math.round(w * scale);
        int sh = Math.round(h * scale);
        graphics.fill(x - 1, y - 1, x + maxSize + 1, y + maxSize + 1, 0xFF333333);
        int px = x + (maxSize - sw) / 2;
        int py = y + (maxSize - sh) / 2;
        graphics.fill(px, py, px + sw, py + sh, 0xFFFFFFFF);
        String dim = transformed.getWidth() + "x" + transformed.getHeight();
        graphics.drawCenteredString(font, Component.literal(dim), px + sw / 2, py + sh / 2 - 4, 0xFF555555);
    }

    private BufferedImage applyTransform(BufferedImage src) {
        if (src == null) return null;
        BufferedImage img = src;
        if (flipH || flipV || rotation != 0) {
            int w = img.getWidth();
            int h = img.getHeight();
            boolean rotated = (rotation / 90) % 2 == 1;
            int outW = rotated ? h : w;
            int outH = rotated ? w : h;
            BufferedImage out = new BufferedImage(outW, outH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = out.createGraphics();
            AffineTransform tx = new AffineTransform();
            tx.translate(outW / 2.0, outH / 2.0);
            tx.rotate(Math.toRadians(rotation));
            if (flipH) tx.scale(-1, 1);
            if (flipV) tx.scale(1, -1);
            tx.translate(-w / 2.0, -h / 2.0);
            g.drawImage(img, tx, null);
            g.dispose();
            img = out;
        }
        return img;
    }

    private int btnLabelY() {
        return height - 30 - 12;
    }

    private void renderImageList(GuiGraphics graphics, int listW, int mouseX, int mouseY) {
        if (images.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("imageframeclient.gui.no_images"),
                    listW / 2, height / 2 - 20, 0x888888);
            return;
        }

        int y = 20;
        int itemH = 20;
        for (int i = scrollOffset; i < images.size() && y + itemH < height - 35; i++) {
            ImageInfo info = images.get(i);
            boolean hovered = mouseX >= 0 && mouseX < listW && mouseY >= y && mouseY < y + itemH;
            boolean selected = i == selectedIndex;

            int bgColor = selected ? 0x660000FF : (hovered ? 0x33FFFFFF : 0x00000000);
            if (bgColor != 0) graphics.fill(2, y, listW - 2, y + itemH, bgColor);

            String name = info.name();
            String sizeStr = formatSize(info.fileSize());
            graphics.drawString(font, name, 5, y + 3, selected ? 0xFFFFAA : 0xFFFFFF);
            graphics.drawString(font, sizeStr, listW - font.width(sizeStr) - 5, y + 3, 0x888888);

            y += itemH;
        }
    }

    private void renderImageDetail(GuiGraphics graphics, int x, int y, int w, int mouseX, int mouseY) {
        ImageInfo info = images.get(selectedIndex);
        graphics.drawString(font, Component.literal(info.name()), x, y, 0xFFFFAA);
        y += 12;
        graphics.drawString(font, Component.translatable("imageframeclient.gui.file_size",
                formatSize(info.fileSize())), x, y, 0xCCCCCC);
        y += 12;
        if (info.width() > 0) {
            graphics.drawString(font, Component.translatable("imageframeclient.gui.dimensions",
                    info.width(), info.height()), x, y, 0xCCCCCC);
            y += 12;
        }
        graphics.drawString(font, Component.translatable("imageframeclient.gui.upload_hint"), x, y, 0x666666);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int listW = width * LIST_WIDTH_RATIO / 100;
            int y = 20;
            int itemH = 20;
            for (int i = scrollOffset; i < images.size() && y + itemH < height - 35; i++) {
                if (mouseX >= 0 && mouseX < listW && mouseY >= y && mouseY < y + itemH) {
                    selectedIndex = i;
                    return true;
                }
                y += itemH;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0) {
            scrollOffset = Math.max(0, Math.min(scrollOffset - (int) Math.signum(scrollY),
                    Math.max(0, images.size() - (height - 35 - 20) / 20)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void refreshList() {
        loading = true;
        statusMessage = "";
        selectedIndex = -1;
        images = List.of();
        ClientPayloadHandler.pendingImageList = null;
        lastRequestTime = System.currentTimeMillis();
        try {
            PacketDistributor.sendToServer(new ServerboundImageListRequest());
        } catch (Exception e) {
            loading = false;
            statusMessage = "Server does not support image management";
        }
    }

    private void deleteSelected() {
        if (selectedIndex < 0 || selectedIndex >= images.size()) return;
        String name = images.get(selectedIndex).name();
        try {
            PacketDistributor.sendToServer(new ServerboundImageDelete(name));
            refreshList();
        } catch (Exception e) {
            setStatus("Server does not support image management");
        }
    }

    private void openFileChooser() {
        CompletableFuture.runAsync(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select an image file");
            chooser.setFileFilter(new FileNameExtensionFilter("Images (PNG, JPG, JPEG, GIF)", "png", "jpg", "jpeg", "gif"));
            chooser.setAcceptAllFileFilterUsed(false);
            int result = chooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                processSelectedFile(file);
            }
        });
    }

    private void processSelectedFile(File file) {
        try {
            BufferedImage bi = ImageIO.read(file);
            if (bi == null) {
                setStatus("Unsupported image format");
                return;
            }
            pendingImage = bi;
            pendingFileName = file.getName();
            rotation = 0;
            flipH = false;
            flipV = false;
            setStatus("Loaded: " + pendingFileName + " (" + bi.getWidth() + "x" + bi.getHeight() + ")");
        } catch (IOException e) {
            LOGGER.error("Failed to read image file", e);
            setStatus("Error: " + e.getMessage());
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (pendingImage != null && keyCode == 257) {
            doUpload();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void doUpload() {
        if (pendingImage == null) return;
        try {
            BufferedImage transformed = applyTransform(pendingImage);

            int tilesW = Math.max(1, Math.min(256, parseIntOrDefault(widthInput.getValue(), 1)));
            int tilesH = Math.max(1, Math.min(256, parseIntOrDefault(heightInput.getValue(), 1)));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(transformed, "PNG", baos);
            byte[] pngBytes = baos.toByteArray();

            String name = pendingFileName.replaceAll("\\.(?i)(png|jpg|jpeg|gif)$", "")
                    .replaceAll("[^a-zA-Z0-9_\\-]", "_");
            if (name.length() > 64) name = name.substring(0, 64);

            final String finalName = name;
            Minecraft mc = Minecraft.getInstance();
            mc.tell(() -> {
                try {
                    PacketDistributor.sendToServer(new ServerboundImageUpload(finalName, tilesW, tilesH, pngBytes));
                    refreshList();
                    setStatus("Uploading '" + finalName + "'...");
                } catch (Exception e) {
                    setStatus("Server does not support image management");
                }
            });

            pendingImage = null;
            pendingFileName = null;
            rotation = 0;
            flipH = false;
            flipV = false;

        } catch (IOException e) {
            LOGGER.error("Failed to upload image", e);
            setStatus("Error: " + e.getMessage());
        }
    }

    private void setStatus(String msg) {
        Minecraft.getInstance().tell(() -> {
            statusMessage = msg;
        });
    }

    @Override
    public void tick() {
        super.tick();
        if (loading && lastRequestTime > 0 && System.currentTimeMillis() - lastRequestTime > TIMEOUT_MS) {
            loading = false;
            statusMessage = "Server did not respond. Is ImageFrame installed?";
            lastRequestTime = 0;
        }
        List<ImageInfo> pending = ClientPayloadHandler.pendingImageList;
        if (pending != null) {
            images = pending;
            loading = false;
            statusMessage = "";
            selectedIndex = Math.min(selectedIndex, images.size() - 1);
            ClientPayloadHandler.pendingImageList = null;
            lastRequestTime = 0;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }

    private static int parseIntOrDefault(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
