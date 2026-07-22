package com.arabaoyunu.map;

import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;

/**
 * ArabaOyunu_32: Uzun otoyol haritası.
 * Hız testi, polis kovalamaca ve trafik için uzun ana yol.
 */
public final class HighwayMap01 extends BaseMap {

    private static final float HALF_X = 135f;
    private static final float HALF_Z = 520f;
    private static final float ROAD_Y = 0.035f;

    @Override
    public String getName() {
        return "OTOYOL";
    }


    @Override
    public float getSpawnX() { return 0f; }

    @Override
    public float getSpawnY() { return 0.42f; }

    @Override
    public float getSpawnZ() { return -440f; }

    @Override
    public float getSpawnYaw() { return 0f; }

    @Override
    public void render(PrimitiveRenderer renderer, float[] viewProjection, RenderStats stats) {
        renderer.drawGround(viewProjection, 560f, stats);

        // Ana otoyol gövdesi
        renderer.drawFlatRect(viewProjection, 0f, ROAD_Y, 0f, 36f, HALF_Z * 2f, 0.070f, 0.077f, 0.086f, 1f, stats);
        renderer.drawFlatRect(viewProjection, -44f, ROAD_Y + 0.004f, 0f, 22f, HALF_Z * 2f, 0.075f, 0.082f, 0.090f, 1f, stats);
        renderer.drawFlatRect(viewProjection, 44f, ROAD_Y + 0.004f, 0f, 22f, HALF_Z * 2f, 0.075f, 0.082f, 0.090f, 1f, stats);

        drawMarkings(renderer, viewProjection, stats);
        drawBarriers(renderer, viewProjection, stats);
        drawServiceAreas(renderer, viewProjection, stats);
        drawSpeedSigns(renderer, viewProjection, stats);

        renderer.drawBoundary(viewProjection, 560f, stats);
    }

    private void drawMarkings(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        for (float z = -500f; z <= 500f; z += 28f) {
            r.drawFlatRect(vp, 0f, ROAD_Y + 0.018f, z, 1.1f, 12f, 0.92f, 0.90f, 0.72f, 1f, stats);
            r.drawFlatRect(vp, -44f, ROAD_Y + 0.018f, z, 1.0f, 12f, 0.88f, 0.88f, 0.88f, 1f, stats);
            r.drawFlatRect(vp, 44f, ROAD_Y + 0.018f, z, 1.0f, 12f, 0.88f, 0.88f, 0.88f, 1f, stats);
        }
        r.drawFlatRect(vp, -18f, ROAD_Y + 0.016f, 0f, 0.8f, HALF_Z * 2f, 0.92f, 0.92f, 0.92f, 1f, stats);
        r.drawFlatRect(vp, 18f, ROAD_Y + 0.016f, 0f, 0.8f, HALF_Z * 2f, 0.92f, 0.92f, 0.92f, 1f, stats);
    }

    private void drawBarriers(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        for (float z = -500f; z <= 500f; z += 32f) {
            r.drawBox(vp, -62f, 0.36f, z, 1.3f, 0.72f, 12f, 0f, 0.58f, 0.60f, 0.64f, 1f, stats);
            r.drawBox(vp, 62f, 0.36f, z, 1.3f, 0.72f, 12f, 0f, 0.58f, 0.60f, 0.64f, 1f, stats);
        }
    }

    private void drawServiceAreas(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        r.drawFlatRect(vp, -86f, ROAD_Y + 0.01f, -160f, 48f, 62f, 0.105f, 0.098f, 0.085f, 1f, stats);
        r.drawBox(vp, -96f, 3.4f, -164f, 18f, 6.8f, 14f, 0f, 0.12f, 0.14f, 0.17f, 1f, stats);
        r.drawBox(vp, -76f, 2.4f, -134f, 18f, 4.8f, 8f, 0f, 0.24f, 0.06f, 0.04f, 1f, stats);

        r.drawFlatRect(vp, 86f, ROAD_Y + 0.01f, 210f, 52f, 66f, 0.105f, 0.098f, 0.085f, 1f, stats);
        r.drawBox(vp, 92f, 3.2f, 214f, 20f, 6.4f, 15f, 0f, 0.13f, 0.15f, 0.18f, 1f, stats);
    }

    private void drawSpeedSigns(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        for (float z = -420f; z <= 420f; z += 210f) {
            r.drawBox(vp, 26f, 2.0f, z, 0.5f, 4f, 0.5f, 0f, 0.65f, 0.65f, 0.68f, 1f, stats);
            r.drawBox(vp, 26f, 4.4f, z, 4f, 2f, 0.35f, 0f, 0.05f, 0.32f, 0.90f, 1f, stats);
        }
    }

    @Override
    public boolean sampleGround(float x, float z, VehicleController.GroundHit outHit) {
        super.sampleGround(x, z, outHit);
        if (Math.abs(x) < 67f && Math.abs(z) < HALF_Z) {
            setSurface(outHit, "OTOYOL_ASFALT", 1.04f, 0.94f, 1.02f, 0.92f);
        } else if (Math.abs(x) < HALF_X && Math.abs(z) < HALF_Z) {
            setSurface(outHit, "SERT_BANKET", 0.78f, 1.22f, 0.82f, 1.16f);
        } else {
            setSurface(outHit, "CIM", 0.58f, 1.50f, 0.68f, 1.34f);
        }
        return true;
    }

    @Override
    public boolean isInsideBounds(float x, float z) {
        return Math.abs(x) < HALF_X && Math.abs(z) < HALF_Z;
    }

    @Override public int getRespawnPointCount() { return 6; }
    @Override public float getRespawnX(int index) {
        switch (Math.max(0, index % 6)) {
            case 1: return -22f;
            case 2: return 22f;
            case 3: return -86f;
            case 4: return 86f;
            case 5: return 0f;
            default: return 0f;
        }
    }
    @Override public float getRespawnY(int index) { return 0.42f; }
    @Override public float getRespawnZ(int index) {
        switch (Math.max(0, index % 6)) {
            case 1: return -310f;
            case 2: return 300f;
            case 3: return -160f;
            case 4: return 210f;
            case 5: return 440f;
            default: return -440f;
        }
    }
    @Override public float getRespawnYaw(int index) {
        return index % 2 == 0 ? 0f : 3.1415926f;
    }
}
