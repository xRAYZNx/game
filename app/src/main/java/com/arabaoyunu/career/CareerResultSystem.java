package com.arabaoyunu.career;

import com.arabaoyunu.util.SaveManager;

/** A65.9: Kariyer etkinliği ve lig ödülü için UI dostu sonuç metinleri. */
public final class CareerResultSystem {
    public static final int SCHEMA_VERSION = 660;

    private CareerResultSystem() {}

    public static String eventResultLine(SaveManager save, int league, int event) {
        String title = CareerEventSystem.eventTitle(league, event);
        boolean done = CareerEventSystem.isEventCompleted(save, league, event);
        boolean claimed = save != null && save.isCareerEventRewardClaimed(league, event);
        if (claimed) return title + " • ödül alındı • " + CareerEventSystem.eventRewardLine(league, event);
        if (done) return title + " • ödül hazır • " + CareerEventSystem.eventRewardLine(league, event);
        return title + " • " + CareerEventSystem.eventGoal(league, event);
    }

    public static String leagueResultLine(SaveManager save, int league) {
        boolean unlocked = CareerEventSystem.isLeagueUnlocked(save, league);
        if (!unlocked) return CareerEventSystem.unlockText(save, league);
        int done = CareerEventSystem.completedEventCount(save, league);
        boolean claimed = save != null && save.isCareerLeagueRewardClaimed(league);
        if (claimed) return CareerEventSystem.leagueTitle(league) + " ödülü alındı";
        if (done >= CareerEventSystem.EVENT_COUNT) return CareerEventSystem.leagueTitle(league) + " büyük ödül hazır • " + CareerEventSystem.leagueRewardLine(league);
        return CareerEventSystem.leagueTitle(league) + " ilerleme: " + done + "/" + CareerEventSystem.EVENT_COUNT;
    }

    public static String careerOverviewLine(SaveManager save) {
        if (save == null) return "Kariyer Ligi verisi bekleniyor";
        return "Kariyer Ligi • " + CareerProgressSystem.careerTierName(save)
                + " • KP " + CareerProgressSystem.careerPoints(save)
                + " • Hedef: " + CareerEventSystem.nextCareerTarget(save);
    }
}
