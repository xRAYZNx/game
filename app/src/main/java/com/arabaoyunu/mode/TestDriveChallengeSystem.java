package com.arabaoyunu.mode;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * ArabaOyunu_60: Test surusu artik sadece sayaç degil; acik test alaninda fiziksel
 * checkpoint/parkur isaretleri, hedef takibi, basari/basarisizlik mesajlari ve
 * dengeli odul hesaplamasi olan bir test parkuru sistemi olarak calisir.
 */
public final class TestDriveChallengeSystem {

    public static final int CH_ACCEL = 0;
    public static final int CH_BRAKE = 1;
    public static final int CH_SLALOM = 2;
    public static final int CH_DRIFT = 3;
    public static final int CH_NITRO = 4;
    public static final int CH_NO_DAMAGE = 5;
    public static final int CH_TOP_SPEED = 6;
    public static final int CH_COUNT = 7;

    private static final int MAX_CHECKPOINTS = 10;
    private static final float DEFAULT_GATE_RADIUS = 14.0f;

    private final SaveManager saveManager;
    private final float[] checkpointX = new float[MAX_CHECKPOINTS];
    private final float[] checkpointZ = new float[MAX_CHECKPOINTS];
    private final float[] checkpointRadius = new float[MAX_CHECKPOINTS];

    private boolean active;
    private int challengeIndex;
    private int vehicleIndex;
    private String vehicleId = "";
    private float elapsed;
    private float challengeTimer;
    private float resultTimer;
    private boolean resultVisible;
    private boolean rewardGiven;
    private int lastReward;
    private String lastResult = "";
    private String message = "";
    private String objective = "";

    private boolean armed;
    private float startX;
    private float startZ;
    private float lastX;
    private float lastZ;
    private float distance;
    private float maxSpeed;
    private float averageSpeedAccum;
    private int averageSpeedSamples;
    private float driftMeters;
    private float nitroTime;
    private float noDamageStartHealth;
    private int slalomSwitches;
    private int lastSteerSign;
    private float brakeStartSpeed;
    private float missedWarningTimer;
    private boolean failed;

    private int checkpointCount;
    private int checkpointIndex;
    private float lastTargetDistance;

    public TestDriveChallengeSystem(SaveManager saveManager) {
        this.saveManager = saveManager;
    }

    public void start(int selectedVehicleIndex, VehicleController vehicle) {
        vehicleIndex = clamp(selectedVehicleIndex, 0, VehicleCatalog.count() - 1);
        vehicleId = VehicleCatalog.id(vehicleIndex);
        active = true;
        challengeIndex = clamp(saveManager == null ? 0 : saveManager.getTestDriveChallengeIndex(), 0, CH_COUNT - 1);
        startChallenge(vehicle);
    }

    public void stop() {
        active = false;
        resultVisible = false;
        rewardGiven = false;
        failed = false;
        message = "";
        objective = "";
    }

    public void restartCurrent(VehicleController vehicle) {
        if (!active) return;
        startChallenge(vehicle);
        message = "Parkur tekrar başlatıldı";
    }

    public boolean isActive() {
        return active;
    }

    public void update(float dt, VehicleController vehicle, InputState input) {
        if (!active || vehicle == null) return;
        float safeDt = clamp(dt, 0f, 0.05f);
        elapsed += safeDt;

        if (resultVisible) {
            resultTimer += safeDt;
            if (resultTimer >= 5.0f) {
                challengeIndex = (challengeIndex + 1) % CH_COUNT;
                if (saveManager != null) saveManager.setTestDriveChallengeIndex(challengeIndex);
                startChallenge(vehicle);
            }
            return;
        }

        challengeTimer += safeDt;
        missedWarningTimer = Math.max(0f, missedWarningTimer - safeDt);

        float x = vehicle.position.x;
        float z = vehicle.position.z;
        float step = distance(lastX, lastZ, x, z);
        if (step < 12f) distance += step;
        lastX = x;
        lastZ = z;

        float speed = vehicle.getSpeedKmh();
        if (speed > maxSpeed) maxSpeed = speed;
        averageSpeedAccum += speed;
        averageSpeedSamples++;

        updateCheckpointProgress(x, z, speed);

        switch (challengeIndex) {
            case CH_ACCEL:
                updateAcceleration(vehicle, speed);
                break;
            case CH_BRAKE:
                updateBrake(vehicle, input, speed);
                break;
            case CH_SLALOM:
                updateSlalom(input, speed);
                break;
            case CH_DRIFT:
                updateDrift(vehicle, step, x, z);
                break;
            case CH_NITRO:
                updateNitro(input, speed, safeDt);
                break;
            case CH_NO_DAMAGE:
                updateNoDamage(vehicle);
                break;
            case CH_TOP_SPEED:
                updateTopSpeed(speed);
                break;
            default:
                complete(120, "Test tamamlandı", false);
                break;
        }
    }

    /** Test parkuru isaretlerini surus sahnesinde cizer; fizik/carpisma eklemez. */
    public void render(PrimitiveRenderer renderer, float[] viewProjection, RenderStats stats) {
        if (!active || renderer == null || viewProjection == null || resultVisible) return;

        // Rota cizgisi.
        for (int i = 0; i < checkpointCount - 1; i++) {
            float intensity = i < checkpointIndex ? 0.34f : 1.0f;
            renderer.drawLine(viewProjection,
                    checkpointX[i], 0.105f, checkpointZ[i],
                    checkpointX[i + 1], 0.105f, checkpointZ[i + 1],
                    0.02f * intensity, 0.78f * intensity, 1.0f * intensity,
                    stats);
        }

        // Gorev tipine gore zemin ipucu.
        drawCourseSurface(renderer, viewProjection, stats);

        // Checkpoint kapilari.
        for (int i = 0; i < checkpointCount; i++) {
            boolean passed = i < checkpointIndex;
            boolean target = i == checkpointIndex;
            drawCheckpointGate(renderer, viewProjection, stats, i, passed, target);
        }
    }

    private void drawCourseSurface(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        switch (challengeIndex) {
            case CH_ACCEL:
                renderer.drawBox(vp, 0f, 0.066f, 72f, 13f, 0.035f, 150f, 0f, 0.02f, 0.10f, 0.16f, 0.82f, stats);
                drawStartFinishStripes(renderer, vp, 0f, 0f, stats);
                drawStartFinishStripes(renderer, vp, 0f, 145f, stats);
                break;
            case CH_BRAKE:
                renderer.drawBox(vp, 22f, 0.066f, 70f, 13f, 0.035f, 145f, 0f, 0.14f, 0.10f, 0.04f, 0.86f, stats);
                renderer.drawBox(vp, 22f, 0.074f, 135f, 23f, 0.04f, 20f, 0f, 0.88f, 0.10f, 0.06f, 0.92f, stats);
                break;
            case CH_SLALOM:
                renderer.drawBox(vp, 0f, 0.066f, 75f, 72f, 0.032f, 128f, 0f, 0.03f, 0.12f, 0.14f, 0.78f, stats);
                break;
            case CH_DRIFT:
                renderer.drawCircle(vp, 78f, 0.085f, 78f, 44f, stats);
                renderer.drawCircle(vp, 78f, 0.09f, 78f, 24f, stats);
                break;
            case CH_NITRO:
                renderer.drawBox(vp, -70f, 0.066f, 60f, 12f, 0.035f, 170f, 0f, 0.04f, 0.05f, 0.20f, 0.84f, stats);
                break;
            case CH_NO_DAMAGE:
                renderer.drawBox(vp, 0f, 0.060f, -85f, 176f, 0.03f, 9f, 0f, 0.04f, 0.16f, 0.05f, 0.70f, stats);
                renderer.drawBox(vp, 0f, 0.060f, 85f, 176f, 0.03f, 9f, 0f, 0.04f, 0.16f, 0.05f, 0.70f, stats);
                renderer.drawBox(vp, -85f, 0.060f, 0f, 9f, 0.03f, 176f, 0f, 0.04f, 0.16f, 0.05f, 0.70f, stats);
                renderer.drawBox(vp, 85f, 0.060f, 0f, 9f, 0.03f, 176f, 0f, 0.04f, 0.16f, 0.05f, 0.70f, stats);
                break;
            case CH_TOP_SPEED:
                renderer.drawBox(vp, 0f, 0.066f, 25f, 16f, 0.035f, 290f, 0f, 0.09f, 0.06f, 0.15f, 0.86f, stats);
                drawStartFinishStripes(renderer, vp, 0f, -115f, stats);
                drawStartFinishStripes(renderer, vp, 0f, 165f, stats);
                break;
            default:
                break;
        }
    }

    private void drawCheckpointGate(PrimitiveRenderer renderer, float[] vp, RenderStats stats, int i, boolean passed, boolean target) {
        float x = checkpointX[i];
        float z = checkpointZ[i];
        float radius = checkpointRadius[i];
        float[] side = sideVectorForGate(i);
        float sx = side[0];
        float sz = side[1];
        float half = Math.max(5f, radius * 0.72f);
        float px0 = x + sx * half;
        float pz0 = z + sz * half;
        float px1 = x - sx * half;
        float pz1 = z - sz * half;

        float r = target ? 0.0f : (passed ? 0.18f : 0.04f);
        float g = target ? 0.95f : (passed ? 0.52f : 0.55f);
        float b = target ? 1.0f : (passed ? 0.36f : 0.88f);
        if (challengeIndex == CH_BRAKE && i == checkpointCount - 1) { r = 1f; g = target ? 0.18f : 0.34f; b = 0.08f; }

        renderer.drawCircle(vp, x, 0.115f, z, target ? radius : radius * 0.62f, stats);
        renderer.drawBox(vp, px0, target ? 1.55f : 0.95f, pz0, 1.2f, target ? 3.0f : 1.8f, 1.2f, 0f, r, g, b, 1f, stats);
        renderer.drawBox(vp, px1, target ? 1.55f : 0.95f, pz1, 1.2f, target ? 3.0f : 1.8f, 1.2f, 0f, r, g, b, 1f, stats);
        renderer.drawLine(vp, px0, target ? 3.1f : 1.9f, pz0, px1, target ? 3.1f : 1.9f, pz1, r, g, b, stats);
    }

    private void drawStartFinishStripes(PrimitiveRenderer renderer, float[] vp, float x, float z, RenderStats stats) {
        for (int i = -3; i <= 3; i++) {
            float c = (i & 1) == 0 ? 1f : 0.05f;
            renderer.drawBox(vp, x + i * 2f, 0.092f, z, 1.0f, 0.035f, 10f, 0f, c, c, c, 1f, stats);
        }
    }

    private float[] sideVectorForGate(int i) {
        float dx = 0f;
        float dz = 1f;
        if (checkpointCount > 1) {
            int a = Math.max(0, i - 1);
            int b = Math.min(checkpointCount - 1, i + 1);
            dx = checkpointX[b] - checkpointX[a];
            dz = checkpointZ[b] - checkpointZ[a];
            float len = (float)Math.sqrt(dx * dx + dz * dz);
            if (len > 0.001f) {
                dx /= len;
                dz /= len;
            } else {
                dx = 0f;
                dz = 1f;
            }
        }
        return new float[] { -dz, dx };
    }

    private void updateCheckpointProgress(float x, float z, float speed) {
        if (checkpointIndex >= checkpointCount) return;
        float tx = checkpointX[checkpointIndex];
        float tz = checkpointZ[checkpointIndex];
        float d = distance(x, z, tx, tz);
        if (d <= checkpointRadius[checkpointIndex]) {
            checkpointIndex++;
            lastTargetDistance = checkpointIndex < checkpointCount ? distance(x, z, checkpointX[checkpointIndex], checkpointZ[checkpointIndex]) : 0f;
            if (checkpointIndex < checkpointCount) {
                message = "Checkpoint " + checkpointIndex + "/" + checkpointCount + " geçti, sıradaki hedefe git";
            } else {
                message = "Son hedef çizgisine girdin";
            }
            return;
        }
        if (speed > 18f && d > Math.max(55f, checkpointRadius[checkpointIndex] * 3.5f)
                && d > lastTargetDistance + 6f && missedWarningTimer <= 0f) {
            missedWarningTimer = 2.3f;
            message = "Checkpoint kaçıyor: mavi hedefe dön";
        }
        lastTargetDistance = d;
    }

    private void updateAcceleration(VehicleController vehicle, float speed) {
        if (!armed && (checkpointIndex > 0 || speed > 8f)) {
            armed = true;
            challengeTimer = 0f;
            message = "0-100 başladı: bitiş çizgisine kadar hızlan";
        }
        if (checkpointIndex >= checkpointCount && speed >= 100f) {
            int reward = challengeTimer <= 5.8f ? 470 : (challengeTimer <= 7.5f ? 360 : 260);
            complete(reward, "0-100 parkuru: " + formatOne(challengeTimer) + " sn", false);
        } else if (speed >= 100f && checkpointIndex < checkpointCount) {
            message = "Hız tamam, şimdi bitiş checkpointine gir";
        } else if (challengeTimer > 32f) {
            complete(maxSpeed >= 92f ? 160 : 90, "0-100 denemesi: " + Math.round(maxSpeed) + " km/s", true);
        }
    }

    private void updateBrake(VehicleController vehicle, InputState input, float speed) {
        if (!armed && (checkpointIndex >= 2 || speed >= 82f)) {
            armed = true;
            brakeStartSpeed = Math.max(speed, 82f);
            message = "Fren alanına gir ve aracı tamamen durdur";
        }
        if (checkpointIndex >= checkpointCount && armed && input != null && input.brake > 0.32f && speed <= 6f) {
            int reward = brakeStartSpeed >= 110f ? 405 : 300;
            complete(reward, "Fren parkuru tamamlandı", false);
        } else if (checkpointIndex >= checkpointCount && speed > 8f) {
            message = "Kırmızı durma alanındasın: frene bas ve dur";
        } else if (challengeTimer > 42f) {
            complete(100, "Fren parkuru başarısız: durma alanını yakala", true);
        }
    }

    private void updateSlalom(InputState input, float speed) {
        if (input != null) {
            int sign = 0;
            if (input.steer > 0.36f) sign = 1;
            else if (input.steer < -0.36f) sign = -1;
            if (speed > 24f && sign != 0 && lastSteerSign != 0 && sign != lastSteerSign) {
                slalomSwitches++;
            }
            if (sign != 0) lastSteerSign = sign;
        }
        if (checkpointIndex >= checkpointCount) {
            int reward = slalomSwitches >= 5 ? 385 : 300;
            complete(reward, "Slalom parkuru tamamlandı", false);
        } else if (challengeTimer > 48f) {
            complete(120, "Slalom denemesi: checkpoint " + checkpointIndex + "/" + checkpointCount, true);
        }
    }

    private void updateDrift(VehicleController vehicle, float step, float x, float z) {
        boolean inDriftZone = distance(x, z, 78f, 78f) <= 52f;
        if (vehicle.isDrifting() && inDriftZone) driftMeters += step;
        if (checkpointIndex >= checkpointCount && driftMeters >= 90f) {
            complete(430, "Drift parkuru: " + Math.round(driftMeters) + " m", false);
        } else if (driftMeters >= 120f) {
            complete(390, "Drift mesafesi: " + Math.round(driftMeters) + " m", false);
        } else if (!inDriftZone && challengeTimer > 4f && missedWarningTimer <= 0f) {
            missedWarningTimer = 2.0f;
            message = "Drift halkasının içine gir";
        } else if (challengeTimer > 52f) {
            complete(125, "Drift denemesi: " + Math.round(driftMeters) + " m", true);
        }
    }

    private void updateNitro(InputState input, float speed, float dt) {
        if (input != null && input.nitro > 0.35f && speed > 55f) nitroTime += dt;
        if (checkpointIndex >= checkpointCount && nitroTime >= 4.0f && maxSpeed >= 100f) {
            complete(410, "Nitro koridoru tamamlandı", false);
        } else if (checkpointIndex >= checkpointCount && nitroTime < 4.0f) {
            message = "Koridordasın: nitroyu daha uzun kullan";
        } else if (challengeTimer > 40f) {
            complete(115, "Nitro denemesi: " + formatOne(nitroTime) + " sn", true);
        }
    }

    private void updateNoDamage(VehicleController vehicle) {
        float lost = noDamageStartHealth - vehicle.getHealth01();
        if (lost > 0.025f) {
            complete(70, "Hasar aldın: parkur düşük ödülle bitti", true);
            return;
        }
        if (checkpointIndex >= checkpointCount) {
            complete(460, "Hasarsız parkur tamamlandı", false);
        } else if (challengeTimer > 70f) {
            complete(120, "Hasarsız deneme: checkpoint " + checkpointIndex + "/" + checkpointCount, true);
        }
    }

    private void updateTopSpeed(float speed) {
        if (checkpointIndex >= checkpointCount && maxSpeed >= 145f) {
            complete(470, "Maksimum hız parkuru: " + Math.round(maxSpeed) + " km/s", false);
        } else if (maxSpeed >= 145f && checkpointIndex < checkpointCount) {
            message = "Hız tamam, mor bitiş çizgisine gir";
        } else if (challengeTimer > 50f) {
            int reward = maxSpeed >= 120f ? 260 : 110;
            complete(reward, "Maksimum hız denemesi: " + Math.round(maxSpeed) + " km/s", true);
        }
    }

    private void startChallenge(VehicleController vehicle) {
        elapsed = 0f;
        challengeTimer = 0f;
        resultTimer = 0f;
        resultVisible = false;
        rewardGiven = false;
        failed = false;
        lastReward = 0;
        lastResult = "";
        armed = false;
        distance = 0f;
        maxSpeed = 0f;
        averageSpeedAccum = 0f;
        averageSpeedSamples = 0;
        driftMeters = 0f;
        nitroTime = 0f;
        slalomSwitches = 0;
        lastSteerSign = 0;
        brakeStartSpeed = 0f;
        missedWarningTimer = 0f;
        checkpointIndex = 0;
        checkpointCount = 0;
        if (vehicle != null) {
            startX = vehicle.position.x;
            startZ = vehicle.position.z;
            lastX = startX;
            lastZ = startZ;
            noDamageStartHealth = vehicle.getHealth01();
        } else {
            startX = 0f;
            startZ = 0f;
            lastX = 0f;
            lastZ = 0f;
            noDamageStartHealth = 1f;
        }
        setupCourse();
        lastTargetDistance = checkpointCount > 0 && vehicle != null
                ? distance(vehicle.position.x, vehicle.position.z, checkpointX[0], checkpointZ[0])
                : 0f;
        objective = challengeObjective();
        message = "Parkur: " + challengeTitle();
    }

    private void setupCourse() {
        switch (challengeIndex) {
            case CH_ACCEL:
                addCheckpoint(0f, 0f, 13f);
                addCheckpoint(0f, 45f, 13f);
                addCheckpoint(0f, 95f, 13f);
                addCheckpoint(0f, 145f, 16f);
                break;
            case CH_BRAKE:
                addCheckpoint(22f, 0f, 14f);
                addCheckpoint(22f, 55f, 14f);
                addCheckpoint(22f, 105f, 14f);
                addCheckpoint(22f, 135f, 18f);
                break;
            case CH_SLALOM:
                addCheckpoint(-26f, 20f, 14f);
                addCheckpoint(26f, 42f, 14f);
                addCheckpoint(-26f, 64f, 14f);
                addCheckpoint(26f, 86f, 14f);
                addCheckpoint(-26f, 108f, 14f);
                addCheckpoint(26f, 130f, 15f);
                break;
            case CH_DRIFT:
                addCheckpoint(78f, 52f, 17f);
                addCheckpoint(106f, 78f, 17f);
                addCheckpoint(78f, 104f, 17f);
                addCheckpoint(50f, 78f, 17f);
                break;
            case CH_NITRO:
                addCheckpoint(-70f, -20f, 13f);
                addCheckpoint(-70f, 30f, 13f);
                addCheckpoint(-70f, 85f, 13f);
                addCheckpoint(-70f, 140f, 16f);
                break;
            case CH_NO_DAMAGE:
                addCheckpoint(-85f, -85f, 16f);
                addCheckpoint(-85f, 85f, 16f);
                addCheckpoint(85f, 85f, 16f);
                addCheckpoint(85f, -85f, 16f);
                addCheckpoint(0f, -120f, 18f);
                break;
            case CH_TOP_SPEED:
                addCheckpoint(0f, -115f, 15f);
                addCheckpoint(0f, -50f, 14f);
                addCheckpoint(0f, 30f, 14f);
                addCheckpoint(0f, 120f, 14f);
                addCheckpoint(0f, 165f, 18f);
                break;
            default:
                addCheckpoint(0f, 0f, DEFAULT_GATE_RADIUS);
                break;
        }
    }

    private void addCheckpoint(float x, float z, float radius) {
        if (checkpointCount >= MAX_CHECKPOINTS) return;
        checkpointX[checkpointCount] = x;
        checkpointZ[checkpointCount] = z;
        checkpointRadius[checkpointCount] = Math.max(4f, radius);
        checkpointCount++;
    }

    private void complete(int reward, String result, boolean softFail) {
        if (resultVisible) return;
        failed = softFail;
        int adjustedReward = Math.max(0, reward + bonusReward(softFail));
        lastReward = adjustedReward;
        lastResult = result == null ? "Test tamamlandı" : result;
        resultVisible = true;
        resultTimer = 0f;
        message = (softFail ? "BAŞARISIZ: " : "BAŞARILI: ") + lastResult + "  +" + lastReward + " coin";
        if (!rewardGiven && saveManager != null) {
            saveManager.addCoins(lastReward);
            saveManager.addXp(Math.max(20, lastReward / 4));
            saveManager.incrementDailyMissionCompleted();
            saveManager.setEconomyLastMessage("TEST PARKURU: " + lastResult + "  +" + lastReward + " coin");
            saveManager.setLastTestDriveResult(lastResult, lastReward);
            rewardGiven = true;
        }
    }

    private int bonusReward(boolean softFail) {
        if (softFail) return 0;
        int bonus = 0;
        if (challengeTimer > 0f && challengeTimer <= 18f) bonus += 35;
        if (noDamageStartHealth > 0f && active) bonus += 15;
        if (maxSpeed >= 150f) bonus += 25;
        return bonus;
    }

    public String getTitle() {
        if (!active) return "-";
        return resultVisible ? "TEST SONUCU" : challengeTitle();
    }

    public String getProgressText() {
        if (!active) return "-";
        if (resultVisible) return (failed ? "Başarısız" : "Başarılı") + "  |  Sıradaki test: " + nextTitle();
        String cp = "CP " + Math.min(checkpointIndex, checkpointCount) + "/" + checkpointCount;
        String dist = checkpointIndex < checkpointCount ? ("  hedef " + Math.round(getDistanceToNextCheckpoint()) + " m") : "  son çizgi";
        switch (challengeIndex) {
            case CH_ACCEL:
                return cp + dist + "  0-100 süre " + formatOne(challengeTimer) + " sn  max " + Math.round(maxSpeed);
            case CH_BRAKE:
                return cp + dist + "  fren alanı  hız " + Math.round(maxSpeed);
            case CH_SLALOM:
                return cp + dist + "  yön değişimi " + slalomSwitches;
            case CH_DRIFT:
                return cp + dist + "  drift " + Math.round(driftMeters) + "/120 m";
            case CH_NITRO:
                return cp + dist + "  nitro " + formatOne(nitroTime) + "/4.0 sn";
            case CH_NO_DAMAGE:
                return cp + dist + "  hasarsız mesafe " + Math.round(distance) + " m";
            case CH_TOP_SPEED:
                return cp + dist + "  maks hız " + Math.round(maxSpeed) + "/145";
            default:
                return cp + dist;
        }
    }

    public String getObjectiveText() {
        return objective == null ? "" : objective;
    }

    public String getMessage() {
        return message == null ? "" : message;
    }

    public int getRewardFlash() {
        if (resultVisible && resultTimer < 2.0f) return lastReward;
        return 0;
    }

    public boolean isResultVisible() {
        return resultVisible;
    }

    public String getLastResult() {
        return lastResult;
    }

    public int getLastReward() {
        return lastReward;
    }

    public int getChallengeIndex() {
        return challengeIndex;
    }

    public int getCheckpointIndex() {
        return checkpointIndex;
    }

    public int getCheckpointTotal() {
        return checkpointCount;
    }

    public float getTargetX() {
        if (checkpointIndex >= checkpointCount) return checkpointCount > 0 ? checkpointX[checkpointCount - 1] : 0f;
        return checkpointX[checkpointIndex];
    }

    public float getTargetZ() {
        if (checkpointIndex >= checkpointCount) return checkpointCount > 0 ? checkpointZ[checkpointCount - 1] : 0f;
        return checkpointZ[checkpointIndex];
    }

    public float getDistanceToNextCheckpoint() {
        if (checkpointIndex >= checkpointCount) return 0f;
        return distance(lastX, lastZ, checkpointX[checkpointIndex], checkpointZ[checkpointIndex]);
    }

    public float getAverageSpeed() {
        return averageSpeedSamples <= 0 ? 0f : averageSpeedAccum / (float)averageSpeedSamples;
    }

    private String challengeObjective() {
        switch (challengeIndex) {
            case CH_ACCEL: return "Başlangıç çizgisinden çık, mavi kapıları takip et ve bitişte 100 km/s üstüne çık.";
            case CH_BRAKE: return "Hızlanma kapılarından geç, kırmızı durma alanında frenle tamamen dur.";
            case CH_SLALOM: return "Sağ-sol kapılardan sırayla geç; hiçbir checkpoint atlama.";
            case CH_DRIFT: return "Drift halkasında kapıları dolaş ve 90 m üstü drift topla.";
            case CH_NITRO: return "Nitro koridorunda kapıları takip et, 4 sn nitro ve 100 km/s üstü hız yap.";
            case CH_NO_DAMAGE: return "Yeşil güvenli rotayı hasar almadan tamamla.";
            case CH_TOP_SPEED: return "Mor hız koridorunu takip et ve bitişe kadar 145 km/s üstünü gör.";
            default: return "Checkpointleri takip et.";
        }
    }

    private String challengeTitle() {
        return challengeLabel(challengeIndex).toUpperCase(java.util.Locale.US);
    }

    private String nextTitle() {
        int next = (challengeIndex + 1) % CH_COUNT;
        return challengeLabel(next);
    }

    public static String challengeLabel(int index) {
        switch (clamp(index, 0, CH_COUNT - 1)) {
            case CH_ACCEL: return "0-100 Hızlanma";
            case CH_BRAKE: return "Fren Testi";
            case CH_SLALOM: return "Slalom / Dönüş";
            case CH_DRIFT: return "Drift Parkuru";
            case CH_NITRO: return "Nitro Koridoru";
            case CH_NO_DAMAGE: return "Hasarsız Sürüş";
            case CH_TOP_SPEED: return "Maksimum Hız";
            default: return "Test Sürüşü";
        }
    }

    private static float distance(float ax, float az, float bx, float bz) {
        float dx = bx - ax;
        float dz = bz - az;
        return (float)Math.sqrt(dx * dx + dz * dz);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static String formatOne(float value) {
        return String.format(java.util.Locale.US, "%.1f", value);
    }
}
