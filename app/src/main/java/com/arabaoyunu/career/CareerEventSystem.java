package com.arabaoyunu.career;

import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.mode.CheckpointRaceSystem;
import com.arabaoyunu.util.SaveManager;

/**
 * A65.9: Kariyer liglerini gerçek oynanabilir modlara bağlayan güvenli etkinlik tablosu.
 * Yeni harita/model eklemez; Checkpoint, Drift ve Polis modlarının mevcut kayıtlarını okur.
 */
public final class CareerEventSystem {
    public static final int SCHEMA_VERSION = 660;
    public static final int SUPPORTED_LEAGUE_COUNT = CareerLeagueSystem.LEAGUE_COUNT;
    public static final int EVENT_COUNT = 4;

    public static final int EVENT_CHECKPOINT = 0;
    public static final int EVENT_DRIFT = 1;
    public static final int EVENT_POLICE = 2;
    public static final int EVENT_MIXED = 3;

    private CareerEventSystem() {}

    public static int safeLeague(int league) {
        if (league < 0) return 0;
        if (league >= CareerLeagueSystem.LEAGUE_COUNT) return CareerLeagueSystem.LEAGUE_COUNT - 1;
        return league;
    }

    public static int safeEvent(int event) {
        if (event < 0) return 0;
        if (event >= EVENT_COUNT) return EVENT_COUNT - 1;
        return event;
    }

    public static int recommendedLeague(SaveManager save) {
        if (save == null) return 0;
        int highestUnlocked = 0;
        for (int i = 0; i < CareerLeagueSystem.LEAGUE_COUNT; i++) {
            if (isLeagueUnlocked(save, i)) {
                highestUnlocked = i;
                if (!isLeagueCompleted(save, i) || !save.isCareerLeagueRewardClaimed(i)) {
                    return i;
                }
            }
        }
        return highestUnlocked;
    }

    public static boolean isLeagueUnlocked(SaveManager save, int league) {
        if (save == null) return false;
        int l = safeLeague(league);
        if (l == 0) return save.isCareerStarted();
        if (!save.isCareerStarted()) return false;
        boolean previousComplete = isLeagueCompleted(save, l - 1) || save.isCareerLeagueRewardClaimed(l - 1);
        boolean levelGate = save.getPlayerLevel() >= CareerLeagueSystem.requiredLevel(l);
        int medals = CareerProgressSystem.totalMedals(save);
        boolean performanceGate;
        if (l == 1) performanceGate = medals >= 2 || save.getCheckpointRouteTotalCompletions() >= 2;
        else if (l == 2) performanceGate = medals >= 5 || save.getPoliceEscapes() >= 1;
        else if (l == 3) performanceGate = medals >= 9 && save.getDriftBestScore() >= 30000;
        else performanceGate = medals >= 14 && save.getPoliceHighestWanted() >= 3;
        return previousComplete || (levelGate && performanceGate);
    }

    public static String leagueTitle(int league) {
        return CareerLeagueSystem.leagueName(safeLeague(league));
    }

    public static String leagueSubtitle(SaveManager save, int league) {
        int l = safeLeague(league);
        if (!isLeagueUnlocked(save, l)) return unlockText(save, l);
        int done = completedEventCount(save, l);
        return done + "/" + EVENT_COUNT + " etkinlik • " + (isLeagueCompleted(save, l) ? "Lig tamam" : "Hedefler devam");
    }

    public static String unlockText(SaveManager save, int league) {
        int l = safeLeague(league);
        if (l == 0) return "Kariyeri başlat";
        String previous = CareerLeagueSystem.leagueName(l - 1) + " bitir";
        String level = "LVL " + CareerLeagueSystem.requiredLevel(l);
        String medal = requiredPerformanceText(l);
        return "Kilitli • " + previous + " veya " + level + " + " + medal;
    }

    public static String requiredPerformanceText(int league) {
        int l = safeLeague(league);
        if (l <= 0) return "başlangıç";
        if (l == 1) return "2 madalya/2 yarış";
        if (l == 2) return "5 madalya veya kaçış";
        if (l == 3) return "9 madalya + 30K drift";
        return "14 madalya + 3 yıldız";
    }

    public static String eventTitle(int league, int event) {
        int l = safeLeague(league);
        int e = safeEvent(event);
        if (l == 0) {
            if (e == EVENT_CHECKPOINT) return "Checkpoint Başlangıç";
            if (e == EVENT_DRIFT) return "İlk Drift Denemesi";
            if (e == EVENT_POLICE) return "İlk Kaçış";
            return "Bronz Karma Hedef";
        }
        if (l == 1) {
            if (e == EVENT_CHECKPOINT) return "Orta Rota Mücadelesi";
            if (e == EVENT_DRIFT) return "B Rank Drift";
            if (e == EVENT_POLICE) return "2 Yıldız Takip";
            return "Gümüş Seri";
        }
        if (l == 2) {
            if (e == EVENT_CHECKPOINT) return "Uzun Rota";
            if (e == EVENT_DRIFT) return "A Rank Drift";
            if (e == EVENT_POLICE) return "90 Sn Kaçış";
            return "Altın Üçlü";
        }
        if (l == 3) {
            if (e == EVENT_CHECKPOINT) return "Teknik Rota";
            if (e == EVENT_DRIFT) return "S Rank Drift";
            if (e == EVENT_POLICE) return "3 Yıldız Kaçış";
            return "Pro Seri";
        }
        if (e == EVENT_CHECKPOINT) return "Elite Checkpoint";
        if (e == EVENT_DRIFT) return "Elite Drift";
        if (e == EVENT_POLICE) return "Elite Kaçış";
        return "Elite Final";
    }

    public static String eventGoal(int league, int event) {
        int l = safeLeague(league);
        int e = safeEvent(event);
        if (l == 0) {
            if (e == EVENT_CHECKPOINT) return "Kısa rotayı tamamla";
            if (e == EVENT_DRIFT) return "Drift modunu bitir";
            if (e == EVENT_POLICE) return "45 saniye takipte kal";
            return "2 farklı mod tamamla";
        }
        if (l == 1) {
            if (e == EVENT_CHECKPOINT) return "Orta rotayı tamamla";
            if (e == EVENT_DRIFT) return "15.000 drift skoru veya madalya";
            if (e == EVENT_POLICE) return "2 yıldız seviyesine ulaş";
            return "3 madalya/rank topla";
        }
        if (l == 2) {
            if (e == EVENT_CHECKPOINT) return "Uzun rotayı tamamla";
            if (e == EVENT_DRIFT) return "Altın/A rank drift";
            if (e == EVENT_POLICE) return "90 saniye başarılı kaç";
            return "6 madalya/rank topla";
        }
        if (l == 3) {
            if (e == EVENT_CHECKPOINT) return "Teknik rotayı tamamla";
            if (e == EVENT_DRIFT) return "30.000+ drift ve yüksek combo";
            if (e == EVENT_POLICE) return "3 yıldız kovalamaca";
            return "9 madalya/rank topla";
        }
        if (e == EVENT_CHECKPOINT) return "Tüm rotalarda ilerle";
        if (e == EVENT_DRIFT) return "50.000 drift skoru";
        if (e == EVENT_POLICE) return "3 yıldız kaçışı tamamla";
        return "14 madalya/rank topla";
    }

    public static int eventMode(int league, int event) {
        int e = safeEvent(event);
        if (e == EVENT_CHECKPOINT) return GameScreenState.MODE_RACE_LOCKED;
        if (e == EVENT_DRIFT) return GameScreenState.MODE_DRIFT;
        if (e == EVENT_POLICE) return GameScreenState.MODE_POLICE_CHASE;
        int l = safeLeague(league);
        if (l == 0) return GameScreenState.MODE_RACE_LOCKED;
        if (l == 1) return GameScreenState.MODE_DRIFT;
        if (l == 2) return GameScreenState.MODE_POLICE_CHASE;
        return GameScreenState.MODE_RACE_LOCKED;
    }

    public static int eventRoute(int league, int event) {
        int l = safeLeague(league);
        int e = safeEvent(event);
        if (e != EVENT_CHECKPOINT && e != EVENT_MIXED) return CheckpointRaceSystem.ROUTE_MEDIUM;
        if (l == 0) return CheckpointRaceSystem.ROUTE_SHORT;
        if (l == 1) return CheckpointRaceSystem.ROUTE_MEDIUM;
        if (l == 2) return CheckpointRaceSystem.ROUTE_LONG;
        return CheckpointRaceSystem.ROUTE_TECHNICAL;
    }

    public static boolean isEventCompleted(SaveManager save, int league, int event) {
        if (save == null || !isLeagueUnlocked(save, league)) return false;
        int l = safeLeague(league);
        int e = safeEvent(event);
        if (e == EVENT_CHECKPOINT) {
            return save.getCheckpointRouteCompletedCount(eventRoute(l, e)) > 0;
        }
        if (e == EVENT_DRIFT) {
            if (l == 0) return save.getDriftCompletedCount() > 0;
            if (l == 1) return save.getDriftBestScore() >= 15000 || save.getDriftBronzeCount() + save.getDriftSilverCount() + save.getDriftGoldCount() + save.getDriftLegendCount() > 0;
            if (l == 2) return save.getDriftBestScore() >= 26000 || save.getDriftGoldCount() + save.getDriftLegendCount() > 0;
            if (l == 3) return save.getDriftBestScore() >= 30000 && save.getDriftBestCombo() >= 2;
            return save.getDriftBestScore() >= 50000 || save.getDriftLegendCount() > 0;
        }
        if (e == EVENT_POLICE) {
            if (l == 0) return save.getPoliceBestSeconds() >= 45 || save.getPoliceTotalChases() > 0;
            if (l == 1) return save.getPoliceHighestWanted() >= 2 || save.getPoliceBestSeconds() >= 60;
            if (l == 2) return save.getPoliceEscapes() > 0 && save.getPoliceBestSeconds() >= 90;
            return save.getPoliceHighestWanted() >= 3 && (save.getPoliceEscapes() > 0 || save.getPoliceBestSeconds() >= 90);
        }
        int totalModes = save.getCheckpointRouteTotalCompletions() + save.getDriftCompletedCount() + save.getPoliceTotalChases();
        int medals = CareerProgressSystem.totalMedals(save);
        if (l == 0) return totalModes >= 2;
        if (l == 1) return medals >= 3;
        if (l == 2) return medals >= 6;
        if (l == 3) return medals >= 9;
        return medals >= 14;
    }

    public static int completedEventCount(SaveManager save, int league) {
        int count = 0;
        for (int i = 0; i < EVENT_COUNT; i++) if (isEventCompleted(save, league, i)) count++;
        return count;
    }

    public static boolean isLeagueCompleted(SaveManager save, int league) {
        return completedEventCount(save, league) >= EVENT_COUNT;
    }

    public static int eventRewardCoins(int league, int event) {
        int l = safeLeague(league);
        return 420 + l * 260 + safeEvent(event) * 70;
    }

    public static int eventRewardXp(int league, int event) {
        int l = safeLeague(league);
        return 70 + l * 38 + safeEvent(event) * 12;
    }

    public static int leagueRewardCoins(int league) {
        int l = safeLeague(league);
        return 1500 + l * 900;
    }

    public static int leagueRewardXp(int league) {
        int l = safeLeague(league);
        return 240 + l * 130;
    }

    public static String eventStatusLine(SaveManager save, int league, int event) {
        if (!isLeagueUnlocked(save, league)) return "KİLİTLİ";
        boolean done = isEventCompleted(save, league, event);
        boolean claimed = save != null && save.isCareerEventRewardClaimed(safeLeague(league), safeEvent(event));
        if (claimed) return "TAMAMLANDI / ÖDÜL ALINDI";
        if (done) return "TAMAMLANDI / ÖDÜL HAZIR";
        return "HEDEF DEVAM EDİYOR";
    }

    public static String eventRewardLine(int league, int event) {
        return "+" + eventRewardCoins(league, event) + "C / +" + eventRewardXp(league, event) + "XP";
    }

    public static String leagueRewardLine(int league) {
        return "+" + leagueRewardCoins(league) + "C / +" + leagueRewardXp(league) + "XP";
    }

    public static boolean isEventRewardReady(SaveManager save, int league, int event) {
        return save != null && isEventCompleted(save, league, event)
                && !save.isCareerEventRewardClaimed(safeLeague(league), safeEvent(event));
    }

    public static boolean isLeagueRewardReady(SaveManager save, int league) {
        return save != null && isLeagueCompleted(save, league)
                && !save.isCareerLeagueRewardClaimed(safeLeague(league));
    }

    public static int readyRewardCount(SaveManager save, int league) {
        int count = 0;
        for (int i = 0; i < EVENT_COUNT; i++) if (isEventRewardReady(save, league, i)) count++;
        if (isLeagueRewardReady(save, league)) count++;
        return count;
    }

    public static String eventTypeLabel(int event) {
        int e = safeEvent(event);
        if (e == EVENT_CHECKPOINT) return "Checkpoint";
        if (e == EVENT_DRIFT) return "Drift";
        if (e == EVENT_POLICE) return "Polis";
        return "Karma";
    }

    public static String eventStartHint(int league, int event) {
        int e = safeEvent(event);
        if (e == EVENT_CHECKPOINT) return "Open Field • rota " + routeName(eventRoute(league, event));
        if (e == EVENT_DRIFT) return "Open Field • skor/rank hedefi";
        if (e == EVENT_POLICE) return "Open Field • kaçış/yıldız hedefi";
        return "Open Field • karma kariyer hedefi";
    }

    public static String routeName(int route) {
        if (route == CheckpointRaceSystem.ROUTE_SHORT) return "Kısa";
        if (route == CheckpointRaceSystem.ROUTE_MEDIUM) return "Orta";
        if (route == CheckpointRaceSystem.ROUTE_LONG) return "Uzun";
        if (route == CheckpointRaceSystem.ROUTE_TECHNICAL) return "Teknik";
        return "Güvenli";
    }

    public static String leagueFlowLine(SaveManager save, int league) {
        int l = safeLeague(league);
        if (!isLeagueUnlocked(save, l)) return unlockText(save, l);
        int done = completedEventCount(save, l);
        int ready = readyRewardCount(save, l);
        if (isLeagueCompleted(save, l)) {
            if (isLeagueRewardReady(save, l)) return leagueTitle(l) + " tamam • büyük ödül hazır";
            if (save != null && save.isCareerLeagueRewardClaimed(l)) return leagueTitle(l) + " tamam • sonraki lige geç";
            return leagueTitle(l) + " tamam • etkinlik ödüllerini kontrol et";
        }
        return leagueTitle(l) + " ilerleme " + done + "/" + EVENT_COUNT + " • hazır ödül " + ready;
    }

    public static String eventRequirementResultLine(SaveManager save, int league, int event) {
        if (!isLeagueUnlocked(save, league)) return unlockText(save, league);
        if (isEventCompleted(save, league, event)) return "Hedef karşılandı • " + eventRewardLine(league, event);
        return "Hedef bekliyor • " + eventGoal(league, event);
    }

    public static String nextCareerTarget(SaveManager save) {
        if (save == null) return "Kariyer verisi bekleniyor";
        for (int l = 0; l < CareerLeagueSystem.LEAGUE_COUNT; l++) {
            if (!isLeagueUnlocked(save, l)) return unlockText(save, l);
            for (int e = 0; e < EVENT_COUNT; e++) {
                if (!isEventCompleted(save, l, e)) return leagueTitle(l) + ": " + eventGoal(l, e);
                if (!save.isCareerEventRewardClaimed(l, e)) return leagueTitle(l) + ": " + eventTitle(l, e) + " ödülünü al";
            }
            if (isLeagueCompleted(save, l) && !save.isCareerLeagueRewardClaimed(l)) return leagueTitle(l) + " lig ödülünü al";
        }
        return "Tüm temel lig hedefleri hazır";
    }
}
