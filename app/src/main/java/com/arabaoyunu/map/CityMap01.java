package com.arabaoyunu.map;

import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;

/**
 * ArabaOyunu_32: Büyük şehir haritası.
 * Primitive bina, cadde, kavşak, park alanı ve polis/görev uyumlu açık yollar.
 */
public final class CityMap01 extends BaseMap {

    private static final float HALF_SIZE = 260f;
    private static final float ROAD_Y = 0.035f;

    @Override
    public String getName() {
        return "BUYUK_SEHIR";
    }


    @Override
    public float getSpawnX() { return 0f; }

    @Override
    public float getSpawnY() { return 0.42f; }

    @Override
    public float getSpawnZ() { return -180f; }

    @Override
    public float getSpawnYaw() { return 0f; }

    @Override
    public void render(PrimitiveRenderer renderer, float[] viewProjection, RenderStats stats) {
        renderer.drawGround(viewProjection, HALF_SIZE, stats);

        drawRoadNetwork(renderer, viewProjection, stats);
        drawCityBlocks(renderer, viewProjection, stats);
        drawParksAndProps(renderer, viewProjection, stats);
        drawRespawnMarkers(renderer, viewProjection, stats);

        renderer.drawBoundary(viewProjection, HALF_SIZE, stats);
    }

    private void drawRoadNetwork(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        // Ana dikey yollar
        for (float x = -120f; x <= 120f; x += 80f) {
            r.drawFlatRect(vp, x, ROAD_Y, 0f, 22f, HALF_SIZE * 1.85f, 0.075f, 0.082f, 0.092f, 1f, stats);
            for (float z = -220f; z <= 220f; z += 28f) {
                r.drawFlatRect(vp, x, ROAD_Y + 0.014f, z, 1.0f, 10f, 0.86f, 0.82f, 0.32f, 1f, stats);
            }
        }

        // Ana yatay yollar
        for (float z = -120f; z <= 120f; z += 80f) {
            r.drawFlatRect(vp, 0f, ROAD_Y + 0.004f, z, HALF_SIZE * 1.85f, 22f, 0.070f, 0.078f, 0.088f, 1f, stats);
            for (float x = -220f; x <= 220f; x += 28f) {
                r.drawFlatRect(vp, x, ROAD_Y + 0.018f, z, 10f, 1.0f, 0.86f, 0.82f, 0.32f, 1f, stats);
            }
        }

        // Kavşak vurguları
        for (float x = -120f; x <= 120f; x += 80f) {
            for (float z = -120f; z <= 120f; z += 80f) {
                r.drawFlatRect(vp, x, ROAD_Y + 0.025f, z, 26f, 26f, 0.095f, 0.102f, 0.112f, 1f, stats);
            }
        }
    }

    private void drawCityBlocks(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        int i = 0;
        for (float x = -185f; x <= 185f; x += 80f) {
            for (float z = -185f; z <= 185f; z += 80f) {
                if (Math.abs(x) < 35f || Math.abs(z) < 35f) continue;
                float h = 8f + (i % 5) * 3.8f;
                float w = 18f + (i % 3) * 5f;
                float d = 16f + ((i + 1) % 4) * 4f;
                float cr = 0.08f + (i % 4) * 0.022f;
                float cg = 0.105f + (i % 5) * 0.020f;
                float cb = 0.135f + (i % 3) * 0.028f;
                r.drawBox(vp, x, h * 0.5f, z, w, h, d, 0f, cr, cg, cb, 1f, stats);
                r.drawBox(vp, x, h + 0.25f, z, w * 0.82f, 0.5f, d * 0.82f, 0f, 0.035f, 0.045f, 0.055f, 1f, stats);
                i++;
            }
        }
    }

    private void drawParksAndProps(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        // Park alanları
        r.drawFlatRect(vp, -42f, ROAD_Y + 0.01f, 178f, 58f, 48f, 0.05f, 0.18f, 0.085f, 1f, stats);
        r.drawFlatRect(vp, 142f, ROAD_Y + 0.01f, -180f, 64f, 42f, 0.05f, 0.17f, 0.080f, 1f, stats);

        // Park ağaçları / direkler
        for (int i = 0; i < 14; i++) {
            float x = -68f + (i % 7) * 9f;
            float z = 160f + (i / 7) * 22f;
            r.drawBox(vp, x, 1.2f, z, 0.8f, 2.4f, 0.8f, 0f, 0.28f, 0.16f, 0.08f, 1f, stats);
            r.drawBox(vp, x, 3.1f, z, 3.0f, 2.2f, 3.0f, 0f, 0.05f, 0.28f, 0.08f, 1f, stats);
        }

        // Şehir giriş tabelası
        r.drawBox(vp, 0f, 3.0f, -228f, 28f, 2.5f, 0.55f, 0f, 0.03f, 0.28f, 0.72f, 1f, stats);
        r.drawBox(vp, -12f, 1.4f, -228f, 0.55f, 2.8f, 0.55f, 0f, 0.60f, 0.60f, 0.64f, 1f, stats);
        r.drawBox(vp, 12f, 1.4f, -228f, 0.55f, 2.8f, 0.55f, 0f, 0.60f, 0.60f, 0.64f, 1f, stats);
    }

    private void drawRespawnMarkers(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        for (int i = 0; i < getRespawnPointCount(); i++) {
            r.drawCircle(vp, getRespawnX(i), 0.06f, getRespawnZ(i), 7.5f, stats);
        }
    }

    @Override
    public boolean sampleGround(float x, float z, VehicleController.GroundHit outHit) {
        super.sampleGround(x, z, outHit);
        float ax = Math.abs(x);
        float az = Math.abs(z);
        boolean roadX = Math.abs(x + 120f) < 14f || Math.abs(x + 40f) < 14f || Math.abs(x - 40f) < 14f || Math.abs(x - 120f) < 14f;
        boolean roadZ = Math.abs(z + 120f) < 14f || Math.abs(z + 40f) < 14f || Math.abs(z - 40f) < 14f || Math.abs(z - 120f) < 14f;
        if (roadX || roadZ) setSurface(outHit, "SEHIR_ASFALT", 0.96f, 1.05f, 0.95f, 1.02f);
        else if (ax < HALF_SIZE && az < HALF_SIZE) setSurface(outHit, "KALDIRIM", 0.78f, 1.18f, 0.85f, 1.10f);
        else setSurface(outHit, "CIM", 0.62f, 1.45f, 0.70f, 1.30f);
        return true;
    }

    @Override
    public boolean isInsideBounds(float x, float z) {
        return Math.abs(x) < HALF_SIZE && Math.abs(z) < HALF_SIZE;
    }

    @Override public int getRespawnPointCount() { return 8; }
    @Override public float getRespawnX(int index) {
        switch (Math.max(0, index % 8)) {
            case 1: return 40f;
            case 2: return -120f;
            case 3: return 120f;
            case 4: return -42f;
            case 5: return 142f;
            case 6: return -180f;
            case 7: return 180f;
            default: return 0f;
        }
    }
    @Override public float getRespawnY(int index) { return 0.42f; }
    @Override public float getRespawnZ(int index) {
        switch (Math.max(0, index % 8)) {
            case 1: return -120f;
            case 2: return 40f;
            case 3: return -40f;
            case 4: return 178f;
            case 5: return -180f;
            case 6: return 120f;
            case 7: return -120f;
            default: return -180f;
        }
    }
    @Override public float getRespawnYaw(int index) {
        return index % 2 == 0 ? 0f : 1.5707963f;
    }
}
