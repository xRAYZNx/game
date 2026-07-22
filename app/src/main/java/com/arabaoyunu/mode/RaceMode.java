package com.arabaoyunu.mode;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.map.MapManager;
import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;
import com.arabaoyunu.util.SaveManager;

/**
 * A65.0: Checkpoint Yarışı artık rota seçimi + madalya + rota bazlı ilerleme ile çalışır.
 * Her yarış Open Field üzerinde kalır; açık dünya veya büyük GLB harita açılmaz.
 */
public final class RaceMode extends BaseGameMode {

    private final VehicleController car;
    private final MapManager mapManager;
    private final SaveManager saveManager;

    private int routeId = CheckpointRaceSystem.ROUTE_MEDIUM;
    private int checkpointCount = CheckpointRaceSystem.checkpointCount(CheckpointRaceSystem.ROUTE_MEDIUM);
    private float countdown;
    private float elapsed;
    private boolean running;
    private boolean finished;
    private boolean newBest;
    private boolean newMedal;
    private int nextCheckpoint;
    private int earnedCoins;
    private int earnedXp;
    private int finishPosition = 4;
    private String finishGrade = "-";
    private String message = "";
    private float messageTimer;
    private float wrongCheckpointTimer;
    private float checkpointFlashTimer;
    private int lastPassedCheckpoint;
    private float activePulse;
    private float bestBeforeStart;
    private String medalBeforeStart = "";
    private boolean rewardFinalized;
    private int completionsBeforeStart;
    private final RivalRaceSystem rival = new RivalRaceSystem();
    private boolean rivalWon;

    public RaceMode(VehicleController car, MapManager mapManager, SaveManager saveManager) {
        this.car = car;
        this.mapManager = mapManager;
        this.saveManager = saveManager;
    }

    @Override
    public String getName() { return "RaceMode"; }

    @Override
    public void start() {
        routeId = saveManager == null ? CheckpointRaceSystem.ROUTE_MEDIUM : saveManager.getSelectedCheckpointRoute();
        routeId = CheckpointRaceSystem.sanitizeRouteId(routeId);
        CheckpointRaceSystem.setActiveRoute(routeId);
        checkpointCount = CheckpointRaceSystem.checkpointCount(routeId);
        resetRaceCar();
        countdown = CheckpointRaceSystem.COUNTDOWN_SECONDS;
        elapsed = 0f;
        running = false;
        finished = false;
        newBest = false;
        newMedal = false;
        nextCheckpoint = 1;
        earnedCoins = 0;
        earnedXp = 0;
        finishPosition = 4;
        finishGrade = "-";
        message = RaceResultSystem.checkpointIntroText(routeId);
        messageTimer = 2.6f;
        wrongCheckpointTimer = 0f;
        checkpointFlashTimer = 0f;
        lastPassedCheckpoint = 0;
        activePulse = 0f;
        bestBeforeStart = saveManager == null ? 0f : saveManager.getCheckpointRouteBestSeconds(routeId);
        medalBeforeStart = saveManager == null ? "" : saveManager.getCheckpointRouteBestMedal(routeId);
        rewardFinalized = false;
        completionsBeforeStart = saveManager == null ? 0 : saveManager.getCheckpointRouteCompletedCount(routeId);
        rival.startCheckpoint(saveManager);
        rivalWon = false;
    }

    @Override
    public void stop() {
        running = false;
        message = "";
        messageTimer = 0f;
        wrongCheckpointTimer = 0f;
        checkpointFlashTimer = 0f;
    }

    @Override
    public void update(float dt, InputState input) {
        if (car == null || dt <= 0f) return;
        if (dt > 0.085f) dt = 0.085f;

        if (messageTimer > 0f) {
            messageTimer -= dt;
            if (messageTimer <= 0f) message = "";
        }
        if (wrongCheckpointTimer > 0f) wrongCheckpointTimer -= dt;
        if (checkpointFlashTimer > 0f) checkpointFlashTimer -= dt;
        activePulse += dt * 3.8f;

        if (finished) {
            if (messageTimer <= 0f && input != null && (input.pausePressed || input.throttle > 0.45f)) start();
            return;
        }

        InputState driveInput = input;
        if (countdown > 0f) {
            countdown -= dt;
            running = false;
            driveInput = null;
        } else {
            running = true;
            elapsed += dt;
        }
        rival.update(dt, running);

        if (mapManager != null && mapManager.getCurrentMap() != null) {
            car.update(dt, driveInput, mapManager.getCurrentMap());
        }

        if (!running) return;
        updateCheckpointProgress();
    }

    private void updateCheckpointProgress() {
        if (nextCheckpoint >= checkpointCount) { finishRace(); return; }
        float x = car.position.x;
        float z = car.position.z;
        if (distanceSq(x, z, cpX(nextCheckpoint), cpZ(nextCheckpoint)) <= CheckpointRaceSystem.CHECKPOINT_RADIUS * CheckpointRaceSystem.CHECKPOINT_RADIUS) {
            nextCheckpoint++;
            if (nextCheckpoint >= checkpointCount) {
                finishRace();
            } else {
                lastPassedCheckpoint = nextCheckpoint - 1;
                checkpointFlashTimer = 1.25f;
                message = RaceResultSystem.checkpointPassedText(lastPassedCheckpoint, getTotalLaps());
                messageTimer = 1.35f;
            }
            return;
        }
        for (int i = nextCheckpoint + 1; i < checkpointCount; i++) {
            if (distanceSq(x, z, cpX(i), cpZ(i)) <= CheckpointRaceSystem.CHECKPOINT_RADIUS * CheckpointRaceSystem.CHECKPOINT_RADIUS) {
                if (wrongCheckpointTimer <= 0f) {
                    message = RaceResultSystem.checkpointWrongWayText();
                    messageTimer = 1.55f;
                    wrongCheckpointTimer = 2.0f;
                }
                return;
            }
        }
    }

    private void finishRace() {
        if (finished) return;
        finished = true;
        running = false;
        finishGrade = CheckpointRaceSystem.medalForTime(routeId, elapsed);
        rivalWon = rival.isPlayerWinner(elapsed);
        finishPosition = Math.min(RaceModeSystem.positionForGrade(finishGrade), rivalWon ? 1 : 2);
        int oldRouteCompleted = Math.max(0, completionsBeforeStart);
        int oldMedalRank = saveManager == null ? 0 : saveManager.getCheckpointRouteMedalRank(routeId);
        int medalRank = CheckpointRaceSystem.medalRank(finishGrade);
        newBest = saveManager == null ? true : (bestBeforeStart <= 0f || elapsed < bestBeforeStart);
        newMedal = medalRank > oldMedalRank;
        earnedCoins = RewardBalanceSystem.balancedCheckpointCoins(routeId, finishGrade, newBest, newMedal, oldRouteCompleted);
        earnedXp = RewardBalanceSystem.balancedCheckpointXp(routeId, finishGrade, newBest, newMedal);
        if (saveManager != null && !rewardFinalized) {
            rewardFinalized = true;
            saveManager.addCoins(earnedCoins);
            saveManager.addXp(earnedXp);
            saveManager.saveRaceBestIfLower(elapsed); // genel checkpoint kartı için eski kayıt uyumluluğu
            saveManager.saveCheckpointRouteBestIfLower(routeId, elapsed);
            saveManager.recordCheckpointRaceResult(elapsed, earnedCoins, finishGrade);
            saveManager.recordCheckpointRouteResult(routeId, elapsed, earnedCoins, finishGrade, newBest, newMedal);
            saveManager.recordRivalRaceResult("Checkpoint " + CheckpointRaceSystem.routeLabel(routeId), rival.getDifficulty(), rivalWon, earnedCoins);
        }
        message = RaceResultSystem.checkpointRouteResultText(saveManager, routeId, rivalWon, elapsed, finishGrade, earnedCoins, earnedXp, newBest, newMedal,
                saveManager == null ? (newBest ? elapsed : bestBeforeStart) : saveManager.getCheckpointRouteBestSeconds(routeId))
                + " | " + CheckpointRaceSystem.repeatRewardRuleText(oldRouteCompleted, newBest, newMedal)
                + " | " + ModeProgressBridgeSystem.resultCardFooter(saveManager);
        messageTimer = 5f;
    }

    public void render(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        if (renderer == null || vp == null) return;
        drawRoute(renderer, vp, stats);
        drawCheckpointGates(renderer, vp, stats);
        drawRivalGhost(renderer, vp, stats);
        drawStartFinish(renderer, vp, stats);
        drawTargetLine(renderer, vp, stats);
    }

    private void drawRoute(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        for (int i = 0; i < checkpointCount - 1; i++) {
            boolean passed = i < nextCheckpoint - 1;
            renderer.drawLine(vp, cpX(i), 0.09f, cpZ(i), cpX(i + 1), 0.09f, cpZ(i + 1),
                    passed ? 0.14f : 0.05f, passed ? 0.85f : 0.55f, passed ? 0.24f : 1.0f, stats);
        }
    }

    private void drawCheckpointGates(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        for (int i = 0; i < checkpointCount; i++) {
            boolean start = i == 0;
            boolean finish = i == checkpointCount - 1;
            boolean target = i == nextCheckpoint;
            boolean passed = i < nextCheckpoint;
            float x = cpX(i);
            float z = cpZ(i);
            float radius = start || finish ? 19f : 13.5f;
            if (target) radius = CheckpointRaceSystem.ACTIVE_CHECKPOINT_RADIUS + (float)Math.sin(activePulse) * 1.6f;
            renderer.drawCircle(vp, x, 0.075f, z, radius, stats);
            if (target) {
                renderer.drawCircle(vp, x, 0.125f, z, radius + 4.2f, stats);
                drawTargetBeacon(renderer, vp, x, z, finish, stats);
            } else if (checkpointFlashTimer > 0f && passed && i == lastPassedCheckpoint) {
                renderer.drawCircle(vp, x, 0.145f, z, radius + 5.0f * checkpointFlashTimer, stats);
            }
            float[] side = gateSide(i);
            float sx = side[0];
            float sz = side[1];
            float half = radius * 0.72f;
            float p0x = x + sx * half;
            float p0z = z + sz * half;
            float p1x = x - sx * half;
            float p1z = z - sz * half;
            float r, g, b;
            if (finish) { r = 1.0f; g = target ? 0.84f : 0.58f; b = 0.10f; }
            else if (target) { r = 0.0f; g = 0.98f; b = 1.0f; }
            else if (passed) { r = 0.16f; g = 0.78f; b = 0.30f; }
            else { r = 0.20f; g = 0.36f; b = 0.92f; }
            float pillarHeight = target ? 3.40f : 2.70f;
            renderer.drawBox(vp, p0x, pillarHeight * 0.5f, p0z, 0.72f, pillarHeight, 0.72f, 0f, r, g, b, 1f, stats);
            renderer.drawBox(vp, p1x, pillarHeight * 0.5f, p1z, 0.72f, pillarHeight, 0.72f, 0f, r, g, b, 1f, stats);
            renderer.drawLine(vp, p0x, pillarHeight + 0.16f, p0z, p1x, pillarHeight + 0.16f, p1z, r, g, b, stats);
            if (target) {
                float midY = pillarHeight + 0.75f + (float)Math.sin(activePulse * 1.35f) * 0.22f;
                renderer.drawBillboardDiamond(vp, x, midY - 0.55f, z, 1.35f, 1.15f, r, g, b, 0.92f, stats);
            }
        }
    }

    private void drawTargetBeacon(PrimitiveRenderer renderer, float[] vp, float x, float z, boolean finish, RenderStats stats) {
        float r = finish ? 1.0f : 0.0f;
        float g = finish ? 0.78f : 0.95f;
        float b = finish ? 0.12f : 1.0f;
        renderer.drawLine(vp, x, 0.20f, z, x, 6.25f, z, r, g, b, stats);
        renderer.drawBox(vp, x, 5.85f, z, 2.25f, 0.22f, 2.25f, activePulse * 0.40f, r, g, b, 0.95f, stats);
    }

    private void drawStartFinish(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        drawStripe(renderer, vp, cpX(0), cpZ(0) - 5.5f, 0.0f, stats);
        drawStripe(renderer, vp, cpX(checkpointCount - 1), cpZ(checkpointCount - 1) + 5.5f, 0.0f, stats);
    }

    private void drawStripe(PrimitiveRenderer renderer, float[] vp, float x, float z, float yaw, RenderStats stats) {
        for (int i = -3; i <= 3; i++) {
            float color = (i & 1) == 0 ? 1f : 0.05f;
            renderer.drawBox(vp, x + i * 1.8f, 0.082f, z, 1.2f, 0.035f, 7.8f, yaw, color, color, color, 1f, stats);
        }
    }

    private void drawTargetLine(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        if (finished || nextCheckpoint <= 0 || nextCheckpoint >= checkpointCount || car == null) return;
        renderer.drawLine(vp, car.position.x, 0.16f, car.position.z, cpX(nextCheckpoint), 0.16f, cpZ(nextCheckpoint), 1.0f, 0.82f, 0.12f, stats);
    }

    private void drawRivalGhost(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        if (renderer == null || rival == null || checkpointCount < 2) return;
        float p = rival.getCheckpointProgress(checkpointCount - 1);
        int from = (int)Math.floor(p);
        if (from < 0) from = 0;
        if (from >= checkpointCount - 1) from = checkpointCount - 2;
        int to = from + 1;
        float t = p - from;
        float x = lerp(cpX(from), cpX(to), t);
        float z = lerp(cpZ(from), cpZ(to), t);
        float yaw = yawToNext(from, to);
        renderer.drawBox(vp, x, 0.78f, z, 1.72f, 0.58f, 3.35f, yaw, 1.0f, 0.22f, 0.16f, 1f, stats);
        renderer.drawBox(vp, x, 1.28f, z - 0.10f, 1.12f, 0.42f, 1.25f, yaw, 0.16f, 0.05f, 0.04f, 1f, stats);
        if (!finished) renderer.drawLine(vp, x, 0.18f, z, cpX(to), 0.18f, cpZ(to), 1.0f, 0.18f, 0.12f, stats);
    }

    private float[] gateSide(int index) {
        int prev = Math.max(0, index - 1);
        int next = Math.min(checkpointCount - 1, index + 1);
        float dx = cpX(next) - cpX(prev);
        float dz = cpZ(next) - cpZ(prev);
        float len = (float)Math.sqrt(dx * dx + dz * dz);
        if (len < 0.001f) return new float[] {1f, 0f};
        dx /= len;
        dz /= len;
        return new float[] { dz, -dx };
    }

    private void resetRaceCar() {
        if (car == null) return;
        float yaw = yawToNext(0, 1);
        float y = car.getTuning() == null ? 0.42f : car.getTuning().rideHeight;
        car.reset(cpX(0), y, cpZ(0), yaw);
        car.repairFull();
    }

    private float yawToNext(int from, int to) {
        float dx = cpX(to) - cpX(from);
        float dz = cpZ(to) - cpZ(from);
        return (float)Math.atan2(dx, dz);
    }

    private float cpX(int index) { return CheckpointRaceSystem.targetX(routeId, index); }
    private float cpZ(int index) { return CheckpointRaceSystem.targetZ(routeId, index); }

    private static float distanceSq(float ax, float az, float bx, float bz) {
        float dx = ax - bx;
        float dz = az - bz;
        return dx * dx + dz * dz;
    }

    private static float lerp(float a, float b, float t) { return a + (b - a) * Math.max(0f, Math.min(1f, t)); }

    private float getPlayerProgress01() {
        float current = Math.max(0, Math.min(checkpointCount - 1, nextCheckpoint - 1));
        if (!finished && nextCheckpoint > 0 && nextCheckpoint < checkpointCount && car != null) {
            float segmentLength = (float)Math.sqrt(distanceSq(cpX(nextCheckpoint - 1), cpZ(nextCheckpoint - 1), cpX(nextCheckpoint), cpZ(nextCheckpoint)));
            float remaining = (float)Math.sqrt(distanceSq(car.position.x, car.position.z, cpX(nextCheckpoint), cpZ(nextCheckpoint)));
            float segmentProgress = segmentLength <= 0.01f ? 0f : 1f - Math.max(0f, Math.min(1f, remaining / segmentLength));
            current += segmentProgress;
        }
        if (finished) current = checkpointCount - 1;
        return Math.max(0f, Math.min(1f, current / (float)(checkpointCount - 1)));
    }

    public boolean isActive() { return true; }
    public float getCountdown() { return Math.max(0f, countdown); }
    public float getElapsed() { return elapsed; }
    public int getPlayerLap() { return Math.min(Math.max(1, nextCheckpoint), checkpointCount - 1); }
    public int getTotalLaps() { return checkpointCount - 1; }
    public int getPlayerCheckpoint() { return getPlayerLap(); }
    public int getCheckpointTotal() { return getTotalLaps(); }
    public int getPlayerPosition() { return finished ? finishPosition : (getPlayerProgress01() >= rival.getCheckpointProgress(checkpointCount - 1) / (float)(checkpointCount - 1) ? 1 : 2); }
    public int getBotCount() { return 1; }
    public boolean isFinished() { return finished; }
    public boolean isNewBest() { return newBest; }
    public boolean isNewMedal() { return newMedal; }
    public int getEarnedCoins() { return earnedCoins; }
    public int getEarnedXp() { return earnedXp; }
    public String getFinishRank() { return finishGrade; }
    public String getRivalName() { return rival == null ? "" : rival.getRivalName(); }
    public String getRivalDifficultyName() { return rival == null ? "" : rival.getDifficultyName(); }
    public boolean didBeatRival() { return rivalWon; }
    public int getRouteId() { return routeId; }
    public String getRouteName() { return CheckpointRaceSystem.routeLabel(routeId); }
    public String getStatusText() {
        if (finished) return RaceResultSystem.checkpointRouteResultText(routeId, rivalWon, elapsed, finishGrade, earnedCoins, earnedXp, newBest, newMedal,
                saveManager == null ? bestBeforeStart : saveManager.getCheckpointRouteBestSeconds(routeId));
        if (countdown > 0f) return CheckpointRaceSystem.routeLabel(routeId) + "  •  RAKİP " + rival.getIntroText() + "  BAŞLANGIÇ " + (int)Math.ceil(countdown);
        if (message != null && message.length() > 0 && messageTimer > 0f) return message;
        int dist = getTargetDistanceMeters();
        return RaceResultSystem.checkpointHudText(routeId, elapsed, getPlayerLap(), getTotalLaps(), dist,
                saveManager == null ? bestBeforeStart : saveManager.getCheckpointRouteBestSeconds(routeId),
                rival.getLeadText(elapsed, getPlayerProgress01()));
    }

    public int getTargetDistanceMeters() {
        if (car == null || nextCheckpoint >= checkpointCount) return 0;
        float dx = cpX(nextCheckpoint) - car.position.x;
        float dz = cpZ(nextCheckpoint) - car.position.z;
        return Math.max(0, (int)Math.sqrt(dx * dx + dz * dz));
    }

    public float getTargetBearingDelta() {
        if (car == null || nextCheckpoint >= checkpointCount) return 0f;
        float dx = cpX(nextCheckpoint) - car.position.x;
        float dz = cpZ(nextCheckpoint) - car.position.z;
        float targetYaw = (float)Math.atan2(dx, dz);
        float delta = targetYaw - car.yaw;
        while (delta > Math.PI) delta -= Math.PI * 2f;
        while (delta < -Math.PI) delta += Math.PI * 2f;
        return delta;
    }

    public float getBestBeforeStart() { return bestBeforeStart; }
    public String getMedalBeforeStart() { return medalBeforeStart; }
    public int getCompletionsBeforeStart() { return completionsBeforeStart; }
    public boolean isRewardFinalized() { return rewardFinalized; }
}
