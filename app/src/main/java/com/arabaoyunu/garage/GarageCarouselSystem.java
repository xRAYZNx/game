package com.arabaoyunu.garage;

import android.graphics.RectF;

import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * A66.9: Garaj araç carousel / swipe final QA yardımcı sistemi.
 * Amaç: alt araç bandının dokunma alanı, kart hedefleri ve swipe eşiği tek merkezden yönetilsin.
 */
public final class GarageCarouselSystem {
    public static final int VISIBLE_CARD_COUNT = 5;

    private GarageCarouselSystem() {}

    public static void layoutBand(RectF out, float width, float height) {
        float min = Math.min(width, height);
        boolean compact = width < 820f || height < 520f;
        float left = width * 0.055f;
        float right = width * (compact ? 0.690f : 0.705f);
        float bottomInset = Math.max(18f, min * 0.034f);
        float bottom = Math.min(height - bottomInset, height * 0.966f);
        float targetHeight = Math.max(compact ? 82f : 92f, min * (compact ? 0.128f : 0.142f));
        float top = Math.max(height * (compact ? 0.735f : 0.746f), bottom - targetHeight);
        if (bottom - top < Math.max(78f, min * 0.120f)) {
            top = Math.max(height * 0.710f, bottom - Math.max(78f, min * 0.120f));
        }
        out.set(left, top, right, bottom);
    }

    public static float arrowSize(float width, float height) {
        float min = Math.min(width, height);
        return Math.max(46f, Math.min(66f, min * 0.071f));
    }

    public static void layoutPrevArrow(RectF band, RectF out, float width, float height) {
        float size = arrowSize(width, height);
        float y = band.top + Math.max(10f, band.height() * 0.22f);
        out.set(band.left + Math.max(6f, band.width() * 0.010f), y,
                band.left + Math.max(6f, band.width() * 0.010f) + size, y + size);
    }

    public static void layoutNextArrow(RectF band, RectF out, float width, float height) {
        float size = arrowSize(width, height);
        float y = band.top + Math.max(10f, band.height() * 0.22f);
        out.set(band.right - Math.max(6f, band.width() * 0.010f) - size, y,
                band.right - Math.max(6f, band.width() * 0.010f), y + size);
    }

    public static void layoutCard(RectF band, RectF out, int slot, float width, float height) {
        float min = Math.min(width, height);
        float arrowPad = arrowSize(width, height) + Math.max(18f, min * 0.028f);
        float left = band.left + arrowPad;
        float right = band.right - arrowPad;
        float gap = Math.max(6f, min * 0.010f);
        float cardW = (right - left - gap * (VISIBLE_CARD_COUNT - 1)) / VISIBLE_CARD_COUNT;
        boolean compact = width < 820f || height < 520f;
        cardW = Math.max(compact ? 62f : 72f, Math.min(compact ? 108f : 122f, cardW));
        float total = cardW * VISIBLE_CARD_COUNT + gap * (VISIBLE_CARD_COUNT - 1);
        left = band.centerX() - total * 0.5f;
        float cardH = Math.max(46f, Math.min(68f, band.height() * 0.43f));
        float y = band.top + Math.max(13f, band.height() * 0.145f);
        int visualSlot = slot + 2;
        float x = left + visualSlot * (cardW + gap);
        out.set(x, y, x + cardW, y + cardH);
    }

    public static int normalizeIndex(int index) {
        int count = Math.max(1, VehicleCatalog.count());
        while (index < 0) index += count;
        while (index >= count) index -= count;
        return index;
    }

    public static int indexForSlot(int selected, int slot) {
        return normalizeIndex(selected + slot);
    }

    public static float swipeThreshold(float width, float height) {
        float min = Math.min(width, height);
        return Math.max(15f, Math.min(34f, min * 0.031f));
    }

    public static boolean isHorizontalSwipe(float dx, float dy, float width, float height) {
        return Math.abs(dx) >= swipeThreshold(width, height)
                && Math.abs(dx) > Math.abs(dy) * 0.92f;
    }

    public static int swipeDelta(float dx, RectF band) {
        int direction = dx < 0f ? 1 : -1;
        if (Math.abs(dx) > Math.max(110f, band.width() * 0.42f)) {
            return direction * 2;
        }
        return direction;
    }

    public static String statusLine(int selected) {
        return "Carousel: ekran içi / swipe ayrı  |  " + (normalizeIndex(selected) + 1) + "/" + Math.max(1, VehicleCatalog.count())
                + " | A67.3 safe QA";
    }

    public static String cardHintLine(int selected) {
        return VehicleCatalog.label(normalizeIndex(selected)) + "  •  önizleme aktif  •  A67.3 taşma guard";
    }

    public static String changeMessage(int index) {
        int safe = normalizeIndex(index);
        return "Araç carousel: " + (safe + 1) + "/" + Math.max(1, VehicleCatalog.count())
                + " | " + VehicleCatalog.label(safe) + " | " + VehicleCatalog.foundationLine(safe);
    }

    public static String tapMessage(int index) {
        int safe = normalizeIndex(index);
        return "Kart önizleme: " + (safe + 1) + "/" + Math.max(1, VehicleCatalog.count())
                + " | " + VehicleCatalog.label(safe);
    }
}
