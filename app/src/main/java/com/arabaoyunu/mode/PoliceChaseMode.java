package com.arabaoyunu.mode;

import com.arabaoyunu.ai.AiPerformanceTuner;
import com.arabaoyunu.input.InputState;
import com.arabaoyunu.map.MapManager;
import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;
import com.arabaoyunu.util.SaveManager;

/**
 * ArabaOyunu_31: Polis kovalamaca modu.
 *
 * İlk gerçek polis sistemi:
 * - 3 polis aracı
 * - Oyuncuyu takip eden basit AI
 * - Aranma seviyesi
 * - Yakalanma sayacı
 * - Kaçış süresi
 * - Siren/HUD bağlantısı
 * - Çarpışma hasarı
 * - Kaçış ödülü
 */
public final class PoliceChaseMode extends BaseGameMode {

    private static final int POLICE_COUNT = 3;
    // A62.7: mesafe/süre/zorluk artık PoliceChaseSystem üzerinden aranma seviyesine göre hesaplanır.

    private final VehicleController car;
    private final MapManager mapManager;
    private final SaveManager saveManager;
    private final PoliceUnit[] units = new PoliceUnit[POLICE_COUNT];

    private float chaseTime;
    private float escapeTimer;
    private float caughtTimer;
    private float spawnTimer;
    private float sirenPulse;
    private float countdown;
    private float policeCollisionCooldown;
    private float recentCollisionPressure;
    private float starMessageTimer;
    private float antiStuckTimer;
    private int wantedLevel;
    private int lastWantedLevel;
    private int peakWantedLevel;
    private boolean rewardFinalized;
    private boolean finished;
    private boolean escaped;
    private boolean caught;
    private int earnedCoins;
    private int earnedXp;
    private String status = "KAC!";
    private String resultTitle = "";
    private float messageTimer;
    private int graphicsQuality = AiPerformanceTuner.QUALITY_HIGH;

    public PoliceChaseMode(VehicleController car, MapManager mapManager, SaveManager saveManager) {
        this.car = car;
        this.mapManager = mapManager;
        this.saveManager = saveManager;
        for (int i = 0; i < units.length; i++) units[i] = new PoliceUnit();
    }

    @Override
    public String getName() {
        return "PoliceChaseMode";
    }

    @Override
    public void start() {
        if (mapManager != null && mapManager.getCurrentMap() != null) {
            car.reset(0f, 0.42f, -96f, 0f);
        }
        chaseTime = 0f;
        escapeTimer = 0f;
        caughtTimer = 0f;
        spawnTimer = 0f;
        sirenPulse = 0f;
        policeCollisionCooldown = 0f;
        recentCollisionPressure = 0f;
        starMessageTimer = 0f;
        antiStuckTimer = 0f;
        countdown = 3.2f;
        wantedLevel = 1;
        lastWantedLevel = 1;
        peakWantedLevel = 1;
        rewardFinalized = false;
        finished = false;
        escaped = false;
        caught = false;
        earnedCoins = 0;
        earnedXp = 0;
        status = "HAZIRLAN";
        resultTitle = "";
        messageTimer = 0f;

        for (int i = 0; i < units.length; i++) {
            resetUnit(units[i], i);
        }
    }

    @Override
    public void update(float dt, InputState input) {
        if (dt <= 0f) return;
        if (dt > 0.085f) dt = 0.085f;

        if (finished) {
            if (messageTimer > 0f) messageTimer -= dt;
            if (input != null && (input.throttle > 0.35f || input.pausePressed)) {
                start();
            }
            return;
        }

        if (countdown > 0f) {
            countdown = Math.max(0f, countdown - dt);
            status = countdown > 0.05f ? ("BAŞLANGIÇ " + (int)Math.ceil(countdown)) : "BAŞLA!";
            return;
        }

        graphicsQuality = input == null ? graphicsQuality : AiPerformanceTuner.clampQuality(input.graphicsQuality);

        if (mapManager != null && mapManager.getCurrentMap() != null) {
            car.update(dt, input, mapManager.getCurrentMap());
        }

        chaseTime += dt;
        float damage01 = 1f - car.getHealth01();
        int computedWanted = PoliceChaseSystem.computeWantedLevel(saveManager, chaseTime, car.getSpeedKmh(), damage01);
        if (computedWanted > wantedLevel) {
            starMessageTimer = 2.1f;
            status = "YILDIZ SEVIYESI ARTTI";
        }
        lastWantedLevel = wantedLevel;
        wantedLevel = computedWanted;
        peakWantedLevel = Math.max(peakWantedLevel, wantedLevel);
        if (recentCollisionPressure > 0f) recentCollisionPressure = Math.max(0f, recentCollisionPressure - dt);
        if (starMessageTimer > 0f) starMessageTimer = Math.max(0f, starMessageTimer - dt);
        antiStuckTimer += dt;
        spawnTimer += dt;
        sirenPulse += dt;

        updatePoliceUnits(dt);
        updateChaseState(dt);
        updatePoliceCollisions(dt);
    }

    private void updatePoliceUnits(float dt) {
        for (int i = 0; i < units.length; i++) {
            PoliceUnit p = units[i];
            float dx = car.position.x - p.x;
            float dz = car.position.z - p.z;
            float dist = (float)Math.sqrt(dx * dx + dz * dz);
            if (dist < 0.001f) dist = 0.001f;

            if (PoliceAiController.shouldSoftRespawn(dist, p.disabledTimer, p.speed)) {
                respawnNearPlayer(p, i);
                dx = car.position.x - p.x;
                dz = car.position.z - p.z;
                dist = (float)Math.sqrt(dx * dx + dz * dz);
                if (dist < 0.001f) dist = 0.001f;
            }

            float carForwardX = (float)Math.sin(car.yaw);
            float carForwardZ = (float)Math.cos(car.yaw);
            float carRightX = (float)Math.cos(car.yaw);
            float carRightZ = (float)-Math.sin(car.yaw);

            float interceptTime = Math.min(1.55f, dist / 46f);
            float predictedX = car.position.x + car.velocity.x * interceptTime;
            float predictedZ = car.position.z + car.velocity.z * interceptTime;
            float sideOffset = PoliceAiController.targetSideOffset(i, dist);
            float backOffset = PoliceAiController.targetBackOffset(wantedLevel, i, dist);
            float targetX = predictedX - carForwardX * backOffset + carRightX * sideOffset;
            float targetZ = predictedZ - carForwardZ * backOffset + carRightZ * sideOffset;
            float tx = targetX - p.x;
            float tz = targetZ - p.z;
            float targetYaw = (float)Math.atan2(tx, tz);
            float aggression = AiPerformanceTuner.policeAggression(wantedLevel, graphicsQuality);
            p.yaw = approachAngle(p.yaw, targetYaw, dt * PoliceAiController.turnRate(wantedLevel, dist, aggression));

            float desiredSpeed = PoliceAiController.desiredSpeed(dist, car.getSpeedKmh(), wantedLevel, i, aggression);
            if (p.hitCooldown > 0f) desiredSpeed *= 0.40f;

            p.speed += (desiredSpeed - p.speed) * Math.min(1f, dt * (2.05f + wantedLevel * 0.10f));
            p.x += (float)Math.sin(p.yaw) * p.speed * dt;
            p.z += (float)Math.cos(p.yaw) * p.speed * dt;
            p.wheelSpin += (p.speed / 0.34f) * dt;

            // A65.7: polis oyuncunun içine girerse sert çarpma yerine yumuşak ayrıştırma uygulanır.
            float ndx = p.x - car.position.x;
            float ndz = p.z - car.position.z;
            float nDist = (float)Math.sqrt(ndx * ndx + ndz * ndz);
            if (nDist > 0.001f && nDist < 2.85f) {
                float push = (2.85f - nDist) * 0.42f;
                p.x += (ndx / nDist) * push;
                p.z += (ndz / nDist) * push;
                p.speed *= 0.72f;
            }

            if (p.hitCooldown > 0f) {
                p.hitCooldown -= dt;
                p.disabledTimer += dt;
            } else {
                p.disabledTimer = Math.max(0f, p.disabledTimer - dt * 0.78f);
            }
        }
    }

    private void updateChaseState(float dt) {
        float nearest = computeNearestPoliceDistance();
        float escapeDistance = PoliceChaseSystem.escapeDistance(wantedLevel);
        float escapeRequired = PoliceChaseSystem.escapeRequiredSeconds(wantedLevel);

        boolean recentHit = recentCollisionPressure > 0.05f;
        float captureGain = PoliceAiController.captureGainPerSecond(nearest, car.getSpeedKmh(), wantedLevel, recentHit);
        if (captureGain > 0f) {
            caughtTimer = Math.min(getCaughtRequired(), caughtTimer + dt * captureGain);
        } else {
            caughtTimer = Math.max(0f, caughtTimer - dt * PoliceAiController.captureRecoverPerSecond(nearest, car.getSpeedKmh(), wantedLevel));
        }

        // 90 saniyelik kaçış hedefi korunur; güvenli mesafe yakalanma baskısını azaltır.
        if (nearest > escapeDistance && car.getSpeedKmh() > 70f) {
            escapeTimer = Math.min(escapeRequired, escapeTimer + dt);
            caughtTimer = Math.max(0f, caughtTimer - dt * 0.70f);
        } else {
            escapeTimer = Math.max(0f, escapeTimer - dt * 0.42f);
        }

        if (caughtTimer >= getCaughtRequired() || car.getHealth01() <= 0.06f) {
            finish(false);
            return;
        }

        if (chaseTime >= PoliceChaseSystem.targetEscapeSeconds()) {
            finish(true);
            return;
        }

        float remaining = Math.max(0f, PoliceChaseSystem.targetEscapeSeconds() - chaseTime);
        float capturePercent = PoliceChaseSystem.capturePercent(caughtTimer, wantedLevel);
        String risk = PoliceAiController.riskText(nearest, capturePercent, wantedLevel, starMessageTimer > 0f && wantedLevel > lastWantedLevel);
        if (starMessageTimer > 0f && wantedLevel > lastWantedLevel) {
            status = PoliceChaseSystem.wantedStars(wantedLevel) + " " + risk;
        } else if (capturePercent >= 58f || nearest < 18f) {
            status = PoliceChaseSystem.wantedStars(wantedLevel) + " " + risk + " • " + (int)Math.ceil(remaining) + "sn";
        } else if (escapeTimer > 0f) {
            status = PoliceChaseSystem.wantedStars(wantedLevel) + " GUVENLI MESAFE " + (int)escapeTimer + "sn";
        } else {
            status = PoliceAiController.aiBalanceLine(nearest, capturePercent, wantedLevel) + " • Hedef " + (int)Math.ceil(remaining) + "sn";
        }
    }

    private void updatePoliceCollisions(float dt) {
        if (policeCollisionCooldown > 0f) {
            policeCollisionCooldown -= dt;
            return;
        }
        for (int i = 0; i < units.length; i++) {
            PoliceUnit p = units[i];
            float dx = car.position.x - p.x;
            float dz = car.position.z - p.z;
            if (dx * dx + dz * dz < 3.65f * 3.65f) {
                float relative = Math.max(13f, Math.abs(car.getSpeedKmh() / 3.6f - p.speed) + 12f);
                car.applyExternalImpact(relative);
                car.velocity.x *= -0.22f;
                car.velocity.z *= -0.22f;
                p.speed *= 0.30f;
                p.hitCooldown = 1.55f;
                p.disabledTimer += 1.1f;
                policeCollisionCooldown = 0.62f;
                recentCollisionPressure = 1.25f;
                caughtTimer = Math.min(getCaughtRequired(), caughtTimer + 0.28f + wantedLevel * 0.04f);
                return;
            }
        }
    }

    private void finish(boolean escape) {
        if (finished || rewardFinalized) return;
        rewardFinalized = true;
        finished = true;
        escaped = escape;
        caught = !escape;
        boolean newBestEscape = escape && saveManager != null && Math.round(chaseTime) > saveManager.getPoliceBestSeconds();
        earnedCoins = RewardBalanceSystem.balancedPoliceCoins(escape, peakWantedLevel, chaseTime, newBestEscape, saveManager == null ? 0 : saveManager.getPoliceTotalChases());
        earnedXp = RewardBalanceSystem.balancedPoliceXp(escape, peakWantedLevel, chaseTime);
        resultTitle = PoliceChaseResultSystem.resultTitle(escape);
        status = PoliceChaseResultSystem.resultLine(saveManager, escape, peakWantedLevel, Math.round(chaseTime), earnedCoins, earnedXp, newBestEscape)
                + " • " + ModeProgressBridgeSystem.resultCardFooter(saveManager);
        if (saveManager != null) {
            if (earnedCoins > 0) saveManager.addCoins(earnedCoins);
            if (earnedXp > 0) saveManager.addXp(earnedXp);
            saveManager.recordPoliceChaseResult(escape, peakWantedLevel, Math.round(chaseTime), earnedCoins, earnedXp);
            if (escape) saveManager.savePoliceBestIfHigher(Math.round(chaseTime));
        }
        messageTimer = 5f;
    }

    public void render(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        if (renderer == null || vp == null) return;
        for (int i = 0; i < units.length; i++) {
            renderUnit(renderer, vp, units[i], i, stats);
        }

        // Polis bölgesi / kaçış çizgisi göstergesi.
        float nearest = computeNearestPoliceDistance();
        float escapeDistance = PoliceChaseSystem.escapeDistance(wantedLevel);
        if (!finished && nearest > escapeDistance * 0.72f) {
            renderer.drawCircle(vp, car.position.x, 0.07f, car.position.z, escapeDistance, stats);
        }
    }

    private void renderUnit(PrimitiveRenderer renderer, float[] vp, PoliceUnit p, int index, RenderStats stats) {
        float dim = p.hitCooldown > 0f ? 0.62f : 1f;
        renderer.drawBox(vp, p.x, 0.60f, p.z, 1.78f, 1.14f, 4.35f, p.yaw,
                0.06f * dim, 0.08f * dim, 0.11f * dim, 1f, stats);
        renderer.drawBox(vp, p.x, 1.22f, p.z - (float)Math.cos(p.yaw) * 0.18f, 1.18f, 0.36f, 1.75f, p.yaw,
                0.02f, 0.035f, 0.055f, 1f, stats);

        // Polis üst siren barı: kırmızı/mavi blink.
        boolean red = ((int)(sirenPulse * 6f + index) & 1) == 0;
        renderer.drawBox(vp, p.x - 0.38f, 1.52f, p.z, 0.46f, 0.16f, 0.72f, p.yaw,
                red ? 1f : 0.08f, 0.03f, red ? 0.05f : 1f, 1f, stats);
        renderer.drawBox(vp, p.x + 0.38f, 1.52f, p.z, 0.46f, 0.16f, 0.72f, p.yaw,
                red ? 0.08f : 1f, 0.03f, red ? 1f : 0.05f, 1f, stats);

        float sin = (float)Math.sin(p.yaw);
        float cos = (float)Math.cos(p.yaw);
        float rightX = cos;
        float rightZ = -sin;
        float forwardX = sin;
        float forwardZ = cos;
        float front = 2.25f;
        float rear = -2.25f;
        float side = 0.74f;
        renderer.drawBox(vp, p.x + forwardX * front - rightX * side, 0.84f, p.z + forwardZ * front - rightZ * side, 0.14f, 0.08f, 0.08f, p.yaw, 0.90f, 0.96f, 1f, 1f, stats);
        renderer.drawBox(vp, p.x + forwardX * front + rightX * side, 0.84f, p.z + forwardZ * front + rightZ * side, 0.14f, 0.08f, 0.08f, p.yaw, 0.90f, 0.96f, 1f, 1f, stats);
        renderer.drawBox(vp, p.x + forwardX * rear - rightX * side, 0.84f, p.z + forwardZ * rear - rightZ * side, 0.14f, 0.08f, 0.08f, p.yaw, 1f, 0.04f, 0.03f, 1f, stats);
        renderer.drawBox(vp, p.x + forwardX * rear + rightX * side, 0.84f, p.z + forwardZ * rear + rightZ * side, 0.14f, 0.08f, 0.08f, p.yaw, 1f, 0.04f, 0.03f, 1f, stats);
    }

    private void resetUnit(PoliceUnit p, int i) {
        p.x = -18f + i * 18f;
        p.z = -136f - i * 18f;
        p.yaw = 0f;
        p.speed = 0f;
        p.hitCooldown = 0f;
        p.disabledTimer = 0f;
        p.wheelSpin = 0f;
    }

    private void respawnNearPlayer(PoliceUnit p, int i) {
        float angle = car.yaw + (float)Math.PI + (i - 1) * 0.72f;
        float dist = PoliceAiController.respawnDistance(car.getSpeedKmh(), i);
        p.x = car.position.x + (float)Math.sin(angle) * dist;
        p.z = car.position.z + (float)Math.cos(angle) * dist;
        p.yaw = car.yaw;
        p.speed = 10f;
        p.hitCooldown = 0f;
        p.disabledTimer = 0f;
    }

    private float computeNearestPoliceDistance() {
        float best = 99999f;
        for (int i = 0; i < units.length; i++) {
            float d = distance(units[i].x, units[i].z, car.position.x, car.position.z);
            if (d < best) best = d;
        }
        return best;
    }

    private static float approachAngle(float current, float target, float amount) {
        float diff = wrapAngle(target - current);
        if (diff > amount) diff = amount;
        if (diff < -amount) diff = -amount;
        return current + diff;
    }

    private static float wrapAngle(float a) {
        while (a > Math.PI) a -= (float)Math.PI * 2f;
        while (a < -Math.PI) a += (float)Math.PI * 2f;
        return a;
    }

    private static float distance(float ax, float az, float bx, float bz) {
        float dx = ax - bx;
        float dz = az - bz;
        return (float)Math.sqrt(dx * dx + dz * dz);
    }

    public boolean isActiveChase() { return !finished; }
    public boolean isFinished() { return finished; }
    public boolean isEscaped() { return escaped; }
    public boolean isCaught() { return caught; }
    public int getWantedLevel() { return wantedLevel; }
    public int getPeakWantedLevel() { return peakWantedLevel; }
    public float getCountdown() { return countdown; }
    public float getTimeRemaining() { return Math.max(0f, PoliceChaseSystem.targetEscapeSeconds() - chaseTime); }
    public float getCapturePercent() { return PoliceChaseSystem.capturePercent(caughtTimer, wantedLevel); }
    public String getRiskStateText() { return PoliceAiController.riskText(getNearestPoliceDistance(), getCapturePercent(), wantedLevel, false); }
    public float getChaseTime() { return chaseTime; }
    public float getEscapeTimer() { return Math.min(chaseTime, PoliceChaseSystem.targetEscapeSeconds()); }
    public float getEscapeRequired() { return PoliceChaseSystem.targetEscapeSeconds(); }
    public float getCaughtTimer() { return caughtTimer; }
    public float getCaughtRequired() { return PoliceChaseSystem.caughtRequiredSeconds(wantedLevel); }
    public float getNearestPoliceDistance() { return computeNearestPoliceDistance(); }
    public int getEarnedCoins() { return earnedCoins; }
    public int getEarnedXp() { return earnedXp; }
    public String getStatusText() { return status; }
    public String getResultTitle() { return resultTitle; }
    public String getWantedStarsText() { return PoliceChaseSystem.wantedStars(wantedLevel); }

    private static final class PoliceUnit {
        float x;
        float z;
        float yaw;
        float speed;
        float wheelSpin;
        float hitCooldown;
        float disabledTimer;
    }
}
