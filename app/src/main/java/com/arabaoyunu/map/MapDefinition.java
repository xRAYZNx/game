package com.arabaoyunu.map;

/**
 * ArabaOyunu_61_1: Büyük GLB harita gelmeden önce her haritanın metadata'sını
 * tek yerde tutan güvenli kayıt modeli. Harita dosyası eklenmeden fizik veya
 * spawn sistemi değiştirilmez; bu sınıf sadece entegrasyon ayarlarını taşır.
 */
public final class MapDefinition {
    public final int id;
    public final String internalName;
    public final String displayName;
    public final String description;
    public final String assetPath;
    public final int expectedSizeMb;
    public final float spawnX;
    public final float spawnY;
    public final float spawnZ;
    public final float spawnYaw;
    public final float groundY;
    public final float visualScale;
    public final float mapHalfSize;
    public final boolean builtInPrimitive;
    public final boolean externalGlb;
    public final boolean openWorld;
    public final boolean testDriveSupported;
    public final boolean trafficSupported;
    public final boolean selectableNow;
    public final int recommendedQuality;

    public MapDefinition(
            int id,
            String internalName,
            String displayName,
            String description,
            String assetPath,
            int expectedSizeMb,
            float spawnX,
            float spawnY,
            float spawnZ,
            float spawnYaw,
            float groundY,
            float visualScale,
            float mapHalfSize,
            boolean builtInPrimitive,
            boolean externalGlb,
            boolean openWorld,
            boolean testDriveSupported,
            boolean trafficSupported,
            boolean selectableNow,
            int recommendedQuality) {
        this.id = id;
        this.internalName = safe(internalName, "MAP");
        this.displayName = safe(displayName, this.internalName);
        this.description = safe(description, "");
        this.assetPath = assetPath == null ? "" : assetPath;
        this.expectedSizeMb = Math.max(0, expectedSizeMb);
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        this.spawnZ = spawnZ;
        this.spawnYaw = spawnYaw;
        this.groundY = groundY;
        this.visualScale = visualScale <= 0f ? 1f : visualScale;
        this.mapHalfSize = mapHalfSize <= 0f ? 180f : mapHalfSize;
        this.builtInPrimitive = builtInPrimitive;
        this.externalGlb = externalGlb;
        this.openWorld = openWorld;
        this.testDriveSupported = testDriveSupported;
        this.trafficSupported = trafficSupported;
        this.selectableNow = selectableNow;
        this.recommendedQuality = recommendedQuality;
    }

    private static String safe(String value, String fallback) {
        return value == null || value.trim().length() == 0 ? fallback : value;
    }

    public boolean hasAssetPath() {
        return assetPath.length() > 0;
    }

    public boolean isPendingExternalAsset() {
        return externalGlb && !selectableNow;
    }

    public String assetStatusLabel() {
        if (!externalGlb) return "DAHILI";
        if (selectableNow && hasAssetPath()) return "HAZIR";
        return expectedSizeMb > 0 ? expectedSizeMb + " MB DOSYA BEKLIYOR" : "DOSYA BEKLIYOR";
    }

    public String performanceLabel() {
        if (!externalGlb) return "NORMAL";
        if (expectedSizeMb >= 150) return "COK BUYUK / YUKLEME EKRANI GEREKLI";
        if (expectedSizeMb >= 60) return "BUYUK";
        return "ORTA";
    }

    public float miniMapHalfSize() {
        return Math.max(80f, mapHalfSize);
    }

    public String miniMapQaLabel() {
        return displayName + " • ölçek " + (int)miniMapHalfSize() + "m" + (selectableNow ? " • aktif" : " • pasif");
    }
}
