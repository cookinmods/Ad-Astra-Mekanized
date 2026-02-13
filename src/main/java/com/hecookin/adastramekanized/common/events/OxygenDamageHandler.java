package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.atmosphere.OxygenManager;
import com.hecookin.adastramekanized.common.tags.ModEntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Handles oxygen damage for entities in non-breathable environments.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class OxygenDamageHandler {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (event.getEntity().level().isClientSide()) return;

        // Skip entities that don't need oxygen
        if (living.getType().is(ModEntityTypeTags.LIVES_WITHOUT_OXYGEN)) return;
        if (living.getType().is(ModEntityTypeTags.CAN_SURVIVE_IN_SPACE)) return;

        // Additional check for modded entities by namespace
        // These mobs are configured on planets and should be immune to oxygen damage
        String entityId = living.getType().getDescriptionId();
        if (entityId != null) {
            if (entityId.contains("mowziesmobs")) {
                return; // All Mowzie's Mobs are exempt from oxygen damage
            }
            if (entityId.contains("undead_revamp2")) {
                return; // All Undead Revamp 2 mobs are exempt from oxygen damage
            }
            if (entityId.contains("doom")) {
                return; // MCDoom demons are exempt - they're from hell
            }
            if (entityId.contains("ribbits")) {
                return; // Ribbits frog people are exempt - magical beings
            }
            if (entityId.contains("kobolds")) {
                return; // Kobolds are exempt - cave dwellers adapted to any atmosphere
            }
            if (entityId.contains("reptilian")) {
                return; // Reptilian creatures are exempt - adapted to harsh environments
            }
            if (entityId.contains("lumination") || entityId.contains("luminousworld")) {
                return; // Luminous World mobs are exempt - magical creatures
            }
            if (entityId.contains("born_in_chaos")) {
                return; // Born in Chaos mobs are exempt - supernatural creatures
            }
            if (entityId.contains("mobs_of_mythology")) {
                return; // Mythology mobs are exempt - mythical beings
            }
            if (entityId.contains("rottencreatures")) {
                return; // Rotten Creatures are exempt - undead adapted to harsh environments
            }
            if (entityId.contains("shineals_prehistoric_expansion")) {
                return; // Prehistoric creatures are exempt - ancient adaptations
            }
        }

        // Check if entity was tagged as space-adapted during natural spawning
        if (living.getPersistentData().getBoolean("SpaceAdapted")) {
            return; // Naturally spawned on this planet - immune to oxygen damage
        }

        // Skip spectators and creative players
        if (living instanceof Player player) {
            if (player.isSpectator() || player.isCreative()) {
                return;
            }
        }

        // Apply oxygen effects
        OxygenManager.getInstance().applyOxygenEffects(living);
    }
}