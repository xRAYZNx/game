package com.arabaoyunu.render;

import com.arabaoyunu.util.GameLog;

/**
 * GLB arac modelini fizik govdesine guvenli sekilde hizalar.
 * - Transform degerlerini sifirlar.
 * - Modeli kendi bounding box merkezine alir.
 * - Taban noktasini zemine oturtur.
 * - Export birimi cok kucukse otomatik metre olcegine getirir.
 */
public final class VehicleEntityInitializer {
    private static final String TAG = "VehicleEntityInit";

    private VehicleEntityInitializer() {}

    public static VehicleVisualTransform initialize(GlbModel model, CarVisualConfig config) {
        VehicleVisualTransform out = new VehicleVisualTransform();
        if (model == null || !model.hasBounds()) {
            out.scale = Math.max(0.01f, config.manualScale);
            out.yOffset = config.yOffset + config.visualGroundSink;
            return out;
        }

        float sizeX = Math.max(0.0001f, model.sizeX());
        float sizeY = Math.max(0.0001f, model.sizeY());
        float sizeZ = Math.max(0.0001f, model.sizeZ());
        float length = Math.max(sizeX, sizeZ);

        float scale = config.manualScale;
        if (config.autoFitToTargetLength && config.targetLengthMeters > 0.1f) {
            scale = config.targetLengthMeters / length;
        }

        if (scale < 0.01f) scale = 0.01f;
        if (scale > 500f) scale = 500f;

        out.scale = scale;
        out.localCenterX = model.centerX();
        out.localBaseY = model.minY;
        out.localCenterZ = model.centerZ();

        // yOffset = fizik rideHeight iptali + ufak zemin gommesi.
        // Bu sayede model havada durmaz ve fizik Y'deki kucuk snap oynamalari gorselde buyumez.
        out.yOffset = config.yOffset + config.visualGroundSink;

        out.modelSizeX = sizeX * scale;
        out.modelSizeY = sizeY * scale;
        out.modelSizeZ = sizeZ * scale;

        GameLog.i(TAG,
                "Model hizalandi scale=" + scale
                        + " sizeMeters=" + out.modelSizeX + "x" + out.modelSizeY + "x" + out.modelSizeZ
                        + " yOffset=" + out.yOffset
                        + " center=" + out.localCenterX + "," + model.centerY() + "," + out.localCenterZ
                        + " baseY=" + out.localBaseY);
        return out;
    }
}
