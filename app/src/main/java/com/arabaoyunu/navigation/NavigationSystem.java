package com.arabaoyunu.navigation;

/**
 * ArabaOyunu_39: Gelişmiş mini harita + navigasyon sistemi.
 *
 * ArabaOyunu_38'deki düz çizgi rota geliştirilerek:
 * - Büyük şehirde kavşak/ana yol takipli rota,
 * - Otoyolda ana şerit takipli rota,
 * - Açık alanda merkez yol/ana rota takipli rota,
 * - Polis bölgesi, garaj, tamir ve yarış hedeflerine akıllı yönlendirme
 * eklenmiştir.
 */
public final class NavigationSystem {

    public static final int ICON_NONE = 0;
    public static final int ICON_MISSION = 1;
    public static final int ICON_GARAGE = 2;
    public static final int ICON_RACE = 3;
    public static final int ICON_REPAIR = 4;
    public static final int ICON_POLICE = 5;

    public static final int MAX_POINTS = 12;
    public static final int MAX_ROUTE_POINTS = 8;

    private final NavPoint[] points = new NavPoint[MAX_POINTS];
    private final float[] routeX = new float[MAX_ROUTE_POINTS];
    private final float[] routeZ = new float[MAX_ROUTE_POINTS];

    private float routeTargetX;
    private float routeTargetZ;
    private int routeIcon = ICON_NONE;
    private String routeLabel = "-";
    private float routeDistance;
    private int pointCount;
    private int routeCount;
    private float mapHalf = 260f;

    public NavigationSystem() {
        for (int i = 0; i < points.length; i++) {
            points[i] = new NavPoint();
        }
    }

    public void update(String mapName, String modeName, float carX, float carZ) {
        pointCount = 0;
        routeCount = 0;
        routeIcon = ICON_NONE;
        routeLabel = "-";
        routeDistance = 0f;

        String map = mapName == null ? "" : mapName;
        String mode = modeName == null ? "" : modeName;
        boolean highway = map.indexOf("OTOYOL") >= 0;
        boolean city = map.indexOf("SEHIR") >= 0 || map.indexOf("BUYUK") >= 0;
        mapHalf = highway ? 560f : city ? 270f : 260f;

        populatePoints(highway, city);
        chooseRouteTarget(mode, carX, carZ);
        buildSmartRoute(highway, city, carX, carZ);
    }

    private void populatePoints(boolean highway, boolean city) {
        if (highway) {
            add(ICON_GARAGE, -86f, -160f, "GARAJ");
            add(ICON_REPAIR, 86f, 210f, "TAMIR");
            add(ICON_RACE, 0f, -440f, "YARIS");
            add(ICON_POLICE, 0f, 310f, "POLIS");
            add(ICON_MISSION, 0f, 440f, "GOREV");
            add(ICON_REPAIR, -86f, -160f, "SERVIS");
        } else if (city) {
            add(ICON_GARAGE, -42f, 178f, "GARAJ");
            add(ICON_REPAIR, 142f, -180f, "TAMIR");
            add(ICON_RACE, 0f, -228f, "YARIS");
            add(ICON_POLICE, 124f, -118f, "POLIS");
            add(ICON_MISSION, 40f, -120f, "GOREV");
            add(ICON_REPAIR, -180f, 120f, "TAMIR");
            add(ICON_GARAGE, 142f, -180f, "SERVIS");
        } else {
            add(ICON_GARAGE, -138f, 118f, "GARAJ");
            add(ICON_REPAIR, 88f, 42f, "TAMIR");
            add(ICON_RACE, 0f, -128f, "YARIS");
            add(ICON_POLICE, 124f, -118f, "POLIS");
            add(ICON_MISSION, 32f, -122f, "GOREV");
            add(ICON_REPAIR, -88f, -28f, "TAMIR");
        }
    }

    private void chooseRouteTarget(String mode, float carX, float carZ) {
        int preferred = ICON_MISSION;
        if (mode.indexOf("RaceMode") >= 0) preferred = ICON_RACE;
        else if (mode.indexOf("PoliceChaseMode") >= 0) preferred = ICON_POLICE;
        else if (mode.indexOf("TimeTrialMode") >= 0 || mode.indexOf("DriftMode") >= 0) preferred = ICON_RACE;

        int index = findNearest(preferred, carX, carZ);
        if (index < 0) index = findNearest(ICON_MISSION, carX, carZ);
        if (index < 0 && pointCount > 0) index = 0;
        if (index < 0) return;

        NavPoint p = points[index];
        routeTargetX = p.x;
        routeTargetZ = p.z;
        routeIcon = p.icon;
        routeLabel = p.label;
    }

    private void buildSmartRoute(boolean highway, boolean city, float carX, float carZ) {
        routeCount = 0;
        if (routeIcon <= 0) return;

        if (highway) {
            // Otoyolda rota önce en yakın ana şeride oturur, sonra hedef hizasına gider.
            float laneX = nearestOf(carX, -44f, 0f, 44f);
            addRoute(laneX, carZ);
            addRoute(laneX, routeTargetZ);
            addRoute(routeTargetX, routeTargetZ);
        } else if (city) {
            // Büyük şehir: yollar x/z = -120, -40, 40, 120 ana akslarıdır.
            float roadX = nearestCityRoadX(carX);
            float targetRoadZ = nearestCityRoadZ(routeTargetZ);
            addRoute(roadX, carZ);
            addRoute(roadX, targetRoadZ);
            addRoute(routeTargetX, targetRoadZ);
            addRoute(routeTargetX, routeTargetZ);
        } else {
            // Açık alan/test haritası: merkez yol ve yarış/görev rotası üzerinden kırılarak ilerler.
            float midZ = chooseOpenMidZ(routeTargetZ);
            addRoute(carX, midZ);
            addRoute(routeTargetX, midZ);
            addRoute(routeTargetX, routeTargetZ);
        }

        simplifyRoute(carX, carZ);
        routeDistance = computeRouteDistance(carX, carZ);
    }

    private void simplifyRoute(float carX, float carZ) {
        if (routeCount <= 1) return;
        int write = 0;
        float prevX = carX;
        float prevZ = carZ;
        for (int i = 0; i < routeCount; i++) {
            float x = routeX[i];
            float z = routeZ[i];
            float dx = x - prevX;
            float dz = z - prevZ;
            if (dx * dx + dz * dz < 8f * 8f && i < routeCount - 1) {
                continue;
            }
            routeX[write] = x;
            routeZ[write] = z;
            prevX = x;
            prevZ = z;
            write++;
        }
        routeCount = Math.max(1, write);
    }

    private float computeRouteDistance(float carX, float carZ) {
        float total = 0f;
        float px = carX;
        float pz = carZ;
        for (int i = 0; i < routeCount; i++) {
            float dx = routeX[i] - px;
            float dz = routeZ[i] - pz;
            total += (float)Math.sqrt(dx * dx + dz * dz);
            px = routeX[i];
            pz = routeZ[i];
        }
        return total;
    }

    private float chooseOpenMidZ(float targetZ) {
        if (routeIcon == ICON_RACE) return -118f;
        if (routeIcon == ICON_POLICE) return -96f;
        if (routeIcon == ICON_REPAIR) return 0f;
        if (targetZ > 40f) return 70f;
        return -32f;
    }

    private float nearestCityRoadX(float x) {
        return nearestOf(x, -120f, -40f, 40f, 120f);
    }

    private float nearestCityRoadZ(float z) {
        return nearestOf(z, -120f, -40f, 40f, 120f);
    }

    private float nearestOf(float value, float a, float b, float c) {
        float best = a;
        float bestD = Math.abs(value - a);
        float db = Math.abs(value - b);
        if (db < bestD) { best = b; bestD = db; }
        float dc = Math.abs(value - c);
        if (dc < bestD) best = c;
        return best;
    }

    private float nearestOf(float value, float a, float b, float c, float d) {
        float best = nearestOf(value, a, b, c);
        if (Math.abs(value - d) < Math.abs(value - best)) best = d;
        return best;
    }

    private void addRoute(float x, float z) {
        if (routeCount >= MAX_ROUTE_POINTS) return;
        routeX[routeCount] = x;
        routeZ[routeCount] = z;
        routeCount++;
    }

    private int findNearest(int icon, float carX, float carZ) {
        int best = -1;
        float bestD = 999999f;
        for (int i = 0; i < pointCount; i++) {
            NavPoint p = points[i];
            if (p.icon != icon) continue;
            float dx = p.x - carX;
            float dz = p.z - carZ;
            float d = dx * dx + dz * dz;
            if (d < bestD) {
                bestD = d;
                best = i;
            }
        }
        return best;
    }

    private void add(int icon, float x, float z, String label) {
        if (pointCount >= points.length) return;
        NavPoint p = points[pointCount++];
        p.icon = icon;
        p.x = x;
        p.z = z;
        p.label = label == null ? "-" : label;
    }

    public int getPointCount() { return pointCount; }
    public int getIcon(int index) { return index >= 0 && index < pointCount ? points[index].icon : ICON_NONE; }
    public float getX(int index) { return index >= 0 && index < pointCount ? points[index].x : 0f; }
    public float getZ(int index) { return index >= 0 && index < pointCount ? points[index].z : 0f; }
    public String getLabel(int index) { return index >= 0 && index < pointCount ? points[index].label : "-"; }

    public float getRouteTargetX() { return routeTargetX; }
    public float getRouteTargetZ() { return routeTargetZ; }
    public int getRouteIcon() { return routeIcon; }
    public String getRouteLabel() { return routeLabel; }
    public float getRouteDistance() { return routeDistance; }
    public float getMapHalf() { return mapHalf; }

    public int getRouteCount() { return routeCount; }
    public float getRouteX(int index) { return index >= 0 && index < routeCount ? routeX[index] : routeTargetX; }
    public float getRouteZ(int index) { return index >= 0 && index < routeCount ? routeZ[index] : routeTargetZ; }

    public static final class NavPoint {
        public int icon;
        public float x;
        public float z;
        public String label = "-";
    }
}
