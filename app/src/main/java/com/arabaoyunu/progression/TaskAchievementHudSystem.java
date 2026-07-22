package com.arabaoyunu.progression;

import com.arabaoyunu.util.SaveManager;

/**
 * A63.9: Görev/başarım sistemini sürüş HUD'u, ana menü rozeti ve bildirimler
 * için tek merkezden özetler. Bu sınıf hiçbir ödül vermez; sadece güvenli
 * görüntüleme, bekleyen ödül sayımı ve ilerleme seçimi yapar.
 */
public final class TaskAchievementHudSystem {
    private TaskAchievementHudSystem() {}

    public static int pendingRewardCount(SaveManager save) {
        if (save == null) return 0;
        DailyWeeklyTaskSystem.ensureWindows(save);
        return DailyWeeklyTaskSystem.unclaimedDailyCount(save)
                + DailyWeeklyTaskSystem.unclaimedWeeklyCount(save)
                + AchievementSystem.unclaimedCount(save);
    }

    public static int pendingCoinTotal(SaveManager save) {
        if (save == null) return 0;
        return DailyWeeklyTaskSystem.pendingDailyCoinTotal(save)
                + DailyWeeklyTaskSystem.pendingWeeklyCoinTotal(save)
                + AchievementSystem.pendingCoinTotal(save);
    }

    public static int pendingXpTotal(SaveManager save) {
        if (save == null) return 0;
        return DailyWeeklyTaskSystem.pendingDailyXpTotal(save)
                + DailyWeeklyTaskSystem.pendingWeeklyXpTotal(save)
                + AchievementSystem.pendingXpTotal(save);
    }

    public static String pendingRewardBadge(SaveManager save) {
        int pending = pendingRewardCount(save);
        return pending > 0 ? "GÖREVLER (" + pending + ")" : "GÖREVLER";
    }

    public static String pendingSummary(SaveManager save) {
        int pending = pendingRewardCount(save);
        if (pending <= 0) return "Alınacak ödül yok";
        return pending + " ödül hazır  +" + pendingCoinTotal(save) + " coin  +" + pendingXpTotal(save) + " XP";
    }

    public static String dailyResetText(SaveManager save) {
        return "Günlük yenilenme: " + formatRemaining(millisUntilNextDay());
    }

    public static String weeklyResetText(SaveManager save) {
        return "Haftalık yenilenme: " + formatRemaining(millisUntilNextWeek(save));
    }

    public static String trackerTitle(SaveManager save) {
        if (save == null) return "GÖREV TAKİBİ";
        DailyWeeklyTaskSystem.ensureWindows(save);
        int claimType = firstClaimableType(save);
        if (claimType == 1) return "GÜNLÜK GÖREV TAMAMLANDI";
        if (claimType == 2) return "HAFTALIK GÖREV TAMAMLANDI";
        if (claimType == 3) return "BAŞARIM TAMAMLANDI";
        return bestTypeLabel(save);
    }

    public static String trackerSubtitle(SaveManager save) {
        if (save == null) return "Kayıt bekleniyor";
        DailyWeeklyTaskSystem.ensureWindows(save);
        int[] pair = bestItem(save);
        int type = pair[0];
        int id = pair[1];
        if (type == 1) return DailyWeeklyTaskSystem.dailyTitle(id);
        if (type == 2) return DailyWeeklyTaskSystem.weeklyTitle(id);
        if (type == 3) return AchievementSystem.label(id);
        return "Sürüşe devam et";
    }

    public static String trackerProgressText(SaveManager save) {
        if (save == null) return "-";
        int[] pair = bestItem(save);
        int type = pair[0];
        int id = pair[1];
        int p = 0;
        int t = 1;
        if (type == 1) { p = Math.max(0, DailyWeeklyTaskSystem.dailyProgress(save, id)); t = DailyWeeklyTaskSystem.dailyTarget(id); }
        else if (type == 2) { p = Math.max(0, DailyWeeklyTaskSystem.weeklyProgress(save, id)); t = DailyWeeklyTaskSystem.weeklyTarget(id); }
        else if (type == 3) { p = Math.max(0, AchievementSystem.progress(save, id)); t = AchievementSystem.target(id); }
        else return pendingSummary(save);
        return Math.min(p, t) + " / " + t + "  %" + (int)(100f * clamp01(p / (float)Math.max(1, t)));
    }

    public static float trackerProgress01(SaveManager save) {
        if (save == null) return 0f;
        int[] pair = bestItem(save);
        int type = pair[0];
        int id = pair[1];
        if (type == 1) return clamp01(DailyWeeklyTaskSystem.dailyProgress(save, id) / (float)Math.max(1, DailyWeeklyTaskSystem.dailyTarget(id)));
        if (type == 2) return clamp01(DailyWeeklyTaskSystem.weeklyProgress(save, id) / (float)Math.max(1, DailyWeeklyTaskSystem.weeklyTarget(id)));
        if (type == 3) return clamp01(AchievementSystem.progress(save, id) / (float)Math.max(1, AchievementSystem.target(id)));
        return 0f;
    }

    public static String trackerStatus(SaveManager save) {
        if (save == null) return "KAYIT YOK";
        int pending = pendingRewardCount(save);
        if (pending > 0) return pendingSummary(save);
        int[] pair = bestItem(save);
        if (pair[0] == 1) return dailyResetText(save);
        if (pair[0] == 2) return weeklyResetText(save);
        return dailyResetText(save) + " | " + weeklyResetText(save);
    }

    public static String completionPopupTitle(SaveManager save) {
        return completionPopupTitle(save, 1);
    }

    public static String completionPopupTitle(SaveManager save, int notificationMode) {
        if (notificationMode <= 0) return "ÖDÜL HAZIR";
        int type = firstClaimableType(save);
        if (type == 3) return "BAŞARIM TAMAMLANDI!";
        if (type == 2) return "HAFTALIK GÖREV TAMAMLANDI!";
        return "GÖREV TAMAMLANDI!";
    }

    public static String completionPopupSubtitle(SaveManager save) {
        return completionPopupSubtitle(save, 1);
    }

    public static String completionPopupSubtitle(SaveManager save, int notificationMode) {
        if (save == null) return "Ödül hazır";
        if (notificationMode <= 0) return pendingRewardCount(save) + " ödül bekliyor";
        if (notificationMode >= 2) {
            return trackerSubtitle(save) + " | " + pendingSummary(save) + " | Panelden ÖDÜL AL veya HEPSİNİ AL";
        }
        return trackerSubtitle(save) + " | " + pendingSummary(save);
    }

    private static int firstClaimableType(SaveManager save) {
        if (save == null) return 0;
        for (int i = 0; i < DailyWeeklyTaskSystem.DAILY_COUNT; i++) if (DailyWeeklyTaskSystem.isDailyClaimable(save, i)) return 1;
        for (int i = 0; i < DailyWeeklyTaskSystem.WEEKLY_COUNT; i++) if (DailyWeeklyTaskSystem.isWeeklyClaimable(save, i)) return 2;
        for (int i = 0; i < AchievementSystem.ACH_COUNT; i++) if (AchievementSystem.isClaimable(save, i)) return 3;
        return 0;
    }

    private static String bestTypeLabel(SaveManager save) {
        int[] pair = bestItem(save);
        if (pair[0] == 1) return "GÜNLÜK GÖREV TAKİBİ";
        if (pair[0] == 2) return "HAFTALIK GÖREV TAKİBİ";
        if (pair[0] == 3) return "BAŞARIM TAKİBİ";
        return "GÖREV TAKİBİ";
    }

    private static int[] bestItem(SaveManager save) {
        int type = firstClaimableType(save);
        if (type == 1) return new int[]{1, firstClaimableDaily(save)};
        if (type == 2) return new int[]{2, firstClaimableWeekly(save)};
        if (type == 3) return new int[]{3, firstClaimableAchievement(save)};
        float best = -1f;
        int bestType = 0;
        int bestId = 0;
        for (int i = 0; i < DailyWeeklyTaskSystem.DAILY_COUNT; i++) {
            if (save != null && !save.isDailyTaskRewardClaimed(i)) {
                float r = clamp01(DailyWeeklyTaskSystem.dailyProgress(save, i) / (float)Math.max(1, DailyWeeklyTaskSystem.dailyTarget(i)));
                if (r < 1f && r > best) { best = r; bestType = 1; bestId = i; }
            }
        }
        for (int i = 0; i < DailyWeeklyTaskSystem.WEEKLY_COUNT; i++) {
            if (save != null && !save.isWeeklyTaskRewardClaimed(i)) {
                float r = clamp01(DailyWeeklyTaskSystem.weeklyProgress(save, i) / (float)Math.max(1, DailyWeeklyTaskSystem.weeklyTarget(i)));
                if (r < 1f && r > best) { best = r; bestType = 2; bestId = i; }
            }
        }
        for (int i = 0; i < AchievementSystem.ACH_COUNT; i++) {
            if (save != null && !save.isAchievementRewardClaimed(i)) {
                float r = clamp01(AchievementSystem.progress(save, i) / (float)Math.max(1, AchievementSystem.target(i)));
                if (r < 1f && r > best) { best = r; bestType = 3; bestId = i; }
            }
        }
        return new int[]{bestType, bestId};
    }

    private static int firstClaimableDaily(SaveManager save) {
        for (int i = 0; i < DailyWeeklyTaskSystem.DAILY_COUNT; i++) if (DailyWeeklyTaskSystem.isDailyClaimable(save, i)) return i;
        return 0;
    }

    private static int firstClaimableWeekly(SaveManager save) {
        for (int i = 0; i < DailyWeeklyTaskSystem.WEEKLY_COUNT; i++) if (DailyWeeklyTaskSystem.isWeeklyClaimable(save, i)) return i;
        return 0;
    }

    private static int firstClaimableAchievement(SaveManager save) {
        for (int i = 0; i < AchievementSystem.ACH_COUNT; i++) if (AchievementSystem.isClaimable(save, i)) return i;
        return 0;
    }

    private static long millisUntilNextDay() {
        long now = System.currentTimeMillis();
        long dayMs = 86400000L;
        long next = (now / dayMs + 1L) * dayMs;
        return Math.max(0L, next - now);
    }

    private static long millisUntilNextWeek(SaveManager save) {
        long now = System.currentTimeMillis();
        long dayMs = 86400000L;
        long day = now / dayMs;
        long week = save == null ? day / 7L : save.getCurrentWeekStamp();
        long nextWeekDay = (week + 1L) * 7L;
        return Math.max(0L, nextWeekDay * dayMs - now);
    }

    private static String formatRemaining(long millis) {
        long totalMinutes = Math.max(0L, millis / 60000L);
        long days = totalMinutes / (60L * 24L);
        long hours = (totalMinutes / 60L) % 24L;
        long minutes = totalMinutes % 60L;
        if (days > 0) return days + "g " + hours + "s";
        if (hours > 0) return hours + "s " + minutes + "d";
        return Math.max(1L, minutes) + "d";
    }

    private static float clamp01(float value) {
        if (value != value) return 0f;
        if (value < 0f) return 0f;
        if (value > 1f) return 1f;
        return value;
    }
}
