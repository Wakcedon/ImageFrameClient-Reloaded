package com.loohp.imageframe.configuration;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "imageframeclient")
public class Configuration implements ConfigData {
    
    @ConfigEntry.Gui.Tooltip
    boolean useNativeResMapImages = true;

    @ConfigEntry.Gui.Tooltip
    boolean previewMapsInTooltip = true;

    @ConfigEntry.Gui.Tooltip
    boolean notifyWhenServerSupports = true;

    public boolean useNativeResMapImages() {
        return useNativeResMapImages;
    }

    public void setUseNativeResMapImages(boolean useNativeResMapImages) {
        this.useNativeResMapImages = useNativeResMapImages;
    }

    public boolean previewMapsInTooltip() {
        return previewMapsInTooltip;
    }

    public void setPreviewMapsInTooltip(boolean previewMapsInTooltip) {
        this.previewMapsInTooltip = previewMapsInTooltip;
    }

    public boolean doNotifyWhenServerSupports() {
        return notifyWhenServerSupports;
    }

    public void setNotifyWhenServerSupports(boolean notifyWhenServerSupports) {
        this.notifyWhenServerSupports = notifyWhenServerSupports;
    }
}