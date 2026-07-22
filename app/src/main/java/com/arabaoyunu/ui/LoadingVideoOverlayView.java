package com.arabaoyunu.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.arabaoyunu.R;

/**
 * ArabaOyunu_61_4:
 * - Oyun ilk girişinde video intro + yükleme çubuğu.
 * - Pasif harita slotlarında güvenli geri dönüş mesajı.
 * Bu view tamamen UI katmanındadır; GL harita hazır olmadan sürüş ekranına geçilmez.
 */
public final class LoadingVideoOverlayView extends FrameLayout {
    public static final int MODE_INTRO = 0;
    public static final int MODE_OPEN_WORLD = 1;

    private final VideoView videoView;
    private final HudLayer hudLayer;

    private int mode = MODE_INTRO;
    private float progress;
    private String status = "Yükleniyor";
    private String hint = "Modifiye ettiğin araç sürüşte aynı ayarlarla kullanılır.";
    private boolean playing;

    public LoadingVideoOverlayView(Context context) {
        super(context);
        setClickable(true);
        setFocusable(true);
        setVisibility(GONE);
        setBackgroundColor(Color.BLACK);

        videoView = new VideoView(context);
        addView(videoView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        hudLayer = new HudLayer(context);
        addView(hudLayer, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                mp.setVolume(0f, 0f);
                if (playing) {
                    videoView.start();
                }
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // Video açılamazsa loading UI yine çalışsın; oyun çökmesin.
                videoView.setVisibility(INVISIBLE);
                return true;
            }
        });
    }

    public void showIntro() {
        mode = MODE_INTRO;
        progress = 0f;
        status = "Oyun dosyaları hazırlanıyor";
        hint = "Motor yükseltmeleri hızlanmayı artırır.";
        beginPlayback();
    }

    public void showOpenWorld() {
        mode = MODE_OPEN_WORLD;
        progress = 0f;
        status = "Açık Dünya kapalı";
        hint = "Açık Dünya haritası geçici olarak kaldırıldı.";
        beginPlayback();
    }

    private void beginPlayback() {
        playing = true;
        setVisibility(VISIBLE);
        bringToFront();
        videoView.setVisibility(VISIBLE);
        try {
            Uri uri = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + R.raw.loading_intro);
            videoView.setVideoURI(uri);
            videoView.start();
        } catch (Throwable t) {
            videoView.setVisibility(INVISIBLE);
        }
        hudLayer.invalidate();
    }

    public void setLoadingState(float progress, String status, String hint) {
        if (progress < 0f) progress = 0f;
        if (progress > 1f) progress = 1f;
        this.progress = progress;
        if (status != null && status.length() > 0) this.status = status;
        if (hint != null && hint.length() > 0) this.hint = hint;
        hudLayer.invalidate();
    }

    public float getProgress() {
        return progress;
    }

    public boolean isOpenWorldMode() {
        return mode == MODE_OPEN_WORLD;
    }

    public void hideOverlay() {
        playing = false;
        try {
            videoView.stopPlayback();
        } catch (Throwable ignored) {
        }
        setVisibility(GONE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Loading sırasında alttaki menü butonlarına dokunma gitmesin.
        return true;
    }

    private final class HudLayer extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF panel = new RectF();
        private final RectF barBg = new RectF();
        private final RectF barFg = new RectF();

        HudLayer(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            float min = Math.max(1f, Math.min(w, h));
            float scale = min / 720f;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(92, 0, 0, 0));
            canvas.drawRect(0, 0, w, h, paint);

            float panelW = Math.min(w * 0.78f, 720f * scale);
            float panelH = Math.max(116f * scale, h * 0.18f);
            float left = (w - panelW) * 0.5f;
            float top = h - panelH - Math.max(28f * scale, h * 0.045f);
            panel.set(left, top, left + panelW, top + panelH);

            paint.setColor(Color.argb(178, 8, 13, 24));
            canvas.drawRoundRect(panel, 22f * scale, 22f * scale, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1.8f * scale);
            paint.setColor(Color.argb(210, 0, 220, 255));
            canvas.drawRoundRect(panel, 22f * scale, 22f * scale, paint);
            paint.setStyle(Paint.Style.FILL);

            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(Color.WHITE);
            paint.setTextSize(Math.max(22f * scale, min * 0.032f));
            String title = mode == MODE_OPEN_WORLD ? "AÇIK DÜNYA KAPALI" : "RAYCAR YÜKLENİYOR";
            canvas.drawText(title, panel.centerX(), panel.top + panelH * 0.28f, paint);

            paint.setColor(Color.argb(232, 220, 238, 255));
            paint.setTextSize(Math.max(12f * scale, min * 0.017f));
            canvas.drawText(status, panel.centerX(), panel.top + panelH * 0.47f, paint);

            paint.setColor(Color.argb(220, 130, 235, 255));
            paint.setTextSize(Math.max(11f * scale, min * 0.015f));
            canvas.drawText(hint, panel.centerX(), panel.top + panelH * 0.64f, paint);

            float barW = panelW * 0.78f;
            float barH = Math.max(10f * scale, panelH * 0.075f);
            float barLeft = panel.centerX() - barW * 0.5f;
            float barTop = panel.bottom - panelH * 0.19f;
            barBg.set(barLeft, barTop, barLeft + barW, barTop + barH);
            barFg.set(barLeft, barTop, barLeft + barW * progress, barTop + barH);

            paint.setColor(Color.argb(160, 70, 78, 92));
            canvas.drawRoundRect(barBg, barH * 0.5f, barH * 0.5f, paint);
            paint.setColor(Color.argb(240, 0, 205, 255));
            canvas.drawRoundRect(barFg, barH * 0.5f, barH * 0.5f, paint);

            paint.setColor(Color.WHITE);
            paint.setTextSize(Math.max(10f * scale, min * 0.014f));
            canvas.drawText((int) (progress * 100f) + "%", barBg.right + 28f * scale, barBg.bottom, paint);
        }
    }
}
