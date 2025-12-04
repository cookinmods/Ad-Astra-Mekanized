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
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Handles gravity modifications for planetary dimensions.
 * Uses Minecraft's built-in gravity attribute system for living entities.
 * Applies simple gravity adjustments to dropped items.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class PlanetGravityHandler {

    // Gravity multiplier cache for performance
    private static final Map<ResourceLocation, Float> GRAVITY_CACHE = new ConcurrentHashMap<>();

    // Attribute modifier ID for gravity
    private static final ResourceLocation GRAVITY_MODIFIER_ID =
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "planet_gravity");

    /**
     * Get gravity multiplier for a dimension
     */
    private static float getGravity(Level level) {
        if (level == null || level.isClientSide()) return 1.0f;

        ResourceLocation dimId = level.dimension().location();

        // Check cache first
        Float cached = GRAVITY_CACHE.get(dimId);
        if (cached != null) return cached;

        // Not our dimension? Default gravity
        if (!dimId.getNamespace().equals(AdAstraMekanized.MOD_ID)) {
            GRAVITY_CACHE.put(dimId, 1.0f);
            return 1.0f;
        }

        // Try to get planet data
        PlanetRegistry registry = PlanetRegistry.getInstance();
        if (!registry.isDataLoaded()) {
            return 1.0f; // Data not loaded yet
        }

        Planet planet = registry.getPlanet(dimId);
        if (planet != null && planet.properties() != null) {
            float gravity = planet.properties().gravity();
            GRAVITY_CACHE.put(dimId, gravity);
            AdAstraMekanized.LOGGER.debug("Cached gravity {} for dimension {}", gravity, dimId);
            return gravity;
        }

        // Default to Earth gravity if planet not found
        GRAVITY_CACHE.put(dimId, 1.0f);
        return 1.0f;
    }

    /**
     * Clear gravity cache when dimensions reload
     */
    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide()) {
            GRAVITY_CACHE.clear();
            AdAstraMekanized.LOGGER.info("Cleared gravity cache on world load");
        }
    }

    /**
     * Apply gravity to entities when they join a world
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        Level level = event.getLevel();

        if (level.isClientSide()) return;

        // Check if entity is in a gravity-normalized zone
        // If so, let GravityNormalizerHandler handle it instead
        BlockPos pos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        if (GravityManager.getInstance().isInGravityZone(level, pos)) {
            return; // Skip planet gravity - normalizer will handle it
        }

        float gravity = getGravity(level);

        // Handle living entities with attributes
        if (entity instanceof LivingEntity living) {
            applyLivingEntityGravity(living, gravity);
        }
        // Items don't need special handling on spawn - we'll get gravity from level in tick
    }

    /**
     * Apply gravity attributes to living entities
     */
    private static void applyLivingEntityGravity(LivingEntity living, float gravity) {

        // Apply gravity attribute modifier
        AttributeInstance gravityAttribute = living.getAttribute(Attributes.GRAVITY);
        if (gravityAttribute != null) {
            // Remove any existing gravity modifier from our mod
            gravityAttribute.removeModifier(GRAVITY_MODIFIER_ID);

            if (gravity != 1.0f) {
                // Gravity attribute works as a multiplier
                // Default is 0.08, so we multiply by our gravity ratio
                double gravityModifier = gravity - 1.0f;

                AttributeModifier modifier = new AttributeModifier(
                    GRAVITY_MODIFIER_ID,
                    gravityModifier,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );

                gravityAttribute.addPermanentModifier(modifier);
                AdAstraMekanized.LOGGER.debug("Applied gravity modifier {} to {} ",
                    gravity, living.getType());
            }
        }

        // Also apply safe fall distance modifier for better gameplay
        AttributeInstance fallAttribute = living.getAttribute(Attributes.SAFE_FALL_DISTANCE);
        if (fallAttribute != null) {
            // Remove any existing fall modifier
            ResourceLocation fallModifierId = ResourceLocation.fromNamespaceAndPath(
                AdAstraMekanized.MOD_ID, "planet_fall_distance"
            );
            fallAttribute.removeModifier(fallModifierId);

            if (gravity != 1.0f && gravity > 0) {
                // Lower gravity = can fall further safely
                // Higher gravity = less safe fall distance
                double fallModifier = (1.0f / gravity) - 1.0f;

                AttributeModifier modifier = new AttributeModifier(
                    fallModifierId,
                    fallModifier,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );

                fallAttribute.addPermanentModifier(modifier);
            }
        }

        // Apply fall damage multiplier for even better feel
        AttributeInstance fallDamageAttribute = living.getAttribute(Attributes.FALL_DAMAGE_MULTIPLIER);
        if (fallDamageAttribute != null) {
            // Remove any existing fall damage modifier
            ResourceLocation fallDamageModifierId = ResourceLocation.fromNamespaceAndPath(
                AdAstraMekanized.MOD_ID, "planet_fall_damage"
            );
            fallDamageAttribute.removeModifier(fallDamageModifierId);

            if (gravity != 1.0f) {
                // Scale fall damage by gravity
                // Lower gravity = less damage, higher gravity = more damage
                double damageModifier = gravity - 1.0f;

                AttributeModifier modifier = new AttributeModifier(
                    fallDamageModifierId,
                    damageModifier,
                    AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                );

                fallDamageAttribute.addPermanentModifier(modifier);
            }
        }
    }

    /**
     * Apply gravity to dropped items during tick
     * Uses the same gravity value from planet data that living entities use
     */
    @SubscribeEvent
    public static void onItemTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity item)) return;
        if (item.level().isClientSide()) return;

        // Skip if in water, lava, or has no gravity
        if (item.isInWater() || item.isInLava() || item.isNoGravity()) return;

        // Check if item is in a gravity-normalized zone
        // If so, let GravityNormalizerHandler handle it instead
        BlockPos pos = BlockPos.containing(item.getX(), item.getY(), item.getZ());
        if (GravityManager.getInstance().isInGravityZone(item.level(), pos)) {
            return; // Skip planet gravity - normalizer will handle it
        }

        // Get gravity directly from the level - same source as living entities
        float gravity = getGravity(item.level());
        if (gravity == 1.0f) return; // No modification needed for Earth gravity

        // Only apply when falling (negative Y velocity)
        Vec3 motion = item.getDeltaMovement();
        if (motion.y >= 0) return; // Not falling

        // Items have a base gravity of about -0.04 per tick
        // We want to scale their fall speed by the gravity multiplier
        // But we need to be careful to not make it too extreme
        double gravityAdjustment = motion.y * (gravity - 1.0f) * 0.5; // Only apply 50% of the difference to keep it subtle

        // Apply the adjustment
        item.setDeltaMovement(motion.x, motion.y + gravityAdjustment, motion.z);
    }
}