package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.atmosphere.OxygenManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.util.RandomSource;

/**
 * Handles mob equipment for planet-specific spawns
 * Inspired by When Dungeons Arise's approach of pre-equipped mobs
 * Equipment is dynamically loaded from planet generation configuration
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class PlanetMobSpawnHandler {

    // Map of dimension -> mob type -> equipment configuration
    private static Map<ResourceLocation, Map<String, EquipmentConfig>> PLANET_MOB_EQUIPMENT = new HashMap<>();
    private static boolean configsLoaded = false;

    /**
     * Load equipment configurations on first spawn event (fallback for development)
     */
    private static void ensureConfigsLoaded() {
        if (!configsLoaded && PLANET_MOB_EQUIPMENT.isEmpty()) {
            // Try loading from file system (development mode fallback)
            PLANET_MOB_EQUIPMENT = PlanetMobEquipmentLoader.loadEquipmentConfigs();
            configsLoaded = true;
            if (!PLANET_MOB_EQUIPMENT.isEmpty()) {
                AdAstraMekanized.LOGGER.info("Loaded planet mob equipment configurations for " + PLANET_MOB_EQUIPMENT.size() + " dimensions (dev mode)");
            }
        }
    }

    /**
     * Set equipment configurations (called by DataPackReloadListener)
     */
    public static void setEquipmentConfigs(Map<ResourceLocation, Map<String, EquipmentConfig>> configs) {
        PLANET_MOB_EQUIPMENT = configs;
        configsLoaded = true;
        AdAstraMekanized.LOGGER.info("Set planet mob equipment configurations for " + configs.size() + " dimensions");
    }


    /**
     * Tags naturally-spawned mobs on non-breathable planets with SpaceAdapted flag.
     * This makes them immune to oxygen damage (checked by OxygenDamageHandler).
     * Mobs from spawn eggs, commands, or spawners do NOT get this tag.
     */
    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        Level level = event.getLevel().getLevel();

        // Only process in our planetary dimensions
        if (!level.dimension().location().getNamespace().equals(AdAstraMekanized.MOD_ID)) {
            return;
        }

        // Only tag natural spawns, not manual spawns (spawn eggs, commands, spawners)
        if (isManualSpawn(event.getSpawnType())) {
            return;
        }

        // Check if this planet has a non-breathable atmosphere
        // OxygenManager.hasOxygen(Level) returns true if breathable, false if not
        if (!OxygenManager.getInstance().hasOxygen(level)) {
            mob.getPersistentData().putBoolean("SpaceAdapted", true);
        }
    }

    /**
     * Check if a spawn type is manual (player-initiated).
     * Mirrors ModdedMobSpawnController.isManualSpawn() logic.
     */
    private static boolean isManualSpawn(MobSpawnType spawnType) {
        return spawnType == MobSpawnType.SPAWN_EGG ||
               spawnType == MobSpawnType.SPAWNER ||
               spawnType == MobSpawnType.TRIAL_SPAWNER ||
               spawnType == MobSpawnType.COMMAND ||
               spawnType == MobSpawnType.DISPENSER ||
               spawnType == MobSpawnType.BUCKET ||
               spawnType == MobSpawnType.BREEDING ||
               spawnType == MobSpawnType.MOB_SUMMONED ||
               spawnType == MobSpawnType.CONVERSION;
    }

    @SubscribeEvent
    public static void onMobSpawn(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (event.getLevel().isClientSide()) return;

        // Load configs if not already loaded
        ensureConfigsLoaded();

        Level level = event.getLevel();
        ResourceLocation dimensionId = level.dimension().location();

        // Debug logging
        String mobId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();

        // Check if this dimension has custom mob equipment
        Map<String, EquipmentConfig> dimensionEquipment = PLANET_MOB_EQUIPMENT.get(dimensionId);
        if (dimensionEquipment == null) {
            // Only log for our planets to avoid spam
            if (dimensionId.getNamespace().equals("adastramekanized")) {
                AdAstraMekanized.LOGGER.debug("No equipment config for dimension: " + dimensionId);
            }
            return;
        }

        // Check if this mob type has equipment configuration
        EquipmentConfig config = dimensionEquipment.get(mobId);
        if (config == null) {
            AdAstraMekanized.LOGGER.debug("No equipment config for mob " + mobId + " in dimension " + dimensionId);
            return;
        }

        // Apply equipment
        AdAstraMekanized.LOGGER.debug("Equipping " + mobId + " with gear on " + dimensionId);
        config.applyToMob(mob, level.getRandom());
    }

    /**
     * Equipment configuration for a mob type
     */
    public static class EquipmentConfig {
        private Map<EquipmentSlot, ItemConfig> equipment = new HashMap<>();
        private int enchantmentLevel = 0;
        private float dropChance = 0.0f;

        public EquipmentConfig withHelmet(String itemId, float chance) {
            equipment.put(EquipmentSlot.HEAD, new ItemConfig(itemId, chance));
            return this;
        }

        public EquipmentConfig withChestplate(String itemId, float chance) {
            equipment.put(EquipmentSlot.CHEST, new ItemConfig(itemId, chance));
            return this;
        }

        public EquipmentConfig withLeggings(String itemId, float chance) {
            equipment.put(EquipmentSlot.LEGS, new ItemConfig(itemId, chance));
            return this;
        }

        public EquipmentConfig withBoots(String itemId, float chance) {
            equipment.put(EquipmentSlot.FEET, new ItemConfig(itemId, chance));
            return this;
        }

        public EquipmentConfig withWeapon(String itemId, float chance) {
            equipment.put(EquipmentSlot.MAINHAND, new ItemConfig(itemId, chance));
            return this;
        }

        public EquipmentConfig withOffhand(String itemId, float chance) {
            equipment.put(EquipmentSlot.OFFHAND, new ItemConfig(itemId, chance));
            return this;
        }

        public EquipmentConfig withFullArmor(String materialPrefix, float chance) {
            withHelmet(materialPrefix + "_helmet", chance);
            withChestplate(materialPrefix + "_chestplate", chance);
            withLeggings(materialPrefix + "_leggings", chance);
            withBoots(materialPrefix + "_boots", chance);
            return this;
        }

        public EquipmentConfig withEnchantmentLevel(int level) {
            this.enchantmentLevel = level;
            return this;
        }

        public EquipmentConfig setDropChance(float chance) {
            this.dropChance = chance;
            return this;
        }

        public void applyToMob(Mob mob, RandomSource random) {
            for (Map.Entry<EquipmentSlot, ItemConfig> entry : equipment.entrySet()) {
                ItemConfig itemConfig = entry.getValue();
                if (random.nextFloat() < itemConfig.chance) {
                    ItemStack item = createItemStack(itemConfig.itemId);

                    // Apply enchantments if configured
                    if (enchantmentLevel > 0 && !item.isEmpty()) {
                        // Simplified enchantment - in practice would use proper enchantment system
                        if (item.isEnchantable()) {
                            // Would apply random enchantments based on level
                        }
                    }

                    mob.setItemSlot(entry.getKey(), item);

                    // Set drop chance (usually 0 to prevent farming)
                    mob.setDropChance(entry.getKey(), this.dropChance);
                }
            }
        }

        private ItemStack createItemStack(String itemId) {
            try {
                var resourceLoc = ResourceLocation.tryParse(itemId);
                if (resourceLoc != null) {
                    var item = BuiltInRegistries.ITEM.get(resourceLoc);
                    if (item != null && item != Items.AIR) {
                        return new ItemStack(item);
                    }
                }
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.warn("Failed to create item stack for: " + itemId);
            }
            return ItemStack.EMPTY;
        }
    }

    private static class ItemConfig {
        final String itemId;
        final float chance;

        ItemConfig(String itemId, float chance) {
            this.itemId = itemId;
            this.chance = chance;
        }
    }

    /**
     * Reload equipment configurations (for data pack reload)
     */
    public static void reloadConfigurations() {
        PLANET_MOB_EQUIPMENT.clear();
        configsLoaded = false;
        AdAstraMekanized.LOGGER.info("Cleared mob equipment configurations for reload");
    }
}