package com.arabaoyunu.util;

import android.util.Log;

public final class GameLog {

    private static final String PREFIX = "ArabaOyunu";
    private static final String[] RING = new String[80];
    private static int cursor;

    private GameLog() {}

    public static synchronized void i(String tag, String message) {
        String line = "I/" + tag + ": " + message;
        RING[cursor++ % RING.length] = line;
        Log.i(PREFIX + ":" + tag, message);
    }

    public static synchronized void e(String tag, String message, Throwable t) {
        String line = "E/" + tag + ": " + message;
        RING[cursor++ % RING.length] = line;
        Log.e(PREFIX + ":" + tag, message, t);
    }

    public static synchronized String[] snapshot() {
        String[] out = new String[RING.length];
        System.arraycopy(RING, 0, out, 0, RING.length);
        return out;
    }
}
