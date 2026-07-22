package com.arabaoyunu.mode;

import com.arabaoyunu.util.SaveManager;

/**
 * A65.0: Checkpoint yarış sonucu, rota seçimi, HUD ve madalya metinlerini ortaklaştırır.
 * Amaç ham/debug satırlarını tek bir profesyonel yarış diliyle vermek ve ödül/rekor
 * bilgisini her ekranda aynı formatta göstermektir.
 */
public final class RaceResultSystem {

    private RaceResultSystem() {}

    public static String checkpointResultText(boolean rivalWon, float elapsed, String grade, int coins, boolean newBest, float bestSeconds) {
        return checkpointRouteResultText(CheckpointRaceSystem.getActiveRouteId(), rivalWon, elapsed, grade, coins, 0, newBest, false, bestSeconds);
    }

    public static String checkpointRouteResultText(int routeId, boolean rivalWon, float elapsed, String grade, int coins, int xp, boolean newBest, boolean newMedal, float bestSeconds) {
        String result = rivalWon ? "KAZANDIN" : "2. OLDUN";
        String time = RaceModeSystem.formatTime(elapsed);
        String best = bestSeconds > 0f ? "EN İYİ " + RaceModeSystem.formatTime(bestSeconds) : "EN İYİ -";
        String bonus = newBest && newMedal ? "YENİ REKOR + MADALYA" : (newBest ? "YENİ REKOR" : (newMedal ? "YENİ MADALYA" : best));
        return CheckpointRaceSystem.routeLabel(routeId) + " | " + result + " | " + time + " | " + safe(grade)
                + " | +" + Math.max(0, coins) + "C" + (xp > 0 ? " +" + xp + "XP" : "") + " | " + bonus;
    }

    public static String checkpointRouteResultText(SaveManager save, int routeId, boolean rivalWon, float elapsed, String grade, int coins, int xp, boolean newBest, boolean newMedal, float bestSeconds) {
        String base = checkpointRouteResultText(routeId, rivalWon, elapsed, grade, coins, xp, newBest, newMedal, bestSeconds);
        return base + " | " + ModeProgressBridgeSystem.postResultLine(save, "Checkpoint", coins, xp);
    }

    public static String checkpointIntroText() {
        return checkpointIntroText(CheckpointRaceSystem.getActiveRouteId());
    }

    public static String checkpointIntroText(int routeId) {
        return CheckpointRaceSystem.routeLabel(routeId) + ": 3-2-1 sonrası sıradaki kapıya git, rota okunu takip et.";
    }

    public static String checkpointPassedText(int passedIndex, int totalTargets) {
        int safePassed = Math.max(1, passedIndex);
        int safeTotal = Math.max(1, totalTargets);
        return "CHECKPOINT GEÇİLDİ  " + safePassed + "/" + safeTotal;
    }

    public static String checkpointWrongWayText() { return "SIRADAKİ HEDEFİ TAKİP ET"; }

    public static String checkpointHudText(float elapsed, int nextCheckpoint, int total, int distanceMeters, float bestSeconds, String leadText) {
        return checkpointHudText(CheckpointRaceSystem.getActiveRouteId(), elapsed, nextCheckpoint, total, distanceMeters, bestSeconds, leadText);
    }

    public static String checkpointHudText(int routeId, float elapsed, int nextCheckpoint, int total, int distanceMeters, float bestSeconds, String leadText) {
        String time = RaceModeSystem.formatTime(Math.max(0f, elapsed));
        String best = bestSeconds > 0f ? RaceModeSystem.formatTime(bestSeconds) : "--:--.--";
        String lead = leadText == null ? "" : leadText.trim();
        if (lead.length() > 20) lead = lead.substring(0, 20);
        return CheckpointRaceSystem.routeLabel(routeId)
                + "  SÜRE " + time
                + "  CP " + Math.max(1, nextCheckpoint) + "/" + Math.max(1, total)
                + "  HEDEF " + Math.max(0, distanceMeters) + "m"
                + "  EN İYİ " + best
                + (lead.length() == 0 ? "" : "  " + lead);
    }

    public static String checkpointModeCardText(int completed, int gold, int silver, int bronze, int earned, float bestSeconds) {
        String best = bestSeconds > 0f ? RaceModeSystem.formatTime(bestSeconds) : "-";
        return "En iyi " + best + " | Tamamlanan " + Math.max(0, completed)
                + " | Madalya " + Math.max(0, gold) + "/" + Math.max(0, silver) + "/" + Math.max(0, bronze)
                + " | Kazanç +" + Math.max(0, earned);
    }

    public static String checkpointRouteCardText(int routeId, float bestSeconds, String bestMedal, int completed, int earned) {
        String best = bestSeconds > 0f ? RaceModeSystem.formatTime(bestSeconds) : "-";
        String medal = bestMedal == null || bestMedal.length() == 0 ? "Madalya yok" : bestMedal;
        return best + " • " + medal + " • " + Math.max(0, completed) + " bitiş • +" + Math.max(0, earned);
    }

    public static String checkpointRouteCardSubline(int routeId, float lastSeconds, String lastMedal) {
        String last = lastSeconds > 0f ? RaceModeSystem.formatTime(lastSeconds) : "-";
        String medal = lastMedal == null || lastMedal.length() == 0 ? "son madalya yok" : lastMedal;
        return CheckpointRaceSystem.routeBalanceText(routeId) + " • Son " + last + " " + medal;
    }

    public static String checkpointRewardAuditText(int previousCompletions, boolean newBest, boolean newMedal) {
        return CheckpointRaceSystem.repeatRewardRuleText(previousCompletions, newBest, newMedal);
    }

    public static String checkpointFinishHint(boolean newBest) {
        return newBest ? "Yeni rekor kaydedildi • Tekrar: gaz • Menü: mod seçimi" : "Ödül verildi • Tekrar: gaz • Menü: mod seçimi";
    }

    private static String safe(String value) { return value == null || value.length() == 0 ? "-" : value; }
}
