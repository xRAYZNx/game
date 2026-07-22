package com.arabaoyunu.career;

import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * ArabaOyunu_41: Kariyer başlangıcı ve kayıt senkronizasyon kontrolü.
 *
 * Amaç:
 * - İlk girişte kariyer başlamadıysa başlangıç ekranı göstermek.
 * - Kariyer başladıysa seviye/para/araç/harita kayıtlarının bozulmamasını sağlamak.
 */
public final class CareerSyncSystem {

    private static final int SYNC_VERSION = 1;

    private CareerSyncSystem() {}

    public static boolean validate(SaveManager saveManager) {
        if (saveManager == null) return false;
        if (!saveManager.isCareerStarted()) return false;

        boolean fixed = false;
        if (saveManager.getPlayerLevel() < 1) {
            saveManager.setPlayerLevel(1);
            fixed = true;
        }
        if (saveManager.getPlayerXp() < 0) {
            saveManager.setPlayerXp(0);
            fixed = true;
        }
        int starter = saveManager.getCareerStarterVehicleIndex();
        if (starter < 0 || starter >= VehicleCatalog.count()) {
            starter = 0;
            saveManager.setCareerStarterVehicleIndex(starter);
            fixed = true;
        }
        String starterId = VehicleCatalog.id(starter);
        if (!saveManager.isVehicleOwned(starterId)) {
            saveManager.setVehicleOwned(starterId, true);
            fixed = true;
        }
        if (saveManager.getSelectedMap() < 0 || saveManager.getSelectedMap() > 5) {
            saveManager.setSelectedMap(0);
            fixed = true;
        }
        if (saveManager.getCareerSyncVersion() < SYNC_VERSION) {
            saveManager.setCareerSyncVersion(SYNC_VERSION);
            fixed = true;
        }
        return !fixed;
    }

    public static int version() {
        return SYNC_VERSION;
    }
}
