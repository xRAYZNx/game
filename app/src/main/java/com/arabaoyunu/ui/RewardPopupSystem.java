package com.arabaoyunu.ui;

/** A63.0: Coin/XP/seviye/mod acildi bildirimlerini tek yerde oran sinirli tutar. */
public final class RewardPopupSystem {
    private String title = "";
    private String subtitle = "";
    private float timer;
    private int lastCoins;
    private int lastXp;
    private int lastLevel;

    public void reset() {
        title = "";
        subtitle = "";
        timer = 0f;
        lastCoins = 0;
        lastXp = 0;
        lastLevel = 0;
    }

    public void prime(int coins, int xp, int level) {
        lastCoins = Math.max(0, coins);
        lastXp = Math.max(0, xp);
        lastLevel = Math.max(1, level);
        title = "";
        subtitle = "";
        timer = 0f;
    }

    public void update(float dt) {
        if (dt < 0f) dt = 0f;
        if (dt > 0.1f) dt = 0.1f;
        if (timer > 0f) timer -= dt;
    }

    public boolean push(String title, String subtitle) {
        if (title == null || title.length() == 0) return false;
        this.title = title;
        this.subtitle = subtitle == null ? "" : subtitle;
        this.timer = 2.2f;
        return true;
    }

    public boolean watchEconomy(int coins, int xp, int level, String message) {
        boolean fired = false;
        if (lastCoins > 0 && coins > lastCoins) {
            push("+" + (coins - lastCoins) + " COIN", trim(message, "Garaj ekonomisi güncellendi"));
            fired = true;
        }
        if (!fired && lastXp >= 0 && xp > lastXp) {
            push("+" + (xp - lastXp) + " XP", trim(message, "Kariyer ilerledi"));
            fired = true;
        }
        if (!fired && lastLevel > 0 && level > lastLevel) {
            push("SEVIYE ATLADIN!", "Yeni seviye: " + level);
            fired = true;
        }
        lastCoins = Math.max(0, coins);
        lastXp = Math.max(0, xp);
        lastLevel = Math.max(1, level);
        return fired;
    }

    private static String trim(String value, String fallback) {
        if (value == null || value.length() == 0) return fallback;
        return value.length() <= 42 ? value : value.substring(0, 42);
    }

    public boolean isVisible() { return timer > 0f && title.length() > 0; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public float getAlpha01() { return Math.max(0f, Math.min(1f, timer / 0.35f)); }
    public float getSlide01() { return Math.max(0f, Math.min(1f, timer / 2.2f)); }
}
