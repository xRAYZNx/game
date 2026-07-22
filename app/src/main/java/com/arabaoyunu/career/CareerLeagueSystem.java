package com.arabaoyunu.career;

import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.mode.PoliceChaseSystem;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * A65.9: Kariyer ligi, seviye kilidi ve ilerleme metinleri.
 * Sistem tamamen kayıt tabanlı çalışır; harita/GLB gerektirmez.
 */
public final class CareerLeagueSystem {

    public static final int SCHEMA_VERSION = 660;

    public static final int LEAGUE_STARTER = 0;
    public static final int LEAGUE_STREET = 1;
    public static final int LEAGUE_PRO = 2;
    public static final int LEAGUE_SUPER = 3;
    public static final int LEAGUE_ELITE = 4;
    public static final int LEAGUE_COUNT = 5;

    private static final String[] NAMES = new String[] {
            "Bronz Lig",
            "Gümüş Lig",
            "Altın Lig",
            "Pro Lig",
            "Elite Lig"
    };

    private static final int[] REQUIRED_LEVELS = new int[] {1, 3, 6, 10, 15};

    private CareerLeagueSystem() {}

    public static String leagueName(int league) {
        return NAMES[clamp(league, 0, LEAGUE_COUNT - 1)];
    }

    public static int requiredLevel(int league) {
        return REQUIRED_LEVELS[clamp(league, 0, LEAGUE_COUNT - 1)];
    }

    public static int leagueForLevel(int level) {
        int safe = Math.max(1, level);
        int result = 0;
        for (int i = 0; i < REQUIRED_LEVELS.length; i++) {
            if (safe >= REQUIRED_LEVELS[i]) result = i;
        }
        return result;
    }

    public static int nextLeagueLevel(int level) {
        int current = leagueForLevel(level);
        if (current >= LEAGUE_COUNT - 1) return 0;
        return REQUIRED_LEVELS[current + 1];
    }

    public static String nextLeagueText(SaveManager save) {
        if (save == null) return "-";
        int next = nextLeagueLevel(save.getPlayerLevel());
        if (next <= 0) return "Tum ligler acik";
        return "Sonraki lig LVL " + next;
    }

    public static boolean isLeagueUnlocked(SaveManager save, int league) {
        return save != null && save.getPlayerLevel() >= requiredLevel(league);
    }

    public static int leagueForMode(int mode) {
        if (mode == GameScreenState.MODE_DRAG_RACE) return LEAGUE_STREET;
        if (mode == GameScreenState.MODE_TIME_TRIAL) return LEAGUE_STREET;
        if (mode == GameScreenState.MODE_DRIFT) return LEAGUE_STARTER;
        if (mode == GameScreenState.MODE_POLICE_CHASE) return LEAGUE_STARTER;
        if (mode == GameScreenState.MODE_RACE_LOCKED) return LEAGUE_STARTER;
        return LEAGUE_STARTER;
    }

    public static boolean isModeUnlocked(SaveManager save, int mode) {
        if (mode == GameScreenState.MODE_DRIFT) {
            return save != null && save.getPlayerLevel() >= 2;
        }
        if (mode == GameScreenState.MODE_POLICE_CHASE) {
            return PoliceChaseSystem.isUnlocked(save);
        }
        return isLeagueUnlocked(save, leagueForMode(mode));
    }

    public static String modeLockText(int mode) {
        if (mode == GameScreenState.MODE_DRIFT) return "Drift LVL 2";
        if (mode == GameScreenState.MODE_POLICE_CHASE) return PoliceChaseSystem.lockText();
        int league = leagueForMode(mode);
        return leagueName(league) + " LVL " + requiredLevel(league);
    }

    public static int vehicleRequiredLevel(int vehicleIndex) {
        float p = VehicleCatalog.performanceClass(vehicleIndex);
        int price = VehicleCatalog.price(vehicleIndex);
        if (price <= 0 || vehicleIndex == 0) return 1;
        if (p >= 1.55f) return 15;
        if (p >= 1.45f) return 10;
        if (p >= 1.30f) return 6;
        if (p >= 1.15f) return 3;
        return 2;
    }

    public static boolean isVehicleLevelUnlocked(SaveManager save, int vehicleIndex) {
        return save != null && save.getPlayerLevel() >= vehicleRequiredLevel(vehicleIndex);
    }

    public static int levelRewardCoins(int newLevel) {
        return 550 + Math.max(1, newLevel) * 110;
    }

    public static String levelRewardText(int level) {
        return levelRewardCoins(level) + " coin" + (level % 3 == 0 ? " + lig/kilit ilerlemesi" : "");
    }

    public static String leagueProgressText(SaveManager save) {
        if (save == null) return "-";
        int level = save.getPlayerLevel();
        int league = leagueForLevel(level);
        int next = nextLeagueLevel(level);
        if (next <= 0) return leagueName(league) + " tamam";
        int need = Math.max(0, next - level);
        return leagueName(league) + " | sonraki lige " + need + " seviye";
    }

    public static int xpForNextLevel(int level) {
        int safe = Math.max(1, level);
        return 420 + safe * 180 + Math.max(0, safe - 1) * Math.max(0, safe - 1) * 18;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
