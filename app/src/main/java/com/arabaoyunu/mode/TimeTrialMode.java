package com.arabaoyunu.mode;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.map.MapManager;
import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.util.SaveManager;

/**
 * Plan 13: Zaman Yarisi Modu.
 *
 * - 3-2-1-GO geri sayim
 * - checkpoint sirasi
 * - finish gate
 * - yanlis yon uyarisi
 * - en iyi sure kaydi
 * - yeniden baslatma
 */
public final class TimeTrialMode extends BaseGameMode {

    private static final float COUNTDOWN_SECONDS = 3.2f;
    private static final float CHECKPOINT_RADIUS = 15f;
    private static final float FINISH_RADIUS = 18f;

    private final VehicleController car;
    private final MapManager mapManager;
    private final SaveManager saveManager;

    // Basit ilk pist: DriftPracticeMap_01 uzun ana yol + viraj alani.
    private final float[] checkpointX = new float[] { 0f, 0f, 48f, 92f, 48f, 0f };
    private final float[] checkpointZ = new float[] { -115f, -32f, 42f, 35f, 102f, 184f };

    private float countdown;
    private float elapsed;
    private float bestTime;
    private int checkpointIndex;
    private boolean running;
    private boolean finished;
    private boolean newBest;
    private boolean wrongWay;
    private String finishGrade = "-";

    public TimeTrialMode(VehicleController car, MapManager mapManager, SaveManager saveManager) {
        this.car = car;
        this.mapManager = mapManager;
        this.saveManager = saveManager;
    }

    @Override
    public String getName() {
        return "TimeTrialMode";
    }

    @Override
    public void start() {
        if (mapManager != null && mapManager.getCurrentMap() != null) {
            car.reset(
                    mapManager.getCurrentMap().getSpawnX(),
                    mapManager.getCurrentMap().getSpawnY(),
                    mapManager.getCurrentMap().getSpawnZ(),
                    mapManager.getCurrentMap().getSpawnYaw());
        }

        countdown = COUNTDOWN_SECONDS;
        elapsed = 0f;
        checkpointIndex = 0;
        running = false;
        finished = false;
        newBest = false;
        wrongWay = false;
        finishGrade = "-";
        bestTime = saveManager == null ? 0f : saveManager.getTimeTrialBestSeconds();
    }

    @Override
    public void update(float dt, InputState input) {
        if (dt <= 0f) return;

        if (finished) {
            if (input != null && (input.throttle > 0.3f || input.pausePressed)) {
                start();
            }
            return;
        }

        InputState driveInput = input;
        if (countdown > 0f) {
            countdown -= dt;
            running = false;
            driveInput = null; // geri sayimda arac sabit kalsin
        } else {
            running = true;
            elapsed += dt;
        }

        if (mapManager != null && mapManager.getCurrentMap() != null) {
            car.update(dt, driveInput, mapManager.getCurrentMap());
        }

        if (!running) {
            wrongWay = false;
            return;
        }

        updateWrongWay();
        updateCheckpointProgress();
    }

    private void updateCheckpointProgress() {
        if (checkpointIndex >= checkpointX.length) {
            finishRace();
            return;
        }

        float tx = checkpointX[checkpointIndex];
        float tz = checkpointZ[checkpointIndex];
        float radius = checkpointIndex == checkpointX.length - 1 ? FINISH_RADIUS : CHECKPOINT_RADIUS;

        if (distanceSq(car.position.x, car.position.z, tx, tz) <= radius * radius) {
            checkpointIndex++;
            if (checkpointIndex >= checkpointX.length) {
                finishRace();
            }
        }
    }

    private void updateWrongWay() {
        if (checkpointIndex >= checkpointX.length) {
            wrongWay = false;
            return;
        }

        float tx = checkpointX[checkpointIndex];
        float tz = checkpointZ[checkpointIndex];
        float dx = tx - car.position.x;
        float dz = tz - car.position.z;
        float len = (float) Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001f || car.getSpeedKmh() < 12f) {
            wrongWay = false;
            return;
        }

        dx /= len;
        dz /= len;
        float forwardX = (float) Math.sin(car.yaw);
        float forwardZ = (float) Math.cos(car.yaw);
        float dot = forwardX * dx + forwardZ * dz;
        wrongWay = dot < -0.22f;
    }

    private void finishRace() {
        if (finished) return;
        finished = true;
        running = false;
        wrongWay = false;
        if (saveManager != null) {
            newBest = saveManager.saveTimeTrialBestIfLower(elapsed);
            bestTime = saveManager.getTimeTrialBestSeconds();
            saveManager.addCoins(calculateCoins());
        }
        finishGrade = calculateGrade(elapsed);
    }

    private int calculateCoins() {
        if (elapsed <= 0f) return 0;
        if (elapsed <= 45f) return 1250;
        if (elapsed <= 60f) return 850;
        if (elapsed <= 80f) return 520;
        return 260;
    }

    private String calculateGrade(float seconds) {
        if (seconds <= 45f) return "S";
        if (seconds <= 60f) return "A";
        if (seconds <= 80f) return "B";
        if (seconds <= 105f) return "C";
        return "D";
    }

    private static float distanceSq(float ax, float az, float bx, float bz) {
        float dx = ax - bx;
        float dz = az - bz;
        return dx * dx + dz * dz;
    }

    public boolean isActive() { return true; }
    public float getElapsed() { return elapsed; }
    public float getBestTime() { return bestTime; }
    public float getCountdown() { return Math.max(0f, countdown); }
    public int getCheckpointIndex() { return Math.min(checkpointIndex, checkpointX.length); }
    public int getCheckpointTotal() { return checkpointX.length; }
    public boolean isFinished() { return finished; }
    public boolean isNewBest() { return newBest; }
    public boolean isWrongWay() { return wrongWay; }
    public String getFinishGrade() { return finishGrade; }
}
