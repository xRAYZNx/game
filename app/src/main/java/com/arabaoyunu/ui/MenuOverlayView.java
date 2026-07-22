package com.arabaoyunu.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.BaseInputConnection;

import com.arabaoyunu.audio.GameAudioManager;
import com.arabaoyunu.customization.VisualCustomizationSystem;
import com.arabaoyunu.customization.VehicleCustomizationSystem;
import com.arabaoyunu.customization.RedeemCodeSystem;
import com.arabaoyunu.economy.DailyRewardSystem;
import com.arabaoyunu.garage.GarageShowroomSystem;
import com.arabaoyunu.garage.GarageInfrastructureSystem;
import com.arabaoyunu.garage.GarageModificationUiSystem;
import com.arabaoyunu.garage.GarageCarouselSystem;
import com.arabaoyunu.garage.PostTenUpdateStabilityQaSystem;
import com.arabaoyunu.garage.VisualModificationSaveFlowSystem;
import com.arabaoyunu.career.CareerSyncSystem;
import com.arabaoyunu.career.CareerLeagueSystem;
import com.arabaoyunu.career.CareerProgressSystem;
import com.arabaoyunu.career.CareerEventSystem;
import com.arabaoyunu.career.CareerResultSystem;
import com.arabaoyunu.career.CareerFlowQaSystem;
import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.menu.MainMenuLayoutSystem;
import com.arabaoyunu.map.MapRegistry;
import com.arabaoyunu.mode.TestDriveChallengeSystem;
import com.arabaoyunu.mode.RaceResultSystem;
import com.arabaoyunu.mode.CheckpointRaceSystem;
import com.arabaoyunu.mode.DriftScoreSystem;
import com.arabaoyunu.mode.GameModeHubSystem;
import com.arabaoyunu.performance.VehicleUpgradeSystem;
import com.arabaoyunu.performance.PerformanceUpgradeBalanceSystem;
import com.arabaoyunu.performance.VehicleTuningSystem;
import com.arabaoyunu.performance.TuningPresetBalanceSystem;
import com.arabaoyunu.quest.QuestPanelSystem;
import com.arabaoyunu.progression.AchievementSystem;
import com.arabaoyunu.progression.DailyWeeklyTaskSystem;
import com.arabaoyunu.progression.PlayerStatsSystem;
import com.arabaoyunu.progression.TaskAchievementHudSystem;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * ArabaOyunu_17: Ana menu / garaj / modlar / ayarlar lobi overlay'i.
 * 3D arac preview GameRenderer tarafinda cizilir; bu view buton ve UI panellerini cizer.
 */
public final class MenuOverlayView extends View {

    public interface Listener {
        void onScreenChanged(int screen);
        void onOpenWorldLoadingRequested();
    }

    private final GameScreenState state;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF rightCars = new RectF();
    private final RectF rightModes = new RectF();
    private final RectF rightMaps = new RectF();
    private final RectF rightCareer = new RectF();
    private final RectF rightQuests = new RectF();
    private final RectF rightSettings = new RectF();

    // A64: Ana menu yerlesim sistemi (center pivot + sol nav + sag profil, neon butonlar).
    private final MainMenuLayoutSystem mainMenuLayout = new MainMenuLayoutSystem();
    private final RectF[] mainNavButtons = new RectF[6];

    private final RectF backButton = new RectF();
    private final RectF selectButton = new RectF();
    private final RectF buyButton = new RectF();
    private final RectF infoButton = new RectF();
    private final RectF engineUpgradeButton = new RectF();
    private final RectF brakeUpgradeButton = new RectF();
    private final RectF tireUpgradeButton = new RectF();
    private final RectF driftUpgradeButton = new RectF();
    private final RectF paintPresetButton = new RectF();
    private final RectF rimPresetButton = new RectF();
    private final RectF platePresetButton = new RectF();
    private final RectF showroomRotateLeftButton = new RectF();
    private final RectF showroomRotateRightButton = new RectF();
    private final RectF showroomZoomOutButton = new RectF();
    private final RectF showroomZoomInButton = new RectF();
    private final RectF showroomResetButton = new RectF();
    private final RectF repairButton = new RectF();
    private final RectF[] performanceUpgradeButtons = new RectF[VehicleUpgradeSystem.PERFORMANCE_ORDER.length];
    private final RectF[] detailedTuningButtons = new RectF[VehicleTuningSystem.TUNING_ORDER.length];
    private final RectF[] tuningPresetButtons = new RectF[4];
    private final RectF[] visualModButtons = new RectF[VisualCustomizationSystem.VISUAL_ORDER.length];
    private final RectF[] visualValueButtons = new RectF[8];
    private static final int MOD_CATEGORY_PERFORMANCE = 0;
    private static final int MOD_CATEGORY_TUNING = 1;
    private static final int MOD_CATEGORY_BODY = 2;
    private static final int MOD_CATEGORY_WHEELS = 3;
    private static final int MOD_CATEGORY_GLASS = 4;
    private static final int MOD_CATEGORY_LIGHTS = 5;
    private static final int MOD_CATEGORY_PLATE = 6;
    private static final int MOD_CATEGORY_REPAIR = 7;

    private final RectF visualSaveButton = new RectF();
    private final RectF visualUndoButton = new RectF();
    private final RectF visualResetButton = new RectF();
    private final RectF performanceConfirmPanel = new RectF();
    private final RectF performanceConfirmButton = new RectF();
    private final RectF performanceCancelButton = new RectF();
    private final RectF modifyButton = new RectF();
    private final RectF testDriveButton = new RectF();
    private final RectF testDrivePrevButton = new RectF();
    private final RectF testDriveNextButton = new RectF();
    private final RectF garagePrevVehicleButton = new RectF();
    private final RectF garageNextVehicleButton = new RectF();
    private final RectF garageCarouselSwipeZone = new RectF();
    private final RectF[] garageVehicleCardButtons = new RectF[GarageCarouselSystem.VISIBLE_CARD_COUNT];
    private final RectF testDriveChoicePanel = new RectF();
    private final RectF buyConfirmPanel = new RectF();
    private final RectF buyConfirmButton = new RectF();
    private final RectF buyCancelButton = new RectF();
    private final RectF[] modifyCategoryButtons = new RectF[8];

    private final RectF redeemField = new RectF();
    private final RectF redeemUseButton = new RectF();
    private final RectF redeemRayzn1Button = new RectF();
    private final RectF redeemRayznCarButton = new RectF();
    private final RectF dailyRewardPanel = new RectF();
    private final RectF dailyRewardClaimButton = new RectF();
    private final RectF dailyRewardLaterButton = new RectF();

    private final RectF modeFree = new RectF();
    private final RectF modeTime = new RectF();
    private final RectF modeDrift = new RectF();
    private final RectF modeRace = new RectF();
    private final RectF modeDragRace = new RectF();
    private final RectF modePolice = new RectF();
    private final RectF modeOpenWorldDrive = new RectF();
    private final RectF startButton = new RectF();
    private final RectF modeGarageButton = new RectF();
    private final RectF modeModifyButton = new RectF();
    private final RectF[] checkpointRouteButtons = new RectF[CheckpointRaceSystem.ROUTE_COUNT];
    private final RectF[] careerEventButtons = new RectF[CareerEventSystem.EVENT_COUNT];
    private final RectF[] careerEventRewardButtons = new RectF[CareerEventSystem.EVENT_COUNT];
    private final RectF careerLeagueRewardButton = new RectF();

    private final RectF mapOpen = new RectF();
    private final RectF mapCity = new RectF();
    private final RectF mapHighway = new RectF();
    private final RectF mapDrift = new RectF();
    private final RectF mapOpenWorld = new RectF();
    private final RectF mapSecondNew = new RectF();
    private final RectF mapSelectButton = new RectF();

    private final RectF settingsGraphics = new RectF();
    private final RectF settingsControls = new RectF();
    private final RectF settingsSound = new RectF();
    private final RectF settingsHud = new RectF();
    private final RectF settingsVibration = new RectF();
    private final RectF settingsSensitivity = new RectF();
    private final RectF settingsLayoutPreset = new RectF();
    private final RectF settingsPedalSize = new RectF();
    private final RectF settingsHudPreset = new RectF();
    private final RectF settingsOpacity = new RectF();
    private final RectF settingsLeftHanded = new RectF();
    private final RectF settingsAutoControl = new RectF();
    private final RectF settingsTaskHud = new RectF();
    private final RectF settingsTaskNotification = new RectF();
    private final RectF settingsRewardPopup = new RectF();

    private final RectF questTab0 = new RectF();
    private final RectF questTab1 = new RectF();
    private final RectF questTab2 = new RectF();
    private final RectF questTab3 = new RectF();
    private final RectF questTab4 = new RectF();
    private final RectF questTab5 = new RectF();
    private final RectF questTab6 = new RectF();
    private final RectF questMainCard = new RectF();
    private final RectF questSideCard = new RectF();
    private final RectF questClaimAllButton = new RectF();
    private final RectF[] questRewardCards = new RectF[AchievementSystem.ACH_COUNT];
    private final RectF[] questRewardButtons = new RectF[AchievementSystem.ACH_COUNT];
    private final int[] questRewardIds = new int[AchievementSystem.ACH_COUNT];

    private final RectF dot0 = new RectF();
    private final RectF dot1 = new RectF();
    private final RectF dot2 = new RectF();

    private final RectF careerStarter0 = new RectF();
    private final RectF careerStarter1 = new RectF();
    private final RectF careerStarter2 = new RectF();
    private final RectF careerStartButton = new RectF();
    private int careerStarterIndex = 0;
    private int questPanelTab = QuestPanelSystem.TAB_ACTIVE;

    private Listener listener;
    private GameAudioManager audioManager;
    private final SaveManager saveManager;
    private final DailyRewardSystem dailyRewardSystem;
    private boolean dailyRewardDismissed;
    private int lastWidth;
    private int lastHeight;
    private float downX;
    private float downY;
    private float lastX;
    private boolean draggingPreview;
    private boolean redeemFocused;
    private int pendingPerformanceUpgradeType = -1;
    private String redeemInput = "";
    private String redeemMessage = "";

    public MenuOverlayView(Context context, GameScreenState state) {
        super(context);
        for (int i = 0; i < performanceUpgradeButtons.length; i++) {
            performanceUpgradeButtons[i] = new RectF();
        }
        for (int i = 0; i < detailedTuningButtons.length; i++) {
            detailedTuningButtons[i] = new RectF();
        }
        for (int i = 0; i < tuningPresetButtons.length; i++) {
            tuningPresetButtons[i] = new RectF();
        }
        for (int i = 0; i < visualModButtons.length; i++) {
            visualModButtons[i] = new RectF();
        }
        for (int i = 0; i < visualValueButtons.length; i++) {
            visualValueButtons[i] = new RectF();
        }
        for (int i = 0; i < modifyCategoryButtons.length; i++) {
            modifyCategoryButtons[i] = new RectF();
        }
        for (int i = 0; i < garageVehicleCardButtons.length; i++) {
            garageVehicleCardButtons[i] = new RectF();
        }
        for (int i = 0; i < checkpointRouteButtons.length; i++) {
            checkpointRouteButtons[i] = new RectF();
        }
        for (int i = 0; i < careerEventButtons.length; i++) {
            careerEventButtons[i] = new RectF();
            careerEventRewardButtons[i] = new RectF();
        }
        for (int i = 0; i < questRewardCards.length; i++) {
            questRewardCards[i] = new RectF();
            questRewardButtons[i] = new RectF();
        }
        this.state = state;
        this.saveManager = new SaveManager(context);
        this.dailyRewardSystem = new DailyRewardSystem(this.saveManager);
        CareerSyncSystem.validate(this.saveManager);
        this.saveManager.markVisualCustomizationSchemaCurrent(VehicleCustomizationSystem.SCHEMA_VERSION);
        this.saveManager.markGarageShowroomSchemaCurrent(GarageShowroomSystem.SCHEMA_VERSION);
        this.state.setSelectedMap(this.saveManager.getSelectedMap());
        this.state.setSelectedCheckpointRoute(this.saveManager.getSelectedCheckpointRoute());
        CheckpointRaceSystem.setActiveRoute(this.saveManager.getSelectedCheckpointRoute());
        if (!this.saveManager.isCareerStarted()) {
            this.state.setScreen(GameScreenState.SCREEN_CAREER_START);
            this.state.setSelectedVehicleIndex(starterIndexForSlot(0));
            this.careerStarterIndex = starterIndexForSlot(0);
        } else {
            // A62_2: oyuncunun en son seçtiği/satın aldığı araç kalıcı olarak yüklenir.
            this.state.setSelectedVehicleIndex(this.saveManager.getSelectedVehicleIndex());
        }
        setWillNotDraw(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));

        // A64: Sol navigasyon panelinin buton dizisi (mevcut RectF'ler korunur).
        mainNavButtons[0] = rightCars;
        mainNavButtons[1] = rightModes;
        mainNavButtons[2] = rightCareer;
        mainNavButtons[3] = rightMaps;
        mainNavButtons[4] = rightQuests;
        mainNavButtons[5] = rightSettings;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setAudioManager(GameAudioManager audioManager) {
        this.audioManager = audioManager;
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return redeemFocused;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;
        return new BaseInputConnection(this, false) {
            @Override
            public boolean commitText(CharSequence text, int newCursorPosition) {
                appendRedeemText(text == null ? "" : text.toString());
                return true;
            }

            @Override
            public boolean deleteSurroundingText(int beforeLength, int afterLength) {
                if (redeemInput.length() > 0) {
                    redeemInput = redeemInput.substring(0, redeemInput.length() - 1);
                    invalidate();
                }
                return true;
            }
        };
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (redeemFocused) {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                if (redeemInput.length() > 0) {
                    redeemInput = redeemInput.substring(0, redeemInput.length() - 1);
                    invalidate();
                }
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                applyRedeemInput();
                return true;
            }
            if (event != null) {
                int unicode = event.getUnicodeChar();
                if (unicode > 0) {
                    appendRedeemText(String.valueOf((char) unicode));
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    private void appendRedeemText(String text) {
        if (text == null || text.length() == 0) return;
        String clean = text.replaceAll("[^A-Za-z0-9]", "");
        if (clean.length() == 0) return;
        redeemInput = (redeemInput + clean).trim();
        if (redeemInput.length() > 18) redeemInput = redeemInput.substring(0, 18);
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layout(w, h);
    }

    private void layout(int w, int h) {
        lastWidth = w;
        lastHeight = h;

        float min = Math.min(w, h);
        float pad = Math.max(18f, min * 0.035f);
        float buttonW = Math.max(165f, w * 0.245f);
        float buttonH = Math.max(56f, min * 0.090f);
        float gap = Math.max(14f, min * 0.026f);

        float right = w - pad;
        float top = h * 0.34f;
        rightCars.set(right - buttonW, top, right, top + buttonH);
        rightModes.set(right - buttonW, rightCars.bottom + gap, right, rightCars.bottom + gap + buttonH);
        rightCareer.set(right - buttonW, rightModes.bottom + gap, right, rightModes.bottom + gap + buttonH);
        rightMaps.set(right - buttonW, rightCareer.bottom + gap, right, rightCareer.bottom + gap + buttonH);
        rightQuests.set(right - buttonW, rightMaps.bottom + gap, right, rightMaps.bottom + gap + buttonH);
        rightSettings.set(right - buttonW, rightQuests.bottom + gap, right, rightQuests.bottom + gap + buttonH);

        // A64: Yeni ana menu duzeni — 3D sahne center pivot, sol nav, sag profil.
        // Mevcut buton RectF'leri korunur; sadece konumlari sol panele tasinir.
        mainMenuLayout.layout(w, h);
        mainMenuLayout.layoutNavButtons(mainNavButtons);

        float redeemW = Math.max(245f, w * 0.275f);
        float redeemH = Math.max(30f, min * 0.052f);
        redeemField.set(w - pad - redeemW, pad, w - pad - Math.max(74f, w * 0.075f), pad + redeemH);
        redeemUseButton.set(redeemField.right + Math.max(6f, min * 0.010f), pad, w - pad, pad + redeemH);
        redeemRayzn1Button.set(redeemField.left, redeemField.bottom + Math.max(5f, min * 0.008f), redeemField.left + redeemW * 0.48f, redeemField.bottom + Math.max(5f, min * 0.008f) + redeemH * 0.82f);
        redeemRayznCarButton.set(redeemRayzn1Button.right + Math.max(6f, min * 0.010f), redeemRayzn1Button.top, redeemField.right, redeemRayzn1Button.bottom);

        float dailyW = Math.max(310f, w * 0.42f);
        float dailyH = Math.max(198f, h * 0.300f);
        dailyRewardPanel.set((w - dailyW) * 0.5f, h * 0.245f, (w + dailyW) * 0.5f, h * 0.245f + dailyH);
        float dailyBtnH = Math.max(42f, min * 0.070f);
        float dailyBtnW = (dailyW - gap * 3f) * 0.5f;
        dailyRewardLaterButton.set(dailyRewardPanel.left + gap, dailyRewardPanel.bottom - gap - dailyBtnH, dailyRewardPanel.left + gap + dailyBtnW, dailyRewardPanel.bottom - gap);
        dailyRewardClaimButton.set(dailyRewardLaterButton.right + gap, dailyRewardLaterButton.top, dailyRewardLaterButton.right + gap + dailyBtnW, dailyRewardLaterButton.bottom);

        boolean compactUi = w < 820 || h < 520;
        float leftW = Math.max(compactUi ? 136f : 158f, Math.min(w * (compactUi ? 0.218f : 0.245f), w * 0.285f));
        // A67.3: Garaj header ve sol aksiyonlar telefon/tablet safe-area içinde kalır;
        // geri butonu her ekranda üstte görünür tutulur.
        float headerBtnW = Math.max(72f, Math.min(compactUi ? 104f : 112f, w * 0.090f));
        float headerBtnH = Math.max(30f, Math.min(compactUi ? 38f : 42f, min * 0.055f));
        backButton.set(pad, pad, pad + headerBtnW, pad + headerBtnH);
        float garageActionH = Math.max(34f, Math.min(52f, min * 0.054f));
        float garageActionGap = Math.max(7f, min * 0.012f);
        float garageActionTop = h * 0.355f;
        selectButton.set(pad, garageActionTop, pad + leftW, garageActionTop + garageActionH);
        buyButton.set(pad, selectButton.bottom + garageActionGap, pad + leftW, selectButton.bottom + garageActionGap + garageActionH);
        infoButton.set(pad, buyButton.bottom + garageActionGap, pad + leftW, buyButton.bottom + garageActionGap + garageActionH);
        float upTop = infoButton.bottom + gap * 1.10f;
        float upH = Math.max(42f, buttonH * 0.72f);
        engineUpgradeButton.set(pad, upTop, pad + leftW, upTop + upH);
        brakeUpgradeButton.set(pad, engineUpgradeButton.bottom + gap * 0.62f, pad + leftW, engineUpgradeButton.bottom + gap * 0.62f + upH);
        tireUpgradeButton.set(pad, brakeUpgradeButton.bottom + gap * 0.62f, pad + leftW, brakeUpgradeButton.bottom + gap * 0.62f + upH);
        driftUpgradeButton.set(pad, tireUpgradeButton.bottom + gap * 0.62f, pad + leftW, tireUpgradeButton.bottom + gap * 0.62f + upH);

        float listW = Math.max(230f, w * 0.36f);
        float listLeft = w - pad - listW;
        float listTop = h * 0.245f;
        modeFree.set(listLeft, listTop, listLeft + listW, listTop + buttonH);
        modeTime.set(listLeft, modeFree.bottom + gap * 0.82f, listLeft + listW, modeFree.bottom + gap * 0.82f + buttonH);
        modeDrift.set(listLeft, modeTime.bottom + gap * 0.82f, listLeft + listW, modeTime.bottom + gap * 0.82f + buttonH);
        modeRace.set(listLeft, modeDrift.bottom + gap * 0.82f, listLeft + listW, modeDrift.bottom + gap * 0.82f + buttonH);
        modeDragRace.set(listLeft, modeRace.bottom + gap * 0.82f, listLeft + listW, modeRace.bottom + gap * 0.82f + buttonH);
        modePolice.set(listLeft, modeDragRace.bottom + gap * 0.82f, listLeft + listW, modeDragRace.bottom + gap * 0.82f + buttonH);
        float startY = Math.min(modePolice.bottom + gap * 0.82f, h * 0.765f);
        float startH = Math.max(42f, Math.min(buttonH * 0.82f, h * 0.062f));
        startButton.set(listLeft, startY, listLeft + listW, startY + startH);
        float actionGap = Math.max(7f, gap * 0.45f);
        float actionW = (listW - actionGap) * 0.5f;
        float actionY = Math.min(startButton.bottom + actionGap, h * 0.845f);
        float actionH = Math.max(34f, Math.min(buttonH * 0.62f, h * 0.052f));
        modeGarageButton.set(listLeft, actionY, listLeft + actionW, actionY + actionH);
        modeModifyButton.set(modeGarageButton.right + actionGap, actionY, listLeft + listW, actionY + actionH);

        // A65.0: Checkpoint rotaları sol profesyonel bilgi panelinin içinde 2x2 kart olarak seçilir.
        float routePanelX = w * 0.055f + Math.max(14f, min * 0.020f);
        float routePanelY = h * 0.435f;
        float routePanelW = w * 0.42f - Math.max(30f, min * 0.045f);
        float routeCardGap = Math.max(7f, gap * 0.50f);
        float routeCardW = (routePanelW - routeCardGap) * 0.5f;
        float routeCardH = Math.max(52f, min * 0.082f);
        for (int i = 0; i < checkpointRouteButtons.length; i++) {
            int col = i % 2;
            int row = i / 2;
            float rx = routePanelX + col * (routeCardW + routeCardGap);
            float ry = routePanelY + row * (routeCardH + routeCardGap);
            checkpointRouteButtons[i].set(rx, ry, rx + routeCardW, ry + routeCardH);
        }
        // A61_6: Açık Dünya GLB haritası kaldırıldı; kart görünmez ve dokunma alanı kapalıdır.
        modeOpenWorldDrive.set(0f, 0f, 0f, 0f);

        float mapButtonH = Math.max(42f, buttonH * 0.76f);
        float mapGap = Math.max(7f, gap * 0.48f);
        mapOpen.set(listLeft, listTop, listLeft + listW, listTop + mapButtonH);
        mapCity.set(listLeft, mapOpen.bottom + mapGap, listLeft + listW, mapOpen.bottom + mapGap + mapButtonH);
        mapHighway.set(listLeft, mapCity.bottom + mapGap, listLeft + listW, mapCity.bottom + mapGap + mapButtonH);
        mapDrift.set(listLeft, mapHighway.bottom + mapGap, listLeft + listW, mapHighway.bottom + mapGap + mapButtonH);
        mapOpenWorld.set(0f, 0f, 0f, 0f);
        mapSecondNew.set(0f, 0f, 0f, 0f);
        mapSelectButton.set(listLeft, mapDrift.bottom + gap * 0.72f, listLeft + listW, mapDrift.bottom + gap * 0.72f + mapButtonH);

        float careerCardW = Math.max(132f, w * 0.185f);
        float careerCardH = Math.max(126f, min * 0.205f);
        float careerGap = Math.max(12f, min * 0.024f);
        float careerLeft = w * 0.055f;
        float careerTop = h * 0.355f;
        careerStarter0.set(careerLeft, careerTop, careerLeft + careerCardW, careerTop + careerCardH);
        careerStarter1.set(careerStarter0.right + careerGap, careerTop, careerStarter0.right + careerGap + careerCardW, careerTop + careerCardH);
        careerStarter2.set(careerStarter1.right + careerGap, careerTop, careerStarter1.right + careerGap + careerCardW, careerTop + careerCardH);
        careerStartButton.set(w - pad - buttonW, h * 0.72f, w - pad, h * 0.72f + buttonH);

        settingsGraphics.set(listLeft, listTop, listLeft + listW, listTop + buttonH);
        settingsControls.set(listLeft, settingsGraphics.bottom + gap * 0.78f, listLeft + listW, settingsGraphics.bottom + gap * 0.78f + buttonH);
        settingsSound.set(listLeft, settingsControls.bottom + gap * 0.78f, listLeft + listW, settingsControls.bottom + gap * 0.78f + buttonH);
        settingsHud.set(listLeft, settingsSound.bottom + gap * 0.78f, listLeft + listW, settingsSound.bottom + gap * 0.78f + buttonH);
        settingsVibration.set(listLeft, settingsHud.bottom + gap * 0.78f, listLeft + listW, settingsHud.bottom + gap * 0.78f + buttonH);
        settingsSensitivity.set(listLeft, settingsVibration.bottom + gap * 0.78f, listLeft + listW, settingsVibration.bottom + gap * 0.78f + buttonH);
        settingsLayoutPreset.set(listLeft, settingsSensitivity.bottom + gap * 0.78f, listLeft + listW, settingsSensitivity.bottom + gap * 0.78f + buttonH);
        settingsPedalSize.set(listLeft, settingsLayoutPreset.bottom + gap * 0.78f, listLeft + listW, settingsLayoutPreset.bottom + gap * 0.78f + buttonH);
        settingsHudPreset.set(listLeft, settingsPedalSize.bottom + gap * 0.78f, listLeft + listW, settingsPedalSize.bottom + gap * 0.78f + buttonH);
        settingsOpacity.set(listLeft, settingsHudPreset.bottom + gap * 0.78f, listLeft + listW, settingsHudPreset.bottom + gap * 0.78f + buttonH);
        settingsLeftHanded.set(listLeft, settingsOpacity.bottom + gap * 0.78f, listLeft + listW, settingsOpacity.bottom + gap * 0.78f + buttonH);
        settingsAutoControl.set(listLeft, settingsLeftHanded.bottom + gap * 0.78f, listLeft + listW, settingsLeftHanded.bottom + gap * 0.78f + buttonH);
        float taskSettingsLeft = Math.max(w * 0.53f, listLeft + listW + gap * 1.8f);
        float taskSettingsW = Math.max(210f, w - pad - taskSettingsLeft);
        float taskSettingsY = h * 0.252f;
        float taskSettingsH = Math.max(42f, buttonH * 0.82f);
        settingsTaskHud.set(taskSettingsLeft, taskSettingsY, taskSettingsLeft + taskSettingsW, taskSettingsY + taskSettingsH);
        settingsTaskNotification.set(taskSettingsLeft, settingsTaskHud.bottom + gap * 0.82f, taskSettingsLeft + taskSettingsW, settingsTaskHud.bottom + gap * 0.82f + taskSettingsH);
        settingsRewardPopup.set(taskSettingsLeft, settingsTaskNotification.bottom + gap * 0.82f, taskSettingsLeft + taskSettingsW, settingsTaskNotification.bottom + gap * 0.82f + taskSettingsH);

        float questLeft = Math.max(pad, w * 0.055f);
        float questTop = h * 0.205f;
        float questTabW = Math.max(96f, w * 0.128f);
        float questTabH = Math.max(34f, min * 0.050f);
        float questTabGap = Math.max(7f, min * 0.010f);
        questTab0.set(questLeft, questTop, questLeft + questTabW, questTop + questTabH);
        questTab1.set(questTab0.right + questTabGap, questTop, questTab0.right + questTabGap + questTabW, questTop + questTabH);
        questTab2.set(questTab1.right + questTabGap, questTop, questTab1.right + questTabGap + questTabW, questTop + questTabH);
        questTab3.set(questTab2.right + questTabGap, questTop, questTab2.right + questTabGap + questTabW, questTop + questTabH);
        questTab4.set(questLeft, questTab0.bottom + questTabGap, questLeft + questTabW, questTab0.bottom + questTabGap + questTabH);
        questTab5.set(questTab4.right + questTabGap, questTab4.top, questTab4.right + questTabGap + questTabW, questTab4.bottom);
        questTab6.set(questTab5.right + questTabGap, questTab4.top, questTab5.right + questTabGap + questTabW, questTab4.bottom);
        questMainCard.set(questLeft, questTab4.bottom + gap * 1.05f, w * 0.64f, h * 0.79f);
        questSideCard.set(w * 0.665f, questMainCard.top, w - pad, questMainCard.bottom);

        float customY = h * 0.715f;
        float customW = Math.max(86f, w * 0.135f);
        float customH = Math.max(38f, min * 0.060f);
        float customGap = Math.max(8f, min * 0.014f);
        float customStart = w * 0.38f;
        paintPresetButton.set(customStart, customY, customStart + customW, customY + customH);
        rimPresetButton.set(paintPresetButton.right + customGap, customY, paintPresetButton.right + customGap + customW, customY + customH);
        platePresetButton.set(rimPresetButton.right + customGap, customY, rimPresetButton.right + customGap + customW, customY + customH);
        repairButton.set(customStart, customY + customH + customGap, customStart + customW * 2.0f + customGap, customY + customH + customGap + customH);

        // A64.4: Showroom kontrol şeridi gerçek 3D showroom/araç üstünü kapatmaz;
        // üst güvenli bölgede kompakt kalır.
        float showroomControlY = Math.max(backButton.bottom + Math.max(8f, min * 0.012f), h * (compactUi ? 0.128f : 0.145f));
        float showroomControlH = Math.max(20f, Math.min(compactUi ? 26f : 28f, min * 0.033f));
        float showroomControlW = Math.max(compactUi ? 30f : 34f, Math.min(compactUi ? 48f : 54f, w * 0.038f));
        float showroomControlGap = Math.max(5f, min * 0.008f);
        float showroomControlStart = w * (compactUi ? 0.366f : 0.392f);
        showroomRotateLeftButton.set(showroomControlStart, showroomControlY, showroomControlStart + showroomControlW, showroomControlY + showroomControlH);
        showroomRotateRightButton.set(showroomRotateLeftButton.right + showroomControlGap, showroomControlY, showroomRotateLeftButton.right + showroomControlGap + showroomControlW, showroomControlY + showroomControlH);
        showroomZoomOutButton.set(showroomRotateRightButton.right + showroomControlGap, showroomControlY, showroomRotateRightButton.right + showroomControlGap + showroomControlW, showroomControlY + showroomControlH);
        showroomZoomInButton.set(showroomZoomOutButton.right + showroomControlGap, showroomControlY, showroomZoomOutButton.right + showroomControlGap + showroomControlW, showroomControlY + showroomControlH);
        showroomResetButton.set(showroomZoomInButton.right + showroomControlGap, showroomControlY, showroomZoomInButton.right + showroomControlGap + showroomControlW * 1.45f, showroomControlY + showroomControlH);

        float statLeft = w * (compactUi ? 0.715f : 0.725f);
        float rightActionH = Math.max(32f, Math.min(compactUi ? 44f : 50f, min * 0.054f));
        float rightActionTop = Math.min(h * 0.630f, h - Math.max(compactUi ? 156f : 178f, min * 0.250f));
        modifyButton.set(statLeft, rightActionTop, w - pad, rightActionTop + rightActionH);
        testDriveButton.set(statLeft, modifyButton.bottom + Math.max(7f, min * 0.012f), w - pad, modifyButton.bottom + Math.max(7f, min * 0.012f) + rightActionH);
        float testChoiceTop = testDriveButton.bottom + Math.max(6f, min * 0.010f);
        float testChoiceH = Math.max(26f, Math.min(34f, buttonH * 0.42f));
        testDriveChoicePanel.set(statLeft, testChoiceTop, w - pad, testChoiceTop + testChoiceH);
        float testArrowW = Math.max(34f, testChoiceH * 1.15f);
        testDrivePrevButton.set(statLeft, testChoiceTop, statLeft + testArrowW, testChoiceTop + testChoiceH);
        testDriveNextButton.set(w - pad - testArrowW, testChoiceTop, w - pad, testChoiceTop + testChoiceH);

        float confirmW = Math.max(310f, w * 0.44f);
        float confirmH = Math.max(220f, h * 0.36f);
        buyConfirmPanel.set((w - confirmW) * 0.5f, h * 0.245f, (w + confirmW) * 0.5f, h * 0.245f + confirmH);
        float confirmButtonW = (confirmW - gap * 3f) * 0.5f;
        float confirmButtonH = Math.max(44f, buttonH * 0.72f);
        buyCancelButton.set(buyConfirmPanel.left + gap, buyConfirmPanel.bottom - gap - confirmButtonH, buyConfirmPanel.left + gap + confirmButtonW, buyConfirmPanel.bottom - gap);
        buyConfirmButton.set(buyCancelButton.right + gap, buyCancelButton.top, buyCancelButton.right + gap + confirmButtonW, buyCancelButton.bottom);

        // A66.4: Modifiye ana kategori kartları artık 4x2 profesyonel atölye rafıdır;
        // gerçek showroom/araç vitrini ve sağ build paneliyle çakışmaz.
        float catLeft = w * 0.055f;
        float catTop = h * (compactUi ? 0.622f : 0.638f);
        float catRightLimit = w * (compactUi ? 0.682f : 0.690f);
        float catGapX = Math.max(7f, min * 0.010f);
        float catGapY = Math.max(7f, min * 0.010f);
        float catW = (catRightLimit - catLeft - catGapX * 3f) / 4f;
        float catH = Math.max(38f, Math.min(54f, min * 0.062f));
        for (int i = 0; i < modifyCategoryButtons.length; i++) {
            int col = i % 4;
            int row = i / 4;
            float x = catLeft + col * (catW + catGapX);
            float y = catTop + row * (catH + catGapY);
            modifyCategoryButtons[i].set(x, y, x + catW, y + catH);
        }

        // A66.4: Performans kartları 4x2 grid ile sağ istatistik panelinden ayrılır.
        float perfLeft = w * 0.055f;
        float perfTop = h * (compactUi ? 0.570f : 0.585f);
        float perfRight = w * (compactUi ? 0.682f : 0.690f);
        float perfGapX = Math.max(7f, min * 0.011f);
        float perfGapY = Math.max(7f, min * 0.010f);
        float perfW = (perfRight - perfLeft - perfGapX * 3f) / 4f;
        float perfH = Math.max(48f, min * 0.072f);
        for (int i = 0; i < performanceUpgradeButtons.length; i++) {
            int col = i % 4;
            int row = i / 4;
            float x = perfLeft + col * (perfW + perfGapX);
            float y = perfTop + row * (perfH + perfGapY);
            performanceUpgradeButtons[i].set(x, y, x + perfW, y + perfH);
        }

        // A66.4: Tuning değerleri 4 kolonlu, presetler ayrı ikinci banttır.
        float tuneLeft = w * 0.055f;
        float tuneTop = h * (compactUi ? 0.555f : 0.570f);
        float tuneRight = w * (compactUi ? 0.682f : 0.690f);
        float tuneGapX = Math.max(7f, min * 0.011f);
        float tuneGapY = Math.max(5f, min * 0.008f);
        float tuneW = (tuneRight - tuneLeft - tuneGapX * 3f) / 4f;
        float tuneH = Math.max(30f, min * 0.049f);
        for (int i = 0; i < detailedTuningButtons.length; i++) {
            int col = i % 4;
            int row = i / 4;
            float x = tuneLeft + col * (tuneW + tuneGapX);
            float y = tuneTop + row * (tuneH + tuneGapY);
            detailedTuningButtons[i].set(x, y, x + tuneW, y + tuneH);
        }
        int tuneRows = Math.max(1, (detailedTuningButtons.length + 3) / 4);
        float presetY = tuneTop + tuneRows * (tuneH + tuneGapY) + tuneGapY * 1.7f;
        float presetW = (tuneRight - tuneLeft - tuneGapX * 3f) / 4f;
        for (int i = 0; i < tuningPresetButtons.length; i++) {
            float x = tuneLeft + i * (presetW + tuneGapX);
            tuningPresetButtons[i].set(x, presetY, x + presetW, presetY + Math.max(42f, tuneH * 1.50f));
        }

        // A66.4: Görsel modifiye parça kartları 4 kolonla okunabilir; sağ panel ve araçla çakışmaz.
        float visualLeft = w * 0.055f;
        float visualTop = h * (compactUi ? 0.570f : 0.585f);
        float visualRight = w * (compactUi ? 0.682f : 0.690f);
        float visualGapX = Math.max(6f, min * 0.010f);
        float visualGapY = Math.max(5f, min * 0.008f);
        float visualW = (visualRight - visualLeft - visualGapX * 3f) / 4f;
        float visualH = Math.max(28f, min * 0.043f);
        for (int i = 0; i < visualModButtons.length; i++) {
            int col = i % 4;
            int row = i / 4;
            float x = visualLeft + col * (visualW + visualGapX);
            float y = visualTop + row * (visualH + visualGapY);
            visualModButtons[i].set(x, y, x + visualW, y + visualH);
        }
        float valueTop = h * (compactUi ? 0.650f : 0.662f);
        float valueW = Math.max(compactUi ? 78f : 94f, w * (compactUi ? 0.135f : 0.155f));
        float valueH = Math.max(30f, min * 0.050f);
        for (int i = 0; i < visualValueButtons.length; i++) {
            int col = i % 4;
            int row = i / 4;
            float x = w * 0.055f + col * (valueW + visualGapX);
            float y = valueTop + row * (valueH + visualGapY);
            visualValueButtons[i].set(x, y, x + valueW, y + valueH);
        }
        float editButtonY = h * (compactUi ? 0.826f : 0.846f);
        float editButtonH = Math.max(32f, min * 0.054f);
        float editButtonW = Math.max(compactUi ? 104f : 130f, w * (compactUi ? 0.138f : 0.155f));
        visualResetButton.set(w * 0.055f, editButtonY, w * 0.055f + editButtonW, editButtonY + editButtonH);
        visualUndoButton.set((w - editButtonW) * 0.5f, editButtonY, (w + editButtonW) * 0.5f, editButtonY + editButtonH);
        visualSaveButton.set(w - pad - editButtonW, editButtonY, w - pad, editButtonY + editButtonH);

        float perfConfirmW = Math.max(330f, w * 0.43f);
        float perfConfirmH = Math.max(218f, h * 0.320f);
        performanceConfirmPanel.set((w - perfConfirmW) * 0.5f, h * 0.286f, (w + perfConfirmW) * 0.5f, h * 0.286f + perfConfirmH);
        float perfBtnH = Math.max(42f, min * 0.068f);
        float perfBtnW = (perfConfirmW - gap * 3f) * 0.5f;
        performanceCancelButton.set(performanceConfirmPanel.left + gap, performanceConfirmPanel.bottom - gap - perfBtnH, performanceConfirmPanel.left + gap + perfBtnW, performanceConfirmPanel.bottom - gap);
        performanceConfirmButton.set(performanceCancelButton.right + gap, performanceCancelButton.top, performanceCancelButton.right + gap + perfBtnW, performanceCancelButton.bottom);

        float dotY = h - Math.max(30f, min * 0.052f);
        float dotSize = Math.max(10f, Math.min(16f, min * 0.019f));
        float center = w * 0.50f;
        dot0.set(center - dotSize * 2.6f, dotY, center - dotSize * 1.6f, dotY + dotSize);
        dot1.set(center - dotSize * 0.5f, dotY, center + dotSize * 0.5f, dotY + dotSize);
        dot2.set(center + dotSize * 1.6f, dotY, center + dotSize * 2.6f, dotY + dotSize);

        // A66.9: Araç carousel'i tek merkezden layout edilir; tüm alt bant swipe/card tap hedefidir.
        GarageCarouselSystem.layoutBand(garageCarouselSwipeZone, w, h);
        GarageCarouselSystem.layoutPrevArrow(garageCarouselSwipeZone, garagePrevVehicleButton, w, h);
        GarageCarouselSystem.layoutNextArrow(garageCarouselSwipeZone, garageNextVehicleButton, w, h);
        for (int i = 0; i < garageVehicleCardButtons.length; i++) {
            GarageCarouselSystem.layoutCard(garageCarouselSwipeZone, garageVehicleCardButtons[i], i - 2, w, h);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (lastWidth != getWidth() || lastHeight != getHeight()) {
            layout(getWidth(), getHeight());
        }

        int screen = state.getScreen();
        if (screen == GameScreenState.SCREEN_DRIVE) return;

        drawBackgroundVignette(canvas);
        refreshA635LongTermSystems();

        if (screen == GameScreenState.SCREEN_CAREER_START) {
            drawCareerStart(canvas);
        } else if (screen == GameScreenState.SCREEN_GARAGE) {
            drawGarage(canvas);
        } else if (screen == GameScreenState.SCREEN_MODES) {
            drawModes(canvas);
        } else if (screen == GameScreenState.SCREEN_SETTINGS) {
            drawSettings(canvas);
        } else if (screen == GameScreenState.SCREEN_MAPS) {
            drawMaps(canvas);
        } else if (screen == GameScreenState.SCREEN_QUESTS) {
            drawQuestPanel(canvas);
        } else if (screen == GameScreenState.SCREEN_CAREER) {
            drawCareerPanel(canvas);
        } else {
            drawMainMenu(canvas);
        }

        if (!shouldShowDailyRewardModal()) {
            drawBottomStatusToast(canvas);
        }

        if (shouldShowDailyRewardModal()) {
            drawDailyRewardModal(canvas);
        }
    }

    private void refreshA635LongTermSystems() {
        if (saveManager == null) return;
        DailyWeeklyTaskSystem.ensureWindows(saveManager);
        // A63.7: Menü yenilemesi yalnızca durumları taze tutar.
        // Tamamlanan başarımlar burada otomatik coin/XP vermez ve ödül sesi çalmaz.
        AchievementSystem.evaluateCompletedOnly(saveManager);
    }

    private void drawBottomStatusToast(Canvas canvas) {
        if (saveManager == null) return;
        String msg = saveManager.getEconomyLastMessage();
        if (msg == null || msg.length() == 0) msg = saveManager.getCareerLastMessage();
        if (msg == null || msg.length() == 0) return;
        float min = Math.min(getWidth(), getHeight());
        float w = Math.min(getWidth() * 0.72f, Math.max(340f, getWidth() * 0.52f));
        float h = Math.max(34f, min * 0.055f);
        RectF r;
        if (state != null && state.getScreen() == GameScreenState.SCREEN_GARAGE) {
            float gw = Math.min(getWidth() * 0.44f, Math.max(270f, getWidth() * 0.34f));
            float gh = Math.max(24f, min * 0.038f);
            float gy = getHeight() * 0.112f;
            r = new RectF((getWidth() - gw) * 0.5f, gy, (getWidth() + gw) * 0.5f, gy + gh);
        } else {
            r = new RectF((getWidth() - w) * 0.5f, getHeight() - h - Math.max(12f, min * 0.020f), (getWidth() + w) * 0.5f, getHeight() - Math.max(12f, min * 0.020f));
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(176, 2, 8, 18));
        canvas.drawRoundRect(r, 18f, 18f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f);
        paint.setColor(Color.argb(190, 0, 220, 255));
        canvas.drawRoundRect(r, 18f, 18f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.argb(235, 235, 245, 255));
        paint.setTextSize(Math.max(10f, min * 0.017f));
        canvas.drawText(trimMenuText(msg, 68), r.centerX(), r.centerY() + Math.max(4f, min * 0.006f), paint);
    }

    private boolean shouldShowDailyRewardModal() {
        return state != null
                && state.getScreen() == GameScreenState.SCREEN_MAIN_MENU
                && saveManager != null
                && saveManager.isCareerStarted()
                && dailyRewardSystem != null
                && dailyRewardSystem.isAvailable()
                && !dailyRewardDismissed;
    }

    private void drawDailyRewardModal(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(148, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        drawGlassPanel(canvas, dailyRewardPanel, Color.argb(232, 4, 10, 22), Color.argb(225, 255, 220, 80));
        float x = dailyRewardPanel.left + Math.max(20f, dailyRewardPanel.width() * 0.065f);
        float y = dailyRewardPanel.top + Math.max(36f, dailyRewardPanel.height() * 0.18f);
        float line = Math.max(22f, dailyRewardPanel.height() * 0.112f);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(19f, getHeight() * 0.034f));
        canvas.drawText("GUNLUK GIRIS ODULU", x, y, paint);
        paint.setTextSize(Math.max(12f, getHeight() * 0.020f));
        paint.setColor(Color.argb(235, 255, 235, 130));
        canvas.drawText("Gun " + dailyRewardSystem.getDayNumber() + " / 7", x, y + line * 1.15f, paint);
        paint.setColor(Color.argb(230, 235, 245, 255));
        canvas.drawText("Odul: " + dailyRewardSystem.getRewardText(), x, y + line * 2.15f, paint);
        canvas.drawText("Bu para garaj/modifiye sisteminde kullanilir.", x, y + line * 3.10f, paint);
        paint.setColor(Color.argb(215, 120, 255, 165));
        canvas.drawText("Seri: Her gun gir, 7. gunde buyuk odulu al.", x, y + line * 4.05f, paint);

        drawButton(canvas, dailyRewardLaterButton, "SONRA", false, false);
        drawButton(canvas, dailyRewardClaimButton, "ODULU AL", true, false);
    }

    private boolean handleDailyRewardClick(float x, float y) {
        if (!shouldShowDailyRewardModal()) return false;
        if (dailyRewardClaimButton.contains(x, y)) {
            if (dailyRewardSystem.claim()) {
                dailyRewardDismissed = true;
                if (audioManager != null) audioManager.playReward();
            } else if (audioManager != null) {
                audioManager.playLocked();
            }
            invalidate();
            return true;
        }
        if (dailyRewardLaterButton.contains(x, y)) {
            dailyRewardDismissed = true;
            if (audioManager != null) audioManager.playBack();
            invalidate();
            return true;
        }
        // Modal acikken arka menudeki butonlara tiklama gecmesin.
        return true;
    }

    private void drawBackgroundVignette(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(84, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
    }

    private void drawCareerStart(Canvas canvas) {
        drawTitle(canvas, "KARIYER BASLANGICI", "Starter sec / ilk gorevleri ac / seviye 1");

        float pulse = 0.5f + 0.5f * (float)Math.sin(System.currentTimeMillis() / 420.0);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(235, 235, 245, 255));
        paint.setTextSize(Math.max(14f, getHeight() * 0.024f));
        canvas.drawText("Oyuna ilk giris: baslangic aracini sec, kariyerini baslat.",
                getWidth() * 0.055f, getHeight() * 0.19f, paint);

        paint.setTextSize(Math.max(12f, getHeight() * 0.020f));
        paint.setColor(Color.argb(215, 255, 220, 90));
        canvas.drawText("Baslangic: Seviye 1  |  " + SaveManager.CAREER_START_COINS + " coin  |  Ilk gorevler acik",
                getWidth() * 0.055f, getHeight() * 0.235f, paint);

        drawStarterCard(canvas, careerStarter0, 0, pulse);
        drawStarterCard(canvas, careerStarter1, 1, pulse);
        drawStarterCard(canvas, careerStarter2, 2, pulse);

        drawButton(canvas, careerStartButton, "KARIYERE BASLA", true, false);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(Math.max(12f, getHeight() * 0.020f));
        paint.setColor(Color.argb(220, 235, 245, 255));
        canvas.drawText("Kilitli araclar ve harita bolgeleri garaj/harita ekraninda gorunur.",
                getWidth() * 0.055f, getHeight() * 0.78f, paint);
        canvas.drawText("Senkronizasyon: kariyer kaydi bozulursa starter arac, seviye ve harita kaydi dogrulanir.",
                getWidth() * 0.055f, getHeight() * 0.825f, paint);
        canvas.drawText("Sonraki hedef: gunluk/haftalik gorevleri tamamla, XP kazan, yeni bolgeleri ac.",
                getWidth() * 0.055f, getHeight() * 0.87f, paint);
    }

    private void drawStarterCard(Canvas canvas, RectF rect, int slot, float pulse) {
        int vehicle = starterIndexForSlot(slot);
        boolean selected = careerStarterIndex == vehicle;
        paint.setStyle(Paint.Style.FILL);
        int alpha = selected ? (170 + (int)(pulse * 45f)) : 96;
        paint.setColor(selected ? Color.argb(alpha, 0, 170, 255) : Color.argb(105, 255, 255, 255));
        canvas.drawRoundRect(rect, 20f, 20f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(selected ? 3.2f : 2.0f);
        paint.setColor(selected ? Color.argb(235, 130, 235, 255) : Color.argb(150, 255, 255, 255));
        canvas.drawRoundRect(rect, 20f, 20f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(12f, rect.height() * 0.105f));
        canvas.drawText(VehicleCatalog.label(vehicle), rect.centerX(), rect.top + rect.height() * 0.22f, paint);

        paint.setColor(Color.argb(235, 255, 220, 90));
        paint.setTextSize(Math.max(11f, rect.height() * 0.092f));
        canvas.drawText("Sınıf x" + formatOne(VehicleCatalog.performanceClass(vehicle)),
                rect.centerX(), rect.top + rect.height() * 0.47f, paint);

        paint.setColor(Color.argb(220, 235, 245, 255));
        String role = slot == 0 ? "Dengeli baslangic" : slot == 1 ? "Arazi / guvenli surus" : "Klasik hizli starter";
        canvas.drawText(role, rect.centerX(), rect.top + rect.height() * 0.67f, paint);

        paint.setColor(selected ? Color.WHITE : Color.argb(190, 220, 230, 240));
        canvas.drawText(selected ? "SECILDI" : "SEC", rect.centerX(), rect.bottom - rect.height() * 0.13f, paint);
    }

    private void drawRedeemPanel(Canvas canvas) {
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(Math.max(9.5f, getHeight() * 0.015f));
        paint.setColor(Color.argb(225, 255, 235, 120));
        canvas.drawText("REDEEM CODE", redeemField.left, redeemField.top - Math.max(5f, getHeight() * 0.008f), paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(redeemFocused ? Color.argb(180, 20, 95, 135) : Color.argb(122, 255, 255, 255));
        canvas.drawRoundRect(redeemField, 14f, 14f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f);
        paint.setColor(redeemFocused ? Color.argb(235, 90, 230, 255) : Color.argb(150, 255, 255, 255));
        canvas.drawRoundRect(redeemField, 14f, 14f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(Math.max(11f, getHeight() * 0.017f));
        String shown = redeemInput.length() == 0 ? "Kod yaz..." : redeemInput;
        canvas.drawText(shown, redeemField.left + 9f, redeemField.centerY() + 4f, paint);

        drawButton(canvas, redeemUseButton, "KULLAN", true, false);
        drawButton(canvas, redeemRayzn1Button, "Rayzn1", false, false);
        drawButton(canvas, redeemRayznCarButton, "Rayzncar", false, false);

        if (redeemMessage != null && redeemMessage.length() > 0) {
            paint.setTextAlign(Paint.Align.RIGHT);
            paint.setTextSize(Math.max(9f, getHeight() * 0.014f));
            paint.setColor(Color.argb(230, 160, 255, 185));
            canvas.drawText(trimMenuText(redeemMessage, 42), redeemUseButton.right, redeemRayzn1Button.bottom + Math.max(12f, getHeight() * 0.015f), paint);
        }
    }

    private void cycleTestDriveChallenge(int dir) {
        if (saveManager == null) return;
        int next = saveManager.getTestDriveChallengeIndex() + dir;
        if (next < 0) next = TestDriveChallengeSystem.CH_COUNT - 1;
        if (next >= TestDriveChallengeSystem.CH_COUNT) next = 0;
        saveManager.setTestDriveChallengeIndex(next);
        saveManager.setEconomyLastMessage("Test parkuru seçildi: " + TestDriveChallengeSystem.challengeLabel(next));
        if (audioManager != null) audioManager.playMenuClick();
        invalidate();
    }

    private boolean handleRedeemClick(float x, float y) {
        if (redeemField.contains(x, y)) {
            redeemFocused = true;
            requestFocus();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
            invalidate();
            return true;
        }
        if (redeemRayzn1Button.contains(x, y)) {
            redeemInput = "Rayzn1";
            redeemFocused = true;
            invalidate();
            return true;
        }
        if (redeemRayznCarButton.contains(x, y)) {
            redeemInput = "Rayzncar";
            redeemFocused = true;
            invalidate();
            return true;
        }
        if (redeemUseButton.contains(x, y)) {
            applyRedeemInput();
            return true;
        }
        redeemFocused = false;
        return false;
    }

    private void applyRedeemInput() {
        RedeemCodeSystem.Result result = RedeemCodeSystem.redeem(saveManager, redeemInput);
        redeemMessage = result.message;
        if (result.success) {
            if (audioManager != null) audioManager.playReward();
            redeemInput = "";
        } else if (audioManager != null) {
            audioManager.playLocked();
        }
        invalidate();
    }

    private void drawMainMenu(Canvas canvas) {
        drawTitle(canvas, "RAYCAR", "PREMIUM LOBI / A63.9");

        // A64: 3D arac/operator sahnesi tam ortada (center pivot) — holografik cerceve.
        mainMenuLayout.drawCenterPivotFrame(canvas, paint);

        drawMainTopStatus(canvas);
        drawMainVehicleGoalCard(canvas);
        drawMainModePreview(canvas);
        drawRedeemPanel(canvas);

        // A64: Sol panel = navigasyon, modern espor/neon butonlar, yumusak animasyonlu.
        mainMenuLayout.drawNavPanel(canvas, paint, "NAVIGASYON");
        mainMenuLayout.drawNeonButton(canvas, paint, rightCars, "GARAJ / MOD", true, false);
        mainMenuLayout.drawNeonButton(canvas, paint, rightModes, "MOD HUB", true, false);
        mainMenuLayout.drawNeonButton(canvas, paint, rightCareer, "KARIYER", true, false);
        mainMenuLayout.drawNeonButton(canvas, paint, rightMaps, "HARITALAR", false, false);
        mainMenuLayout.drawNeonButton(canvas, paint, rightQuests,
                TaskAchievementHudSystem.pendingRewardBadge(saveManager), true, false);
        mainMenuLayout.drawNeonButton(canvas, paint, rightSettings, "AYARLAR", false, false);

        // A64: Sag panel = profil / sosyal.
        mainMenuLayout.drawProfilePanels(canvas, paint,
                "LVL " + saveManager.getPlayerLevel() + "  •  " + saveManager.getCareerLeagueName(),
                "Coin: " + coinsText() + "  •  Kasa: " + saveManager.getRewardCrates(),
                UiBalanceSystem.economyTier(saveManager),
                "Ekip: RAYZN Garage",
                "Aktif uye: " + (saveManager.getPlayerLevel() % 4 + 1));

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(205, 255, 255, 255));
        paint.setTextSize(Math.max(11f, getHeight() * 0.018f));
        canvas.drawText("A64: Center pivot sahne, sol navigasyon ve sag profil/sosyal panelleri aktif.",
                getWidth() * 0.055f, getHeight() * 0.89f, paint);

        // Neon nabiz animasyonu ana menude yumusak sekilde surer.
        postInvalidateDelayed(80L);
    }

    private void drawMainTopStatus(Canvas canvas) {
        float min = Math.min(getWidth(), getHeight());
        // A64: Durum karti ust-orta bolgeye alindi (center pivot duzeni).
        float w = getWidth() * 0.460f;
        float x = getWidth() * 0.5f - w * 0.5f;
        float y = getHeight() * 0.150f;
        float h = Math.max(66f, getHeight() * 0.105f);
        RectF r = new RectF(x, y, x + w, y + h);
        drawGlassPanel(canvas, r, Color.argb(142, 4, 12, 26), Color.argb(185, 0, 220, 255));

        int level = saveManager.getPlayerLevel();
        int xp = saveManager.getPlayerXp();
        int nextXp = saveManager.getXpForNextLevel();
        float xp01 = nextXp <= 0 ? 1f : Math.max(0f, Math.min(1f, xp / (float)nextXp));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(13f, min * 0.023f));
        canvas.drawText("LVL " + level + "  •  " + saveManager.getCareerLeagueName(), r.left + 16f, r.top + h * 0.34f, paint);
        paint.setColor(Color.argb(245, 255, 220, 90));
        canvas.drawText("Coin: " + coinsText() + "  •  Kasa: " + saveManager.getRewardCrates(), r.left + w * 0.50f, r.top + h * 0.34f, paint);

        RectF bg = new RectF(r.left + 16f, r.top + h * 0.57f, r.right - 16f, r.top + h * 0.57f + Math.max(8f, min * 0.012f));
        paint.setColor(Color.argb(68, 255, 255, 255));
        canvas.drawRoundRect(bg, 7f, 7f, paint);
        RectF fg = new RectF(bg.left, bg.top, bg.left + bg.width() * xp01, bg.bottom);
        paint.setColor(Color.argb(235, 0, 220, 255));
        canvas.drawRoundRect(fg, 7f, 7f, paint);
        paint.setColor(Color.argb(220, 235, 245, 255));
        paint.setTextSize(Math.max(9.5f, min * 0.016f));
        canvas.drawText("XP " + xp + "/" + nextXp + "  •  " + UiBalanceSystem.economyTier(saveManager), r.left + 16f, r.bottom - h * 0.15f, paint);
    }

    private void drawMainVehicleGoalCard(Canvas canvas) {
        float min = Math.min(getWidth(), getHeight());
        // A64: Arac karti pivot cercevesinin alt-soluna tasindi.
        float w = getWidth() * 0.235f;
        float x = getWidth() * 0.5f - w - Math.max(8f, min * 0.012f);
        float y = getHeight() * 0.660f;
        float h = Math.max(140f, getHeight() * 0.225f);
        RectF r = new RectF(x, y, x + w, y + h);
        drawGlassPanel(canvas, r, Color.argb(130, 5, 12, 24), Color.argb(160, 255, 210, 80));

        int vehicle = state.getSelectedVehicleIndex();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(14f, min * 0.025f));
        canvas.drawText("Seçili araç", r.left + 16f, r.top + h * 0.20f, paint);
        paint.setColor(Color.argb(238, 235, 245, 255));
        paint.setTextSize(Math.max(12f, min * 0.020f));
        canvas.drawText(trimMenuText(GameScreenState.vehicleLabel(vehicle), 34), r.left + 16f, r.top + h * 0.38f, paint);
        paint.setColor(Color.argb(230, 120, 235, 255));
        canvas.drawText(VehicleCatalog.className(vehicle) + "  •  " + VehicleCatalog.garageRole(vehicle), r.left + 16f, r.top + h * 0.55f, paint);
        paint.setColor(Color.argb(220, 155, 255, 185));
        canvas.drawText("Hedef: " + trimMenuText(UiBalanceSystem.recommendedNextAction(saveManager), 42), r.left + 16f, r.top + h * 0.74f, paint);
        paint.setColor(Color.argb(210, 235, 245, 255));
        canvas.drawText("Günlük ödül: " + (dailyRewardSystem == null ? "-" : dailyRewardSystem.getStatusText()), r.left + 16f, r.top + h * 0.90f, paint);
    }

    private void drawMainModePreview(Canvas canvas) {
        float min = Math.min(getWidth(), getHeight());
        // A64: Mod onizleme karti pivot cercevesinin alt-sagina tasindi.
        float w = getWidth() * 0.235f;
        float x = getWidth() * 0.5f + Math.max(8f, min * 0.012f);
        float y = getHeight() * 0.660f;
        float h = Math.max(140f, getHeight() * 0.225f);
        RectF r = new RectF(x, y, x + w, y + h);
        drawGlassPanel(canvas, r, Color.argb(122, 5, 12, 24), Color.argb(142, 120, 255, 170));
        int mode = state.getSelectedMode();
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(12f, min * 0.022f));
        canvas.drawText("Sonraki mod", r.left + 14f, r.top + h * 0.20f, paint);
        paint.setColor(Color.argb(240, 0, 220, 255));
        canvas.drawText(trimMenuText(UiBalanceSystem.modeTitle(mode), 22), r.left + 14f, r.top + h * 0.40f, paint);
        paint.setColor(Color.argb(220, 235, 245, 255));
        paint.setTextSize(Math.max(9.2f, min * 0.0155f));
        canvas.drawText(trimMenuText(UiBalanceSystem.modeDescription(mode), 34), r.left + 14f, r.top + h * 0.60f, paint);
        paint.setColor(Color.argb(232, 255, 220, 90));
        canvas.drawText(trimMenuText(UiBalanceSystem.modeRewardText(mode), 34), r.left + 14f, r.top + h * 0.78f, paint);
        paint.setColor(Color.argb(205, 235, 245, 255));
        canvas.drawText(UiBalanceSystem.hudModeLabel(saveManager) + "  •  " + saveManager.getControlSensitivityLabel(), r.left + 14f, r.top + h * 0.92f, paint);
    }
