package com.arabaoyunu.performance;

import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * A66.7: Tuning presetlerini sadece etiket olmaktan çıkarıp profesyonel
 * sürüş imzasına bağlayan denge katmanı. Yeni araç/model üretmez; mevcut
 * fizik tuning değerlerini güvenli aralıkta yorumlar.
 */
public final class TuningPresetBalanceSystem {
    public static final int SCHEMA_VERSION = 667;

    private TuningPresetBalanceSystem() {}

    public static String heroLine(int preset) {
        switch (preset) {
            case VehicleTuningSystem.PRESET_RACE:
                return "Pist odaklı: yüksek tutuş, hızlı tepki, temiz viraj";
            case VehicleTuningSystem.PRESET_DRIFT:
                return "Yanlama odaklı: geniş açı, kolay drift, combo kontrolü";
            case VehicleTuningSystem.PRESET_POLICE:
                return "Kovalamaca odaklı: güçlü fren, stabil kaçış, dayanıklılık";
            default:
                return "Güvenli temel: her modda dengeli kontrol ve tutuş";
        }
    }

    public static String prosLine(int preset) {
        switch (preset) {
            case VehicleTuningSystem.PRESET_RACE:
                return "+ Tutuş  + Viraj  + Hızlanma";
            case VehicleTuningSystem.PRESET_DRIFT:
                return "+ Drift açısı  + Combo  + Direksiyon";
            case VehicleTuningSystem.PRESET_POLICE:
                return "+ Fren  + Stabilite  + Hasar direnci";
            default:
                return "+ Kontrol  + Öğrenme  + Güvenli sürüş";
        }
    }

    public static String tradeoffLine(int preset) {
        switch (preset) {
            case VehicleTuningSystem.PRESET_RACE:
                return "- Drift daha zor, kayma erken toparlanır";
            case VehicleTuningSystem.PRESET_DRIFT:
                return "- Düz sürüşte daha hareketli, fren dikkat ister";
            case VehicleTuningSystem.PRESET_POLICE:
                return "- Saf hız yerine kaçış kontrolü öncelikli";
            default:
                return "- Uzman modlar kadar keskin değildir";
        }
    }

    public static String modeAdvantageLine(int preset) {
        switch (preset) {
            case VehicleTuningSystem.PRESET_RACE:
                return "En iyi: Checkpoint / Time Trial";
            case VehicleTuningSystem.PRESET_DRIFT:
                return "En iyi: Drift Skor / serbest yanlama";
            case VehicleTuningSystem.PRESET_POLICE:
                return "En iyi: Polis Kovalamaca / güvenli kaçış";
            default:
                return "En iyi: Test sürüşü / yeni oyuncu";
        }
    }

    public static String saveStateLine(SaveManager saveManager, int vehicleIndex) {
        String id = VehicleCatalog.id(vehicleIndex);
        int preset = saveManager == null ? VehicleTuningSystem.PRESET_BALANCED : saveManager.getTuningPreset(id);
        int build = VehicleUpgradeSystem.buildScore(saveManager, vehicleIndex);
        return "Tuning kayıtlı: " + VehicleTuningSystem.presetLabel(preset)
                + " | Build " + build + "/100 | test+sürüş modlarına aktarılır";
    }

    public static String physicsSummaryLine(SaveManager saveManager, int vehicleIndex) {
        String id = VehicleCatalog.id(vehicleIndex);
        int preset = saveManager == null ? VehicleTuningSystem.PRESET_BALANCED : saveManager.getTuningPreset(id);
        if (preset == VehicleTuningSystem.PRESET_RACE) return "Fizik imzası: tutuş/stabil viraj, drift koruması düşük";
        if (preset == VehicleTuningSystem.PRESET_DRIFT) return "Fizik imzası: düşük handbrake grip, yüksek drift açısı";
        if (preset == VehicleTuningSystem.PRESET_POLICE) return "Fizik imzası: fren/stabilite/hasar direnci dengeli";
        return "Fizik imzası: güvenli temel, orta tutuş ve fren";
    }

    public static void applyModeAwarePresetSignature(SaveManager saveManager, int vehicleIndex, VehicleController.Tuning tuning, String modeName) {
        if (saveManager == null || tuning == null) return;
        String id = VehicleCatalog.id(vehicleIndex);
        int preset = saveManager.getTuningPreset(id);
        String mode = modeName == null ? "" : modeName;
        boolean checkpoint = mode.indexOf("Checkpoint") >= 0 || mode.indexOf("Race") >= 0 || mode.indexOf("Time") >= 0;
        boolean drift = mode.indexOf("Drift") >= 0;
        boolean police = mode.indexOf("Police") >= 0 || mode.indexOf("Chase") >= 0;

        if (preset == VehicleTuningSystem.PRESET_RACE) {
            tuning.lateralGrip *= checkpoint ? 1.052f : 1.035f;
            tuning.acceleration *= 1.018f;
            tuning.maxForwardSpeed *= 1.018f;
            tuning.handbrakeGrip *= 1.035f;
            tuning.bodyRollStrength *= 0.925f;
            tuning.driftBlendRise *= 0.935f;
            tuning.driftBlendFall *= 1.060f;
        } else if (preset == VehicleTuningSystem.PRESET_DRIFT) {
            tuning.maxSteerAngleDeg += drift ? 3.35f : 2.15f;
            tuning.lateralGrip *= drift ? 0.910f : 0.935f;
            tuning.handbrakeGrip *= drift ? 0.765f : 0.835f;
            tuning.driftSlipAngleDeg += drift ? 3.6f : 2.25f;
            tuning.driftBlendRise *= drift ? 1.180f : 1.105f;
            tuning.driftBlendFall *= drift ? 0.850f : 0.905f;
            tuning.highSpeedStabilityAssist *= 0.78f;
        } else if (preset == VehicleTuningSystem.PRESET_POLICE) {
            tuning.brakeDeceleration *= police ? 1.075f : 1.045f;
            tuning.lateralGrip *= police ? 1.040f : 1.020f;
            tuning.handbrakeGrip *= 1.015f;
            tuning.bodyPitchStrength *= 0.900f;
            tuning.bodyRollStrength *= 0.915f;
            tuning.damageResistance *= police ? 1.075f : 1.040f;
            tuning.highSpeedStabilityAssist *= 1.160f;
        } else {
            tuning.lateralGrip *= 1.006f;
            tuning.brakeDeceleration *= 1.006f;
            tuning.highSpeedStabilityAssist *= 1.020f;
        }

        // Preset + performans etkileşimi güvenlik sınırı.
        tuning.maxForwardSpeed = clamp(tuning.maxForwardSpeed, 30f, 82f);
        tuning.acceleration = clamp(tuning.acceleration, 9f, 36f);
        tuning.brakeDeceleration = clamp(tuning.brakeDeceleration, 18f, 58f);
        tuning.lateralGrip = clamp(tuning.lateralGrip, preset == VehicleTuningSystem.PRESET_DRIFT ? 3.20f : 3.95f, preset == VehicleTuningSystem.PRESET_RACE ? 14.8f : 14.2f);
        tuning.handbrakeGrip = clamp(tuning.handbrakeGrip, preset == VehicleTuningSystem.PRESET_DRIFT ? 0.60f : 0.88f, 3.55f);
        tuning.maxSteerAngleDeg = clamp(tuning.maxSteerAngleDeg, 28f, preset == VehicleTuningSystem.PRESET_DRIFT ? 44.0f : 40.8f);
        tuning.driftSlipAngleDeg = clamp(tuning.driftSlipAngleDeg, preset == VehicleTuningSystem.PRESET_DRIFT ? 9.0f : 7.5f, preset == VehicleTuningSystem.PRESET_DRIFT ? 25.0f : 21.5f);
        tuning.damageResistance = clamp(tuning.damageResistance, 0.70f, 1.86f);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
