package com.loohp.imageframe.object;

public enum Resolution {

    NATIVE(Integer.MAX_VALUE),
    MAX_4096(4096),
    MAX_2048(2048),
    MAX_1024(1024),
    MAX_512(512),
    MAX_256(256),
    MAX_128(128);

    private final int maxSize;

    Resolution(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

}
