package com.loohp.imageframe.util;

import com.mojang.blaze3d.platform.NativeImage;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public final class ImageUtil {

    public static NativeImage resizeBicubic(NativeImage src, int maxSize) {
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= maxSize && h <= maxSize) return src;
        float scale = Math.min((float) maxSize / w, (float) maxSize / h);
        int newW = Math.round(w * scale);
        int newH = Math.round(h * scale);
        BufferedImage srcBi = toBufferedImage(src);
        BufferedImage dstBi = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dstBi.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(srcBi, 0, 0, newW, newH, null);
        g.dispose();
        return fromBufferedImage(dstBi);
    }

    public static byte[] nativeImageToPng(NativeImage img) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedImage bi = toBufferedImage(img);
        ImageIO.write(bi, "PNG", baos);
        return baos.toByteArray();
    }

    public static NativeImage pngToNativeImage(byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        BufferedImage bi = ImageIO.read(bais);
        if (bi == null) throw new IOException("Invalid PNG data");
        return fromBufferedImage(bi);
    }

    public static BufferedImage toBufferedImage(NativeImage nativeImage) {
        int w = nativeImage.getWidth();
        int h = nativeImage.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int abgr = nativeImage.getPixelRGBA(x, y);
                int a = (abgr >> 24) & 0xFF;
                int b = (abgr >> 16) & 0xFF;
                int g = (abgr >> 8) & 0xFF;
                int r = abgr & 0xFF;
                bi.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        return bi;
    }

    public static NativeImage fromBufferedImage(BufferedImage bi) {
        int w = bi.getWidth();
        int h = bi.getHeight();
        NativeImage ni = new NativeImage(w, h, true);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int argb = bi.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                ni.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }
        return ni;
    }

    private ImageUtil() {}
}
