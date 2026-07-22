package com.arabaoyunu.vehicle;

/**
 * Tek optimize GLB arac icin statik model denetim katmani.
 * Katalogdaki oyun/ekonomi kimlikleri korunur; hepsi ayni doğrulanmis model
 * metriklerini ve fallback kararini kullanir.
 */
public final class VehicleModelAuditSystem {
    public static final int SCHEMA_VERSION = 662;

    private static final class AuditEntry {
        final String status; final float fileMb; final int nodes; final int meshes; final int primitives;
        final int materials; final int images; final int textures; final int wheelHints; final int paintHints;
        final int glassHints; final int lightHints; final int propHints; final float sizeX; final float sizeY; final float sizeZ; final int riskLevel; final String riskLabel;
        AuditEntry(String status, float fileMb, int nodes, int meshes, int primitives, int materials, int images, int textures, int wheelHints, int paintHints, int glassHints, int lightHints, int propHints, float sizeX, float sizeY, float sizeZ, int riskLevel, String riskLabel) {
            this.status=status; this.fileMb=fileMb; this.nodes=nodes; this.meshes=meshes; this.primitives=primitives; this.materials=materials; this.images=images; this.textures=textures; this.wheelHints=wheelHints; this.paintHints=paintHints; this.glassHints=glassHints; this.lightHints=lightHints; this.propHints=propHints; this.sizeX=sizeX; this.sizeY=sizeY; this.sizeZ=sizeZ; this.riskLevel=riskLevel; this.riskLabel=riskLabel;
        }
    }

    private static final AuditEntry[] AUDIT = new AuditEntry[] {
        new AuditEntry("asset_var", 35.13f, 643, 204, 204, 54, 31, 31, 65, 95, 49, 73, 13, 2.183f, 1.305f, 4.798f, 0, "DUSUK"),
    };

    private VehicleModelAuditSystem() {}

    private static AuditEntry entry(int vehicleIndex) {
        return AUDIT[0];
    }

    public static int count() { return AUDIT.length; }

    public static int riskLevel(int vehicleIndex) { return entry(vehicleIndex).riskLevel; }

    public static boolean needsStrictBodyGuard(int vehicleIndex) {
        AuditEntry e = entry(vehicleIndex);
        return e.riskLevel >= 1 || e.paintHints <= 1 || e.wheelHints <= 1 || e.propHints > e.paintHints + e.wheelHints + e.glassHints;
    }

    public static boolean hasWeakWheelNaming(int vehicleIndex) { return entry(vehicleIndex).wheelHints <= 1; }

    public static boolean hasWeakBodyNaming(int vehicleIndex) { return entry(vehicleIndex).paintHints <= 1; }

    public static float rawLargestAxis(int vehicleIndex) {
        AuditEntry e = entry(vehicleIndex);
        return Math.max(e.sizeX, Math.max(e.sizeY, e.sizeZ));
    }

    public static String riskLabel(int vehicleIndex) { return entry(vehicleIndex).riskLabel; }

    public static String auditLine(int vehicleIndex) {
        AuditEntry e = entry(vehicleIndex);
        return "Model QA: " + healthLabel(vehicleIndex)
                + " | mesh " + e.meshes + "/" + e.primitives
                + " | body " + e.paintHints
                + " | wheel " + e.wheelHints
                + " | risk " + e.riskLabel;
    }

    public static String compactAssetLine(int vehicleIndex) {
        AuditEntry e = entry(vehicleIndex);
        return "GLB " + e.fileMb + "MB | node " + e.nodes + " | mat " + e.materials + " | tex " + e.textures;
    }

    public static String calibrationLine(int vehicleIndex) {
        AuditEntry e = entry(vehicleIndex);
        int guard = needsStrictBodyGuard(vehicleIndex) ? 1 : 0;
        return "Kalibrasyon: bounds " + one(e.sizeX) + "x" + one(e.sizeY) + "x" + one(e.sizeZ)
                + " | bodyGuard " + (guard == 1 ? "aktif" : "normal")
                + " | " + repairHint(vehicleIndex);
    }

    public static String repairHint(int vehicleIndex) {
        AuditEntry e = entry(vehicleIndex);
        if (e.wheelHints <= 1 && e.paintHints <= 1) return "isim zayıf: gövde/teker filtre gevşek";
        if (e.wheelHints <= 1) return "teker ismi zayıf: konum/rubber refine";
        if (e.paintHints <= 1) return "gövde ismi zayıf: non-wheel gövde korunur";
        if (e.propHints > e.paintHints + e.wheelHints + e.glassHints) return "prop baskın: zemin/platform filtre dikkatli";
        return "düşük risk";
    }

    public static String healthLabel(int vehicleIndex) {
        int r = riskLevel(vehicleIndex);
        if (r <= 0) return "SAĞLAM";
        if (r == 1) return "İZLEME";
        return "ONARIM";
    }

    private static String one(float v) { return String.valueOf(Math.round(v * 10f) / 10f); }
}
