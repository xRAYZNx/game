package com.arabaoyunu.mode;

import com.arabaoyunu.physics.DriftSystem;
import com.arabaoyunu.physics.VehicleController;

/**
 * ArabaOyunu_62_5: Drift modu puan/odul/kariyer kurallari.
 *
 * Bu sinif fizik tarafina mudahele etmez; drift algisini DriftSystem +
 * VehicleController telemetrisi uzerinden yorumlar. Boylece harita veya yeni GLB
 * gerektirmeden drift modu tekrar oynanabilir bir kariyer/ekonomi moduna baglanir.
 */
public final class DriftModeSystem {

    public static final int TARGET_BRONZE = DriftScoreSystem.TARGET_BRONZE;
    public static final int TARGET_SILVER = DriftScoreSystem.TARGET_SILVER;
    public static final int TARGET_GOLD = DriftScoreSystem.TARGET_GOLD;
    public static final int TARGET_LEGEND = DriftScoreSystem.TARGET_LEGEND;

    public static final String GRADE_LEGEND = DriftScoreSystem.GRADE_LEGEND;
    public static final String GRADE_GOLD = DriftScoreSystem.GRADE_GOLD;
    public static final String GRADE_SILVER = DriftScoreSystem.GRADE_SILVER;
    public static final String GRADE_BRONZE = DriftScoreSystem.GRADE_BRONZE;
    public static final String GRADE_FINISH = DriftScoreSystem.GRADE_FINISH;

    private DriftModeSystem() {}

    public static String gradeForScore(int score) {
        return DriftScoreSystem.gradeForScore(score);
    }

    public static int rewardForGrade(String grade) {
        return DriftScoreSystem.rewardCoins(grade, false, 0, 0);
    }

    public static int xpForGrade(String grade) {
        return DriftScoreSystem.rewardXp(grade, false, 0);
    }

    public static int targetForNextGrade(int score) {
        return DriftScoreSystem.targetForNextGrade(score);
    }

    public static String targetText(int score) {
        return DriftScoreSystem.targetText(score);
    }

    public static String liveCallout(VehicleController car, DriftSystem drift) {
        if (car == null || drift == null) return "";
        String breakMessage = drift.getComboBreakMessage();
        if (breakMessage != null && breakMessage.length() > 0 && !drift.isActive()) return breakMessage;
        if (!drift.isActive()) return DriftScoreSystem.scoringHint(car, drift.isInsideDriftZone());
        float slip = Math.abs(car.getSlipAngleDeg());
        if (DriftScoreSystem.isSpinOut(car)) return "SPIN RİSKİ";
        if (drift.getMultiplier() >= 2.15f) return DriftScoreSystem.comboLabel(drift.getMultiplier(), drift.getComboLevel());
        if (drift.getDriftTime() >= 11f) return "UZUN KONTROLLÜ DRIFT";
        if (slip >= DriftScoreSystem.IDEAL_SLIP_MIN_DEG && slip <= DriftScoreSystem.IDEAL_SLIP_MAX_DEG) return "MÜKEMMEL AÇI";
        return drift.isInsideDriftZone() ? "BONUS ALANDA DRIFT" : "İYİ DRIFT";
    }

    public static float controlBonus(VehicleController.Tuning tuning) {
        if (tuning == null) return 1f;
        // A65.3: Drift tuning daha odaklı avantaj alır; Race/Police tutuşu skoru patlatmaz.
        float driftEase = clamp((tuning.driftSlipAngleDeg - 11.0f) * 0.018f, -0.06f, 0.14f);
        float handbrake = clamp((2.80f - tuning.handbrakeGrip) * 0.060f, -0.05f, 0.16f);
        float steering = clamp((tuning.maxSteerAngleDeg - 34f) * 0.010f, -0.04f, 0.12f);
        float gripPenalty = clamp((tuning.lateralGrip - 7.85f) * -0.020f, -0.08f, 0.08f);
        return clamp(1f + driftEase + handbrake + steering + gripPenalty, 0.86f, 1.24f);
    }

    private static String oneDecimal(float value) {
        return String.format(java.util.Locale.US, "%.1f", value);
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
