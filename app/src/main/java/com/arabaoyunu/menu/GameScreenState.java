package com.arabaoyunu.menu;

import com.arabaoyunu.vehicle.VehicleCatalog;
import com.arabaoyunu.garage.GarageShowroomSystem;
import com.arabaoyunu.mode.CheckpointRaceSystem;

/**
 * ArabaOyunu_17: Ana menu/lobi ve mod secim durumu.
 * Thread-safe basit state: UI thread yazar, GL render thread okur.
 */
public final class GameScreenState {

    public static final int SCREEN_MAIN_MENU = 0;
    public static final int SCREEN_GARAGE = 1;
    public static final int SCREEN_MODES = 2;
    public static final int SCREEN_SETTINGS = 3;
    public static final int SCREEN_MAPS = 4;
    public static final int SCREEN_DRIVE = 5;
    public static final int SCREEN_CAREER_START = 6;
    public static final int SCREEN_QUESTS = 7;
    public static final int SCREEN_CAREER = 8;

    public static final int MODE_FREE_DRIVE = 0;
    public static final int MODE_TIME_TRIAL = 1;
    public static final int MODE_DRIFT = 2;
    public static final int MODE_RACE_LOCKED = 3; // ArabaOyunu_30: artık gerçek yarış modu olarak kullanılır.
    public static final int MODE_POLICE_CHASE = 4;
    public static final int MODE_DRAG_RACE = 5;

    public static final int MAP_OPEN_FIELD = 0;
    public static final int MAP_CITY = 1;
    public static final int MAP_HIGHWAY = 2;
    public static final int MAP_DRIFT_PARK = 3;
    // ArabaOyunu_61_6: GLB harita slotları geçici kapalı/pasif tutulur.
    public static final int MAP_OPEN_WORLD = 4;
    public static final int MAP_SECOND_NEW = 5;
    public static final int MAP_MAX_ID = MAP_SECOND_NEW;

    // ArabaOyunu_54: Garaj artik tek kalabalik ekran degil.
    // 0: arac secme, 1: modifiye ana, 2: performans, 3: tuning, 4: gorsel liste, 5: tek parca/zoom duzenleme.
    public static final int GARAGE_MODE_SELECT = 0;
    public static final int GARAGE_MODE_MODIFY_HOME = 1;
    public static final int GARAGE_MODE_PERFORMANCE = 2;
    public static final int GARAGE_MODE_TUNING = 3;
    public static final int GARAGE_MODE_VISUAL = 4;
    public static final int GARAGE_MODE_VISUAL_EDIT = 5;
    public static final int GARAGE_MODE_BUY_CONFIRM = 6;

    public static final int VISUAL_GROUP_BODY = 0;
    public static final int VISUAL_GROUP_WHEELS = 1;
    public static final int VISUAL_GROUP_GLASS = 2;
    public static final int VISUAL_GROUP_LIGHTS = 3;
    public static final int VISUAL_GROUP_PLATE = 4;

    private int screen = SCREEN_MAIN_MENU;
    private int selectedVehicleIndex = 0;
    private int selectedMode = MODE_FREE_DRIVE;
    private int selectedMap = MAP_OPEN_FIELD;
    private int selectedCheckpointRoute = CheckpointRaceSystem.ROUTE_MEDIUM;
    private int garageMode = GARAGE_MODE_SELECT;
    private int selectedVisualModType = 0;
    private int selectedVisualGroup = VISUAL_GROUP_BODY;
    private int garageComparisonBaseVehicleIndex = 0;
    private int pendingBuyVehicleIndex = -1;
    private int visualEditOriginalValue = 0;
    private int visualEditPreviewValue = 0;
    private boolean visualEditActive = false;
    private float previewYaw;
    private float previewZoom = GarageShowroomSystem.DEFAULT_ZOOM;
    private boolean returnToDriveAfterMenu;

    // ArabaOyunu_59: showroom -> test surusu gecisi ve aktif test oturumu.
    private boolean garageToTestDriveTransition;
    private long garageToTestDriveTransitionStartMs;
    private int garageToTestDriveVehicleIndex = -1;
    private boolean testDriveSessionActive;
    private boolean testDriveLaunchRequest;
    private boolean testDriveRestartRequest;
    private int lastTestDriveReward;
    private String lastTestDriveResult = "";

    private long lastChangeMs = System.currentTimeMillis();

    public synchronized int getScreen() {
        return screen;
    }

    public synchronized void setScreen(int screen) {
        if (screen < SCREEN_MAIN_MENU || screen > SCREEN_CAREER) screen = SCREEN_MAIN_MENU;
        int previous = this.screen;
        this.screen = screen;
        if (screen == SCREEN_GARAGE && previous != SCREEN_GARAGE) {
            garageComparisonBaseVehicleIndex = selectedVehicleIndex;
            pendingBuyVehicleIndex = -1;
        }
        if (screen != SCREEN_GARAGE) {
            garageMode = GARAGE_MODE_SELECT;
            pendingBuyVehicleIndex = -1;
            selectedVisualModType = 0;
            selectedVisualGroup = VISUAL_GROUP_BODY;
            visualEditActive = false;
            visualEditOriginalValue = 0;
            visualEditPreviewValue = 0;
        }
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized boolean isDriving() {
        return screen == SCREEN_DRIVE;
    }

    public synchronized boolean isMenuLike() {
        return screen != SCREEN_DRIVE;
    }

    public synchronized int getSelectedVehicleIndex() {
        return selectedVehicleIndex;
    }

    public synchronized void setSelectedVehicleIndex(int index) {
        if (index < 0) index = 0;
        if (index >= VehicleCatalog.count()) index = VehicleCatalog.count() - 1;
        selectedVehicleIndex = index;
        lastChangeMs = System.currentTimeMillis();
    }


    public synchronized int getGarageMode() {
        return garageMode;
    }

    public synchronized void setGarageMode(int mode) {
        if (mode < GARAGE_MODE_SELECT || mode > GARAGE_MODE_BUY_CONFIRM) {
            mode = GARAGE_MODE_SELECT;
        }
        garageMode = mode;
        if (mode != GARAGE_MODE_VISUAL_EDIT) {
            visualEditActive = false;
        }
        if (mode != GARAGE_MODE_BUY_CONFIRM) {
            pendingBuyVehicleIndex = -1;
        }
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized void beginBuyConfirm(int vehicleIndex) {
        if (vehicleIndex < 0) vehicleIndex = 0;
        if (vehicleIndex >= VehicleCatalog.count()) vehicleIndex = VehicleCatalog.count() - 1;
        pendingBuyVehicleIndex = vehicleIndex;
        garageMode = GARAGE_MODE_BUY_CONFIRM;
        visualEditActive = false;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized int getPendingBuyVehicleIndex() {
        return pendingBuyVehicleIndex;
    }

    public synchronized void cancelBuyConfirm() {
        pendingBuyVehicleIndex = -1;
        garageMode = GARAGE_MODE_SELECT;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized int getGarageComparisonBaseVehicleIndex() {
        return garageComparisonBaseVehicleIndex;
    }

    public synchronized void markVehicleSelectedForComparison() {
        garageComparisonBaseVehicleIndex = selectedVehicleIndex;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized int getSelectedVisualModType() {
        return selectedVisualModType;
    }

    public synchronized int getSelectedVisualGroup() {
        return selectedVisualGroup;
    }

    public synchronized void setSelectedVisualGroup(int group) {
        if (group < VISUAL_GROUP_BODY || group > VISUAL_GROUP_PLATE) group = VISUAL_GROUP_BODY;
        selectedVisualGroup = group;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized void setSelectedVisualModType(int type) {
        if (type < 0) type = 0;
        if (selectedVisualModType != type) {
            visualEditActive = false;
        }
        selectedVisualModType = type;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized void beginVisualEdit(int type, int currentValue) {
        if (type < 0) type = 0;
        selectedVisualModType = type;
        visualEditOriginalValue = Math.max(0, currentValue);
        visualEditPreviewValue = Math.max(0, currentValue);
        visualEditActive = true;
        garageMode = GARAGE_MODE_VISUAL_EDIT;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized boolean isVisualEditActiveFor(int type) {
        return visualEditActive && garageMode == GARAGE_MODE_VISUAL_EDIT && selectedVisualModType == type;
    }

    public synchronized int getVisualEditPreviewValue() {
        return visualEditPreviewValue;
    }

    public synchronized int getVisualEditOriginalValue() {
        return visualEditOriginalValue;
    }

    public synchronized int getVisualEditValueOr(int savedValue, int type) {
        if (isVisualEditActiveFor(type)) {
            return visualEditPreviewValue;
        }
        return savedValue;
    }

    public synchronized void setVisualEditPreviewValue(int value) {
        visualEditPreviewValue = Math.max(0, value);
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized boolean isVisualEditDirty() {
        return visualEditActive && visualEditPreviewValue != visualEditOriginalValue;
    }

    public synchronized void finishVisualEdit() {
        visualEditOriginalValue = visualEditPreviewValue;
        visualEditActive = false;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized int getSelectedMode() {
        return selectedMode;
    }

    public synchronized void setSelectedMode(int mode) {
        if (mode < MODE_FREE_DRIVE || mode > MODE_DRAG_RACE) mode = MODE_FREE_DRIVE;
        selectedMode = mode;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized int getSelectedMap() {
        return selectedMap;
    }

    public synchronized void setSelectedMap(int map) {
        if (map < MAP_OPEN_FIELD || map > MAP_MAX_ID) map = MAP_OPEN_FIELD;
        selectedMap = map;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized int getSelectedCheckpointRoute() {
        return CheckpointRaceSystem.sanitizeRouteId(selectedCheckpointRoute);
    }

    public synchronized void setSelectedCheckpointRoute(int routeId) {
        selectedCheckpointRoute = CheckpointRaceSystem.sanitizeRouteId(routeId);
        CheckpointRaceSystem.setActiveRoute(selectedCheckpointRoute);
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized float getPreviewYaw() {
        return previewYaw;
    }

    public synchronized void addPreviewYaw(float delta) {
        previewYaw += delta;
        if (previewYaw > 6.2831853f || previewYaw < -6.2831853f) {
            previewYaw = previewYaw % 6.2831853f;
        }
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized void setPreviewYaw(float yaw) {
        previewYaw = yaw;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized float getPreviewZoom() {
        return GarageShowroomSystem.clampZoom(previewZoom);
    }

    public synchronized void addPreviewZoom(float delta) {
        previewZoom = GarageShowroomSystem.clampZoom(previewZoom + delta);
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized void setPreviewZoom(float zoom) {
        previewZoom = GarageShowroomSystem.clampZoom(zoom);
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized void resetPreviewCamera() {
        previewYaw = 0f;
        previewZoom = GarageShowroomSystem.DEFAULT_ZOOM;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized void setReturnToDriveAfterMenu(boolean value) {
        returnToDriveAfterMenu = value;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized boolean consumeReturnToDriveAfterMenu() {
        boolean value = returnToDriveAfterMenu;
        returnToDriveAfterMenu = false;
        lastChangeMs = System.currentTimeMillis();
        return value;
    }

    public synchronized void beginGarageToTestDriveTransition(int vehicleIndex) {
        if (vehicleIndex < 0) vehicleIndex = 0;
        if (vehicleIndex >= VehicleCatalog.count()) vehicleIndex = VehicleCatalog.count() - 1;
        selectedVehicleIndex = vehicleIndex;
        selectedMode = MODE_FREE_DRIVE;
        selectedMap = MAP_OPEN_FIELD;
        garageMode = GARAGE_MODE_SELECT;
        pendingBuyVehicleIndex = -1;
        visualEditActive = false;
        garageToTestDriveVehicleIndex = vehicleIndex;
        garageToTestDriveTransition = true;
        garageToTestDriveTransitionStartMs = System.currentTimeMillis();
        lastChangeMs = garageToTestDriveTransitionStartMs;
    }

    public synchronized boolean isGarageToTestDriveTransitionActive() {
        return garageToTestDriveTransition;
    }

    public synchronized float getGarageToTestDriveTransitionProgress() {
        if (!garageToTestDriveTransition) return 0f;
        long elapsed = System.currentTimeMillis() - garageToTestDriveTransitionStartMs;
        float progress = elapsed / 1050f;
        if (progress < 0f) return 0f;
        if (progress > 1f) return 1f;
        return progress;
    }

    public synchronized int getGarageToTestDriveVehicleIndex() {
        return garageToTestDriveVehicleIndex < 0 ? selectedVehicleIndex : garageToTestDriveVehicleIndex;
    }

    public synchronized void completeGarageToTestDriveTransition() {
        garageToTestDriveTransition = false;
        garageToTestDriveVehicleIndex = -1;
        selectedMode = MODE_FREE_DRIVE;
        selectedMap = MAP_OPEN_FIELD;
        garageMode = GARAGE_MODE_SELECT;
        testDriveSessionActive = true;
        testDriveLaunchRequest = true;
        testDriveRestartRequest = false;
        screen = SCREEN_DRIVE;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized boolean isTestDriveSessionActive() {
        return testDriveSessionActive;
    }

    public synchronized boolean consumeTestDriveLaunchRequest() {
        boolean value = testDriveLaunchRequest;
        testDriveLaunchRequest = false;
        if (value) lastChangeMs = System.currentTimeMillis();
        return value;
    }

    public synchronized void requestTestDriveRestart() {
        if (!testDriveSessionActive) return;
        testDriveRestartRequest = true;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized boolean consumeTestDriveRestartRequest() {
        boolean value = testDriveRestartRequest;
        testDriveRestartRequest = false;
        if (value) lastChangeMs = System.currentTimeMillis();
        return value;
    }

    public synchronized void endTestDriveSession() {
        testDriveSessionActive = false;
        testDriveLaunchRequest = false;
        testDriveRestartRequest = false;
        garageToTestDriveTransition = false;
        garageToTestDriveVehicleIndex = -1;
        selectedMode = MODE_FREE_DRIVE;
        selectedMap = MAP_OPEN_FIELD;
        garageMode = GARAGE_MODE_SELECT;
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized void setLastTestDriveResult(String result, int reward) {
        lastTestDriveResult = result == null ? "" : result;
        lastTestDriveReward = Math.max(0, reward);
        lastChangeMs = System.currentTimeMillis();
    }

    public synchronized String getLastTestDriveResult() {
        return lastTestDriveResult;
    }

    public synchronized int getLastTestDriveReward() {
        return lastTestDriveReward;
    }

    public synchronized long getLastChangeMs() {
        return lastChangeMs;
    }

    public static String modeLabel(int mode) {
        if (mode == MODE_TIME_TRIAL) return "ZAMAN YARISI";
        if (mode == MODE_DRIFT) return "DRIFT SKOR";
        if (mode == MODE_RACE_LOCKED) return "CHECKPOINT YARISI";
        if (mode == MODE_DRAG_RACE) return "DRAG YARISI";
        if (mode == MODE_POLICE_CHASE) return "POLIS KOVALAMACA";
        return "SERBEST SURUS";
    }

    public static String mapLabel(int map) {
        if (map == MAP_CITY) return "BUYUK SEHIR";
        if (map == MAP_HIGHWAY) return "OTOYOL";
        if (map == MAP_DRIFT_PARK) return "DRIFT PARK";
        if (map == MAP_OPEN_WORLD) return "ACIK DUNYA (KAPALI)";
        if (map == MAP_SECOND_NEW) return "2. YENI HARITA";
        return "ACIK TEST ALANI";
    }

    public static int vehicleCount() {
        return VehicleCatalog.count();
    }

    public static String vehicleLabel(int index) {
        return VehicleCatalog.label(index);
    }

    public static boolean isVehicleLocked(int index) {
        return VehicleCatalog.isLocked(index);
    }
}
