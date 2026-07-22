package com.arabaoyunu.render;

public final class VehicleVisualTransform {
    public float scale = 1f;
    public float localCenterX;
    public float localBaseY;
    public float localCenterZ;
    public float yOffset;
    public float modelSizeX;
    public float modelSizeY;
    public float modelSizeZ;

    // Render stabilizasyonu
    public boolean initialized;
    public float smoothedX;
    public float smoothedY;
    public float smoothedZ;
    public float smoothedYaw;
    public float smoothedPitch;
    public float smoothedRoll;
}
