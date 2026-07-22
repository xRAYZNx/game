package com.arabaoyunu.graphics;

import com.arabaoyunu.util.GraphicsQuality;

/**
 * ArabaOyunu grafik profil yoneticisi.
 *
 * Dort profil sunar:
 * - AKICI   : dusuk render olcegi, efektler kapali, yuksek kare hizi hedefi.
 * - DENGELI : orta render olcegi, temel efektler acik.
 * - HD      : tam render olcegi, golgeler + efektler acik.
 * - ULTRA   : tam render olcegi, tum efektler + genisletilmis cizim mesafesi.
 *
 * Mevcut {@link GraphicsQuality} enumu ile geriye donuk uyumludur; eski
 * kalite sabitleri (0..3) bu profillere birebir eslenir. Hicbir mevcut
 * davranis silinmez, sadece genisletilir.
 *
 * Ayrica durbun (scope) render optimizasyon ayarlarini da tasir:
 * - Scope render-target cozunurluk carpani (FPS dususunu engeller).
 * - FOV gecisinin kare hizindan bagimsiz yumusatilmasi icin sondurma katsayisi.
 */
public final class GraphicsProfileManager {

    public static final int PROFILE_AKICI = 0;
    public static final int PROFILE_DENGELI = 1;
    public static final int PROFILE_HD = 2;
    public static final int PROFILE_ULTRA = 3;

    /** Profil tanimi; tum alanlar degistirilemez (immutable). */
    public static final class Profile {
        public final int id;
        public final String label;
        public final float renderScale;
        public final boolean shadowsEnabled;
        public final boolean effectsEnabled;
        public final boolean particlesEnabled;
        public final int targetFps;
        public final float drawDistanceScale;
        public final int textureDetail;       // 0 dusuk, 1 orta, 2 yuksek
        public final float scopeRenderScale;  // durbun render-target olcegi
        public final boolean scopeOverlayCached; // reticle bitmap onbellek

        Profile(int id, String label, float renderScale, boolean shadowsEnabled,
                boolean effectsEnabled, boolean particlesEnabled, int targetFps,
                float drawDistanceScale, int textureDetail,
                float scopeRenderScale, boolean scopeOverlayCached) {
            this.id = id;
            this.label = label;
            this.renderScale = renderScale;
            this.shadowsEnabled = shadowsEnabled;
            this.effectsEnabled = effectsEnabled;
            this.particlesEnabled = particlesEnabled;
            this.targetFps = targetFps;
            this.drawDistanceScale = drawDistanceScale;
            this.textureDetail = textureDetail;
            this.scopeRenderScale = scopeRenderScale;
            this.scopeOverlayCached = scopeOverlayCached;
        }
    }

    private static final Profile[] PROFILES = new Profile[] {
            new Profile(PROFILE_AKICI, "AKICI",
                    0.65f, false, false, false, 60,
                    0.70f, 0, 0.50f, true),
            new Profile(PROFILE_DENGELI, "DENGELI",
                    0.85f, false, true, true, 60,
                    0.85f, 1, 0.65f, true),
            new Profile(PROFILE_HD, "HD",
                    1.00f, true, true, true, 60,
                    1.00f, 2, 0.80f, true),
            new Profile(PROFILE_ULTRA, "ULTRA",
                    1.00f, true, true, true, 60,
                    1.25f, 2, 1.00f, true)
    };

    private int activeProfileId;

    public GraphicsProfileManager(int legacyQuality) {
        this.activeProfileId = clampProfile(fromLegacyQuality(legacyQuality));
    }

    /** Eski TouchControlsView kalite sabitini (0..3) profil kimligine cevirir. */
    public static int fromLegacyQuality(int legacyQuality) {
        switch (legacyQuality) {
            case 0: return PROFILE_AKICI;
            case 1: return PROFILE_DENGELI;
            case 2: return PROFILE_HD;
            case 3: return PROFILE_ULTRA;
            default: return PROFILE_HD;
        }
    }

    /** Mevcut GraphicsQuality enum degerini profil kimligine cevirir. */
    public static int fromGraphicsQuality(GraphicsQuality quality) {
        if (quality == null) return PROFILE_HD;
        switch (quality) {
            case LOW: return PROFILE_AKICI;
            case MEDIUM: return PROFILE_DENGELI;
            case HIGH: return PROFILE_HD;
            case ULTRA: return PROFILE_ULTRA;
            default: return PROFILE_HD;
        }
    }

    /** Profil kimligini eski GraphicsQuality enum degerine cevirir. */
    public static GraphicsQuality toGraphicsQuality(int profileId) {
        switch (clampProfile(profileId)) {
            case PROFILE_AKICI: return GraphicsQuality.LOW;
            case PROFILE_DENGELI: return GraphicsQuality.MEDIUM;
            case PROFILE_HD: return GraphicsQuality.HIGH;
            case PROFILE_ULTRA: return GraphicsQuality.ULTRA;
            default: return GraphicsQuality.HIGH;
        }
    }

    public static Profile profile(int profileId) {
        return PROFILES[clampProfile(profileId)];
    }

    public static int profileCount() {
        return PROFILES.length;
    }

    public static String label(int profileId) {
        return PROFILES[clampProfile(profileId)].label;
    }

    public synchronized void setActiveProfile(int profileId) {
        this.activeProfileId = clampProfile(profileId);
    }

    public synchronized int getActiveProfileId() {
        return activeProfileId;
    }

    public synchronized Profile getActiveProfile() {
        return PROFILES[activeProfileId];
    }

    /**
     * Durbun acikken uygulanacak etkin render olcegini dondurur.
     * Scope render-target cozunurlugu dusurulerek FOV gecislerindeki
     * FPS dususu engellenir; ana gorus olcegi korunur.
     */
    public float effectiveScopeRenderScale() {
        Profile p = getActiveProfile();
        return p.renderScale * p.scopeRenderScale;
    }

    /** Profilin dokunmatik HUD kalite etiketi ile uyumlu eski indeksini verir. */
    public int legacyQualityIndex() {
        return activeProfileId;
    }

    private static int clampProfile(int profileId) {
        if (profileId < PROFILE_AKICI) return PROFILE_AKICI;
        if (profileId > PROFILE_ULTRA) return PROFILE_ULTRA;
        return profileId;
    }
}
