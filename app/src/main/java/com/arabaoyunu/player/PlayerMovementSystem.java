package com.arabaoyunu.player;

/**
 * Karakter hareket sistemi: egilme (lean/peek), atlama (jump) ve
 * kayma (slide) mekanikleri.
 *
 * Kurallar:
 * - Egilme: sol/sag -1..+1 arasi yumusatilmis deger; kamerayi ve govdeyi
 *   yan yatirmak icin kullanilir (peek).
 * - Atlama: sadece yere basiyorken tetiklenir; yer cekimi ile duser,
 *   yere inince "landing" bayragi bir kareligine set edilir.
 * - Kayma: kosarken tetiklenir; sabit sure boyunca surtunmeyle yavaslar,
 *   kayma bitince otomatik olarak ayaga kalkar. Kayma sirasinda nisan
 *   alma ve ates etme serbesttir (canAim / canFire true kalir).
 *
 * Thread-guvenlidir; input thread'i yazar, oyun thread'i update() cagirir.
 */
public final class PlayerMovementSystem {

    public static final int STANCE_STANDING = 0;
    public static final int STANCE_SLIDING = 1;

    private static final float GRAVITY = 22f;             // m/s^2 (oyun hissi icin hafif sisme)
    private static final float JUMP_VELOCITY = 7.2f;      // m/s
    private static final float SLIDE_DURATION = 0.85f;    // s
    private static final float SLIDE_MIN_SPEED = 3.4f;    // kayma baslatabilmek icin min hiz
    private static final float SLIDE_FRICTION = 6.5f;     // kayma yavaslama ivmesi
    private static final float LEAN_DAMP_TAU = 0.070f;    // egilme yumusatma zaman sabiti

    private int stance = STANCE_STANDING;
    private int leanInput;             // -1 sol, 0 yok, +1 sag
    private float leanAmount;          // yumusatilmis -1..+1

    private boolean grounded = true;
    private float verticalVelocity;
    private float heightOffset;        // ziplama yuksekligi (m)

    private boolean sliding;
    private float slideTimer;
    private float slideSpeed;
    private float moveSpeed = 5.4f;    // normal kosu hizi (m/s)

    // Tek karelik olay bayraklari (update sonrasi tuketilir).
    private boolean jumpStartedEvent;
    private boolean landedEvent;
    private boolean slideStartedEvent;
    private boolean slideEndedEvent;

    /** Sag/sol egilme girisini ayarlar. -1 sol, +1 sag, 0 birak. */
    public synchronized void setLeanInput(int direction) {
        if (direction < -1) direction = -1;
        if (direction > 1) direction = 1;
        this.leanInput = direction;
    }

    /** Yurume/kosu hizini disaridan gunceller (slide tetikleme kosulu icin). */
    public synchronized void setMoveSpeed(float speedMetersPerSecond) {
        this.moveSpeed = Math.max(0f, speedMetersPerSecond);
    }

    /** Atlama istegi. Sadece yere basiyorken ve kaymiyorken kabul edilir. */
    public synchronized boolean requestJump() {
        if (!grounded || sliding) return false;
        grounded = false;
        verticalVelocity = JUMP_VELOCITY;
        jumpStartedEvent = true;
        return true;
    }

    /**
     * Kayma istegi. Yeterli hizda ve yere basiyorken baslar.
     * Kayma suresi dolunca update() icinde otomatik ayaga kalkilir.
     */
    public synchronized boolean requestSlide() {
        if (sliding || !grounded) return false;
        if (moveSpeed < SLIDE_MIN_SPEED) return false;
        sliding = true;
        stance = STANCE_SLIDING;
        slideTimer = SLIDE_DURATION;
        slideSpeed = moveSpeed * 1.18f; // kayma aninda kisa hiz kazanci
        slideStartedEvent = true;
        return true;
    }

    /** Kaymayi erken bitirir (or. ziplama girisimi veya engel). */
    public synchronized void cancelSlide() {
        if (!sliding) return;
        endSlide();
    }

    /**
     * Fizik adimini ilerletir.
     * @param dtSeconds kare suresi (s)
     * @return kayma sirasindaki anlik yatay hiz (kayma yoksa moveSpeed)
     */
    public synchronized float update(float dtSeconds) {
        if (dtSeconds < 0f) dtSeconds = 0f;
        if (dtSeconds > 0.1f) dtSeconds = 0.1f;

        // Egilme yumusatma (kare hizindan bagimsiz ustel sondurme).
        float leanTarget = leanInput;
        float k = (float) Math.exp(-dtSeconds / LEAN_DAMP_TAU);
        leanAmount = leanTarget + (leanAmount - leanTarget) * k;
        if (Math.abs(leanAmount - leanTarget) < 0.002f) leanAmount = leanTarget;

        // Atlama / dusme entegrasyonu.
        if (!grounded) {
            verticalVelocity -= GRAVITY * dtSeconds;
            heightOffset += verticalVelocity * dtSeconds;
            if (heightOffset <= 0f) {
                heightOffset = 0f;
                verticalVelocity = 0f;
                grounded = true;
                landedEvent = true;
            }
        }

        // Kayma ilerlemesi: surtunme ile yavasla, sure bitince kalk.
        float currentSpeed = moveSpeed;
        if (sliding) {
            slideTimer -= dtSeconds;
            slideSpeed = Math.max(0f, slideSpeed - SLIDE_FRICTION * dtSeconds);
            currentSpeed = slideSpeed;
            if (slideTimer <= 0f || slideSpeed < 0.6f) {
                endSlide(); // otomatik ayaga kalkma
            }
        }
        return currentSpeed;
    }

    private void endSlide() {
        sliding = false;
        stance = STANCE_STANDING;
        slideTimer = 0f;
        slideSpeed = 0f;
        slideEndedEvent = true;
    }

    public synchronized int getStance() {
        return stance;
    }

    public synchronized boolean isSliding() {
        return sliding;
    }

    public synchronized boolean isGrounded() {
        return grounded;
    }

    public synchronized float getHeightOffset() {
        return heightOffset;
    }

    /** Yumusatilmis egilme: -1 tam sol, +1 tam sag. */
    public synchronized float getLeanAmount() {
        return leanAmount;
    }

    /** Kayma sirasinda da nisan alinabilir. */
    public synchronized boolean canAim() {
        return true;
    }

    /** Kayma sirasinda da ates edilebilir; havada iken de serbest. */
    public synchronized boolean canFire() {
        return true;
    }

    /** Tek karelik "zipladi" olayini tuketir. */
    public synchronized boolean consumeJumpStarted() {
        boolean e = jumpStartedEvent;
        jumpStartedEvent = false;
        return e;
    }

    /** Tek karelik "yere indi" olayini tuketir. */
    public synchronized boolean consumeLanded() {
        boolean e = landedEvent;
        landedEvent = false;
        return e;
    }

    /** Tek karelik "kayma basladi" olayini tuketir. */
    public synchronized boolean consumeSlideStarted() {
        boolean e = slideStartedEvent;
        slideStartedEvent = false;
        return e;
    }

    /** Tek karelik "kayma bitti / ayaga kalkti" olayini tuketir. */
    public synchronized boolean consumeSlideEnded() {
        boolean e = slideEndedEvent;
        slideEndedEvent = false;
        return e;
    }
}
