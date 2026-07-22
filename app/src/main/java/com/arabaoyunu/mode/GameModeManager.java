package com.arabaoyunu.mode;

import com.arabaoyunu.input.InputState;

public final class GameModeManager {

    private BaseGameMode currentMode;
    private boolean started;

    public GameModeManager(BaseGameMode initialMode) {
        currentMode = initialMode;
    }

    public void start() {
        if (currentMode != null && !started) {
            currentMode.start();
            started = true;
        }
    }

    public void update(float dt, InputState input) {
        if (currentMode != null) {
            currentMode.update(dt, input);
        }
    }

    public void switchMode(BaseGameMode nextMode) {
        if (currentMode != null) currentMode.stop();
        currentMode = nextMode;
        started = false;
        start();
    }

    public String getCurrentModeName() {
        return currentMode == null ? "NONE" : currentMode.getName();
    }
}
