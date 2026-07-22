package com.arabaoyunu.mode;

/**
 * A65.0: Checkpoint / Time Trial artık tek sabit rota değil; Open Field üzerinde
 * birden fazla rota, rota bazlı madalya hedefleri, ödül dengesi ve kalıcı ilerleme
 * için ortak kural kaynağıdır.
 *
 * Büyük harita veya dış GLB gerektirmez; açık dünya kapalı kalırken güvenli test
 * alanı üzerinde çalışır.
 */
public final class CheckpointRaceSystem {

    public static final float COUNTDOWN_SECONDS = 3.25f;
    public static final float CHECKPOINT_RADIUS = 16f;
    public static final float ACTIVE_CHECKPOINT_RADIUS = 18f;

    public static final int ROUTE_SHORT = 0;
    public static final int ROUTE_MEDIUM = 1;
    public static final int ROUTE_LONG = 2;
    public static final int ROUTE_TECHNICAL = 3;
    public static final int ROUTE_COUNT = 4;

    // Eski kod/HUD uyumluluğu: aktif rota üzerinden okunur.
    public static final float GOLD_TARGET_SECONDS = 58f;
    public static final float SILVER_TARGET_SECONDS = 74f;
    public static final float BRONZE_TARGET_SECONDS = 94f;
    public static final int CHECKPOINT_COUNT = 10;

    // 0. nokta başlangıçtır; oyuncu 1..N checkpointlerini sırayla geçer.
    private static final float[][] ROUTE_X = new float[][] {
            // Kısa rota: başlangıçtan sonra 5 hedef; yeni oyuncu için güvenli S dönüşü.
            { 0f, 0f, 48f, 96f, 68f, -18f, 0f },
            // Orta rota: 8 hedef; hızlanma + iki yön değişimi ile ana dengeli rota.
            { 0f, 0f, 54f, 112f, 128f, 58f, -34f, -104f, -62f, 0f },
            // Uzun rota: 11 hedef; uzun düzlüğe ek olarak Open Field sınırlarında geniş yay.
            { 0f, 0f, 48f, 112f, 160f, 138f, 64f, -28f, -124f, -166f, -112f, -30f, 0f },
            // Teknik rota: 10 hedef; checkpointler daha kısa ama dönüş açısı daha yoğun.
            { 0f, 30f, 88f, 36f, -40f, -96f, -46f, 34f, 104f, 28f, -54f, 0f }
    };

    private static final float[][] ROUTE_Z = new float[][] {
            { -132f, -46f, 18f, 86f, 146f, 78f, -132f },
            { -132f, -46f, 22f, 86f, 148f, 182f, 142f, 72f, -18f, -132f },
            { -132f, -40f, 28f, 98f, 164f, 226f, 242f, 202f, 134f, 46f, -40f, -112f, -132f },
            { -132f, -62f, -10f, 48f, 92f, 36f, -34f, -86f, -24f, 46f, 100f, -132f }
    };

    // A65.1: Hedef süreler Open Field rotalarının uzunluk + dönüş yoğunluğu için yeniden dengelendi.
    private static final float[] GOLD_SECONDS = new float[] { 64f, 96f, 142f, 118f };
    private static final float[] SILVER_SECONDS = new float[] { 84f, 124f, 178f, 150f };
    private static final float[] BRONZE_SECONDS = new float[] { 108f, 158f, 222f, 190f };
    private static final int[] BASE_REWARD = new int[] { 440, 700, 1060, 920 };
    private static final int[] BASE_XP = new int[] { 78, 122, 178, 156 };

    private static int activeRouteId = ROUTE_MEDIUM;

    private CheckpointRaceSystem() {}

    public static int sanitizeRouteId(int routeId) {
        if (routeId < 0) return ROUTE_SHORT;
        if (routeId >= ROUTE_COUNT) return ROUTE_COUNT - 1;
        return routeId;
    }

    public static void setActiveRoute(int routeId) {
        activeRouteId = sanitizeRouteId(routeId);
    }

    public static int getActiveRouteId() {
        return activeRouteId;
    }

    public static int checkpointCount(int routeId) {
        return ROUTE_X[sanitizeRouteId(routeId)].length;
    }

    public static float targetX(int index) {
        return targetX(activeRouteId, index);
    }

    public static float targetZ(int index) {
        return targetZ(activeRouteId, index);
    }

    public static float targetX(int routeId, int index) {
        int route = sanitizeRouteId(routeId);
        return ROUTE_X[route][clamp(index, 0, ROUTE_X[route].length - 1)];
    }

    public static float targetZ(int routeId, int index) {
        int route = sanitizeRouteId(routeId);
        return ROUTE_Z[route][clamp(index, 0, ROUTE_Z[route].length - 1)];
    }

    public static float distanceToTarget(float carX, float carZ, int index) {
        float dx = targetX(index) - carX;
        float dz = targetZ(index) - carZ;
        return (float)Math.sqrt(dx * dx + dz * dz);
    }

    public static String routeLabel() {
        return routeLabel(activeRouteId);
    }

    public static String routeLabel(int routeId) {
        switch (sanitizeRouteId(routeId)) {
            case ROUTE_SHORT: return "Kısa Rota";
            case ROUTE_LONG: return "Uzun Rota";
            case ROUTE_TECHNICAL: return "Teknik Rota";
            case ROUTE_MEDIUM:
            default: return "Orta Rota";
        }
    }

    public static String routeDifficulty(int routeId) {
        switch (sanitizeRouteId(routeId)) {
            case ROUTE_SHORT: return "Kolay";
            case ROUTE_LONG: return "Zor";
            case ROUTE_TECHNICAL: return "Teknik";
            case ROUTE_MEDIUM:
            default: return "Normal";
        }
    }

    public static String routeDescription(int routeId) {
        switch (sanitizeRouteId(routeId)) {
            case ROUTE_SHORT: return "5 hedef • kolay S rota • hızlı tekrar";
            case ROUTE_LONG: return "12 hedef • uzun yay • yüksek ödül";
            case ROUTE_TECHNICAL: return "11 hedef • keskin dönüş • beceri";
            case ROUTE_MEDIUM:
            default: return "9 hedef • dengeli rota • ana yarış";
        }
    }

    public static String targetLabel(int index) {
        return targetLabel(activeRouteId, index);
    }

    public static String targetLabel(int routeId, int index) {
        int count = checkpointCount(routeId);
        int safe = clamp(index, 0, count - 1);
        if (safe == 0) return "Başlangıç";
        if (safe == count - 1) return "Bitiş";
        return "Checkpoint " + safe;
    }

    public static float goldSeconds(int routeId) { return GOLD_SECONDS[sanitizeRouteId(routeId)]; }
    public static float silverSeconds(int routeId) { return SILVER_SECONDS[sanitizeRouteId(routeId)]; }
    public static float bronzeSeconds(int routeId) { return BRONZE_SECONDS[sanitizeRouteId(routeId)]; }

    public static String medalForTime(int routeId, float seconds) {
        if (seconds <= goldSeconds(routeId)) return RaceModeSystem.GRADE_GOLD;
        if (seconds <= silverSeconds(routeId)) return RaceModeSystem.GRADE_SILVER;
        if (seconds <= bronzeSeconds(routeId)) return RaceModeSystem.GRADE_BRONZE;
        return RaceModeSystem.GRADE_FINISH;
    }

    public static int medalRank(String medal) {
        if (RaceModeSystem.GRADE_GOLD.equals(medal)) return 4;
        if (RaceModeSystem.GRADE_SILVER.equals(medal)) return 3;
        if (RaceModeSystem.GRADE_BRONZE.equals(medal)) return 2;
        if (RaceModeSystem.GRADE_FINISH.equals(medal)) return 1;
        return 0;
    }

    public static int rewardCoins(int routeId, String medal, boolean newBest, boolean newMedal, int previousCompletions) {
        int route = sanitizeRouteId(routeId);
        int reward = BASE_REWARD[route];
        int rank = medalRank(medal);
        if (rank >= 4) reward += 420;
        else if (rank == 3) reward += 260;
        else if (rank == 2) reward += 130;
        if (newBest) reward += 180 + route * 55;
        if (newMedal) reward += 240 + route * 70;
        // A65.1 ödül güvenliği: aynı rota sınırsız farm olmasın; rekor/madalya yoksa tekrar ödülü kademeli azalır.
        if (!newBest && !newMedal) {
            if (previousCompletions <= 0) {
                // İlk bitiriş tam ödül.
            } else if (previousCompletions <= 2) {
                reward = Math.max(210, (int)(reward * 0.72f));
            } else if (previousCompletions <= 7) {
                reward = Math.max(170, (int)(reward * 0.48f));
            } else {
                reward = Math.max(130, (int)(reward * 0.34f));
            }
        }
        return Math.max(120, reward);
    }

    public static int rewardXp(int routeId, String medal, boolean newBest, boolean newMedal) {
        int xp = BASE_XP[sanitizeRouteId(routeId)];
        int rank = medalRank(medal);
        if (rank >= 4) xp += 75;
        else if (rank == 3) xp += 48;
        else if (rank == 2) xp += 28;
        if (newBest) xp += 35;
        if (newMedal) xp += 45;
        return Math.max(45, xp);
    }

    public static String medalTargetsText() {
        return medalTargetsText(activeRouteId);
    }

    public static String medalTargetsText(int routeId) {
        return "Altın " + RaceModeSystem.formatTime(goldSeconds(routeId))
                + " • Gümüş " + RaceModeSystem.formatTime(silverSeconds(routeId))
                + " • Bronz " + RaceModeSystem.formatTime(bronzeSeconds(routeId));
    }

    public static String routeBalanceText(int routeId) {
        int route = sanitizeRouteId(routeId);
        return routeDifficulty(route) + " • " + Math.max(1, checkpointCount(route) - 1)
                + " hedef • " + medalTargetsText(route);
    }

    public static String repeatRewardRuleText(int previousCompletions, boolean newBest, boolean newMedal) {
        if (newBest && newMedal) return "Rekor + madalya bonusu aktif";
        if (newBest) return "Yeni rekor bonusu aktif";
        if (newMedal) return "Yeni madalya bonusu aktif";
        if (previousCompletions <= 0) return "İlk bitiriş tam ödül";
        if (previousCompletions <= 2) return "Tekrar ödülü %72";
        if (previousCompletions <= 7) return "Tekrar ödülü %48";
        return "Tekrar ödülü güvenli minimum";
    }

    public static String routeQaSummary() {
        return "4 rota dengelendi: kısa güvenli S, orta ana halka, uzun geniş yay, teknik keskin dönüş.";
    }

    public static String ruleText() {
        return "Rota seç, 3-2-1 sonrası checkpointleri sırayla geç, süre + madalya + ödül al.";
    }

    public static int safeTargetIndex(int index) {
        return clamp(index, 0, checkpointCount(activeRouteId) - 1);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
