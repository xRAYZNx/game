package com.arabaoyunu.mode;

import com.arabaoyunu.career.CareerProgressSystem;
import com.arabaoyunu.util.SaveManager;

/**
 * A65.8: Üç gerçek modun ortak ilerleme/kariyer/ekonomi özet köprüsü.
 * Bu sınıf kayıtları tek formatta okur; mod başlatmaz, açık dünya veya yeni asset eklemez.
 */
public final class ModeProgressBridgeSystem {
    public static final int SCHEMA_VERSION = 658;

    private ModeProgressBridgeSystem() {}

    public static int totalModeCompletions(SaveManager save) {
        if (save == null) return 0;
        return Math.max(0, save.getCheckpointRouteTotalCompletions())
                + Math.max(0, save.getDriftCompletedCount())
                + Math.max(0, save.getPoliceTotalChases());
    }

    public static int totalModeWins(SaveManager save) {
        if (save == null) return 0;
        return Math.max(0, save.getCheckpointRouteTotalCompletions())
                + Math.max(0, save.getDriftCompletedCount())
                + Math.max(0, save.getPoliceEscapes());
    }

    public static int totalModeEarnedCoins(SaveManager save) {
        if (save == null) return 0;
        return Math.max(0, save.getCheckpointRouteTotalEarnedCoins())
                + Math.max(0, save.getDriftEarnedCoins())
                + Math.max(0, save.getPoliceEarnedCoins());
    }

    public static int totalModeEarnedXp(SaveManager save) {
        if (save == null) return 0;
        return Math.max(0, save.getDriftEarnedXp()) + Math.max(0, save.getPoliceEarnedXp());
    }

    public static String hubLoopSummary(SaveManager save) {
        if (save == null) return "Ana döngü: veri bekleniyor";
        return "Ana döngü • " + totalModeCompletions(save) + " mod bitişi"
                + " • +" + totalModeEarnedCoins(save) + " coin"
                + " • KP " + CareerProgressSystem.careerPoints(save)
                + " • " + CareerProgressSystem.nextShortGoal(save);
    }

    public static String postResultLine(SaveManager save, String modeLabel, int coins, int xp) {
        if (save == null) return "Kariyer ilerleme kaydı bekleniyor";
        return safe(modeLabel) + " sonucu • +" + Math.max(0, coins) + "C +" + Math.max(0, xp) + "XP"
                + " • KP " + CareerProgressSystem.careerPoints(save)
                + " • Toplam +" + totalModeEarnedCoins(save) + "C"
                + " • Hedef: " + CareerProgressSystem.nextShortGoal(save);
    }

    public static String modeSecurityLine() {
        return "Güvenlik: tek sonuç = tek ödül • HUD temizliği • görev ödülü manuel";
    }

    public static String resultCardFooter(SaveManager save) {
        if (save == null) return modeSecurityLine();
        return RewardBalanceSystem.economyBalanceLine(save) + " • " + modeSecurityLine();
    }

    public static String modeProgressShort(SaveManager save) {
        if (save == null) return "Mod ilerlemesi yok";
        return "CP " + save.getCheckpointRouteTotalCompletions()
                + " • Drift " + save.getDriftCompletedCount()
                + " • Polis " + save.getPoliceEscapes() + "/" + save.getPoliceTotalChases();
    }

    public static String taskAchievementBridgeLine() {
        return "Görev/Başarım: checkpoint, drift ve polis ilerlemeleri sayılır; ödüller panelden alınır";
    }

    private static String safe(String value) {
        return value == null || value.length() == 0 ? "Mod" : value;
    }
}
