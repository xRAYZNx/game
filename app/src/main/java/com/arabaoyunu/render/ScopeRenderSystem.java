package com.arabaoyunu.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Holografik durbun (scope) render sistemi.
 *
 * Sorumluluklar:
 * - Holografik nisangah (reticle) cizimi. Onceki surumdeki merkezdeki
 *   mavi nokta kaldirilmistir; reticle sadece ince holografik halka,
 *   arti cizgisi ve kose markerlarindan olusur (temiz gorus).
 * - FOV gecislerinin kare hizindan bagimsiz, ustel yumusatma (exponential
 *   damping) ile yapilmasi: boylece scope ac/kapa animasyonu FPS dususu
 *   yaratmaz, hitch olusmaz.
 * - Reticle'in bir Bitmap'e bir kez rasterize edilip onbelleklenmesi:
 *   her karede vektor cizimi tekrarlanmaz, GPU/CPU yuku sabit kalir.
 * - Per-frame heap allocation yapilmaz (tum gecici nesneler alan olarak
 *   tutulur) — GC kaynakli kasma engellenir.
 */
public final class ScopeRenderSystem {

    /** Varsayilan (hipsfire) dikey FOV, derece. */
    public static final float DEFAULT_FOV = 62f;
    /** Holografik durbun tam zumda iken dikey FOV, derece. */
    public static final float SCOPED_FOV = 30f;

    /** FOV yumusatma zaman sabiti (saniye). Kucuk deger = hizli gecis. */
    private static final float FOV_DAMP_TAU = 0.085f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path tempPath = new Path();
    private final RectF tempRect = new RectF();

    private Bitmap cachedReticle;
    private int cachedReticleSize = -1;

    private boolean scoped;
    private float currentFov = DEFAULT_FOV;
    /** 0 = tamamen normal gorus, 1 = tamamen durbun. */
    private float scopeBlend;

    /** Durbun durumunu degistirir; gecis update() icinde yumusatilir. */
    public void setScoped(boolean scoped) {
        this.scoped = scoped;
    }

    public boolean isScoped() {
        return scoped;
    }

    /** Gecis buyuk olcude tamamlandiysa true doner (tam ekran karartma icin). */
    public boolean isScopeFullyEngaged() {
        return scoped && scopeBlend > 0.92f;
    }

    public float getScopeBlend() {
        return scopeBlend;
    }

    public float getCurrentFov() {
        return currentFov;
    }

    /**
     * Kare hizindan bagimsiz FOV gecisi.
     * dt bagimsiz ustel sondurme kullanilir: blend(t+dt) = target +
     * (blend - target) * exp(-dt / tau). Bu formul 30 FPS ile 120 FPS
     * arasinda ayni gecis suresini verir; FOV gecisi FPS dususune yol
     * acmaz.
     */
    public void update(float dtSeconds) {
        float clampedDt = dtSeconds;
        if (clampedDt < 0f) clampedDt = 0f;
        if (clampedDt > 0.1f) clampedDt = 0.1f; // uzun frame spike'larinda FOV ziplamasi olmaz
        float target = scoped ? 1f : 0f;
        float k = (float) Math.exp(-clampedDt / FOV_DAMP_TAU);
        scopeBlend = target + (scopeBlend - target) * k;
        if (Math.abs(scopeBlend - target) < 0.0015f) scopeBlend = target;
        currentFov = DEFAULT_FOV + (SCOPED_FOV - DEFAULT_FOV) * scopeBlend;
    }

    /**
     * Durbun overlay'ini cizer: dis karartma + holografik reticle.
     * Reticle, cozunurluk basina bir kez Bitmap'e rasterize edilir ve
     * sonraki karelerde dogrudan blit edilir (onbellek).
     */
    public void drawScopeOverlay(Canvas canvas, int viewWidth, int viewHeight) {
        if (scopeBlend <= 0.003f) return;

        int cx = viewWidth / 2;
        int cy = viewHeight / 2;
        int size = Math.min(viewWidth, viewHeight);

        // Dis karartma: scope disi alan yumusak sekilde kararir.
        int dimAlpha = (int) (205 * scopeBlend);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(dimAlpha, 2, 4, 6));
        canvas.drawRect(0f, 0f, viewWidth, Math.max(0f, cy - size * 0.42f), paint);
        canvas.drawRect(0f, Math.min(viewHeight, cy + size * 0.42f), viewWidth, viewHeight, paint);
        canvas.drawRect(0f, Math.max(0f, cy - size * 0.42f),
                Math.max(0f, cx - size * 0.42f), Math.min(viewHeight, cy + size * 0.42f), paint);
        canvas.drawRect(Math.min(viewWidth, cx + size * 0.42f), Math.max(0f, cy - size * 0.42f),
                viewWidth, Math.min(viewHeight, cy + size * 0.42f), paint);

        Bitmap reticle = obtainReticle(size);
        if (reticle != null) {
            paint.setColor(Color.WHITE);
            int alpha = (int) (235 * scopeBlend);
            paint.setAlpha(alpha);
            float left = cx - reticle.getWidth() * 0.5f;
            float top = cy - reticle.getHeight() * 0.5f;
            canvas.drawBitmap(reticle, left, top, paint);
            paint.setAlpha(255);
        }
    }

    /**
     * Holografik reticle bitmap'ini dondurur; gerekiyorsa yeniden uretir.
     * Mavi merkez nokta BILINCLI olarak cizilmez (kaldirildi).
     */
    private Bitmap obtainReticle(int size) {
        int targetSize = Math.max(64, size & ~1);
        if (cachedReticle != null && cachedReticleSize == targetSize) {
            return cachedReticle;
        }
        releaseReticle();
        cachedReticleSize = targetSize;
        cachedReticle = Bitmap.createBitmap(targetSize, targetSize, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(cachedReticle);
        rasterizeReticle(c, targetSize);
        return cachedReticle;
    }

    /**
     * Reticle vektor cizimi: holografik cember, arti cizgisi, mil isaretleri
     * ve kose braketleri. Merkeze nokta konmaz — hedef gorusu acik kalir.
     */
    private void rasterizeReticle(Canvas c, int size) {
        float cx = size * 0.5f;
        float cy = size * 0.5f;
        float r = size * 0.40f;

        int holo = Color.argb(235, 90, 235, 210);
        int holoDim = Color.argb(140, 90, 235, 210);

        // Dis holografik halka (cift cizgi: parlak + golge).
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, size * 0.006f));
        paint.setColor(holoDim);
        c.drawCircle(cx, cy, r, paint);
        paint.setStrokeWidth(Math.max(1.2f, size * 0.003f));
        paint.setColor(holo);
        c.drawCircle(cx, cy, r * 0.985f, paint);

        // Arti cizgisi: merkeze dogru kisa bosluk birakilir (nokta yok).
        float gap = r * 0.10f;
        paint.setStrokeWidth(Math.max(1.4f, size * 0.0035f));
        paint.setColor(holo);
        c.drawLine(cx - r, cy, cx - gap, cy, paint);
        c.drawLine(cx + gap, cy, cx + r, cy, paint);
        c.drawLine(cx, cy - r, cx, cy - gap, paint);
        c.drawLine(cx, cy + gap, cx, cy + r, paint);

        // Mesafe mil isaretleri (alt yarimda).
        paint.setStrokeWidth(Math.max(1.1f, size * 0.0028f));
        for (int i = 1; i <= 3; i++) {
            float my = cy + gap + (r - gap) * (i / 4f);
            float half = r * (0.045f + i * 0.015f);
            c.drawLine(cx - half, my, cx + half, my, paint);
        }

        // Kose braketleri (holografik cerceve hissi).
        tempPath.reset();
        float b = r * 1.10f;
        float bl = r * 0.16f;
        tempPath.moveTo(cx - b, cy - b + bl);
        tempPath.lineTo(cx - b, cy - b);
        tempPath.lineTo(cx - b + bl, cy - b);
        tempPath.moveTo(cx + b - bl, cy - b);
        tempPath.lineTo(cx + b, cy - b);
        tempPath.lineTo(cx + b, cy - b + bl);
        tempPath.moveTo(cx + b, cy + b - bl);
        tempPath.lineTo(cx + b, cy + b);
        tempPath.lineTo(cx + b - bl, cy + b);
        tempPath.moveTo(cx - b + bl, cy + b);
        tempPath.lineTo(cx - b, cy + b);
        tempPath.lineTo(cx - b, cy + b - bl);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.6f, size * 0.004f));
        paint.setColor(holoDim);
        c.drawPath(tempPath, paint);

        // Holografik parlama icin cok hafif ic cember.
        tempRect.set(cx - r * 0.55f, cy - r * 0.55f, cx + r * 0.55f, cy + r * 0.55f);
        paint.setStrokeWidth(Math.max(1f, size * 0.002f));
        paint.setColor(Color.argb(70, 120, 255, 235));
        c.drawArc(tempRect, 200f, 140f, false, paint);
    }

    /** GL thread / view yok edilirken bitmap kaynagini serbest birakir. */
    public void release() {
        releaseReticle();
    }

    private void releaseReticle() {
        if (cachedReticle != null) {
            cachedReticle.recycle();
            cachedReticle = null;
        }
        cachedReticleSize = -1;
    }
}
