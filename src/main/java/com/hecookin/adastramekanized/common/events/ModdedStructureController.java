package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

/**
 * Controls structure generation from modded mods to restrict them to specific dimensions.
 *
 * This system:
 * - Tracks which dimensions should allow structures from controlled mods
 * - Kobolds structures are controlled via biome tags (they check for forest/taiga biomes)
 * - MCDoom structures are controlled via their biome requirements
 *
 * NOTE: In NeoForge 1.21.1, structure generation is controlled via:
 * 1. Biome tags - structures check if they're allowed in specific biomes
 * 2. Structure sets - define spacing and allowed dimensions
 * 3. Custom planets should NOT include vanilla biome tags that trigger modded structures
 */
public class ModdedStructureController {

    // Map: Dimension ID → Set of allowed structure namespaces
    private static final Map<ResourceLocation, Set<String>> dimensionStructureWhitelist = new HashMap<>();

    // Map: Dimension ID → Set of specific allowed structure IDs
    private static final Map<ResourceLocation, Set<ResourceLocation>> dimensionSpecificStructures = new HashMap<>();

    // Set of mod namespaces whose structures are controlled
    private static final Set<String> controlledStructureMods = new HashSet<>();

    static {
        // Initialize with controlled structure mods
        controlledStructureMods.add("kobolds");  // Kobold dens and structures
        controlledStructureMods.add("ribbits");  // Ribbit swamp villages
    }

    /**
     * Add a mod namespace to structure control.
     * Structures from this mod will be blocked unless explicitly whitelisted.
     */
    public static void addControlledStructureMod(String modNamespace) {
        controlledStructureMods.add(modNamespace);
        AdAstraMekanized.LOGGER.info("Added '{}' to controlled structure mods", modNamespace);
    }

    /**
     * Whitelist all structures from a mod for a specific dimension.
     * Called automatically by PlanetMaker when structures are enabled.
     */
    public static void whitelistModStructures(ResourceLocation dimensionId, String modNamespace) {
        dimensionStructureWhitelist.computeIfAbsent(dimensionId, k -> new HashSet<>()).add(modNamespace);
        AdAstraMekanized.LOGGER.info("Whitelisted all structures from '{}' for dimension '{}'",
            modNamespace, dimensionId);
    }

    /**
     * Whitelist a specific structure for a dimension.
     * Use this for fine-grained control (e.g., only kobold_den, not kobold_den_pirate).
     */
    public static void whitelistStructure(ResourceLocation dimensionId, ResourceLocation structureId) {
        dimensionSpecificStructures.computeIfAbsent(dimensionId, k -> new HashSet<>()).add(structureId);
        AdAstraMekanized.LOGGER.info("Whitelisted structure '{}' for dimension '{}'",
            structureId, dimensionId);
    }

    /**
     * Check if a structure is allowed in a dimension.
     */
    private static boolean isStructureAllowed(ResourceLocation dimensionId, ResourceLocation structureId) {
        String structureNamespace = structureId.getNamespace();

        // Check if this mod's structures are controlled
        if (!controlledStructureMods.contains(structureNamespace)) {
            return true; // Not a controlled mod, allow structure
        }

        // Check specific structure whitelist first (highest priority)
        if (dimensionSpecificStructures.containsKey(dimensionId)) {
            if (dimensionSpecificStructures.get(dimensionId).contains(structureId)) {
                return true; // Explicitly whitelisted
            }
        }

        // Check mod-level whitelist
        if (dimensionStructureWhitelist.containsKey(dimensionId)) {
            if (dimensionStructureWhitelist.get(dimensionId).contains(structureNamespace)) {
                return true; // All structures from this mod are whitelisted
            }
        }

        // Not whitelisted
        return false;
    }

    /**
     * Check if structures are enabled for a dimension.
     * NOTE: Actual structure blocking happens via biome tag control.
     * This method is used to determine if we should generate structure-specific
     * biome tags for custom planets.
     */
    public static boolean areStructuresAllowed(ResourceLocation dimensionId, String modNamespace) {
        return isStructureAllowed(dimensionId, ResourceLocation.fromNamespaceAndPath(modNamespace, ""));
    }

    /**
     * Get debug information about structure whitelist configuration.
     */
    public static String getDebugInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Controlled Structure Mods: ").append(controlledStructureMods).append("\n");
        sb.append("Dimension Structure Whitelists:\n");
        for (Map.Entry<ResourceLocation, Set<String>> entry : dimensionStructureWhitelist.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("Specific Structure Whitelists:\n");
        for (Map.Entry<ResourceLocation, Set<ResourceLocation>> entry : dimensionSpecificStructures.entrySet()) {
            sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
