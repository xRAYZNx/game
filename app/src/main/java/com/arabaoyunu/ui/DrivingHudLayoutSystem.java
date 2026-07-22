package com.arabaoyunu.ui;

import android.graphics.RectF;

/**
 * A67.4: Sürüş HUD elemanlarının telefon/tablet safe-area içinde çakışmadan
 * yerleşmesi için merkezi ölçü sistemi. Mini harita, büyük harita overlay,
 * hız yazısı ve dokunma alanı aynı koordinatları kullanır.
 */
public final class DrivingHudLayoutSystem {
    private DrivingHudLayoutSystem() {}

    public static float scaleFor(int w, int h) {
        return Math.max(1f, Math.min(w, h) / 720f);
    }

    public static boolean compact(int w, int h) {
        return w < 900 || h < 540;
    }

    public static RectF miniMapRect(int w, int h, float scale) {
        float min = Math.min(w, h);
        float size = Math.max(118f * scale, Math.min(min * 0.186f, compact(w, h) ? 148f * scale : 172f * scale));
        float left = Math.max(14f * scale, w * 0.012f);
        float top = Math.max(66f * scale, h * 0.080f);
        return new RectF(left, top, left + size, top + size);
    }

    public static RectF miniMapTouchRect(int w, int h, float scale) {
        RectF r = miniMapRect(w, h, scale);
        float pad = Math.max(8f * scale, Math.min(w, h) * 0.010f);
        r.inset(-pad, -pad);
        return r;
    }

    public static RectF expandedMapPanelRect(int w, int h, float scale) {
        float marginX = Math.max(18f * scale, w * (compact(w, h) ? 0.050f : 0.090f));
        float marginTop = Math.max(58f * scale, h * 0.075f);
        float marginBottom = Math.max(24f * scale, h * 0.045f);
        return new RectF(marginX, marginTop, w - marginX, h - marginBottom);
    }

    public static RectF expandedMapCloseRect(int w, int h, float scale) {
        RectF p = expandedMapPanelRect(w, h, scale);
        float bw = Math.max(76f * scale, p.width() * 0.115f);
        float bh = Math.max(34f * scale, p.height() * 0.070f);
        return new RectF(p.right - bw - 14f * scale, p.top + 12f * scale, p.right - 14f * scale, p.top + 12f * scale + bh);
    }

    public static float speedTextY(int w, int h, float scale) {
        RectF m = miniMapRect(w, h, scale);
        return m.bottom + Math.max(25f * scale, Math.min(h * 0.045f, 34f * scale));
    }

    public static float compactStatusTop(int w, int h, float scale) {
        RectF m = miniMapRect(w, h, scale);
        return m.bottom + Math.max(50f * scale, h * 0.075f);
    }
}
