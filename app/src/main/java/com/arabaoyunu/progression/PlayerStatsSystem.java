package com.arabaoyunu.progression;

import com.arabaoyunu.quest.QuestPanelSystem;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/** A63.6: Görev/başarım panelinde gösterilecek tamamlanmış oyuncu istatistik özetleri. */
public final class PlayerStatsSystem {
    private PlayerStatsSystem() {}

    public static String line(SaveManager s, int index) {
        if (s == null) return "İstatistik kaydı yok";
        switch (index) {
            case 0: return "Yarış: " + s.getCareerTotalRaces() + " | Galibiyet: " + s.getCareerTotalWins() + " | Mağlubiyet: " + s.getCareerTotalLosses();
            case 1: return "Coin: " + s.getTotalEarnedCoins() + " kazanıldı | Mevcut: " + s.getCoins();
            case 2: return "XP: " + s.getCareerTotalXp() + " | Seviye: " + s.getPlayerLevel() + " | Lig: " + s.getCareerLeagueName();
            case 3: return "Drag en iyi: " + formatSeconds(s.getDragBestSeconds()) + " | En yüksek hız: " + (int)s.getDragBestSpeedKmh() + " km/h";
            case 4: return "Checkpoint en iyi: " + formatSeconds(s.getRaceBestSeconds()) + " | Tamamlanan: " + s.getCheckpointRaceCompletedCount();
            case 5: return "Drift: " + s.getDriftTotalScore() + " toplam | En iyi: " + s.getDriftBestScore() + " | Combo: x" + s.getDriftBestCombo();
            case 6: return "Polis: " + s.getPoliceEscapes() + " kaçış / " + s.getPoliceCaughtCount() + " yakalanma | En uzun: " + formatSeconds(s.getPoliceBestSeconds());
            case 7: return "Trafik: " + s.getTrafficNearMissTotal() + " yakın geçiş | Çarpışma: " + s.getTrafficCollisions();
            case 8: return "Araç: " + QuestPanelSystem.ownedVehicleCount(s) + "/" + VehicleCatalog.count() + " | Modifiye toplam: " + s.getTotalPerformanceUpgradeLevelAllVehicles();
            case 9: return "Görevler: Günlük " + DailyWeeklyTaskSystem.completedDailyCount(s) + "/" + DailyWeeklyTaskSystem.DAILY_COUNT
                    + " | Haftalık " + DailyWeeklyTaskSystem.completedWeeklyCount(s) + "/" + DailyWeeklyTaskSystem.WEEKLY_COUNT;
            case 10: return "Ödül: Günlük " + s.getDailyTaskClaimedCount(DailyWeeklyTaskSystem.DAILY_COUNT)
                    + " | Haftalık " + s.getWeeklyTaskClaimedCount(DailyWeeklyTaskSystem.WEEKLY_COUNT)
                    + " | Başarım " + s.getAchievementRewardedCount(AchievementSystem.ACH_COUNT);
            default: return "Başarım: " + AchievementSystem.completedCount(s) + "/" + AchievementSystem.ACH_COUNT
                    + " | Bekleyen ödül: " + AchievementSystem.unclaimedCount(s);
        }
    }

    public static String formatSeconds(float seconds) {
        if (seconds <= 0f) return "-";
        int min = (int)(seconds / 60f);
        float sec = seconds - min * 60f;
        if (min > 0) return min + ":" + (sec < 10f ? "0" : "") + String.format(java.util.Locale.US, "%.2f", sec);
        return String.format(java.util.Locale.US, "%.2f sn", sec);
    }
}
