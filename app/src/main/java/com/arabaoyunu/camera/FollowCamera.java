package com.arabaoyunu.camera;

import android.opengl.Matrix;

import com.arabaoyunu.physics.VehicleController;

/**
 * Profesyonel takip kamera sistemi.
 *
 * Kamera modlari:
 * 0 - 3. SAHIS / TAKIP: hiz/drift hissi + geri viteste otomatik arka bakis
 * 1 - UZAK 3. SAHIS: harita/gorev gorusu
 * 2 - KAPUT: kaput ustu kamera
 * 3 - SERBEST: kullanici parmak kaydirmasiyla manuel orbit kamera
 * 4 - IC KAMERA: direksiyon/iceriden surus kamerasi
 * 5 - DRIFT KAMERA: yan acili drift odakli takip kamera
 */
public final class FollowCamera {

    private final float[] eye = new float[] {0f, 3.2f, -6.2f};
    private final float[] target = new float[] {0f, 0.9f, 0f};

    private int mode;
    private float orbitAngle = 3.14159f;
    private float orbitHeight = 2.85f;
    private float reverseBlend;
    private boolean forceSnapNext;
    private float feelShakePhase;
    private float eventShakeBoost;
    private float nitroKickBoost;
    private float throttleKickBoost;
    private float brakeDiveBoost;
    private float handbrakeFocusBoost;
    private float vehicleCameraScale = 1f;

    private static final float CHASE_BASE_DISTANCE = 6.55f;
    private static final float CHASE_MAX_EXTRA_DISTANCE = 1.55f;
    private static final float CHASE_BASE_HEIGHT = 3.18f;
    private static final float CHASE_MAX_EXTRA_HEIGHT = 0.42f;

    public void update(float dt, VehicleController car, boolean braking, boolean nitro, float cameraDragX, float cameraDragY) {
        if (car == null) return;
        if (dt < 0f) dt = 0f;
        if (dt > 0.05f) dt = 0.05f;

        if (mode == 3) {
            reverseBlend = 0f;
            updateOrbit360(dt, car, cameraDragX, cameraDragY);
            return;
        }

        if (mode == 4) {
            reverseBlend = 0f;
            updateCockpit(dt, car);
            return;
        }

        eventShakeBoost = Math.max(0f, eventShakeBoost - dt * 2.8f);
        nitroKickBoost = Math.max(0f, nitroKickBoost - dt * 1.65f);
        throttleKickBoost = Math.max(0f, throttleKickBoost - dt * 2.25f);
        brakeDiveBoost = Math.max(0f, brakeDiveBoost - dt * 2.65f);
        handbrakeFocusBoost = Math.max(0f, handbrakeFocusBoost - dt * 2.15f);

        float speedKmh = car.getSpeedKmh();
        float forwardSpeed = car.getForwardSpeed();
        float feel = clamp(car.getDrivingFeelIntensity(), 0f, 1.35f);
        float driftBlend = clamp(car.getDriftBlend(), 0f, 1f);
        float impact = clamp(car.getImpactFeedback(), 0f, 1f);
        float carX = car.getRenderX();
        float carY = car.getRenderY();
        float carZ = car.getRenderZ();
        float carYaw = car.getRenderYaw();
        feelShakePhase += dt * (5.5f + speedKmh * 0.055f + driftBlend * 3.0f);

        // A61.7: daha sinematik ve daha az sert takip. Hızlanınca kamera
        // az miktarda geriye açılır; hedef noktası ise ani frenlerde zıplamaz.
        float profileScale = clamp(vehicleCameraScale, 0.90f, 1.18f);
        float distance = (CHASE_BASE_DISTANCE + Math.min(CHASE_MAX_EXTRA_DISTANCE, speedKmh * 0.0115f) + feel * 0.22f) * profileScale;
        float height = CHASE_BASE_HEIGHT + Math.min(CHASE_MAX_EXTRA_HEIGHT, speedKmh * 0.0038f) + feel * 0.055f
                + (profileScale - 1f) * 0.72f;
        float lookAhead = (2.28f + Math.min(0.72f, speedKmh * 0.0048f) + feel * 0.18f)
                * clamp(1f + (profileScale - 1f) * 0.32f, 0.96f, 1.08f);

        if (braking) {
            brakeDiveBoost = Math.max(brakeDiveBoost, 0.22f);
            distance -= 0.12f + brakeDiveBoost * 0.12f;
            height += brakeDiveBoost * 0.08f;
            lookAhead -= 0.10f + brakeDiveBoost * 0.09f;
        }

        if (nitro) {
            nitroKickBoost = Math.max(nitroKickBoost, 0.42f);
            distance += 0.38f;
            height += 0.08f;
            lookAhead += 0.25f;
        }
        distance += nitroKickBoost * 0.72f + throttleKickBoost * 0.34f + handbrakeFocusBoost * 0.22f;
        height += throttleKickBoost * 0.035f;
        lookAhead += nitroKickBoost * 0.34f + throttleKickBoost * 0.22f;

        boolean reverseViewEnabled = mode == 0 && forwardSpeed < -0.75f;
        float targetReverseBlend = reverseViewEnabled ? 1f : 0f;
        reverseBlend = lerp(reverseBlend, targetReverseBlend, smoothing(dt, 6.5f));

        if (mode == 1) {
            distance = 9.2f + feel * 0.25f;
            height = 4.8f + feel * 0.12f;
            lookAhead = 2.6f;
        } else if (mode == 2) {
            distance = 2.65f;
            height = 1.55f;
            lookAhead = 4.2f;
        } else if (mode == 5) {
            distance = 6.9f + driftBlend * 0.95f;
            height = 3.35f;
            lookAhead = 3.1f + driftBlend * 0.55f;
        }

        float sin = (float) Math.sin(carYaw);
        float cos = (float) Math.cos(carYaw);

        float rightX = cos;
        float rightZ = -sin;
        float driftSide = clamp(car.getSideSpeed() * (mode == 5 ? 0.085f : 0.055f), -1.05f, 1.05f) * Math.max(driftBlend, mode == 5 ? 0.45f : 0f);
        float shakeAmount = clamp(speedKmh / 240f + impact * 0.62f + driftBlend * 0.14f
                + eventShakeBoost * 0.75f + throttleKickBoost * 0.22f + brakeDiveBoost * 0.18f, 0f, 1.2f) * 0.046f;
        float shakeX = (float) Math.sin(feelShakePhase * 1.7f) * shakeAmount;
        float shakeY = (float) Math.sin(feelShakePhase * 2.3f) * shakeAmount * 0.42f;

        float normalEyeX = carX - sin * distance + rightX * (driftSide * 0.55f + shakeX);
        float normalEyeZ = carZ - cos * distance + rightZ * (driftSide * 0.55f + shakeX);
        float normalTargetX = carX + sin * lookAhead + rightX * (driftSide * 0.34f);
        float normalTargetZ = carZ + cos * lookAhead + rightZ * (driftSide * 0.34f);

        float reverseDistance = Math.max(5.4f, distance * 0.92f);
        float reverseLook = Math.max(5.8f, lookAhead * 2.0f);
        float reverseEyeX = carX + sin * reverseDistance;
        float reverseEyeZ = carZ + cos * reverseDistance;
        float reverseTargetX = carX - sin * reverseLook;
        float reverseTargetZ = carZ - cos * reverseLook;

        float desiredX = lerp(normalEyeX, reverseEyeX, reverseBlend);
        float desiredY = carY + height + shakeY;
        float desiredZ = lerp(normalEyeZ, reverseEyeZ, reverseBlend);

        float targetX = lerp(normalTargetX, reverseTargetX, reverseBlend);
        float targetY = carY + 0.82f;
        float targetZ = lerp(normalTargetZ, reverseTargetZ, reverseBlend);

        if (forceSnapNext) {
            eye[0] = desiredX;
            eye[1] = Math.max(1.15f, desiredY);
            eye[2] = desiredZ;
            target[0] = targetX;
            target[1] = targetY;
            target[2] = targetZ;
            forceSnapNext = false;
            return;
        }

        // A64.7: yüksek hızda kamera biraz daha stabil, driftte ise hedef
        // aracı daha akıcı takip eder. Ani sağ-sol hareketlerde sert savrulma azalır.
        float eyeSharpness = mode == 2 ? 9.2f : clamp(5.8f + speedKmh * 0.0048f - driftBlend * 0.55f, 5.2f, 7.4f);
        float targetSharpness = clamp(8.6f + driftBlend * 0.85f + speedKmh * 0.0022f, 8.2f, 10.4f);
        float eyeSmooth = smoothing(dt, eyeSharpness);
        float targetSmooth = smoothing(dt, targetSharpness);

        eye[0] = lerp(eye[0], desiredX, eyeSmooth);
        eye[1] = lerp(eye[1], Math.max(1.15f, desiredY), eyeSmooth);
        eye[2] = lerp(eye[2], desiredZ, eyeSmooth);

        target[0] = lerp(target[0], targetX, targetSmooth);
        target[1] = lerp(target[1], targetY, targetSmooth);
        target[2] = lerp(target[2], targetZ, targetSmooth);
    }

    private void updateOrbit360(float dt, VehicleController car, float cameraDragX, float cameraDragY) {
        // Otomatik donus yok. Sadece kullanici bos ekrani kaydirinca doner.
        float carX = car.getRenderX();
        float carY = car.getRenderY();
        float carZ = car.getRenderZ();
        orbitAngle -= cameraDragX * 0.0105f;
        orbitHeight = clamp(orbitHeight + cameraDragY * 0.010f, 1.45f, 6.2f);

        float radius = 7.4f * clamp(vehicleCameraScale, 0.92f, 1.18f);
        float desiredX = carX + (float) Math.sin(orbitAngle) * radius;
        float desiredY = carY + orbitHeight;
        float desiredZ = carZ + (float) Math.cos(orbitAngle) * radius;

        float s = smoothing(dt, cameraDragX != 0f || cameraDragY != 0f ? 10.0f : 4.8f);
        eye[0] = lerp(eye[0], desiredX, s);
        eye[1] = lerp(eye[1], desiredY, s);
        eye[2] = lerp(eye[2], desiredZ, s);

        float t = smoothing(dt, 8.5f);
        target[0] = lerp(target[0], carX, t);
        target[1] = lerp(target[1], carY + 0.82f, t);
        target[2] = lerp(target[2], carZ, t);
    }

    private void updateCockpit(float dt, VehicleController car) {
        float carX = car.getRenderX();
        float carY = car.getRenderY();
        float carZ = car.getRenderZ();
        float carYaw = car.getRenderYaw();
        float sin = (float) Math.sin(carYaw);
        float cos = (float) Math.cos(carYaw);
        float rightX = cos;
        float rightZ = -sin;

        // GLB direksiyon merkezine gore yaklasik kabin ici kamera:
        // Steering node yerel konumu yaklasik x=0.38, y=0.85, z=0.05.
        // Kamera direksiyonun biraz gerisinden ve icinden yola bakar.
        float cockpitForward = -0.62f;
        float cockpitRight = 0.38f;
        float cockpitHeight = 0.88f;

        float desiredX = carX + sin * cockpitForward + rightX * cockpitRight;
        float desiredY = carY + cockpitHeight;
        float desiredZ = carZ + cos * cockpitForward + rightZ * cockpitRight;

        float lookAhead = 10.5f;
        float targetX = carX + sin * lookAhead + rightX * cockpitRight * 0.20f;
        float targetY = carY + 0.98f;
        float targetZ = carZ + cos * lookAhead + rightZ * cockpitRight * 0.20f;

        float eyeSmooth = smoothing(dt, 16.0f);
        float targetSmooth = smoothing(dt, 17.0f);

        eye[0] = lerp(eye[0], desiredX, eyeSmooth);
        eye[1] = lerp(eye[1], desiredY, eyeSmooth);
        eye[2] = lerp(eye[2], desiredZ, eyeSmooth);

        target[0] = lerp(target[0], targetX, targetSmooth);
        target[1] = lerp(target[1], targetY, targetSmooth);
        target[2] = lerp(target[2], targetZ, targetSmooth);
    }

    public void setVehicleCameraScale(float scale) {
        vehicleCameraScale = clamp(scale, 0.88f, 1.22f);
    }

    public void forceSnap() {
        forceSnapNext = true;
        reverseBlend = 0f;
        feelShakePhase = 0f;
        eventShakeBoost = 0f;
        nitroKickBoost = 0f;
        throttleKickBoost = 0f;
        brakeDiveBoost = 0f;
        handbrakeFocusBoost = 0f;
        mode = 0;
    }

    public void addImpactShake(float amount) {
        eventShakeBoost = Math.max(eventShakeBoost, clamp(amount, 0f, 1.4f));
    }

    public void addNitroKick(float amount) {
        nitroKickBoost = Math.max(nitroKickBoost, clamp(amount, 0f, 1.2f));
    }

    public void addThrottleKick(float amount) {
        throttleKickBoost = Math.max(throttleKickBoost, clamp(amount, 0f, 1.0f));
    }

    public void addBrakeDive(float amount) {
        brakeDiveBoost = Math.max(brakeDiveBoost, clamp(amount, 0f, 1.0f));
    }

    public void addHandbrakeFocus(float amount) {
        handbrakeFocusBoost = Math.max(handbrakeFocusBoost, clamp(amount, 0f, 1.0f));
    }

    public void nextMode() {
        mode = (mode + 1) % 6;
    }

    public void setMode(int mode) {
        if (mode < 0 || mode > 5) mode = 0;
        this.mode = mode;
        forceSnapNext = true;
    }

    public String getModeName() {
        if (mode == 1) return "UZAK 3P";
        if (mode == 2) return "KAPUT";
        if (mode == 3) return "SERBEST";
        if (mode == 4) return "IC KAMERA";
        if (mode == 5) return "DRIFT";
        return reverseBlend > 0.45f ? "GERI KAM" : "3. SAHIS";
    }

    public void fillViewMatrix(float[] outViewMatrix) {
        Matrix.setLookAtM(outViewMatrix, 0,
                eye[0], eye[1], eye[2],
                target[0], target[1], target[2],
                0f, 1f, 0f);
    }

    private static float smoothing(float dt, float sharpness) {
        if (dt <= 0f) return 0f;
        if (dt > 0.05f) dt = 0.05f;
        return 1f - (float) Math.exp(-sharpness * dt);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
