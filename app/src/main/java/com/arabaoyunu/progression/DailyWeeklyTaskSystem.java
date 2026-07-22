package com.arabaoyunu.progression;

import com.arabaoyunu.util.SaveManager;

/** A63.8: Günlük/haftalık görevler. Kartlı panel için tek tek claim ve toplu ödül alma desteği eklendi. */
public final class DailyWeeklyTaskSystem {
    public static final int DAILY_COUNT = 6;
    public static final int WEEKLY_COUNT = 6;

    private DailyWeeklyTaskSystem() {}

    public static void ensureWindows(SaveManager save) {
        if (save != null) save.ensureDailyWeeklyTaskWindows();
    }

    public static String dailyTitle(int id) {
        switch (id) {
            case 0: return "3 yarış tamamla";
            case 1: return "5 yakın geçiş yap";
            case 2: return "1 drag yarışı tamamla";
            case 3: return "3.000 drift skoru yap";
            case 4: return "500 metre sür";
            case 5: return "1 polis kovalamacası oyna";
            default: return "Günlük görev";
        }
    }

    public static String weeklyTitle(int id) {
        switch (id) {
            case 0: return "25 yarış tamamla";
            case 1: return "100 yakın geçiş yap";
            case 2: return "50.000 drift skoru yap";
            case 3: return "10 polis kaçışı yap";
            case 4: return "3 araç yükseltmesi satın al";
            case 5: return "15.000 coin kazan";
            default: return "Haftalık görev";
        }
    }

    public static String dailyDescription(int id) {
        switch (id) {
            case 0: return "Kariyer veya serbest sürüşte toplam 3 yarışı bitir";
            case 1: return "Trafikte temiz yakın geçişler yap";
            case 2: return "Drag modunda bir yarışı tamamla";
            case 3: return "Drift skorunu tek gün içinde biriktir";
            case 4: return "Haritada kısa bir sürüş yap";
            case 5: return "Polis kovalamaca moduna gir";
            default: return "Günlük hedef";
        }
    }

    public static String weeklyDescription(int id) {
        switch (id) {
            case 0: return "Hafta boyunca yarış sayını yükselt";
            case 1: return "Riskli trafikte temiz yakın geçiş serisi yap";
            case 2: return "Büyük drift puanı biriktir";
            case 3: return "Polisten kaçış sayını yükselt";
            case 4: return "Araçlarını garajda güçlendir";
            case 5: return "Ödül ve yarışlardan coin biriktir";
            default: return "Haftalık hedef";
        }
    }

    public static int dailyTarget(int id) {
        switch (id) {
            case 0: return 3;
            case 1: return 5;
            case 2: return 1;
            case 3: return 3000;
            case 4: return 500;
            case 5: return 1;
            default: return 1;
        }
    }

    public static int weeklyTarget(int id) {
        switch (id) {
            case 0: return 25;
            case 1: return 100;
            case 2: return 50000;
            case 3: return 10;
            case 4: return 3;
            case 5: return 15000;
            default: return 1;
        }
    }

    public static int dailyProgress(SaveManager save, int id) {
        if (save == null) return 0;
        ensureWindows(save);
        switch (id) {
            case 0: return save.getCareerTotalRaces() - save.getTaskWindowBaseline(false, "races");
            case 1: return save.getTrafficNearMissTotal() - save.getTaskWindowBaseline(false, "near");
            case 2: return save.getDragRaceCompletedCount() - save.getTaskWindowBaseline(false, "drag");
            case 3: return save.getDriftTotalScore() - save.getTaskWindowBaseline(false, "drift");
            case 4: return save.getDrivenMeters() - save.getTaskWindowBaseline(false, "meters");
            case 5: return save.getPoliceTotalChases() - save.getTaskWindowBaseline(false, "police_chases");
            default: return 0;
        }
    }

    public static int weeklyProgress(SaveManager save, int id) {
        if (save == null) return 0;
        ensureWindows(save);
        switch (id) {
            case 0: return save.getCareerTotalRaces() - save.getTaskWindowBaseline(true, "races");
            case 1: return save.getTrafficNearMissTotal() - save.getTaskWindowBaseline(true, "near");
            case 2: return save.getDriftTotalScore() - save.getTaskWindowBaseline(true, "drift");
            case 3: return save.getPoliceEscapes() - save.getTaskWindowBaseline(true, "police_escapes");
            case 4: return save.getTotalPerformanceUpgradeLevelAllVehicles() - save.getTaskWindowBaseline(true, "upgrades");
            case 5: return save.getTotalEarnedCoins() - save.getTaskWindowBaseline(true, "coins");
            default: return 0;
        }
    }

    public static int dailyCoinReward(int id) { return 220 + id * 70; }
    public static int dailyXpReward(int id) { return 35 + id * 10; }
    public static int weeklyCoinReward(int id) { return 1500 + id * 350; }
    public static int weeklyXpReward(int id) { return 240 + id * 55; }

    public static boolean isDailyComplete(SaveManager save, int id) { return dailyProgress(save, id) >= dailyTarget(id); }
    public static boolean isWeeklyComplete(SaveManager save, int id) { return weeklyProgress(save, id) >= weeklyTarget(id); }
    public static boolean isDailyClaimable(SaveManager save, int id) { return save != null && isDailyComplete(save, id) && !save.isDailyTaskRewardClaimed(id); }
    public static boolean isWeeklyClaimable(SaveManager save, int id) { return save != null && isWeeklyComplete(save, id) && !save.isWeeklyTaskRewardClaimed(id); }

    public static String dailyStatus(SaveManager save, int id) {
        if (save == null) return "KAYIT YOK";
        ensureWindows(save);
        if (save.isDailyTaskRewardClaimed(id)) return "ÖDÜL ALINDI";
        if (isDailyComplete(save, id)) return "TAMAMLANDI - ÖDÜL AL";
        return "DEVAM EDİYOR";
    }

    public static String weeklyStatus(SaveManager save, int id) {
        if (save == null) return "KAYIT YOK";
        ensureWindows(save);
        if (save.isWeeklyTaskRewardClaimed(id)) return "ÖDÜL ALINDI";
        if (isWeeklyComplete(save, id)) return "TAMAMLANDI - ÖDÜL AL";
        return "DEVAM EDİYOR";
    }

    public static int unclaimedDailyCount(SaveManager save) {
        int count = 0;
        for (int i = 0; i < DAILY_COUNT; i++) if (isDailyClaimable(save, i)) count++;
        return count;
    }

    public static int unclaimedWeeklyCount(SaveManager save) {
        int count = 0;
        for (int i = 0; i < WEEKLY_COUNT; i++) if (isWeeklyClaimable(save, i)) count++;
        return count;
    }

    public static int pendingDailyCoinTotal(SaveManager save) {
        int total = 0;
        for (int i = 0; i < DAILY_COUNT; i++) if (isDailyClaimable(save, i)) total += dailyCoinReward(i);
        return total;
    }

    public static int pendingDailyXpTotal(SaveManager save) {
        int total = 0;
        for (int i = 0; i < DAILY_COUNT; i++) if (isDailyClaimable(save, i)) total += dailyXpReward(i);
        return total;
    }

    public static int pendingWeeklyCoinTotal(SaveManager save) {
        int total = 0;
        for (int i = 0; i < WEEKLY_COUNT; i++) if (isWeeklyClaimable(save, i)) total += weeklyCoinReward(i);
        return total;
    }

    public static int pendingWeeklyXpTotal(SaveManager save) {
        int total = 0;
        for (int i = 0; i < WEEKLY_COUNT; i++) if (isWeeklyClaimable(save, i)) total += weeklyXpReward(i);
        return total;
    }

    public static String dailyLine(SaveManager save, int line) {
        ensureWindows(save);
        if (line == 0) return "Günlük görevler: " + completedDailyCount(save) + "/" + DAILY_COUNT
                + " | Bekleyen ödül: " + unclaimedDailyCount(save)
                + " | Alınan: " + (save == null ? 0 : save.getDailyTaskClaimedCount(DAILY_COUNT));
        int id = line - 1;
        if (id < 0 || id >= DAILY_COUNT) return "Tamamlanan görevde panele dokun: ÖDÜL AL";
        int p = Math.max(0, dailyProgress(save, id));
        int t = dailyTarget(id);
        boolean claimed = save != null && save.isDailyTaskRewardClaimed(id);
        boolean done = p >= t;
        String mark = claimed ? "✓" : (done ? "AL" : " ");
        return "[" + mark + "] " + dailyTitle(id) + " — " + Math.min(p, t) + "/" + t
                + " | " + dailyStatus(save, id)
                + " | +" + dailyCoinReward(id) + " coin +" + dailyXpReward(id) + " XP";
    }

    public static String weeklyLine(SaveManager save, int line) {
        ensureWindows(save);
        if (line == 0) return "Haftalık görevler: " + completedWeeklyCount(save) + "/" + WEEKLY_COUNT
                + " | Bekleyen ödül: " + unclaimedWeeklyCount(save)
                + " | Alınan: " + (save == null ? 0 : save.getWeeklyTaskClaimedCount(WEEKLY_COUNT));
        int id = line - 1;
        if (id < 0 || id >= WEEKLY_COUNT) return "Tamamlanan görevde panele dokun: ÖDÜL AL";
        int p = Math.max(0, weeklyProgress(save, id));
        int t = weeklyTarget(id);
        boolean claimed = save != null && save.isWeeklyTaskRewardClaimed(id);
        boolean done = p >= t;
        String mark = claimed ? "✓" : (done ? "AL" : " ");
        return "[" + mark + "] " + weeklyTitle(id) + " — " + Math.min(p, t) + "/" + t
                + " | " + weeklyStatus(save, id)
                + " | +" + weeklyCoinReward(id) + " coin +" + weeklyXpReward(id) + " XP";
    }

    public static int completedDailyCount(SaveManager save) {
        int count = 0;
        for (int i = 0; i < DAILY_COUNT; i++) if (isDailyComplete(save, i)) count++;
        return count;
    }

    public static int completedWeeklyCount(SaveManager save) {
        int count = 0;
        for (int i = 0; i < WEEKLY_COUNT; i++) if (isWeeklyComplete(save, i)) count++;
        return count;
    }

    public static boolean claimDailyTask(SaveManager save, int id) {
        if (!isDailyClaimable(save, id)) return false;
        return save.claimDailyTaskReward(id, dailyCoinReward(id), dailyXpReward(id), dailyTitle(id));
    }

    public static boolean claimWeeklyTask(SaveManager save, int id) {
        if (!isWeeklyClaimable(save, id)) return false;
        return save.claimWeeklyTaskReward(id, weeklyCoinReward(id), weeklyXpReward(id), weeklyTitle(id));
    }

    public static boolean claimFirstCompletedDailyTask(SaveManager save) {
        if (save == null) return false;
        ensureWindows(save);
        for (int i = 0; i < DAILY_COUNT; i++) if (claimDailyTask(save, i)) return true;
        return false;
    }

    public static boolean claimFirstCompletedWeeklyTask(SaveManager save) {
        if (save == null) return false;
        ensureWindows(save);
        for (int i = 0; i < WEEKLY_COUNT; i++) if (claimWeeklyTask(save, i)) return true;
        return false;
    }

    public static int claimAllCompletedDailyTasks(SaveManager save) {
        if (save == null) return 0;
        ensureWindows(save);
        int claimed = 0;
        int coins = 0;
        int xp = 0;
        for (int i = 0; i < DAILY_COUNT; i++) {
            if (isDailyClaimable(save, i) && save.claimDailyTaskReward(i, dailyCoinReward(i), dailyXpReward(i), dailyTitle(i))) {
                claimed++;
                coins += dailyCoinReward(i);
                xp += dailyXpReward(i);
            }
        }
        if (claimed > 0) save.setEconomyLastMessage("TÜM GÜNLÜK ÖDÜLLER ALINDI: +" + coins + " coin +" + xp + " XP");
        return claimed;
    }

    public static int claimAllCompletedWeeklyTasks(SaveManager save) {
        if (save == null) return 0;
        ensureWindows(save);
        int claimed = 0;
        int coins = 0;
        int xp = 0;
        for (int i = 0; i < WEEKLY_COUNT; i++) {
            if (isWeeklyClaimable(save, i) && save.claimWeeklyTaskReward(i, weeklyCoinReward(i), weeklyXpReward(i), weeklyTitle(i))) {
                claimed++;
                coins += weeklyCoinReward(i);
                xp += weeklyXpReward(i);
            }
        }
        if (claimed > 0) save.setEconomyLastMessage("TÜM HAFTALIK ÖDÜLLER ALINDI: +" + coins + " coin +" + xp + " XP");
        return claimed;
    }
}
