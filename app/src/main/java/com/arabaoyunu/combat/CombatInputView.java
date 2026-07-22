package com.arabaoyunu.combat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import com.arabaoyunu.audio.GameAudioManager;
import com.arabaoyunu.player.PlayerMovementSystem;
import com.arabaoyunu.render.ScopeRenderSystem;

/**
 * Savas dokunma katmani (HUD ustune biner).
 *
 * Ozellikler:
 * - SOL ATES butonu: sabittir; basili tutuldugunda silah otomatik ates
 *   uretir (surukleme kamerayi dondurmez).
 * - SAG ATES butonu: basili tutulup SURUKLENINCE (drag) kamera yonunu
 *   dondurur (aim) ve ayni anda ates etmeye devam eder.
 * - FIRLATILABILIR butonu: basilip suruklenince 4'lu secim carki acilir
 *   (BOMBA / MOLOTOF / C4 / IPTAL) — {@link ThrowableWheelSystem}.
 * - EG L / EG R (lean/peek), ZIPLA (jump), KAY (slide) butonlari —
 *   {@link PlayerMovementSystem}. Kayma bitince otomatik ayaga kalkar;
 *   kayarken nisan alma ve ates serbesttir.
 * - DURBUN butonu: holografik scope ac/kapa. Durbun acikken hareket
 *   joystick'i ekranda GORUNUR kalir ve calisir durumdadir.
 * - Reticle: {@link ScopeRenderSystem}; merkezde mavi nokta yoktur.
 *
 * Modern espor/neon gorunum: ince cizgili, cyan/magenta vurgulu,
 * yumusak basim animasyonlu butonlar.
 */
public final class CombatInputView extends View {

    /** Oyun thread'inin tukettigi anlik savas girisi. */
    public static final class CombatState {
        public boolean fireHeld;
        public boolean scoped;
        public float aimDeltaX;
        public float aimDeltaY;
        public float moveAxisX;   // joystick -1..1
        public float moveAxisY;   // joystick -1..1
        public int leanInput;     // -1 sol, +1 sag
        public boolean jumpRequested;
        public boolean slideRequested;
        public int throwableSlot = ThrowableWheelSystem.SLOT_NONE;
        public float recoilPitch;
        public float recoilYaw;
        public int ammo;
        public int magazineSize;
        public int stance;

        public void copyFrom(CombatState o) {
            fireHeld = o.fireHeld;
            scoped = o.scoped;
            aimDeltaX = o.aimDeltaX;
            aimDeltaY = o.aimDeltaY;
            moveAxisX = o.moveAxisX;
            moveAxisY = o.moveAxisY;
            leanInput = o.leanInput;
            jumpRequested = o.jumpRequested;
            slideRequested = o.slideRequested;
            throwableSlot = o.throwableSlot;
            recoilPitch = o.recoilPitch;
            recoilYaw = o.recoilYaw;
            ammo = o.ammo;
            magazineSize = o.magazineSize;
            stance = o.stance;
        }
    }

    /** Kamera/kayma sesi gibi olaylar icin dinleyici. */
    public interface CombatListener {
        void onThrowableSelected(int slot);
        void onShotFired();
        void onJump();
        void onSlideStarted();
        void onSlideEnded();
        void onScopeChanged(boolean scoped);
    }

    private static final int PTR_NONE = -1;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF joystickBase = new RectF();
    private final RectF fireLeftButton = new RectF();
    private final RectF fireRightButton = new RectF();
    private final RectF throwableButton = new RectF();
    private final RectF scopeButton = new RectF();
    private final RectF leanLeftButton = new RectF();
    private final RectF leanRightButton = new RectF();
    private final RectF jumpButton = new RectF();
    private final RectF slideButton = new RectF();

    private final WeaponSystem weaponSystem = WeaponSystem.createDefaultRifle();
    private final ThrowableWheelSystem wheelSystem = new ThrowableWheelSystem();
    private final PlayerMovementSystem movementSystem = new PlayerMovementSystem();
    private final ScopeRenderSystem scopeSystem = new ScopeRenderSystem();
    private final CombatState state = new CombatState();
    private final CombatState snapshot = new CombatState();

    private GameAudioManager audioManager;
    private CombatListener combatListener;

    private int joystickPointer = PTR_NONE;
    private float joystickKnobX;
    private float joystickKnobY;

    private int fireLeftPointer = PTR_NONE;
    private int fireRightPointer = PTR_NONE;
    private float fireRightLastX;
    private float fireRightLastY;

    private int throwablePointer = PTR_NONE;

    private long lastFrameMs;
    private float pressAnimFireLeft;
    private float pressAnimFireRight;

    public CombatInputView(Context context) {
        super(context);
        paint.setTextAlign(Paint.Align.CENTER);
        setFocusable(false);
        lastFrameMs = System.currentTimeMillis();
    }

    public void setAudioManager(GameAudioManager audioManager) {
        this.audioManager = audioManager;
    }

    public void setCombatListener(CombatListener listener) {
        this.combatListener = listener;
    }

    public WeaponSystem getWeaponSystem() {
        return weaponSystem;
    }

    public PlayerMovementSystem getMovementSystem() {
        return movementSystem;
    }

    public ScopeRenderSystem getScopeSystem() {
        return scopeSystem;
    }

    /** Oyun thread'i cagirir: anlik durumun tutarli kopyasini verir. */
    public synchronized CombatState snapshotCombat() {
        state.scoped = scopeSystem.isScoped();
        state.ammo = weaponSystem.getAmmo();
        state.magazineSize = weaponSystem.getMagazineSize();
        state.recoilPitch = weaponSystem.getRecoilPitch();
        state.recoilYaw = weaponSystem.getRecoilYaw();
        state.stance = movementSystem.getStance();
        snapshot.copyFrom(state);
        state.aimDeltaX = 0f;
        state.aimDeltaY = 0f;
        state.jumpRequested = false;
        state.slideRequested = false;
        state.throwableSlot = ThrowableWheelSystem.SLOT_NONE;
        return snapshot;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        layoutControls(w, h);
    }

    private void layoutControls(int w, int h) {
        float min = Math.min(w, h);
        float pad = Math.max(12f, min * 0.024f);
        float bottom = h - pad;

        // Sol alt: hareket joystick'i (durbun acikken de gorunur).
        float joyR = Math.max(64f, min * 0.135f);
        float joyCx = pad + joyR * 1.15f;
        float joyCy = bottom - joyR * 1.05f;
        joystickBase.set(joyCx - joyR, joyCy - joyR, joyCx + joyR, joyCy + joyR);

        // Sol ates: sabit, joystick ustunde.
        float fireL = Math.max(46f, min * 0.075f);
        fireLeftButton.set(joyCx - fireL, joyCy - joyR - fireL * 2.25f,
                joyCx + fireL, joyCy - joyR - fireL * 0.25f);

        // Sag ates: buyuk, surukleme ile kamera dondurur.
        float fireR = Math.max(58f, min * 0.105f);
        float fireCx = w - pad - fireR * 1.15f;
        float fireCy = bottom - fireR * 1.30f;
        fireRightButton.set(fireCx - fireR, fireCy - fireR, fireCx + fireR, fireCy + fireR);

        // Firlatilabilir (cark): sag atesin sol ustu.
        float thrR = Math.max(40f, min * 0.062f);
        throwableButton.set(fireCx - fireR - thrR * 2.35f, fireCy - fireR - thrR * 1.1f,
                fireCx - fireR - thrR * 0.25f, fireCy - fireR + thrR * 1.1f);

        // Durbun: sag atesin ustu.
        float scpR = Math.max(40f, min * 0.060f);
        scopeButton.set(fireCx - scpR, fireCy - fireR - scpR * 2.5f,
                fireCx + scpR, fireCy - fireR - scpR * 0.5f);

        // Egilme: sol atesin ustu, yan yana.
        float leanW = Math.max(44f, min * 0.075f);
        float leanH = Math.max(34f, min * 0.055f);
        leanLeftButton.set(pad, fireLeftButton.top - leanH * 1.5f - pad,
                pad + leanW, fireLeftButton.top - leanH * 0.5f - pad);
        leanRightButton.set(leanLeftButton.right + pad * 0.6f, leanLeftButton.top,
                leanLeftButton.right + pad * 0.6f + leanW, leanLeftButton.bottom);

        // Zipla + Kay: ekran sag ortasi.
        float actR = Math.max(40f, min * 0.066f);
        float actCx = w - pad - actR * 1.1f;
        jumpButton.set(actCx - actR, h * 0.52f - actR, actCx + actR, h * 0.52f + actR);
        slideButton.set(actCx - actR, h * 0.66f - actR, actCx + actR, h * 0.66f + actR);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        synchronized (this) {
            int action = event.getActionMasked();
            int index = event.getActionIndex();
            int pointerId = event.getPointerId(index);
            float x = event.getX(index);
            float y = event.getY(index);

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_POINTER_DOWN:
                    handlePointerDown(pointerId, x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    handleMove(event);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    handlePointerUp(pointerId);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    releaseAllPointers();
                    break;
                default:
                    break;
            }
        }
        invalidate();
        return true;
    }

    private void handlePointerDown(int pointerId, float x, float y) {
        // Cark acikken baska kontrol devreye girmez; surukleme wheel'i yonlendirir.
        if (wheelSystem.isOpen()) {
            if (pointerId == throwablePointer) {
                wheelSystem.updateDrag(x, y);
            }
            return;
        }

        if (throwableButton.contains(x, y)) {
            throwablePointer = pointerId;
            playClick();
            wheelSystem.beginWheel(throwableButton.centerX(), throwableButton.centerY(),
                    Math.min(getWidth(), getHeight()) * 0.24f);
            return;
        }
        if (scopeButton.contains(x, y)) {
            playClick();
            boolean scoped = !scopeSystem.isScoped();
            scopeSystem.setScoped(scoped);
            if (combatListener != null) combatListener.onScopeChanged(scoped);
            return;
        }
        if (fireLeftButton.contains(x, y)) {
            fireLeftPointer = pointerId;
            pressAnimFireLeft = 1f;
            updateTriggerState();
            return;
        }
        if (fireRightButton.contains(x, y)) {
            fireRightPointer = pointerId;
            fireRightLastX = x;
            fireRightLastY = y;
            pressAnimFireRight = 1f;
            updateTriggerState();
            return;
        }
        if (leanLeftButton.contains(x, y)) {
            playClick();
            movementSystem.setLeanInput(movementSystem.getLeanAmount() < -0.5f ? 0 : -1);
            state.leanInput = movementSystem.getLeanAmount() < -0.5f ? -1 : 0;
            return;
        }
        if (leanRightButton.contains(x, y)) {
            playClick();
            movementSystem.setLeanInput(movementSystem.getLeanAmount() > 0.5f ? 0 : 1);
            state.leanInput = movementSystem.getLeanAmount() > 0.5f ? 1 : 0;
            return;
        }
        if (jumpButton.contains(x, y)) {
            playClick();
            if (movementSystem.requestJump()) {
                state.jumpRequested = true;
                if (combatListener != null) combatListener.onJump();
            }
            return;
        }
        if (slideButton.contains(x, y)) {
            playClick();
            movementSystem.setMoveSpeed(5.4f); // kosu hizi oyun thread'ince de guncellenebilir
            if (movementSystem.requestSlide()) {
                state.slideRequested = true;
                if (combatListener != null) combatListener.onSlideStarted();
            }
            return;
        }
        if (joystickBase.contains(x, y)) {
            joystickPointer = pointerId;
            updateJoystick(x, y);
            return;
        }
    }

    private void handleMove(MotionEvent event) {
        int count = event.getPointerCount();
        for (int i = 0; i < count; i++) {
            int id = event.getPointerId(i);
            float px = event.getX(i);
            float py = event.getY(i);
            if (id == joystickPointer) {
                updateJoystick(px, py);
            } else if (id == fireRightPointer) {
                // SAG ATES suruklemesi: kamera yonunu dondur, ates surer.
                state.aimDeltaX += px - fireRightLastX;
                state.aimDeltaY += py - fireRightLastY;
                fireRightLastX = px;
                fireRightLastY = py;
            } else if (id == throwablePointer && wheelSystem.isOpen()) {
                wheelSystem.updateDrag(px, py);
            }
        }
    }

    private void handlePointerUp(int pointerId) {
        if (pointerId == joystickPointer) {
            joystickPointer = PTR_NONE;
            state.moveAxisX = 0f;
            state.moveAxisY = 0f;
            joystickKnobX = 0f;
            joystickKnobY = 0f;
        } else if (pointerId == fireLeftPointer) {
            fireLeftPointer = PTR_NONE;
            updateTriggerState();
        } else if (pointerId == fireRightPointer) {
            fireRightPointer = PTR_NONE;
            updateTriggerState();
        } else if (pointerId == throwablePointer) {
            throwablePointer = PTR_NONE;
            if (wheelSystem.isOpen()) {
                int slot = wheelSystem.confirmSelection();
                if (slot != ThrowableWheelSystem.SLOT_NONE) {
                    state.throwableSlot = slot;
                    playClick();
                    if (combatListener != null) combatListener.onThrowableSelected(slot);
                }
            }
        }
    }

    private void releaseAllPointers() {
        joystickPointer = PTR_NONE;
        fireLeftPointer = PTR_NONE;
        fireRightPointer = PTR_NONE;
        throwablePointer = PTR_NONE;
        state.moveAxisX = 0f;
        state.moveAxisY = 0f;
        joystickKnobX = 0f;
        joystickKnobY = 0f;
        wheelSystem.cancelWheel();
        updateTriggerState();
    }

    private void updateTriggerState() {
        boolean held = fireLeftPointer != PTR_NONE || fireRightPointer != PTR_NONE;
        weaponSystem.setTriggerHeld(held);
        state.fireHeld = held;
    }

    private void updateJoystick(float x, float y) {
        float radius = joystickBase.width() * 0.5f;
        float dx = (x - joystickBase.centerX()) / radius;
        float dy = (y - joystickBase.centerY()) / radius;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len > 1f) {
            dx /= len;
            dy /= len;
        }
        joystickKnobX = dx;
        joystickKnobY = dy;
        state.moveAxisX = dx;
        state.moveAxisY = dy;
    }

    /** Oyun thread'i her kare cagirir: silah + hareket + scope gecisi. */
    public void tickGame(float dtSeconds, long nowMs) {
        WeaponSystem.Shot shot;
        synchronized (this) {
            weaponSystem.update(dtSeconds);
            movementSystem.update(dtSeconds);
            scopeSystem.update(dtSeconds);
            pressAnimFireLeft = Math.max(0f, pressAnimFireLeft - dtSeconds * 6f);
            pressAnimFireRight = Math.max(0f, pressAnimFireRight - dtSeconds * 6f);
            if (movementSystem.consumeSlideEnded() && combatListener != null) {
                combatListener.onSlideEnded();
            }
            shot = weaponSystem.tryFire(nowMs, movementSystem.canAim(), movementSystem.canFire());
        }
        if (shot != null && combatListener != null) {
            combatListener.onShotFired();
        }
    }

    private void playClick() {
        if (audioManager != null) audioManager.playMenuClick();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        lastFrameMs = System.currentTimeMillis();

        // Durbun overlay (reticle + karartma) — mavi noktasiz holo reticle.
        // FOV gecisi tickGame() icinde tek noktadan ilerletilir.
        scopeSystem.drawScopeOverlay(canvas, getWidth(), getHeight());

        drawJoystick(canvas);
        drawNeonRoundButton(canvas, fireLeftButton, "ATES", pressAnimFireLeft,
                fireLeftPointer != PTR_NONE, Color.argb(255, 255, 90, 120));
        drawNeonRoundButton(canvas, fireRightButton, "ATES\nAIM", pressAnimFireRight,
                fireRightPointer != PTR_NONE, Color.argb(255, 255, 90, 120));
        drawNeonRoundButton(canvas, throwableButton, "ELT", 0f,
                wheelSystem.isOpen(), Color.argb(255, 255, 200, 80));
        drawNeonRoundButton(canvas, scopeButton, "DRBN", 0f,
                scopeSystem.isScoped(), Color.argb(255, 90, 235, 210));
        drawNeonRectButton(canvas, leanLeftButton, "EG L", movementSystem.getLeanAmount() < -0.2f);
        drawNeonRectButton(canvas, leanRightButton, "EG R", movementSystem.getLeanAmount() > 0.2f);
        drawNeonRoundButton(canvas, jumpButton, "ZIPLA", 0f, false, Color.argb(255, 0, 210, 255));
        drawNeonRoundButton(canvas, slideButton,
                movementSystem.isSliding() ? "KALK" : "KAY", 0f,
                movementSystem.isSliding(), Color.argb(255, 0, 210, 255));

        drawAmmoBadge(canvas);
        if (wheelSystem.isOpen()) {
            drawThrowableWheel(canvas);
        }

        // Yumusak animasyonlar icin kare basina yeniden cizim (sadece bu view).
        postInvalidateDelayed(33L);
    }

    /** Hareket joystick'i: durbun acikken de gorunur ve aktif kalir. */
    private void drawJoystick(Canvas canvas) {
        float cx = joystickBase.centerX();
        float cy = joystickBase.centerY();
        float r = joystickBase.width() * 0.5f;

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(52, 8, 14, 22));
        canvas.drawCircle(cx, cy, r, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, r * 0.035f));
        paint.setColor(Color.argb(170, 0, 210, 255));
        canvas.drawCircle(cx, cy, r * 0.96f, paint);

        float knobR = r * 0.42f;
        float kx = cx + joystickKnobX * (r - knobR);
        float ky = cy + joystickKnobY * (r - knobR);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(150, 20, 30, 44));
        canvas.drawCircle(kx, ky, knobR, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.argb(220, 0, 220, 255));
        canvas.drawCircle(kx, ky, knobR, paint);

        // Durbun acik gostergesi: joystick hala aktif oldugunu vurgula.
        if (scopeSystem.isScoped()) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.argb(210, 90, 235, 210));
            paint.setTextSize(Math.max(10f, r * 0.16f));
            canvas.drawText("JOYPAD AKTIF", cx, cy + r + Math.max(12f, r * 0.18f), paint);
        }
    }

    private void drawNeonRoundButton(Canvas canvas, RectF r, String label, float pressAnim,
                                     boolean active, int neonColor) {
        float cx = r.centerX();
        float cy = r.centerY();
        float radius = r.width() * 0.5f;
        float pulse = active ? 1f : 0f;
        float anim = Math.max(pressAnim, pulse);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(active ? 120 : 62, 10, 16, 24));
        canvas.drawCircle(cx, cy, radius, paint);

        // Neon halka: parlak cizgi + yumusak dis parilti.
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(5f, radius * 0.10f));
        paint.setColor(withAlpha(neonColor, active ? 90 : 34));
        canvas.drawCircle(cx, cy, radius * (1.0f + anim * 0.06f), paint);
        paint.setStrokeWidth(Math.max(2f, radius * 0.045f));
        paint.setColor(withAlpha(neonColor, active ? 235 : 150));
        canvas.drawCircle(cx, cy, radius * 0.94f, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(235, 240, 248, 255));
        paint.setTextSize(Math.max(10f, radius * 0.30f));
        String[] lines = label.split("\n");
        Paint.FontMetrics fm = paint.getFontMetrics();
        float lineH = fm.descent - fm.ascent;
        float startY = cy - ((lines.length - 1) * lineH) * 0.5f - (fm.ascent + fm.descent) * 0.5f;
        for (int i = 0; i < lines.length; i++) {
            canvas.drawText(lines[i], cx, startY + i * lineH, paint);
        }
    }

    private void drawNeonRectButton(Canvas canvas, RectF r, String label, boolean active) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(active ? 130 : 60, 10, 16, 24));
        canvas.drawRoundRect(r, 14f, 14f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2.2f);
        paint.setColor(Color.argb(active ? 235 : 140, 0, 210, 255));
        canvas.drawRoundRect(r, 14f, 14f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(235, 240, 248, 255));
        paint.setTextSize(Math.max(10f, r.height() * 0.34f));
        Paint.FontMetrics fm = paint.getFontMetrics();
        canvas.drawText(label, r.centerX(), r.centerY() - (fm.ascent + fm.descent) * 0.5f, paint);
    }

    /** 4'lu firlatilabilir carki (BOMBA / MOLOTOF / C4 / IPTAL). */
    private void drawThrowableWheel(Canvas canvas) {
        float cx = wheelSystem.getCenterX();
        float cy = wheelSystem.getCenterY();
        float radius = wheelSystem.getRadiusPx();
        int highlighted = wheelSystem.getHighlightedSlot();

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(150, 4, 8, 14));
        canvas.drawCircle(cx, cy, radius * 1.18f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(Math.max(2f, radius * 0.02f));
        paint.setColor(Color.argb(170, 0, 220, 255));
        canvas.drawCircle(cx, cy, radius * 1.18f, paint);

        for (int slot = 0; slot < ThrowableWheelSystem.SLOT_COUNT; slot++) {
            double angleRad = Math.toRadians(ThrowableWheelSystem.slotAngleDegrees(slot));
            float sx = cx + (float) Math.sin(angleRad) * radius * 0.72f;
            float sy = cy - (float) Math.cos(angleRad) * radius * 0.72f;
            float itemR = radius * 0.30f;
            boolean active = slot == highlighted;

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(active ? Color.argb(200, 0, 190, 235) : Color.argb(120, 16, 26, 38));
            canvas.drawCircle(sx, sy, itemR, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(Math.max(2f, itemR * 0.08f));
            paint.setColor(active ? Color.argb(255, 200, 250, 255) : Color.argb(160, 0, 210, 255));
            canvas.drawCircle(sx, sy, itemR, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
            paint.setTextSize(Math.max(9f, itemR * 0.30f));
            Paint.FontMetrics fm = paint.getFontMetrics();
            canvas.drawText(ThrowableWheelSystem.slotLabel(slot), sx,
                    sy - (fm.ascent + fm.descent) * 0.5f, paint);
        }
    }

    private void drawAmmoBadge(Canvas canvas) {
        float min = Math.min(getWidth(), getHeight());
        float w = Math.max(92f, min * 0.16f);
        float h = Math.max(30f, min * 0.052f);
        float x = getWidth() - w - Math.max(12f, min * 0.024f);
        float y = Math.max(12f, min * 0.024f) + h * 1.2f;
        RectF badge = new RectF(x, y, x + w, y + h);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(120, 8, 14, 22));
        canvas.drawRoundRect(badge, 12f, 12f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        paint.setColor(Color.argb(180, 0, 210, 255));
        canvas.drawRoundRect(badge, 12f, 12f, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.argb(245, 240, 248, 255));
        paint.setTextSize(Math.max(12f, h * 0.44f));
        Paint.FontMetrics fm = paint.getFontMetrics();
        canvas.drawText(weaponSystem.getAmmo() + " / " + weaponSystem.getMagazineSize(),
                badge.centerX(), badge.centerY() - (fm.ascent + fm.descent) * 0.5f, paint);
    }

    private static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
