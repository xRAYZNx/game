package com.arabaoyunu.economy;

import com.arabaoyunu.util.SaveManager;

/**
 * ArabaOyunu_61_9: 7 gunluk giris odulu sistemi.
 * Para odulleri garaj/modifiye ekonomisine direkt baglanir; nitro ve indirim
 * tokenlari sonraki surumlerde daha derin sistemlere baglanmak icin kaydedilir.
 */
public final class DailyRewardSystem {

    private static final int[] COINS = new int[] {250, 500, 0, 750, 0, 1000, 2500};
    private static final int[] NITRO_PACKS = new int[] {0, 0, 1, 0, 0, 0, 1};
    private static final int[] DISCOUNT_TOKENS = new int[] {0, 0, 0, 0, 1, 0, 1};

    private final SaveManager saveManager;
    private String lastMessage = "";

    public DailyRewardSystem(SaveManager saveManager) {
        this.saveManager = saveManager;
    }

    public boolean isAvailable() {
        return saveManager != null && saveManager.isDailyRewardAvailable();
    }

    public int getDayNumber() {
        return getDayIndex() + 1;
    }

    public int getDayIndex() {
        return saveManager == null ? 0 : clamp(saveManager.getDailyRewardDayIndex(), 0, 6);
    }

    public int getCoins() {
        return COINS[getDayIndex()];
    }

    public int getNitroPacks() {
        return NITRO_PACKS[getDayIndex()];
    }

    public int getDiscountTokens() {
        return DISCOUNT_TOKENS[getDayIndex()];
    }

    public String getRewardText() {
        int day = getDayIndex();
        String text;
        if (COINS[day] > 0) text = COINS[day] + " coin";
        else if (NITRO_PACKS[day] > 0) text = NITRO_PACKS[day] + " N2O paketi";
        else text = DISCOUNT_TOKENS[day] + " garaj indirim kuponu";
        if (day == 6) text = "2500 coin + N2O + indirim";
        return text;
    }

    public String getStatusText() {
        if (isAvailable()) return "Bugunku odul hazir: Gun " + getDayNumber() + " / 7";
        return "Bugunun odulu alindi. Yarin tekrar gel.";
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public boolean claim() {
        if (saveManager == null) return false;
        int day = getDayIndex();
        boolean ok = saveManager.claimDailyReward(day, COINS[day], NITRO_PACKS[day], DISCOUNT_TOKENS[day]);
        if (ok) {
            saveManager.incrementProgressDaily();
            saveManager.incrementProgressWeekly();
            saveManager.addXp(25);
            lastMessage = "Gunluk odul alindi: " + getRewardText() + " +25 XP";
            saveManager.setEconomyLastMessage(lastMessage);
        } else {
            lastMessage = "Bugunun odulu zaten alindi";
        }
        return ok;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
