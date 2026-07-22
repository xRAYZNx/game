package com.arabaoyunu.performance;

import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * A66.6: Performans yükseltmelerinin fiyat, etki ve sürüşe yansıma dengesini
 * tek yerde toplar. Yeni araç/model üretmez; mevcut garaj yükseltmelerinin
 * profesyonel ve güvenli şekilde hissedilmesini sağlar.
 */
public final class PerformanceUpgradeBalanceSystem {

    private PerformanceUpgradeBalanceSystem() {}

    public static int balancedCost(int type, int level) {
        int safe = clampInt(level, 0, SaveManager.MAX_UPGRADE_LEVEL);
        if (safe >= SaveManager.MAX_UPGRADE_LEVEL) return 0;
        int base = baseCost(type);
        // A66.6: erken seviyeler erişilebilir, üst seviyeler değerli kalır.
        return Math.max(250, base + safe * 520 + safe * safe * 145 + costWeight(type) * safe);
    }

    public static int baseCost(int type) {
        switch (type) {
            case SaveManager.UPGRADE_ENGINE: return 900;
            case SaveManager.UPGRADE_TURBO: return 1040;
            case SaveManager.UPGRADE_TRANSMISSION: return 820;
            case SaveManager.UPGRADE_BRAKE: return 690;
            case SaveManager.UPGRADE_TIRES: return 780;
            case SaveManager.UPGRADE_SUSPENSION: return 800;
            case SaveManager.UPGRADE_NITRO: return 1160;
            case SaveManager.UPGRADE_DIFFERENTIAL: return 850;
            case SaveManager.UPGRADE_ECU: return 940;
            case SaveManager.UPGRADE_WEIGHT_REDUCTION: return 920;
            case SaveManager.UPGRADE_TRACTION: return 870;
            case SaveManager.UPGRADE_DURABILITY: return 740;
            case SaveManager.UPGRADE_DRIFT: return 820;
            default: return 650;
        }
    }

    private static int costWeight(int type) {
        switch (type) {
            case SaveManager.UPGRADE_NITRO: return 55;
            case SaveManager.UPGRADE_TURBO: return 48;
            case SaveManager.UPGRADE_ENGINE: return 42;
            case SaveManager.UPGRADE_ECU: return 34;
            case SaveManager.UPGRADE_WEIGHT_REDUCTION: return 30;
            default: return 24;
        }
    }

    public static String previewLine(int type, int level, boolean maxed) {
        if (maxed) return "MAX | " + VehicleUpgradeSystem.effectSummary(type);
        int next = clampInt(level + 1, 1, SaveManager.MAX_UPGRADE_LEVEL);
        switch (type) {
            case SaveManager.UPGRADE_ENGINE: return "L" + next + ": ivme + üst hız dengeli artar";
            case SaveManager.UPGRADE_TURBO: return "L" + next + ": turbo itişi + yüksek devir";
            case SaveManager.UPGRADE_TRANSMISSION: return "L" + next + ": vites tepkisi + ivme";
            case SaveManager.UPGRADE_BRAKE: return "L" + next + ": fren mesafesi kısalır";
            case SaveManager.UPGRADE_TIRES: return "L" + next + ": tutuş + viraj güveni";
            case SaveManager.UPGRADE_SUSPENSION: return "L" + next + ": gövde salınımı azalır";
            case SaveManager.UPGRADE_NITRO: return "L" + next + ": N2O patlaması güçlenir";
            case SaveManager.UPGRADE_DIFFERENTIAL: return "L" + next + ": drift çıkışı + çekiş dengesi";
            case SaveManager.UPGRADE_ECU: return "L" + next + ": gaz tepkisi + verim";
            case SaveManager.UPGRADE_WEIGHT_REDUCTION: return "L" + next + ": çeviklik + ivme, dayanıklılık az";
            case SaveManager.UPGRADE_TRACTION: return "L" + next + ": kalkış + stabilite";
            case SaveManager.UPGRADE_DURABILITY: return "L" + next + ": hasar direnci artar";
            case SaveManager.UPGRADE_DRIFT: return "L" + next + ": kayma açısı + combo kontrol";
            default: return "L" + next + ": sürüş değeri artar";
        }
    }

    public static String impactLine(int type) {
        switch (type) {
            case SaveManager.UPGRADE_ENGINE: return "Sürüş etkisi: gaz tepkisi, kalkış ve üst hız.";
            case SaveManager.UPGRADE_TURBO: return "Sürüş etkisi: orta/üst hızda daha güçlü ivme.";
            case SaveManager.UPGRADE_TRANSMISSION: return "Sürüş etkisi: hızlanma çizgisi daha akıcı.";
            case SaveManager.UPGRADE_BRAKE: return "Sürüş etkisi: fren daha güçlü ama kilitlemez.";
            case SaveManager.UPGRADE_TIRES: return "Sürüş etkisi: viraj tutuşu ve fren dengesi.";
            case SaveManager.UPGRADE_SUSPENSION: return "Sürüş etkisi: zıplama ve gövde yatması azalır.";
            case SaveManager.UPGRADE_NITRO: return "Sürüş etkisi: N2O kullanımı daha etkili.";
            case SaveManager.UPGRADE_DIFFERENTIAL: return "Sürüş etkisi: drift çıkışı ve kayma kontrolü.";
            case SaveManager.UPGRADE_ECU: return "Sürüş etkisi: motor yönetimi ve gaz tepkisi.";
            case SaveManager.UPGRADE_WEIGHT_REDUCTION: return "Sürüş etkisi: çeviklik artar, dayanıklılık dengelenir.";
            case SaveManager.UPGRADE_TRACTION: return "Sürüş etkisi: kalkış ve yüksek hız stabilitesi.";
            case SaveManager.UPGRADE_DURABILITY: return "Sürüş etkisi: çarpışma sonrası performans daha az düşer.";
            default: return "Sürüş etkisi: araç hissini güvenli şekilde geliştirir.";
        }
    }

    public static String stageLine(SaveManager save, int vehicleIndex) {
        if (save == null) return "Performans dengesi: kayıt yok";
        String id = VehicleCatalog.id(vehicleIndex);
        int total = VehicleUpgradeSystem.totalPerformanceLevel(save, id);
        int max = SaveManager.MAX_UPGRADE_LEVEL * (VehicleUpgradeSystem.PERFORMANCE_ORDER.length + 1);
        int build = VehicleUpgradeSystem.buildScore(save, vehicleIndex);
        String focus = strongestFocus(save, id);
        return "Performans dengesi: LVL " + total + "/" + max + " | Build " + build + "/100 | Odak: " + focus;
    }

    public static String tuningSafetyLine(SaveManager save, int vehicleIndex) {
        if (save == null) return "Sürüş guard: kayıt yok";
        String id = VehicleCatalog.id(vehicleIndex);
        int power = save.getUpgradeLevel(id, SaveManager.UPGRADE_ENGINE)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_TURBO)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_NITRO)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_ECU);
        int control = save.getUpgradeLevel(id, SaveManager.UPGRADE_TIRES)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_BRAKE)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_SUSPENSION)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_TRACTION);
        if (power > control + 5) return "Uyarı: güç yüksek, lastik/fren/çekiş yükseltmesi önerilir";
        if (control > power + 7) return "Kontrol paketi güçlü: güvenli viraj/fren dengesi";
        return "Güç ve kontrol dengesi sağlıklı";
    }

    public static void applyFinalBalance(SaveManager save, int vehicleIndex, VehicleController.Tuning tuning) {
        if (save == null || tuning == null) return;
        String id = VehicleCatalog.id(vehicleIndex);
        int engine = save.getUpgradeLevel(id, SaveManager.UPGRADE_ENGINE);
        int turbo = save.getUpgradeLevel(id, SaveManager.UPGRADE_TURBO);
        int transmission = save.getUpgradeLevel(id, SaveManager.UPGRADE_TRANSMISSION);
        int brake = save.getUpgradeLevel(id, SaveManager.UPGRADE_BRAKE);
        int tires = save.getUpgradeLevel(id, SaveManager.UPGRADE_TIRES);
        int suspension = save.getUpgradeLevel(id, SaveManager.UPGRADE_SUSPENSION);
        int nitro = save.getUpgradeLevel(id, SaveManager.UPGRADE_NITRO);
        int differential = save.getUpgradeLevel(id, SaveManager.UPGRADE_DIFFERENTIAL);
        int ecu = save.getUpgradeLevel(id, SaveManager.UPGRADE_ECU);
        int weight = save.getUpgradeLevel(id, SaveManager.UPGRADE_WEIGHT_REDUCTION);
        int traction = save.getUpgradeLevel(id, SaveManager.UPGRADE_TRACTION);
        int durability = save.getUpgradeLevel(id, SaveManager.UPGRADE_DURABILITY);

        int power = engine + turbo + nitro + ecu;
        int control = brake + tires + suspension + traction;
        float balance01 = clamp((control - power + 12f) / 24f, 0f, 1f);

        // Güç yükseltmeleri hissedilir kalır; kontrol düşükse uç değerler güvenli kırpılır.
        tuning.maxForwardSpeed *= clamp(1.000f + (engine + turbo + transmission + ecu) * 0.0022f, 1.0f, 1.052f);
        tuning.acceleration *= clamp(1.000f + (engine + turbo + transmission + weight + ecu) * 0.0026f, 1.0f, 1.064f);
        tuning.nitroAcceleration *= clamp(1.000f + (nitro + turbo + ecu) * 0.0042f, 1.0f, 1.072f);

        tuning.brakeDeceleration *= clamp(1.000f + (brake + tires) * 0.0035f, 1.0f, 1.065f);
        tuning.lateralGrip *= clamp(1.000f + (tires + suspension + traction) * 0.0028f - differential * 0.0008f, 0.98f, 1.065f);
        tuning.bodyPitchStrength *= clamp(1.000f - suspension * 0.010f - brake * 0.002f, 0.86f, 1.0f);
        tuning.bodyRollStrength *= clamp(1.000f - suspension * 0.010f - traction * 0.003f, 0.86f, 1.0f);
        tuning.highSpeedStabilityAssist = clamp(tuning.highSpeedStabilityAssist + traction * 0.012f + suspension * 0.006f + balance01 * 0.04f, 0.18f, 0.72f);

        // Diferansiyel drift kontrolünü güçlendirir; çekiş yükseltmesi aşırı spin'i keser.
        tuning.handbrakeGrip *= clamp(1.000f - differential * 0.018f + traction * 0.004f, 0.82f, 1.05f);
        tuning.driftBlendRise = clamp(tuning.driftBlendRise + differential * 0.050f, 2.9f, 7.25f);
        tuning.driftBlendFall = clamp(tuning.driftBlendFall - differential * 0.018f + traction * 0.010f, 1.45f, 4.85f);

        tuning.damageResistance = clamp(tuning.damageResistance + durability * 0.020f + suspension * 0.006f - weight * 0.006f, 0.70f, 1.86f);

        // Nihai güvenli sınırlar: oyuncunun güçlenmesini engellemez, fizik patlamasını engeller.
        tuning.maxForwardSpeed = clamp(tuning.maxForwardSpeed, 30f, 86f);
        tuning.acceleration = clamp(tuning.acceleration, 9f, 38f);
        tuning.nitroAcceleration = clamp(tuning.nitroAcceleration, 6f, 32f);
        tuning.brakeDeceleration = clamp(tuning.brakeDeceleration, 18f, 60f);
        tuning.lateralGrip = clamp(tuning.lateralGrip, 3.25f, 15.2f);
        tuning.handbrakeGrip = clamp(tuning.handbrakeGrip, 0.58f, 3.65f);
        tuning.damageResistance = clamp(tuning.damageResistance, 0.70f, 1.86f);
    }

    private static String strongestFocus(SaveManager save, String id) {
        int power = save.getUpgradeLevel(id, SaveManager.UPGRADE_ENGINE)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_TURBO)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_NITRO)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_ECU);
        int control = save.getUpgradeLevel(id, SaveManager.UPGRADE_BRAKE)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_TIRES)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_SUSPENSION)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_TRACTION);
        int drift = save.getUpgradeLevel(id, SaveManager.UPGRADE_DIFFERENTIAL)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_DRIFT);
        int body = save.getUpgradeLevel(id, SaveManager.UPGRADE_DURABILITY)
                + save.getUpgradeLevel(id, SaveManager.UPGRADE_WEIGHT_REDUCTION);
        if (power >= control && power >= drift && power >= body) return "Güç";
        if (control >= drift && control >= body) return "Kontrol";
        if (drift >= body) return "Drift";
        return "Dayanıklılık";
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int clampInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
