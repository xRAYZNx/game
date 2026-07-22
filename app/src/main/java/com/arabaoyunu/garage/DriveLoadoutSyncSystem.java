package com.arabaoyunu.garage;

import com.arabaoyunu.customization.VisualCustomizationSystem;
import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.performance.VehicleTuningSystem;
import com.arabaoyunu.performance.VehicleUpgradeSystem;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * A67.0: Garajda seçilen araç + modifiye + performans/tuning yükünün
 * test sürüşü ve tüm oynanabilir modlara aynı kuralla aktarılmasını sağlayan
 * küçük, AIDE uyumlu köprü sistemi.
 */
public final class DriveLoadoutSyncSystem {
    private DriveLoadoutSyncSystem() {}

    public static int resolvePlayableVehicleIndex(SaveManager saveManager, GameScreenState screenState) {
        int count = Math.max(1, VehicleCatalog.count());
        int desired = screenState == null ? 0 : screenState.getSelectedVehicleIndex();
        desired = clamp(desired, 0, count - 1);
        if (saveManager == null) return desired;

        String desiredId = VehicleCatalog.id(desired);
        if (saveManager.isVehicleOwned(desiredId)) return desired;

        int fallback = clamp(saveManager.getSelectedVehicleIndex(), 0, count - 1);
        String fallbackId = VehicleCatalog.id(fallback);
        if (saveManager.isVehicleOwned(fallbackId)) return fallback;
        return 0;
    }

    public static int syncDriveSelection(SaveManager saveManager, GameScreenState screenState) {
        int safe = resolvePlayableVehicleIndex(saveManager, screenState);
        if (screenState != null && screenState.getSelectedVehicleIndex() != safe) {
            screenState.setSelectedVehicleIndex(safe);
        }
        if (saveManager != null && saveManager.getSelectedVehicleIndex() != safe) {
            saveManager.setSelectedVehicleIndex(safe);
        }
        return safe;
    }

    public static int safeMapForMode(int mode, int requestedMap) {
        if (requestedMap < GameScreenState.MAP_OPEN_FIELD || requestedMap > GameScreenState.MAP_MAX_ID) {
            requestedMap = GameScreenState.MAP_OPEN_FIELD;
        }
        // A67.0 final aktarım QA: açık dünya/harici GLB slotları bu seri içinde sürüşe açılmaz.
        if (requestedMap == GameScreenState.MAP_OPEN_WORLD || requestedMap == GameScreenState.MAP_SECOND_NEW) {
            return GameScreenState.MAP_OPEN_FIELD;
        }
        // Mevcut ana modların tamamı güvenli Open Field üstünde doğrulanır.
        if (mode == GameScreenState.MODE_DRIFT
                || mode == GameScreenState.MODE_RACE_LOCKED
                || mode == GameScreenState.MODE_POLICE_CHASE
                || mode == GameScreenState.MODE_DRAG_RACE) {
            return GameScreenState.MAP_OPEN_FIELD;
        }
        return requestedMap;
    }

    public static int syncModeMap(SaveManager saveManager, GameScreenState screenState) {
        if (screenState == null) return GameScreenState.MAP_OPEN_FIELD;
        int safeMap = safeMapForMode(screenState.getSelectedMode(), screenState.getSelectedMap());
        if (screenState.getSelectedMap() != safeMap) screenState.setSelectedMap(safeMap);
        if (saveManager != null && saveManager.getSelectedMap() != safeMap) saveManager.setSelectedMap(safeMap);
        return safeMap;
    }

    public static String loadoutTitle(SaveManager saveManager, int vehicleIndex, int mode) {
        int safeVehicle = clamp(vehicleIndex, 0, Math.max(1, VehicleCatalog.count()) - 1);
        return VehicleCatalog.label(safeVehicle) + " • " + GameScreenState.modeLabel(mode)
                + " • " + GameScreenState.mapLabel(safeMapForMode(mode, GameScreenState.MAP_OPEN_FIELD));
    }

    public static String tuningLine(SaveManager saveManager, int vehicleIndex) {
        String id = VehicleCatalog.id(clamp(vehicleIndex, 0, Math.max(1, VehicleCatalog.count()) - 1));
        int preset = saveManager == null ? VehicleTuningSystem.PRESET_BALANCED : saveManager.getTuningPreset(id);
        int total = saveManager == null ? 0 : VehicleUpgradeSystem.totalPerformanceLevel(saveManager, id);
        int build = saveManager == null ? 0 : VehicleUpgradeSystem.buildScore(saveManager, vehicleIndex);
        return "Tuning " + VehicleTuningSystem.presetLabel(preset)
                + " • Performans L" + total
                + " • Build " + build + "/100";
    }

    public static String visualLine(SaveManager saveManager, int vehicleIndex) {
        String id = VehicleCatalog.id(clamp(vehicleIndex, 0, Math.max(1, VehicleCatalog.count()) - 1));
        if (saveManager == null) return "Görsel: varsayılan";
        int paint = saveManager.getVisualModValue(id, VisualCustomizationSystem.PAINT_COLOR);
        int rim = saveManager.getRimPreset(id);
        int neon = saveManager.getVisualModValue(id, VisualCustomizationSystem.NEON);
        int tint = saveManager.getVisualModValue(id, VisualCustomizationSystem.WINDOW_TINT);
        int plate = saveManager.getVisualModValue(id, VisualCustomizationSystem.PLATE_STYLE);
        return "Görsel aktarım: boya " + paint
                + " • jant " + rim
                + " • neon " + neon
                + " • cam " + tint
                + " • plaka " + plate;
    }

    public static String qaLine(SaveManager saveManager, int vehicleIndex, int mode, int map) {
        int safeVehicle = clamp(vehicleIndex, 0, Math.max(1, VehicleCatalog.count()) - 1);
        int safeMap = safeMapForMode(mode, map);
        String owned = saveManager != null && saveManager.isVehicleOwned(VehicleCatalog.id(safeVehicle)) ? "sahip" : "kilitli/fallback";
        return "A67.0 Aktarım QA: " + VehicleCatalog.label(safeVehicle)
                + " • " + owned
                + " • " + GameScreenState.modeLabel(mode)
                + " • " + GameScreenState.mapLabel(safeMap)
                + " | " + PostTenUpdateStabilityQaSystem.driveFlowQaLine(saveManager, safeVehicle, mode, safeMap);
    }

    public static String modeHint(int mode) {
        if (mode == GameScreenState.MODE_RACE_LOCKED) return "Checkpoint: seçili araç + performans + tuning ile başlar.";
        if (mode == GameScreenState.MODE_DRIFT) return "Drift: seçili araç + drift/tuning imzası korunur.";
        if (mode == GameScreenState.MODE_POLICE_CHASE) return "Polis: seçili araç + dayanıklılık/fren dengesi korunur.";
        if (mode == GameScreenState.MODE_DRAG_RACE) return "Drag: seçili araç + hızlanma dengesi korunur.";
        return "Test/serbest sürüş: garajdaki son kayıtlı araç yükü kullanılır.";
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}
