package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles acid rain damage for planets with acidic atmospheres.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class AcidRainDamageHandler {

    private static final Map<ResourceKey<Level>, Float> ACID_RAIN_DAMAGE_CACHE = new HashMap<>();
    private static final int DAMAGE_INTERVAL = 40; // Damage every 2 seconds (40 ticks)

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity living)) return;
        if (event.getEntity().level().isClientSide()) return;

        // Only check every 40 ticks to reduce performance impact
        if (living.tickCount % DAMAGE_INTERVAL != 0) return;

        Level level = living.level();
        BlockPos pos = living.blockPosition();

        // Check if it's raining in this biome
        if (!isRainingAt(level, pos)) return;

        // Check if entity is exposed to sky (not under cover)
        if (!level.canSeeSky(pos)) return;

        // Skip spectators and creative players
        if (living instanceof Player player) {
            if (player.isSpectator() || player.isCreative()) {
                return;
            }
        }

        // Get acid rain damage for this dimension
        float acidDamage = getAcidRainDamage(level);
        if (acidDamage > 0.0f) {
            // Apply acid rain damage
            DamageSource damageSource = level.damageSources().magic();
            living.hurt(damageSource, acidDamage);
        }
    }

    /**
     * Check if it's raining at a specific position
     */
    private static boolean isRainingAt(Level level, BlockPos pos) {
        if (!level.isRaining()) return false;

        // Check if biome allows rain at this position
        Biome biome = level.getBiome(pos).value();
        return biome.getPrecipitationAt(pos) == Biome.Precipitation.RAIN;
    }

    /**
     * Get acid rain damage amount for a dimension
     */
    private static float getAcidRainDamage(Level level) {
        ResourceKey<Level> dimensionKey = level.dimension();

        // Check cache first
        if (ACID_RAIN_DAMAGE_CACHE.containsKey(dimensionKey)) {
            return ACID_RAIN_DAMAGE_CACHE.get(dimensionKey);
        }

        // Load planet data
        float damage = loadAcidRainDamageFromPlanetData(dimensionKey);
        ACID_RAIN_DAMAGE_CACHE.put(dimensionKey, damage);
        return damage;
    }

    /**
     * Load acid rain damage from planet JSON data
     */
    private static float loadAcidRainDamageFromPlanetData(ResourceKey<Level> dimensionKey) {
        try {
            // Extract planet name from dimension key
            ResourceLocation dimLocation = dimensionKey.location();
            String planetName = dimLocation.getPath(); // e.g., "moon", "mars", etc.

            // Load planet JSON file
            ResourceLocation planetDataLocation = ResourceLocation.fromNamespaceAndPath(
                AdAstraMekanized.MOD_ID, "planets/" + planetName + ".json"
            );

            String resourcePath = "/data/" + planetDataLocation.getNamespace() + "/" + planetDataLocation.getPath();
            var stream = AcidRainDamageHandler.class.getResourceAsStream(resourcePath);

            if (stream != null) {
                JsonObject planetData = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                if (planetData.has("rendering")) {
                    JsonObject rendering = planetData.getAsJsonObject("rendering");

                    if (rendering.has("weather")) {
                        JsonObject weather = rendering.getAsJsonObject("weather");

                        if (weather.has("rain_acidity")) {
                            return weather.get("rain_acidity").getAsFloat();
                        }
                    }
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Could not load acid rain data for dimension {}: {}",
                dimensionKey.location(), e.getMessage());
        }

        return 0.0f; // No acid rain damage
    }

    /**
     * Clear the cache (useful when reloading data)
     */
    public static void clearCache() {
        ACID_RAIN_DAMAGE_CACHE.clear();
    }
}
