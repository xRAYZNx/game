package com.arabaoyunu.input;

public final class InputState {
    public float throttle;
    public float brake;
    public float steer;
    public float visualWheelSteer;
    public float handbrake;
    public float nitro;
    public float cameraDragX;
    public float cameraDragY;

    public boolean cameraSwitchPressed;
    public boolean pausePressed;
    public boolean interactPressed;
    public boolean mapOverlayOpen;
    public boolean mapOverlayTogglePressed;

    public boolean leftMirrorOpen;
    public boolean rightMirrorOpen;
    public boolean headlightsOn;
    public boolean hazardOn;
    public boolean leftSignalOn;
    public boolean rightSignalOn;
    public int controlMode;
    public int graphicsQuality;
    public int trafficDensity;
    public int controlLayoutPreset;
    public int steeringSensitivityPreset;
    public int pedalSizePreset;
    public int hudPreset;
    public int buttonOpacityPercent;
    public boolean leftHandedMode;
    public boolean autoControlByMode;

    public void set(InputState other) {
        throttle = other.throttle;
        brake = other.brake;
        steer = other.steer;
        visualWheelSteer = other.visualWheelSteer;
        handbrake = other.handbrake;
        nitro = other.nitro;
        cameraDragX = other.cameraDragX;
        cameraDragY = other.cameraDragY;
        cameraSwitchPressed = other.cameraSwitchPressed;
        pausePressed = other.pausePressed;
        interactPressed = other.interactPressed;
        mapOverlayOpen = other.mapOverlayOpen;
        mapOverlayTogglePressed = other.mapOverlayTogglePressed;
        leftMirrorOpen = other.leftMirrorOpen;
        rightMirrorOpen = other.rightMirrorOpen;
        headlightsOn = other.headlightsOn;
        hazardOn = other.hazardOn;
        leftSignalOn = other.leftSignalOn;
        rightSignalOn = other.rightSignalOn;
        controlMode = other.controlMode;
        graphicsQuality = other.graphicsQuality;
        trafficDensity = other.trafficDensity;
        controlLayoutPreset = other.controlLayoutPreset;
        steeringSensitivityPreset = other.steeringSensitivityPreset;
        pedalSizePreset = other.pedalSizePreset;
        hudPreset = other.hudPreset;
        buttonOpacityPercent = other.buttonOpacityPercent;
        leftHandedMode = other.leftHandedMode;
        autoControlByMode = other.autoControlByMode;
    }

    public void clearMomentary() {
        cameraSwitchPressed = false;
        pausePressed = false;
        interactPressed = false;
        mapOverlayTogglePressed = false;
        cameraDragX = 0f;
        cameraDragY = 0f;
    }
}
