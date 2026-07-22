package com.arabaoyunu.mode;

/**
 * A65.7: Polis kovalamaca final AI dengeleme yardımcısı.
 *
 * Android bağımlılığı içermez. PoliceChaseMode bu sınıfı yalnızca takip hızı,
 * yakalanma baskısı, toparlama mesafeleri ve HUD durum metinleri için kullanır.
 * Amaç polis aracını oyuncuya yapıştırmadan baskı hissettirmek ve yakalanma
 * barını kısa temas yerine süreli/adaletli takip mantığına bağlamaktır.
 */
public final class PoliceAiController {

    private PoliceAiController() {}

    public static float desiredSpeed(float distanceMeters, float playerSpeedKmh, int wantedLevel, int unitIndex, float aggression) {
        int w = clamp(wantedLevel, 1, 5);
        float playerMps = Math.max(0f, playerSpeedKmh) / 3.6f;
        float base = 12.8f + w * 1.85f + unitIndex * 0.55f;
        float rubberBand = 0f;
        if (distanceMeters > 92f) rubberBand = 10.5f + w * 1.4f;
        else if (distanceMeters > 58f) rubberBand = 6.2f + w * 0.8f;
        else if (distanceMeters > 28f) rubberBand = 2.8f + w * 0.35f;

        float pressureBrake = 1f;
        if (distanceMeters < 7.2f) pressureBrake = 0.42f;
        else if (distanceMeters < 12.5f) pressureBrake = 0.66f;
        else if (distanceMeters < 18f) pressureBrake = 0.84f;

        float desired = Math.max(base, playerMps * (0.84f + w * 0.045f)) + rubberBand;
        desired *= Math.max(0.72f, aggression);
        desired *= PoliceChaseSystem.policeSpeedBonus(w);
        desired *= pressureBrake;
        return clamp(desired, 5.5f, 40.5f + w * 2.2f);
    }

    public static float turnRate(int wantedLevel, float distanceMeters, float aggression) {
        int w = clamp(wantedLevel, 1, 5);
        float closeAssist = distanceMeters < 18f ? 0.88f : 1f;
        return (1.10f + w * 0.12f) * Math.max(0.75f, aggression) * closeAssist;
    }

    public static float targetBackOffset(int wantedLevel, int unitIndex, float distanceMeters) {
        int w = clamp(wantedLevel, 1, 5);
        float base = 12.0f + unitIndex * 3.8f - w * 0.7f;
        if (distanceMeters < 18f) base += 4.5f;
        if (distanceMeters < 9f) base += 7.0f;
        return clamp(base, 6.5f, 24f);
    }

    public static float targetSideOffset(int unitIndex, float distanceMeters) {
        float side = (unitIndex - 1) * 7.5f;
        if (distanceMeters < 20f) side *= 1.55f;
        if (Math.abs(side) < 0.1f && distanceMeters < 10f) side = 4.25f;
        return side;
    }

    public static boolean shouldSoftRespawn(float distanceMeters, float disabledTimer, float unitSpeed) {
        return distanceMeters > 170f || disabledTimer > 9.5f || (distanceMeters > 125f && unitSpeed < 2.5f);
    }

    public static float respawnDistance(float playerSpeedKmh, int unitIndex) {
        return 74f + unitIndex * 15f + Math.min(28f, Math.max(0f, playerSpeedKmh) * 0.10f);
    }

    public static float captureGainPerSecond(float nearestMeters, float playerSpeedKmh, int wantedLevel, boolean recentCollision) {
        int w = clamp(wantedLevel, 1, 5);
        float caughtDistance = PoliceChaseSystem.caughtDistance(w);
        if (nearestMeters > caughtDistance + 2.5f && !recentCollision) return 0f;
        float closeness = 1f - clamp((nearestMeters - 2.2f) / Math.max(2.5f, caughtDistance), 0f, 1f);
        float speedEscape = playerSpeedKmh > 92f ? 0.62f : (playerSpeedKmh > 62f ? 0.78f : 1f);
        float base = (0.42f + w * 0.09f) * closeness * speedEscape;
        if (recentCollision) base += 0.22f + w * 0.035f;
        return clamp(base, 0f, 1.05f);
    }

    public static float captureRecoverPerSecond(float nearestMeters, float playerSpeedKmh, int wantedLevel) {
        int w = clamp(wantedLevel, 1, 5);
        float escapeDistance = PoliceChaseSystem.escapeDistance(w);
        float rate = 0.54f + Math.min(0.50f, Math.max(0f, playerSpeedKmh - 45f) / 130f);
        if (nearestMeters > escapeDistance * 0.88f) rate += 0.42f;
        if (nearestMeters > escapeDistance) rate += 0.28f;
        return clamp(rate, 0.42f, 1.65f);
    }

    public static String riskText(float nearestMeters, float capturePercent, int wantedLevel, boolean starUp) {
        if (starUp) return "YILDIZ SEVIYESI ARTTI";
        if (capturePercent >= 82f) return "YAKALANMA KRITIK";
        if (capturePercent >= 58f) return "POLIS BASKISI YUKSEK";
        if (nearestMeters < PoliceChaseSystem.caughtDistance(wantedLevel) + 2f) return "POLIS COK YAKIN";
        if (nearestMeters > PoliceChaseSystem.escapeDistance(wantedLevel) * 0.86f) return "GUVENLI MESAFE";
        return "KACIS FIRSATI ARA";
    }

    public static String aiBalanceLine(float nearestMeters, float capturePercent, int wantedLevel) {
        return PoliceChaseSystem.wantedStars(wantedLevel)
                + " Mesafe " + Math.max(0, Math.round(nearestMeters)) + "m"
                + " Risk %" + Math.max(0, Math.min(100, Math.round(capturePercent)));
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
