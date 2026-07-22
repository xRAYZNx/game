package com.arabaoyunu.render;

/**
 * ArabaOyunu_19: Procedural far/fren/sinyal/dortlu durumlari.
 * GLB icinde animation clip olmasa bile emissive material ile calisir.
 */
public final class VehicleLightState {
    public boolean headlightsOn;
    public boolean brakeOn;
    public boolean reverseOn;
    public boolean hazardOn;
    public boolean leftSignalOn;
    public boolean rightSignalOn;
    public float blinkPhase;

    public float blinkValue() {
        if (blinkPhase < 0.5f) return 1f;
        return 0f;
    }

    public void set(VehicleLightState other) {
        if (other == null) return;
        headlightsOn = other.headlightsOn;
        brakeOn = other.brakeOn;
        reverseOn = other.reverseOn;
        hazardOn = other.hazardOn;
        leftSignalOn = other.leftSignalOn;
        rightSignalOn = other.rightSignalOn;
        blinkPhase = other.blinkPhase;
    }
}
