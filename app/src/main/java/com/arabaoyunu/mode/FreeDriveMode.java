package com.arabaoyunu.mode;

import com.arabaoyunu.input.InputState;
import com.arabaoyunu.map.MapManager;
import com.arabaoyunu.physics.VehicleController;

/**
 * ArabaOyunu_17: Serbest Surus / Acik Bos Harita.
 * Checkpoint, geri sayim, drift skoru veya yaris sistemi otomatik baslamaz.
 */
public final class FreeDriveMode extends BaseGameMode {

    private final VehicleController car;
    private final MapManager mapManager;

    public FreeDriveMode(VehicleController car, MapManager mapManager) {
        this.car = car;
        this.mapManager = mapManager;
    }

    @Override
    public String getName() {
        return "FreeDriveMode";
    }

    @Override
    public void start() {
        if (car != null && mapManager != null && mapManager.getCurrentMap() != null) {
            car.reset(
                    mapManager.getCurrentMap().getSpawnX(),
                    mapManager.getCurrentMap().getSpawnY(),
                    mapManager.getCurrentMap().getSpawnZ(),
                    mapManager.getCurrentMap().getSpawnYaw());
        }
    }

    @Override
    public void update(float dt, InputState input) {
        if (dt <= 0f) return;
        if (car != null && mapManager != null && mapManager.getCurrentMap() != null) {
            car.update(dt, input, mapManager.getCurrentMap());
        }
    }
}
