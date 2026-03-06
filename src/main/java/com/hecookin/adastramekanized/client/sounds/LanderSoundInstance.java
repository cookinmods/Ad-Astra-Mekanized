package com.hecookin.adastramekanized.client.sounds;

import com.hecookin.adastramekanized.common.entities.vehicles.Vehicle;

/**
 * Lander engine sound that only plays while thrusting (space held) and airborne.
 */
public class LanderSoundInstance extends RocketSoundInstance {

    public LanderSoundInstance(Vehicle vehicle) {
        super(vehicle);
    }

    @Override
    public void tick() {
        this.canPlay = vehicle.passengerHasSpaceDown();
        if (!vehicle.isRemoved() && !vehicle.onGround()) {
            x = vehicle.getX();
            y = vehicle.getY();
            z = vehicle.getZ();
        } else {
            stop();
        }
    }
}
