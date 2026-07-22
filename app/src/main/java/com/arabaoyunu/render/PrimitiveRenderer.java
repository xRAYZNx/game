package com.arabaoyunu.render;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class PrimitiveRenderer {

    private final ShaderProgram shader = new ShaderProgram();
    private final float[] identity = new float[16];
    private final float[] model = new float[16];
    private final float[] mvp = new float[16];
    private final float[] temp = new float[16];

    private FloatBuffer dynamicBuffer;
    private FloatBuffer cubeBuffer;
    private int cubeVertexCount;

    public void create() {
        shader.create();
        Matrix.setIdentityM(identity, 0);
        dynamicBuffer = ByteBuffer.allocateDirect(4096 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        createCubeBuffer();
    }

    private void createCubeBuffer() {
        float[] v = new float[] {
                // front
                -0.5f,-0.5f, 0.5f,  0.5f,-0.5f, 0.5f,  0.5f, 0.5f, 0.5f,
                -0.5f,-0.5f, 0.5f,  0.5f, 0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
                // back
                 0.5f,-0.5f,-0.5f, -0.5f,-0.5f,-0.5f, -0.5f, 0.5f,-0.5f,
                 0.5f,-0.5f,-0.5f, -0.5f, 0.5f,-0.5f,  0.5f, 0.5f,-0.5f,
                // left
                -0.5f,-0.5f,-0.5f, -0.5f,-0.5f, 0.5f, -0.5f, 0.5f, 0.5f,
                -0.5f,-0.5f,-0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f,-0.5f,
                // right
                 0.5f,-0.5f, 0.5f,  0.5f,-0.5f,-0.5f,  0.5f, 0.5f,-0.5f,
                 0.5f,-0.5f, 0.5f,  0.5f, 0.5f,-0.5f,  0.5f, 0.5f, 0.5f,
                // top
                -0.5f, 0.5f, 0.5f,  0.5f, 0.5f, 0.5f,  0.5f, 0.5f,-0.5f,
                -0.5f, 0.5f, 0.5f,  0.5f, 0.5f,-0.5f, -0.5f, 0.5f,-0.5f,
                // bottom
                -0.5f,-0.5f,-0.5f,  0.5f,-0.5f,-0.5f,  0.5f,-0.5f, 0.5f,
                -0.5f,-0.5f,-0.5f,  0.5f,-0.5f, 0.5f, -0.5f,-0.5f, 0.5f
        };
        cubeVertexCount = v.length / 3;
        cubeBuffer = ByteBuffer.allocateDirect(v.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        cubeBuffer.put(v).position(0);
    }

    public void drawGround(float[] viewProjection, float halfSize, RenderStats stats) {
        float[] v = new float[] {
                -halfSize, 0f, -halfSize,
                 halfSize, 0f, -halfSize,
                 halfSize, 0f,  halfSize,
                -halfSize, 0f, -halfSize,
                 halfSize, 0f,  halfSize,
                -halfSize, 0f,  halfSize
        };
        drawArray(viewProjection, identity, v, GLES20.GL_TRIANGLES, 0.12f, 0.14f, 0.15f, 1f, stats);
    }

    public void drawGrid(float[] viewProjection, float halfSize, float step, RenderStats stats) {
        for (float i = -halfSize; i <= halfSize; i += step) {
            drawLine(viewProjection, -halfSize, 0.015f, i, halfSize, 0.015f, i, 0.24f, 0.28f, 0.30f, stats);
            drawLine(viewProjection, i, 0.015f, -halfSize, i, 0.015f, halfSize, 0.24f, 0.28f, 0.30f, stats);
        }
        drawLine(viewProjection, -halfSize, 0.025f, 0f, halfSize, 0.025f, 0f, 0.55f, 0.55f, 0.55f, stats);
        drawLine(viewProjection, 0f, 0.025f, -halfSize, 0f, 0.025f, halfSize, 0.38f, 0.48f, 0.58f, stats);
    }

    public void drawBoundary(float[] viewProjection, float halfSize, RenderStats stats) {
        drawLine(viewProjection, -halfSize, 0.05f, -halfSize, halfSize, 0.05f, -halfSize, 0.95f, 0.26f, 0.26f, stats);
        drawLine(viewProjection, halfSize, 0.05f, -halfSize, halfSize, 0.05f, halfSize, 0.95f, 0.26f, 0.26f, stats);
        drawLine(viewProjection, halfSize, 0.05f, halfSize, -halfSize, 0.05f, halfSize, 0.95f, 0.26f, 0.26f, stats);
        drawLine(viewProjection, -halfSize, 0.05f, halfSize, -halfSize, 0.05f, -halfSize, 0.95f, 0.26f, 0.26f, stats);
    }

    public void drawCircle(float[] viewProjection, float x, float y, float z, float radius, RenderStats stats) {
        int segments = 72;
        float[] v = new float[segments * 2 * 3];
        int k = 0;
        for (int i = 0; i < segments; i++) {
            float a0 = (float) (Math.PI * 2.0 * i / segments);
            float a1 = (float) (Math.PI * 2.0 * (i + 1) / segments);
            v[k++] = x + (float) Math.cos(a0) * radius;
            v[k++] = y;
            v[k++] = z + (float) Math.sin(a0) * radius;
            v[k++] = x + (float) Math.cos(a1) * radius;
            v[k++] = y;
            v[k++] = z + (float) Math.sin(a1) * radius;
        }
        drawArray(viewProjection, identity, v, GLES20.GL_LINES, 0.08f, 0.72f, 1f, 1f, stats);
    }

    public void drawStartMarker(float[] viewProjection, float x, float y, float z, RenderStats stats) {
        drawLine(viewProjection, x - 5f, y, z, x + 5f, y, z, 0.1f, 1f, 0.42f, stats);
        drawLine(viewProjection, x, y, z - 5f, x, y, z + 5f, 0.1f, 1f, 0.42f, stats);
    }


    public void drawFlatRect(float[] viewProjection, float centerX, float y, float centerZ, float sizeX, float sizeZ, float r, float g, float b, float a, RenderStats stats) {
        float hx = sizeX * 0.5f;
        float hz = sizeZ * 0.5f;
        float[] v = new float[] {
                centerX - hx, y, centerZ - hz,
                centerX + hx, y, centerZ - hz,
                centerX + hx, y, centerZ + hz,
                centerX - hx, y, centerZ - hz,
                centerX + hx, y, centerZ + hz,
                centerX - hx, y, centerZ + hz
        };
        drawArray(viewProjection, identity, v, GLES20.GL_TRIANGLES, r, g, b, a, stats);
    }

    public void drawBox(float[] viewProjection, float x, float y, float z, float sizeX, float sizeY, float sizeZ, float yawRadians, float r, float g, float b, float a, RenderStats stats) {
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, x, y, z);
        Matrix.rotateM(model, 0, (float) Math.toDegrees(yawRadians), 0f, 1f, 0f);
        Matrix.scaleM(model, 0, sizeX, sizeY, sizeZ);
        drawCube(viewProjection, model, r, g, b, a, stats);
    }


    public void drawCarFallback(float[] viewProjection, float x, float y, float z, float pitch, float yaw, float roll, RenderStats stats) {
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, x, y + 0.35f, z);
        Matrix.rotateM(model, 0, (float) Math.toDegrees(yaw), 0f, 1f, 0f);
        Matrix.rotateM(model, 0, (float) Math.toDegrees(pitch), 1f, 0f, 0f);
        Matrix.rotateM(model, 0, (float) Math.toDegrees(roll), 0f, 0f, 1f);
        Matrix.scaleM(model, 0, 1.75f, 0.62f, 3.35f);
        drawCube(viewProjection, model, 0.07f, 0.55f, 1f, 1f, stats);

        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, x, y + 0.9f, z - 0.15f);
        Matrix.rotateM(model, 0, (float) Math.toDegrees(yaw), 0f, 1f, 0f);
        Matrix.scaleM(model, 0, 1.25f, 0.55f, 1.45f);
        drawCube(viewProjection, model, 0.06f, 0.18f, 0.32f, 1f, stats);
    }


    public void drawBillboardDiamond(float[] vp, float x, float y, float z, float radius, float height, float r, float g, float b, float a, RenderStats stats) {
        float[] v = new float[] {
                x, y + height, z,
                x - radius, y, z,
                x, y, z + radius,
                x, y + height, z,
                x, y, z + radius,
                x + radius, y, z,
                x, y + height, z,
                x + radius, y, z,
                x, y, z - radius,
                x, y + height, z,
                x, y, z - radius,
                x - radius, y, z
        };
        drawArray(vp, identity, v, GLES20.GL_TRIANGLES, r, g, b, a, stats);
    }

    public void drawLine(float[] vp, float x0, float y0, float z0, float x1, float y1, float z1, float r, float g, float b, RenderStats stats) {
        float[] v = new float[] {x0, y0, z0, x1, y1, z1};
        drawArray(vp, identity, v, GLES20.GL_LINES, r, g, b, 1f, stats);
    }

    private void drawCube(float[] vp, float[] modelMatrix, float r, float g, float b, float a, RenderStats stats) {
        GLES20.glUseProgram(shader.program);
        Matrix.multiplyMM(mvp, 0, vp, 0, modelMatrix, 0);
        GLES20.glUniformMatrix4fv(shader.uMvp, 1, false, mvp, 0);
        GLES20.glUniform4f(shader.uColor, r, g, b, a);
        cubeBuffer.position(0);
        GLES20.glEnableVertexAttribArray(shader.aPosition);
        GLES20.glVertexAttribPointer(shader.aPosition, 3, GLES20.GL_FLOAT, false, 0, cubeBuffer);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, cubeVertexCount);
        GLES20.glDisableVertexAttribArray(shader.aPosition);
        if (stats != null) { stats.drawCalls++; stats.renderedObjects++; }
    }

    private void drawArray(float[] vp, float[] modelMatrix, float[] vertices, int mode, float r, float g, float b, float a, RenderStats stats) {
        if (vertices.length > dynamicBuffer.capacity()) {
            dynamicBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        }
        dynamicBuffer.clear();
        dynamicBuffer.put(vertices).position(0);

        boolean useBlend = a < 0.985f;
        if (useBlend) {
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glDepthMask(false);
        }

        GLES20.glUseProgram(shader.program);
        Matrix.multiplyMM(mvp, 0, vp, 0, modelMatrix, 0);
        GLES20.glUniformMatrix4fv(shader.uMvp, 1, false, mvp, 0);
        GLES20.glUniform4f(shader.uColor, r, g, b, a);
        GLES20.glEnableVertexAttribArray(shader.aPosition);
        GLES20.glVertexAttribPointer(shader.aPosition, 3, GLES20.GL_FLOAT, false, 0, dynamicBuffer);
        GLES20.glDrawArrays(mode, 0, vertices.length / 3);
        GLES20.glDisableVertexAttribArray(shader.aPosition);

        if (useBlend) {
            GLES20.glDepthMask(true);
            GLES20.glDisable(GLES20.GL_BLEND);
        }
        if (stats != null) { stats.drawCalls++; stats.renderedObjects++; }
    }
}
