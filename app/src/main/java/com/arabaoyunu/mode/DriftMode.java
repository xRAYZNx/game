package com.arabaoyunu.mode;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.map.MapManager;
import com.arabaoyunu.physics.DriftSystem;
import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;
import com.arabaoyunu.util.SaveManager;

/**
 * A65.3: Open Field uzerinde final oynanabilir Drift Skor modu.
 * Checkpoint modundan sonra ikinci gercek oyun dongusu: sure, skor, combo,
 * guvenli odul finalizasyonu ve profesyonel sonuc akisi.
 */
public final class DriftMode extends BaseGameMode {

    public static final float SESSION_SECONDS = DriftScoreSystem.SESSION_SECONDS;

    private final VehicleController car;
    private final MapManager mapManager;
    private final DriftSystem driftSystem;
    private final SaveManager saveManager;

    private float timeRemaining;
    private float countdown;
    private boolean finished;
    private boolean newBest;
    private boolean rewardFinalized;
    private int earnedCoins;
    private int earnedXp;
    private int completionsBeforeStart;
    private int bestBeforeStart;
    private String rank = DriftScoreSystem.GRADE_FINISH;
    private String resultMessage = "";
    private boolean exitConfirmVisible;
    private float exitConfirmTimer;

    public DriftMode(VehicleController car, MapManager mapManager, DriftSystem driftSystem, SaveManager saveManager) {
        this.car = car;
        this.mapManager = mapManager;
        this.driftSystem = driftSystem;
        this.saveManager = saveManager;
    }

    @Override
    public String getName() { return "DriftMode"; }

    @Override
    public void start() {
        if (mapManager != null && mapManager.getCurrentMap() != null && car != null) {
            car.reset(
                    mapManager.getCurrentMap().getSpawnX(),
                    mapManager.getCurrentMap().getSpawnY(),
                    mapManager.getCurrentMap().getSpawnZ(),
                    mapManager.getCurrentMap().getSpawnYaw());
        }
        timeRemaining = SESSION_SECONDS;
        countdown = DriftScoreSystem.COUNTDOWN_SECONDS;
        finished = false;
        newBest = false;
        rewardFinalized = false;
        earnedCoins = 0;
        earnedXp = 0;
        rank = DriftScoreSystem.GRADE_FINISH;
        resultMessage = "";
        exitConfirmVisible = false;
        exitConfirmTimer = 0f;
        completionsBeforeStart = saveManager == null ? 0 : saveManager.getDriftCompletedCount();
        bestBeforeStart = saveManager == null ? 0 : saveManager.getDriftBestScore();
        driftSystem.resetSession(bestBeforeStart);
    }

    @Override
    public void update(float dt, InputState input) {
        if (dt <= 0f) return;

        if (finished) {
            // Bitti ekranindayken gaz veya pause tusu yeni deneme baslatir.
            if (input != null && (input.throttle > 0.3f || input.pausePressed)) {
                start();
            }
            return;
        }

        if (countdown > 0f) {
            countdown -= dt;
            return;
        }

        if (exitConfirmVisible) {
            exitConfirmTimer -= dt;
            if (exitConfirmTimer <= 0f || (input != null && input.throttle > 0.18f)) {
                exitConfirmVisible = false;
            } else if (input != null && input.pausePressed) {
                abandonSession();
                return;
            }
        } else if (input != null && input.pausePressed) {
            exitConfirmVisible = true;
            exitConfirmTimer = 3.0f;
            return;
        }

        if (mapManager != null && mapManager.getCurrentMap() != null && car != null) {
            car.update(dt, input, mapManager.getCurrentMap());
        }

        boolean collision = car != null && car.consumeCollisionEvent();
        boolean insideZone = car != null && isInsideDriftZone(car.position.x, car.position.z);
        driftSystem.update(dt, car, collision, insideZone, true);

        timeRemaining -= dt;
        if (timeRemaining <= 0f) {
            finishSession();
        }
    }

    private void finishSession() {
        if (finished) return;
        finished = true;
        timeRemaining = 0f;
        driftSystem.forceFinish();

        int score = driftSystem.getSessionScore();
        rank = DriftScoreSystem.gradeForScore(score);
        newBest = score > bestBeforeStart;
        earnedCoins = RewardBalanceSystem.balancedDriftCoins(rank, newBest, completionsBeforeStart, driftSystem.getBestComboLevel());
        earnedXp = RewardBalanceSystem.balancedDriftXp(rank, newBest, driftSystem.getBestComboLevel());
        if (saveManager != null && !rewardFinalized) {
            rewardFinalized = true;
            if (newBest) saveManager.saveDriftBestScoreIfHigher(score);
            saveManager.addCoins(earnedCoins);
            saveManager.addXp(earnedXp);
            saveManager.recordDriftModeResult(score, earnedCoins, earnedXp, rank,
                    driftSystem.getLongestDriftTime(), driftSystem.getBestComboLevel());
        }
        int bestAfter = Math.max(score, bestBeforeStart);
        resultMessage = DriftResultSystem.resultTitle(rank, newBest)
                + " | " + DriftResultSystem.resultLine(score, bestAfter, newBest)
                + " | " + DriftResultSystem.medalLine(rank, newBest, driftSystem.getBestComboLevel())
                + " | " + DriftResultSystem.rewardLine(saveManager, earnedCoins, earnedXp, rank)
                + " | " + DriftScoreSystem.repeatRewardRuleText(completionsBeforeStart, newBest)
                + " | " + ModeProgressBridgeSystem.resultCardFooter(saveManager);
    }

    private void abandonSession() {
        if (finished) return;
        finished = true;
        timeRemaining = 0f;
        rewardFinalized = true;
        earnedCoins = 0;
        earnedXp = 0;
        rank = "ÇIKILDI";
        newBest = false;
        driftSystem.forceFinish();
        resultMessage = "Drift denemesi bırakıldı | Ödül verilmedi | Gaz: tekrar dene | Menü: mod seç";
    }

    /** A65.2: Open Field skor alani. Bonus halkalari vardir ama skor alani tum acik test alanidir. */
    private boolean isInsideDriftZone(float x, float z) {
        return isInsideCircle(x, z, -92f, 42f, 58f)
                || isInsideCircle(x, z, 92f, 35f, 46f)
                || isInsideCircle(x, z, 0f, 116f, 52f);
    }

    private static boolean isInsideCircle(float x, float z, float cx, float cz, float radius) {
        float dx = x - cx;
        float dz = z - cz;
        return dx * dx + dz * dz <= radius * radius;
    }

    public void render(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        if (renderer == null || vp == null) return;
        // Open Field uzerinde 3 drift bonus bolgesi + orta baglanti cizgisi.
        drawZone(renderer, vp, -92f, 42f, 18f, 34f, 50f, stats);
        drawZone(renderer, vp, 92f, 35f, 18f, 32f, 48f, stats);
        drawZone(renderer, vp, 0f, 116f, 16f, 32f, 55f, stats);
        for (float z = -190f; z <= 190f; z += 38f) {
            renderer.drawLine(vp, -18f, 0.08f, z, 18f, 0.08f, z, 0.08f, 0.86f, 1.0f, stats);
        }
        renderer.drawLine(vp, -92f, 0.12f, 42f, 0f, 0.12f, 116f, 1.0f, 0.75f, 0.10f, stats);
        renderer.drawLine(vp, 0f, 0.12f, 116f, 92f, 0.12f, 35f, 1.0f, 0.75f, 0.10f, stats);
        renderer.drawStartMarker(vp, 0f, 0.12f, -185f, stats);
    }

    private void drawZone(PrimitiveRenderer renderer, float[] vp, float x, float z, float r1, float r2, float r3, RenderStats stats) {
        renderer.drawCircle(vp, x, 0.085f, z, r1, stats);
        renderer.drawCircle(vp, x, 0.090f, z, r2, stats);
        renderer.drawCircle(vp, x, 0.095f, z, r3, stats);
        renderer.drawLine(vp, x, 0.16f, z, x, 5.4f, z, 0.0f, 0.95f, 1.0f, stats);
        renderer.drawLine(vp, x - 7f, 2.8f, z, x + 7f, 2.8f, z, 1.0f, 0.85f, 0.15f, stats);
        renderer.drawLine(vp, x, 2.8f, z - 7f, x, 2.8f, z + 7f, 1.0f, 0.85f, 0.15f, stats);
    }

    public String getLiveCallout() {
        if (exitConfirmVisible) return "Çıkmak için Menü’ye tekrar bas • Devam: Gaz";
        return DriftModeSystem.liveCallout(car, driftSystem);
    }

    public boolean isExitConfirmVisible() { return exitConfirmVisible; }
    public int getXpReward() { return earnedXp; }
    public DriftSystem getDriftSystem() { return driftSystem; }
    public float getTimeRemaining() { return countdown > 0f && !finished ? countdown : timeRemaining; }
    public float getCountdown() { return Math.max(0f, countdown); }
    public boolean isFinished() { return finished; }
    public boolean isNewBest() { return newBest; }
    public int getEarnedCoins() { return earnedCoins; }
    public int getEarnedXp() { return earnedXp; }
    public String getRank() { return rank; }
    public String getResultMessage() { return resultMessage; }
}
