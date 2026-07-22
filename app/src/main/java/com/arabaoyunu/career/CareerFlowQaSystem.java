package com.arabaoyunu.career;

import com.arabaoyunu.util.SaveManager;

/**
 * A66.0: Kariyer ligi final QA, etkinlik/lig ödülü ve UI akış güvenliği için
 * yalnızca kayıt okuyan, AIDE uyumlu yardımcı sistem.
 */
public final class CareerFlowQaSystem {
    public static final int SCHEMA_VERSION = 660;

    private CareerFlowQaSystem() {}

    public static String screenHeaderLine(SaveManager save) {
        if (save == null) return "Kariyer Final QA • kayıt bekleniyor";
        int league = CareerEventSystem.recommendedLeague(save);
        return "A66.0 Final QA • " + CareerEventSystem.leagueFlowLine(save, league)
                + " • " + rewardGuardLine(save, league);
    }

    public static String rewardGuardLine(SaveManager save, int league) {
        if (save == null) return "Ödül güvenliği: veri yok";
        int ready = CareerEventSystem.readyRewardCount(save, league);
        int claimed = save.getCareerEventClaimedCount(league)
                + (save.isCareerLeagueRewardClaimed(league) ? 1 : 0);
        return "Tek sefer ödül • hazır " + ready + " • alınan " + claimed;
    }

    public static String transitionSafetyLine(SaveManager save) {
        if (save == null) return "Geçiş güvenliği: veri yok";
        String active = save.getActiveCareerEventLabel();
        return "Geçiş: Kariyer→Mod→Sonuç→Kariyer • Aktif: " + active;
    }

    public static String eventCardFooter(SaveManager save, int league, int event) {
        String type = CareerEventSystem.eventTypeLabel(event);
        String start = CareerEventSystem.eventStartHint(league, event);
        String result = CareerEventSystem.eventRequirementResultLine(save, league, event);
        return type + " • " + start + " • " + result;
    }

    public static String leagueUnlockAuditLine(SaveManager save, int league) {
        if (CareerEventSystem.isLeagueUnlocked(save, league)) {
            return CareerEventSystem.leagueTitle(league) + " açık • " + CareerEventSystem.leagueFlowLine(save, league);
        }
        return CareerEventSystem.leagueTitle(league) + " kilitli • " + CareerEventSystem.unlockText(save, league);
    }

    public static String finalQaSummary(SaveManager save) {
        if (save == null) return "A66.0 QA: veri yok";
        int current = CareerEventSystem.recommendedLeague(save);
        return "A66.0 QA: lig " + CareerEventSystem.leagueTitle(current)
                + " • etkinlik " + CareerEventSystem.completedEventCount(save, current) + "/" + CareerEventSystem.EVENT_COUNT
                + " • ödül güvenli • açık dünya kapalı";
    }
}
