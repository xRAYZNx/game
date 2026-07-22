package com.arabaoyunu.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import com.arabaoyunu.mode.CheckpointRaceSystem;
import com.arabaoyunu.mode.DriftScoreSystem;
import com.arabaoyunu.mode.RaceModeSystem;
import com.arabaoyunu.navigation.CircularMiniMapSystem;

import java.util.Locale;

/**
 * ArabaOyunu_10 temiz HUD.
 * Buyuk MOD/FPS/ZAMAN panelleri kaldirildi.
 */
public final class HudView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF panel = new RectF();
    private final Path miniMapClipPath = new Path();

    private float speedKmh;
    private int fps;

    private boolean drifting;
    private int driftScore;
    private int bestScore;
    private int driftComboLevel;
    private float driftMultiplier = 1f;
    private boolean insideDriftZone;
    private int driftCrashPenalty;
    private float driftTimeRemaining;
    private boolean driftFinished;
    private boolean driftNewBest;
    private int earnedCoins;
    private String rank = "D";
    private String currentModeName = "";
    private String currentMapName = "";
    private boolean drivingMapOverlayOpen;

    private boolean timeTrialActive;
    private float timeTrialElapsed;
    private float timeTrialBest;
    private float timeTrialCountdown;
    private int checkpointIndex;
    private int checkpointTotal;
    private boolean timeTrialFinished;
    private boolean timeTrialNewBest;
    private boolean wrongWay;
    private String timeTrialGrade = "-";

    private String gear = "D";
    private float carX;
    private float carZ;
    private float carYaw;
    private boolean leftMirrorOpen;
    private boolean rightMirrorOpen;

    private float vehicleHealth = 1f;
    private float motorDamage;
    private float tireDamage;
    private float glassDamage;
    private float bodyDamage;

    private String trafficDensity = "KAPALI";
    private boolean trafficNight;
    private int trafficActiveCount;
    private int trafficCollisionCount;
    private int trafficNearMissCount;

    private String missionTitle = "-";
    private String missionProgress = "-";
    private String missionMessage = "";
    private int missionCoinsFlash;
    private int dailyMissionCompleted;

    private boolean raceActive;
    private boolean dragRaceActive;
    private String raceStatus = "";
    private int raceLap;
    private int raceTotalLaps;
    private int racePosition;
    private int raceBotCount;
    private boolean raceFinished;
    private int raceEarnedCoins;
    private String raceFinishRank = "-";
    private float raceCountdown;

    private boolean policeActive;
    private String policeStatus = "";
    private int wantedLevel;
    private float policeChaseTime;
    private float policeEscapeTimer;
    private float policeEscapeRequired = 1f;
    private float policeCaughtTimer;
    private float policeCaughtRequired = 1f;
    private float nearestPoliceDistance = 999f;
    private boolean policeFinished;
    private boolean policeEscaped;
    private boolean policeCaught;
    private int policeEarnedCoins;

    private int diagDrawCalls;
    private int diagRenderedObjects;
    private int diagTrafficSkipped;
    private int diagTrafficCulled;
    private int diagGraphicsQuality;
    private String diagBuildTag = "A61.3";

    private String weatherText = "GUNDUZ / ACIK";
    private float weatherGripScale = 1f;
    private float headlightImportance;
    private boolean weatherNight;
    private boolean weatherRain;
    private boolean weatherFog;

    private static final int NAV_MAX_POINTS = 12;
    private static final int NAV_MAX_ROUTE = 8;
    private static final int WORLD_MAX_POINTS = 16;
    private final int[] navIcons = new int[NAV_MAX_POINTS];
    private final float[] navX = new float[NAV_MAX_POINTS];
    private final float[] navZ = new float[NAV_MAX_POINTS];
    private float navMapHalf = 260f;
    private int navPointCount;
    private float navRouteX;
    private float navRouteZ;
    private int navRouteIcon;
    private String navRouteLabel = "-";
    private float navRouteDistance;
    private int navRouteCount;
    private final float[] navRouteXs = new float[NAV_MAX_ROUTE];
    private final float[] navRouteZs = new float[NAV_MAX_ROUTE];

    private int playerLevel = 1;
    private int playerXp;
    private int playerNextXp = 100;
    private int progressDaily;
    private int progressWeekly;
    private int achievementCount;
    private int rewardCrates;
    private int unlockedVehicles = 1;
    private int unlockedMapTier = 1;
    private String unlockText = "";
    private String progressionMessage = "";

    private boolean worldNearby;
    private int worldType;
    private String worldTitle = "";
    private String worldSubtitle = "";
    private String worldReward = "";
    private float worldDistance = 999f;
    private int worldRequiredLevel = 1;
    private String worldActionText = "";
    private String worldActionMessage = "";
    private int worldPointCount;
    private final int[] worldTypes = new int[WORLD_MAX_POINTS];
    private final float[] worldX = new float[WORLD_MAX_POINTS];
    private final float[] worldZ = new float[WORLD_MAX_POINTS];

    private boolean questActive;
    private int questStep = 1;
    private int questTotal = 7;
    private String questTitle = "";
    private String questObjective = "";
    private String questReward = "";
    private String questMessage = "";
    private float questTargetX;
    private float questTargetZ;
    private int questTargetType;
    private int questCompletedCount;

    private String gameFeelPopupTitle = "";
    private String gameFeelPopupSubtitle = "";
    private float gameFeelPopupAlpha;
    private float gameFeelPopupSlide;

    private String taskHudTitle = "";
    private String taskHudSubtitle = "";
    private String taskHudProgress = "";
    private String taskHudStatus = "";
    private float taskHudProgress01;
    private int taskHudPendingRewardCount;

    private boolean garageTestDriveActive;
    private String garageTestDriveTitle = "";
    private String garageTestDriveVehicle = "";
    private String garageTestDriveBuild = "";
    private String garageTestDriveStatus = "";

    public HudView(Context context) {
        super(context);
        paint.setTypeface(android.graphics.Typeface.create("sans", android.graphics.Typeface.BOLD));
        setWillNotDraw(false);
    }

    public void setMetrics(
            float speedKmh,
            int fps,
            boolean drifting,
            int driftScore,
            int bestScore,
            int comboLevel,
            float multiplier,
            boolean insideDriftZone,
            int crashPenalty,
            float driftTimeRemaining,
            boolean driftFinished,
            boolean driftNewBest,
            int earnedCoins,
            String rank,
            String gear,
            String cameraMode,
            String mapName,
            String modeName,
            float carX,
            float carZ,
            float carYaw,
            boolean leftMirrorOpen,
            boolean rightMirrorOpen,
            boolean timeTrialActive,
            float timeTrialElapsed,
            float timeTrialBest,
            float timeTrialCountdown,
            int checkpointIndex,
            int checkpointTotal,
            boolean timeTrialFinished,
            boolean timeTrialNewBest,
            boolean wrongWay,
            String timeTrialGrade,
            float vehicleHealth,
            float motorDamage,
            float tireDamage,
            float glassDamage,
            float bodyDamage,
            String trafficDensity,
            boolean trafficNight,
            int trafficActiveCount,
            int trafficCollisionCount,
            int trafficNearMissCount,
            String missionTitle,
            String missionProgress,
            String missionMessage,
            int missionCoinsFlash,
            int dailyMissionCompleted,
            boolean raceActive,
            boolean dragRaceActive,
            String raceStatus,
            int raceLap,
            int raceTotalLaps,
            int racePosition,
            int raceBotCount,
            boolean raceFinished,
            int raceEarnedCoins,
            String raceFinishRank,
            float raceCountdown,
            boolean policeActive,
            String policeStatus,
            int wantedLevel,
            float policeChaseTime,
            float policeEscapeTimer,
            float policeEscapeRequired,
            float policeCaughtTimer,
            float policeCaughtRequired,
            float nearestPoliceDistance,
            boolean policeFinished,
            boolean policeEscaped,
            boolean policeCaught,
            int policeEarnedCoins,
            int diagDrawCalls,
            int diagRenderedObjects,
            int diagTrafficSkipped,
            int diagTrafficCulled,
            int diagGraphicsQuality,
            String diagBuildTag,
            String weatherText,
            float weatherGripScale,
            float headlightImportance,
            boolean weatherNight,
            boolean weatherRain,
            boolean weatherFog,
            float navMapHalf,
            int navPointCount,
            float navRouteX,
            float navRouteZ,
            int navRouteIcon,
            String navRouteLabel,
            float navRouteDistance,
            int navRouteCount,
            float navRouteX0, float navRouteZ0,
            float navRouteX1, float navRouteZ1,
            float navRouteX2, float navRouteZ2,
            float navRouteX3, float navRouteZ3,
            float navRouteX4, float navRouteZ4,
            float navRouteX5, float navRouteZ5,
            float navRouteX6, float navRouteZ6,
            float navRouteX7, float navRouteZ7,
            int navIcon0, float navX0, float navZ0,
            int navIcon1, float navX1, float navZ1,
            int navIcon2, float navX2, float navZ2,
            int navIcon3, float navX3, float navZ3,
            int navIcon4, float navX4, float navZ4,
            int navIcon5, float navX5, float navZ5,
            int navIcon6, float navX6, float navZ6,
            int navIcon7, float navX7, float navZ7,
            int navIcon8, float navX8, float navZ8,
            int navIcon9, float navX9, float navZ9,
            int navIcon10, float navX10, float navZ10,
            int navIcon11, float navX11, float navZ11,
            int playerLevel,
            int playerXp,
            int playerNextXp,
            int progressDaily,
            int progressWeekly,
            int achievementCount,
            int rewardCrates,
            int unlockedVehicles,
            int unlockedMapTier,
            String unlockText,
            String progressionMessage,
            boolean worldNearby,
            int worldType,
            String worldTitle,
            String worldSubtitle,
            String worldReward,
            float worldDistance,
            int worldRequiredLevel,
            String worldActionText,
            String worldActionMessage,
            int worldPointCount,
            int worldType0, float worldX0, float worldZ0,
            int worldType1, float worldX1, float worldZ1,
            int worldType2, float worldX2, float worldZ2,
            int worldType3, float worldX3, float worldZ3,
            int worldType4, float worldX4, float worldZ4,
            int worldType5, float worldX5, float worldZ5,
            int worldType6, float worldX6, float worldZ6,
            int worldType7, float worldX7, float worldZ7,
            int worldType8, float worldX8, float worldZ8,
            int worldType9, float worldX9, float worldZ9,
            int worldType10, float worldX10, float worldZ10,
            int worldType11, float worldX11, float worldZ11,
            int worldType12, float worldX12, float worldZ12,
            int worldType13, float worldX13, float worldZ13,
            int worldType14, float worldX14, float worldZ14,
            int worldType15, float worldX15, float worldZ15,
            boolean questActive,
            int questStep,
            int questTotal,
            String questTitle,
            String questObjective,
            String questReward,
            String questMessage,
            float questTargetX,
            float questTargetZ,
            int questTargetType,
            int questCompletedCount) {
        this.speedKmh = speedKmh;
        this.fps = fps;

        this.drifting = drifting;
        this.driftScore = driftScore;
        this.bestScore = bestScore;
        this.driftComboLevel = Math.max(0, comboLevel);
        this.driftMultiplier = Math.max(1f, multiplier);
        this.insideDriftZone = insideDriftZone;
        this.driftCrashPenalty = Math.max(0, crashPenalty);
        this.driftTimeRemaining = Math.max(0f, driftTimeRemaining);
        this.driftFinished = driftFinished;
        this.driftNewBest = driftNewBest;
        this.earnedCoins = earnedCoins;
        this.rank = rank == null ? "D" : rank;

        this.gear = gear == null ? "D" : gear;
        this.currentMapName = mapName == null ? "" : mapName;
        this.currentModeName = modeName == null ? "" : modeName;
        this.carX = carX;
        this.carZ = carZ;
        this.carYaw = carYaw;
        this.leftMirrorOpen = leftMirrorOpen;
        this.rightMirrorOpen = rightMirrorOpen;

        this.timeTrialActive = timeTrialActive;
        this.timeTrialElapsed = Math.max(0f, timeTrialElapsed);
        this.timeTrialBest = Math.max(0f, timeTrialBest);
        this.timeTrialCountdown = Math.max(0f, timeTrialCountdown);
        this.checkpointIndex = checkpointIndex;
        this.checkpointTotal = Math.max(1, checkpointTotal);
        this.timeTrialFinished = timeTrialFinished;
        this.timeTrialNewBest = timeTrialNewBest;
        this.wrongWay = wrongWay;
        this.timeTrialGrade = timeTrialGrade == null ? "-" : timeTrialGrade;

        this.vehicleHealth = clamp(vehicleHealth, 0f, 1f);
        this.motorDamage = clamp(motorDamage, 0f, 1f);
        this.tireDamage = clamp(tireDamage, 0f, 1f);
        this.glassDamage = clamp(glassDamage, 0f, 1f);
        this.bodyDamage = clamp(bodyDamage, 0f, 1f);
        this.trafficDensity = trafficDensity == null ? "KAPALI" : trafficDensity;
        this.trafficNight = trafficNight;
        this.trafficActiveCount = Math.max(0, trafficActiveCount);
        this.trafficCollisionCount = Math.max(0, trafficCollisionCount);
        this.trafficNearMissCount = Math.max(0, trafficNearMissCount);
        this.missionTitle = missionTitle == null ? "-" : missionTitle;
        this.missionProgress = missionProgress == null ? "-" : missionProgress;
        this.missionMessage = missionMessage == null ? "" : missionMessage;
        this.missionCoinsFlash = Math.max(0, missionCoinsFlash);
        this.dailyMissionCompleted = Math.max(0, dailyMissionCompleted);

        this.raceActive = raceActive;
        this.dragRaceActive = dragRaceActive;
        this.raceStatus = raceStatus == null ? "" : raceStatus;
        this.raceLap = Math.max(0, raceLap);
        this.raceTotalLaps = Math.max(0, raceTotalLaps);
        this.racePosition = Math.max(0, racePosition);
        this.raceBotCount = Math.max(0, raceBotCount);
        this.raceFinished = raceFinished;
        this.raceEarnedCoins = Math.max(0, raceEarnedCoins);
        this.raceFinishRank = raceFinishRank == null ? "-" : raceFinishRank;
        this.raceCountdown = Math.max(0f, raceCountdown);

        this.policeActive = policeActive;
        this.policeStatus = policeStatus == null ? "" : policeStatus;
        this.wantedLevel = Math.max(0, wantedLevel);
        this.policeChaseTime = Math.max(0f, policeChaseTime);
        this.policeEscapeTimer = Math.max(0f, policeEscapeTimer);
        this.policeEscapeRequired = Math.max(0.1f, policeEscapeRequired);
        this.policeCaughtTimer = Math.max(0f, policeCaughtTimer);
        this.policeCaughtRequired = Math.max(0.1f, policeCaughtRequired);
        this.nearestPoliceDistance = Math.max(0f, nearestPoliceDistance);
        this.policeFinished = policeFinished;
        this.policeEscaped = policeEscaped;
        this.policeCaught = policeCaught;
        this.policeEarnedCoins = Math.max(0, policeEarnedCoins);

        this.diagDrawCalls = Math.max(0, diagDrawCalls);
        this.diagRenderedObjects = Math.max(0, diagRenderedObjects);
        this.diagTrafficSkipped = Math.max(0, diagTrafficSkipped);
        this.diagTrafficCulled = Math.max(0, diagTrafficCulled);
        this.diagGraphicsQuality = Math.max(0, diagGraphicsQuality);
        this.diagBuildTag = diagBuildTag == null ? "A61.3" : diagBuildTag;

        this.weatherText = weatherText == null ? "GUNDUZ / ACIK" : weatherText;
        this.weatherGripScale = Math.max(0f, weatherGripScale);
        this.headlightImportance = Math.max(0f, headlightImportance);
        this.weatherNight = weatherNight;
        this.weatherRain = weatherRain;
        this.weatherFog = weatherFog;

        this.navMapHalf = Math.max(80f, navMapHalf);
        this.navPointCount = Math.max(0, Math.min(NAV_MAX_POINTS, navPointCount));
        this.navRouteX = navRouteX;
        this.navRouteZ = navRouteZ;
        this.navRouteIcon = navRouteIcon;
        this.navRouteLabel = navRouteLabel == null ? "-" : navRouteLabel;
        this.navRouteDistance = Math.max(0f, navRouteDistance);
        this.navRouteCount = Math.max(0, Math.min(NAV_MAX_ROUTE, navRouteCount));
        setRoutePoint(0, navRouteX0, navRouteZ0);
        setRoutePoint(1, navRouteX1, navRouteZ1);
        setRoutePoint(2, navRouteX2, navRouteZ2);
        setRoutePoint(3, navRouteX3, navRouteZ3);
        setRoutePoint(4, navRouteX4, navRouteZ4);
        setRoutePoint(5, navRouteX5, navRouteZ5);
        setRoutePoint(6, navRouteX6, navRouteZ6);
        setRoutePoint(7, navRouteX7, navRouteZ7);
        setNavPoint(0, navIcon0, navX0, navZ0);
        setNavPoint(1, navIcon1, navX1, navZ1);
        setNavPoint(2, navIcon2, navX2, navZ2);
        setNavPoint(3, navIcon3, navX3, navZ3);
        setNavPoint(4, navIcon4, navX4, navZ4);
        setNavPoint(5, navIcon5, navX5, navZ5);
        setNavPoint(6, navIcon6, navX6, navZ6);
        setNavPoint(7, navIcon7, navX7, navZ7);
        setNavPoint(8, navIcon8, navX8, navZ8);
        setNavPoint(9, navIcon9, navX9, navZ9);
        setNavPoint(10, navIcon10, navX10, navZ10);
        setNavPoint(11, navIcon11, navX11, navZ11);

        this.playerLevel = Math.max(1, playerLevel);
        this.playerXp = Math.max(0, playerXp);
        this.playerNextXp = Math.max(1, playerNextXp);
        this.progressDaily = Math.max(0, progressDaily);
        this.progressWeekly = Math.max(0, progressWeekly);
        this.achievementCount = Math.max(0, achievementCount);
        this.rewardCrates = Math.max(0, rewardCrates);
        this.unlockedVehicles = Math.max(1, unlockedVehicles);
        this.unlockedMapTier = Math.max(1, unlockedMapTier);
        this.unlockText = unlockText == null ? "" : unlockText;
        this.progressionMessage = progressionMessage == null ? "" : progressionMessage;

        this.worldNearby = worldNearby;
        this.worldType = Math.max(0, worldType);
        this.worldTitle = worldTitle == null ? "" : worldTitle;
        this.worldSubtitle = worldSubtitle == null ? "" : worldSubtitle;
        this.worldReward = worldReward == null ? "" : worldReward;
        this.worldDistance = Math.max(0f, worldDistance);
        this.worldRequiredLevel = Math.max(1, worldRequiredLevel);
        this.worldActionText = worldActionText == null ? "" : worldActionText;
        this.worldActionMessage = worldActionMessage == null ? "" : worldActionMessage;
        this.worldPointCount = Math.max(0, Math.min(WORLD_MAX_POINTS, worldPointCount));
        setWorldPoint(0, worldType0, worldX0, worldZ0);
        setWorldPoint(1, worldType1, worldX1, worldZ1);
        setWorldPoint(2, worldType2, worldX2, worldZ2);
        setWorldPoint(3, worldType3, worldX3, worldZ3);
        setWorldPoint(4, worldType4, worldX4, worldZ4);
        setWorldPoint(5, worldType5, worldX5, worldZ5);
        setWorldPoint(6, worldType6, worldX6, worldZ6);
        setWorldPoint(7, worldType7, worldX7, worldZ7);
        setWorldPoint(8, worldType8, worldX8, worldZ8);
        setWorldPoint(9, worldType9, worldX9, worldZ9);
        setWorldPoint(10, worldType10, worldX10, worldZ10);
        setWorldPoint(11, worldType11, worldX11, worldZ11);
        setWorldPoint(12, worldType12, worldX12, worldZ12);
        setWorldPoint(13, worldType13, worldX13, worldZ13);
        setWorldPoint(14, worldType14, worldX14, worldZ14);
        setWorldPoint(15, worldType15, worldX15, worldZ15);

        this.questActive = questActive;
        this.questStep = Math.max(1, questStep);
        this.questTotal = Math.max(1, questTotal);
        this.questTitle = questTitle == null ? "" : questTitle;
        this.questObjective = questObjective == null ? "" : questObjective;
        this.questReward = questReward == null ? "" : questReward;
        this.questMessage = questMessage == null ? "" : questMessage;
        this.questTargetX = questTargetX;
        this.questTargetZ = questTargetZ;
        this.questTargetType = Math.max(0, questTargetType);
        this.questCompletedCount = Math.max(0, questCompletedCount);

        postInvalidateOnAnimation();
    }


    private void setNavPoint(int index, int icon, float x, float z) {
        if (index < 0 || index >= NAV_MAX_POINTS) return;
        navIcons[index] = icon;
        navX[index] = x;
        navZ[index] = z;
    }

    private void setRoutePoint(int index, float x, float z) {
        if (index < 0 || index >= NAV_MAX_ROUTE) return;
        navRouteXs[index] = x;
        navRouteZs[index] = z;
    }

    private void setWorldPoint(int index, int type, float x, float z) {
        if (index < 0 || index >= WORLD_MAX_POINTS) return;
        worldTypes[index] = type;
        worldX[index] = x;
        worldZ[index] = z;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        float scale = Math.max(1f, Math.min(w, h) / 720f);

        // A61.7: Sürüş/test alanında HUD sadeleştirildi. Sol tarafta üst üste
        // binen hava/hasar/trafik/debug kutuları artık serbest sürüşte çizilmez.
        boolean compactDriveHud = isCompactDriveHud();
        drawMiniMap(canvas, w, h, scale);
        drawMirrorPanelFrames(canvas, w, h, scale);
        drawSpeedPanel(canvas, w, h, scale);
        drawCompactDriveStatusPanel(canvas, w, h, scale);
        drawTaskAchievementTracker(canvas, w, h, scale);

        if (!compactDriveHud || hasVisibleDamage()) {
            drawDamagePanel(canvas, w, h, scale);
        }
        if (!compactDriveHud) {
            drawWeatherPanel(canvas, w, h, scale);
            drawProgressionPanel(canvas, w, h, scale);
            drawQuestChainPanel(canvas, w, h, scale);
            drawTrafficPanel(canvas, w, h, scale);
            drawDiagnosticPanel(canvas, w, h, scale);
        } else if (progressionMessage != null && progressionMessage.length() > 0) {
            drawCompactMessage(canvas, w, h, scale, progressionMessage, 0);
        }

        boolean showPoliceHud = policeActive;
        boolean showRaceHud = !showPoliceHud && raceActive;
        boolean showDriftHud = !showPoliceHud && !showRaceHud && isDriftModeHudVisible();
        boolean showMissionHud = !showPoliceHud && !showRaceHud && !showDriftHud;

        if (showMissionHud) {
            drawMissionPanel(canvas, w, h, scale);
            drawWorldInteractionPanel(canvas, w, h, scale);
        }
        if (showDriftHud) drawDriftPanel(canvas, w, h, scale);
        if (showRaceHud) drawRacePanel(canvas, w, h, scale);
        if (showPoliceHud) drawPolicePanel(canvas, w, h, scale);

        if (timeTrialActive) {
            drawTimeTrialCenter(canvas, w, h, scale);
            if (timeTrialFinished) {
                drawTimeTrialFinishOverlay(canvas, w, h, scale);
            }
        } else if (driftFinished) {
            drawDriftFinishOverlay(canvas, w, h, scale);
        }
        drawGarageTestDriveHud(canvas, w, h, scale);
        drawExpandedMapOverlay(canvas, w, h, scale);
        drawGameFeelPopup(canvas, w, h, scale);
    }

    public void setGameFeelPopup(String title, String subtitle, float alpha, float slide) {
        this.gameFeelPopupTitle = title == null ? "" : title;
        this.gameFeelPopupSubtitle = subtitle == null ? "" : subtitle;
        this.gameFeelPopupAlpha = clamp(alpha, 0f, 1f);
        this.gameFeelPopupSlide = clamp(slide, 0f, 1f);
        postInvalidateOnAnimation();
    }

    public void setTaskAchievementTracker(String title, String subtitle, String progress,
                                          float progress01, int pendingRewardCount, String status) {
        this.taskHudTitle = title == null ? "" : title;
        this.taskHudSubtitle = subtitle == null ? "" : subtitle;
        this.taskHudProgress = progress == null ? "" : progress;
        this.taskHudProgress01 = clamp(progress01, 0f, 1f);
        this.taskHudPendingRewardCount = Math.max(0, pendingRewardCount);
        this.taskHudStatus = status == null ? "" : status;
        postInvalidateOnAnimation();
    }


    public void setGarageTestDriveHud(boolean active, String title, String vehicle, String build, String status) {
        this.garageTestDriveActive = active;
        this.garageTestDriveTitle = title == null ? "" : title;
        this.garageTestDriveVehicle = vehicle == null ? "" : vehicle;
        this.garageTestDriveBuild = build == null ? "" : build;
        this.garageTestDriveStatus = status == null ? "" : status;
        postInvalidateOnAnimation();
    }

    public void setDrivingMapOverlayOpen(boolean open) {
        if (drivingMapOverlayOpen == open) return;
        drivingMapOverlayOpen = open;
        postInvalidateOnAnimation();
    }


    private void drawGarageTestDriveHud(Canvas canvas, int w, int h, float scale) {
        if (!garageTestDriveActive) return;
        boolean compact = w < 900 || h < 540;
        float panelW = Math.min(Math.max((compact ? 218f : 236f) * scale, w * (compact ? 0.235f : 0.265f)), w - 28f * scale);
        // A67.3: Test sürüşü HUD kartı telefon/tablet safe-area içinde kompakt kalır;
        // checkpoint/drift/polis HUD kartlarıyla üst üste binmez.
        float panelH = Math.max((compact ? 66f : 72f) * scale, h * (compact ? 0.076f : 0.084f));
        float left = w * 0.50f - panelW * 0.5f;
        float top = Math.max((compact ? 54f : 62f) * scale, h * (compact ? 0.066f : 0.078f));
        if (gameFeelPopupTitle != null && gameFeelPopupTitle.length() > 0 && gameFeelPopupAlpha > 0.02f) {
            top += (compact ? 54f : 66f) * scale;
        }
        top = Math.min(top, h - panelH - Math.max(92f * scale, h * 0.135f));
        panel.set(left, top, left + panelW, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(118, 2, 8, 20));
        canvas.drawRoundRect(panel, 18f * scale, 18f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f * scale);
        paint.setColor(Color.argb(210, 0, 220, 255));
        canvas.drawRoundRect(panel, 18f * scale, 18f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(246, 120, 235, 255));
        paint.setTextSize(9.2f * scale);
        canvas.drawText(trimHudText(garageTestDriveTitle, compact ? 26 : 34), panel.left + 12f * scale, panel.top + 16f * scale, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(12.0f * scale);
        canvas.drawText(trimHudText(garageTestDriveVehicle, compact ? 28 : 36), panel.left + 12f * scale, panel.top + 35f * scale, paint);

        paint.setColor(Color.argb(230, 235, 245, 255));
        paint.setTextSize(8.7f * scale);
        canvas.drawText(trimHudText(garageTestDriveBuild, compact ? 30 : 38), panel.left + 12f * scale, panel.top + 53f * scale, paint);

        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(Color.argb(240, 255, 220, 80));
        canvas.drawText(trimHudText(garageTestDriveStatus, compact ? 26 : 36), panel.right - 12f * scale, panel.top + 65f * scale, paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }

    private void drawGameFeelPopup(Canvas canvas, int w, int h, float scale) {
        if (gameFeelPopupTitle == null || gameFeelPopupTitle.length() == 0 || gameFeelPopupAlpha <= 0.02f) return;
        float popupW = Math.max(230f * scale, w * 0.27f);
        float popupH = Math.max(58f * scale, h * 0.074f);
        float left = w * 0.5f - popupW * 0.5f;
        float top = h * 0.16f - (1f - gameFeelPopupSlide) * 24f * scale;
        panel.set(left, top, left + popupW, top + popupH);

        int alpha = (int)(210f * gameFeelPopupAlpha);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(alpha, 8, 14, 22));
        canvas.drawRoundRect(panel, 18f * scale, 18f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f * scale);
        paint.setColor(Color.argb((int)(220f * gameFeelPopupAlpha), 80, 210, 255));
        canvas.drawRoundRect(panel, 18f * scale, 18f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.argb((int)(255f * gameFeelPopupAlpha), 255, 255, 255));
        paint.setTextSize(17f * scale);
        canvas.drawText(gameFeelPopupTitle, panel.centerX(), panel.top + 24f * scale, paint);

        if (gameFeelPopupSubtitle != null && gameFeelPopupSubtitle.length() > 0) {
            paint.setColor(Color.argb((int)(220f * gameFeelPopupAlpha), 190, 235, 255));
            paint.setTextSize(10.5f * scale);
            canvas.drawText(trimHudText(gameFeelPopupSubtitle, 42), panel.centerX(), panel.top + 43f * scale, paint);
        }
    }

    private boolean isCompactDriveHud() {
        return !timeTrialActive && !raceActive && !policeActive && !isDriftModeHudVisible();
    }


    private void drawTaskAchievementTracker(Canvas canvas, int w, int h, float scale) {
        if ((taskHudTitle == null || taskHudTitle.length() == 0) && taskHudPendingRewardCount <= 0) return;
        float margin = Math.max(14f * scale, w * 0.012f);
        float panelW = Math.min(Math.max(238f * scale, w * 0.285f), w - margin * 2f);
        if (w < 820) panelW = Math.min(panelW, w * 0.42f);
        float panelH = Math.max(74f * scale, h * 0.090f);
        float left = w - panelW - margin;
        float top = Math.max(92f * scale, h * 0.116f);
        if (rightMirrorOpen) {
            float mirrorTop = Math.max(48f, h * 0.065f);
            float mirrorH = Math.max(60f, h * 0.115f);
            top = Math.max(top, mirrorTop + mirrorH + 10f * scale);
        }
        float maxTop = h - Math.max(152f * scale, h * 0.22f) - panelH;
        if (top > maxTop) top = Math.max(70f * scale, maxTop);
        panel.set(left, top, left + panelW, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(taskHudPendingRewardCount > 0 ? 142 : 104, 4, 10, 20));
        canvas.drawRoundRect(panel, 17f * scale, 17f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f * scale);
        paint.setColor(taskHudPendingRewardCount > 0 ? Color.argb(215, 255, 220, 70) : Color.argb(170, 0, 210, 255));
        canvas.drawRoundRect(panel, 17f * scale, 17f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(taskHudPendingRewardCount > 0 ? Color.argb(245, 255, 235, 120) : Color.argb(235, 150, 230, 255));
        paint.setTextSize(9.4f * scale);
        String head = taskHudPendingRewardCount > 0 ? (taskHudTitle + "  •  " + taskHudPendingRewardCount + " ödül") : taskHudTitle;
        canvas.drawText(trimHudText(head, 38), panel.left + 10f * scale, panel.top + 15f * scale, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(11.2f * scale);
        canvas.drawText(trimHudText(taskHudSubtitle, 34), panel.left + 10f * scale, panel.top + 32f * scale, paint);

        float barLeft = panel.left + 10f * scale;
        float barTop = panel.top + 42f * scale;
        float barW = panelW - 20f * scale;
        float barH = 7f * scale;
        paint.setColor(Color.argb(95, 255, 255, 255));
        canvas.drawRoundRect(new RectF(barLeft, barTop, barLeft + barW, barTop + barH), 8f * scale, 8f * scale, paint);
        paint.setColor(taskHudPendingRewardCount > 0 ? Color.argb(235, 255, 210, 70) : Color.argb(235, 0, 210, 255));
        canvas.drawRoundRect(new RectF(barLeft, barTop, barLeft + barW * taskHudProgress01, barTop + barH), 8f * scale, 8f * scale, paint);

        paint.setTextSize(8.4f * scale);
        paint.setColor(Color.argb(226, 232, 242, 255));
        canvas.drawText(trimHudText(taskHudProgress, 24), panel.left + 10f * scale, panel.top + 64f * scale, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        paint.setColor(taskHudPendingRewardCount > 0 ? Color.argb(240, 120, 255, 170) : Color.argb(220, 235, 245, 255));
        canvas.drawText(trimHudText(taskHudStatus, 32), panel.right - 10f * scale, panel.top + 64f * scale, paint);
        paint.setTextAlign(Paint.Align.LEFT);
    }


    private boolean isDriftModeHudVisible() {
        return "DriftMode".equals(currentModeName) || ("DriftMode".equals(currentModeName) && (drifting || driftScore > 0 || driftFinished));
    }

    private boolean hasVisibleDamage() {
        return vehicleHealth < 0.985f || motorDamage > 0.02f || tireDamage > 0.02f || glassDamage > 0.02f || bodyDamage > 0.02f;
    }

    private void drawCompactDriveStatusPanel(Canvas canvas, int w, int h, float scale) {
        float mapSize = Math.max(122f * scale, Math.min(w, h) * 0.178f);
        float left = Math.max(14f * scale, w * 0.012f);
        float top = Math.max(70f * scale, h * 0.085f) + mapSize + 52f * scale;
        float panelW = Math.max(170f * scale, w * 0.19f);
        float panelH = Math.max(48f * scale, h * 0.058f);
        panel.set(left, top, left + panelW, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(92, 0, 0, 0));
        canvas.drawRoundRect(panel, 13f * scale, 13f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.4f * scale);
        paint.setColor(Color.argb(145, 120, 210, 255));
        canvas.drawRoundRect(panel, 13f * scale, 13f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(9.4f * scale);
        paint.setColor(Color.WHITE);
        canvas.drawText("Vites " + gear + "  |  Can " + percent(vehicleHealth),
                panel.left + 8f * scale, panel.top + 15f * scale, paint);

        paint.setColor(Color.argb(225, 235, 245, 255));
        String weather = weatherText == null ? "" : weatherText;
        canvas.drawText(trimHudText(weather + "  Yol " + (int)(weatherGripScale * 100f) + "%", 30),
                panel.left + 8f * scale, panel.top + 31f * scale, paint);

        paint.setColor(Color.argb(225, 255, 215, 80));
        canvas.drawText("LVL " + playerLevel + "  XP " + playerXp + "/" + playerNextXp,
                panel.left + 8f * scale, panel.top + 45f * scale, paint);
    }

    private void drawCompactMessage(Canvas canvas, int w, int h, float scale, String message, int line) {
        if (message == null || message.length() == 0) return;
        float y = Math.max(78f * scale, h * 0.095f) + line * 22f * scale;
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(13f * scale);
        paint.setColor(Color.argb(235, 120, 255, 170));
        canvas.drawText(trimHudText(message, 48), w * 0.5f, y, paint);
    }

    private void drawMiniMap(Canvas canvas, int w, int h, float scale) {
        // A67.4: Yuvarlak mini harita. Eski kare panel yerine aynı nav/route
        // verileri dairesel mask içinde çizilir; gerçek harita asset'i eklenmez.
        RectF mini = DrivingHudLayoutSystem.miniMapRect(w, h, scale);
        panel.set(mini);
        float mapHalf = Math.max(80f, navMapHalf);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(88, 0, 0, 0));
        canvas.drawCircle(panel.centerX(), panel.centerY(), panel.width() * 0.5f + 5f * scale, paint);

        canvas.save();
        CircularMiniMapSystem.clipCircle(canvas, miniMapClipPath, panel);
        CircularMiniMapSystem.drawBase(canvas, paint, panel, scale, false);

        if (timeTrialActive) {
            drawTimeTrialMiniMap(canvas, panel, panel.width() * 0.82f, mapHalf, scale);
        }

        float cx = mapToMiniX(panel, panel.width() * 0.82f, mapHalf, carX);
        float cz = mapToMiniZ(panel, panel.width() * 0.82f, mapHalf, carZ);

        drawMiniRouteAndTargets(canvas, panel, mapHalf, cx, cz, scale, false);
        CircularMiniMapSystem.drawPlayer(canvas, paint, panel, mapHalf, carX, carZ, carYaw, scale, false);
        canvas.restore();

        CircularMiniMapSystem.drawOuterRing(canvas, paint, panel, scale, false);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(8.2f * scale);
        paint.setColor(Color.WHITE);
        String label = navRouteLabel == null ? "-" : navRouteLabel;
        canvas.drawText(label + " " + (int)navRouteDistance + "m", panel.centerX(), panel.bottom + 13f * scale, paint);
        paint.setTextSize(7.2f * scale);
        paint.setColor(Color.argb(220, 190, 225, 240));
        canvas.drawText("DOKUN: HARİTA", panel.centerX(), panel.top - 5f * scale, paint);
    }

    private void drawMiniRouteAndTargets(Canvas canvas, RectF rect, float mapHalf, float carMiniX, float carMiniZ, float scale, boolean expanded) {
        if (navRouteIcon > 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(expanded ? 4.2f * scale : 2.8f * scale);
            paint.setColor(Color.argb(225, 0, 220, 255));
            float px = carMiniX;
            float pz = carMiniZ;
            int count = Math.max(0, Math.min(navRouteCount, NAV_MAX_ROUTE));
            if (count <= 0) {
                float tx = mapToMiniX(rect, rect.width() * 0.82f, mapHalf, navRouteX);
                float tz = mapToMiniZ(rect, rect.width() * 0.82f, mapHalf, navRouteZ);
                canvas.drawLine(px, pz, tx, tz, paint);
                px = tx;
                pz = tz;
            } else {
                for (int i = 0; i < count; i++) {
                    float tx = mapToMiniX(rect, rect.width() * 0.82f, mapHalf, navRouteXs[i]);
                    float tz = mapToMiniZ(rect, rect.width() * 0.82f, mapHalf, navRouteZs[i]);
                    canvas.drawLine(px, pz, tx, tz, paint);
                    paint.setStyle(Paint.Style.FILL);
                    paint.setColor(Color.argb(230, 0, 220, 255));
                    canvas.drawCircle(tx, tz, expanded ? 4.0f * scale : 2.3f * scale, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(Color.argb(225, 0, 220, 255));
                    px = tx;
                    pz = tz;
                }
            }
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(expanded ? 2.0f * scale : 1.2f * scale);
            paint.setColor(Color.argb(230, 255, 255, 255));
            canvas.drawCircle(px, pz, expanded ? 12f * scale : 8f * scale, paint);
        }

        if (questActive && questTargetType > 0) {
            drawQuestMiniTarget(canvas, rect, rect.width() * 0.82f, mapHalf, carMiniX, carMiniZ, scale);
        }
        for (int i = 0; i < worldPointCount && i < WORLD_MAX_POINTS; i++) {
            drawWorldMiniMapIcon(canvas, rect, rect.width() * 0.82f, mapHalf, worldTypes[i], worldX[i], worldZ[i], scale);
        }
        for (int i = 0; i < navPointCount && i < NAV_MAX_POINTS; i++) {
            drawMiniMapIcon(canvas, rect, rect.width() * 0.82f, mapHalf, navIcons[i], navX[i], navZ[i], scale);
        }
    }

    private void drawExpandedMapOverlay(Canvas canvas, int w, int h, float scale) {
        if (!drivingMapOverlayOpen) return;
        RectF overlay = DrivingHudLayoutSystem.expandedMapPanelRect(w, h, scale);
        RectF close = DrivingHudLayoutSystem.expandedMapCloseRect(w, h, scale);
        InGameMapOverlaySystem.drawDim(canvas, paint, w, h);
        InGameMapOverlaySystem.drawPanelFrame(canvas, paint, overlay, scale);
        InGameMapOverlaySystem.drawHeader(
                canvas,
                paint,
                overlay,
                close,
                "BÜYÜK HARİTA",
                InGameMapOverlaySystem.subtitle(currentMapName, currentModeName, navRouteDistance),
                scale);

        float headerH = Math.max(68f * scale, overlay.height() * 0.16f);
        float mapSide = Math.min(overlay.width() - 36f * scale, overlay.height() - headerH - 26f * scale);
        RectF big = new RectF(
                overlay.centerX() - mapSide * 0.5f,
                overlay.top + headerH,
                overlay.centerX() + mapSide * 0.5f,
                overlay.top + headerH + mapSide);
        float mapHalf = Math.max(80f, navMapHalf);

        canvas.save();
        CircularMiniMapSystem.clipCircle(canvas, miniMapClipPath, big);
        CircularMiniMapSystem.drawBase(canvas, paint, big, scale, true);
        float cx = mapToMiniX(big, big.width() * 0.82f, mapHalf, carX);
        float cz = mapToMiniZ(big, big.width() * 0.82f, mapHalf, carZ);
        drawMiniRouteAndTargets(canvas, big, mapHalf, cx, cz, scale, true);
        CircularMiniMapSystem.drawPlayer(canvas, paint, big, mapHalf, carX, carZ, carYaw, scale, true);
        canvas.restore();
        CircularMiniMapSystem.drawOuterRing(canvas, paint, big, scale, true);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(10f * scale, overlay.height() * 0.025f));
        paint.setColor(Color.argb(225, 220, 235, 245));
        canvas.drawText("Konum: X " + (int)carX + " / Z " + (int)carZ + "  •  Ölçek: " + (int)mapHalf + "m",
                overlay.centerX(), Math.min(overlay.bottom - 14f * scale, big.bottom + 24f * scale), paint);
    }

    private float mapToMiniX(RectF rect, float inner, float mapHalf, float worldX) {
        return CircularMiniMapSystem.mapToX(rect, mapHalf, worldX);
    }

    private float mapToMiniZ(RectF rect, float inner, float mapHalf, float worldZ) {
        return CircularMiniMapSystem.mapToZ(rect, mapHalf, worldZ);
    }

    private void drawQuestMiniTarget(Canvas canvas, RectF rect, float inner, float mapHalf, float carMiniX, float carMiniZ, float scale) {
        float tx = mapToMiniX(rect, inner, mapHalf, questTargetX);
        float tz = mapToMiniZ(rect, inner, mapHalf, questTargetZ);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f * scale);
        paint.setColor(Color.argb(230, 255, 220, 70));
        canvas.drawLine(carMiniX, carMiniZ, tx, tz, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(245, 255, 220, 70));
        canvas.drawCircle(tx, tz, 6.8f * scale, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(7.2f * scale);
        paint.setColor(Color.argb(245, 0, 0, 0));
        canvas.drawText("Q", tx, tz + 2.5f * scale, paint);
    }

    private void drawWorldMiniMapIcon(Canvas canvas, RectF rect, float inner, float mapHalf, int type, float worldX, float worldZ, float scale) {
        if (type <= 0) return;
        float x = mapToMiniX(rect, inner, mapHalf, worldX);
        float z = mapToMiniZ(rect, inner, mapHalf, worldZ);

        int color;
        String text;
        if (type == 1) { color = Color.argb(240, 255, 90, 90); text = "R"; }
        else if (type == 2) { color = Color.argb(240, 255, 190, 70); text = "D"; }
        else if (type == 3) { color = Color.argb(240, 90, 220, 255); text = "T"; }
        else if (type == 4) { color = Color.argb(240, 255, 70, 70); text = "P"; }
        else if (type == 5) { color = Color.argb(240, 130, 180, 255); text = "G"; }
        else if (type == 6) { color = Color.argb(240, 160, 230, 255); text = "A"; }
        else if (type == 7) { color = Color.argb(240, 120, 255, 160); text = "R"; }
        else if (type == 8) { color = Color.argb(240, 255, 230, 90); text = "F"; }
        else if (type == 9) { color = Color.argb(240, 210, 130, 255); text = "E"; }
        else { color = Color.argb(235, 255, 255, 255); text = "?"; }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(x, z, 5.4f * scale, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(6.0f * scale);
        paint.setColor(Color.argb(245, 0, 0, 0));
        canvas.drawText(text, x, z + 2.1f * scale, paint);
    }

    private void drawMiniMapIcon(Canvas canvas, RectF rect, float inner, float mapHalf, int icon, float worldX, float worldZ, float scale) {
        if (icon <= 0) return;
        float x = mapToMiniX(rect, inner, mapHalf, worldX);
        float z = mapToMiniZ(rect, inner, mapHalf, worldZ);
        int color;
        String text;
        if (icon == 1) { color = Color.argb(240, 95, 220, 255); text = "G"; }
        else if (icon == 2) { color = Color.argb(240, 255, 205, 70); text = "GA"; }
        else if (icon == 3) { color = Color.argb(240, 120, 255, 130); text = "Y"; }
        else if (icon == 4) { color = Color.argb(240, 255, 95, 95); text = "T"; }
        else if (icon == 5) { color = Color.argb(240, 90, 135, 255); text = "P"; }
        else { color = Color.WHITE; text = "?"; }

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        canvas.drawCircle(x, z, 5.8f * scale, paint);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(6.6f * scale);
        paint.setColor(Color.argb(245, 0, 0, 0));
        canvas.drawText(text, x, z + 2.3f * scale, paint);
    }

    private void drawTimeTrialMiniMap(Canvas canvas, RectF panel, float inner, float mapHalf, float scale) {
        float[] xs = new float[] {0f, 0f, 48f, 92f, 48f, 0f};
        float[] zs = new float[] {-115f, -32f, 42f, 35f, 102f, 184f};

        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < xs.length; i++) {
            float px = panel.centerX() + clamp(xs[i] / mapHalf, -1f, 1f) * inner * 0.5f;
            float pz = panel.centerY() + clamp(zs[i] / mapHalf, -1f, 1f) * inner * 0.5f;
            boolean next = i == Math.min(checkpointIndex, xs.length - 1);
            paint.setColor(next ? Color.argb(255, 255, 230, 60) : Color.argb(160, 80, 255, 140));
            canvas.drawCircle(px, pz, next ? 4.5f * scale : 3f * scale, paint);
        }
    }

    private void drawMirrorPanelFrames(Canvas canvas, int w, int h, float scale) {
        if (!leftMirrorOpen && !rightMirrorOpen) return;

        float panelW = Math.max(120f, w * 0.18f);
        float panelH = Math.max(60f, h * 0.115f);
        float marginX = Math.max(10f, w * 0.012f);
        float top = Math.max(48f, h * 0.065f);

        if (leftMirrorOpen) {
            drawMirrorFrame(canvas, marginX, top, panelW, panelH, "SOL", scale);
        }
        if (rightMirrorOpen) {
            drawMirrorFrame(canvas, w - marginX - panelW, top, panelW, panelH, "SAG", scale);
        }
    }

    private void drawMirrorFrame(Canvas canvas, float x, float y, float w, float h, String title, float scale) {
        panel.set(x, y, x + w, y + h);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(58, 0, 0, 0));
        canvas.drawRoundRect(panel, 14f * scale, 14f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.4f * scale);
        paint.setColor(Color.argb(210, 0, 210, 255));
        canvas.drawRoundRect(panel, 14f * scale, 14f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(10f * scale);
        canvas.drawText(title, panel.left + 8f * scale, panel.top + 14f * scale, paint);
    }

    private void drawSpeedPanel(Canvas canvas, int w, int h, float scale) {
        // Arka plan kutusu yok: hız yazısı mini map'in tam altına hizalanır.
        RectF mapRect = DrivingHudLayoutSystem.miniMapRect(w, h, scale);
        float centerX = mapRect.centerX();
        float baseY = DrivingHudLayoutSystem.speedTextY(w, h, scale);

        int speedColor = speedColor(speedKmh);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(speedColor);
        paint.setTextSize(31f * scale);
        canvas.drawText(String.format(Locale.US, "%.0f", speedKmh), centerX, baseY, paint);

        paint.setTextSize(11f * scale);
        paint.setColor(speedColor);
        canvas.drawText("KM/H", centerX, baseY + 17f * scale, paint);

        if (diagBuildTag != null && diagBuildTag.indexOf("A67.5") >= 0) {
            paint.setTextSize(8.5f * scale);
            paint.setColor(Color.argb(205, 190, 235, 255));
            canvas.drawText(trimHudText(diagBuildTag, 18), centerX, baseY + 30f * scale, paint);
        }
    }


    private void drawDamagePanel(Canvas canvas, int w, int h, float scale) {
        float panelW = Math.max(150f * scale, w * 0.18f);
        float panelH = Math.max(78f * scale, h * 0.105f);
        float left = Math.max(12f * scale, w * 0.012f);
        float top = Math.max(210f * scale, h * 0.250f);
        panel.set(left, top, left + panelW, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(88, 0, 0, 0));
        canvas.drawRoundRect(panel, 16f * scale, 16f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f * scale);
        paint.setColor(damageColor(vehicleHealth));
        canvas.drawRoundRect(panel, 16f * scale, 16f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(10.5f * scale);
        paint.setColor(Color.WHITE);
        canvas.drawText("ARAC HASARI", panel.left + 10f * scale, panel.top + 15f * scale, paint);

        drawBar(canvas, panel.left + 10f * scale, panel.top + 25f * scale, panelW - 20f * scale, 8f * scale, vehicleHealth, damageColor(vehicleHealth));

        paint.setTextSize(9.5f * scale);
        paint.setColor(Color.argb(230, 235, 245, 255));
        canvas.drawText("CAN " + percent(vehicleHealth), panel.left + 10f * scale, panel.top + 48f * scale, paint);
        canvas.drawText("MTR " + percent(1f - motorDamage) + "  LSTK " + percent(1f - tireDamage), panel.left + 10f * scale, panel.top + 63f * scale, paint);

        if (vehicleHealth < 0.32f || motorDamage > 0.62f || tireDamage > 0.62f) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(22f * scale);
            paint.setColor(Color.argb(235, 255, 72, 72));
            canvas.drawText("KRITIK HASAR!", w * 0.5f, h * 0.18f, paint);
        }
    }

    private void drawBar(Canvas canvas, float x, float y, float width, float height, float value, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 255, 255, 255));
        RectF bg = new RectF(x, y, x + width, y + height);
        canvas.drawRoundRect(bg, height * 0.5f, height * 0.5f, paint);
        paint.setColor(color);
        RectF fg = new RectF(x, y, x + width * clamp(value, 0f, 1f), y + height);
        canvas.drawRoundRect(fg, height * 0.5f, height * 0.5f, paint);
    }

    private static String percent(float value) {
        int p = Math.max(0, Math.min(100, Math.round(value * 100f)));
        return p + "%";
    }

    private static int damageColor(float health) {
        if (health > 0.72f) return Color.argb(245, 80, 255, 150);
        if (health > 0.38f) return Color.argb(245, 255, 210, 70);
        return Color.argb(245, 255, 80, 70);
    }



    private void drawWeatherPanel(Canvas canvas, int w, int h, float scale) {
        float panelW = Math.max(168f * scale, w * 0.19f);
        float panelH = Math.max(54f * scale, h * 0.066f);
        float left = Math.max(14f * scale, w * 0.012f);
        float top = Math.max(70f * scale, h * 0.085f) + Math.max(104f * scale, Math.min(w, h) * 0.160f);
        panel.set(left, top, left + panelW, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(weatherNight ? 96 : 72, 0, 0, 0));
        canvas.drawRoundRect(panel, 13f * scale, 13f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * scale);
        int strokeColor = weatherRain ? Color.argb(180, 90, 170, 255)
                : weatherFog ? Color.argb(170, 205, 215, 225)
                : weatherNight ? Color.argb(170, 120, 145, 255)
                : Color.argb(150, 255, 220, 90);
        paint.setColor(strokeColor);
        canvas.drawRoundRect(panel, 13f * scale, 13f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(9.6f * scale);
        paint.setColor(Color.WHITE);
        canvas.drawText("HAVA: " + weatherText, panel.left + 8f * scale, panel.top + 15f * scale, paint);
        paint.setColor(Color.argb(225, 235, 245, 255));
        canvas.drawText("GRIP " + (int)(weatherGripScale * 100f) + "%  FAR " + (int)(headlightImportance * 100f) + "%",
                panel.left + 8f * scale, panel.top + 33f * scale, paint);
        String warn = weatherRain ? "YAGMUR: yol tutusu azalir" : weatherFog ? "SIS: gorus azalir" : weatherNight ? "GECE: far onemli" : "GUNDUZ: normal surus";
        canvas.drawText(warn, panel.left + 8f * scale, panel.top + 48f * scale, paint);
    }


    private void drawProgressionPanel(Canvas canvas, int w, int h, float scale) {
        float panelW = Math.max(174f * scale, w * 0.205f);
        float panelH = Math.max(68f * scale, h * 0.082f);
        float left = Math.max(14f * scale, w * 0.012f);
        float top = Math.max(70f * scale, h * 0.085f)
                + Math.max(122f * scale, Math.min(w, h) * 0.178f)
                + Math.max(58f * scale, h * 0.070f);
        panel.set(left, top, left + panelW, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(82, 0, 0, 0));
        canvas.drawRoundRect(panel, 13f * scale, 13f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * scale);
        paint.setColor(Color.argb(160, 255, 205, 80));
        canvas.drawRoundRect(panel, 13f * scale, 13f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(9.4f * scale);
        paint.setColor(Color.WHITE);
        canvas.drawText("LVL " + playerLevel + "  XP " + playerXp + "/" + playerNextXp,
                panel.left + 8f * scale, panel.top + 15f * scale, paint);

        float barLeft = panel.left + 8f * scale;
        float barTop = panel.top + 21f * scale;
        float barW = panelW - 16f * scale;
        float barH = 6f * scale;
        paint.setColor(Color.argb(95, 255, 255, 255));
        canvas.drawRect(barLeft, barTop, barLeft + barW, barTop + barH, paint);
        paint.setColor(Color.argb(230, 255, 195, 65));
        canvas.drawRect(barLeft, barTop, barLeft + barW * clamp(playerXp / (float)playerNextXp, 0f, 1f), barTop + barH, paint);

        paint.setTextSize(8.4f * scale);
        paint.setColor(Color.argb(230, 235, 245, 255));
        canvas.drawText("GÜN " + progressDaily + "/3  HAFTA " + progressWeekly + "/10  BAŞ " + achievementCount + "/8",
                panel.left + 8f * scale, panel.top + 40f * scale, paint);
        canvas.drawText("KASA " + rewardCrates + "  " + unlockText,
                panel.left + 8f * scale, panel.top + 54f * scale, paint);
        if (progressionMessage != null && progressionMessage.length() > 0) {
            paint.setColor(Color.argb(245, 120, 255, 150));
            canvas.drawText(progressionMessage, panel.left + 8f * scale, panel.top + 66f * scale, paint);
        }
    }

    private void drawTrafficPanel(Canvas canvas, int w, int h, float scale) {
        float panelW = Math.max(150f * scale, w * 0.18f);
        float panelH = Math.max(58f * scale, h * 0.078f);
        float left = Math.max(12f * scale, w * 0.012f);
        float top = Math.max(296f * scale, h * 0.355f);
        panel.set(left, top, left + panelW, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(74, 0, 0, 0));
        canvas.drawRoundRect(panel, 15f * scale, 15f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f * scale);
        paint.setColor(trafficNight ? Color.argb(190, 120, 170, 255) : Color.argb(170, 255, 220, 90));
        canvas.drawRoundRect(panel, 15f * scale, 15f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(10f * scale);
        paint.setColor(Color.WHITE);
        canvas.drawText("TRAFIK " + trafficDensity + "  " + (trafficNight ? "GECE" : "GUNDUZ"),
                panel.left + 10f * scale, panel.top + 15f * scale, paint);

        paint.setColor(Color.argb(225, 235, 245, 255));
        canvas.drawText("ARAC: " + trafficActiveCount + "  CARP: " + trafficCollisionCount,
                panel.left + 10f * scale, panel.top + 34f * scale, paint);
        canvas.drawText("YAKIN GECIS: " + trafficNearMissCount,
                panel.left + 10f * scale, panel.top + 50f * scale, paint);
    }


    private void drawMissionPanel(Canvas canvas, int w, int h, float scale) {
        if (missionTitle == null || missionTitle.length() == 0 || "-".equals(missionTitle)) {
            return;
        }
        float panelW = Math.max(178f * scale, w * 0.22f);
        float panelH = Math.max(72f * scale, h * 0.092f);
        float left = Math.max(12f * scale, w * 0.012f);
        float top = Math.max(364f * scale, h * 0.435f);
        panel.set(left, top, left + panelW, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(78, 0, 0, 0));
        canvas.drawRoundRect(panel, 16f * scale, 16f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f * scale);
        paint.setColor(Color.argb(190, 90, 255, 180));
        canvas.drawRoundRect(panel, 16f * scale, 16f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(10.5f * scale);
        paint.setColor(Color.WHITE);
        canvas.drawText("GOREV: " + missionTitle, panel.left + 10f * scale, panel.top + 16f * scale, paint);

        paint.setColor(Color.argb(230, 235, 245, 255));
        canvas.drawText("ILERLEME: " + missionProgress, panel.left + 10f * scale, panel.top + 35f * scale, paint);
        canvas.drawText("GUNLUK: " + dailyMissionCompleted + "/3", panel.left + 10f * scale, panel.top + 53f * scale, paint);

        if (missionMessage.length() > 0) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(20f * scale);
            paint.setColor(Color.argb(240, 120, 255, 165));
            canvas.drawText(missionMessage, w * 0.5f, h * 0.245f, paint);
        }
    }


    private void drawDriftPanel(Canvas canvas, int w, int h, float scale) {
        float panelW = Math.min(Math.max(256f * scale, w * 0.305f), w - 28f * scale);
        float panelH = Math.max(118f * scale, h * 0.145f);
        float right = w - Math.max(12f * scale, w * 0.012f);
        float top = Math.max(82f * scale, h * 0.105f);
        panel.set(right - panelW, top, right, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(118, 2, 8, 18));
        canvas.drawRoundRect(panel, 18f * scale, 18f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f * scale);
        paint.setColor(drifting ? Color.argb(235, 120, 255, 170) : Color.argb(205, 0, 210, 255));
        canvas.drawRoundRect(panel, 18f * scale, 18f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(9.4f * scale);
        paint.setColor(Color.argb(238, 120, 235, 255));
        canvas.drawText("DRIFT SKOR • FINAL QA", panel.left + 11f * scale, panel.top + 16f * scale, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(17.5f * scale);
        canvas.drawText(String.valueOf(driftScore), panel.left + 11f * scale, panel.top + 40f * scale, paint);

        paint.setTextSize(9.2f * scale);
        paint.setColor(Color.argb(236, 255, 225, 90));
        canvas.drawText("Hedef " + driftTargetText() + "  |  En iyi " + bestScore, panel.left + 11f * scale, panel.top + 58f * scale, paint);

        float barLeft = panel.left + 11f * scale;
        float barTop = panel.top + 67f * scale;
        float barW = panel.width() - 22f * scale;
        float nextTarget = Math.max(1f, DriftScoreSystem.targetForNextGrade(driftScore));
        float progress = driftScore >= DriftScoreSystem.TARGET_LEGEND ? 1f : clamp(driftScore / nextTarget, 0f, 1f);
        drawBar(canvas, barLeft, barTop, barW, 7f * scale, progress, drifting ? Color.argb(240, 120, 255, 170) : Color.argb(235, 0, 210, 255));

        paint.setColor(Color.argb(230, 235, 245, 255));
        paint.setTextSize(9.2f * scale);
        canvas.drawText("Combo x" + oneDecimal(driftMultiplier) + "  Seviye " + driftComboLevel + "  Süre " + (int)driftTimeRemaining + " sn",
                panel.left + 11f * scale, panel.top + 92f * scale, paint);

        paint.setColor(insideDriftZone ? Color.argb(235, 120, 255, 170) : Color.argb(230, 255, 150, 80));
        String driftHint = insideDriftZone ? "BONUS ALAN • kontrollü kaymayı sürdür" : "Hız + açı + kontrol ile skor kazan";
        if (driftCrashPenalty > 0) driftHint = "Ceza -" + driftCrashPenalty + " • combo güvenli sür";
        canvas.drawText(driftHint, panel.left + 11f * scale, panel.top + 109f * scale, paint);

        if (drifting) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(24f * scale);
            paint.setColor(Color.argb(240, 120, 255, 165));
            String callout = DriftScoreSystem.comboLabel(driftMultiplier, driftComboLevel);
            canvas.drawText(callout, w * 0.5f, h * 0.25f, paint);
        } else if (insideDriftZone) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(17f * scale);
            paint.setColor(Color.argb(220, 120, 255, 170));
            canvas.drawText("BONUS ALAN", w * 0.5f, h * 0.25f, paint);
        }
    }

    private String driftTargetText() {
        return DriftScoreSystem.targetText(driftScore);
    }


    private void drawRacePanel(Canvas canvas, int w, int h, float scale) {
        boolean checkpointRace = !dragRaceActive && raceTotalLaps >= 4;
        if (checkpointRace) {
            drawCheckpointRacePanel(canvas, w, h, scale);
            return;
        }

        float panelW = Math.max((dragRaceActive ? 246f : 178f) * scale,
                w * (dragRaceActive ? 0.300f : 0.22f));
        float panelH = Math.max((dragRaceActive ? 108f : 76f) * scale,
                h * (dragRaceActive ? 0.135f : 0.098f));
        float right = w - Math.max(12f * scale, w * 0.012f);
        float top = Math.max(82f * scale, h * 0.105f);
        panel.set(right - panelW, top, right, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawRoundRect(panel, 16f * scale, 16f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f * scale);
        paint.setColor(Color.argb(215, 255, 170, 45));
        canvas.drawRoundRect(panel, 16f * scale, 16f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(10.5f * scale);
        paint.setColor(Color.WHITE);
        canvas.drawText(dragRaceActive ? "DRAG YARISI 400M" : "YARIS", panel.left + 10f * scale, panel.top + 16f * scale, paint);

        paint.setColor(Color.argb(230, 235, 245, 255));
        if (dragRaceActive) {
            canvas.drawText("Mesafe " + raceLap + "/" + raceTotalLaps + " m  Ödül +" + raceEarnedCoins,
                    panel.left + 10f * scale, panel.top + 36f * scale, paint);
            canvas.drawText(trimHudText(raceStatus, 48), panel.left + 10f * scale, panel.top + 57f * scale, paint);
            paint.setColor(Color.argb(228, 255, 225, 90));
            canvas.drawText("Rakipli 400m | Sıra #" + racePosition + "/" + (raceBotCount + 1), panel.left + 10f * scale, panel.top + 78f * scale, paint);
            paint.setColor(Color.argb(220, 180, 225, 255));
            canvas.drawText("Altın hedef: 00:13.50  |  Yeşil ışıkta kalk", panel.left + 10f * scale, panel.top + 96f * scale, paint);
        } else {
            canvas.drawText("TUR " + raceLap + "/" + raceTotalLaps + "  SIRA #" + racePosition + "/" + (raceBotCount + 1),
                    panel.left + 10f * scale, panel.top + 36f * scale, paint);
            canvas.drawText(raceStatus, panel.left + 10f * scale, panel.top + 56f * scale, paint);
        }

        drawGenericRaceCountdownAndFinish(canvas, w, h, scale, false);
    }

    private void drawCheckpointRacePanel(Canvas canvas, int w, int h, float scale) {
        float panelW = Math.max(292f * scale, w * 0.335f);
        float panelH = Math.max(122f * scale, h * 0.148f);
        float right = w - Math.max(12f * scale, w * 0.012f);
        float top = Math.max(80f * scale, h * 0.102f);
        panel.set(right - panelW, top, right, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(132, 2, 10, 24));
        canvas.drawRoundRect(panel, 18f * scale, 18f * scale, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.8f * scale);
        paint.setColor(Color.argb(220, 0, 224, 255));
        canvas.drawRoundRect(panel, 18f * scale, 18f * scale, paint);

        float pad = 12f * scale;
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(Math.max(10.5f * scale, h * 0.014f));
        paint.setColor(Color.WHITE);
        canvas.drawText("CHECKPOINT YARIŞI", panel.left + pad, panel.top + 17f * scale, paint);

        paint.setTextSize(Math.max(9.4f * scale, h * 0.0122f));
        paint.setColor(Color.argb(230, 235, 245, 255));
        canvas.drawText(trimHudText(raceStatus, 54), panel.left + pad, panel.top + 39f * scale, paint);

        int targetDistance = CheckpointRaceSystem.distanceToTarget(carX, carZ, CheckpointRaceSystem.safeTargetIndex(raceLap)) < 1f
                ? 0 : (int)CheckpointRaceSystem.distanceToTarget(carX, carZ, CheckpointRaceSystem.safeTargetIndex(raceLap));
        String targetLabel = CheckpointRaceSystem.targetLabel(raceLap);
        paint.setColor(Color.argb(238, 255, 225, 85));
        canvas.drawText(targetLabel + "  •  " + targetDistance + " m", panel.left + pad, panel.top + 60f * scale, paint);

        paint.setColor(Color.argb(230, 130, 255, 190));
        canvas.drawText("Sıra #" + racePosition + "/" + (raceBotCount + 1) + "  •  " + CheckpointRaceSystem.medalTargetsText(),
                panel.left + pad, panel.top + 81f * scale, paint);

        drawCheckpointProgressBar(canvas, panel.left + pad, panel.top + 94f * scale, panel.width() - pad * 2f, 7f * scale, scale);
        drawTargetDirectionChip(canvas, w, h, scale, targetDistance);
        drawGenericRaceCountdownAndFinish(canvas, w, h, scale, true);
    }

    private void drawCheckpointProgressBar(Canvas canvas, float x, float y, float width, float height, float scale) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(105, 255, 255, 255));
        RectF bar = new RectF(x, y, x + width, y + height);
        canvas.drawRoundRect(bar, height * 0.5f, height * 0.5f, paint);
        float total = Math.max(1f, raceTotalLaps);
        float progress = Math.max(0f, Math.min(1f, (raceLap - 1f) / total));
        RectF fill = new RectF(x, y, x + width * progress, y + height);
        paint.setColor(Color.argb(230, 0, 230, 255));
        canvas.drawRoundRect(fill, height * 0.5f, height * 0.5f, paint);
    }

    private void drawTargetDirectionChip(Canvas canvas, int w, int h, float scale, int distanceMeters) {
        if (!raceActive || raceFinished || raceCountdown > 0f || dragRaceActive) return;
        int targetIndex = CheckpointRaceSystem.safeTargetIndex(raceLap);
        float tx = CheckpointRaceSystem.targetX(targetIndex);
        float tz = CheckpointRaceSystem.targetZ(targetIndex);
        float dx = tx - carX;
        float dz = tz - carZ;
        float targetYaw = (float)Math.atan2(dx, dz);
        float delta = targetYaw - carYaw;
        while (delta > Math.PI) delta -= (float)Math.PI * 2f;
        while (delta < -Math.PI) delta += (float)Math.PI * 2f;

        String arrow = "↑";
        if (delta > 0.62f) arrow = "→";
        else if (delta < -0.62f) arrow = "←";
        else if (Math.abs(delta) > 2.45f) arrow = "↓";

        float chipW = Math.max(178f * scale, w * 0.185f);
        float chipH = Math.max(42f * scale, h * 0.053f);
        float cx = w * 0.5f - chipW * 0.5f;
        float cy = Math.max(88f * scale, h * 0.122f);
        RectF chip = new RectF(cx, cy, cx + chipW, cy + chipH);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(122, 0, 10, 22));
        canvas.drawRoundRect(chip, 16f * scale, 16f * scale, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.2f * scale);
        paint.setColor(Color.argb(205, 255, 220, 80));
        canvas.drawRoundRect(chip, 16f * scale, 16f * scale, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(Math.max(20f * scale, h * 0.027f));
        paint.setColor(Color.argb(245, 255, 230, 75));
        canvas.drawText(arrow, chip.left + 29f * scale, chip.centerY() + 7f * scale, paint);
        paint.setTextSize(Math.max(10.5f * scale, h * 0.014f));
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(236, 245, 250, 255));
        canvas.drawText("SONRAKİ HEDEF", chip.left + 55f * scale, chip.top + 17f * scale, paint);
        paint.setColor(Color.argb(232, 135, 255, 205));
        canvas.drawText(distanceMeters + " m", chip.left + 55f * scale, chip.top + 34f * scale, paint);
    }

    private void drawGenericRaceCountdownAndFinish(Canvas canvas, int w, int h, float scale, boolean checkpointRace) {
        if (raceCountdown > 0f) {
            float cardW = Math.max(170f * scale, w * 0.19f);
            float cardH = Math.max(118f * scale, h * 0.145f);
            RectF countdownCard = new RectF(w * 0.5f - cardW * 0.5f, h * 0.30f - cardH * 0.5f, w * 0.5f + cardW * 0.5f, h * 0.30f + cardH * 0.5f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(150, 0, 0, 0));
            canvas.drawRoundRect(countdownCard, 22f * scale, 22f * scale, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f * scale);
            paint.setColor(Color.argb(232, 255, 220, 80));
            canvas.drawRoundRect(countdownCard, 22f * scale, 22f * scale, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(13f * scale);
            paint.setColor(Color.argb(230, 235, 245, 255));
            canvas.drawText(checkpointRace ? "KONTROL KİLİTLİ" : "BAŞLANGIÇ", countdownCard.centerX(), countdownCard.top + 28f * scale, paint);
            paint.setTextSize(46f * scale);
            paint.setColor(Color.argb(245, 255, 220, 80));
            canvas.drawText(String.valueOf((int)Math.ceil(raceCountdown)), countdownCard.centerX(), countdownCard.centerY() + 20f * scale, paint);
        }

        if (raceFinished) {
            float cardW = Math.max(330f * scale, w * 0.42f);
            float cardH = Math.max((checkpointRace ? 168f : 118f) * scale, h * (checkpointRace ? 0.205f : 0.145f));
            RectF result = new RectF(w * 0.5f - cardW * 0.5f, h * 0.315f - cardH * 0.5f, w * 0.5f + cardW * 0.5f, h * 0.315f + cardH * 0.5f);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(164, 2, 12, 26));
            canvas.drawRoundRect(result, 24f * scale, 24f * scale, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2.2f * scale);
            paint.setColor(Color.argb(232, 90, 255, 170));
            canvas.drawRoundRect(result, 24f * scale, 24f * scale, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(Math.max(20f * scale, h * 0.026f));
            paint.setColor(Color.argb(245, 120, 255, 165));
            canvas.drawText(checkpointRace ? "YARIŞ TAMAMLANDI" : "YARIŞ BİTTİ", result.centerX(), result.top + 34f * scale, paint);
            paint.setTextSize(Math.max(12.5f * scale, h * 0.016f));
            paint.setColor(Color.argb(236, 235, 245, 255));
            canvas.drawText(trimHudText(raceStatus, 58), result.centerX(), result.top + 65f * scale, paint);
            paint.setColor(Color.argb(242, 255, 220, 80));
            canvas.drawText("Rank " + raceFinishRank + "  •  Ödül +" + raceEarnedCoins + " coin", result.centerX(), result.top + 93f * scale, paint);
            if (checkpointRace) {
                paint.setColor(Color.argb(224, 160, 225, 255));
                canvas.drawText("Tekrar oyna: gaz  •  Garaja/Mod seçimi: menü", result.centerX(), result.top + 122f * scale, paint);
                paint.setColor(Color.argb(210, 235, 245, 255));
                canvas.drawText("Ödül tek sefer verildi; sonuç ekranı tekrar coin çoğaltmaz.", result.centerX(), result.top + 147f * scale, paint);
            }
        }
    }


    private void drawPolicePanel(Canvas canvas, int w, int h, float scale) {
        float panelW = Math.max(252f * scale, w * 0.30f);
        float panelH = Math.max(128f * scale, h * 0.158f);
        float right = w - Math.max(12f * scale, w * 0.012f);
        float top = raceActive ? Math.max(168f * scale, h * 0.215f) : Math.max(82f * scale, h * 0.105f);
        panel.set(right - panelW, top, right, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(150, 8, 6, 12));
        canvas.drawRoundRect(panel, 18f * scale, 18f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f * scale);
        paint.setColor(Color.argb(230, 255, 65, 65));
        canvas.drawRoundRect(panel, 18f * scale, 18f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(11f * scale);
        paint.setColor(Color.WHITE);
        canvas.drawText("POLİS KOVALAMACA", panel.left + 12f * scale, panel.top + 18f * scale, paint);

        float caught01 = clamp(policeCaughtTimer / Math.max(0.1f, policeCaughtRequired), 0f, 1f);
        float safe01 = clamp(1f - caught01, 0f, 1f);
        paint.setColor(Color.argb(240, 255, 220, 70));
        canvas.drawText(starsForWanted(wantedLevel) + "  Mesafe " + (int)nearestPoliceDistance + "m  Risk %" + Math.round(caught01 * 100f),
                panel.left + 12f * scale, panel.top + 39f * scale, paint);

        drawMiniProgress(canvas, panel.left + 12f * scale, panel.top + 51f * scale, panelW - 24f * scale, 7f * scale, safe01, Color.argb(230, 90, 230, 130));
        drawMiniProgress(canvas, panel.left + 12f * scale, panel.top + 67f * scale, panelW - 24f * scale, 8f * scale, caught01, Color.argb(235, 255, 70, 70));

        paint.setColor(Color.argb(225, 235, 245, 255));
        paint.setTextSize(10f * scale);
        canvas.drawText("Kalan " + Math.max(0, (int)(policeEscapeRequired - policeChaseTime)) + "sn   Güvenlik %" + Math.round(safe01 * 100f),
                panel.left + 12f * scale, panel.top + 94f * scale, paint);

        boolean critical = caught01 >= 0.72f || policeCaught;
        paint.setColor(critical ? Color.argb(245, 255, 92, 92) : Color.argb(240, 120, 255, 165));
        paint.setTextSize(10f * scale);
        canvas.drawText(trimHudText(policeStatus, 46), panel.left + 12f * scale, panel.top + 112f * scale, paint);

        if (policeFinished) {
            float fw = Math.max(340f * scale, w * 0.43f);
            float fh = Math.max(126f * scale, h * 0.158f);
            RectF resultCard = new RectF(w * 0.5f - fw * 0.5f, h * 0.285f, w * 0.5f + fw * 0.5f, h * 0.285f + fh);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(214, 5, 8, 18));
            canvas.drawRoundRect(resultCard, 22f * scale, 22f * scale, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2f * scale);
            paint.setColor(policeEscaped ? Color.argb(235, 95, 255, 150) : Color.argb(235, 255, 80, 80));
            canvas.drawRoundRect(resultCard, 22f * scale, 22f * scale, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(21f * scale);
            paint.setColor(policeEscaped ? Color.argb(245, 120, 255, 165) : Color.argb(245, 255, 90, 90));
            canvas.drawText(policeEscaped ? "KAÇIŞ BAŞARILI" : "YAKALANDIN", resultCard.centerX(), resultCard.top + 34f * scale, paint);
            paint.setTextSize(12f * scale);
            paint.setColor(Color.argb(235, 235, 245, 255));
            String result = "Süre " + (int)policeChaseTime + "sn  •  " + starsForWanted(wantedLevel) + "  •  +" + policeEarnedCoins + " coin";
            canvas.drawText(result, resultCard.centerX(), resultCard.top + 62f * scale, paint);
            paint.setTextSize(10f * scale);
            canvas.drawText("Yakalanma riski %" + Math.round(caught01 * 100f) + "  •  ödül tek sefer güvenli", resultCard.centerX(), resultCard.top + 86f * scale, paint);
            canvas.drawText(policeEscaped ? "Gaz: tekrar kaçış  |  Menü: mod merkezi" : "Gaz: tekrar dene  |  Garajda onar", resultCard.centerX(), resultCard.top + 108f * scale, paint);
        }
    }

    private void drawMiniProgress(Canvas canvas, float x, float y, float w, float h, float value01, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(72, 255, 255, 255));
        RectF bg = new RectF(x, y, x + w, y + h);
        canvas.drawRoundRect(bg, h * 0.5f, h * 0.5f, paint);
        paint.setColor(color);
        RectF fg = new RectF(x, y, x + w * clamp(value01, 0f, 1f), y + h);
        canvas.drawRoundRect(fg, h * 0.5f, h * 0.5f, paint);
    }

    private static String starsForWanted(int wanted) {
        int w = Math.max(0, Math.min(5, wanted));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i < w ? '★' : '☆');
        return sb.toString();
    }


    private void drawQuestChainPanel(Canvas canvas, int w, int h, float scale) {
        if (!questActive && (questMessage == null || questMessage.length() == 0)) return;

        float panelW = Math.max(230f * scale, w * 0.30f);
        float panelH = Math.max(84f * scale, h * 0.105f);
        float left = w - panelW - Math.max(14f * scale, w * 0.012f);
        float top = Math.max(142f * scale, h * 0.18f);
        panel.set(left, top, left + panelW, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(118, 0, 0, 0));
        canvas.drawRoundRect(panel, 16f * scale, 16f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.9f * scale);
        paint.setColor(Color.argb(190, 255, 220, 70));
        canvas.drawRoundRect(panel, 16f * scale, 16f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.argb(245, 255, 235, 120));
        paint.setTextSize(9.6f * scale);
        canvas.drawText("KARİYER GÖREVİ  " + questStep + "/" + questTotal,
                panel.left + 9f * scale, panel.top + 15f * scale, paint);

        paint.setColor(Color.WHITE);
        paint.setTextSize(10.8f * scale);
        canvas.drawText(questTitle, panel.left + 9f * scale, panel.top + 32f * scale, paint);

        paint.setColor(Color.argb(225, 235, 245, 255));
        paint.setTextSize(8.4f * scale);
        canvas.drawText(trimHudText(questObjective, 38), panel.left + 9f * scale, panel.top + 49f * scale, paint);
        canvas.drawText("Ödül: " + questReward, panel.left + 9f * scale, panel.top + 64f * scale, paint);

        if (questMessage != null && questMessage.length() > 0) {
            paint.setColor(Color.argb(245, 120, 255, 170));
            canvas.drawText(trimHudText(questMessage, 42), panel.left + 9f * scale, panel.top + 79f * scale, paint);
        }
    }

    private String trimHudText(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private void drawWorldInteractionPanel(Canvas canvas, int w, int h, float scale) {
        boolean show = worldNearby || (worldActionMessage != null && worldActionMessage.length() > 0);
        if (!show) return;
        if (isCompactDriveHud()) {
            drawCompactWorldInteractionPanel(canvas, w, h, scale);
            return;
        }

        float panelW = Math.max(250f * scale, w * 0.36f);
        float panelH = Math.max(112f * scale, h * 0.145f);
        float left = w * 0.5f - panelW * 0.5f;
        float top = Math.max(84f * scale, h * 0.13f);
        panel.set(left, top, left + panelW, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(185, 8, 12, 18));
        canvas.drawRoundRect(panel, 20f * scale, 20f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.4f * scale);
        paint.setColor(worldColor(worldType, 210));
        canvas.drawRoundRect(panel, 20f * scale, 20f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);
        paint.setTextSize(14f * scale);
        String title = worldNearby ? worldTitle : "AÇIK DÜNYA";
        canvas.drawText(title, panel.left + 14f * scale, panel.top + 24f * scale, paint);

        paint.setTextSize(10.5f * scale);
        paint.setColor(Color.argb(225, 235, 245, 255));
        String sub = worldNearby ? worldSubtitle : "";
        canvas.drawText(sub, panel.left + 14f * scale, panel.top + 44f * scale, paint);

        paint.setColor(Color.argb(235, 255, 220, 90));
        String reward = worldNearby ? worldReward : "";
        canvas.drawText("Ödül: " + reward + "   Mesafe: " + (int)worldDistance + "m",
                panel.left + 14f * scale, panel.top + 63f * scale, paint);

        paint.setColor(Color.argb(245, 120, 255, 170));
        String action = worldNearby ? ("ETK: " + worldActionText + "  |  LVL " + worldRequiredLevel) : worldActionMessage;
        canvas.drawText(action, panel.left + 14f * scale, panel.top + 84f * scale, paint);

        if (worldActionMessage != null && worldActionMessage.length() > 0) {
            paint.setColor(Color.argb(245, 255, 255, 255));
            canvas.drawText(worldActionMessage, panel.left + 14f * scale, panel.top + 103f * scale, paint);
        }
    }

    private void drawCompactWorldInteractionPanel(Canvas canvas, int w, int h, float scale) {
        float panelW = Math.min(Math.max(220f * scale, w * 0.30f), w * 0.52f);
        float panelH = Math.max(48f * scale, h * 0.060f);
        float left = w * 0.5f - panelW * 0.5f;
        float top = Math.max(72f * scale, h * 0.105f);
        panel.set(left, top, left + panelW, top + panelH);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(132, 8, 12, 18));
        canvas.drawRoundRect(panel, 16f * scale, 16f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.6f * scale);
        paint.setColor(worldColor(worldType, 165));
        canvas.drawRoundRect(panel, 16f * scale, 16f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(10.4f * scale);
        paint.setColor(Color.WHITE);
        String title = worldNearby ? worldTitle : worldActionMessage;
        canvas.drawText(trimHudText(title, 34), panel.centerX(), panel.top + 18f * scale, paint);

        paint.setTextSize(8.8f * scale);
        paint.setColor(Color.argb(230, 120, 255, 170));
        String action = worldNearby ? ("ETK: " + worldActionText + "  " + (int)worldDistance + "m") : worldActionMessage;
        canvas.drawText(trimHudText(action, 38), panel.centerX(), panel.top + 36f * scale, paint);
    }

    private int worldColor(int type, int alpha) {
        if (type == 1) return Color.argb(alpha, 90, 255, 120);
        if (type == 2) return Color.argb(alpha, 195, 90, 255);
        if (type == 3) return Color.argb(alpha, 70, 210, 255);
        if (type == 4) return Color.argb(alpha, 90, 130, 255);
        if (type == 5) return Color.argb(alpha, 255, 210, 70);
        if (type == 6) return Color.argb(alpha, 255, 145, 45);
        if (type == 7) return Color.argb(alpha, 255, 75, 75);
        if (type == 8) return Color.argb(alpha, 70, 245, 210);
        if (type == 9) return Color.argb(alpha, 255, 65, 210);
        return Color.argb(alpha, 255, 255, 255);
    }

    private String worldIconText(int type) {
        if (type == 1) return "Y";
        if (type == 2) return "D";
        if (type == 3) return "Z";
        if (type == 4) return "P";
        if (type == 5) return "G";
        if (type == 6) return "GA";
        if (type == 7) return "T";
        if (type == 8) return "B";
        if (type == 9) return "E";
        return "?";
    }

    private void drawDiagnosticPanel(Canvas canvas, int w, int h, float scale) {
        float panelW = Math.max(150f * scale, w * 0.17f);
        float panelH = Math.max(52f * scale, h * 0.064f);
        float right = w - Math.max(12f * scale, w * 0.012f);
        float bottom = h - Math.max(12f * scale, h * 0.014f);
        panel.set(right - panelW, bottom - panelH, right, bottom);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(62, 0, 0, 0));
        canvas.drawRoundRect(panel, 12f * scale, 12f * scale, paint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.3f * scale);
        paint.setColor(Color.argb(110, 130, 220, 255));
        canvas.drawRoundRect(panel, 12f * scale, 12f * scale, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(8.8f * scale);
        paint.setColor(Color.argb(220, 235, 245, 255));
        canvas.drawText("DIAG " + diagBuildTag + "  Q" + diagGraphicsQuality + "  FPS " + fps,
                panel.left + 8f * scale, panel.top + 14f * scale, paint);
        canvas.drawText("DC " + diagDrawCalls + "  OBJ " + diagRenderedObjects,
                panel.left + 8f * scale, panel.top + 30f * scale, paint);
        canvas.drawText("TRF SKIP " + diagTrafficSkipped + "  CULL " + diagTrafficCulled,
                panel.left + 8f * scale, panel.top + 45f * scale, paint);
    }

    private void drawTimeTrialCenter(Canvas canvas, int w, int h, float scale) {
        if (timeTrialCountdown > 0f && !timeTrialFinished) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(72f * scale);
            paint.setColor(Color.argb(235, 255, 255, 255));
            String text = timeTrialCountdown <= 0.85f ? "GO!" : String.valueOf((int) Math.ceil(timeTrialCountdown));
            canvas.drawText(text, w * 0.5f, h * 0.38f, paint);
        }

        if (wrongWay && !timeTrialFinished) {
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(34f * scale);
            paint.setColor(Color.argb(255, 255, 70, 70));
            canvas.drawText("YANLIS YON!", w * 0.5f, h * 0.23f, paint);
        }
    }

    private void drawDriftFinishOverlay(Canvas canvas, int w, int h, float scale) {
        String title = "ÇIKILDI".equals(rank) ? "DRIFT BIRAKILDI" : (driftNewBest ? "DRIFT REKORU!" : "DRIFT TAMAMLANDI");
        String rankCode = DriftScoreSystem.rankCode(rank);
        drawFinishBox(canvas, w, h, scale, title,
                "SKOR: " + driftScore + "  |  EN IYI: " + Math.max(driftScore, bestScore),
                "RANK: " + rankCode + " / " + rank + "  |  COMBO x" + driftComboLevel,
                "ODUL: +" + earnedCoins + " COIN  |  TEK SEFER GUVENLI",
                "GAZ: TEKRAR  |  MENU: MOD SEC / GARAJ");
    }

    private void drawTimeTrialFinishOverlay(Canvas canvas, int w, int h, float scale) {
        drawFinishBox(canvas, w, h, scale, "ZAMAN YARISI", "SURE: " + formatTime(timeTrialElapsed),
                "RANK: " + timeTrialGrade, "EN IYI: " + (timeTrialBest > 0f ? formatTime(timeTrialBest) : "--:--.--")
                        + (timeTrialNewBest ? "  YENI!" : ""), "GAZ'A BAS: TEKRAR");
    }

    private void drawFinishBox(Canvas canvas, int w, int h, float scale, String title, String l1, String l2, String l3, String l4) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(180, 0, 0, 0));
        canvas.drawRect(0f, 0f, w, h, paint);

        float boxW = Math.min(w * 0.78f, 520f * scale);
        float boxH = 285f * scale;
        panel.set((w - boxW) * 0.5f, (h - boxH) * 0.5f, (w + boxW) * 0.5f, (h + boxH) * 0.5f);

        paint.setColor(Color.argb(232, 20, 24, 30));
        canvas.drawRoundRect(panel, 28f * scale, 28f * scale, paint);

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(Color.WHITE);
        paint.setTextSize(28f * scale);
        canvas.drawText(title, panel.centerX(), panel.top + 48f * scale, paint);

        paint.setTextSize(20f * scale);
        canvas.drawText(l1, panel.centerX(), panel.top + 92f * scale, paint);
        canvas.drawText(l2, panel.centerX(), panel.top + 125f * scale, paint);
        canvas.drawText(l3, panel.centerX(), panel.top + 158f * scale, paint);
        canvas.drawText(l4, panel.centerX(), panel.top + 196f * scale, paint);
    }


    private static int speedColor(float kmh) {
        if (kmh < 30f) {
            return Color.argb(235, 245, 245, 245);
        }
        if (kmh < 90f) {
            return Color.argb(255, 0, 210, 255);
        }
        if (kmh < 140f) {
            return Color.argb(255, 255, 190, 60);
        }
        return Color.argb(255, 255, 72, 72);
    }

    private static String oneDecimal(float value) {
        return String.format(Locale.US, "%.1f", value);
    }

    private static String formatTime(float seconds) {
        if (seconds <= 0f) return "00:00.00";
        int minutes = (int) (seconds / 60f);
        float remain = seconds - minutes * 60f;
        return String.format(Locale.US, "%02d:%05.2f", minutes, remain);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
