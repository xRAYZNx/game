package com.arabaoyunu.economy;

import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * ArabaOyunu_44: Araç, parça, renk ve jant kilit açma sistemi.
 */
public final class UnlockSystem {

    private final SaveManager saveManager;

    public UnlockSystem(SaveManager saveManager) {
        this.saveManager = saveManager;
    }

    public String unlockForQuestStep(int questStep) {
        if (saveManager == null) return "";

        if (questStep == 1) {
            return unlockNextPaint();
        }
        if (questStep == 2) {
            return unlockNextRim();
        }
        if (questStep == 3) {
            String part = unlockNextPart();
            String crate = giveCrate("Polis kaçış ödülü");
            return merge(part, crate);
        }
        if (questStep == 4) {
            return unlockNextPart();
        }
        if (questStep == 5) {
            return unlockNextVehicle();
        }
        if (questStep == 6) {
            String region = unlockNextRegion();
            String crate = giveCrate("Bölge ödülü");
            return merge(region, crate);
        }
        return "";
    }

    public String unlockRaceReward(boolean firstPlace) {
        if (!firstPlace) return "";
        return unlockNextRim();
    }

    public String unlockDriftReward() {
        return unlockNextPaint();
    }

    public String unlockPoliceEscapeReward() {
        return giveCrate("Kaçış ödülü");
    }

    public String unlockSpecialEventReward() {
        String paint = unlockNextPaint();
        String rim = unlockNextRim();
        String crate = giveCrate("Etkinlik kasası");
        return merge(merge(paint, rim), crate);
    }

    public String unlockNextVehicle() {
        if (saveManager == null) return "";
        for (int i = 1; i < VehicleCatalog.count(); i++) {
            String id = VehicleCatalog.id(i);
            if (!saveManager.isVehicleRewardUnlocked(id) && !saveManager.isVehicleOwned(id)) {
                saveManager.setVehicleRewardUnlocked(id, true);
                return "Yeni araç açıldı: " + VehicleCatalog.label(i);
            }
        }
        return "";
    }

    public String unlockNextPart() {
        if (saveManager == null) return "";
        int before = saveManager.getUnlockedPartTier();
        int after = saveManager.unlockNextPartTier();
        if (after > before) {
            return "Yeni parça seviyesi açıldı: Tier " + after;
        }
        return "";
    }

    public String unlockNextPaint() {
        if (saveManager == null) return "";
        int before = saveManager.getUnlockedPaintCount();
        int after = saveManager.unlockNextPaintPreset();
        if (after > before) {
            return "Yeni renk açıldı: " + after + "/6";
        }
        return "";
    }

    public String unlockNextRim() {
        if (saveManager == null) return "";
        int before = saveManager.getUnlockedRimCount();
        int after = saveManager.unlockNextRimPreset();
        if (after > before) {
            return "Yeni jant açıldı: " + after + "/5";
        }
        return "";
    }

    public String unlockNextRegion() {
        if (saveManager == null) return "";
        int level = saveManager.getPlayerLevel();
        if (level < 3) {
            saveManager.addXp(240);
            return "Büyük Şehir için XP desteği verildi";
        }
        return "Yeni bölge erişimi hazır";
    }

    private String giveCrate(String label) {
        if (saveManager == null) return "";
        saveManager.setRewardCrates(saveManager.getRewardCrates() + 1);
        return label + ": +1 kasa";
    }

    private String merge(String a, String b) {
        boolean aa = a != null && a.length() > 0;
        boolean bb = b != null && b.length() > 0;
        if (aa && bb) return a + " | " + b;
        if (aa) return a;
        if (bb) return b;
        return "";
    }
}
