package com.arabaoyunu.render;

import java.util.ArrayList;
import java.util.List;

public final class GlbModel {
    public final List<GlbMeshPart> parts = new ArrayList<GlbMeshPart>();
    public final List<GlbMaterial> materials = new ArrayList<GlbMaterial>();
    public final List<byte[]> images = new ArrayList<byte[]>();

    public String sourceAsset;
    public int totalVertices;
    public int totalIndices;

    public float minX = Float.MAX_VALUE;
    public float minY = Float.MAX_VALUE;
    public float minZ = Float.MAX_VALUE;
    public float maxX = -Float.MAX_VALUE;
    public float maxY = -Float.MAX_VALUE;
    public float maxZ = -Float.MAX_VALUE;

    public boolean hasBounds() {
        return minX != Float.MAX_VALUE && maxX != -Float.MAX_VALUE;
    }

    public void clearBounds() {
        minX = Float.MAX_VALUE;
        minY = Float.MAX_VALUE;
        minZ = Float.MAX_VALUE;
        maxX = -Float.MAX_VALUE;
        maxY = -Float.MAX_VALUE;
        maxZ = -Float.MAX_VALUE;
    }

    public void includeBounds(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        if (minX < this.minX) this.minX = minX;
        if (minY < this.minY) this.minY = minY;
        if (minZ < this.minZ) this.minZ = minZ;
        if (maxX > this.maxX) this.maxX = maxX;
        if (maxY > this.maxY) this.maxY = maxY;
        if (maxZ > this.maxZ) this.maxZ = maxZ;
    }

    public float sizeX() { return hasBounds() ? maxX - minX : 1f; }
    public float sizeY() { return hasBounds() ? maxY - minY : 1f; }
    public float sizeZ() { return hasBounds() ? maxZ - minZ : 1f; }
    public float centerX() { return hasBounds() ? (minX + maxX) * 0.5f : 0f; }
    public float centerY() { return hasBounds() ? (minY + maxY) * 0.5f : 0f; }
    public float centerZ() { return hasBounds() ? (minZ + maxZ) * 0.5f : 0f; }

    public void releaseImageData() {
        for (int i = 0; i < images.size(); i++) {
            images.set(i, null);
        }
    }
}
