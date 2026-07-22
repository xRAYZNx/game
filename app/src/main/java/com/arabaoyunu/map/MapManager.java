package com.arabaoyunu.map;

import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;

/**
 * Çoklu harita yöneticisi.
 * A61_6: GLB açık dünya haritası kaldırıldığı için aktif sürüş dahili haritalarla yürür.
 * Eski/pasif GLB slot istekleri Açık Test Alanı'na düşer.
 */
public final class MapManager {

    private BaseMap currentMap;
    private MapDefinition currentDefinition;
    private final LargeMapLoadGuard loadGuard = new LargeMapLoadGuard();

    public MapManager(BaseMap initialMap) {
        currentMap = initialMap;
        currentDefinition = MapRegistry.definitionFor(GameScreenState.MAP_OPEN_FIELD);
    }

    public BaseMap getCurrentMap() {
        return currentMap;
    }

    public MapDefinition getCurrentDefinition() {
        return currentDefinition;
    }

    public LargeMapLoadGuard getLoadGuard() {
        return loadGuard;
    }

    public void setCurrentMap(BaseMap map) {
        currentMap = map;
        if (map != null) {
            currentDefinition = definitionFromMapName(map.getName());
        }
        loadGuard.reset();
    }

    public void setCurrentMap(BaseMap map, MapDefinition definition) {
        currentMap = map;
        currentDefinition = definition == null ? definitionFromMapName(map == null ? "" : map.getName()) : definition;
        if (currentDefinition != null && currentDefinition.isPendingExternalAsset()) {
            loadGuard.markWaitingForAsset(currentDefinition);
        } else {
            loadGuard.reset();
        }
    }

    public boolean canStartSelectedMap(int mapId) {
        if (mapId == GameScreenState.MAP_OPEN_WORLD || mapId == GameScreenState.MAP_SECOND_NEW) return false;
        MapDefinition definition = MapRegistry.definitionFor(mapId);
        return definition != null && !definition.isPendingExternalAsset() && definition.selectableNow;
    }

    public boolean isCurrentExternalGlb() {
        return currentDefinition != null && currentDefinition.externalGlb && currentDefinition.selectableNow;
    }

    public String getMapLoadStatus() {
        if (loadGuard.getState() != LargeMapLoadGuard.STATE_IDLE) return loadGuard.getStatus();
        return currentDefinition == null ? "" : currentDefinition.displayName + " hazır";
    }

    public void render(PrimitiveRenderer renderer, float[] viewProjection, RenderStats stats) {
        if (currentMap != null) {
            currentMap.render(renderer, viewProjection, stats);
        }
    }

    private MapDefinition definitionFromMapName(String name) {
        if ("BUYUK_SEHIR".equals(name)) return MapRegistry.definitionFor(GameScreenState.MAP_CITY);
        if ("OTOYOL".equals(name)) return MapRegistry.definitionFor(GameScreenState.MAP_HIGHWAY);
        if ("DRIFT_PARK".equals(name)) return MapRegistry.definitionFor(GameScreenState.MAP_DRIFT_PARK);
        return MapRegistry.definitionFor(GameScreenState.MAP_OPEN_FIELD);
    }
}
