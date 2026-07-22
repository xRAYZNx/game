package com.arabaoyunu.mode;

import com.arabaoyunu.util.SaveManager;

/**
 * A65.6: Polis kovalamaca sonucunu, HUD ipuclarini ve mod merkezi metinlerini
 * tek yerden uretir. Yeni model/harita eklemez; mevcut Open Field polis
 * altyapisini profesyonel oyun akisi haline getirir.
 */
public final class PoliceChaseResultSystem {
    private PoliceChaseResultSystem() {}

    public static String resultTitle(boolean escaped) {
        return escaped ? "KAÇIŞ BAŞARILI" : "YAKALANDIN";
    }

    public static String resultLine(boolean escaped, int wanted, int seconds, int coins, int xp, boolean newBest) {
        String state = escaped ? "Kaçış Başarılı" : "Yakalandın";
        return state + " • " + PoliceChaseSystem.wantedStars(wanted)
                + " • Süre " + formatSeconds(seconds)
                + " • Risk/AI dengeli"
                + " • +" + Math.max(0, coins) + "C +" + Math.max(0, xp) + "XP"
                + (newBest ? " • YENİ REKOR" : "");
    }

    public static String resultLine(SaveManager save, boolean escaped, int wanted, int seconds, int coins, int xp, boolean newBest) {
        return resultLine(escaped, wanted, seconds, coins, xp, newBest)
                + " • " + ModeProgressBridgeSystem.postResultLine(save, "Polis", coins, xp);
    }

    public static String hudLine(int wanted, float remaining, float nearestMeters, float capturePercent) {
        return PoliceAiController.aiBalanceLine(nearestMeters, capturePercent, wanted)
                + "  Kalan " + Math.max(0, (int)Math.ceil(remaining)) + "sn";
    }

    public static String modeHubSummary(SaveManager save) {
        if (save == null) return "Kaçış verisi yok";
        String best = save.getPoliceBestSeconds() > 0 ? formatSeconds(save.getPoliceBestSeconds()) : "-";
        return "En iyi " + best
                + " • Kaçış " + save.getPoliceEscapes()
                + "/" + save.getPoliceTotalChases()
                + " • ★" + Math.max(0, save.getPoliceHighestWanted())
                + " • +" + save.getPoliceEarnedCoins() + " coin";
    }

    public static String finishHint(boolean escaped) {
        return escaped ? "Gaz: tekrar kaçış  |  Menü: mod merkezi  |  AI/ödül güvenli"
                : "Gaz: tekrar dene  |  Garajda onar / güçlendir  |  Ödül güvenli";
    }

    public static String formatSeconds(int seconds) {
        int safe = Math.max(0, seconds);
        int m = safe / 60;
        int s = safe % 60;
        return (m < 10 ? "0" : "") + m + ":" + (s < 10 ? "0" : "") + s;
    }
}
