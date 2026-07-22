package com.arabaoyunu.mode;

import com.arabaoyunu.career.CareerLeagueSystem;
import com.arabaoyunu.career.CareerProgressSystem;
import com.arabaoyunu.career.CareerEventSystem;
import com.arabaoyunu.career.CareerResultSystem;
import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.performance.VehicleTuningSystem;

/**
 * A65.4: Checkpoint ve Drift modlarını tek profesyonel oyun modu merkezi
 * metinleriyle birleştirir. Bu sınıf sadece mevcut kayıtları okur;
 * yeni yarış modu veya açık dünya açmaz.
 */
public final class GameModeHubSystem {
    public static final int SCHEMA_VERSION = 660;

    private GameModeHubSystem() {}

    public static String hubSubtitle(SaveManager save) {
        if (save == null) return "Mod Merkezi • kariyer verisi yukleniyor";
        return "Mod Merkezi • " + CareerProgressSystem.careerCenterLine(save) + " • " + ModeProgressBridgeSystem.modeProgressShort(save);
    }

    public static String freeDriveCardLine(SaveManager save) {
        if (save == null) return "Test ve serbest surus";
        return "Garaj araci • test surusu • " + save.getCareerLeagueName();
    }

    public static String checkpointCardLine(SaveManager save) {
        if (save == null) return "4 rota • madalya • rekor";
        return CheckpointRaceSystem.ROUTE_COUNT + " rota • "
                + save.getCheckpointRouteTotalCompletions() + " bitis • +"
                + save.getCheckpointRouteTotalEarnedCoins() + " coin";
    }

    public static String driftCardLine(SaveManager save) {
        if (save == null) return "skor • combo • rank";
        return "En iyi " + save.getDriftBestScore()
                + " • Combo x" + save.getDriftBestCombo()
                + " • +" + save.getDriftEarnedCoins() + " coin";
    }

    public static String futureModeLine(SaveManager save, int mode) {
        if (save == null) return "Yakinda";
        if (CareerLeagueSystem.isModeUnlocked(save, mode)) return "Acilabilir • merkezden baslat";
        return "Kilitli • " + CareerLeagueSystem.modeLockText(mode);
    }

    public static String selectedModeSummary(SaveManager save, int mode) {
        if (save == null) return "Mod verisi yok";
        if (mode == GameScreenState.MODE_RACE_LOCKED) {
            return save.getCheckpointRouteBestSummary()
                    + " • " + save.getCheckpointRouteTotalCompletions() + " bitis"
                    + " • +" + save.getCheckpointRouteTotalEarnedCoins() + " coin";
        }
        if (mode == GameScreenState.MODE_DRIFT) {
            return "En iyi " + save.getDriftBestScore()
                    + " • Rank " + safeText(save.getDriftLastGrade(), "-")
                    + " • Combo x" + save.getDriftBestCombo();
        }
        if (mode == GameScreenState.MODE_POLICE_CHASE) {
            return PoliceChaseResultSystem.modeHubSummary(save);
        }
        if (mode == GameScreenState.MODE_DRAG_RACE) {
            return futureModeLine(save, mode);
        }
        return CareerProgressSystem.rewardSummaryLine(save);
    }

    public static String unlockPreviewLine(SaveManager save) {
        return CareerProgressSystem.nextMilestoneLine(save);
    }

    public static String progressPercentLine(SaveManager save) {
        return "Kariyer hazirlik: %" + CareerProgressSystem.overallProgressPercent(save);
    }

    /** A65.5: Mod merkezi üst rozetlerinde kullanılan kısa profesyonel özet. */
    public static String hubHeaderLine(SaveManager save) {
        if (save == null) return "KP 0 • Veri bekleniyor";
        return "KP " + CareerProgressSystem.careerPoints(save)
                + " • " + CareerProgressSystem.careerTierName(save)
                + " • %" + CareerProgressSystem.overallProgressPercent(save)
                + " • +" + ModeProgressBridgeSystem.totalModeEarnedCoins(save) + "C";
    }

    /** A65.5: Checkpoint kartı için daha yoğun fakat okunabilir final istatistik satırı. */
    public static String checkpointFinalCardLine(SaveManager save) {
        if (save == null) return "4 rota • en iyi - • madalya -";
        return CheckpointRaceSystem.ROUTE_COUNT + " rota • "
                + save.getCheckpointRouteBestSummary()
                + " • " + save.getCheckpointRouteTotalCompletions() + " bitiş";
    }

    /** A65.5: Drift kartı için final merkez istatistiği. */
    public static String driftFinalCardLine(SaveManager save) {
        if (save == null) return "Skor - • combo - • rank -";
        return "Skor " + save.getDriftBestScore()
                + " • x" + save.getDriftBestCombo()
                + " • Rank " + DriftScoreSystem.rankCode(save.getDriftLastGrade());
    }

    /** A65.6: Polis kartı için oynanabilir mod özeti. */
    public static String policeFinalCardLine(SaveManager save) {
        return PoliceChaseResultSystem.modeHubSummary(save);
    }

    public static String selectedVehicleBuildLine(SaveManager save, int vehicleIndex) {
        if (save == null) return "Araç verisi yükleniyor";
        String id = com.arabaoyunu.vehicle.VehicleCatalog.id(vehicleIndex);
        int build = Math.max(0, save.getTotalPerformanceUpgradeLevel(id));
        return "Seçili araç: " + com.arabaoyunu.menu.GameScreenState.vehicleLabel(vehicleIndex)
                + " • Build " + build + "/" + (com.arabaoyunu.util.SaveManager.PERFORMANCE_UPGRADE_COUNT * com.arabaoyunu.util.SaveManager.MAX_UPGRADE_LEVEL)
                + " • " + VehicleTuningSystem.presetLabel(save.getTuningPreset(id));
    }

    public static String modeTransitionSafetyLine(SaveManager save, int selectedMode) {
        if (save == null) return "Geçiş güvenliği: veri bekleniyor";
        if (!CareerLeagueSystem.isModeUnlocked(save, selectedMode)) {
            return "Kilitli mod • " + CareerLeagueSystem.modeLockText(selectedMode);
        }
        if (selectedMode == GameScreenState.MODE_RACE_LOCKED) return "Güvenli akış: Rota seç → Yarış → Sonuç → Mod Merkezi";
        if (selectedMode == GameScreenState.MODE_DRIFT) return "Güvenli akış: Drift → Sonuç → Mod Merkezi / Garaj";
        if (selectedMode == GameScreenState.MODE_FREE_DRIVE) return "Güvenli akış: Serbest sürüş → Garaj / Mod Merkezi";
        return "Güvenli akış: Kilit durumu ve HUD temizliği kontrol edilir";
    }

    public static String lockedModeCardLine(SaveManager save, int mode) {
        String lock = futureModeLine(save, mode);
        if (mode == GameScreenState.MODE_POLICE_CHASE) return "Polis kaçışı • " + lock;
        if (mode == GameScreenState.MODE_DRAG_RACE) return "Yakında • Drag/Rakip • " + lock;
        if (mode == GameScreenState.MODE_TIME_TRIAL) return "Yakında • Time attack • " + lock;
        return "Yakında • " + lock;
    }

    public static String postResultCareerLine(SaveManager save, int earnedCoins, int earnedXp) {
        if (save == null) return "Kariyer ilerleme kaydı bekleniyor";
        return ModeProgressBridgeSystem.postResultLine(save, "Mod", earnedCoins, earnedXp);
    }

    /** A65.8: Üç gerçek modun tek merkezde dengeli ekonomi/ilerleme özeti. */
    public static String threeModeLoopLine(SaveManager save) {
        return ModeProgressBridgeSystem.hubLoopSummary(save);
    }

    public static String economyBalanceLine(SaveManager save) {
        return RewardBalanceSystem.economyBalanceLine(save);
    }

    public static String taskAchievementBridgeLine() {
        return ModeProgressBridgeSystem.taskAchievementBridgeLine();
    }


    /** A65.9: Kariyer Ligi kartı, mod merkezi içinde dördüncü ana döngü olarak görünür. */
    public static String careerLeagueCardLine(SaveManager save) {
        if (save == null) return "Kariyer Ligi • veri bekleniyor";
        int league = CareerEventSystem.recommendedLeague(save);
        return CareerEventSystem.leagueTitle(league) + " • "
                + CareerEventSystem.leagueFlowLine(save, league)
                + " • KP " + CareerProgressSystem.careerPoints(save);
    }

    public static String careerLeagueOverviewLine(SaveManager save) {
        return CareerResultSystem.careerOverviewLine(save);
    }

    private static String safeText(String value, String fallback) {
        return value == null || value.length() == 0 ? fallback : value;
    }
}
