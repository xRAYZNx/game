package com.arabaoyunu.garage;

import com.arabaoyunu.render.CarVisualConfig;
import com.arabaoyunu.vehicle.VehicleCatalog;

/**
 * A66.8: Gerçek 3D showroom sunum katmanı.
 *
 * Yeni showroom modeli üretmez; projedeki mevcut
 * models/showroom/scifi_tron_studio__baked.glb asset'ini ana sahne olarak kullanır.
 * Kamera, ışık/kontrast boost ve araç ölçek/zemin uyumunu tek merkezden raporlar.
 */
public final class ShowroomPresentationSystem {
    public static final int SCHEMA_VERSION = 668;
    public static final String SHOWROOM_ASSET = "models/showroom/scifi_tron_studio__baked.glb";

    private ShowroomPresentationSystem() {}

    public static CarVisualConfig showroomConfigForQuality(int quality) {
        int q = quality;
        if (q < CarVisualConfig.QUALITY_LOW || q > CarVisualConfig.QUALITY_ULTRA) {
            q = CarVisualConfig.QUALITY_HIGH;
        }
        int textureCap;
        float paintBoost;
        float glassBoost;
        float emissiveBoost;
        String qualityName;
        if (q == CarVisualConfig.QUALITY_LOW) {
            textureCap = 768;
            paintBoost = 1.10f;
            glassBoost = 1.08f;
            emissiveBoost = 1.38f;
            qualityName = "SHOWROOM_A66_8_LOW";
        } else if (q == CarVisualConfig.QUALITY_MEDIUM) {
            textureCap = 1024;
            paintBoost = 1.18f;
            glassBoost = 1.12f;
            emissiveBoost = 1.50f;
            qualityName = "SHOWROOM_A66_8_MEDIUM";
        } else if (q == CarVisualConfig.QUALITY_ULTRA) {
            textureCap = 2048;
            paintBoost = 1.32f;
            glassBoost = 1.20f;
            emissiveBoost = 1.78f;
            qualityName = "SHOWROOM_A66_8_ULTRA";
        } else {
            textureCap = 1536;
            paintBoost = 1.24f;
            glassBoost = 1.15f;
            emissiveBoost = 1.62f;
            qualityName = "SHOWROOM_A66_8_HIGH";
        }

        return new CarVisualConfig(
                SHOWROOM_ASSET,
                1.0f,
                0f,
                sceneYOffset(),
                0f,
                0f,
                0f,
                sceneTargetLength(),
                textureCap,
                true,
                false,
                sceneGroundSink(),
                0.018f,
                1.0f,
                paintBoost,
                glassBoost,
                emissiveBoost,
                q,
                qualityName
        );
    }

    public static float sceneTargetLength() {
        // Gerçek showroom asset'i araçtan daha büyük bir çevre olduğu için geniş hedef uzunluk kullanılır.
        return 16.4f;
    }

    public static float sceneYOffset() {
        // Showroom zemini araç tekerlerinin altında kalacak şekilde hafif aşağıda tutulur.
        return -0.055f;
    }

    public static float sceneGroundSink() {
        return -0.010f;
    }

    public static float renderY() {
        return -0.055f;
    }

    public static float renderScaleHint() {
        return 100f;
    }

    public static String stageLine() {
        return "Gerçek showroom: scifi_tron_studio__baked | ana sahne | fallback sadece son çare";
    }

    public static String lightingLine(int quality) {
        int q = quality;
        if (q < CarVisualConfig.QUALITY_LOW || q > CarVisualConfig.QUALITY_ULTRA) q = CarVisualConfig.QUALITY_HIGH;
        if (q == CarVisualConfig.QUALITY_LOW) return "Işık QA: düşük cihaz için hafif emissive + sade kontrast";
        if (q == CarVisualConfig.QUALITY_MEDIUM) return "Işık QA: orta kalite dengeli parlaklık + cam/emissive boost";
        if (q == CarVisualConfig.QUALITY_ULTRA) return "Işık QA: ultra kalite yüksek showroom kontrastı + güçlü neon/emissive";
        return "Işık QA: yüksek kalite dengeli showroom kontrastı + net araç silüeti";
    }

    public static String vehicleFitLine(int vehicleIndex) {
        int safe = vehicleIndex;
        if (safe < 0) safe = 0;
        if (safe >= VehicleCatalog.count()) safe = VehicleCatalog.count() - 1;
        return "Showroom fit: kamera " + Math.round(VehicleCatalog.showroomCameraScale(safe) * 100f) + "%"
                + " | hedefY " + Math.round(VehicleCatalog.showroomTargetYOffset(safe) * 100f)
                + " | zemin " + Math.round(VehicleCatalog.safeYOffset(safe) * 100f)
                + " | " + VehicleCatalog.assetFileName(safe);
    }

    public static String finalQaLine(int vehicleIndex, int quality) {
        return vehicleFitLine(vehicleIndex) + " | " + lightingLine(quality);
    }
}
