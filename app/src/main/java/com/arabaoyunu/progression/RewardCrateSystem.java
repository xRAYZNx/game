package com.arabaoyunu.progression;

import com.arabaoyunu.util.SaveManager;

/**
 * ArabaOyunu_40: Ödül kasası sistemi.
 *
 * Kasa açınca:
 * - Coin verir
 * - XP verir
 * - Bazı seviyelerde araç kilidi açılmasına yardımcı olur
 */
public final class RewardCrateSystem {

    private final SaveManager saveManager;
    private int lastCoins;
    private int lastXp;
    private String lastMessage = "";

    public RewardCrateSystem(SaveManager saveManager) {
        this.saveManager = saveManager;
    }

    public boolean openCrate(int playerLevel) {
        if (saveManager == null || saveManager.getRewardCrates() <= 0) return false;
        saveManager.setRewardCrates(saveManager.getRewardCrates() - 1);

        int crateIndex = saveManager.getOpenedCrates() + 1;
        saveManager.setOpenedCrates(crateIndex);

        lastCoins = 450 + playerLevel * 75 + (crateIndex % 4) * 120;
        lastXp = 110 + playerLevel * 12;
        saveManager.addCoins(lastCoins);
        saveManager.addXp(lastXp);
        lastMessage = "KASA: +" + lastCoins + " coin +" + lastXp + " XP";
        return true;
    }

    public int getLastCoins() { return lastCoins; }
    public int getLastXp() { return lastXp; }
    public String getLastMessage() { return lastMessage; }
}
