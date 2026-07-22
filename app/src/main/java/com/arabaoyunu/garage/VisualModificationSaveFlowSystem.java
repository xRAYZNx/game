package com.arabaoyunu.garage;

import com.arabaoyunu.customization.VisualCustomizationSystem;
import com.arabaoyunu.customization.VehicleCustomizationSystem;
import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.util.SaveManager;

/**
 * A66.5: Görsel modifiye kaydet / geri al / sıfırla akışını tek yerde
 * açıklayan ve eski renderer alanlarını yeni visual slotlarıyla senkronlayan
 * güvenli yardımcı. Yeni 3D parça veya sahte model üretmez.
 */
public final class VisualModificationSaveFlowSystem {
    public static final int SCHEMA_VERSION = 665;

    private VisualModificationSaveFlowSystem() {}

    public static int sanitizeValue(int type, int value) {
        int max = VisualCustomizationSystem.maxValue(type);
        if (value < 0) return 0;
        if (value > max) return max;
        return value;
    }

    public static String actionStateLine(SaveManager save, String vehicleId, int type, int preview, int saved) {
        int p = sanitizeValue(type, preview);
        int s = sanitizeValue(type, saved);
        boolean dirty = p != s;
        boolean owns = save == null || save.isVisualModOptionOwned(vehicleId, type, p) || p == s;
        if (!dirty) {
            return "Kayıtlı görünüm aktif | Test sürüşü kayıtlı ayarla açılır";
        }
        if (p == 0) {
            return "Sıfırla ön izlemede | Kalıcı yapmak için KAYDET";
        }
        if (!VehicleCustomizationSystem.isOptionLevelUnlocked(save, type, p)) {
            return "Ön izleme kilitli | " + VehicleCustomizationSystem.lockLabel(save, type, p);
        }
        if (!owns) {
            int cost = VisualCustomizationSystem.optionCost(type, p);
            return "Satın alma bekliyor | KAYDET = $" + cost + " | GERİ AL kayıtlı hale döner";
        }
        return "Kaydedilmemiş değişiklik | KAYDET kalıcı yapar | GERİ AL iptal eder";
    }

    public static String commitResultLine(int type, int value, boolean purchased) {
        return (purchased ? "Satın alındı ve kaydedildi: " : "Kaydedildi: ")
                + VisualCustomizationSystem.label(type) + " = "
                + VisualCustomizationSystem.valueLabel(type, sanitizeValue(type, value));
    }

    public static String undoLine(int type, int saved) {
        return "GERİ AL: " + VisualCustomizationSystem.label(type) + " kayıtlı hale döndü → "
                + VisualCustomizationSystem.valueLabel(type, sanitizeValue(type, saved));
    }

    public static String resetPreviewLine(int type) {
        return "SIFIRLA: " + VisualCustomizationSystem.label(type)
                + " varsayılan ön izleme oldu; kalıcı yapmak için KAYDET";
    }

    public static String backDiscardLine(int type, int saved) {
        return "PARÇA ekranından çıkıldı: kaydedilmemiş ön izleme iptal, kayıtlı "
                + VisualCustomizationSystem.valueLabel(type, sanitizeValue(type, saved)) + " korundu";
    }

    public static String groupSaveAuditLine(SaveManager save, String vehicleId, int group) {
        if (save == null || vehicleId == null) return "Kayıt sistemi bekleniyor";
        int active = 0;
        int owned = 0;
        for (int i = 0; i < VisualCustomizationSystem.VISUAL_ORDER.length; i++) {
            int type = VisualCustomizationSystem.VISUAL_ORDER[i];
            if (!matchesGroup(type, group)) continue;
            int value = save.getVisualModValue(vehicleId, type);
            if (value > 0) active++;
            owned += save.getOwnedVisualOptionCount(vehicleId, type);
        }
        return "Kayıt QA: " + active + " aktif parça | " + owned
                + " sahip seçenek | Kaydet/Geri Al/Sıfırla canlı ön izlemeden ayrıldı";
    }

    public static String savedSignature(SaveManager save, String vehicleId) {
        if (save == null || vehicleId == null) return "Kayıt yok";
        return "Kayıtlı görünüm: "
                + VisualCustomizationSystem.valueLabel(VisualCustomizationSystem.PAINT_COLOR, save.getVisualModValue(vehicleId, VisualCustomizationSystem.PAINT_COLOR))
                + " / Jant " + VisualCustomizationSystem.valueLabel(VisualCustomizationSystem.RIM_STYLE, save.getVisualModValue(vehicleId, VisualCustomizationSystem.RIM_STYLE))
                + " / Cam " + VisualCustomizationSystem.valueLabel(VisualCustomizationSystem.WINDOW_TINT, save.getVisualModValue(vehicleId, VisualCustomizationSystem.WINDOW_TINT))
                + " / Neon " + VisualCustomizationSystem.valueLabel(VisualCustomizationSystem.NEON, save.getVisualModValue(vehicleId, VisualCustomizationSystem.NEON))
                + " / Plaka " + save.getPlateCode(vehicleId);
    }

    public static void syncLegacyRendererFields(SaveManager save, String vehicleId, int type, int value) {
        if (save == null || vehicleId == null) return;
        int safe = sanitizeValue(type, value);
        if (type == VisualCustomizationSystem.RIM_STYLE) {
            save.setRimPreset(vehicleId, safe);
        } else if (type == VisualCustomizationSystem.PAINT_COLOR) {
            save.setPaintPreset(vehicleId, safe);
        }
    }

    public static boolean matchesGroup(int type, int group) {
        if (group == GameScreenState.VISUAL_GROUP_WHEELS) {
            return type == VisualCustomizationSystem.RIM_STYLE
                    || type == VisualCustomizationSystem.RIM_COLOR
                    || type == VisualCustomizationSystem.TIRE_SIDEWALL;
        }
        if (group == GameScreenState.VISUAL_GROUP_GLASS) {
            return type == VisualCustomizationSystem.WINDOW_TINT
                    || type == VisualCustomizationSystem.MIRROR_COLOR
                    || type == VisualCustomizationSystem.ROOF_ACCESSORY;
        }
        if (group == GameScreenState.VISUAL_GROUP_LIGHTS) {
            return type == VisualCustomizationSystem.HEADLIGHT_COLOR
                    || type == VisualCustomizationSystem.TAIL_LIGHT_STYLE
                    || type == VisualCustomizationSystem.NEON;
        }
        if (group == GameScreenState.VISUAL_GROUP_PLATE) {
            return type == VisualCustomizationSystem.PLATE_STYLE
                    || type == VisualCustomizationSystem.EXHAUST_TIP;
        }
        return type == VisualCustomizationSystem.PAINT_COLOR
                || type == VisualCustomizationSystem.PAINT_FINISH
                || type == VisualCustomizationSystem.WRAP
                || type == VisualCustomizationSystem.STICKER
                || type == VisualCustomizationSystem.HOOD
                || type == VisualCustomizationSystem.FRONT_BUMPER
                || type == VisualCustomizationSystem.REAR_BUMPER
                || type == VisualCustomizationSystem.SIDE_SKIRT
                || type == VisualCustomizationSystem.SPOILER;
    }
}
