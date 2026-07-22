package com.arabaoyunu.core;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.arabaoyunu.audio.GameAudioManager;
import com.arabaoyunu.garage.GarageShowroomSystem;
import com.arabaoyunu.garage.ShowroomPresentationSystem;
import com.arabaoyunu.garage.DriveLoadoutSyncSystem;
import com.arabaoyunu.garage.PostTenUpdateStabilityQaSystem;
import com.arabaoyunu.util.HapticFeedbackSystem;
import com.arabaoyunu.ui.RewardPopupSystem;
import com.arabaoyunu.progression.AchievementSystem;
import com.arabaoyunu.progression.DailyWeeklyTaskSystem;
import com.arabaoyunu.progression.TaskAchievementHudSystem;
import com.arabaoyunu.render.VehicleFxSystem;
import com.arabaoyunu.audio.EngineSoundSystem;
import com.arabaoyunu.customization.VisualCustomizationSystem;
import com.arabaoyunu.economy.RewardPenaltySystem;
import com.arabaoyunu.economy.DriveRewardSystem;
import com.arabaoyunu.career.CareerSyncSystem;
import com.arabaoyunu.camera.FollowCamera;
import com.arabaoyunu.input.InputState;
import com.arabaoyunu.input.TouchControlsView;
import com.arabaoyunu.map.MapManager;
import com.arabaoyunu.map.BaseMap;
import com.arabaoyunu.map.MapDefinition;
import com.arabaoyunu.map.MapRegistry;
import com.arabaoyunu.map.LargeMapLoadGuard;
import com.arabaoyunu.map.CityMap01;
import com.arabaoyunu.map.HighwayMap01;
import com.arabaoyunu.map.DriftPracticeMap01;
import com.arabaoyunu.map.TestMapOpenField;
import com.arabaoyunu.map.TrackLayoutSystem;
import com.arabaoyunu.mode.DriftMode;
import com.arabaoyunu.mode.FreeDriveMode;
import com.arabaoyunu.mode.RaceMode;
import com.arabaoyunu.mode.DragRaceMode;
import com.arabaoyunu.mode.PoliceChaseMode;
import com.arabaoyunu.mode.GameModeManager;
import com.arabaoyunu.mode.GameModeCoordinator;
import com.arabaoyunu.mode.TimeTrialMode;
import com.arabaoyunu.mode.TestDriveChallengeSystem;
import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.physics.DrivingFeelResponseSystem;
import com.arabaoyunu.progression.ProgressionSystem;
import com.arabaoyunu.quest.QuestChainSystem;
import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.mission.MissionSystem;
import com.arabaoyunu.mission.DrivingMissionSystem;
import com.arabaoyunu.performance.VehicleUpgradeSystem;
import com.arabaoyunu.performance.VehicleTuningSystem;
import com.arabaoyunu.navigation.NavigationSystem;
import com.arabaoyunu.physics.DriftSystem;
import com.arabaoyunu.render.CarVisualConfig;
import com.arabaoyunu.render.ModelRenderer;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;
import com.arabaoyunu.render.StaticMapRenderer;
import com.arabaoyunu.render.VehicleLightState;
import com.arabaoyunu.ui.HudView;
import com.arabaoyunu.traffic.TrafficSystem;
import com.arabaoyunu.vehicle.VehicleCatalog;
import com.arabaoyunu.util.FpsCounter;
import com.arabaoyunu.util.GameLog;
import com.arabaoyunu.util.PerformanceMonitor;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.weather.WeatherSystem;
import com.arabaoyunu.world.WorldInteractionSystem;
import com.arabaoyunu.world.WorldPointType;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.util.Random;

public final class GameRenderer implements GLSurfaceView.Renderer {

    public interface UiBridge {
        void onScreenChangeRequested(int screen);
        void onOpenWorldPrepared(boolean success, String message);
    }

    private UiBridge uiBridge;
    private final Context context;
    private final TouchControlsView controls;
    private final HudView hudView;
    private final GameScreenState screenState;

    private final VehicleController vehicleController;
    private final FollowCamera followCamera;
    private final GameModeManager gameModeManager;
    private final GameModeCoordinator gameModeCoordinator;
    private final MapManager mapManager;
    private final LargeMapLoadGuard largeMapLoadGuard = new LargeMapLoadGuard();
    private final FpsCounter fpsCounter = new FpsCounter();
    private final RenderStats renderStats = new RenderStats();
    private final DriftSystem driftSystem = new DriftSystem();
    private final PerformanceMonitor performanceMonitor = new PerformanceMonitor();
    private final FreeDriveMode freeDriveMode;
    private final DriftMode driftMode;
    private final TimeTrialMode timeTrialMode;
    private final RaceMode raceMode;
    private final DragRaceMode dragRaceMode;
    private final PoliceChaseMode policeChaseMode;
    private final TestDriveChallengeSystem testDriveChallengeSystem;
    private final SaveManager saveManager;
    private final TrafficSystem trafficSystem;
    private final MissionSystem missionSystem;
    private final WeatherSystem weatherSystem;
    private final NavigationSystem navigationSystem;
    private final ProgressionSystem progressionSystem;
    private final RewardPenaltySystem rewardPenaltySystem;
    private final DriveRewardSystem driveRewardSystem;
    private final DrivingMissionSystem drivingMissionSystem;
    private final QuestChainSystem questChainSystem;
    private final WorldInteractionSystem worldInteractionSystem;
    private final TrackLayoutSystem trackLayoutSystem;
    private final VehicleFxSystem vehicleFxSystem;
    private final EngineSoundSystem engineSoundSystem;
    private final RewardPopupSystem rewardPopupSystem;
    private final HapticFeedbackSystem hapticFeedbackSystem;

    private static final float TARGET_FRAME_DT = 1f / 60f;
    private static final float MIN_STABLE_FRAME_DT = 1f / 120f;
    private static final float MAX_STABLE_FRAME_DT = 1f / 30f;
    private static final float HUD_UPDATE_INTERVAL = 1f / 24f;
    private static final String SHOWROOM_ASSET = ShowroomPresentationSystem.SHOWROOM_ASSET;

    private PrimitiveRenderer primitiveRenderer;
    private ModelRenderer modelRenderer;
    private ModelRenderer showroomRenderer;
    private StaticMapRenderer staticMapRenderer;
    private int staticMapRendererMapId = -1;
    private int staticMapRendererQuality = -1;
    private boolean showroomLoadAttempted;
    private int showroomQualityAtLoad = -1;
    private float showroomWarmupTimer;
    private int lastMenuPreviewScreen = -1;
    private long lastFrameNs;
    private float smoothedFrameDt = TARGET_FRAME_DT;
    private float hudUpdateTimer;
    private int hudDeferredFrames;
    private int width = 1;
    private int height = 1;

    private final float[] projection = new float[16];
    private final float[] view = new float[16];
    private final float[] viewProjection = new float[16];
    private final float[] mirrorProjection = new float[16];
    private final float[] mirrorView = new float[16];
    private final float[] mirrorViewProjection = new float[16];

    private boolean leftMirrorOpen;
    private boolean rightMirrorOpen;
    private float currentFrameDt;
    private float currentFrameVisualSteer;
    private int currentGraphicsQuality = CarVisualConfig.QUALITY_HIGH;
    private int currentVehicleIndex = -1;
    private boolean garageCameraReady;
    private float garageEyeX;
    private float garageEyeY;
    private float garageEyeZ;
    private float garageTargetX;
    private float garageTargetY;
    private float garageTargetZ;
    private String loadedDamageVehicleId = "";
    private float damageSaveTimer;
    private int activeModeSelection = -1;
    private int activeMapSelection = -1;
    private float lightBlinkTimer;
    private final VehicleLightState currentLightState = new VehicleLightState();
    private final VehicleLightState menuLightState = new VehicleLightState();
    private GameAudioManager audioManager;
    private int lastMissionRewardAudio;
    private boolean raceFinishAudioPlayed;
    private boolean policeFinishAudioPlayed;
    private boolean driftFinishAudioPlayed;
    private boolean raceProgressAwarded;
    private boolean policeProgressAwarded;
    private boolean policePenaltyApplied;
    private boolean lastNitroFx;
    private boolean lastThrottleFx;
    private boolean lastBrakeFx;
    private boolean lastHandbrakeFx;
    private float lastImpactFx;
    private int lastRaceLapFx;
    private int lastDragDistanceBucketFx;
    private boolean lastRewardPopupAudio;
    private int lastTaskAchievementPendingCount = -1;
    private int lastTestDriveIntegratedVehicle = -1;
    private int lastTestDriveIntegratedPreset = -1;
    private int lastDriveLoadoutSyncVehicle = -1;
    private int lastDriveLoadoutSyncMode = -1;
    private int lastDriveLoadoutSyncMap = -1;
    private final Random respawnRandom = new Random();

    public void setAudioManager(GameAudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public void setUiBridge(UiBridge uiBridge) {
        this.uiBridge = uiBridge;
    }

    /**
     * ArabaOyunu_61_6: Kullanıcı isteğiyle GLB Açık Dünya haritası kaldırıldı.
     * Eski kayıtlardan veya yanlışlıkla gelen isteklerden dolayı bu akış çağrılırsa
     * oyun çökmeden Açık Test Alanı'na geri döner.
     */
    public void prepareOpenWorldForDrive() {
        if (mapManager != null) {
            mapManager.setCurrentMap(new TestMapOpenField(), MapRegistry.definitionFor(GameScreenState.MAP_OPEN_FIELD));
        }
        if (screenState != null) {
            screenState.endTestDriveSession();
            screenState.setSelectedMode(GameScreenState.MODE_FREE_DRIVE);
            screenState.setSelectedMap(GameScreenState.MAP_OPEN_FIELD);
            screenState.setScreen(GameScreenState.SCREEN_MODES);
        }
        String message = "Açık Dünya haritası geçici olarak kaldırıldı; Açık Test Alanı kullanılacak.";
        GameLog.i("MapLoader", "A61_6 açık dünya GLB kaldırıldı, istek engellendi");
        if (uiBridge != null) {
            uiBridge.onOpenWorldPrepared(false, message);
        }
    }


    public GameRenderer(Context context, TouchControlsView controls, HudView hudView, GameScreenState screenState) {
        this.context = context;
        this.controls = controls;
        this.hudView = hudView;
        this.screenState = screenState == null ? new GameScreenState() : screenState;
        this.currentGraphicsQuality = controls == null ? CarVisualConfig.QUALITY_HIGH : controls.getGraphicsQuality();

        VehicleController.Tuning tuning = new VehicleController.Tuning();
        vehicleController = new VehicleController(tuning);
        vehicleController.reset(0f, tuning.rideHeight, 0f, 0f);

        saveManager = new SaveManager(context);
        saveManager.validateAndRepairState();
        CareerSyncSystem.validate(saveManager);
        trafficSystem = new TrafficSystem();
        missionSystem = new MissionSystem(saveManager);
        weatherSystem = new WeatherSystem();
        navigationSystem = new NavigationSystem();
        progressionSystem = new ProgressionSystem(saveManager);
        rewardPenaltySystem = new RewardPenaltySystem(saveManager);
        driveRewardSystem = new DriveRewardSystem(saveManager);
        drivingMissionSystem = new DrivingMissionSystem(saveManager);
        questChainSystem = new QuestChainSystem(saveManager);
        worldInteractionSystem = new WorldInteractionSystem();
        trackLayoutSystem = new TrackLayoutSystem();
        vehicleFxSystem = new VehicleFxSystem();
        engineSoundSystem = new EngineSoundSystem();
        rewardPopupSystem = new RewardPopupSystem();
        rewardPopupSystem.prime(saveManager.getCoins(), progressionSystem.getXp(), progressionSystem.getLevel());
        hapticFeedbackSystem = new HapticFeedbackSystem(context, saveManager);
        followCamera = new FollowCamera();
        mapManager = new MapManager(new TestMapOpenField());

        freeDriveMode = new FreeDriveMode(vehicleController, mapManager);
        driftMode = new DriftMode(vehicleController, mapManager, driftSystem, saveManager);
        timeTrialMode = new TimeTrialMode(vehicleController, mapManager, saveManager);
        raceMode = new RaceMode(vehicleController, mapManager, saveManager);
        dragRaceMode = new DragRaceMode(vehicleController, mapManager, saveManager);
        policeChaseMode = new PoliceChaseMode(vehicleController, mapManager, saveManager);
        testDriveChallengeSystem = new TestDriveChallengeSystem(saveManager);

        // ArabaOyunu_17: baslangic artik menu/lobi; oyun baslatilinca varsayilan serbest surus.
        gameModeManager = new GameModeManager(freeDriveMode);
        gameModeCoordinator = new GameModeCoordinator();
        gameModeCoordinator.beginMode(freeDriveMode.getName(), GameScreenState.MODE_FREE_DRIVE, GameScreenState.MAP_OPEN_FIELD, false);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.04f, 0.055f, 0.075f, 1f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        primitiveRenderer = new PrimitiveRenderer();
        primitiveRenderer.create();

        createModelRendererForSelection(currentGraphicsQuality, screenState == null ? 0 : screenState.getSelectedVehicleIndex());

        lastFrameNs = System.nanoTime();
        smoothedFrameDt = TARGET_FRAME_DT;
        hudUpdateTimer = HUD_UPDATE_INTERVAL;
        hudDeferredFrames = 0;
        if (vehicleFxSystem != null) vehicleFxSystem.reset();
        if (engineSoundSystem != null) engineSoundSystem.resetSession();
        if (rewardPopupSystem != null) rewardPopupSystem.prime(saveManager.getCoins(), progressionSystem.getXp(), progressionSystem.getLevel());
        gameModeManager.start();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        GLES20.glViewport(0, 0, this.width, this.height);
        float aspect = (float) this.width / (float) this.height;
        // ArabaOyunu_61_6: GLB haritalar pasif; dahili haritalar için görüş mesafesi
        // altyapısı hazır tutulur. Mevcut Open Field davranışı değişmez.
        Matrix.perspectiveM(projection, 0, 58f, aspect, 0.1f, 2200f);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        long now = System.nanoTime();
        float rawDt = (now - lastFrameNs) / 1000000000f;
        lastFrameNs = now;

        // ArabaOyunu_53: 40-70 ms gibi tek karelik dalgalanmalar fizik/kamera zincirine
        // ham olarak verilirse arac gazda kucuk kucuk atlar. Simulasyon zamani yumusatilir,
        // cok buyuk duraklamalar ise tek karede ileri firlatma yerine 60 FPS referansina cekilir.
        float dt = calculateStableFrameDt(rawDt);

        currentFrameDt = dt;
        updateBlinkTimer(dt);

        if (screenState != null && screenState.isMenuLike()) {
            if (audioManager != null) audioManager.setDriving(false);
            renderMenuPreview(dt);
            return;
        }

        synchronizeDriveLoadoutForCurrentMode("frame_drive");
        applySelectedModeIfNeeded();
        if (gameModeCoordinator != null) {
            gameModeCoordinator.update(
                    gameModeManager.getCurrentModeName(),
                    screenState == null ? GameScreenState.MODE_FREE_DRIVE : screenState.getSelectedMode(),
                    screenState == null ? GameScreenState.MAP_OPEN_FIELD : screenState.getSelectedMap(),
                    screenState != null && screenState.isTestDriveSessionActive());
        }
        handleTestDriveLaunchRequests();

        InputState input = controls.snapshotInput();
        if (hudView != null) {
            hudView.setDrivingMapOverlayOpen(input.mapOverlayOpen);
        }
        ensureSelectedVehicleLoaded(input.graphicsQuality);
        // ArabaOyunu_52: Kullanıcı sağa basınca araç sağa gidiyordu ama teker görseli sola dönüyordu.
        // GLB teker yaw dönüş işareti fizik yönüyle aynı hizaya alınır.
        currentFrameVisualSteer = -input.visualWheelSteer;
        boolean raceModeActiveForWeather = gameModeCoordinator != null && gameModeCoordinator.isRaceLike();
        String weatherMapName = mapManager.getCurrentMap() == null ? "" : mapManager.getCurrentMap().getName();
        weatherSystem.update(dt, input, raceModeActiveForWeather, weatherMapName);
        vehicleController.setWeatherRoadEffect(weatherSystem.getGripScale(), weatherSystem.getDragScale());
        updateDriveLightState(input);
        leftMirrorOpen = input.leftMirrorOpen;
        rightMirrorOpen = input.rightMirrorOpen;
        if (input.graphicsQuality != currentGraphicsQuality) {
            createModelRendererForSelection(input.graphicsQuality, screenState == null ? 0 : screenState.getSelectedVehicleIndex());
        }
        applySelectedVehicleTuning();
        progressionSystem.update(
                dt,
                vehicleController.getSpeedKmh(),
                gameModeManager.getCurrentModeName(),
                mapManager.getCurrentMap() == null ? "" : mapManager.getCurrentMap().getName());
        rewardPenaltySystem.update(dt);
        questChainSystem.update(
                dt,
                gameModeManager.getCurrentModeName(),
                mapManager.getCurrentMap() == null ? "" : mapManager.getCurrentMap().getName(),
                vehicleController.getSpeedKmh());
        gameModeManager.update(dt, input);
        updateTestDriveChallenge(dt, input);
        boolean simpleOpenFieldTick = isSimpleOpenFieldDrive(
                mapManager.getCurrentMap() == null ? "" : mapManager.getCurrentMap().getName(),
                gameModeManager.getCurrentModeName());
        boolean freeDriveEconomyActive = gameModeCoordinator != null
                && gameModeCoordinator.allowsFreeDriveEconomy(simpleOpenFieldTick, screenState != null && screenState.isTestDriveSessionActive());
        if (driveRewardSystem != null) {
            driveRewardSystem.update(dt, vehicleController, input, freeDriveEconomyActive);
        }
        if (drivingMissionSystem != null) {
            drivingMissionSystem.update(dt, vehicleController, input, freeDriveEconomyActive);
        }
        if (gameModeCoordinator == null || gameModeCoordinator.allowsTraffic(simpleOpenFieldTick)) updateTraffic(dt, input);
        simpleOpenFieldTick = isSimpleOpenFieldDrive(
                mapManager.getCurrentMap() == null ? "" : mapManager.getCurrentMap().getName(),
                gameModeManager.getCurrentModeName());
        if ((gameModeCoordinator == null || gameModeCoordinator.allowsMissionSystem(simpleOpenFieldTick)) && missionSystem != null) missionSystem.update(dt, vehicleController, trafficSystem);
        if (gameModeCoordinator == null || gameModeCoordinator.allowsWorldInteraction(simpleOpenFieldTick)) {
            worldInteractionSystem.update(
                    dt,
                    mapManager.getCurrentMap() == null ? "" : mapManager.getCurrentMap().getName(),
                    vehicleController.position.x,
                    vehicleController.position.z,
                    saveManager);
        }
        if (input.interactPressed && (gameModeCoordinator == null || gameModeCoordinator.allowsWorldInteraction(simpleOpenFieldTick))) {
            handleWorldInteraction();
        }
        persistDamageIfNeeded(dt);
        updateAudio(input);
        updateEventAudio();
        updateGameFeel(dt, input);
        if (input.cameraSwitchPressed) {
            followCamera.nextMode();
        }

        if (followCamera != null && screenState != null) {
            followCamera.setVehicleCameraScale(VehicleCatalog.driveCameraScale(screenState.getSelectedVehicleIndex()));
        }
        followCamera.update(dt, vehicleController, input.brake > 0.2f, input.nitro > 0.2f, input.cameraDragX, input.cameraDragY);
        followCamera.fillViewMatrix(view);
        Matrix.multiplyMM(viewProjection, 0, projection, 0, view, 0);

        GLES20.glClearColor(weatherSystem.getClearR(), weatherSystem.getClearG(), weatherSystem.getClearB(), 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        renderStats.reset();

        boolean currentExternalGlbReady = isCurrentExternalGlbReady();
        if (!currentExternalGlbReady) {
            mapManager.render(primitiveRenderer, viewProjection, renderStats);
        }
        renderExternalMapIfNeeded(viewProjection);
        if (trackLayoutSystem != null) {
            trackLayoutSystem.renderForMode(
                    primitiveRenderer,
                    viewProjection,
                    renderStats,
                    gameModeCoordinator,
                    screenState == null ? GameScreenState.MODE_FREE_DRIVE : screenState.getSelectedMode(),
                    vehicleController == null ? 0f : vehicleController.position.x,
                    vehicleController == null ? 0f : vehicleController.position.z);
        }
        if (!isOpenWorldFreeDrive() && testDriveChallengeSystem != null && testDriveChallengeSystem.isActive()) {
            testDriveChallengeSystem.render(primitiveRenderer, viewProjection, renderStats);
        }
        weatherSystem.render(
                primitiveRenderer,
                viewProjection,
                renderStats,
                vehicleController.position.x,
                vehicleController.position.z,
                mapManager.getCurrentMap() == null ? "" : mapManager.getCurrentMap().getName(),
                currentGraphicsQuality);
        boolean simpleOpenFieldRender = isSimpleOpenFieldDrive(
                mapManager.getCurrentMap() == null ? "" : mapManager.getCurrentMap().getName(),
                gameModeManager.getCurrentModeName());
        if ((gameModeCoordinator == null || gameModeCoordinator.allowsMissionSystem(simpleOpenFieldRender)) && missionSystem != null) {
            missionSystem.render(primitiveRenderer, viewProjection, renderStats);
        }
        if (gameModeCoordinator == null || gameModeCoordinator.allowsWorldInteraction(simpleOpenFieldRender)) {
            worldInteractionSystem.render(primitiveRenderer, viewProjection, renderStats);
        }
        if (raceMode != null && gameModeCoordinator != null && gameModeCoordinator.isCheckpointRace()) {
            raceMode.render(primitiveRenderer, viewProjection, renderStats);
        }
        if (dragRaceMode != null && gameModeCoordinator != null && gameModeCoordinator.isDragRace()) {
            dragRaceMode.render(primitiveRenderer, viewProjection, renderStats);
        }
        if (driftMode != null && gameModeCoordinator != null && gameModeCoordinator.isDrift()) {
            driftMode.render(primitiveRenderer, viewProjection, renderStats);
        }
        if (policeChaseMode != null && gameModeCoordinator != null && gameModeCoordinator.isPoliceChase()) {
            policeChaseMode.render(primitiveRenderer, viewProjection, renderStats);
        }
        if ((gameModeCoordinator == null || gameModeCoordinator.allowsTraffic(simpleOpenFieldRender)) && trafficSystem != null) {
            trafficSystem.render(primitiveRenderer, viewProjection, renderStats);
        }
        if (vehicleFxSystem != null) {
            int neonPreset = 0;
            if (saveManager != null) {
                neonPreset = saveManager.getVisualModValue(VehicleCatalog.id(currentVehicleIndex < 0 ? 0 : currentVehicleIndex), VisualCustomizationSystem.NEON);
            }
            vehicleFxSystem.render(primitiveRenderer, viewProjection, renderStats, vehicleController, neonPreset);
        }
        renderCarVisual(viewProjection);
        renderMirrorViews();

        int fps = fpsCounter.frame(dt);
        performanceMonitor.update(dt, fps);

        String currentModeName = gameModeManager.getCurrentModeName();
        String currentMapName = mapManager.getCurrentMap() == null ? "MAP" : mapManager.getCurrentMap().getName();
        String hudTrackName = currentMapName;
        if (trackLayoutSystem != null) {
            hudTrackName = trackLayoutSystem.labelForLayout(trackLayoutSystem.layoutFor(
                    gameModeCoordinator,
                    screenState == null ? GameScreenState.MODE_FREE_DRIVE : screenState.getSelectedMode()));
        }
        boolean simpleOpenFieldDrive = isSimpleOpenFieldDrive(currentMapName, currentModeName);
        boolean trafficHudActive = gameModeCoordinator == null ? !simpleOpenFieldDrive : gameModeCoordinator.allowsTraffic(simpleOpenFieldDrive);
        navigationSystem.update(currentMapName, currentModeName, vehicleController.position.x, vehicleController.position.z);
        float a674HudMapHalf = navigationSystem.getMapHalf();
        if (mapManager != null && mapManager.getCurrentDefinition() != null) {
            a674HudMapHalf = Math.max(a674HudMapHalf, mapManager.getCurrentDefinition().mapHalfSize);
        }

        boolean timeTrialActive = currentModeName.equals("TimeTrialMode");
        boolean openWorldFreeDrive = isOpenWorldFreeDrive();
        String openFieldMissionTitle = "-";
        String openFieldMissionProgress = "-";
        String openFieldMissionMessage = "";
        int openFieldMissionFlash = 0;
        int openFieldDailyCount = missionSystem == null ? 0 : missionSystem.getDailyCompleted();
        if (simpleOpenFieldDrive && drivingMissionSystem != null) {
            openFieldMissionTitle = drivingMissionSystem.getMissionTitle();
            openFieldMissionProgress = drivingMissionSystem.getProgressText();
            openFieldMissionMessage = firstNonEmpty(drivingMissionSystem.getMessage(), driveRewardSystem == null ? "" : driveRewardSystem.getMessage());
            openFieldMissionFlash = Math.max(drivingMissionSystem.getRewardFlash(), driveRewardSystem == null ? 0 : driveRewardSystem.getRewardFlash());
            openFieldDailyCount = saveManager == null ? openFieldDailyCount : saveManager.getDailyMissionCompletedCount();
        }
        if (shouldUpdateHud(dt)) {
            hudView.setMetrics(
                vehicleController.getSpeedKmh(),
                fps,
                driftSystem.isActive(),
                driftSystem.getCurrentScore(),
                driftSystem.getBestScore(),
                driftSystem.getComboLevel(),
                driftSystem.getMultiplier(),
                driftSystem.isInsideDriftZone(),
                driftSystem.getCrashPenalty(),
                driftMode.getTimeRemaining(),
                driftMode.isFinished(),
                driftMode.isNewBest(),
                driftMode.getEarnedCoins(),
                driftMode.getRank(),
                vehicleController.getForwardSpeed() < -0.5f ? "R" : "D",
                followCamera.getModeName(),
                hudTrackName,
                currentModeName,
                vehicleController.getRenderX(),
                vehicleController.getRenderZ(),
                vehicleController.getRenderYaw(),
                leftMirrorOpen,
                rightMirrorOpen,
                timeTrialActive,
                timeTrialMode.getElapsed(),
                timeTrialMode.getBestTime(),
                timeTrialMode.getCountdown(),
                timeTrialMode.getCheckpointIndex(),
                timeTrialMode.getCheckpointTotal(),
                timeTrialMode.isFinished(),
                timeTrialMode.isNewBest(),
                timeTrialMode.isWrongWay(),
                timeTrialMode.getFinishGrade(),
                vehicleController.getHealth01(),
                vehicleController.getMotorDamage01(),
                vehicleController.getTireDamage01(),
                vehicleController.getGlassDamage01(),
                vehicleController.getBodyDamage01(),
                !trafficHudActive || trafficSystem == null ? "KAPALI" : trafficSystem.getDensityName(),
                trafficHudActive && trafficSystem != null && trafficSystem.isNight(),
                !trafficHudActive || trafficSystem == null ? 0 : trafficSystem.getActiveCount(),
                !trafficHudActive || trafficSystem == null ? 0 : trafficSystem.getCollisionCount(),
                !trafficHudActive || trafficSystem == null ? 0 : trafficSystem.getNearMissCount(),
                simpleOpenFieldDrive ? openFieldMissionTitle : (openWorldFreeDrive ? "-" : (testDriveChallengeSystem != null && testDriveChallengeSystem.isActive() ? testDriveChallengeSystem.getTitle() : (missionSystem == null ? "-" : missionSystem.getMissionTitle()))),
                simpleOpenFieldDrive ? openFieldMissionProgress : (openWorldFreeDrive ? "-" : (testDriveChallengeSystem != null && testDriveChallengeSystem.isActive() ? testDriveChallengeSystem.getProgressText() : (missionSystem == null ? "-" : missionSystem.getMissionProgressText()))),
                simpleOpenFieldDrive ? openFieldMissionMessage : (openWorldFreeDrive ? "" : (testDriveChallengeSystem != null && testDriveChallengeSystem.isActive() ? testDriveChallengeSystem.getMessage() : (missionSystem == null ? "" : missionSystem.getMessage()))),
                simpleOpenFieldDrive ? openFieldMissionFlash : (openWorldFreeDrive ? 0 : (testDriveChallengeSystem != null && testDriveChallengeSystem.isActive() ? testDriveChallengeSystem.getRewardFlash() : (missionSystem == null ? 0 : missionSystem.getEarnedCoinsFlash()))),
                simpleOpenFieldDrive ? openFieldDailyCount : (openWorldFreeDrive ? 0 : (missionSystem == null ? 0 : missionSystem.getDailyCompleted())),
                (gameModeCoordinator != null && (gameModeCoordinator.isCheckpointRace() || gameModeCoordinator.isDragRace())),
                gameModeCoordinator != null && gameModeCoordinator.isDragRace(),
                gameModeCoordinator != null && gameModeCoordinator.isDragRace()
                        ? dragRaceMode.getStatusText()
                        : (raceMode == null ? "" : raceMode.getStatusText()),
                gameModeCoordinator != null && gameModeCoordinator.isDragRace()
                        ? (int)dragRaceMode.getDistanceMeters()
                        : (raceMode == null ? 0 : raceMode.getPlayerLap()),
                gameModeCoordinator != null && gameModeCoordinator.isDragRace()
                        ? (int)dragRaceMode.getTotalDistanceMeters()
                        : (raceMode == null ? 0 : raceMode.getTotalLaps()),
                gameModeCoordinator != null && gameModeCoordinator.isDragRace()
                        ? (dragRaceMode.didBeatRival() || !dragRaceMode.isFinished() && dragRaceMode.getDistanceMeters() >= dragRaceMode.getRivalDistanceMeters() ? 1 : 2)
                        : (raceMode == null ? 0 : raceMode.getPlayerPosition()),
                gameModeCoordinator != null && gameModeCoordinator.isDragRace()
                        ? 1
                        : (raceMode == null ? 0 : raceMode.getBotCount()),
                gameModeCoordinator != null && gameModeCoordinator.isDragRace()
                        ? dragRaceMode.isFinished()
                        : (raceMode == null ? false : raceMode.isFinished()),
                gameModeCoordinator != null && gameModeCoordinator.isDragRace()
                        ? dragRaceMode.getEarnedCoins()
                        : (raceMode == null ? 0 : raceMode.getEarnedCoins()),
                gameModeCoordinator != null && gameModeCoordinator.isDragRace()
                        ? dragRaceMode.getFinishRank()
                        : (raceMode == null ? "-" : raceMode.getFinishRank()),
                gameModeCoordinator != null && gameModeCoordinator.isDragRace()
                        ? dragRaceMode.getCountdown()
                        : (raceMode == null ? 0f : raceMode.getCountdown()),
                gameModeCoordinator != null && gameModeCoordinator.isPoliceChase(),
                policeChaseMode == null ? "" : policeChaseMode.getStatusText(),
                policeChaseMode == null ? 0 : policeChaseMode.getWantedLevel(),
                policeChaseMode == null ? 0f : policeChaseMode.getChaseTime(),
                policeChaseMode == null ? 0f : policeChaseMode.getEscapeTimer(),
                policeChaseMode == null ? 1f : policeChaseMode.getEscapeRequired(),
                policeChaseMode == null ? 0f : policeChaseMode.getCaughtTimer(),
                policeChaseMode == null ? 1f : policeChaseMode.getCaughtRequired(),
                policeChaseMode == null ? 999f : policeChaseMode.getNearestPoliceDistance(),
                policeChaseMode == null ? false : policeChaseMode.isFinished(),
                policeChaseMode == null ? false : policeChaseMode.isEscaped(),
                policeChaseMode == null ? false : policeChaseMode.isCaught(),
                policeChaseMode == null ? 0 : policeChaseMode.getEarnedCoins(),
                renderStats.drawCalls,
                renderStats.renderedObjects,
                trafficSystem == null ? 0 : trafficSystem.getSkippedFarUpdates(),
                trafficSystem == null ? 0 : trafficSystem.getCulledRenderObjects(),
                currentGraphicsQuality,
                "A67.5 " + DrivingFeelResponseSystem.profileName(input == null ? 1 : input.steeringSensitivityPreset),
                weatherSystem.getHudText(),
                weatherSystem.getGripScale(),
                weatherSystem.getHeadlightImportance(),
                weatherSystem.isNight(),
                weatherSystem.isRainy(),
                weatherSystem.isFoggy(),
                a674HudMapHalf,
                navigationSystem.getPointCount(),
                navigationSystem.getRouteTargetX(),
                navigationSystem.getRouteTargetZ(),
                navigationSystem.getRouteIcon(),
                navigationSystem.getRouteLabel(),
                navigationSystem.getRouteDistance(),
                navigationSystem.getRouteCount(),
                navigationSystem.getRouteX(0), navigationSystem.getRouteZ(0),
                navigationSystem.getRouteX(1), navigationSystem.getRouteZ(1),
                navigationSystem.getRouteX(2), navigationSystem.getRouteZ(2),
                navigationSystem.getRouteX(3), navigationSystem.getRouteZ(3),
                navigationSystem.getRouteX(4), navigationSystem.getRouteZ(4),
                navigationSystem.getRouteX(5), navigationSystem.getRouteZ(5),
                navigationSystem.getRouteX(6), navigationSystem.getRouteZ(6),
                navigationSystem.getRouteX(7), navigationSystem.getRouteZ(7),
                navigationSystem.getIcon(0), navigationSystem.getX(0), navigationSystem.getZ(0),
                navigationSystem.getIcon(1), navigationSystem.getX(1), navigationSystem.getZ(1),
                navigationSystem.getIcon(2), navigationSystem.getX(2), navigationSystem.getZ(2),
                navigationSystem.getIcon(3), navigationSystem.getX(3), navigationSystem.getZ(3),
                navigationSystem.getIcon(4), navigationSystem.getX(4), navigationSystem.getZ(4),
                navigationSystem.getIcon(5), navigationSystem.getX(5), navigationSystem.getZ(5),
                navigationSystem.getIcon(6), navigationSystem.getX(6), navigationSystem.getZ(6),
                navigationSystem.getIcon(7), navigationSystem.getX(7), navigationSystem.getZ(7),
                navigationSystem.getIcon(8), navigationSystem.getX(8), navigationSystem.getZ(8),
                navigationSystem.getIcon(9), navigationSystem.getX(9), navigationSystem.getZ(9),
                navigationSystem.getIcon(10), navigationSystem.getX(10), navigationSystem.getZ(10),
                navigationSystem.getIcon(11), navigationSystem.getX(11), navigationSystem.getZ(11),
                progressionSystem == null ? 1 : progressionSystem.getLevel(),
                progressionSystem == null ? 0 : progressionSystem.getXp(),
                progressionSystem == null ? 100 : progressionSystem.getNextXp(),
                progressionSystem == null ? 0 : progressionSystem.getDailyCount(),
                progressionSystem == null ? 0 : progressionSystem.getWeeklyCount(),
                progressionSystem == null ? 0 : progressionSystem.getAchievementCount(),
                progressionSystem == null ? 0 : progressionSystem.getCrates(),
                progressionSystem == null ? 1 : progressionSystem.getUnlockedVehicleCount(),
                progressionSystem == null ? 1 : progressionSystem.getUnlockedMapTier(),
                progressionSystem == null ? "" : progressionSystem.getUnlockText(),
                rewardPenaltySystem == null ? (progressionSystem == null ? "" : progressionSystem.getMessage()) : rewardPenaltySystem.getHudMessage(progressionSystem == null ? "" : progressionSystem.getMessage()),
                !simpleOpenFieldDrive && worldInteractionSystem != null && worldInteractionSystem.hasActivePoint(),
                simpleOpenFieldDrive || worldInteractionSystem == null ? 0 : worldInteractionSystem.getActiveType(),
                simpleOpenFieldDrive || worldInteractionSystem == null ? "" : worldInteractionSystem.getActiveTitle(),
                simpleOpenFieldDrive || worldInteractionSystem == null ? "" : worldInteractionSystem.getActiveSubtitle(),
                simpleOpenFieldDrive || worldInteractionSystem == null ? "" : worldInteractionSystem.getActiveReward(),
                simpleOpenFieldDrive || worldInteractionSystem == null ? 999f : worldInteractionSystem.getActiveDistance(),
                simpleOpenFieldDrive || worldInteractionSystem == null ? 1 : worldInteractionSystem.getActiveRequiredLevel(),
                simpleOpenFieldDrive || worldInteractionSystem == null ? "" : worldInteractionSystem.getActionText(),
                simpleOpenFieldDrive || worldInteractionSystem == null ? "" : worldInteractionSystem.getActionMessage(),
                simpleOpenFieldDrive || worldInteractionSystem == null ? 0 : worldInteractionSystem.getPointCount(),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(0), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(0), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(0),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(1), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(1), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(1),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(2), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(2), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(2),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(3), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(3), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(3),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(4), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(4), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(4),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(5), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(5), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(5),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(6), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(6), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(6),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(7), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(7), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(7),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(8), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(8), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(8),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(9), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(9), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(9),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(10), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(10), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(10),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(11), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(11), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(11),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(12), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(12), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(12),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(13), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(13), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(13),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(14), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(14), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(14),
                worldInteractionSystem == null ? 0 : worldInteractionSystem.getType(15), worldInteractionSystem == null ? 0f : worldInteractionSystem.getX(15), worldInteractionSystem == null ? 0f : worldInteractionSystem.getZ(15),
                questChainSystem == null ? false : !questChainSystem.isCompleted(),
                questChainSystem == null ? 1 : questChainSystem.getStepNumber(),
                questChainSystem == null ? 7 : questChainSystem.getTotalSteps(),
                questChainSystem == null ? "" : questChainSystem.getTitle(),
                questChainSystem == null ? "" : questChainSystem.getObjective(),
                questChainSystem == null ? "" : questChainSystem.getRewardText(),
                questChainSystem == null ? "" : questChainSystem.getMessage(),
                questChainSystem == null ? 0f : questChainSystem.getTargetX(),
                questChainSystem == null ? 0f : questChainSystem.getTargetZ(),
                questChainSystem == null ? 0 : questChainSystem.getTargetWorldType(),
                questChainSystem == null ? 0 : questChainSystem.getCompletedCount());
            if (saveManager == null || saveManager.isTaskAchievementHudEnabled()) {
                hudView.setTaskAchievementTracker(
                        TaskAchievementHudSystem.trackerTitle(saveManager),
                        TaskAchievementHudSystem.trackerSubtitle(saveManager),
                        TaskAchievementHudSystem.trackerProgressText(saveManager),
                        TaskAchievementHudSystem.trackerProgress01(saveManager),
                        TaskAchievementHudSystem.pendingRewardCount(saveManager),
                        TaskAchievementHudSystem.trackerStatus(saveManager));
            } else {
                hudView.setTaskAchievementTracker("", "", "", 0f, 0, "");
            }
            updateGarageTestDriveHudOverlay();
            if (saveManager != null && !saveManager.isRewardPopupEnabled()) {
                hudView.setGameFeelPopup("", "", 0f, 0f);
            } else if (rewardPopupSystem != null && rewardPopupSystem.isVisible()) {
                hudView.setGameFeelPopup(rewardPopupSystem.getTitle(), rewardPopupSystem.getSubtitle(), rewardPopupSystem.getAlpha01(), rewardPopupSystem.getSlide01());
            } else {
                hudView.setGameFeelPopup("", "", 0f, 0f);
            }
        }
    }













    private void updateGarageTestDriveHudOverlay() {
        if (hudView == null) return;
        boolean active = screenState != null && screenState.isTestDriveSessionActive();
        if (!active) {
            hudView.setGarageTestDriveHud(false, "", "", "", "");
            return;
        }
        int vehicleIndex = screenState.getSelectedVehicleIndex();
        String id = VehicleCatalog.id(vehicleIndex);
        int preset = saveManager == null ? VehicleTuningSystem.PRESET_BALANCED : saveManager.getTuningPreset(id);
        int build = saveManager == null ? 0 : VehicleUpgradeSystem.buildScore(saveManager, vehicleIndex);
        int perf = saveManager == null ? 0 : VehicleUpgradeSystem.totalPerformanceLevel(saveManager, id);
        String buildLine = DriveLoadoutSyncSystem.tuningLine(saveManager, vehicleIndex)
                + "  |  " + DriveLoadoutSyncSystem.visualLine(saveManager, vehicleIndex)
                + "  |  " + PostTenUpdateStabilityQaSystem.saveRepairLine(saveManager);
        hudView.setGarageTestDriveHud(
                true,
                "TEST SÜRÜŞÜ • AÇIK TEST ALANI",
                VehicleCatalog.label(vehicleIndex),
                buildLine,
                PostTenUpdateStabilityQaSystem.driveFlowQaLine(saveManager, vehicleIndex,
                        GameScreenState.MODE_FREE_DRIVE, GameScreenState.MAP_OPEN_FIELD));
    }

    private void synchronizeDriveLoadoutForCurrentMode(String reason) {
        if (screenState == null || saveManager == null) return;
        int safeVehicle = DriveLoadoutSyncSystem.syncDriveSelection(saveManager, screenState);
        int safeMap = DriveLoadoutSyncSystem.syncModeMap(saveManager, screenState);
        if (modelRenderer == null || safeVehicle != currentVehicleIndex) {
            createModelRendererForSelection(currentGraphicsQuality, safeVehicle);
        }
        int mode = screenState.getSelectedMode();
        if (safeVehicle != lastDriveLoadoutSyncVehicle
                || mode != lastDriveLoadoutSyncMode
                || safeMap != lastDriveLoadoutSyncMap) {
            lastDriveLoadoutSyncVehicle = safeVehicle;
            lastDriveLoadoutSyncMode = mode;
            lastDriveLoadoutSyncMap = safeMap;
            GameLog.i("DriveLoadout", "A67.0 " + reason + " | "
                    + DriveLoadoutSyncSystem.qaLine(saveManager, safeVehicle, mode, safeMap)
                    + " | " + DriveLoadoutSyncSystem.tuningLine(saveManager, safeVehicle));
        }
    }

    private void synchronizeGarageSelectionForTestDrive() {
        if (screenState == null || saveManager == null) return;
        DriveLoadoutSyncSystem.syncDriveSelection(saveManager, screenState);
        screenState.setSelectedMode(GameScreenState.MODE_FREE_DRIVE);
        screenState.setSelectedMap(GameScreenState.MAP_OPEN_FIELD);
        saveManager.setSelectedMap(GameScreenState.MAP_OPEN_FIELD);
        synchronizeDriveLoadoutForCurrentMode("test_drive_sync");
    }

    private void logTestDriveIntegrationIfChanged() {
        if (screenState == null || saveManager == null) return;
        int vehicleIndex = screenState.getSelectedVehicleIndex();
        String id = VehicleCatalog.id(vehicleIndex);
        int preset = saveManager.getTuningPreset(id);
        if (vehicleIndex == lastTestDriveIntegratedVehicle && preset == lastTestDriveIntegratedPreset) return;
        lastTestDriveIntegratedVehicle = vehicleIndex;
        lastTestDriveIntegratedPreset = preset;
        GameLog.i("GarageTestDrive", "A67.0 sürüş aktarım final QA: "
                + VehicleCatalog.label(vehicleIndex)
                + " | " + VehicleTuningSystem.drivingFeelFinalLine(saveManager, vehicleIndex));
    }

    private void handleTestDriveLaunchRequests() {
        if (screenState == null) return;
        if (screenState.isTestDriveSessionActive()) {
            if (screenState.consumeTestDriveLaunchRequest()) {
                activeModeSelection = -99;
                activeMapSelection = -99;
                synchronizeGarageSelectionForTestDrive();
                applySelectedModeIfNeeded();
                createModelRendererForSelection(currentGraphicsQuality, screenState.getSelectedVehicleIndex());
                applySelectedVehicleTuning();
                resetVehicleForTestDrive();
                logTestDriveIntegrationIfChanged();
                if (testDriveChallengeSystem != null) {
                    testDriveChallengeSystem.start(screenState.getSelectedVehicleIndex(), vehicleController);
                }
                if (audioManager != null) audioManager.playReward();
            } else if (screenState.consumeTestDriveRestartRequest()) {
                synchronizeGarageSelectionForTestDrive();
                applySelectedVehicleTuning();
                resetVehicleForTestDrive();
                logTestDriveIntegrationIfChanged();
                if (testDriveChallengeSystem != null) {
                    testDriveChallengeSystem.restartCurrent(vehicleController);
                }
                if (audioManager != null) audioManager.playRespawn();
            }
        } else if (testDriveChallengeSystem != null && testDriveChallengeSystem.isActive()) {
            testDriveChallengeSystem.stop();
        }
    }

    private void resetVehicleForTestDrive() {
        synchronizeGarageSelectionForTestDrive();
        applySelectedVehicleTuning();
        BaseMap map = mapManager == null ? null : mapManager.getCurrentMap();
        float x = 0f;
        float z = 0f;
        float yaw = 0f;
        float y = vehicleController == null ? 0.42f : vehicleController.getTuning().rideHeight;
        if (map != null && map.getRespawnPointCount() > 0) {
            x = map.getRespawnX(0);
            y = map.getRespawnY(0);
            z = map.getRespawnZ(0);
            yaw = map.getRespawnYaw(0);
        }
        vehicleController.reset(x, y, z, yaw);
        loadDamageForCurrentVehicle();
        forceFullHealthForFreshDrive("test_drive_start");
        if (followCamera != null) followCamera.forceSnap();
        if (worldInteractionSystem != null) {
            int v = screenState == null ? currentVehicleIndex : screenState.getSelectedVehicleIndex();
            worldInteractionSystem.setActionMessage("Test sürüşü: " + VehicleCatalog.label(v) + " | "
                    + VehicleTuningSystem.testDriveFeelLine(saveManager, v));
        }
    }

    private void resetVehicleForCurrentMap(String actionMessage) {
        synchronizeDriveLoadoutForCurrentMode("mode_reset");
        BaseMap map = mapManager == null ? null : mapManager.getCurrentMap();
        float x = 0f;
        float z = 0f;
        float yaw = 0f;
        float y = vehicleController == null ? 0.42f : vehicleController.getTuning().rideHeight;
        if (map != null && map.getRespawnPointCount() > 0) {
            x = map.getRespawnX(0);
            y = map.getRespawnY(0);
            z = map.getRespawnZ(0);
            yaw = map.getRespawnYaw(0);
        }
        if (trackLayoutSystem != null && screenState != null) {
            TrackLayoutSystem.SpawnPoint spawn = trackLayoutSystem.spawnForMode(screenState.getSelectedMode());
            if (spawn != null) {
                x = spawn.x;
                y = spawn.y;
                z = spawn.z;
                yaw = spawn.yaw;
            }
        }
        if (vehicleController != null) {
            vehicleController.reset(x, y, z, yaw);
            loadDamageForCurrentVehicle();
            forceFullHealthForFreshDrive("map_reset");
        }
        if (followCamera != null) followCamera.forceSnap();
        if (worldInteractionSystem != null && actionMessage != null) {
            worldInteractionSystem.setActionMessage(actionMessage);
        }
    }


    private void updateTestDriveChallenge(float dt, InputState input) {
        if (screenState == null || testDriveChallengeSystem == null) return;
        if (isOpenWorldFreeDrive()) {
            if (testDriveChallengeSystem.isActive()) testDriveChallengeSystem.stop();
            return;
        }
        if (!screenState.isTestDriveSessionActive()) {
            if (testDriveChallengeSystem.isActive()) testDriveChallengeSystem.stop();
            return;
        }
        if (!testDriveChallengeSystem.isActive()) {
            testDriveChallengeSystem.start(screenState.getSelectedVehicleIndex(), vehicleController);
        }
        testDriveChallengeSystem.update(dt, vehicleController, input);
        if (testDriveChallengeSystem.isResultVisible()) {
            screenState.setLastTestDriveResult(testDriveChallengeSystem.getLastResult(), testDriveChallengeSystem.getLastReward());
        }
    }

    private static String firstNonEmpty(String a, String b) {
        if (a != null && a.length() > 0) return a;
        return b == null ? "" : b;
    }

    private float calculateStableFrameDt(float rawDt) {
        if (rawDt <= 0f || rawDt != rawDt || rawDt > 0.18f) {
            rawDt = TARGET_FRAME_DT;
        }
        float clamped = clamp(rawDt, MIN_STABLE_FRAME_DT, MAX_STABLE_FRAME_DT);
        float blend = rawDt > MAX_STABLE_FRAME_DT ? 0.10f : 0.18f;
        smoothedFrameDt = lerp(smoothedFrameDt, clamped, blend);
        return clamp(smoothedFrameDt, MIN_STABLE_FRAME_DT, MAX_STABLE_FRAME_DT);
    }

    private boolean shouldUpdateHud(float dt) {
        hudUpdateTimer += Math.max(0f, dt);
        hudDeferredFrames++;
        if (hudUpdateTimer >= HUD_UPDATE_INTERVAL || hudDeferredFrames >= 4) {
            hudUpdateTimer = 0f;
            hudDeferredFrames = 0;
            return true;
        }
        return false;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private void handleWorldInteraction() {
        if (worldInteractionSystem == null || !worldInteractionSystem.hasActivePoint()) return;
        if (!worldInteractionSystem.isLevelAllowed(saveManager)) {
            if (audioManager != null) audioManager.playLocked();
            worldInteractionSystem.setActionMessage("Gerekli seviye: " + worldInteractionSystem.getActiveRequiredLevel());
            return;
        }

        int type = worldInteractionSystem.getActiveType();
        if (questChainSystem != null) questChainSystem.onWorldAction(type);
        if (type == WorldPointType.RACE) {
            startWorldMode(GameScreenState.MODE_RACE_LOCKED, currentMapSelectionSafe(), "Yarış noktası başlatıldı");
        } else if (type == WorldPointType.DRIFT) {
            startWorldMode(GameScreenState.MODE_DRIFT, GameScreenState.MAP_OPEN_FIELD, "Drift skor modu başlatıldı");
        } else if (type == WorldPointType.TIME_TRIAL) {
            startWorldMode(GameScreenState.MODE_TIME_TRIAL, currentMapSelectionSafe(), "Zaman yarışı başlatıldı");
        } else if (type == WorldPointType.POLICE_ESCAPE) {
            startWorldMode(GameScreenState.MODE_POLICE_CHASE, currentMapSelectionSafe(), "Polis kaçış noktası başlatıldı");
        } else if (type == WorldPointType.GARAGE || type == WorldPointType.VEHICLE_GALLERY) {
            requestScreenFromWorld(GameScreenState.SCREEN_GARAGE);
            worldInteractionSystem.setActionMessage(type == WorldPointType.GARAGE ? "Garaj açıldı" : "Araç galerisi açıldı");
        } else if (type == WorldPointType.REPAIR) {
            handleWorldRepair();
        } else if (type == WorldPointType.FUEL_SERVICE) {
            if (saveManager != null) {
                saveManager.addXp(45);
                saveManager.addCoins(45);
                saveManager.setEconomyLastMessage("BAKIM ÖDÜLÜ: +45 XP +45 coin");
            }
            if (audioManager != null) audioManager.playReward();
            worldInteractionSystem.setActionMessage("Bakım yapıldı: +45 XP +45 coin");
        } else if (type == WorldPointType.SPECIAL_EVENT) {
            String msg = rewardPenaltySystem == null ? "Özel etkinlik" : rewardPenaltySystem.grantSpecialEventReward();
            if (audioManager != null) audioManager.playReward();
            worldInteractionSystem.setActionMessage(msg);
        }
    }

    private int currentMapSelectionSafe() {
        return screenState == null ? GameScreenState.MAP_OPEN_FIELD : screenState.getSelectedMap();
    }

    private void startWorldMode(int mode, int map, String message) {
        if (screenState != null) {
            screenState.setSelectedMode(mode);
            screenState.setSelectedMap(DriveLoadoutSyncSystem.safeMapForMode(mode, map));
        }
        synchronizeDriveLoadoutForCurrentMode("world_mode_start");
        activeModeSelection = -99;
        activeMapSelection = -99;
        applySelectedModeIfNeeded();
        followCamera.forceSnap();
        if (audioManager != null) audioManager.playReward();
        if (worldInteractionSystem != null) worldInteractionSystem.setActionMessage(message);
    }

    private void requestScreenFromWorld(int screen) {
        if (screenState != null) {
            screenState.setReturnToDriveAfterMenu(true);
            screenState.setScreen(screen);
        }
        if (audioManager != null) audioManager.playMenuClick();
        if (uiBridge != null) {
            uiBridge.onScreenChangeRequested(screen);
        }
    }

    private void handleWorldRepair() {
        if (saveManager == null || currentVehicleIndex < 0) return;
        String id = VehicleCatalog.id(currentVehicleIndex);
        boolean ok = saveManager.repairVehicle(id);
        if (ok) {
            loadDamageForCurrentVehicle();
            if (audioManager != null) audioManager.playReward();
            worldInteractionSystem.setActionMessage("Araç tamir edildi");
        } else {
            if (audioManager != null) audioManager.playLocked();
            worldInteractionSystem.setActionMessage("Tamir için coin yetersiz");
        }
    }

    private void updateTraffic(float dt, InputState input) {
        if (trafficSystem == null) return;
        int density = input == null ? TrafficSystem.DENSITY_MEDIUM : input.trafficDensity;
        int quality = input == null ? currentGraphicsQuality : input.graphicsQuality;
        String modeName = gameModeManager == null ? "" : gameModeManager.getCurrentModeName();
        String mapName = mapManager == null || mapManager.getCurrentMap() == null ? "" : mapManager.getCurrentMap().getName();
        boolean simpleOpenField = isSimpleOpenFieldDrive(mapName, modeName);
        boolean police = gameModeCoordinator != null && gameModeCoordinator.isPoliceChase();
        boolean checkpointLight = false;
        boolean freeTraffic = gameModeCoordinator == null ? simpleOpenField : (gameModeCoordinator.isFreeDrive() && simpleOpenField);
        trafficSystem.setModeContext(freeTraffic, police, checkpointLight);
        trafficSystem.setDensity(density);
        trafficSystem.setPerformanceLevel(quality);
        trafficSystem.update(dt, vehicleController);

        int nearMissCoins = trafficSystem.consumePendingRewardCoins();
        int nearMissEvents = trafficSystem.consumePendingNearMissEvents();
        if (nearMissCoins > 0 && saveManager != null) {
            saveManager.recordTrafficNearMissReward(nearMissCoins, trafficSystem.getRiskCombo(), police, nearMissEvents);
            if (rewardPopupSystem != null) {
                rewardPopupSystem.push("YAKIN GECIS x" + trafficSystem.getRiskCombo(), "+" + nearMissCoins + " coin" + (police ? " | POLIS RISK" : ""));
            }
            if (hapticFeedbackSystem != null && trafficSystem.getRiskCombo() >= 3) hapticFeedbackSystem.tick("reward");
        }
        int collisions = trafficSystem.consumePendingCollisionEvents();
        if (collisions > 0 && saveManager != null) {
            saveManager.recordTrafficCollision(collisions);
            if (rewardPopupSystem != null) rewardPopupSystem.push("TRAFIK CARPISMASI", "Risk combo sifirlandi");
        }
    }

    private void loadDamageForCurrentVehicle() {
        if (saveManager == null || currentVehicleIndex < 0) return;
        String id = VehicleCatalog.id(currentVehicleIndex);
        loadedDamageVehicleId = id;
        vehicleController.loadDamageState(
                saveManager.getVehicleHealth(id),
                saveManager.getVehicleMotorDamage(id),
                saveManager.getVehicleTireDamage(id),
                saveManager.getVehicleGlassDamage(id),
                saveManager.getVehicleBodyDamage(id));
    }

    /**
     * A61.7: Serbest/test surusune girerken eski kaydedilmis hasar oyuncuyu
     * direkt KRITIK HASAR ekranina sokmasin. Hasar sistemi kalir; sadece yeni
     * test/surus oturumu temiz baslatilir.
     */
    private void forceFullHealthForFreshDrive(String reason) {
        if (vehicleController == null) return;
        vehicleController.repairFull();
        damageSaveTimer = 0f;
        if (saveManager != null && currentVehicleIndex >= 0) {
            String id = VehicleCatalog.id(currentVehicleIndex);
            if (id != null) {
                saveManager.saveVehicleDamage(id, 1f, 0f, 0f, 0f, 0f);
            }
        }
        GameLog.i("DriveStability", "A61_7 fresh drive health reset: " + reason);
    }

    private boolean isSimpleOpenFieldDrive(String mapName, String modeName) {
        String map = mapName == null ? "" : mapName;
        String mode = modeName == null ? "" : modeName;
        return map.indexOf("TestMap_OpenField") >= 0 && mode.indexOf("FreeDrive") >= 0;
    }

    private void persistDamageIfNeeded(float dt) {
        if (saveManager == null || currentVehicleIndex < 0) return;
        damageSaveTimer += Math.max(0f, dt);
        boolean changed = vehicleController.consumeDamageChangedEvent();
        if (!changed && damageSaveTimer < 0.85f) return;
        damageSaveTimer = 0f;
        String id = VehicleCatalog.id(currentVehicleIndex);
        if (id == null) return;
        saveManager.saveVehicleDamage(
                id,
                vehicleController.getHealth01(),
                vehicleController.getMotorDamage01(),
                vehicleController.getTireDamage01(),
                vehicleController.getGlassDamage01(),
                vehicleController.getBodyDamage01());
    }

    private void applySelectedVehicleTuning() {
        if (vehicleController == null || saveManager == null || screenState == null) return;
        int index = screenState.getSelectedVehicleIndex();
        VehicleController.Tuning tuning = vehicleController.getTuning();
        // ArabaOyunu_61_8: 13 performans yükseltmesi gerçek sürüş fiziğine ve hasar direncine bağlanır.
        VehicleUpgradeSystem.applyTuning(saveManager, index, tuning);
        // ArabaOyunu_47: detaylı tuning ayarları aynı aracı yarış/drift/polis için farklı hissettirir.
        VehicleTuningSystem.applyDetailedTuning(
                saveManager,
                index,
                tuning,
                gameModeManager == null ? "" : gameModeManager.getCurrentModeName());
        boolean testDrive = screenState != null && screenState.isTestDriveSessionActive();
        if (testDrive) {
            VehicleTuningSystem.applyTestDriveFeelProfile(saveManager, index, tuning);
        }
        VehicleTuningSystem.applyDrivingFeelFinalGuard(saveManager, index, tuning, testDrive);
    }


    private void updateEventAudio() {
        if (audioManager == null) return;

        int missionReward = missionSystem == null ? 0 : missionSystem.getEarnedCoinsFlash();
        if (missionReward > 0 && missionReward != lastMissionRewardAudio) {
            audioManager.playReward();
            if (progressionSystem != null) progressionSystem.onMissionReward(missionReward);
            lastMissionRewardAudio = missionReward;
        } else if (missionReward <= 0) {
            lastMissionRewardAudio = 0;
        }

        boolean raceModeActive = gameModeManager.getCurrentModeName().equals("RaceMode");
        if (raceModeActive && raceMode != null && raceMode.isFinished()) {
            if (!raceFinishAudioPlayed) {
                audioManager.playReward();
                if (progressionSystem != null && !raceProgressAwarded) {
                    progressionSystem.onRaceFinished(raceMode.getEarnedCoins(), raceMode.getPlayerPosition());
                    raceProgressAwarded = true;
                }
                raceFinishAudioPlayed = true;
            }
        } else {
            raceFinishAudioPlayed = false;
            raceProgressAwarded = false;
        }

        boolean driftModeActive = gameModeManager.getCurrentModeName().equals("DriftMode");
        if (driftModeActive && driftMode != null && driftMode.isFinished()) {
            if (!driftFinishAudioPlayed) {
                if (driftMode.getEarnedCoins() > 0) audioManager.playReward();
                else audioManager.playBack();
                driftFinishAudioPlayed = true;
            }
        } else {
            driftFinishAudioPlayed = false;
        }

        boolean policeModeActive = gameModeManager.getCurrentModeName().equals("PoliceChaseMode");
        if (policeModeActive && policeChaseMode != null && policeChaseMode.isFinished()) {
            if (!policeFinishAudioPlayed) {
                if (policeChaseMode.isEscaped()) audioManager.playReward();
                else audioManager.playBack();
                if (progressionSystem != null && !policeProgressAwarded) {
                    progressionSystem.onPoliceFinished(policeChaseMode.isEscaped(), policeChaseMode.getEarnedCoins(), policeChaseMode.getWantedLevel(), policeChaseMode.getEarnedXp());
                    policeProgressAwarded = true;
                }
                if (rewardPenaltySystem != null && policeChaseMode.isCaught() && !policePenaltyApplied) {
                    String vehicleId = currentVehicleIndex < 0 ? "" : VehicleCatalog.id(currentVehicleIndex);
                    rewardPenaltySystem.applyPoliceCaughtPenalty(vehicleId, policeChaseMode.getWantedLevel());
                    policePenaltyApplied = true;
                }
                policeFinishAudioPlayed = true;
            }
        } else {
            policeFinishAudioPlayed = false;
            policeProgressAwarded = false;
            policePenaltyApplied = false;
        }
    }

    private void updateAudio(InputState input) {
        if (audioManager == null) return;
        boolean timeTrialActive = gameModeManager.getCurrentModeName().equals("TimeTrialMode");
        audioManager.setDriving(true);
        audioManager.updateDrivingAudio(
                vehicleController,
                input,
                timeTrialActive,
                timeTrialMode.getCountdown());
        if (policeChaseMode != null
                && gameModeManager.getCurrentModeName().equals("PoliceChaseMode")
                && policeChaseMode.isActiveChase()) {
            audioManager.playSirenTick();
        }
    }


    private void updateGameFeel(float dt, InputState input) {
        if (vehicleFxSystem != null) {
            vehicleFxSystem.update(dt, vehicleController, input, gameModeCoordinator);
        }
        boolean throttleNow = input != null && input.throttle > 0.45f;
        if (throttleNow && !lastThrottleFx) {
            if (followCamera != null) followCamera.addThrottleKick(0.36f);
            if (hapticFeedbackSystem != null && vehicleController != null && vehicleController.getSpeedKmh() < 35f) hapticFeedbackSystem.tick("throttle");
        }
        lastThrottleFx = throttleNow;

        boolean brakeNow = input != null && input.brake > 0.48f;
        if (brakeNow && !lastBrakeFx) {
            if (followCamera != null) followCamera.addBrakeDive(0.42f);
            if (hapticFeedbackSystem != null && vehicleController != null && vehicleController.getSpeedKmh() > 18f) hapticFeedbackSystem.tick("brake");
        }
        lastBrakeFx = brakeNow;

        boolean handbrakeNow = input != null && input.handbrake > 0.48f;
        if (handbrakeNow && !lastHandbrakeFx) {
            if (followCamera != null) followCamera.addHandbrakeFocus(0.50f);
            if (hapticFeedbackSystem != null) hapticFeedbackSystem.tick("handbrake");
        }
        lastHandbrakeFx = handbrakeNow;

        boolean nitroNow = input != null && input.nitro > 0.35f;
        if (nitroNow && !lastNitroFx) {
            if (followCamera != null) followCamera.addNitroKick(0.78f);
            if (hapticFeedbackSystem != null) hapticFeedbackSystem.tick("nitro");
        }
        lastNitroFx = nitroNow;

        float impact = vehicleController == null ? 0f : vehicleController.getImpactFeedback();
        if (impact > 0.65f && impact > lastImpactFx + 0.05f) {
            if (followCamera != null) followCamera.addImpactShake(impact);
            if (hapticFeedbackSystem != null) hapticFeedbackSystem.tick("impact");
        }
        lastImpactFx = impact * 0.84f;

        boolean checkpointPassed = false;
        if (gameModeCoordinator != null && gameModeCoordinator.isCheckpointRace() && raceMode != null) {
            int lap = raceMode.getPlayerLap();
            if (lastRaceLapFx > 0 && lap > lastRaceLapFx) checkpointPassed = true;
            lastRaceLapFx = lap;
        } else if (gameModeCoordinator != null && gameModeCoordinator.isDragRace() && dragRaceMode != null) {
            int bucket = ((int)dragRaceMode.getDistanceMeters()) / 100;
            if (bucket > lastDragDistanceBucketFx && bucket > 0) checkpointPassed = true;
            lastDragDistanceBucketFx = bucket;
        } else {
            lastRaceLapFx = 0;
            lastDragDistanceBucketFx = 0;
        }

        if (checkpointPassed && hapticFeedbackSystem != null) hapticFeedbackSystem.tick("checkpoint");

        int taskAchievementPendingCount = 0;
        if (saveManager != null) {
            DailyWeeklyTaskSystem.ensureWindows(saveManager);
            taskAchievementPendingCount = TaskAchievementHudSystem.pendingRewardCount(saveManager);
            // A63.9: Render/update döngüsü hâlâ ödül vermez; yalnızca HUD ve bildirim için bekleyen ödülü okur.
        }

        boolean popupReward = false;
        if (rewardPopupSystem != null) {
            boolean popupEnabled = saveManager == null || saveManager.isRewardPopupEnabled();
            if (popupEnabled) {
                int level = progressionSystem == null ? 1 : progressionSystem.getLevel();
                int xp = progressionSystem == null ? 0 : progressionSystem.getXp();
                int coins = saveManager == null ? 0 : saveManager.getCoins();
                String msg = saveManager == null ? "" : saveManager.getEconomyLastMessage();
                popupReward = rewardPopupSystem.watchEconomy(coins, xp, level, msg);
                int mode = saveManager == null ? 1 : saveManager.getTaskAchievementNotificationMode();
                if (mode >= 0 && lastTaskAchievementPendingCount >= 0 && taskAchievementPendingCount > lastTaskAchievementPendingCount) {
                    popupReward = rewardPopupSystem.push(
                            TaskAchievementHudSystem.completionPopupTitle(saveManager, mode),
                            TaskAchievementHudSystem.completionPopupSubtitle(saveManager, mode));
                }
            }
            lastTaskAchievementPendingCount = taskAchievementPendingCount;
            rewardPopupSystem.update(dt);
            if (popupReward && hapticFeedbackSystem != null) hapticFeedbackSystem.tick("reward");
        }

        if (gameModeCoordinator != null && gameModeCoordinator.isPoliceChase() && policeChaseMode != null
                && policeChaseMode.isActiveChase() && hapticFeedbackSystem != null
                && policeChaseMode.getNearestPoliceDistance() < 34f) {
            hapticFeedbackSystem.tick("police");
        }

        if (engineSoundSystem != null) {
            engineSoundSystem.update(audioManager, vehicleController, input, gameModeCoordinator, checkpointPassed, popupReward && !lastRewardPopupAudio);
        }
        lastRewardPopupAudio = popupReward;
    }

    public void requestRandomRespawn() {
        if (screenState != null && screenState.isTestDriveSessionActive()) {
            screenState.requestTestDriveRestart();
        }
        BaseMap map = mapManager == null ? null : mapManager.getCurrentMap();
        if (map == null) {
            vehicleController.reset(0f, vehicleController.position.y, 0f, 0f);
            forceFullHealthForFreshDrive("respawn_default");
            GameLog.i("Respawn", "Map yok; default respawn uygulandi.");
            return;
        }

        int count = Math.max(1, map.getRespawnPointCount());
        int index = count <= 1 ? 0 : respawnRandom.nextInt(count);
        float x = map.getRespawnX(index);
        float y = map.getRespawnY(index);
        float z = map.getRespawnZ(index);
        float yaw = map.getRespawnYaw(index);
        vehicleController.reset(x, y, z, yaw);
        forceFullHealthForFreshDrive("random_respawn");
        followCamera.forceSnap();
        GameLog.i("Respawn", "Rastgele respawn index=" + index
                + " count=" + count
                + " map=" + map.getName()
                + " pos=" + x + "," + y + "," + z);
    }

    private void updateBlinkTimer(float dt) {
        if (dt < 0f || dt > 0.2f) dt = 1f / 60f;
        lightBlinkTimer += dt * 1.6f;
        if (lightBlinkTimer > 1f) lightBlinkTimer -= (float) Math.floor(lightBlinkTimer);
        menuLightState.headlightsOn = true;
        menuLightState.brakeOn = false;
        menuLightState.reverseOn = false;
        menuLightState.hazardOn = false;
        menuLightState.leftSignalOn = false;
        menuLightState.rightSignalOn = false;
        menuLightState.blinkPhase = lightBlinkTimer;
    }

    private void updateDriveLightState(InputState input) {
        // ArabaOyunu_25: ışıklar artık oyun içi butonlardan kontrol edilir.
        // Fren ve geri vites otomatik kalır; far/sinyal/dörtlü manuel gelir.
        currentLightState.headlightsOn = input == null || input.headlightsOn || weatherSystem.getHeadlightImportance() > 0.55f;
        currentLightState.brakeOn = input != null && input.brake > 0.18f;
        currentLightState.reverseOn = vehicleController.getForwardSpeed() < -0.55f;
        currentLightState.hazardOn = input != null && input.hazardOn;
        currentLightState.leftSignalOn = input != null && input.leftSignalOn;
        currentLightState.rightSignalOn = input != null && input.rightSignalOn;
        currentLightState.blinkPhase = lightBlinkTimer;
    }

    private void applySelectedModeIfNeeded() {
        int desired = screenState == null ? GameScreenState.MODE_FREE_DRIVE : screenState.getSelectedMode();
        int desiredMap = screenState == null ? GameScreenState.MAP_OPEN_FIELD : screenState.getSelectedMap();
        desiredMap = DriveLoadoutSyncSystem.safeMapForMode(desired, desiredMap);
        if (screenState != null && screenState.getSelectedMap() != desiredMap) screenState.setSelectedMap(desiredMap);
        if (saveManager != null && saveManager.getSelectedMap() != desiredMap) saveManager.setSelectedMap(desiredMap);
        synchronizeDriveLoadoutForCurrentMode("mode_apply");

        if (desired == activeModeSelection && desiredMap == activeMapSelection) return;

        activeModeSelection = desired;
        activeMapSelection = desiredMap;

        if (desired == GameScreenState.MODE_TIME_TRIAL) {
            mapManager.setCurrentMap(createMapForSelection(desiredMap), MapRegistry.definitionFor(desiredMap));
            gameModeManager.switchMode(timeTrialMode);
        } else if (desired == GameScreenState.MODE_DRIFT) {
            // A65.2: Drift Skor modu açık dünya/harici GLB gerektirmez; Open Field üzerinde çalışır.
            mapManager.setCurrentMap(new TestMapOpenField(), MapRegistry.definitionFor(GameScreenState.MAP_OPEN_FIELD));
            gameModeManager.switchMode(driftMode);
        } else if (desired == GameScreenState.MODE_RACE_LOCKED) {
            mapManager.setCurrentMap(createMapForSelection(desiredMap), MapRegistry.definitionFor(desiredMap));
            gameModeManager.switchMode(raceMode);
        } else if (desired == GameScreenState.MODE_DRAG_RACE) {
            mapManager.setCurrentMap(new TestMapOpenField(), MapRegistry.definitionFor(GameScreenState.MAP_OPEN_FIELD));
            gameModeManager.switchMode(dragRaceMode);
        } else if (desired == GameScreenState.MODE_POLICE_CHASE) {
            mapManager.setCurrentMap(createMapForSelection(desiredMap), MapRegistry.definitionFor(desiredMap));
            gameModeManager.switchMode(policeChaseMode);
        } else {
            mapManager.setCurrentMap(createMapForSelection(desiredMap), MapRegistry.definitionFor(desiredMap));
            gameModeManager.switchMode(freeDriveMode);
        }
        resetVehicleForCurrentMap("Sürüş alanı hazır");
        // A62.0: GameRenderer genel map reseti, RaceMode.start() içindeki yarış
        // başlangıç çizgisini ezmesin. Reset sonrası yarış modunu bir kez daha
        // başlatıp aracı checkpoint start noktasına sabitliyoruz.
        if (desired == GameScreenState.MODE_RACE_LOCKED && raceMode != null) {
            raceMode.start();
        }
        if (desired == GameScreenState.MODE_DRAG_RACE && dragRaceMode != null) {
            dragRaceMode.start();
        }
        if (desired == GameScreenState.MODE_DRIFT && driftMode != null) {
            driftMode.start();
        }
        if (desired == GameScreenState.MODE_POLICE_CHASE && policeChaseMode != null) {
            policeChaseMode.start();
        }
        if (gameModeCoordinator != null) {
            gameModeCoordinator.beginMode(gameModeManager.getCurrentModeName(), desired, desiredMap, screenState != null && screenState.isTestDriveSessionActive());
        }
        raceFinishAudioPlayed = false;
        driftFinishAudioPlayed = false;
        raceProgressAwarded = false;
        policeFinishAudioPlayed = false;
        policeProgressAwarded = false;
        policePenaltyApplied = false;
        followCamera.forceSnap();
    }

    private BaseMap createMapForSelection(int map) {
        MapDefinition definition = MapRegistry.definitionFor(map);
        if (map == GameScreenState.MAP_OPEN_WORLD || map == GameScreenState.MAP_SECOND_NEW) {
            largeMapLoadGuard.reset();
            if (screenState != null) {
                screenState.setSelectedMap(GameScreenState.MAP_OPEN_FIELD);
            }
            GameLog.i("MapLoader", "A61_6 pasif GLB harita yerine Açık Test Alanı kullanıldı");
            return new TestMapOpenField();
        }
        if (definition != null && definition.isPendingExternalAsset()) {
            // Dosyası hâlâ beklenen büyük harita slotları sürüşe açılmaz.
            largeMapLoadGuard.markWaitingForAsset(definition);
            if (screenState != null) {
                screenState.setSelectedMap(GameScreenState.MAP_OPEN_FIELD);
            }
            GameLog.i("MapLoader", "A61_2 büyük harita slotu pasif: "
                    + definition.displayName + " asset=" + definition.assetPath);
            return new TestMapOpenField();
        }
        largeMapLoadGuard.reset();
        if (map == GameScreenState.MAP_CITY) return new CityMap01();
        if (map == GameScreenState.MAP_HIGHWAY) return new HighwayMap01();
        if (map == GameScreenState.MAP_DRIFT_PARK) return new DriftPracticeMap01();
        return new TestMapOpenField();
    }


    private void renderExternalMapIfNeeded(float[] vp) {
        if (mapManager == null) return;
        MapDefinition definition = mapManager.getCurrentDefinition();
        if (definition == null || !definition.externalGlb || !definition.selectableNow) {
            disposeStaticMapRendererIfInactive();
            return;
        }
        ensureStaticMapRenderer(definition);
        if (staticMapRenderer != null && staticMapRenderer.isReady()) {
            mapManager.getLoadGuard().markReady(definition);
            staticMapRenderer.render(
                    vp,
                    vehicleController == null ? 0f : vehicleController.position.x,
                    vehicleController == null ? 0f : vehicleController.position.z,
                    renderStats);
        }
    }

    private void ensureStaticMapRenderer(MapDefinition definition) {
        if (definition == null || !definition.externalGlb || !definition.selectableNow) return;
        int mapQuality = qualityForStaticMap(definition);
        if (staticMapRenderer != null
                && staticMapRenderer.isReady()
                && staticMapRendererMapId == definition.id
                && staticMapRendererQuality == mapQuality) {
            return;
        }
        if (staticMapRenderer != null) {
            staticMapRenderer.dispose();
            staticMapRenderer = null;
        }
        staticMapRendererMapId = definition.id;
        staticMapRendererQuality = mapQuality;
        staticMapRenderer = new StaticMapRenderer();
        mapManager.getLoadGuard().markLoading(definition);
        staticMapRenderer.create(context, definition, mapQuality);
        if (staticMapRenderer.isReady()) {
            mapManager.getLoadGuard().markReady(definition);
            GameLog.i("MapLoader", "A61_6 static GLB harita aktif: " + staticMapRenderer.getStatus());
        } else {
            mapManager.getLoadGuard().markFailed(definition, staticMapRenderer.getStatus());
            GameLog.i("MapLoader", "A61_6 static GLB harita yüklenemedi: " + staticMapRenderer.getStatus());
        }
    }

    private boolean isCurrentExternalGlbReady() {
        if (mapManager == null) return false;
        MapDefinition definition = mapManager.getCurrentDefinition();
        return definition != null
                && definition.externalGlb
                && staticMapRenderer != null
                && staticMapRenderer.isReady()
                && staticMapRendererMapId == definition.id;
    }

    private boolean isOpenWorldFreeDrive() {
        // A61_6: Açık Dünya haritası geçici olarak kaldırıldığı için özel açık dünya HUD/görev mantığı kapalı.
        return false;
    }

    private int qualityForStaticMap(MapDefinition definition) {
        if (definition == null) return currentGraphicsQuality;
        int recommended = definition.recommendedQuality;
        if (recommended < CarVisualConfig.QUALITY_LOW || recommended > CarVisualConfig.QUALITY_ULTRA) {
            recommended = CarVisualConfig.QUALITY_MEDIUM;
        }
        if (definition.id == GameScreenState.MAP_OPEN_WORLD) {
            // A61_6: GLB açık dünya pasif; bu yol sadece gelecekte tekrar aktif edilirse kullanılır.
            return Math.min(currentGraphicsQuality, recommended);
        }
        return Math.min(currentGraphicsQuality, recommended);
    }

    private void disposeStaticMapRendererIfInactive() {
        if (staticMapRenderer == null) return;
        staticMapRenderer.dispose();
        staticMapRenderer = null;
        staticMapRendererMapId = -1;
        staticMapRendererQuality = -1;
    }

    private void renderMenuPreview(float dt) {
        if (hudView != null) {
            hudView.setGarageTestDriveHud(false, "", "", "", "");
        }
        if (primitiveRenderer == null) return;
        ensureSelectedVehicleLoaded(currentGraphicsQuality);
        // Menü/garaj tarafında tamir yapılırsa preview hemen güncellensin.
        loadDamageForCurrentVehicle();

        GLES20.glClearColor(0.018f, 0.022f, 0.032f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        renderStats.reset();

        int screen = screenState == null ? GameScreenState.SCREEN_MAIN_MENU : screenState.getScreen();
        if (screen == GameScreenState.SCREEN_GARAGE) {
            if (lastMenuPreviewScreen != GameScreenState.SCREEN_GARAGE) {
                showroomWarmupTimer = 0f;
            }
            // A64.4: Projede gerçek 3D showroom asset'i var. Artık ilk karelerde
            // yapay/prosedürel grid zorla gösterilmez; gerçek showroom önceliklidir.
            showroomWarmupTimer = 1f;
        } else {
            showroomWarmupTimer = 0f;
        }
        lastMenuPreviewScreen = screen;

        float yaw = screenState == null ? 0f : screenState.getPreviewYaw();

        if (screen == GameScreenState.SCREEN_MAIN_MENU) {
            // Ana menude secili arac hafif canli dursun; garajda kullanici elle cevirir.
            if (screenState != null) screenState.addPreviewYaw(dt * 0.22f);
            yaw = screenState == null ? yaw : screenState.getPreviewYaw();
        } else if (screen == GameScreenState.SCREEN_GARAGE && screenState != null
                && screenState.getGarageMode() == GameScreenState.GARAGE_MODE_SELECT) {
            // ArabaOyunu_57: showroomda arac cok hafif donerek sergilenir.
            screenState.addPreviewYaw(dt * 0.10f);
            yaw = screenState.getPreviewYaw();
        }

        float carX = screen == GameScreenState.SCREEN_GARAGE ? 0.06f : -1.18f;
        float carZ = 0f;
        float carY = screen == GameScreenState.SCREEN_GARAGE ? 0.45f : 0.42f;

        float eyeX = screen == GameScreenState.SCREEN_GARAGE ? 0.06f : -1.18f;
        float eyeY = screen == GameScreenState.SCREEN_GARAGE ? 2.32f : 2.05f;
        float eyeZ = screen == GameScreenState.SCREEN_GARAGE ? 8.10f : 7.8f;
        float targetX = carX;
        float targetY = 0.82f;
        float targetZ = carZ;

        if (screen == GameScreenState.SCREEN_GARAGE && screenState != null) {
            int previewVehicleIndex = currentVehicleIndex < 0 ? screenState.getSelectedVehicleIndex() : currentVehicleIndex;
            GarageShowroomSystem.CameraProfile profile = GarageShowroomSystem.cameraFor(
                    screenState.getGarageMode(),
                    screenState.getSelectedVisualModType(),
                    screenState.getPreviewZoom(),
                    carX,
                    carY,
                    carZ,
                    previewVehicleIndex);
            eyeX = profile.eyeX;
            eyeY = profile.eyeY;
            eyeZ = profile.eyeZ;
            targetX = profile.targetX;
            targetY = profile.targetY;
            targetZ = profile.targetZ;
        }

        if (screen == GameScreenState.SCREEN_GARAGE) {
            // ArabaOyunu_59: sabit çarpan yerine zamana bağlı smooth blend; 30/60/90 FPS'te benzer kamera hissi.
            float safeDt = Math.max(0.001f, Math.min(dt, 0.050f));
            float blend = 1f - (float) Math.pow(0.018f, safeDt * 60f);
            blend = Math.max(0.07f, Math.min(0.30f, blend));
            if (!garageCameraReady) {
                garageEyeX = eyeX;
                garageEyeY = eyeY;
                garageEyeZ = eyeZ;
                garageTargetX = targetX;
                garageTargetY = targetY;
                garageTargetZ = targetZ;
                garageCameraReady = true;
            } else {
                garageEyeX += (eyeX - garageEyeX) * blend;
                garageEyeY += (eyeY - garageEyeY) * blend;
                garageEyeZ += (eyeZ - garageEyeZ) * blend;
                garageTargetX += (targetX - garageTargetX) * blend;
                garageTargetY += (targetY - garageTargetY) * blend;
                garageTargetZ += (targetZ - garageTargetZ) * blend;
            }
            eyeX = garageEyeX;
            eyeY = garageEyeY;
            eyeZ = garageEyeZ;
            targetX = garageTargetX;
            targetY = garageTargetY;
            targetZ = garageTargetZ;
        } else {
            garageCameraReady = false;
        }

        Matrix.setLookAtM(view, 0,
                eyeX, eyeY, eyeZ,
                targetX, targetY, targetZ,
                0f, 1f, 0f);
        Matrix.multiplyMM(viewProjection, 0, projection, 0, view, 0);

        boolean showroomDrawn = false;
        if (screen == GameScreenState.SCREEN_GARAGE) {
            showroomDrawn = renderGarageShowroom(viewProjection, dt);
        }
        if (!showroomDrawn) {
            // A64.4: Gerçek showroom yüklenemezse bile oyuncuya yapay mavi grid
            // ana vitrin gibi gösterilmez; sadece temiz, koyu ve sade zemin kalır.
            primitiveRenderer.drawGround(viewProjection, 46f, renderStats);
        }
        if (screen == GameScreenState.SCREEN_GARAGE && !showroomDrawn) {
            // Prosedürel showroom artık sadece son çare fallback'tir.
            drawGarageShowroomStage(viewProjection, carX, carY, carZ, yaw, screenState == null ? 0 : screenState.getGarageMode());
        }

        boolean glbDrawn = false;
        if (modelRenderer != null && modelRenderer.isReady()) {
            String customId = VehicleCatalog.id(currentVehicleIndex < 0 ? 0 : currentVehicleIndex);
            modelRenderer.setCustomization(
                    saveManager == null ? 0 : getGaragePreviewVisualValue(customId, VisualCustomizationSystem.PAINT_COLOR),
                    saveManager == null ? 0 : saveManager.getRimPreset(customId),
                    getGaragePreviewVisualValue(customId, VisualCustomizationSystem.PAINT_FINISH),
                    getGaragePreviewVisualValue(customId, VisualCustomizationSystem.WRAP),
                    getGaragePreviewVisualValue(customId, VisualCustomizationSystem.STICKER),
                    getGaragePreviewVisualValue(customId, VisualCustomizationSystem.RIM_STYLE),
                    getGaragePreviewVisualValue(customId, VisualCustomizationSystem.RIM_COLOR),
                    getGaragePreviewVisualValue(customId, VisualCustomizationSystem.MIRROR_COLOR),
                    getGaragePreviewVisualValue(customId, VisualCustomizationSystem.WINDOW_TINT),
                    getGaragePreviewVisualValue(customId, VisualCustomizationSystem.HEADLIGHT_COLOR),
                    getGaragePreviewVisualValue(customId, VisualCustomizationSystem.TAIL_LIGHT_STYLE));
            modelRenderer.setDamageVisual(
                    vehicleController.getHealth01(),
                    vehicleController.getMotorDamage01(),
                    vehicleController.getTireDamage01(),
                    vehicleController.getGlassDamage01(),
                    vehicleController.getBodyDamage01());
            glbDrawn = modelRenderer.renderCar(
                    viewProjection,
                    carX,
                    carY,
                    carZ,
                    0f,
                    yaw,
                    0f,
                    0f,
                    0f,
                    0f,
                    menuLightState,
                    renderStats);
        }

        if (!glbDrawn && primitiveRenderer != null) {
            primitiveRenderer.drawCarFallback(
                    viewProjection,
                    carX,
                    carY,
                    carZ,
                    0f,
                    yaw,
                    0f,
                    renderStats);
        }
    }

    private void drawGarageShowroomStage(float[] vp, float carX, float carY, float carZ, float yaw, int garageMode) {
        if (primitiveRenderer == null || vp == null) return;
        String vehicleId = VehicleCatalog.id(currentVehicleIndex < 0 ? 0 : currentVehicleIndex);
        float[] paintRgb = GarageShowroomSystem.paintRgb(saveManager, vehicleId);
        float[] neonRgb = GarageShowroomSystem.neonRgb(saveManager, vehicleId);
        boolean neon = GarageShowroomSystem.hasNeon(saveManager, vehicleId);

        // A64.4: Bu blok sadece gerçek 3D showroom yüklenemezse çalışır.
        // Ana showroom görüntüsü olarak kullanılmaz; araç boşlukta kalmasın diye
        // sade, düşük opaklıklı fallback platformu çizer.
        primitiveRenderer.drawFlatRect(vp, carX, 0.026f, carZ, 6.2f, 6.2f, 0.020f, 0.026f, 0.040f, 0.42f, renderStats);

        // Boya renk swatch/spot ışık çubukları: oyuncu garajda seçili rengi hemen görür.
        // Renk/neon swatch çizimleri gerçek showroom yerine geçmediği için A64.4'te
        // fallbackte bile çok hafif tutulur.
        if (neon) {
            primitiveRenderer.drawFlatRect(vp, carX, 0.070f, carZ + 0.12f, 2.45f, 3.85f, neonRgb[0], neonRgb[1], neonRgb[2], 0.20f, renderStats);
        }
    }

    private boolean renderGarageShowroom(float[] vp, float dt) {
        if (vp == null) return false;
        ensureShowroomRenderer();
        if (showroomRenderer == null || !showroomRenderer.isReady()) return false;
        // Showroom sadece menü/garaj render katmanında çizilir; sürüş fiziğine, spawn'a veya haritaya eklenmez.
        // A64.4: Gerçek 3D showroom asset'i araç boyası/jant/neon presetleriyle yanlışlıkla
        // renklendirilmez; araç kişiselleştirme yalnızca araç modeline uygulanır.
        showroomRenderer.setCustomization(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
        showroomRenderer.setDamageVisual(1f, 0f, 0f, 0f, 0f);
        return showroomRenderer.renderCar(
                vp,
                0f,
                ShowroomPresentationSystem.renderY(),
                0f,
                0f,
                0f,
                0f,
                0f,
                0f,
                dt,
                null,
                renderStats);
    }

    private void ensureShowroomRenderer() {
        if (showroomLoadAttempted && showroomQualityAtLoad == currentGraphicsQuality) return;
        showroomLoadAttempted = true;
        showroomQualityAtLoad = currentGraphicsQuality;
        if (showroomRenderer != null) {
            showroomRenderer.dispose();
        }
        showroomRenderer = new ModelRenderer();
        CarVisualConfig showroomConfig = ShowroomPresentationSystem.showroomConfigForQuality(currentGraphicsQuality);
        showroomRenderer.createStatic(context, showroomConfig);
        GameLog.i("Renderer", showroomRenderer.isReady()
                ? "A66.8 Gerçek 3D showroom final sunum aktif: " + SHOWROOM_ASSET
                        + " texMax=" + showroomConfig.maxTextureSize
                        + " | " + ShowroomPresentationSystem.lightingLine(currentGraphicsQuality)
                        + " | " + showroomRenderer.qaSummary()
                : "A66.8 Gerçek showroom yüklenemedi; sadece temiz fallback zemin kullanılacak.");
    }

    private int getGaragePreviewVisualValue(String vehicleId, int type) {
        int saved = saveManager == null ? 0 : saveManager.getVisualModValue(vehicleId, type);
        if (screenState != null
                && screenState.getScreen() == GameScreenState.SCREEN_GARAGE
                && screenState.getGarageMode() == GameScreenState.GARAGE_MODE_VISUAL_EDIT
                && screenState.isVisualEditActiveFor(type)) {
            return screenState.getVisualEditPreviewValue();
        }
        return saved;
    }

    private void createModelRendererForSelection(int quality, int vehicleIndex) {
        int q = quality;
        if (q < CarVisualConfig.QUALITY_LOW || q > CarVisualConfig.QUALITY_ULTRA) {
            q = CarVisualConfig.QUALITY_HIGH;
        }

        int v = vehicleIndex;
        if (v < 0 || v >= VehicleCatalog.count()) {
            v = 0;
        }

        if (modelRenderer != null) {
            modelRenderer.dispose();
        }

        currentGraphicsQuality = q;
        currentVehicleIndex = v;
        loadDamageForCurrentVehicle();
        if (screenState == null || !screenState.isMenuLike()) {
            forceFullHealthForFreshDrive("vehicle_loaded_for_drive");
        }
        modelRenderer = new ModelRenderer();
        CarVisualConfig visualConfig = VehicleCatalog.configForVehicle(currentVehicleIndex, currentGraphicsQuality);
        modelRenderer.create(context, visualConfig);
        GameLog.i("Renderer", modelRenderer.isReady()
                ? "Gercek GLB arac aktif: arac=" + VehicleCatalog.label(currentVehicleIndex)
                        + " kalite=" + visualConfig.qualityName
                        + " textureMax=" + visualConfig.maxTextureSize
                : "GLB arac hazir degil; fallback araba govdesi kullaniliyor. arac="
                        + VehicleCatalog.label(currentVehicleIndex)
                        + " kalite=" + visualConfig.qualityName);
    }

    private void ensureSelectedVehicleLoaded(int quality) {
        int selectedVehicle = screenState == null ? 0 : screenState.getSelectedVehicleIndex();
        if (modelRenderer == null
                || selectedVehicle != currentVehicleIndex
                || quality != currentGraphicsQuality) {
            createModelRendererForSelection(quality, selectedVehicle);
        }
    }

    private void renderCarVisual(float[] vp) {
        boolean glbDrawn = false;
        if (modelRenderer != null && modelRenderer.isReady()) {
            String customId = VehicleCatalog.id(currentVehicleIndex < 0 ? 0 : currentVehicleIndex);
            modelRenderer.setCustomization(
                    saveManager == null ? 0 : getGaragePreviewVisualValue(customId, VisualCustomizationSystem.PAINT_COLOR),
                    saveManager == null ? 0 : saveManager.getRimPreset(customId),
                    saveManager == null ? 0 : saveManager.getVisualModValue(customId, VisualCustomizationSystem.PAINT_FINISH),
                    saveManager == null ? 0 : saveManager.getVisualModValue(customId, VisualCustomizationSystem.WRAP),
                    saveManager == null ? 0 : saveManager.getVisualModValue(customId, VisualCustomizationSystem.STICKER),
                    saveManager == null ? 0 : saveManager.getVisualModValue(customId, VisualCustomizationSystem.RIM_STYLE),
                    saveManager == null ? 0 : saveManager.getVisualModValue(customId, VisualCustomizationSystem.RIM_COLOR),
                    saveManager == null ? 0 : saveManager.getVisualModValue(customId, VisualCustomizationSystem.MIRROR_COLOR),
                    saveManager == null ? 0 : saveManager.getVisualModValue(customId, VisualCustomizationSystem.WINDOW_TINT),
                    saveManager == null ? 0 : saveManager.getVisualModValue(customId, VisualCustomizationSystem.HEADLIGHT_COLOR),
                    saveManager == null ? 0 : saveManager.getVisualModValue(customId, VisualCustomizationSystem.TAIL_LIGHT_STYLE));
            modelRenderer.setDamageVisual(
                    vehicleController.getHealth01(),
                    vehicleController.getMotorDamage01(),
                    vehicleController.getTireDamage01(),
                    vehicleController.getGlassDamage01(),
                    vehicleController.getBodyDamage01());
            glbDrawn = modelRenderer.renderCar(
                    vp,
                    vehicleController.getRenderX(),
                    vehicleController.getRenderY(),
                    vehicleController.getRenderZ(),
                    vehicleController.getRenderPitch(),
                    vehicleController.getRenderYaw(),
                    vehicleController.getRenderRoll(),
                    vehicleController.getForwardSpeed(),
                    currentFrameVisualSteer,
                    currentFrameDt,
                    currentLightState,
                    renderStats);
        }

        // Kritik guvenlik: GLB yuklense bile hic parca cizilemezse ekranda araba kaybolmasin.
        if (!glbDrawn && primitiveRenderer != null) {
            primitiveRenderer.drawCarFallback(
                    vp,
                    vehicleController.getRenderX(),
                    vehicleController.getRenderY(),
                    vehicleController.getRenderZ(),
                    vehicleController.getRenderPitch(),
                    vehicleController.getRenderYaw(),
                    vehicleController.getRenderRoll(),
                    renderStats);
        }

        // ArabaOyunu_48: Sahte/fallback far-stop-sinyal markerları kaldırıldı.
        // Modelde gerçek ışık mesh/material yoksa dışarıdan ışık eklenmez.
    }

    private void renderMirrorViews() {
        if (!leftMirrorOpen && !rightMirrorOpen) return;
        if (primitiveRenderer == null || mapManager == null) return;

        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);

        if (leftMirrorOpen) {
            renderMirrorViewport(true);
        }
        if (rightMirrorOpen) {
            renderMirrorViewport(false);
        }

        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
        GLES20.glViewport(0, 0, width, height);
        GLES20.glScissor(0, 0, width, height);
        GLES20.glClearColor(weatherSystem.getClearR(), weatherSystem.getClearG(), weatherSystem.getClearB(), 1f);
    }

    private void renderMirrorViewport(boolean leftSide) {
        // ArabaOyunu_21: aynalar kucultuldu ve ust koselere alindi.
        // Onceki 0.22w / 0.16h panel butonlarin ustune geliyordu.
        int panelW = Math.max(108, (int) (width * 0.165f));
        int panelH = Math.max(58, (int) (height * 0.105f));
        int marginX = Math.max(10, (int) (width * 0.016f));
        int topMargin = Math.max(46, (int) (height * 0.070f));

        int vx = leftSide ? marginX : width - marginX - panelW;
        // OpenGL viewport/scissor koordinati alt soldan baslar.
        int vy = height - topMargin - panelH;

        GLES20.glViewport(vx, vy, panelW, panelH);
        GLES20.glScissor(vx, vy, panelW, panelH);
        GLES20.glClearColor(0.018f, 0.024f, 0.032f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        float aspect = (float) panelW / (float) panelH;
        Matrix.perspectiveM(mirrorProjection, 0, 62f, aspect, 0.1f, 420f);

        buildMirrorView(leftSide);
        Matrix.multiplyMM(mirrorViewProjection, 0, mirrorProjection, 0, mirrorView, 0);

        mapManager.render(primitiveRenderer, mirrorViewProjection, renderStats);
        float savedDt = currentFrameDt;
        currentFrameDt = 0f;
        renderCarVisual(mirrorViewProjection);
        currentFrameDt = savedDt;
    }

    private void buildMirrorView(boolean leftSide) {
        float yaw = vehicleController.getRenderYaw();
        float sin = (float) Math.sin(yaw);
        float cos = (float) Math.cos(yaw);
        float rightX = cos;
        float rightZ = -sin;

        float side = leftSide ? -1f : 1f;
        float rearDistance = 1.25f;
        float sideOffset = 1.25f;
        float height = 1.25f;

        float eyeX = vehicleController.getRenderX() - sin * rearDistance + rightX * sideOffset * side;
        float eyeY = vehicleController.getRenderY() + height;
        float eyeZ = vehicleController.getRenderZ() - cos * rearDistance + rightZ * sideOffset * side;

        // Kamera sol/sag arka tarafi gorsun.
        float targetX = vehicleController.getRenderX() - sin * 18f + rightX * side * 6.0f;
        float targetY = vehicleController.getRenderY() + 0.82f;
        float targetZ = vehicleController.getRenderZ() - cos * 18f + rightZ * side * 6.0f;

        Matrix.setLookAtM(mirrorView, 0,
                eyeX, eyeY, eyeZ,
                targetX, targetY, targetZ,
                0f, 1f, 0f);
    }
}
