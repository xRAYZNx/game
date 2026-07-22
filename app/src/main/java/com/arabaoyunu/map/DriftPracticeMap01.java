package com.arabaoyunu.map;

import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;

/**
 * Plan 11: Bos test alanindan sonra ilk oynanabilir test haritasi.
 * Obje sayisi dusuk tutulur; cogu obje gorseldir, dis sinirlar fizik tarafinda geri sektirir.
 */
public final class DriftPracticeMap01 extends BaseMap {

    private static final float HALF_SIZE = 260f;
    private static final float ROAD_Y = 0.018f;

    private static final float[][] RESPAWN_POINTS = new float[][] {
            {0f, 0.42f, -185f, 0f},
            {0f, 0.42f, -95f, 0f},
            {-92f, 0.42f, 42f, 90f},
            {92f, 0.42f, 42f, -90f},
            {0f, 0.42f, 72f, 180f},
            {-48f, 0.42f, -30f, 45f},
            {48f, 0.42f, -30f, -45f}
    };


    @Override
    public String getName() {
        return "DriftPracticeMap_01";
    }

    @Override
    public float getSpawnX() { return 0f; }

    @Override
    public float getSpawnY() { return 0.42f; }

    @Override
    public float getSpawnZ() { return -185f; }

    @Override
    public float getSpawnYaw() { return 0f; }


    @Override
    public int getRespawnPointCount() {
        return RESPAWN_POINTS.length;
    }

    @Override
    public float getRespawnX(int index) {
        return RESPAWN_POINTS[safeRespawnIndex(index)][0];
    }

    @Override
    public float getRespawnY(int index) {
        return RESPAWN_POINTS[safeRespawnIndex(index)][1];
    }

    @Override
    public float getRespawnZ(int index) {
        return RESPAWN_POINTS[safeRespawnIndex(index)][2];
    }

    @Override
    public float getRespawnYaw(int index) {
        return RESPAWN_POINTS[safeRespawnIndex(index)][3] * 0.017453292f;
    }

    private int safeRespawnIndex(int index) {
        if (index < 0) return 0;
        if (index >= RESPAWN_POINTS.length) return RESPAWN_POINTS.length - 1;
        return index;
    }

    @Override
    public boolean sampleGround(float x, float z, VehicleController.GroundHit outHit) {
        outHit.grounded = true;
        outHit.y = 0f;
        outHit.normalX = 0f;
        outHit.normalY = 1f;
        outHit.normalZ = 0f;

        if (isInsideDriftPad(x, z)) {
            setSurface(outHit, "DRIFT_ASFALT", 0.74f, 1.04f, 0.86f, 1.45f);
        } else if (isOnRoad(x, z)) {
            setSurface(outHit, "ASFALT", 1.00f, 1.00f, 1.00f, 1.00f);
        } else {
            setSurface(outHit, "CIM", 0.62f, 1.48f, 0.70f, 1.30f);
        }
        return true;
    }

    private boolean isInsideDriftPad(float x, float z) {
        float dx = x + 92f;
        float dz = z - 42f;
        if (dx * dx + dz * dz < 38f * 38f) return true;

        float rx = x - 92f;
        float rz = z - 35f;
        return rx * rx + rz * rz < 34f * 34f;
    }

    private boolean isOnRoad(float x, float z) {
        if (Math.abs(x) <= 15f && Math.abs(z) <= 220f) return true;
        if (Math.abs(x) <= 60f && Math.abs(z) <= 50f) return true;
        if (Math.abs(x + 46f) <= 48f && Math.abs(z - 42f) <= 12f) return true;
        if (Math.abs(x - 48f) <= 52f && Math.abs(z - 68f) <= 12f) return true;
        if (Math.abs(x - 92f) <= 15f && Math.abs(z + 52f) <= 32f) return true;
        return false;
    }

    @Override
    public boolean isInsideBounds(float x, float z) {
        return true;
    }

    @Override
    public void render(PrimitiveRenderer renderer, float[] viewProjection, RenderStats stats) {
        renderer.drawGround(viewProjection, HALF_SIZE, stats);

        drawAsphaltLayout(renderer, viewProjection, stats);
        drawRoadMarkings(renderer, viewProjection, stats);
        drawDriftZones(renderer, viewProjection, stats);
        drawSpeedAndBrakeZones(renderer, viewProjection, stats);
        drawSlalomCones(renderer, viewProjection, stats);
        drawBarriers(renderer, viewProjection, stats);
        drawEnvironment(renderer, viewProjection, stats);
        drawTimeTrialCourse(renderer, viewProjection, stats);
        drawStartFinish(renderer, viewProjection, stats);
    }

    private void drawAsphaltLayout(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        // Ana hiz testi yolu.
        r.drawFlatRect(vp, 0f, ROAD_Y, 0f, 26f, 430f, 0.105f, 0.112f, 0.118f, 1f, stats);
        // Orta genis test platformu.
        r.drawFlatRect(vp, 0f, ROAD_Y + 0.002f, 0f, 118f, 92f, 0.095f, 0.102f, 0.108f, 1f, stats);
        // Sol drift alani.
        r.drawFlatRect(vp, -92f, ROAD_Y, 42f, 86f, 86f, 0.095f, 0.102f, 0.108f, 1f, stats);
        // Sag viraj ve fren alani.
        r.drawFlatRect(vp, 92f, ROAD_Y, 35f, 76f, 118f, 0.095f, 0.102f, 0.108f, 1f, stats);
        // Basit virajli bolumleri birbirine baglayan yan yollar.
        r.drawFlatRect(vp, -46f, ROAD_Y, 42f, 92f, 18f, 0.105f, 0.112f, 0.118f, 1f, stats);
        r.drawFlatRect(vp, 48f, ROAD_Y, 68f, 98f, 18f, 0.105f, 0.112f, 0.118f, 1f, stats);
        r.drawFlatRect(vp, 92f, ROAD_Y, -52f, 24f, 58f, 0.105f, 0.112f, 0.118f, 1f, stats);
    }

    private void drawRoadMarkings(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        // Uzun yol orta kesik cizgileri.
        for (float z = -198f; z <= 198f; z += 24f) {
            r.drawFlatRect(vp, 0f, ROAD_Y + 0.015f, z, 1.1f, 12f, 0.86f, 0.86f, 0.72f, 1f, stats);
        }
        // Yol kenar cizgileri.
        r.drawFlatRect(vp, -13.4f, ROAD_Y + 0.012f, 0f, 0.8f, 422f, 0.9f, 0.9f, 0.9f, 1f, stats);
        r.drawFlatRect(vp, 13.4f, ROAD_Y + 0.012f, 0f, 0.8f, 422f, 0.9f, 0.9f, 0.9f, 1f, stats);

        // Viraj test rehber cizgileri.
        r.drawCircle(vp, 92f, ROAD_Y + 0.02f, 35f, 18f, stats);
        r.drawCircle(vp, 92f, ROAD_Y + 0.02f, 35f, 30f, stats);
    }

    private void drawDriftZones(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        r.drawCircle(vp, -92f, ROAD_Y + 0.025f, 42f, 18f, stats);
        r.drawCircle(vp, -92f, ROAD_Y + 0.028f, 42f, 31f, stats);
        r.drawCircle(vp, -92f, ROAD_Y + 0.031f, 42f, 42f, stats);
        r.drawStartMarker(vp, -92f, ROAD_Y + 0.04f, 42f, stats);
    }

    private void drawSpeedAndBrakeZones(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        // Ivmelenme baslangic koridoru.
        r.drawFlatRect(vp, 0f, ROAD_Y + 0.02f, -170f, 24f, 5f, 0.04f, 0.65f, 0.95f, 0.95f, stats);
        // Fren testi icin kademeli seritler.
        for (int i = 0; i < 6; i++) {
            float z = 112f + i * 10f;
            float c = 0.18f + i * 0.11f;
            r.drawFlatRect(vp, 0f, ROAD_Y + 0.022f, z, 24f, 1.8f, 0.95f, c, 0.05f, 1f, stats);
        }
        r.drawFlatRect(vp, 0f, ROAD_Y + 0.024f, 184f, 24f, 4f, 0.95f, 0.05f, 0.05f, 1f, stats);
    }

    private void drawSlalomCones(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        for (int i = 0; i < 10; i++) {
            float z = -118f + i * 18f;
            float x = (i % 2 == 0) ? -6.5f : 6.5f;
            r.drawBox(vp, x, 0.24f, z, 1.2f, 0.48f, 1.2f, 0f, 1f, 0.38f, 0.04f, 1f, stats);
        }
    }

    private void drawBarriers(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        // Dis sinir bariyerleri: fizik tarafinda sadece map bounds ile geri sektirme var.
        r.drawBoundary(vp, HALF_SIZE, stats);
        for (float z = -210f; z <= 210f; z += 28f) {
            r.drawBox(vp, -19f, 0.42f, z, 2.2f, 0.85f, 14f, 0f, 0.72f, 0.72f, 0.76f, 1f, stats);
            r.drawBox(vp, 19f, 0.42f, z, 2.2f, 0.85f, 14f, 0f, 0.72f, 0.72f, 0.76f, 1f, stats);
        }
        for (float a = 0f; a < 360f; a += 30f) {
            float rad = (float) Math.toRadians(a);
            float x = -92f + (float) Math.cos(rad) * 47f;
            float z = 42f + (float) Math.sin(rad) * 47f;
            r.drawBox(vp, x, 0.35f, z, 2.8f, 0.7f, 1.2f, -rad, 0.84f, 0.12f, 0.12f, 1f, stats);
        }
    }

    private void drawEnvironment(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        // Dusuk maliyetli sehir arka plani: tek tek basit kutular.
        for (int i = 0; i < 9; i++) {
            float x = -225f + i * 56f;
            float h = 10f + (i % 4) * 5f;
            r.drawBox(vp, x, h * 0.5f, -238f, 22f, h, 16f, 0f, 0.12f, 0.16f, 0.20f, 1f, stats);
            r.drawBox(vp, x + 24f, h * 0.38f, 238f, 18f, h * 0.76f, 14f, 0f, 0.14f, 0.17f, 0.20f, 1f, stats);
        }

        // Isik direkleri.
        for (float z = -170f; z <= 170f; z += 68f) {
            drawLightPole(r, vp, -34f, z, stats);
            drawLightPole(r, vp, 34f, z + 34f, stats);
        }

        // Basit tabelalar.
        drawSign(r, vp, -24f, -178f, "START", stats);
        drawSign(r, vp, 24f, 110f, "BRAKE", stats);
        drawSign(r, vp, -138f, 42f, "DRIFT", stats);
    }

    private void drawLightPole(PrimitiveRenderer r, float[] vp, float x, float z, RenderStats stats) {
        r.drawBox(vp, x, 3.2f, z, 0.55f, 6.4f, 0.55f, 0f, 0.55f, 0.55f, 0.58f, 1f, stats);
        r.drawBox(vp, x + 1.9f, 6.2f, z, 3.4f, 0.28f, 0.38f, 0f, 0.78f, 0.78f, 0.72f, 1f, stats);
    }

    private void drawSign(PrimitiveRenderer r, float[] vp, float x, float z, String label, RenderStats stats) {
        // OpenGL primitive katmaninda yazi yok; tabela panosu renk koduyla ayirt edilir.
        float blue = label.equals("START") ? 0.95f : 0.22f;
        float red = label.equals("BRAKE") ? 0.95f : 0.10f;
        float green = label.equals("DRIFT") ? 0.85f : 0.32f;
        r.drawBox(vp, x, 1.05f, z, 0.35f, 2.1f, 0.35f, 0f, 0.52f, 0.52f, 0.52f, 1f, stats);
        r.drawBox(vp, x, 2.35f, z, 5.4f, 1.8f, 0.35f, 0f, red, green, blue, 1f, stats);
    }


    private void drawTimeTrialCourse(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        // Plan 13: ilk zaman yarisi rotasi.
        // Checkpoint koordinatlari TimeTrialMode ile aynidir.
        float[] xs = new float[] {0f, 0f, 48f, 92f, 48f, 0f};
        float[] zs = new float[] {-115f, -32f, 42f, 35f, 102f, 184f};

        for (int i = 0; i < xs.length; i++) {
            float x = xs[i];
            float z = zs[i];

            // Dairesel gate cizgisi
            r.drawCircle(vp, x, ROAD_Y + 0.055f, z, i == xs.length - 1 ? 18f : 15f, stats);

            // Sol/sag checkpoint direkleri
            float markerColorR = i == xs.length - 1 ? 1.0f : 0.10f;
            float markerColorG = i == xs.length - 1 ? 0.18f : 0.95f;
            float markerColorB = i == xs.length - 1 ? 0.12f : 0.22f;

            r.drawBox(vp, x - 8f, 1.35f, z, 0.75f, 2.7f, 0.75f, 0f,
                    markerColorR, markerColorG, markerColorB, 1f, stats);
            r.drawBox(vp, x + 8f, 1.35f, z, 0.75f, 2.7f, 0.75f, 0f,
                    markerColorR, markerColorG, markerColorB, 1f, stats);

            // Zeminde rota oku benzeri isaret
            if (i < xs.length - 1) {
                float nx = xs[i + 1] - x;
                float nz = zs[i + 1] - z;
                float yaw = (float) Math.atan2(nx, nz);
                r.drawBox(vp, x, ROAD_Y + 0.08f, z + 3f, 5.5f, 0.05f, 1.1f, yaw,
                        0.08f, 0.92f, 0.35f, 1f, stats);
            }
        }

        // Finish seridi daha belirgin olsun.
        for (int x = -12; x < 12; x += 3) {
            for (int i = 0; i < 4; i++) {
                boolean white = ((x / 3) + i) % 2 == 0;
                float c = white ? 0.98f : 0.02f;
                r.drawFlatRect(vp, x + 1.5f, ROAD_Y + 0.075f, 184f + i * 2.2f, 3f, 2.2f, c, c, c, 1f, stats);
            }
        }
    }

    private void drawStartFinish(PrimitiveRenderer r, float[] vp, RenderStats stats) {
        float startZ = -185f;
        for (int x = -12; x < 12; x += 3) {
            for (int i = 0; i < 4; i++) {
                boolean white = ((x / 3) + i) % 2 == 0;
                float c = white ? 0.95f : 0.05f;
                r.drawFlatRect(vp, x + 1.5f, ROAD_Y + 0.035f, startZ + i * 2.2f, 3f, 2.2f, c, c, c, 1f, stats);
            }
        }
        r.drawStartMarker(vp, 0f, ROAD_Y + 0.05f, startZ, stats);
    }
}
