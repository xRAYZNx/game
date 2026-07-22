package com.arabaoyunu.render;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * GLB texture yukleme yardimcisi.
 * Mobilde kalite/bellek dengesini korumak icin buyuk texturelari limitler,
 * mipmap acarak uzak/yakin hareketlerde kalite dususu ve titremeyi azaltir.
 */
public final class TextureCache {

    private TextureCache() {}

    public static int createWhiteTexture() {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        byte[] white = new byte[] {(byte)255, (byte)255, (byte)255, (byte)255};
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocateDirect(4);
        buffer.put(white).position(0);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 1, 1, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        return tex[0];
    }

    public static int createTextureFromImageBytes(byte[] imageBytes, int maxTextureSize) {
        return createTextureFromImageBytes(imageBytes, maxTextureSize, true);
    }

    public static int createTextureFromImageBytes(byte[] imageBytes, int maxTextureSize, boolean mipmap) {
        if (imageBytes == null || imageBytes.length == 0) {
            return 0;
        }

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, bounds);

        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return 0;
        }

        int sample = 1;
        int max = Math.max(256, maxTextureSize);
        int[] glMax = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, glMax, 0);
        if (glMax[0] > 0) {
            max = Math.min(max, glMax[0]);
        }
        while ((bounds.outWidth / sample) > max || (bounds.outHeight / sample) > max) {
            sample *= 2;
        }

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opts.inDither = true;
        opts.inSampleSize = sample;

        Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, opts);
        if (bitmap == null) {
            return 0;
        }

        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, tex[0]);

        if (mipmap) {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        } else {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        }
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        if (mipmap) {
            GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        bitmap.recycle();

        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            deleteTexture(tex[0]);
            return 0;
        }
        return tex[0];
    }

    public static void deleteTexture(int id) {
        if (id == 0) return;
        int[] tex = new int[] { id };
        GLES20.glDeleteTextures(1, tex, 0);
    }

    public static void deleteTextures(int[] ids) {
        if (ids == null || ids.length == 0) return;
        for (int i = 0; i < ids.length; i++) {
            if (ids[i] != 0) {
                deleteTexture(ids[i]);
                ids[i] = 0;
            }
        }
    }
}
