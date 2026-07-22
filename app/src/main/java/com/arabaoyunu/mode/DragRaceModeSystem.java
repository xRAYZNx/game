package com.arabaoyunu.mode;

/**
 * ArabaOyunu_62_1: 400m Drag Yarışı derece/ödül kuralları.
 * Checkpoint modundan ayrı tutulur; garaj/modifiye etkisini en net gösteren
 * kısa, düz hızlanma yarışının süre hedefleri ve kayıt metinlerini yönetir.
 */
public final class DragRaceModeSystem {

    public static final String GRADE_GOLD = "ALTIN";
    public static final String GRADE_SILVER = "GUMUS";
    public static final String GRADE_BRONZE = "BRONZ";
    public static final String GRADE_FINISH = "TAMAM";

    public static final float DRAG_DISTANCE_METERS = 400f;
    public static final float GOLD_SECONDS = 13.5f;
    public static final float SILVER_SECONDS = 16.5f;
    public static final float BRONZE_SECONDS = 21.0f;

    private DragRaceModeSystem() {}

    public static String gradeForTime(float seconds) {
        if (seconds <= GOLD_SECONDS) return GRADE_GOLD;
        if (seconds <= SILVER_SECONDS) return GRADE_SILVER;
        if (seconds <= BRONZE_SECONDS) return GRADE_BRONZE;
        return GRADE_FINISH;
    }

    public static int rewardForGrade(String grade) {
        if (GRADE_GOLD.equals(grade)) return 900;
        if (GRADE_SILVER.equals(grade)) return 650;
        if (GRADE_BRONZE.equals(grade)) return 400;
        return 250;
    }

    public static int xpForGrade(String grade) {
        if (GRADE_GOLD.equals(grade)) return 160;
        if (GRADE_SILVER.equals(grade)) return 115;
        if (GRADE_BRONZE.equals(grade)) return 80;
        return 45;
    }

    public static String targetText() {
        return formatTime(GOLD_SECONDS);
    }

    public static String formatTime(float seconds) {
        if (seconds < 0f || seconds != seconds) seconds = 0f;
        int total = Math.max(0, (int) seconds);
        int minutes = total / 60;
        int sec = total % 60;
        int hundred = Math.max(0, Math.min(99, (int)((seconds - total) * 100f)));
        return String.format(java.util.Locale.US, "%02d:%02d.%02d", minutes, sec, hundred);
    }
}
