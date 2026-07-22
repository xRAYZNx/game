package com.arabaoyunu;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.arabaoyunu.audio.GameAudioManager;
import com.arabaoyunu.core.GameSurfaceView;
import com.arabaoyunu.input.TouchControlsView;
import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.ui.HudView;
import com.arabaoyunu.ui.LoadingVideoOverlayView;
import com.arabaoyunu.ui.MenuOverlayView;
import com.arabaoyunu.util.GameLog;

public final class MainActivity extends Activity {

    private GameSurfaceView gameSurfaceView;
    private TouchControlsView touchControlsView;
    private HudView hudView;
    private MenuOverlayView menuOverlayView;
    private LoadingVideoOverlayView loadingOverlayView;
    private GameScreenState screenState;
    private GameAudioManager audioManager;

    private final Handler loadingHandler = new Handler(Looper.getMainLooper());
    private boolean introLoadingActive;
    private boolean openWorldLoadingActive;
    private long loadingStartMs;

    private static final String[] INTRO_HINTS = new String[] {
            "Motor yükseltmeleri hızlanmayı artırır.",
            "Fren geliştirmesi fren testlerinde avantaj sağlar.",
            "Modifiye ettiğin araç sürüşte aynı ayarlarla kullanılır.",
            "Garajdaki seçili araç, sürüşe aynı ayarlarla çıkar."
    };

    private static final String[] OPEN_WORLD_HINTS = new String[] {
            "Açık Dünya haritası geçici olarak kaldırıldı.",
            "Geliştirme şimdilik Açık Test Alanı ve dahili haritalar üzerinden devam eder.",
            "Harita slotu pasif olduğu için oyun çökmeden modlara döner."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUi();

        screenState = new GameScreenState();
        audioManager = new GameAudioManager(this);
        audioManager.start();
        touchControlsView = new TouchControlsView(this);
        touchControlsView.setAudioManager(audioManager);
        touchControlsView.setActionListener(new TouchControlsView.ActionListener() {
            @Override
            public void onReturnToMainMenuRequested() {
                returnToMainMenuFromDrive();
            }

            @Override
            public void onRespawnRequested() {
                requestRandomRespawn();
            }
        });
        hudView = new HudView(this);
        gameSurfaceView = new GameSurfaceView(this, touchControlsView, hudView, screenState);
        gameSurfaceView.getGameRenderer().setAudioManager(audioManager);
        gameSurfaceView.getGameRenderer().setUiBridge(new com.arabaoyunu.core.GameRenderer.UiBridge() {
            @Override
            public void onScreenChangeRequested(final int screen) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        applyScreenVisibility(screen);
                        if (menuOverlayView != null) menuOverlayView.invalidate();
                    }
                });
            }

            @Override
            public void onOpenWorldPrepared(final boolean success, final String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finishOpenWorldLoading(success, message);
                    }
                });
            }
        });
        menuOverlayView = new MenuOverlayView(this, screenState);
        menuOverlayView.setAudioManager(audioManager);
        menuOverlayView.setListener(new MenuOverlayView.Listener() {
            @Override
            public void onScreenChanged(int screen) {
                applyScreenVisibility(screen);
            }

            @Override
            public void onOpenWorldLoadingRequested() {
                requestOpenWorldLoading();
            }
        });
        loadingOverlayView = new LoadingVideoOverlayView(this);

        FrameLayout root = new FrameLayout(this);
        root.addView(gameSurfaceView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(hudView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(touchControlsView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(menuOverlayView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));
        root.addView(loadingOverlayView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        applyScreenVisibility(screenState.getScreen());

        setContentView(root);
        root.post(new Runnable() {
            @Override
            public void run() {
                showIntroLoading();
            }
        });
        GameLog.i("MainActivity", "ArabaOyunu_61_4 video intro + acik dunya loading aktif");
    }

    private void showIntroLoading() {
        if (loadingOverlayView == null || openWorldLoadingActive) return;
        introLoadingActive = true;
        loadingStartMs = System.currentTimeMillis();
        loadingOverlayView.showIntro();
        tickIntroLoading();
    }

    private void tickIntroLoading() {
        if (!introLoadingActive || loadingOverlayView == null) return;
        long elapsed = System.currentTimeMillis() - loadingStartMs;
        float progress = Math.min(1f, elapsed / 4500f);
        String status;
        if (progress < 0.25f) status = "Dosyalar hazırlanıyor";
        else if (progress < 0.55f) status = "Araçlar yükleniyor";
        else if (progress < 0.82f) status = "Garaj hazırlanıyor";
        else status = "Menü açılıyor";
        String hint = INTRO_HINTS[(int) ((elapsed / 1300L) % INTRO_HINTS.length)];
        loadingOverlayView.setLoadingState(progress, status, hint);
        if (progress >= 1f) {
            introLoadingActive = false;
            loadingOverlayView.hideOverlay();
            hideSystemUi();
            return;
        }
        loadingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tickIntroLoading();
            }
        }, 85L);
    }

    private void requestOpenWorldLoading() {
        if (openWorldLoadingActive) return;
        introLoadingActive = false;
        openWorldLoadingActive = true;
        loadingStartMs = System.currentTimeMillis();
        if (loadingOverlayView != null) {
            loadingOverlayView.showOpenWorld();
            loadingOverlayView.setLoadingState(1f, "Açık Dünya kapalı", OPEN_WORLD_HINTS[0]);
        }
        if (screenState != null) {
            screenState.setSelectedMode(GameScreenState.MODE_FREE_DRIVE);
            screenState.setSelectedMap(GameScreenState.MAP_OPEN_FIELD);
        }
        finishOpenWorldLoading(false, "Açık Dünya haritası kaldırıldı; Açık Test Alanı kullanılacak.");
    }

    private void tickOpenWorldLoading() {
        if (!openWorldLoadingActive || loadingOverlayView == null) return;
        long elapsed = System.currentTimeMillis() - loadingStartMs;
        float simulated = Math.min(0.92f, 0.03f + elapsed / 11000f);
        String status;
        if (simulated < 0.22f) status = "Harita slotu kontrol ediliyor";
        else if (simulated < 0.44f) status = "Açık Dünya geçici kapalı";
        else if (simulated < 0.66f) status = "Açık Test Alanı seçiliyor";
        else if (simulated < 0.84f) status = "Güvenli dönüş hazırlanıyor";
        else status = "Modlar ekranına dönülüyor";
        String hint = OPEN_WORLD_HINTS[(int) ((elapsed / 1800L) % OPEN_WORLD_HINTS.length)];
        if (loadingOverlayView.getProgress() < simulated) {
            loadingOverlayView.setLoadingState(simulated, status, hint);
        }
        loadingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tickOpenWorldLoading();
            }
        }, 120L);
    }

    private void finishOpenWorldLoading(final boolean success, String message) {
        if (!openWorldLoadingActive) return;
        openWorldLoadingActive = false;
        if (loadingOverlayView != null) {
            loadingOverlayView.setLoadingState(
                    1f,
                    success ? "Açık Test Alanı hazır" : "Açık Dünya kapalı",
                    success ? "Sürüş başlatılıyor." : (message == null ? "Modlar ekranına dönülüyor." : message));
        }
        loadingHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (loadingOverlayView != null) loadingOverlayView.hideOverlay();
                if (screenState != null) {
                    if (success) {
                        screenState.setSelectedMode(GameScreenState.MODE_FREE_DRIVE);
                        screenState.setSelectedMap(GameScreenState.MAP_OPEN_FIELD);
                        screenState.setScreen(GameScreenState.SCREEN_DRIVE);
                        applyScreenVisibility(GameScreenState.SCREEN_DRIVE);
                    } else {
                        screenState.setSelectedMode(GameScreenState.MODE_FREE_DRIVE);
                        screenState.setSelectedMap(GameScreenState.MAP_OPEN_FIELD);
                        screenState.setScreen(GameScreenState.SCREEN_MODES);
                        applyScreenVisibility(GameScreenState.SCREEN_MODES);
                    }
                }
                if (menuOverlayView != null) menuOverlayView.invalidate();
                hideSystemUi();
            }
        }, success ? 420L : 1200L);
    }

    private void returnToMainMenuFromDrive() {
        if (audioManager != null) {
            audioManager.playBack();
            audioManager.setDriving(false);
        }
        int targetScreen = GameScreenState.SCREEN_MAIN_MENU;
        if (screenState != null) {
            if (screenState.isTestDriveSessionActive()) {
                screenState.endTestDriveSession();
                targetScreen = GameScreenState.SCREEN_GARAGE;
            }
            screenState.setScreen(targetScreen);
        }
        if (touchControlsView != null) {
            touchControlsView.closeSettingsPanel();
        }
        applyScreenVisibility(targetScreen);
        if (menuOverlayView != null) {
            menuOverlayView.invalidate();
        }
        GameLog.i("MainActivity", "ArabaOyunu_61_4 ana menuye donuldu");
    }

    private void requestRandomRespawn() {
        if (audioManager != null) audioManager.playRespawn();
        if (gameSurfaceView != null && gameSurfaceView.getGameRenderer() != null) {
            gameSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    gameSurfaceView.getGameRenderer().requestRandomRespawn();
                }
            });
        }
        if (touchControlsView != null) {
            touchControlsView.closeSettingsPanel();
        }
        GameLog.i("MainActivity", "ArabaOyunu_61_4 respawn istendi");
    }

    private void applyScreenVisibility(int screen) {
        boolean driving = screen == GameScreenState.SCREEN_DRIVE;
        if (audioManager != null) {
            audioManager.setDriving(driving);
            if (!driving) audioManager.pause();
            else audioManager.resume();
        }
        if (touchControlsView != null) touchControlsView.setVisibility(driving ? View.VISIBLE : View.GONE);
        if (hudView != null) hudView.setVisibility(driving ? View.VISIBLE : View.GONE);
        if (menuOverlayView != null) menuOverlayView.setVisibility(driving ? View.GONE : View.VISIBLE);
        if (loadingOverlayView != null && loadingOverlayView.getVisibility() == View.VISIBLE) {
            loadingOverlayView.bringToFront();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if (audioManager != null) {
            audioManager.resume();
            audioManager.setDriving(screenState != null && screenState.isDriving());
        }
        if (gameSurfaceView != null) {
            gameSurfaceView.onResume();
        }
    }

    @Override
    protected void onPause() {
        introLoadingActive = false;
        openWorldLoadingActive = false;
        loadingHandler.removeCallbacksAndMessages(null);
        if (loadingOverlayView != null) loadingOverlayView.hideOverlay();
        if (audioManager != null) audioManager.pause();
        if (gameSurfaceView != null) {
            gameSurfaceView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        loadingHandler.removeCallbacksAndMessages(null);
        if (loadingOverlayView != null) loadingOverlayView.hideOverlay();
        if (audioManager != null) {
            audioManager.stop();
        }
        super.onDestroy();
    }

    private void hideSystemUi() {
        View decor = getWindow().getDecorView();
        decor.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
