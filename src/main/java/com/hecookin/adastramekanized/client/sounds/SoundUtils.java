package com.hecookin.adastramekanized.client.sounds;

import com.hecookin.adastramekanized.common.entities.vehicles.Lander;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import net.minecraft.client.Minecraft;

public class SoundUtils {

    public static void playRocketSound(Rocket rocket) {
        Minecraft.getInstance().getSoundManager().play(new RocketSoundInstance(rocket));
    }

    public static void playLanderSound(Lander lander) {
        Minecraft.getInstance().getSoundManager().play(new LanderSoundInstance(lander));
    }
}
