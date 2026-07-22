package com.arabaoyunu.garage;

import com.arabaoyunu.customization.VisualCustomizationSystem;
import com.arabaoyunu.customization.VehicleCustomizationSystem;
import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.performance.VehicleUpgradeSystem;
import com.arabaoyunu.performance.PerformanceUpgradeBalanceSystem;
import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * A63.2: Garaj 3D önizleme / showroom koordinatörü.
 *
 * Bu sınıf sadece garaj önizleme kamerası, showroom sahne etiketi, renk/neon
 * verisi ve performans karşılaştırma metinlerini üretir. Gerçek araç GLB'si
 * kullanıcı tarafından gönderilene kadar mevcut fallback/GLB araç renderer'ı
 * güvenli şekilde kullanılmaya devam eder.
 */
public final class GarageShowroomSystem {
    public static final int SCHEMA_VERSION = 673;
    public static final float MIN_ZOOM = 0.74f;
    public static final float MAX_ZOOM = 1.30f;
    public static final float DEFAULT_ZOOM = 1.00f;

    public static final class CameraProfile {
        public final float eyeX;
        public final float eyeY;
        public final float eyeZ;
        public final float targetX;
        public final float targetY;
        public final float targetZ;

        public CameraProfile(float eyeX, float eyeY, float eyeZ, float targetX, float targetY, float targetZ) {
            this.eyeX = eyeX;
            this.eyeY = eyeY;
            this.eyeZ = eyeZ;
            this.targetX = targetX;
            this.targetY = targetY;
            this.targetZ = targetZ;
        }
    }

    private GarageShowroomSystem() {}

    public static float clampZoom(float value) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return DEFAULT_ZOOM;
        if (value < MIN_ZOOM) return MIN_ZOOM;
        if (value > MAX_ZOOM) return MAX_ZOOM;
        return value;
    }

    public static float stepZoom(float current, int direction) {
        float v = clampZoom(current);
        v += direction * 0.10f;
        return clampZoom(v);
    }

    public static String zoomLabel(float zoom) {
        int percent = Math.round(clampZoom(zoom) * 100f);
        return percent + "%";
    }

    public static CameraProfile cameraFor(int garageMode, int visualType, float zoom, float carX, float carY, float carZ) {
        return cameraFor(garageMode, visualType, zoom, carX, carY, carZ, -1);
    }

    public static CameraProfile cameraFor(int garageMode, int visualType, float zoom, float carX, float carY, float carZ, int vehicleIndex) {
        float z = clampZoom(zoom);
        float vehicleCameraScale = VehicleCatalog.showroomCameraScale(vehicleIndex);
        float targetLift = VehicleCatalog.showroomTargetYOffset(vehicleIndex);
        float eyeX = carX;
        float eyeY = 2.32f;
        float eyeZ = 8.10f;
        float targetX = carX;
        float targetY = 0.82f;
        float targetZ = carZ;

        if (garageMode == GameScreenState.GARAGE_MODE_BUY_CONFIRM) {
            eyeX = carX + 0.18f;
            eyeY = 2.02f;
            eyeZ = 6.20f;
            targetY = 0.84f;
        } else if (garageMode == GameScreenState.GARAGE_MODE_MODIFY_HOME) {
            eyeX = carX + 0.10f;
            eyeY = 2.06f;
            eyeZ = 6.85f;
            targetY = 0.76f;
        } else if (garageMode == GameScreenState.GARAGE_MODE_PERFORMANCE
                || garageMode == GameScreenState.GARAGE_MODE_TUNING
                || garageMode == GameScreenState.GARAGE_MODE_VISUAL) {
            eyeX = carX;
            eyeY = 2.02f;
            eyeZ = 6.45f;
            targetY = 0.72f;
        } else if (garageMode == GameScreenState.GARAGE_MODE_VISUAL_EDIT) {
            if (visualType == VisualCustomizationSystem.RIM_STYLE
                    || visualType == VisualCustomizationSystem.RIM_COLOR
                    || visualType == VisualCustomizationSystem.TIRE_SIDEWALL) {
                eyeX = carX + 2.35f;
                eyeY = 0.92f;
                eyeZ = 3.15f;
                targetX = carX + 0.82f;
                targetY = 0.35f;
                targetZ = carZ + 0.08f;
            } else if (visualType == VisualCustomizationSystem.HOOD
                    || visualType == VisualCustomizationSystem.FRONT_BUMPER
                    || visualType == VisualCustomizationSystem.HEADLIGHT_COLOR) {
                eyeX = carX;
                eyeY = 1.42f;
                eyeZ = 3.05f;
                targetX = carX;
                targetY = 0.78f;
                targetZ = carZ - 0.52f;
            } else if (visualType == VisualCustomizationSystem.REAR_BUMPER
                    || visualType == VisualCustomizationSystem.TAIL_LIGHT_STYLE
                    || visualType == VisualCustomizationSystem.PLATE_STYLE
                    || visualType == VisualCustomizationSystem.EXHAUST_TIP
                    || visualType == VisualCustomizationSystem.SPOILER) {
                eyeX = carX;
                eyeY = 1.35f;
                eyeZ = -3.15f;
                targetX = carX;
                targetY = 0.72f;
                targetZ = carZ + 0.52f;
            } else if (visualType == VisualCustomizationSystem.WINDOW_TINT
                    || visualType == VisualCustomizationSystem.MIRROR_COLOR
                    || visualType == VisualCustomizationSystem.ROOF_ACCESSORY) {
                eyeX = carX + 2.15f;
                eyeY = 1.62f;
                eyeZ = 3.00f;
                targetX = carX + 0.30f;
                targetY = 1.05f;
                targetZ = carZ + 0.02f;
            } else {
                eyeX = carX;
                eyeY = 1.82f;
                eyeZ = 5.05f;
                targetY = 0.72f;
            }
        }

        // A64.5: Araçlar farklı export ölçülerinden geldiği için kamera mesafesi
        // ve hedef yüksekliği araç bazlı hafif ölçeklenir; hedef noktası korunur.
        targetY += targetLift;
        eyeY += targetLift * 0.35f;
        eyeZ = targetZ + (eyeZ - targetZ) * vehicleCameraScale;

        // Zoom sadece kamera mesafesini etkiler; hedef noktasını bozmaz.
        eyeX = targetX + (eyeX - targetX) * z;
        eyeY = targetY + (eyeY - targetY) * z;
        eyeZ = targetZ + (eyeZ - targetZ) * z;
        return new CameraProfile(eyeX, eyeY, eyeZ, targetX, targetY, targetZ);
    }

    public static String cameraHint(int garageMode, int visualType) {
        if (garageMode == GameScreenState.GARAGE_MODE_VISUAL_EDIT) {
            if (visualType == VisualCustomizationSystem.RIM_STYLE || visualType == VisualCustomizationSystem.RIM_COLOR) return "Jant zoom";
            if (visualType == VisualCustomizationSystem.WINDOW_TINT || visualType == VisualCustomizationSystem.MIRROR_COLOR) return "Kabin zoom";
            if (visualType == VisualCustomizationSystem.PLATE_STYLE || visualType == VisualCustomizationSystem.REAR_BUMPER) return "Arka zoom";
            return "Gövde zoom";
        }
        if (garageMode == GameScreenState.GARAGE_MODE_PERFORMANCE) return "Performans preview";
        if (garageMode == GameScreenState.GARAGE_MODE_TUNING) return "Tuning preview";
        if (garageMode == GameScreenState.GARAGE_MODE_VISUAL) return "Görsel preview";
        if (garageMode == GameScreenState.GARAGE_MODE_BUY_CONFIRM) return "Satın alma showroom";
        return "360° showroom";
    }

    public static String identityLine(SaveManager saveManager, int vehicleIndex) {
        String id = VehicleCatalog.id(vehicleIndex);
        return VehicleCatalog.className(vehicleIndex)
                + " | " + VehicleCustomizationSystem.garageIdentityLine(saveManager, id);
    }

    public static String professionalWorkshopLine(SaveManager saveManager, int vehicleIndex) {
        String id = VehicleCatalog.id(vehicleIndex);
        VehicleUpgradeSystem.Stats s = VehicleUpgradeSystem.buildStats(saveManager, vehicleIndex);
        return "Build " + VehicleUpgradeSystem.buildScore(saveManager, vehicleIndex) + "/100"
                + " | Performans L" + VehicleUpgradeSystem.totalPerformanceLevel(saveManager, id)
                + " | Hız " + s.speed + " | Drift " + s.drift
                + " | " + PerformanceUpgradeBalanceSystem.tuningSafetyLine(saveManager, vehicleIndex);
    }

    public static String showroomIntro(SaveManager saveManager, int vehicleIndex) {
        VehicleUpgradeSystem.Stats s = VehicleUpgradeSystem.buildStats(saveManager, vehicleIndex);
        return "Hız " + s.speed + "  Hızlanma " + s.acceleration
                + "  Tutuş " + s.handling + "  N2O " + s.nitro;
    }

    public static String selectedPartLine(SaveManager saveManager, int vehicleIndex, int type) {
        String id = VehicleCatalog.id(vehicleIndex);
        int value = saveManager == null ? 0 : saveManager.getVisualModValue(id, type);
        return VisualCustomizationSystem.label(type) + ": " + VisualCustomizationSystem.valueLabel(type, value);
    }

    public static String nextUpgradePreview(SaveManager saveManager, int vehicleIndex, int upgradeType) {
        if (saveManager == null) return "Kayıt yok";
        String id = VehicleCatalog.id(vehicleIndex);
        int level = saveManager.getUpgradeLevel(id, upgradeType);
        if (level >= SaveManager.MAX_UPGRADE_LEVEL) {
            return VehicleUpgradeSystem.shortLabel(upgradeType) + " MAX | " + VehicleUpgradeSystem.effectSummary(upgradeType);
        }
        int cost = saveManager.getUpgradeCost(id, upgradeType);
        return VehicleUpgradeSystem.shortLabel(upgradeType) + " L" + level + "→" + (level + 1)
                + " | $" + cost + " | " + PerformanceUpgradeBalanceSystem.previewLine(upgradeType, level, false);
    }

    public static String showroomQaLine(int vehicleIndex) {
        return GarageInfrastructureSystem.showroomFoundationLine(vehicleIndex) + " | " + ShowroomPresentationSystem.vehicleFitLine(vehicleIndex);
    }

    public static String deviceQaLine(int vehicleIndex) {
        return GarageInfrastructureSystem.catalogFoundationLine(vehicleIndex);
    }

    public static float[] paintRgb(SaveManager saveManager, String vehicleId) {
        int color = 0;
        if (saveManager != null) color = saveManager.getVisualModValue(vehicleId, VisualCustomizationSystem.PAINT_COLOR);
        return rgbFromPreview(VisualCustomizationSystem.previewColor(VisualCustomizationSystem.PAINT_COLOR, color));
    }

    public static float[] neonRgb(SaveManager saveManager, String vehicleId) {
        int neon = 0;
        if (saveManager != null) neon = saveManager.getVisualModValue(vehicleId, VisualCustomizationSystem.NEON);
        return rgbFromPreview(VisualCustomizationSystem.previewColor(VisualCustomizationSystem.NEON, neon));
    }

    public static boolean hasNeon(SaveManager saveManager, String vehicleId) {
        return saveManager != null && saveManager.getVisualModValue(vehicleId, VisualCustomizationSystem.NEON) > 0;
    }

    private static float[] rgbFromPreview(int argb) {
        float r = ((argb >> 16) & 0xff) / 255f;
        float g = ((argb >> 8) & 0xff) / 255f;
        float b = (argb & 0xff) / 255f;
        return new float[] { r, g, b };
    }
}
