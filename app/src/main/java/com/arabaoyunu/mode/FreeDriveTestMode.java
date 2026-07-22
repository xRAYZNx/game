package com.arabaoyunu.mode;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.map.MapManager;
import com.arabaoyunu.physics.VehicleController;

public final class FreeDriveTestMode extends BaseGameMode {

    private final VehicleController car;
    private final MapManager mapManager;

    public FreeDriveTestMode(VehicleController car, MapManager mapManager) {
        this.car = car;
        this.mapManager = mapManager;
    }

    @Override
    public String getName() {
        return "FreeDriveTestMode";
    }

    @Override
    public void start() {
        if (mapManager != null && mapManager.getCurrentMap() != null) {
            car.reset(
                    mapManager.getCurrentMap().getSpawnX(),
                    mapManager.getCurrentMap().getSpawnY(),
                    mapManager.getCurrentMap().getSpawnZ(),
                    mapManager.getCurrentMap().getSpawnYaw());
        }
    }

    @Override
    public void update(float dt, InputState input) {
        if (mapManager != null && mapManager.getCurrentMap() != null) {
            car.update(dt, input, mapManager.getCurrentMap());
        }
    }
}
