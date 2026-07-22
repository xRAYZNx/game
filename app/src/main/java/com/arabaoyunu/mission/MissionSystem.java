package com.arabaoyunu.mission;

import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;
import com.arabaoyunu.traffic.TrafficSystem;
import com.arabaoyunu.util.SaveManager;

/**
 * ArabaOyunu_29: Harita + görev sistemi.
 *
 * İlk sürümde görevler:
 * - Serbest sürüş mesafe görevi
 * - Checkpoint görevi
 * - Hız radarı görevi
 * - Trafik yakın geçiş görevi
 * - Günlük görev tamamlama sayacı
 */
public final class MissionSystem {

    private static final int MISSION_DISTANCE = 0;
    private static final int MISSION_CHECKPOINT = 1;
    private static final int MISSION_SPEED_RADAR = 2;
    private static final int MISSION_TRAFFIC_NEAR = 3;
    private static final int MISSION_DAILY_BONUS = 4;
    private static final int MISSION_COUNT = 5;

    private static final float[][] CHECKPOINTS = new float[][] {
            {0f, -105f},
            {42f, -18f},
            {82f, 42f},
            {36f, 112f},
            {-44f, 72f},
            {-84f, -24f}
    };

    private final SaveManager saveManager;

    private int activeMission;
    private int checkpointIndex;
    private float distanceProgress;
    private float lastX;
    private float lastZ;
    private boolean haveLastPosition;
    private int lastTrafficNearMiss;
    private int earnedCoinsFlash;
    private float messageTimer;
    private String message = "";

    public MissionSystem(SaveManager saveManager) {
        this.saveManager = saveManager;
        activeMission = saveManager == null ? 0 : saveManager.getActiveMission();
        if (activeMission < 0 || activeMission >= MISSION_COUNT) activeMission = 0;
    }

    public void update(float dt, VehicleController car, TrafficSystem traffic) {
        if (car == null) return;
        if (dt < 0f) dt = 0f;
        if (dt > 0.10f) dt = 0.10f;

        if (messageTimer > 0f) {
            messageTimer -= dt;
            if (messageTimer <= 0f) {
                message = "";
                earnedCoinsFlash = 0;
            }
        }

        if (!haveLastPosition) {
            lastX = car.position.x;
            lastZ = car.position.z;
            lastTrafficNearMiss = traffic == null ? 0 : traffic.getNearMissCount();
            haveLastPosition = true;
        }

        float dx = car.position.x - lastX;
        float dz = car.position.z - lastZ;
        float stepDistance = (float) Math.sqrt(dx * dx + dz * dz);
        if (stepDistance < 12f) distanceProgress += stepDistance;
        lastX = car.position.x;
        lastZ = car.position.z;

        if (activeMission == MISSION_DISTANCE) {
            if (distanceProgress >= 900f) completeMission(420, "Serbest surus gorevi tamam!");
        } else if (activeMission == MISSION_CHECKPOINT) {
            updateCheckpointMission(car);
        } else if (activeMission == MISSION_SPEED_RADAR) {
            updateSpeedRadarMission(car);
        } else if (activeMission == MISSION_TRAFFIC_NEAR) {
            int near = traffic == null ? 0 : traffic.getNearMissCount();
            if (near - lastTrafficNearMiss >= 3) completeMission(680, "Trafik yakin gecis gorevi tamam!");
        } else if (activeMission == MISSION_DAILY_BONUS) {
            int done = saveManager == null ? 0 : saveManager.getDailyMissionCompletedCount();
            if (done >= 3) completeMission(1000, "Gunluk gorev bonusu alindi!");
            else {
                // Günlük bonus tek başına bitmez; oyuncuya diğer görevleri yaptırır.
                activeMission = MISSION_DISTANCE;
                saveActiveMission();
            }
        }
    }

    private void updateCheckpointMission(VehicleController car) {
        if (checkpointIndex >= CHECKPOINTS.length) {
            completeMission(760, "Checkpoint gorevi tamam!");
            return;
        }
        float tx = CHECKPOINTS[checkpointIndex][0];
        float tz = CHECKPOINTS[checkpointIndex][1];
        float dx = car.position.x - tx;
        float dz = car.position.z - tz;
        if (dx * dx + dz * dz < 14f * 14f) {
            checkpointIndex++;
            message = "Checkpoint " + checkpointIndex + "/" + CHECKPOINTS.length;
            messageTimer = 1.3f;
        }
    }

    private void updateSpeedRadarMission(VehicleController car) {
        float[][] radars = new float[][] {
                {0f, 0f, 115f},
                {88f, 42f, 130f},
                {-88f, -28f, 125f}
        };
        for (int i = 0; i < radars.length; i++) {
            float dx = car.position.x - radars[i][0];
            float dz = car.position.z - radars[i][1];
            if (dx * dx + dz * dz < 18f * 18f && car.getSpeedKmh() >= radars[i][2]) {
                completeMission(620, "Hiz radari gorevi tamam!");
                return;
            }
        }
    }

    private void completeMission(int coins, String msg) {
        earnedCoinsFlash = coins;
        message = msg + " +" + coins;
        messageTimer = 3.4f;
        if (saveManager != null) {
            saveManager.addCoins(coins);
            saveManager.incrementDailyMissionCompleted();
        }
        activeMission = (activeMission + 1) % MISSION_COUNT;
        if (activeMission == MISSION_DAILY_BONUS && saveManager != null && saveManager.getDailyMissionCompletedCount() < 3) {
            activeMission = MISSION_DISTANCE;
        }
        checkpointIndex = 0;
        distanceProgress = 0f;
        haveLastPosition = false;
        saveActiveMission();
    }

    private void saveActiveMission() {
        if (saveManager != null) saveManager.setActiveMission(activeMission);
    }

    public void render(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        if (renderer == null || vp == null) return;
        if (activeMission == MISSION_CHECKPOINT) {
            int idx = Math.max(0, Math.min(checkpointIndex, CHECKPOINTS.length - 1));
            float x = CHECKPOINTS[idx][0];
            float z = CHECKPOINTS[idx][1];
            renderer.drawCircle(vp, x, 0.10f, z, 14f, stats);
            renderer.drawBox(vp, x - 6f, 1.5f, z, 0.65f, 3f, 0.65f, 0f, 0.08f, 0.95f, 0.28f, 1f, stats);
            renderer.drawBox(vp, x + 6f, 1.5f, z, 0.65f, 3f, 0.65f, 0f, 0.08f, 0.95f, 0.28f, 1f, stats);
        } else if (activeMission == MISSION_SPEED_RADAR) {
            drawRadar(renderer, vp, 0f, 0f, stats);
            drawRadar(renderer, vp, 88f, 42f, stats);
            drawRadar(renderer, vp, -88f, -28f, stats);
        } else if (activeMission == MISSION_TRAFFIC_NEAR) {
            renderer.drawBox(vp, 0f, 2.1f, 0f, 9.5f, 1.4f, 0.55f, 0f, 1f, 0.65f, 0.10f, 1f, stats);
        }
    }

    private void drawRadar(PrimitiveRenderer renderer, float[] vp, float x, float z, RenderStats stats) {
        renderer.drawCircle(vp, x, 0.09f, z, 18f, stats);
        renderer.drawBox(vp, x, 2.2f, z, 0.65f, 4.4f, 0.65f, 0f, 0.22f, 0.65f, 1f, 1f, stats);
        renderer.drawBox(vp, x, 4.55f, z, 3.8f, 1.05f, 0.35f, 0f, 0.12f, 0.14f, 0.18f, 1f, stats);
    }

    public String getMissionTitle() {
        if (activeMission == MISSION_CHECKPOINT) return "CHECKPOINT";
        if (activeMission == MISSION_SPEED_RADAR) return "HIZ RADARI";
        if (activeMission == MISSION_TRAFFIC_NEAR) return "TRAFIK GECIS";
        if (activeMission == MISSION_DAILY_BONUS) return "GUNLUK BONUS";
        return "SERBEST SURUS";
    }

    public String getMissionProgressText() {
        if (activeMission == MISSION_CHECKPOINT) return checkpointIndex + "/" + CHECKPOINTS.length;
        if (activeMission == MISSION_SPEED_RADAR) return "Radar noktasi: 115+ km/h";
        if (activeMission == MISSION_TRAFFIC_NEAR) return "3 yakin gecis";
        if (activeMission == MISSION_DAILY_BONUS) {
            int done = saveManager == null ? 0 : saveManager.getDailyMissionCompletedCount();
            return done + "/3 gunluk";
        }
        return ((int) distanceProgress) + "/900 m";
    }

    public String getMessage() {
        return message;
    }

    public int getEarnedCoinsFlash() {
        return earnedCoinsFlash;
    }

    public int getDailyCompleted() {
        return saveManager == null ? 0 : saveManager.getDailyMissionCompletedCount();
    }
}
