package com.arabaoyunu.menu;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Ana menu yerlesim sistemi (A64+ espor/neon tasarim).
 *
 * Duzen:
 * - 3D arac/operator sahnesi TAM ORTADA (center pivot): sahne merkezi
 *   ekran merkezine sabitlenir; etrafina holografik kose braketli bir
 *   pivot cercevesi cizilir.
 * - SOL panel  : NAVIGASYON (menu butonlari dikey dizilir).
 * - SAG panel  : PROFIL / SOSYAL (seviye, lig, coin, ekip/cevrimici bilgisi).
 *
 * Butonlar modern espor/neon tarzda: ince parlak cizgi, yumusak dis
 * parilti (glow) ve zamana bagli nabiz (pulse) animasyonu. Animasyon
 * saf matematiktir (System.currentTimeMillis), ayri thread gerektirmez.
 */
public final class MainMenuLayoutSystem {

    private final RectF centerPivotFrame = new RectF();
    private final RectF leftNavPanel = new RectF();
    private final RectF rightProfilePanel = new RectF();
    private final RectF rightSocialPanel = new RectF();
    private final RectF tempRect = new RectF();

    private int lastWidth;
    private int lastHeight;

    /** Ekran boyutuna gore tum bolgeleri hesaplar. */
    public void layout(int w, int h) {
        lastWidth = w;
        lastHeight = h;
        float min = Math.min(w, h);
        float pad = Math.max(14f, min * 0.026f);

        // Center pivot: 3D sahne tam ekran merkezinde.
        float pivotHalfW = w * 0.155f;
        float pivotHalfH = h * 0.235f;
        centerPivotFrame.set(w * 0.5f - pivotHalfW, h * 0.5f - pivotHalfH,
                w * 0.5f + pivotHalfW, h * 0.5f + pivotHalfH);

        // Sol navigasyon paneli.
        float navW = Math.max(150f, w * 0.205f);
        float navTop = h * 0.265f;
        leftNavPanel.set(pad, navTop, pad + navW, h * 0.885f);

        // Sag profil + sosyal panelleri.
        float profW = Math.max(170f, w * 0.235f);
        float profTop = h * 0.150f;
        rightProfilePanel.set(w - pad - profW, profTop, w - pad, profTop + Math.max(120f, h * 0.190f));
        rightSocialPanel.set(w - pad - profW, rightProfilePanel.bottom + pad * 0.7f,
                w - pad, rightProfilePanel.bottom + pad * 0.7f + Math.max(96f, h * 0.150f));
    }

    public RectF getCenterPivotFrame() {
        return centerPivotFrame;
    }

    public RectF getLeftNavPanel() {
        return leftNavPanel;
    }

    public RectF getRightProfilePanel() {
        return rightProfilePanel;
    }

    /**
     * Sol panel icindeki dikey navigasyon butonlarinin konumlarini verilen
     * RectF dizisine yazar (mevcut buton RectF'leri yeniden kullanilir).
     */
    public void layoutNavButtons(RectF[] buttons) {
        if (buttons == null || buttons.length == 0) return;
        float inner = Math.max(10f, Math.min(lastWidth, lastHeight) * 0.016f);
        float gap = Math.max(8f, Math.min(lastWidth, lastHeight) * 0.014f);
        float top = leftNavPanel.top + inner * 2.6f;
        float usableH = leftNavPanel.bottom - inner - top;
        float btnH = Math.min(Math.max(40f, Math.min(lastWidth, lastHeight) * 0.070f),
                (usableH - gap * (buttons.length - 1)) / buttons.length);
        for (int i = 0; i < buttons.length; i++) {
            float y = top + i * (btnH + gap);
            buttons[i].set(leftNavPanel.left + inner, y, leftNavPanel.right - inner, y + btnH);
        }
    }

    /** Nabiz fazi: 0..1 arasi yumusak sinus (neon glow animasyonu). */
    public static float pulsePhase() {
        return (float) (0.5 + 0.5 * Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI * 2.0 * 0.45));
    }

    /** 3D sahnenin tam ortada oldugunu gosteren pivot cercevesi. */
    public void drawCenterPivotFrame(Canvas canvas, Paint paint) {
        RectF r = centerPivotFrame;
        float pulse = pulsePhase();
        int holo = Color.argb(90 + (int) (60 * pulse), 0, 220, 255);

        // Kose braketleri (holografik).
        float b = Math.min(r.width(), r.height()) * 0.14f;
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2.4f, Math.min(lastWidth, lastHeight) * 0.004f));
        paint.setColor(holo);
        canvas.drawLine(r.left, r.top + b, r.left, r.top, paint);
        canvas.drawLine(r.left, r.top, r.left + b, r.top, paint);
        canvas.drawLine(r.right - b, r.top, r.right, r.top, paint);
        canvas.drawLine(r.right, r.top, r.right, r.top + b, paint);
        canvas.drawLine(r.right, r.bottom - b, r.right, r.bottom, paint);
        canvas.drawLine(r.right, r.bottom, r.right - b, r.bottom, paint);
        canvas.drawLine(r.left + b, r.bottom, r.left, r.bottom, paint);
        canvas.drawLine(r.left, r.bottom, r.left, r.bottom - b, paint);

        // Tam merkez isareti (pivot noktasi).
        float cx = r.centerX();
        float cy = r.centerY();
        float m = Math.max(6f, Math.min(lastWidth, lastHeight) * 0.010f);
        paint.setStrokeWidth(Math.max(1.6f, Math.min(lastWidth, lastHeight) * 0.0025f));
        paint.setColor(Color.argb(120 + (int) (80 * pulse), 120, 240, 255));
        canvas.drawLine(cx - m, cy, cx + m, cy, paint);
        canvas.drawLine(cx, cy - m, cx, cy + m, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    /** Sol navigasyon paneli arka plani + baslik. */
    public void drawNavPanel(Canvas canvas, Paint paint, String title) {
        drawGlassPanel(canvas, paint, leftNavPanel,
                Color.argb(150, 6, 12, 24), Color.argb(200, 0, 210, 255));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(235, 0, 220, 255));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(Math.max(12f, Math.min(lastWidth, lastHeight) * 0.020f));
        canvas.drawText(title, leftNavPanel.left + Math.max(10f, leftNavPanel.width() * 0.07f),
                leftNavPanel.top + Math.max(22f, leftNavPanel.height() * 0.075f), paint);
    }

    /** Sag profil/sosyal panelleri. */
    public void drawProfilePanels(Canvas canvas, Paint paint,
                                  String levelLine, String coinLine, String leagueLine,
                                  String crewLine, String onlineLine) {
        float min = Math.min(lastWidth, lastHeight);
        drawGlassPanel(canvas, paint, rightProfilePanel,
                Color.argb(150, 6, 12, 24), Color.argb(200, 255, 90, 200));
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        float px = rightProfilePanel.left + Math.max(10f, rightProfilePanel.width() * 0.07f);
        paint.setColor(Color.argb(240, 255, 120, 220));
        paint.setTextSize(Math.max(12f, min * 0.020f));
        canvas.drawText("PROFIL", px, rightProfilePanel.top + rightProfilePanel.height() * 0.20f, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(11f, min * 0.018f));
        canvas.drawText(levelLine, px, rightProfilePanel.top + rightProfilePanel.height() * 0.44f, paint);
        paint.setColor(Color.argb(240, 255, 220, 90));
        canvas.drawText(coinLine, px, rightProfilePanel.top + rightProfilePanel.height() * 0.64f, paint);
        paint.setColor(Color.argb(225, 235, 245, 255));
        canvas.drawText(leagueLine, px, rightProfilePanel.top + rightProfilePanel.height() * 0.84f, paint);

        drawGlassPanel(canvas, paint, rightSocialPanel,
                Color.argb(140, 6, 12, 24), Color.argb(180, 120, 255, 170));
        float sx = rightSocialPanel.left + Math.max(10f, rightSocialPanel.width() * 0.07f);
        paint.setColor(Color.argb(240, 120, 255, 170));
        paint.setTextSize(Math.max(12f, min * 0.020f));
        canvas.drawText("SOSYAL", sx, rightSocialPanel.top + rightSocialPanel.height() * 0.24f, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(11f, min * 0.018f));
        canvas.drawText(crewLine, sx, rightSocialPanel.top + rightSocialPanel.height() * 0.52f, paint);
        paint.setColor(Color.argb(225, 235, 245, 255));
        canvas.drawText(onlineLine, sx, rightSocialPanel.top + rightSocialPanel.height() * 0.76f, paint);
    }

    /**
     * Modern espor/neon buton: ince parlak govde cizgisi, nabizli dis
     * glow ve hafif ice golge. Aktif butonlarda glow guclenir.
     */
    public void drawNeonButton(Canvas canvas, Paint paint, RectF r, String label,
                               boolean primary, boolean pressed) {
        float pulse = pulsePhase();
        int neon = primary ? Color.argb(255, 0, 220, 255) : Color.argb(255, 255, 90, 200);
        float radius = Math.max(10f, r.height() * 0.24f);

        // Govde.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(pressed ? 170 : 120, 8, 14, 26));
        canvas.drawRoundRect(r, radius, radius, paint);

        // Dis glow (nabizli).
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(4f, r.height() * 0.09f));
        int glowAlpha = (primary ? 46 : 30) + (int) (26 * pulse) + (pressed ? 30 : 0);
        paint.setColor(Color.argb(Math.min(160, glowAlpha),
                Color.red(neon), Color.green(neon), Color.blue(neon)));
        tempRect.set(r.left - 1.5f, r.top - 1.5f, r.right + 1.5f, r.bottom + 1.5f);
        canvas.drawRoundRect(tempRect, radius + 1.5f, radius + 1.5f, paint);

        // Ana parlak cizgi.
        paint.setStrokeWidth(Math.max(2f, r.height() * 0.045f));
        paint.setColor(Color.argb(primary ? 230 : 185,
                Color.red(neon), Color.green(neon), Color.blue(neon)));
        canvas.drawRoundRect(r, radius, radius, paint);

        // Ust kenar isigi (espor panel hissi).
        paint.setStrokeWidth(Math.max(1.2f, r.height() * 0.03f));
        paint.setColor(Color.argb(120 + (int) (60 * pulse), 255, 255, 255));
        canvas.drawLine(r.left + radius, r.top + 1.5f, r.right - radius, r.top + 1.5f, paint);

        // Etiket.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(245, 242, 248, 255));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(12f, r.height() * 0.34f));
        Paint.FontMetrics fm = paint.getFontMetrics();
        canvas.drawText(label, r.centerX(), r.centerY() - (fm.ascent + fm.descent) * 0.5f, paint);
    }

    private void drawGlassPanel(Canvas canvas, Paint paint, RectF r, int fillColor, int strokeColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        canvas.drawRoundRect(r, 18f, 18f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.2f);
        paint.setColor(strokeColor);
        canvas.drawRoundRect(r, 18f, 18f, paint);
        paint.setStyle(Paint.Style.FILL);
    }
}
