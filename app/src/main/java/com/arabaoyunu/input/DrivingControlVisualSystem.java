package com.arabaoyunu.input;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * A67.4: Sürüş butonlarını tek premium oyun UI stilinde çizen yardımcı sistem.
 * Dokunma mantığı TouchControlsView içinde kalır; bu sınıf sadece görsel kaliteyi
 * ve aktif/pasif okunabilirliği merkezileştirir.
 */
public final class DrivingControlVisualSystem {
    private DrivingControlVisualSystem() {}

    public static void drawButton(Canvas canvas, Paint paint, RectF r, String text, boolean active, boolean circular,
                                  int passiveAlpha, int activeAlpha) {
        drawButtonAdvanced(canvas, paint, r, text, active ? 1f : 0f, circular, passiveAlpha, activeAlpha, "");
    }

    public static void drawButtonAdvanced(Canvas canvas, Paint paint, RectF r, String text, float pressure, boolean circular,
                                          int passiveAlpha, int activeAlpha, String microLabel) {
        float base = Math.max(1f, Math.min(r.width(), r.height()));
        float rad = circular ? base * 0.5f : base * 0.28f;
        float p = clamp(pressure, 0f, 1f);
        boolean active = p > 0.04f;
        float pressInset = active ? base * 0.035f * p : 0f;
        RectF body = new RectF(r.left + pressInset, r.top + pressInset, r.right - pressInset, r.bottom - pressInset);

        int fillAlpha = active ? (int)(activeAlpha * (0.74f + p * 0.26f)) : passiveAlpha;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(active ? Color.argb(fillAlpha, 0, 210, 255) : Color.argb(passiveAlpha, 255, 255, 255));
        canvas.drawRoundRect(body, rad, rad, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2.2f, base * (0.044f + p * 0.018f)));
        paint.setColor(active ? Color.argb(235, 255, 255, 255) : Color.argb(160, 255, 255, 255));
        canvas.drawRoundRect(body, rad, rad, paint);

        paint.setStrokeWidth(Math.max(1.1f, base * 0.020f));
        paint.setColor(Color.argb(active ? 220 : 145, 0, 0, 0));
        RectF inner = new RectF(body.left + base * 0.09f, body.top + base * 0.09f, body.right - base * 0.09f, body.bottom - base * 0.09f);
        canvas.drawRoundRect(inner, Math.max(8f, rad * 0.62f), Math.max(8f, rad * 0.62f), paint);

        if (active) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(2.0f, base * 0.035f));
            paint.setColor(Color.argb(190, 255, 255, 255));
            RectF arc = new RectF(body.left + base * 0.075f, body.top + base * 0.075f, body.right - base * 0.075f, body.bottom - base * 0.075f);
            canvas.drawArc(arc, -90f, 360f * p, false, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        float textScale = text != null && text.length() >= 4 ? 0.24f : (text != null && text.length() == 3 ? 0.29f : 0.39f);
        paint.setTextSize(Math.max(12f, base * textScale));
        Paint.FontMetrics fm = paint.getFontMetrics();
        canvas.drawText(text == null ? "" : text, body.centerX(), body.centerY() - (fm.ascent + fm.descent) * 0.5f, paint);

        if (microLabel != null && microLabel.length() > 0 && !circular) {
            paint.setColor(Color.argb(active ? 235 : 165, 205, 235, 255));
            paint.setTextSize(Math.max(7.5f, base * 0.105f));
            canvas.drawText(microLabel, body.centerX(), body.bottom - base * 0.16f, paint);
        }
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
