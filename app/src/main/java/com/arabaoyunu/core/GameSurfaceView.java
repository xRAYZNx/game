package com.arabaoyunu.core;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.arabaoyunu.input.TouchControlsView;
import com.arabaoyunu.menu.GameScreenState;
import com.arabaoyunu.ui.HudView;

public final class GameSurfaceView extends GLSurfaceView {

    private final GameRenderer renderer;

    public GameSurfaceView(Context context, TouchControlsView controls, HudView hudView, GameScreenState screenState) {
        super(context);
        setEGLContextClientVersion(2);
        setPreserveEGLContextOnPause(true);
        renderer = new GameRenderer(context.getApplicationContext(), controls, hudView, screenState);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    public GameRenderer getGameRenderer() {
        return renderer;
    }
}
