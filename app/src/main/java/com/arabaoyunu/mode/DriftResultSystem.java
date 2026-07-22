package com.arabaoyunu.mode;

import com.arabaoyunu.util.SaveManager;

/** A65.3: Drift sonucunu HUD/sonuc karti icin profesyonel rank/odul metnine cevirir. */
public final class DriftResultSystem {
    private DriftResultSystem() {}

    public static String resultTitle(String grade, boolean newBest) {
        if (newBest) return "DRIFT REKORU!";
        if (DriftScoreSystem.GRADE_LEGEND.equals(grade)) return "EFSANE DRIFT";
        if (DriftScoreSystem.GRADE_GOLD.equals(grade)) return "ALTIN DRIFT";
        if (DriftScoreSystem.GRADE_SILVER.equals(grade)) return "GÜMÜŞ DRIFT";
        if (DriftScoreSystem.GRADE_BRONZE.equals(grade)) return "BRONZ DRIFT";
        return "DRIFT TAMAMLANDI";
    }

    public static String resultLine(int score, int best, boolean newBest) {
        return "Skor " + score + "  |  En iyi " + Math.max(score, best) + (newBest ? "  YENİ" : "");
    }

    public static String comboLine(float longestSeconds, int bestCombo) {
        return "En uzun " + oneDecimal(longestSeconds) + " sn  |  En iyi combo x" + Math.max(0, bestCombo);
    }

    public static String rewardLine(int coins, int xp, String grade) {
        String code = DriftScoreSystem.rankCode(grade);
        return "Rank " + code + " / " + (grade == null || grade.length() == 0 ? "-" : grade)
                + "  |  Ödül +" + Math.max(0, coins) + " coin +" + Math.max(0, xp) + " XP";
    }

    public static String medalLine(String grade, boolean newBest, int bestCombo) {
        String code = DriftScoreSystem.rankCode(grade);
        return "Rank " + code + (newBest ? "  |  YENİ REKOR" : "") + "  |  En iyi combo x" + Math.max(0, bestCombo);
    }

    public static String rewardLine(SaveManager save, int coins, int xp, String grade) {
        return rewardLine(coins, xp, grade) + "  |  " + ModeProgressBridgeSystem.postResultLine(save, "Drift", coins, xp);
    }

    public static String modeHubSummary(SaveManager save) {
        if (save == null) return "Drift verisi yok";
        return "En iyi " + save.getDriftBestScore() + " • Rank " + DriftScoreSystem.rankCode(save.getDriftLastGrade())
                + " • x" + save.getDriftBestCombo() + " • " + save.getDriftCompletedCount() + " deneme";
    }

    public static String finishHint() {
        return "Gaz: tekrar dene  |  Menü: mod seç / garaj  |  Ödül tek sefer";
    }

    private static String oneDecimal(float value) {
        return String.format(java.util.Locale.US, "%.1f", value);
    }
}
