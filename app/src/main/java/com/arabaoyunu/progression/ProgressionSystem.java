package com.arabaoyunu.progression;

import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * ArabaOyunu_40: Oyuncu ilerleme, seviye, XP, günlük/haftalık görev,
 * başarımlar, ödül kasası ve kilit açma sistemi.
 */
public final class ProgressionSystem {

    private final SaveManager saveManager;
    private final RewardCrateSystem rewardCrateSystem;

    private float drivenDistanceAccumulator;
    private String message = "";
    private float messageTimer;
    private int lastLevel;
    private int lastUnlockedVehicleCount;
    private int lastUnlockedMapTier;

    public ProgressionSystem(SaveManager saveManager) {
        this.saveManager = saveManager;
        this.rewardCrateSystem = new RewardCrateSystem(saveManager);
        if (saveManager != null) {
            lastLevel = saveManager.getPlayerLevel();
            lastUnlockedVehicleCount = getUnlockedVehicleCount();
            lastUnlockedMapTier = getUnlockedMapTier();
        }
    }

    public void update(float dt, float speedKmh, String modeName, String mapName) {
        if (saveManager == null || dt <= 0f) return;
        if (dt > 0.15f) dt = 0.15f;

        // Sürüş mesafesi: km/h -> m/s
        float meters = Math.max(0f, speedKmh) / 3.6f * dt;
        if (meters > 0.01f) {
            drivenDistanceAccumulator += meters;
            if (drivenDistanceAccumulator >= 25f) {
                int add = (int)(drivenDistanceAccumulator);
                drivenDistanceAccumulator -= add;
                saveManager.addDrivenMeters(add);
                if (add >= 25) saveManager.addXp(Math.max(1, add / 20));
            }
        }

        checkLevelUps();
        checkUnlocks();
        checkAchievements();

        if (messageTimer > 0f) {
            messageTimer -= dt;
            if (messageTimer <= 0f) message = "";
        }
    }

    public void onMissionReward(int coins) {
        if (saveManager == null || coins <= 0) return;
        int xp = Math.max(60, coins / 5);
        saveManager.addXp(xp);
        saveManager.incrementProgressDaily();
        saveManager.incrementProgressWeekly();
        maybeAddCrate("Görev ödülü", coins >= 650);
        setMessage("GÖREV: +" + xp + " XP");
        unlockAchievement(AchievementSystem.ACH_FIRST_MISSION);
    }

    public void onRaceFinished(int coins, int position) {
        if (saveManager == null) return;
        int xp = 120 + Math.max(0, coins / 6);
        if (position == 1) xp += 120;
        saveManager.addXp(xp);
        saveManager.incrementProgressWeekly();
        if (position == 1) {
            maybeAddCrate("Yarış birinciliği", true);
            unlockAchievement(AchievementSystem.ACH_RACE_WIN);
        }
        setMessage("YARIŞ: +" + xp + " XP");
    }

    public void onPoliceFinished(boolean escaped, int coins) {
        onPoliceFinished(escaped, coins, 1, 0);
    }

    public void onPoliceFinished(boolean escaped, int coins, int wantedLevel, int explicitXp) {
        if (saveManager == null) return;
        int xp = explicitXp > 0 ? explicitXp : (escaped ? 240 : 90) + Math.max(0, coins / 8);
        saveManager.addXp(xp);
        saveManager.incrementProgressWeekly();
        if (escaped) {
            maybeAddCrate(wantedLevel >= 3 ? "3 yıldız polis kaçışı" : "Polisten kaçış", true);
            unlockAchievement(AchievementSystem.ACH_POLICE_ESCAPE);
        }
        setMessage((escaped ? "KAÇIŞ" : "POLİS") + ": +" + xp + " XP");
    }

    public void tryOpenCrate() {
        if (rewardCrateSystem.openCrate(getLevel())) {
            unlockAchievement(AchievementSystem.ACH_CRATE_OPEN);
            setMessage(rewardCrateSystem.getLastMessage());
        }
    }

    private void maybeAddCrate(String reason, boolean strongReward) {
        if (saveManager == null) return;
        int weekly = saveManager.getProgressWeeklyCount();
        int daily = saveManager.getProgressDailyCount();
        boolean shouldGive = strongReward || daily == 3 || weekly == 10 || weekly == 20;
        if (!shouldGive) return;
        saveManager.setRewardCrates(saveManager.getRewardCrates() + 1);
        setMessage(reason + ": ödül kasası +1");
    }

    private void checkLevelUps() {
        if (saveManager == null) return;
        int level = saveManager.getPlayerLevel();
        while (saveManager.getPlayerXp() >= xpForNextLevel(level)) {
            saveManager.setPlayerXp(saveManager.getPlayerXp() - xpForNextLevel(level));
            level++;
            saveManager.setPlayerLevel(level);
            saveManager.setRewardCrates(saveManager.getRewardCrates() + 1);
            saveManager.addCoins(700 + level * 90);
            setMessage("SEVİYE " + level + "! Kasa +1");
        }
        if (level != lastLevel) {
            lastLevel = level;
        }
    }

    private void checkUnlocks() {
        if (saveManager == null) return;
        int unlockedVehicles = getUnlockedVehicleCount();
        if (unlockedVehicles > lastUnlockedVehicleCount) {
            lastUnlockedVehicleCount = unlockedVehicles;
            setMessage("Yeni araç kilidi açıldı!");
        }

        int mapTier = getUnlockedMapTier();
        if (mapTier > lastUnlockedMapTier) {
            lastUnlockedMapTier = mapTier;
            setMessage("Yeni harita bölgesi açıldı!");
        }
    }

    private void checkAchievements() {
        if (saveManager == null) return;
        if (saveManager.getProgressDailyCount() >= 3) unlockAchievement(AchievementSystem.ACH_DAILY_3);
        if (saveManager.getProgressWeeklyCount() >= 10) unlockAchievement(AchievementSystem.ACH_WEEKLY_10);
        if (saveManager.getPlayerLevel() >= 5) unlockAchievement(AchievementSystem.ACH_LEVEL_5);
        if (saveManager.getDrivenMeters() >= 10000) unlockAchievement(AchievementSystem.ACH_DISTANCE_10KM);
    }

    private void unlockAchievement(int id) {
        if (saveManager == null || saveManager.isAchievementUnlocked(id)) return;
        saveManager.setAchievementUnlocked(id, true);
        // A63.7: Eski ilerleme sistemi başarımları yalnızca tamamlandı olarak işaretler.
        // Coin/XP ödülü artık otomatik verilmez; oyuncu ödülü A63.7 görev panelinde manuel alır.
        if (id >= 0 && id < AchievementSystem.ACH_COUNT) {
            setMessage("BAŞARIM TAMAMLANDI: " + AchievementSystem.label(id) + " | Ödül panelinden al");
        } else {
            setMessage("BAŞARIM TAMAMLANDI: " + AchievementSystem.label(id));
        }
    }

    public int xpForNextLevel(int level) {
        return 420 + Math.max(1, level) * 180 + Math.max(0, level - 1) * Math.max(0, level - 1) * 18;
    }

    public int getLevel() { return saveManager == null ? 1 : saveManager.getPlayerLevel(); }
    public int getXp() { return saveManager == null ? 0 : saveManager.getPlayerXp(); }
    public int getNextXp() { return xpForNextLevel(getLevel()); }
    public int getDailyCount() { return saveManager == null ? 0 : saveManager.getProgressDailyCount(); }
    public int getWeeklyCount() { return saveManager == null ? 0 : saveManager.getProgressWeeklyCount(); }
    public int getCrates() { return saveManager == null ? 0 : saveManager.getRewardCrates(); }
    public int getAchievementCount() { return saveManager == null ? 0 : saveManager.getAchievementUnlockedCount(AchievementSystem.ACH_COUNT); }
    public int getUnlockedVehicleCount() {
        if (saveManager == null) return 1;
        int level = saveManager.getPlayerLevel();
        int count = 1;
        if (level >= 2) count++;
        if (level >= 3) count++;
        if (level >= 5) count++;
        if (level >= 7) count++;
        if (level >= 9) count++;
        if (level >= 11) count++;
        if (level >= 13) count++;
        return Math.min(VehicleCatalog.count(), count);
    }

    public int getUnlockedMapTier() {
        if (saveManager == null) return 1;
        int level = saveManager.getPlayerLevel();
        if (level >= 8) return 4;
        if (level >= 5) return 3;
        if (level >= 3) return 2;
        return 1;
    }

    public String getUnlockText() {
        return "ARAÇ " + getUnlockedVehicleCount() + "/" + VehicleCatalog.count() + "  HARİTA " + getUnlockedMapTier() + "/4";
    }

    public String getMessage() { return message == null ? "" : message; }

    private void setMessage(String text) {
        message = text == null ? "" : text;
        messageTimer = 4.0f;
    }
}
