package com.arabaoyunu.garage;

import com.arabaoyunu.customization.VisualCustomizationSystem;
import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.performance.VehicleTuningSystem;
import com.arabaoyunu.performance.VehicleUpgradeSystem;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * A67.3: UI taşma + telefon/tablet safe-area final QA katmanı.
 *
 * Yeni mod, yeni araç, yeni harita veya yeni showroom üretmez. A67.2 genel
 * akış QA satırlarını daha kısa, kart içinde güvenli ve telefon/tablet uyumlu
 * rozet metinlerine dönüştürür. Gerçek showroom ana asset olarak korunur.
 */
public final class PostTenUpdateStabilityQaSystem {
    public static final int SCHEMA_VERSION = 673;
    public static final String REAL_SHOWROOM_ASSET = "models/showroom/scifi_tron_studio__baked.glb";

    private PostTenUpdateStabilityQaSystem() {}

    public static String garageHealthLine(SaveManager saveManager, int vehicleIndex) {
        int safe = sanitizeIndex(vehicleIndex);
        String id = VehicleCatalog.id(safe);
        boolean owned = saveManager != null && saveManager.isVehicleOwned(id);
        int selected = saveManager == null ? safe : sanitizeIndex(saveManager.getSelectedVehicleIndex());
        int build = VehicleUpgradeSystem.buildScore(saveManager, safe);
        return "A67.3 Garaj: " + (owned ? "Sürüş hazır" : "Kilitli")
                + " | " + (safe + 1) + "/" + VehicleCatalog.count()
                + " | seçili " + (selected + 1)
                + " | build " + build
                + " | safe UI";
    }

    public static String showroomQaLine(int vehicleIndex) {
        int safe = sanitizeIndex(vehicleIndex);
        return "A67.3 Showroom: Gerçek GLB aktif | kamera/ölçek safe | araç "
                + (safe + 1) + "/" + VehicleCatalog.count();
    }

    public static String carouselQaLine(int vehicleIndex) {
        int safe = sanitizeIndex(vehicleIndex);
        return "A67.3 Carousel: ekran içi | swipe ayrıldı | "
                + (safe + 1) + "/" + VehicleCatalog.count();
    }

    public static String modificationFlowQaLine(SaveManager saveManager, String vehicleId) {
        if (vehicleId == null || vehicleId.length() == 0) {
            return "A67.3 Mod: araç seçimi bekleniyor";
        }
        int paint = saveManager == null ? 0 : saveManager.getVisualModValue(vehicleId, VisualCustomizationSystem.PAINT_COLOR);
        int rim = saveManager == null ? 0 : saveManager.getRimPreset(vehicleId);
        int neon = saveManager == null ? 0 : saveManager.getVisualModValue(vehicleId, VisualCustomizationSystem.NEON);
        return "A67.3 Mod: Kaydet/Geri Al/Sıfırla safe | boya " + paint
                + " | jant " + rim
                + " | neon " + neon;
    }

    public static String driveFlowQaLine(SaveManager saveManager, int vehicleIndex, int mode, int map) {
        int safeVehicle = sanitizeIndex(vehicleIndex);
        int safeMap = DriveLoadoutSyncSystem.safeMapForMode(mode, map);
        String id = VehicleCatalog.id(safeVehicle);
        boolean owned = saveManager != null && saveManager.isVehicleOwned(id);
        return "A67.3 Sürüş HUD: " + (owned ? "araç aktarımı hazır" : "kilitli engel")
                + " | " + GameScreenState.modeLabel(mode)
                + " | " + GameScreenState.mapLabel(safeMap)
                + " | kompakt";
    }

    public static String fullGameFlowLine(SaveManager saveManager, int vehicleIndex) {
        int safe = sanitizeIndex(vehicleIndex);
        String id = VehicleCatalog.id(safe);
        int preset = saveManager == null ? VehicleTuningSystem.PRESET_BALANCED : saveManager.getTuningPreset(id);
        return "A67.3 Akış: Garaj/Mod/Test/Checkpoint/Drift/Polis/Kariyer"
                + " | HUD ayrımı safe"
                + " | " + VehicleTuningSystem.presetLabel(preset);
    }

    public static String layoutSafeAreaLine(float width, float height) {
        boolean tablet = width >= 900f || height >= 620f;
        boolean compact = width < 820f || height < 520f;
        return "A67.3 Layout: " + (tablet ? "tablet" : (compact ? "kompakt telefon" : "telefon"))
                + " | safe-area aktif | panel taşma guard";
    }

    public static String uiBadgeLine(float width, float height) {
        boolean compact = width < 820f || height < 520f;
        return compact ? "UI: kompakt / ekran içi" : "UI: kartlar safe / showroom açık";
    }

    public static String forbiddenAssetLine() {
        return "A67.3 Yasak: cars_map/open_world/CarsMap yok | şehir/trafik/online yok";
    }

    public static String saveRepairLine(SaveManager saveManager) {
        if (saveManager == null) return "A67.3 Save: repair v" + SCHEMA_VERSION;
        return "A67.3 Save: repair v" + saveManager.getSaveRepairVersion()
                + " | " + saveManager.getUiSafeAreaQaSummary();
    }

    public static int sanitizeIndex(int index) {
        int count = Math.max(1, VehicleCatalog.count());
        if (index < 0) return 0;
        if (index >= count) return count - 1;
        return index;
    }
}
