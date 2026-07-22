package com.arabaoyunu.input;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Process;
import android.view.MotionEvent;
import android.view.View;

import com.arabaoyunu.audio.GameAudioManager;
import com.arabaoyunu.traffic.TrafficSystem;
import com.arabaoyunu.ui.DrivingHudLayoutSystem;

/**
 * ArabaOyunu_10 kompakt oyun kontrol katmani.
 * - Butonlar buyuk kutu yerine kucuk yuvarlak/opak oyun butonu stilindedir.
 * - Sol taraf: yon/direksiyon.
 * - Sag taraf: gaz/fren/el/nitro/kamera/ayna.
 * - Ayarlar: kontrol tipi + grafik kalitesi.
 */
public final class TouchControlsView extends View {

    public interface ActionListener {
        void onReturnToMainMenuRequested();
        void onRespawnRequested();
    }


    public static final int CONTROL_BUTTONS = 0;
    public static final int CONTROL_STEERING_WHEEL = 1;

    public static final int QUALITY_LOW = 0;
    public static final int QUALITY_MEDIUM = 1;
    public static final int QUALITY_HIGH = 2;
    public static final int QUALITY_ULTRA = 3;

    private static final String PREFS = "araba_oyunu_controls";
    private static final String KEY_CONTROL_MODE = "control_mode";
    private static final String KEY_GRAPHICS_QUALITY = "graphics_quality";
    private static final String KEY_TRAFFIC_DENSITY = "traffic_density";

    // Fizik yönü önceki çalışan sürümdeki gibi korunur; sol/sağ eşitlik VehicleController'da sağlanır.
    private static final float STEER_FIX_MULTIPLIER = -1f;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final InputState state = new InputState();
    private final InputState snapshot = new InputState();
    private final SharedPreferences prefs;
    private final ControlLayoutSystem controlLayout;
    private ActionListener actionListener;
    private GameAudioManager audioManager;

    private final RectF leftButton = new RectF();
    private final RectF rightButton = new RectF();
    private final RectF steeringWheel = new RectF();

    private final RectF gasButton = new RectF();
    private final RectF brakeButton = new RectF();
    private final RectF handbrakeButton = new RectF();
    private final RectF nitroButton = new RectF();
    private final RectF cameraButton = new RectF();
    private final RectF interactButton = new RectF();
    private final RectF pauseButton = new RectF();
    private final RectF miniMapTouchArea = new RectF();
    private final RectF expandedMapPanel = new RectF();
    private final RectF expandedMapCloseButton = new RectF();

    private final RectF settingsButton = new RectF();
    private final RectF settingsPanel = new RectF();
    private final RectF buttonModeOption = new RectF();
    private final RectF wheelModeOption = new RectF();

    private final RectF lowOption = new RectF();
    private final RectF mediumOption = new RectF();
    private final RectF highOption = new RectF();
    private final RectF ultraOption = new RectF();
    private final RectF saveGraphicsOption = new RectF();
    private final RectF respawnOption = new RectF();
    private final RectF cameraModeOption = new RectF();
    private final RectF trafficOption = new RectF();
    private final RectF returnMenuOption = new RectF();
    private final RectF closeSettingsOption = new RectF();
    private final RectF layoutPresetOption = new RectF();
    private final RectF sensitivityPresetOption = new RectF();
    private final RectF pedalSizeOption = new RectF();
    private final RectF hudPresetOption = new RectF();
    private final RectF opacityOption = new RectF();
    private final RectF leftHandedOption = new RectF();
    private final RectF autoControlOption = new RectF();

    private final RectF leftMirrorButton = new RectF();
    private final RectF rightMirrorButton = new RectF();
    private final RectF headlightButton = new RectF();
    private final RectF hazardButton = new RectF();
    private final RectF leftSignalButton = new RectF();
    private final RectF rightSignalButton = new RectF();

    private int lastWidth;
    private int lastHeight;

    private int controlMode;
    private int graphicsQuality;
    private int pendingGraphicsQuality;
    private int trafficDensity;
    private boolean settingsOpen;
    private boolean leftMirrorOpen;
    private boolean rightMirrorOpen;
    private boolean headlightsOn = true;
    private boolean hazardOn;
    private boolean leftSignalOn;
    private boolean rightSignalOn;
    private boolean mapOverlayOpen;

    private float wheelVisualSteer;
    private boolean cameraDragging;
    private float dragLastX;
    private float dragLastY;

    public TouchControlsView(Context context) {
        super(context);
        setFocusable(true);
        paint.setTextAlign(Paint.Align.CENTER);
        prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        controlLayout = new ControlLayoutSystem(context);
        controlMode = prefs.getInt(KEY_CONTROL_MODE, CONTROL_BUTTONS);
        graphicsQuality = prefs.getInt(KEY_GRAPHICS_QUALITY, QUALITY_HIGH);
        pendingGraphicsQuality = graphicsQuality;
        trafficDensity = prefs.getInt(KEY_TRAFFIC_DENSITY, TrafficSystem.DENSITY_MEDIUM);
    }

    public synchronized void setActionListener(ActionListener listener) {
        this.actionListener = listener;
    }

    public synchronized void setAudioManager(GameAudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public synchronized void closeSettingsPanel() {
        settingsOpen = false;
        invalidate();
    }

    public synchronized InputState snapshotInput() {
        state.leftMirrorOpen = leftMirrorOpen;
        state.rightMirrorOpen = rightMirrorOpen;
        state.headlightsOn = headlightsOn;
        state.hazardOn = hazardOn;
        state.leftSignalOn = leftSignalOn;
        state.rightSignalOn = rightSignalOn;
        state.controlMode = controlMode;
        state.graphicsQuality = graphicsQuality;
        state.trafficDensity = trafficDensity;
        state.controlLayoutPreset = controlLayout.getLayoutPreset();
        state.steeringSensitivityPreset = controlLayout.getSensitivityPreset();
        state.pedalSizePreset = controlLayout.getPedalSizePreset();
        state.hudPreset = controlLayout.getHudPreset();
        state.buttonOpacityPercent = controlLayout.getButtonOpacityPercent();
        state.leftHandedMode = controlLayout.isLeftHandedMode();
        state.autoControlByMode = controlLayout.isAutoByMode();
        state.mapOverlayOpen = mapOverlayOpen;
        snapshot.set(state);
        state.clearMomentary();
        return snapshot;
    }

    public synchronized int getGraphicsQuality() {
        return graphicsQuality;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        lastWidth = w;
        lastHeight = h;
        layoutControls(w, h);
    }

    private void layoutControls(int w, int h) {
        float min = Math.min(w, h);
        float hudScale = DrivingHudLayoutSystem.scaleFor(w, h);
        miniMapTouchArea.set(DrivingHudLayoutSystem.miniMapTouchRect(w, h, hudScale));
        expandedMapPanel.set(DrivingHudLayoutSystem.expandedMapPanelRect(w, h, hudScale));
        expandedMapCloseButton.set(DrivingHudLayoutSystem.expandedMapCloseRect(w, h, hudScale));
        float baseUnit = Math.max(50f, min * 0.095f);
        float pedalUnit = baseUnit * controlLayout.pedalScale();
        float steerUnit = baseUnit * controlLayout.steeringScale();
        float unit = baseUnit;
        float pad = Math.max(10f, min * 0.020f);
        float bottom = h - pad;
        boolean steeringRight = controlLayout.steeringOnRight();

        // A63.3: Kontrol şablonu. Klasik: direksiyon solda, pedallar sağda.
        // Solak/sağ direksiyon düzeninde bu bölgeler güvenli şekilde ters çevrilir.
        float steerLeft = steeringRight ? w - pad - steerUnit * 2.12f : pad;
        leftButton.set(steerLeft, bottom - steerUnit, steerLeft + steerUnit, bottom);
        rightButton.set(steerLeft + steerUnit * 1.12f, bottom - steerUnit, steerLeft + steerUnit * 2.12f, bottom);

        float wheelSize = steerUnit * 2.05f;
        steeringWheel.set(steeringRight ? w - pad - wheelSize : pad, bottom - wheelSize,
                steeringRight ? w - pad : pad + wheelSize, bottom);

        float pedalRight = steeringRight ? pad + pedalUnit * 3.20f : w - pad;
        gasButton.set(pedalRight - pedalUnit, bottom - pedalUnit, pedalRight, bottom);
        brakeButton.set(pedalRight - pedalUnit * 2.10f, bottom - pedalUnit, pedalRight - pedalUnit * 1.10f, bottom);
        handbrakeButton.set(pedalRight - pedalUnit * 3.20f, bottom - pedalUnit, pedalRight - pedalUnit * 2.20f, bottom);

        nitroButton.set(pedalRight - pedalUnit, bottom - pedalUnit * 2.10f, pedalRight, bottom - pedalUnit * 1.10f);
        cameraButton.set(pedalRight - pedalUnit * 2.10f, bottom - pedalUnit * 2.10f, pedalRight - pedalUnit * 1.10f, bottom - pedalUnit * 1.10f);
        interactButton.set(pedalRight - pedalUnit * 3.20f, bottom - pedalUnit * 2.10f, pedalRight - pedalUnit * 2.20f, bottom - pedalUnit * 1.10f);

        float mirrorSize = Math.max(36f, min * 0.052f);
        float mirrorY = pad + Math.max(48f, min * 0.082f);
        leftMirrorButton.set(pad, mirrorY, pad + mirrorSize * 1.28f, mirrorY + mirrorSize);
        rightMirrorButton.set(w - pad - mirrorSize * 1.28f, mirrorY, w - pad, mirrorY + mirrorSize);

        float lightW = Math.max(48f, min * 0.070f);
        float lightH = Math.max(34f, min * 0.050f);
        float lightGap = Math.max(7f, min * 0.012f);
        float lightTop = mirrorY + mirrorSize + lightGap;
        float lightRight = w - pad;
        rightSignalButton.set(lightRight - lightW, lightTop, lightRight, lightTop + lightH);
        leftSignalButton.set(rightSignalButton.left - lightGap - lightW, lightTop, rightSignalButton.left - lightGap, lightTop + lightH);
        hazardButton.set(leftSignalButton.left - lightGap - lightW, lightTop, leftSignalButton.left - lightGap, lightTop + lightH);
        headlightButton.set(hazardButton.left - lightGap - lightW, lightTop, hazardButton.left - lightGap, lightTop + lightH);

        float settingsSize = Math.max(44f, min * 0.070f);
        settingsButton.set(w * 0.5f - settingsSize * 0.5f, pad, w * 0.5f + settingsSize * 0.5f, pad + settingsSize);
        pauseButton.set(pad, pad, pad + settingsSize * 0.82f, pad + settingsSize * 0.82f);

        float panelW = Math.min(w * 0.94f, unit * 10.2f);
        float panelMaxH = Math.max(unit * 5.7f, h - settingsButton.bottom - pad * 1.45f);
        float panelH = Math.min(panelMaxH, unit * 8.35f);
        settingsPanel.set(w * 0.5f - panelW * 0.5f, settingsButton.bottom + pad * 0.7f,
                w * 0.5f + panelW * 0.5f, settingsButton.bottom + pad * 0.7f + panelH);

        float inner = pad * 1.05f;
        float gap = inner * 0.60f;
        float rowH = Math.max(28f, (settingsPanel.height() - inner * 2.3f - gap * 5.4f) / 6f);
        float closeW = Math.max(72f, rowH * 1.95f);
        closeSettingsOption.set(settingsPanel.right - inner - closeW, settingsPanel.top + inner * 0.55f,
                settingsPanel.right - inner, settingsPanel.top + inner * 0.55f + rowH * 0.70f);

        float contentLeft = settingsPanel.left + inner;
        float contentRight = settingsPanel.right - inner;
        float top = settingsPanel.top + inner;
        float halfW = (contentRight - contentLeft - gap) * 0.5f;
        buttonModeOption.set(contentLeft, top, contentLeft + halfW, top + rowH);
        wheelModeOption.set(buttonModeOption.right + gap, top, contentRight, top + rowH);

        float qTop = buttonModeOption.bottom + gap;
        float qW = (contentRight - contentLeft - gap * 3f) / 4f;
        lowOption.set(contentLeft, qTop, contentLeft + qW, qTop + rowH * 0.88f);
        mediumOption.set(lowOption.right + gap, qTop, lowOption.right + gap + qW, qTop + rowH * 0.88f);
        highOption.set(mediumOption.right + gap, qTop, mediumOption.right + gap + qW, qTop + rowH * 0.88f);
        ultraOption.set(highOption.right + gap, qTop, contentRight, qTop + rowH * 0.88f);

        float saveTop = ultraOption.bottom + gap * 0.72f;
        saveGraphicsOption.set(contentLeft, saveTop, contentRight, saveTop + rowH * 0.72f);

        float cTop = saveGraphicsOption.bottom + gap * 0.72f;
        float thirdW = (contentRight - contentLeft - gap * 2f) / 3f;
        layoutPresetOption.set(contentLeft, cTop, contentLeft + thirdW, cTop + rowH * 0.88f);
        sensitivityPresetOption.set(layoutPresetOption.right + gap, cTop, layoutPresetOption.right + gap + thirdW, cTop + rowH * 0.88f);
        pedalSizeOption.set(sensitivityPresetOption.right + gap, cTop, contentRight, cTop + rowH * 0.88f);

        float hTop = layoutPresetOption.bottom + gap * 0.72f;
        hudPresetOption.set(contentLeft, hTop, contentLeft + thirdW, hTop + rowH * 0.88f);
        opacityOption.set(hudPresetOption.right + gap, hTop, hudPresetOption.right + gap + thirdW, hTop + rowH * 0.88f);
        leftHandedOption.set(opacityOption.right + gap, hTop, contentRight, hTop + rowH * 0.88f);

        float aTop = hudPresetOption.bottom + gap * 0.72f;
        autoControlOption.set(contentLeft, aTop, contentLeft + thirdW, aTop + rowH * 0.82f);
        float actionW = (contentRight - autoControlOption.right - gap * 4f) / 4f;
        respawnOption.set(autoControlOption.right + gap, aTop, autoControlOption.right + gap + actionW, aTop + rowH * 0.82f);
        cameraModeOption.set(respawnOption.right + gap, aTop, respawnOption.right + gap + actionW, aTop + rowH * 0.82f);
        trafficOption.set(cameraModeOption.right + gap, aTop, cameraModeOption.right + gap + actionW, aTop + rowH * 0.82f);
        returnMenuOption.set(trafficOption.right + gap, aTop, contentRight, aTop + rowH * 0.82f);
    }

    private void playUiClick() {
        if (audioManager != null) audioManager.playMenuClick();
    }

    private void playUiBack() {
        if (audioManager != null) audioManager.playBack();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        synchronized (this) {
            state.throttle = 0f;
            state.brake = 0f;
            state.steer = 0f;
            state.visualWheelSteer = 0f;
            state.handbrake = 0f;
            state.nitro = 0f;
            wheelVisualSteer = 0f;

            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                cameraDragging = false;
                invalidate();
                return true;
            }

            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                int index = event.getActionIndex();
                float x = event.getX(index);
                float y = event.getY(index);

                // A67.4: Mini haritaya dokununca büyük oyun içi harita aç/kapatılır.
                // Büyük harita açıkken sürüş butonları yanlışlıkla çalışmaz.
                if (mapOverlayOpen) {
                    if (expandedMapCloseButton.contains(x, y) || pauseButton.contains(x, y)) {
                        playUiBack();
                        mapOverlayOpen = false;
                        state.mapOverlayOpen = false;
                        invalidate();
                        return true;
                    }
                    if (miniMapTouchArea.contains(x, y)) {
                        playUiBack();
                        mapOverlayOpen = false;
                        state.mapOverlayOpen = false;
                        invalidate();
                        return true;
                    }
                    if (!expandedMapPanel.contains(x, y)) {
                        playUiBack();
                        mapOverlayOpen = false;
                        state.mapOverlayOpen = false;
                        invalidate();
                        return true;
                    }
                    invalidate();
                    return true;
                }

                if (miniMapTouchArea.contains(x, y)) {
                    playUiClick();
                    mapOverlayOpen = true;
                    state.mapOverlayOpen = true;
                    state.mapOverlayTogglePressed = true;
                    cameraDragging = false;
                    invalidate();
                    return true;
                }

                if (settingsButton.contains(x, y)) {
                    playUiClick();
                    settingsOpen = !settingsOpen;
                    invalidate();
                    return true;
                }

                if (settingsOpen) {
                    if (closeSettingsOption.contains(x, y)) {
                        playUiBack();
                        settingsOpen = false;
                        invalidate();
                        return true;
                    }
                    if (buttonModeOption.contains(x, y)) {
                        playUiClick();
                        setControlMode(CONTROL_BUTTONS);
                        settingsOpen = false;
                        invalidate();
                        return true;
                    }
                    if (wheelModeOption.contains(x, y)) {
                        playUiClick();
                        setControlMode(CONTROL_STEERING_WHEEL);
                        settingsOpen = false;
                        invalidate();
                        return true;
                    }
                    if (lowOption.contains(x, y)) {
                        playUiClick();
                        selectPendingGraphicsQuality(QUALITY_LOW);
                        invalidate();
                        return true;
                    }
                    if (mediumOption.contains(x, y)) {
                        playUiClick();
                        selectPendingGraphicsQuality(QUALITY_MEDIUM);
                        invalidate();
                        return true;
                    }
                    if (highOption.contains(x, y)) {
                        playUiClick();
                        selectPendingGraphicsQuality(QUALITY_HIGH);
                        invalidate();
                        return true;
                    }
                    if (ultraOption.contains(x, y)) {
                        playUiClick();
                        selectPendingGraphicsQuality(QUALITY_ULTRA);
                        invalidate();
                        return true;
                    }
                    if (saveGraphicsOption.contains(x, y)) {
                        playUiClick();
                        saveGraphicsAndRestart();
                        return true;
                    }
                    if (layoutPresetOption.contains(x, y)) {
                        playUiClick();
                        controlLayout.cycleLayoutPreset();
                        layoutControls(getWidth(), getHeight());
                        invalidate();
                        return true;
                    }
                    if (sensitivityPresetOption.contains(x, y)) {
                        playUiClick();
                        controlLayout.cycleSensitivityPreset();
                        invalidate();
                        return true;
                    }
                    if (pedalSizeOption.contains(x, y)) {
                        playUiClick();
                        controlLayout.cyclePedalSizePreset();
                        layoutControls(getWidth(), getHeight());
                        invalidate();
                        return true;
                    }
                    if (hudPresetOption.contains(x, y)) {
                        playUiClick();
                        controlLayout.cycleHudPreset();
                        invalidate();
                        return true;
                    }
                    if (opacityOption.contains(x, y)) {
                        playUiClick();
                        controlLayout.cycleButtonOpacity();
                        invalidate();
                        return true;
                    }
                    if (leftHandedOption.contains(x, y)) {
                        playUiClick();
                        controlLayout.toggleLeftHandedMode();
                        layoutControls(getWidth(), getHeight());
                        invalidate();
                        return true;
                    }
                    if (autoControlOption.contains(x, y)) {
                        playUiClick();
                        controlLayout.toggleAutoByMode();
                        invalidate();
                        return true;
                    }
                    if (respawnOption.contains(x, y)) {
                        playUiClick();
                        settingsOpen = false;
                        if (actionListener != null) actionListener.onRespawnRequested();
                        invalidate();
                        return true;
                    }
                    if (cameraModeOption.contains(x, y)) {
                        playUiClick();
                        state.cameraSwitchPressed = true;
                        settingsOpen = false;
                        invalidate();
                        return true;
                    }
                    if (trafficOption.contains(x, y)) {
                        playUiClick();
                        trafficDensity = (trafficDensity + 1) % 4;
                        prefs.edit().putInt(KEY_TRAFFIC_DENSITY, trafficDensity).apply();
                        invalidate();
                        return true;
                    }
                    if (returnMenuOption.contains(x, y)) {
                        playUiBack();
                        settingsOpen = false;
                        if (actionListener != null) actionListener.onReturnToMainMenuRequested();
                        invalidate();
                        return true;
                    }
                    if (!settingsPanel.contains(x, y)) {
                        settingsOpen = false;
                    } else {
                        invalidate();
                        return true;
                    }
                }

                if (cameraButton.contains(x, y)) {
                    playUiClick();
                    state.cameraSwitchPressed = true;
                }
                if (interactButton.contains(x, y)) {
                    playUiClick();
                    state.interactPressed = true;
                }
                if (pauseButton.contains(x, y)) {
                    playUiClick();
                    state.pausePressed = true;
                }

                if (leftMirrorButton.contains(x, y)) {
                    playUiClick();
                    leftMirrorOpen = !leftMirrorOpen;
                    invalidate();
                    return true;
                }
                if (rightMirrorButton.contains(x, y)) {
                    playUiClick();
                    rightMirrorOpen = !rightMirrorOpen;
                    invalidate();
                    return true;
                }
                if (headlightButton.contains(x, y)) {
                    playUiClick();
                    headlightsOn = !headlightsOn;
                    invalidate();
                    return true;
                }
                if (hazardButton.contains(x, y)) {
                    playUiClick();
                    hazardOn = !hazardOn;
                    if (hazardOn) {
                        leftSignalOn = false;
                        rightSignalOn = false;
                    }
                    invalidate();
                    return true;
                }
                if (leftSignalButton.contains(x, y)) {
                    playUiClick();
                    leftSignalOn = !leftSignalOn;
                    if (leftSignalOn) {
                        rightSignalOn = false;
                        hazardOn = false;
                    }
                    invalidate();
                    return true;
                }
                if (rightSignalButton.contains(x, y)) {
                    playUiClick();
                    rightSignalOn = !rightSignalOn;
                    if (rightSignalOn) {
                        leftSignalOn = false;
                        hazardOn = false;
                    }
                    invalidate();
                    return true;
                }

                if (!isAnyControlHit(x, y)) {
                    cameraDragging = true;
                    dragLastX = x;
                    dragLastY = y;
                } else {
                    cameraDragging = false;
                }
            }

            if (action == MotionEvent.ACTION_MOVE && cameraDragging && event.getPointerCount() > 0) {
                float x = event.getX(0);
                float y = event.getY(0);
                state.cameraDragX += x - dragLastX;
                state.cameraDragY += y - dragLastY;
                dragLastX = x;
                dragLastY = y;
            }

            if (!settingsOpen) {
                int releasedIndex = action == MotionEvent.ACTION_POINTER_UP ? event.getActionIndex() : -1;
                int count = event.getPointerCount();
                boolean leftPressed = false;
                boolean rightPressed = false;
                boolean wheelTouched = false;
                float wheelSteer = 0f;

                for (int i = 0; i < count; i++) {
                    if (i == releasedIndex) continue;
                    float x = event.getX(i);
                    float y = event.getY(i);

                    if (gasButton.contains(x, y)) state.throttle = Math.max(state.throttle, controlPressureForButton(x, y, gasButton, 0.66f));
                    if (brakeButton.contains(x, y)) state.brake = Math.max(state.brake, controlPressureForButton(x, y, brakeButton, 0.70f));
                    if (handbrakeButton.contains(x, y)) state.handbrake = Math.max(state.handbrake, controlPressureForButton(x, y, handbrakeButton, 0.74f));
                    if (nitroButton.contains(x, y)) state.nitro = Math.max(state.nitro, controlPressureForButton(x, y, nitroButton, 0.76f));

                    if (controlMode == CONTROL_STEERING_WHEEL) {
                        if (steeringWheel.contains(x, y)) {
                            float radius = Math.max(1f, steeringWheel.width() * 0.5f);
                            wheelSteer += clamp((x - steeringWheel.centerX()) / radius, -1f, 1f);
                            wheelTouched = true;
                        }
                    } else {
                        if (leftButton.contains(x, y)) leftPressed = true;
                        if (rightButton.contains(x, y)) rightPressed = true;
                    }
                }

                float visualSteer;
                if (controlMode == CONTROL_STEERING_WHEEL && wheelTouched) {
                    visualSteer = clamp(wheelSteer, -1f, 1f);
                } else if (leftPressed && !rightPressed) {
                    visualSteer = -1f;
                } else if (rightPressed && !leftPressed) {
                    visualSteer = 1f;
                } else {
                    visualSteer = 0f;
                }

                // ArabaOyunu_16:
                // Sol ve sag teker gorsel tepkisi dogrudan butondan gelir.
                // Sol=-1, Sag=+1; gecikme/fixed-step/multiplier zinciri yok.
                wheelVisualSteer = visualSteer;
                state.visualWheelSteer = visualSteer;

                // Fizik surus yonu onceki calisan isaretle korunur ama iki taraf da ayni hizdadir.
                state.steer = controlLayout.applySteering(clamp(visualSteer * STEER_FIX_MULTIPLIER, -1f, 1f));
            }
        }
        invalidate();
        return true;
    }


    private boolean isAnyControlHit(float x, float y) {
        if (settingsButton.contains(x, y) || pauseButton.contains(x, y) || miniMapTouchArea.contains(x, y)) return true;
        if (mapOverlayOpen && (expandedMapPanel.contains(x, y) || expandedMapCloseButton.contains(x, y))) return true;
        if (settingsOpen && (settingsPanel.contains(x, y) || respawnOption.contains(x, y) || returnMenuOption.contains(x, y)
                || closeSettingsOption.contains(x, y) || layoutPresetOption.contains(x, y) || sensitivityPresetOption.contains(x, y)
                || pedalSizeOption.contains(x, y) || hudPresetOption.contains(x, y) || opacityOption.contains(x, y)
                || leftHandedOption.contains(x, y) || autoControlOption.contains(x, y))) return true;
        if (gasButton.contains(x, y) || brakeButton.contains(x, y) || handbrakeButton.contains(x, y) || nitroButton.contains(x, y)) return true;
        if (cameraButton.contains(x, y) || interactButton.contains(x, y) || leftMirrorButton.contains(x, y) || rightMirrorButton.contains(x, y)) return true;
        if (headlightButton.contains(x, y) || hazardButton.contains(x, y) || leftSignalButton.contains(x, y) || rightSignalButton.contains(x, y)) return true;
        if (controlMode == CONTROL_STEERING_WHEEL) {
            return steeringWheel.contains(x, y);
        }
        return leftButton.contains(x, y) || rightButton.contains(x, y);
    }

    private void setControlMode(int mode) {
        controlMode = mode == CONTROL_STEERING_WHEEL ? CONTROL_STEERING_WHEEL : CONTROL_BUTTONS;
        prefs.edit().putInt(KEY_CONTROL_MODE, controlMode).apply();
    }

    private void selectPendingGraphicsQuality(int quality) {
        if (quality < QUALITY_LOW || quality > QUALITY_ULTRA) quality = QUALITY_HIGH;
        pendingGraphicsQuality = quality;
    }

    private void saveGraphicsAndRestart() {
        graphicsQuality = pendingGraphicsQuality;
        prefs.edit().putInt(KEY_GRAPHICS_QUALITY, graphicsQuality).apply();
        settingsOpen = false;

        hardRestartApp();
    }

    private void hardRestartApp() {
        Context c = getContext();
        Intent restartIntent = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
        if (restartIntent == null) {
            if (c instanceof Activity) {
                ((Activity) c).finish();
            }
            return;
        }

        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                c.getApplicationContext(),
                93014,
                restartIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        AlarmManager alarmManager = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 260, pendingIntent);
        } else {
            c.startActivity(restartIntent);
        }

        if (c instanceof Activity) {
            ((Activity) c).finishAffinity();
        }

        Process.killProcess(Process.myPid());
        System.exit(0);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lastWidth != getWidth() || lastHeight != getHeight()) {
            layoutControls(getWidth(), getHeight());
        }

        if (mapOverlayOpen) {
            drawMapOverlayTouchHint(canvas);
            return;
        }

        drawCompactButton(canvas, gasButton, "GAZ", state.throttle > 0.1f, true);
        drawCompactButton(canvas, brakeButton, "FREN", state.brake > 0.1f, true);
        drawCompactButton(canvas, handbrakeButton, "EL", state.handbrake > 0.1f, false);
        drawCompactButton(canvas, nitroButton, "N2O", state.nitro > 0.1f, true);
        drawCompactButton(canvas, cameraButton, "KAM", false, true);
        drawCompactButton(canvas, interactButton, "ETK", state.interactPressed, true);
        drawCompactButton(canvas, leftMirrorButton, "AYN L", leftMirrorOpen, false);
        drawCompactButton(canvas, rightMirrorButton, "AYN R", rightMirrorOpen, false);
        drawCompactButton(canvas, headlightButton, "FAR", headlightsOn, false);
        drawCompactButton(canvas, hazardButton, "4LU", hazardOn, false);
        drawCompactButton(canvas, leftSignalButton, "SIN L", leftSignalOn, false);
        drawCompactButton(canvas, rightSignalButton, "SIN R", rightSignalOn, false);

        if (controlMode == CONTROL_STEERING_WHEEL) {
            drawSteeringWheel(canvas);
        } else {
            drawCompactButton(canvas, leftButton, "←", wheelVisualSteer < -0.1f, true);
            drawCompactButton(canvas, rightButton, "→", wheelVisualSteer > 0.1f, true);
        }

        drawCompactButton(canvas, pauseButton, "Ⅱ", false, false);
        drawSettingsButton(canvas);

        if (settingsOpen) {
            drawSettingsPanel(canvas);
        }
    }

    private void drawSteeringWheel(Canvas canvas) {
        float cx = steeringWheel.centerX();
        float cy = steeringWheel.centerY();
        float radius = steeringWheel.width() * 0.5f;
        float angle = wheelVisualSteer * 58f;

        canvas.save();
        canvas.rotate(angle, cx, cy);

        // Dis lastik/deri halka.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(94, 8, 14, 20));
        canvas.drawCircle(cx, cy, radius, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(7f, radius * 0.105f));
        paint.setColor(Color.argb(225, 28, 34, 42));
        canvas.drawCircle(cx, cy, radius * 0.80f, paint);

        paint.setStrokeWidth(Math.max(2.5f, radius * 0.032f));
        paint.setColor(Color.argb(190, 0, 210, 255));
        canvas.drawCircle(cx, cy, radius * 0.81f, paint);
        paint.setColor(Color.argb(135, 255, 255, 255));
        canvas.drawCircle(cx, cy, radius * 0.64f, paint);

        // 3 kollu direksiyon: sol, sag ve alt kol.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(8f, radius * 0.105f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(Color.argb(230, 40, 47, 58));
        canvas.drawLine(cx - radius * 0.18f, cy, cx - radius * 0.58f, cy - radius * 0.18f, paint);
        canvas.drawLine(cx + radius * 0.18f, cy, cx + radius * 0.58f, cy - radius * 0.18f, paint);
        canvas.drawLine(cx, cy + radius * 0.18f, cx, cy + radius * 0.58f, paint);

        paint.setStrokeWidth(Math.max(2f, radius * 0.032f));
        paint.setColor(Color.argb(200, 0, 210, 255));
        canvas.drawLine(cx - radius * 0.18f, cy, cx - radius * 0.58f, cy - radius * 0.18f, paint);
        canvas.drawLine(cx + radius * 0.18f, cy, cx + radius * 0.58f, cy - radius * 0.18f, paint);
        canvas.drawLine(cx, cy + radius * 0.18f, cx, cy + radius * 0.58f, paint);

        // Orta gobek.
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(245, 24, 29, 38));
        canvas.drawCircle(cx, cy, radius * 0.25f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, radius * 0.026f));
        paint.setColor(Color.argb(190, 255, 255, 255));
        canvas.drawCircle(cx, cy, radius * 0.25f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(230, 235, 245, 255));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(9f, radius * 0.11f));
        Paint.FontMetrics fm = paint.getFontMetrics();
        canvas.drawText("DRIVE", cx, cy - (fm.ascent + fm.descent) * 0.5f, paint);

        paint.setStrokeCap(Paint.Cap.BUTT);
        canvas.restore();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(13f, radius * 0.13f));
        canvas.drawText("DIREKSIYON", cx, cy - radius - 7f, paint);
    }

    private void drawSettingsButton(Canvas canvas) {
        float cx = settingsButton.centerX();
        float cy = settingsButton.centerY();
        float radius = settingsButton.width() * 0.5f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(settingsOpen ? Color.argb(170, 0, 210, 255) : Color.argb(78, 255, 255, 255));
        canvas.drawCircle(cx, cy, radius, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2.2f, radius * 0.08f));
        paint.setColor(Color.argb(170, 255, 255, 255));
        canvas.drawCircle(cx, cy, radius, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(radius * 0.68f);
        Paint.FontMetrics fm = paint.getFontMetrics();
        canvas.drawText("⚙", cx, cy - (fm.ascent + fm.descent) * 0.5f, paint);
    }

    private void drawSettingsPanel(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(226, 12, 16, 22));
        canvas.drawRoundRect(settingsPanel, 22f, 22f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.4f);
        paint.setColor(Color.argb(150, 255, 255, 255));
        canvas.drawRoundRect(settingsPanel, 22f, 22f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(Math.max(13f, settingsPanel.height() * 0.050f));
        canvas.drawText("Kontrol", buttonModeOption.left, buttonModeOption.top - 5f, paint);
        drawOption(canvas, closeSettingsOption, "GERI", false);

        drawOption(canvas, buttonModeOption, "BUTON", controlMode == CONTROL_BUTTONS);
        drawOption(canvas, wheelModeOption, "DIREKSIYON", controlMode == CONTROL_STEERING_WHEEL);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(12f, settingsPanel.height() * 0.045f));
        canvas.drawText("Grafik", lowOption.left, lowOption.top - 5f, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("AKTIF: " + qualityLabel(graphicsQuality), settingsPanel.right - 14f, lowOption.top - 5f, paint);

        drawOption(canvas, lowOption, "DUS", pendingGraphicsQuality == QUALITY_LOW);
        drawOption(canvas, mediumOption, "ORT", pendingGraphicsQuality == QUALITY_MEDIUM);
        drawOption(canvas, highOption, "YUK", pendingGraphicsQuality == QUALITY_HIGH);
        drawOption(canvas, ultraOption, "ULTRA", pendingGraphicsQuality == QUALITY_ULTRA);
        drawOption(canvas, saveGraphicsOption, "GRAFIK KAYDET / YENIDEN BASLAT", pendingGraphicsQuality != graphicsQuality);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(225, 255, 255, 255));
        paint.setTextSize(Math.max(12f, settingsPanel.height() * 0.042f));
        canvas.drawText("A63.3 Mobil Kontrol Editoru", layoutPresetOption.left, layoutPresetOption.top - 5f, paint);
        drawOption(canvas, layoutPresetOption, "DÜZEN: " + controlLayout.layoutLabel(), false);
        drawOption(canvas, sensitivityPresetOption, "HASS: " + controlLayout.sensitivityLabel(), false);
        drawOption(canvas, pedalSizeOption, "PEDAL: " + controlLayout.pedalLabel(), false);
        drawOption(canvas, hudPresetOption, "HUD: " + controlLayout.hudLabel(), false);
        drawOption(canvas, opacityOption, "OPAK: %" + controlLayout.getButtonOpacityPercent(), false);
        drawOption(canvas, leftHandedOption, controlLayout.isLeftHandedMode() ? "SOLAK: AÇIK" : "SOLAK: KAPALI", controlLayout.isLeftHandedMode());
        drawOption(canvas, autoControlOption, controlLayout.isAutoByMode() ? "OTO: AÇIK" : "OTO: KAPALI", controlLayout.isAutoByMode());

        drawOption(canvas, respawnOption, "RESP", false);
        drawOption(canvas, cameraModeOption, "KAM", false);
        drawOption(canvas, trafficOption, "TRF " + trafficLabel(), false);
        drawOption(canvas, returnMenuOption, "MENU", false);
    }

    private String trafficLabel() {
        if (trafficDensity == TrafficSystem.DENSITY_OFF) return "KAP";
        if (trafficDensity == TrafficSystem.DENSITY_LOW) return "AZ";
        if (trafficDensity == TrafficSystem.DENSITY_HIGH) return "YOG";
        return "ORT";
    }

    private String qualityLabel(int quality) {
        if (quality == QUALITY_LOW) return "DUS";
        if (quality == QUALITY_MEDIUM) return "ORT";
        if (quality == QUALITY_ULTRA) return "ULTRA";
        return "YUK";
    }

    private void drawOption(Canvas canvas, RectF r, String text, boolean active) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(active ? Color.argb(178, 0, 210, 255) : Color.argb(74, 255, 255, 255));
        canvas.drawRoundRect(r, 16f, 16f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(145, 255, 255, 255));
        canvas.drawRoundRect(r, 16f, 16f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(11f, r.height() * 0.25f));
        paint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fm = paint.getFontMetrics();
        canvas.drawText(text, r.centerX(), r.centerY() - (fm.ascent + fm.descent) * 0.5f, paint);
    }

    private void drawMapOverlayTouchHint(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(60, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        drawCompactButton(canvas, expandedMapCloseButton, "KAPAT", false, false);
        drawCompactButton(canvas, pauseButton, "GERİ", false, false);
    }

    private void drawCompactButton(Canvas canvas, RectF r, String text, boolean active, boolean circular) {
        float pressure = active ? 1f : 0f;
        String micro = "";
        if (r == gasButton) { pressure = state.throttle; micro = "GAZ"; }
        else if (r == brakeButton) { pressure = state.brake; micro = "BITE"; }
        else if (r == handbrakeButton) { pressure = state.handbrake; micro = "DRIFT"; }
        else if (r == nitroButton) { pressure = state.nitro; micro = "BOOST"; }
        else if (r == leftButton || r == rightButton) { pressure = Math.abs(wheelVisualSteer); micro = controlLayout.sensitivityLabel(); }
        DrivingControlVisualSystem.drawButtonAdvanced(
                canvas,
                paint,
                r,
                text,
                pressure,
                circular,
                controlLayout.passiveAlpha(),
                controlLayout.activeAlpha(),
                micro);
    }

    private float controlPressureForButton(float x, float y, RectF r, float minPressure) {
        float dx = Math.abs(x - r.centerX()) / Math.max(1f, r.width() * 0.5f);
        float dy = Math.abs(y - r.centerY()) / Math.max(1f, r.height() * 0.5f);
        float edge = clamp(Math.max(dx, dy), 0f, 1f);
        float centerBoost = 1f - edge;
        return clamp(minPressure + centerBoost * (1f - minPressure), minPressure, 1f);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
