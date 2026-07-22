package com.arabaoyunu.render;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public final class GlbMeshPart {
    public static final int FLOATS_PER_VERTEX = 8;
    public static final int STRIDE_BYTES = FLOATS_PER_VERTEX * 4;

    public String name = "mesh";
    public String nodeName = "node";
    public String semanticName = "part";

    public float[] vertices;
    public short[] indices;
    public float[] localMatrix = new float[16];
    public int materialIndex;
    public int vertexCount;
    public int indexCount;

    public float minX = Float.MAX_VALUE;
    public float minY = Float.MAX_VALUE;
    public float minZ = Float.MAX_VALUE;
    public float maxX = -Float.MAX_VALUE;
    public float maxY = -Float.MAX_VALUE;
    public float maxZ = -Float.MAX_VALUE;

    public float worldMinX = Float.MAX_VALUE;
    public float worldMinY = Float.MAX_VALUE;
    public float worldMinZ = Float.MAX_VALUE;
    public float worldMaxX = -Float.MAX_VALUE;
    public float worldMaxY = -Float.MAX_VALUE;
    public float worldMaxZ = -Float.MAX_VALUE;

    public int vertexBufferId;
    public int indexBufferId;
    public boolean gpuReady;
    public boolean skipped;
    public String error;

    public void includeLocal(float x, float y, float z) {
        if (x < minX) minX = x;
        if (y < minY) minY = y;
        if (z < minZ) minZ = z;
        if (x > maxX) maxX = x;
        if (y > maxY) maxY = y;
        if (z > maxZ) maxZ = z;
    }

    public float centerX() { return (minX + maxX) * 0.5f; }
    public float centerY() { return (minY + maxY) * 0.5f; }
    public float centerZ() { return (minZ + maxZ) * 0.5f; }

    public boolean hasWorldBounds() {
        return worldMinX != Float.MAX_VALUE && worldMaxX != -Float.MAX_VALUE;
    }

    public float worldSizeX() { return hasWorldBounds() ? worldMaxX - worldMinX : 0f; }
    public float worldSizeY() { return hasWorldBounds() ? worldMaxY - worldMinY : 0f; }
    public float worldSizeZ() { return hasWorldBounds() ? worldMaxZ - worldMinZ : 0f; }
    public float worldCenterX() { return hasWorldBounds() ? (worldMinX + worldMaxX) * 0.5f : 0f; }
    public float worldCenterY() { return hasWorldBounds() ? (worldMinY + worldMaxY) * 0.5f : 0f; }
    public float worldCenterZ() { return hasWorldBounds() ? (worldMinZ + worldMaxZ) * 0.5f : 0f; }

    public boolean isSceneProp() {
        return "scene_prop".equals(semanticName);
    }

    public boolean isWheel() {
        return semanticName != null && semanticName.startsWith("wheel");
    }

    public boolean isFrontWheel() {
        return "wheel_fl".equals(semanticName) || "wheel_fr".equals(semanticName);
    }

    public void uploadToGpu() {
        if (vertices == null || indices == null || vertexBufferId != 0 || indexBufferId != 0) {
            return;
        }

        try {
            int[] ids = new int[2];
            GLES20.glGenBuffers(2, ids, 0);
            vertexBufferId = ids[0];
            indexBufferId = ids[1];

            if (vertexBufferId == 0 || indexBufferId == 0) {
                throw new RuntimeException("glGenBuffers 0 dondurdu");
            }

            FloatBuffer vb = ByteBuffer
                    .allocateDirect(vertices.length * 4)
                    .order(ByteOrder.nativeOrder())
                    .asFloatBuffer();
            vb.put(vertices).position(0);

            ShortBuffer ib = ByteBuffer
                    .allocateDirect(indices.length * 2)
                    .order(ByteOrder.nativeOrder())
                    .asShortBuffer();
            ib.put(indices).position(0);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vertexBufferId);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertices.length * 4, vb, GLES20.GL_STATIC_DRAW);
            checkGlError("vertex buffer upload");

            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, indexBufferId);
            GLES20.glBufferData(GLES20.GL_ELEMENT_ARRAY_BUFFER, indices.length * 2, ib, GLES20.GL_STATIC_DRAW);
            checkGlError("index buffer upload");

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
            GLES20.glBindBuffer(GLES20.GL_ELEMENT_ARRAY_BUFFER, 0);

            vertices = null;
            indices = null;
            gpuReady = true;
        } catch (Throwable t) {
            error = t.getMessage();
            skipped = true;
            gpuReady = false;
            dispose();
            vertices = null;
            indices = null;
        }
    }

    public void dispose() {
        int[] ids = new int[1];
        if (vertexBufferId != 0) {
            ids[0] = vertexBufferId;
            GLES20.glDeleteBuffers(1, ids, 0);
            vertexBufferId = 0;
        }
        if (indexBufferId != 0) {
            ids[0] = indexBufferId;
            GLES20.glDeleteBuffers(1, ids, 0);
            indexBufferId = 0;
        }
    }

    private static void checkGlError(String stage) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(stage + " GL error=" + error);
        }
    }
}
