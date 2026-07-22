package com.arabaoyunu.vehicle;

/**
 * ArabaOyunu_19: Her arac icin ayri render/yukseklik/yon/kalite ayari.
 */
public final class VehicleRenderConfig {
    public final float yawOffsetDeg;
    public final float targetLengthMeters;
    public final float yOffset;
    public final float paintBoost;
    public final float glassBoost;
    public final float emissiveBoost;
    public final String wheelPattern;
    public final String lightPattern;

    public VehicleRenderConfig(
            float yawOffsetDeg,
            float targetLengthMeters,
            float yOffset,
            float paintBoost,
            float glassBoost,
            float emissiveBoost,
            String wheelPattern,
            String lightPattern
    ) {
        this.yawOffsetDeg = yawOffsetDeg;
        this.targetLengthMeters = targetLengthMeters;
        this.yOffset = yOffset;
        this.paintBoost = paintBoost;
        this.glassBoost = glassBoost;
        this.emissiveBoost = emissiveBoost;
        this.wheelPattern = wheelPattern == null ? "" : wheelPattern;
        this.lightPattern = lightPattern == null ? "" : lightPattern;
    }
}
