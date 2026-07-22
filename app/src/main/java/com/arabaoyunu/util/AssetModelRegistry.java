package com.arabaoyunu.util;

import android.content.Context;

import java.io.InputStream;

public final class AssetModelRegistry {

    private AssetModelRegistry() {}

    public static boolean hasAsset(Context context, String assetPath) {
        if (context == null || assetPath == null) return false;
        InputStream stream = null;
        try {
            stream = context.getAssets().open(assetPath);
            return true;
        } catch (Exception ignored) {
            return false;
        } finally {
            if (stream != null) {
                try { stream.close(); } catch (Exception ignored) {}
            }
        }
    }
}
