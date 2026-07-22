package com.arabaoyunu.mission;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.util.SaveManager;

/**
 * ArabaOyunu_61_9: Harita olmadan calisan mini surus gorevleri.
 * Open Field serbest suruste oyuncuya para/XP kazandiran ilk donguyu verir.
 */
public final class DrivingMissionSystem {
    private static final int M_DISTANCE = 0;
    private static final int M_SPEED_HOLD = 1;
    private static final int M_NITRO = 2;
    private static final int M_NO_DAMAGE = 3;
    private static final int M_TOP_SPEED = 4;
    private static final int COUNT = 5;

    private final SaveManager saveManager;
    private int missionIndex;
    private float progress;
    private boolean haveLastPosition;
    private float lastX;
    private float lastZ;
    private float messageTimer;
    private String message = "";
    private int rewardFlash;

    public DrivingMissionSystem(SaveManager saveManager) {
        this.saveManager = saveManager;
        missionIndex = saveManager == null ? 0 : saveManager.getDrivingMissionIndex() % COUNT;
        progress = saveManager == null ? 0f : saveManager.getDrivingMissionProgress();
    }

    public void update(float dt, VehicleController vehicle, InputState input, boolean active) {
        float safeDt = clamp(dt, 0f, 0.05f);
        if (messageTimer > 0f) {
            messageTimer -= safeDt;
            if (messageTimer <= 0f) {
                message = "";
                rewardFlash = 0;
            }
        }
        if (!active || vehicle == null) {
            haveLastPosition = false;
            return;
        }
        float step = 0f;
        if (!haveLastPosition) {
            lastX = vehicle.position.x;
            lastZ = vehicle.position.z;
            haveLastPosition = true;
        } else {
            float dx = vehicle.position.x - lastX;
            float dz = vehicle.position.z - lastZ;
            step = (float)Math.sqrt(dx * dx + dz * dz);
            lastX = vehicle.position.x;
            lastZ = vehicle.position.z;
            if (step > 18f) step = 0f;
        }

        switch (missionIndex) {
            case M_DISTANCE:
                progress += step;
                if (progress >= 500f) complete(220, 45, "500 metre gorevi tamamlandi");
                break;
            case M_SPEED_HOLD:
                if (vehicle.getSpeedKmh() >= 100f) progress += safeDt;
                else progress = Math.max(0f, progress - safeDt * 1.2f);
                if (progress >= 30f) complete(300, 60, "100 km/h ustu surus tamamlandi");
                break;
            case M_NITRO:
                if (input != null && input.nitro > 0.25f && vehicle.getSpeedKmh() > 35f) progress += safeDt;
                if (progress >= 10f) complete(260, 55, "N2O gorevi tamamlandi");
                break;
            case M_NO_DAMAGE:
                if (vehicle.getHealth01() > 0.985f) progress += safeDt;
                else progress = 0f;
                if (progress >= 60f) complete(420, 85, "Hasarsiz surus gorevi tamamlandi");
                break;
            case M_TOP_SPEED:
                progress = Math.max(progress, vehicle.getSpeedKmh());
                if (progress >= 150f) complete(360, 75, "150 km/h hedefi tamamlandi");
                break;
            default:
                missionIndex = 0;
                progress = 0f;
                break;
        }
        saveProgress();
    }

    private void complete(int coins, int xp, String text) {
        rewardFlash = coins;
        message = text + " +" + coins + " coin";
        messageTimer = 3.2f;
        if (saveManager != null) {
            saveManager.addCoins(coins);
            saveManager.addXp(xp);
            saveManager.incrementDailyMissionCompleted();
            saveManager.incrementProgressDaily();
            saveManager.incrementProgressWeekly();
            saveManager.incrementDrivingMissionCompleted();
            saveManager.setEconomyLastMessage(message + " +" + xp + " XP");
        }
        missionIndex = (missionIndex + 1) % COUNT;
        progress = 0f;
        haveLastPosition = false;
        saveProgress();
    }

    private void saveProgress() {
        if (saveManager == null) return;
        saveManager.setDrivingMissionIndex(missionIndex);
        saveManager.setDrivingMissionProgress(progress);
    }

    public String getMissionTitle() {
        switch (missionIndex) {
            case M_SPEED_HOLD: return "HIZI KORU";
            case M_NITRO: return "N2O KULLAN";
            case M_NO_DAMAGE: return "HASARSIZ SUR";
            case M_TOP_SPEED: return "HIZ REKORU";
            default: return "MESAFE SURUSU";
        }
    }

    public String getProgressText() {
        switch (missionIndex) {
            case M_SPEED_HOLD: return ((int)progress) + "/30 sn 100+ km/h";
            case M_NITRO: return ((int)progress) + "/10 sn N2O";
            case M_NO_DAMAGE: return ((int)progress) + "/60 sn hasarsiz";
            case M_TOP_SPEED: return ((int)progress) + "/150 km/h";
            default: return ((int)progress) + "/500 m";
        }
    }

    public String getMessage() { return message; }
    public int getRewardFlash() { return rewardFlash; }
    public int getCompletedCount() { return saveManager == null ? 0 : saveManager.getDrivingMissionCompletedCount(); }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
