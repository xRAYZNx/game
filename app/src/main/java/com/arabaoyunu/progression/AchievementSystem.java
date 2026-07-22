package com.arabaoyunu.progression;

import com.arabaoyunu.quest.QuestPanelSystem;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * A63.8: Başarımlar kartlı görev paneli için tek tek ödül alma ve
 * toplu "Hepsini Al" hesaplarıyla genişletildi.
 *
 * Başarımlar kayıtlı istatistiklerden hesaplanır. Tamamlanan başarımlar
 * kendiliğinden coin/XP vermez; ödül oyuncunun karttaki "ÖDÜL AL"
 * butonuna veya ilgili sekmedeki "HEPSİNİ AL" butonuna basmasıyla verilir.
 */
public final class AchievementSystem {

    public static final int ACH_FIRST_RACE = 0;
    public static final int ACH_FIRST_WIN = 1;
    public static final int ACH_DRAG_MASTER = 2;
    public static final int ACH_DRIFTER = 3;
    public static final int ACH_ESCAPE_ARTIST = 4;
    public static final int ACH_RISK_LOVER = 5;
    public static final int ACH_GARAGE_OWNER = 6;
    public static final int ACH_COLLECTOR = 7;
    public static final int ACH_COUNT = 8;

    // Eski ProgressionSystem çağrıları için geriye dönük sabitler.
    public static final int ACH_FIRST_MISSION = ACH_FIRST_RACE;
    public static final int ACH_RACE_WIN = ACH_FIRST_WIN;
    public static final int ACH_POLICE_ESCAPE = ACH_ESCAPE_ARTIST;
    public static final int ACH_DAILY_3 = 8;
    public static final int ACH_WEEKLY_10 = 9;
    public static final int ACH_LEVEL_5 = 10;
    public static final int ACH_DISTANCE_10KM = 11;
    public static final int ACH_CRATE_OPEN = 12;

    private AchievementSystem() {}

    public static String key(int id) { return "ach_" + id; }

    public static String label(int id) {
        switch (id) {
            case ACH_FIRST_RACE: return "İlk Yarış";
            case ACH_FIRST_WIN: return "İlk Galibiyet";
            case ACH_DRAG_MASTER: return "Drag Ustası";
            case ACH_DRIFTER: return "Driftçi";
            case ACH_ESCAPE_ARTIST: return "Kaçışçı";
            case ACH_RISK_LOVER: return "Risk Seven";
            case ACH_GARAGE_OWNER: return "Garajcı";
            case ACH_COLLECTOR: return "Koleksiyoncu";
            case ACH_DAILY_3: return "Günlük Seri";
            case ACH_WEEKLY_10: return "Haftalık Sürücü";
            case ACH_LEVEL_5: return "Seviye 5";
            case ACH_DISTANCE_10KM: return "10 KM Sürüş";
            case ACH_CRATE_OPEN: return "İlk Kasa";
            default: return "Başarım";
        }
    }

    public static String description(int id) {
        switch (id) {
            case ACH_FIRST_RACE: return "İlk yarışı tamamla";
            case ACH_FIRST_WIN: return "İlk yarışı kazan";
            case ACH_DRAG_MASTER: return "10 drag yarışı tamamla";
            case ACH_DRIFTER: return "50.000 toplam drift skoru yap";
            case ACH_ESCAPE_ARTIST: return "5 polis kovalamacasından kaç";
            case ACH_RISK_LOVER: return "25 yakın geçiş yap";
            case ACH_GARAGE_OWNER: return "İlk performans modifiyesini satın al";
            case ACH_COLLECTOR: return "3 araç satın al";
            default: return "Oyunda ilerle";
        }
    }

    public static int target(int id) {
        switch (id) {
            case ACH_FIRST_RACE: return 1;
            case ACH_FIRST_WIN: return 1;
            case ACH_DRAG_MASTER: return 10;
            case ACH_DRIFTER: return 50000;
            case ACH_ESCAPE_ARTIST: return 5;
            case ACH_RISK_LOVER: return 25;
            case ACH_GARAGE_OWNER: return 1;
            case ACH_COLLECTOR: return 3;
            default: return 1;
        }
    }

    public static int progress(SaveManager save, int id) {
        if (save == null) return 0;
        switch (id) {
            case ACH_FIRST_RACE: return save.getCareerTotalRaces();
            case ACH_FIRST_WIN: return save.getCareerTotalWins();
            case ACH_DRAG_MASTER: return save.getDragRaceCompletedCount();
            case ACH_DRIFTER: return save.getDriftTotalScore();
            case ACH_ESCAPE_ARTIST: return save.getPoliceEscapes();
            case ACH_RISK_LOVER: return save.getTrafficNearMissTotal();
            case ACH_GARAGE_OWNER: return save.getTotalPerformanceUpgradeLevelAllVehicles();
            case ACH_COLLECTOR: return QuestPanelSystem.ownedVehicleCount(save);
            default: return 0;
        }
    }

    public static boolean isCompleted(SaveManager save, int id) {
        return progress(save, id) >= target(id);
    }

    public static boolean isClaimable(SaveManager save, int id) {
        return save != null && isCompleted(save, id) && !save.isAchievementRewardClaimed(id);
    }

    public static int xpReward(int id) {
        switch (id) {
            case ACH_FIRST_RACE: return 60;
            case ACH_FIRST_WIN: return 90;
            case ACH_DRAG_MASTER: return 180;
            case ACH_DRIFTER: return 220;
            case ACH_ESCAPE_ARTIST: return 190;
            case ACH_RISK_LOVER: return 150;
            case ACH_GARAGE_OWNER: return 110;
            case ACH_COLLECTOR: return 240;
            case ACH_DAILY_3: return 130;
            case ACH_WEEKLY_10: return 220;
            case ACH_LEVEL_5: return 240;
            case ACH_DISTANCE_10KM: return 260;
            case ACH_CRATE_OPEN: return 90;
            default: return 50;
        }
    }

    public static int coinReward(int id) {
        switch (id) {
            case ACH_FIRST_RACE: return 250;
            case ACH_FIRST_WIN: return 400;
            case ACH_DRAG_MASTER: return 1200;
            case ACH_DRIFTER: return 1500;
            case ACH_ESCAPE_ARTIST: return 1300;
            case ACH_RISK_LOVER: return 900;
            case ACH_GARAGE_OWNER: return 650;
            case ACH_COLLECTOR: return 2000;
            case ACH_DAILY_3: return 420;
            case ACH_WEEKLY_10: return 850;
            case ACH_LEVEL_5: return 900;
            case ACH_DISTANCE_10KM: return 1000;
            case ACH_CRATE_OPEN: return 300;
            default: return 150;
        }
    }

    public static String rewardText(int id) {
        return "+" + coinReward(id) + " coin / +" + xpReward(id) + " XP";
    }

    public static String status(SaveManager save, int id) {
        if (save == null) return "KAYIT YOK";
        if (save.isAchievementRewardClaimed(id)) return "ÖDÜL ALINDI";
        if (isCompleted(save, id)) return "TAMAMLANDI - ÖDÜL AL";
        return "DEVAM EDİYOR";
    }

    public static int completedCount(SaveManager save) {
        int count = 0;
        for (int i = 0; i < ACH_COUNT; i++) if (isCompleted(save, i)) count++;
        return count;
    }

    public static int unclaimedCount(SaveManager save) {
        int count = 0;
        for (int i = 0; i < ACH_COUNT; i++) if (isClaimable(save, i)) count++;
        return count;
    }

    public static int pendingCoinTotal(SaveManager save) {
        int total = 0;
        for (int i = 0; i < ACH_COUNT; i++) if (isClaimable(save, i)) total += coinReward(i);
        return total;
    }

    public static int pendingXpTotal(SaveManager save) {
        int total = 0;
        for (int i = 0; i < ACH_COUNT; i++) if (isClaimable(save, i)) total += xpReward(i);
        return total;
    }

    public static String line(SaveManager save, int id) {
        int p = progress(save, id);
        int t = target(id);
        boolean done = p >= t;
        boolean claimed = save != null && save.isAchievementRewardClaimed(id);
        String state = claimed ? "✓" : (done ? "AL" : " ");
        return "[" + state + "] " + label(id) + " — " + Math.min(p, t) + "/" + t + " | " + status(save, id) + " | " + rewardText(id);
    }

    public static String evaluateCompletedOnly(SaveManager save) {
        if (save == null) return "";
        for (int i = 0; i < ACH_COUNT; i++) {
            if (isClaimable(save, i)) return label(i);
        }
        return "";
    }

    public static String evaluateAndClaimAll(SaveManager save) {
        return evaluateCompletedOnly(save);
    }

    public static boolean claimAchievement(SaveManager save, int id) {
        if (!isClaimable(save, id)) return false;
        return save.claimAchievementReward(id, coinReward(id), xpReward(id), label(id));
    }

    public static boolean claimFirstCompletedAchievement(SaveManager save) {
        if (save == null) return false;
        for (int i = 0; i < ACH_COUNT; i++) {
            if (claimAchievement(save, i)) return true;
        }
        return false;
    }

    public static int claimAllCompletedAchievements(SaveManager save) {
        if (save == null) return 0;
        int claimed = 0;
        int totalCoin = 0;
        int totalXp = 0;
        for (int i = 0; i < ACH_COUNT; i++) {
            if (isClaimable(save, i) && save.claimAchievementReward(i, coinReward(i), xpReward(i), label(i))) {
                claimed++;
                totalCoin += coinReward(i);
                totalXp += xpReward(i);
            }
        }
        if (claimed > 0) {
            save.setEconomyLastMessage("TÜM BAŞARIM ÖDÜLLERİ ALINDI: +" + totalCoin + " coin +" + totalXp + " XP");
        }
        return claimed;
    }

    public static String summary(SaveManager save) {
        if (save == null) return "Başarım kaydı yok";
        return "Başarımlar: " + completedCount(save) + "/" + ACH_COUNT
                + " | Bekleyen ödül: " + unclaimedCount(save)
                + " | Alınan: " + save.getAchievementRewardedCount(ACH_COUNT)
                + " | Araç " + QuestPanelSystem.ownedVehicleCount(save) + "/" + VehicleCatalog.count()
                + " | Risk " + save.getTrafficNearMissTotal();
    }
}
