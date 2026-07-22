package com.arabaoyunu.physics;

/**
 * A65.3: Sureli drift modu icin final puan, combo ve ceza sistemi.
 * Sadece VehicleController verilerini okur; fizik tarafina mudahale etmez.
 */
public final class DriftSystem {

    private float activeDriftTime;
    private float lastScoreTime;
    private float comboGraceTimer;
    private float controlledDriftChainTime;
    private float noScoreStraightTimer;
    private String comboBreakMessage = "";

    private int sessionScore;
    private int currentComboScore;
    private int bestScore;
    private int comboLevel;
    private int crashPenalty;
    private int bestComboLevel;
    private float longestDriftTime;

    private boolean active;
    private boolean lastInsideZone;

    private static final float COMBO_GRACE_SECONDS = 0.92f;
    private static final float MIN_VALID_SPEED_KMH = com.arabaoyunu.mode.DriftScoreSystem.MIN_SCORE_SPEED_KMH;

    public void resetSession(int savedBestScore) {
        activeDriftTime = 0f;
        lastScoreTime = 0f;
        comboGraceTimer = 0f;
        controlledDriftChainTime = 0f;
        noScoreStraightTimer = 0f;
        comboBreakMessage = "";
        sessionScore = 0;
        currentComboScore = 0;
        bestScore = Math.max(0, savedBestScore);
        comboLevel = 0;
        crashPenalty = 0;
        bestComboLevel = 0;
        longestDriftTime = 0f;
        active = false;
        lastInsideZone = false;
    }

    public void update(float dt, VehicleController car, boolean collisionHappened, boolean insideDriftZone, boolean scoringAllowed) {
        if (car == null || dt <= 0f) return;

        lastInsideZone = insideDriftZone;

        if (collisionHappened) {
            comboBreakMessage = "Çarpışma: combo kırıldı";
            applyCrashPenalty();
            finishCombo(false);
            return;
        }

        if (com.arabaoyunu.mode.DriftScoreSystem.isSpinOut(car)) {
            comboBreakMessage = "Spin: kontrol kaybı";
            applySpinPenalty();
            finishCombo(false);
            return;
        }

        boolean validSpeed = car.getSpeedKmh() >= MIN_VALID_SPEED_KMH;
        boolean validDrift = scoringAllowed
                && validSpeed
                && com.arabaoyunu.mode.DriftScoreSystem.isScoreable(car, insideDriftZone);

        if (validDrift) {
            if (!active) {
                active = true;
                activeDriftTime = 0f;
                comboGraceTimer = COMBO_GRACE_SECONDS;
            }

            activeDriftTime += dt;
            controlledDriftChainTime += dt;
            noScoreStraightTimer = 0f;
            if (activeDriftTime > longestDriftTime) longestDriftTime = activeDriftTime;
            lastScoreTime += dt;
            comboGraceTimer = COMBO_GRACE_SECONDS;

            int targetCombo = 1;
            if (controlledDriftChainTime >= 16.0f) targetCombo = 5;
            else if (controlledDriftChainTime >= 10.0f) targetCombo = 4;
            else if (controlledDriftChainTime >= 6.0f) targetCombo = 3;
            else if (controlledDriftChainTime >= 2.8f) targetCombo = 2;
            if (targetCombo > comboLevel) {
                comboLevel = targetCombo;
                if (comboLevel > bestComboLevel) bestComboLevel = comboLevel;
            } else if (comboLevel <= 0) {
                comboLevel = 1;
            }

            int gain = com.arabaoyunu.mode.DriftScoreSystem.scoreGain(car, dt, getMultiplier(), insideDriftZone);

            sessionScore += gain;
            currentComboScore += gain;
            if (sessionScore > bestScore) bestScore = sessionScore;
            comboBreakMessage = "";
        } else {
            noScoreStraightTimer += dt;
            if (active) {
                comboGraceTimer -= dt;
                if (comboGraceTimer <= 0f) {
                    comboBreakMessage = validSpeed ? "Açı kayboldu: combo beklemede" : "Hız düştü: combo kırıldı";
                    finishCombo(true);
                }
            } else if (comboLevel > 0) {
                comboGraceTimer -= dt;
                if (comboGraceTimer <= 0f || noScoreStraightTimer > 1.65f) {
                    comboBreakMessage = "Combo sıfırlandı";
                    comboLevel = 0;
                    currentComboScore = 0;
                    controlledDriftChainTime = 0f;
                }
            }
        }
    }

    private void applyCrashPenalty() {
        int penalty = Math.max(150, Math.round(currentComboScore * 0.35f));
        crashPenalty += penalty;
        sessionScore = Math.max(0, sessionScore - penalty);
    }

    private void applySpinPenalty() {
        int penalty = Math.max(120, Math.round(currentComboScore * 0.24f));
        crashPenalty += penalty;
        sessionScore = Math.max(0, sessionScore - penalty);
    }

    private void finishCombo(boolean keepComboForGrace) {
        active = false;
        activeDriftTime = 0f;
        currentComboScore = keepComboForGrace ? currentComboScore : 0;
        comboLevel = keepComboForGrace ? comboLevel : 0;
        comboGraceTimer = keepComboForGrace ? 0.42f : 0f;
        if (!keepComboForGrace) controlledDriftChainTime = 0f;
    }

    public void forceFinish() {
        finishCombo(false);
        if (sessionScore > bestScore) bestScore = sessionScore;
    }

    public boolean isActive() { return active; }
    public boolean isInsideDriftZone() { return lastInsideZone; }
    public float getDriftTime() { return activeDriftTime; }
    public float getMultiplier() { return 1f + Math.min(comboLevel, 9) * 0.23f; }
    public int getComboLevel() { return comboLevel; }
    public int getSessionScore() { return sessionScore; }
    public int getCurrentComboScore() { return currentComboScore; }
    public int getCurrentScore() { return sessionScore; }
    public int getBestScore() { return bestScore; }
    public int getCrashPenalty() { return crashPenalty; }
    public int getBestComboLevel() { return bestComboLevel; }
    public float getLongestDriftTime() { return longestDriftTime; }
    public float getLastScoreTime() { return lastScoreTime; }
    public float getControlledDriftChainTime() { return controlledDriftChainTime; }
    public String getComboBreakMessage() { return comboBreakMessage == null ? "" : comboBreakMessage; }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
