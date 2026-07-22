package com.arabaoyunu.weather;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;

/**
 * ArabaOyunu_37: Hava durumu + gece/gündüz sistemi.
 *
 * - Gündüz / gece döngüsü
 * - Yağmur
 * - Sis
 * - Islak yol efekti
 * - Farların gece daha önemli olması
 * - Yağmurda yol tutuş azalması için grip scale
 */
public final class WeatherSystem {

    public static final int WEATHER_CLEAR = 0;
    public static final int WEATHER_RAIN = 1;
    public static final int WEATHER_FOG = 2;

    private static final float FULL_DAY_SECONDS = 210f;

    private float timeOfDay = 0.28f; // sabah
    private float weatherClock;
    private float rainAmount;
    private float fogAmount;
    private float wetness;
    private int weatherMode = WEATHER_CLEAR;
    private boolean forceNightRace;

    public void update(float dt, InputState input, boolean raceActive, String mapName) {
        if (dt <= 0f) return;
        if (dt > 0.085f) dt = 0.085f;

        weatherClock += dt;
        timeOfDay += dt / FULL_DAY_SECONDS;
        if (timeOfDay > 1f) timeOfDay -= 1f;

        // Yarış modlarında arada gece yarışı üretir; bu otomatik döngüye bağlıdır.
        forceNightRace = raceActive && ((int)(weatherClock / 48f) % 3 == 1);
        if (forceNightRace && !isNight()) {
            timeOfDay = 0.78f;
        }

        int phase = ((int)(weatherClock / 42f)) % 4;
        int targetWeather;
        if (phase == 1) targetWeather = WEATHER_RAIN;
        else if (phase == 2) targetWeather = WEATHER_FOG;
        else if (phase == 3 && isNight()) targetWeather = WEATHER_RAIN;
        else targetWeather = WEATHER_CLEAR;
        weatherMode = targetWeather;

        float targetRain = weatherMode == WEATHER_RAIN ? 1f : 0f;
        float targetFog = weatherMode == WEATHER_FOG ? 0.88f : (isNight() ? 0.14f : 0.0f);
        rainAmount = moveTowards(rainAmount, targetRain, dt * 0.33f);
        fogAmount = moveTowards(fogAmount, targetFog, dt * 0.28f);

        float targetWet = rainAmount > 0.05f ? rainAmount : 0f;
        if (targetWet > wetness) wetness = moveTowards(wetness, targetWet, dt * 0.26f);
        else wetness = moveTowards(wetness, 0f, dt * 0.055f);
    }

    public void render(PrimitiveRenderer renderer, float[] vp, RenderStats stats, float playerX, float playerZ, String mapName, int graphicsQuality) {
        if (renderer == null || vp == null) return;
        renderAtmosphereTint(renderer, vp, stats, playerX, playerZ);
        if (wetness > 0.04f) renderWetRoads(renderer, vp, stats, playerX, playerZ, mapName);
        if (fogAmount > 0.05f) renderFog(renderer, vp, stats, playerX, playerZ, graphicsQuality);
        if (rainAmount > 0.05f) renderRain(renderer, vp, stats, playerX, playerZ, graphicsQuality);
    }

    private void renderAtmosphereTint(PrimitiveRenderer r, float[] vp, RenderStats stats, float x, float z) {
        // Çok hafif yerde renk/atmosfer katmanı: gece/sis/yağmur hissi.
        float darkness = getNight01() * 0.18f + fogAmount * 0.08f + rainAmount * 0.06f;
        if (darkness <= 0.02f) return;
        r.drawFlatRect(vp, x, 0.052f, z, 320f, 320f,
                0.03f + fogAmount * 0.05f,
                0.04f + rainAmount * 0.05f,
                0.06f + getNight01() * 0.10f,
                Math.min(0.28f, darkness), stats);
    }

    private void renderWetRoads(PrimitiveRenderer r, float[] vp, RenderStats stats, float playerX, float playerZ, String mapName) {
        float alpha = 0.18f + wetness * 0.20f;
        String name = mapName == null ? "" : mapName;
        if (name.indexOf("OTOYOL") >= 0) {
            r.drawFlatRect(vp, 0f, 0.062f, playerZ, 72f, 260f, 0.10f, 0.17f, 0.22f, alpha, stats);
            r.drawFlatRect(vp, -44f, 0.064f, playerZ, 24f, 260f, 0.09f, 0.15f, 0.20f, alpha * 0.72f, stats);
            r.drawFlatRect(vp, 44f, 0.064f, playerZ, 24f, 260f, 0.09f, 0.15f, 0.20f, alpha * 0.72f, stats);
        } else if (name.indexOf("BUYUK") >= 0 || name.indexOf("SEHIR") >= 0) {
            for (float x = -120f; x <= 120f; x += 80f) {
                r.drawFlatRect(vp, x, 0.062f, playerZ, 24f, 260f, 0.10f, 0.17f, 0.22f, alpha, stats);
            }
            for (float z = -120f; z <= 120f; z += 80f) {
                r.drawFlatRect(vp, playerX, 0.064f, z, 260f, 24f, 0.10f, 0.17f, 0.22f, alpha, stats);
            }
        } else {
            r.drawFlatRect(vp, 0f, 0.062f, 0f, 42f, 300f, 0.10f, 0.17f, 0.22f, alpha, stats);
            r.drawFlatRect(vp, 0f, 0.064f, 0f, 300f, 42f, 0.10f, 0.17f, 0.22f, alpha * 0.85f, stats);
        }
    }

    private void renderFog(PrimitiveRenderer r, float[] vp, RenderStats stats, float playerX, float playerZ, int graphicsQuality) {
        int rings = graphicsQuality <= 0 ? 2 : graphicsQuality >= 3 ? 5 : 3;
        for (int i = 0; i < rings; i++) {
            float radius = 58f + i * 46f;
            float alpha = fogAmount * (0.18f - i * 0.020f);
            r.drawCircle(vp, playerX, 0.20f + i * 0.05f, playerZ, radius, stats);
            // Çemberler PrimitiveRenderer içinde mavi çiziliyor; sis için hafif marker olarak yeterli.
        }
    }

    private void renderRain(PrimitiveRenderer r, float[] vp, RenderStats stats, float playerX, float playerZ, int graphicsQuality) {
        int count = graphicsQuality <= 0 ? 22 : graphicsQuality >= 3 ? 62 : 40;
        float base = weatherClock * 31f;
        for (int i = 0; i < count; i++) {
            float ox = pseudo(i * 17 + 3, base) * 130f - 65f;
            float oz = pseudo(i * 31 + 9, base * 0.73f) * 130f - 65f;
            float y = 7.0f + pseudo(i * 11 + 5, base * 1.21f) * 8.0f;
            float x = playerX + ox;
            float z = playerZ + oz;
            r.drawLine(vp, x, y, z, x - 0.55f, y - 2.2f, z + 0.42f, 0.56f, 0.72f, 0.92f, stats);
        }
    }

    private static float pseudo(float a, float b) {
        double v = Math.sin(a * 12.9898 + b * 78.233) * 43758.5453;
        return (float)(v - Math.floor(v));
    }

    private static float moveTowards(float current, float target, float maxDelta) {
        if (current < target) return Math.min(target, current + Math.max(0f, maxDelta));
        if (current > target) return Math.max(target, current - Math.max(0f, maxDelta));
        return target;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    public boolean isNight() {
        return timeOfDay > 0.68f || timeOfDay < 0.18f;
    }

    public float getNight01() {
        if (timeOfDay > 0.68f) return clamp((timeOfDay - 0.68f) / 0.12f, 0f, 1f);
        if (timeOfDay < 0.18f) return clamp((0.18f - timeOfDay) / 0.12f, 0f, 1f);
        return 0f;
    }

    public float getGripScale() {
        return clamp(1f - wetness * 0.24f - rainAmount * 0.08f, 0.66f, 1f);
    }

    public float getDragScale() {
        return clamp(1f + wetness * 0.08f, 1f, 1.12f);
    }

    public float getWetness() { return wetness; }
    public float getRainAmount() { return rainAmount; }
    public float getFogAmount() { return fogAmount; }
    public float getTimeOfDay() { return timeOfDay; }
    public int getWeatherMode() { return weatherMode; }
    public boolean isRainy() { return rainAmount > 0.25f; }
    public boolean isFoggy() { return fogAmount > 0.25f; }
    public boolean isForceNightRace() { return forceNightRace; }

    public String getWeatherLabel() {
        if (isRainy()) return "YAGMUR";
        if (isFoggy()) return "SIS";
        return "ACIK";
    }

    public String getTimeLabel() {
        if (isNight()) return "GECE";
        if (timeOfDay > 0.52f) return "AKSAM";
        if (timeOfDay < 0.30f) return "SABAH";
        return "GUNDUZ";
    }

    public String getHudText() {
        return getTimeLabel() + " / " + getWeatherLabel() + " / ISLAK " + (int)(wetness * 100f) + "%";
    }

    public float getHeadlightImportance() {
        return clamp(getNight01() * 0.85f + fogAmount * 0.42f + rainAmount * 0.26f, 0f, 1f);
    }

    public float getClearR() {
        return 0.04f + (1f - getNight01()) * 0.08f - rainAmount * 0.015f + fogAmount * 0.035f;
    }

    public float getClearG() {
        return 0.055f + (1f - getNight01()) * 0.10f - rainAmount * 0.010f + fogAmount * 0.035f;
    }

    public float getClearB() {
        return 0.075f + (1f - getNight01()) * 0.13f + fogAmount * 0.050f;
    }
}
