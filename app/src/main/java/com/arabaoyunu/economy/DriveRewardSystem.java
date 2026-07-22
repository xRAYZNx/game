package com.arabaoyunu.economy;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.util.SaveManager;

/** ArabaOyunu_61_9: Serbest suruste mikro odul ekonomisi. */
public final class DriveRewardSystem {
    private final SaveManager saveManager;

    private boolean haveLastPosition;
    private float lastX;
    private float lastZ;
    private float distanceBank;
    private float saveDistanceBank;
    private float highSpeedTimer;
    private float noDamageTimer;
    private float nitroTimer;
    private float messageTimer;
    private int rewardFlash;
    private String message = "";

    public DriveRewardSystem(SaveManager saveManager) {
        this.saveManager = saveManager;
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
            highSpeedTimer = 0f;
            noDamageTimer = 0f;
            nitroTimer = 0f;
            return;
        }

        if (!haveLastPosition) {
            lastX = vehicle.position.x;
            lastZ = vehicle.position.z;
            haveLastPosition = true;
            return;
        }

        float dx = vehicle.position.x - lastX;
        float dz = vehicle.position.z - lastZ;
        float step = (float)Math.sqrt(dx * dx + dz * dz);
        lastX = vehicle.position.x;
        lastZ = vehicle.position.z;
        if (step > 0f && step < 18f) {
            distanceBank += step;
            saveDistanceBank += step;
        }

        if (saveManager != null) {
            if (saveDistanceBank >= 50f) {
                int meters = (int)saveDistanceBank;
                saveDistanceBank -= meters;
                saveManager.addDrivenMeters(meters);
            }
            saveManager.updateBestSpeedKmh(vehicle.getSpeedKmh());
        }

        while (distanceBank >= 500f) {
            distanceBank -= 500f;
            reward(85, "500m surus bonusu");
        }

        if (vehicle.getSpeedKmh() >= 100f) {
            highSpeedTimer += safeDt;
            if (highSpeedTimer >= 10f) {
                highSpeedTimer = 0f;
                reward(60, "Yuksek hiz bonusu");
            }
        } else {
            highSpeedTimer = Math.max(0f, highSpeedTimer - safeDt * 1.5f);
        }

        if (vehicle.getHealth01() > 0.985f) {
            noDamageTimer += safeDt;
            if (noDamageTimer >= 60f) {
                noDamageTimer = 0f;
                reward(150, "Hasarsiz surus bonusu");
            }
        } else {
            noDamageTimer = 0f;
        }

        boolean nitroOn = input != null && input.nitro > 0.25f && vehicle.getSpeedKmh() > 30f;
        if (nitroOn) {
            nitroTimer += safeDt;
            if (nitroTimer >= 8f) {
                nitroTimer = 0f;
                reward(70, "N2O kullanim bonusu");
            }
        } else {
            nitroTimer = Math.max(0f, nitroTimer - safeDt * 1.2f);
        }
    }

    private void reward(int coins, String text) {
        rewardFlash = Math.max(rewardFlash, coins);
        message = text + " +" + coins;
        messageTimer = 2.8f;
        if (saveManager != null) {
            saveManager.addCoins(coins);
            saveManager.setEconomyLastMessage(message);
        }
    }

    public String getMessage() { return message; }
    public int getRewardFlash() { return rewardFlash; }
    public float getDistanceToNextBonus() { return Math.max(0f, 500f - distanceBank); }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
