package com.hecookin.adastramekanized.client.sounds;

import com.hecookin.adastramekanized.common.constants.RocketConstants;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import com.hecookin.adastramekanized.common.entities.vehicles.Vehicle;
import com.hecookin.adastramekanized.common.registry.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Looping rocket engine sound that follows the vehicle.
 */
public class RocketSoundInstance extends AbstractTickableSoundInstance {

    protected final Vehicle vehicle;
    protected boolean canPlay = true;

    public RocketSoundInstance(Vehicle vehicle) {
        super(ModSounds.ROCKET.get(), SoundSource.AMBIENT, RandomSource.create());
        this.vehicle = vehicle;
        this.looping = true;
        this.delay = 0;
        this.x = vehicle.getX();
        this.y = vehicle.getY();
        this.z = vehicle.getZ();
    }

    @Override
    public float getVolume() {
        return canPlay ? 10.0f : 0.0f;
    }

    @Override
    public void tick() {
        if (vehicle.isRemoved()) {
            stop();
            return;
        }
        // Stop rocket sound when reaching atmosphere leave height (planet menu opens)
        if (vehicle instanceof Rocket && vehicle.getY() >= RocketConstants.ATMOSPHERE_LEAVE_HEIGHT) {
            stop();
            return;
        }
        x = vehicle.getX();
        y = vehicle.getY();
        z = vehicle.getZ();
    }
}
