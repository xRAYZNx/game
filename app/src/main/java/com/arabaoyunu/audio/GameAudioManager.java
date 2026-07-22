package com.arabaoyunu.audio;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.SoundPool;
import android.media.ToneGenerator;
import android.os.Build;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.util.GameLog;

import java.util.HashMap;
import java.util.Map;

/**
 * ArabaOyunu_33: Gerçek WAV ses dosyaları + kaliteli fallback ses sistemi.
 *
 * Yeni:
 * - assets/audio/*.wav dosyaları SoundPool ile yüklenir.
 * - UI / geri / geri sayım / çarpışma / siren / nitro / checkpoint / reward sesleri WAV oynatır.
 * - Motor sesi için WAV loop + eski procedural AudioTrack fallback birlikte güvenli çalışır.
 *
 * Fallback:
 * - WAV yüklenemezse ToneGenerator ve EngineSynth eski sürüm davranışını korur.
 */
public final class GameAudioManager {

    private static final String TAG = "GameAudio";
    private static final int SAMPLE_RATE = 22050;

    private static final String S_UI_CLICK = "ui_click";
    private static final String S_UI_BACK = "ui_back";
    private static final String S_COUNTDOWN = "countdown_beep";
    private static final String S_ENGINE_LOOP = "engine_loop";
    private static final String S_ENGINE_REV = "engine_rev";
    private static final String S_NITRO = "nitro_burst";
    private static final String S_CRASH_HEAVY = "crash_heavy";
    private static final String S_CRASH_LIGHT = "crash_light";
    private static final String S_SIREN = "police_siren";
    private static final String S_CHECKPOINT = "checkpoint";
    private static final String S_REWARD = "reward";

    private final Context context;
    private final EngineSynth engineSynth = new EngineSynth();
    private final SampleBank sampleBank = new SampleBank();

    private ToneGenerator effectTone;
    private boolean enabled = true;
    private boolean started;
    private boolean driving;

    private long lastBrakeMs;
    private long lastDriftMs;
    private long lastCollisionMs;
    private long lastMenuMs;
    private long lastBackMs;
    private long lastCountdownMs = -1L;
    private long lastSirenMs;
    private long lastNitroMs;
    private long lastRewardMs;
    private long lastCheckpointMs;

    private int engineLoopStream;
    private boolean engineLoopPlaying;

    private float masterVolume = 0.86f;
    private float engineVolume = 0.62f;
    private float effectsVolume = 0.82f;

    public GameAudioManager(Context context) {
        this.context = context == null ? null : context.getApplicationContext();
    }

    public synchronized void start() {
        if (started) return;
        started = true;
        try {
            effectTone = new ToneGenerator(AudioManager.STREAM_MUSIC, 68);
        } catch (Throwable t) {
            effectTone = null;
            GameLog.e(TAG, "ToneGenerator baslatilamadi", t);
        }

        sampleBank.load(context);
        engineSynth.start();
        GameLog.i(TAG, "ArabaOyunu_33 WAV + procedural fallback ses sistemi baslatildi. samples=" + sampleBank.loadedCount());
    }

    public synchronized void stop() {
        started = false;
        driving = false;
        stopEngineLoop();
        engineSynth.stop();
        sampleBank.release();
        if (effectTone != null) {
            try {
                effectTone.release();
            } catch (Throwable ignored) {}
            effectTone = null;
        }
    }

    public synchronized void pause() {
        driving = false;
        stopEngineLoop();
        engineSynth.setActive(false);
    }

    public synchronized void resume() {
        if (!started) start();
    }

    public synchronized void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            stopEngineLoop();
            engineSynth.setActive(false);
        }
    }

    public synchronized boolean isEnabled() {
        return enabled;
    }

    public synchronized void setDriving(boolean driving) {
        this.driving = driving;
        if (!enabled || !driving) {
            stopEngineLoop();
            engineSynth.setActive(false);
        }
    }

    public void updateDrivingAudio(VehicleController car, InputState input, boolean timeTrialActive, float countdown) {
        if (!started || !enabled || car == null) return;

        boolean nowDriving = driving;
        float throttle = input == null ? 0f : clamp(input.throttle, 0f, 1f);
        float brake = input == null ? 0f : clamp(input.brake, 0f, 1f);
        float nitro = input == null ? 0f : clamp(input.nitro, 0f, 1f);
        float speed01 = clamp(car.getSpeedKmh() / 230f, 0f, 1f);
        float drift01 = clamp(car.getDriftBlend(), 0f, 1f);
        float rpm = clamp(0.20f + throttle * 0.50f + speed01 * 0.48f + drift01 * 0.12f, 0.16f, 1.22f);

        updateEngineLayers(nowDriving, rpm, throttle, speed01);

        long now = System.currentTimeMillis();

        if (brake > 0.45f && car.getSpeedKmh() > 20f && now - lastBrakeMs > 520L) {
            if (!sampleBank.play(S_CRASH_LIGHT, effectsVolume * 0.16f, 0.92f, 0)) {
                playTone(ToneGenerator.TONE_PROP_BEEP, 42);
            }
            lastBrakeMs = now;
        }

        if (car.getDriftBlend() > 0.55f && car.getSpeedKmh() > 28f && now - lastDriftMs > 360L) {
            if (!sampleBank.play(S_ENGINE_REV, effectsVolume * 0.25f, 1.15f + drift01 * 0.15f, 0)) {
                playTone(ToneGenerator.TONE_PROP_BEEP2, 54);
            }
            lastDriftMs = now;
        }

        if (nitro > 0.55f && now - lastNitroMs > 850L) {
            sampleBank.play(S_NITRO, effectsVolume * 0.44f, 1.00f + speed01 * 0.16f, 0);
            lastNitroMs = now;
        }

        if (car.getImpactFeedback() > 0.72f && now - lastCollisionMs > 650L) {
            if (!sampleBank.play(car.getLastImpactDamage() > 0.075f ? S_CRASH_HEAVY : S_CRASH_LIGHT,
                    effectsVolume * 0.88f, 0.94f + speed01 * 0.10f, 0)) {
                playTone(ToneGenerator.TONE_PROP_NACK, 115);
            }
            lastCollisionMs = now;
        }

        if (timeTrialActive && countdown > 0f) {
            int second = (int) Math.ceil(countdown);
            long key = second;
            if (key != lastCountdownMs) {
                playCountdown(second);
                lastCountdownMs = key;
            }
        } else if (!timeTrialActive) {
            lastCountdownMs = -1L;
        }
    }

    private synchronized void updateEngineLayers(boolean nowDriving, float rpm, float throttle, float speed01) {
        float wavVolume = masterVolume * engineVolume * (0.18f + throttle * 0.20f + speed01 * 0.34f);
        float synthVolume = masterVolume * engineVolume * (sampleBank.isLoaded(S_ENGINE_LOOP) ? 0.18f : 0.48f)
                * (0.22f + throttle * 0.26f + speed01 * 0.32f);
        engineSynth.setActive(enabled && nowDriving);
        engineSynth.setTarget(rpm, synthVolume);

        if (!nowDriving || !sampleBank.isReady()) {
            stopEngineLoop();
            return;
        }

        if (!sampleBank.isLoaded(S_ENGINE_LOOP)) return;

        float rate = clamp(0.72f + rpm * 0.55f, 0.65f, 1.55f);
        if (!engineLoopPlaying || engineLoopStream == 0) {
            engineLoopStream = sampleBank.playLoop(S_ENGINE_LOOP, wavVolume, rate);
            engineLoopPlaying = engineLoopStream != 0;
        } else {
            sampleBank.setStream(engineLoopStream, wavVolume, rate);
        }
    }

    private synchronized void stopEngineLoop() {
        if (engineLoopStream != 0) {
            sampleBank.stop(engineLoopStream);
            engineLoopStream = 0;
        }
        engineLoopPlaying = false;
    }

    public synchronized void playMenuClick() {
        long now = System.currentTimeMillis();
        if (now - lastMenuMs < 62L) return;
        if (!sampleBank.play(S_UI_CLICK, effectsVolume * 0.62f, 1.0f, 0)) {
            playTone(ToneGenerator.TONE_PROP_ACK, 36);
        }
        lastMenuMs = now;
    }

    public synchronized void playBack() {
        long now = System.currentTimeMillis();
        if (now - lastBackMs < 92L) return;
        if (!sampleBank.play(S_UI_BACK, effectsVolume * 0.52f, 1.0f, 0)) {
            playTone(ToneGenerator.TONE_PROP_BEEP2, 44);
        }
        lastBackMs = now;
    }

    public synchronized void playRespawn() {
        if (!sampleBank.play(S_ENGINE_REV, effectsVolume * 0.62f, 0.82f, 0)) {
            playTone(ToneGenerator.TONE_PROP_ACK, 120);
        }
    }

    public synchronized void playCountdown(int second) {
        if (second <= 0) {
            if (!sampleBank.play(S_REWARD, effectsVolume * 0.72f, 1.05f, 0)) {
                playTone(ToneGenerator.TONE_PROP_ACK, 180);
            }
        } else {
            if (!sampleBank.play(S_COUNTDOWN, effectsVolume * 0.76f, 1.0f + (3 - Math.min(3, second)) * 0.08f, 0)) {
                playTone(ToneGenerator.TONE_PROP_PROMPT, 90);
            }
        }
    }

    public synchronized void playSirenTick() {
        long now = System.currentTimeMillis();
        if (now - lastSirenMs < 840L) return;
        if (!sampleBank.play(S_SIREN, effectsVolume * 0.68f, 1.0f, 0)) {
            playTone(ToneGenerator.TONE_PROP_PROMPT, 150);
        }
        lastSirenMs = now;
    }

    public synchronized void playCheckpoint() {
        long now = System.currentTimeMillis();
        if (now - lastCheckpointMs < 180L) return;
        sampleBank.play(S_CHECKPOINT, effectsVolume * 0.68f, 1.0f, 0);
        lastCheckpointMs = now;
    }

    public synchronized void playReward() {
        long now = System.currentTimeMillis();
        if (now - lastRewardMs < 450L) return;
        sampleBank.play(S_REWARD, effectsVolume * 0.72f, 1.0f, 0);
        lastRewardMs = now;
    }

    public synchronized void playCareerStart() {
        if (!sampleBank.play(S_REWARD, effectsVolume * 0.86f, 1.08f, 0)) {
            playTone(ToneGenerator.TONE_PROP_ACK, 210);
        }
    }

    public synchronized void playSelect() {
        if (!sampleBank.play(S_UI_CLICK, effectsVolume * 0.66f, 1.12f, 0)) {
            playTone(ToneGenerator.TONE_PROP_ACK, 42);
        }
    }

    public synchronized void playLocked() {
        if (!sampleBank.play(S_UI_BACK, effectsVolume * 0.56f, 0.82f, 0)) {
            playTone(ToneGenerator.TONE_PROP_NACK, 72);
        }
    }


    public synchronized void playNitroFeedback() {
        long now = System.currentTimeMillis();
        if (now - lastNitroMs < 320L) return;
        if (!sampleBank.play(S_NITRO, effectsVolume * 0.78f, 1.08f, 0)) {
            playTone(ToneGenerator.TONE_PROP_BEEP, 90);
        }
        lastNitroMs = now;
    }

    public synchronized void playImpactFeedback(boolean heavy) {
        long now = System.currentTimeMillis();
        if (now - lastCollisionMs < 260L) return;
        if (!sampleBank.play(heavy ? S_CRASH_HEAVY : S_CRASH_LIGHT, effectsVolume * (heavy ? 0.92f : 0.62f), heavy ? 0.94f : 1.05f, 0)) {
            playTone(heavy ? ToneGenerator.TONE_PROP_NACK : ToneGenerator.TONE_PROP_BEEP2, heavy ? 130 : 70);
        }
        lastCollisionMs = now;
    }

    public synchronized void playDriftSkidTick() {
        long now = System.currentTimeMillis();
        if (now - lastDriftMs < 220L) return;
        if (!sampleBank.play(S_ENGINE_REV, effectsVolume * 0.34f, 1.26f, 0)) {
            playTone(ToneGenerator.TONE_PROP_BEEP2, 48);
        }
        lastDriftMs = now;
    }

    public synchronized void playPoliceWarning(int wantedLevel) {
        long now = System.currentTimeMillis();
        if (now - lastSirenMs < 540L) return;
        float pitch = 0.94f + Math.max(0, wantedLevel) * 0.05f;
        if (!sampleBank.play(S_SIREN, effectsVolume * 0.74f, pitch, 0)) {
            playTone(ToneGenerator.TONE_PROP_PROMPT, 132);
        }
        lastSirenMs = now;
    }

    private synchronized void playTone(int tone, int durationMs) {
        if (!enabled || effectTone == null) return;
        try {
            effectTone.startTone(tone, Math.max(24, durationMs));
        } catch (Throwable ignored) {}
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    private static final class SampleBank {
        private final Map<String, Integer> ids = new HashMap<String, Integer>();
        private SoundPool pool;
        private boolean ready;

        void load(Context context) {
            release();
            if (context == null) return;
            try {
                if (Build.VERSION.SDK_INT >= 21) {
                    AudioAttributes attrs = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
                    pool = new SoundPool.Builder()
                            .setAudioAttributes(attrs)
                            .setMaxStreams(9)
                            .build();
                } else {
                    pool = new SoundPool(9, AudioManager.STREAM_MUSIC, 0);
                }
                loadOne(context, S_UI_CLICK, "audio/ui_click.wav");
                loadOne(context, S_UI_BACK, "audio/ui_back.wav");
                loadOne(context, S_COUNTDOWN, "audio/countdown_beep.wav");
                loadOne(context, S_ENGINE_LOOP, "audio/engine_loop.wav");
                loadOne(context, S_ENGINE_REV, "audio/engine_rev.wav");
                loadOne(context, S_NITRO, "audio/nitro_burst.wav");
                loadOne(context, S_CRASH_HEAVY, "audio/crash_heavy.wav");
                loadOne(context, S_CRASH_LIGHT, "audio/crash_light.wav");
                loadOne(context, S_SIREN, "audio/police_siren.wav");
                loadOne(context, S_CHECKPOINT, "audio/checkpoint.wav");
                loadOne(context, S_REWARD, "audio/reward.wav");
                ready = ids.size() > 0;
            } catch (Throwable t) {
                ready = false;
                release();
                GameLog.e(TAG, "WAV SampleBank yuklenemedi", t);
            }
        }

        private void loadOne(Context context, String key, String assetPath) {
            try {
                AssetFileDescriptor afd = context.getAssets().openFd(assetPath);
                int id = pool.load(afd, 1);
                ids.put(key, id);
                try { afd.close(); } catch (Throwable ignored) {}
            } catch (Throwable t) {
                GameLog.e(TAG, "Ses dosyasi yuklenemedi: " + assetPath, t);
            }
        }

        boolean isReady() { return ready && pool != null; }

        boolean isLoaded(String key) {
            Integer id = ids.get(key);
            return isReady() && id != null && id > 0;
        }

        int loadedCount() { return ids.size(); }

        boolean play(String key, float volume, float rate, int loop) {
            if (!isLoaded(key)) return false;
            Integer id = ids.get(key);
            float v = clamp(volume, 0f, 1f);
            float r = clamp(rate, 0.5f, 1.9f);
            try {
                pool.play(id, v, v, 1, loop, r);
                return true;
            } catch (Throwable ignored) {
                return false;
            }
        }

        int playLoop(String key, float volume, float rate) {
            if (!isLoaded(key)) return 0;
            Integer id = ids.get(key);
            float v = clamp(volume, 0f, 1f);
            float r = clamp(rate, 0.5f, 1.9f);
            try {
                return pool.play(id, v, v, 1, -1, r);
            } catch (Throwable ignored) {
                return 0;
            }
        }

        void setStream(int streamId, float volume, float rate) {
            if (!isReady() || streamId == 0) return;
            float v = clamp(volume, 0f, 1f);
            float r = clamp(rate, 0.5f, 1.9f);
            try {
                pool.setVolume(streamId, v, v);
                pool.setRate(streamId, r);
            } catch (Throwable ignored) {}
        }

        void stop(int streamId) {
            if (!isReady() || streamId == 0) return;
            try {
                pool.stop(streamId);
            } catch (Throwable ignored) {}
        }

        void release() {
            if (pool != null) {
                try {
                    pool.release();
                } catch (Throwable ignored) {}
                pool = null;
            }
            ids.clear();
            ready = false;
        }

        private static float clamp(float v, float min, float max) {
            return Math.max(min, Math.min(max, v));
        }
    }

    private static final class EngineSynth implements Runnable {
        private AudioTrack track;
        private Thread thread;
        private volatile boolean running;
        private volatile boolean active;
        private volatile float targetRpm = 0.20f;
        private volatile float targetVolume = 0f;

        private float currentRpm = 0.20f;
        private float currentVolume;
        private float phase;
        private float pulsePhase;

        void start() {
            if (running) return;
            running = true;
            int min = AudioTrack.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            if (min < 2048) min = 2048;
            try {
                track = new AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        min * 2,
                        AudioTrack.MODE_STREAM);
                track.play();
                thread = new Thread(this, "ArabaOyunuEngineSynth");
                thread.setDaemon(true);
                thread.start();
            } catch (Throwable t) {
                running = false;
                GameLog.e(TAG, "EngineSynth baslatilamadi", t);
            }
        }

        void stop() {
            running = false;
            active = false;
            if (thread != null) {
                try {
                    thread.join(180L);
                } catch (Throwable ignored) {}
                thread = null;
            }
            if (track != null) {
                try {
                    track.stop();
                } catch (Throwable ignored) {}
                try {
                    track.release();
                } catch (Throwable ignored) {}
                track = null;
            }
        }

        void setActive(boolean active) {
            this.active = active;
        }

        void setTarget(float rpm, float volume) {
            targetRpm = clamp(rpm, 0.12f, 1.28f);
            targetVolume = clamp(volume, 0f, 0.92f);
        }

        @Override
        public void run() {
            short[] buffer = new short[512];
            while (running && track != null) {
                float wantedVol = active ? targetVolume : 0f;
                float wantedRpm = targetRpm;

                for (int i = 0; i < buffer.length; i++) {
                    currentRpm += (wantedRpm - currentRpm) * 0.0028f;
                    currentVolume += (wantedVol - currentVolume) * 0.0038f;

                    float freq = 42f + currentRpm * 165f;
                    phase += (float) (2.0 * Math.PI) * freq / SAMPLE_RATE;
                    pulsePhase += (float) (2.0 * Math.PI) * (freq * 0.48f) / SAMPLE_RATE;
                    if (phase > Math.PI * 2f) phase -= (float) (Math.PI * 2f);
                    if (pulsePhase > Math.PI * 2f) pulsePhase -= (float) (Math.PI * 2f);

                    float base = (float) Math.sin(phase);
                    float harmonic = (float) Math.sin(phase * 2.0f) * 0.34f;
                    float pulse = (float) Math.sin(pulsePhase) * 0.22f;
                    float sample = (base * 0.62f + harmonic + pulse) * currentVolume;
                    sample = clamp(sample, -0.95f, 0.95f);
                    buffer[i] = (short) (sample * 32767);
                }

                try {
                    track.write(buffer, 0, buffer.length);
                } catch (Throwable t) {
                    running = false;
                }
            }
        }

        private static float clamp(float v, float min, float max) {
            return Math.max(min, Math.min(max, v));
        }
    }
}
