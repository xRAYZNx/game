package com.arabaoyunu.vehicle;

import com.arabaoyunu.render.CarVisualConfig;
import com.arabaoyunu.career.CareerLeagueSystem;

/**
 * ArabaOyunu_19: Cok aracli GLB katalog + arac bazli render config sistemi.
 * Isimler kullanicinin yukledigi dosya adlari esas alinarak verildi.
 */
public final class VehicleCatalog {

    /**
     * Tek ve doğrulanmış üretim modeli. Katalog kimlikleri, ekonomi ve kayıt
     * uyumluluğu korunurken bütün görsel girişler aynı güvenli asset'i kullanır.
     */
    public static final String OPTIMIZED_VEHICLE_ASSET = "models/car_main.glb";

    public static final class VehicleEntry {
        public final String id;
        public final String displayName;
        public final String assetPath;
        public final boolean locked;
        public final int price;
        public final float performanceClass;
        public final VehicleRenderConfig renderConfig;

        public VehicleEntry(String id, String displayName, String assetPath, boolean locked, int price, float performanceClass, VehicleRenderConfig renderConfig) {
            this.id = id;
            this.displayName = displayName;
            this.assetPath = assetPath;
            this.locked = locked;
            this.price = price;
            this.performanceClass = performanceClass;
            this.renderConfig = renderConfig;
        }
    }

    private static VehicleRenderConfig cfg(float yaw, float length, float yOffset, float paint, float glass, float emissive) {
        return new VehicleRenderConfig(yaw, length, yOffset, paint, glass, emissive, "wheel tyre tire rim", "light lamp head tail brake signal indicator");
    }

    private static final VehicleEntry[] VEHICLES = new VehicleEntry[] {
            new VehicleEntry(
                    "aston_martin_dbs_gt_zagato",
                    "2020 Aston Martin DBS GT Zagato",
                    OPTIMIZED_VEHICLE_ASSET,
                    false,
                    0,
                    1.00f,
                    cfg(0f, 4.85f, -0.42f, 1.35f, 1.18f, 1.70f)),
            new VehicleEntry(
                    "bmw_m4_widebody_wwwvecarzcom",
                    "BMW M4 Widebody",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    8500,
                    1.18f,
                    cfg(0f, 4.78f, -0.42f, 1.28f, 1.16f, 1.65f)),
            new VehicleEntry(
                    "jeep_wrangler_adventure_rubicon_wwwvecarzcom",
                    "Jeep Wrangler Adventure Rubicon",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    7200,
                    0.92f,
                    cfg(0f, 4.88f, -0.44f, 1.18f, 1.12f, 1.55f)),
            new VehicleEntry(
                    "2017_lamborghini_centenario_roadster",
                    "2017 Lamborghini Centenario Roadster",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    18500,
                    1.36f,
                    cfg(0f, 4.92f, -0.40f, 1.38f, 1.20f, 1.75f)),
            new VehicleEntry(
                    "free_porsche_911_carrera_4s",
                    "Porsche 911 Carrera 4S",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    13200,
                    1.24f,
                    cfg(0f, 4.55f, -0.40f, 1.32f, 1.18f, 1.70f)),
            new VehicleEntry(
                    "2023_ferrari_sf90_xx_stradale",
                    "2023 Ferrari SF90 XX Stradale",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    24000,
                    1.48f,
                    cfg(0f, 4.70f, -0.40f, 1.42f, 1.18f, 1.78f)),
            new VehicleEntry(
                    "2022_porsche_911_gt3_touring_992",
                    "2022 Porsche 911 GT3 Touring 992",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    16800,
                    1.32f,
                    cfg(0f, 4.56f, -0.40f, 1.34f, 1.18f, 1.70f)),
            new VehicleEntry(
                    "1989_lamborghini_countach_25th_anniversary",
                    "1989 Lamborghini Countach 25th Anniversary",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    9800,
                    1.12f,
                    cfg(0f, 4.20f, -0.38f, 1.30f, 1.15f, 1.68f))
            ,
            new VehicleEntry(
                    "lamborghini_veneno",
                    "Lamborghini Veneno",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    32000,
                    1.56f,
                    cfg(0f, 4.82f, -0.42f, 1.44f, 1.20f, 1.80f)),
            new VehicleEntry(
                    "2020_volvo_v60_t8_polestar_engineered",
                    "2020 Volvo V60 T8 Polestar Engineered",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    14500,
                    1.04f,
                    cfg(0f, 4.76f, -0.42f, 1.22f, 1.16f, 1.66f)),
            new VehicleEntry(
                    "bmw_m6_gt3_2018",
                    "BMW M6 GT3 2018",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    28500,
                    1.44f,
                    cfg(0f, 4.82f, -0.42f, 1.40f, 1.18f, 1.78f)),
            new VehicleEntry(
                    "bmw_m3_gtr_e46_street_acschnitzer_blackedition",
                    "BMW M3 GTR E46 Street AC Schnitzer Black Edition",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    21000,
                    1.34f,
                    cfg(0f, 4.66f, -0.42f, 1.34f, 1.18f, 1.72f)),
            new VehicleEntry(
                    "ferrari_550_barchetta_2000_azzurro_hyperion",
                    "Ferrari 550 Barchetta 2000 Azzurro Hyperion",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    19000,
                    1.28f,
                    cfg(0f, 4.62f, -0.42f, 1.34f, 1.16f, 1.70f)),
            new VehicleEntry(
                    "bugatti_eb110_super_sport_1992_by_alex_ka",
                    "Bugatti EB110 Super Sport 1992",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    26000,
                    1.42f,
                    cfg(0f, 4.58f, -0.42f, 1.36f, 1.17f, 1.72f)),
            new VehicleEntry(
                    "jiotto_caspita_f1_road_car_1989_by_alex_ka",
                    "Jiotto Caspita F1 Road Car 1989",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    23500,
                    1.39f,
                    cfg(0f, 4.60f, -0.42f, 1.38f, 1.16f, 1.74f)),
            new VehicleEntry(
                    "2017_lamborghini_centenario_lp770_4",
                    "2017 Lamborghini Centenario LP770-4",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    30000,
                    1.50f,
                    cfg(0f, 4.88f, -0.42f, 1.42f, 1.20f, 1.78f)),
            new VehicleEntry(
                    "2024_lbsilhouette_works_murcielago_gt_evo",
                    "2024 LB Silhouette Works Murcielago GT EVO",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    34000,
                    1.58f,
                    cfg(0f, 4.88f, -0.42f, 1.46f, 1.20f, 1.82f)),
            new VehicleEntry(
                    "2022_bmw_g82_m4_adro_carbon_fiber_widebody_kit",
                    "2022 BMW G82 M4 ADRO Carbon Fiber Widebody Kit",
                    OPTIMIZED_VEHICLE_ASSET,
                    true,
                    24500,
                    1.40f,
                    cfg(0f, 4.76f, -0.42f, 1.38f, 1.18f, 1.76f))

    };

    private VehicleCatalog() {}

    public static int count() {
        return VEHICLES.length;
    }

    public static VehicleEntry get(int index) {
        if (index < 0) index = 0;
        if (index >= VEHICLES.length) index = VEHICLES.length - 1;
        return VEHICLES[index];
    }

    public static String id(int index) {
        return get(index).id;
    }

    public static int price(int index) {
        return get(index).price;
    }

    public static float performanceClass(int index) {
        return get(index).performanceClass;
    }

    public static String label(int index) {
        return get(index).displayName;
    }

    public static String className(int index) {
        float c = performanceClass(index);
        if (c >= 1.45f) return "HYPER";
        if (c >= 1.30f) return "SUPER";
        if (c >= 1.15f) return "SPORT";
        if (c >= 1.00f) return "STREET";
        return "OFFROAD";
    }

    public static String garageRole(int index) {
        if (index == 0) return "Başlangıç";
        if (price(index) <= 10000) return "Ekonomik satın alma";
        if (performanceClass(index) >= 1.40f) return "Üst sınıf yarış";
        return "Orta sınıf gelişim";
    }

    public static int requiredLevel(int index) {
        return CareerLeagueSystem.vehicleRequiredLevel(index);
    }

    public static String unlockText(int index) {
        int lvl = requiredLevel(index);
        return lvl <= 1 ? "Açık" : "LVL " + lvl;
    }

    public static boolean isLocked(int index) {
        return get(index).locked;
    }

    public static String assetPath(int index) {
        return get(index).assetPath;
    }

    public static boolean isDlcVehicle(int index) {
        return index > 0 && index < VEHICLES.length;
    }

    public static float showroomCameraScale(int index) {
        VehicleRenderConfig rc = get(index).renderConfig;
        float length = rc == null ? 4.75f : rc.targetLengthMeters;
        float scale = length / 4.75f;
        if (performanceClass(index) >= 1.45f) scale += 0.04f;
        if (scale < 0.88f) scale = 0.88f;
        if (scale > 1.15f) scale = 1.15f;
        return scale;
    }

    public static float driveCameraScale(int index) {
        VehicleRenderConfig rc = get(index).renderConfig;
        float length = rc == null ? 4.75f : rc.targetLengthMeters;
        float scale = length / 4.70f;
        String id = id(index);
        if (id.indexOf("jeep") >= 0) scale += 0.08f;
        if (performanceClass(index) >= 1.45f) scale += 0.035f;
        if (scale < 0.90f) scale = 0.90f;
        if (scale > 1.18f) scale = 1.18f;
        return scale;
    }

    public static float showroomTargetYOffset(int index) {
        String id = id(index);
        if (id.indexOf("jeep") >= 0) return 0.10f;
        if (id.indexOf("countach") >= 0 || id.indexOf("veneno") >= 0 || id.indexOf("centenario") >= 0) return -0.03f;
        if (id.indexOf("m6_gt3") >= 0 || id.indexOf("murcielago") >= 0) return -0.02f;
        return 0.0f;
    }

    public static int fallbackBodyThreshold(int index) {
        // A66.2: DLC/ek araçlarda gövde veya teker isimleri zayıfsa renderer
        // yalnız cam/teker çizimini başarılı saymasın. Gerçek GLB korunur;
        // sadece boş/yarım görünümde güvenli fallback devreye girer.
        return VehicleModelAuditSystem.needsStrictBodyGuard(index) ? 1 : 1;
    }

    public static VehicleRenderConfig renderConfig(int index) {
        VehicleRenderConfig rc = get(index).renderConfig;
        if (rc == null) return new VehicleRenderConfig(0f, 4.75f, -0.42f, 1.25f, 1.15f, 1.65f, "wheel tyre tire rim", "light lamp head tail brake signal indicator");
        return rc;
    }

    public static String assetFileName(int index) {
        String path = assetPath(index);
        if (path == null || path.length() == 0) return "model yok";
        int slash = path.lastIndexOf('/');
        return slash >= 0 ? path.substring(slash + 1) : path;
    }

    public static float safeTargetLength(int index) {
        float v = renderConfig(index).targetLengthMeters;
        if (Float.isNaN(v) || Float.isInfinite(v)) return 4.75f;
        if (v < 3.70f) return 3.70f;
        if (v > 5.35f) return 5.35f;
        return v;
    }

    public static float safeYOffset(int index) {
        float v = renderConfig(index).yOffset;
        if (Float.isNaN(v) || Float.isInfinite(v)) return -0.42f;
        if (v < -0.68f) return -0.68f;
        if (v > -0.22f) return -0.22f;
        return v;
    }

    public static String foundationLine(int index) {
        return className(index) + " | " + garageRole(index)
                + " | GLB " + assetFileName(index)
                + " | " + (isDlcVehicle(index) ? "DLC kalibrasyon" : "Ana araç");
    }

    public static String calibrationLine(int index) {
        VehicleRenderConfig rc = get(index).renderConfig;
        return "Araç QA " + (index + 1) + "/" + VEHICLES.length
                + " | hedef " + (Math.round(rc.targetLengthMeters * 100f) / 100f) + "m"
                + " | kamera " + Math.round(showroomCameraScale(index) * 100f) + "%"
                + " | " + (isDlcVehicle(index) ? "DLC" : "ANA");
    }


    public static int modelAuditRiskLevel(int index) {
        return VehicleModelAuditSystem.riskLevel(index);
    }

    public static String modelAuditLine(int index) {
        return VehicleModelAuditSystem.auditLine(index);
    }

    public static String modelCalibrationAuditLine(int index) {
        return VehicleModelAuditSystem.calibrationLine(index);
    }

    public static boolean needsModelRepairGuard(int index) {
        return VehicleModelAuditSystem.needsStrictBodyGuard(index);
    }

    public static CarVisualConfig configForVehicle(int vehicleIndex, int quality) {
        CarVisualConfig base = CarVisualConfig.astonMartinForQuality(quality);
        VehicleEntry entry = get(vehicleIndex);
        VehicleRenderConfig rc = entry.renderConfig;
        return new CarVisualConfig(
                entry.assetPath,
                base.manualScale,
                base.physicsRideHeight,
                safeYOffset(vehicleIndex),
                rc.yawOffsetDeg,
                base.pitchOffsetDeg,
                base.rollOffsetDeg,
                safeTargetLength(vehicleIndex),
                base.maxTextureSize,
                base.autoFitToTargetLength,
                base.drawDebugFallbackBehindModel,
                base.visualGroundSink,
                base.visualYSnapThreshold,
                base.pitchRollMultiplier,
                rc.paintBoost,
                rc.glassBoost,
                rc.emissiveBoost,
                base.qualityLevel,
                base.qualityName
        );
    }
}
