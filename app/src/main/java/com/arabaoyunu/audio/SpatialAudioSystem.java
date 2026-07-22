package com.arabaoyunu.audio;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;

import com.arabaoyunu.util.GameLog;

/**
 * 3D konumsal ses (spatial audio) + raycast ile zemin tespiti.
 *
 * Zemin tespiti:
 * - {@link GroundProvider} arayuzu, dunya/harita katmaninin sagladigi bir
 *   raycast fonksiyonudur: oyuncunun (x, z) konumundan asagi inen isin
 *   hangi malzemeye carptigini dondurur (CIM, BETON, METAL).
 * - Her adimda raycast yapilir ve malzemeye uygun adim sesi secilir.
 *
 * 3D konumlama:
 * - Dinleyici (kamera) konum ve yonune gore ses kaynagi pan/zayiflama
 *   hesabi yapilir: sag/sol kulaklik dengesi ve mesafe zayiflamasi.
 *
 * Ses uretimi:
 * - Dis ses dosyasi gerektirmez; adim sesleri prosedurel PCM olarak
 *   malzemeye ozel uretilir (cim: yumusak dusuk gecisli girulti,
 *   beton: keskin tik, metal: rezonansli cinlama). AudioTrack ile calinir.
 */
public final class SpatialAudioSystem {

    public static final int GROUND_CIM = 0;
    public static final int GROUND_BETON = 1;
    public static final int GROUND_METAL = 2;

    private static final int SAMPLE_RATE = 22050;
    private static final float MAX_HEARING_DISTANCE = 42f; // metre
    private static final String TAG = "SpatialAudio";

    /**
     * Dunya katmaninin sagladigi zemin raycast'i.
     * (x, z) konumundan asagi inen isinin carptigi malzemeyi dondurur.
     */
    public interface GroundProvider {
        int groundMaterialAt(float x, float z);
    }

    /** Dinleyici (kamera) durumu. */
    public static final class ListenerPose {
        public float x;
        public float z;
        public float yawRadians; // baktigi yon (0 = -Z ilerisi)

        public void set(float x, float z, float yawRadians) {
            this.x = x;
            this.z = z;
            this.yawRadians = yawRadians;
        }
    }

    private final ListenerPose listener = new ListenerPose();
    private GroundProvider groundProvider;
    private volatile boolean enabled = true;

    // Prosedurel PCM onbellekleri (bir kez uretilir).
    private short[] pcmCim;
    private short[] pcmBeton;
    private short[] pcmMetal;

    private long noiseSeed = 0x9E3779B97F4A7C15L;

    public SpatialAudioSystem() {
        generateFootstepSamples();
    }

    public void setGroundProvider(GroundProvider provider) {
        this.groundProvider = provider;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /** Dinleyici konum/yon guncellemesi (her kare cagrilabilir). */
    public void updateListener(float x, float z, float yawRadians) {
        listener.set(x, z, yawRadians);
    }

    /**
     * Oyuncunun kendi adimi: raycast ile zemini tespit eder, uygun sesi
     * merkezde (pan 0, tam ses) calar.
     */
    public void playLocalFootstep(float playerX, float playerZ) {
        int material = GROUND_BETON;
        GroundProvider provider = groundProvider;
        if (provider != null) {
            material = provider.groundMaterialAt(playerX, playerZ);
        }
        playFootstepAt(material, playerX, playerZ);
    }

    /**
     * Uzaktaki bir kaynaktan gelen adim sesi: raycast zemin malzemesini
     * verir, 3D pan + mesafe zayiflamasi ile calar.
     */
    public void playFootstepAt(int material, float sourceX, float sourceZ) {
        if (!enabled) return;
        short[] pcm = pcmForMaterial(material);
        if (pcm == null) return;

        float dx = sourceX - listener.x;
        float dz = sourceZ - listener.z;
        float distance = (float) Math.sqrt(dx * dx + dz * dz);
        if (distance > MAX_HEARING_DISTANCE) return;

        // Mesafe zayiflamasi: 1/d, 1 metreden kisa mesafe kirpilir.
        float attenuation = 1f / Math.max(1f, distance);
        // Pan: kaynagin dinleyicinin sag vektorune izdusumu.
        float rightX = (float) Math.cos(listener.yawRadians);
        float rightZ = (float) Math.sin(listener.yawRadians);
        float pan = 0f;
        if (distance > 0.001f) {
            pan = (dx * rightX + dz * rightZ) / distance; // -1 sol .. +1 sag
        }
        playPcm(pcm, attenuation, pan);
    }

    /** Malzeme icin PCM tamponu. */
    private short[] pcmForMaterial(int material) {
        switch (material) {
            case GROUND_CIM: return pcmCim;
            case GROUND_METAL: return pcmMetal;
            case GROUND_BETON:
            default: return pcmBeton;
        }
    }

    /** Stereo PCM'i AudioTrack uzerinden calar (STATIK mod, bloklamaz). */
    private void playPcm(short[] mono, float volume, float pan) {
        try {
            int frames = mono.length;
            short[] stereo = new short[frames * 2];
            float clampedPan = pan < -1f ? -1f : (pan > 1f ? 1f : pan);
            // Esit guc panlama (constant-power panning).
            double angle = (clampedPan + 1.0) * Math.PI / 4.0;
            float leftGain = (float) Math.cos(angle);
            float rightGain = (float) Math.sin(angle);
            float clampedVolume = volume < 0f ? 0f : (volume > 1f ? 1f : volume);
            for (int i = 0; i < frames; i++) {
                short s = mono[i];
                stereo[i * 2] = (short) (s * leftGain * clampedVolume);
                stereo[i * 2 + 1] = (short) (s * rightGain * clampedVolume);
            }

            int bufferSize = stereo.length * 2;
            AudioTrack track;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                track = new AudioTrack.Builder()
                        .setAudioAttributes(new AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_GAME)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build())
                        .setAudioFormat(new AudioFormat.Builder()
                                .setSampleRate(SAMPLE_RATE)
                                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                                .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                                .build())
                        .setBufferSizeInBytes(bufferSize)
                        .setTransferMode(AudioTrack.MODE_STATIC)
                        .build();
            } else {
                track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize, AudioTrack.MODE_STATIC);
            }
            int written = track.write(stereo, 0, stereo.length);
            if (written != stereo.length) {
                track.release();
                return;
            }
            final AudioTrack finalTrack = track;
            track.setNotificationMarkerPosition(frames);
            track.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
                @Override
                public void onMarkerReached(AudioTrack t) {
                    finalTrack.release();
                }

                @Override
                public void onPeriodicNotification(AudioTrack t) {
                    // Periyodik bildirim kullanilmiyor; marker kaynak serbest birakir.
                }
            });
            track.play();
        } catch (Throwable t) {
            GameLog.e(TAG, "playPcm hata", t);
        }
    }

    // ------------------------------------------------------------------
    // Prosedurel adim sesi uretimi
    // ------------------------------------------------------------------

    private void generateFootstepSamples() {
        pcmCim = generateGrassStep();
        pcmBeton = generateConcreteStep();
        pcmMetal = generateMetalStep();
    }

    private float nextNoise() {
        noiseSeed ^= noiseSeed << 13;
        noiseSeed ^= noiseSeed >>> 7;
        noiseSeed ^= noiseSeed << 17;
        return ((noiseSeed & 0xFFFF) / 32768f) - 1f;
    }

    /** Cim: yumusak, dusuk frekansli girulti (lowpass + uzun decay). */
    private short[] generateGrassStep() {
        int len = (int) (SAMPLE_RATE * 0.16f);
        short[] out = new short[len];
        float lp = 0f;
        for (int i = 0; i < len; i++) {
            float t = i / (float) SAMPLE_RATE;
            float env = (float) Math.exp(-t * 26f);
            float n = nextNoise();
            lp += 0.18f * (n - lp); // basit tek kutuplu lowpass
            float sample = lp * env * 2.4f;
            out[i] = (short) (clampUnit(sample) * 32767f);
        }
        return out;
    }

    /** Beton: kisa, keskin tik (genis bant girulti + hizli decay). */
    private short[] generateConcreteStep() {
        int len = (int) (SAMPLE_RATE * 0.09f);
        short[] out = new short[len];
        float hpPrev = 0f;
        for (int i = 0; i < len; i++) {
            float t = i / (float) SAMPLE_RATE;
            float env = (float) Math.exp(-t * 60f);
            float n = nextNoise();
            float hp = n - hpPrev; // basit highpass: tik keskinligi
            hpPrev = n;
            float body = (n * 0.5f + hp * 1.6f) * env;
            out[i] = (short) (clampUnit(body) * 32767f);
        }
        return out;
    }

    /** Metal: rezonansli cinlama (birkac sinus kipi + orta decay). */
    private short[] generateMetalStep() {
        int len = (int) (SAMPLE_RATE * 0.28f);
        short[] out = new short[len];
        for (int i = 0; i < len; i++) {
            float t = i / (float) SAMPLE_RATE;
            float env = (float) Math.exp(-t * 14f);
            float attack = t < 0.006f ? (t / 0.006f) : 1f;
            double ring = Math.sin(2.0 * Math.PI * 620.0 * t) * 0.45
                    + Math.sin(2.0 * Math.PI * 932.0 * t) * 0.30
                    + Math.sin(2.0 * Math.PI * 1480.0 * t) * 0.18;
            float click = nextNoise() * (float) Math.exp(-t * 90f) * 0.5f;
            float sample = (float) ring * env * attack * 0.8f + click;
            out[i] = (short) (clampUnit(sample) * 32767f);
        }
        return out;
    }

    private static float clampUnit(float v) {
        if (v > 1f) return 1f;
        if (v < -1f) return -1f;
        return v;
    }

    /** Malzeme etiketi (HUD/debug icin). */
    public static String groundLabel(int material) {
        switch (material) {
            case GROUND_CIM: return "CIM";
            case GROUND_METAL: return "METAL";
            case GROUND_BETON:
            default: return "BETON";
        }
    }
}
