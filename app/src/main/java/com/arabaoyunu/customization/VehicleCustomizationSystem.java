package com.arabaoyunu.customization;

import com.arabaoyunu.util.SaveManager;

/**
 * A63.1: Garajdaki görsel modifiye sistemini araba kişiselleştirme
 * mantığına bağlayan güvenli facade. Yeni GLB/body kit uydurmaz;
 * mevcut shader/preset/kayıt sistemini araç bazında yönetir.
 */
public final class VehicleCustomizationSystem {
    public static final int SCHEMA_VERSION = 631;

    private VehicleCustomizationSystem() {}

    public static int requiredLevel(int type, int value) {
        if (value <= 0) return 1;
        if (type == VisualCustomizationSystem.PAINT_COLOR) {
            if (value >= 8) return 10; // mor/turuncu özel renkler
            if (value >= 5) return 3;
            return 1;
        }
        if (type == VisualCustomizationSystem.PAINT_FINISH) return value == 2 ? 6 : 3;
        if (type == VisualCustomizationSystem.RIM_STYLE) return value >= 4 ? 6 : 2;
        if (type == VisualCustomizationSystem.RIM_COLOR) return value >= 3 ? 5 : 2;
        if (type == VisualCustomizationSystem.NEON) return value >= 5 ? 10 : 5;
        if (type == VisualCustomizationSystem.PLATE_STYLE) return value >= 4 ? 12 : 1;
        if (type == VisualCustomizationSystem.WINDOW_TINT) return value >= 4 ? 4 : 1;
        if (type == VisualCustomizationSystem.FRONT_BUMPER
                || type == VisualCustomizationSystem.REAR_BUMPER
                || type == VisualCustomizationSystem.SIDE_SKIRT
                || type == VisualCustomizationSystem.SPOILER
                || type == VisualCustomizationSystem.HOOD) {
            return value >= 3 ? 8 : 4;
        }
        return value >= 4 ? 6 : 1;
    }

    public static boolean isOptionLevelUnlocked(SaveManager save, int type, int value) {
        if (save == null) return true;
        return save.getPlayerLevel() >= requiredLevel(type, value);
    }

    public static String lockLabel(SaveManager save, int type, int value) {
        int req = requiredLevel(type, value);
        if (save != null && save.getPlayerLevel() >= req) return "";
        return "LVL " + req + " GEREKLI";
    }

    public static String garageIdentityLine(SaveManager save, String vehicleId) {
        if (save == null || vehicleId == null) return "Görsel: varsayılan";
        return "Renk " + VisualCustomizationSystem.valueLabel(VisualCustomizationSystem.PAINT_COLOR, save.getVisualModValue(vehicleId, VisualCustomizationSystem.PAINT_COLOR))
                + " / Jant " + VisualCustomizationSystem.valueLabel(VisualCustomizationSystem.RIM_STYLE, save.getVisualModValue(vehicleId, VisualCustomizationSystem.RIM_STYLE))
                + " / Neon " + VisualCustomizationSystem.valueLabel(VisualCustomizationSystem.NEON, save.getVisualModValue(vehicleId, VisualCustomizationSystem.NEON))
                + " / Plaka " + save.getPlateCode(vehicleId);
    }

    public static String summary(SaveManager save, String vehicleId) {
        if (save == null || vehicleId == null) return "-";
        return garageIdentityLine(save, vehicleId)
                + " | Cam " + VisualCustomizationSystem.valueLabel(VisualCustomizationSystem.WINDOW_TINT, save.getVisualModValue(vehicleId, VisualCustomizationSystem.WINDOW_TINT))
                + " | Body kit " + bodyKitStatus(save, vehicleId);
    }

    public static String bodyKitStatus(SaveManager save, String vehicleId) {
        int total = 0;
        total += save.getVisualModValue(vehicleId, VisualCustomizationSystem.FRONT_BUMPER);
        total += save.getVisualModValue(vehicleId, VisualCustomizationSystem.REAR_BUMPER);
        total += save.getVisualModValue(vehicleId, VisualCustomizationSystem.SIDE_SKIRT);
        total += save.getVisualModValue(vehicleId, VisualCustomizationSystem.SPOILER);
        total += save.getVisualModValue(vehicleId, VisualCustomizationSystem.HOOD);
        if (total <= 0) return "Standart";
        if (total < 5) return "Street";
        if (total < 11) return "Racing";
        return "Premium";
    }

    public static float neonIntensity(SaveManager save, String vehicleId) {
        if (save == null || vehicleId == null) return 0f;
        int neon = save.getVisualModValue(vehicleId, VisualCustomizationSystem.NEON);
        return neon <= 0 ? 0f : Math.min(1f, 0.42f + neon * 0.10f);
    }

    public static String optionStatus(SaveManager save, String vehicleId, int type, int value) {
        if (!isOptionLevelUnlocked(save, type, value)) return lockLabel(save, type, value);
        boolean owned = save == null || save.isVisualModOptionOwned(vehicleId, type, value);
        if (owned || value == 0) return "SAHIP / UYGULA";
        return "SATIN AL $" + VisualCustomizationSystem.optionCost(type, value);
    }
}
