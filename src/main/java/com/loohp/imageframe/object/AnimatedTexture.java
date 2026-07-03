package com.loohp.imageframe.object;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.DynamicTexture;

import java.util.List;

public class AnimatedTexture {

    private final List<NativeImage> frames;
    private final int[] delays;
    private final int totalDuration;
    private int currentFrame;
    private long lastSwitch;

    public AnimatedTexture(List<NativeImage> frames, int[] delaysMs) {
        this.frames = frames;
        this.delays = delaysMs;
        this.totalDuration = sum(delaysMs);
        this.currentFrame = 0;
        this.lastSwitch = System.currentTimeMillis();
    }

    private static int sum(int[] arr) {
        int s = 0;
        for (int v : arr) s += v;
        return s;
    }

    public void tick(DynamicTexture tex) {
        if (frames.size() <= 1) return;
        long now = System.currentTimeMillis();
        int elapsed = (int) (now - lastSwitch);
        if (totalDuration > 0) {
            int looped = elapsed % totalDuration;
            int accum = 0;
            for (int i = 0; i < delays.length; i++) {
                accum += delays[i];
                if (looped < accum) {
                    if (i != currentFrame) {
                        currentFrame = i;
                        NativeImage frame = frames.get(i);
                        copyToTexture(tex, frame);
                    }
                    break;
                }
            }
        }
    }

    private void copyToTexture(DynamicTexture tex, NativeImage frame) {
        NativeImage current = tex.getPixels();
        if (current == null || frame == null) return;
        int w = Math.min(current.getWidth(), frame.getWidth());
        int h = Math.min(current.getHeight(), frame.getHeight());
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                current.setPixelRGBA(x, y, frame.getPixelRGBA(x, y));
            }
        }
        tex.upload();
    }

    public int getFrameCount() {
        return frames.size();
    }

    public void close() {
        for (NativeImage frame : frames) {
            frame.close();
        }
        frames.clear();
    }
}
