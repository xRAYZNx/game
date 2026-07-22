package com.arabaoyunu.performance;

import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * ArabaOyunu_47: Detaylı tuning ayarları.
 *
 * Parça yükseltmeleri aracı güçlendirir; tuning ayarları ise aynı aracı yarış,
 * drift, polis kaçışı veya dengeli sürüş için farklı hissettirir.
 */
public final class VehicleTuningSystem {

    public static final int GEAR_RATIO = 0;
    public static final int STEER_SENSITIVITY = 1;
    public static final int BRAKE_BALANCE = 2;
    public static final int SUSPENSION_STIFFNESS = 3;
    public static final int RIDE_HEIGHT = 4;
    public static final int CAMBER = 5;
    public static final int TOE = 6;
    public static final int TIRE_PRESSURE = 7;
    public static final int DRIFT_ANGLE = 8;
    public static final int TCS = 9;
    public static final int ABS = 10;
    public static final int ESP = 11;

    public static final int PRESET_BALANCED = 0;
    public static final int PRESET_RACE = 1;
    public static final int PRESET_DRIFT = 2;
    public static final int PRESET_POLICE = 3;

    public static final int[] TUNING_ORDER = new int[] {
            GEAR_RATIO,
            STEER_SENSITIVITY,
            BRAKE_BALANCE,
            SUSPENSION_STIFFNESS,
            RIDE_HEIGHT,
            CAMBER,
            TOE,
            TIRE_PRESSURE,
            DRIFT_ANGLE,
            TCS,
            ABS,
            ESP
    };

    private VehicleTuningSystem() {}

    public static String label(int type) {
        switch (type) {
            case GEAR_RATIO: return "Vites oranı";
            case STEER_SENSITIVITY: return "Direksiyon";
            case BRAKE_BALANCE: return "Fren dengesi";
            case SUSPENSION_STIFFNESS: return "Süspansiyon";
            case RIDE_HEIGHT: return "Yükseklik";
            case CAMBER: return "Kamber";
            case TOE: return "Toe";
            case TIRE_PRESSURE: return "Lastik basıncı";
            case DRIFT_ANGLE: return "Drift açısı";
            case TCS: return "TCS";
            case ABS: return "ABS";
            case ESP: return "ESP";
            default: return "Tuning";
        }
    }

    public static String shortLabel(int type) {
        switch (type) {
            case GEAR_RATIO: return "VİTES";
            case STEER_SENSITIVITY: return "DİREK";
            case BRAKE_BALANCE: return "FREN D.";
            case SUSPENSION_STIFFNESS: return "SÜSP.";
            case RIDE_HEIGHT: return "YÜKS.";
            case CAMBER: return "KAMBER";
            case TOE: return "TOE";
            case TIRE_PRESSURE: return "BASINÇ";
            case DRIFT_ANGLE: return "DRIFT°";
            case TCS: return "TCS";
            case ABS: return "ABS";
            case ESP: return "ESP";
            default: return "AYAR";
        }
    }

    public static String displayValue(SaveManager saveManager, String vehicleId, int type) {
        if (saveManager == null || vehicleId == null) return "-";
        int v = saveManager.getDetailedTuningValue(vehicleId, type);
        if (type == TCS || type == ABS || type == ESP) return v > 0 ? "AÇIK" : "KAPALI";
        if (type == GEAR_RATIO) {
            if (v < 38) return "Kısa " + v;
            if (v > 62) return "Uzun " + v;
            return "Dengeli " + v;
        }
        if (type == BRAKE_BALANCE) return "Ön " + v + " / Arka " + (100 - v);
        if (type == RIDE_HEIGHT) return (v < 42 ? "Alçak " : v > 62 ? "Yüksek " : "Normal ") + v;
        if (type == CAMBER) return formatSigned((v - 50) / 10f);
        if (type == TOE) return formatSigned((v - 50) / 25f);
        if (type == TIRE_PRESSURE) return (24 + Math.round(v * 0.18f)) + " PSI";
        if (type == DRIFT_ANGLE) return (30 + Math.round(v * 0.30f)) + "°";
        return v + "%";
    }

    public static String presetLabel(int preset) {
        switch (preset) {
            case PRESET_RACE: return "YARIŞ";
            case PRESET_DRIFT: return "DRIFT";
            case PRESET_POLICE: return "POLİS";
            default: return "DENGELİ";
        }
    }

    public static String presetDescription(int preset) {
        return TuningPresetBalanceSystem.heroLine(preset);
    }

    public static String presetProsLine(int preset) {
        return TuningPresetBalanceSystem.prosLine(preset);
    }

    public static String presetTradeoffLine(int preset) {
        return TuningPresetBalanceSystem.tradeoffLine(preset);
    }

    public static String presetModeAdvantageLine(int preset) {
        return TuningPresetBalanceSystem.modeAdvantageLine(preset);
    }

    public static int[] presetValues(int preset) {
        if (preset == PRESET_RACE) {
            return new int[] {68, 58, 63, 70, 42, 63, 53, 58, 42, 1, 1, 1};
        }
        if (preset == PRESET_DRIFT) {
            return new int[] {42, 74, 47, 43, 54, 73, 67, 37, 90, 0, 0, 0};
        }
        if (preset == PRESET_POLICE) {
            return new int[] {58, 61, 63, 60, 48, 55, 51, 52, 54, 1, 1, 1};
        }
        return new int[] {50, 50, 55, 50, 50, 50, 50, 50, 50, 1, 1, 1};
    }

    public static void applyPreset(SaveManager saveManager, String vehicleId, int preset) {
        if (saveManager == null || vehicleId == null) return;
        int[] values = presetValues(preset);
        for (int i = 0; i < TUNING_ORDER.length && i < values.length; i++) {
            saveManager.setDetailedTuningValue(vehicleId, TUNING_ORDER[i], values[i]);
        }
        saveManager.setTuningPreset(vehicleId, preset);
        saveManager.setEconomyLastMessage("TUNING PRESET: " + presetLabel(preset) + " | " + TuningPresetBalanceSystem.heroLine(preset));
    }

    public static void applyDetailedTuning(SaveManager saveManager, int vehicleIndex, VehicleController.Tuning tuning, String modeName) {
        if (saveManager == null || tuning == null) return;
        String id = VehicleCatalog.id(vehicleIndex);

        float gear = norm(saveManager.getDetailedTuningValue(id, GEAR_RATIO));
        float steer = norm(saveManager.getDetailedTuningValue(id, STEER_SENSITIVITY));
        float brakeBalance = norm(saveManager.getDetailedTuningValue(id, BRAKE_BALANCE));
        float suspension = norm(saveManager.getDetailedTuningValue(id, SUSPENSION_STIFFNESS));
        float rideHeight = norm(saveManager.getDetailedTuningValue(id, RIDE_HEIGHT));
        float camber = norm(saveManager.getDetailedTuningValue(id, CAMBER));
        float toe = norm(saveManager.getDetailedTuningValue(id, TOE));
        float tirePressure = norm(saveManager.getDetailedTuningValue(id, TIRE_PRESSURE));
        float driftAngle = norm(saveManager.getDetailedTuningValue(id, DRIFT_ANGLE));

        boolean tcs = saveManager.getDetailedTuningValue(id, TCS) > 0;
        boolean abs = saveManager.getDetailedTuningValue(id, ABS) > 0;
        boolean esp = saveManager.getDetailedTuningValue(id, ESP) > 0;

        // Vites oranı: kısa oran hızlanma, uzun oran maksimum hız.
        tuning.maxForwardSpeed *= clamp(1f + gear * 0.060f, 0.92f, 1.08f);
        tuning.acceleration *= clamp(1f - gear * 0.070f, 0.90f, 1.09f);
        tuning.rollingResistance *= clamp(1f + Math.abs(gear) * 0.020f, 1.0f, 1.04f);

        // Direksiyon hassasiyeti ve toe birlikte dönüş tepkisini değiştirir.
        tuning.maxSteerAngleDeg += steer * 4.2f + toe * 1.8f;
        tuning.steeringSpeedReduction = clamp(tuning.steeringSpeedReduction - steer * 0.0017f - toe * 0.0007f, 0.010f, 0.024f);

        // Fren dengesi öne alınırsa stabil fren, arkaya alınırsa hafif drift eğilimi.
        tuning.brakeDeceleration *= clamp(1f + brakeBalance * 0.050f, 0.94f, 1.07f);
        tuning.handbrakeGrip *= clamp(1f - brakeBalance * 0.045f, 0.94f, 1.06f);

        // Süspansiyon ve araç yüksekliği viraj/denge/zıplama hissini etkiler.
        tuning.lateralGrip *= clamp(1f + suspension * 0.070f - Math.abs(rideHeight) * 0.035f, 0.90f, 1.10f);
        tuning.bodyPitchStrength = clamp(tuning.bodyPitchStrength - suspension * 0.035f + rideHeight * 0.018f, 0.10f, 0.28f);
        tuning.bodyRollStrength = clamp(tuning.bodyRollStrength - suspension * 0.035f + rideHeight * 0.020f, 0.10f, 0.28f);
        tuning.rideHeight = clamp(tuning.rideHeight + rideHeight * 0.075f, 0.32f, 0.55f);

        // Kamber / toe / basınç: yarış tutuşu ile drift kayganlığı arasında denge.
        tuning.lateralGrip *= clamp(1f + camber * 0.065f + tirePressure * 0.035f - Math.abs(toe) * 0.020f, 0.88f, 1.12f);
        tuning.rollingResistance *= clamp(1f - tirePressure * 0.032f + Math.abs(toe) * 0.024f, 0.94f, 1.08f);

        // Drift açısı: handbrake grip azalır, slip eşiği değişir ve drift daha erken başlar.
        tuning.handbrakeGrip *= clamp(1f - driftAngle * 0.160f - camber * 0.035f, 0.74f, 1.10f);
        tuning.driftSlipAngleDeg = clamp(tuning.driftSlipAngleDeg + driftAngle * 5.8f - camber * 1.6f, 7.0f, 22.0f);
        tuning.driftBlendRise = clamp(tuning.driftBlendRise + driftAngle * 0.65f, 3.0f, 6.8f);
        tuning.driftBlendFall = clamp(tuning.driftBlendFall - driftAngle * 0.34f, 1.6f, 4.4f);

        // Elektronik yardımcılar.
        if (tcs) {
            tuning.acceleration *= 0.985f;
            tuning.lateralGrip *= 1.030f;
            tuning.handbrakeGrip *= 1.020f;
        } else {
            tuning.acceleration *= 1.035f;
            tuning.handbrakeGrip *= 0.940f;
        }

        if (abs) {
            tuning.brakeDeceleration *= 1.030f;
            tuning.lateralGrip *= 1.010f;
        } else {
            tuning.brakeDeceleration *= 0.965f;
            tuning.handbrakeGrip *= 0.970f;
        }

        if (esp) {
            tuning.lateralGrip *= 1.040f;
            tuning.driftBlendRise *= 0.92f;
            tuning.driftBlendFall *= 1.08f;
        } else {
            tuning.lateralGrip *= 0.965f;
            tuning.handbrakeGrip *= 0.920f;
            tuning.driftBlendRise *= 1.10f;
        }

        if (modeName != null && modeName.indexOf("Drift") >= 0) {
            tuning.handbrakeGrip *= 0.94f;
            tuning.driftBlendRise *= 1.06f;
        } else if (modeName != null && modeName.indexOf("Police") >= 0) {
            tuning.lateralGrip *= 1.02f;
            tuning.brakeDeceleration *= 1.02f;
        }

        TuningPresetBalanceSystem.applyModeAwarePresetSignature(saveManager, vehicleIndex, tuning, modeName);

        tuning.maxForwardSpeed = clamp(tuning.maxForwardSpeed, 30f, 78f);
        tuning.acceleration = clamp(tuning.acceleration, 9f, 34f);
        tuning.nitroAcceleration = clamp(tuning.nitroAcceleration, 6f, 28f);
        tuning.brakeDeceleration = clamp(tuning.brakeDeceleration, 18f, 54f);
        tuning.lateralGrip = clamp(tuning.lateralGrip, 3.7f, 14f);
        tuning.handbrakeGrip = clamp(tuning.handbrakeGrip, 0.80f, 3.4f);
    }


    /**
     * A64.6: Garajdaki tuning presetleri test sürüşünde sadece yazı olarak
     * kalmasın; seçilen preset küçük ama hissedilir fizik imzasına dönüşür.
     * Bu katman yalnızca test sürüşü oturumunda uygulanır, yarış/kariyer
     * modlarının dengesini bozmaz.
     */
    public static void applyTestDriveFeelProfile(SaveManager saveManager, int vehicleIndex, VehicleController.Tuning tuning) {
        if (saveManager == null || tuning == null) return;
        String id = VehicleCatalog.id(vehicleIndex);
        int preset = saveManager.getTuningPreset(id);
        int build = VehicleUpgradeSystem.buildScore(saveManager, vehicleIndex);
        float build01 = clamp(build / 100f, 0f, 1f);

        // Yükseltme seviyesi test alanında biraz daha okunur hale gelsin.
        tuning.maxForwardSpeed *= clamp(1.000f + build01 * 0.020f, 1.0f, 1.025f);
        tuning.acceleration *= clamp(1.000f + build01 * 0.025f, 1.0f, 1.030f);
        tuning.brakeDeceleration *= clamp(1.000f + build01 * 0.018f, 1.0f, 1.022f);

        if (preset == PRESET_RACE) {
            tuning.maxForwardSpeed *= 1.030f;
            tuning.acceleration *= 1.022f;
            tuning.lateralGrip *= 1.034f;
            tuning.handbrakeGrip *= 1.028f;
            tuning.bodyRollStrength *= 0.930f;
            tuning.bodyPitchStrength *= 0.940f;
        } else if (preset == PRESET_DRIFT) {
            tuning.maxSteerAngleDeg += 2.85f;
            tuning.lateralGrip *= 0.925f;
            tuning.handbrakeGrip *= 0.805f;
            tuning.driftSlipAngleDeg = clamp(tuning.driftSlipAngleDeg + 2.8f, 7.0f, 24.0f);
            tuning.driftBlendRise *= 1.145f;
            tuning.driftBlendFall *= 0.890f;
            tuning.bodyRollStrength *= 1.070f;
        } else if (preset == PRESET_POLICE) {
            tuning.brakeDeceleration *= 1.060f;
            tuning.lateralGrip *= 1.028f;
            tuning.handbrakeGrip *= 1.018f;
            tuning.bodyPitchStrength *= 0.910f;
            tuning.bodyRollStrength *= 0.925f;
            tuning.damageResistance *= 1.045f;
        } else {
            tuning.lateralGrip *= 1.010f;
            tuning.brakeDeceleration *= 1.010f;
        }

        tuning.maxForwardSpeed = clamp(tuning.maxForwardSpeed, 30f, 82f);
        tuning.acceleration = clamp(tuning.acceleration, 9f, 36f);
        tuning.nitroAcceleration = clamp(tuning.nitroAcceleration, 6f, 30f);
        tuning.brakeDeceleration = clamp(tuning.brakeDeceleration, 18f, 58f);
        tuning.lateralGrip = clamp(tuning.lateralGrip, 3.4f, 14.8f);
        tuning.handbrakeGrip = clamp(tuning.handbrakeGrip, 0.66f, 3.6f);
        tuning.maxSteerAngleDeg = clamp(tuning.maxSteerAngleDeg, 28f, 43f);
        tuning.damageResistance = clamp(tuning.damageResistance, 0.70f, 1.78f);
    }

    /**
     * A64.7: Garajdan gelen yükseltme + detay tuning + test profili sonrasında
     * fizik değerlerini gerçek cihazda güvenli aralıkta tutar. Amaç aracı
     * güçsüzleştirmek değil; aşırı spin, ani fren kilidi, zıplama ve kamera
     * kopması üreten uç değerleri kesmektir.
     */
    public static void applyDrivingFeelFinalGuard(SaveManager saveManager, int vehicleIndex, VehicleController.Tuning tuning, boolean testDrive) {
        if (tuning == null) return;
        String id = VehicleCatalog.id(vehicleIndex);
        int preset = saveManager == null ? PRESET_BALANCED : saveManager.getTuningPreset(id);
        float perfClass = VehicleCatalog.performanceClass(vehicleIndex);

        tuning.maxForwardSpeed = clamp(tuning.maxForwardSpeed, 31f, testDrive ? 84f : 80f);
        tuning.acceleration = clamp(tuning.acceleration, 9.2f, testDrive ? 36.5f : 34.5f);
        tuning.nitroAcceleration = clamp(tuning.nitroAcceleration, 6.0f, testDrive ? 30.5f : 28.5f);
        tuning.brakeDeceleration = clamp(tuning.brakeDeceleration, 20f, testDrive ? 57f : 54f);
        tuning.lateralGrip = clamp(tuning.lateralGrip, preset == PRESET_DRIFT ? 3.25f : 4.05f, preset == PRESET_RACE ? 14.6f : 13.9f);
        tuning.handbrakeGrip = clamp(tuning.handbrakeGrip, preset == PRESET_DRIFT ? 0.62f : 0.92f, 3.45f);
        tuning.maxSteerAngleDeg = clamp(tuning.maxSteerAngleDeg, 29f, preset == PRESET_DRIFT ? 43.5f : 40.5f);
        tuning.steeringSpeedReduction = clamp(tuning.steeringSpeedReduction, 0.0105f, 0.025f);
        tuning.driftSlipAngleDeg = clamp(tuning.driftSlipAngleDeg, preset == PRESET_DRIFT ? 9.0f : 7.5f, preset == PRESET_DRIFT ? 24.5f : 21.0f);
        tuning.driftBlendRise = clamp(tuning.driftBlendRise, 3.0f, preset == PRESET_DRIFT ? 7.15f : 6.25f);
        tuning.driftBlendFall = clamp(tuning.driftBlendFall, preset == PRESET_DRIFT ? 1.45f : 2.0f, 4.75f);

        // Mobil kontrol: spor araçlar hızlı tepki verir, drift biraz daha serbest kalır,
        // polis/dengeli daha stabil kalır.
        tuning.steerInputRiseRate = 8.4f + perfClass * 0.65f;
        tuning.steerInputFallRate = 10.6f + perfClass * 0.75f;
        tuning.highSpeedStabilityAssist = 0.34f + perfClass * 0.035f;
        if (preset == PRESET_DRIFT) {
            tuning.steerInputRiseRate += 0.55f;
            tuning.highSpeedStabilityAssist *= 0.66f;
        } else if (preset == PRESET_RACE) {
            tuning.highSpeedStabilityAssist *= 1.12f;
        } else if (preset == PRESET_POLICE) {
            tuning.brakeDeceleration *= 1.012f;
            tuning.highSpeedStabilityAssist *= 1.18f;
        }

        tuning.rideHeight = clamp(tuning.rideHeight, 0.32f, 0.56f);
        tuning.groundSnapStrength = clamp(tuning.groundSnapStrength, 11.5f, 16.0f);
        tuning.bodyPitchStrength = clamp(tuning.bodyPitchStrength, 0.09f, 0.27f);
        tuning.bodyRollStrength = clamp(tuning.bodyRollStrength, 0.09f, 0.27f);
        tuning.damageResistance = clamp(tuning.damageResistance, 0.72f, 1.80f);
    }

    public static String drivingFeelFinalLine(SaveManager saveManager, int vehicleIndex) {
        if (saveManager == null) return "Fizik final QA: kayıt yok";
        String id = VehicleCatalog.id(vehicleIndex);
        int preset = saveManager.getTuningPreset(id);
        int build = VehicleUpgradeSystem.buildScore(saveManager, vehicleIndex);
        return presetLabel(preset) + " final tuning | Build " + build + "/100 | "
                + TuningPresetBalanceSystem.modeAdvantageLine(preset);
    }

    public static String testDriveFeelLine(SaveManager saveManager, int vehicleIndex) {
        if (saveManager == null) return "Test sürüşü profili: kayıt yok";
        String id = VehicleCatalog.id(vehicleIndex);
        int preset = saveManager.getTuningPreset(id);
        int build = VehicleUpgradeSystem.buildScore(saveManager, vehicleIndex);
        if (preset == PRESET_RACE) return "YARIŞ hissi | Build " + build + "/100 | tutuş+hız aktif";
        if (preset == PRESET_DRIFT) return "DRIFT hissi | Build " + build + "/100 | drift açısı+combo aktif";
        if (preset == PRESET_POLICE) return "POLİS hissi | Build " + build + "/100 | fren+stabilite aktif";
        return "DENGELİ hissi | Build " + build + "/100 | güvenli test";
    }

    public static String summary(SaveManager saveManager, String vehicleId) {
        if (saveManager == null || vehicleId == null) return "-";
        return "Preset " + presetLabel(saveManager.getTuningPreset(vehicleId))
                + " | TCS " + displayValue(saveManager, vehicleId, TCS)
                + " ABS " + displayValue(saveManager, vehicleId, ABS)
                + " ESP " + displayValue(saveManager, vehicleId, ESP);
    }

    private static float norm(int value) {
        return clamp((value - 50f) / 50f, -1f, 1f);
    }

    private static String formatSigned(float value) {
        String sign = value >= 0f ? "+" : "";
        return sign + String.format(java.util.Locale.US, "%.1f", value);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
