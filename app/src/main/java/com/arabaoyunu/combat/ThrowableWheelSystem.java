package com.arabaoyunu.combat;

/**
 * Firlatilabilir secim carki (radial wheel).
 *
 * Bomba, Molotov ve C4 icin: butona basilip suruklenince (press & drag)
 * acilan 4'lu secim carki. 4. dilim "IPTAL"dir. Secim, parmak baslangic
 * noktasina gore acisal konumdan hesaplanir; dilimler 90'ar derecedir ve
 * ust dilimden saat yonunde dizilir:
 *
 *        BOMBA
 *   C4        MOLOTOF
 *        IPTAL
 *
 * Kullanim:
 *   beginWheel(cx, cy)  -> cark acilir
 *   updateDrag(x, y)    -> vurgulu dilim guncellenir
 *   confirmSelection()  -> secilen dilim (vektorel olarak) kesinlesir
 *   cancelWheel()       -> kapat, secim yok
 */
public final class ThrowableWheelSystem {

    public static final int SLOT_NONE = -1;
    public static final int SLOT_BOMBA = 0;
    public static final int SLOT_MOLOTOF = 1;
    public static final int SLOT_C4 = 2;
    public static final int SLOT_IPTAL = 3;

    public static final int SLOT_COUNT = 4;

    /** Cark acikken secim sayilmasi icin gereken minimum surukleme mesafe orani. */
    private static final float DEAD_ZONE_RATIO = 0.22f;

    private boolean open;
    private float centerX;
    private float centerY;
    private float radiusPx = 120f;
    private int highlightedSlot = SLOT_NONE;
    private int lastConfirmedSlot = SLOT_NONE;

    public synchronized void beginWheel(float cx, float cy, float radiusPx) {
        this.open = true;
        this.centerX = cx;
        this.centerY = cy;
        this.radiusPx = Math.max(48f, radiusPx);
        this.highlightedSlot = SLOT_NONE;
    }

    public synchronized void updateDrag(float x, float y) {
        if (!open) return;
        float dx = x - centerX;
        float dy = y - centerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < radiusPx * DEAD_ZONE_RATIO) {
            highlightedSlot = SLOT_NONE;
            return;
        }
        // Aciyi saat yonunde, ustten baslayarak 0..360 araligina cevir.
        double angle = Math.toDegrees(Math.atan2(dx, -dy));
        if (angle < 0) angle += 360.0;
        // Her dilim 90 derece; dilim merkezleri 0, 90, 180, 270.
        int slot = (int) Math.floor(((angle + 45.0) % 360.0) / 90.0);
        highlightedSlot = slot;
    }

    /** Carki kapatir ve vurgulu dilimi kesin secim olarak dondurur. */
    public synchronized int confirmSelection() {
        if (!open) return SLOT_NONE;
        open = false;
        lastConfirmedSlot = highlightedSlot == SLOT_IPTAL ? SLOT_NONE : highlightedSlot;
        highlightedSlot = SLOT_NONE;
        return lastConfirmedSlot;
    }

    /** Carki secim yapmadan kapatir. */
    public synchronized void cancelWheel() {
        open = false;
        highlightedSlot = SLOT_NONE;
    }

    public synchronized boolean isOpen() {
        return open;
    }

    public synchronized int getHighlightedSlot() {
        return highlightedSlot;
    }

    public synchronized int getLastConfirmedSlot() {
        return lastConfirmedSlot;
    }

    public synchronized float getCenterX() {
        return centerX;
    }

    public synchronized float getCenterY() {
        return centerY;
    }

    public synchronized float getRadiusPx() {
        return radiusPx;
    }

    /** Dilim merkez acisi (derece, ustten saat yonunde). Cizim katmani kullanir. */
    public static float slotAngleDegrees(int slot) {
        switch (slot) {
            case SLOT_BOMBA: return 0f;
            case SLOT_MOLOTOF: return 90f;
            case SLOT_C4: return 270f;
            case SLOT_IPTAL: return 180f;
            default: return 0f;
        }
    }

    public static String slotLabel(int slot) {
        switch (slot) {
            case SLOT_BOMBA: return "BOMBA";
            case SLOT_MOLOTOF: return "MOLOTOF";
            case SLOT_C4: return "C4";
            case SLOT_IPTAL: return "IPTAL";
            default: return "";
        }
    }

    /** Kesinlesmis slot icin envanterde karsilik gelen ekipman adi. */
    public static String slotEquipmentId(int slot) {
        switch (slot) {
            case SLOT_BOMBA: return "frag_grenade";
            case SLOT_MOLOTOF: return "molotov";
            case SLOT_C4: return "c4_charge";
            default: return "none";
        }
    }
}
