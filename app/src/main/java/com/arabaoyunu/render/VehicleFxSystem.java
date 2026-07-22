package com.arabaoyunu.render;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.mode.GameModeCoordinator;
import com.arabaoyunu.physics.VehicleController;

/**
 * A63.0: Buyuk texture/model kullanmadan drift dumani, lastik izi, nitro alevi
 * ve carpma uyarisi cizen hafif efekt havuzu.
 */
public final class VehicleFxSystem {
    private static final int MAX_SMOKE = 36;
    private static final int MAX_SKID = 44;
    private final Fx[] smoke = new Fx[MAX_SMOKE];
    private final Skid[] skid = new Skid[MAX_SKID];
    private int smokeCursor;
    private int skidCursor;
    private float spawnTimer;
    private float skidTimer;
    private float nitroPulse;
    private float impactFlash;
    private boolean lastNitro;

    public VehicleFxSystem() {
        for (int i = 0; i < MAX_SMOKE; i++) smoke[i] = new Fx();
        for (int i = 0; i < MAX_SKID; i++) skid[i] = new Skid();
    }

    public void reset() {
        for (int i = 0; i < MAX_SMOKE; i++) smoke[i].life = 0f;
        for (int i = 0; i < MAX_SKID; i++) skid[i].life = 0f;
        spawnTimer = 0f;
        skidTimer = 0f;
        nitroPulse = 0f;
        impactFlash = 0f;
        lastNitro = false;
    }

    public void update(float dt, VehicleController car, InputState input, GameModeCoordinator coordinator) {
        if (dt < 0f) dt = 0f;
        if (dt > 0.08f) dt = 0.08f;
        for (int i = 0; i < MAX_SMOKE; i++) {
            Fx f = smoke[i];
            if (f.life > 0f) {
                f.life -= dt;
                f.y += dt * 0.18f;
                f.size += dt * 0.34f;
            }
        }
        for (int i = 0; i < MAX_SKID; i++) {
            Skid s = skid[i];
            if (s.life > 0f) s.life -= dt;
        }
        nitroPulse = Math.max(0f, nitroPulse - dt * 1.55f);
        impactFlash = Math.max(0f, impactFlash - dt * 2.6f);
        if (car == null) return;

        boolean nitro = input != null && input.nitro > 0.35f;
        if (nitro && !lastNitro) nitroPulse = 1f;
        lastNitro = nitro;
        if (car.getImpactFeedback() > 0.68f) impactFlash = Math.max(impactFlash, car.getImpactFeedback());

        float drift = car.getDriftBlend();
        boolean driftMode = coordinator != null && coordinator.isDrift();
        boolean shouldSmoke = (drift > 0.34f || driftMode) && car.getSpeedKmh() > 22f;
        spawnTimer -= dt;
        skidTimer -= dt;
        if (shouldSmoke && spawnTimer <= 0f) {
            spawnSmoke(car, driftMode ? 1.20f : 0.82f + drift);
            spawnTimer = 0.045f + Math.max(0f, 0.08f - drift * 0.045f);
        }
        if (shouldSmoke && skidTimer <= 0f) {
            spawnSkid(car);
            skidTimer = 0.055f;
        }
    }

    private void spawnSmoke(VehicleController car, float power) {
        float yaw = car.getRenderYaw();
        float sin = (float)Math.sin(yaw);
        float cos = (float)Math.cos(yaw);
        float rightX = cos;
        float rightZ = -sin;
        float rearX = car.getRenderX() - sin * 1.22f;
        float rearZ = car.getRenderZ() - cos * 1.22f;
        for (int side = -1; side <= 1; side += 2) {
            Fx f = smoke[smokeCursor++ % MAX_SMOKE];
            f.x = rearX + rightX * side * 0.62f;
            f.z = rearZ + rightZ * side * 0.62f;
            f.y = car.getRenderY() + 0.13f;
            f.life = 0.72f;
            f.size = 0.38f + 0.20f * Math.min(1.3f, power);
        }
    }

    private void spawnSkid(VehicleController car) {
        float yaw = car.getRenderYaw();
        float sin = (float)Math.sin(yaw);
        float cos = (float)Math.cos(yaw);
        float rightX = cos;
        float rightZ = -sin;
        float rearX = car.getRenderX() - sin * 1.32f;
        float rearZ = car.getRenderZ() - cos * 1.32f;
        for (int side = -1; side <= 1; side += 2) {
            Skid s = skid[skidCursor++ % MAX_SKID];
            s.x0 = rearX + rightX * side * 0.56f;
            s.z0 = rearZ + rightZ * side * 0.56f;
            s.x1 = s.x0 - sin * 1.25f;
            s.z1 = s.z0 - cos * 1.25f;
            s.life = 1.95f;
        }
    }

    public void render(PrimitiveRenderer renderer, float[] vp, RenderStats stats, VehicleController car, int neonPreset) {
        if (renderer == null) return;
        if (car != null && neonPreset > 0) {
            float[] c = neonColor(neonPreset);
            renderer.drawFlatRect(vp, car.getRenderX(), car.getRenderY() + 0.026f, car.getRenderZ(), 2.35f, 4.20f, c[0], c[1], c[2], 0.22f + Math.min(0.30f, neonPreset * 0.035f), stats);
            renderer.drawCircle(vp, car.getRenderX(), car.getRenderY() + 0.035f, car.getRenderZ(), 2.25f + neonPreset * 0.10f, stats);
        }
        for (int i = 0; i < MAX_SKID; i++) {
            Skid s = skid[i];
            if (s.life <= 0f) continue;
            float a = clamp(s.life / 1.95f, 0f, 1f);
            renderer.drawLine(vp, s.x0, 0.032f, s.z0, s.x1, 0.032f, s.z1, 0.03f * a, 0.035f * a, 0.04f * a, stats);
        }
        for (int i = 0; i < MAX_SMOKE; i++) {
            Fx f = smoke[i];
            if (f.life <= 0f) continue;
            float a = clamp(f.life / 0.72f, 0f, 1f);
            renderer.drawBillboardDiamond(vp, f.x, f.y, f.z, f.size, f.size * 0.72f, 0.72f, 0.76f, 0.78f, 0.38f * a, stats);
        }
        if (car != null && (nitroPulse > 0f || (impactFlash > 0f))) {
            float yaw = car.getRenderYaw();
            float sin = (float)Math.sin(yaw);
            float cos = (float)Math.cos(yaw);
            if (nitroPulse > 0f) {
                float rearX = car.getRenderX() - sin * 2.02f;
                float rearZ = car.getRenderZ() - cos * 2.02f;
                renderer.drawBillboardDiamond(vp, rearX, car.getRenderY() + 0.24f, rearZ, 0.28f + nitroPulse * 0.42f, 0.55f + nitroPulse * 0.65f, 0.05f, 0.55f, 1.0f, 0.72f, stats);
            }
            if (impactFlash > 0f) {
                renderer.drawCircle(vp, car.getRenderX(), car.getRenderY() + 0.12f, car.getRenderZ(), 2.2f + impactFlash * 2.5f, stats);
            }
        }
    }

    private static float[] neonColor(int preset) {
        switch (preset) {
            case 1: return new float[] {0.05f, 0.55f, 1.0f};
            case 2: return new float[] {1.0f, 0.08f, 0.05f};
            case 3: return new float[] {0.10f, 1.0f, 0.32f};
            case 4: return new float[] {0.62f, 0.26f, 1.0f};
            case 5: return new float[] {1.0f, 0.72f, 0.08f};
            case 6: return new float[] {0.90f, 0.96f, 1.0f};
            default: return new float[] {0.05f, 0.55f, 1.0f};
        }
    }

    private static float clamp(float v, float min, float max) { return Math.max(min, Math.min(max, v)); }

    private static final class Fx { float x, y, z, life, size; }
    private static final class Skid { float x0, z0, x1, z1, life; }
}
