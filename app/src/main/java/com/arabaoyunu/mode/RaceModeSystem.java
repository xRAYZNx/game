package com.arabaoyunu.mode;

/**
 * ArabaOyunu_62_0: Checkpoint yarışı derece/ödül kuralları.
 * Büyük harita olmadan Open Field üzerinde çalışan ilk net yarış döngüsünü
 * RaceMode'dan ayrı ve test edilebilir sabit kurallara ayırır.
 */
public final class RaceModeSystem {

    public static final String GRADE_GOLD = "ALTIN";
    public static final String GRADE_SILVER = "GUMUS";
    public static final String GRADE_BRONZE = "BRONZ";
    public static final String GRADE_FINISH = "TAMAM";

    public static final float GOLD_SECONDS = 90f;
    public static final float SILVER_SECONDS = 115f;
    public static final float BRONZE_SECONDS = 150f;

    private RaceModeSystem() {}

    public static String gradeForTime(float seconds) {
        if (seconds <= GOLD_SECONDS) return GRADE_GOLD;
        if (seconds <= SILVER_SECONDS) return GRADE_SILVER;
        if (seconds <= BRONZE_SECONDS) return GRADE_BRONZE;
        return GRADE_FINISH;
    }

    public static int rewardForGrade(String grade) {
        if (GRADE_GOLD.equals(grade)) return 1200;
        if (GRADE_SILVER.equals(grade)) return 850;
        if (GRADE_BRONZE.equals(grade)) return 550;
        return 300;
    }

    public static int xpForGrade(String grade) {
        if (GRADE_GOLD.equals(grade)) return 220;
        if (GRADE_SILVER.equals(grade)) return 160;
        if (GRADE_BRONZE.equals(grade)) return 105;
        return 65;
    }

    public static int positionForGrade(String grade) {
        if (GRADE_GOLD.equals(grade)) return 1;
        if (GRADE_SILVER.equals(grade)) return 2;
        if (GRADE_BRONZE.equals(grade)) return 3;
        return 4;
    }

    public static String targetText() {
        return formatTime(GOLD_SECONDS);
    }

    public static String formatTime(float seconds) {
        if (seconds < 0f || seconds != seconds) seconds = 0f;
        int total = Math.max(0, (int) seconds);
        int minutes = total / 60;
        int sec = total % 60;
        int tenth = Math.max(0, Math.min(9, (int)((seconds - total) * 10f)));
        return String.format(java.util.Locale.US, "%02d:%02d.%d", minutes, sec, tenth);
    }
}
