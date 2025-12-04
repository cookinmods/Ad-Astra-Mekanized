package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.gravity.GravityManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles gravity modifications for entities within gravity normalizer zones.
 * When an entity enters a gravity-normalized zone, the planet's gravity modifier
 * is replaced with the normalizer's target gravity.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class GravityNormalizerHandler {

    // Attribute modifier IDs
    private static final ResourceLocation NORMALIZER_GRAVITY_ID =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "gravity_normalizer");
    private static final ResourceLocation NORMALIZER_FALL_DISTANCE_ID =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "gravity_normalizer_fall_distance");
    private static final ResourceLocation NORMALIZER_FALL_DAMAGE_ID =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "gravity_normalizer_fall_damage");

    // Planet gravity modifier IDs (from PlanetGravityHandler)
    private static final ResourceLocation PLANET_GRAVITY_ID =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planet_gravity");
    private static final ResourceLocation PLANET_FALL_DISTANCE_ID =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planet_fall_distance");
    private static final ResourceLocation PLANET_FALL_DAMAGE_ID =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planet_fall_damage");

    // Track which entities are currently in a normalized zone
    // This lets us detect when they leave and need planet gravity restored
    private static final Map<UUID, Float> ENTITIES_IN_ZONE = new ConcurrentHashMap<>();

    /**
     * Check and apply gravity normalization every tick for living entities
     */
    @SubscribeEvent
    public static void onLivingEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof LivingEntity living)) return;
        if (entity.level().isClientSide()) return;

        // Only check every 5 ticks for performance (gravity doesn't need instant response)
        if (entity.tickCount % 5 != 0) return;

        BlockPos pos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        Float targetGravity = GravityManager.getInstance().getTargetGravity(entity.level(), pos);

        UUID entityId = entity.getUUID();
        Float currentZoneGravity = ENTITIES_IN_ZONE.get(entityId);

        if (targetGravity != null) {
            // Entity is in a gravity-normalized zone
            if (currentZoneGravity == null || !currentZoneGravity.equals(targetGravity)) {
                // Just entered zone or gravity target changed
                applyNormalizerGravity(living, targetGravity);
                ENTITIES_IN_ZONE.put(entityId, targetGravity);

                if (entity.tickCount % 40 == 0) {
                    AdAstraMekanized.LOGGER.debug("Entity {} entered gravity zone with target {}",
                        entity.getName().getString(), targetGravity);
                }
            }
        } else {
            // Entity is NOT in a gravity-normalized zone
            if (currentZoneGravity != null) {
                // Just left the zone - restore planet gravity
                removeNormalizerGravity(living);
                restorePlanetGravity(living);
                ENTITIES_IN_ZONE.remove(entityId);

                AdAstraMekanized.LOGGER.debug("Entity {} left gravity zone, restoring planet gravity",
                    entity.getName().getString());
            }
        }
    }

    /**
     * Handle item entities in gravity zones
     */
    @SubscribeEvent
    public static void onItemTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item)) return;
        if (item.level().isClientSide()) return;

        // Skip if in water, lava, or has no gravity
        if (item.isInWater() || item.isInLava() || item.isNoGravity()) return;

        BlockPos pos = BlockPos.containing(item.getX(), item.getY(), item.getZ());
        Float targetGravity = GravityManager.getInstance().getTargetGravity(item.level(), pos);

        if (targetGravity == null) return; // Not in zone, let PlanetGravityHandler handle it

        // Only apply when falling
        Vec3 motion = item.getDeltaMovement();
        if (motion.y >= 0) return;

        // Get planet gravity for comparison
        float planetGravity = getPlanetGravity(item.level());

        // Calculate the difference between target and planet gravity
        // and adjust item motion accordingly
        if (Math.abs(targetGravity - planetGravity) > 0.01f) {
            // Items have base gravity of about -0.04 per tick
            // We need to counteract planet gravity and apply target gravity
            double gravityRatio = targetGravity / Math.max(0.01f, planetGravity);
            double adjustment = motion.y * (gravityRatio - 1.0) * 0.5;

            item.setDeltaMovement(motion.x, motion.y + adjustment, motion.z);
        }
    }

    /**
     * Apply gravity normalizer modifiers to a living entity.
     * This removes planet gravity modifiers and applies normalizer modifiers.
     */
    private static void applyNormalizerGravity(LivingEntity living, float targetGravity) {
        // Remove planet gravity modifiers
        removePlanetGravityModifiers(living);

        // Apply normalizer gravity modifier
        AttributeInstance gravityAttribute = living.getAttribute(Attributes.GRAVITY);
        if (gravityAttribute != null) {
            gravityAttribute.removeModifier(NORMALIZER_GRAVITY_ID);

            if (targetGravity != 1.0f) {
                double gravityModifier = targetGravity - 1.0f;
                AttributeModifier modifier = new AttributeModifier(
                    NORMALIZER_GRAVITY_ID,
                    gravityModifier,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                gravityAttribute.addPermanentModifier(modifier);
            }
        }

        // Apply fall distance modifier
        AttributeInstance fallAttribute = living.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (fallAttribute != null) {
            fallAttribute.removeModifier(NORMALIZER_FALL_DISTANCE_ID);

            if (targetGravity != 1.0f && targetGravity > 0) {
                double fallModifier = (1.0f / targetGravity) - 1.0f;
                AttributeModifier modifier = new AttributeModifier(
                    NORMALIZER_FALL_DISTANCE_ID,
                    fallModifier,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                fallAttribute.addPermanentModifier(modifier);
            }
        }

        // Apply fall damage modifier
        AttributeInstance fallDamageAttribute = living.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER);
        if (fallDamageAttribute != null) {
            fallDamageAttribute.removeModifier(NORMALIZER_FALL_DAMAGE_ID);

            if (targetGravity != 1.0f) {
                double damageModifier = targetGravity - 1.0f;
                AttributeModifier modifier = new AttributeModifier(
                    NORMALIZER_FALL_DAMAGE_ID,
                    damageModifier,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                fallDamageAttribute.addPermanentModifier(modifier);
            }
        }
    }

    /**
     * Remove gravity normalizer modifiers from a living entity.
     */
    private static void removeNormalizerGravity(LivingEntity living) {
        AttributeInstance gravityAttribute = living.getAttribute(Attributes.GRAVITY);
        if (gravityAttribute != null) {
            gravityAttribute.removeModifier(NORMALIZER_GRAVITY_ID);
        }

        AttributeInstance fallAttribute = living.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (fallAttribute != null) {
            fallAttribute.removeModifier(NORMALIZER_FALL_DISTANCE_ID);
        }

        AttributeInstance fallDamageAttribute = living.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER);
        if (fallDamageAttribute != null) {
            fallDamageAttribute.removeModifier(NORMALIZER_FALL_DAMAGE_ID);
        }
    }

    /**
     * Remove planet gravity modifiers from a living entity.
     */
    private static void removePlanetGravityModifiers(LivingEntity living) {
        AttributeInstance gravityAttribute = living.getAttribute(Attributes.GRAVITY);
        if (gravityAttribute != null) {
            gravityAttribute.removeModifier(PLANET_GRAVITY_ID);
        }

        AttributeInstance fallAttribute = living.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (fallAttribute != null) {
            fallAttribute.removeModifier(PLANET_FALL_DISTANCE_ID);
        }

        AttributeInstance fallDamageAttribute = living.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER);
        if (fallDamageAttribute != null) {
            fallDamageAttribute.removeModifier(PLANET_FALL_DAMAGE_ID);
        }
    }

    /**
     * Restore planet gravity modifiers to a living entity after leaving a normalized zone.
     */
    private static void restorePlanetGravity(LivingEntity living) {
        float planetGravity = getPlanetGravity(living.level());

        // Apply gravity modifier
        AttributeInstance gravityAttribute = living.getAttribute(Attributes.GRAVITY);
        if (gravityAttribute != null) {
            gravityAttribute.removeModifier(PLANET_GRAVITY_ID);

            if (planetGravity != 1.0f) {
                double gravityModifier = planetGravity - 1.0f;
                AttributeModifier modifier = new AttributeModifier(
                    PLANET_GRAVITY_ID,
                    gravityModifier,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                gravityAttribute.addPermanentModifier(modifier);
            }
        }

        // Apply fall distance modifier
        AttributeInstance fallAttribute = living.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (fallAttribute != null) {
            fallAttribute.removeModifier(PLANET_FALL_DISTANCE_ID);

            if (planetGravity != 1.0f && planetGravity > 0) {
                double fallModifier = (1.0f / planetGravity) - 1.0f;
                AttributeModifier modifier = new AttributeModifier(
                    PLANET_FALL_DISTANCE_ID,
                    fallModifier,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                fallAttribute.addPermanentModifier(modifier);
            }
        }

        // Apply fall damage modifier
        AttributeInstance fallDamageAttribute = living.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER);
        if (fallDamageAttribute != null) {
            fallDamageAttribute.removeModifier(PLANET_FALL_DAMAGE_ID);

            if (planetGravity != 1.0f) {
                double damageModifier = planetGravity - 1.0f;
                AttributeModifier modifier = new AttributeModifier(
                    PLANET_FALL_DAMAGE_ID,
                    damageModifier,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );
                fallDamageAttribute.addPermanentModifier(modifier);
            }
        }
    }

    /**
     * Get the planet's gravity for a level.
     */
    private static float getPlanetGravity(Level level) {
        if (level == null) return 1.0f;

        ResourceLocation dimId = level.dimension().location();

        // Not our dimension? Default gravity
        if (!dimId.getNamespace().equals(AdAstraMekanized.MOD_ID)) {
            return 1.0f;
        }

        // Try to get planet data
        PlanetRegistry registry = PlanetRegistry.getInstance();
        if (!registry.isDataLoaded()) {
            return 1.0f;
        }

        Planet planet = registry.getPlanet(dimId);
        if (planet != null && planet.properties() != null) {
            return planet.properties().gravity();
        }

        return 1.0f;
    }

    /**
     * Clean up tracking when entity is removed from world.
     * Called from entity removal events if needed.
     */
    public static void onEntityRemoved(Entity entity) {
        ENTITIES_IN_ZONE.remove(entity.getUUID());
    }
}
