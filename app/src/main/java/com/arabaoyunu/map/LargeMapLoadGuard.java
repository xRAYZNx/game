package com.arabaoyunu.map;

/**
 * ArabaOyunu_61_1: büyük GLB haritalar için fail-safe yükleme durumu.
 * A61_1 haritayı yüklemez; sadece sonraki entegrasyonda donma/çökme olmaması için
 * tek merkezli durum ve hata mesajı altyapısını hazırlar.
 */
public final class LargeMapLoadGuard {
    public static final int STATE_IDLE = 0;
    public static final int STATE_WAITING_FOR_ASSET = 1;
    public static final int STATE_LOADING = 2;
    public static final int STATE_READY = 3;
    public static final int STATE_FAILED = 4;

    private int state = STATE_IDLE;
    private int mapId = -1;
    private String status = "";
    private long startedMs;

    public void reset() {
        state = STATE_IDLE;
        mapId = -1;
        status = "";
        startedMs = 0L;
    }

    public void markWaitingForAsset(MapDefinition definition) {
        mapId = definition == null ? -1 : definition.id;
        state = STATE_WAITING_FOR_ASSET;
        startedMs = System.currentTimeMillis();
        status = definition == null
                ? "Harita dosyası bekleniyor"
                : definition.displayName + " dosyası bekleniyor: " + definition.assetPath;
    }

    public void markLoading(MapDefinition definition) {
        mapId = definition == null ? -1 : definition.id;
        state = STATE_LOADING;
        startedMs = System.currentTimeMillis();
        status = definition == null ? "Harita yükleniyor" : definition.displayName + " yükleniyor";
    }

    public void markReady(MapDefinition definition) {
        mapId = definition == null ? -1 : definition.id;
        state = STATE_READY;
        status = definition == null ? "Harita hazır" : definition.displayName + " hazır";
    }

    public void markFailed(MapDefinition definition, String reason) {
        mapId = definition == null ? -1 : definition.id;
        state = STATE_FAILED;
        status = (definition == null ? "Harita" : definition.displayName)
                + " yüklenemedi"
                + (reason == null || reason.length() == 0 ? "" : ": " + reason);
    }

    public int getState() { return state; }
    public int getMapId() { return mapId; }
    public String getStatus() { return status; }
    public long getStartedMs() { return startedMs; }

    public boolean isBlockingDriveStart() {
        return state == STATE_WAITING_FOR_ASSET || state == STATE_LOADING || state == STATE_FAILED;
    }
}
