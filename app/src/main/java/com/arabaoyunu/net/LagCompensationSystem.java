package com.arabaoyunu.net;

import com.arabaoyunu.combat.HitboxSystem;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/**
 * Lag compensation: istemci tahmini (client prediction) + sunucu geri
 * sarma (server rewind) ile hatasiz vurus kaydi (hit registration).
 *
 * Istemci tarafi:
 * - Her input bir sequence numarasi ile sunucuya gonderilir ve kuyrukda
 *   tutulur (pending).
 * - Sunucu onayi (ack) gelince onaylanan sequence ve oncesi kuyruktan
 *   silinir; sonraki inputlar son sunucu durumu uzerine yeniden
 *   uygulanir (reconciliation).
 *
 * Sunucu tarafi:
 * - Her varligin pozisyon gecmisi halka tamponda (ring buffer) tutulur.
 * - Vurus istegi gelince, aticinin RTT'sine gore hedef gecmiste
 *   "shootTime - rtt/2 - interp" anina geri sarilir ve hitbox testi
 *   o anki konumla yapilir. Boylece yuksek ping'li oyuncularin
 *   ekranda gordugu vuruslar dogru kaydedilir.
 *
 * Saf Java'dir; Android bagimliligi yoktur. Thread-guvenlidir.
 */
public final class LagCompensationSystem {

    /** Gecmis tamponu derinligi (ms). Standart 1 saniyelik rewind penceresi. */
    public static final int HISTORY_WINDOW_MS = 1000;
    /** Kare/konum orneklemesinde izin verilen maksimum zaman atlama (ms). */
    private static final long MAX_SNAPSHOT_GAP_MS = 250L;

    /** Tek varlik konum ornegi. */
    public static final class StateSnapshot {
        public final long serverTimeMs;
        public final float x;
        public final float y;
        public final float z;
        public final float heightMeters;

        public StateSnapshot(long serverTimeMs, float x, float y, float z, float heightMeters) {
            this.serverTimeMs = serverTimeMs;
            this.x = x;
            this.y = y;
            this.z = z;
            this.heightMeters = heightMeters;
        }
    }

    /** Istemci input ornegi (prediction icin). */
    public static final class InputCmd {
        public final int sequence;
        public final float moveX;
        public final float moveZ;
        public final float dtSeconds;

        public InputCmd(int sequence, float moveX, float moveZ, float dtSeconds) {
            this.sequence = sequence;
            this.moveX = moveX;
            this.moveZ = moveZ;
            this.dtSeconds = dtSeconds;
        }
    }

    /** Vurus dogrulama sonucu. */
    public static final class HitRegistration {
        public final boolean hit;
        public final int zone;
        public final float rewoundX;
        public final float rewoundY;
        public final float rewoundZ;
        public final long rewindTimeMs;

        HitRegistration(boolean hit, int zone, float x, float y, float z, long rewindTimeMs) {
            this.hit = hit;
            this.zone = zone;
            this.rewoundX = x;
            this.rewoundY = y;
            this.rewoundZ = z;
            this.rewindTimeMs = rewindTimeMs;
        }
    }

    // ---- Istemci tahmini (client prediction) durumu ----
    private final ArrayDeque<InputCmd> pendingInputs = new ArrayDeque<InputCmd>();
    private int nextSequence = 1;
    private int lastAckedSequence;
    private float predictedX;
    private float predictedZ;

    // ---- Sunucu rewind gecmisi ----
    private final Map<Integer, ArrayDeque<StateSnapshot>> historyByEntity =
            new HashMap<Integer, ArrayDeque<StateSnapshot>>();

    // ------------------------------------------------------------------
    // Istemci tahmini
    // ------------------------------------------------------------------

    /** Yeni input uretir, kuyruga ekler ve tahmini konumu ilerletir. */
    public synchronized InputCmd produceInput(float moveX, float moveZ, float dtSeconds, float speedMps) {
        InputCmd cmd = new InputCmd(nextSequence++, moveX, moveZ, dtSeconds);
        pendingInputs.addLast(cmd);
        applyInput(cmd, speedMps);
        return cmd;
    }

    private void applyInput(InputCmd cmd, float speedMps) {
        predictedX += cmd.moveX * speedMps * cmd.dtSeconds;
        predictedZ += cmd.moveZ * speedMps * cmd.dtSeconds;
    }

    /**
     * Sunucudan otoriter konum + ack geldi: onaylanan inputlari dus,
     * kalanlari sunucu konumu uzerine yeniden uygula.
     */
    public synchronized void reconcile(int ackedSequence, float serverX, float serverZ, float speedMps) {
        lastAckedSequence = Math.max(lastAckedSequence, ackedSequence);
        while (!pendingInputs.isEmpty() && pendingInputs.peekFirst().sequence <= ackedSequence) {
            pendingInputs.pollFirst();
        }
        predictedX = serverX;
        predictedZ = serverZ;
        for (InputCmd cmd : pendingInputs) {
            applyInput(cmd, speedMps);
        }
    }

    public synchronized float getPredictedX() {
        return predictedX;
    }

    public synchronized float getPredictedZ() {
        return predictedZ;
    }

    public synchronized int getPendingInputCount() {
        return pendingInputs.size();
    }

    public synchronized int getLastAckedSequence() {
        return lastAckedSequence;
    }

    // ------------------------------------------------------------------
    // Sunucu geri sarma (server rewind)
    // ------------------------------------------------------------------

    /** Sunucu her tick varlik konumlarini buraya kaydeder. */
    public synchronized void recordSnapshot(int entityId, StateSnapshot snapshot) {
        ArrayDeque<StateSnapshot> deque = historyByEntity.get(entityId);
        if (deque == null) {
            deque = new ArrayDeque<StateSnapshot>();
            historyByEntity.put(entityId, deque);
        }
        deque.addLast(snapshot);
        long cutoff = snapshot.serverTimeMs - HISTORY_WINDOW_MS;
        while (!deque.isEmpty() && deque.peekFirst().serverTimeMs < cutoff) {
            deque.pollFirst();
        }
    }

    /**
     * Vurus kaydini geri sarma ile dogrular.
     *
     * @param targetId        hedef varlik
     * @param shotServerTimeMs sunucunun ates istegini aldigi an
     * @param shooterRttMs    aticinin gidis-donus gecikmesi
     * @param interpolationMs istemcinin render interpolasyon gecikmesi (tipik ~2 tick)
     * @param aimX/aimY/aimZ  atis isini uzerindeki en yakin gecis noktasi (istemci raporu)
     * @param hitRadiusMeters hitbox yatay yaricapi
     */
    public synchronized HitRegistration validateShot(int targetId,
                                                     long shotServerTimeMs,
                                                     long shooterRttMs,
                                                     long interpolationMs,
                                                     float aimX, float aimY, float aimZ,
                                                     float hitRadiusMeters) {
        long rewindTime = shotServerTimeMs - shooterRttMs / 2L - interpolationMs;
        ArrayDeque<StateSnapshot> deque = historyByEntity.get(targetId);
        if (deque == null || deque.isEmpty()) {
            return new HitRegistration(false, HitboxSystem.ZONE_BODY, aimX, aimY, aimZ, rewindTime);
        }

        StateSnapshot before = null;
        StateSnapshot after = null;
        for (StateSnapshot s : deque) {
            if (s.serverTimeMs <= rewindTime) {
                before = s;
            } else {
                after = s;
                break;
            }
        }
        if (before == null) before = deque.peekFirst();
        if (after == null) after = deque.peekLast();
        if (before == null || after == null) {
            return new HitRegistration(false, HitboxSystem.ZONE_BODY, aimX, aimY, aimZ, rewindTime);
        }
        // Geri sarma penceresi asiri eskiyse vurusu reddet (anti-exploit).
        if (after.serverTimeMs - before.serverTimeMs > MAX_SNAPSHOT_GAP_MS
                && shotServerTimeMs - after.serverTimeMs > HISTORY_WINDOW_MS) {
            return new HitRegistration(false, HitboxSystem.ZONE_BODY, aimX, aimY, aimZ, rewindTime);
        }

        float span = Math.max(1f, after.serverTimeMs - before.serverTimeMs);
        float t = (rewindTime - before.serverTimeMs) / span;
        if (t < 0f) t = 0f;
        if (t > 1f) t = 1f;
        float rx = before.x + (after.x - before.x) * t;
        float ry = before.y + (after.y - before.y) * t;
        float rz = before.z + (after.z - before.z) * t;
        float height = before.heightMeters + (after.heightMeters - before.heightMeters) * t;

        // Yatay hitbox testi (silindir): geri sarilmis konum - isin gecis noktasi.
        float dx = aimX - rx;
        float dz = aimZ - rz;
        float horizontalDist = (float) Math.sqrt(dx * dx + dz * dz);
        if (horizontalDist > Math.max(0.05f, hitRadiusMeters)) {
            return new HitRegistration(false, HitboxSystem.ZONE_BODY, rx, ry, rz, rewindTime);
        }
        // Dikey test + bolge tespiti.
        float relY = aimY - ry;
        if (relY < 0f || relY > height) {
            return new HitRegistration(false, HitboxSystem.ZONE_BODY, rx, ry, rz, rewindTime);
        }
        int zone = HitboxSystem.zoneFromHeightRatio(relY / Math.max(0.01f, height));
        return new HitRegistration(true, zone, rx, ry, rz, rewindTime);
    }

    /** Belirli varligin gecmis kaydini temizler (disconnect/despawn). */
    public synchronized void clearEntity(int entityId) {
        historyByEntity.remove(entityId);
    }

    /** Tum gecmisi ve tahmin durumunu sifirlar. */
    public synchronized void reset() {
        pendingInputs.clear();
        historyByEntity.clear();
        nextSequence = 1;
        lastAckedSequence = 0;
        predictedX = 0f;
        predictedZ = 0f;
    }
}
