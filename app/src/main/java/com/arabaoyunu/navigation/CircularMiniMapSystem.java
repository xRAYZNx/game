package com.arabaoyunu.navigation;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * A67.4: Mini haritayı dairesel alana taşıyan hafif koordinat/çizim yardımcısı.
 * Harita gerçek GLB/şehir asset'i eklemez; mevcut dünya koordinatlarını güvenli
 * şekilde küçük/büyük harita koordinatına çevirir.
 */
public final class CircularMiniMapSystem {
    private CircularMiniMapSystem() {}

    public static float mapToX(RectF rect, float mapHalf, float worldX) {
        float radius = safeRadius(rect) * 0.82f;
        return rect.centerX() + clamp(worldX / safeHalf(mapHalf), -1f, 1f) * radius;
    }

    public static float mapToZ(RectF rect, float mapHalf, float worldZ) {
        float radius = safeRadius(rect) * 0.82f;
        return rect.centerY() + clamp(worldZ / safeHalf(mapHalf), -1f, 1f) * radius;
    }

    public static void clipCircle(Canvas canvas, Path path, RectF rect) {
        path.reset();
        path.addCircle(rect.centerX(), rect.centerY(), safeRadius(rect), Path.Direction.CW);
        canvas.clipPath(path);
    }

    public static void drawBase(Canvas canvas, Paint paint, RectF rect, float scale, boolean expanded) {
        float radius = safeRadius(rect);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(expanded ? Color.argb(238, 8, 14, 24) : Color.argb(178, 5, 12, 20));
        canvas.drawCircle(rect.centerX(), rect.centerY(), radius, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(expanded ? 2.6f * scale : 1.7f * scale);
        paint.setColor(Color.argb(118, 255, 255, 255));
        float step = radius / 2f;
        for (int i = -2; i <= 2; i++) {
            float x = rect.centerX() + i * step;
            canvas.drawLine(x, rect.centerY() - radius, x, rect.centerY() + radius, paint);
            float y = rect.centerY() + i * step;
            canvas.drawLine(rect.centerX() - radius, y, rect.centerX() + radius, y, paint);
        }

        paint.setStrokeWidth(expanded ? 4f * scale : 2.4f * scale);
        paint.setColor(Color.argb(180, 0, 210, 255));
        canvas.drawLine(rect.centerX(), rect.top + radius * 0.16f, rect.centerX(), rect.bottom - radius * 0.16f, paint);
        canvas.drawLine(rect.left + radius * 0.16f, rect.centerY(), rect.right - radius * 0.16f, rect.centerY(), paint);
    }

    public static void drawOuterRing(Canvas canvas, Paint paint, RectF rect, float scale, boolean expanded) {
        float radius = safeRadius(rect);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(expanded ? 4.2f * scale : 3.0f * scale);
        paint.setColor(Color.argb(220, 255, 255, 255));
        canvas.drawCircle(rect.centerX(), rect.centerY(), radius, paint);
        paint.setStrokeWidth(expanded ? 2.0f * scale : 1.2f * scale);
        paint.setColor(Color.argb(230, 0, 220, 255));
        canvas.drawCircle(rect.centerX(), rect.centerY(), radius - 4f * scale, paint);
    }

    public static void drawPlayer(Canvas canvas, Paint paint, RectF rect, float mapHalf, float carX, float carZ, float yaw, float scale, boolean expanded) {
        float x = mapToX(rect, mapHalf, carX);
        float z = mapToZ(rect, mapHalf, carZ);
        float r = expanded ? 8.5f * scale : 5.2f * scale;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(255, 0, 225, 255));
        canvas.drawCircle(x, z, r, paint);

        float len = expanded ? 24f * scale : 13f * scale;
        float dirX = (float)Math.sin(yaw) * len;
        float dirY = (float)Math.cos(yaw) * len;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(expanded ? 4.6f * scale : 3.0f * scale);
        paint.setColor(Color.argb(255, 0, 245, 255));
        canvas.drawLine(x, z, x + dirX, z + dirY, paint);
    }

    public static float safeHalf(float mapHalf) {
        return Math.max(80f, mapHalf);
    }

    private static float safeRadius(RectF rect) {
        return Math.max(1f, Math.min(rect.width(), rect.height()) * 0.5f);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
