package com.arabaoyunu.render;

/**
 * Gercek 3D arac modelinin fizik govdesine nasil oturacagini belirleyen ayarlar.
 * Fizik VehicleController icinde kalir; GLB sadece gorsel temsil olarak kullanilir.
 */
public final class CarVisualConfig {
    public static final int QUALITY_LOW = 0;
    public static final int QUALITY_MEDIUM = 1;
    public static final int QUALITY_HIGH = 2;
    public static final int QUALITY_ULTRA = 3;

    public final String assetPath;
    public final float manualScale;
    public final float physicsRideHeight;
    public final float yOffset;
    public final float yawOffsetDeg;
    public final float pitchOffsetDeg;
    public final float rollOffsetDeg;
    public final float targetLengthMeters;
    public final int maxTextureSize;
    public final boolean autoFitToTargetLength;
    public final boolean drawDebugFallbackBehindModel;

    // Gorsel stabilizasyon ve kalite ayarlari
    public final float visualGroundSink;
    public final float visualYSnapThreshold;
    public final float pitchRollMultiplier;
    public final float bodyPaintBoost;
    public final float glassBoost;
    public final float emissiveBoost;
    public final int qualityLevel;
    public final String qualityName;

    public CarVisualConfig(
            String assetPath,
            float manualScale,
            float physicsRideHeight,
            float yOffset,
            float yawOffsetDeg,
            float pitchOffsetDeg,
            float rollOffsetDeg,
            float targetLengthMeters,
            int maxTextureSize,
            boolean autoFitToTargetLength,
            boolean drawDebugFallbackBehindModel,
            float visualGroundSink,
            float visualYSnapThreshold,
            float pitchRollMultiplier,
            float bodyPaintBoost,
            float glassBoost,
            float emissiveBoost,
            int qualityLevel,
            String qualityName
    ) {
        this.assetPath = assetPath;
        this.manualScale = manualScale;
        this.physicsRideHeight = physicsRideHeight;
        this.yOffset = yOffset;
        this.yawOffsetDeg = yawOffsetDeg;
        this.pitchOffsetDeg = pitchOffsetDeg;
        this.rollOffsetDeg = rollOffsetDeg;
        this.targetLengthMeters = targetLengthMeters;
        this.maxTextureSize = maxTextureSize;
        this.autoFitToTargetLength = autoFitToTargetLength;
        this.drawDebugFallbackBehindModel = drawDebugFallbackBehindModel;
        this.visualGroundSink = visualGroundSink;
        this.visualYSnapThreshold = visualYSnapThreshold;
        this.pitchRollMultiplier = pitchRollMultiplier;
        this.bodyPaintBoost = bodyPaintBoost;
        this.glassBoost = glassBoost;
        this.emissiveBoost = emissiveBoost;
        this.qualityLevel = qualityLevel;
        this.qualityName = qualityName == null ? "YUKSEK" : qualityName;
    }

    public static CarVisualConfig astonMartinDefault() {
        return astonMartinForQuality(QUALITY_HIGH);
    }

    public static CarVisualConfig astonMartinForQuality(int quality) {
        int q = quality;
        if (q < QUALITY_LOW || q > QUALITY_ULTRA) q = QUALITY_HIGH;

        int textureSize;
        float paintBoost;
        String name;
        if (q == QUALITY_LOW) {
            textureSize = 512;
            paintBoost = 0.92f;
            name = "DUSUK";
        } else if (q == QUALITY_MEDIUM) {
            textureSize = 1024;
            paintBoost = 1.10f;
            name = "ORTA";
        } else if (q == QUALITY_ULTRA) {
            // GLB icindeki en yuksek gomu texture kalitesini hedefler.
            // Cihaz GL_MAX_TEXTURE_SIZE daha dusukse TextureCache otomatik indirger.
            textureSize = 4096;
            paintBoost = 1.72f;
            name = "ULTRA";
        } else {
            textureSize = 2048;
            paintBoost = 1.35f;
            name = "YUKSEK";
        }

        return new CarVisualConfig(
                "models/car_main.glb",
                1.0f,
                0.42f,
                -0.42f,
                0.0f,
                0.0f,
                0.0f,
                4.85f,
                textureSize,
                true,
                false,
                -0.035f,
                0.018f,
                0.42f,
                paintBoost,
                1.18f,
                1.65f,
                q,
                name
        );
    }
}
