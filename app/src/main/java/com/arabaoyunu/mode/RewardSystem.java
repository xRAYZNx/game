package com.arabaoyunu.mode;

/** Plan 12 odul altyapisi: ileride garaj/para sistemine baglanacak. */
public final class RewardSystem {

    public int calculateCoinsForDrift(int score, int crashPenalty, boolean finished) {
        if (!finished) return 0;
        // ArabaOyunu_26: garaj ekonomisi için drift ödülü artık daha anlamlı.
        int baseCoins = Math.max(0, score / 110);
        int comboBonus = Math.min(850, Math.max(0, score / 900));
        int finishBonus = 140;
        int penaltyCut = Math.max(0, crashPenalty / 650);
        return Math.max(0, baseCoins + comboBonus + finishBonus - penaltyCut);
    }

    public int calculateCoinsForMission(int difficulty, boolean dailyBonus) {
        int base = 320 + Math.max(0, difficulty) * 140;
        return dailyBonus ? base + 450 : base;
    }

    public String getRankForScore(int score) {
        if (score >= 45000) return "S";
        if (score >= 30000) return "A";
        if (score >= 18000) return "B";
        if (score >= 9000) return "C";
        return "D";
    }
}
