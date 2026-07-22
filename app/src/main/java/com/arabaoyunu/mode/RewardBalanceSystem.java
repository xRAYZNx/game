package com.arabaoyunu.mode;

import com.arabaoyunu.util.SaveManager;

/**
 * A65.8: Checkpoint, Drift ve Polis modlarının coin/XP dengesini tek yerde toplar.
 * Amaç bir modun diğerlerine göre aşırı para kasma yoluna dönüşmesini önlemek,
 * yeni rekor/madalya/rank/yıldız bonuslarını korurken tekrar ödülünü güvenli sınıra almaktır.
 */
public final class RewardBalanceSystem {
    public static final int SCHEMA_VERSION = 658;
    public static final String MODE_CHECKPOINT = "Checkpoint";
    public static final String MODE_DRIFT = "Drift";
    public static final String MODE_POLICE = "Polis";

    private RewardBalanceSystem() {}

    public static int balancedCheckpointCoins(int routeId, String medal, boolean newBest, boolean newMedal, int previousCompletions) {
        int raw = CheckpointRaceSystem.rewardCoins(routeId, medal, newBest, newMedal, Math.max(0, previousCompletions));
        return clampModeCoins(MODE_CHECKPOINT, raw, newBest, newMedal, previousCompletions);
    }

    public static int balancedCheckpointXp(int routeId, String medal, boolean newBest, boolean newMedal) {
        int raw = CheckpointRaceSystem.rewardXp(routeId, medal, newBest, newMedal);
        return clampModeXp(MODE_CHECKPOINT, raw);
    }

    public static int balancedDriftCoins(String rank, boolean newBest, int previousCompletions, int bestCombo) {
        int raw = DriftScoreSystem.rewardCoins(rank, newBest, Math.max(0, previousCompletions), Math.max(0, bestCombo));
        boolean milestone = DriftScoreSystem.GRADE_LEGEND.equals(rank) || DriftScoreSystem.GRADE_GOLD.equals(rank) || bestCombo >= 3;
        return clampModeCoins(MODE_DRIFT, raw, newBest, milestone, previousCompletions);
    }

    public static int balancedDriftXp(String rank, boolean newBest, int bestCombo) {
        int raw = DriftScoreSystem.rewardXp(rank, newBest, Math.max(0, bestCombo));
        return clampModeXp(MODE_DRIFT, raw);
    }

    public static int balancedPoliceCoins(boolean escaped, int wantedLevel, float chaseTime, boolean newBest, int previousChases) {
        int raw = PoliceChaseSystem.coinReward(escaped, wantedLevel, chaseTime);
        boolean milestone = escaped && wantedLevel >= 3;
        return clampModeCoins(MODE_POLICE, raw, newBest, milestone, previousChases);
    }

    public static int balancedPoliceXp(boolean escaped, int wantedLevel, float chaseTime) {
        int raw = PoliceChaseSystem.xpReward(escaped, wantedLevel, chaseTime);
        return clampModeXp(MODE_POLICE, raw);
    }

    public static int clampModeCoins(String mode, int rawCoins, boolean newRecord, boolean milestone, int previousCompletions) {
        int raw = Math.max(0, rawCoins);
        int cap = modeCoinCap(mode);
        int floor = modeCoinFloor(mode);
        if (raw == 0) return 0;
        int result = Math.min(raw, cap);

        // A65.8: Tekrar oynama ödülü azalsın ama mod tamamen boşa dönmesin.
        int repeats = Math.max(0, previousCompletions);
        if (!newRecord && !milestone && repeats >= 3) {
            float factor = repeats >= 10 ? 0.45f : repeats >= 6 ? 0.55f : 0.68f;
            result = Math.round(result * factor);
        }
        if (newRecord) result = Math.max(result, Math.min(cap, floor + 120));
        if (milestone) result = Math.max(result, Math.min(cap, floor + 90));
        return Math.max(floor, Math.min(cap, result));
    }

    public static int clampModeXp(String mode, int rawXp) {
        int raw = Math.max(0, rawXp);
        if (raw == 0) return 0;
        int cap;
        if (MODE_POLICE.equals(mode)) cap = 260;
        else if (MODE_DRIFT.equals(mode)) cap = 240;
        else cap = 220;
        return Math.max(35, Math.min(cap, raw));
    }

    public static int modeCoinCap(String mode) {
        if (MODE_POLICE.equals(mode)) return 1850;
        if (MODE_DRIFT.equals(mode)) return 1700;
        return 1600;
    }

    public static int modeCoinFloor(String mode) {
        if (MODE_POLICE.equals(mode)) return 90;
        if (MODE_DRIFT.equals(mode)) return 80;
        return 85;
    }

    public static int careerPointsForCheckpoint(String medal, boolean newBest, boolean newMedal, int routeId) {
        int rank = CheckpointRaceSystem.medalRank(medal);
        int points = 8 + rank * 4 + Math.max(0, routeId) * 2;
        if (newBest) points += 8;
        if (newMedal) points += 10;
        return Math.max(0, points);
    }

    public static int careerPointsForDrift(String rank, boolean newBest, int bestCombo) {
        int points = 8 + Math.max(0, bestCombo) * 3;
        if (DriftScoreSystem.GRADE_LEGEND.equals(rank)) points += 28;
        else if (DriftScoreSystem.GRADE_GOLD.equals(rank)) points += 20;
        else if (DriftScoreSystem.GRADE_SILVER.equals(rank)) points += 14;
        else if (DriftScoreSystem.GRADE_BRONZE.equals(rank)) points += 8;
        if (newBest) points += 10;
        return Math.max(0, points);
    }

    public static int careerPointsForPolice(boolean escaped, int wantedLevel, int seconds, boolean newBest) {
        int points = escaped ? 16 : 5;
        points += Math.max(1, wantedLevel) * 5;
        points += Math.min(12, Math.max(0, seconds) / 15);
        if (newBest) points += 10;
        return Math.max(0, points);
    }

    public static String economyBalanceLine(SaveManager save) {
        if (save == null) return "Ekonomi: veri bekleniyor";
        int checkpoint = save.getCheckpointRouteTotalEarnedCoins();
        int drift = save.getDriftEarnedCoins();
        int police = save.getPoliceEarnedCoins();
        int max = Math.max(checkpoint, Math.max(drift, police));
        int min = Math.min(checkpoint, Math.min(drift, police));
        String state = max > 0 && min * 3 < max ? "denge izleniyor" : "dengeli";
        return "Ekonomi " + state + " • CP +" + checkpoint + " • Drift +" + drift + " • Polis +" + police;
    }

    public static String rewardRuleSummary() {
        return "Ödül: ilk bitiriş + rekor/madalya/rank/yıldız bonusu • tekrar ödülü kademeli • sonuçtan çoğaltma yok";
    }
}
