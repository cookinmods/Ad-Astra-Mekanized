package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
        DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, AdAstraMekanized.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET_LAUNCH =
        SOUND_EVENTS.register("rocket_launch",
            () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "rocket_launch")));

    public static final DeferredHolder<SoundEvent, SoundEvent> ROCKET =
        SOUND_EVENTS.register("rocket",
            () -> SoundEvent.createVariableRangeEvent(
                ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "rocket")));

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }
}
