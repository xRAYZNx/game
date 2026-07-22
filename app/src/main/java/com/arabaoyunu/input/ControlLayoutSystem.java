package com.arabaoyunu.input;

import android.content.Context;
import android.content.SharedPreferences;

import com.arabaoyunu.menu.GameScreenState;

/**
 * A63.3: Mobil kontrol düzeni, tablet şablonu, hassasiyet, pedal boyutu,
 * solak mod ve buton opaklığını tek yerde toplayan hafif kontrol sistemi.
 * Veriler SaveManager ile aynı SharedPreferences dosyasında tutulur; böylece
 * menü ayarları ve oyun içi kontrol katmanı aynı kaydı okur.
 */
public final class ControlLayoutSystem {
    public static final int LAYOUT_CLASSIC = 0;
    public static final int LAYOUT_LEFT_STEER_RIGHT_PEDAL = 1;
    public static final int LAYOUT_RIGHT_STEER_LEFT_PEDAL = 2;
    public static final int LAYOUT_TABLET_WIDE = 3;

    public static final int SENS_SMOOTH = 0;
    public static final int SENS_BALANCED = 1;
    public static final int SENS_SPORT = 2;
    public static final int SENS_DRIFT = 3;
    public static final int SENS_DRAG = 4;

    public static final int PEDAL_SMALL = 0;
    public static final int PEDAL_NORMAL = 1;
    public static final int PEDAL_LARGE = 2;
    public static final int PEDAL_TABLET = 3;

    public static final int HUD_COMPACT = 0;
    public static final int HUD_ADVANCED = 1;
    public static final int HUD_RACE = 2;
    public static final int HUD_DRIFT = 3;

    private static final String PREFS = "araba_oyunu_save";
    private static final String KEY_CONTROL_LAYOUT_PRESET = "control_layout_preset";
    private static final String KEY_CONTROL_SENSITIVITY = "control_sensitivity";
    private static final String KEY_PEDAL_SIZE_PRESET = "pedal_size_preset";
    private static final String KEY_HUD_PRESET = "hud_preset";
    private static final String KEY_BUTTON_OPACITY = "button_opacity_percent";
    private static final String KEY_LEFT_HANDED = "left_handed_mode";
    private static final String KEY_AUTO_CONTROL = "auto_control_by_mode";

    private final SharedPreferences prefs;

    public ControlLayoutSystem(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        ensureDefaults();
    }

    private void ensureDefaults() {
        SharedPreferences.Editor e = null;
        if (!prefs.contains(KEY_CONTROL_LAYOUT_PRESET)) { e = edit(e); e.putInt(KEY_CONTROL_LAYOUT_PRESET, LAYOUT_CLASSIC); }
        if (!prefs.contains(KEY_CONTROL_SENSITIVITY)) { e = edit(e); e.putInt(KEY_CONTROL_SENSITIVITY, SENS_BALANCED); }
        if (!prefs.contains(KEY_PEDAL_SIZE_PRESET)) { e = edit(e); e.putInt(KEY_PEDAL_SIZE_PRESET, PEDAL_NORMAL); }
        if (!prefs.contains(KEY_HUD_PRESET)) { e = edit(e); e.putInt(KEY_HUD_PRESET, HUD_COMPACT); }
        if (!prefs.contains(KEY_BUTTON_OPACITY)) { e = edit(e); e.putInt(KEY_BUTTON_OPACITY, 80); }
        if (!prefs.contains(KEY_LEFT_HANDED)) { e = edit(e); e.putBoolean(KEY_LEFT_HANDED, false); }
        if (!prefs.contains(KEY_AUTO_CONTROL)) { e = edit(e); e.putBoolean(KEY_AUTO_CONTROL, true); }
        if (e != null) e.apply();
    }

    private SharedPreferences.Editor edit(SharedPreferences.Editor e) { return e == null ? prefs.edit() : e; }

    public int getLayoutPreset() { return clampInt(prefs.getInt(KEY_CONTROL_LAYOUT_PRESET, LAYOUT_CLASSIC), 0, 3); }
    public int getSensitivityPreset() { return clampInt(prefs.getInt(KEY_CONTROL_SENSITIVITY, SENS_BALANCED), 0, 4); }
    public int getPedalSizePreset() { return clampInt(prefs.getInt(KEY_PEDAL_SIZE_PRESET, PEDAL_NORMAL), 0, 3); }
    public int getHudPreset() { return clampInt(prefs.getInt(KEY_HUD_PRESET, HUD_COMPACT), 0, 3); }
    public int getButtonOpacityPercent() { return clampInt(prefs.getInt(KEY_BUTTON_OPACITY, 80), 40, 100); }
    public boolean isLeftHandedMode() { return prefs.getBoolean(KEY_LEFT_HANDED, false); }
    public boolean isAutoByMode() { return prefs.getBoolean(KEY_AUTO_CONTROL, true); }

    public void cycleLayoutPreset() { prefs.edit().putInt(KEY_CONTROL_LAYOUT_PRESET, (getLayoutPreset() + 1) % 4).apply(); }
    public void cycleSensitivityPreset() { prefs.edit().putInt(KEY_CONTROL_SENSITIVITY, (getSensitivityPreset() + 1) % 5).apply(); }
    public void cyclePedalSizePreset() { prefs.edit().putInt(KEY_PEDAL_SIZE_PRESET, (getPedalSizePreset() + 1) % 4).apply(); }
    public void cycleHudPreset() { prefs.edit().putInt(KEY_HUD_PRESET, (getHudPreset() + 1) % 4).apply(); }
    public void cycleButtonOpacity() {
        int v = getButtonOpacityPercent();
        if (v < 60) v = 60; else if (v < 80) v = 80; else if (v < 100) v = 100; else v = 40;
        prefs.edit().putInt(KEY_BUTTON_OPACITY, v).apply();
    }
    public void toggleLeftHandedMode() { prefs.edit().putBoolean(KEY_LEFT_HANDED, !isLeftHandedMode()).apply(); }
    public void toggleAutoByMode() { prefs.edit().putBoolean(KEY_AUTO_CONTROL, !isAutoByMode()).apply(); }

    public boolean steeringOnRight() {
        int layout = getLayoutPreset();
        return isLeftHandedMode() || layout == LAYOUT_RIGHT_STEER_LEFT_PEDAL;
    }

    public float pedalScale() {
        switch (getPedalSizePreset()) {
            case PEDAL_SMALL: return 0.86f;
            case PEDAL_LARGE: return 1.16f;
            case PEDAL_TABLET: return 1.30f;
            default: return getLayoutPreset() == LAYOUT_TABLET_WIDE ? 1.18f : 1.0f;
        }
    }

    public float steeringScale() {
        return getLayoutPreset() == LAYOUT_TABLET_WIDE ? 1.18f : 1.0f;
    }

    public float applySteering(float rawSteer) {
        float scale;
        switch (getSensitivityPreset()) {
            case SENS_SMOOTH: scale = 0.82f; break;
            case SENS_SPORT: scale = 1.15f; break;
            case SENS_DRIFT: scale = 1.34f; break;
            case SENS_DRAG: scale = 0.72f; break;
            case SENS_BALANCED:
            default: scale = 1.0f; break;
        }
        return clamp(rawSteer * scale, -1f, 1f);
    }

    public int passiveAlpha() { return clampInt((int)(getButtonOpacityPercent() * 0.88f), 35, 100); }
    public int activeAlpha() { return clampInt(passiveAlpha() + 92, 120, 205); }

    public String layoutLabel() {
        switch (getLayoutPreset()) {
            case LAYOUT_LEFT_STEER_RIGHT_PEDAL: return "Sol direk";
            case LAYOUT_RIGHT_STEER_LEFT_PEDAL: return "Sağ direk";
            case LAYOUT_TABLET_WIDE: return "Tablet";
            default: return "Klasik";
        }
    }

    public String sensitivityLabel() {
        switch (getSensitivityPreset()) {
            case SENS_SMOOTH: return "Yumuşak";
            case SENS_SPORT: return "Sport";
            case SENS_DRIFT: return "Drift";
            case SENS_DRAG: return "Drag";
            default: return "Dengeli";
        }
    }

    public String pedalLabel() {
        switch (getPedalSizePreset()) {
            case PEDAL_SMALL: return "Küçük";
            case PEDAL_LARGE: return "Büyük";
            case PEDAL_TABLET: return "Tablet";
            default: return "Normal";
        }
    }

    public String hudLabel() {
        switch (getHudPreset()) {
            case HUD_ADVANCED: return "Gelişmiş";
            case HUD_RACE: return "Yarış";
            case HUD_DRIFT: return "Drift";
            default: return "Sade";
        }
    }

    public String recommendedForMode(int mode) {
        if (mode == GameScreenState.MODE_DRAG_RACE) return "Öneri: Drag hassasiyeti + büyük pedal";
        if (mode == GameScreenState.MODE_DRIFT) return "Öneri: Drift hassasiyeti + geniş kamera";
        if (mode == GameScreenState.MODE_POLICE_CHASE) return "Öneri: Sport hassasiyeti + yüksek opaklık";
        if (mode == GameScreenState.MODE_RACE_LOCKED || mode == GameScreenState.MODE_TIME_TRIAL) return "Öneri: Dengeli/Sport viraj kontrolü";
        return "Öneri: Klasik veya tablet geniş düzen";
    }

    private static int clampInt(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }
}
