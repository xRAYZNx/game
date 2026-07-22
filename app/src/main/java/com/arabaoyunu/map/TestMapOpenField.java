package com.arabaoyunu.map;

import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;

public final class TestMapOpenField extends BaseMap {

    private static final float HALF_SIZE = 260f;

    private static final float[][] RESPAWN_POINTS = new float[][] {
            {0f, 0.42f, 0f, 0f},
            {25f, 0.42f, 25f, 45f},
            {-25f, 0.42f, 25f, -45f},
            {25f, 0.42f, -25f, 135f},
            {-25f, 0.42f, -25f, -135f},
            {0f, 0.42f, 70f, 0f},
            {0f, 0.42f, -70f, 180f}
    };


    @Override
    public String getName() {
        return "TestMap_OpenField";
    }

    @Override
    public float getSpawnX() { return 0f; }

    @Override
    public float getSpawnY() { return 0.42f; }

    @Override
    public float getSpawnZ() { return 0f; }

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

        float ax = Math.abs(x);
        float az = Math.abs(z);
        if (ax > 210f || az > 210f) {
            setSurface(outHit, "CIM", 0.66f, 1.42f, 0.72f, 1.28f);
        } else if (ax < 18f || az < 34f) {
            setSurface(outHit, "ASFALT", 1.00f, 1.00f, 1.00f, 1.00f);
        } else {
            setSurface(outHit, "TEST_ASFALT", 0.92f, 1.08f, 0.94f, 1.10f);
        }
        return true;
    }

    @Override
    public boolean isInsideBounds(float x, float z) {
        return true;
    }

    @Override
    public void render(PrimitiveRenderer renderer, float[] viewProjection, RenderStats stats) {
        // A61.7: GLB harita bırakıldıktan sonra bu alan ana sürüş test pistidir.
        // Görsel karmaşa azaltıldı: geniş zemin, okunabilir grid, iki asfalt şerit,
        // fren/ivmelenme çizgileri ve kalibrasyon halkaları bırakıldı.
        renderer.drawGround(viewProjection, HALF_SIZE, stats);
        renderer.drawGrid(viewProjection, HALF_SIZE, 12.5f, stats);

        // Ana asfalt koridorları.
        renderer.drawBox(viewProjection, 0f, 0.026f, 0f, 22f, 0.04f, HALF_SIZE * 2f, 0f,
                0.075f, 0.080f, 0.092f, 1f, stats);
        renderer.drawBox(viewProjection, 0f, 0.030f, 0f, HALF_SIZE * 2f, 0.04f, 42f, 0f,
                0.070f, 0.078f, 0.090f, 1f, stats);

        // Şerit ve ölçüm çizgileri.
        for (float z = -240f; z <= 240f; z += 30f) {
            renderer.drawBox(viewProjection, -5.5f, 0.062f, z, 0.45f, 0.035f, 10f, 0f, 1f, 0.92f, 0.18f, 1f, stats);
            renderer.drawBox(viewProjection, 5.5f, 0.062f, z, 0.45f, 0.035f, 10f, 0f, 1f, 0.92f, 0.18f, 1f, stats);
        }
        for (float x = -240f; x <= 240f; x += 30f) {
            renderer.drawBox(viewProjection, x, 0.064f, -10.5f, 10f, 0.035f, 0.45f, 0f, 1f, 0.92f, 0.18f, 1f, stats);
            renderer.drawBox(viewProjection, x, 0.064f, 10.5f, 10f, 0.035f, 0.45f, 0f, 1f, 0.92f, 0.18f, 1f, stats);
        }

        // Başlangıç ve sürüş kalibrasyon alanı.
        renderer.drawCircle(viewProjection, 0f, 0.024f, 0f, 18f, stats);
        renderer.drawCircle(viewProjection, 0f, 0.028f, 0f, 42f, stats);
        renderer.drawCircle(viewProjection, 0f, 0.032f, 0f, 78f, stats);
        renderer.drawStartMarker(viewProjection, 0f, 0.05f, 0f, stats);

        // Fren / dönüş test konileri. Az sayıda, okunaklı ve sürüş yolundan uzak.
        for (int i = 0; i < 8; i++) {
            float z = -100f + i * 28f;
            renderer.drawBox(viewProjection, -38f, 0.45f, z, 1.6f, 0.90f, 1.6f, 0f, 1f, 0.42f, 0.08f, 1f, stats);
            renderer.drawBox(viewProjection, 38f, 0.45f, z, 1.6f, 0.90f, 1.6f, 0f, 1f, 0.42f, 0.08f, 1f, stats);
        }

        // Ufuk referansı: düşük yükseklikte sade bloklar.
        for (int i = 0; i < 5; i++) {
            float x = -165f + i * 82f;
            renderer.drawBox(viewProjection, x, 3.0f, 170f, 24f, 6.0f, 18f, 0f,
                    0.075f, 0.095f, 0.125f, 1f, stats);
            renderer.drawBox(viewProjection, x + 34f, 2.2f, -176f, 18f, 4.4f, 16f, 0f,
                    0.070f, 0.088f, 0.116f, 1f, stats);
        }

        renderer.drawBoundary(viewProjection, HALF_SIZE, stats);
    }

    private void drawCityBlocks(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        // ArabaOyunu_29: küçük şehir test alanı. Trafik/görev sisteminin boş haritada kalmaması için.
        for (int i = 0; i < 6; i++) {
            float x = -145f + i * 58f;
            float h1 = 8f + (i % 3) * 4f;
            float h2 = 7f + ((i + 1) % 4) * 3.5f;
            renderer.drawBox(vp, x, h1 * 0.5f, 72f, 18f, h1, 16f, 0f, 0.10f, 0.14f, 0.18f, 1f, stats);
            renderer.drawBox(vp, x + 22f, h2 * 0.5f, -74f, 16f, h2, 14f, 0f, 0.12f, 0.15f, 0.19f, 1f, stats);
        }

        // Otoyol bariyerleri.
        for (float z = -210f; z <= 210f; z += 34f) {
            renderer.drawBox(vp, -18f, 0.32f, z, 1.2f, 0.64f, 11f, 0f, 0.62f, 0.64f, 0.67f, 1f, stats);
            renderer.drawBox(vp, 18f, 0.32f, z, 1.2f, 0.64f, 11f, 0f, 0.62f, 0.64f, 0.67f, 1f, stats);
        }
    }

    private void drawMissionAreas(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        // Hız radarı direkleri.
        drawRadar(renderer, vp, 0f, 0f, stats);
        drawRadar(renderer, vp, 88f, 42f, stats);
        drawRadar(renderer, vp, -88f, -28f, stats);

        // Parkur alanı.
        renderer.drawBox(vp, -138f, 0.04f, 118f, 58f, 0.04f, 44f, 0f, 0.12f, 0.09f, 0.055f, 1f, stats);
        for (int i = 0; i < 6; i++) {
            float x = -162f + i * 10f;
            renderer.drawBox(vp, x, 0.42f, 116f + (i % 2) * 10f, 1.8f, 0.84f, 1.8f, 0f, 1f, 0.38f, 0.05f, 1f, stats);
        }

        // Görev tabelası.
        renderer.drawBox(vp, 32f, 2.6f, -122f, 8.8f, 2.0f, 0.45f, 0f, 0.05f, 0.55f, 0.95f, 1f, stats);
        renderer.drawBox(vp, 28f, 1.25f, -122f, 0.45f, 2.5f, 0.45f, 0f, 0.55f, 0.55f, 0.58f, 1f, stats);
        renderer.drawBox(vp, 36f, 1.25f, -122f, 0.45f, 2.5f, 0.45f, 0f, 0.55f, 0.55f, 0.58f, 1f, stats);
    }

    private void drawRadar(PrimitiveRenderer renderer, float[] vp, float x, float z, RenderStats stats) {
        renderer.drawBox(vp, x, 2.3f, z, 0.65f, 4.6f, 0.65f, 0f, 0.22f, 0.50f, 1f, 1f, stats);
        renderer.drawBox(vp, x, 4.75f, z, 3.7f, 1.0f, 0.34f, 0f, 0.08f, 0.10f, 0.15f, 1f, stats);
    }



    private void drawRaceCourse(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        // ArabaOyunu_30: botlu yarış modu rotası.
        float[] xs = new float[] { 0f, 0f, 70f, 128f, 88f, 0f, -88f, -128f, -70f, 0f };
        float[] zs = new float[] { -118f, -32f, 0f, 70f, 132f, 156f, 132f, 70f, 0f, -118f };

        for (int i = 0; i < xs.length; i++) {
            renderer.drawCircle(vp, xs[i], 0.075f, zs[i], i == 0 ? 20f : 13.5f, stats);
            if (i < xs.length - 1) {
                float nx = xs[i + 1] - xs[i];
                float nz = zs[i + 1] - zs[i];
                float yaw = (float) Math.atan2(nx, nz);
                float mx = (xs[i] + xs[i + 1]) * 0.5f;
                float mz = (zs[i] + zs[i + 1]) * 0.5f;
                renderer.drawBox(vp, mx, 0.071f, mz, 1.15f, 0.035f, 8.4f, yaw, 1f, 0.64f, 0.08f, 1f, stats);
            }
        }

        // Start/finish çizgisi.
        for (int i = -4; i <= 4; i++) {
            float color = (i & 1) == 0 ? 1f : 0.05f;
            renderer.drawBox(vp, i * 2.2f, 0.085f, -128f, 1.1f, 0.04f, 5.5f, 0f, color, color, color, 1f, stats);
        }
    }



    private void drawPoliceChaseArea(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        // ArabaOyunu_31: polis kovalamaca modu için polis istasyonu / uyarı alanı.
        renderer.drawBox(vp, 124f, 4.2f, -118f, 22f, 8.4f, 16f, 0f, 0.055f, 0.075f, 0.105f, 1f, stats);
        renderer.drawBox(vp, 124f, 9.1f, -118f, 24f, 1.2f, 18f, 0f, 0.08f, 0.10f, 0.14f, 1f, stats);
        renderer.drawBox(vp, 116f, 1.2f, -107f, 0.9f, 2.4f, 0.9f, 0f, 0.9f, 0.05f, 0.05f, 1f, stats);
        renderer.drawBox(vp, 132f, 1.2f, -107f, 0.9f, 2.4f, 0.9f, 0f, 0.05f, 0.15f, 0.95f, 1f, stats);
        renderer.drawBox(vp, 124f, 2.8f, -105f, 12f, 2.0f, 0.45f, 0f, 0.02f, 0.12f, 0.32f, 1f, stats);

        // Kaçış bölgesi görsel ipucu.
        renderer.drawCircle(vp, 0f, 0.065f, -96f, 82f, stats);
    }


}
