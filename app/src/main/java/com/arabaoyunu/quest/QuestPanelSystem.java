package com.arabaoyunu.quest;

import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * ArabaOyunu_45: Ana menü profesyonel görev paneli bilgi sağlayıcısı.
 * Bu sınıf sadece kayıtlı oyun durumunu okur ve panele güvenli özet üretir.
 */
public final class QuestPanelSystem {

    public static final int TAB_ACTIVE = 0;
    public static final int TAB_DAILY = 1;
    public static final int TAB_WEEKLY = 2;
    public static final int TAB_COMPLETED = 3;
    public static final int TAB_REWARDS = 4;
    public static final int TAB_UPCOMING = 5;
    public static final int TAB_LOCKED = 6;
    public static final int TAB_COUNT = 7;

    private QuestPanelSystem() {}

    public static String tabLabel(int tab) {
        switch (tab) {
            case TAB_ACTIVE: return "AKTİF";
            case TAB_DAILY: return "GÜNLÜK";
            case TAB_WEEKLY: return "HAFTALIK";
            case TAB_COMPLETED: return "TAMAM";
            case TAB_REWARDS: return "ÖDÜL";
            case TAB_UPCOMING: return "YAKINDA";
            case TAB_LOCKED: return "KİLİTLİ";
            default: return "GÖREV";
        }
    }

    public static String questTitle(int step) {
        switch (clampStep(step)) {
            case 0: return "Başlangıç Görevi";
            case 1: return "İlk Drift Görevi";
            case 2: return "İlk Yarış Görevi";
            case 3: return "İlk Polis Kaçışı";
            case 4: return "İlk Araç Yükseltme";
            case 5: return "Yeni Araç Kilidi";
            case 6: return "Yeni Bölge Açma";
            default: return "Kariyer Zinciri Tamamlandı";
        }
    }

    public static String questObjective(int step) {
        switch (clampStep(step)) {
            case 0: return "Açık dünyada sürüş yap ve kariyeri başlat.";
            case 1: return "Drift noktasına git, ETK ile drift modunu başlat.";
            case 2: return "Yarış noktasına git, ETK ile ilk yarışı başlat.";
            case 3: return "Polis kaçış noktasına git, kovalamacayı başlat.";
            case 4: return "Garajda ilk motor/fren/lastik/drift yükseltmesini yap.";
            case 5: return "Seviye 2’ye ulaş veya ikinci aracı satın al.";
            case 6: return "Seviye 3’e ulaş ve yeni bölge erişimini aç.";
            default: return "Yeni görev zinciri için hazır.";
        }
    }

    public static String questReward(int step) {
        switch (clampStep(step)) {
            case 0: return "+120 XP +250 coin";
            case 1: return "+180 XP +350 coin + renk şansı";
            case 2: return "+220 XP +500 coin + jant şansı";
            case 3: return "+260 XP +650 coin +1 kasa";
            case 4: return "+260 XP +500 coin + parça tier";
            case 5: return "+300 XP +750 coin + araç kilidi";
            case 6: return "+400 XP +1000 coin +1 kasa + bölge";
            default: return "Tamamlandı";
        }
    }

    public static int questRequiredLevel(int step) {
        switch (clampStep(step)) {
            case 3: return 2;
            case 5: return 2;
            case 6: return 3;
            default: return 1;
        }
    }

    public static boolean isQuestLocked(SaveManager saveManager, int step) {
        if (saveManager == null) return true;
        int current = saveManager.getQuestChainStep();
        if (step <= current) return false;
        return saveManager.getPlayerLevel() < questRequiredLevel(step);
    }

    public static String dailySummary(SaveManager saveManager) {
        if (saveManager == null) return "Günlük kayıt yok";
        return "Günlük ilerleme: " + saveManager.getProgressDailyCount() + "/3  |  Ödül: XP + kasa şansı";
    }

    public static String weeklySummary(SaveManager saveManager) {
        if (saveManager == null) return "Haftalık kayıt yok";
        return "Haftalık ilerleme: " + saveManager.getProgressWeeklyCount() + "/10  |  Ödül: 850 coin + kasa";
    }

    public static String rewardsSummary(SaveManager saveManager) {
        if (saveManager == null) return "Ödül kaydı yok";
        return "Coin " + saveManager.getCoins()
                + " | LVL " + saveManager.getPlayerLevel()
                + " | XP " + saveManager.getPlayerXp()
                + " | Kasa " + saveManager.getRewardCrates();
    }

    public static String unlockSummary(SaveManager saveManager) {
        if (saveManager == null) return "Kilit açma kaydı yok";
        return "Renk " + saveManager.getUnlockedPaintCount() + "/6"
                + " | Jant " + saveManager.getUnlockedRimCount() + "/5"
                + " | Parça Tier " + saveManager.getUnlockedPartTier()
                + " | Araç " + ownedVehicleCount(saveManager) + "/" + VehicleCatalog.count();
    }

    public static String mapUnlockSummary(SaveManager saveManager) {
        if (saveManager == null) return "Harita kaydı yok";
        String city = saveManager.isMapUnlockedByCareer(GameScreenState.MAP_CITY) ? "Şehir açık" : "Şehir LVL 3";
        String highway = saveManager.isMapUnlockedByCareer(GameScreenState.MAP_HIGHWAY) ? "Otoyol açık" : "Otoyol LVL 5";
        String drift = saveManager.isMapUnlockedByCareer(GameScreenState.MAP_DRIFT_PARK) ? "Drift Park açık" : "Drift Park LVL 8";
        return city + " | " + highway + " | " + drift;
    }

    public static int ownedVehicleCount(SaveManager saveManager) {
        if (saveManager == null) return 0;
        int count = 0;
        for (int i = 0; i < VehicleCatalog.count(); i++) {
            if (saveManager.isVehicleOwned(VehicleCatalog.id(i))) count++;
        }
        return count;
    }

    public static int clampStep(int step) {
        if (step < 0) return 0;
        if (step > 7) return 7;
        return step;
    }
}
