package com.arabaoyunu.customization;

import com.arabaoyunu.util.SaveManager;
import com.arabaoyunu.vehicle.VehicleCatalog;

/** ArabaOyunu_48: Tek seferlik redeem code sistemi. */
public final class RedeemCodeSystem {

    public static final class Result {
        public final boolean success;
        public final String message;

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message == null ? "" : message;
        }
    }

    private RedeemCodeSystem() {}

    public static Result redeem(SaveManager saveManager, String rawCode) {
        if (saveManager == null) return new Result(false, "Kayıt sistemi hazır değil");
        String code = normalize(rawCode);
        if (code.length() == 0) return new Result(false, "Kod boş");

        if (saveManager.isRedeemCodeUsed(code)) {
            return new Result(false, "Kod zaten kullanıldı: " + rawCode);
        }

        if ("rayzn1".equals(code)) {
            saveManager.addCoins(200000);
            saveManager.setRedeemCodeUsed(code, true);
            saveManager.setEconomyLastMessage("REDEEM: Rayzn1 +200000 coin");
            return new Result(true, "Rayzn1 aktif: +200000 para");
        }

        if ("rayzncar".equals(code)) {
            for (int i = 0; i < VehicleCatalog.count(); i++) {
                saveManager.setVehicleOwned(VehicleCatalog.id(i), true);
                saveManager.setVehicleRewardUnlocked(VehicleCatalog.id(i), true);
            }
            saveManager.setRedeemCodeUsed(code, true);
            saveManager.setEconomyLastMessage("REDEEM: Rayzncar tüm araçları açtı");
            return new Result(true, "Rayzncar aktif: Tüm araçlar açıldı");
        }

        return new Result(false, "Geçersiz kod: " + rawCode);
    }

    public static String normalize(String code) {
        if (code == null) return "";
        return code.trim().toLowerCase(java.util.Locale.US);
    }
}
