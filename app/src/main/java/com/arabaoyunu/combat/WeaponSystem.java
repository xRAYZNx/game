package com.arabaoyunu.combat;

/**
 * Silah durum makinesi: ates hizi sinirlama, sarjor, geri tepme (recoil)
 * ve cift ates girislerinin (sol sabit buton / sag suruklemeli buton)
 * tek bir ates kararina indirgenmesi.
 *
 * Thread-guvenlidir: dokunma thread'i fire isteklerini iletir, oyun
 * thread'i update() ile sarjor/recoil durumunu ilerletir.
 */
public final class WeaponSystem {

    public static final int FIRE_MODE_AUTO = 0;
    public static final int FIRE_MODE_BURST = 1;
    public static final int FIRE_MODE_SINGLE = 2;

    /** Tek atislik gerceklesen ates olayi. */
    public static final class Shot {
        public final long timestampMs;
        public final float recoilPitch;
        public final float recoilYaw;
        public final int ammoAfter;

        Shot(long timestampMs, float recoilPitch, float recoilYaw, int ammoAfter) {
            this.timestampMs = timestampMs;
            this.recoilPitch = recoilPitch;
            this.recoilYaw = recoilYaw;
            this.ammoAfter = ammoAfter;
        }
    }

    private final int magazineSize;
    private final long fireIntervalMs;
    private final float baseDamage;
    private final float recoilPitchPerShot;
    private final float recoilYawMax;
    private final float recoilRecoveryPerSec;

    private int ammo;
    private long lastShotMs = Long.MIN_VALUE / 2;
    private boolean triggerHeld;
    private boolean singleShotLatch; // tekli modda bir basim = bir kursun
    private int burstRemaining;
    private float recoilPitch;
    private float recoilYaw;
    private long seed = 0x2F6E2B1L;

    private int fireMode = FIRE_MODE_AUTO;

    public WeaponSystem(int magazineSize, float roundsPerSecond, float baseDamage,
                        float recoilPitchPerShot, float recoilYawMax,
                        float recoilRecoveryPerSec) {
        this.magazineSize = Math.max(1, magazineSize);
        this.fireIntervalMs = Math.max(20L, (long) (1000f / Math.max(0.5f, roundsPerSecond)));
        this.baseDamage = Math.max(1f, baseDamage);
        this.recoilPitchPerShot = Math.max(0f, recoilPitchPerShot);
        this.recoilYawMax = Math.max(0f, recoilYawMax);
        this.recoilRecoveryPerSec = Math.max(0.5f, recoilRecoveryPerSec);
        this.ammo = this.magazineSize;
    }

    /** Varsayilan tufek profili. */
    public static WeaponSystem createDefaultRifle() {
        return new WeaponSystem(30, 9.5f, 26f, 0.85f, 0.45f, 6.0f);
    }

    public synchronized void setFireMode(int mode) {
        if (mode < FIRE_MODE_AUTO || mode > FIRE_MODE_SINGLE) return;
        fireMode = mode;
        burstRemaining = 0;
        singleShotLatch = false;
    }

    public synchronized int getFireMode() {
        return fireMode;
    }

    /** Tetik basili/birakildi bilgisi (sol sabit veya sag suruklemeli buton). */
    public synchronized void setTriggerHeld(boolean held) {
        if (held && !triggerHeld) {
            // Yeni basim: tekli modda kapiyi ac, burst modda seriyi kur.
            singleShotLatch = false;
            if (fireMode == FIRE_MODE_BURST) burstRemaining = 3;
        }
        triggerHeld = held;
    }

    public synchronized boolean isTriggerHeld() {
        return triggerHeld;
    }

    /**
     * Ates etmeye uygunsa bir Shot uretir; degilse null dondurur.
     * Oyun thread'i her kare cagirabilir; ates hizi burada sinirlanir.
     */
    public synchronized Shot tryFire(long nowMs, boolean aimAllowed, boolean fireAllowed) {
        if (!triggerHeld || !fireAllowed) return null;
        if (!aimAllowed) return null;
        if (ammo <= 0) return null;
        if (nowMs - lastShotMs < fireIntervalMs) return null;

        if (fireMode == FIRE_MODE_SINGLE) {
            if (singleShotLatch) return null;
            singleShotLatch = true;
        } else if (fireMode == FIRE_MODE_BURST) {
            if (burstRemaining <= 0) return null;
            burstRemaining--;
        }

        lastShotMs = nowMs;
        ammo--;

        // Deterministik pseudo-random yaw (agirlik merkezi ortada).
        seed = seed * 6364136223846793005L + 1442695040888963407L;
        float unit = ((seed >>> 11) & 0xFFFF) / 65535f; // 0..1
        float yawKick = (unit * 2f - 1f) * recoilYawMax;
        recoilPitch += recoilPitchPerShot;
        recoilYaw += yawKick;
        return new Shot(nowMs, yawKick == 0f ? recoilPitchPerShot : recoilPitchPerShot, yawKick, ammo);
    }

    /** Geri tepmenin zamanla toparlanmasi. */
    public synchronized void update(float dtSeconds) {
        if (dtSeconds <= 0f) return;
        float recover = recoilRecoveryPerSec * dtSeconds;
        recoilPitch = approachZero(recoilPitch, recover);
        recoilYaw = approachZero(recoilYaw, recover * 1.4f);
    }

    private static float approachZero(float v, float step) {
        if (v > 0f) return Math.max(0f, v - step);
        if (v < 0f) return Math.min(0f, v + step);
        return 0f;
    }

    /** Sarjoru doldurur; doldurma suresi dis sistemde yonetilir. */
    public synchronized void reload() {
        ammo = magazineSize;
        burstRemaining = 0;
        singleShotLatch = false;
    }

    public synchronized int getAmmo() {
        return ammo;
    }

    public synchronized int getMagazineSize() {
        return magazineSize;
    }

    public synchronized float getBaseDamage() {
        return baseDamage;
    }

    public synchronized float getRecoilPitch() {
        return recoilPitch;
    }

    public synchronized float getRecoilYaw() {
        return recoilYaw;
    }
}
