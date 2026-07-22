package com.arabaoyunu.util;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

/** A63.0: Ayarlardaki titresimi gerçek oyun olaylarına bağlayan güvenli haptic katman. */
public final class HapticFeedbackSystem {
    private final Vibrator vibrator;
    private final SaveManager saveManager;
    private long lastMs;

    public HapticFeedbackSystem(Context context, SaveManager saveManager) {
        this.saveManager = saveManager;
        Vibrator v = null;
        try {
            if (context != null) v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Throwable ignored) {}
        this.vibrator = v;
    }

    public void tick(String kind) {
        if (vibrator == null || saveManager == null || !saveManager.isVibrationEnabled()) return;
        long now = System.currentTimeMillis();
        if (now - lastMs < 120L) return;
        int ms = 18;
        if ("impact".equals(kind)) ms = 46;
        else if ("reward".equals(kind)) ms = 34;
        else if ("police".equals(kind)) ms = 28;
        else if ("nitro".equals(kind)) ms = 24;
        else if ("throttle".equals(kind)) ms = 16;
        else if ("brake".equals(kind)) ms = 20;
        else if ("handbrake".equals(kind)) ms = 26;
        try {
            if (Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
            else vibrator.vibrate(ms);
            lastMs = now;
        } catch (Throwable ignored) {}
    }
}
