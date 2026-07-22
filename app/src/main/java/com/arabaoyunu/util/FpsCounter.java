package com.arabaoyunu.util;

public final class FpsCounter {
    private float elapsed;
    private int frames;
    private int currentFps;

    public int frame(float dt) {
        elapsed += dt;
        frames++;
        if (elapsed >= 0.5f) {
            currentFps = Math.round(frames / elapsed);
            frames = 0;
            elapsed = 0f;
        }
        return currentFps;
    }
}
