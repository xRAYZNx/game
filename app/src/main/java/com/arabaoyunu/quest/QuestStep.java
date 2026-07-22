package com.arabaoyunu.quest;

/** Tek kariyer görev adımı. */
public final class QuestStep {
    public final int type;
    public final String title;
    public final String objective;
    public final String rewardText;
    public final int xpReward;
    public final int coinReward;
    public final int crateReward;
    public final float targetX;
    public final float targetZ;
    public final int targetWorldType;

    public QuestStep(int type, String title, String objective, String rewardText,
                     int xpReward, int coinReward, int crateReward,
                     float targetX, float targetZ, int targetWorldType) {
        this.type = type;
        this.title = title == null ? "" : title;
        this.objective = objective == null ? "" : objective;
        this.rewardText = rewardText == null ? "" : rewardText;
        this.xpReward = Math.max(0, xpReward);
        this.coinReward = Math.max(0, coinReward);
        this.crateReward = Math.max(0, crateReward);
        this.targetX = targetX;
        this.targetZ = targetZ;
        this.targetWorldType = targetWorldType;
    }
}
