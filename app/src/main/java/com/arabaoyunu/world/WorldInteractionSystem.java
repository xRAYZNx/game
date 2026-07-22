package com.arabaoyunu.world;

import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;
import com.arabaoyunu.util.SaveManager;

/**
 * ArabaOyunu_42: Açık dünya görev noktaları + oyun içi etkileşim sistemi.
 *
 * Menü sistemi korunur; aynı özellikler oyun içindeki fiziksel noktalardan da başlatılabilir.
 */
public final class WorldInteractionSystem {

    public static final int MAX_POINTS = 16;

    private final WorldPoint[] points = new WorldPoint[MAX_POINTS];

    private int pointCount;
    private int activeIndex = -1;
    private float activeDistance = 9999f;
    private String actionMessage = "";
    private float actionMessageTimer;
    private float pulse;

    public WorldInteractionSystem() {
        for (int i = 0; i < points.length; i++) {
            points[i] = new WorldPoint();
        }
    }

    public void update(float dt, String mapName, float carX, float carZ, SaveManager saveManager) {
        if (dt < 0f) dt = 0f;
        if (dt > 0.12f) dt = 0.12f;
        pulse += dt * 2.6f;
        if (pulse > 1000f) pulse = 0f;

        populate(mapName);
        activeIndex = -1;
        activeDistance = 9999f;

        for (int i = 0; i < pointCount; i++) {
            WorldPoint p = points[i];
            float dx = p.x - carX;
            float dz = p.z - carZ;
            float d = (float)Math.sqrt(dx * dx + dz * dz);
            if (d < p.radius && d < activeDistance) {
                activeDistance = d;
                activeIndex = i;
            }
        }

        if (actionMessageTimer > 0f) {
            actionMessageTimer -= dt;
            if (actionMessageTimer <= 0f) actionMessage = "";
        }
    }

    private void populate(String mapName) {
        pointCount = 0;
        String map = mapName == null ? "" : mapName;
        // A61.7: Harita GLB sistemi bırakıldıktan sonra Açık Test Alanı sadece
        // sürüş/kamera/fizik doğrulama alanıdır. Burada görev/servis kolonları
        // otomatik görünmez; böylece oyuncu boş alana girince üstte tamir/görev
        // paneli ve neon işaretler ekranı kaplamaz.
        if (map.indexOf("TestMap_OpenField") >= 0) {
            return;
        }
        boolean highway = map.indexOf("OTOYOL") >= 0;
        boolean city = map.indexOf("SEHIR") >= 0 || map.indexOf("BUYUK") >= 0;
        boolean drift = map.indexOf("DRIFT") >= 0;

        if (highway) {
            add(WorldPointType.RACE, 0f, -440f, 22f, 1, "Otoyol Yarışı", "Uzun düzlük hız yarışı", "+XP +Coin");
            add(WorldPointType.TIME_TRIAL, 44f, -300f, 18f, 1, "Radar Süresi", "Süreye karşı otoyol sprinti", "+XP");
            add(WorldPointType.POLICE_ESCAPE, 0f, 310f, 24f, 2, "Otoyol Kaçışı", "Polis kovalamaca başlat", "+Kasa şansı");
            add(WorldPointType.GARAGE, -86f, -160f, 18f, 1, "Otoyol Garajı", "Aracı değiştir / modifiye et", "Garaj");
            add(WorldPointType.VEHICLE_GALLERY, -96f, -164f, 18f, 1, "Galeri", "Yeni araçlara bak", "Araçlar");
            add(WorldPointType.REPAIR, 86f, 210f, 18f, 1, "Servis", "Hasarı onar", "Tamir");
            add(WorldPointType.FUEL_SERVICE, 86f, 240f, 16f, 1, "Benzin / Bakım", "Hızlı bakım ve temizlik", "+Bakım");
            add(WorldPointType.SPECIAL_EVENT, -44f, 420f, 20f, 4, "Haftalık Etkinlik", "Yüksek ödüllü özel etkinlik", "+Kasa");
        } else if (city) {
            add(WorldPointType.RACE, 0f, -228f, 20f, 1, "Şehir Yarışı", "Kavşaklı cadde rotası", "+XP +Coin");
            add(WorldPointType.DRIFT, -42f, 178f, 18f, 1, "Park Drift", "Drift alanına gir", "+XP");
            add(WorldPointType.TIME_TRIAL, 40f, -120f, 18f, 1, "Şehir Zaman Yarışı", "Kontrol noktalarını geç", "+XP");
            add(WorldPointType.POLICE_ESCAPE, 124f, -118f, 22f, 2, "Polis Bölgesi", "Şehir kaçışını başlat", "+Kasa şansı");
            add(WorldPointType.GARAGE, -42f, 178f, 18f, 1, "Merkez Garaj", "Garaj / modifiye", "Garaj");
            add(WorldPointType.VEHICLE_GALLERY, 142f, -180f, 18f, 1, "Araç Galerisi", "Kilitli araçları gör", "Araçlar");
            add(WorldPointType.REPAIR, 142f, -150f, 16f, 1, "Tamirci", "Hasar onarımı", "Tamir");
            add(WorldPointType.FUEL_SERVICE, -180f, 120f, 16f, 1, "Bakım İstasyonu", "Benzin / lastik / genel bakım", "+Bakım");
            add(WorldPointType.SPECIAL_EVENT, -120f, -120f, 18f, 3, "Neon Etkinlik", "Özel şehir etkinliği", "+XP +Kasa");
        } else if (drift) {
            add(WorldPointType.DRIFT, 0f, 0f, 26f, 1, "Ana Drift Alanı", "Drift modunu başlat", "+XP");
            add(WorldPointType.TIME_TRIAL, 52f, 64f, 18f, 1, "Drift Sprint", "Kısa süre mücadelesi", "+XP");
            add(WorldPointType.GARAGE, -72f, 42f, 18f, 1, "Drift Garaj", "Ayar ve modifiye", "Garaj");
            add(WorldPointType.REPAIR, -88f, -38f, 16f, 1, "Drift Servis", "Lastik / hasar bakımı", "Tamir");
            add(WorldPointType.SPECIAL_EVENT, 92f, -54f, 20f, 4, "Özel Drift Etkinliği", "Yüksek skor etkinliği", "+Kasa");
        } else {
            add(WorldPointType.RACE, 0f, -128f, 20f, 1, "Test Yarışı", "Yarış modunu başlat", "+XP +Coin");
            add(WorldPointType.DRIFT, -138f, 118f, 18f, 1, "Drift Noktası", "Drift çalışması", "+XP");
            add(WorldPointType.TIME_TRIAL, 32f, -122f, 18f, 1, "Zaman Yarışı", "Süreye karşı rota", "+XP");
            add(WorldPointType.POLICE_ESCAPE, 124f, -118f, 22f, 2, "Polis Kaçış", "Kovalamaca başlat", "+Kasa şansı");
            add(WorldPointType.GARAGE, -138f, 118f, 18f, 1, "Garaj", "Araç / modifiye", "Garaj");
            add(WorldPointType.VEHICLE_GALLERY, -112f, 86f, 16f, 1, "Araç Galerisi", "Kilitli araçları incele", "Araçlar");
            add(WorldPointType.REPAIR, 88f, 42f, 16f, 1, "Tamir Noktası", "Hasarı onar", "Tamir");
            add(WorldPointType.FUEL_SERVICE, -88f, -28f, 16f, 1, "Benzin / Bakım", "Hızlı bakım", "+Bakım");
            add(WorldPointType.SPECIAL_EVENT, 118f, 84f, 18f, 3, "Özel Etkinlik", "Açık dünya etkinliği", "+XP +Kasa");
        }
    }

    private void add(int type, float x, float z, float radius, int requiredLevel, String title, String subtitle, String reward) {
        if (pointCount >= points.length) return;
        points[pointCount++].set(type, x, z, radius, requiredLevel, title, subtitle, reward);
    }

    public void render(PrimitiveRenderer renderer, float[] vp, RenderStats stats) {
        if (renderer == null || vp == null) return;
        float pulse01 = 0.5f + 0.5f * (float)Math.sin(pulse);
        for (int i = 0; i < pointCount; i++) {
            WorldPoint p = points[i];
            float[] c = colorFor(p.type);
            float ring = p.radius * (0.42f + pulse01 * 0.08f);
            renderer.drawCircle(vp, p.x, 0.088f, p.z, ring, stats);
            renderer.drawBox(vp, p.x, 1.55f + pulse01 * 0.22f, p.z, 1.2f, 3.1f, 1.2f, pulse * 0.55f, c[0], c[1], c[2], 1f, stats);
            renderer.drawBox(vp, p.x, 3.25f + pulse01 * 0.30f, p.z, 4.6f, 0.45f, 4.6f, pulse * 0.75f, c[0], c[1], c[2], 0.95f, stats);
        }
    }

    private float[] colorFor(int type) {
        if (type == WorldPointType.RACE) return new float[] {0.10f, 0.95f, 0.28f};
        if (type == WorldPointType.DRIFT) return new float[] {0.70f, 0.25f, 1.00f};
        if (type == WorldPointType.TIME_TRIAL) return new float[] {0.05f, 0.75f, 1.00f};
        if (type == WorldPointType.POLICE_ESCAPE) return new float[] {0.10f, 0.18f, 1.00f};
        if (type == WorldPointType.GARAGE) return new float[] {1.00f, 0.78f, 0.20f};
        if (type == WorldPointType.VEHICLE_GALLERY) return new float[] {1.00f, 0.48f, 0.12f};
        if (type == WorldPointType.REPAIR) return new float[] {1.00f, 0.10f, 0.10f};
        if (type == WorldPointType.FUEL_SERVICE) return new float[] {0.15f, 0.95f, 0.72f};
        if (type == WorldPointType.SPECIAL_EVENT) return new float[] {1.00f, 0.12f, 0.82f};
        return new float[] {1f, 1f, 1f};
    }

    public boolean hasActivePoint() { return activeIndex >= 0; }
    public int getActiveType() { return activeIndex >= 0 ? points[activeIndex].type : WorldPointType.NONE; }
    public String getActiveTitle() { return activeIndex >= 0 ? points[activeIndex].title : ""; }
    public String getActiveSubtitle() { return activeIndex >= 0 ? points[activeIndex].subtitle : ""; }
    public String getActiveReward() { return activeIndex >= 0 ? points[activeIndex].reward : ""; }
    public int getActiveRequiredLevel() { return activeIndex >= 0 ? points[activeIndex].requiredLevel : 1; }
    public float getActiveDistance() { return activeDistance; }
    public String getActionText() { return WorldPointType.action(getActiveType()); }

    public boolean isLevelAllowed(SaveManager saveManager) {
        if (activeIndex < 0) return false;
        return saveManager == null || saveManager.getPlayerLevel() >= points[activeIndex].requiredLevel;
    }

    public void setActionMessage(String message) {
        actionMessage = message == null ? "" : message;
        actionMessageTimer = 3.4f;
    }

    public String getActionMessage() { return actionMessage == null ? "" : actionMessage; }

    public int getPointCount() { return pointCount; }
    public int getType(int index) { return index >= 0 && index < pointCount ? points[index].type : WorldPointType.NONE; }
    public float getX(int index) { return index >= 0 && index < pointCount ? points[index].x : 0f; }
    public float getZ(int index) { return index >= 0 && index < pointCount ? points[index].z : 0f; }
}
