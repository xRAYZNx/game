package com.arabaoyunu.ai;

/**
 * ArabaOyunu_34: AI ve performans ayarları.
 *
 * Amaç:
 * - Trafik/polis/bot sistemlerini düşük cihazlarda daha hafif çalıştırmak.
 * - Grafik kalitesi düştükçe aktif AI ve çizim mesafesini azaltmak.
 * - Tablet/telefonlarda FPS düşüşünü sınırlamak.
 */
public final class AiPerformanceTuner {

    public static final int QUALITY_LOW = 0;
    public static final int QUALITY_MEDIUM = 1;
    public static final int QUALITY_HIGH = 2;
    public static final int QUALITY_ULTRA = 3;

    private AiPerformanceTuner() {}

    public static int clampQuality(int quality) {
        if (quality < QUALITY_LOW) return QUALITY_LOW;
        if (quality > QUALITY_ULTRA) return QUALITY_ULTRA;
        return quality;
    }

    public static int maxTrafficCount(int density, boolean night, int graphicsQuality) {
        if (density <= 0) return 0;
        int q = clampQuality(graphicsQuality);
        int base;
        if (density == 1) base = 7;
        else if (density == 3) base = 22;
        else base = 14;

        float qualityScale;
        if (q == QUALITY_LOW) qualityScale = 0.45f;
        else if (q == QUALITY_MEDIUM) qualityScale = 0.68f;
        else if (q == QUALITY_ULTRA) qualityScale = 1.12f;
        else qualityScale = 1.0f;

        if (night) qualityScale *= 0.70f;
        return Math.max(0, Math.min(24, Math.round(base * qualityScale)));
    }

    public static float aiRenderDistance(int graphicsQuality) {
        int q = clampQuality(graphicsQuality);
        if (q == QUALITY_LOW) return 135f;
        if (q == QUALITY_MEDIUM) return 185f;
        if (q == QUALITY_ULTRA) return 310f;
        return 245f;
    }

    public static float farAiUpdateScale(int graphicsQuality) {
        int q = clampQuality(graphicsQuality);
        if (q == QUALITY_LOW) return 0.34f;
        if (q == QUALITY_MEDIUM) return 0.52f;
        if (q == QUALITY_ULTRA) return 1.0f;
        return 0.74f;
    }

    public static float policeAggression(int wantedLevel, int graphicsQuality) {
        float base = 1.0f + Math.max(0, wantedLevel - 1) * 0.10f;
        int q = clampQuality(graphicsQuality);
        if (q == QUALITY_LOW) return base * 0.92f;
        if (q == QUALITY_ULTRA) return base * 1.06f;
        return base;
    }

    public static float botCatchupFactor(int playerPosition, int graphicsQuality) {
        int q = clampQuality(graphicsQuality);
        float factor = playerPosition <= 1 ? 1.03f : playerPosition == 2 ? 1.00f : 0.96f;
        if (q == QUALITY_LOW) factor *= 0.98f;
        return factor;
    }
}
