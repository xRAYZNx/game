package com.arabaoyunu.util;

public enum GraphicsQuality {
    LOW(0.65f, false, false, 35),
    MEDIUM(0.85f, false, true, 50),
    HIGH(1.0f, true, true, 60),
    ULTRA(1.0f, true, true, 60);

    public final float renderScale;
    public final boolean shadowsEnabled;
    public final boolean effectsEnabled;
    public final int targetFps;

    GraphicsQuality(float renderScale, boolean shadowsEnabled, boolean effectsEnabled, int targetFps) {
        this.renderScale = renderScale;
        this.shadowsEnabled = shadowsEnabled;
        this.effectsEnabled = effectsEnabled;
        this.targetFps = targetFps;
    }
}
