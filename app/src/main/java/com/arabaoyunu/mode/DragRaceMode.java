package com.arabaoyunu.mode;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.map.MapManager;
import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;
import com.arabaoyunu.util.SaveManager;

/**
 * ArabaOyunu_62_3: Rakipli 400m Drag Yarışı.
 *
 * Harita GLB olmadan Open Field üzerinde düz bir drag şeridi kurar.
 * Garaj/modifiye yükseltmelerinin motor, N2O, lastik ve ağırlık etkisi
 * doğrudan araç fiziğinde hissedildiği için drag süresi oyuncuya net ilerleme
 * geri bildirimi verir.
 */
public final class DragRaceMode extends BaseGameMode {

    private static final float COUNTDOWN_SECONDS = 3.25f;
    private static final float START_X = -5.8f;
    private static final float RIVAL_X = 5.8f;
    private static final float START_Z = -190f;
    private static final float FINISH_Z = START_Z + DragRaceModeSystem.DRAG_DISTANCE_METERS;
    private static final float LANE_HALF_WIDTH = 12.5f;
    private static final float FALSE_START_THROTTLE = 0.18f;

    private final VehicleController car;
    private final MapManager mapManager;
    private final SaveManager saveManager;

    private float countdown;
    private float elapsed;
    private float distanceMeters;
    private float bestSpeedKmh;
    private boolean running;
    private boolean finished;
    private boolean falseStart;
    private boolean newBest;
    private int earnedCoins;
    private String finishGrade = "-";
    private String message = "";
    private float messageTimer;
    private float bestBeforeStart;
    private final RivalRaceSystem rival = new RivalRaceSystem();
    private boolean rivalWon;

    public DragRaceMode(VehicleController car, MapManager mapManager, SaveManager saveManager) {
        this.car = car;
        this.mapManager = mapManager;
        this.saveManager = saveManager;
    }

    @Override
    public String getName() {
        return "DragRaceMode";
    }

    @Override
    public void start() {
        resetDragCar();
        countdown = COUNTDOWN_SECONDS;
        elapsed = 0f;
        distanceMeters = 0f;
        bestSpeedKmh = 0f;
        running = false;
        finished = false;
        falseStart = false;
        newBest = false;
        earnedCoins = 0;
        finishGrade = "-";
        message = "Drag yarışına hazırlan";
        messageTimer = 2.3f;
        bestBeforeStart = saveManager == null ? 0f : saveManager.getDragBestSeconds();
        rival.startDrag(saveManager);
        rivalWon = false;
    }

    @Override
    public void stop() {
        running = false;
        message = "";
        messageTimer = 0f;
    }

    @Override
    public void update(float dt, InputState input) {
        if (car == null) return;
        if (dt <= 0f) return;
        if (dt > 0.085f) dt = 0.085f;

        if (messageTimer > 0f) {
            messageTimer -= dt;
            if (messageTimer <= 0f) message = "";
        }

        if (finished) {
            if (messageTimer <= 0f && input != null && (input.pausePressed || input.throttle > 0.45f)) {
                start();
            }
            return;
        }

        InputState driveInput = input;
        if (countdown > 0f) {
            if (input != null && input.throttle > FALSE_START_THROTTLE) {
                falseStart = true;
                countdown = COUNTDOWN_SECONDS;
                resetDragCar();
                rival.resetTimer();
                message = "Hatalı çıkış! Yeşili bekle";
                messageTimer = 1.7f;
            } else {
                countdown -= dt;
            }
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

        updateProgress();
    }

    private void updateProgress() {
        distanceMeters = clamp(car.position.z - START_Z, 0f, DragRaceModeSystem.DRAG_DISTANCE_METERS);
        float speed = car.getSpeedKmh();
        if (speed > bestSpeedKmh) bestSpeedKmh = speed;
        if (running && distanceMeters >= DragRaceModeSystem.DRAG_DISTANCE_METERS - 0.5f) {
            finishRace();
        }
    }

    private void finishRace() {
        if (finished) return;
        finished = true;
        running = false;
        finishGrade = DragRaceModeSystem.gradeForTime(elapsed);
        rivalWon = rival.isPlayerWinner(elapsed);
        earnedCoins = rival.rewardForResult(rivalWon);
        if (saveManager != null) {
            saveManager.addCoins(earnedCoins);
            saveManager.addXp(rival.xpForResult(rivalWon));
            saveManager.recordDragRaceResult(elapsed, earnedCoins, finishGrade, bestSpeedKmh);
            saveManager.recordRivalRaceResult("Drag", rival.getDifficulty(), rivalWon, earnedCoins);
            newBest = saveManager.saveDragBestIfLower(elapsed);
        }
        message = rival.getResultText(rivalWon) + " " + finishGrade + " +" + earnedCoins;
        messageTimer = 5f;
    }

    public void render(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        if (renderer == null || vp == null) return;
        drawDragLane(renderer, vp, stats);
        drawDistanceMarkers(renderer, vp, stats);
        drawStartFinish(renderer, vp, stats);
        drawRivalCar(renderer, vp, stats);
        drawProgressGuide(renderer, vp, stats);
    }

    private void drawDragLane(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        float centerZ = (START_Z + FINISH_Z) * 0.5f;
        float length = DragRaceModeSystem.DRAG_DISTANCE_METERS;

        // Ana drag asfaltı ve iki sınır çizgisi.
        renderer.drawBox(vp, START_X, 0.074f, centerZ, LANE_HALF_WIDTH * 2.0f, 0.035f, length + 34f, 0f,
                0.060f, 0.065f, 0.078f, 1f, stats);
        renderer.drawBox(vp, -LANE_HALF_WIDTH, 0.096f, centerZ, 0.55f, 0.035f, length + 18f, 0f,
                0.10f, 0.72f, 1.0f, 1f, stats);
        renderer.drawBox(vp, LANE_HALF_WIDTH, 0.096f, centerZ, 0.55f, 0.035f, length + 18f, 0f,
                0.10f, 0.72f, 1.0f, 1f, stats);

        // Orta kesik çizgi.
        for (float z = START_Z + 18f; z < FINISH_Z - 12f; z += 24f) {
            renderer.drawBox(vp, 0f, 0.105f, z, 0.42f, 0.035f, 10.5f, 0f,
                    1.0f, 0.92f, 0.24f, 1f, stats);
        }
    }

    private void drawDistanceMarkers(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        for (int i = 1; i <= 3; i++) {
            float z = START_Z + i * 100f;
            renderer.drawBox(vp, -LANE_HALF_WIDTH - 3.4f, 1.15f, z, 1.25f, 2.3f, 1.25f, 0f,
                    0.12f, 0.42f, 1.0f, 1f, stats);
            renderer.drawBox(vp, LANE_HALF_WIDTH + 3.4f, 1.15f, z, 1.25f, 2.3f, 1.25f, 0f,
                    0.12f, 0.42f, 1.0f, 1f, stats);
            renderer.drawLine(vp, -LANE_HALF_WIDTH, 0.14f, z, LANE_HALF_WIDTH, 0.14f, z,
                    0.35f, 0.72f, 1.0f, stats);
        }
    }

    private void drawStartFinish(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        drawCheckerStripe(renderer, vp, START_Z, false, stats);
        drawCheckerStripe(renderer, vp, FINISH_Z, true, stats);

        // Drag ışıkları: başlangıçta üç küçük kule, yeşil anında parlak çizgi.
        float lampZ = START_Z - 11f;
        renderer.drawBox(vp, -8f, 2.8f, lampZ, 0.8f, 5.6f, 0.8f, 0f, 0.07f, 0.08f, 0.10f, 1f, stats);
        renderer.drawBox(vp, 8f, 2.8f, lampZ, 0.8f, 5.6f, 0.8f, 0f, 0.07f, 0.08f, 0.10f, 1f, stats);
        float r = countdown > 2.15f ? 1f : 0.16f;
        float y = countdown <= 2.15f && countdown > 1.05f ? 0.95f : 0.16f;
        float g = countdown <= 1.05f ? 1f : 0.16f;
        renderer.drawBox(vp, -8f, 5.2f, lampZ, 1.6f, 0.55f, 1.0f, 0f, r, 0.06f, 0.04f, 1f, stats);
        renderer.drawBox(vp, 0f, 5.2f, lampZ, 1.6f, 0.55f, 1.0f, 0f, y, y * 0.75f, 0.04f, 1f, stats);
        renderer.drawBox(vp, 8f, 5.2f, lampZ, 1.6f, 0.55f, 1.0f, 0f, 0.04f, g, 0.16f, 1f, stats);
    }

    private void drawCheckerStripe(PrimitiveRenderer renderer, float[] vp, float z, boolean finish, RenderStats stats) {
        for (int i = -6; i <= 6; i++) {
            float color = (i & 1) == 0 ? 1f : 0.05f;
            float r = finish ? 1f : color;
            float g = finish ? color : color;
            float b = finish ? 0.08f : color;
            renderer.drawBox(vp, i * 1.9f, 0.13f, z, 1.2f, 0.040f, 8.6f, 0f, r, g, b, 1f, stats);
        }
    }

    private void drawProgressGuide(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        if (finished || car == null) return;
        float nextZ = START_Z + Math.min(DragRaceModeSystem.DRAG_DISTANCE_METERS, ((int)(distanceMeters / 100f) + 1) * 100f);
        if (nextZ > FINISH_Z) nextZ = FINISH_Z;
        renderer.drawLine(vp, car.position.x, 0.18f, car.position.z, START_X, 0.18f, nextZ,
                1.0f, 0.88f, 0.14f, stats);
    }

    private void drawRivalCar(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        if (rival == null) return;
        float z = START_Z + rival.getDragDistance(DragRaceModeSystem.DRAG_DISTANCE_METERS);
        if (z > FINISH_Z) z = FINISH_Z;
        float y = car == null || car.getTuning() == null ? 0.74f : car.getTuning().rideHeight + 0.34f;
        float pulse = rival.hasFinished() && !finished ? 1.0f : 0.0f;
        renderer.drawBox(vp, RIVAL_X, y, z, 1.85f, 0.62f, 3.55f, 0f,
                0.95f, pulse > 0f ? 0.18f : 0.42f, 0.12f, 1f, stats);
        renderer.drawBox(vp, RIVAL_X, y + 0.54f, z - 0.12f, 1.22f, 0.48f, 1.35f, 0f,
                0.12f, 0.08f, 0.07f, 1f, stats);
        renderer.drawLine(vp, RIVAL_X, 0.18f, z, RIVAL_X, 0.18f, FINISH_Z, 1.0f, 0.24f, 0.18f, stats);
    }

    private void resetDragCar() {
        if (car == null) return;
        float y = car.getTuning() == null ? 0.42f : car.getTuning().rideHeight;
        car.reset(START_X, y, START_Z, 0f);
        car.repairFull();
    }

    private static float clamp(float value, float min, float max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    public boolean isActive() { return true; }
    public float getCountdown() { return Math.max(0f, countdown); }
    public float getElapsed() { return elapsed; }
    public float getDistanceMeters() { return distanceMeters; }
    public float getTotalDistanceMeters() { return DragRaceModeSystem.DRAG_DISTANCE_METERS; }
    public float getBestSpeedKmh() { return bestSpeedKmh; }
    public boolean isFinished() { return finished; }
    public boolean isFalseStart() { return falseStart; }
    public boolean isNewBest() { return newBest; }
    public int getEarnedCoins() { return earnedCoins; }
    public String getFinishRank() { return finishGrade; }
    public float getBestBeforeStart() { return bestBeforeStart; }
    public float getRivalDistanceMeters() { return rival == null ? 0f : rival.getDragDistance(DragRaceModeSystem.DRAG_DISTANCE_METERS); }
    public boolean didBeatRival() { return rivalWon; }
    public String getRivalName() { return rival == null ? "" : rival.getRivalName(); }
    public String getRivalDifficultyName() { return rival == null ? "" : rival.getDifficultyName(); }

    public String getStatusText() {
        if (finished) {
            String best = saveManager == null || saveManager.getDragBestSeconds() <= 0f
                    ? "" : "  EN IYI " + DragRaceModeSystem.formatTime(saveManager.getDragBestSeconds());
            return rival.getResultText(rivalWon) + "  DRAG " + DragRaceModeSystem.formatTime(elapsed)
                    + "  " + finishGrade + " +" + earnedCoins + (newBest ? " REKOR" : best);
        }
        if (countdown > 0f) {
            if (message != null && message.length() > 0 && messageTimer > 0f) return message;
            return countdown <= 1.05f
                    ? "YESIL ISIK | RAKIP " + rival.getRivalName()
                    : "RAKIP " + rival.getIntroText() + "  BASLANGIC " + (int)Math.ceil(countdown);
        }
        if (falseStart && message != null && message.length() > 0 && messageTimer > 0f) return message;
        float player01 = DragRaceModeSystem.DRAG_DISTANCE_METERS <= 0f ? 0f : distanceMeters / DragRaceModeSystem.DRAG_DISTANCE_METERS;
        return rival.getLeadText(elapsed, player01)
                + "  SEN " + (int)distanceMeters + "m RAKIP " + (int)getRivalDistanceMeters() + "m"
                + "  SURE " + DragRaceModeSystem.formatTime(elapsed)
                + "  HIZ " + (int)bestSpeedKmh + " km/h";
    }

}
