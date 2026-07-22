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

    // A64: Ana menu yerlesim sistemi (center pivot + sol navigasyon + sag profil, neon butonlar).
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
        drawTitle(canvas, "RAYCAR", "PREMIUM LOBI / A64");

        // A64: center pivot 3D sahne cercevesi ve sol navigasyon paneli.
        mainMenuLayout.drawCenterPivotFrame(canvas, paint);
        mainMenuLayout.drawNavPanel(canvas, paint, "NAVIGASYON");

        drawMainTopStatus(canvas);
        drawMainVehicleGoalCard(canvas);
        drawMainModePreview(canvas);
        drawRedeemPanel(canvas);

        // A64: sol navigasyon panelindeki 6 buton neon/espor stilinde cizilir.
        mainMenuLayout.drawNeonButton(canvas, paint, rightCars, "GARAJ / MOD", true, false);
        mainMenuLayout.drawNeonButton(canvas, paint, rightModes, "MOD HUB", true, false);
        mainMenuLayout.drawNeonButton(canvas, paint, rightCareer, "KARIYER", true, false);
        mainMenuLayout.drawNeonButton(canvas, paint, rightMaps, "HARITALAR", false, false);
        mainMenuLayout.drawNeonButton(canvas, paint, rightQuests, TaskAchievementHudSystem.pendingRewardBadge(saveManager), true, false);
        mainMenuLayout.drawNeonButton(canvas, paint, rightSettings, "AYARLAR", false, false);

        // A64: sag tarafta profil ve sosyal panelleri.
        mainMenuLayout.drawProfilePanels(canvas, paint,
                "LVL " + saveManager.getPlayerLevel() + " \u2022 " + saveManager.getCareerLeagueName(),
                "Coin: " + coinsText() + " \u2022 Kasa: " + saveManager.getRewardCrates(),
                UiBalanceSystem.economyTier(saveManager),
                "Ekip: RAYZN Garage",
                "Aktif uye: " + (saveManager.getPlayerLevel() % 4 + 1));

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(205, 255, 255, 255));
        paint.setTextSize(Math.max(11f, getHeight() * 0.018f));
        canvas.drawText("A64: Center pivot sahne, sol navigasyon ve sag profil/sosyal panelleri aktif.", getWidth() * 0.055f, getHeight() * 0.89f, paint);
        postInvalidateDelayed(80L);
    }

    private void drawMainTopStatus(Canvas canvas) {
        float min = Math.min(getWidth(), getHeight());
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
        float w = getWidth() * 0.235f;
        float cardGap = getWidth() * 0.012f;
        float x = getWidth() * 0.5f - w - cardGap;
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
        float w = getWidth() * 0.235f;
        float cardGap = getWidth() * 0.012f;
        float x = getWidth() * 0.5f + cardGap;
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

    private void drawGarage(Canvas canvas) {
        int mode = state.getGarageMode();
        if (mode == GameScreenState.GARAGE_MODE_BUY_CONFIRM) {
            drawGarageSelect(canvas);
            drawGarageBuyConfirm(canvas);
        } else if (mode == GameScreenState.GARAGE_MODE_MODIFY_HOME) {
            drawGarageModifyHome(canvas);
        } else if (mode == GameScreenState.GARAGE_MODE_PERFORMANCE) {
            drawGaragePerformance(canvas);
        } else if (mode == GameScreenState.GARAGE_MODE_TUNING) {
            drawGarageTuning(canvas);
        } else if (mode == GameScreenState.GARAGE_MODE_VISUAL) {
            drawGarageVisualList(canvas);
        } else if (mode == GameScreenState.GARAGE_MODE_VISUAL_EDIT) {
            drawGarageVisualEdit(canvas);
        } else {
            drawGarageSelect(canvas);
        }
    }

    private void drawGarageSelect(Canvas canvas) {
        drawTitle(canvas, "GARAJ", "Araç seç / satın al / modifiye et / test sürüşü");
        drawButton(canvas, backButton, "GERI", false, false);

        int selected = state.getSelectedVehicleIndex();
        String vehicleId = VehicleCatalog.id(selected);
        boolean owned = isOwned(selected);
        int price = VehicleCatalog.price(selected);
        boolean rewardUnlocked = saveManager != null && saveManager.isVehicleRewardUnlocked(vehicleId);
        boolean canAfford = saveManager != null && saveManager.getCoins() >= price;
        boolean levelUnlocked = saveManager != null && saveManager.isVehicleLevelUnlocked(selected);

        String status = showroomVehicleStatus(selected, owned, rewardUnlocked, canAfford, levelUnlocked);
        drawGarageShowroomViewportFrame(canvas, selected);
        drawShowroomHeroPanel(canvas, selected, status, price, owned, canAfford);
        drawShowroomControlStrip(canvas, selected);

        drawButton(canvas, selectButton, owned ? "SEÇILI / KULLAN" : status, owned, !owned);
        drawButton(canvas, buyButton, owned ? "SAHIP" : (!levelUnlocked ? ("LVL " + VehicleCatalog.requiredLevel(selected) + " GEREKLI") : (canAfford ? ("SATIN AL " + price) : "PARA YETERSİZ")), canAfford && levelUnlocked && !owned, owned || price <= 0 || !canAfford || !levelUnlocked);
        drawButton(canvas, infoButton, "PARA " + coinsText(), false, false);

        drawShowroomSidePanel(canvas, selected, owned);
        drawGarageComparison(canvas, selected);
        drawGarageProfessionalStatusStrip(canvas, selected, owned, levelUnlocked, canAfford, price);

        if (owned) {
            drawButton(canvas, modifyButton, "MODIFIYE ET", true, false);
            drawButton(canvas, testDriveButton, "TEST SÜRÜŞÜNE ÇIK", true, false);
            drawTestDriveChoice(canvas);
        } else {
            drawButton(canvas, modifyButton, "MODIFIYE KILITLI", false, true);
            drawButton(canvas, testDriveButton, "ÖNCE SATIN AL", false, true);
        }

        drawLastTestDriveResult(canvas);
        drawShowroomCarouselCards(canvas, selected);
        drawDots(canvas, selected);
        if (state.isGarageToTestDriveTransitionActive()) {
            drawTestDriveTransition(canvas);
        }
    }

    private void drawShowroomControlStrip(Canvas canvas, int selected) {
        drawButton(canvas, showroomRotateLeftButton, "↺", false, false);
        drawButton(canvas, showroomRotateRightButton, "↻", false, false);
        drawButton(canvas, showroomZoomOutButton, "−", false, false);
        drawButton(canvas, showroomZoomInButton, "+", false, false);
        drawButton(canvas, showroomResetButton, "SIFIRLA", false, false);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(Math.max(8.6f, getHeight() * 0.0142f));
        paint.setColor(Color.argb(210, 235, 245, 255));
        float cy = showroomResetButton.bottom + Math.max(12f, getHeight() * 0.016f);
        canvas.drawText("Showroom " + GarageShowroomSystem.zoomLabel(state.getPreviewZoom())
                + "  |  " + GarageShowroomSystem.cameraHint(state.getGarageMode(), state.getSelectedVisualModType()),
                (showroomRotateLeftButton.left + showroomResetButton.right) * 0.5f, cy, paint);
        paint.setColor(Color.argb(210, 120, 235, 255));
        canvas.drawText(trimMenuText(GarageShowroomSystem.identityLine(saveManager, selected), 50),
                (showroomRotateLeftButton.left + showroomResetButton.right) * 0.5f, cy + Math.max(14f, getHeight() * 0.019f), paint);
        paint.setColor(Color.argb(205, 255, 220, 80));
        paint.setTextSize(Math.max(7.4f, getHeight() * 0.0122f));
        canvas.drawText(trimMenuText(PostTenUpdateStabilityQaSystem.uiBadgeLine(getWidth(), getHeight()), 42),
                (showroomRotateLeftButton.left + showroomResetButton.right) * 0.5f, cy + Math.max(27f, getHeight() * 0.036f), paint);
    }

    private void drawTestDriveChoice(Canvas canvas) {
        int index = saveManager == null ? 0 : saveManager.getTestDriveChallengeIndex();
        drawGlassPanel(canvas, testDriveChoicePanel, Color.argb(115, 4, 12, 25), Color.argb(135, 0, 220, 255));
        drawButton(canvas, testDrivePrevButton, "‹", false, false);
        drawButton(canvas, testDriveNextButton, "›", false, false);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(235, 230, 248, 255));
        paint.setTextSize(Math.max(9.2f, Math.min(getWidth(), getHeight()) * 0.0152f));
        canvas.drawText("Test: " + trimMenuText(TestDriveChallengeSystem.challengeLabel(index), 24),
                testDriveChoicePanel.centerX(), testDriveChoicePanel.centerY() + Math.max(3.5f, Math.min(getWidth(), getHeight()) * 0.006f), paint);
    }

    private void drawLastTestDriveResult(Canvas canvas) {
        String result = state.getLastTestDriveResult();
        int reward = state.getLastTestDriveReward();
        if ((result == null || result.length() == 0) && saveManager != null) {
            result = saveManager.getLastTestDriveResult();
            reward = saveManager.getLastTestDriveReward();
        }
        if (result == null || result.length() == 0) return;
        float min = Math.min(getWidth(), getHeight());
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(218, 120, 245, 255));
        paint.setTextSize(Math.max(8.8f, min * 0.0148f));
        canvas.drawText("Son test: " + trimMenuText(result, 34) + "  +" + reward + " coin",
                testDriveButton.left, testDriveChoicePanel.bottom + Math.max(14f, min * 0.022f), paint);
    }

    private void drawTestDriveTransition(Canvas canvas) {
        float progress = state.getGarageToTestDriveTransitionProgress();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb((int)(150 + 70 * progress), 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

        float min = Math.min(getWidth(), getHeight());
        float panelW = Math.max(310f, getWidth() * 0.40f);
        float panelH = Math.max(118f, getHeight() * 0.18f);
        RectF panel = new RectF((getWidth() - panelW) * 0.5f, getHeight() * 0.40f, (getWidth() + panelW) * 0.5f, getHeight() * 0.40f + panelH);
        drawGlassPanel(canvas, panel, Color.argb(210, 3, 10, 24), Color.argb(235, 0, 225, 255));

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(17f, min * 0.030f));
        canvas.drawText("TEST SÜRÜŞÜ HAZIRLANIYOR", panel.centerX(), panel.top + panelH * 0.36f, paint);
        paint.setColor(Color.argb(220, 235, 245, 255));
        paint.setTextSize(Math.max(10f, min * 0.017f));
        int v = state.getGarageToTestDriveVehicleIndex();
        canvas.drawText(trimMenuText(GameScreenState.vehicleLabel(v), 34), panel.centerX(), panel.top + panelH * 0.58f, paint);

        float barW = panelW * 0.70f;
        float barH = Math.max(6f, min * 0.010f);
        RectF bg = new RectF(panel.centerX() - barW * 0.5f, panel.top + panelH * 0.76f, panel.centerX() + barW * 0.5f, panel.top + panelH * 0.76f + barH);
        paint.setColor(Color.argb(75, 255, 255, 255));
        canvas.drawRoundRect(bg, barH, barH, paint);
        RectF fg = new RectF(bg.left, bg.top, bg.left + bg.width() * progress, bg.bottom);
        paint.setColor(Color.argb(230, 0, 220, 255));
        canvas.drawRoundRect(fg, barH, barH, paint);

        if (progress >= 1f) {
            if (saveManager != null) saveManager.setSelectedMap(GameScreenState.MAP_OPEN_FIELD);
            state.completeGarageToTestDriveTransition();
            if (listener != null) listener.onScreenChanged(GameScreenState.SCREEN_DRIVE);
        } else {
            postInvalidateOnAnimation();
        }
    }

    private String showroomVehicleStatus(int selected, boolean owned, boolean rewardUnlocked, boolean canAfford, boolean levelUnlocked) {
        if (owned) return "SEÇİLİ";
        if (!levelUnlocked) return "LVL " + VehicleCatalog.requiredLevel(selected) + " GEREKLI";
        if (!canAfford) return "PARA YETERSİZ";
        if (!rewardUnlocked && VehicleCatalog.isLocked(selected) && VehicleCatalog.price(selected) <= 0) return "KİLİTLİ";
        return "SATIN AL";
    }

    private void drawGarageShowroomViewportFrame(Canvas canvas, int selected) {
        float min = Math.min(getWidth(), getHeight());
        RectF frame = new RectF(getWidth() * 0.330f, getHeight() * 0.188f, getWidth() * 0.705f, getHeight() * 0.690f);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(1.2f, min * 0.0021f));
        paint.setColor(Color.argb(62, 0, 220, 255));
        canvas.drawRoundRect(frame, 28f, 28f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(34, 0, 180, 255));
        RectF topChip = new RectF(frame.left + 12f, frame.top + 10f, frame.left + Math.max(185f, frame.width() * 0.52f), frame.top + Math.max(30f, min * 0.042f));
        canvas.drawRoundRect(topChip, 18f, 18f, paint);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(178, 220, 245, 255));
        paint.setTextSize(Math.max(8.0f, Math.min(10.8f, min * 0.0135f)));
        canvas.drawText("GERÇEK 3D SHOWROOM • " + trimMenuText(GarageInfrastructureSystem.modelRepairHintLine(selected), 38), topChip.left + 10f, topChip.centerY() + 3.5f, paint);
    }

    private void drawShowroomHeroPanel(Canvas canvas, int selected, String status, int price, boolean owned, boolean canAfford) {
        float min = Math.min(getWidth(), getHeight());
        float x = getWidth() * 0.050f;
        float y = getHeight() * 0.145f;
        float w = Math.max(250f, getWidth() * 0.270f);
        float h = Math.max(126f, Math.min(getHeight() * 0.205f, min * 0.210f));
        RectF panel = new RectF(x, y, x + w, y + h);
        drawGlassPanel(canvas, panel, Color.argb(138, 4, 12, 26), Color.argb(178, 0, 220, 255));

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(15f, Math.min(24f, min * 0.026f)));
        canvas.drawText(trimMenuText(GameScreenState.vehicleLabel(selected), 24), x + w * 0.065f, y + h * 0.25f, paint);

        int statusColor = owned ? Color.argb(245, 90, 255, 155)
                : (canAfford ? Color.argb(245, 255, 220, 85) : Color.argb(230, 255, 95, 95));
        paint.setColor(statusColor);
        paint.setTextSize(Math.max(9.4f, Math.min(14f, min * 0.016f)));
        canvas.drawText(status + "  •  " + VehicleCatalog.className(selected)
                + "  •  Sınıf x" + formatOne(VehicleCatalog.performanceClass(selected)), x + w * 0.065f, y + h * 0.43f, paint);

        paint.setColor(Color.argb(225, 235, 245, 255));
        paint.setTextSize(Math.max(8.7f, Math.min(12.2f, min * 0.0143f)));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.selectionFoundationLine(saveManager, selected), 44), x + w * 0.065f, y + h * 0.60f, paint);
        paint.setColor(Color.argb(210, 120, 235, 255));
        canvas.drawText(trimMenuText(GarageShowroomSystem.showroomIntro(saveManager, selected), 42), x + w * 0.065f, y + h * 0.75f, paint);
        paint.setColor(Color.argb(214, 255, 210, 90));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.stabilityRepairLine(saveManager, selected), 42), x + w * 0.065f, y + h * 0.90f, paint);
    }

    private void drawGlassPanel(Canvas canvas, RectF r, int fillColor, int strokeColor) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(fillColor);
        canvas.drawRoundRect(r, 24f, 24f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.2f);
        paint.setColor(strokeColor);
        canvas.drawRoundRect(r, 24f, 24f, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawShowroomCarouselCards(Canvas canvas, int selected) {
        float min = Math.min(getWidth(), getHeight());
        RectF band = garageCarouselSwipeZone;
        drawGlassPanel(canvas, band, Color.argb(86, 4, 12, 26), Color.argb(104, 0, 220, 255));

        drawButton(canvas, garagePrevVehicleButton, "‹", false, false);
        drawButton(canvas, garageNextVehicleButton, "›", false, false);

        for (int slot = -2; slot <= 2; slot++) {
            int visualSlot = slot + 2;
            int index = GarageCarouselSystem.indexForSlot(selected, slot);
            RectF r = garageVehicleCardButtons[visualSlot];
            boolean center = slot == 0;
            boolean owned = isOwned(index);
            int fill = center ? Color.argb(194, 0, 150, 255) : Color.argb(104, 8, 16, 32);
            int stroke = center ? Color.argb(238, 120, 235, 255) : Color.argb(118, 255, 255, 255);
            drawGlassPanel(canvas, r, fill, stroke);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setColor(center ? Color.WHITE : Color.argb(220, 230, 240, 250));
            paint.setTextSize(Math.max(7.4f, Math.min(10.3f, min * 0.0122f)));
            canvas.drawText(trimMenuText(GameScreenState.vehicleLabel(index), 14), r.centerX(), r.top + r.height() * 0.35f, paint);
            paint.setTextSize(Math.max(6.7f, Math.min(9.0f, min * 0.0110f)));
            paint.setColor(owned ? Color.argb(232, 90, 255, 155) : Color.argb(222, 255, 205, 82));
            canvas.drawText(owned ? "SAHIP" : (VehicleCatalog.price(index) + " coin"), r.centerX(), r.top + r.height() * 0.64f, paint);
            paint.setColor(center ? Color.argb(235, 255, 235, 120) : Color.argb(180, 160, 190, 210));
            canvas.drawText((index + 1) + "/" + GameScreenState.vehicleCount(), r.centerX(), r.top + r.height() * 0.84f, paint);
        }
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.argb(218, 220, 235, 245));
        paint.setTextSize(Math.max(7.2f, Math.min(9.8f, min * 0.0120f)));
        canvas.drawText(trimMenuText(GarageCarouselSystem.statusLine(selected), 70), band.centerX(), band.bottom - Math.max(20f, min * 0.028f), paint);
        paint.setColor(Color.argb(205, 120, 235, 255));
        canvas.drawText(trimMenuText(GarageCarouselSystem.cardHintLine(selected), 70), band.centerX(), band.bottom - Math.max(8f, min * 0.012f), paint);
    }

    private void drawShowroomSidePanel(Canvas canvas, int selected, boolean owned) {
        float min = Math.min(getWidth(), getHeight());
        float x = getWidth() * 0.725f;
        float y = getHeight() * 0.158f;
        float w = getWidth() - x - Math.max(18f, min * 0.035f);
        float h = getHeight() * 0.452f;
        RectF panel = new RectF(x, y, x + w, y + h);
        drawGlassPanel(canvas, panel, Color.argb(116, 5, 12, 24), Color.argb(130, 0, 220, 255));
        drawGarageSideStats(canvas, selected);
        if (!owned) {
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(Color.argb(220, 255, 210, 90));
            paint.setTextSize(Math.max(8.2f, Math.min(10.8f, min * 0.0135f)));
            canvas.drawText("Modifiye için önce aracı satın al.", x + 12f, panel.bottom - 10f, paint);
        }
    }

    private void drawGarageComparison(Canvas canvas, int selected) {
        int base = state.getGarageComparisonBaseVehicleIndex();
        if (base < 0 || base >= VehicleCatalog.count() || base == selected) return;
        VehicleUpgradeSystem.Stats a = VehicleUpgradeSystem.buildStats(saveManager, selected);
        VehicleUpgradeSystem.Stats b = VehicleUpgradeSystem.buildStats(saveManager, base);
        float x = getWidth() * 0.055f;
        float y = getHeight() * 0.525f;
        float w = Math.max(265f, getWidth() * 0.315f);
        float h = Math.max(122f, getHeight() * 0.175f);
        RectF panel = new RectF(x, y, x + w, y + h);
        drawGlassPanel(canvas, panel, Color.argb(118, 4, 12, 26), Color.argb(118, 255, 255, 255));

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(238, 0, 225, 255));
        paint.setTextSize(Math.max(11f, getHeight() * 0.019f));
        canvas.drawText("KARŞILAŞTIRMA", x + w * 0.065f, y + h * 0.20f, paint);
        paint.setColor(Color.argb(205, 235, 245, 255));
        paint.setTextSize(Math.max(8.5f, getHeight() * 0.0145f));
        canvas.drawText("Baz: " + trimMenuText(GameScreenState.vehicleLabel(base), 22), x + w * 0.065f, y + h * 0.36f, paint);

        float rowY = y + h * 0.55f;
        drawComparisonMetric(canvas, "Hız", a.speed - b.speed, x + w * 0.065f, rowY);
        drawComparisonMetric(canvas, "Hızlanma", a.acceleration - b.acceleration, x + w * 0.53f, rowY);
        rowY += h * 0.18f;
        drawComparisonMetric(canvas, "Tutuş", a.handling - b.handling, x + w * 0.065f, rowY);
        drawComparisonMetric(canvas, "Fren", a.brake - b.brake, x + w * 0.53f, rowY);
        rowY += h * 0.18f;
        drawComparisonMetric(canvas, "Nitro", a.nitro - b.nitro, x + w * 0.065f, rowY);
        drawComparisonMetric(canvas, "Drift", a.drift - b.drift, x + w * 0.53f, rowY);
    }

    private void drawComparisonMetric(Canvas canvas, String label, int diff, float x, float y) {
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(205, 230, 240, 250));
        paint.setTextSize(Math.max(8.8f, getHeight() * 0.0148f));
        canvas.drawText(label, x, y, paint);
        paint.setColor(diffColor(diff));
        paint.setTextSize(Math.max(9.8f, getHeight() * 0.0165f));
        canvas.drawText(diffText(diff), x + Math.max(62f, getWidth() * 0.060f), y, paint);
    }

    private static String diffText(int diff) {
        if (diff > 0) return "+" + diff;
        return String.valueOf(diff);
    }

    private static int diffColor(int diff) {
        if (diff > 0) return Color.argb(240, 90, 255, 155);
        if (diff < 0) return Color.argb(235, 255, 105, 95);
        return Color.argb(220, 210, 220, 230);
    }

    private void drawGarageBuyConfirm(Canvas canvas) {
        int selected = state.getPendingBuyVehicleIndex();
        if (selected < 0 || selected >= VehicleCatalog.count()) selected = state.getSelectedVehicleIndex();
        int price = VehicleCatalog.price(selected);
        int coins = saveManager == null ? 0 : saveManager.getCoins();
        int after = Math.max(0, coins - Math.max(0, price));
        boolean canBuy = saveManager != null && !isOwned(selected) && coins >= price;

        drawGlassPanel(canvas, buyConfirmPanel, Color.argb(232, 3, 9, 20), canBuy ? Color.argb(215, 0, 225, 255) : Color.argb(190, 255, 90, 90));

        float x = buyConfirmPanel.left + Math.max(18f, buyConfirmPanel.width() * 0.070f);
        float y = buyConfirmPanel.top + Math.max(34f, buyConfirmPanel.height() * 0.150f);
        float line = Math.max(20f, buyConfirmPanel.height() * 0.105f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(17f, getHeight() * 0.030f));
        canvas.drawText("SATIN ALMA ONAYI", x, y, paint);
        paint.setTextSize(Math.max(11f, getHeight() * 0.019f));
        paint.setColor(Color.argb(225, 235, 245, 255));
        canvas.drawText(trimMenuText(GameScreenState.vehicleLabel(selected), 34), x, y + line * 1.25f, paint);
        canvas.drawText("Fiyat: " + price + " coin", x, y + line * 2.25f, paint);
        canvas.drawText("Mevcut para: " + coins + " coin", x, y + line * 3.15f, paint);
        paint.setColor(canBuy ? Color.argb(240, 90, 255, 155) : Color.argb(235, 255, 110, 95));
        canvas.drawText(canBuy ? ("Satın alma sonrası: " + after + " coin") : "Para yetersiz: satın alma yapılamaz", x, y + line * 4.05f, paint);
        paint.setColor(Color.argb(190, 220, 235, 245));
        paint.setTextSize(Math.max(9.4f, getHeight() * 0.016f));
        canvas.drawText(canBuy ? "Onaylanırsa araç seçilecek ve modifiye butonu aktif olacak." : "Daha fazla coin kazan veya daha ucuz araç seç.", x, y + line * 5.00f, paint);

        drawButton(canvas, buyCancelButton, "VAZGEÇ", false, false);
        drawButton(canvas, buyConfirmButton, canBuy ? "ONAYLA" : "YETERSİZ", true, !canBuy);
    }

    private void drawGarageSideStats(Canvas canvas, int vehicleIndex) {
        String id = VehicleCatalog.id(vehicleIndex);
        VehicleUpgradeSystem.Stats stats = VehicleUpgradeSystem.buildStats(saveManager, vehicleIndex);
        float min = Math.min(getWidth(), getHeight());
        float panelLeft = getWidth() * 0.725f;
        float panelTop = getHeight() * 0.158f;
        float panelRight = getWidth() - Math.max(18f, min * 0.035f);
        float x = panelLeft + 13f;
        float y = panelTop + Math.max(25f, min * 0.034f);
        float line = Math.max(14f, Math.min(19f, min * 0.020f));
        float barW = Math.max(100f, panelRight - x - 16f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(238, 255, 235, 120));
        paint.setTextSize(Math.max(10.0f, Math.min(13.2f, min * 0.0158f)));
        canvas.drawText("ARAÇ İSTATİSTİKLERİ", x, y, paint);
        float statY = y + line * 1.10f;
        drawStatBar(canvas, "Hız", stats.speed, 150, x, statY, barW); statY += line;
        drawStatBar(canvas, "Hızlanma", stats.acceleration, 150, x, statY, barW); statY += line;
        drawStatBar(canvas, "Tutuş", stats.handling, 150, x, statY, barW); statY += line;
        drawStatBar(canvas, "Fren", stats.brake, 150, x, statY, barW); statY += line;
        drawStatBar(canvas, "Drift", stats.drift, 150, x, statY, barW); statY += line * 1.15f;

        paint.setTextSize(Math.max(7.4f, Math.min(9.6f, min * 0.0118f)));
        paint.setColor(damageColor(saveManager.getVehicleHealth(id)));
        canvas.drawText("Can " + percent(saveManager.getVehicleHealth(id))
                + "  Motor " + percent(1f - saveManager.getVehicleMotorDamage(id)), x, statY, paint);
        statY += line * 0.90f;
        paint.setColor(Color.argb(200, 235, 245, 255));
        canvas.drawText("Lastik " + percent(1f - saveManager.getVehicleTireDamage(id))
                + "  Kaporta " + percent(saveManager.getVehicleBodyDamage(id)), x, statY, paint);
        statY += line * 1.05f;
        paint.setColor(Color.argb(205, 255, 235, 120));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.catalogFoundationLine(vehicleIndex), 46), x, statY, paint);
        statY += line * 0.88f;
        paint.setColor(Color.argb(190, 120, 235, 255));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.showroomFoundationLine(vehicleIndex), 46), x, statY, paint);
        statY += line * 0.88f;
        paint.setColor(Color.argb(205, 255, 170, 90));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.stabilityRepairLine(saveManager, vehicleIndex), 46), x, statY, paint);
        statY += line * 0.88f;
        paint.setColor(Color.argb(200, 160, 255, 190));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.postTenFullGameFlowQaLine(saveManager, vehicleIndex), 46), x, statY, paint);
    }

    private void drawStatBar(Canvas canvas, String label, int value, int max, float x, float y, float totalWidth) {
        float min = Math.min(getWidth(), getHeight());
        float labelW = Math.max(58f, Math.min(78f, totalWidth * 0.38f));
        float barW = Math.max(72f, totalWidth - labelW - 8f);
        float barH = Math.max(4f, min * 0.0065f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(215, 235, 245, 255));
        paint.setTextSize(Math.max(7.2f, Math.min(9.6f, min * 0.0118f)));
        canvas.drawText(label + " " + value, x, y, paint);
        float bx = x + labelW;
        float by = y - barH * 1.35f;
        RectF bg = new RectF(bx, by, bx + barW, by + barH);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(92, 255, 255, 255));
        canvas.drawRoundRect(bg, barH, barH, paint);
        float ratio = Math.max(0f, Math.min(1f, value / (float) Math.max(1, max)));
        RectF fg = new RectF(bg.left, bg.top, bg.left + bg.width() * ratio, bg.bottom);
        paint.setColor(Color.argb(210, 0, 220, 255));
        canvas.drawRoundRect(fg, barH, barH, paint);
    }

    private void drawStatBar(Canvas canvas, String label, int value, int max, float x, float y) {
        float min = Math.min(getWidth(), getHeight());
        float labelW = Math.max(64f, getWidth() * 0.060f);
        float barW = Math.max(92f, getWidth() * 0.120f);
        float barH = Math.max(5f, min * 0.008f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(215, 235, 245, 255));
        paint.setTextSize(Math.max(8.5f, getHeight() * 0.0145f));
        canvas.drawText(label + " " + value, x, y, paint);
        float bx = x + labelW;
        float by = y - barH * 1.35f;
        RectF bg = new RectF(bx, by, bx + barW, by + barH);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(92, 255, 255, 255));
        canvas.drawRoundRect(bg, barH, barH, paint);
        float ratio = Math.max(0f, Math.min(1f, value / (float) Math.max(1, max)));
        RectF fg = new RectF(bx, by, bx + barW * ratio, by + barH);
        paint.setColor(Color.argb(210, 0, 220, 255));
        canvas.drawRoundRect(fg, barH, barH, paint);
    }

    private void drawGarageModifyHome(Canvas canvas) {
        drawTitle(canvas, "MODİFİYE ATÖLYESİ", "Performans + tuning + görsel kişiselleştirme merkezi");
        drawButton(canvas, backButton, "GERİ", false, false);
        int selected = state.getSelectedVehicleIndex();
        String id = VehicleCatalog.id(selected);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(17f, getHeight() * 0.030f));
        canvas.drawText(GameScreenState.vehicleLabel(selected), getWidth() * 0.50f, getHeight() * 0.135f, paint);

        paint.setTextSize(Math.max(9.0f, Math.min(13f, getHeight() * 0.014f)));
        paint.setColor(Color.argb(200, 235, 245, 255));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.workshopFoundationLine(saveManager, selected), 74), getWidth() * 0.50f, getHeight() * 0.535f, paint);
        paint.setColor(Color.argb(205, 160, 255, 190));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.postTenFullGameFlowQaLine(saveManager, selected), 74), getWidth() * 0.50f, getHeight() * 0.558f, paint);

        int repairCost = saveManager.getRepairCost(id);
        drawModificationWorkshopRack(canvas, selected, id, repairCost);
        for (int i = 0; i < modifyCategoryButtons.length; i++) {
            boolean primary = i == MOD_CATEGORY_PERFORMANCE;
            boolean disabled = i == MOD_CATEGORY_REPAIR && repairCost <= 0;
            drawModifyCategoryCard(canvas, modifyCategoryButtons[i],
                    GarageModificationUiSystem.categoryTitle(i, repairCost),
                    GarageModificationUiSystem.categorySubtitle(i, repairCost),
                    GarageModificationUiSystem.categoryStatus(saveManager, id, selected, i, repairCost),
                    primary, disabled);
        }

        drawGarageBuildFoundationPanel(canvas, selected, id);
        drawWorkshopActionHintPanel(canvas, selected, id);
        drawWorkshopBottomStatusPanel(canvas, selected, id);
        drawButton(canvas, testDriveButton, "TEST SÜRÜŞÜ", true, false);
    }

    private void drawGarageBuildFoundationPanel(Canvas canvas, int vehicleIndex, String vehicleId) {
        float x = getWidth() * 0.705f;
        float y = getHeight() * 0.535f;
        float w = getWidth() * 0.245f;
        float h = getHeight() * 0.145f;
        RectF panel = new RectF(x, y, Math.min(getWidth() - 18f, x + w), y + h);
        drawGlassPanel(canvas, panel, Color.argb(116, 4, 12, 26), Color.argb(135, 0, 220, 255));
        VehicleUpgradeSystem.Stats stats = VehicleUpgradeSystem.buildStats(saveManager, vehicleIndex);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(238, 255, 235, 120));
        paint.setTextSize(Math.max(9.2f, getHeight() * 0.0155f));
        canvas.drawText("BUILD ÖZETİ", panel.left + 12f, panel.top + h * 0.24f, paint);
        paint.setColor(Color.argb(220, 235, 245, 255));
        paint.setTextSize(Math.max(8.4f, getHeight() * 0.014f));
        canvas.drawText("Puan " + VehicleUpgradeSystem.buildScore(saveManager, vehicleIndex) + "/100  |  Para " + coinsText(), panel.left + 12f, panel.top + h * 0.46f, paint);
        canvas.drawText("Hız " + stats.speed + "  Hızlanma " + stats.acceleration + "  Fren " + stats.brake, panel.left + 12f, panel.top + h * 0.66f, paint);
        canvas.drawText("Tutuş " + stats.handling + "  N2O " + stats.nitro + "  Dayan. " + stats.durability, panel.left + 12f, panel.top + h * 0.76f, paint);
        paint.setColor(Color.argb(215, 120, 235, 255));
        paint.setTextSize(Math.max(7.5f, getHeight() * 0.0125f));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.drivingFoundationLine(saveManager, vehicleIndex), 43), panel.left + 12f, panel.top + h * 0.92f, paint);
    }

    private void drawModificationWorkshopRack(Canvas canvas, int vehicleIndex, String vehicleId, int repairCost) {
        if (modifyCategoryButtons.length == 0) return;
        RectF first = modifyCategoryButtons[0];
        RectF last = modifyCategoryButtons[modifyCategoryButtons.length - 1];
        float min = Math.min(getWidth(), getHeight());
        RectF panel = new RectF(first.left - Math.max(8f, min * 0.012f),
                first.top - Math.max(26f, min * 0.040f),
                last.right + Math.max(8f, min * 0.012f),
                last.bottom + Math.max(10f, min * 0.014f));
        drawGlassPanel(canvas, panel, Color.argb(92, 4, 12, 26), Color.argb(118, 0, 220, 255));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(240, 255, 235, 120));
        paint.setTextSize(Math.max(9.2f, min * 0.0145f));
        canvas.drawText("PRO ATÖLYE KATEGORİLERİ", panel.left + 12f, first.top - Math.max(9f, min * 0.014f), paint);
        paint.setColor(Color.argb(215, 235, 245, 255));
        paint.setTextSize(Math.max(7.4f, min * 0.0118f));
        canvas.drawText(trimMenuText(GarageModificationUiSystem.workshopStageLine(saveManager, vehicleIndex), 76),
                panel.left + 12f, first.top + Math.max(7f, min * 0.011f), paint);
    }

    private void drawModifyCategoryCard(Canvas canvas, RectF r, String title, String subtitle, String status, boolean primary, boolean disabled) {
        paint.setStyle(Paint.Style.FILL);
        int bg = disabled ? Color.argb(74, 80, 86, 96) : (primary ? Color.argb(188, 0, 150, 255) : Color.argb(128, 12, 18, 30));
        paint.setColor(bg);
        canvas.drawRoundRect(r, 20f, 20f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(primary ? 2.6f : 1.6f);
        paint.setColor(primary ? Color.argb(230, 120, 235, 255) : Color.argb(145, 255, 255, 255));
        canvas.drawRoundRect(r, 20f, 20f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(disabled ? Color.argb(175, 210, 210, 210) : Color.WHITE);
        paint.setTextSize(Math.max(8.9f, r.height() * 0.205f));
        canvas.drawText(trimMenuText(title, 13), r.left + r.width() * 0.08f, r.top + r.height() * 0.34f, paint);
        paint.setColor(disabled ? Color.argb(145, 210, 210, 210) : Color.argb(208, 220, 240, 255));
        paint.setTextSize(Math.max(7.0f, r.height() * 0.150f));
        canvas.drawText(trimMenuText(subtitle, 22), r.left + r.width() * 0.08f, r.top + r.height() * 0.57f, paint);
        float chipH = Math.max(13f, r.height() * 0.22f);
        float chipW = Math.min(r.width() * 0.76f, Math.max(58f, r.width() * 0.58f));
        RectF chip = new RectF(r.left + r.width() * 0.08f, r.bottom - chipH - r.height() * 0.08f, r.left + r.width() * 0.08f + chipW, r.bottom - r.height() * 0.08f);
        paint.setColor(primary ? Color.argb(205, 255, 235, 120) : Color.argb(disabled ? 100 : 155, 0, 220, 255));
        canvas.drawRoundRect(chip, chipH * 0.5f, chipH * 0.5f, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(6.2f, chip.height() * 0.42f));
        Paint.FontMetrics fm = paint.getFontMetrics();
        canvas.drawText(trimMenuText(status, 16), chip.centerX(), chip.centerY() - (fm.ascent + fm.descent) * 0.5f, paint);
    }

    private void drawGaragePerformance(Canvas canvas) {
        drawTitle(canvas, "PRO PERFORMANS", "Onaylı yükseltme / maliyet / test sürüşü döngüsü");
        drawButton(canvas, backButton, "MODIFIYE", false, false);
        int selected = state.getSelectedVehicleIndex();
        drawGarageMiniHeader(canvas, selected);
        drawPerformanceStatsCompact(canvas, selected, getWidth() * 0.705f, getHeight() * 0.245f);
        drawPerformanceUpgradeGrid(canvas, selected);
        drawPerformanceEffectLegend(canvas);
        drawButton(canvas, testDriveButton, "TEST SÜRÜŞÜ", true, false);
        if (pendingPerformanceUpgradeType >= 0) drawPerformanceUpgradeConfirm(canvas, selected, pendingPerformanceUpgradeType);
    }

    private void drawGarageTuning(Canvas canvas) {
        drawTitle(canvas, "PRO TUNING", "Kaydedilmiş sürüş profili / test sürüşü döngüsü");
        drawButton(canvas, backButton, "MODIFIYE", false, false);
        int selected = state.getSelectedVehicleIndex();
        drawGarageMiniHeader(canvas, selected);
        drawDetailedTuningPanel(canvas, selected);
        drawButton(canvas, testDriveButton, "TEST SÜRÜŞÜ", true, false);
    }

    private void drawGarageVisualList(Canvas canvas) {
        int selected = state.getSelectedVehicleIndex();
        int group = state.getSelectedVisualGroup();
        drawTitle(canvas, visualGroupTitle(group), visualGroupSubtitle(group));
        drawButton(canvas, backButton, "ATÖLYE", false, false);
        drawGarageMiniHeader(canvas, selected);
        drawVisualCustomizationPanel(canvas, selected);
    }

    private void drawGarageVisualEdit(Canvas canvas) {
        int selected = state.getSelectedVehicleIndex();
        String id = VehicleCatalog.id(selected);
        int type = state.getSelectedVisualModType();
        int saved = saveManager.getVisualModValue(id, type);
        int current = state.getVisualEditValueOr(saved, type);
        int original = state.getVisualEditOriginalValue();
        int max = VisualCustomizationSystem.maxValue(type);
        boolean dirty = current != original;
        boolean ownsCurrent = saveManager == null || saveManager.isVisualModOptionOwned(id, type, current) || current == saved;
        int cost = VisualCustomizationSystem.optionCost(type, current);
        boolean levelLockedCurrent = !VehicleCustomizationSystem.isOptionLevelUnlocked(saveManager, type, current);
        boolean canBuyCurrent = saveManager != null && saveManager.getCoins() >= cost && !levelLockedCurrent;
        drawTitle(canvas, VisualCustomizationSystem.label(type).toUpperCase(), "Görsel modifiye / canlı ön izleme / satın al ve kaydet");
        drawButton(canvas, backButton, "PARÇA", false, false);

        drawVisualEditOptionRack(canvas, type, current, saved, ownsCurrent, dirty);

        int count = Math.min(visualValueButtons.length, max + 1);
        for (int i = 0; i < count; i++) {
            boolean owned = saveManager == null || saveManager.isVisualModOptionOwned(id, type, i) || i == saved;
            int optionCost = VisualCustomizationSystem.optionCost(type, i);
            boolean levelLocked = !owned && !VehicleCustomizationSystem.isOptionLevelUnlocked(saveManager, type, i);
            boolean coinLocked = !owned && saveManager != null && saveManager.getCoins() < optionCost;
            boolean locked = levelLocked || coinLocked;
            String status;
            if (i == current) {
                if (owned) status = "AKTİF";
                else if (levelLocked) status = VehicleCustomizationSystem.lockLabel(saveManager, type, i);
                else status = coinLocked ? "PARA YETERSİZ" : ("SATIN AL $" + optionCost);
            } else if (owned) status = "SAHİP / UYGULA";
            else if (levelLocked) status = VehicleCustomizationSystem.lockLabel(saveManager, type, i);
            else status = coinLocked ? ("KİLİTLİ $" + optionCost) : ("SATIN AL $" + optionCost);
            int swatch = VisualCustomizationSystem.previewColor(type, i);
            drawVisualOptionButton(canvas, visualValueButtons[i], VisualCustomizationSystem.valueLabel(type, i), status, i == current, locked, swatch, VisualCustomizationSystem.isColorLike(type) || type == VisualCustomizationSystem.PAINT_FINISH);
        }

        drawButton(canvas, visualResetButton, "SIFIRLA", false, current == 0);
        drawButton(canvas, visualUndoButton, "GERİ AL", false, !dirty);
        String saveText;
        boolean saveDisabled;
        if (!ownsCurrent && levelLockedCurrent) {
            saveText = VehicleCustomizationSystem.lockLabel(saveManager, type, current);
            saveDisabled = true;
        } else if (!ownsCurrent && !canBuyCurrent) {
            saveText = "PARA YETERSİZ";
            saveDisabled = true;
        } else if (!ownsCurrent) {
            saveText = "AL & KAYDET $" + cost;
            saveDisabled = false;
        } else if (dirty) {
            saveText = "KAYDET";
            saveDisabled = false;
        } else {
            saveText = "AKTİF";
            saveDisabled = true;
        }
        drawButton(canvas, visualSaveButton, saveText, true, saveDisabled);
        drawButton(canvas, testDriveButton, dirty ? "KAYDET & TEST" : "TEST SÜRÜŞÜ", true, false);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(dirty ? Color.argb(235, 255, 235, 120) : Color.argb(210, 160, 255, 185));
        paint.setTextSize(Math.max(11.5f, getHeight() * 0.019f));
        String stateText = ownsCurrent ? "SAHİP" : (levelLockedCurrent ? VehicleCustomizationSystem.lockLabel(saveManager, type, current) : (canBuyCurrent ? "SATIN ALINABİLİR" : "KİLİTLİ"));
        canvas.drawText(VisualModificationSaveFlowSystem.actionStateLine(saveManager, id, type, current, saved)
                + "  |  " + stateText,
                getWidth() * 0.50f, getHeight() * 0.790f, paint);

        drawVisualEditStatusPanel(canvas, type, dirty, ownsCurrent, canBuyCurrent, levelLockedCurrent, current, cost);
        paint.setTextSize(Math.max(8.2f, getHeight() * 0.0132f));
        paint.setColor(Color.argb(205, 235, 245, 255));
        canvas.drawText(visualFocusHelp(type), getWidth() * 0.50f, getHeight() * 0.925f, paint);
        canvas.drawText(VisualCustomizationSystem.editHint(type), getWidth() * 0.50f, getHeight() * 0.952f, paint);
    }

    private void drawVisualEditOptionRack(Canvas canvas, int type, int current, int saved, boolean ownsCurrent, boolean dirty) {
        if (visualValueButtons.length == 0) return;
        float min = Math.min(getWidth(), getHeight());
        RectF first = visualValueButtons[0];
        RectF last = visualValueButtons[visualValueButtons.length - 1];
        RectF panel = new RectF(first.left - Math.max(8f, min * 0.012f),
                first.top - Math.max(36f, min * 0.052f),
                last.right + Math.max(8f, min * 0.012f),
                last.bottom + Math.max(12f, min * 0.016f));
        drawGlassPanel(canvas, panel, dirty ? Color.argb(100, 40, 28, 8) : Color.argb(88, 4, 12, 26),
                dirty ? Color.argb(150, 255, 220, 80) : Color.argb(126, 0, 220, 255));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(238, 255, 210, 110));
        paint.setTextSize(Math.max(9.0f, getHeight() * 0.0145f));
        canvas.drawText("SEÇENEK RAFI", first.left, first.top - Math.max(18f, getHeight() * 0.024f), paint);
        paint.setColor(Color.argb(218, 235, 245, 255));
        paint.setTextSize(Math.max(7.2f, getHeight() * 0.0118f));
        canvas.drawText(trimMenuText(GarageModificationUiSystem.visualEditFlowLine(type, current, saved, ownsCurrent, dirty), 84),
                first.left, first.top - Math.max(5f, getHeight() * 0.007f), paint);
    }

    private void drawVisualOptionButton(Canvas canvas, RectF r, String title, String status, boolean selected, boolean disabled, int swatchColor, boolean showSwatch) {
        paint.setStyle(Paint.Style.FILL);
        int bg = disabled ? Color.argb(76, 80, 86, 96) : (selected ? Color.argb(190, 0, 170, 255) : Color.argb(124, 10, 16, 28));
        paint.setColor(bg);
        canvas.drawRoundRect(r, 18f, 18f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(selected ? 2.6f : 1.4f);
        paint.setColor(selected ? Color.argb(230, 120, 235, 255) : Color.argb(138, 255, 255, 255));
        canvas.drawRoundRect(r, 18f, 18f, paint);
        paint.setStyle(Paint.Style.FILL);

        float textLeft = r.left + r.width() * 0.08f;
        if (showSwatch) {
            float radius = Math.max(5f, Math.min(r.width(), r.height()) * 0.145f);
            float cx = r.left + radius * 1.9f;
            float cy = r.centerY();
            paint.setColor(swatchColor);
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(selected ? 2.1f : 1.2f);
            paint.setColor(selected ? Color.WHITE : Color.argb(160, 255, 255, 255));
            canvas.drawCircle(cx, cy, radius, paint);
            paint.setStyle(Paint.Style.FILL);
            textLeft = cx + radius * 1.45f;
        }

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(disabled ? Color.argb(174, 210, 210, 210) : Color.WHITE);
        paint.setTextSize(Math.max(8.6f, r.height() * 0.255f));
        canvas.drawText(trimMenuText(title, 13), textLeft, r.top + r.height() * 0.42f, paint);
        paint.setColor(disabled ? Color.argb(145, 210, 210, 210) : (selected ? Color.argb(235, 255, 235, 120) : Color.argb(205, 220, 240, 255)));
        paint.setTextSize(Math.max(7.3f, r.height() * 0.200f));
        canvas.drawText(trimMenuText(status, 15), textLeft, r.top + r.height() * 0.72f, paint);
    }

    private void drawVisualSwatch(Canvas canvas, RectF rect, int type, int value, boolean selected) {
        if (!VisualCustomizationSystem.isColorLike(type) && type != VisualCustomizationSystem.PAINT_FINISH) return;
        float radius = Math.max(5f, Math.min(rect.width(), rect.height()) * 0.16f);
        float cx = rect.left + radius * 1.9f;
        float cy = rect.centerY();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(VisualCustomizationSystem.previewColor(type, value));
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(selected ? 2.2f : 1.3f);
        paint.setColor(selected ? Color.WHITE : Color.argb(160, 255, 255, 255));
        canvas.drawCircle(cx, cy, radius, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawGarageMiniHeader(Canvas canvas, int vehicleIndex) {
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(13f, Math.min(20f, getHeight() * 0.022f)));
        canvas.drawText(trimMenuText(GameScreenState.vehicleLabel(vehicleIndex), 42), getWidth() * 0.50f, getHeight() * 0.155f, paint);
        drawShowroomControlStrip(canvas, vehicleIndex);
        paint.setColor(Color.argb(210, 235, 245, 255));
        paint.setTextSize(Math.max(8.8f, Math.min(11.5f, getHeight() * 0.0145f)));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.catalogFoundationLine(vehicleIndex), 58), getWidth() * 0.50f, getHeight() * 0.198f, paint);
        paint.setColor(Color.argb(210, 120, 235, 255));
        paint.setTextSize(Math.max(8.2f, Math.min(10.5f, getHeight() * 0.0135f)));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.showroomFoundationLine(vehicleIndex), 58), getWidth() * 0.50f, getHeight() * 0.220f, paint);
        paint.setColor(Color.argb(210, 255, 170, 90));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.stabilityRepairLine(saveManager, vehicleIndex), 58), getWidth() * 0.50f, getHeight() * 0.240f, paint);
    }

    private void drawPerformanceStatsCompact(Canvas canvas, int vehicleIndex, float x, float y) {
        VehicleUpgradeSystem.Stats stats = VehicleUpgradeSystem.buildStats(saveManager, vehicleIndex);
        float min = Math.min(getWidth(), getHeight());
        RectF panel = new RectF(x - 12f, y - 24f, getWidth() - Math.max(18f, min * 0.035f), y + Math.max(178f, getHeight() * 0.280f));
        drawGlassPanel(canvas, panel, Color.argb(118, 4, 12, 26), Color.argb(140, 0, 220, 255));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(238, 255, 235, 120));
        paint.setTextSize(Math.max(11f, getHeight() * 0.018f));
        canvas.drawText("PRO BUILD ÖZETİ", panel.left + 13f, panel.top + 24f, paint);
        float row = panel.top + 48f;
        float step = Math.max(18f, getHeight() * 0.030f);
        drawGarageMetricBar(canvas, "Hız", stats.speed, 100, panel.left + 13f, row, panel.width() - 30f); row += step;
        drawGarageMetricBar(canvas, "Hızlanma", stats.acceleration, 100, panel.left + 13f, row, panel.width() - 30f); row += step;
        drawGarageMetricBar(canvas, "Tutuş", stats.handling, 100, panel.left + 13f, row, panel.width() - 30f); row += step;
        drawGarageMetricBar(canvas, "Fren", stats.brake, 100, panel.left + 13f, row, panel.width() - 30f); row += step;
        drawGarageMetricBar(canvas, "Nitro", stats.nitro, 100, panel.left + 13f, row, panel.width() - 30f); row += step;
        drawGarageMetricBar(canvas, "Drift", stats.drift, 100, panel.left + 13f, row, panel.width() - 30f);
        paint.setColor(Color.argb(220, 120, 235, 255));
        paint.setTextSize(Math.max(8.5f, getHeight() * 0.0142f));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.upgradeEffectLine(saveManager, vehicleIndex), 48), panel.left + 13f, panel.bottom - 12f, paint);
    }

    private void drawGarageProfessionalStatusStrip(Canvas canvas, int selected, boolean owned, boolean levelUnlocked, boolean canAfford, int price) {
        float min = Math.min(getWidth(), getHeight());
        float x = getWidth() * 0.050f;
        float y = getHeight() * 0.585f;
        float w = Math.max(270f, getWidth() * 0.280f);
        float h = Math.max(74f, Math.min(96f, min * 0.106f));
        RectF panel = new RectF(x, y, x + w, y + h);
        drawGlassPanel(canvas, panel, Color.argb(108, 4, 12, 26), Color.argb(128, 0, 220, 255));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(9.8f, Math.min(12.4f, min * 0.0148f)));
        canvas.drawText("Garaj Durumu", panel.left + 14f, panel.top + h * 0.28f, paint);
        paint.setColor(Color.argb(220, 235, 245, 255));
        paint.setTextSize(Math.max(7.6f, Math.min(9.8f, min * 0.0122f)));
        String line = owned ? "Sahip olundu: Modifiye ve test sürüşü aktif"
                : (!levelUnlocked ? ("Kilitli: LVL " + VehicleCatalog.requiredLevel(selected) + " gerekli")
                : (canAfford ? ("Satın alınabilir: " + price + " coin") : ("Para yetersiz: " + price + " coin")));
        canvas.drawText(trimMenuText(line, 54), panel.left + 14f, panel.top + h * 0.60f, paint);
        paint.setColor(Color.argb(220, 120, 235, 255));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.catalogFoundationLine(selected), 54), panel.left + 14f, panel.top + h * 0.78f, paint);
        paint.setColor(Color.argb(225, 255, 170, 90));
        canvas.drawText(trimMenuText(PostTenUpdateStabilityQaSystem.layoutSafeAreaLine(getWidth(), getHeight()), 54), panel.left + 14f, panel.top + h * 0.95f, paint);
    }

    private void drawWorkshopActionHintPanel(Canvas canvas, int selected, String vehicleId) {
        float min = Math.min(getWidth(), getHeight());
        RectF panel = new RectF(getWidth() * 0.705f, getHeight() * 0.695f, getWidth() - Math.max(18f, min * 0.035f), getHeight() * 0.815f);
        drawGlassPanel(canvas, panel, Color.argb(108, 4, 12, 26), Color.argb(118, 255, 220, 80));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(10.2f, min * 0.0165f));
        canvas.drawText("Atölye Akışı", panel.left + 12f, panel.top + panel.height() * 0.28f, paint);
        paint.setColor(Color.argb(220, 235, 245, 255));
        paint.setTextSize(Math.max(8.2f, min * 0.0135f));
        canvas.drawText("1) Performans: onayla → sürüş fiziğine uygula", panel.left + 12f, panel.top + panel.height() * 0.52f, paint);
        canvas.drawText("2) Görsel: ön izle → kaydet/geri al/sıfırla", panel.left + 12f, panel.top + panel.height() * 0.72f, paint);
        paint.setColor(Color.argb(215, 120, 235, 255));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.modificationRepairLine(saveManager, vehicleId), 42), panel.left + 12f, panel.top + panel.height() * 0.91f, paint);
    }


    private void drawWorkshopBottomStatusPanel(Canvas canvas, int selected, String vehicleId) {
        float min = Math.min(getWidth(), getHeight());
        RectF panel = new RectF(getWidth() * 0.055f, getHeight() * 0.865f, getWidth() * 0.675f, getHeight() * 0.944f);
        drawGlassPanel(canvas, panel, Color.argb(108, 4, 12, 26), Color.argb(125, 0, 220, 255));
        float left = panel.left + 12f;
        float mid = panel.left + panel.width() * 0.52f;
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(9.4f, Math.min(12f, min * 0.0148f)));
        canvas.drawText("Görsel Durum", left, panel.top + panel.height() * 0.34f, paint);
        canvas.drawText("Build Özeti", mid, panel.top + panel.height() * 0.34f, paint);
        paint.setColor(Color.argb(220, 235, 245, 255));
        paint.setTextSize(Math.max(7.8f, Math.min(10.5f, min * 0.0125f)));
        canvas.drawText(trimMenuText(VehicleCustomizationSystem.summary(saveManager, vehicleId), 38), left, panel.top + panel.height() * 0.67f, paint);
        paint.setColor(Color.argb(220, 120, 235, 255));
        canvas.drawText(trimMenuText(GarageInfrastructureSystem.tuningEffectLine(saveManager, selected), 38), mid, panel.top + panel.height() * 0.67f, paint);
    }

    private void drawGarageMetricBar(Canvas canvas, String label, int value, int max, float x, float y, float width) {
        float min = Math.min(getWidth(), getHeight());
        float labelW = Math.max(72f, width * 0.33f);
        float barW = Math.max(80f, width - labelW - 34f);
        float barH = Math.max(5f, min * 0.008f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(220, 235, 245, 255));
        paint.setTextSize(Math.max(8.0f, min * 0.0134f));
        canvas.drawText(label + " " + value, x, y, paint);
        float bx = x + labelW;
        float by = y - barH * 1.35f;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(84, 255, 255, 255));
        canvas.drawRoundRect(new RectF(bx, by, bx + barW, by + barH), barH, barH, paint);
        float ratio = Math.max(0f, Math.min(1f, value / (float)Math.max(1, max)));
        paint.setColor(Color.argb(225, 0, 220, 255));
        canvas.drawRoundRect(new RectF(bx, by, bx + barW * ratio, by + barH), barH, barH, paint);
    }

    private String performancePreviewLine(int type, int level, boolean maxed) {
        return PerformanceUpgradeBalanceSystem.previewLine(type, level, maxed);
    }

    private void drawTuningSettingChip(Canvas canvas, RectF r, int type, String vehicleId) {
        drawGlassPanel(canvas, r, Color.argb(118, 10, 16, 28), Color.argb(130, 255, 255, 255));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(8.4f, r.height() * 0.245f));
        canvas.drawText(trimMenuText(VehicleTuningSystem.shortLabel(type), 12), r.left + r.width() * 0.07f, r.top + r.height() * 0.42f, paint);
        paint.setColor(Color.argb(225, 255, 235, 120));
        paint.setTextSize(Math.max(7.0f, r.height() * 0.200f));
        canvas.drawText(trimMenuText(VehicleTuningSystem.displayValue(saveManager, vehicleId, type), 17), r.left + r.width() * 0.07f, r.top + r.height() * 0.72f, paint);
    }

    private void drawTuningPresetCard(Canvas canvas, RectF r, int preset, String vehicleId) {
        boolean selected = saveManager != null && saveManager.getTuningPreset(vehicleId) == preset;
        drawGlassPanel(canvas, r, selected ? Color.argb(176, 0, 150, 255) : Color.argb(110, 10, 16, 28), selected ? Color.argb(225, 120, 235, 255) : Color.argb(126, 255, 255, 255));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(8.6f, r.height() * 0.245f));
        canvas.drawText(VehicleTuningSystem.presetLabel(preset), r.left + r.width() * 0.08f, r.top + r.height() * 0.35f, paint);
        paint.setColor(selected ? Color.argb(238, 255, 235, 120) : Color.argb(205, 220, 240, 255));
        paint.setTextSize(Math.max(6.4f, r.height() * 0.135f));
        canvas.drawText(trimMenuText(VehicleTuningSystem.presetDescription(preset), 32), r.left + r.width() * 0.08f, r.top + r.height() * 0.55f, paint);
        paint.setColor(selected ? Color.argb(240, 180, 255, 190) : Color.argb(205, 220, 245, 255));
        paint.setTextSize(Math.max(5.9f, r.height() * 0.122f));
        canvas.drawText(trimMenuText(VehicleTuningSystem.presetProsLine(preset), 32), r.left + r.width() * 0.08f, r.top + r.height() * 0.70f, paint);
        paint.setColor(Color.argb(190, 255, 210, 145));
        canvas.drawText(trimMenuText(VehicleTuningSystem.presetTradeoffLine(preset), 32), r.left + r.width() * 0.08f, r.top + r.height() * 0.82f, paint);
        paint.setColor(selected ? Color.argb(238, 255, 235, 120) : Color.argb(215, 130, 235, 255));
        paint.setTextSize(Math.max(5.8f, r.height() * 0.118f));
        canvas.drawText(selected ? "AKTİF / KAYITLI" : "UYGULA + KAYDET", r.left + r.width() * 0.08f, r.top + r.height() * 0.94f, paint);
    }

    private void drawVisualEditStatusPanel(Canvas canvas, int type, boolean dirty, boolean ownsCurrent, boolean canBuyCurrent, boolean levelLockedCurrent, int current, int cost) {
        float min = Math.min(getWidth(), getHeight());
        RectF panel = new RectF(getWidth() * 0.18f, getHeight() * 0.805f, getWidth() * 0.82f, getHeight() * 0.838f);
        drawGlassPanel(canvas, panel, dirty ? Color.argb(136, 45, 32, 8) : Color.argb(108, 4, 16, 26), dirty ? Color.argb(185, 255, 220, 80) : Color.argb(135, 0, 220, 255));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(8.6f, min * 0.014f));
        paint.setColor(dirty ? Color.argb(238, 255, 235, 120) : Color.argb(222, 235, 245, 255));
        String status = dirty ? "KAYDEDİLMEMİŞ DEĞİŞİKLİK VAR" : "KAYITLI GÖRÜNÜM AKTİF";
        if (!ownsCurrent && levelLockedCurrent) status = "SEÇENEK SEVİYE KİLİTLİ";
        else if (!ownsCurrent && !canBuyCurrent) status = "PARA YETERSİZ: " + cost + " COIN";
        else if (!ownsCurrent) status = "SATIN AL & KAYDET: " + cost + " COIN";
        canvas.drawText(status + "  |  " + VisualCustomizationSystem.valueLabel(type, current), panel.centerX(), panel.centerY() + Math.max(3f, min * 0.005f), paint);
    }

    private String visualFocusHelp(int type) {
        if (type == VisualCustomizationSystem.RIM_STYLE || type == VisualCustomizationSystem.RIM_COLOR || type == VisualCustomizationSystem.TIRE_SIDEWALL) {
            return "Kamera jant/lastik bölgesine zoom yapar; çıkınca tüm araç görünümüne döner.";
        }
        if (type == VisualCustomizationSystem.HOOD || type == VisualCustomizationSystem.FRONT_BUMPER || type == VisualCustomizationSystem.HEADLIGHT_COLOR) {
            return "Kamera kaput/ön taraf bölgesine zoom yapar.";
        }
        if (type == VisualCustomizationSystem.WINDOW_TINT || type == VisualCustomizationSystem.MIRROR_COLOR || type == VisualCustomizationSystem.ROOF_ACCESSORY) {
            return "Kamera cam, kabin ve ayna bölgesine yaklaşır.";
        }
        if (type == VisualCustomizationSystem.REAR_BUMPER || type == VisualCustomizationSystem.TAIL_LIGHT_STYLE || type == VisualCustomizationSystem.PLATE_STYLE || type == VisualCustomizationSystem.EXHAUST_TIP || type == VisualCustomizationSystem.SPOILER) {
            return "Kamera arka/parça odaklı yakın görünüm verir.";
        }
        return "Kamera tüm aracı yakın gösterir; KAYDET basılana kadar sadece ön izlemedir.";
    }

    private void drawUpgradeButton(Canvas canvas, RectF rect, String label, int vehicleIndex, int type) {
        drawSinglePerformanceButton(canvas, rect, label, VehicleCatalog.id(vehicleIndex), type);
    }

    private void drawPerformanceUpgradeGrid(Canvas canvas, int vehicleIndex) {
        String id = VehicleCatalog.id(vehicleIndex);
        if (performanceUpgradeButtons.length == 0) return;
        RectF first = performanceUpgradeButtons[0];
        RectF last = performanceUpgradeButtons[performanceUpgradeButtons.length - 1];
        float min = Math.min(getWidth(), getHeight());
        RectF panel = new RectF(first.left - Math.max(8f, min * 0.012f),
                first.top - Math.max(32f, min * 0.048f),
                Math.max(first.right, last.right) + Math.max(8f, min * 0.012f),
                last.bottom + Math.max(12f, min * 0.016f));
        drawGlassPanel(canvas, panel, Color.argb(86, 4, 12, 26), Color.argb(118, 0, 220, 255));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(235, 255, 255, 255));
        paint.setTextSize(Math.max(10f, getHeight() * 0.0162f));
        float titleY = first.top - Math.max(17f, getHeight() * 0.023f);
        canvas.drawText("PERFORMANS MODİFİYE", first.left, titleY, paint);
        paint.setColor(Color.argb(215, 235, 245, 255));
        paint.setTextSize(Math.max(7.8f, getHeight() * 0.0128f));
        canvas.drawText(trimMenuText(GarageModificationUiSystem.performanceStageLine(saveManager, vehicleIndex), 76),
                first.left, first.top - Math.max(5f, getHeight() * 0.007f), paint);
        paint.setColor(Color.argb(210, 255, 235, 135));
        paint.setTextSize(Math.max(7.2f, getHeight() * 0.0118f));
        canvas.drawText(trimMenuText(PerformanceUpgradeBalanceSystem.tuningSafetyLine(saveManager, vehicleIndex), 72),
                first.left + Math.max(280f, getWidth() * 0.205f), first.top - Math.max(5f, getHeight() * 0.007f), paint);
        for (int i = 0; i < performanceUpgradeButtons.length; i++) {
            int type = VehicleUpgradeSystem.PERFORMANCE_ORDER[i];
            drawSinglePerformanceButton(canvas, performanceUpgradeButtons[i], VehicleUpgradeSystem.shortLabel(type), id, type);
        }
    }

    private void drawSinglePerformanceButton(Canvas canvas, RectF rect, String label, String vehicleId, int type) {
        int level = saveManager == null ? 0 : saveManager.getUpgradeLevel(vehicleId, type);
        int cost = saveManager == null ? 0 : saveManager.getUpgradeCost(vehicleId, type);
        int tier = saveManager == null ? SaveManager.MAX_UPGRADE_LEVEL : saveManager.getUnlockedPartTier();
        boolean maxed = level >= SaveManager.MAX_UPGRADE_LEVEL;
        boolean tierLocked = level >= tier && !maxed;
        boolean coinLocked = !maxed && !tierLocked && saveManager != null && saveManager.getCoins() < cost;
        boolean canUpgrade = !maxed && !tierLocked && !coinLocked;

        int fill = maxed ? Color.argb(132, 20, 90, 48) : (canUpgrade ? Color.argb(142, 8, 28, 42) : Color.argb(96, 45, 48, 58));
        int stroke = maxed ? Color.argb(170, 80, 255, 150) : (canUpgrade ? Color.argb(180, 0, 220, 255) : Color.argb(135, 255, 120, 95));
        drawGlassPanel(canvas, rect, fill, stroke);

        float x = rect.left + Math.max(10f, rect.width() * 0.055f);
        float y = rect.top + Math.max(14f, rect.height() * 0.22f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(9.6f, rect.height() * 0.205f));
        canvas.drawText(trimMenuText(VehicleUpgradeSystem.label(type), 18), x, y, paint);

        paint.setColor(Color.argb(225, 235, 245, 255));
        paint.setTextSize(Math.max(7.8f, rect.height() * 0.155f));
        String stateLine = maxed ? "Seviye MAX" : (tierLocked ? ("Tier " + tier + " kilitli") : (coinLocked ? ("Para yetersiz $" + cost) : ("L" + level + " → L" + (level + 1) + "  $" + cost)));
        canvas.drawText(stateLine, x, y + rect.height() * 0.25f, paint);

        paint.setColor(canUpgrade ? Color.argb(235, 255, 235, 120) : Color.argb(205, 205, 220, 235));
        canvas.drawText(trimMenuText(performancePreviewLine(type, level, maxed), 33), x, y + rect.height() * 0.47f, paint);
        paint.setColor(Color.argb(190, 170, 225, 255));
        paint.setTextSize(Math.max(6.6f, rect.height() * 0.132f));
        canvas.drawText(trimMenuText(PerformanceUpgradeBalanceSystem.impactLine(type), 36), x, y + rect.height() * 0.67f, paint);

        float chipW = Math.max(64f, rect.width() * 0.31f);
        RectF chip = new RectF(rect.right - chipW - 8f, rect.bottom - Math.max(23f, rect.height() * 0.30f), rect.right - 8f, rect.bottom - 7f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(maxed ? Color.argb(190, 35, 150, 80) : (canUpgrade ? Color.argb(205, 0, 170, 255) : Color.argb(120, 100, 104, 112)));
        canvas.drawRoundRect(chip, 13f, 13f, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(7.4f, chip.height() * 0.42f));
        Paint.FontMetrics fm = paint.getFontMetrics();
        canvas.drawText(maxed ? "MAX" : (canUpgrade ? "YÜKSELT" : "KİLİT"), chip.centerX(), chip.centerY() - (fm.ascent + fm.descent) * 0.5f, paint);
    }

    private void drawPerformanceEffectLegend(Canvas canvas) {
        float x = getWidth() * 0.08f;
        float y = getHeight() * 0.815f;
        float line = Math.max(14f, getHeight() * 0.020f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(220, 235, 245, 255));
        paint.setTextSize(Math.max(8.8f, getHeight() * 0.0148f));
        canvas.drawText("Etki: Motor/hızlanma, Turbo/N2O, Fren/fren mesafesi, Lastik/tutuş, Ağırlık/çeviklik, Dayanıklılık/hasar direnci.", x, y, paint);
        paint.setColor(Color.argb(195, 120, 235, 255));
        canvas.drawText("Her yükseltme kaydedilir ve sürüşe çıkınca VehicleController tuning değerlerine otomatik uygulanır.", x, y + line, paint);
    }

    private void drawPerformanceStats(Canvas canvas, int vehicleIndex) {
        VehicleUpgradeSystem.Stats stats = VehicleUpgradeSystem.buildStats(saveManager, vehicleIndex);
        float x = getWidth() * 0.31f;
        float y = getHeight() * 0.322f;
        float line = Math.max(16f, getHeight() * 0.022f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(235, 255, 235, 120));
        paint.setTextSize(Math.max(11f, getHeight() * 0.018f));
        canvas.drawText("ARAÇ İSTATİSTİKLERİ", x, y, paint);
        paint.setColor(Color.argb(220, 235, 245, 255));
        paint.setTextSize(Math.max(10f, getHeight() * 0.016f));
        canvas.drawText("Hız " + stats.speed + "  Hızlanma " + stats.acceleration + "  Nitro " + stats.nitro, x, y + line, paint);
        canvas.drawText("Yol tutuş " + stats.handling + "  Fren " + stats.brake + "  Drift " + stats.drift, x, y + line * 2f, paint);
        canvas.drawText("Dayanıklılık " + stats.durability + "  Toplam mod " + saveManager.getTotalPerformanceUpgradeLevel(VehicleCatalog.id(vehicleIndex)), x, y + line * 3f, paint);
    }

    private void drawPerformanceUpgradeConfirm(Canvas canvas, int vehicleIndex, int type) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(150, 0, 0, 0));
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        String vehicleId = VehicleCatalog.id(vehicleIndex);
        int level = saveManager == null ? 0 : saveManager.getUpgradeLevel(vehicleId, type);
        int cost = saveManager == null ? 0 : saveManager.getUpgradeCost(vehicleId, type);
        boolean can = saveManager != null && level < SaveManager.MAX_UPGRADE_LEVEL
                && level < saveManager.getUnlockedPartTier()
                && saveManager.getCoins() >= cost;
        drawGlassPanel(canvas, performanceConfirmPanel, Color.argb(236, 3, 10, 24), can ? Color.argb(230, 0, 225, 255) : Color.argb(210, 255, 95, 95));
        float x = performanceConfirmPanel.left + Math.max(18f, performanceConfirmPanel.width() * 0.065f);
        float y = performanceConfirmPanel.top + Math.max(30f, performanceConfirmPanel.height() * 0.135f);
        float line = Math.max(20f, performanceConfirmPanel.height() * 0.100f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(16f, getHeight() * 0.027f));
        canvas.drawText("YÜKSELTME ONAYI", x, y, paint);
        paint.setColor(Color.argb(235, 120, 235, 255));
        paint.setTextSize(Math.max(12f, getHeight() * 0.020f));
        canvas.drawText(VehicleUpgradeSystem.label(type) + "  L" + level + " → L" + Math.min(SaveManager.MAX_UPGRADE_LEVEL, level + 1), x, y + line * 1.35f, paint);
        paint.setColor(Color.argb(222, 235, 245, 255));
        paint.setTextSize(Math.max(10f, getHeight() * 0.0165f));
        canvas.drawText("Maliyet: " + cost + " coin  |  Mevcut: " + coinsText(), x, y + line * 2.55f, paint);
        canvas.drawText("Etki: " + performancePreviewLine(type, level, level >= SaveManager.MAX_UPGRADE_LEVEL), x, y + line * 3.35f, paint);
        canvas.drawText(trimMenuText(PerformanceUpgradeBalanceSystem.impactLine(type), 64), x, y + line * 4.25f, paint);
        paint.setColor(can ? Color.argb(230, 160, 255, 190) : Color.argb(230, 255, 150, 130));
        canvas.drawText(can ? "Onaylarsan coin düşer, fiyat dengesi kaydedilir ve sürüşe uygulanır." : "Bu yükseltme şu anda alınamaz; seviye, tier veya coin kontrol et.", x, y + line * 5.10f, paint);
        drawButton(canvas, performanceCancelButton, "VAZGEÇ", false, false);
        drawButton(canvas, performanceConfirmButton, can ? "ONAYLA" : "KİLİTLİ", true, !can);
    }

    private void beginGarageTestDriveFromCurrentMode(String vehicleId) {
        int selected = state.getSelectedVehicleIndex();
        if (!isOwned(selected)) {
            if (saveManager != null) saveManager.setEconomyLastMessage("Test sürüşü için önce aracı satın al");
            if (audioManager != null) audioManager.playLocked();
            return;
        }
        if (saveManager != null) {
            saveManager.setSelectedVehicleIndex(selected);
            saveManager.setSelectedMap(GameScreenState.MAP_OPEN_FIELD);
            saveManager.setEconomyLastMessage("Garaj test sürüşü: Açık Test Alanı hazırlanıyor");
        }
        state.beginGarageToTestDriveTransition(selected);
        if (audioManager != null) audioManager.playReward();
        postInvalidateOnAnimation();
    }

    private void resetVisualEditToSaved(String vehicleId, int type) {
        if (saveManager == null) return;
        state.setVisualEditPreviewValue(saveManager.getVisualModValue(vehicleId, type));
        saveManager.setEconomyLastMessage("GÖRSEL: Son kayıtlı ayara geri alındı");
        if (audioManager != null) audioManager.playMenuClick();
    }

    private boolean tryUpgradePerformance(String vehicleId, float x, float y) {
        int type = hitPerformanceUpgrade(x, y);
        if (type < 0) return false;
        pendingPerformanceUpgradeType = type;
        if (saveManager != null) {
            saveManager.setEconomyLastMessage("PERFORMANS: " + VehicleUpgradeSystem.label(type) + " için onay bekleniyor");
        }
        if (audioManager != null) audioManager.playMenuClick();
        return true;
    }

    private boolean confirmPendingPerformanceUpgrade(String vehicleId) {
        if (pendingPerformanceUpgradeType < 0) return false;
        int type = pendingPerformanceUpgradeType;
        pendingPerformanceUpgradeType = -1;
        if (saveManager != null && saveManager.upgradeVehicle(vehicleId, type)) {
            completeMenuQuestIfActive(4, "İlk yükseltme görevi tamamlandı", 260, 500, 0);
            saveManager.setEconomyLastMessage("PERFORMANS: " + VehicleUpgradeSystem.label(type) + " yükseltildi | " + PerformanceUpgradeBalanceSystem.tuningSafetyLine(saveManager, state.getSelectedVehicleIndex()));
            if (audioManager != null) audioManager.playReward();
            return true;
        }
        if (saveManager != null) saveManager.setEconomyLastMessage("PERFORMANS: Yükseltme alınamadı");
        if (audioManager != null) audioManager.playLocked();
        return false;
    }

    private int hitPerformanceUpgrade(float x, float y) {
        for (int i = 0; i < performanceUpgradeButtons.length; i++) {
            if (performanceUpgradeButtons[i].contains(x, y)) {
                return VehicleUpgradeSystem.PERFORMANCE_ORDER[i];
            }
        }
        return -1;
    }

    private void drawDetailedTuningPanel(Canvas canvas, int vehicleIndex) {
        String id = VehicleCatalog.id(vehicleIndex);
        if (detailedTuningButtons.length == 0 || tuningPresetButtons.length == 0) return;
        RectF first = detailedTuningButtons[0];
        RectF lastPreset = tuningPresetButtons[tuningPresetButtons.length - 1];
        float min = Math.min(getWidth(), getHeight());
        RectF panel = new RectF(first.left - Math.max(8f, min * 0.012f),
                first.top - Math.max(34f, min * 0.050f),
                lastPreset.right + Math.max(8f, min * 0.012f),
                lastPreset.bottom + Math.max(14f, min * 0.018f));
        drawGlassPanel(canvas, panel, Color.argb(86, 4, 12, 26), Color.argb(118, 130, 235, 255));
        float titleY = first.top - Math.max(18f, getHeight() * 0.024f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(235, 130, 235, 255));
        paint.setTextSize(Math.max(10.0f, getHeight() * 0.0162f));
        canvas.drawText("DETAYLI TUNING", first.left, titleY, paint);
        paint.setColor(Color.argb(218, 235, 245, 255));
        paint.setTextSize(Math.max(7.8f, getHeight() * 0.0128f));
        canvas.drawText(trimMenuText(GarageModificationUiSystem.tuningStageLine(saveManager, vehicleIndex), 82), first.left, first.top - Math.max(5f, getHeight() * 0.007f), paint);
        paint.setColor(Color.argb(218, 255, 235, 120));
        paint.setTextSize(Math.max(7.0f, getHeight() * 0.0114f));
        canvas.drawText(trimMenuText(GarageModificationUiSystem.tuningFinalQaLine(saveManager, vehicleIndex), 92), first.left, first.top + Math.max(10f, getHeight() * 0.015f), paint);

        for (int i = 0; i < detailedTuningButtons.length; i++) {
            int type = VehicleTuningSystem.TUNING_ORDER[i];
            drawTuningSettingChip(canvas, detailedTuningButtons[i], type, id);
        }

        drawTuningPresetCard(canvas, tuningPresetButtons[0], VehicleTuningSystem.PRESET_BALANCED, id);
        drawTuningPresetCard(canvas, tuningPresetButtons[1], VehicleTuningSystem.PRESET_RACE, id);
        drawTuningPresetCard(canvas, tuningPresetButtons[2], VehicleTuningSystem.PRESET_DRIFT, id);
        drawTuningPresetCard(canvas, tuningPresetButtons[3], VehicleTuningSystem.PRESET_POLICE, id);
    }

    private boolean tryDetailedTuning(String vehicleId, float x, float y) {
        for (int i = 0; i < tuningPresetButtons.length; i++) {
            if (tuningPresetButtons[i].contains(x, y)) {
                VehicleTuningSystem.applyPreset(saveManager, vehicleId, i);
                if (audioManager != null) audioManager.playReward();
                return true;
            }
        }
        for (int i = 0; i < detailedTuningButtons.length; i++) {
            if (detailedTuningButtons[i].contains(x, y)) {
                int type = VehicleTuningSystem.TUNING_ORDER[i];
                saveManager.cycleDetailedTuningValue(vehicleId, type);
                saveManager.setTuningPreset(vehicleId, VehicleTuningSystem.PRESET_BALANCED);
                saveManager.setEconomyLastMessage("TUNING: " + VehicleTuningSystem.label(type) + " = "
                        + VehicleTuningSystem.displayValue(saveManager, vehicleId, type)
                        + " | özel ayar dengeli profile kaydedildi");
                if (audioManager != null) audioManager.playMenuClick();
                return true;
            }
        }
        return false;
    }

    private void drawVisualCustomizationPanel(Canvas canvas, int vehicleIndex) {
        String id = VehicleCatalog.id(vehicleIndex);
        if (visualModButtons.length == 0) return;

        int group = state.getSelectedVisualGroup();
        RectF first = visualModButtons[0];
        RectF last = visualModButtons[visualModButtons.length - 1];
        float min = Math.min(getWidth(), getHeight());
        RectF panel = new RectF(first.left - Math.max(8f, min * 0.012f),
                first.top - Math.max(34f, min * 0.050f),
                Math.max(first.right, last.right) + Math.max(8f, min * 0.012f),
                last.bottom + Math.max(28f, min * 0.040f));
        drawGlassPanel(canvas, panel, Color.argb(86, 4, 12, 26), Color.argb(118, 255, 210, 110));
        float titleY = first.top - Math.max(18f, getHeight() * 0.024f);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(238, 255, 210, 110));
        paint.setTextSize(Math.max(9.8f, getHeight() * 0.0155f));
        canvas.drawText(visualGroupTitle(group), first.left, titleY, paint);
        paint.setColor(Color.argb(218, 235, 245, 255));
        paint.setTextSize(Math.max(7.6f, getHeight() * 0.0126f));
        canvas.drawText(trimMenuText(GarageModificationUiSystem.visualGroupStageLine(saveManager, id, group), 82),
                first.left, first.top - Math.max(5f, getHeight() * 0.007f), paint);
        paint.setColor(Color.argb(205, 190, 235, 255));
        paint.setTextSize(Math.max(7.0f, getHeight() * 0.0112f));
        canvas.drawText(trimMenuText(VisualModificationSaveFlowSystem.groupSaveAuditLine(saveManager, id, group), 92),
                first.left, Math.min(panel.bottom - Math.max(8f, getHeight() * 0.011f), last.bottom + Math.max(17f, getHeight() * 0.023f)), paint);

        int slot = 0;
        for (int i = 0; i < VisualCustomizationSystem.VISUAL_ORDER.length && slot < visualModButtons.length; i++) {
            int type = VisualCustomizationSystem.VISUAL_ORDER[i];
            if (!matchesVisualGroup(type, group)) continue;
            int value = saveManager == null ? 0 : saveManager.getVisualModValue(id, type);
            boolean active = value > 0;
            String title = VisualCustomizationSystem.shortLabel(type);
            String status = active ? ("AKTİF: " + VisualCustomizationSystem.valueLabel(type, value)) : "STOK";
            drawVisualPartCard(canvas, visualModButtons[slot], title, status, active);
            slot++;
        }

        paint.setColor(Color.argb(210, 235, 245, 255));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(Math.max(7.4f, getHeight() * 0.0122f));
        float infoY = Math.min(panel.bottom - 10f, visualModButtons[Math.max(0, slot - 1)].bottom + Math.max(14f, getHeight() * 0.017f));
        canvas.drawText("Parça kartı → tek düzenleme ekranı. Sahte 3D parça eklenmez; mevcut model/material presetleri kullanılır.",
                first.left, infoY, paint);
    }

    private void drawVisualPartCard(Canvas canvas, RectF r, String title, String status, boolean active) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(active ? Color.argb(166, 0, 165, 255) : Color.argb(118, 10, 16, 28));
        canvas.drawRoundRect(r, 18f, 18f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(active ? 2.2f : 1.4f);
        paint.setColor(active ? Color.argb(220, 120, 235, 255) : Color.argb(135, 255, 255, 255));
        canvas.drawRoundRect(r, 18f, 18f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(8.8f, r.height() * 0.300f));
        canvas.drawText(trimMenuText(title, 11), r.left + r.width() * 0.09f, r.top + r.height() * 0.43f, paint);
        paint.setColor(active ? Color.argb(230, 255, 235, 120) : Color.argb(205, 220, 240, 255));
        paint.setTextSize(Math.max(7.3f, r.height() * 0.220f));
        canvas.drawText(trimMenuText(status, 17), r.left + r.width() * 0.09f, r.top + r.height() * 0.73f, paint);
    }


    private int hitVisualCustomization(float x, float y) {
        int group = state.getSelectedVisualGroup();
        int slot = 0;
        for (int i = 0; i < VisualCustomizationSystem.VISUAL_ORDER.length && slot < visualModButtons.length; i++) {
            int type = VisualCustomizationSystem.VISUAL_ORDER[i];
            if (!matchesVisualGroup(type, group)) continue;
            if (visualModButtons[slot].contains(x, y)) {
                return type;
            }
            slot++;
        }
        return -1;
    }

    private boolean matchesVisualGroup(int type, int group) {
        if (group == GameScreenState.VISUAL_GROUP_WHEELS) {
            return type == VisualCustomizationSystem.RIM_STYLE
                    || type == VisualCustomizationSystem.RIM_COLOR
                    || type == VisualCustomizationSystem.TIRE_SIDEWALL;
        }
        if (group == GameScreenState.VISUAL_GROUP_GLASS) {
            return type == VisualCustomizationSystem.WINDOW_TINT
                    || type == VisualCustomizationSystem.MIRROR_COLOR
                    || type == VisualCustomizationSystem.ROOF_ACCESSORY;
        }
        if (group == GameScreenState.VISUAL_GROUP_LIGHTS) {
            return type == VisualCustomizationSystem.HEADLIGHT_COLOR
                    || type == VisualCustomizationSystem.TAIL_LIGHT_STYLE
                    || type == VisualCustomizationSystem.NEON;
        }
        if (group == GameScreenState.VISUAL_GROUP_PLATE) {
            return type == VisualCustomizationSystem.PLATE_STYLE
                    || type == VisualCustomizationSystem.EXHAUST_TIP;
        }
        return type == VisualCustomizationSystem.PAINT_COLOR
                || type == VisualCustomizationSystem.PAINT_FINISH
                || type == VisualCustomizationSystem.WRAP
                || type == VisualCustomizationSystem.STICKER
                || type == VisualCustomizationSystem.HOOD
                || type == VisualCustomizationSystem.FRONT_BUMPER
                || type == VisualCustomizationSystem.REAR_BUMPER
                || type == VisualCustomizationSystem.SIDE_SKIRT
                || type == VisualCustomizationSystem.SPOILER;
    }

    private String visualGroupTitle(int group) {
        if (group == GameScreenState.VISUAL_GROUP_WHEELS) return "JANT / TEKER";
        if (group == GameScreenState.VISUAL_GROUP_GLASS) return "CAM / AYNA";
        if (group == GameScreenState.VISUAL_GROUP_LIGHTS) return "FAR / STOP";
        if (group == GameScreenState.VISUAL_GROUP_PLATE) return "PLAKA";
        return "BOYA / BODY KIT";
    }

    private String visualGroupSubtitle(int group) {
        if (group == GameScreenState.VISUAL_GROUP_WHEELS) return "Jant modeli / jant rengi / lastik yazısı";
        if (group == GameScreenState.VISUAL_GROUP_GLASS) return "Cam filmi / ayna / tavan aksesuarı";
        if (group == GameScreenState.VISUAL_GROUP_LIGHTS) return "Far rengi / stop tasarımı / neon preset";
        if (group == GameScreenState.VISUAL_GROUP_PLATE) return "Plaka ve arka detay presetleri";
        return "Boya rengi / boya tipi / kaplama / sticker / kaput / body kit";
    }

    private boolean trySetVisualValue(String vehicleId, float x, float y) {
        int type = state.getSelectedVisualModType();
        int max = VisualCustomizationSystem.maxValue(type);
        int count = Math.min(visualValueButtons.length, max + 1);
        for (int i = 0; i < count; i++) {
            if (visualValueButtons[i].contains(x, y)) {
                state.setVisualEditPreviewValue(i);
                if (audioManager != null) audioManager.playMenuClick();
                return true;
            }
        }
        return false;
    }

    private void cycleSelectedVisualValue(int delta) {
        int selected = state.getSelectedVehicleIndex();
        if (!isOwned(selected)) return;
        String id = VehicleCatalog.id(selected);
        int type = state.getSelectedVisualModType();
        int max = VisualCustomizationSystem.maxValue(type);
        int saved = saveManager.getVisualModValue(id, type);
        int value = state.getVisualEditValueOr(saved, type) + delta;
        if (value < 0) value = max;
        if (value > max) value = 0;
        state.setVisualEditPreviewValue(value);
        if (audioManager != null) audioManager.playMenuClick();
    }

    private boolean tryCommitVisualEdit(String vehicleId, int type) {
        if (saveManager == null) return false;
        int saved = saveManager.getVisualModValue(vehicleId, type);
        int value = state.getVisualEditValueOr(saved, type);
        boolean owns = saveManager.isVisualModOptionOwned(vehicleId, type, value) || value == saved;
        if (!owns && !VehicleCustomizationSystem.isOptionLevelUnlocked(saveManager, type, value)) {
            saveManager.setEconomyLastMessage("GÖRSEL: " + VehicleCustomizationSystem.lockLabel(saveManager, type, value)
                    + " - " + VisualCustomizationSystem.label(type) + " / "
                    + VisualCustomizationSystem.valueLabel(type, value));
            return false;
        }
        if (!owns) {
            int cost = VisualCustomizationSystem.optionCost(type, value);
            if (!saveManager.buyVisualModOption(vehicleId, type, value, cost)) {
                saveManager.setEconomyLastMessage("MODİFİYE: Para yetersiz - "
                        + VisualCustomizationSystem.label(type) + " / "
                        + VisualCustomizationSystem.valueLabel(type, value));
                return false;
            }
        }
        saveManager.setVisualModValue(vehicleId, type, value);
        VisualModificationSaveFlowSystem.syncLegacyRendererFields(saveManager, vehicleId, type, value);
        setVisualEconomyMessage(vehicleId, type, value);
        saveManager.setEconomyLastMessage(VisualModificationSaveFlowSystem.commitResultLine(type, value, !owns)
                + " | " + VisualModificationSaveFlowSystem.savedSignature(saveManager, vehicleId));
        return true;
    }

    private void setVisualEconomyMessage(String vehicleId, int type, int value) {
        if (type == VisualCustomizationSystem.PAINT_COLOR) {
            saveManager.setEconomyLastMessage("GÖRSEL: Boya rengi = "
                    + VisualCustomizationSystem.valueLabel(type, value)
                    + " | Her araç için ayrı kaydedildi");
        } else if (type == VisualCustomizationSystem.RIM_STYLE || type == VisualCustomizationSystem.RIM_COLOR) {
            saveManager.setEconomyLastMessage("GÖRSEL: " + VisualCustomizationSystem.label(type)
                    + " = " + VisualCustomizationSystem.valueLabel(type, value)
                    + " | Jant/lastik zoom görünümünde seçildi");
        } else if (type == VisualCustomizationSystem.HOOD
                || type == VisualCustomizationSystem.FRONT_BUMPER
                || type == VisualCustomizationSystem.REAR_BUMPER
                || type == VisualCustomizationSystem.SIDE_SKIRT
                || type == VisualCustomizationSystem.SPOILER
                || type == VisualCustomizationSystem.EXHAUST_TIP
                || type == VisualCustomizationSystem.ROOF_ACCESSORY
                || type == VisualCustomizationSystem.NEON) {
            saveManager.setEconomyLastMessage("GÖRSEL: " + VisualCustomizationSystem.label(type)
                    + " = " + VisualCustomizationSystem.valueLabel(type, value)
                    + " | Sahte parça eklenmedi, preset kaydedildi");
        } else {
            saveManager.setEconomyLastMessage("GÖRSEL: " + VisualCustomizationSystem.label(type)
                    + " = " + VisualCustomizationSystem.valueLabel(type, value));
        }
    }

    private boolean tryVisualCustomization(String vehicleId, float x, float y) {
        for (int i = 0; i < visualModButtons.length; i++) {
            if (visualModButtons[i].contains(x, y)) {
                int type = VisualCustomizationSystem.VISUAL_ORDER[i];
                int value = saveManager.cycleVisualModValue(vehicleId, type);
                if (type == VisualCustomizationSystem.PAINT_COLOR) {
            saveManager.setEconomyLastMessage("GÖRSEL: Boya rengi = "
                    + VisualCustomizationSystem.valueLabel(type, value)
                    + " | Her araç için ayrı kaydedildi");
        } else if (type == VisualCustomizationSystem.RIM_STYLE || type == VisualCustomizationSystem.RIM_COLOR) {
                    saveManager.setEconomyLastMessage("GÖRSEL: " + VisualCustomizationSystem.label(type)
                            + " = " + VisualCustomizationSystem.valueLabel(type, value)
                            + " | Jant sadece modeldeki gerçek jant materyaline uygulanır");
                } else if (type == VisualCustomizationSystem.FRONT_BUMPER
                        || type == VisualCustomizationSystem.REAR_BUMPER
                        || type == VisualCustomizationSystem.SIDE_SKIRT
                        || type == VisualCustomizationSystem.SPOILER
                        || type == VisualCustomizationSystem.EXHAUST_TIP
                        || type == VisualCustomizationSystem.ROOF_ACCESSORY
                        || type == VisualCustomizationSystem.NEON) {
                    saveManager.setEconomyLastMessage("GÖRSEL: " + VisualCustomizationSystem.label(type)
                            + " = " + VisualCustomizationSystem.valueLabel(type, value)
                            + " | Sahte parça eklenmedi, preset kaydedildi");
                } else {
                    saveManager.setEconomyLastMessage("GÖRSEL: " + VisualCustomizationSystem.label(type)
                            + " = " + VisualCustomizationSystem.valueLabel(type, value));
                }
                if (audioManager != null) audioManager.playMenuClick();
                return true;
            }
        }
        return false;
    }


    private void drawCareerPanel(Canvas canvas) {
        drawTitle(canvas, "KARİYER LİGİ", CareerFlowQaSystem.screenHeaderLine(saveManager));
        drawButton(canvas, backButton, "GERİ", false, false);

        float min = Math.min(getWidth(), getHeight());
        int league = CareerEventSystem.recommendedLeague(saveManager);
        boolean leagueUnlocked = CareerEventSystem.isLeagueUnlocked(saveManager, league);
        boolean leagueCompleted = CareerEventSystem.isLeagueCompleted(saveManager, league);
        boolean leagueClaimed = saveManager != null && saveManager.isCareerLeagueRewardClaimed(league);

        float x = getWidth() * 0.055f;
        float y = getHeight() * 0.165f;
        float w = getWidth() * 0.47f;
        float h = getHeight() * 0.245f;
        RectF main = new RectF(x, y, x + w, y + h);
        drawGlassPanel(canvas, main, Color.argb(156, 4, 12, 26), Color.argb(210, 0, 220, 255));

        int level = saveManager == null ? 1 : saveManager.getPlayerLevel();
        int xp = saveManager == null ? 0 : saveManager.getPlayerXp();
        int nextXp = saveManager == null ? 1 : saveManager.getXpForNextLevel();
        float xp01 = nextXp <= 0 ? 1f : Math.max(0f, Math.min(1f, xp / (float)nextXp));

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(17f, min * 0.030f));
        canvas.drawText(CareerEventSystem.leagueTitle(league) + "  |  Seviye " + level, main.left + 18f, main.top + h * 0.19f, paint);
        paint.setColor(Color.argb(225, 235, 245, 255));
        paint.setTextSize(Math.max(11f, min * 0.0188f));
        canvas.drawText("XP: " + xp + " / " + nextXp + "  |  " + CareerProgressSystem.careerTierName(saveManager), main.left + 18f, main.top + h * 0.36f, paint);

        RectF bg = new RectF(main.left + 18f, main.top + h * 0.45f, main.right - 18f, main.top + h * 0.45f + Math.max(8f, min * 0.012f));
        paint.setColor(Color.argb(70, 255, 255, 255));
        canvas.drawRoundRect(bg, 8f, 8f, paint);
        RectF fg = new RectF(bg.left, bg.top, bg.left + bg.width() * xp01, bg.bottom);
        paint.setColor(Color.argb(235, 0, 220, 255));
        canvas.drawRoundRect(fg, 8f, 8f, paint);

        paint.setColor(Color.argb(230, 255, 220, 90));
        canvas.drawText(trimMenuText(CareerResultSystem.careerOverviewLine(saveManager), 58), main.left + 18f, main.top + h * 0.64f, paint);
        paint.setColor(Color.argb(220, 155, 255, 185));
        canvas.drawText(trimMenuText("Son: " + (saveManager == null ? "-" : saveManager.getCareerLastMessage()), 58), main.left + 18f, main.top + h * 0.82f, paint);

        float sx = getWidth() * 0.57f;
        float sw = getWidth() * 0.37f;
        RectF stats = new RectF(sx, y, sx + sw, y + h);
        drawGlassPanel(canvas, stats, Color.argb(142, 4, 12, 26), Color.argb(178, 255, 210, 80));
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(14f, min * 0.025f));
        canvas.drawText("Kariyer Özeti", stats.left + 16f, stats.top + h * 0.19f, paint);
        paint.setColor(Color.argb(225, 235, 245, 255));
        paint.setTextSize(Math.max(10f, min * 0.0172f));
        canvas.drawText(trimMenuText("Etkinlik: " + (saveManager == null ? 0 : saveManager.getCareerEventClaimedCount(league)) + "/" + CareerEventSystem.EVENT_COUNT
                + " • Madalya " + CareerProgressSystem.totalMedals(saveManager), 54), stats.left + 16f, stats.top + h * 0.38f, paint);
        canvas.drawText(trimMenuText("Mod kazancı: " + GameModeHubSystem.threeModeLoopLine(saveManager), 56), stats.left + 16f, stats.top + h * 0.55f, paint);
        canvas.drawText(trimMenuText("Ekonomi: " + GameModeHubSystem.economyBalanceLine(saveManager), 56), stats.left + 16f, stats.top + h * 0.72f, paint);
        paint.setColor(Color.argb(230, 255, 220, 90));
        canvas.drawText(trimMenuText("Sıradaki hedef: " + CareerEventSystem.nextCareerTarget(saveManager), 56), stats.left + 16f, stats.top + h * 0.84f, paint);
        paint.setColor(Color.argb(220, 150, 235, 255));
        canvas.drawText(trimMenuText(CareerFlowQaSystem.rewardGuardLine(saveManager, league), 54), stats.left + 16f, stats.top + h * 0.94f, paint);

        drawCareerLeagueStrip(canvas, league);
        drawCareerEventCards(canvas, league);

        float rewardY = getHeight() * 0.865f;
        careerLeagueRewardButton.set(getWidth() * 0.57f, rewardY, getWidth() * 0.94f, rewardY + Math.max(42f, min * 0.062f));
        String rewardLabel = leagueClaimed ? "LİG ÖDÜLÜ ALINDI" : leagueCompleted ? (CareerEventSystem.leagueTitle(league).toUpperCase() + " ÖDÜLÜ AL") : "LİG ÖDÜLÜ KİLİTLİ";
        drawButton(canvas, careerLeagueRewardButton, rewardLabel + "  " + CareerEventSystem.leagueRewardLine(league), leagueCompleted && !leagueClaimed, !leagueCompleted || leagueClaimed);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(212, 235, 245, 255));
        paint.setTextSize(Math.max(10f, min * 0.0165f));
        canvas.drawText(trimMenuText(CareerFlowQaSystem.transitionSafetyLine(saveManager), 88), getWidth() * 0.055f, getHeight() * 0.955f, paint);
    }

    private void drawCareerLeagueStrip(Canvas canvas, int currentLeague) {
        float min = Math.min(getWidth(), getHeight());
        float y = getHeight() * 0.455f;
        float cardW = (getWidth() * 0.89f) / CareerLeagueSystem.LEAGUE_COUNT;
        float cardH = Math.max(82f, getHeight() * 0.125f);
        for (int i = 0; i < CareerLeagueSystem.LEAGUE_COUNT; i++) {
            float lx = getWidth() * 0.055f + i * cardW;
            RectF r = new RectF(lx, y, lx + cardW - Math.max(8f, min * 0.012f), y + cardH);
            boolean unlocked = CareerEventSystem.isLeagueUnlocked(saveManager, i);
            boolean current = i == currentLeague;
            boolean completed = CareerEventSystem.isLeagueCompleted(saveManager, i);
            drawGlassPanel(canvas, r,
                    current ? Color.argb(175, 0, 130, 210) : (unlocked ? Color.argb(125, 12, 28, 36) : Color.argb(115, 20, 20, 24)),
                    current ? Color.argb(235, 0, 240, 255) : (completed ? Color.argb(190, 255, 220, 80) : (unlocked ? Color.argb(160, 120, 255, 170) : Color.argb(135, 255, 95, 95))));
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextSize(Math.max(9f, min * 0.0155f));
            paint.setColor(Color.WHITE);
            canvas.drawText(CareerLeagueSystem.leagueName(i), r.centerX(), r.top + r.height() * 0.25f, paint);
            paint.setColor(unlocked ? Color.argb(235, 145, 255, 185) : Color.argb(230, 255, 120, 120));
            canvas.drawText(unlocked ? CareerEventSystem.completedEventCount(saveManager, i) + "/" + CareerEventSystem.EVENT_COUNT : ("LVL " + CareerLeagueSystem.requiredLevel(i)), r.centerX(), r.top + r.height() * 0.52f, paint);
            paint.setColor(Color.argb(215, 235, 245, 255));
            canvas.drawText(trimMenuText(CareerFlowQaSystem.leagueUnlockAuditLine(saveManager, i), 24), r.centerX(), r.top + r.height() * 0.78f, paint);
        }
    }

    private void drawCareerEventCards(Canvas canvas, int league) {
        float min = Math.min(getWidth(), getHeight());
        float startX = getWidth() * 0.055f;
        float startY = getHeight() * 0.605f;
        float totalW = getWidth() * 0.89f;
        float gap = Math.max(8f, min * 0.014f);
        float cardW = (totalW - gap) * 0.5f;
        float cardH = Math.max(82f, getHeight() * 0.115f);
        for (int i = 0; i < CareerEventSystem.EVENT_COUNT; i++) {
            int col = i % 2;
            int row = i / 2;
            RectF r = careerEventButtons[i];
            r.set(startX + col * (cardW + gap), startY + row * (cardH + gap), startX + col * (cardW + gap) + cardW, startY + row * (cardH + gap) + cardH);
            boolean done = CareerEventSystem.isEventCompleted(saveManager, league, i);
            boolean claimed = saveManager != null && saveManager.isCareerEventRewardClaimed(league, i);
            boolean unlocked = CareerEventSystem.isLeagueUnlocked(saveManager, league);
            drawGlassPanel(canvas, r,
                    done ? Color.argb(152, 8, 46, 34) : Color.argb(126, 5, 13, 28),
                    claimed ? Color.argb(150, 120, 255, 170) : (done ? Color.argb(220, 255, 220, 80) : Color.argb(150, 0, 210, 255)));
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setTextSize(Math.max(10f, cardH * 0.165f));
            canvas.drawText(CareerEventSystem.eventTypeLabel(i) + " • " + CareerEventSystem.eventTitle(league, i), r.left + 12f, r.top + cardH * 0.23f, paint);
            paint.setColor(Color.argb(220, 235, 245, 255));
            paint.setTextSize(Math.max(8.5f, cardH * 0.135f));
            canvas.drawText(trimMenuText(CareerEventSystem.eventGoal(league, i), 44), r.left + 12f, r.top + cardH * 0.40f, paint);
            paint.setColor(done ? Color.argb(235, 155, 255, 185) : Color.argb(220, 255, 220, 90));
            canvas.drawText(trimMenuText(CareerEventSystem.eventStatusLine(saveManager, league, i), 42), r.left + 12f, r.top + cardH * 0.57f, paint);
            paint.setColor(Color.argb(205, 150, 235, 255));
            canvas.drawText(trimMenuText(CareerEventSystem.eventStartHint(league, i), 42), r.left + 12f, r.top + cardH * 0.72f, paint);
            paint.setColor(Color.argb(225, 255, 235, 120));
            canvas.drawText(CareerEventSystem.eventRewardLine(league, i), r.left + 12f, r.bottom - 10f, paint);

            RectF b = careerEventRewardButtons[i];
            float bw = Math.max(96f, r.width() * 0.30f);
            float bh = Math.max(30f, r.height() * 0.34f);
            b.set(r.right - bw - 10f, r.bottom - bh - 8f, r.right - 10f, r.bottom - 8f);
            String label = claimed ? "ALINDI" : done ? "ÖDÜL AL" : unlocked ? "BAŞLA" : "KİLİTLİ";
            drawButton(canvas, b, label, done && !claimed, !unlocked || claimed);
        }
    }

    private void startCareerEvent(int league, int event) {
        if (!CareerEventSystem.isLeagueUnlocked(saveManager, league)) {
            if (saveManager != null) saveManager.setEconomyLastMessage(CareerEventSystem.unlockText(saveManager, league));
            if (audioManager != null) audioManager.playLocked();
            return;
        }
        int mode = CareerEventSystem.eventMode(league, event);
        if (!CareerLeagueSystem.isModeUnlocked(saveManager, mode)) {
            if (saveManager != null) saveManager.setEconomyLastMessage("Bu kariyer etkinliği için " + CareerLeagueSystem.modeLockText(mode) + " gerekli");
            if (audioManager != null) audioManager.playLocked();
            return;
        }
        state.setSelectedMode(mode);
        if (mode == GameScreenState.MODE_RACE_LOCKED) {
            int route = CareerEventSystem.eventRoute(league, event);
            state.setSelectedCheckpointRoute(route);
            if (saveManager != null) saveManager.setSelectedCheckpointRoute(route);
            CheckpointRaceSystem.setActiveRoute(route);
        }
        setSelectedMapPersisted(GameScreenState.MAP_OPEN_FIELD);
        if (saveManager != null) {
            saveManager.setActiveCareerEvent(league, event, mode);
            saveManager.setEconomyLastMessage("Kariyer etkinliği başladı: " + CareerEventSystem.eventTitle(league, event));
        }
        if (audioManager != null) audioManager.playSelect();
        setScreen(GameScreenState.SCREEN_DRIVE);
    }

    private void drawModes(Canvas canvas) {
        drawTitle(canvas, "OYUN MODLARI MERKEZI", GameModeHubSystem.hubSubtitle(saveManager));
        drawButton(canvas, backButton, "GERI", false, false);

        int selected = state.getSelectedMode();
        boolean freeOk = CareerLeagueSystem.isModeUnlocked(saveManager, GameScreenState.MODE_FREE_DRIVE);
        boolean timeOk = CareerLeagueSystem.isModeUnlocked(saveManager, GameScreenState.MODE_TIME_TRIAL);
        boolean driftOk = CareerLeagueSystem.isModeUnlocked(saveManager, GameScreenState.MODE_DRIFT);
        boolean raceOk = CareerLeagueSystem.isModeUnlocked(saveManager, GameScreenState.MODE_RACE_LOCKED);
        boolean dragOk = CareerLeagueSystem.isModeUnlocked(saveManager, GameScreenState.MODE_DRAG_RACE);
        boolean policeOk = CareerLeagueSystem.isModeUnlocked(saveManager, GameScreenState.MODE_POLICE_CHASE);

        drawModeHubInfo(canvas, selected);

        drawButton(canvas, modeFree, "SERBEST SÜRÜŞ", selected == GameScreenState.MODE_FREE_DRIVE, !freeOk);
        drawModeSubline(canvas, modeFree, GameModeHubSystem.freeDriveCardLine(saveManager), freeOk);
        drawButton(canvas, modeTime, timeOk ? "ZAMAN YARIŞI" : "ZAMAN  " + CareerLeagueSystem.modeLockText(GameScreenState.MODE_TIME_TRIAL), selected == GameScreenState.MODE_TIME_TRIAL, !timeOk);
        drawModeSubline(canvas, modeTime, timeOk ? "Zaman denemesi • kariyer rotasi" : GameModeHubSystem.futureModeLine(saveManager, GameScreenState.MODE_TIME_TRIAL), timeOk);
        drawButton(canvas, modeDrift, driftOk ? "DRIFT SKOR" : "DRIFT  " + CareerLeagueSystem.modeLockText(GameScreenState.MODE_DRIFT), selected == GameScreenState.MODE_DRIFT, !driftOk);
        drawModeSubline(canvas, modeDrift, driftOk ? GameModeHubSystem.driftFinalCardLine(saveManager) : "LVL 2 açılır • skor + combo", driftOk);
        drawButton(canvas, modeRace, "CHECKPOINT YARIŞI", selected == GameScreenState.MODE_RACE_LOCKED, !raceOk);
        drawModeSubline(canvas, modeRace, GameModeHubSystem.checkpointFinalCardLine(saveManager), raceOk);
        drawButton(canvas, modeDragRace, dragOk ? "RAKİPLİ DRAG 400M" : "DRAG  " + CareerLeagueSystem.modeLockText(GameScreenState.MODE_DRAG_RACE), selected == GameScreenState.MODE_DRAG_RACE, !dragOk);
        drawModeSubline(canvas, modeDragRace, dragOk ? "Start ışığı • 400M" : GameModeHubSystem.lockedModeCardLine(saveManager, GameScreenState.MODE_DRAG_RACE), dragOk);
        drawButton(canvas, modePolice, policeOk ? "POLİS KOVALAMACA" : "POLİS  " + CareerLeagueSystem.modeLockText(GameScreenState.MODE_POLICE_CHASE), selected == GameScreenState.MODE_POLICE_CHASE, !policeOk);
        drawModeSubline(canvas, modePolice, policeOk ? GameModeHubSystem.policeFinalCardLine(saveManager) : GameModeHubSystem.lockedModeCardLine(saveManager, GameScreenState.MODE_POLICE_CHASE), policeOk);

        String startLabel = selected == GameScreenState.MODE_RACE_LOCKED
                ? (CheckpointRaceSystem.routeLabel(state.getSelectedCheckpointRoute()).toUpperCase() + " BAŞLA")
                : selected == GameScreenState.MODE_DRIFT ? "DRIFT SKOR'A BAŞLA" : selected == GameScreenState.MODE_POLICE_CHASE ? "POLİS KOVALAMACA BAŞLA" : "SECILI MODLA BASLA";
        drawButton(canvas, startButton, startLabel, true, false);
        drawButton(canvas, modeGarageButton, "GARAJ", false, false);
        drawButton(canvas, modeModifyButton, "MODİFİYE", false, false);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(220, 235, 245, 255));
        paint.setTextSize(Math.max(11f, getHeight() * 0.018f));
        canvas.drawText(GameModeHubSystem.progressPercentLine(saveManager) + "  |  " + CareerProgressSystem.nextShortGoal(saveManager), getWidth() * 0.055f, getHeight() * 0.84f, paint);
        canvas.drawText(trimMenuText(GameModeHubSystem.threeModeLoopLine(saveManager), 72), getWidth() * 0.055f, getHeight() * 0.88f, paint);
        canvas.drawText(trimMenuText(GameModeHubSystem.economyBalanceLine(saveManager), 72), getWidth() * 0.055f, getHeight() * 0.92f, paint);
        canvas.drawText(trimMenuText(GameModeHubSystem.taskAchievementBridgeLine(), 72), getWidth() * 0.055f, getHeight() * 0.955f, paint);
    }

    private void drawModeHubInfo(Canvas canvas, int selected) {
        float min = Math.min(getWidth(), getHeight());
        float x = getWidth() * 0.055f;
        float y = getHeight() * 0.190f;
        float w = getWidth() * 0.42f;
        float h = Math.max(265f, getHeight() * 0.455f);
        RectF r = new RectF(x, y, x + w, y + h);
        drawGlassPanel(canvas, r, Color.argb(138, 4, 12, 26), Color.argb(172, 0, 220, 255));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(16f, min * 0.029f));
        canvas.drawText(UiBalanceSystem.modeTitle(selected), r.left + 18f, r.top + h * 0.13f, paint);
        paint.setColor(Color.argb(225, 235, 245, 255));
        paint.setTextSize(Math.max(11f, min * 0.0185f));
        canvas.drawText(trimMenuText(UiBalanceSystem.modeDescription(selected), 52), r.left + 18f, r.top + h * 0.26f, paint);
        paint.setColor(Color.argb(238, 255, 220, 90));
        canvas.drawText(UiBalanceSystem.modeRewardText(selected), r.left + 18f, r.top + h * 0.38f, paint);
        paint.setColor(Color.argb(220, 155, 255, 185));
        canvas.drawText(GameModeHubSystem.hubHeaderLine(saveManager), r.left + 18f, r.top + h * 0.50f, paint);
        canvas.drawText(CareerLeagueSystem.nextLeagueText(saveManager), r.left + 18f, r.top + h * 0.60f, paint);

        RectF progressBg = new RectF(r.left + 18f, r.top + h * 0.650f, r.right - 18f, r.top + h * 0.650f + Math.max(7f, h * 0.020f));
        paint.setColor(Color.argb(72, 255, 255, 255));
        canvas.drawRoundRect(progressBg, 8f, 8f, paint);
        float hubPct = CareerProgressSystem.overallProgressPercent(saveManager) / 100f;
        RectF progressFg = new RectF(progressBg.left, progressBg.top, progressBg.left + progressBg.width() * hubPct, progressBg.bottom);
        paint.setColor(Color.argb(225, 0, 220, 255));
        canvas.drawRoundRect(progressFg, 8f, 8f, paint);

        paint.setColor(Color.argb(210, 235, 245, 255));
        canvas.drawText(trimMenuText(CareerProgressSystem.careerCenterLine(saveManager), 52), r.left + 18f, r.top + h * 0.755f, paint);
        canvas.drawText(trimMenuText(GameModeHubSystem.selectedVehicleBuildLine(saveManager, state.getSelectedVehicleIndex()), 52), r.left + 18f, r.top + h * 0.845f, paint);
        paint.setColor(Color.argb(218, 155, 255, 205));
        canvas.drawText(trimMenuText(GameModeHubSystem.selectedModeSummary(saveManager, selected), 54), r.left + 18f, r.top + h * 0.930f, paint);
        canvas.drawText(trimMenuText(GameModeHubSystem.modeTransitionSafetyLine(saveManager, selected), 54), r.left + 18f, r.top + h * 0.985f, paint);
        if (selected == GameScreenState.MODE_RACE_LOCKED) {
            int route = state.getSelectedCheckpointRoute();
            String best = saveManager == null || saveManager.getCheckpointRouteBestSeconds(route) <= 0f ? "-" : raceTimeText(saveManager.getCheckpointRouteBestSeconds(route));
            // A65.4: rota detayları artık üst kariyer özetini ezmeden alttaki 2x2 profesyonel rota kartlarında gösterilir.
            drawCheckpointRouteCards(canvas);
        } else if (selected == GameScreenState.MODE_DRIFT) {
            // Drift özeti GameModeHubSystem.selectedModeSummary içinde gösterilir;
            // ekstra satır çizilip panel üst üste bindirilmez.
        }
    }

    private void drawCheckpointRouteCards(Canvas canvas) {
        if (checkpointRouteButtons[0] == null) return;
        int selectedRoute = state.getSelectedCheckpointRoute();
        paint.setTextAlign(Paint.Align.LEFT);
        for (int i = 0; i < checkpointRouteButtons.length; i++) {
            RectF rr = checkpointRouteButtons[i];
            boolean selected = i == selectedRoute;
            drawGlassPanel(canvas, rr, selected ? Color.argb(160, 10, 42, 62) : Color.argb(112, 4, 12, 26),
                    selected ? Color.argb(230, 255, 220, 80) : Color.argb(142, 0, 205, 255));
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(selected ? Color.argb(255, 255, 235, 120) : Color.WHITE);
            paint.setTextSize(Math.max(10f, rr.height() * 0.245f));
            canvas.drawText(CheckpointRaceSystem.routeLabel(i), rr.left + 10f, rr.top + rr.height() * 0.28f, paint);
            paint.setColor(Color.argb(218, 225, 245, 255));
            paint.setTextSize(Math.max(8f, rr.height() * 0.175f));
            canvas.drawText(trimMenuText(CheckpointRaceSystem.routeDescription(i), 30), rr.left + 10f, rr.top + rr.height() * 0.48f, paint);
            paint.setColor(Color.argb(224, 255, 220, 90));
            paint.setTextSize(Math.max(7.3f, rr.height() * 0.145f));
            String balance = RaceResultSystem.checkpointRouteCardSubline(i,
                    saveManager == null ? 0f : saveManager.getCheckpointRouteLastSeconds(i),
                    saveManager == null ? "" : saveManager.getCheckpointRouteLastMedal(i));
            canvas.drawText(trimMenuText(balance, 36), rr.left + 10f, rr.top + rr.height() * 0.66f, paint);
            String progress = saveManager == null ? "En iyi -" : RaceResultSystem.checkpointRouteCardText(
                    i,
                    saveManager.getCheckpointRouteBestSeconds(i),
                    saveManager.getCheckpointRouteBestMedal(i),
                    saveManager.getCheckpointRouteCompletedCount(i),
                    saveManager.getCheckpointRouteEarnedCoins(i));
            paint.setColor(Color.argb(218, 155, 255, 205));
            paint.setTextSize(Math.max(7.3f, rr.height() * 0.145f));
            canvas.drawText(trimMenuText(progress, 34), rr.left + 10f, rr.bottom - 8f, paint);
            if (selected) {
                paint.setTextAlign(Paint.Align.RIGHT);
                paint.setColor(Color.argb(230, 255, 220, 80));
                paint.setTextSize(Math.max(8f, rr.height() * 0.18f));
                canvas.drawText("SEÇİLİ", rr.right - 10f, rr.top + rr.height() * 0.28f, paint);
                paint.setTextAlign(Paint.Align.LEFT);
            }
        }
    }

    private void drawModeSubline(Canvas canvas, RectF rect, String text, boolean unlocked) {
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(unlocked ? Color.argb(190, 225, 245, 255) : Color.argb(190, 255, 125, 125));
        paint.setTextSize(Math.max(7.5f, rect.height() * 0.17f));
        canvas.drawText(trimMenuText(text, 34), rect.right - 10f, rect.bottom - Math.max(5f, rect.height() * 0.10f), paint);
    }

    private void drawMaps(Canvas canvas) {
        drawTitle(canvas, "HARITALAR", "Dahili haritalar / Açık Dünya GLB kapalı");
        drawButton(canvas, backButton, "GERI", false, false);

        int selected = state.getSelectedMap();
        if (selected == GameScreenState.MAP_OPEN_WORLD || selected == GameScreenState.MAP_SECOND_NEW) {
            selected = GameScreenState.MAP_OPEN_FIELD;
        }
        drawButton(canvas, mapOpen, MapRegistry.displayName(GameScreenState.MAP_OPEN_FIELD), selected == GameScreenState.MAP_OPEN_FIELD, false);
        drawButton(canvas, mapCity, saveManager.isMapUnlockedByCareer(GameScreenState.MAP_CITY) ? MapRegistry.displayName(GameScreenState.MAP_CITY) : "BUYUK SEHIR  LVL 3", selected == GameScreenState.MAP_CITY, !saveManager.isMapUnlockedByCareer(GameScreenState.MAP_CITY));
        drawButton(canvas, mapHighway, saveManager.isMapUnlockedByCareer(GameScreenState.MAP_HIGHWAY) ? MapRegistry.displayName(GameScreenState.MAP_HIGHWAY) : "OTOYOL  LVL 5", selected == GameScreenState.MAP_HIGHWAY, !saveManager.isMapUnlockedByCareer(GameScreenState.MAP_HIGHWAY));
        drawButton(canvas, mapDrift, saveManager.isMapUnlockedByCareer(GameScreenState.MAP_DRIFT_PARK) ? MapRegistry.displayName(GameScreenState.MAP_DRIFT_PARK) : "DRIFT PARK  LVL 8", selected == GameScreenState.MAP_DRIFT_PARK, !saveManager.isMapUnlockedByCareer(GameScreenState.MAP_DRIFT_PARK));
        drawButton(canvas, mapSelectButton, "HARITAYI SEC", true, false);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(230, 235, 245, 255));
        paint.setTextSize(Math.max(13f, getHeight() * 0.021f));
        canvas.drawText("Secili harita: " + GameScreenState.mapLabel(selected), getWidth() * 0.06f, getHeight() * 0.76f, paint);
        canvas.drawText("A61.6: Açık Dünya GLB asset'i kaldırıldı, menüden seçilemez.", getWidth() * 0.06f, getHeight() * 0.82f, paint);
        canvas.drawText("Şimdilik boş/test sürüş ve dahili haritalar korunur.", getWidth() * 0.06f, getHeight() * 0.875f, paint);
    }

    private void drawQuestPanel(Canvas canvas) {
        drawTitle(canvas, "GÖREV PANELİ", "A63.8: profesyonel kartlı tasarım, tek tek ödül al ve Hepsini Al");
        drawButton(canvas, backButton, "GERİ", false, false);

        drawQuestTab(canvas, questTab0, QuestPanelSystem.TAB_ACTIVE);
        drawQuestTab(canvas, questTab1, QuestPanelSystem.TAB_DAILY);
        drawQuestTab(canvas, questTab2, QuestPanelSystem.TAB_WEEKLY);
        drawQuestTab(canvas, questTab3, QuestPanelSystem.TAB_COMPLETED);
        drawQuestTab(canvas, questTab4, QuestPanelSystem.TAB_REWARDS);
        drawQuestTab(canvas, questTab5, QuestPanelSystem.TAB_UPCOMING);
        drawQuestTab(canvas, questTab6, QuestPanelSystem.TAB_LOCKED);

        drawQuestMainCard(canvas);
        drawQuestSideCard(canvas);
    }

    private void drawQuestTab(Canvas canvas, RectF rect, int tab) {
        boolean selected = questPanelTab == tab;
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(selected ? Color.argb(205, 0, 180, 255) : Color.argb(100, 10, 14, 22));
        canvas.drawRoundRect(rect, 14f, 14f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(selected ? 2.4f : 1.3f);
        paint.setColor(selected ? Color.argb(235, 160, 235, 255) : Color.argb(120, 255, 255, 255));
        canvas.drawRoundRect(rect, 14f, 14f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(9.5f, getHeight() * 0.0165f));
        paint.setColor(Color.WHITE);
        String label = questTabLabel(tab);
        canvas.drawText(label, rect.centerX(), rect.centerY() + 4f, paint);
    }

    private void drawQuestMainCard(Canvas canvas) {
        drawGlassPanel(canvas, questMainCard, Color.argb(168, 4, 8, 16), Color.argb(175, 0, 210, 255));
        if (questPanelTab == QuestPanelSystem.TAB_DAILY
                || questPanelTab == QuestPanelSystem.TAB_WEEKLY
                || questPanelTab == QuestPanelSystem.TAB_REWARDS) {
            drawQuestRewardGrid(canvas);
            return;
        }
        if (questPanelTab == QuestPanelSystem.TAB_COMPLETED) {
            drawQuestStatsDashboard(canvas);
            return;
        }

        float x = questMainCard.left + 18f;
        float y = questMainCard.top + 34f;
        int step = saveManager.getQuestChainStep();
        int completed = saveManager.getQuestChainCompletedCount();
        float progress = Math.max(0f, Math.min(1f, completed / 7f));

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(Math.max(18f, getHeight() * 0.030f));
        paint.setColor(Color.WHITE);
        canvas.drawText(mainQuestTitle(), x, y, paint);

        paint.setTextSize(Math.max(12f, getHeight() * 0.021f));
        paint.setColor(Color.argb(220, 235, 245, 255));
        for (int i = 0; i < mainQuestLineCount(); i++) {
            canvas.drawText(mainQuestLine(i), x, y + 34f + i * Math.max(22f, getHeight() * 0.036f), paint);
        }

        float barLeft = x;
        float barTop = questMainCard.bottom - 54f;
        float barW = questMainCard.width() - 36f;
        float barH = 10f;
        paint.setColor(Color.argb(95, 255, 255, 255));
        canvas.drawRoundRect(new RectF(barLeft, barTop, barLeft + barW, barTop + barH), 8f, 8f, paint);
        paint.setColor(Color.argb(235, 255, 215, 70));
        canvas.drawRoundRect(new RectF(barLeft, barTop, barLeft + barW * progress, barTop + barH), 8f, 8f, paint);
        paint.setTextSize(Math.max(11f, getHeight() * 0.019f));
        paint.setColor(Color.argb(230, 255, 235, 120));
        canvas.drawText("Kariyer ilerleme: " + completed + "/7", barLeft, barTop + 30f, paint);
        resetQuestActionRects();
    }

    private String mainQuestTitle() {
        int step = saveManager.getQuestChainStep();
        if (questPanelTab == QuestPanelSystem.TAB_ACTIVE) return "AKTİF GÖREV: " + QuestPanelSystem.questTitle(step);
        if (questPanelTab == QuestPanelSystem.TAB_DAILY) return "GÜNLÜK GÖREVLER / ÖDÜL AL";
        if (questPanelTab == QuestPanelSystem.TAB_WEEKLY) return "HAFTALIK GÖREVLER / ÖDÜL AL";
        if (questPanelTab == QuestPanelSystem.TAB_COMPLETED) return "İSTATİSTİKLER";
        if (questPanelTab == QuestPanelSystem.TAB_REWARDS) return "BAŞARIMLAR VE ÖDÜLLER";
        if (questPanelTab == QuestPanelSystem.TAB_UPCOMING) return "YAKINDA AÇILACAK GÖREVLER";
        return "KİLİTLİ GÖREVLER";
    }

    private int mainQuestLineCount() {
        if (questPanelTab == QuestPanelSystem.TAB_COMPLETED) return 11;
        if (questPanelTab == QuestPanelSystem.TAB_REWARDS) return 9;
        return 6;
    }

    private String mainQuestLine(int line) {
        int step = saveManager.getQuestChainStep();
        int next = Math.min(6, step + 1);
        if (questPanelTab == QuestPanelSystem.TAB_ACTIVE) {
            if (line == 0) return QuestPanelSystem.questObjective(step);
            if (line == 1) return "Ödül: " + QuestPanelSystem.questReward(step);
            if (line == 2) return "Hedef: Mini haritada Q işareti / açık dünya noktası";
            if (line == 3) return "Durum: " + saveManager.getQuestChainCompletedCount() + "/7 tamamlandı";
            if (line == 4) return "Son mesaj: " + trimPanelText(saveManager.getQuestChainLastMessage(), 58);
            return "Ekonomi: " + trimPanelText(saveManager.getEconomyLastMessage(), 58);
        }
        if (questPanelTab == QuestPanelSystem.TAB_DAILY) {
            return DailyWeeklyTaskSystem.dailyLine(saveManager, line);
        }
        if (questPanelTab == QuestPanelSystem.TAB_WEEKLY) {
            return DailyWeeklyTaskSystem.weeklyLine(saveManager, line);
        }
        if (questPanelTab == QuestPanelSystem.TAB_COMPLETED) {
            return PlayerStatsSystem.line(saveManager, line);
        }
        if (questPanelTab == QuestPanelSystem.TAB_REWARDS) {
            if (line == 0) return AchievementSystem.summary(saveManager);
            int ach = line - 1;
            if (ach >= 0 && ach < AchievementSystem.ACH_COUNT) return AchievementSystem.line(saveManager, ach);
            return "Kart alanına dokun: sıradaki hazır başarım ödülünü manuel al. Aynı ödül ikinci kez verilmez.";
        }
        if (questPanelTab == QuestPanelSystem.TAB_UPCOMING) {
            if (line == 0) return "Sıradaki: " + QuestPanelSystem.questTitle(next);
            if (line == 1) return QuestPanelSystem.questObjective(next);
            if (line == 2) return "Ödül: " + QuestPanelSystem.questReward(next);
            if (line == 3) return "Gereken seviye: " + QuestPanelSystem.questRequiredLevel(next);
            if (line == 4) return "Sonraki içerikler: araç / parça / renk / jant / bölge";
            return "Yakında açılacak görevler ilerledikçe otomatik aktif olur";
        }
        if (line == 0) return lockedLine(0);
        if (line == 1) return lockedLine(1);
        if (line == 2) return lockedLine(2);
        if (line == 3) return lockedLine(3);
        if (line == 4) return "Harita kilitleri: " + QuestPanelSystem.mapUnlockSummary(saveManager);
        return "Araç/parça kilitleri ödül sistemiyle açılır";
    }

    private String completedLine(int offset) {
        int index = offset;
        if (index >= saveManager.getQuestChainCompletedCount()) return "- Bekliyor: " + QuestPanelSystem.questTitle(index);
        return "✓ " + QuestPanelSystem.questTitle(index) + " | " + QuestPanelSystem.questReward(index);
    }

    private String lockedLine(int index) {
        boolean locked = QuestPanelSystem.isQuestLocked(saveManager, index);
        return (locked ? "🔒 " : "Açık ") + QuestPanelSystem.questTitle(index)
                + " | LVL " + QuestPanelSystem.questRequiredLevel(index);
    }

    private void drawQuestSideCard(Canvas canvas) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(142, 4, 8, 16));
        canvas.drawRoundRect(questSideCard, 22f, 22f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.6f);
        paint.setColor(Color.argb(155, 255, 220, 70));
        canvas.drawRoundRect(questSideCard, 22f, 22f, paint);

        float x = questSideCard.left + 15f;
        float y = questSideCard.top + 30f;
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(Math.max(13f, getHeight() * 0.023f));
        paint.setColor(Color.WHITE);
        canvas.drawText("Hızlı Özet", x, y, paint);
        paint.setTextSize(Math.max(10f, getHeight() * 0.018f));
        paint.setColor(Color.argb(222, 235, 245, 255));
        canvas.drawText("Aktif: " + QuestPanelSystem.questTitle(saveManager.getQuestChainStep()), x, y + 30f, paint);
        canvas.drawText("Günlük: " + DailyWeeklyTaskSystem.completedDailyCount(saveManager) + "/" + DailyWeeklyTaskSystem.DAILY_COUNT
                + "  Alınan: " + saveManager.getDailyTaskClaimedCount(DailyWeeklyTaskSystem.DAILY_COUNT), x, y + 55f, paint);
        canvas.drawText("Haftalık: " + DailyWeeklyTaskSystem.completedWeeklyCount(saveManager) + "/" + DailyWeeklyTaskSystem.WEEKLY_COUNT
                + "  Alınan: " + saveManager.getWeeklyTaskClaimedCount(DailyWeeklyTaskSystem.WEEKLY_COUNT), x, y + 80f, paint);
        canvas.drawText("Başarım: " + AchievementSystem.completedCount(saveManager) + "/" + AchievementSystem.ACH_COUNT
                + "  Bekleyen: " + AchievementSystem.unclaimedCount(saveManager), x, y + 105f, paint);
        canvas.drawText("Trafik: " + saveManager.getTrafficNearMissTotal() + " yakın geçiş | " + saveManager.getTrafficCollisions() + " çarpışma", x, y + 130f, paint);
        canvas.drawText("Drift: " + saveManager.getDriftTotalScore() + " skor | Combo x" + saveManager.getDriftBestCombo(), x, y + 155f, paint);
        canvas.drawText("Araç: " + QuestPanelSystem.ownedVehicleCount(saveManager) + "/" + VehicleCatalog.count()
                + " | Mod: " + saveManager.getTotalPerformanceUpgradeLevelAllVehicles(), x, y + 180f, paint);
    }

    private int hitQuestTab(float x, float y) {
        if (questTab0.contains(x, y)) return QuestPanelSystem.TAB_ACTIVE;
        if (questTab1.contains(x, y)) return QuestPanelSystem.TAB_DAILY;
        if (questTab2.contains(x, y)) return QuestPanelSystem.TAB_WEEKLY;
        if (questTab3.contains(x, y)) return QuestPanelSystem.TAB_COMPLETED;
        if (questTab4.contains(x, y)) return QuestPanelSystem.TAB_REWARDS;
        if (questTab5.contains(x, y)) return QuestPanelSystem.TAB_UPCOMING;
        if (questTab6.contains(x, y)) return QuestPanelSystem.TAB_LOCKED;
        return -1;
    }

    private String questTabLabel(int tab) {
        String label = QuestPanelSystem.tabLabel(tab);
        int count = 0;
        if (tab == QuestPanelSystem.TAB_DAILY) count = DailyWeeklyTaskSystem.unclaimedDailyCount(saveManager);
        else if (tab == QuestPanelSystem.TAB_WEEKLY) count = DailyWeeklyTaskSystem.unclaimedWeeklyCount(saveManager);
        else if (tab == QuestPanelSystem.TAB_REWARDS) count = AchievementSystem.unclaimedCount(saveManager);
        if (count > 0) return label + " (" + count + ")";
        return label;
    }

    private void resetQuestActionRects() {
        questClaimAllButton.set(0f, 0f, 0f, 0f);
        for (int i = 0; i < questRewardCards.length; i++) {
            questRewardCards[i].set(0f, 0f, 0f, 0f);
            questRewardButtons[i].set(0f, 0f, 0f, 0f);
        }
    }

    private void prepareQuestGrid(int itemCount, int rows) {
        resetQuestActionRects();
        if (itemCount <= 0 || rows <= 0) return;
        float margin = Math.max(16f, Math.min(getWidth(), getHeight()) * 0.018f);
        float top = questMainCard.top + Math.max(54f, Math.min(getWidth(), getHeight()) * 0.070f);
        float bottom = questMainCard.bottom - margin;
        float left = questMainCard.left + margin;
        float right = questMainCard.right - margin;
        float gapX = Math.max(10f, Math.min(getWidth(), getHeight()) * 0.014f);
        float gapY = Math.max(10f, Math.min(getWidth(), getHeight()) * 0.014f);
        int cols = 2;
        float cardW = (right - left - gapX) / cols;
        float cardH = (bottom - top - gapY * (rows - 1)) / rows;
        for (int i = 0; i < itemCount && i < questRewardCards.length; i++) {
            int row = i / cols;
            int col = i % cols;
            float cl = left + col * (cardW + gapX);
            float ct = top + row * (cardH + gapY);
            questRewardCards[i].set(cl, ct, cl + cardW, ct + cardH);
            float btnW = Math.max(74f, cardW * 0.34f);
            float btnH = Math.max(24f, cardH * 0.22f);
            questRewardButtons[i].set(cl + cardW - btnW - 10f, ct + cardH - btnH - 10f, cl + cardW - 10f, ct + cardH - 10f);
        }
    }

    private void prepareQuestRewardOrder(int itemCount) {
        boolean[] used = new boolean[AchievementSystem.ACH_COUNT];
        for (int i = 0; i < questRewardIds.length; i++) questRewardIds[i] = i;
        int out = 0;
        for (int group = 0; group < 3 && out < itemCount; group++) {
            while (out < itemCount) {
                int best = -1;
                float bestScore = -99999f;
                for (int id = 0; id < itemCount; id++) {
                    if (used[id]) continue;
                    if (questSortGroup(id) != group) continue;
                    float score = group == 1 ? questProgressRatioForSort(id) : (1000f - id);
                    if (score > bestScore) { bestScore = score; best = id; }
                }
                if (best < 0) break;
                used[best] = true;
                questRewardIds[out++] = best;
            }
        }
    }

    private int questSortGroup(int id) {
        if (questPanelTab == QuestPanelSystem.TAB_DAILY) {
            if (DailyWeeklyTaskSystem.isDailyClaimable(saveManager, id)) return 0;
            if (saveManager != null && saveManager.isDailyTaskRewardClaimed(id)) return 2;
            return 1;
        }
        if (questPanelTab == QuestPanelSystem.TAB_WEEKLY) {
            if (DailyWeeklyTaskSystem.isWeeklyClaimable(saveManager, id)) return 0;
            if (saveManager != null && saveManager.isWeeklyTaskRewardClaimed(id)) return 2;
            return 1;
        }
        if (AchievementSystem.isClaimable(saveManager, id)) return 0;
        if (saveManager != null && saveManager.isAchievementRewardClaimed(id)) return 2;
        return 1;
    }

    private float questProgressRatioForSort(int id) {
        if (questPanelTab == QuestPanelSystem.TAB_DAILY) return DailyWeeklyTaskSystem.dailyProgress(saveManager, id) / (float)Math.max(1, DailyWeeklyTaskSystem.dailyTarget(id));
        if (questPanelTab == QuestPanelSystem.TAB_WEEKLY) return DailyWeeklyTaskSystem.weeklyProgress(saveManager, id) / (float)Math.max(1, DailyWeeklyTaskSystem.weeklyTarget(id));
        return AchievementSystem.progress(saveManager, id) / (float)Math.max(1, AchievementSystem.target(id));
    }


    private void drawQuestRewardGrid(Canvas canvas) {
        int itemCount = questPanelTab == QuestPanelSystem.TAB_REWARDS ? AchievementSystem.ACH_COUNT : DailyWeeklyTaskSystem.DAILY_COUNT;
        int rows = questPanelTab == QuestPanelSystem.TAB_REWARDS ? 4 : 3;
        prepareQuestGrid(itemCount, rows);
        prepareQuestRewardOrder(itemCount);

        float titleX = questMainCard.left + 18f;
        float titleY = questMainCard.top + 32f;
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(16f, getHeight() * 0.026f));
        canvas.drawText(mainQuestTitle(), titleX, titleY, paint);
        paint.setTextSize(Math.max(9.5f, getHeight() * 0.0165f));
        paint.setColor(Color.argb(215, 220, 235, 245));
        if (questPanelTab == QuestPanelSystem.TAB_DAILY) {
            canvas.drawText(TaskAchievementHudSystem.dailyResetText(saveManager) + "  •  Tek tek al veya sağdaki HEPSİNİ AL ile topla.", titleX, titleY + 20f, paint);
        } else if (questPanelTab == QuestPanelSystem.TAB_WEEKLY) {
            canvas.drawText(TaskAchievementHudSystem.weeklyResetText(saveManager) + "  •  Her ödül güvenli ve tek seferliktir.", titleX, titleY + 20f, paint);
        } else {
            canvas.drawText("Başarımlar otomatik verilmez. " + TaskAchievementHudSystem.pendingSummary(saveManager), titleX, titleY + 20f, paint);
        }

        for (int i = 0; i < itemCount && i < questRewardCards.length; i++) {
            int id = questRewardIds[i];
            if (questPanelTab == QuestPanelSystem.TAB_DAILY) drawDailyQuestCard(canvas, id, questRewardCards[i], questRewardButtons[i]);
            else if (questPanelTab == QuestPanelSystem.TAB_WEEKLY) drawWeeklyQuestCard(canvas, id, questRewardCards[i], questRewardButtons[i]);
            else drawAchievementCard(canvas, id, questRewardCards[i], questRewardButtons[i]);
        }
    }

    private void drawDailyQuestCard(Canvas canvas, int id, RectF card, RectF button) {
        drawQuestRewardCard(canvas, card, button,
                "GÜNLÜK", DailyWeeklyTaskSystem.dailyTitle(id), DailyWeeklyTaskSystem.dailyDescription(id),
                Math.max(0, DailyWeeklyTaskSystem.dailyProgress(saveManager, id)), DailyWeeklyTaskSystem.dailyTarget(id),
                DailyWeeklyTaskSystem.dailyCoinReward(id), DailyWeeklyTaskSystem.dailyXpReward(id),
                DailyWeeklyTaskSystem.isDailyComplete(saveManager, id), saveManager.isDailyTaskRewardClaimed(id), DailyWeeklyTaskSystem.dailyStatus(saveManager, id));
    }

    private void drawWeeklyQuestCard(Canvas canvas, int id, RectF card, RectF button) {
        drawQuestRewardCard(canvas, card, button,
                "HAFTALIK", DailyWeeklyTaskSystem.weeklyTitle(id), DailyWeeklyTaskSystem.weeklyDescription(id),
                Math.max(0, DailyWeeklyTaskSystem.weeklyProgress(saveManager, id)), DailyWeeklyTaskSystem.weeklyTarget(id),
                DailyWeeklyTaskSystem.weeklyCoinReward(id), DailyWeeklyTaskSystem.weeklyXpReward(id),
                DailyWeeklyTaskSystem.isWeeklyComplete(saveManager, id), saveManager.isWeeklyTaskRewardClaimed(id), DailyWeeklyTaskSystem.weeklyStatus(saveManager, id));
    }

    private void drawAchievementCard(Canvas canvas, int id, RectF card, RectF button) {
        drawQuestRewardCard(canvas, card, button,
                "BAŞARIM", AchievementSystem.label(id), AchievementSystem.description(id),
                Math.max(0, AchievementSystem.progress(saveManager, id)), AchievementSystem.target(id),
                AchievementSystem.coinReward(id), AchievementSystem.xpReward(id),
                AchievementSystem.isCompleted(saveManager, id), saveManager.isAchievementRewardClaimed(id), AchievementSystem.status(saveManager, id));
    }

    private void drawQuestRewardCard(Canvas canvas, RectF card, RectF button, String badge, String title, String subtitle,
                                     int progress, int target, int coinReward, int xpReward,
                                     boolean completed, boolean claimed, String statusText) {
        int fill = claimed ? Color.argb(110, 20, 80, 40) : (completed ? Color.argb(125, 14, 62, 36) : Color.argb(110, 12, 18, 28));
        int stroke = claimed ? Color.argb(150, 86, 230, 140) : (completed ? Color.argb(170, 255, 220, 90) : Color.argb(135, 0, 220, 255));
        drawGlassPanel(canvas, card, fill, stroke);

        float x = card.left + 12f;
        float y = card.top + 18f;
        float min = Math.min(card.width(), card.height());

        RectF badgeRect = new RectF(card.left + 10f, card.top + 10f, card.left + Math.max(54f, card.width() * 0.26f), card.top + Math.max(18f, card.height() * 0.18f));
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(claimed ? Color.argb(200, 40, 160, 90) : Color.argb(160, 0, 180, 255));
        canvas.drawRoundRect(badgeRect, 14f, 14f, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(7.8f, min * 0.095f));
        paint.setColor(Color.WHITE);
        Paint.FontMetrics fm = paint.getFontMetrics();
        canvas.drawText(badge, badgeRect.centerX(), badgeRect.centerY() - (fm.ascent + fm.descent) * 0.5f, paint);

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(10.5f, min * 0.118f));
        canvas.drawText(trimPanelText(title, 24), x, y + 18f, paint);

        paint.setColor(Color.argb(214, 226, 238, 247));
        paint.setTextSize(Math.max(8.0f, min * 0.080f));
        canvas.drawText(trimPanelText(subtitle, 38), x, y + 36f, paint);

        float barLeft = x;
        float barTop = card.top + card.height() * 0.52f;
        float barRight = card.right - 12f;
        float barH = Math.max(8f, card.height() * 0.08f);
        float ratio = target <= 0 ? 0f : Math.max(0f, Math.min(1f, progress / (float) target));
        paint.setColor(Color.argb(82, 255, 255, 255));
        canvas.drawRoundRect(new RectF(barLeft, barTop, barRight, barTop + barH), 10f, 10f, paint);
        paint.setColor(claimed ? Color.argb(235, 64, 220, 130) : Color.argb(235, 255, 210, 70));
        canvas.drawRoundRect(new RectF(barLeft, barTop, barLeft + (barRight - barLeft) * ratio, barTop + barH), 10f, 10f, paint);

        paint.setTextSize(Math.max(7.8f, min * 0.078f));
        paint.setColor(Color.argb(230, 250, 240, 165));
        canvas.drawText(Math.min(progress, target) + "/" + target + "  (" + (int) (ratio * 100f) + "%)", barLeft, barTop + barH + 12f, paint);

        paint.setColor(Color.argb(230, 170, 225, 255));
        canvas.drawText("Ödül: +" + coinReward + " coin  +" + xpReward + " XP", barLeft, card.bottom - Math.max(34f, card.height() * 0.23f), paint);
        paint.setColor(claimed ? Color.argb(230, 120, 255, 170) : (completed ? Color.argb(240, 255, 235, 130) : Color.argb(210, 214, 226, 240)));
        canvas.drawText(trimPanelText(statusText, 28), barLeft, card.bottom - 14f, paint);

        String btnText = claimed ? "ALINDI" : (completed ? "ÖDÜL AL" : "DEVAM");
        drawButton(canvas, button, btnText, completed && !claimed, claimed || !completed);
    }

    private void drawQuestBulkAction(Canvas canvas) {
        int pendingCount = 0;
        int totalCoin = 0;
        int totalXp = 0;
        String scope = "Bu sekmede";
        if (questPanelTab == QuestPanelSystem.TAB_DAILY) {
            pendingCount = DailyWeeklyTaskSystem.unclaimedDailyCount(saveManager);
            totalCoin = DailyWeeklyTaskSystem.pendingDailyCoinTotal(saveManager);
            totalXp = DailyWeeklyTaskSystem.pendingDailyXpTotal(saveManager);
            scope = "Günlük ödüller";
        } else if (questPanelTab == QuestPanelSystem.TAB_WEEKLY) {
            pendingCount = DailyWeeklyTaskSystem.unclaimedWeeklyCount(saveManager);
            totalCoin = DailyWeeklyTaskSystem.pendingWeeklyCoinTotal(saveManager);
            totalXp = DailyWeeklyTaskSystem.pendingWeeklyXpTotal(saveManager);
            scope = "Haftalık ödüller";
        } else if (questPanelTab == QuestPanelSystem.TAB_REWARDS) {
            pendingCount = AchievementSystem.unclaimedCount(saveManager);
            totalCoin = AchievementSystem.pendingCoinTotal(saveManager);
            totalXp = AchievementSystem.pendingXpTotal(saveManager);
            scope = "Başarım ödülleri";
        }

        float panelTop = questSideCard.top + questSideCard.height() * 0.60f;
        RectF actionInfo = new RectF(questSideCard.left + 12f, panelTop, questSideCard.right - 12f, questSideCard.bottom - 18f);
        drawGlassPanel(canvas, actionInfo, Color.argb(80, 6, 16, 30), Color.argb(120, 0, 220, 255));
        float x = actionInfo.left + 12f;
        float y = actionInfo.top + 24f;
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(Math.max(11f, getHeight() * 0.0185f));
        paint.setColor(Color.WHITE);
        canvas.drawText("Toplu Ödül Alma", x, y, paint);
        paint.setTextSize(Math.max(9.0f, getHeight() * 0.0155f));
        paint.setColor(Color.argb(220, 235, 245, 255));
        canvas.drawText(scope + ": " + pendingCount + " bekliyor", x, y + 24f, paint);
        if (questPanelTab == QuestPanelSystem.TAB_DAILY) canvas.drawText(TaskAchievementHudSystem.dailyResetText(saveManager), x, y + 89f, paint);
        else if (questPanelTab == QuestPanelSystem.TAB_WEEKLY) canvas.drawText(TaskAchievementHudSystem.weeklyResetText(saveManager), x, y + 89f, paint);
        else if (questPanelTab == QuestPanelSystem.TAB_REWARDS) canvas.drawText(TaskAchievementHudSystem.pendingSummary(saveManager), x, y + 89f, paint);
        canvas.drawText("Toplam: +" + totalCoin + " coin  +" + totalXp + " XP", x, y + 46f, paint);
        paint.setColor(Color.argb(196, 255, 236, 125));
        if (questPanelTab == QuestPanelSystem.TAB_DAILY || questPanelTab == QuestPanelSystem.TAB_WEEKLY || questPanelTab == QuestPanelSystem.TAB_REWARDS) {
            canvas.drawText("Kartlardan tek tek alabilir veya tek tuşla tüm bekleyen ödülleri toplayabilirsin.", x, y + 68f, paint);
            questClaimAllButton.set(actionInfo.left + 12f, actionInfo.bottom - 48f, actionInfo.right - 12f, actionInfo.bottom - 10f);
            drawButton(canvas, questClaimAllButton, pendingCount > 0 ? "HEPSİNİ AL" : "ALINACAK ÖDÜL YOK", true, pendingCount <= 0);
        } else {
            canvas.drawText("Bu sekmede toplu ödül alma yok. Diğer sekmelerde aktifleşir.", x, y + 68f, paint);
            questClaimAllButton.set(0f, 0f, 0f, 0f);
        }
    }

    private void drawQuestStatsDashboard(Canvas canvas) {
        resetQuestActionRects();
        float x = questMainCard.left + 18f;
        float y = questMainCard.top + 32f;
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(16f, getHeight() * 0.026f));
        canvas.drawText("İSTATİSTİK PANELİ", x, y, paint);
        paint.setTextSize(Math.max(9.5f, getHeight() * 0.0165f));
        paint.setColor(Color.argb(220, 235, 245, 255));
        canvas.drawText("Sürüş, ekonomi, ilerleme ve mod verileri profesyonel kart düzeninde gösterilir.", x, y + 20f, paint);

        float margin = Math.max(16f, Math.min(getWidth(), getHeight()) * 0.018f);
        float top = questMainCard.top + Math.max(54f, Math.min(getWidth(), getHeight()) * 0.070f);
        float left = questMainCard.left + margin;
        float right = questMainCard.right - margin;
        float bottom = questMainCard.bottom - margin;
        float gapX = Math.max(12f, Math.min(getWidth(), getHeight()) * 0.014f);
        float gapY = Math.max(12f, Math.min(getWidth(), getHeight()) * 0.014f);
        float cardW = (right - left - gapX) * 0.5f;
        float cardH = (bottom - top - gapY) * 0.5f;
        RectF c0 = new RectF(left, top, left + cardW, top + cardH);
        RectF c1 = new RectF(c0.right + gapX, top, c0.right + gapX + cardW, top + cardH);
        RectF c2 = new RectF(left, c0.bottom + gapY, left + cardW, c0.bottom + gapY + cardH);
        RectF c3 = new RectF(c2.right + gapX, c1.bottom + gapY, c2.right + gapX + cardW, c1.bottom + gapY + cardH);
        drawStatsCard(canvas, c0, "YARIŞ & SÜRÜŞ", new String[]{
                "Toplam yarış: " + saveManager.getCareerTotalRaces(),
                "Galibiyet / mağlubiyet: " + saveManager.getCareerTotalWins() + " / " + saveManager.getCareerTotalLosses(),
                "Sürüş mesafesi: " + saveManager.getDrivenMeters() + " m",
                "Yakın geçiş: " + saveManager.getTrafficNearMissTotal()});
        drawStatsCard(canvas, c1, "EKONOMİ", new String[]{
                "Mevcut coin: " + saveManager.getCoins(),
                "Toplam kazanılan: " + saveManager.getTotalEarnedCoins(),
                "Günlük ödül alındı: " + saveManager.getDailyTaskClaimedCount(DailyWeeklyTaskSystem.DAILY_COUNT),
                "Haftalık ödül alındı: " + saveManager.getWeeklyTaskClaimedCount(DailyWeeklyTaskSystem.WEEKLY_COUNT)});
        drawStatsCard(canvas, c2, "MOD PERFORMANSI", new String[]{
                "Drag en iyi: " + PlayerStatsSystem.formatSeconds(saveManager.getDragBestSeconds()),
                "Checkpoint en iyi: " + PlayerStatsSystem.formatSeconds(saveManager.getRaceBestSeconds()),
                "Drift en iyi: " + saveManager.getDriftBestScore() + "  Combo x" + saveManager.getDriftBestCombo(),
                "Polis kaçış / yakalanma: " + saveManager.getPoliceEscapes() + " / " + saveManager.getPoliceCaughtCount()});
        drawStatsCard(canvas, c3, "İLERLEME", new String[]{
                "Seviye: " + saveManager.getPlayerLevel(),
                "Toplam XP: " + saveManager.getCareerTotalXp(),
                "Araç: " + QuestPanelSystem.ownedVehicleCount(saveManager) + "/" + VehicleCatalog.count(),
                "Başarım alındı: " + saveManager.getAchievementRewardedCount(AchievementSystem.ACH_COUNT)});
    }

    private void drawStatsCard(Canvas canvas, RectF rect, String title, String[] lines) {
        drawGlassPanel(canvas, rect, Color.argb(104, 255, 255, 255), Color.argb(120, 255, 255, 255));
        float x = rect.left + 12f;
        float y = rect.top + 22f;
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(10.5f, rect.height() * 0.10f));
        canvas.drawText(title, x, y, paint);
        paint.setColor(Color.argb(220, 232, 242, 255));
        paint.setTextSize(Math.max(8.5f, rect.height() * 0.075f));
        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(trimPanelText(lines[i], 34), x, y + 24f + i * Math.max(18f, rect.height() * 0.16f), paint);
        }
    }

    private boolean handleQuestRewardButtonTap(float x, float y) {
        if (!(questPanelTab == QuestPanelSystem.TAB_DAILY || questPanelTab == QuestPanelSystem.TAB_WEEKLY || questPanelTab == QuestPanelSystem.TAB_REWARDS)) return false;
        int itemCount = questPanelTab == QuestPanelSystem.TAB_REWARDS ? AchievementSystem.ACH_COUNT : DailyWeeklyTaskSystem.DAILY_COUNT;
        for (int i = 0; i < itemCount && i < questRewardButtons.length; i++) {
            if (questRewardButtons[i].contains(x, y)) {
                int id = questRewardIds[i];
                boolean claimed;
                if (questPanelTab == QuestPanelSystem.TAB_DAILY) claimed = DailyWeeklyTaskSystem.claimDailyTask(saveManager, id);
                else if (questPanelTab == QuestPanelSystem.TAB_WEEKLY) claimed = DailyWeeklyTaskSystem.claimWeeklyTask(saveManager, id);
                else claimed = AchievementSystem.claimAchievement(saveManager, id);
                if (claimed) {
                    if (audioManager != null) audioManager.playReward();
                } else {
                    if (saveManager != null) saveManager.setEconomyLastMessage("Bu kart için alınacak hazır ödül yok");
                    if (audioManager != null) audioManager.playLocked();
                }
                invalidate();
                return true;
            }
        }
        return false;
    }

    private boolean handleQuestClaimAllTap() {
        if (questClaimAllButton.width() <= 0f || questClaimAllButton.height() <= 0f) return false;
        int claimed;
        if (questPanelTab == QuestPanelSystem.TAB_DAILY) claimed = DailyWeeklyTaskSystem.claimAllCompletedDailyTasks(saveManager);
        else if (questPanelTab == QuestPanelSystem.TAB_WEEKLY) claimed = DailyWeeklyTaskSystem.claimAllCompletedWeeklyTasks(saveManager);
        else if (questPanelTab == QuestPanelSystem.TAB_REWARDS) claimed = AchievementSystem.claimAllCompletedAchievements(saveManager);
        else return false;
        if (claimed > 0) {
            if (audioManager != null) audioManager.playReward();
        } else {
            if (saveManager != null) saveManager.setEconomyLastMessage("Bu sekmede alınacak hazır ödül yok");
            if (audioManager != null) audioManager.playLocked();
        }
        invalidate();
        return true;
    }

    private String trimPanelText(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private void drawSettings(Canvas canvas) {
        drawTitle(canvas, "AYARLAR", "Ses / HUD / kontrol / görev bildirimleri / tablet uyumu");
        drawButton(canvas, backButton, "GERI", false, false);
        drawButton(canvas, settingsGraphics, "GRAFİK: OTOMATİK", false, false);
        drawButton(canvas, settingsControls, "KONTROL: " + saveManager.getControlSensitivityLabel(), false, false);
        drawButton(canvas, settingsSound, audioManager != null && audioManager.isEnabled() ? "SES: AÇIK" : "SES: KAPALI", false, false);
        drawButton(canvas, settingsHud, saveManager.isHudCompactEnabled() ? "HUD: SADE" : "HUD: GELİŞMİŞ", false, false);
        drawButton(canvas, settingsVibration, saveManager.isVibrationEnabled() ? "TİTREŞİM: AÇIK" : "TİTREŞİM: KAPALI", false, false);
        drawButton(canvas, settingsSensitivity, "HASSASİYET: " + saveManager.getControlSensitivityLabel(), false, false);
        drawButton(canvas, settingsLayoutPreset, "DÜZEN: " + saveManager.getControlLayoutLabel(), false, false);
        drawButton(canvas, settingsPedalSize, "PEDAL: " + saveManager.getPedalSizeLabel(), false, false);
        drawButton(canvas, settingsHudPreset, "HUD PRESET: " + saveManager.getHudPresetLabel(), false, false);
        drawButton(canvas, settingsOpacity, "BUTON OPAKLIĞI: %" + saveManager.getButtonOpacityPercent(), false, false);
        drawButton(canvas, settingsLeftHanded, saveManager.isLeftHandedModeEnabled() ? "SOLAK MOD: AÇIK" : "SOLAK MOD: KAPALI", false, false);
        drawButton(canvas, settingsAutoControl, saveManager.isAutoControlByModeEnabled() ? "MODA GÖRE OTO: AÇIK" : "MODA GÖRE OTO: KAPALI", false, false);
        drawButton(canvas, settingsTaskHud, saveManager.isTaskAchievementHudEnabled() ? "GÖREV HUD: AÇIK" : "GÖREV HUD: KAPALI", true, false);
        drawButton(canvas, settingsTaskNotification, "GÖREV BİLDİRİM: " + saveManager.getTaskAchievementNotificationModeLabel(), false, false);
        drawButton(canvas, settingsRewardPopup, saveManager.isRewardPopupEnabled() ? "ÖDÜL POPUP: AÇIK" : "ÖDÜL POPUP: KAPALI", false, false);

        float min = Math.min(getWidth(), getHeight());
        float x = getWidth() * 0.055f;
        float y = getHeight() * 0.205f;
        float w = getWidth() * 0.40f;
        float h = Math.max(245f, getHeight() * 0.41f);
        RectF info = new RectF(x, y, x + w, y + h);
        drawGlassPanel(canvas, info, Color.argb(128, 4, 12, 26), Color.argb(145, 0, 220, 255));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(15f, min * 0.026f));
        canvas.drawText("A63.3 mobil kontrol editörü", info.left + 18f, info.top + h * 0.16f, paint);
        paint.setColor(Color.argb(222, 235, 245, 255));
        paint.setTextSize(Math.max(10.5f, min * 0.0175f));
        canvas.drawText("• Ses butonu mevcut audio sistemine bağlı.", info.left + 18f, info.top + h * 0.33f, paint);
        canvas.drawText("• HUD sade/gelişmiş tercihi kaydedilir.", info.left + 18f, info.top + h * 0.47f, paint);
        canvas.drawText("• Hassasiyet: Yumuşak/Dengeli/Sport/Drift/Drag.", info.left + 18f, info.top + h * 0.61f, paint);
        canvas.drawText("• Düzen, pedal, opaklık, solak mod kalıcı kaydedilir.", info.left + 18f, info.top + h * 0.75f, paint);
        paint.setColor(Color.argb(228, 255, 220, 90));
        canvas.drawText(UiBalanceSystem.balanceHint(saveManager) + " | Repair " + saveManager.getSaveRepairVersion(), info.left + 18f, info.top + h * 0.90f, paint);

        float panelX = settingsTaskHud.left - Math.max(14f, min * 0.018f);
        float panelY = settingsTaskHud.top - Math.max(44f, min * 0.060f);
        RectF taskPanel = new RectF(panelX, panelY, getWidth() - Math.max(18f, min * 0.035f), settingsRewardPopup.bottom + Math.max(64f, min * 0.100f));
        drawGlassPanel(canvas, taskPanel, Color.argb(126, 4, 12, 26), Color.argb(150, 255, 220, 70));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(Math.max(15f, min * 0.025f));
        canvas.drawText("Görev/Başarım Final Ayarları", taskPanel.left + 18f, taskPanel.top + 28f, paint);
        paint.setColor(Color.argb(222, 235, 245, 255));
        paint.setTextSize(Math.max(9.5f, min * 0.016f));
        canvas.drawText("• HUD kapalıyken sürüş ekranında görev kartı çizilmez.", taskPanel.left + 18f, settingsRewardPopup.bottom + Math.max(28f, min * 0.042f), paint);
        canvas.drawText("• Bildirim modu sadece bilgilendirir; ödülü otomatik vermez.", taskPanel.left + 18f, settingsRewardPopup.bottom + Math.max(50f, min * 0.074f), paint);
        drawButton(canvas, settingsTaskHud, saveManager.isTaskAchievementHudEnabled() ? "GÖREV HUD: AÇIK" : "GÖREV HUD: KAPALI", true, false);
        drawButton(canvas, settingsTaskNotification, "GÖREV BİLDİRİM: " + saveManager.getTaskAchievementNotificationModeLabel(), false, false);
        drawButton(canvas, settingsRewardPopup, saveManager.isRewardPopupEnabled() ? "ÖDÜL POPUP: AÇIK" : "ÖDÜL POPUP: KAPALI", false, false);
    }

    private void drawTitle(Canvas canvas, String title, String subtitle) {
        paint.setTextAlign(Paint.Align.LEFT);
        float titleX = Math.max(getWidth() * 0.055f, backButton.right + Math.max(12f, Math.min(getWidth(), getHeight()) * 0.018f));
        paint.setColor(Color.WHITE);
        // A64.3: Dev başlıklar garaj/modifiye ekranını kapatıyordu; tablet/telefon için üst sınır kondu.
        paint.setTextSize(Math.max(22f, Math.min(36f, getHeight() * 0.042f)));
        canvas.drawText(trimMenuText(title, 34), titleX, getHeight() * 0.083f, paint);

        paint.setColor(Color.argb(210, 0, 210, 255));
        paint.setTextSize(Math.max(11f, Math.min(17f, getHeight() * 0.019f)));
        canvas.drawText(trimMenuText(subtitle, 62), titleX, getHeight() * 0.123f, paint);
    }

    private String trimMenuText(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private void drawButton(Canvas canvas, RectF r, String text, boolean primary, boolean disabled) {
        paint.setStyle(Paint.Style.FILL);
        int bg;
        if (disabled) bg = Color.argb(92, 90, 95, 105);
        else bg = primary ? Color.argb(180, 0, 170, 255) : Color.argb(122, 255, 255, 255);
        paint.setColor(bg);
        canvas.drawRoundRect(r, 22f, 22f, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.4f);
        paint.setColor(primary ? Color.argb(190, 120, 230, 255) : Color.argb(150, 255, 255, 255));
        canvas.drawRoundRect(r, 22f, 22f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(disabled ? Color.argb(180, 210, 210, 210) : Color.WHITE);
        paint.setTextSize(Math.max(10.5f, Math.min(18f, r.height() * 0.28f)));
        Paint.FontMetrics fm = paint.getFontMetrics();
        canvas.drawText(text, r.centerX(), r.centerY() - (fm.ascent + fm.descent) * 0.5f, paint);
    }

    private void drawDots(Canvas canvas, int selected) {
        int count = Math.max(1, GameScreenState.vehicleCount());
        float min = Math.min(getWidth(), getHeight());
        float dot = Math.max(10f, Math.min(15f, min * 0.020f));
        float gap = dot * 1.15f;
        float centerX = getWidth() * 0.50f;
        float y = getHeight() - Math.max(24f, min * 0.043f);

        // A64.4: 18 dot'u yan yana basmak küçük ekranda taşıyordu. Seçili araç
        // çevresinde kompakt 5 nokta + sayısal gösterge kullanılır.
        for (int slot = -2; slot <= 2; slot++) {
            int idx = selected + slot;
            while (idx < 0) idx += count;
            while (idx >= count) idx -= count;
            float cx = centerX + slot * (dot + gap);
            RectF r = new RectF(cx - dot * 0.5f, y, cx + dot * 0.5f, y + dot);
            drawDot(canvas, r, slot == 0, !isOwned(idx));
        }

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.argb(222, 235, 245, 255));
        paint.setTextSize(Math.max(10f, Math.min(14f, getHeight() * 0.016f)));
        canvas.drawText((selected + 1) + " / " + count, centerX, y - dot * 0.70f, paint);
    }

    private void drawDot(Canvas canvas, RectF r, boolean selected, boolean locked) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(selected ? Color.argb(255, 0, 210, 255) : Color.argb(130, 255, 255, 255));
        canvas.drawOval(r, paint);
        if (locked) {
            paint.setColor(Color.argb(220, 0, 0, 0));
            paint.setStrokeWidth(2f);
            canvas.drawLine(r.left, r.top, r.right, r.bottom, paint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int screen = state.getScreen();
        if (screen == GameScreenState.SCREEN_DRIVE) return false;

        float x = event.getX();
        float y = event.getY();
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN) {
            downX = x;
            downY = y;
            lastX = x;
            draggingPreview = isPreviewArea(x, y);
            return true;
        }

        if (action == MotionEvent.ACTION_MOVE) {
            if (draggingPreview && !isGarageCarouselSwipeStart()) {
                float dx = x - lastX;
                state.addPreviewYaw(dx * 0.0125f);
                lastX = x;
                invalidate();
            }
            return true;
        }

        if (action == MotionEvent.ACTION_UP) {
            float dxTotal = x - downX;
            float dyTotal = y - downY;
            float dist = Math.abs(dxTotal) + Math.abs(dyTotal);

            if (state.getScreen() == GameScreenState.SCREEN_GARAGE
                    && state.getGarageMode() == GameScreenState.GARAGE_MODE_SELECT
                    && isGarageCarouselSwipeStart()
                    && GarageCarouselSystem.isHorizontalSwipe(dxTotal, dyTotal, getWidth(), getHeight())) {
                // A66.9: Alt carousel alanı her zaman araç değiştirir; uzun swipe iki kart atlayabilir.
                changeVehicle(GarageCarouselSystem.swipeDelta(dxTotal, garageCarouselSwipeZone));
                draggingPreview = false;
                invalidate();
                return true;
            } else if (state.getScreen() == GameScreenState.SCREEN_GARAGE
                    && state.getGarageMode() == GameScreenState.GARAGE_MODE_VISUAL_EDIT
                    && downY > getHeight() * 0.68f
                    && Math.abs(dxTotal) > Math.max(22f, getWidth() * 0.032f)
                    && Math.abs(dxTotal) > Math.abs(dyTotal) * 1.12f) {
                // Tek parça düzenlemede sağ/sol kaydırarak değer seçme.
                cycleSelectedVisualValue(dxTotal < 0f ? 1 : -1);
            } else if (dist < 24f) {
                handleClick(x, y);
            }

            draggingPreview = false;
            invalidate();
            return true;
        }

        if (action == MotionEvent.ACTION_CANCEL) {
            draggingPreview = false;
            return true;
        }

        return true;
    }

    private boolean isGarageCarouselSwipeStart() {
        return state != null
                && state.getScreen() == GameScreenState.SCREEN_GARAGE
                && state.getGarageMode() == GameScreenState.GARAGE_MODE_SELECT
                && garageCarouselSwipeZone.contains(downX, downY);
    }

    private boolean isPreviewArea(float x, float y) {
        int screen = state.getScreen();
        if (screen == GameScreenState.SCREEN_GARAGE) {
            if (state.getGarageMode() == GameScreenState.GARAGE_MODE_VISUAL_EDIT) {
                return x > getWidth() * 0.20f && x < getWidth() * 0.94f && y > getHeight() * 0.16f && y < getHeight() * 0.60f;
            }
            if (state.getGarageMode() == GameScreenState.GARAGE_MODE_SELECT && garageCarouselSwipeZone.contains(x, y)) {
                return false;
            }
            return x > getWidth() * 0.26f && x < getWidth() * 0.94f && y > getHeight() * 0.17f && y < getHeight() * 0.66f;
        }
        if (screen == GameScreenState.SCREEN_MAIN_MENU) {
            return x > mainMenuLayout.getLeftNavPanel().right && x < mainMenuLayout.getRightProfilePanel().left && y > getHeight() * 0.14f;
        }
        if (screen == GameScreenState.SCREEN_CAREER_START) {
            return x < getWidth() * 0.70f && y > getHeight() * 0.14f;
        }
        return false;
    }

    private boolean handleShowroomControlClick(float x, float y) {
        if (showroomRotateLeftButton.contains(x, y)) {
            state.addPreviewYaw(-0.35f);
            return true;
        }
        if (showroomRotateRightButton.contains(x, y)) {
            state.addPreviewYaw(0.35f);
            return true;
        }
        if (showroomZoomOutButton.contains(x, y)) {
            state.addPreviewZoom(0.10f);
            return true;
        }
        if (showroomZoomInButton.contains(x, y)) {
            state.addPreviewZoom(-0.10f);
            return true;
        }
        if (showroomResetButton.contains(x, y)) {
            state.resetPreviewCamera();
            return true;
        }
        return false;
    }

    private void handleClick(float x, float y) {
        if (audioManager != null) audioManager.playMenuClick();
        int screen = state.getScreen();

        if (handleDailyRewardClick(x, y)) {
            return;
        }

        if (screen == GameScreenState.SCREEN_CAREER_START) {
            if (careerStarter0.contains(x, y)) selectCareerStarter(0);
            else if (careerStarter1.contains(x, y)) selectCareerStarter(1);
            else if (careerStarter2.contains(x, y)) selectCareerStarter(2);
            else if (careerStartButton.contains(x, y)) completeCareerStart();
            return;
        }

        if (screen == GameScreenState.SCREEN_MAIN_MENU) {
            if (!saveManager.isCareerStarted()) {
                setScreen(GameScreenState.SCREEN_CAREER_START);
                return;
            }
            if (handleRedeemClick(x, y)) return;
            if (rightCars.contains(x, y)) setScreen(GameScreenState.SCREEN_GARAGE);
            else if (rightModes.contains(x, y)) setScreen(GameScreenState.SCREEN_MODES);
            else if (rightCareer.contains(x, y)) setScreen(GameScreenState.SCREEN_CAREER);
            else if (rightMaps.contains(x, y)) setScreen(GameScreenState.SCREEN_MAPS);
            else if (rightQuests.contains(x, y)) setScreen(GameScreenState.SCREEN_QUESTS);
            else if (rightSettings.contains(x, y)) setScreen(GameScreenState.SCREEN_SETTINGS);
            return;
        }

        if (backButton.contains(x, y)) {
            if (audioManager != null) audioManager.playBack();
            if (screen == GameScreenState.SCREEN_GARAGE && state.getGarageMode() != GameScreenState.GARAGE_MODE_SELECT) {
                int mode = state.getGarageMode();
                pendingPerformanceUpgradeType = -1;
                if (mode == GameScreenState.GARAGE_MODE_BUY_CONFIRM) {
                    state.cancelBuyConfirm();
                } else if (mode == GameScreenState.GARAGE_MODE_VISUAL_EDIT) {
                    if (saveManager != null && state.isVisualEditDirty()) {
                        int type = state.getSelectedVisualModType();
                        int saved = saveManager.getVisualModValue(VehicleCatalog.id(state.getSelectedVehicleIndex()), type);
                        saveManager.setEconomyLastMessage(VisualModificationSaveFlowSystem.backDiscardLine(type, saved));
                    }
                    state.setGarageMode(GameScreenState.GARAGE_MODE_VISUAL);
                } else if (mode == GameScreenState.GARAGE_MODE_MODIFY_HOME) {
                    state.setGarageMode(GameScreenState.GARAGE_MODE_SELECT);
                } else {
                    state.setGarageMode(GameScreenState.GARAGE_MODE_MODIFY_HOME);
                }
            } else if (state.consumeReturnToDriveAfterMenu()) {
                setScreen(GameScreenState.SCREEN_DRIVE);
            } else {
                setScreen(GameScreenState.SCREEN_MAIN_MENU);
            }
            invalidate();
            return;
        }

        if (screen == GameScreenState.SCREEN_GARAGE) {
            int selected = state.getSelectedVehicleIndex();
            String id = VehicleCatalog.id(selected);
            int mode = state.getGarageMode();
            if (handleShowroomControlClick(x, y)) {
                invalidate();
                return;
            }

            if (mode == GameScreenState.GARAGE_MODE_SELECT) {
                if (garagePrevVehicleButton.contains(x, y)) {
                    changeVehicle(-1);
                    invalidate();
                    return;
                } else if (garageNextVehicleButton.contains(x, y)) {
                    changeVehicle(1);
                    invalidate();
                    return;
                } else {
                    int cardIndex = hitVehicleCarouselCard(x, y);
                    if (cardIndex >= 0) {
                        previewVehicle(cardIndex, true);
                        invalidate();
                        return;
                    }
                    int dotIndex = hitVehicleDot(x, y);
                    if (dotIndex >= 0) {
                        previewVehicle(dotIndex, true);
                        invalidate();
                        return;
                    }
                }
                selected = state.getSelectedVehicleIndex();
                id = VehicleCatalog.id(selected);
                if (isOwned(selected) && modifyButton.contains(x, y)) {
                    state.setGarageMode(GameScreenState.GARAGE_MODE_MODIFY_HOME);
                } else if (isOwned(selected) && testDrivePrevButton.contains(x, y)) {
                    cycleTestDriveChallenge(-1);
                } else if (isOwned(selected) && testDriveNextButton.contains(x, y)) {
                    cycleTestDriveChallenge(1);
                } else if (isOwned(selected) && testDriveButton.contains(x, y)) {
                    state.beginGarageToTestDriveTransition(selected);
                    if (saveManager != null) {
                        saveManager.setSelectedMap(GameScreenState.MAP_OPEN_FIELD);
                        saveManager.setEconomyLastMessage("Showroom test parkuru hazırlanıyor: "
                                + TestDriveChallengeSystem.challengeLabel(saveManager.getTestDriveChallengeIndex()));
                    }
                    if (audioManager != null) audioManager.playReward();
                    postInvalidateOnAnimation();
                } else if (selectButton.contains(x, y) && isOwned(selected)) {
                    if (saveManager != null) {
                        saveManager.setSelectedVehicleIndex(selected);
                        saveManager.setEconomyLastMessage("Seçili araç: " + trimMenuText(GameScreenState.vehicleLabel(selected), 26));
                    }
                    state.markVehicleSelectedForComparison();
                    setScreen(GameScreenState.SCREEN_MAIN_MENU);
                } else if (buyButton.contains(x, y) && !isOwned(selected)) {
                    if (saveManager != null && !saveManager.isVehicleLevelUnlocked(selected)) {
                        saveManager.setEconomyLastMessage("Bu araç için LVL " + VehicleCatalog.requiredLevel(selected) + " gerekli");
                        if (audioManager != null) audioManager.playLocked();
                    } else {
                        state.beginBuyConfirm(selected);
                    }
                }
                invalidate();
                return;
            }

            if (mode == GameScreenState.GARAGE_MODE_BUY_CONFIRM) {
                if (buyCancelButton.contains(x, y)) {
                    state.cancelBuyConfirm();
                } else if (buyConfirmButton.contains(x, y)) {
                    int pending = state.getPendingBuyVehicleIndex();
                    if (pending < 0 || pending >= VehicleCatalog.count()) pending = selected;
                    String pendingId = VehicleCatalog.id(pending);
                    if (saveManager != null && saveManager.buyVehicle(pendingId, VehicleCatalog.price(pending))) {
                        saveManager.setEconomyLastMessage("SHOWROOM: " + trimMenuText(GameScreenState.vehicleLabel(pending), 24) + " satın alındı ve seçildi");
                        saveManager.setSelectedVehicleIndex(pending);
                        state.setSelectedVehicleIndex(pending);
                        state.markVehicleSelectedForComparison();
                        state.setGarageMode(GameScreenState.GARAGE_MODE_SELECT);
                        completeMenuQuestIfActive(5, "Yeni araç görevi tamamlandı", 300, 750, 0);
                        if (audioManager != null) audioManager.playReward();
                    } else if (audioManager != null) {
                        audioManager.playLocked();
                    }
                }
                invalidate();
                return;
            }

            if (!isOwned(selected)) {
                state.setGarageMode(GameScreenState.GARAGE_MODE_SELECT);
                invalidate();
                return;
            }

            if (mode == GameScreenState.GARAGE_MODE_MODIFY_HOME) {
                if (testDriveButton.contains(x, y)) {
                    beginGarageTestDriveFromCurrentMode(id);
                } else if (modifyCategoryButtons[MOD_CATEGORY_PERFORMANCE].contains(x, y)) {
                    state.setGarageMode(GameScreenState.GARAGE_MODE_PERFORMANCE);
                } else if (modifyCategoryButtons[MOD_CATEGORY_TUNING].contains(x, y)) {
                    state.setGarageMode(GameScreenState.GARAGE_MODE_TUNING);
                } else if (modifyCategoryButtons[MOD_CATEGORY_BODY].contains(x, y)) {
                    state.setSelectedVisualGroup(GameScreenState.VISUAL_GROUP_BODY);
                    state.setGarageMode(GameScreenState.GARAGE_MODE_VISUAL);
                } else if (modifyCategoryButtons[MOD_CATEGORY_WHEELS].contains(x, y)) {
                    state.setSelectedVisualGroup(GameScreenState.VISUAL_GROUP_WHEELS);
                    state.setGarageMode(GameScreenState.GARAGE_MODE_VISUAL);
                } else if (modifyCategoryButtons[MOD_CATEGORY_GLASS].contains(x, y)) {
                    state.setSelectedVisualGroup(GameScreenState.VISUAL_GROUP_GLASS);
                    state.setGarageMode(GameScreenState.GARAGE_MODE_VISUAL);
                } else if (modifyCategoryButtons[MOD_CATEGORY_LIGHTS].contains(x, y)) {
                    state.setSelectedVisualGroup(GameScreenState.VISUAL_GROUP_LIGHTS);
                    state.setGarageMode(GameScreenState.GARAGE_MODE_VISUAL);
                } else if (modifyCategoryButtons[MOD_CATEGORY_PLATE].contains(x, y)) {
                    state.setSelectedVisualGroup(GameScreenState.VISUAL_GROUP_PLATE);
                    state.setGarageMode(GameScreenState.GARAGE_MODE_VISUAL);
                } else if (modifyCategoryButtons[MOD_CATEGORY_REPAIR].contains(x, y)) {
                    if (saveManager != null && saveManager.getRepairCost(id) > 0) saveManager.repairVehicle(id);
                }
                invalidate();
                return;
            }

            if (mode == GameScreenState.GARAGE_MODE_PERFORMANCE) {
                if (pendingPerformanceUpgradeType >= 0 && performanceCancelButton.contains(x, y)) {
                    pendingPerformanceUpgradeType = -1;
                    if (audioManager != null) audioManager.playBack();
                } else if (pendingPerformanceUpgradeType >= 0 && performanceConfirmButton.contains(x, y)) {
                    confirmPendingPerformanceUpgrade(id);
                } else if (testDriveButton.contains(x, y)) {
                    beginGarageTestDriveFromCurrentMode(id);
                } else {
                    tryUpgradePerformance(id, x, y);
                }
                invalidate();
                return;
            }

            if (mode == GameScreenState.GARAGE_MODE_TUNING) {
                if (testDriveButton.contains(x, y)) {
                    beginGarageTestDriveFromCurrentMode(id);
                } else {
                    tryDetailedTuning(id, x, y);
                }
                invalidate();
                return;
            }

            if (mode == GameScreenState.GARAGE_MODE_VISUAL) {
                int type = hitVisualCustomization(x, y);
                if (type >= 0) {
                    state.beginVisualEdit(type, saveManager.getVisualModValue(id, type));
                }
                invalidate();
                return;
            }

            if (mode == GameScreenState.GARAGE_MODE_VISUAL_EDIT) {
                int type = state.getSelectedVisualModType();
                if (visualSaveButton.contains(x, y)) {
                    if (tryCommitVisualEdit(id, type)) {
                        state.finishVisualEdit();
                        state.setGarageMode(GameScreenState.GARAGE_MODE_VISUAL);
                        if (audioManager != null) audioManager.playReward();
                    } else if (audioManager != null) {
                        audioManager.playLocked();
                    }
                    invalidate();
                    return;
                }
                if (visualResetButton.contains(x, y)) {
                    state.setVisualEditPreviewValue(0);
                    if (saveManager != null) saveManager.setEconomyLastMessage(VisualModificationSaveFlowSystem.resetPreviewLine(type));
                    if (audioManager != null) audioManager.playMenuClick();
                    invalidate();
                    return;
                }
                if (visualUndoButton.contains(x, y)) {
                    resetVisualEditToSaved(id, type);
                    invalidate();
                    return;
                }
                if (testDriveButton.contains(x, y)) {
                    if (state.isVisualEditDirty()) {
                        if (tryCommitVisualEdit(id, type)) {
                            state.finishVisualEdit();
                            if (saveManager != null) saveManager.setEconomyLastMessage("GÖRSEL: Kaydedildi, test sürüşü hazırlanıyor");
                            beginGarageTestDriveFromCurrentMode(id);
                        } else if (audioManager != null) {
                            audioManager.playLocked();
                        }
                    } else {
                        beginGarageTestDriveFromCurrentMode(id);
                    }
                    invalidate();
                    return;
                }
                if (trySetVisualValue(id, x, y)) {
                    invalidate();
                    return;
                }
                invalidate();
                return;
            }
        }

        if (screen == GameScreenState.SCREEN_MODES) {
            if (state.getSelectedMode() == GameScreenState.MODE_RACE_LOCKED) {
                for (int i = 0; i < checkpointRouteButtons.length; i++) {
                    if (checkpointRouteButtons[i].contains(x, y)) {
                        state.setSelectedCheckpointRoute(i);
                        saveManager.setSelectedCheckpointRoute(i);
                        CheckpointRaceSystem.setActiveRoute(i);
                        setSelectedMapPersisted(GameScreenState.MAP_OPEN_FIELD);
                        if (audioManager != null) audioManager.playSelect();
                        invalidate();
                        return;
                    }
                }
            }
            if (modeFree.contains(x, y)) trySelectCareerMode(GameScreenState.MODE_FREE_DRIVE);
            else if (modeTime.contains(x, y)) trySelectCareerMode(GameScreenState.MODE_TIME_TRIAL);
            else if (modeDrift.contains(x, y)) trySelectCareerMode(GameScreenState.MODE_DRIFT);
            else if (modeRace.contains(x, y)) {
                trySelectCareerMode(GameScreenState.MODE_RACE_LOCKED);
                if (state.getSelectedMode() == GameScreenState.MODE_RACE_LOCKED) {
                    state.setSelectedCheckpointRoute(saveManager.getSelectedCheckpointRoute());
                    CheckpointRaceSystem.setActiveRoute(state.getSelectedCheckpointRoute());
                    setSelectedMapPersisted(GameScreenState.MAP_OPEN_FIELD);
                }
            }
            else if (modeDragRace.contains(x, y)) trySelectCareerMode(GameScreenState.MODE_DRAG_RACE);
            else if (modePolice.contains(x, y)) trySelectCareerMode(GameScreenState.MODE_POLICE_CHASE);
            else if (modeGarageButton.contains(x, y)) {
                state.setGarageMode(GameScreenState.GARAGE_MODE_SELECT);
                setScreen(GameScreenState.SCREEN_GARAGE);
            }
            else if (modeModifyButton.contains(x, y)) {
                state.setGarageMode(GameScreenState.GARAGE_MODE_MODIFY_HOME);
                setScreen(GameScreenState.SCREEN_GARAGE);
            }
            else if (startButton.contains(x, y)) {
                if (!CareerLeagueSystem.isModeUnlocked(saveManager, state.getSelectedMode())) {
                    saveManager.setEconomyLastMessage("Bu yarış için " + CareerLeagueSystem.modeLockText(state.getSelectedMode()) + " gerekli");
                    if (audioManager != null) audioManager.playLocked();
                } else if (isOwned(state.getSelectedVehicleIndex())) {
                    if (state.getSelectedMode() == GameScreenState.MODE_RACE_LOCKED
                            || state.getSelectedMode() == GameScreenState.MODE_DRIFT
                            || state.getSelectedMode() == GameScreenState.MODE_POLICE_CHASE
                            || state.getSelectedMap() == GameScreenState.MAP_OPEN_WORLD
                            || state.getSelectedMap() == GameScreenState.MAP_SECOND_NEW) {
                        setSelectedMapPersisted(GameScreenState.MAP_OPEN_FIELD);
                    }
                    setScreen(GameScreenState.SCREEN_DRIVE);
                } else {
                    setScreen(GameScreenState.SCREEN_GARAGE);
                }
            }
            return;
        }

        if (screen == GameScreenState.SCREEN_MAPS) {
            if (mapOpen.contains(x, y)) trySelectMap(GameScreenState.MAP_OPEN_FIELD);
            else if (mapCity.contains(x, y)) trySelectMap(GameScreenState.MAP_CITY);
            else if (mapHighway.contains(x, y)) trySelectMap(GameScreenState.MAP_HIGHWAY);
            else if (mapDrift.contains(x, y)) trySelectMap(GameScreenState.MAP_DRIFT_PARK);
            else if (mapSelectButton.contains(x, y)) setScreen(GameScreenState.SCREEN_MAIN_MENU);
            return;
        }

        if (screen == GameScreenState.SCREEN_QUESTS) {
            int tab = hitQuestTab(x, y);
            if (tab >= 0) {
                questPanelTab = tab;
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
                return;
            }
            if (questClaimAllButton.contains(x, y)) {
                handleQuestClaimAllTap();
                return;
            }
            if (handleQuestRewardButtonTap(x, y)) {
                return;
            }
            if (questMainCard.contains(x, y) && (questPanelTab == QuestPanelSystem.TAB_DAILY
                    || questPanelTab == QuestPanelSystem.TAB_WEEKLY
                    || questPanelTab == QuestPanelSystem.TAB_REWARDS)) {
                if (saveManager != null) saveManager.setEconomyLastMessage("Kartlardaki ÖDÜL AL veya sağdaki HEPSİNİ AL butonunu kullan");
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
                return;
            }
            return;
        }

        if (screen == GameScreenState.SCREEN_CAREER) {
            int league = CareerEventSystem.recommendedLeague(saveManager);
            if (careerLeagueRewardButton.contains(x, y)) {
                if (CareerEventSystem.isLeagueCompleted(saveManager, league)
                        && saveManager != null
                        && saveManager.claimCareerLeagueReward(league,
                        CareerEventSystem.leagueRewardCoins(league),
                        CareerEventSystem.leagueRewardXp(league),
                        CareerEventSystem.leagueTitle(league))) {
                    if (audioManager != null) audioManager.playReward();
                } else {
                    if (saveManager != null) saveManager.setEconomyLastMessage("Lig ödülü için tüm etkinlikleri tamamla");
                    if (audioManager != null) audioManager.playLocked();
                }
                invalidate();
                return;
            }
            for (int i = 0; i < careerEventRewardButtons.length; i++) {
                if (careerEventRewardButtons[i].contains(x, y)) {
                    if (CareerEventSystem.isEventCompleted(saveManager, league, i)
                            && saveManager != null
                            && saveManager.claimCareerEventReward(league, i,
                            CareerEventSystem.eventRewardCoins(league, i),
                            CareerEventSystem.eventRewardXp(league, i),
                            CareerEventSystem.eventTitle(league, i))) {
                        if (audioManager != null) audioManager.playReward();
                    } else {
                        startCareerEvent(league, i);
                    }
                    invalidate();
                    return;
                }
            }
            for (int i = 0; i < careerEventButtons.length; i++) {
                if (careerEventButtons[i].contains(x, y)) {
                    startCareerEvent(league, i);
                    invalidate();
                    return;
                }
            }
            return;
        }

        if (screen == GameScreenState.SCREEN_SETTINGS) {
            if (settingsControls.contains(x, y)) {
                if (saveManager != null) saveManager.cycleControlSensitivityPreset();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsSensitivity.contains(x, y)) {
                if (saveManager != null) saveManager.cycleControlSensitivityPreset();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsLayoutPreset.contains(x, y)) {
                if (saveManager != null) saveManager.cycleControlLayoutPreset();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsPedalSize.contains(x, y)) {
                if (saveManager != null) saveManager.cyclePedalSizePreset();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsHudPreset.contains(x, y)) {
                if (saveManager != null) saveManager.cycleHudPreset();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsOpacity.contains(x, y)) {
                if (saveManager != null) saveManager.cycleButtonOpacityPercent();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsLeftHanded.contains(x, y)) {
                if (saveManager != null) saveManager.toggleLeftHandedMode();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsAutoControl.contains(x, y)) {
                if (saveManager != null) saveManager.toggleAutoControlByMode();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsTaskHud.contains(x, y)) {
                if (saveManager != null) saveManager.toggleTaskAchievementHudEnabled();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsTaskNotification.contains(x, y)) {
                if (saveManager != null) saveManager.cycleTaskAchievementNotificationMode();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsRewardPopup.contains(x, y)) {
                if (saveManager != null) saveManager.toggleRewardPopupEnabled();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsHud.contains(x, y)) {
                if (saveManager != null) saveManager.toggleHudCompact();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsVibration.contains(x, y)) {
                if (saveManager != null) saveManager.toggleVibrationEnabled();
                if (audioManager != null) audioManager.playMenuClick();
                invalidate();
            } else if (settingsSound.contains(x, y) && audioManager != null) {
                audioManager.setEnabled(!audioManager.isEnabled());
                audioManager.playMenuClick();
                invalidate();
            }
            return;
        }
    }

    private int starterIndexForSlot(int slot) {
        if (slot == 1) return 2; // Jeep
        if (slot == 2) return 7; // Countach
        return 0; // Aston
    }

    private void selectCareerStarter(int slot) {
        careerStarterIndex = starterIndexForSlot(slot);
        state.setSelectedVehicleIndex(careerStarterIndex);
        if (audioManager != null) audioManager.playSelect();
        invalidate();
    }

    private void completeCareerStart() {
        if (saveManager != null) {
            saveManager.completeCareerStart(careerStarterIndex);
            CareerSyncSystem.validate(saveManager);
        }
        state.setSelectedVehicleIndex(careerStarterIndex);
        state.setSelectedMap(GameScreenState.MAP_OPEN_FIELD);
        if (audioManager != null) audioManager.playCareerStart();
        setScreen(GameScreenState.SCREEN_MAIN_MENU);
        invalidate();
    }

    private void launchOpenWorldFromModes() {
        // A61_6: Açık Dünya GLB haritası kullanıcı isteğiyle kaldırıldı.
        setSelectedMapPersisted(GameScreenState.MAP_OPEN_FIELD);
        if (saveManager != null) {
            saveManager.setEconomyLastMessage("Açık Dünya haritası kaldırıldı; Açık Test Alanı kullanılacak.");
        }
        if (audioManager != null) audioManager.playLocked();
        invalidate();
    }

    private void trySelectCareerMode(int mode) {
        if (!CareerLeagueSystem.isModeUnlocked(saveManager, mode)) {
            if (saveManager != null) {
                saveManager.setEconomyLastMessage("Bu yarış için "
                        + CareerLeagueSystem.modeLockText(mode) + " gerekli");
            }
            if (audioManager != null) audioManager.playLocked();
            invalidate();
            return;
        }

        state.setSelectedMode(mode);
        if (audioManager != null) audioManager.playSelect();
        invalidate();
    }

    private void trySelectMap(int map) {
        if (map == GameScreenState.MAP_OPEN_WORLD || map == GameScreenState.MAP_SECOND_NEW) {
            setSelectedMapPersisted(GameScreenState.MAP_OPEN_FIELD);
            if (saveManager != null) {
                saveManager.setEconomyLastMessage("Bu harita slotu geçici kapalı; Açık Test Alanı seçildi.");
            }
            if (audioManager != null) audioManager.playLocked();
            invalidate();
            return;
        }
        if (MapRegistry.isPendingExternalAsset(map)) {
            if (saveManager != null) {
                saveManager.setEconomyLastMessage(MapRegistry.displayName(map)
                        + " dosyası bekleniyor. Gerçek GLB eklenince aktif edilecek.");
            }
            if (audioManager != null) audioManager.playLocked();
            invalidate();
            return;
        }
        if (saveManager != null && !saveManager.isMapUnlockedByCareer(map)) {
            if (audioManager != null) audioManager.playLocked();
            return;
        }
        setSelectedMapPersisted(map);
        if (map == GameScreenState.MAP_CITY || map == GameScreenState.MAP_HIGHWAY || map == GameScreenState.MAP_DRIFT_PARK) {
            completeMenuQuestIfActive(6, "Yeni bölge görevi tamamlandı", 400, 1000, 1);
        }
    }

    private void completeMenuQuestIfActive(int questIndex, String message, int xp, int coins, int crates) {
        if (saveManager == null) return;
        if (saveManager.getQuestChainStep() != questIndex) return;

        saveManager.addXp(xp);
        saveManager.addCoins(coins);
        if (crates > 0) {
            saveManager.setRewardCrates(saveManager.getRewardCrates() + crates);
        }

        String unlock = "";
        if (questIndex == 4) {
            int before = saveManager.getUnlockedPartTier();
            int after = saveManager.unlockNextPartTier();
            if (after > before) unlock = " | Yeni parça tier " + after;
        } else if (questIndex == 5) {
            for (int i = 1; i < VehicleCatalog.count(); i++) {
                String id = VehicleCatalog.id(i);
                if (!saveManager.isVehicleOwned(id) && !saveManager.isVehicleRewardUnlocked(id)) {
                    saveManager.setVehicleRewardUnlocked(id, true);
                    unlock = " | Yeni araç açıldı: " + VehicleCatalog.label(i);
                    break;
                }
            }
        } else if (questIndex == 6) {
            int beforePaint = saveManager.getUnlockedPaintCount();
            int afterPaint = saveManager.unlockNextPaintPreset();
            int beforeRim = saveManager.getUnlockedRimCount();
            int afterRim = saveManager.unlockNextRimPreset();
            unlock = " | Bölge ödülü";
            if (afterPaint > beforePaint) unlock += " | Renk " + afterPaint + "/6";
            if (afterRim > beforeRim) unlock += " | Jant " + afterRim + "/5";
        }

        saveManager.incrementProgressDaily();
        saveManager.incrementProgressWeekly();
        saveManager.setQuestChainStep(Math.min(7, questIndex + 1));
        saveManager.setQuestChainCompletedCount(Math.max(saveManager.getQuestChainCompletedCount(), questIndex + 1));
        saveManager.setQuestChainLastMessage((message == null ? "" : message) + unlock);
        saveManager.setEconomyLastMessage("ÖDÜL: +" + coins + " coin +" + xp + " XP" + (crates > 0 ? " +" + crates + " kasa" : "") + unlock);
        if (audioManager != null) audioManager.playReward();
    }

    private int hitVehicleCarouselCard(float x, float y) {
        if (state == null || state.getGarageMode() != GameScreenState.GARAGE_MODE_SELECT) return -1;
        int selected = state.getSelectedVehicleIndex();
        for (int i = 0; i < garageVehicleCardButtons.length; i++) {
            if (garageVehicleCardButtons[i].contains(x, y)) {
                return GarageCarouselSystem.indexForSlot(selected, i - 2);
            }
        }
        return -1;
    }

    private int hitVehicleDot(float x, float y) {
        int count = Math.max(1, GameScreenState.vehicleCount());
        float min = Math.min(getWidth(), getHeight());
        float dot = Math.max(10f, Math.min(15f, min * 0.020f));
        float gap = dot * 1.15f;
        float centerX = getWidth() * 0.54f;
        float top = getHeight() - Math.max(30f, min * 0.050f) - dot * 0.8f;
        float bottom = top + dot * 2.7f;
        if (y < top || y > bottom) return -1;
        int selected = state == null ? 0 : state.getSelectedVehicleIndex();
        for (int slot = -2; slot <= 2; slot++) {
            float cx = centerX + slot * (dot + gap);
            if (x >= cx - dot * 1.05f && x <= cx + dot * 1.05f) {
                return GarageCarouselSystem.indexForSlot(selected, slot);
            }
        }
        return -1;
    }

    private boolean isOwned(int index) {
        if (VehicleCatalog.price(index) <= 0) return true;
        return saveManager != null && saveManager.isVehicleOwned(VehicleCatalog.id(index));
    }

    private String coinsText() {
        return saveManager == null ? "0" : String.valueOf(saveManager.getCoins());
    }

    private static String formatOne(float value) {
        int whole = (int) value;
        int decimal = (int) ((value - whole) * 10f);
        if (decimal < 0) decimal = -decimal;
        return whole + "." + decimal;
    }


    private static String raceTimeText(float seconds) {
        if (seconds <= 0f) return "-";
        int total = Math.max(0, (int) seconds);
        int minutes = total / 60;
        int sec = total % 60;
        int hundred = Math.max(0, Math.min(99, (int) ((seconds - total) * 100f)));
        return String.format(java.util.Locale.US, "%02d:%02d.%02d", minutes, sec, hundred);
    }

    private static String percent(float value) {
        int p = Math.max(0, Math.min(100, Math.round(value * 100f)));
        return p + "%";
    }

    private static int damageColor(float health) {
        if (health > 0.72f) return Color.argb(235, 90, 255, 150);
        if (health > 0.38f) return Color.argb(240, 255, 210, 70);
        return Color.argb(245, 255, 80, 70);
    }


    private void setSelectedMapPersisted(int map) {
        state.setSelectedMap(map);
        if (saveManager != null) saveManager.setSelectedMap(map);
    }

    private void previewVehicle(int index, boolean cardTap) {
        int next = GarageCarouselSystem.normalizeIndex(index);
        if (state == null) return;
        state.setSelectedVehicleIndex(next);
        state.resetPreviewCamera();
        if (saveManager != null) {
            saveManager.setEconomyLastMessage(cardTap
                    ? GarageCarouselSystem.tapMessage(next)
                    : GarageCarouselSystem.changeMessage(next));
        }
        if (audioManager != null) audioManager.playMenuClick();
    }

    private void changeVehicle(int delta) {
        int base = state == null ? 0 : state.getSelectedVehicleIndex();
        previewVehicle(base + delta, false);
    }

    private void setScreen(int screen) {
        state.setScreen(screen);
        if (listener != null) listener.onScreenChanged(screen);
    }
}
