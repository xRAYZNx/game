package com.arabaoyunu.map;

import com.arabaoyunu.physics.VehicleController;
import com.arabaoyunu.render.PrimitiveRenderer;
import com.arabaoyunu.render.RenderStats;

public abstract class BaseMap implements VehicleController.GroundProvider {
    public abstract String getName();
    public abstract float getSpawnX();
    public abstract float getSpawnY();
    public abstract float getSpawnZ();
    public abstract float getSpawnYaw();

    public int getRespawnPointCount() {
        return 1;
    }

    public float getRespawnX(int index) {
        return getSpawnX();
    }

    public float getRespawnY(int index) {
        return getSpawnY();
    }

    public float getRespawnZ(int index) {
        return getSpawnZ();
    }

    public float getRespawnYaw(int index) {
        return getSpawnYaw();
    }

    protected final void setSurface(
            VehicleController.GroundHit hit,
            String name,
            float grip,
            float drag,
            float brake,
            float driftAssist
    ) {
        if (hit == null) return;
        hit.surfaceName = name;
        hit.surfaceGrip = grip;
        hit.surfaceDrag = drag;
        hit.surfaceBrake = brake;
        hit.surfaceDriftAssist = driftAssist;
    }


    @Override
    public boolean sampleGround(float x, float z, VehicleController.GroundHit outHit) {
        if (outHit == null) return false;
        outHit.grounded = true;
        outHit.y = 0f;
        outHit.normalX = 0f;
        outHit.normalY = 1f;
        outHit.normalZ = 0f;
        outHit.resetSurface();
        return true;
    }


    /** A61_1: Büyük harita entegrasyonunda her harita kendi metadata tanımını verebilir. */
    public MapDefinition getDefinition() {
        return MapRegistry.definitionFor(com.arabaoyunu.menu.GameScreenState.MAP_OPEN_FIELD);
    }

    /** Büyük GLB haritalarda görsel model ayrı, sürüş zemini/collision proxy ayrı tutulacak. */
    public boolean usesExternalGlbModel() {
        MapDefinition definition = getDefinition();
        return definition != null && definition.externalGlb;
    }

    public abstract void render(PrimitiveRenderer renderer, float[] viewProjection, RenderStats stats);
}
