package com.arabaoyunu.world;

/** Açık dünya fiziksel nokta tanımı. */
public final class WorldPoint {
    public int type;
    public float x;
    public float z;
    public float radius;
    public int requiredLevel;
    public String title;
    public String subtitle;
    public String reward;

    public void set(int type, float x, float z, float radius, int requiredLevel, String title, String subtitle, String reward) {
        this.type = type;
        this.x = x;
        this.z = z;
        this.radius = radius;
        this.requiredLevel = Math.max(1, requiredLevel);
        this.title = title == null ? WorldPointType.label(type) : title;
        this.subtitle = subtitle == null ? "" : subtitle;
        this.reward = reward == null ? "" : reward;
    }
}
