package com.arabaoyunu.mode;

/**
 * A62.8: Tek merkezli mod koordinatörü.
 *
 * Amaç: Bir mod aktifken diğer modların HUD/ödül/update akışının yanlışlıkla
 * çalışmasını engellemek. Bu sınıf bilinçli olarak Android bağımsızdır; AIDE
 * içinde kolay derlenir ve yeni modlar eklendiğinde tek kapıdan kontrol sağlar.
 */
public final class GameModeCoordinator {

    public static final int MODE_FREE = 0;
    public static final int MODE_TIME_TRIAL = 1;
    public static final int MODE_CHECKPOINT_RACE = 2;
    public static final int MODE_DRAG_RACE = 3;
    public static final int MODE_DRIFT = 4;
    public static final int MODE_POLICE = 5;
    public static final int MODE_UNKNOWN = 99;

    private int activeType = MODE_FREE;
    private int selectedMode;
    private int selectedMap;
    private int sessionId;
    private String activeName = "FreeDriveMode";
    private String lastRewardKey = "";
    private boolean resultPanelLocked;
    private boolean testDriveSession;

    public void beginMode(String modeName, int selectedMode, int selectedMap, boolean testDriveSession) {
        this.activeName = modeName == null ? "NONE" : modeName;
        this.activeType = typeForName(this.activeName);
        this.selectedMode = selectedMode;
        this.selectedMap = selectedMap;
        this.testDriveSession = testDriveSession;
        this.sessionId++;
        this.lastRewardKey = "";
        this.resultPanelLocked = false;
    }

    public void update(String modeName, int selectedMode, int selectedMap, boolean testDriveSession) {
        String safeName = modeName == null ? "NONE" : modeName;
        int nextType = typeForName(safeName);
        if (nextType != activeType || selectedMode != this.selectedMode || selectedMap != this.selectedMap
                || testDriveSession != this.testDriveSession) {
            beginMode(safeName, selectedMode, selectedMap, testDriveSession);
        } else {
            this.activeName = safeName;
        }
    }

    public int getSessionId() { return sessionId; }
    public int getActiveType() { return activeType; }
    public String getActiveName() { return activeName; }

    public boolean isFreeDrive() { return activeType == MODE_FREE; }
    public boolean isTimeTrial() { return activeType == MODE_TIME_TRIAL; }
    public boolean isCheckpointRace() { return activeType == MODE_CHECKPOINT_RACE; }
    public boolean isDragRace() { return activeType == MODE_DRAG_RACE; }
    public boolean isDrift() { return activeType == MODE_DRIFT; }
    public boolean isPoliceChase() { return activeType == MODE_POLICE; }
    public boolean isRaceLike() { return isTimeTrial() || isCheckpointRace() || isDragRace(); }
    public boolean isCompetitiveMode() { return isRaceLike() || isDrift() || isPoliceChase(); }

    public boolean allowsFreeDriveEconomy(boolean simpleOpenFieldDrive, boolean isGarageTestDrive) {
        return simpleOpenFieldDrive && isFreeDrive() && !isGarageTestDrive;
    }

    public boolean allowsMissionSystem(boolean simpleOpenFieldDrive) {
        return !simpleOpenFieldDrive && isFreeDrive();
    }

    public boolean allowsWorldInteraction(boolean simpleOpenFieldDrive) {
        return !simpleOpenFieldDrive && isFreeDrive();
    }

    public boolean allowsTraffic(boolean simpleOpenFieldDrive) {
        // A63.4: Trafik artik serbest test pisti ve polis kacis alaninda aktif.
        // Drag/drift kapali kalir; checkpoint icin simdilik kapali tutularak rota karismasi engellenir.
        return (isFreeDrive() && simpleOpenFieldDrive) || isPoliceChase() || isTimeTrial();
    }

    public boolean shouldShowMissionHud(boolean simpleOpenFieldDrive) {
        return isFreeDrive() && simpleOpenFieldDrive;
    }

    public boolean claimRewardOnce(String rewardKey) {
        String safe = rewardKey == null ? "" : rewardKey;
        if (safe.length() == 0) return false;
        String scoped = sessionId + "/" + safe;
        if (scoped.equals(lastRewardKey)) return false;
        lastRewardKey = scoped;
        return true;
    }

    public boolean lockResultPanelOnce() {
        if (resultPanelLocked) return false;
        resultPanelLocked = true;
        return true;
    }

    public String hudChannel() {
        if (isPoliceChase()) return "POLICE";
        if (isDrift()) return "DRIFT";
        if (isDragRace()) return "DRAG";
        if (isCheckpointRace()) return "CHECKPOINT";
        if (isTimeTrial()) return "TIME";
        return "FREE";
    }

    public static int typeForName(String modeName) {
        if ("RaceMode".equals(modeName)) return MODE_CHECKPOINT_RACE;
        if ("DragRaceMode".equals(modeName)) return MODE_DRAG_RACE;
        if ("DriftMode".equals(modeName)) return MODE_DRIFT;
        if ("PoliceChaseMode".equals(modeName)) return MODE_POLICE;
        if ("TimeTrialMode".equals(modeName)) return MODE_TIME_TRIAL;
        if ("FreeDriveMode".equals(modeName) || "FreeDriveTestMode".equals(modeName)) return MODE_FREE;
        return MODE_UNKNOWN;
    }
}
