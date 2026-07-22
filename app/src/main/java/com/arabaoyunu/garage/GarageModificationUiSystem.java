package com.arabaoyunu.garage;

import com.arabaoyunu.customization.VisualCustomizationSystem;
import com.arabaoyunu.customization.VehicleCustomizationSystem;
import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.performance.VehicleTuningSystem;
import com.arabaoyunu.performance.TuningPresetBalanceSystem;
import com.arabaoyunu.performance.VehicleUpgradeSystem;
import com.arabaoyunu.performance.PerformanceUpgradeBalanceSystem;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * A66.4: Garaj/modifiye atölyesi UI metinleri ve kalite durum satırları.
 * Bu sınıf model üretmez; mevcut araç, gerçek showroom ve kayıt sistemini daha okunur
 * profesyonel atölye kartlarına bağlamak için kısa, güvenli metinler sağlar.
 */
public final class GarageModificationUiSystem {
    public static final int SCHEMA_VERSION = 673;

    private GarageModificationUiSystem() {}

    public static String categoryTitle(int category, int repairCost) {
        switch (category) {
            case 0: return "PERFORMANS";
            case 1: return "TUNING";
            case 2: return "BOYA / BODY";
            case 3: return "JANT / TEKER";
            case 4: return "CAM / AYNA";
            case 5: return "NEON / IŞIK";
            case 6: return "PLAKA";
            case 7: return repairCost <= 0 ? "TAMİR" : ("TAMİR $" + repairCost);
            default: return "ATÖLYE";
        }
    }

    public static String categorySubtitle(int category, int repairCost) {
        switch (category) {
            case 0: return "Motor / turbo / fren / lastik";
            case 1: return "Yarış / drift / polis profili";
            case 2: return "Renk / kaplama / body kit";
            case 3: return "Jant / lastik / yanak";
            case 4: return "Cam filmi / ayna / tavan";
            case 5: return "Far / stop / neon preset";
            case 6: return "Plaka / egzoz / arka detay";
            case 7: return repairCost <= 0 ? "Hasar yok" : "Sürüş öncesi onarım";
            default: return "Profesyonel modifiye";
        }
    }

    public static String categoryStatus(SaveManager save, String vehicleId, int vehicleIndex, int category, int repairCost) {
        switch (category) {
            case 0:
                return "Toplam LVL " + (save == null ? 0 : save.getTotalPerformanceUpgradeLevel(vehicleId));
            case 1:
                return save == null ? "Dengeli" : VehicleTuningSystem.presetLabel(save.getTuningPreset(vehicleId));
            case 2:
                return visualCount(save, vehicleId, GameScreenState.VISUAL_GROUP_BODY) + " aktif";
            case 3:
                return visualCount(save, vehicleId, GameScreenState.VISUAL_GROUP_WHEELS) + " aktif";
            case 4:
                return visualCount(save, vehicleId, GameScreenState.VISUAL_GROUP_GLASS) + " aktif";
            case 5:
                return visualCount(save, vehicleId, GameScreenState.VISUAL_GROUP_LIGHTS) + " aktif";
            case 6:
                return visualCount(save, vehicleId, GameScreenState.VISUAL_GROUP_PLATE) + " aktif";
            case 7:
                return repairCost <= 0 ? "Hazır" : "Onarım gerekli";
            default:
                return "Hazır";
        }
    }

    public static String workshopStageLine(SaveManager save, int vehicleIndex) {
        String id = VehicleCatalog.id(vehicleIndex);
        String tuning = save == null ? "Dengeli" : VehicleTuningSystem.presetLabel(save.getTuningPreset(id));
        int build = VehicleUpgradeSystem.buildScore(save, vehicleIndex);
        int visual = visualCount(save, id, GameScreenState.VISUAL_GROUP_BODY)
                + visualCount(save, id, GameScreenState.VISUAL_GROUP_WHEELS)
                + visualCount(save, id, GameScreenState.VISUAL_GROUP_GLASS)
                + visualCount(save, id, GameScreenState.VISUAL_GROUP_LIGHTS)
                + visualCount(save, id, GameScreenState.VISUAL_GROUP_PLATE);
        return "Atölye hazır: Build " + build + "/100 | Tuning " + tuning + " | Görsel aktif " + visual
                + " | A67.3 safe-area QA";
    }

    public static String performanceStageLine(SaveManager save, int vehicleIndex) {
        String id = VehicleCatalog.id(vehicleIndex);
        return "Onaylı yükseltme: toplam LVL " + (save == null ? 0 : save.getTotalPerformanceUpgradeLevel(id))
                + " | Build " + VehicleUpgradeSystem.buildScore(save, vehicleIndex) + "/100 | "
                + PerformanceUpgradeBalanceSystem.tuningSafetyLine(save, vehicleIndex);
    }

    public static String tuningStageLine(SaveManager save, int vehicleIndex) {
        String id = VehicleCatalog.id(vehicleIndex);
        String preset = save == null ? "Dengeli" : VehicleTuningSystem.presetLabel(save.getTuningPreset(id));
        return "Kayıtlı sürüş profili: " + preset + " | "
                + TuningPresetBalanceSystem.modeAdvantageLine(save == null ? VehicleTuningSystem.PRESET_BALANCED : save.getTuningPreset(id));
    }

    public static String tuningFinalQaLine(SaveManager save, int vehicleIndex) {
        return TuningPresetBalanceSystem.saveStateLine(save, vehicleIndex) + " | "
                + TuningPresetBalanceSystem.physicsSummaryLine(save, vehicleIndex);
    }

    public static String visualGroupStageLine(SaveManager save, String vehicleId, int group) {
        return visualGroupName(group) + ": " + visualCount(save, vehicleId, group)
                + " aktif parça | Ön izleme KAYDET basılana kadar kalıcı değildir";
    }

    public static String visualGroupName(int group) {
        if (group == GameScreenState.VISUAL_GROUP_WHEELS) return "Jant / Teker";
        if (group == GameScreenState.VISUAL_GROUP_GLASS) return "Cam / Ayna";
        if (group == GameScreenState.VISUAL_GROUP_LIGHTS) return "Neon / Işık";
        if (group == GameScreenState.VISUAL_GROUP_PLATE) return "Plaka";
        return "Boya / Body";
    }

    public static String visualEditFlowLine(int type, int current, int saved, boolean owns, boolean dirty) {
        String state = dirty ? "Kaydedilmemiş ön izleme" : "Kayıtlı görünüm";
        String ownership = owns ? "Sahip" : "Satın al gerekli";
        return state + " | " + ownership + " | " + VisualCustomizationSystem.shortLabel(type)
                + ": " + VisualCustomizationSystem.valueLabel(type, current)
                + " / kayıtlı " + VisualCustomizationSystem.valueLabel(type, saved);
    }

    private static int visualCount(SaveManager save, String vehicleId, int group) {
        if (save == null || vehicleId == null) return 0;
        int count = 0;
        for (int i = 0; i < VisualCustomizationSystem.VISUAL_ORDER.length; i++) {
            int type = VisualCustomizationSystem.VISUAL_ORDER[i];
            if (matchesVisualGroup(type, group) && save.getVisualModValue(vehicleId, type) > 0) count++;
        }
        return count;
    }

    private static boolean matchesVisualGroup(int type, int group) {
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
