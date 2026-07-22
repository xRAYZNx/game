package com.arabaoyunu.map;

import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.render.CarVisualConfig;

/**
 * ArabaOyunu_61_6: Harita GLB entegrasyonu geçici olarak kapatıldı.
 * Açık Dünya ve ikinci harita slotları pasif kalır; sürüş Open Field / dahili haritalarla devam eder.
 */
public final class MapRegistry {
    public static final String OPEN_WORLD_ASSET_PATH = "";
    public static final String SECOND_MAP_ASSET_PATH = "";

    private MapRegistry() {}

    public static int maxMapId() {
        return GameScreenState.MAP_SECOND_NEW;
    }

    public static MapDefinition definitionFor(int id) {
        if (id == GameScreenState.MAP_CITY) {
            return new MapDefinition(
                    id,
                    "BUYUK_SEHIR",
                    "BÜYÜK ŞEHİR",
                    "Dahili primitive şehir test haritası.",
                    "",
                    0,
                    0f,
                    0.42f,
                    -180f,
                    0f,
                    0f,
                    1f,
                    260f,
                    true,
                    false,
                    false,
                    false,
                    true,
                    true,
                    CarVisualConfig.QUALITY_HIGH);
        }
        if (id == GameScreenState.MAP_HIGHWAY) {
            return new MapDefinition(
                    id,
                    "OTOYOL",
                    "OTOYOL",
                    "Dahili primitive uzun yol haritası.",
                    "",
                    0,
                    0f,
                    0.42f,
                    -210f,
                    0f,
                    0f,
                    1f,
                    360f,
                    true,
                    false,
                    false,
                    false,
                    true,
                    true,
                    CarVisualConfig.QUALITY_HIGH);
        }
        if (id == GameScreenState.MAP_DRIFT_PARK) {
            return new MapDefinition(
                    id,
                    "DRIFT_PARK",
                    "DRIFT PARK",
                    "Dahili drift antrenman alanı.",
                    "",
                    0,
                    0f,
                    0.42f,
                    -95f,
                    0f,
                    0f,
                    1f,
                    220f,
                    true,
                    false,
                    false,
                    false,
                    false,
                    true,
                    CarVisualConfig.QUALITY_HIGH);
        }
        if (id == GameScreenState.MAP_OPEN_WORLD) {
            return new MapDefinition(
                    id,
                    "OPEN_WORLD_DISABLED",
                    "AÇIK DÜNYA",
                    "Açık Dünya GLB haritası geçici olarak kaldırıldı. Bu slot pasif; sürüş için Açık Test Alanı kullanılır.",
                    OPEN_WORLD_ASSET_PATH,
                    0,
                    0f,
                    0.42f,
                    0f,
                    0f,
                    0f,
                    1f,
                    180f,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    CarVisualConfig.QUALITY_HIGH);
        }
        if (id == GameScreenState.MAP_SECOND_NEW) {
            return new MapDefinition(
                    id,
                    "NEW_MAP_2",
                    "2. YENİ HARİTA",
                    "İkinci yeni 3D harita dosyası için hazır/pasif slot.",
                    SECOND_MAP_ASSET_PATH,
                    0,
                    0f,
                    0.42f,
                    0f,
                    0f,
                    0f,
                    1f,
                    180f,
                    false,
                    false,
                    false,
                    false,
                    false,
                    false,
                    CarVisualConfig.QUALITY_MEDIUM);
        }
        return new MapDefinition(
                GameScreenState.MAP_OPEN_FIELD,
                "TestMap_OpenField",
                "AÇIK TEST ALANI",
                "Mevcut test alanı ve A60 checkpoint parkurları.",
                "",
                0,
                0f,
                0.42f,
                0f,
                0f,
                0f,
                1f,
                180f,
                true,
                false,
                false,
                true,
                true,
                true,
                CarVisualConfig.QUALITY_HIGH);
    }

    public static boolean isSelectableNow(int id) {
        return definitionFor(id).selectableNow;
    }

    public static boolean isPendingExternalAsset(int id) {
        return definitionFor(id).isPendingExternalAsset();
    }

    public static boolean isExternalReady(int id) {
        MapDefinition definition = definitionFor(id);
        return definition != null && definition.externalGlb && definition.selectableNow;
    }

    public static String displayName(int id) {
        return definitionFor(id).displayName;
    }

    public static String statusLabel(int id) {
        return definitionFor(id).assetStatusLabel();
    }

    public static String description(int id) {
        return definitionFor(id).description;
    }

    public static float miniMapHalfFor(int id) {
        return definitionFor(id).miniMapHalfSize();
    }

    public static String miniMapQaLine(int id) {
        return definitionFor(id).miniMapQaLabel();
    }
}
