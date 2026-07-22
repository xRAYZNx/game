package com.arabaoyunu.render;

public final class RenderStats {
    public int drawCalls;
    public int renderedObjects;

    public void reset() {
        drawCalls = 0;
        renderedObjects = 0;
    }
}
