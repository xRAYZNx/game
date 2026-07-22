package com.arabaoyunu.combat;

/**
 * Bolgesel hasar (hitbox) ve zirh absorbe sistemi.
 *
 * Bolge carpani:
 * - Kafa  : x2.5
 * - Govde : x1.0
 * - Uzuv  : x0.75
 *
 * Zirh mantigi:
 * - Kask: kafa vuruslarinda hasarin bir kismini emer, dayaniklilik duser.
 * - Yelek: govde vuruslarinda hasarin bir kismini emer, dayaniklilik duser.
 * - Zirh dayanikliligi bittiginde absorbe kalmaz; bolge carpani tam uygulanir.
 *
 * Tum metotlar saf fonksiyon / thread-guvenli tasarlanmistir; ag katmani
 * (LagCompensationSystem) ve yapay zeka ayni hesabi kullanir.
 */
public final class HitboxSystem {

    public static final int ZONE_HEAD = 0;
    public static final int ZONE_BODY = 1;
    public static final int ZONE_LIMB = 2;

    public static final float MULTIPLIER_HEAD = 2.5f;
    public static final float MULTIPLIER_BODY = 1.0f;
    public static final float MULTIPLIER_LIMB = 0.75f;

    /** Kask absorbe orani (kalan hasar kafaya gecer). */
    public static final float HELMET_ABSORB_RATIO = 0.55f;
    /** Yelek absorbe orani. */
    public static final float VEST_ABSORB_RATIO = 0.50f;
    /** Emilen hasarin zirh dayanikliligina yansima katsayisi. */
    public static final float ARMOR_DAMAGE_TO_DURABILITY = 0.65f;

    /** Tek vurusun cozumlenmis sonucu. */
    public static final class DamageResult {
        public final int zone;
        public final float rawDamage;          // bolge carpani uygulanmis ham hasar
        public final float absorbedByArmor;    // zirh tarafindan emilen miktar
        public final float finalDamage;        // cana islenen hasar
        public final float armorDurabilityAfter;
        public final boolean armorBroken;
        public final boolean lethal;

        DamageResult(int zone, float rawDamage, float absorbedByArmor, float finalDamage,
                     float armorDurabilityAfter, boolean armorBroken, boolean lethal) {
            this.zone = zone;
            this.rawDamage = rawDamage;
            this.absorbedByArmor = absorbedByArmor;
            this.finalDamage = finalDamage;
            this.armorDurabilityAfter = armorDurabilityAfter;
            this.armorBroken = armorBroken;
            this.lethal = lethal;
        }
    }

    /** Bolge icin hasar carpani. */
    public static float multiplierForZone(int zone) {
        switch (zone) {
            case ZONE_HEAD: return MULTIPLIER_HEAD;
            case ZONE_LIMB: return MULTIPLIER_LIMB;
            case ZONE_BODY:
            default: return MULTIPLIER_BODY;
        }
    }

    /**
     * Dikey vurus konumundan bolge tespiti.
     * @param hitHeightRatio vurusun karakter yuksekligine orani (0 ayak, 1 tepe)
     */
    public static int zoneFromHeightRatio(float hitHeightRatio) {
        if (hitHeightRatio >= 0.86f) return ZONE_HEAD;
        if (hitHeightRatio >= 0.34f) return ZONE_BODY;
        return ZONE_LIMB;
    }

    /**
     * Hasar cozumu: bolge carpani + zirh absorbe + dayaniklilik dusumu.
     *
     * @param baseDamage        silah taban hasari
     * @param zone              vurulan bolge
     * @param armorDurability   mevcut zirh dayanikliligi (kask veya yelek)
     * @param currentHealth     hedefin mevcut cani
     */
    public static DamageResult computeDamage(float baseDamage, int zone,
                                             float armorDurability, float currentHealth) {
        float raw = Math.max(0f, baseDamage) * multiplierForZone(zone);
        float absorbRatio = 0f;
        if (zone == ZONE_HEAD && armorDurability > 0f) {
            absorbRatio = HELMET_ABSORB_RATIO;
        } else if (zone == ZONE_BODY && armorDurability > 0f) {
            absorbRatio = VEST_ABSORB_RATIO;
        }

        float absorbed = raw * absorbRatio;
        float durabilityLoss = absorbed * ARMOR_DAMAGE_TO_DURABILITY;
        float durabilityAfter = Math.max(0f, armorDurability - durabilityLoss);
        boolean broken = armorDurability > 0f && durabilityAfter <= 0f;
        float finalDamage = raw - absorbed;
        boolean lethal = finalDamage >= Math.max(0f, currentHealth);

        return new DamageResult(zone, raw, absorbed, finalDamage, durabilityAfter, broken, lethal);
    }

    /** Bolge etiketi (killfeed / HUD icin). */
    public static String zoneLabel(int zone) {
        switch (zone) {
            case ZONE_HEAD: return "KAFADAN";
            case ZONE_LIMB: return "UZUV";
            case ZONE_BODY:
            default: return "GOVDE";
        }
    }
}
