package com.arabaoyunu.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * A67.4: Mini haritaya dokununca açılan büyük harita panelinin ortak kart/
 * başlık/close görseli. Harita verisi HudView tarafından çizilir; bu sınıf
 * overlay'in profesyonel oyun UI çerçevesini sağlar.
 */
public final class InGameMapOverlaySystem {
    private InGameMapOverlaySystem() {}

    public static void drawDim(Canvas canvas, Paint paint, int w, int h) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(132, 0, 0, 0));
        canvas.drawRect(0, 0, w, h, paint);
    }

    public static void drawPanelFrame(Canvas canvas, Paint paint, RectF panel, float scale) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(228, 8, 13, 22));
        canvas.drawRoundRect(panel, 26f * scale, 26f * scale, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.8f * scale);
        paint.setColor(Color.argb(170, 255, 255, 255));
        canvas.drawRoundRect(panel, 26f * scale, 26f * scale, paint);
        paint.setStrokeWidth(1.4f * scale);
        paint.setColor(Color.argb(210, 0, 210, 255));
        canvas.drawRoundRect(new RectF(panel.left + 4f * scale, panel.top + 4f * scale,
                panel.right - 4f * scale, panel.bottom - 4f * scale), 22f * scale, 22f * scale, paint);
    }

    public static void drawHeader(Canvas canvas, Paint paint, RectF panel, RectF close, String title, String subtitle, float scale) {
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(15f * scale, panel.height() * 0.035f));
        canvas.drawText(title == null ? "HARİTA" : title, panel.left + 18f * scale, panel.top + 30f * scale, paint);
        paint.setColor(Color.argb(210, 210, 230, 245));
        paint.setTextSize(Math.max(10f * scale, panel.height() * 0.024f));
        canvas.drawText(subtitle == null ? "Konum / rota / hedef" : subtitle, panel.left + 18f * scale, panel.top + 50f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(150, 255, 255, 255));
        canvas.drawRoundRect(close, 14f * scale, 14f * scale, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f * scale);
        paint.setColor(Color.argb(190, 255, 255, 255));
        canvas.drawRoundRect(close, 14f * scale, 14f * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(11f * scale, close.height() * 0.33f));
        paint.setColor(Color.WHITE);
        Paint.FontMetrics fm = paint.getFontMetrics();
        canvas.drawText("KAPAT", close.centerX(), close.centerY() - (fm.ascent + fm.descent) * 0.5f, paint);
    }

    public static String subtitle(String map, String mode, float distance) {
        String safeMap = map == null || map.length() == 0 ? "Harita" : map;
        String safeMode = mode == null || mode.length() == 0 ? "Sürüş" : mode;
        return safeMap + " • " + safeMode + " • hedef " + (int)Math.max(0f, distance) + "m";
    }
}
