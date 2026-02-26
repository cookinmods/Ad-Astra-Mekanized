package com.hecookin.adastramekanized.common.events;

import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Data structure for tracking which mod namespaces are allowed to spawn mobs
 * in specific dimensions and biomes.
 */
public class ModdedMobWhitelistData {
    // Map: Dimension ID → Set of allowed mod namespaces (planet-level whitelist)
    private final Map<ResourceLocation, Set<String>> dimensionWhitelist = new HashMap<>();

    // Map: Dimension ID → (Biome ID → Set of allowed mod namespaces) (biome-specific overrides)
    private final Map<ResourceLocation, Map<ResourceLocation, Set<String>>> biomeWhitelist = new HashMap<>();

    // Set of mod namespaces that are under spawn control
    private final Set<String> controlledMods = new HashSet<>();

    // Map: Dimension ID → Set of allowed entity IDs (entity-level whitelist for planet dimensions)
    private final Map<ResourceLocation, Set<ResourceLocation>> entityWhitelist = new HashMap<>();

    public ModdedMobWhitelistData() {
        // Initialize with controlled mods
        controlledMods.add("kobolds");
        controlledMods.add("ribbits");  // Frog villagers and swamp villages
        controlledMods.add("born_in_chaos_v1");  // Born in Chaos spirits, undead, and Halloween mobs
    }

    /**
     * Add a mod namespace to the controlled mods list.
     * Controlled mods will have their spawns restricted to whitelisted dimensions/biomes.
     */
    public void addControlledMod(String modNamespace) {
        controlledMods.add(modNamespace);
    }

    /**
     * Check if a mod namespace is under spawn control.
     */
    public boolean isControlledMod(String modNamespace) {
        return controlledMods.contains(modNamespace);
    }

    /**
     * Get all controlled mod namespaces.
     */
    public Set<String> getControlledMods() {
        return Collections.unmodifiableSet(controlledMods);
    }

    /**
     * Whitelist a mod for an entire dimension (all biomes).
     */
    public void whitelistModForDimension(ResourceLocation dimensionId, String modNamespace) {
        dimensionWhitelist.computeIfAbsent(dimensionId, k -> new HashSet<>()).add(modNamespace);
    }

    /**
     * Whitelist a mod for a specific biome in a dimension.
     */
    public void whitelistModForBiome(ResourceLocation dimensionId, ResourceLocation biomeId, String modNamespace) {
        biomeWhitelist
            .computeIfAbsent(dimensionId, k -> new HashMap<>())
            .computeIfAbsent(biomeId, k -> new HashSet<>())
            .add(modNamespace);
    }

    /**
     * Whitelist a specific entity type for a dimension.
     */
    public void whitelistEntityForDimension(ResourceLocation dimensionId, ResourceLocation entityId) {
        entityWhitelist.computeIfAbsent(dimensionId, k -> new HashSet<>()).add(entityId);
    }

    /**
     * Check if a specific entity is allowed in a dimension.
     *
     * For planet dimensions (which have entity-level entries via registerEntityWhitelist):
     *   Only allows specific entities we configured in PlanetGenerationRunner.
     *
     * For vanilla dimensions (Overworld/Nether/End, which only have namespace-level entries
     * via allowInVanillaDimensions/allowInOverworld/etc.):
     *   Falls back to namespace-level check, allowing all entities from that mod.
     *
     * @param dimensionId The dimension resource location
     * @param entityId The full entity resource location (e.g., born_in_chaos_v1:decrepit_skeleton)
     * @param modNamespace The mod namespace (for fallback to namespace-level check)
     * @return true if the entity is allowed to spawn in this dimension
     */
    public boolean isEntityAllowed(ResourceLocation dimensionId, ResourceLocation entityId, String modNamespace) {
        // If entity-level whitelist exists for this dimension, use it (planet dimensions)
        if (entityWhitelist.containsKey(dimensionId)) {
            return entityWhitelist.get(dimensionId).contains(entityId);
        }

        // Fall back to namespace-level (for vanilla dimensions where we whitelist whole namespaces)
        // Check dimension-level whitelist directly
        if (dimensionWhitelist.containsKey(dimensionId)) {
            return dimensionWhitelist.get(dimensionId).contains(modNamespace);
        }

        // Not whitelisted at any level
        return false;
    }

    /**
     * Check if a mod is allowed to spawn in a specific dimension and biome.
     *
     * @param dimensionId The dimension resource location
     * @param biomeId The biome resource location
     * @param modNamespace The mod namespace to check
     * @return true if the mod is allowed to spawn in this dimension+biome
     */
    public boolean isModAllowed(ResourceLocation dimensionId, ResourceLocation biomeId, String modNamespace) {
        // Check biome-specific whitelist first (highest priority)
        if (biomeWhitelist.containsKey(dimensionId)) {
            Map<ResourceLocation, Set<String>> biomeMods = biomeWhitelist.get(dimensionId);
            if (biomeMods.containsKey(biomeId)) {
                return biomeMods.get(biomeId).contains(modNamespace);
            }
        }

        // Fall back to dimension-level whitelist
        if (dimensionWhitelist.containsKey(dimensionId)) {
            return dimensionWhitelist.get(dimensionId).contains(modNamespace);
        }

        // Not whitelisted
        return false;
    }

    /**
     * Get all dimensions that whitelist a specific mod.
     */
    public Set<ResourceLocation> getDimensionsForMod(String modNamespace) {
        Set<ResourceLocation> dimensions = new HashSet<>();
        for (Map.Entry<ResourceLocation, Set<String>> entry : dimensionWhitelist.entrySet()) {
            if (entry.getValue().contains(modNamespace)) {
                dimensions.add(entry.getKey());
            }
        }
        return dimensions;
    }

    /**
     * Clear all whitelist data (useful for testing or reloading).
     */
    public void clear() {
        dimensionWhitelist.clear();
        biomeWhitelist.clear();
        entityWhitelist.clear();
    }

    /**
     * Get debug information about current whitelist state.
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Controlled Mods: ").append(controlledMods).append("\n");
        sb.append("Dimension Whitelists:\n");
        for (Map.Entry<ResourceLocation, Set<String>> entry : dimensionWhitelist.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("Biome-Specific Whitelists:\n");
        for (Map.Entry<ResourceLocation, Map<ResourceLocation, Set<String>>> dimEntry : biomeWhitelist.entrySet()) {
            sb.append("  Dimension ").append(dimEntry.getKey()).append(":\n");
            for (Map.Entry<ResourceLocation, Set<String>> biomeEntry : dimEntry.getValue().entrySet()) {
                sb.append("    ").append(biomeEntry.getKey()).append(": ").append(biomeEntry.getValue()).append("\n");
            }
        }
        sb.append("Entity Whitelists:\n");
        for (Map.Entry<ResourceLocation, Set<ResourceLocation>> entry : entityWhitelist.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue().size()).append(" entities\n");
        }
        return sb.toString();
    }
}
