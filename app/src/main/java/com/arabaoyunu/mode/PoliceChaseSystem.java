package com.arabaoyunu.mode;

import com.arabaoyunu.util.SaveManager;

/**
 * A62.7: Polis kovalamaca ekonomisi, aranan seviye dengesi ve kariyer kilitleri.
 *
 * Bu sınıf bilinçli olarak Android bağımlılığı içermez. PoliceChaseMode yalnızca
 * sürüş/takip simülasyonunu yürütür; ödül, yıldız limiti, XP ve sonuç metinleri
 * burada tek yerden hesaplanır. Böylece polis modu menü, HUD, kariyer ve kayıt
 * sistemiyle aynı denge değerlerini kullanır.
 */
public final class PoliceChaseSystem {

    public static final int ACTIVE_MIN_WANTED = 1;
    public static final int ACTIVE_MAX_WANTED = 3;
    public static final int FUTURE_MAX_WANTED = 5;
    public static final int UNLOCK_LEVEL = 2;
    public static final float TARGET_ESCAPE_SECONDS = 90f;

    private static final int[] ESCAPE_COINS = new int[] {0, 400, 800, 1300, 2000, 3000};
    private static final int[] CAUGHT_COINS = new int[] {0, 50, 100, 150, 200, 300};
    private static final int[] ESCAPE_XP = new int[] {0, 60, 120, 190, 280, 400};
    private static final int[] CAUGHT_XP = new int[] {0, 20, 35, 50, 70, 95};

    private PoliceChaseSystem() {}

    public static boolean isUnlocked(SaveManager save) {
        if (save == null) return false;
        return save.getPlayerLevel() >= UNLOCK_LEVEL
                || save.getCheckpointRouteTotalCompletions() > 0
                || save.getDriftCompletedCount() > 0;
    }

    public static String lockText() {
        return "Polis LVL " + UNLOCK_LEVEL + " veya 1 mod tamamla";
    }

    /**
     * A62.7 ilk sürümde 1-3 yıldız gerçek çalışır. 4-5 yıldız değerleri ödül ve
     * kayıt altyapısı olarak hazır kalır, daha sonra gelişmiş AI ile açılabilir.
     */
    public static int maxActiveWanted(SaveManager save) {
        int level = save == null ? 1 : save.getPlayerLevel();
        if (level >= 9) return 3;
        if (level >= 6) return 2;
        return 1;
    }

    public static int computeWantedLevel(SaveManager save, float chaseTime, float speedKmh, float damage01) {
        int wanted = 1 + (int)(Math.max(0f, chaseTime) / 34f);
        if (speedKmh > 125f && chaseTime > 16f) wanted++;
        if (speedKmh > 165f && chaseTime > 28f) wanted++;
        if (damage01 > 0.35f) wanted++;
        return clamp(wanted, ACTIVE_MIN_WANTED, maxActiveWanted(save));
    }

    public static float escapeDistance(int wanted) {
        int w = clamp(wanted, 1, FUTURE_MAX_WANTED);
        // A65.7: güvenli mesafe biraz genişletildi; oyuncu gerçekten uzaklaşınca risk daha adil düşer.
        return 104f + w * 20f;
    }

    public static float escapeRequiredSeconds(int wanted) {
        int w = clamp(wanted, 1, FUTURE_MAX_WANTED);
        return 10.5f + w * 2.3f;
    }

    public static float caughtDistance(int wanted) {
        int w = clamp(wanted, 1, FUTURE_MAX_WANTED);
        // A65.7: kısa temas anında yakalanma olmasın diye temas çekirdeği küçültüldü.
        return Math.max(5.2f, 7.6f - w * 0.38f);
    }

    public static float caughtRequiredSeconds(int wanted) {
        int w = clamp(wanted, 1, FUTURE_MAX_WANTED);
        // A65.7: yakalanma barı yakınlık + süre ister; yüksek yıldızda baskı artar ama anlık temas cezalandırmaz.
        return Math.max(4.0f, 6.2f - w * 0.42f);
    }

    public static float policeSpeedBonus(int wanted) {
        int w = clamp(wanted, 1, FUTURE_MAX_WANTED);
        return 0.96f + (w - 1) * 0.13f;
    }

    public static int coinReward(boolean escaped, int wanted, float chaseTime) {
        int w = clamp(wanted, 1, FUTURE_MAX_WANTED);
        int base = escaped ? ESCAPE_COINS[w] : CAUGHT_COINS[w];
        if (!escaped) return base;
        return base + (int)Math.min(700f, Math.max(0f, chaseTime) * (7f + w * 1.5f));
    }

    public static int xpReward(boolean escaped, int wanted, float chaseTime) {
        int w = clamp(wanted, 1, FUTURE_MAX_WANTED);
        int base = escaped ? ESCAPE_XP[w] : CAUGHT_XP[w];
        if (!escaped) return base;
        return base + (int)Math.min(120f, Math.max(0f, chaseTime) * 1.2f);
    }

    public static String resultTitle(boolean escaped) {
        return escaped ? "KAÇIŞ BAŞARILI" : "YAKALANDIN";
    }

    public static String wantedStars(int wanted) {
        int w = clamp(wanted, 0, FUTURE_MAX_WANTED);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < FUTURE_MAX_WANTED; i++) sb.append(i < w ? '★' : '☆');
        return sb.toString();
    }

    public static float targetEscapeSeconds() {
        return TARGET_ESCAPE_SECONDS;
    }

    public static float capturePercent(float caughtTimer, int wanted) {
        float required = Math.max(0.1f, caughtRequiredSeconds(wanted));
        return Math.max(0f, Math.min(100f, (caughtTimer / required) * 100f));
    }

    public static String activeModeCardLine(SaveManager save) {
        return PoliceChaseResultSystem.modeHubSummary(save);
    }

    public static String rewardText(boolean escaped, int wanted, int coins, int xp) {
        return resultTitle(escaped) + " " + wantedStars(wanted) + " +" + coins + " coin +" + xp + " XP";
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
