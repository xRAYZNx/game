package com.arabaoyunu.performance;

import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * ArabaOyunu_46: Gerçek performans yükseltmeleri + araç istatistik sistemi.
 *
 * Bu sistem garajdaki performans parçalarını sürüş fiziğine bağlar.
 */
public final class VehicleUpgradeSystem {

    public static final int[] PERFORMANCE_ORDER = new int[] {
            SaveManager.UPGRADE_ENGINE,
            SaveManager.UPGRADE_TURBO,
            SaveManager.UPGRADE_TRANSMISSION,
            SaveManager.UPGRADE_BRAKE,
            SaveManager.UPGRADE_TIRES,
            SaveManager.UPGRADE_SUSPENSION,
            SaveManager.UPGRADE_NITRO,
            SaveManager.UPGRADE_DIFFERENTIAL,
            SaveManager.UPGRADE_ECU,
            SaveManager.UPGRADE_WEIGHT_REDUCTION,
            SaveManager.UPGRADE_TRACTION,
            SaveManager.UPGRADE_DURABILITY
    };

    public static final class Stats {
        public int speed;
        public int acceleration;
        public int handling;
        public int brake;
        public int drift;
        public int durability;
        public int nitro;
    }

    private VehicleUpgradeSystem() {}

    public static String label(int type) {
        switch (type) {
            case SaveManager.UPGRADE_ENGINE: return "Motor";
            case SaveManager.UPGRADE_TURBO: return "Turbo";
            case SaveManager.UPGRADE_TRANSMISSION: return "Şanzıman";
            case SaveManager.UPGRADE_BRAKE: return "Fren";
            case SaveManager.UPGRADE_TIRES: return "Lastik";
            case SaveManager.UPGRADE_SUSPENSION: return "Süspansiyon";
            case SaveManager.UPGRADE_NITRO: return "Nitro";
            case SaveManager.UPGRADE_DIFFERENTIAL: return "Diferansiyel";
            case SaveManager.UPGRADE_ECU: return "ECU";
            case SaveManager.UPGRADE_WEIGHT_REDUCTION: return "Ağırlık";
            case SaveManager.UPGRADE_TRACTION: return "Çekiş";
            case SaveManager.UPGRADE_DURABILITY: return "Dayanıklılık";
            case SaveManager.UPGRADE_DRIFT: return "Drift";
            default: return "Parça";
        }
    }

    public static String shortLabel(int type) {
        switch (type) {
            case SaveManager.UPGRADE_ENGINE: return "MOTOR";
            case SaveManager.UPGRADE_TURBO: return "TURBO";
            case SaveManager.UPGRADE_TRANSMISSION: return "ŞANZ";
            case SaveManager.UPGRADE_BRAKE: return "FREN";
            case SaveManager.UPGRADE_TIRES: return "LASTİK";
            case SaveManager.UPGRADE_SUSPENSION: return "SÜSP";
            case SaveManager.UPGRADE_NITRO: return "NİTRO";
            case SaveManager.UPGRADE_DIFFERENTIAL: return "DİF";
            case SaveManager.UPGRADE_ECU: return "ECU";
            case SaveManager.UPGRADE_WEIGHT_REDUCTION: return "AĞIRLIK";
            case SaveManager.UPGRADE_TRACTION: return "ÇEKİŞ";
            case SaveManager.UPGRADE_DURABILITY: return "DAYAN";
            case SaveManager.UPGRADE_DRIFT: return "DRIFT";
            default: return "PARÇA";
        }
    }

    public static void applyTuning(SaveManager saveManager, int vehicleIndex, VehicleController.Tuning tuning) {
        if (saveManager == null || tuning == null) return;
        String id = VehicleCatalog.id(vehicleIndex);
        float perf = VehicleCatalog.performanceClass(vehicleIndex);

        int engine = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_ENGINE);
        int turbo = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_TURBO);
        int transmission = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_TRANSMISSION);
        int brake = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_BRAKE);
        int tires = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_TIRES);
        int suspension = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_SUSPENSION);
        int nitro = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_NITRO);
        int differential = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_DIFFERENTIAL);
        int ecu = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_ECU);
        int weight = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_WEIGHT_REDUCTION);
        int traction = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_TRACTION);
        int durability = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_DURABILITY);
        int legacyDrift = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_DRIFT);

        // Temel hız/hızlanma dengesi: her parça belirli fiziği değiştirir.
        tuning.maxForwardSpeed = 40f + perf * 5.7f
                + engine * 1.65f
                + turbo * 1.30f
                + transmission * 0.90f
                + ecu * 0.72f
                + weight * 0.60f;

        tuning.acceleration = 14.2f + perf * 1.85f
                + engine * 1.05f
                + turbo * 1.25f
                + transmission * 1.12f
                + ecu * 0.72f
                + weight * 0.82f
                + traction * 0.34f;

        tuning.nitroAcceleration = 8.6f
                + nitro * 2.10f
                + turbo * 0.72f
                + ecu * 0.56f
                + transmission * 0.20f;

        tuning.brakeDeceleration = 27.0f
                + brake * 3.15f
                + tires * 0.70f
                + suspension * 0.35f
                + weight * 0.22f;

        tuning.reverseAcceleration = 8.1f
                + transmission * 0.42f
                + engine * 0.28f;

        tuning.lateralGrip = 6.45f
                + tires * 0.68f
                + suspension * 0.52f
                + traction * 0.58f
                + perf * 0.22f
                - legacyDrift * 0.05f;

        tuning.rollingResistance = Math.max(1.08f, 1.86f
                - tires * 0.070f
                - transmission * 0.045f
                - weight * 0.060f);

        tuning.airResistance = Math.max(0.011f, 0.018f
                - weight * 0.00055f
                - ecu * 0.00035f);

        tuning.handbrakeGrip = Math.max(1.20f, 2.25f
                - differential * 0.120f
                - legacyDrift * 0.110f
                + traction * 0.018f);

        tuning.driftSlipAngleDeg = Math.max(8.0f, 13.0f
                - differential * 0.52f
                - legacyDrift * 0.55f
                - suspension * 0.13f);

        tuning.driftBlendRise = 4.2f + differential * 0.26f + legacyDrift * 0.30f;
        tuning.driftBlendFall = Math.max(2.10f, 3.0f - differential * 0.075f - legacyDrift * 0.085f);

        tuning.maxSteerAngleDeg = 34.0f + suspension * 0.42f + traction * 0.25f;
        tuning.steeringSpeedReduction = Math.max(0.012f, 0.018f - suspension * 0.00055f - traction * 0.00035f);
        tuning.bodyPitchStrength = Math.max(0.12f, 0.22f - suspension * 0.014f - weight * 0.004f);
        tuning.bodyRollStrength = Math.max(0.11f, 0.20f - suspension * 0.014f - traction * 0.004f);

        // Ağırlık azaltma hızlanmayı artırır ama dayanıklılığı biraz düşürür; süspansiyon/çekiş bunu dengeler.
        tuning.damageResistance = clamp(1.0f + durability * 0.105f + suspension * 0.045f + traction * 0.025f - weight * 0.030f, 0.72f, 1.65f);

        // A66.6: tüm performans paketleri ortak final denge katmanından geçer.
        // Böylece güç artışı hissedilir kalır, fakat uç değerler sürüşü bozmaz.
        PerformanceUpgradeBalanceSystem.applyFinalBalance(saveManager, vehicleIndex, tuning);
    }

    public static Stats buildStats(SaveManager saveManager, int vehicleIndex) {
        Stats s = new Stats();
        if (saveManager == null) return s;
        String id = VehicleCatalog.id(vehicleIndex);
        float perf = VehicleCatalog.performanceClass(vehicleIndex);
        int engine = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_ENGINE);
        int turbo = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_TURBO);
        int transmission = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_TRANSMISSION);
        int brake = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_BRAKE);
        int tires = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_TIRES);
        int suspension = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_SUSPENSION);
        int nitro = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_NITRO);
        int differential = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_DIFFERENTIAL);
        int ecu = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_ECU);
        int weight = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_WEIGHT_REDUCTION);
        int traction = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_TRACTION);
        int durability = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_DURABILITY);
        int legacyDrift = saveManager.getUpgradeLevel(id, SaveManager.UPGRADE_DRIFT);

        s.speed = clampInt(Math.round(46f + perf * 18f + engine * 4.0f + turbo * 4.2f + transmission * 2.4f + ecu * 1.8f + weight * 1.6f), 1, 100);
        s.acceleration = clampInt(Math.round(42f + perf * 15f + engine * 3.2f + turbo * 4.3f + transmission * 3.6f + ecu * 2.4f + weight * 3.0f + traction * 1.4f), 1, 100);
        s.handling = clampInt(Math.round(43f + perf * 10f + tires * 4.3f + suspension * 4.5f + traction * 4.0f + differential * 1.0f), 1, 100);
        s.brake = clampInt(Math.round(42f + brake * 6.2f + tires * 2.2f + suspension * 1.4f + weight * 1.0f), 1, 100);
        s.drift = clampInt(Math.round(34f + differential * 5.3f + legacyDrift * 5.4f + tires * 1.4f + suspension * 2.0f + perf * 7f), 1, 100);
        s.durability = clampInt(Math.round(58f + durability * 7.2f + suspension * 2.0f + traction * 1.3f - weight * 1.6f + perf * 3.0f), 1, 100);
        s.nitro = clampInt(Math.round(24f + nitro * 8.4f + turbo * 2.3f + ecu * 2.0f + transmission * 0.8f), 1, 100);
        return s;
    }

    public static int totalPerformanceLevel(SaveManager saveManager, String vehicleId) {
        if (saveManager == null || vehicleId == null) return 0;
        int total = 0;
        for (int i = 0; i < PERFORMANCE_ORDER.length; i++) {
            total += saveManager.getUpgradeLevel(vehicleId, PERFORMANCE_ORDER[i]);
        }
        return total + saveManager.getUpgradeLevel(vehicleId, SaveManager.UPGRADE_DRIFT);
    }

    public static String effectSummary(int type) {
        switch (type) {
            case SaveManager.UPGRADE_ENGINE: return "İvme + üst hız dengesi";
            case SaveManager.UPGRADE_TURBO: return "Turbo ivmesi + yüksek devir";
            case SaveManager.UPGRADE_TRANSMISSION: return "Vites tepkisi + hızlanma çizgisi";
            case SaveManager.UPGRADE_BRAKE: return "Kısa fren mesafesi + kontrol";
            case SaveManager.UPGRADE_TIRES: return "Viraj tutuşu + fren güveni";
            case SaveManager.UPGRADE_SUSPENSION: return "Denge + gövde salınımı azalır";
            case SaveManager.UPGRADE_NITRO: return "N2O itişi + hızlanma patlaması";
            case SaveManager.UPGRADE_DIFFERENTIAL: return "Drift çıkışı + kayma kontrolü";
            case SaveManager.UPGRADE_ECU: return "Motor yönetimi + gaz tepkisi";
            case SaveManager.UPGRADE_WEIGHT_REDUCTION: return "Çeviklik + ivme, direnç dengeli";
            case SaveManager.UPGRADE_TRACTION: return "Kalkış çekişi + stabilite";
            case SaveManager.UPGRADE_DURABILITY: return "Hasar direnci + sürüş sürekliliği";
            case SaveManager.UPGRADE_DRIFT: return "Drift açısı ve kayma";
            default: return "Sürüş değerlerini geliştirir";
        }
    }

    public static String levelText(SaveManager saveManager, String vehicleId, int type) {
        if (saveManager == null || vehicleId == null) return "L0/5";
        return "L" + saveManager.getUpgradeLevel(vehicleId, type) + "/" + SaveManager.MAX_UPGRADE_LEVEL;
    }

    public static int buildScore(SaveManager saveManager, int vehicleIndex) {
        Stats s = buildStats(saveManager, vehicleIndex);
        return clampInt(Math.round((s.speed + s.acceleration + s.handling + s.brake + s.durability + s.nitro) / 6f), 1, 100);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
