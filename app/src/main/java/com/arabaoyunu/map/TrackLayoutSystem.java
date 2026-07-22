package com.arabaoyunu.map;

import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.mode.GameModeCoordinator;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;

/**
 * A62.9: GLB harita eklemeden modlara özel hafif pist/arena çizim sistemi.
 *
 * Büyük model/texture kullanmaz. Sadece PrimitiveRenderer ile asfalt plakaları,
 * şerit çizgileri, bariyerler, koniler, checkpoint rota kılavuzları, drag şeridi,
 * drift halkası ve polis kaçış alanı çizer. Böylece Açık Dünya kapalı kalırken
 * oyun boş Open Field hissinden çıkar.
 */
public final class TrackLayoutSystem {

    public static final int LAYOUT_FREE = 0;
    public static final int LAYOUT_CHECKPOINT = 1;
    public static final int LAYOUT_DRAG = 2;
    public static final int LAYOUT_DRIFT = 3;
    public static final int LAYOUT_POLICE = 4;

    public static final class SpawnPoint {
        public final float x;
        public final float y;
        public final float z;
        public final float yaw;

        public SpawnPoint(float x, float y, float z, float yaw) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
        }
    }

    private static final SpawnPoint FREE_SPAWN = new SpawnPoint(0f, 0.42f, -18f, 0f);
    private static final SpawnPoint CHECKPOINT_SPAWN = new SpawnPoint(0f, 0.42f, -142f, 0f);
    private static final SpawnPoint DRAG_SPAWN = new SpawnPoint(-5.8f, 0.42f, -190f, 0f);
    private static final SpawnPoint DRIFT_SPAWN = new SpawnPoint(0f, 0.42f, -46f, 0f);
    private static final SpawnPoint POLICE_SPAWN = new SpawnPoint(0f, 0.42f, -92f, 0f);

    private static final float[] CP_X = new float[] { 0f, 0f, 52f, 108f, 114f, 42f, -44f, -104f, -56f, 0f };
    private static final float[] CP_Z = new float[] { -132f, -42f, 20f, 72f, 132f, 172f, 132f, 60f, -18f, -132f };

    public int layoutFor(GameModeCoordinator coordinator, int selectedMode) {
        if (coordinator != null) {
            if (coordinator.isPoliceChase()) return LAYOUT_POLICE;
            if (coordinator.isDrift()) return LAYOUT_DRIFT;
            if (coordinator.isDragRace()) return LAYOUT_DRAG;
            if (coordinator.isCheckpointRace()) return LAYOUT_CHECKPOINT;
        }
        if (selectedMode == GameScreenState.MODE_POLICE_CHASE) return LAYOUT_POLICE;
        if (selectedMode == GameScreenState.MODE_DRIFT) return LAYOUT_DRIFT;
        if (selectedMode == GameScreenState.MODE_DRAG_RACE) return LAYOUT_DRAG;
        if (selectedMode == GameScreenState.MODE_RACE_LOCKED) return LAYOUT_CHECKPOINT;
        return LAYOUT_FREE;
    }

    public SpawnPoint spawnForMode(int selectedMode) {
        if (selectedMode == GameScreenState.MODE_POLICE_CHASE) return POLICE_SPAWN;
        if (selectedMode == GameScreenState.MODE_DRIFT) return DRIFT_SPAWN;
        if (selectedMode == GameScreenState.MODE_DRAG_RACE) return DRAG_SPAWN;
        if (selectedMode == GameScreenState.MODE_RACE_LOCKED) return CHECKPOINT_SPAWN;
        return FREE_SPAWN;
    }

    public String labelForLayout(int layout) {
        if (layout == LAYOUT_CHECKPOINT) return "CHECKPOINT TEST PISTI";
        if (layout == LAYOUT_DRAG) return "DRAG 400M SERIDI";
        if (layout == LAYOUT_DRIFT) return "DRIFT ARENA";
        if (layout == LAYOUT_POLICE) return "POLIS KACIS ALANI";
        return "OPEN FIELD TEST PISTI";
    }

    public void renderForMode(
            PrimitiveRenderer renderer,
            float[] vp,
            RenderStats stats,
            GameModeCoordinator coordinator,
            int selectedMode,
            float carX,
            float carZ
    ) {
        if (renderer == null || vp == null) return;
        int layout = layoutFor(coordinator, selectedMode);
        drawModeBackplate(renderer, vp, stats, layout);
        if (layout == LAYOUT_DRAG) {
            drawDragStrip(renderer, vp, stats);
        } else if (layout == LAYOUT_CHECKPOINT) {
            drawCheckpointTrack(renderer, vp, stats);
        } else if (layout == LAYOUT_DRIFT) {
            drawDriftArena(renderer, vp, stats);
        } else if (layout == LAYOUT_POLICE) {
            drawPoliceEscapeArena(renderer, vp, stats, carX, carZ);
        } else {
            drawFreeDriveTestArea(renderer, vp, stats);
        }
    }

    private void drawModeBackplate(PrimitiveRenderer r, float[] vp, RenderStats s, int layout) {
        // Aktif pist alanını temel zemin üstünde biraz ayıran hafif asfalt panel.
        if (layout == LAYOUT_DRAG) {
            r.drawBox(vp, 0f, 0.041f, 12f, 38f, 0.025f, 455f, 0f, 0.045f, 0.050f, 0.062f, 1f, s);
        } else if (layout == LAYOUT_CHECKPOINT) {
            r.drawBox(vp, 0f, 0.040f, 26f, 280f, 0.025f, 340f, 0f, 0.042f, 0.048f, 0.058f, 1f, s);
        } else if (layout == LAYOUT_DRIFT) {
            r.drawBox(vp, 0f, 0.040f, 0f, 210f, 0.025f, 210f, 0f, 0.043f, 0.046f, 0.055f, 1f, s);
        } else if (layout == LAYOUT_POLICE) {
            r.drawBox(vp, 0f, 0.040f, -10f, 270f, 0.025f, 300f, 0f, 0.046f, 0.050f, 0.058f, 1f, s);
        }
    }

    private void drawFreeDriveTestArea(PrimitiveRenderer r, float[] vp, RenderStats s) {
        // Geniş hız deneme şeritleri.
        r.drawBox(vp, -28f, 0.082f, 0f, 7.0f, 0.030f, 330f, 0f, 0.068f, 0.076f, 0.090f, 1f, s);
        r.drawBox(vp, 28f, 0.082f, 0f, 7.0f, 0.030f, 330f, 0f, 0.068f, 0.076f, 0.090f, 1f, s);
        for (float z = -150f; z <= 150f; z += 25f) {
            r.drawBox(vp, -28f, 0.112f, z, 0.40f, 0.030f, 8.0f, 0f, 0.20f, 0.82f, 1f, 1f, s);
            r.drawBox(vp, 28f, 0.112f, z, 0.40f, 0.030f, 8.0f, 0f, 0.20f, 0.82f, 1f, 1f, s);
        }

        // Park / manevra alanı.
        r.drawBox(vp, -88f, 0.070f, 74f, 64f, 0.030f, 42f, 0f, 0.055f, 0.060f, 0.070f, 1f, s);
        for (int i = 0; i < 7; i++) {
            float x = -116f + i * 9.5f;
            r.drawLine(vp, x, 0.116f, 54f, x + 7f, 0.116f, 94f, 0.70f, 0.78f, 0.82f, s);
        }

        // Slalom konileri ve fren alanı.
        for (int i = 0; i < 10; i++) {
            float z = -122f + i * 24f;
            float x = (i % 2 == 0) ? 70f : 92f;
            drawCone(r, vp, x, z, s);
        }
        r.drawBox(vp, 92f, 0.080f, -152f, 52f, 0.032f, 12f, 0f, 0.55f, 0.04f, 0.04f, 1f, s);
        r.drawBox(vp, 92f, 0.084f, -130f, 52f, 0.032f, 8f, 0f, 0.95f, 0.82f, 0.08f, 1f, s);
        r.drawBox(vp, 92f, 0.088f, -108f, 52f, 0.032f, 6f, 0f, 0.06f, 0.85f, 0.18f, 1f, s);

        drawArrow(r, vp, 0f, -94f, 0f, 0.12f, 0.82f, 1f, s);
        drawArrow(r, vp, 0f, 94f, 3.14159f, 0.12f, 0.82f, 1f, s);
        drawLowBarriers(r, vp, -126f, 126f, -176f, s);
        drawLowBarriers(r, vp, -126f, 126f, 176f, s);
    }

    private void drawCheckpointTrack(PrimitiveRenderer r, float[] vp, RenderStats s) {
        // Çift çizgili rota koridoru: RaceMode checkpoint kapılarının altında yolu okunur yapar.
        for (int i = 0; i < CP_X.length - 1; i++) {
            float x0 = CP_X[i];
            float z0 = CP_Z[i];
            float x1 = CP_X[i + 1];
            float z1 = CP_Z[i + 1];
            float dx = x1 - x0;
            float dz = z1 - z0;
            float len = (float)Math.sqrt(dx * dx + dz * dz);
            if (len < 1f) continue;
            float yaw = (float)Math.atan2(dx, dz);
            float mx = (x0 + x1) * 0.5f;
            float mz = (z0 + z1) * 0.5f;
            r.drawBox(vp, mx, 0.075f, mz, 18.0f, 0.030f, len + 8f, yaw, 0.055f, 0.060f, 0.074f, 1f, s);
            r.drawBox(vp, mx, 0.118f, mz, 0.55f, 0.032f, len - 4f, yaw, 1f, 0.86f, 0.12f, 1f, s);
            drawSegmentEdge(r, vp, mx, mz, len, yaw, 9.2f, 0.10f, 0.78f, 1f, s);
            drawSegmentEdge(r, vp, mx, mz, len, yaw, -9.2f, 0.10f, 0.78f, 1f, s);
        }
        for (int i = 1; i < CP_X.length - 1; i++) {
            drawArrow(r, vp, CP_X[i], CP_Z[i] - 9f, 0f, 0.05f, 0.95f, 0.25f, s);
            drawCone(r, vp, CP_X[i] + 14f, CP_Z[i], s);
            drawCone(r, vp, CP_X[i] - 14f, CP_Z[i], s);
        }
        drawCheckerLine(r, vp, 0f, -132f, 24f, 0f, false, s);
        drawCheckerLine(r, vp, 0f, -132f, 28f, 0f, true, s);
    }

    private void drawDragStrip(PrimitiveRenderer r, float[] vp, RenderStats s) {
        float startZ = -190f;
        float finishZ = 210f;
        float centerZ = 10f;
        r.drawBox(vp, -5.8f, 0.070f, centerZ, 11.2f, 0.030f, 430f, 0f, 0.050f, 0.055f, 0.068f, 1f, s);
        r.drawBox(vp, 5.8f, 0.070f, centerZ, 11.2f, 0.030f, 430f, 0f, 0.050f, 0.055f, 0.068f, 1f, s);
        r.drawBox(vp, 0f, 0.120f, centerZ, 0.45f, 0.030f, 420f, 0f, 1f, 0.92f, 0.18f, 1f, s);
        r.drawBox(vp, -12.8f, 0.120f, centerZ, 0.55f, 0.030f, 428f, 0f, 0.10f, 0.78f, 1f, 1f, s);
        r.drawBox(vp, 12.8f, 0.120f, centerZ, 0.55f, 0.030f, 428f, 0f, 0.10f, 0.78f, 1f, 1f, s);
        for (int i = 1; i <= 3; i++) {
            float z = startZ + i * 100f;
            r.drawLine(vp, -18f, 0.145f, z, 18f, 0.145f, z, 0.40f, 0.78f, 1f, s);
            r.drawBox(vp, -20.5f, 1.2f, z, 1.4f, 2.4f, 1.4f, 0f, 0.12f, 0.38f, 1f, 1f, s);
            r.drawBox(vp, 20.5f, 1.2f, z, 1.4f, 2.4f, 1.4f, 0f, 0.12f, 0.38f, 1f, 1f, s);
        }
        drawCheckerLine(r, vp, 0f, startZ, 34f, 0f, false, s);
        drawCheckerLine(r, vp, 0f, finishZ, 36f, 0f, true, s);
        drawStartLightGantry(r, vp, startZ - 14f, s);
        drawLowBarriers(r, vp, -23f, -23f, startZ, finishZ, s);
        drawLowBarriers(r, vp, 23f, 23f, startZ, finishZ, s);
    }

    private void drawDriftArena(PrimitiveRenderer r, float[] vp, RenderStats s) {
        r.drawCircle(vp, 0f, 0.125f, 0f, 34f, s);
        r.drawCircle(vp, 0f, 0.126f, 0f, 58f, s);
        r.drawCircle(vp, 0f, 0.127f, 0f, 88f, s);
        r.drawBox(vp, 0f, 0.086f, -64f, 130f, 0.030f, 9f, 0f, 0.055f, 0.062f, 0.074f, 1f, s);
        r.drawBox(vp, 0f, 0.120f, -64f, 120f, 0.030f, 0.55f, 0f, 0.98f, 0.78f, 0.08f, 1f, s);
        for (int i = 0; i < 16; i++) {
            float a = (float)(i * Math.PI * 2.0 / 16.0);
            float x = (float)Math.cos(a) * 76f;
            float z = (float)Math.sin(a) * 76f;
            drawCone(r, vp, x, z, s);
        }
        for (int i = 0; i < 8; i++) {
            float a = (float)(i * Math.PI * 2.0 / 8.0);
            drawArrow(r, vp, (float)Math.cos(a) * 48f, (float)Math.sin(a) * 48f, -a + 1.5707f, 0.72f, 0.20f, 1f, s);
        }
        r.drawBox(vp, -96f, 0.095f, 0f, 8f, 0.030f, 120f, 0f, 0.52f, 0.05f, 0.10f, 1f, s);
        r.drawBox(vp, 96f, 0.095f, 0f, 8f, 0.030f, 120f, 0f, 0.52f, 0.05f, 0.10f, 1f, s);
    }

    private void drawPoliceEscapeArena(PrimitiveRenderer r, float[] vp, RenderStats s, float carX, float carZ) {
        // Kaçış koridoru ve risk bölgeleri.
        r.drawBox(vp, 0f, 0.080f, -18f, 120f, 0.030f, 220f, 0f, 0.055f, 0.062f, 0.073f, 1f, s);
        r.drawBox(vp, -64f, 0.090f, -20f, 14f, 0.030f, 205f, 0f, 0.18f, 0.04f, 0.05f, 1f, s);
        r.drawBox(vp, 64f, 0.090f, -20f, 14f, 0.030f, 205f, 0f, 0.18f, 0.04f, 0.05f, 1f, s);
        for (float z = -120f; z <= 92f; z += 34f) {
            r.drawBox(vp, 0f, 0.128f, z, 52f, 0.030f, 0.65f, 0f, 0.12f, 0.48f, 1f, 1f, s);
        }
        drawPoliceSpawnBox(r, vp, -22f, -138f, s);
        drawPoliceSpawnBox(r, vp, 22f, -138f, s);
        drawCheckerLine(r, vp, 0f, 116f, 86f, 0f, true, s);
        r.drawCircle(vp, 0f, 0.132f, 116f, 42f, s);
        drawArrow(r, vp, -34f, -72f, 0f, 0.10f, 0.78f, 1f, s);
        drawArrow(r, vp, 34f, -28f, 0f, 0.10f, 0.78f, 1f, s);
        drawArrow(r, vp, 0f, 36f, 0f, 0.10f, 0.78f, 1f, s);
        // Oyuncu etrafında hafif kaçış yarıçapı referansı.
        if (carX == carX && carZ == carZ) {
            r.drawCircle(vp, carX, 0.138f, carZ, 36f, s);
        }
    }

    private void drawSegmentEdge(PrimitiveRenderer r, float[] vp, float mx, float mz, float len, float yaw, float offset, float cr, float cg, float cb, RenderStats s) {
        float sin = (float)Math.sin(yaw);
        float cos = (float)Math.cos(yaw);
        float ox = cos * offset;
        float oz = -sin * offset;
        r.drawBox(vp, mx + ox, 0.122f, mz + oz, 0.45f, 0.032f, len, yaw, cr, cg, cb, 1f, s);
    }

    private void drawArrow(PrimitiveRenderer r, float[] vp, float x, float z, float yaw, float cr, float cg, float cb, RenderStats s) {
        r.drawBox(vp, x, 0.150f, z, 1.2f, 0.040f, 9.5f, yaw, cr, cg, cb, 1f, s);
        float tipX = x + (float)Math.sin(yaw) * 6.2f;
        float tipZ = z + (float)Math.cos(yaw) * 6.2f;
        r.drawBox(vp, tipX - (float)Math.cos(yaw) * 2.0f, 0.155f, tipZ + (float)Math.sin(yaw) * 2.0f, 1.1f, 0.040f, 5.8f, yaw + 0.65f, cr, cg, cb, 1f, s);
        r.drawBox(vp, tipX + (float)Math.cos(yaw) * 2.0f, 0.155f, tipZ - (float)Math.sin(yaw) * 2.0f, 1.1f, 0.040f, 5.8f, yaw - 0.65f, cr, cg, cb, 1f, s);
    }

    private void drawCone(PrimitiveRenderer r, float[] vp, float x, float z, RenderStats s) {
        r.drawBox(vp, x, 0.32f, z, 1.35f, 0.64f, 1.35f, 0f, 1f, 0.34f, 0.05f, 1f, s);
        r.drawBox(vp, x, 0.78f, z, 0.82f, 0.34f, 0.82f, 0f, 1f, 0.72f, 0.08f, 1f, s);
    }

    private void drawCheckerLine(PrimitiveRenderer r, float[] vp, float x, float z, float width, float yaw, boolean finish, RenderStats s) {
        int cells = 12;
        float cell = width / cells;
        for (int i = 0; i < cells; i++) {
            float offset = -width * 0.5f + cell * (i + 0.5f);
            float color = (i & 1) == 0 ? 1f : 0.06f;
            float ox = (float)Math.cos(yaw) * offset;
            float oz = -(float)Math.sin(yaw) * offset;
            float red = finish ? 1f : color;
            float green = finish ? color : color;
            float blue = finish ? 0.08f : color;
            r.drawBox(vp, x + ox, 0.170f, z + oz, cell * 0.88f, 0.045f, 6.0f, yaw, red, green, blue, 1f, s);
        }
    }

    private void drawStartLightGantry(PrimitiveRenderer r, float[] vp, float z, RenderStats s) {
        r.drawBox(vp, -16f, 2.7f, z, 0.9f, 5.4f, 0.9f, 0f, 0.07f, 0.08f, 0.10f, 1f, s);
        r.drawBox(vp, 16f, 2.7f, z, 0.9f, 5.4f, 0.9f, 0f, 0.07f, 0.08f, 0.10f, 1f, s);
        r.drawBox(vp, 0f, 5.0f, z, 34f, 0.9f, 0.9f, 0f, 0.07f, 0.08f, 0.10f, 1f, s);
        r.drawBox(vp, -7f, 5.2f, z - 0.8f, 2.4f, 0.55f, 0.5f, 0f, 1f, 0.08f, 0.04f, 1f, s);
        r.drawBox(vp, 0f, 5.2f, z - 0.8f, 2.4f, 0.55f, 0.5f, 0f, 1f, 0.74f, 0.04f, 1f, s);
        r.drawBox(vp, 7f, 5.2f, z - 0.8f, 2.4f, 0.55f, 0.5f, 0f, 0.04f, 1f, 0.16f, 1f, s);
    }

    private void drawPoliceSpawnBox(PrimitiveRenderer r, float[] vp, float x, float z, RenderStats s) {
        r.drawBox(vp, x, 0.080f, z, 18f, 0.030f, 18f, 0f, 0.05f, 0.09f, 0.16f, 1f, s);
        r.drawBox(vp, x, 0.130f, z, 15f, 0.030f, 0.8f, 0f, 0.05f, 0.35f, 1f, 1f, s);
        r.drawBox(vp, x, 0.135f, z - 7f, 15f, 0.030f, 0.8f, 0f, 0.95f, 0.05f, 0.05f, 1f, s);
    }

    private void drawLowBarriers(PrimitiveRenderer r, float[] vp, float fromX, float toX, float z, RenderStats s) {
        float min = Math.min(fromX, toX);
        float max = Math.max(fromX, toX);
        for (float x = min; x <= max; x += 18f) {
            r.drawBox(vp, x, 0.44f, z, 12f, 0.88f, 1.1f, 0f, 0.52f, 0.55f, 0.58f, 1f, s);
        }
    }

    private void drawLowBarriers(PrimitiveRenderer r, float[] vp, float x0, float x1, float z0, float z1, RenderStats s) {
        // Dikey bariyer hattı için x0/x1 aynı tutulur; imza karmaşayı önler.
        float x = (x0 + x1) * 0.5f;
        float min = Math.min(z0, z1);
        float max = Math.max(z0, z1);
        for (float z = min; z <= max; z += 18f) {
            r.drawBox(vp, x, 0.44f, z, 1.1f, 0.88f, 12f, 0f, 0.52f, 0.55f, 0.58f, 1f, s);
        }
    }
}
