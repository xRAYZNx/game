package com.arabaoyunu.garage;

import com.arabaoyunu.customization.VisualCustomizationSystem;
import com.arabaoyunu.performance.VehicleTuningSystem;
import com.arabaoyunu.performance.VehicleUpgradeSystem;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * A67.1: Araç / garaj / modifiye genel stabilite ve kayıt onarım katmanı.
 *
 * Bu sınıf yeni mod veya model üretmez. SaveManager içindeki güvenli onarımın
 * UI ve QA tarafında profesyonel, kısa satırlarla izlenmesini sağlar.
 */
public final class GarageStabilityRepairSystem {
    public static final int SCHEMA_VERSION = 671;

    private GarageStabilityRepairSystem() {}

    public static String selectedVehicleStabilityLine(SaveManager saveManager, int previewIndex) {
        int safePreview = sanitizeIndex(previewIndex);
        int selected = saveManager == null ? safePreview : saveManager.getSelectedVehicleIndex();
        String selectedId = VehicleCatalog.id(selected);
        boolean owned = saveManager != null && saveManager.isVehicleOwned(selectedId);
        String status = owned ? "sürüşe uygun" : "starter araca dönmeli";
        return "Kayıt onarım: seçili " + (selected + 1) + "/" + VehicleCatalog.count()
                + " | " + status
                + " | repair v" + (saveManager == null ? SCHEMA_VERSION : saveManager.getSaveRepairVersion());
    }

    public static String garageDataHealthLine(SaveManager saveManager, int vehicleIndex) {
        int safe = sanitizeIndex(vehicleIndex);
        String id = VehicleCatalog.id(safe);
        int build = VehicleUpgradeSystem.buildScore(saveManager, safe);
        int activeVisual = activeVisualCount(saveManager, id);
        int tuning = saveManager == null ? VehicleTuningSystem.PRESET_BALANCED : saveManager.getTuningPreset(id);
        return "Garaj veri sağlığı: build " + build + "/100"
                + " | görsel " + activeVisual
                + " | tuning " + VehicleTuningSystem.presetLabel(tuning)
                + " | legacy paint/rim senkron";
    }

    public static String modificationSaveRepairLine(SaveManager saveManager, String vehicleId) {
        if (saveManager == null || vehicleId == null || vehicleId.length() == 0) {
            return "Modifiye kayıt: araç seçimi bekleniyor";
        }
        int paint = saveManager.getVisualModValue(vehicleId, VisualCustomizationSystem.PAINT_COLOR);
        int rim = saveManager.getVisualModValue(vehicleId, VisualCustomizationSystem.RIM_STYLE);
        int neon = saveManager.getVisualModValue(vehicleId, VisualCustomizationSystem.NEON);
        return "Modifiye kayıt: boya " + VisualCustomizationSystem.valueLabel(VisualCustomizationSystem.PAINT_COLOR, paint)
                + " | jant " + VisualCustomizationSystem.valueLabel(VisualCustomizationSystem.RIM_STYLE, rim)
                + " | neon " + VisualCustomizationSystem.valueLabel(VisualCustomizationSystem.NEON, neon)
                + " | güvenli aralık";
    }

    public static String finalQaLine(SaveManager saveManager, int vehicleIndex) {
        return selectedVehicleStabilityLine(saveManager, vehicleIndex)
                + " | " + garageDataHealthLine(saveManager, vehicleIndex);
    }

    public static int activeVisualCount(SaveManager saveManager, String vehicleId) {
        if (saveManager == null || vehicleId == null || vehicleId.length() == 0) return 0;
        int total = 0;
        for (int i = 0; i < VisualCustomizationSystem.VISUAL_ORDER.length; i++) {
            int type = VisualCustomizationSystem.VISUAL_ORDER[i];
            if (saveManager.getVisualModValue(vehicleId, type) > 0) total++;
        }
        return total;
    }

    public static int sanitizeIndex(int index) {
        int count = Math.max(1, VehicleCatalog.count());
        if (index < 0) return 0;
        if (index >= count) return count - 1;
        return index;
    }
}
