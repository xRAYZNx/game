package com.arabaoyunu.customization;

import com.arabaoyunu.util.SaveManager;

/**
 * ArabaOyunu_48: Görsel modifiye kayıt/preset sistemi.
 *
 * Kural: Modelde olmayan sahte 3D parça zorla eklenmez.
 * Bu sistem güvenli olanları shader/material parametresiyle uygular,
 * diğerlerini kayıt + UI/preset olarak saklar.
 */
public final class VisualCustomizationSystem {

    public static final int PAINT_FINISH = 0;
    public static final int WRAP = 1;
    public static final int STICKER = 2;
    public static final int RIM_STYLE = 3;
    public static final int RIM_COLOR = 4;
    public static final int TIRE_SIDEWALL = 5;
    public static final int FRONT_BUMPER = 6;
    public static final int REAR_BUMPER = 7;
    public static final int SIDE_SKIRT = 8;
    public static final int HOOD = 9;
    public static final int SPOILER = 10;
    public static final int EXHAUST_TIP = 11;
    public static final int ROOF_ACCESSORY = 12;
    public static final int MIRROR_COLOR = 13;
    public static final int WINDOW_TINT = 14;
    public static final int PLATE_STYLE = 15;
    public static final int NEON = 16;
    public static final int HEADLIGHT_COLOR = 17;
    public static final int TAIL_LIGHT_STYLE = 18;
    public static final int PAINT_COLOR = 19; // A63.1: gerçek boya rengi slotu

    public static final int[] VISUAL_ORDER = new int[] {
            PAINT_COLOR,
            PAINT_FINISH,
            WRAP,
            STICKER,
            RIM_STYLE,
            RIM_COLOR,
            TIRE_SIDEWALL,
            FRONT_BUMPER,
            REAR_BUMPER,
            SIDE_SKIRT,
            HOOD,
            SPOILER,
            EXHAUST_TIP,
            ROOF_ACCESSORY,
            MIRROR_COLOR,
            WINDOW_TINT,
            PLATE_STYLE,
            NEON,
            HEADLIGHT_COLOR,
            TAIL_LIGHT_STYLE
    };

    private VisualCustomizationSystem() {}

    public static String label(int type) {
        switch (type) {
            case PAINT_COLOR: return "Boya rengi";
            case PAINT_FINISH: return "Boya tipi";
            case WRAP: return "Kaplama";
            case STICKER: return "Sticker";
            case RIM_STYLE: return "Jant modeli";
            case RIM_COLOR: return "Jant rengi";
            case TIRE_SIDEWALL: return "Lastik yazısı";
            case FRONT_BUMPER: return "Ön tampon";
            case REAR_BUMPER: return "Arka tampon";
            case SIDE_SKIRT: return "Yan marşpiyel";
            case HOOD: return "Kaput";
            case SPOILER: return "Spoiler";
            case EXHAUST_TIP: return "Egzoz ucu";
            case ROOF_ACCESSORY: return "Tavan aksesuarı";
            case MIRROR_COLOR: return "Ayna rengi";
            case WINDOW_TINT: return "Cam filmi";
            case PLATE_STYLE: return "Plaka";
            case NEON: return "Neon";
            case HEADLIGHT_COLOR: return "Far rengi";
            case TAIL_LIGHT_STYLE: return "Stop tasarımı";
            default: return "Görsel";
        }
    }

    public static String shortLabel(int type) {
        switch (type) {
            case PAINT_COLOR: return "RENK";
            case PAINT_FINISH: return "BOYA";
            case WRAP: return "KAPLAMA";
            case STICKER: return "STICKER";
            case RIM_STYLE: return "JANT";
            case RIM_COLOR: return "JANT R.";
            case TIRE_SIDEWALL: return "LASTİK Y.";
            case FRONT_BUMPER: return "ÖN T.";
            case REAR_BUMPER: return "ARKA T.";
            case SIDE_SKIRT: return "MARŞP.";
            case HOOD: return "KAPUT";
            case SPOILER: return "SPOILER";
            case EXHAUST_TIP: return "EGZOZ";
            case ROOF_ACCESSORY: return "TAVAN";
            case MIRROR_COLOR: return "AYNA";
            case WINDOW_TINT: return "CAM";
            case PLATE_STYLE: return "PLAKA";
            case NEON: return "NEON";
            case HEADLIGHT_COLOR: return "FAR";
            case TAIL_LIGHT_STYLE: return "STOP";
            default: return "GÖRSEL";
        }
    }

    public static int maxValue(int type) {
        switch (type) {
            case PAINT_COLOR: return 9;       // A63.1: kırmızı/mavi/siyah/beyaz/gri/sarı/yeşil/mor/turuncu/mat siyah
            case PAINT_FINISH: return 2;      // parlak/mat/metalik
            case WRAP: return 5;             // kaplama preset
            case STICKER: return 6;          // sticker/decal preset
            case RIM_STYLE: return 5;
            case RIM_COLOR: return 5;
            case TIRE_SIDEWALL: return 4;
            case FRONT_BUMPER:
            case REAR_BUMPER:
            case SIDE_SKIRT:
            case HOOD:
            case SPOILER:
            case EXHAUST_TIP:
            case ROOF_ACCESSORY: return 4;
            case MIRROR_COLOR: return 5;
            case WINDOW_TINT: return 5;
            case PLATE_STYLE: return 5;
            case NEON: return 6;
            case HEADLIGHT_COLOR: return 4;
            case TAIL_LIGHT_STYLE: return 4;
            default: return 3;
        }
    }

    public static String valueLabel(int type, int value) {
        if (type == PAINT_COLOR) {
            if (value <= 0) return "Kırmızı";
            return pick(value, "Mavi", "Siyah", "Beyaz", "Gri", "Sarı", "Yeşil", "Mor", "Turuncu", "Mat siyah");
        }
        if (value <= 0) {
            if (type == PAINT_FINISH) return "Parlak";
            return "Yok";
        }
        switch (type) {
            case PAINT_FINISH:
                return value == 1 ? "Mat" : "Metalik";
            case WRAP:
                return pick(value, "Yarış çizgisi", "Karbon", "Drift", "Rayzn", "Gece");
            case STICKER:
                return pick(value, "Kaput çizgi", "Yan decal", "Rayzn", "Drift", "Polis", "Numara");
            case RIM_STYLE:
                return "Model " + value;
            case RIM_COLOR:
                return pick(value, "Siyah", "Mavi", "Altın", "Krom", "Kırmızı");
            case TIRE_SIDEWALL:
                return pick(value, "Beyaz yazı", "Sarı yazı", "Rayzn", "Drift");
            case FRONT_BUMPER:
            case REAR_BUMPER:
            case SIDE_SKIRT:
            case HOOD:
            case SPOILER:
            case EXHAUST_TIP:
            case ROOF_ACCESSORY:
                return "Preset " + value;
            case MIRROR_COLOR:
                return pick(value, "Siyah", "Gövde", "Krom", "Karbon", "Mavi");
            case WINDOW_TINT:
                return pick(value, "Hafif", "Orta", "Koyu", "Siyah", "Mavi");
            case PLATE_STYLE:
                return pick(value, "Rayzn", "Yarış", "Kısa", "Klasik", "Özel");
            case NEON:
                return pick(value, "Mavi", "Kırmızı", "Yeşil", "Mor", "Sarı", "Beyaz");
            case HEADLIGHT_COLOR:
                return pick(value, "Beyaz", "Mavi", "Sarı", "Xenon");
            case TAIL_LIGHT_STYLE:
                return pick(value, "Kırmızı", "Koyu", "Şerit", "LED");
            default:
                return "Preset " + value;
        }
    }

    public static int optionCost(int type, int value) {
        if (value <= 0) return 0;
        int base = 280;
        if (type == PAINT_COLOR) base = value >= 8 ? 1200 : (value >= 5 ? 750 : 250);
        else if (type == PAINT_FINISH) base = 360;
        else if (type == WRAP) base = 620;
        else if (type == STICKER) base = 420;
        else if (type == RIM_STYLE) base = 760;
        else if (type == RIM_COLOR) base = 360;
        else if (type == TIRE_SIDEWALL) base = 300;
        else if (type == FRONT_BUMPER || type == REAR_BUMPER || type == SIDE_SKIRT) base = 820;
        else if (type == HOOD) base = 720;
        else if (type == SPOILER) base = 900;
        else if (type == EXHAUST_TIP) base = 640;
        else if (type == ROOF_ACCESSORY) base = 520;
        else if (type == MIRROR_COLOR) base = 320;
        else if (type == WINDOW_TINT) base = 380;
        else if (type == PLATE_STYLE) base = 260;
        else if (type == NEON) base = 980;
        else if (type == HEADLIGHT_COLOR || type == TAIL_LIGHT_STYLE) base = 460;
        return base + Math.max(0, value - 1) * 170;
    }

    public static String buttonText(SaveManager saveManager, String vehicleId, int type) {
        int value = saveManager == null ? 0 : saveManager.getVisualModValue(vehicleId, type);
        return shortLabel(type) + " " + valueLabel(type, value);
    }

    public static String summary(SaveManager saveManager, String vehicleId) {
        if (saveManager == null || vehicleId == null) return "-";
        return "Renk " + valueLabel(PAINT_COLOR, saveManager.getVisualModValue(vehicleId, PAINT_COLOR))
                + " | Boya " + valueLabel(PAINT_FINISH, saveManager.getVisualModValue(vehicleId, PAINT_FINISH))
                + " | Kaplama " + valueLabel(WRAP, saveManager.getVisualModValue(vehicleId, WRAP))
                + " | Sticker " + valueLabel(STICKER, saveManager.getVisualModValue(vehicleId, STICKER))
                + " | Cam " + valueLabel(WINDOW_TINT, saveManager.getVisualModValue(vehicleId, WINDOW_TINT));
    }


    public static boolean hasVisibleMaterialEffect(int type) {
        return type == PAINT_COLOR
                || type == PAINT_FINISH
                || type == WRAP
                || type == STICKER
                || type == RIM_STYLE
                || type == RIM_COLOR
                || type == MIRROR_COLOR
                || type == WINDOW_TINT
                || type == HEADLIGHT_COLOR
                || type == TAIL_LIGHT_STYLE;
    }

    public static boolean isColorLike(int type) {
        return type == PAINT_COLOR
                || type == RIM_COLOR
                || type == MIRROR_COLOR
                || type == WINDOW_TINT
                || type == HEADLIGHT_COLOR
                || type == TAIL_LIGHT_STYLE
                || type == WRAP
                || type == STICKER;
    }

    public static int previewColor(int type, int value) {
        if (type == PAINT_COLOR) {
            switch (value) {
                case 0: return 0xFFFF2D24;
                case 1: return 0xFF0A8BFF;
                case 2: return 0xFF050608;
                case 3: return 0xFFE9F2FF;
                case 4: return 0xFF7B828C;
                case 5: return 0xFFFFC247;
                case 6: return 0xFF19D15F;
                case 7: return 0xFF9B4DFF;
                case 8: return 0xFFFF7A1A;
                case 9: return 0xFF0B0B0C;
                default: return 0xFFFF2D24;
            }
        }
        if (value <= 0) return 0xFFBFC7CF;
        if (type == RIM_COLOR || type == RIM_STYLE) {
            return pickColor(value, 0xFF15171A, 0xFF0A8BFF, 0xFFFFB22E, 0xFFE8F2FF, 0xFFFF2D24);
        }
        if (type == MIRROR_COLOR) {
            return pickColor(value, 0xFF111318, 0xFF6AA9FF, 0xFFE6EEF5, 0xFF202020, 0xFF0A8BFF);
        }
        if (type == WINDOW_TINT) {
            return pickColor(value, 0xFF0A1B22, 0xFF081018, 0xFF03070A, 0xFF000000, 0xFF0B3C66);
        }
        if (type == HEADLIGHT_COLOR) {
            return pickColor(value, 0xFFEFF8FF, 0xFF55AFFF, 0xFFFFD86A, 0xFFE6F4FF);
        }
        if (type == TAIL_LIGHT_STYLE) {
            return pickColor(value, 0xFFFF1919, 0xFF4F0909, 0xFFFF4A22, 0xFFFF0E0E);
        }
        if (type == WRAP || type == STICKER || type == NEON) {
            return pickColor(value, 0xFF0A8BFF, 0xFFE33A36, 0xFF19D15F, 0xFF9B4DFF, 0xFFFFC247, 0xFFEFEFEF);
        }
        return 0xFFBFC7CF;
    }

    public static String editHint(int type) {
        if (type == PAINT_COLOR) {
            return "Gerçek boya rengi GLB materyal tint sistemine bağlanır; her araca ayrı kaydedilir.";
        }
        if (hasVisibleMaterialEffect(type)) {
            return "Canlı ön izleme: Kaydetmeden çıkarsan eski değer korunur.";
        }
        return "Bu GLB dosyalarında güvenli kayıt presetidir; sahte parça eklenmez.";
    }

    private static int pickColor(int index, int... values) {
        if (values == null || values.length == 0) return 0xFFBFC7CF;
        int i = Math.max(1, index) - 1;
        if (i < 0) i = 0;
        if (i >= values.length) i = values.length - 1;
        return values[i];
    }

    private static String pick(int index, String... values) {
        if (values == null || values.length == 0) return "Preset " + index;
        int i = Math.max(1, index) - 1;
        if (i < 0) i = 0;
        if (i >= values.length) i = values.length - 1;
        return values[i];
    }
}
