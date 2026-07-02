package com.loohp.imageframe.gui;

import com.loohp.imageframe.handler.ClientPayloadHandler;
import com.loohp.imageframe.payload.ImageInfo;
import com.loohp.imageframe.payload.ServerboundImageDelete;
import com.loohp.imageframe.payload.ServerboundImageListRequest;
import com.loohp.imageframe.payload.ServerboundImageUpload;
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

    private List<ImageInfo> images = List.of();
    private int selectedIndex = -1;
    private int scrollOffset = 0;
    private boolean loading = true;
    private String statusMessage = "";

    private Button uploadBtn;
    private Button deleteBtn;
    private Button refreshBtn;
    private EditBox widthInput;
    private EditBox heightInput;

    public ImageManagerScreen() {
        super(Component.translatable("imageframeclient.gui.title"));
    }

    @Override
    protected void init() {
        int listW = width * LIST_WIDTH_RATIO / 100;
        int detailW = width - listW;
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

        addRenderableWidget(Button.builder(
                Component.translatable("imageframeclient.gui.close"),
                btn -> onClose()
        ).bounds(width - 70, btnY, 60, 20).build());

        widthInput = addRenderableWidget(new EditBox(font, 250, btnY, 40, 20,
                Component.translatable("imageframeclient.gui.width")));
        widthInput.setValue("1");
        widthInput.setFilter(s -> s.matches("\\d*") && (!s.isEmpty() && Integer.parseInt(s) <= 256));

        heightInput = addRenderableWidget(new EditBox(font, 300, btnY, 40, 20,
                Component.translatable("imageframeclient.gui.height")));
        heightInput.setValue("1");
        heightInput.setFilter(s -> s.matches("\\d*") && (!s.isEmpty() && Integer.parseInt(s) <= 256));

        refreshList();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);

        int listW = width * LIST_WIDTH_RATIO / 100;
        int detailW = width - listW;

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
            renderImageDetail(graphics, listW + 10, 10, detailW - 20, mouseX, mouseY);
        }

        graphics.drawCenteredString(font, title, width / 2, 5, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("imageframeclient.gui.tiles_label"), 195, btnLabelY(), 0xAAAAAA);

        super.render(graphics, mouseX, mouseY, delta);
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
        int itemH = 16;
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
            int itemH = 16;
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
                    Math.max(0, images.size() - (height - 35 - 20) / 16)));
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
        PacketDistributor.sendToServer(new ServerboundImageListRequest());
    }

    private void deleteSelected() {
        if (selectedIndex < 0 || selectedIndex >= images.size()) return;
        String name = images.get(selectedIndex).name();
        PacketDistributor.sendToServer(new ServerboundImageDelete(name));
        refreshList();
    }

    private void openFileChooser() {
        CompletableFuture.runAsync(() -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select an image file");
            chooser.setFileFilter(new FileNameExtensionFilter("Images (PNG, JPG, JPEG)", "png", "jpg", "jpeg"));
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
            byte[] rawBytes = Files.readAllBytes(file.toPath());
            BufferedImage bi = ImageIO.read(file);
            if (bi == null) {
                setStatus("Unsupported image format");
                return;
            }
            NativeImage nativeImage = ImageUtil.fromBufferedImage(bi);

            Minecraft mc = Minecraft.getInstance();
            int tilesW = Math.max(1, Math.min(256, parseIntOrDefault(widthInput.getValue(), 1)));
            int tilesH = Math.max(1, Math.min(256, parseIntOrDefault(heightInput.getValue(), 1)));
            final int finalW = tilesW;
            final int finalH = tilesH;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "PNG", baos);
            byte[] pngBytes = baos.toByteArray();

            String name = file.getName().replaceAll("\\.(?i)(png|jpg|jpeg)$", "")
                    .replaceAll("[^a-zA-Z0-9_\\-]", "_");
            if (name.length() > 64) name = name.substring(0, 64);

            final String finalName = name;
            final byte[] finalPng = pngBytes;
            mc.tell(() -> {
                PacketDistributor.sendToServer(new ServerboundImageUpload(finalName, finalW, finalH, finalPng));
                refreshList();
                setStatus("Uploading '" + finalName + "'...");
            });

        } catch (IOException e) {
            LOGGER.error("Failed to read image file", e);
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
        List<ImageInfo> pending = ClientPayloadHandler.pendingImageList;
        if (pending != null) {
            images = pending;
            loading = false;
            statusMessage = "";
            selectedIndex = Math.min(selectedIndex, images.size() - 1);
            ClientPayloadHandler.pendingImageList = null;
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
