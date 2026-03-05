package com.hecookin.adastramekanized.common.commands;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.planets.Planet;
import com.hecookin.adastramekanized.api.planets.PlanetRegistry;
import com.hecookin.adastramekanized.common.blockentities.machines.OxygenDistributorBlockEntity;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import com.hecookin.adastramekanized.common.atmosphere.GlobalOxygenManager;
import com.hecookin.adastramekanized.common.planets.PlanetManager;
import com.hecookin.adastramekanized.common.teleportation.PlanetTeleportationSystem;
import com.hecookin.adastramekanized.integration.ModIntegrationManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;

/**
 * Enhanced planet commands with improved usability and error handling.
 *
 * Commands:
 * - /planet (lists all planets)
 * - /planet list (lists all planets)
 * - /planet tp <planet_name> (teleport to planet)
 * - /planet teleport <planet_name> (teleport to planet)
 * - /planet info <planet_name> (show planet details)
 * - /planet details <planet_name> (show planet details)
 *
 * Supports command-safe names (lowercase, no spaces), display names, and resource IDs.
 * Autocomplete suggestions use command-safe format (e.g., "atlantis", "fermi-9").
 */
public class PlanetDebugCommands {

    /**
     * Register planet debug commands
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("planet")
            .requires(source -> source.hasPermission(2)) // Require OP level 2
            .executes(PlanetDebugCommands::listPlanets) // Default to list when no subcommand
            .then(Commands.literal("list")
                .executes(PlanetDebugCommands::listPlanets))
            .then(Commands.literal("teleport")
                .then(Commands.argument("planet", StringArgumentType.string())
                    .suggests(PLANET_SUGGESTIONS)
                    .executes(PlanetDebugCommands::teleportToPlanet)))
            .then(Commands.literal("tp") // Alias for teleport
                .then(Commands.argument("planet", StringArgumentType.string())
                    .suggests(PLANET_SUGGESTIONS)
                    .executes(PlanetDebugCommands::teleportToPlanet)))
            .then(Commands.literal("info")
                .then(Commands.argument("planet", StringArgumentType.string())
                    .suggests(PLANET_SUGGESTIONS)
                    .executes(PlanetDebugCommands::showPlanetInfo)))
            .then(Commands.literal("details") // Alias for info
                .then(Commands.argument("planet", StringArgumentType.string())
                    .suggests(PLANET_SUGGESTIONS)
                    .executes(PlanetDebugCommands::showPlanetInfo)))
            .then(Commands.literal("debug")
                .then(Commands.literal("oxygen")
                    .executes(PlanetDebugCommands::debugOxygenDistributor)))
        );
    }

    /**
     * Suggestion provider for planet names
     */
    private static final SuggestionProvider<CommandSourceStack> PLANET_SUGGESTIONS =
        (context, builder) -> {
            PlanetRegistry registry = PlanetRegistry.getInstance();

            // Always include "earth" as an option to return to overworld
            java.util.List<String> suggestions = new java.util.ArrayList<>();
            suggestions.add("earth");

            if (registry.isDataLoaded()) {
                // Use command-safe versions of display names (lowercase, no spaces)
                registry.getAllPlanets().stream()
                    .map(planet -> toCommandSafeName(planet.displayName()))
                    .distinct()
                    .sorted()  // Sort alphabetically for better UX
                    .forEach(suggestions::add);
            }

            return SharedSuggestionProvider.suggest(suggestions, builder);
        };

    /**
     * Convert a display name to a command-safe name (lowercase, spaces to hyphens)
     */
    private static String toCommandSafeName(String displayName) {
        return displayName.toLowerCase().replace(" ", "-");
    }

    /**
     * Resolve a planet from either a display name, command-safe name, or resource location
     */
    private static Planet resolvePlanet(String input, PlanetManager manager) {
        // First try direct ID lookup
        ResourceLocation planetId = null;
        try {
            if (input.contains(":")) {
                planetId = ResourceLocation.parse(input);
            } else {
                planetId = ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, input);
            }

            Planet planet = manager.getPlanet(planetId);
            if (planet != null) {
                return planet;
            }
        } catch (Exception e) {
            // Continue to name-based search
        }

        PlanetRegistry registry = PlanetRegistry.getInstance();

        // Try to find by display name (case-insensitive)
        Planet byDisplayName = registry.getAllPlanets().stream()
            .filter(planet -> planet.displayName().equalsIgnoreCase(input))
            .findFirst()
            .orElse(null);

        if (byDisplayName != null) {
            return byDisplayName;
        }

        // Try to find by command-safe name
        return registry.getAllPlanets().stream()
            .filter(planet -> toCommandSafeName(planet.displayName()).equals(input.toLowerCase()))
            .findFirst()
            .orElse(null);
    }

    /**
     * List all available planets
     */
    private static int listPlanets(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlanetRegistry registry = PlanetRegistry.getInstance();
        PlanetManager manager = PlanetManager.getInstance();

        if (!manager.isReady()) {
            source.sendFailure(Component.literal("Planet system not ready. Manager status: " + manager.getStatus()));
            return 0;
        }

        var planets = registry.getAllPlanets();
        if (planets.isEmpty()) {
            source.sendFailure(Component.literal("No planets found in registry"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6=== Planet List (" + planets.size() + " planets) ==="), false);

        for (Planet planet : planets) {
            boolean dimensionLoaded = manager.isPlanetDimensionLoaded(planet.id());
            String status = dimensionLoaded ? "§a✓" : "§c✗";
            String habitableIcon = planet.isHabitable() ? "§2🌍" : "§4🪨";

            Component planetInfo = Component.literal(String.format(
                "%s %s §f%s §7(%s) - %s°C, %.1fg gravity %s",
                status,
                habitableIcon,
                planet.displayName(),
                planet.id(),
                Math.round(planet.properties().temperature()),
                planet.properties().gravity(),
                dimensionLoaded ? "§a[LOADED]" : "§c[NOT LOADED]"
            ));

            source.sendSuccess(() -> planetInfo, false);
        }

        source.sendSuccess(() -> Component.literal("§7Commands:"), false);
        source.sendSuccess(() -> Component.literal("  §f/planet tp <planet_name>§7 - Teleport to a planet"), false);
        source.sendSuccess(() -> Component.literal("  §f/planet info <planet_name>§7 - Show detailed planet information"), false);
        source.sendSuccess(() -> Component.literal("§7Tip: Use lowercase names with hyphens (e.g., 'atlantis', 'fermi-9')"), false);

        return planets.size();
    }

    /**
     * Teleport player to specified planet
     */
    private static int teleportToPlanet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String planetInput = StringArgumentType.getString(context, "planet");

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("§cThis command can only be used by players"));
            return 0;
        }

        // Special case: "earth" or "overworld" teleports back to the overworld
        if (planetInput.equalsIgnoreCase("earth") || planetInput.equalsIgnoreCase("overworld")) {
            return teleportToOverworld(player, source);
        }

        PlanetManager manager = PlanetManager.getInstance();
        if (!manager.isReady()) {
            source.sendFailure(Component.literal("§cPlanet system not ready. Try again in a moment."));
            return 0;
        }

        // Resolve planet using improved resolver
        Planet planet = resolvePlanet(planetInput, manager);
        if (planet == null) {
            source.sendFailure(Component.literal("§cPlanet not found: §f" + planetInput));
            source.sendFailure(Component.literal("§7Use '/planet list' to see available planets"));
            return 0;
        }

        // Check if dimension is loaded
        if (!manager.isPlanetDimensionLoaded(planet.id())) {
            source.sendFailure(Component.literal("§cPlanet dimension not loaded: §f" + planet.displayName()));
            source.sendFailure(Component.literal("§7The planet may still be generating. Try again in a moment."));
            return 0;
        }

        // Attempt teleportation
        source.sendSuccess(() -> Component.literal("§6Attempting teleportation to " + planet.displayName() + "..."), false);

        PlanetTeleportationSystem teleportSystem = PlanetTeleportationSystem.getInstance();

        CompletableFuture<PlanetTeleportationSystem.TeleportResult> teleportFuture =
            teleportSystem.teleportToAnyPlanet(player, planet.id());

        teleportFuture.whenComplete((result, throwable) -> {
            if (throwable != null) {
                AdAstraMekanized.LOGGER.error("Teleportation failed", throwable);
                source.sendFailure(Component.literal("§cTeleportation failed: " + throwable.getMessage()));
            } else if (result == PlanetTeleportationSystem.TeleportResult.SUCCESS) {
                source.sendSuccess(() -> Component.literal("§aTeleportation successful! Welcome to " + planet.displayName()), false);

                // Show planet info after teleportation
                if (!planet.isHabitable()) {
                    source.sendSuccess(() -> Component.literal("§c⚠ Warning: This planet is not habitable! You may need oxygen support."), false);
                }

                source.sendSuccess(() -> Component.literal(String.format(
                    "§7Planet Info: %.1f°C, %.1fg gravity, %s atmosphere",
                    planet.properties().temperature(),
                    planet.properties().gravity(),
                    planet.atmosphere().type().name().toLowerCase()
                )), false);
            } else {
                String errorMessage = switch (result) {
                    case PLANET_NOT_FOUND -> "Planet not found";
                    case DIMENSION_NOT_LOADED -> "Planet dimension not loaded";
                    case PLAYER_ERROR -> "Player error";
                    case SYSTEM_ERROR -> "System error";
                    default -> "Unknown error";
                };
                source.sendFailure(Component.literal("§cTeleportation failed: " + errorMessage));
            }
        });

        return 1;
    }

    /**
     * Teleport player back to the Overworld (Earth)
     */
    private static int teleportToOverworld(ServerPlayer player, CommandSourceStack source) {
        ServerLevel overworld = player.server.getLevel(Level.OVERWORLD);
        if (overworld == null) {
            source.sendFailure(Component.literal("§cCould not find the Overworld dimension"));
            return 0;
        }

        // If player is already in overworld, just notify them
        if (player.level().dimension() == Level.OVERWORLD) {
            source.sendSuccess(() -> Component.literal("§7You are already on Earth (Overworld)"), false);
            return 1;
        }

        source.sendSuccess(() -> Component.literal("§6Returning to Earth..."), false);

        // Use player's current X/Z coordinates, find safe Y
        double x = player.getX();
        double z = player.getZ();
        int surfaceY = overworld.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);

        // Teleport to overworld at surface level
        player.teleportTo(overworld, x, surfaceY + 1, z, player.getYRot(), player.getXRot());

        source.sendSuccess(() -> Component.literal("§aWelcome back to Earth!"), false);
        return 1;
    }

    /**
     * Show detailed information about a planet
     */
    private static int showPlanetInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        String planetInput = StringArgumentType.getString(context, "planet");

        PlanetManager manager = PlanetManager.getInstance();
        if (!manager.isReady()) {
            source.sendFailure(Component.literal("§cPlanet system not ready. Try again in a moment."));
            return 0;
        }

        // Resolve planet using improved resolver
        Planet planet = resolvePlanet(planetInput, manager);
        if (planet == null) {
            source.sendFailure(Component.literal("§cPlanet not found: §f" + planetInput));
            source.sendFailure(Component.literal("§7Use '/planet list' to see available planets"));
            return 0;
        }

        // Display detailed planet information
        boolean dimensionLoaded = manager.isPlanetDimensionLoaded(planet.id());
        String habitableIcon = planet.isHabitable() ? "§2🌍" : "§4🪨";

        source.sendSuccess(() -> Component.literal("§6=== " + habitableIcon + " " + planet.displayName() + " ==="), false);
        source.sendSuccess(() -> Component.literal("§7ID: §f" + planet.id()), false);
        source.sendSuccess(() -> Component.literal("§7Dimension: §f" + planet.getDimensionLocation() + (dimensionLoaded ? " §a[LOADED]" : " §c[NOT LOADED]")), false);

        // Physical properties
        source.sendSuccess(() -> Component.literal("§6Physical Properties:"), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Temperature: §f%.1f°C", planet.properties().temperature())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Gravity: §f%.1fg", planet.properties().gravity())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Day Length: §f%.1f hours", planet.properties().dayLength())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Orbit Distance: §f%d million km", planet.properties().orbitDistance())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Moons: §f%d", planet.properties().moonCount())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Rings: §f%s", planet.properties().hasRings() ? "Yes" : "No")), false);

        // Atmosphere
        source.sendSuccess(() -> Component.literal("§6Atmosphere:"), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Type: §f%s", planet.atmosphere().type().name().toLowerCase())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Pressure: §f%.1f atm", planet.atmosphere().pressure())), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Oxygen: §f%.1f%%", planet.atmosphere().oxygenLevel() * 100)), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Breathable: §f%s", planet.atmosphere().breathable() ? "Yes" : "No")), false);
        source.sendSuccess(() -> Component.literal(String.format("  §7Life Support Required: §f%s", planet.atmosphere().requiresLifeSupport() ? "Yes" : "No")), false);

        // Habitability
        String habitabilityColor = planet.isHabitable() ? "§a" : "§c";
        source.sendSuccess(() -> Component.literal("§6Habitability: " + habitabilityColor + (planet.isHabitable() ? "Habitable" : "Hostile")), false);

        return 1;
    }

    /**
     * Debug oxygen distributor functionality
     */
    private static int debugOxygenDistributor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel level = player.serverLevel();
        BlockPos pos = player.blockPosition();

        // Find nearest oxygen distributor
        BlockEntity be = null;
        BlockPos distributorPos = null;
        for (int x = -10; x <= 10; x++) {
            for (int y = -5; y <= 5; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockEntity check = level.getBlockEntity(checkPos);
                    if (check instanceof OxygenDistributorBlockEntity) {
                        be = check;
                        distributorPos = checkPos;
                        break;
                    }
                }
                if (be != null) break;
            }
            if (be != null) break;
        }

        CommandSourceStack source = context.getSource();

        if (be instanceof ImprovedOxygenDistributor distributor) {
            final BlockPos finalPos = distributorPos;
            source.sendSuccess(() -> Component.literal("§6=== Improved Oxygen Distributor Debug ==="), false);
            source.sendSuccess(() -> Component.literal("§7Position: §f" + finalPos), false);
            source.sendSuccess(() -> Component.literal("§7Active: " + (distributor.isActive() ? "§aYes" : "§cNo")), false);
            source.sendSuccess(() -> Component.literal("§7Energy: §f" + distributor.getEnergyStorage().getEnergyStored() + "/" + distributor.getEnergyStorage().getMaxEnergyStored() + " FE"), false);
            source.sendSuccess(() -> Component.literal("§7Oxygen: §f" + distributor.getOxygenTank().getStored() + "/" + distributor.getOxygenTank().getCapacity() + " mB"), false);
            source.sendSuccess(() -> Component.literal("§7Current Radius: §a" + distributor.getCurrentRadius() + " blocks (dynamic expansion)"), false);
            source.sendSuccess(() -> Component.literal("§7Oxygenated Blocks: §f" + distributor.getOxygenatedBlockCount() + "/" + distributor.getMaxOxygenBlocks()), false);
            source.sendSuccess(() -> Component.literal("§7Efficiency: §f" + String.format("%.1f%%", distributor.getEfficiency())), false);

            // Check global oxygen manager
            GlobalOxygenManager globalManager = GlobalOxygenManager.getInstance();
            int totalBlocksInDim = globalManager.getTotalOxygenBlocks(player.level().dimension());
            source.sendSuccess(() -> Component.literal("§7Total Oxygen Blocks in Dimension: §f" + totalBlocksInDim), false);

            source.sendSuccess(() -> Component.literal("§6Features:"), false);
            source.sendSuccess(() -> Component.literal("§a✓ Dynamic radius expansion (starts at 3, +1 every 10 ticks)"), false);
            source.sendSuccess(() -> Component.literal("§a✓ Ring-based priority claiming (closer blocks first)"), false);
            source.sendSuccess(() -> Component.literal("§a✓ Respects other distributors' boundaries"), false);
            source.sendSuccess(() -> Component.literal("§a✓ 100-tick pathfinding cache"), false);
            source.sendSuccess(() -> Component.literal("§a✓ Treats claimed blocks as walls"), false);
        } else {
            source.sendFailure(Component.literal("§cNo oxygen distributor found within 10 blocks"));
            source.sendSuccess(() -> Component.literal("§7Place an oxygen distributor nearby and try again"), false);
        }

        return 1;
    }
}