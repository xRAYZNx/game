package com.arabaoyunu.mode;

import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * ArabaOyunu_62_3: Rakip yarış profili.
 *
 * Gerçek AI/collider karmaşasına girmeden, yarış modlarına stabil rakip hissi verir:
 * - Drag için yan şeritte zamana bağlı rakip ilerlemesi
 * - Checkpoint için ghost rakip/süre farkı
 * - Kolay/Orta/Zor zorluk, kazanma-kaybetme ve ödül katsayıları
 */
public final class RivalRaceSystem {

    public static final int DIFFICULTY_EASY = 0;
    public static final int DIFFICULTY_MEDIUM = 1;
    public static final int DIFFICULTY_HARD = 2;

    public static final int MODE_CHECKPOINT = 0;
    public static final int MODE_DRAG = 1;

    private int mode = MODE_CHECKPOINT;
    private int difficulty = DIFFICULTY_EASY;
    private String rivalName = "Street Rookie";
    private String rivalVehicle = "Street Car";
    private float targetFinishSeconds = 90f;
    private float elapsed;
    private boolean started;
    private boolean finished;

    public void startCheckpoint(SaveManager saveManager) {
        mode = MODE_CHECKPOINT;
        difficulty = chooseDifficulty(saveManager, MODE_CHECKPOINT);
        setupProfile();
        float base = difficulty == DIFFICULTY_EASY ? 108f : (difficulty == DIFFICULTY_MEDIUM ? 92f : 78f);
        targetFinishSeconds = base * classBalance(saveManager);
        elapsed = 0f;
        started = false;
        finished = false;
    }

    public void startDrag(SaveManager saveManager) {
        mode = MODE_DRAG;
        difficulty = chooseDifficulty(saveManager, MODE_DRAG);
        setupProfile();
        float base = difficulty == DIFFICULTY_EASY ? 17.8f : (difficulty == DIFFICULTY_MEDIUM ? 14.8f : 12.9f);
        targetFinishSeconds = base * classBalance(saveManager);
        elapsed = 0f;
        started = false;
        finished = false;
    }

    public void update(float dt, boolean running) {
        if (dt <= 0f) return;
        if (!running) return;
        started = true;
        elapsed += dt;
        if (elapsed >= targetFinishSeconds) finished = true;
    }

    public void resetTimer() {
        elapsed = 0f;
        started = false;
        finished = false;
    }

    public boolean isPlayerWinner(float playerFinishSeconds) {
        return playerFinishSeconds > 0f && playerFinishSeconds <= targetFinishSeconds;
    }

    public int rewardForResult(boolean won) {
        if (difficulty == DIFFICULTY_HARD) return won ? 1300 : 200;
        if (difficulty == DIFFICULTY_MEDIUM) return won ? 850 : 150;
        return won ? 500 : 100;
    }

    public int xpForResult(boolean won) {
        if (difficulty == DIFFICULTY_HARD) return won ? 210 : 55;
        if (difficulty == DIFFICULTY_MEDIUM) return won ? 145 : 38;
        return won ? 90 : 25;
    }

    public float getDragDistance(float totalDistanceMeters) {
        float p = getProgress01();
        return Math.max(0f, Math.min(totalDistanceMeters, totalDistanceMeters * easeOut(p)));
    }

    public float getCheckpointProgress(float totalCheckpoints) {
        return Math.max(0f, Math.min(totalCheckpoints, getProgress01() * totalCheckpoints));
    }

    public float getRivalTimeDelta(float playerElapsed, float playerProgress01) {
        float p = Math.max(0.001f, Math.min(0.999f, playerProgress01));
        float rivalExpectedAtSameProgress = targetFinishSeconds * p;
        return playerElapsed - rivalExpectedAtSameProgress;
    }

    public String getLeadText(float playerElapsed, float playerProgress01) {
        float delta = getRivalTimeDelta(playerElapsed, playerProgress01);
        if (delta <= -0.35f) return "Öndesin " + formatSeconds(-delta);
        if (delta >= 0.35f) return "Geridesin " + formatSeconds(delta);
        return "Başa baş";
    }

    public String getDifficultyName() {
        if (difficulty == DIFFICULTY_HARD) return "ZOR";
        if (difficulty == DIFFICULTY_MEDIUM) return "ORTA";
        return "KOLAY";
    }

    public int getDifficulty() { return difficulty; }
    public int getMode() { return mode; }
    public String getRivalName() { return rivalName; }
    public String getRivalVehicle() { return rivalVehicle; }
    public float getTargetFinishSeconds() { return targetFinishSeconds; }
    public float getElapsed() { return elapsed; }
    public boolean hasFinished() { return finished; }
    public boolean hasStarted() { return started; }

    public String getIntroText() {
        return rivalName + " | " + getDifficultyName() + " | " + rivalVehicle;
    }

    public String getResultText(boolean won) {
        return won ? "KAZANDIN" : "KAYBETTIN";
    }

    private void setupProfile() {
        if (difficulty == DIFFICULTY_HARD) {
            rivalName = mode == MODE_DRAG ? "Pro Drag Racer" : "Pro Checkpoint Pilot";
            rivalVehicle = "Super Class";
        } else if (difficulty == DIFFICULTY_MEDIUM) {
            rivalName = mode == MODE_DRAG ? "Street Sprinter" : "Street Racer";
            rivalVehicle = "Sport Class";
        } else {
            rivalName = mode == MODE_DRAG ? "Rookie Launcher" : "Street Rookie";
            rivalVehicle = "Street Car";
        }
    }

    private static int chooseDifficulty(SaveManager saveManager, int mode) {
        if (saveManager == null) return DIFFICULTY_EASY;
        int level = saveManager.getPlayerLevel();
        int selected = saveManager.getSelectedVehicleIndex();
        float cls = VehicleCatalog.performanceClass(selected);
        int completed = mode == MODE_DRAG ? saveManager.getDragRaceCompletedCount() : saveManager.getCheckpointRaceCompletedCount();
        if (level >= 8 || cls >= 1.32f || completed >= 8) return DIFFICULTY_HARD;
        if (level >= 4 || cls >= 1.12f || completed >= 3) return DIFFICULTY_MEDIUM;
        return DIFFICULTY_EASY;
    }

    private static float classBalance(SaveManager saveManager) {
        if (saveManager == null) return 1f;
        float cls = VehicleCatalog.performanceClass(saveManager.getSelectedVehicleIndex());
        // Daha güçlü oyuncu aracına karşı rakip biraz güçlenir ama tamamen hileli olmaz.
        if (cls >= 1.45f) return 0.90f;
        if (cls >= 1.30f) return 0.94f;
        if (cls >= 1.15f) return 0.98f;
        if (cls < 0.98f) return 1.06f;
        return 1.0f;
    }

    private float getProgress01() {
        if (targetFinishSeconds <= 0.01f) return 1f;
        return Math.max(0f, Math.min(1f, elapsed / targetFinishSeconds));
    }

    private static float easeOut(float p) {
        p = Math.max(0f, Math.min(1f, p));
        return 1f - (1f - p) * (1f - p);
    }

    private static String formatSeconds(float seconds) {
        if (seconds < 0f || seconds != seconds) seconds = 0f;
        return String.format(java.util.Locale.US, "%.1fs", seconds);
    }
}
