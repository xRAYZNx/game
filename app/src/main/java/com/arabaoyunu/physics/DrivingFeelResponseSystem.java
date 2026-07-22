package com.arabaoyunu.physics;

/**
 * A67.5: Mobil sürüş hissiyatı için ortak tepki eğrileri.
 * Fizik sistemi yeni mod/harita eklemeden; gaz, fren, direksiyon, el freni
 * ve nitro girişlerini daha okunaklı/profesyonel hale getirmek için bu
 * hafif yardımcı sınıfı kullanır.
 */
public final class DrivingFeelResponseSystem {
    private DrivingFeelResponseSystem() {}

    public static final int SENS_SMOOTH = 0;
    public static final int SENS_BALANCED = 1;
    public static final int SENS_SPORT = 2;
    public static final int SENS_DRIFT = 3;
    public static final int SENS_DRAG = 4;

    public static float throttleCurve(float raw, float speedKmh) {
        float v = clamp(raw, 0f, 1f);
        float progressive = v * v * (3f - 2f * v);
        float launchSupport = speedKmh < 18f ? 0.10f * (1f - speedKmh / 18f) : 0f;
        return clamp(progressive + v * launchSupport, 0f, 1f);
    }

    public static float brakeCurve(float raw, float speedKmh) {
        float v = clamp(raw, 0f, 1f);
        float bite = v < 0.55f ? v * 0.92f : 0.50f + (v - 0.55f) * 1.10f;
        if (speedKmh > 80f) bite = Math.max(bite, v * 0.96f);
        return clamp(bite, 0f, 1f);
    }

    public static float handbrakeCurve(float raw) {
        float v = clamp(raw, 0f, 1f);
        return clamp(v < 0.08f ? 0f : 0.18f + v * 0.82f, 0f, 1f);
    }

    public static float nitroCurve(float raw, float forwardSpeed, float maxForwardSpeed) {
        float v = clamp(raw, 0f, 1f);
        float speed01 = clamp(Math.abs(forwardSpeed) / Math.max(1f, maxForwardSpeed), 0f, 1f);
        // Düşük hızda patlama kontrollü, orta hızda daha canlı, son hızda yumuşak.
        float window = 0.72f + (1f - Math.abs(speed01 - 0.52f)) * 0.18f;
        return clamp(v * window, 0f, 1f);
    }

    public static float steerDeadZone(int sensitivityPreset) {
        if (sensitivityPreset == SENS_SPORT || sensitivityPreset == SENS_DRIFT) return 0.026f;
        if (sensitivityPreset == SENS_DRAG) return 0.045f;
        if (sensitivityPreset == SENS_SMOOTH) return 0.050f;
        return 0.036f;
    }

    public static float steerCurve(float raw, int sensitivityPreset) {
        float v = clamp(raw, -1f, 1f);
        float abs = Math.abs(v);
        float shaped;
        if (sensitivityPreset == SENS_DRIFT) shaped = (float)Math.pow(abs, 0.82f);
        else if (sensitivityPreset == SENS_SPORT) shaped = (float)Math.pow(abs, 0.90f);
        else if (sensitivityPreset == SENS_DRAG) shaped = abs * abs;
        else if (sensitivityPreset == SENS_SMOOTH) shaped = abs * abs * (3f - 2f * abs);
        else shaped = (float)Math.pow(abs, 1.08f);
        return v < 0f ? -shaped : shaped;
    }

    public static float steerRateMultiplier(int sensitivityPreset, float absForwardSpeed, float maxForwardSpeed) {
        float speed01 = clamp(absForwardSpeed / Math.max(1f, maxForwardSpeed), 0f, 1f);
        float base = 1.0f - speed01 * 0.12f;
        if (sensitivityPreset == SENS_SPORT) base += 0.12f;
        if (sensitivityPreset == SENS_DRIFT) base += 0.18f;
        if (sensitivityPreset == SENS_DRAG) base -= 0.14f;
        if (sensitivityPreset == SENS_SMOOTH) base -= 0.08f;
        return clamp(base, 0.72f, 1.24f);
    }

    public static float highSpeedStabilityMultiplier(int sensitivityPreset) {
        if (sensitivityPreset == SENS_DRIFT) return 0.72f;
        if (sensitivityPreset == SENS_SPORT) return 0.92f;
        if (sensitivityPreset == SENS_DRAG) return 1.18f;
        if (sensitivityPreset == SENS_SMOOTH) return 1.14f;
        return 1.0f;
    }

    public static String profileName(int sensitivityPreset) {
        if (sensitivityPreset == SENS_SMOOTH) return "YUMUŞAK";
        if (sensitivityPreset == SENS_SPORT) return "SPORT";
        if (sensitivityPreset == SENS_DRIFT) return "DRIFT";
        if (sensitivityPreset == SENS_DRAG) return "DRAG";
        return "DENGELİ";
    }

    public static String feelSummary(int sensitivityPreset) {
        if (sensitivityPreset == SENS_SMOOTH) return "Yumuşak direksiyon + stabil kamera";
        if (sensitivityPreset == SENS_SPORT) return "Sport direksiyon + hızlı gaz/fren";
        if (sensitivityPreset == SENS_DRIFT) return "Drift açısı + el freni hissi";
        if (sensitivityPreset == SENS_DRAG) return "Düz çizgi stabilitesi + kontrollü direksiyon";
        return "Dengeli mobil sürüş hissi";
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
