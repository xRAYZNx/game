package com.arabaoyunu.mode;

import com.arabaoyunu.input.InputState;

public abstract class BaseGameMode {
    public abstract String getName();
    public void start() {}
    public void stop() {}
    public abstract void update(float dt, InputState input);
}
