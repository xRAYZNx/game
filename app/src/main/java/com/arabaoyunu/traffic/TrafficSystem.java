package com.arabaoyunu.traffic;

import com.arabaoyunu.ai.AiPerformanceTuner;
import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;

/**
 * A63.4: Hafif NPC trafik sistemi.
 *
 * - Serbest sürüş ve polis modunda aktif olur.
 * - Trafik araçları şerit/koridor düzeninde ilerler.
 * - Yakın geçiş, risk combo ve trafik çarpışma olayları üretir.
 * - GLB/texture yüklemez; düşük cihazlar için primitive araç gövdesi kullanır.
 */
public final class TrafficSystem {

    public static final int DENSITY_OFF = 0;
    public static final int DENSITY_LOW = 1;
    public static final int DENSITY_MEDIUM = 2;
    public static final int DENSITY_HIGH = 3;

    private static final int MAX_TRAFFIC = 24;
    private static final int MODE_FREE = 0;
    private static final int MODE_POLICE = 1;
    private static final int MODE_CHECKPOINT_LIGHT = 2;

    private final TrafficVehicle[] vehicles = new TrafficVehicle[MAX_TRAFFIC];

    private int density = DENSITY_MEDIUM;
    private int graphicsQuality = AiPerformanceTuner.QUALITY_HIGH;
    private int modeKey = MODE_FREE;
    private boolean enabledByMode = true;
    private boolean policeMode;
    private boolean checkpointMode;
    private float playerX;
    private float playerZ;
    private float framePhase;
    private int skippedFarUpdates;
    private int culledRenderObjects;
    private float dayNightTime;
    private int nearMissCount;
    private int collisionCount;
    private int riskCombo;
    private int bestSessionCombo;
    private int cleanNearMissStreak;
    private float comboTimer;
    private float lastCollisionTimer;
    private int pendingRewardCoins;
    private int pendingNearMissEvents;
    private int pendingCollisionEvents;
    private float messageTimer;
    private String lastEventMessage = "";

    public TrafficSystem() {
        for (int i = 0; i < vehicles.length; i++) {
            vehicles[i] = new TrafficVehicle();
            initVehicle(vehicles[i], i);
        }
    }

    public void setDensity(int density) {
        if (density < DENSITY_OFF) density = DENSITY_OFF;
        if (density > DENSITY_HIGH) density = DENSITY_HIGH;
        this.density = density;
    }

    /** A63.4: Modlara göre trafik şeritleri ve aktiflik ayrılır. */
    public void setModeContext(boolean freeDrive, boolean police, boolean checkpointLight) {
        int nextMode = police ? MODE_POLICE : (checkpointLight ? MODE_CHECKPOINT_LIGHT : MODE_FREE);
        boolean nextEnabled = freeDrive || police || checkpointLight;
        if (nextMode != modeKey || nextEnabled != enabledByMode) {
            modeKey = nextMode;
            enabledByMode = nextEnabled;
            policeMode = police;
            checkpointMode = checkpointLight;
            resetSessionState();
            for (int i = 0; i < vehicles.length; i++) initVehicle(vehicles[i], i);
        } else {
            policeMode = police;
            checkpointMode = checkpointLight;
        }
    }

    public void setPerformanceLevel(int graphicsQuality) {
        this.graphicsQuality = AiPerformanceTuner.clampQuality(graphicsQuality);
    }

    public int getSkippedFarUpdates() { return skippedFarUpdates; }
    public int getCulledRenderObjects() { return culledRenderObjects; }
    public int getDensity() { return density; }

    public String getDensityName() {
        if (!enabledByMode || density == DENSITY_OFF) return "KAPALI";
        if (density == DENSITY_LOW) return "AZ";
        if (density == DENSITY_HIGH) return "YOGUN";
        return "ORTA";
    }

    public boolean isNight() { return dayNightTime > 0.58f; }

    public int getActiveCount() {
        int c = 0;
        for (int i = 0; i < vehicles.length; i++) if (vehicles[i].active) c++;
        return c;
    }

    public int getNearMissCount() { return nearMissCount; }
    public int getCollisionCount() { return collisionCount; }
    public int getRiskCombo() { return riskCombo; }
    public int getBestSessionCombo() { return bestSessionCombo; }
    public int getCleanNearMissStreak() { return cleanNearMissStreak; }
    public String getLastEventMessage() { return messageTimer > 0f ? lastEventMessage : ""; }

    public int consumePendingRewardCoins() {
        int value = pendingRewardCoins;
        pendingRewardCoins = 0;
        return Math.max(0, value);
    }

    public int consumePendingNearMissEvents() {
        int value = pendingNearMissEvents;
        pendingNearMissEvents = 0;
        return Math.max(0, value);
    }

    public int consumePendingCollisionEvents() {
        int value = pendingCollisionEvents;
        pendingCollisionEvents = 0;
        return Math.max(0, value);
    }

    public void resetCounters() {
        nearMissCount = 0;
        collisionCount = 0;
        riskCombo = 0;
        bestSessionCombo = 0;
        cleanNearMissStreak = 0;
        pendingRewardCoins = 0;
        pendingNearMissEvents = 0;
        pendingCollisionEvents = 0;
        lastEventMessage = "";
        messageTimer = 0f;
    }

    private void resetSessionState() {
        skippedFarUpdates = 0;
        culledRenderObjects = 0;
        riskCombo = 0;
        comboTimer = 0f;
        pendingRewardCoins = 0;
        pendingNearMissEvents = 0;
        pendingCollisionEvents = 0;
        lastCollisionTimer = 0f;
        lastEventMessage = "";
        messageTimer = 0f;
    }

    public void update(float dt, VehicleController player) {
        if (dt <= 0f) return;
        if (dt > 0.08f) dt = 0.08f;

        if (player != null) {
            playerX = player.position.x;
            playerZ = player.position.z;
        }
        skippedFarUpdates = 0;
        framePhase += dt;
        if (framePhase > 10f) framePhase = 0f;

        dayNightTime += dt * 0.012f;
        if (dayNightTime > 1f) dayNightTime -= 1f;

        if (comboTimer > 0f) {
            comboTimer -= dt;
            if (comboTimer <= 0f) riskCombo = 0;
        }
        if (messageTimer > 0f) {
            messageTimer -= dt;
            if (messageTimer <= 0f) lastEventMessage = "";
        }

        int desired = desiredActiveCount();
        if (!enabledByMode || density == DENSITY_OFF || desired <= 0) {
            for (int i = 0; i < vehicles.length; i++) vehicles[i].active = false;
            return;
        }

        float farScale = AiPerformanceTuner.farAiUpdateScale(graphicsQuality);
        float farDistance = AiPerformanceTuner.aiRenderDistance(graphicsQuality) * 0.85f;
        float farDistanceSq = farDistance * farDistance;

        for (int i = 0; i < vehicles.length; i++) {
            TrafficVehicle v = vehicles[i];
            v.active = i < desired;
            if (!v.active) continue;

            float dx = v.x - playerX;
            float dz = v.z - playerZ;
            float distSq = dx * dx + dz * dz;
            boolean far = distSq > farDistanceSq;
            if (far && ((i + (int)(framePhase * 12f)) & 1) == 1) {
                updateVehicle(v, dt * farScale, i);
                skippedFarUpdates++;
            } else {
                updateVehicle(v, dt, i);
            }
            updateCollisionAndNearMiss(v, player, dt);
        }

        if (lastCollisionTimer > 0f) lastCollisionTimer -= dt;
    }

    public void render(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        if (renderer == null || vp == null) return;
        culledRenderObjects = 0;
        if (!enabledByMode || density == DENSITY_OFF) return;
        float renderDistance = AiPerformanceTuner.aiRenderDistance(graphicsQuality);
        float renderDistanceSq = renderDistance * renderDistance;
        for (int i = 0; i < vehicles.length; i++) {
            TrafficVehicle v = vehicles[i];
            if (!v.active) continue;
            float pdx = v.x - playerX;
            float pdz = v.z - playerZ;
            if (pdx * pdx + pdz * pdz > renderDistanceSq) {
                culledRenderObjects++;
                continue;
            }
            float nightBoost = isNight() ? 0.72f : 1f;
            float damagedDim = v.collidedRecently ? 0.72f : 1f;
            float sirenTint = policeMode && (i & 1) == 0 ? 0.18f : 0f;
            renderer.drawBox(vp, v.x, 0.62f, v.z, v.width, v.height, v.length, v.yaw,
                    clamp01(v.r * nightBoost * damagedDim + sirenTint),
                    clamp01(v.g * nightBoost * damagedDim),
                    clamp01(v.b * nightBoost * damagedDim + (policeMode ? 0.12f : 0f)),
                    1f, stats);
            renderer.drawBox(vp, v.x, 1.28f, v.z - (float)Math.cos(v.yaw) * 0.20f,
                    v.width * 0.68f, 0.42f, v.length * 0.42f, v.yaw,
                    0.08f * nightBoost, 0.12f * nightBoost, 0.16f * nightBoost, 1f, stats);
            renderLights(renderer, vp, v, stats);
            if (policeMode && i < 3) renderSiren(renderer, vp, v, stats);
        }
    }

    private void renderSiren(PrimitiveRenderer renderer, float[] vp, TrafficVehicle v, RenderStats stats) {
        float flash = ((int)(framePhase * 8f) & 1) == 0 ? 1f : 0.15f;
        renderer.drawBox(vp, v.x - 0.32f, 1.64f, v.z, 0.34f, 0.10f, 0.28f, v.yaw,
                flash, 0.03f, 0.05f, 1f, stats);
        renderer.drawBox(vp, v.x + 0.32f, 1.64f, v.z, 0.34f, 0.10f, 0.28f, v.yaw,
                0.05f, 0.10f, flash, 1f, stats);
    }

    private void renderLights(PrimitiveRenderer renderer, float[] vp, TrafficVehicle v, RenderStats stats) {
        float sin = (float) Math.sin(v.yaw);
        float cos = (float) Math.cos(v.yaw);
        float rightX = cos;
        float rightZ = -sin;
        float forwardX = sin;
        float forwardZ = cos;

        float front = v.length * 0.54f;
        float rear = -v.length * 0.54f;
        float side = v.width * 0.36f;
        float y = 0.86f;

        if (isNight()) {
            renderer.drawBox(vp, v.x + forwardX * front - rightX * side, y, v.z + forwardZ * front - rightZ * side,
                    0.18f, 0.08f, 0.10f, v.yaw, 0.92f, 0.96f, 1f, 1f, stats);
            renderer.drawBox(vp, v.x + forwardX * front + rightX * side, y, v.z + forwardZ * front + rightZ * side,
                    0.18f, 0.08f, 0.10f, v.yaw, 0.92f, 0.96f, 1f, 1f, stats);
        }

        renderer.drawBox(vp, v.x + forwardX * rear - rightX * side, y, v.z + forwardZ * rear - rightZ * side,
                0.14f, 0.08f, 0.08f, v.yaw, 1f, 0.03f, 0.02f, 1f, stats);
        renderer.drawBox(vp, v.x + forwardX * rear + rightX * side, y, v.z + forwardZ * rear + rightZ * side,
                0.14f, 0.08f, 0.08f, v.yaw, 1f, 0.03f, 0.02f, 1f, stats);
    }

    private int desiredActiveCount() {
        if (!enabledByMode || density == DENSITY_OFF) return 0;
        int base = AiPerformanceTuner.maxTrafficCount(density, isNight(), graphicsQuality);
        if (density == DENSITY_LOW) base = Math.max(base, policeMode ? 4 : 3);
        if (density == DENSITY_MEDIUM) base = Math.max(base, policeMode ? 7 : 6);
        if (density == DENSITY_HIGH) base = Math.max(base, policeMode ? 12 : 10);
        if (checkpointMode) base = Math.min(base, 5);
        return Math.min(MAX_TRAFFIC, base);
    }

    private void initVehicle(TrafficVehicle v, int i) {
        v.route = i % 4;
        v.lane = i % 3;
        v.vehicleType = i % 5;
        configureVehicleShape(v, i);
        float offset = ((i * 37) % 420) - 210f;
        float laneShift = (v.lane - 1) * 2.8f;
        float corridor = policeMode ? 18f : (checkpointMode ? 12f : 22f);
        float roadX = policeMode ? 11f : 8f;
        float roadZ = corridor;
        if (v.route == 0) {
            v.x = -roadX + laneShift * 0.18f;
            v.z = offset;
            v.yaw = 0f;
        } else if (v.route == 1) {
            v.x = roadX + laneShift * 0.18f;
            v.z = -offset;
            v.yaw = (float) Math.PI;
        } else if (v.route == 2) {
            v.x = offset;
            v.z = -roadZ + laneShift * 0.14f;
            v.yaw = (float) Math.PI * 0.5f;
        } else {
            v.x = -offset;
            v.z = roadZ + laneShift * 0.14f;
            v.yaw = (float) -Math.PI * 0.5f;
        }
        float modeBoost = policeMode ? 1.15f : (checkpointMode ? 0.9f : 1f);
        v.baseSpeed = (8.5f + (i % 5) * 1.35f) * modeBoost;
        v.speed = v.baseSpeed;
        v.r = 0.18f + (i % 4) * 0.17f;
        v.g = 0.22f + (i % 5) * 0.11f;
        v.b = 0.30f + (i % 3) * 0.19f;
        v.collidedRecently = false;
        v.cooldown = 0f;
        v.nearMissCooldown = 0.2f + (i % 4) * 0.1f;
    }

    private void configureVehicleShape(TrafficVehicle v, int i) {
        int t = i % 5;
        if (t == 1) { // hatchback
            v.length = 3.8f; v.width = 1.72f; v.height = 1.18f;
        } else if (t == 2) { // van
            v.length = 5.0f; v.width = 1.95f; v.height = 1.55f;
        } else if (t == 3) { // pickup
            v.length = 4.8f; v.width = 1.92f; v.height = 1.32f;
        } else if (t == 4) { // sport traffic
            v.length = 4.25f; v.width = 1.86f; v.height = 1.02f;
        } else { // sedan
            v.length = 4.35f; v.width = 1.78f; v.height = 1.18f;
        }
    }

    private void updateVehicle(TrafficVehicle v, float dt, int index) {
        float targetSpeed = v.baseSpeed * (isNight() ? 0.86f : 1f);
        if (v.collidedRecently) targetSpeed *= 0.45f;
        v.speed += (targetSpeed - v.speed) * Math.min(1f, dt * 2.4f);

        float dx = (float) Math.sin(v.yaw) * v.speed * dt;
        float dz = (float) Math.cos(v.yaw) * v.speed * dt;
        v.x += dx;
        v.z += dz;

        float limit = policeMode ? 250f : 230f;
        if (v.route == 0 && v.z > limit) v.z = -limit;
        else if (v.route == 1 && v.z < -limit) v.z = limit;
        else if (v.route == 2 && v.x > limit) v.x = -limit;
        else if (v.route == 3 && v.x < -limit) v.x = limit;

        if (v.cooldown > 0f) {
            v.cooldown -= dt;
            if (v.cooldown <= 0f) v.collidedRecently = false;
        }
    }

    private void updateCollisionAndNearMiss(TrafficVehicle v, VehicleController player, float dt) {
        if (player == null) return;
        float dx = player.position.x - v.x;
        float dz = player.position.z - v.z;
        float distSq = dx * dx + dz * dz;

        if (v.nearMissCooldown > 0f) v.nearMissCooldown -= dt;
        float speedKmh = player.getSpeedKmh();
        if (distSq < 8.6f * 8.6f && distSq > 3.15f * 3.15f && speedKmh > 78f && v.nearMissCooldown <= 0f) {
            registerNearMiss(speedKmh);
            v.nearMissCooldown = 1.15f;
        }

        if (distSq > 2.35f * 2.35f) return;
        if (v.cooldown > 0f || lastCollisionTimer > 0f) return;

        float playerSpeed = Math.max(0f, speedKmh / 3.6f);
        if (playerSpeed < 2.2f) return;
        float relative = Math.abs(playerSpeed - v.speed) + 4f;
        player.applyExternalImpact(relative);

        float dist = (float) Math.sqrt(Math.max(0.0001f, distSq));
        float nx = dx / dist;
        float nz = dz / dist;
        float intoTraffic = player.velocity.x * nx + player.velocity.z * nz;
        if (intoTraffic < 0f) {
            player.velocity.x -= nx * intoTraffic * 0.55f;
            player.velocity.z -= nz * intoTraffic * 0.55f;
        }
        player.velocity.x *= 0.72f;
        player.velocity.z *= 0.72f;
        player.position.x += nx * 0.18f;
        player.position.z += nz * 0.18f;

        v.collidedRecently = true;
        v.cooldown = 1.35f;
        lastCollisionTimer = 0.42f;
        collisionCount++;
        pendingCollisionEvents++;
        cleanNearMissStreak = 0;
        riskCombo = 0;
        comboTimer = 0f;
        lastEventMessage = "TRAFIK CARPISMASI";
        messageTimer = 2.4f;
    }

    private void registerNearMiss(float speedKmh) {
        nearMissCount++;
        pendingNearMissEvents++;
        riskCombo = Math.min(9, riskCombo + 1);
        bestSessionCombo = Math.max(bestSessionCombo, riskCombo);
        cleanNearMissStreak++;
        comboTimer = 4.2f;
        int base = speedKmh > 145f ? 50 : (speedKmh > 110f ? 38 : 25);
        int comboBonus = Math.min(75, riskCombo * 10);
        int policeBonus = policeMode ? 15 : 0;
        int coins = base + comboBonus + policeBonus;
        pendingRewardCoins += coins;
        lastEventMessage = "YAKIN GECIS x" + riskCombo + " +" + coins;
        messageTimer = 2.4f;
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
