package com.arabaoyunu.garage;

import com.arabaoyunu.customization.VehicleCustomizationSystem;
import com.arabaoyunu.customization.VisualCustomizationSystem;
import com.arabaoyunu.performance.VehicleTuningSystem;
import com.arabaoyunu.performance.VehicleUpgradeSystem;
import com.arabaoyunu.performance.PerformanceUpgradeBalanceSystem;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;
import com.arabaoyunu.vehicle.VehicleRenderConfig;
import com.arabaoyunu.vehicle.VehicleModelAuditSystem;

/**
 * A66.1: Araç / garaj / modifiye temel kalite katmanı.
 *
 * Yeni mod eklemez; mevcut araç kataloğu, gerçek showroom, kaydet/geri al/sıfırla
 * ve sürüşe yansıyan performans/tuning hattının UI'da tek standarda göre görünmesini sağlar.
 */
public final class GarageInfrastructureSystem {
    public static final int SCHEMA_VERSION = 673;

    private GarageInfrastructureSystem() {}

    public static String catalogFoundationLine(int vehicleIndex) {
        int safe = sanitizeVehicleIndex(vehicleIndex);
        VehicleRenderConfig rc = VehicleCatalog.renderConfig(safe);
        String type = VehicleCatalog.isDlcVehicle(safe) ? "DLC" : "ANA";
        return "Katalog " + (safe + 1) + "/" + VehicleCatalog.count()
                + " | " + type
                + " | GLB " + VehicleCatalog.assetFileName(safe)
                + " | hedef " + formatOne(rc.targetLengthMeters) + "m"
                + " | " + VehicleModelAuditSystem.healthLabel(safe);
    }

    public static String showroomFoundationLine(int vehicleIndex) {
        int safe = sanitizeVehicleIndex(vehicleIndex);
        return ShowroomPresentationSystem.stageLine()
                + " | " + ShowroomPresentationSystem.vehicleFitLine(safe);
    }


    public static String showroomFinalQaLine(int vehicleIndex, int quality) {
        int safe = sanitizeVehicleIndex(vehicleIndex);
        return ShowroomPresentationSystem.finalQaLine(safe, quality)
                + " | " + PostTenUpdateStabilityQaSystem.showroomQaLine(safe);
    }

    public static String selectionFoundationLine(SaveManager saveManager, int vehicleIndex) {
        int safe = sanitizeVehicleIndex(vehicleIndex);
        String id = VehicleCatalog.id(safe);
        boolean owned = saveManager != null && saveManager.isVehicleOwned(id);
        boolean level = saveManager != null && saveManager.isVehicleLevelUnlocked(safe);
        String status = owned ? "Sahip" : (level ? "Satın alınabilir" : "Kilitli " + VehicleCatalog.unlockText(safe));
        return status + " | " + VehicleCatalog.className(safe)
                + " | " + VehicleCatalog.garageRole(safe)
                + " | fiyat " + VehicleCatalog.price(safe);
    }

    public static String workshopFoundationLine(SaveManager saveManager, int vehicleIndex) {
        int safe = sanitizeVehicleIndex(vehicleIndex);
        String id = VehicleCatalog.id(safe);
        int preset = saveManager == null ? VehicleTuningSystem.PRESET_BALANCED : saveManager.getTuningPreset(id);
        int build = VehicleUpgradeSystem.buildScore(saveManager, safe);
        int visualCount = countOwnedVisualOptions(saveManager, id);
        return "Atölye: Kaydet/Geri Al/Sıfırla aktif | build " + build + "/100"
                + " | tuning " + VehicleTuningSystem.presetLabel(preset)
                + " | görsel sahiplik " + visualCount;
    }

    public static String drivingFoundationLine(SaveManager saveManager, int vehicleIndex) {
        int safe = sanitizeVehicleIndex(vehicleIndex);
        VehicleUpgradeSystem.Stats s = VehicleUpgradeSystem.buildStats(saveManager, safe);
        return "Sürüş etkisi: Hız " + s.speed
                + " / Tutuş " + s.handling
                + " / Fren " + s.brake
                + " / Drift " + s.drift;
    }

    public static String visualSaveFlowLine(SaveManager saveManager, String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) return "Görsel kayıt: araç seçili değil";
        String summary = VehicleCustomizationSystem.summary(saveManager, vehicleId);
        return "Görsel kayıt: Kaydet kalıcı, Geri Al kayıtlıya döner, Sıfırla varsayılan ön izlemedir | " + summary;
    }

    public static String tuningEffectLine(SaveManager saveManager, int vehicleIndex) {
        int safe = sanitizeVehicleIndex(vehicleIndex);
        String id = VehicleCatalog.id(safe);
        int preset = saveManager == null ? 0 : saveManager.getTuningPreset(id);
        return VehicleTuningSystem.presetLabel(preset) + ": " + VehicleTuningSystem.presetDescription(preset);
    }

    public static String upgradeEffectLine(SaveManager saveManager, int vehicleIndex) {
        int safe = sanitizeVehicleIndex(vehicleIndex);
        String id = VehicleCatalog.id(safe);
        int level = saveManager == null ? 0 : VehicleUpgradeSystem.totalPerformanceLevel(saveManager, id);
        int max = SaveManager.MAX_UPGRADE_LEVEL * VehicleUpgradeSystem.PERFORMANCE_ORDER.length;
        return "Performans seviyesi " + level + "/" + max + " | "
                + PerformanceUpgradeBalanceSystem.tuningSafetyLine(saveManager, safe)
                + " | onay sonrası sürüş fiziğine uygulanır";
    }


    public static String modelAuditLine(int vehicleIndex) {
        return VehicleModelAuditSystem.auditLine(sanitizeVehicleIndex(vehicleIndex));
    }

    public static String modelCalibrationLine(int vehicleIndex) {
        return VehicleModelAuditSystem.calibrationLine(sanitizeVehicleIndex(vehicleIndex));
    }

    public static String modelRepairHintLine(int vehicleIndex) {
        int safe = sanitizeVehicleIndex(vehicleIndex);
        return "DLC/model onarım: " + VehicleModelAuditSystem.repairHint(safe)
                + " | gövde guard " + (VehicleModelAuditSystem.needsStrictBodyGuard(safe) ? "aktif" : "normal");
    }

    public static String stabilityRepairLine(SaveManager saveManager, int vehicleIndex) {
        int safe = sanitizeVehicleIndex(vehicleIndex);
        return PostTenUpdateStabilityQaSystem.garageHealthLine(saveManager, safe);
    }

    public static String modificationRepairLine(SaveManager saveManager, String vehicleId) {
        return PostTenUpdateStabilityQaSystem.modificationFlowQaLine(saveManager, vehicleId);
    }

    public static String postTenFullGameFlowQaLine(SaveManager saveManager, int vehicleIndex) {
        return PostTenUpdateStabilityQaSystem.fullGameFlowLine(saveManager, sanitizeVehicleIndex(vehicleIndex));
    }

    public static String postTenForbiddenAssetLine() {
        return PostTenUpdateStabilityQaSystem.forbiddenAssetLine();
    }

    public static int sanitizeVehicleIndex(int index) {
        if (index < 0) return 0;
        int count = Math.max(1, VehicleCatalog.count());
        if (index >= count) return count - 1;
        return index;
    }

    public static int countOwnedVisualOptions(SaveManager saveManager, String vehicleId) {
        if (saveManager == null || vehicleId == null || vehicleId.length() == 0) return 0;
        int total = 0;
        for (int i = 0; i < VisualCustomizationSystem.VISUAL_ORDER.length; i++) {
            total += saveManager.getOwnedVisualOptionCount(vehicleId, VisualCustomizationSystem.VISUAL_ORDER[i]);
        }
        return total;
    }

    private static String formatOne(float value) {
        return String.valueOf(Math.round(value * 10f) / 10f);
    }
}
