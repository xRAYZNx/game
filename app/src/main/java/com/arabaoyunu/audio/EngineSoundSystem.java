package com.arabaoyunu.audio;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.mode.GameModeCoordinator;
import com.arabaoyunu.physics.VehicleController;

/**
 * A63.0: Oyun hissi icin olay tabanli ses katmani.
 * Ana motor loop'u GameAudioManager'da kalir; bu sinif nitro baslangici,
 * drift surtunmesi, polis uyarisi ve carpismayi tek yerde oran sinirli tetikler.
 */
public final class EngineSoundSystem {
    private boolean lastNitro;
    private boolean lastDrift;
    private float lastImpactSeen;
    private long lastDriftTickMs;
    private long lastPoliceWarnMs;
    private long lastCheckpointMs;

    public void resetSession() {
        lastNitro = false;
        lastDrift = false;
        lastImpactSeen = 0f;
        lastDriftTickMs = 0L;
        lastPoliceWarnMs = 0L;
        lastCheckpointMs = 0L;
    }

    public void update(GameAudioManager audio, VehicleController car, InputState input, GameModeCoordinator coordinator, boolean checkpointPassed, boolean rewardPopup) {
        if (audio == null || car == null) return;
        long now = System.currentTimeMillis();
        boolean nitro = input != null && input.nitro > 0.35f;
        if (nitro && !lastNitro) {
            audio.playNitroFeedback();
        }
        lastNitro = nitro;

        boolean drift = car.getDriftBlend() > 0.42f && car.getSpeedKmh() > 26f;
        if (drift && (now - lastDriftTickMs > (lastDrift ? 520L : 90L))) {
            audio.playDriftSkidTick();
            lastDriftTickMs = now;
        }
        lastDrift = drift;

        float impact = car.getImpactFeedback();
        if (impact > 0.62f && impact > lastImpactSeen + 0.05f) {
            audio.playImpactFeedback(car.getLastImpactDamage() > 0.075f);
        }
        lastImpactSeen = impact * 0.86f;

        if (coordinator != null && coordinator.isPoliceChase() && now - lastPoliceWarnMs > 1250L) {
            audio.playPoliceWarning(1);
            lastPoliceWarnMs = now;
        }

        if (checkpointPassed && now - lastCheckpointMs > 220L) {
            audio.playCheckpoint();
            lastCheckpointMs = now;
        }
        if (rewardPopup) {
            audio.playReward();
        }
    }
}
