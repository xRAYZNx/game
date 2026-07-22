package com.arabaoyunu.mode;

import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.util.SaveManager;

/**
 * A65.3: Drift Skor modu icin final skor/combo/odul kural kaynagi.
 * Open Field uzerinde yeni GLB/harita gerektirmeden skor, combo, odul ve HUD
 * metinlerini dengeler. Fizik sistemini bozmaz; yalnizca VehicleController
 * telemetrisini okur.
 */
public final class DriftScoreSystem {

    public static final float SESSION_SECONDS = 120f;
    public static final float COUNTDOWN_SECONDS = 2.75f;
    public static final float MIN_SCORE_SPEED_KMH = 32f;
    public static final float MIN_SCORE_SLIP_DEG = 11.5f;
    public static final float IDEAL_SLIP_MIN_DEG = 20f;
    public static final float IDEAL_SLIP_MAX_DEG = 48f;
    public static final float SPINOUT_SLIP_DEG = 67f;

    public static final int TARGET_BRONZE = 3200;
    public static final int TARGET_SILVER = 7600;
    public static final int TARGET_GOLD = 13200;
    public static final int TARGET_LEGEND = 22000;

    public static final String GRADE_LEGEND = "EFSANE";
    public static final String GRADE_GOLD = "ALTIN";
    public static final String GRADE_SILVER = "GUMUS";
    public static final String GRADE_BRONZE = "BRONZ";
    public static final String GRADE_FINISH = "TAMAM";

    private DriftScoreSystem() {}

    public static boolean isScoreable(VehicleController car, boolean insideDriftZone) {
        if (car == null) return false;
        float speed = car.getSpeedKmh();
        float slip = Math.abs(car.getSlipAngleDeg());
        float blend = car.getDriftBlend();
        float forward = Math.abs(car.getForwardSpeed());
        float side = Math.abs(car.getSideSpeed());
        boolean enoughMotion = speed >= MIN_SCORE_SPEED_KMH && forward > 3.5f;
        boolean notSpinOut = slip < SPINOUT_SLIP_DEG || (speed > 54f && blend > 0.72f && side < forward * 1.55f);
        boolean controlledSlide = side > 1.15f && side < Math.max(5.0f, forward * 1.85f);
        boolean driftState = car.isDrifting() || blend > 0.42f || slip >= MIN_SCORE_SLIP_DEG + 4f;
        // A65.3: dururken donme/kucuk kayma/skoru sisiren spin puan sayilmaz.
        return enoughMotion && controlledSlide && notSpinOut && slip >= MIN_SCORE_SLIP_DEG && driftState;
    }

    public static boolean isSpinOut(VehicleController car) {
        if (car == null) return false;
        float speed = car.getSpeedKmh();
        float slip = Math.abs(car.getSlipAngleDeg());
        float forward = Math.abs(car.getForwardSpeed());
        float side = Math.abs(car.getSideSpeed());
        return speed > 20f && (slip >= 78f || side > Math.max(6f, forward * 2.15f));
    }

    public static int scoreGain(VehicleController car, float dt, float multiplier, boolean insideDriftZone) {
        if (car == null || dt <= 0f) return 0;
        float speed = car.getSpeedKmh();
        float slip = Math.abs(car.getSlipAngleDeg());
        float speedFactor = clamp(speed / 88f, 0.30f, 1.70f);
        float slipFactor = clamp((slip - MIN_SCORE_SLIP_DEG) / 38f, 0.18f, 1.35f);
        float angleSweetSpot = slip >= IDEAL_SLIP_MIN_DEG && slip <= IDEAL_SLIP_MAX_DEG ? 1.24f : 0.82f;
        float spinGuard = slip > SPINOUT_SLIP_DEG ? 0.38f : 1.0f;
        float driftFeel = clamp(0.58f + car.getDriftBlend() * 0.46f, 0.58f, 1.08f);
        float zoneBonus = insideDriftZone ? 1.22f : 1.0f;
        float tuningBonus = DriftModeSystem.controlBonus(car.getTuning());
        float safeMultiplier = clamp(Math.max(1f, multiplier), 1f, 3.35f);
        int gain = Math.round((10.5f + slip * 0.70f) * speedFactor * slipFactor
                * angleSweetSpot * spinGuard * driftFeel * zoneBonus * tuningBonus * safeMultiplier * dt * 8.6f);
        return Math.max(0, gain);
    }

    public static String gradeForScore(int score) {
        if (score >= TARGET_LEGEND) return GRADE_LEGEND;
        if (score >= TARGET_GOLD) return GRADE_GOLD;
        if (score >= TARGET_SILVER) return GRADE_SILVER;
        if (score >= TARGET_BRONZE) return GRADE_BRONZE;
        return GRADE_FINISH;
    }

    public static int targetForNextGrade(int score) {
        if (score < TARGET_BRONZE) return TARGET_BRONZE;
        if (score < TARGET_SILVER) return TARGET_SILVER;
        if (score < TARGET_GOLD) return TARGET_GOLD;
        if (score < TARGET_LEGEND) return TARGET_LEGEND;
        return TARGET_LEGEND;
    }

    public static String targetText(int score) {
        if (score >= TARGET_LEGEND) return "EFSANE TAMAM";
        return score + " / " + targetForNextGrade(score);
    }

    public static int rewardCoins(String grade, boolean newBest, int previousCompletions, int bestCombo) {
        int reward;
        if (GRADE_LEGEND.equals(grade)) reward = 1750;
        else if (GRADE_GOLD.equals(grade)) reward = 1120;
        else if (GRADE_SILVER.equals(grade)) reward = 690;
        else if (GRADE_BRONZE.equals(grade)) reward = 380;
        else reward = 150;
        if (newBest) reward += 310;
        if (bestCombo >= 10) reward += 240;
        else if (bestCombo >= 7) reward += 165;
        else if (bestCombo >= 4) reward += 85;
        if (!newBest && previousCompletions > 0) {
            if (previousCompletions <= 2) reward = Math.max(170, Math.round(reward * 0.74f));
            else if (previousCompletions <= 7) reward = Math.max(135, Math.round(reward * 0.52f));
            else reward = Math.max(105, Math.round(reward * 0.36f));
        }
        return Math.max(90, reward);
    }

    public static int rewardXp(String grade, boolean newBest, int bestCombo) {
        int xp;
        if (GRADE_LEGEND.equals(grade)) xp = 270;
        else if (GRADE_GOLD.equals(grade)) xp = 180;
        else if (GRADE_SILVER.equals(grade)) xp = 112;
        else if (GRADE_BRONZE.equals(grade)) xp = 68;
        else xp = 30;
        if (newBest) xp += 42;
        if (bestCombo >= 7) xp += 36;
        else if (bestCombo >= 4) xp += 22;
        return Math.max(24, xp);
    }

    public static String repeatRewardRuleText(int previousCompletions, boolean newBest) {
        if (newBest) return "Yeni rekor bonusu aktif";
        if (previousCompletions <= 0) return "İlk drift bitirişi tam ödül";
        if (previousCompletions <= 2) return "Tekrar drift ödülü %74";
        if (previousCompletions <= 7) return "Tekrar drift ödülü %52";
        return "Tekrar drift ödülü güvenli minimum";
    }

    public static String modeStatsLine(SaveManager save) {
        if (save == null) return "En iyi skor - | Combo - | Toplam -";
        return "En iyi " + save.getDriftBestScore()
                + " | Combo x" + save.getDriftBestCombo()
                + " | Bitiş " + save.getDriftCompletedCount()
                + " | +" + save.getDriftEarnedCoins() + " coin";
    }

    public static String ruleText() {
        return "120 sn Open Field drift: hız + açı + kontrollü kayma + combo ile skor kazan.";
    }

    public static String comboLabel(float multiplier, int comboLevel) {
        if (multiplier >= 3.0f) return "EFSANE COMBO x" + oneDecimal(multiplier);
        if (multiplier >= 2.25f) return "UZUN COMBO x" + oneDecimal(multiplier);
        if (comboLevel >= 3) return "RİSKLİ COMBO x" + oneDecimal(multiplier);
        if (comboLevel >= 2) return "COMBO x" + oneDecimal(multiplier);
        return "KONTROLLÜ DRIFT";
    }

    static String oneDecimal(float value) {
        return String.format(java.util.Locale.US, "%.1f", value);
    }

    public static String rankCode(String grade) {
        if (GRADE_LEGEND.equals(grade)) return "S";
        if (GRADE_GOLD.equals(grade)) return "A";
        if (GRADE_SILVER.equals(grade)) return "B";
        if (GRADE_BRONZE.equals(grade)) return "C";
        return "D";
    }

    public static String scoringHint(VehicleController car, boolean insideDriftZone) {
        if (car == null) return "Açı + hız ile drift başlat";
        float speed = car.getSpeedKmh();
        float slip = Math.abs(car.getSlipAngleDeg());
        if (isSpinOut(car)) return "Spin kontrolü: combo kırılır";
        if (speed < MIN_SCORE_SPEED_KMH) return "Daha hızlı gir";
        if (slip < MIN_SCORE_SLIP_DEG) return "Daha fazla açı ver";
        if (insideDriftZone) return "Bonus alan: kontrollü kaymayı sürdür";
        return "Kontrollü drift: combo koru";
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
