package com.arabaoyunu.util;

public final class PerformanceMonitor {

    private GraphicsQuality quality = GraphicsQuality.MEDIUM;
    private float lowFpsTimer;

    public GraphicsQuality getQuality() {
        return quality;
    }

    public void setQuality(GraphicsQuality quality) {
        if (quality != null) this.quality = quality;
    }

    public void update(float dt, int fps) {
        if (fps <= 0) return;
        if (fps < 28) lowFpsTimer += dt;
        else lowFpsTimer = Math.max(0f, lowFpsTimer - dt * 0.5f);

        if (lowFpsTimer > 4f) {
            autoLowerQuality();
            lowFpsTimer = 0f;
        }
    }

    private void autoLowerQuality() {
        if (quality == GraphicsQuality.ULTRA) quality = GraphicsQuality.HIGH;
        else if (quality == GraphicsQuality.HIGH) quality = GraphicsQuality.MEDIUM;
        else if (quality == GraphicsQuality.MEDIUM) quality = GraphicsQuality.LOW;
        GameLog.i("Performance", "Otomatik kalite ayari: " + quality.name());
    }

    public long getUsedMemoryMb() {
        Runtime r = Runtime.getRuntime();
        return (r.totalMemory() - r.freeMemory()) / (1024L * 1024L);
    }
}
