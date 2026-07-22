package com.arabaoyunu.career;

import com.arabaoyunu.mode.CheckpointRaceSystem;
import com.arabaoyunu.mode.ModeProgressBridgeSystem;
import com.arabaoyunu.util.SaveManager;

/**
 * A65.4: Oyun modları merkezi ve kariyer başlangıç altyapısı için
 * merkezi, kayıt okuyan ve UI dostu ilerleme hesapları.
 * Yeni mod açmaz; mevcut Checkpoint + Drift ilerlemesini tek kariyer
 * görünümünde toplar.
 */
public final class CareerProgressSystem {
    public static final int SCHEMA_VERSION = 660;

    private CareerProgressSystem() {}

    public static int careerPoints(SaveManager save) {
        if (save == null) return 0;
        int checkpointCompleted = save.getCheckpointRouteTotalCompletions();
        int checkpointCoins = save.getCheckpointRouteTotalEarnedCoins();
        int driftCompleted = save.getDriftCompletedCount();
        int driftScore = save.getDriftTotalScore();
        int medals = totalMedals(save);
        int routeGold = 0;
        for (int i = 0; i < CheckpointRaceSystem.ROUTE_COUNT; i++) {
            routeGold += save.getCheckpointRouteGoldCount(i);
        }
        int policeEscapes = save.getPoliceEscapes();
        int policeChases = save.getPoliceTotalChases();
        int policeWanted = save.getPoliceHighestWanted();
        int points = checkpointCompleted * 14
                + driftCompleted * 12
                + policeEscapes * 18
                + Math.max(0, policeChases - policeEscapes) * 5
                + policeWanted * 12
                + medals * 9
                + routeGold * 8
                + save.getDriftLegendCount() * 12
                + save.getDriftGoldCount() * 8
                + Math.min(220, checkpointCoins / 180)
                + Math.min(260, driftScore / 6500)
                + Math.min(220, save.getPoliceEarnedCoins() / 190);
        return Math.max(0, points);
    }

    public static int totalMedals(SaveManager save) {
        if (save == null) return 0;
        int total = 0;
        for (int i = 0; i < CheckpointRaceSystem.ROUTE_COUNT; i++) {
            total += save.getCheckpointRouteGoldCount(i);
            total += save.getCheckpointRouteSilverCount(i);
            total += save.getCheckpointRouteBronzeCount(i);
        }
        total += save.getDriftLegendCount();
        total += save.getDriftGoldCount();
        total += save.getDriftSilverCount();
        total += save.getDriftBronzeCount();
        if (save.getPoliceHighestWanted() >= 3) total += 1;
        return Math.max(0, total);
    }

    public static int playableModeCount(SaveManager save) {
        if (save == null) return 0;
        int count = 0;
        if (CareerLeagueSystem.isModeUnlocked(save, com.arabaoyunu.menu.GameScreenState.MODE_FREE_DRIVE)) count++;
        if (CareerLeagueSystem.isModeUnlocked(save, com.arabaoyunu.menu.GameScreenState.MODE_RACE_LOCKED)) count++;
        if (CareerLeagueSystem.isModeUnlocked(save, com.arabaoyunu.menu.GameScreenState.MODE_DRIFT)) count++;
        if (CareerLeagueSystem.isModeUnlocked(save, com.arabaoyunu.menu.GameScreenState.MODE_TIME_TRIAL)) count++;
        if (CareerLeagueSystem.isModeUnlocked(save, com.arabaoyunu.menu.GameScreenState.MODE_DRAG_RACE)) count++;
        if (CareerLeagueSystem.isModeUnlocked(save, com.arabaoyunu.menu.GameScreenState.MODE_POLICE_CHASE)) count++;
        return count;
    }

    public static String careerCenterLine(SaveManager save) {
        if (save == null) return "Kariyer merkezi hazirlaniyor";
        return "KP " + careerPoints(save)
                + " • Madalya " + totalMedals(save)
                + " • Mod " + playableModeCount(save) + "/6"
                + " • Lig " + save.getCareerLeagueName();
    }

    public static String nextMilestoneLine(SaveManager save) {
        if (save == null) return "Veri yok";
        int level = save.getPlayerLevel();
        if (level < 2) return "Sonraki hedef: Drift Skor LVL 2";
        if (level < 3) return "Sonraki hedef: Sokak Ligi LVL 3";
        if (level < 6) return "Sonraki hedef: Profesyonel Lig LVL 6";
        if (level < 10) return "Sonraki hedef: Super Lig LVL 10";
        if (level < 15) return "Sonraki hedef: Elit Lig LVL 15";
        return "Tum temel kariyer kilitleri acik";
    }

    public static String rewardSummaryLine(SaveManager save) {
        if (save == null) return "Odul verisi yok";
        int checkpoint = save.getCheckpointRouteTotalEarnedCoins();
        int drift = save.getDriftEarnedCoins();
        int police = save.getPoliceEarnedCoins();
        return "+" + (checkpoint + drift + police) + " coin mod kazancı • Checkpoint " + checkpoint + " • Drift " + drift + " • Polis " + police;
    }

    /** A65.5: Kariyer merkezinde oyuncuya kısa lig/ilerleme etiketi verir. */
    public static String careerTierName(SaveManager save) {
        if (save == null) return "Hazırlık";
        int points = careerPoints(save);
        if (points >= 900) return "Elit Hazırlık";
        if (points >= 620) return "Profesyonel";
        if (points >= 360) return "Sokak Ligi";
        if (points >= 140) return "Aday";
        return "Başlangıç";
    }

    public static String nextShortGoal(SaveManager save) {
        if (save == null) return "Veri bekleniyor";
        int medals = totalMedals(save);
        int cp = save.getCheckpointRouteTotalCompletions();
        int drift = save.getDriftCompletedCount();
        int police = save.getPoliceTotalChases();
        String careerTarget = CareerEventSystem.nextCareerTarget(save);
        if (careerTarget != null && careerTarget.length() > 0) return careerTarget;
        if (cp < 1) return "1 checkpoint yarışı bitir";
        if (drift < 1) return "1 drift denemesi bitir";
        if (police < 1) return "1 polis kovalamaca dene";
        if (medals < 3) return (3 - medals) + " madalya/rank daha kazan";
        if (save.getPlayerLevel() < 5) return "Seviye 5'e ulaş";
        return nextMilestoneLine(save).replace("Sonraki hedef: ", "");
    }

    public static String modeHubQaLine(SaveManager save) {
        if (save == null) return "Mod geçiş QA: veri yok";
        return "QA: üç mod + kariyer ligleri • ödül tek sefer • "
                + ModeProgressBridgeSystem.modeProgressShort(save) + " • " + nextShortGoal(save);
    }

    public static String careerLeagueQaLine(SaveManager save) {
        if (save == null) return "Kariyer QA: veri yok";
        int league = CareerEventSystem.recommendedLeague(save);
        return CareerEventSystem.leagueFlowLine(save, league)
                + " • KP " + careerPoints(save)
                + " • Hazırlık %" + overallProgressPercent(save);
    }

    public static int overallProgressPercent(SaveManager save) {
        if (save == null) return 0;
        int points = careerPoints(save);
        int medals = totalMedals(save);
        int modes = playableModeCount(save);
        int pct = Math.round(Math.min(100f, points * 0.30f + medals * 3.2f + modes * 8f + save.getPoliceEscapes() * 4f));
        return Math.max(0, Math.min(100, pct));
    }
}
