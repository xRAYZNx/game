package com.arabaoyunu.ui;

import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.util.SaveManager;

/**
 * A62.6: Menü, mod hub, ekonomi ve XP ekranlarında ortak denge/metin yardımcıları.
 * Bu sınıf oyun mekaniğini bozmaz; sadece oyuncuya doğru hedefi ve ödül dengesini net gösterir.
 */
public final class UiBalanceSystem {

    private UiBalanceSystem() { }

    public static String recommendedNextAction(SaveManager save) {
        if (save == null) return "Kariyere başla, garajı aç ve ilk sürüşe çık.";
        if (!save.isCareerStarted()) return "Starter aracını seç ve kariyeri başlat.";
        if (save.isDailyRewardAvailable()) return "Günlük ödül hazır: önce ödülünü al.";
        if (save.getPlayerLevel() < 2) return "Serbest sürüş görevleriyle LVL 2 yap, Drift modunu aç.";
        if (save.getCoins() < 2500) return "Serbest sürüş/Drag ile coin kas, garaj yükseltmesi al.";
        if (save.getCareerTotalWins() < 3) return "Checkpoint Yarışı kazan, coin/XP ve en iyi süreyi geliştir.";
        if (save.getPlayerLevel() < 6) return "XP kasarak Profesyonel Lig ve daha iyi araçları aç.";
        return "Kariyer yarışları ve garaj yükseltmeleriyle üst liglere ilerle.";
    }

    public static String economyTier(SaveManager save) {
        int coins = save == null ? 0 : save.getCoins();
        if (coins < 1000) return "Ekonomi: düşük bütçe";
        if (coins < 7500) return "Ekonomi: garaj yükseltmesi mümkün";
        if (coins < 25000) return "Ekonomi: yeni araç hedefi";
        return "Ekonomi: üst sınıf araç hedefi";
    }

    public static String balanceHint(SaveManager save) {
        if (save == null) return "Ödül dengesi: başlangıç hızlı, üst seviye kontrollü.";
        int level = save.getPlayerLevel();
        if (level <= 2) return "Denge: başlangıçta hızlı ödül, oyuncu oyuna alışır.";
        if (level <= 6) return "Denge: yarış + görev + günlük ödül birlikte ilerletir.";
        if (level <= 10) return "Denge: araç satın alma ve modifiye maliyetleri önem kazanır.";
        return "Denge: üst liglerde galibiyet ve iyi derece ana kazanç kaynağıdır.";
    }

    public static String modeTitle(int mode) {
        switch (mode) {
            case GameScreenState.MODE_TIME_TRIAL: return "Zaman Yarışı";
            case GameScreenState.MODE_DRIFT: return "Drift Combo";
            case GameScreenState.MODE_RACE_LOCKED: return "Checkpoint Yarışı";
            case GameScreenState.MODE_POLICE_CHASE: return "Polis Kovalamaca";
            case GameScreenState.MODE_DRAG_RACE: return "Rakipli Drag 400M";
            case GameScreenState.MODE_FREE_DRIVE:
            default: return "Serbest Sürüş";
        }
    }

    public static String modeDescription(int mode) {
        switch (mode) {
            case GameScreenState.MODE_TIME_TRIAL:
                return "Süreye karşı koş, temiz sürüşle en iyi zamanı geliştir.";
            case GameScreenState.MODE_DRIFT:
                return "Open Field üzerinde 120 saniye drift yap, açı + hız + combo ile skor kas.";
            case GameScreenState.MODE_RACE_LOCKED:
                return "Open Field kısa rotasında checkpointleri sırayla geç, süreyi düşür ve ödül kazan.";
            case GameScreenState.MODE_POLICE_CHASE:
                return "Takipten kaç, hasar almadan süreyi doldur.";
            case GameScreenState.MODE_DRAG_RACE:
                return "400 metrede doğru kalkış, N2O ve hızlanma fark yaratır.";
            case GameScreenState.MODE_FREE_DRIVE:
            default:
                return "Mini görev, sürüş bonusu, günlük ilerleme ve test alanı.";
        }
    }

    public static String modeRewardText(int mode) {
        switch (mode) {
            case GameScreenState.MODE_DRIFT: return "Ödül: skor rankı + rekor bonusu + coin + XP";
            case GameScreenState.MODE_RACE_LOCKED: return "Ödül: süre derecesi + coin + XP + en iyi süre";
            case GameScreenState.MODE_DRAG_RACE: return "Ödül: süre + rakip sonucu + en iyi hız";
            case GameScreenState.MODE_POLICE_CHASE: return "Ödül: kaçış başarısı + risk bonusu";
            case GameScreenState.MODE_TIME_TRIAL: return "Ödül: en iyi süre + XP";
            default: return "Ödül: mini görev + sürüş bonusu";
        }
    }

    public static String hudModeLabel(SaveManager save) {
        if (save == null) return "HUD: Standart";
        return "HUD: " + save.getHudPresetLabel();
    }
}
