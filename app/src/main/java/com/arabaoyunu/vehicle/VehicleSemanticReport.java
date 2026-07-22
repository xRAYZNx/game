package com.arabaoyunu.vehicle;

/**
 * ArabaOyunu_19: GLB icinden yakalanan semantik parcacik raporu.
 */
public final class VehicleSemanticReport {
    public int wheelFL;
    public int wheelFR;
    public int wheelRL;
    public int wheelRR;
    public int frontLights;
    public int rearLights;
    public int brakeLights;
    public int turnSignals;
    public int glass;
    public int paint;

    public String summary() {
        return "FL=" + wheelFL
                + " FR=" + wheelFR
                + " RL=" + wheelRL
                + " RR=" + wheelRR
                + " FrontLight=" + frontLights
                + " RearLight=" + rearLights
                + " Brake=" + brakeLights
                + " Signal=" + turnSignals
                + " Glass=" + glass
                + " Paint=" + paint;
    }
}
