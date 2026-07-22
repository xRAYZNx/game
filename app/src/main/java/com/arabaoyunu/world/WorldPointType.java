package com.arabaoyunu.world;

/** ArabaOyunu_42: Açık dünya fiziksel etkileşim nokta türleri. */
public final class WorldPointType {
    public static final int NONE = 0;
    public static final int RACE = 1;
    public static final int DRIFT = 2;
    public static final int TIME_TRIAL = 3;
    public static final int POLICE_ESCAPE = 4;
    public static final int GARAGE = 5;
    public static final int VEHICLE_GALLERY = 6;
    public static final int REPAIR = 7;
    public static final int FUEL_SERVICE = 8;
    public static final int SPECIAL_EVENT = 9;

    private WorldPointType() {}

    public static String label(int type) {
        switch (type) {
            case RACE: return "YARIŞ NOKTASI";
            case DRIFT: return "DRIFT NOKTASI";
            case TIME_TRIAL: return "ZAMAN YARIŞI";
            case POLICE_ESCAPE: return "POLİS KAÇIŞ";
            case GARAGE: return "GARAJ";
            case VEHICLE_GALLERY: return "ARAÇ GALERİSİ";
            case REPAIR: return "TAMİR NOKTASI";
            case FUEL_SERVICE: return "BENZİN / BAKIM";
            case SPECIAL_EVENT: return "ÖZEL ETKİNLİK";
            default: return "-";
        }
    }

    public static String icon(int type) {
        switch (type) {
            case RACE: return "Y";
            case DRIFT: return "D";
            case TIME_TRIAL: return "Z";
            case POLICE_ESCAPE: return "P";
            case GARAGE: return "G";
            case VEHICLE_GALLERY: return "GA";
            case REPAIR: return "T";
            case FUEL_SERVICE: return "B";
            case SPECIAL_EVENT: return "E";
            default: return "?";
        }
    }

    public static String action(int type) {
        switch (type) {
            case RACE: return "Yarışı Başlat";
            case DRIFT: return "Drift Alanına Gir";
            case TIME_TRIAL: return "Zaman Yarışı Başlat";
            case POLICE_ESCAPE: return "Polis Kaçışını Başlat";
            case GARAGE: return "Garajı Aç";
            case VEHICLE_GALLERY: return "Araç Galerisini Aç";
            case REPAIR: return "Aracı Tamir Et";
            case FUEL_SERVICE: return "Bakım Yap";
            case SPECIAL_EVENT: return "Etkinliği Başlat";
            default: return "Etkileşim";
        }
    }
}
