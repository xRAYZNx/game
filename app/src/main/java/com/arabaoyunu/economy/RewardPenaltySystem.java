package com.arabaoyunu.economy;

import com.arabaoyunu.util.SaveManager;

/**
 * ArabaOyunu_44: Ödül ve ceza ekonomisi.
 *
 * Görev ödülleri:
 * - coin
 * - XP
 * - kasa
 * - araç/parça/renk/jant kilit açma
 *
 * Cezalar:
 * - polis yakalanma para cezası
 * - wanted level çarpanı
 * - araç hasarına göre tamir masrafı
 */
public final class RewardPenaltySystem {

    private final SaveManager saveManager;
    private final UnlockSystem unlockSystem;

    private String sessionMessage = "";
    private float messageTimer;

    public RewardPenaltySystem(SaveManager saveManager) {
        this.saveManager = saveManager;
        this.unlockSystem = new UnlockSystem(saveManager);
    }

    public void update(float dt) {
        if (dt < 0f) dt = 0f;
        if (dt > 0.12f) dt = 0.12f;
        if (messageTimer > 0f) {
            messageTimer -= dt;
            if (messageTimer <= 0f) sessionMessage = "";
        }
    }

    public String grantQuestReward(int completedQuestIndex, int xp, int coins, int crates, String reason) {
        if (saveManager == null) return "";
        if (xp > 0) saveManager.addXp(xp);
        if (coins > 0) saveManager.addCoins(coins);
        if (crates > 0) saveManager.setRewardCrates(saveManager.getRewardCrates() + crates);

        String unlock = unlockSystem.unlockForQuestStep(completedQuestIndex);
        String msg = "ÖDÜL: +" + coins + " coin +" + xp + " XP";
        if (crates > 0) msg += " +" + crates + " kasa";
        if (unlock != null && unlock.length() > 0) msg += " | " + unlock;
        if (reason != null && reason.length() > 0) msg = reason + " | " + msg;
        setMessage(msg);
        return msg;
    }

    public String grantRaceReward(boolean firstPlace, int xp, int coins) {
        if (saveManager == null) return "";
        saveManager.addXp(Math.max(0, xp));
        saveManager.addCoins(Math.max(0, coins));
        String unlock = unlockSystem.unlockRaceReward(firstPlace);
        String msg = "YARIŞ ÖDÜLÜ: +" + coins + " coin +" + xp + " XP";
        if (unlock.length() > 0) msg += " | " + unlock;
        setMessage(msg);
        return msg;
    }

    public String grantDriftReward(int xp, int coins) {
        if (saveManager == null) return "";
        saveManager.addXp(Math.max(0, xp));
        saveManager.addCoins(Math.max(0, coins));
        String unlock = unlockSystem.unlockDriftReward();
        String msg = "DRIFT ÖDÜLÜ: +" + coins + " coin +" + xp + " XP";
        if (unlock.length() > 0) msg += " | " + unlock;
        setMessage(msg);
        return msg;
    }

    public String grantSpecialEventReward() {
        if (saveManager == null) return "";
        saveManager.addXp(220);
        saveManager.addCoins(420);
        saveManager.setRewardCrates(saveManager.getRewardCrates() + 1);
        String unlock = unlockSystem.unlockSpecialEventReward();
        String msg = "ÖZEL ETKİNLİK: +420 coin +220 XP +1 kasa";
        if (unlock.length() > 0) msg += " | " + unlock;
        setMessage(msg);
        return msg;
    }

    public String applyPoliceCaughtPenalty(String vehicleId, int wantedLevel) {
        if (saveManager == null) return "";
        int safeWanted = Math.max(1, wantedLevel);
        int baseFine = 260;
        int wantedFine = safeWanted * 185;
        int repairCost = saveManager.getRepairCost(vehicleId);
        int total = baseFine + wantedFine + repairCost;
        int paid = saveManager.removeCoinsUpTo(total);
        int shortage = Math.max(0, total - paid);

        String msg = "POLİS CEZASI: -" + paid + " coin";
        msg += " | Wanted: " + safeWanted;
        if (repairCost > 0) msg += " | Tamir masrafı: " + repairCost;
        if (shortage > 0) msg += " | Eksik coin: " + shortage + " - tamir gerekli";
        saveManager.setLastPenaltyAmount(total);
        setMessage(msg);
        return msg;
    }

    public String getHudMessage(String fallback) {
        if (sessionMessage != null && sessionMessage.length() > 0) return sessionMessage;
        String saved = saveManager == null ? "" : saveManager.getEconomyLastMessage();
        if (saved != null && saved.length() > 0) return saved;
        return fallback == null ? "" : fallback;
    }

    public String getSessionMessage() {
        return sessionMessage == null ? "" : sessionMessage;
    }

    private void setMessage(String text) {
        sessionMessage = text == null ? "" : text;
        messageTimer = 5.0f;
        if (saveManager != null) {
            saveManager.setEconomyLastMessage(sessionMessage);
        }
    }
}
