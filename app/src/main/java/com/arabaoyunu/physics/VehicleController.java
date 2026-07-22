package com.arabaoyunu.physics;

import com.arabaoyunu.input.InputState;

public final class VehicleController {

    public static final class Vec3 {
        public float x, y, z;
        public Vec3() { this(0f, 0f, 0f); }
        public Vec3(float x, float y, float z) { set(x, y, z); }
        public void set(float x, float y, float z) { this.x = x; this.y = y; this.z = z; }
        public float lengthXZ() { return (float) Math.sqrt(x * x + z * z); }
    }

    public static final class GroundHit {
        public boolean grounded;
        public float y;
        public float normalX = 0f;
        public float normalY = 1f;
        public float normalZ = 0f;

        // ArabaOyunu_23: zemin hissi. Haritalar bu değerleri ayarlayabilir.
        public float surfaceGrip = 1f;
        public float surfaceDrag = 1f;
        public float surfaceBrake = 1f;
        public float surfaceDriftAssist = 1f;
        public String surfaceName = "ASFALT";

        public void resetSurface() {
            surfaceGrip = 1f;
            surfaceDrag = 1f;
            surfaceBrake = 1f;
            surfaceDriftAssist = 1f;
            surfaceName = "ASFALT";
        }
    }

    public interface GroundProvider {
        boolean sampleGround(float x, float z, GroundHit outHit);
        boolean isInsideBounds(float x, float z);
    }

    public static final class Tuning {
        public float maxForwardSpeed = 43.5f;
        public float maxReverseSpeed = 12f;
        public float acceleration = 15.9f;
        public float nitroAcceleration = 9.15f;
        public float reverseAcceleration = 9f;
        public float brakeDeceleration = 28.6f;
        public float rollingResistance = 1.8f;
        public float airResistance = 0.018f;
        public float wheelBase = 2.65f;
        public float maxSteerAngleDeg = 34f;
        public float lateralGrip = 7.85f;
        public float handbrakeGrip = 2.12f;
        public float minSteerSpeed = 0.65f;
        public float steeringSpeedReduction = 0.021f;
        public float rideHeight = 0.42f;
        public float groundSnapStrength = 13f;
        public float boundaryBounce = 0.28f;
        public float driftMinSpeed = 8f;
        public float driftSlipAngleDeg = 13f;

        // ArabaOyunu_23: sürüş hissi parametreleri.
        public float throttleRiseRate = 4.25f;
        public float throttleFallRate = 7.25f;
        public float brakeRiseRate = 8.20f;
        public float brakeFallRate = 9.75f;
        public float handbrakeRiseRate = 10.25f;
        public float inputFallRate = 8.5f;
        // A64.7: Mobil sürüşte direksiyon ham tek kare hareket etmesin;
        // hızlı ama kontrollü bir tepki eğrisiyle fizik tarafına girsin.
        public float steerInputRiseRate = 9.55f;
        public float steerInputFallRate = 12.15f;
        // A64.7: yüksek hızda istemsiz yan kayma/mini zıplama hissini azaltan
        // stabilite katmanı. Drift/handbrake sırasında otomatik gevşer.
        public float highSpeedStabilityAssist = 0.50f;
        // A67.7 driving feel refinement
        public float lowSpeedSteerAssist = 0.18f;
        public float driftBlendRise = 4.2f;
        public float driftBlendFall = 3.0f;
        public float bodyPitchStrength = 0.18f;
        public float bodyRollStrength = 0.17f;
        public float damageResistance = 1.0f;
    }

    public final Vec3 position = new Vec3();
    public final Vec3 velocity = new Vec3();

    // ArabaOyunu_52: render interpolation için son iki fixed-step fizik durumu saklanır.
    // Render artık cihaz FPS'inde ham fixed-step pozisyonuna kilitlenmez; aradaki kareler yumuşatılır.
    private final Vec3 previousRenderPosition = new Vec3();
    private float previousRenderYaw;
    private float previousRenderPitch;
    private float previousRenderRoll;
    private float renderAlpha = 1f;

    // ArabaOyunu_53: fixed-step interpolation tek basina dusuk/degisken cihaz FPS'inde
    // gozle gorulur mikro dur-kalk uretebiliyordu. Kamera ve GLB cizimi artik
    // ikinci bir gorsel yastik katmanindan okunur; fizik pozisyonu degismez.
    private final Vec3 visualRenderPosition = new Vec3();
    private float visualRenderYaw;
    private float visualRenderPitch;
    private float visualRenderRoll;
    private boolean visualRenderReady;

    public float yaw;
    public float pitch;
    public float roll;

    private final Tuning tuning;
    private final GroundHit groundHit = new GroundHit();

    private boolean grounded;
    private boolean drifting;
    private boolean collisionEvent;
    private float forwardSpeed;
    private float sideSpeed;
    private float currentSteer;
    private float smoothedSteer;
    private float throttleStuckTimer;
    private float accumulator;

    private float smoothedThrottle;
    private float smoothedBrake;
    private float smoothedHandbrake;
    private float smoothedNitro;
    private float driftBlend;
    private float longitudinalLoad;
    private float lateralLoad;
    private float suspensionFeel;
    private float impactFeedback;
    private String currentSurfaceName = "ASFALT";
    private float weatherGripScale = 1f;
    private float weatherDragScale = 1f;
    private int currentFeelPreset = DrivingFeelResponseSystem.SENS_BALANCED;

    // ArabaOyunu_27: hasar sistemi.
    private float health = 1f;
    private float motorDamage;
    private float tireDamage;
    private float glassDamage;
    private float bodyDamage;
    private float lastImpactDamage;
    private boolean damageChanged;

    private static final float LEFT_STEER_RESPONSE_BALANCE = 1.35f;
    private static final float FIXED_STEP = 1f / 60f;
    private static final float MAX_FRAME_TIME = 1f / 20f;
    private static final int MAX_STEPS_PER_FRAME = 5;

    public VehicleController(Tuning tuning) {
        this.tuning = tuning == null ? new Tuning() : tuning;
    }

    public void reset(float x, float y, float z, float yawRadians) {
        position.set(x, y, z);
        velocity.set(0f, 0f, 0f);
        yaw = yawRadians;
        pitch = 0f;
        roll = 0f;
        previousRenderPosition.set(x, y, z);
        previousRenderYaw = yawRadians;
        previousRenderPitch = 0f;
        previousRenderRoll = 0f;
        renderAlpha = 1f;
        visualRenderPosition.set(x, y, z);
        visualRenderYaw = yawRadians;
        visualRenderPitch = 0f;
        visualRenderRoll = 0f;
        visualRenderReady = true;
        grounded = true;
        drifting = false;
        collisionEvent = false;
        forwardSpeed = 0f;
        sideSpeed = 0f;
        currentSteer = 0f;
        smoothedSteer = 0f;
        throttleStuckTimer = 0f;
        accumulator = 0f;
        smoothedThrottle = 0f;
        smoothedBrake = 0f;
        smoothedHandbrake = 0f;
        smoothedNitro = 0f;
        driftBlend = 0f;
        longitudinalLoad = 0f;
        lateralLoad = 0f;
        suspensionFeel = 0f;
        impactFeedback = 0f;
        lastImpactDamage = 0f;
        currentSurfaceName = "ASFALT";
        weatherGripScale = 1f;
        weatherDragScale = 1f;
    }

    public void setWeatherRoadEffect(float gripScale, float dragScale) {
        weatherGripScale = clamp(gripScale, 0.55f, 1.05f);
        weatherDragScale = clamp(dragScale, 0.95f, 1.22f);
    }

    public float getWeatherGripScale() {
        return weatherGripScale;
    }

    public float getWeatherDragScale() {
        return weatherDragScale;
    }

    public void update(float dt, InputState input, GroundProvider groundProvider) {
        if (dt <= 0f) return;
        if (dt > MAX_FRAME_TIME) dt = MAX_FRAME_TIME;

        collisionEvent = false;

        accumulator += dt;
        int steps = 0;
        while (accumulator >= FIXED_STEP && steps < MAX_STEPS_PER_FRAME) {
            fixedStep(FIXED_STEP, input, groundProvider);
            accumulator -= FIXED_STEP;
            steps++;
        }
        if (steps == MAX_STEPS_PER_FRAME) {
            accumulator = 0f;
            renderAlpha = 1f;
        } else {
            renderAlpha = clamp(accumulator / FIXED_STEP, 0f, 1f);
        }

        updateVisualRenderState(dt);
    }

    private void updateVisualRenderState(float dt) {
        float targetX = lerp(previousRenderPosition.x, position.x, renderAlpha);
        float targetY = lerp(previousRenderPosition.y, position.y, renderAlpha);
        float targetZ = lerp(previousRenderPosition.z, position.z, renderAlpha);
        float targetYaw = lerpAngle(previousRenderYaw, yaw, renderAlpha);
        float targetPitch = lerp(previousRenderPitch, pitch, renderAlpha);
        float targetRoll = lerp(previousRenderRoll, roll, renderAlpha);

        float dx = targetX - visualRenderPosition.x;
        float dy = targetY - visualRenderPosition.y;
        float dz = targetZ - visualRenderPosition.z;
        float distSq = dx * dx + dy * dy + dz * dz;
        if (!visualRenderReady || distSq > 9f) {
            visualRenderPosition.set(targetX, targetY, targetZ);
            visualRenderYaw = targetYaw;
            visualRenderPitch = targetPitch;
            visualRenderRoll = targetRoll;
            visualRenderReady = true;
            return;
        }

        float speed = velocity.lengthXZ();
        // A61.7: yüksek hızda görsel pozisyon daha kararlı; düşük hızda hızlı oturur.
        float response = speed > 24f ? 18.0f : 16.0f;
        if (speed < 1.2f) response = 24.0f;
        float smooth = 1f - (float) Math.exp(-Math.max(0f, dt) * response);
        smooth = clamp(smooth, 0.12f, 0.68f);

        visualRenderPosition.x = lerp(visualRenderPosition.x, targetX, smooth);
        visualRenderPosition.y = lerp(visualRenderPosition.y, targetY, smooth);
        visualRenderPosition.z = lerp(visualRenderPosition.z, targetZ, smooth);
        visualRenderYaw = lerpAngle(visualRenderYaw, targetYaw, smooth);
        visualRenderPitch = lerp(visualRenderPitch, targetPitch, smooth);
        visualRenderRoll = lerp(visualRenderRoll, targetRoll, smooth);
    }

    private void capturePreviousRenderState() {
        previousRenderPosition.set(position.x, position.y, position.z);
        previousRenderYaw = yaw;
        previousRenderPitch = pitch;
        previousRenderRoll = roll;
    }

    private void fixedStep(float dt, InputState input, GroundProvider groundProvider) {
        capturePreviousRenderState();
        int feelPreset = input == null ? DrivingFeelResponseSystem.SENS_BALANCED : input.steeringSensitivityPreset;
        currentFeelPreset = Math.max(0, Math.min(4, feelPreset));
        float speedKmhForFeel = velocity.lengthXZ() * 3.6f;
        float rawThrottle = input == null ? 0f : DrivingFeelResponseSystem.throttleCurve(input.throttle, speedKmhForFeel);
        float rawBrake = input == null ? 0f : DrivingFeelResponseSystem.brakeCurve(input.brake, speedKmhForFeel);
        float steer = input == null ? 0f : DrivingFeelResponseSystem.steerCurve(input.steer, feelPreset);
        float rawHandbrake = input == null ? 0f : DrivingFeelResponseSystem.handbrakeCurve(input.handbrake);
        float rawNitro = input == null ? 0f : DrivingFeelResponseSystem.nitroCurve(input.nitro, forwardSpeed, tuning.maxForwardSpeed);

        smoothedThrottle = moveTowards(smoothedThrottle, rawThrottle,
                (rawThrottle > smoothedThrottle ? tuning.throttleRiseRate : tuning.throttleFallRate) * dt);
        smoothedBrake = moveTowards(smoothedBrake, rawBrake,
                (rawBrake > smoothedBrake ? tuning.brakeRiseRate : tuning.brakeFallRate) * dt);
        smoothedHandbrake = moveTowards(smoothedHandbrake, rawHandbrake,
                (rawHandbrake > smoothedHandbrake ? tuning.handbrakeRiseRate : tuning.inputFallRate) * dt);
        smoothedNitro = moveTowards(smoothedNitro, rawNitro,
                (rawNitro > smoothedNitro ? 8.0f : tuning.inputFallRate) * dt);

        float throttle = smoothedThrottle;
        float brake = smoothedBrake;
        float handbrake = smoothedHandbrake;
        float nitro = smoothedNitro;

        // ArabaOyunu_16:
        // Mevcut input isaretinde sol tus fizik tarafinda pozitif steer'e denk geliyor.
        // Kullanici testinde sol donus sagdan yavas kaldigi icin sol tepki net dengelenir.
        if (steer > 0f) steer = clamp(steer * LEFT_STEER_RESPONSE_BALANCE, -1f, 1f);
        // A64.7: çok küçük joystick/parmak gürültüsünü kes, sonra direksiyonu
        // sert zıplatmadan fizik sistemine yedir. Bu, gecikme üretmeden
        // yüksek hızdaki ani savrulmayı ve mikro zikzak hissini azaltır.
        float steerDeadZone = DrivingFeelResponseSystem.steerDeadZone(feelPreset);
        if (Math.abs(steer) < steerDeadZone) steer = 0f;
        float steerRate = (Math.abs(steer) > Math.abs(smoothedSteer)
                ? tuning.steerInputRiseRate
                : tuning.steerInputFallRate)
                * DrivingFeelResponseSystem.steerRateMultiplier(feelPreset, Math.abs(forwardSpeed), tuning.maxForwardSpeed);
        smoothedSteer = moveTowards(smoothedSteer, steer, steerRate * dt);
        currentSteer = smoothedSteer;
        steer = smoothedSteer;

        if (groundHit != null) groundHit.resetSurface();
        grounded = groundProvider != null && groundProvider.sampleGround(position.x, position.z, groundHit);
        if (!grounded) {
            groundHit.resetSurface();
        }
        currentSurfaceName = groundHit.surfaceName == null ? "ASFALT" : groundHit.surfaceName;
        if (weatherGripScale < 0.98f && currentSurfaceName.indexOf("ISLAK") < 0) {
            currentSurfaceName = currentSurfaceName + " ISLAK";
        }

        float sinYaw = (float) Math.sin(yaw);
        float cosYaw = (float) Math.cos(yaw);
        float forwardX = sinYaw;
        float forwardZ = cosYaw;
        float rightX = cosYaw;
        float rightZ = -sinYaw;

        forwardSpeed = velocity.x * forwardX + velocity.z * forwardZ;
        sideSpeed = velocity.x * rightX + velocity.z * rightZ;

        float previousForwardSpeed = forwardSpeed;
        float previousSideSpeed = sideSpeed;

        updateDriftBlend(dt, handbrake, steer);
        applyEngineBrakeNitro(dt, throttle, brake, nitro, forwardX, forwardZ);
        applyDrag(dt);
        applyLateralGrip(dt, rightX, rightZ, handbrake);
        applyHighSpeedStabilityGuard(dt, rightX, rightZ, handbrake);
        applySteering(dt, steer, forwardSpeed);

        clampPlanarSpeed(forwardX, forwardZ, rightX, rightZ);

        longitudinalLoad = lerp(longitudinalLoad,
                clamp((forwardSpeed - previousForwardSpeed) * 0.055f, -0.32f, 0.28f),
                clamp(8.0f * dt, 0f, 1f));
        lateralLoad = lerp(lateralLoad,
                clamp((sideSpeed - previousSideSpeed) * 0.060f + sideSpeed * 0.030f, -0.42f, 0.42f),
                clamp(7.0f * dt, 0f, 1f));
        suspensionFeel = lerp(suspensionFeel,
                clamp(Math.abs(longitudinalLoad) * 0.95f + Math.abs(lateralLoad) * 0.65f + driftBlend * 0.15f, 0f, 1.0f),
                clamp(5.5f * dt, 0f, 1f));

        applyMicroJitterGuard(throttle, brake, handbrake);

        float oldX = position.x;
        float oldZ = position.z;
        position.x += velocity.x * dt;
        position.z += velocity.z * dt;

        if (groundProvider != null && !groundProvider.isInsideBounds(position.x, position.z)) {
            // Arac hizlanirken "tak tak geri atma" etkisinin ana nedeni eski konuma sert donmek olabilir.
            // Bu nedenle gorunmez sinirler artik araci geri firlatmaz; sadece cok disari cikarsa hizi yumusatir.
            float impactSpeed = Math.max(Math.abs(previousForwardSpeed), velocity.lengthXZ());
            applyImpactDamage(impactSpeed);
            velocity.x *= 0.86f;
            velocity.z *= 0.86f;
            impactFeedback = Math.max(impactFeedback, 1.0f);
            collisionEvent = true;
        }

        impactFeedback = Math.max(0f, impactFeedback - dt * 2.8f);

        applyLowSpeedUnstuck(dt, throttle, brake, forwardX, forwardZ);
        snapToGround(dt);
        updateVisualTilt();
        updateDriftState(handbrake > 0.2f);
    }

    private void updateDriftBlend(float dt, float handbrake, float steer) {
        float speed = velocity.lengthXZ();
        float slipAngleDeg = Math.abs((float) Math.toDegrees(Math.atan2(sideSpeed, Math.abs(forwardSpeed) + 0.5f)));
        float surfaceAssist = clamp(groundHit.surfaceDriftAssist, 0.65f, 1.65f);
        float steerIntent = Math.abs(steer);

        float target = 0f;
        if (handbrake > 0.15f && speed > tuning.driftMinSpeed * 0.65f) {
            target = 0.72f + handbrake * 0.28f;
        }
        if (slipAngleDeg > tuning.driftSlipAngleDeg * 0.82f && speed > tuning.driftMinSpeed) {
            target = Math.max(target, clamp((slipAngleDeg - 6f) / 34f, 0f, 1f));
        }
        if (steerIntent > 0.65f && speed > tuning.driftMinSpeed * 1.15f && surfaceAssist > 1.05f) {
            target = Math.max(target, 0.34f * surfaceAssist);
        }
        target = clamp(target * surfaceAssist, 0f, 1f);

        float rate = target > driftBlend ? tuning.driftBlendRise : tuning.driftBlendFall;
        driftBlend = moveTowards(driftBlend, target, rate * dt);
    }

    private void applyEngineBrakeNitro(float dt, float throttle, float brake, float nitro, float forwardX, float forwardZ) {
        float speed01 = clamp(Math.abs(forwardSpeed) / Math.max(1f, tuning.maxForwardSpeed), 0f, 1f);
        float lowSpeedPunch = 1.0f + (1f - speed01) * 0.18f;
        float highSpeedFade = 1.0f - speed01 * speed01 * 0.52f;
        float surfaceGrip = clamp(groundHit.surfaceGrip * weatherGripScale, 0.28f, 1.22f);
        float torqueCurve = clamp(lowSpeedPunch * highSpeedFade, 0.42f, 1.18f);

        float motorFactor = getMotorPerformanceFactor();
        float tireFactor = getTireGripFactor();

        float accel = 0f;
        if (throttle > 0f) {
            float launchBite = 1.0f + (1f - speed01) * 0.075f;
            accel += throttle * tuning.acceleration * torqueCurve * launchBite * (0.78f + surfaceGrip * 0.22f) * motorFactor;
        }
        if (nitro > 0f && forwardSpeed > 3f) {
            accel += nitro * tuning.nitroAcceleration * (0.85f + 0.15f * surfaceGrip) * motorFactor;
        }

        if (brake > 0f) {
            float brakeGrip = clamp(groundHit.surfaceBrake * weatherGripScale, 0.34f, 1.18f) * tireFactor;
            float brakeBite = 1.0f + brake * 0.10f;
            if (forwardSpeed > 1f) accel -= brake * tuning.brakeDeceleration * brakeGrip * brakeBite;
            else accel -= brake * tuning.reverseAcceleration * (0.80f + brakeGrip * 0.20f) * motorFactor;
        }

        velocity.x += forwardX * accel * dt;
        velocity.z += forwardZ * accel * dt;
    }

    private void applyDrag(float dt) {
        float speed = velocity.lengthXZ();
        if (speed < 0.001f) return;
        float surfaceDrag = clamp(groundHit.surfaceDrag * weatherDragScale, 0.75f, 1.85f);
        float damageDrag = 1f + bodyDamage * 0.38f + tireDamage * 0.22f;
        float drag = tuning.rollingResistance * surfaceDrag * damageDrag + tuning.airResistance * speed * speed * (1f + bodyDamage * 0.22f);
        float amount = Math.min(speed, drag * dt);
        velocity.x -= (velocity.x / speed) * amount;
        velocity.z -= (velocity.z / speed) * amount;
    }

    private void applyLateralGrip(float dt, float rightX, float rightZ, float handbrake) {
        float surfaceGrip = clamp(groundHit.surfaceGrip * weatherGripScale, 0.28f, 1.25f);
        float tireFactor = getTireGripFactor();
        float baseGrip = tuning.lateralGrip * surfaceGrip * tireFactor;
        float slideGrip = tuning.handbrakeGrip * surfaceGrip * 0.82f * tireFactor;
        float grip = lerp(baseGrip, slideGrip, driftBlend);
        if (handbrake > 0.2f) grip = Math.min(grip, slideGrip);

        float damp = clamp(grip * dt, 0f, 1f);
        float newSideSpeed = sideSpeed * (1f - damp);
        float deltaSide = newSideSpeed - sideSpeed;
        velocity.x += rightX * deltaSide;
        velocity.z += rightZ * deltaSide;
    }

    private void applyHighSpeedStabilityGuard(float dt, float rightX, float rightZ, float handbrake) {
        // A64.7: Performans/tuning yükseltmeleri test sürüşünde daha güçlü olunca
        // yüksek hızda istemsiz yan hız birikimi kamera ve araçta titreme hissi
        // oluşturabiliyordu. Bu katman drift veya el freni sırasında kendini
        // geri çeker; düz hızlı sürüşte aracı ray gibi yapmadan toparlar.
        float speed = velocity.lengthXZ();
        if (!grounded || speed < 18f || tuning.highSpeedStabilityAssist <= 0f) return;
        float stability01 = clamp((speed - 18f) / 34f, 0f, 1f);
        float driftRelease = 1f - clamp(driftBlend * 0.78f + handbrake * 0.72f, 0f, 0.92f);
        float presetAssist = DrivingFeelResponseSystem.highSpeedStabilityMultiplier(currentFeelPreset);
        float damp = clamp(tuning.highSpeedStabilityAssist * presetAssist * stability01 * driftRelease * dt, 0f, 0.22f);
        if (damp <= 0f) return;

        float currentSide = velocity.x * rightX + velocity.z * rightZ;
        float deltaSide = -currentSide * damp;
        velocity.x += rightX * deltaSide;
        velocity.z += rightZ * deltaSide;
    }

    private void applySteering(float dt, float steer, float speedForYaw) {
        float absSpeed = Math.abs(speedForYaw);
        if (absSpeed < tuning.minSteerSpeed) return;
        float surfaceGrip = clamp(groundHit.surfaceGrip * weatherGripScale, 0.32f, 1.15f);
        float tireFactor = getTireGripFactor();
        float speedReduction = 1f / (1f + absSpeed * absSpeed * tuning.steeringSpeedReduction);
        float driftSteerHelp = 1f + driftBlend * 0.20f;
        float steerAngleRad = degToRad(tuning.maxSteerAngleDeg) * steer * speedReduction * (0.82f + surfaceGrip * 0.18f) * driftSteerHelp * tireFactor;
        float yawRate = (speedForYaw / tuning.wheelBase) * (float) Math.tan(steerAngleRad);
        yaw = normalizeAngle(yaw + yawRate * dt);
    }

    private void clampPlanarSpeed(float forwardX, float forwardZ, float rightX, float rightZ) {
        forwardSpeed = velocity.x * forwardX + velocity.z * forwardZ;
        sideSpeed = velocity.x * rightX + velocity.z * rightZ;

        float damageSpeedFactor = getMotorPerformanceFactor() * (0.72f + health * 0.28f);
        float maxForward = tuning.maxForwardSpeed * damageSpeedFactor;
        float maxReverse = tuning.maxReverseSpeed * Math.max(0.70f, damageSpeedFactor);

        if (forwardSpeed > maxForward) forwardSpeed = maxForward;
        if (forwardSpeed < -maxReverse) forwardSpeed = -maxReverse;

        velocity.x = forwardX * forwardSpeed + rightX * sideSpeed;
        velocity.z = forwardZ * forwardSpeed + rightZ * sideSpeed;
    }



    private void applyMicroJitterGuard(float throttle, float brake, float handbrake) {
        // ArabaOyunu_22:
        // Cozumlu referans dosyadaki fizik korunur; ek olarak cok kucuk hizlarda
        // titreyen artik velocity sifirlanir. Bu normal surusu etkilemez, sadece
        // arac neredeyse dururken gelen mikroskobik ileri/geri salinimi keser.
        float planarSpeed = velocity.lengthXZ();
        boolean noDriverForce = throttle < 0.04f && brake < 0.04f && handbrake < 0.04f;
        if (grounded && noDriverForce && planarSpeed < 0.055f) {
            velocity.x = 0f;
            velocity.z = 0f;
            forwardSpeed = 0f;
            sideSpeed = 0f;
            currentSteer = 0f;
        }
    }

    private void applyLowSpeedUnstuck(float dt, float throttle, float brake, float forwardX, float forwardZ) {
        float planarSpeed = velocity.lengthXZ();
        // Sadece gercekten durmaya yakin ve uzun sure gaz verilmisken calisir.
        // Hizlanirken kesinlikle impulse vermez; bu sayede yolda geri atma/tikleme yapmaz.
        if (throttle > 0.88f && brake < 0.05f && grounded && planarSpeed < 0.055f) {
            throttleStuckTimer += dt;
            if (throttleStuckTimer > 1.15f) {
                // ArabaOyunu_52: Tek karelik sert impulse yerine çok küçük ve nadir destek.
                // Sürüşte tık/tık hissi oluşturmaması için sadece gerçekten sıkışmış araçta çalışır.
                velocity.x += forwardX * 0.16f;
                velocity.z += forwardZ * 0.16f;
                throttleStuckTimer = 0.72f;
            }
        } else {
            throttleStuckTimer = 0f;
        }
    }

    private void snapToGround(float dt) {
        if (!grounded) return;
        float targetY = groundHit.y + tuning.rideHeight;

        // Duz zeminde kucuk farklari sonsuz lerp ile tasimak gorselde "tik tik" titresim yapabilir.
        // Esik altinda direkt hedefe kilitlenir, buyuk farklarda yumusak gecis korunur.
        if (Math.abs(position.y - targetY) < 0.05f) {
            position.y = targetY;
        } else {
            position.y = lerp(position.y, targetY, clamp(6.0f * dt, 0f, 1f));
        }

        pitch = clamp(-groundHit.normalZ * 0.18f, -0.25f, 0.25f);
        roll = clamp(groundHit.normalX * 0.18f, -0.25f, 0.25f);
    }

    private void updateVisualTilt() {
        // ArabaOyunu_23:
        // Görsel ağırlık: gazda hafif arkaya, frende öne, yan yükte sağ/sol gövde yatışı.
        pitch += clamp(-longitudinalLoad * tuning.bodyPitchStrength - suspensionFeel * 0.025f, -0.16f, 0.14f);
        roll += clamp(-sideSpeed * 0.014f - lateralLoad * tuning.bodyRollStrength, -0.20f, 0.20f);
    }

    private void updateDriftState(boolean handbrake) {
        float speed = velocity.lengthXZ();
        float slipAngle = Math.abs((float) Math.atan2(sideSpeed, Math.abs(forwardSpeed) + 0.5f));
        float threshold = degToRad(tuning.driftSlipAngleDeg) * (1f - clamp(driftBlend, 0f, 1f) * 0.18f);
        drifting = speed >= tuning.driftMinSpeed && slipAngle >= threshold;
        if (handbrake && speed >= tuning.driftMinSpeed * 0.70f) drifting = true;
        if (driftBlend > 0.55f && speed >= tuning.driftMinSpeed * 0.80f) drifting = true;
    }

    public float getSpeedKmh() { return velocity.lengthXZ() * 3.6f; }
    public float getForwardSpeed() { return forwardSpeed; }
    public float getSideSpeed() { return sideSpeed; }
    public float getSteerInput() { return currentSteer; }
    public float getSmoothedThrottle() { return smoothedThrottle; }
    public float getSmoothedBrake() { return smoothedBrake; }
    public float getSmoothedHandbrake() { return smoothedHandbrake; }
    public float getSmoothedNitro() { return smoothedNitro; }
    public String getDrivingFeelProfileName(int preset) { return DrivingFeelResponseSystem.profileName(preset); }
    public int getCurrentFeelPreset() { return currentFeelPreset; }

    public float getRenderAlpha() { return renderAlpha; }
    public float getRenderX() { return visualRenderPosition.x; }
    public float getRenderY() { return visualRenderPosition.y; }
    public float getRenderZ() { return visualRenderPosition.z; }
    public float getRenderYaw() { return visualRenderYaw; }
    public float getRenderPitch() { return visualRenderPitch; }
    public float getRenderRoll() { return visualRenderRoll; }
    public float getDriftBlend() { return driftBlend; }
    public float getLongitudinalLoad() { return longitudinalLoad; }
    public float getLateralLoad() { return lateralLoad; }
    public float getSuspensionFeel() { return suspensionFeel; }
    public float getImpactFeedback() { return impactFeedback; }
    public String getCurrentSurfaceName() { return currentSurfaceName; }
    public float getDrivingFeelIntensity() {
        return clamp(velocity.lengthXZ() / 42f + Math.abs(lateralLoad) * 0.35f + driftBlend * 0.22f + impactFeedback * 0.25f, 0f, 1.35f);
    }
    public float getSlipAngleDeg() {
        return (float) Math.toDegrees(Math.atan2(sideSpeed, Math.abs(forwardSpeed) + 0.5f));
    }
    public boolean isGrounded() { return grounded; }
    public boolean isDrifting() { return drifting; }
    public Tuning getTuning() { return tuning; }

    /**
     * Drift modundaki carpmalari tek frame icinde cezalandirmak icin kullanilir.
     * update() basinda otomatik temizlenir; mod sistemi update sonrasi okuyabilir.
     */

    public void applyExternalImpact(float impactSpeed) {
        applyImpactDamage(impactSpeed);
        impactFeedback = Math.max(impactFeedback, 1.0f);
        collisionEvent = true;
    }

    private void applyImpactDamage(float impactSpeed) {
        if (impactSpeed < 7.5f) return;
        float normalized = clamp((impactSpeed - 7.5f) / 30f, 0f, 1.25f);
        float amount = (0.035f + normalized * 0.115f) / clamp(tuning.damageResistance, 0.60f, 1.60f);
        health = clamp(health - amount, 0f, 1f);
        bodyDamage = clamp(bodyDamage + amount * 0.85f, 0f, 1f);
        motorDamage = clamp(motorDamage + amount * (impactSpeed > 16f ? 0.44f : 0.18f), 0f, 1f);
        tireDamage = clamp(tireDamage + amount * (impactSpeed > 20f ? 0.38f : 0.14f), 0f, 1f);
        glassDamage = clamp(glassDamage + amount * (impactSpeed > 14f ? 0.52f : 0.18f), 0f, 1f);
        lastImpactDamage = amount;
        damageChanged = true;
    }

    public void repairFull() {
        health = 1f;
        motorDamage = 0f;
        tireDamage = 0f;
        glassDamage = 0f;
        bodyDamage = 0f;
        lastImpactDamage = 0f;
        damageChanged = true;
    }

    public void loadDamageState(float health, float motor, float tire, float glass, float body) {
        this.health = clamp(health, 0f, 1f);
        this.motorDamage = clamp(motor, 0f, 1f);
        this.tireDamage = clamp(tire, 0f, 1f);
        this.glassDamage = clamp(glass, 0f, 1f);
        this.bodyDamage = clamp(body, 0f, 1f);
        this.lastImpactDamage = 0f;
        this.damageChanged = false;
    }

    public boolean consumeDamageChangedEvent() {
        boolean changed = damageChanged;
        damageChanged = false;
        return changed;
    }

    public float getHealth01() { return health; }
    public float getMotorDamage01() { return motorDamage; }
    public float getTireDamage01() { return tireDamage; }
    public float getGlassDamage01() { return glassDamage; }
    public float getBodyDamage01() { return bodyDamage; }
    public float getLastImpactDamage() { return lastImpactDamage; }

    public float getMotorPerformanceFactor() {
        return clamp(1f - motorDamage * 0.58f - (1f - health) * 0.22f, 0.28f, 1f);
    }

    public float getTireGripFactor() {
        return clamp(1f - tireDamage * 0.48f - (1f - health) * 0.12f, 0.42f, 1f);
    }

    public boolean consumeCollisionEvent() {
        boolean happened = collisionEvent;
        collisionEvent = false;
        return happened;
    }

    private static float clamp(float value, float min, float max) { return Math.max(min, Math.min(max, value)); }
    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }
    private static float lerpAngle(float a, float b, float t) {
        float delta = b - a;
        while (delta > Math.PI) delta -= (float) (Math.PI * 2.0);
        while (delta < -Math.PI) delta += (float) (Math.PI * 2.0);
        return a + delta * t;
    }
    private static float moveTowards(float current, float target, float maxDelta) {
        if (current < target) return Math.min(target, current + Math.max(0f, maxDelta));
        if (current > target) return Math.max(target, current - Math.max(0f, maxDelta));
        return target;
    }
    private static float degToRad(float deg) { return deg * 0.017453292f; }
    private static float normalizeAngle(float angle) {
        while (angle > Math.PI) angle -= Math.PI * 2f;
        while (angle < -Math.PI) angle += Math.PI * 2f;
        return angle;
    }
}
