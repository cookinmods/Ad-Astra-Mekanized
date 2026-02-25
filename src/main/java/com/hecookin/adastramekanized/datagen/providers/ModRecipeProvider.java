package com.hecookin.adastramekanized.datagen.providers;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import com.hecookin.adastramekanized.common.registry.ModItems;
import com.hecookin.adastramekanized.datagen.builders.NasaWorkbenchRecipeBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.concurrent.CompletableFuture;

/**
 * Main recipe provider for Ad Astra Mekanized.
 * Handles generation of all recipe types including NASA Workbench, Create integration, and standard recipes.
 */
public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    private Item getIronSheet() {
        return ModItems.IRON_SHEET.get();
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        AdAstraMekanized.LOGGER.info("Building Ad Astra Mekanized recipes...");

        // NASA Workbench Recipes
        buildNasaWorkbenchRecipes(recipeOutput);

        // Create Integration Recipes
        buildCreateRecipes(recipeOutput);

        // Standard Crafting Recipes
        buildCraftingRecipes(recipeOutput);

        // Smelting and Blasting Recipes
        buildSmeltingRecipes(recipeOutput);
        buildBlastingRecipes(recipeOutput);

        AdAstraMekanized.LOGGER.info("Finished building Ad Astra Mekanized recipes");
    }

    private void buildNasaWorkbenchRecipes(RecipeOutput recipeOutput) {
        AdAstraMekanized.LOGGER.info("Building NASA Workbench recipes...");

        // ========== BASIC STEEL TIER (T1) - No planet drops required ==========

        // Fan (prerequisite for engines) - 10 items
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModItems.FAN.get(), 1)
                .addIngredient(ModItems.IRON_ROD.get(), 1)
                .addIngredient(ModItems.STEEL_SHEET.get(), 2)
                .addIngredient(ModItems.STEEL_ROD.get(), 2)
            .addIngredient(ModItems.STEEL_SHEET.get(), 2)
            .addIngredient(ModItems.IRON_ROD.get(), 4)
            .unlockedBy("has_steel_rod", has(ModItems.STEEL_ROD.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "fan"));

        // Engine Frame (prerequisite for steel engine) - 10 items
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModItems.ENGINE_FRAME.get(), 1)
            .addIngredient(ModItems.STEEL_SHEET.get(), 1)
                .addIngredient(Items.REDSTONE_BLOCK, 2)
                .addIngredient(ModItems.STEEL_ROD.get(), 2)
            .addIngredient(ModItems.STEEL_SHEET.get(), 2)
            .unlockedBy("has_steel_rod", has(ModItems.STEEL_ROD.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "engine_frame"));

        // Gas Tank (prerequisite for steel tank) - 10 items
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModItems.GAS_TANK.get(), 1)
                .addIngredient(ModItems.IRON_ROD.get())
                .addIngredient(getIronSheet())
            .addIngredient(ModItems.STEEL_ROD.get())
            .addIngredient(ModItems.STEEL_SHEET.get())
                .addIngredient(ModItems.STEEL_ROD.get())
                .addIngredient(ModItems.STEEL_SHEET.get(), 2)

                .unlockedBy("has_steel_sheet", has(ModItems.STEEL_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "gas_tank"));

        // Rocket Fin - 10 items
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.TRANSPORTATION, ModItems.ROCKET_FIN.get(), 1)
            .addIngredient(ModItems.STEEL_SHEET.get(), 5)
            .addIngredient(ModItems.STEEL_ROD.get(), 3)
            .addIngredient(ModItems.IRON_ROD.get(),2)
                .addIngredient(ModItems.STEEL_ROD.get(), 1)

                .unlockedBy("has_steel_sheet", has(ModItems.STEEL_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "rocket_fin"));

        // Rocket Nose Cone - 10 items
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.TRANSPORTATION, ModItems.ROCKET_NOSE_CONE.get(), 1)
            .addIngredient(Items.LIGHTNING_ROD)
            .addIngredient(ModItems.STEEL_SHEET.get(), 2)
            .addIngredient(ModItems.STEEL_ROD.get(), 2)
            .addIngredient(Items.REDSTONE, 2)
            .addIngredient(ModItems.STEEL_SHEET.get(), 4)
                .unlockedBy("has_lightning_rod", has(Items.LIGHTNING_ROD))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "rocket_nose_cone"));

        // Steel Engine - 10 items
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.TRANSPORTATION, ModItems.STEEL_ENGINE.get(), 1)
                .addIngredient(Items.REDSTONE_BLOCK, 1)
            .addIngredient(ModItems.STEEL_ROD.get(), 2)
                .addIngredient(ModItems.ENGINE_FRAME.get())
                .addIngredient(ModItems.ENGINE_FRAME.get())
            .addIngredient(ModItems.FAN.get())
            .addIngredient(ModItems.FAN.get())
            .addIngredient(ModItems.STEEL_SHEET.get(), 4)

                .unlockedBy("has_engine_frame", has(ModItems.ENGINE_FRAME.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "steel_engine"));

        // Steel Tank - 10 items
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModItems.STEEL_TANK.get(), 1)
                .addIngredient(ModItems.GAS_TANK.get())
                .addIngredient(getIronSheet())
                .addIngredient(ModItems.STEEL_ROD.get())
                .addIngredient(ModItems.STEEL_SHEET.get())
                .addIngredient(ModItems.STEEL_ROD.get())
                .addIngredient(ModItems.STEEL_ROD.get())
                .addIngredient(ModItems.STEEL_ROD.get())
                .addIngredient(ModItems.STEEL_SHEET.get(), 4)
            .unlockedBy("has_gas_tank", has(ModItems.GAS_TANK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "steel_tank"));

        // ========== DESH TIER (T2) - Requires desh nuggets from Moon exploration ==========

        // Large Gas Tank (upgraded gas tank) - 10 items, gated by desh nuggets
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModItems.LARGE_GAS_TANK.get(), 1)
                .addIngredient(ModItems.STEEL_SHEET.get(), 3)
                .addIngredient(ModItems.GAS_TANK.get(), 2)
                .addIngredient(ModItems.GAS_TANK.get(),2)
                .addIngredient(ModItems.STEEL_SHEET.get(),4)
            .unlockedBy("has_desh_nugget", has(ModItems.DESH_NUGGET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "large_gas_tank"));

        // Oxygen Gear - 10 items, requires desh nuggets
        // Top-left slot uses Mekanism Electrolytic Core (oxygen separation component)
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModItems.OXYGEN_GEAR.get(), 1)
            .addIngredient(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mekanism", "electrolytic_core")), 1)
            .addIngredient(ModItems.DESH_SHEET.get(), 2)
            .addIngredient(ModItems.GAS_TANK.get(), 2)
            .addIngredient(ModItems.STEEL_ROD.get(),2)
            .addIngredient(ModItems.STEEL_SHEET.get(),4)
            .unlockedBy("has_gas_tank", has(ModItems.GAS_TANK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "oxygen_gear"));

        // Desh Engine (upgrade from steel engine) - 10 items, requires desh nuggets
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.TRANSPORTATION, ModItems.DESH_ENGINE.get(), 1)
                .addIngredient(ModItems.STEEL_ENGINE.get(), 1)
                .addIngredient(ModItems.DESH_SHEET.get(), 2)
                .addIngredient(ModItems.ENGINE_FRAME.get())
                .addIngredient(ModItems.ENGINE_FRAME.get())
                .addIngredient(ModItems.FAN.get())
                .addIngredient(ModItems.FAN.get())
                .addIngredient(ModItems.DESH_SHEET.get(), 4)
            .unlockedBy("has_desh_sheet", has(ModItems.DESH_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "desh_engine"));

        // Desh Tank (upgrade from steel tank) - 10 items, requires desh nuggets
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModItems.DESH_TANK.get(), 1)
                .addIngredient(ModItems.STEEL_TANK.get())
                .addIngredient(ModItems.STEEL_SHEET.get())
                .addIngredient(ModItems.STEEL_ROD.get())
                .addIngredient(ModItems.DESH_SHEET.get())
                .addIngredient(ModItems.STEEL_ROD.get())
                .addIngredient(ModItems.DESH_SHEET.get())
                .addIngredient(ModItems.STEEL_ROD.get())
                .addIngredient(ModItems.DESH_SHEET.get(), 4)
            .unlockedBy("has_desh_sheet", has(ModItems.DESH_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "desh_tank"));

        // ========== OSTRUM TIER (T3) - Requires ostrum nuggets from Mars exploration ==========

        // Ostrum Engine (upgrade from desh engine) - 10 items, requires ostrum nuggets
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.TRANSPORTATION, ModItems.OSTRUM_ENGINE.get(), 1)
                .addIngredient(ModItems.DESH_ENGINE.get(), 1)
                .addIngredient(ModItems.ETRIONIC_CORE.get(), 2)
                .addIngredient(ModItems.ENGINE_FRAME.get())
                .addIngredient(ModItems.ENGINE_FRAME.get())
                .addIngredient(ModItems.FAN.get())
                .addIngredient(ModItems.FAN.get())
                .addIngredient(ModItems.OSTRUM_SHEET.get(), 4)
            .unlockedBy("has_ostrum_sheet", has(ModItems.OSTRUM_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "ostrum_engine"));

        // Ostrum Tank (upgrade from desh tank) - 10 items, requires ostrum nuggets
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModItems.OSTRUM_TANK.get(), 1)
                .addIngredient(ModItems.DESH_TANK.get())
                .addIngredient(ModItems.DESH_SHEET.get())
                .addIngredient(ModItems.ETRIUM_ROD.get())
                .addIngredient(ModItems.OSTRUM_SHEET.get())
                .addIngredient(ModItems.STEEL_ROD.get())
                .addIngredient(ModItems.OSTRUM_SHEET.get())
                .addIngredient(ModItems.STEEL_ROD.get())
                .addIngredient(ModItems.OSTRUM_SHEET.get(), 4)
            .unlockedBy("has_ostrum_sheet", has(ModItems.OSTRUM_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "ostrum_tank"));

        // ========== CALORITE TIER (T4) - Requires calorite nuggets from Venus exploration ==========

        // Calorite Engine (upgrade from ostrum engine) - 10 items, requires calorite nuggets
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.TRANSPORTATION, ModItems.CALORITE_ENGINE.get(), 1)
                .addIngredient(ModItems.DESH_ENGINE.get(), 1)
                .addIngredient(ModItems.ETRIONIC_CAPACITOR.get(), 2)
                .addIngredient(ModItems.ENGINE_FRAME.get())
                .addIngredient(ModItems.ENGINE_FRAME.get())
                .addIngredient(ModItems.FAN.get())
                .addIngredient(ModItems.FAN.get())
                .addIngredient(ModItems.OSTRUM_SHEET.get(), 4)
            .unlockedBy("has_calorite_nugget", has(ModItems.CALORITE_NUGGET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "calorite_engine"));

        // Calorite Tank (upgrade from ostrum tank) - 10 items, requires calorite nuggets
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModItems.CALORITE_TANK.get(), 1)
                .addIngredient(ModItems.OSTRUM_TANK.get())
                .addIngredient(ModItems.OSTRUM_SHEET.get())
                .addIngredient(ModItems.ETRIUM_ROD.get())
                .addIngredient(ModItems.CALORITE_SHEET.get())
                .addIngredient(ModItems.ETRIUM_ROD.get())
                .addIngredient(ModItems.CALORITE_SHEET.get())
                .addIngredient(ModItems.STEEL_ROD.get())
                .addIngredient(ModItems.CALORITE_SHEET.get(), 4)
            .unlockedBy("has_calorite_sheet", has(ModItems.CALORITE_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "calorite_tank"));

        // ========== ETRIUM TIER (Endgame) - Requires etrium nuggets from planet mob drops ==========

        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.REDSTONE, ModItems.ETRIONIC_CORE.get(), 1)
                .addIngredient(ModItems.ETRIUM_SHEET.get())
                .addIngredient(Items.REDSTONE_LAMP, 2)
                .addIngredient(ModItems.ETRIUM_ROD.get(), 2)
                .addIngredient(ModItems.ETRIUM_SHEET.get(), 2)
                .addIngredient(ModItems.ETRIUM_NUGGET.get(), 4)
                .unlockedBy("has_etrium_nugget", has(ModItems.ETRIUM_NUGGET.get()))
                .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "etrionic_core"));

        // Etrionic Capacitor (endgame component) - 10 items, requires etrium nuggets from planet mobs
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.REDSTONE, ModItems.ETRIONIC_CAPACITOR.get(), 1)
                .addIngredient(ModItems.ETRIONIC_CORE.get())
                .addIngredient(Items.ENDER_EYE, 2)
            .addIngredient(ModItems.ETRIUM_ROD.get(), 4)
                .addIngredient(ModItems.ETRIUM_NUGGET.get(), 4)

                .unlockedBy("has_etrium_core", has(ModItems.ETRIONIC_CORE.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "etrionic_capacitor"));

        // ========== NETWORK & OXYGEN COMPONENTS ==========

        // Wireless Power Relay - 9 items
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModBlocks.WIRELESS_POWER_RELAY.get(), 1)
            .addIngredient(ModItems.STEEL_SHEET.get())
            .addIngredient(Items.LIGHTNING_ROD).addIngredient(Items.LIGHTNING_ROD)

            .addIngredient(Items.REDSTONE_BLOCK, 2)
            .addIngredient(ModItems.ETRIONIC_CORE.get())          .addIngredient(ModItems.ETRIONIC_CORE.get())
                .addIngredient(ModItems.STEEL_SHEET.get())
            .addIngredient(ModItems.STEEL_SHEET.get()).addIngredient(ModItems.STEEL_SHEET.get()).addIngredient(ModItems.STEEL_SHEET.get())
            .unlockedBy("has_etrionic_core", has(ModItems.ETRIONIC_CORE.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "wireless_power_relay"));

        // Oxygen Network Controller - 9 items
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModItems.OXYGEN_NETWORK_CONTROLLER.get(), 1)
                .addIngredient(Items.LIGHTNING_ROD)

                .addIngredient(ModItems.FAN.get(),2)
            .addIngredient(ModItems.DESH_SHEET.get())
            .addIngredient(ModItems.ETRIONIC_CAPACITOR.get(),1)
                .addIngredient(Items.COMPARATOR)
                .addIngredient(Items.COMPARATOR)
            .unlockedBy("has_etrionic_core", has(ModItems.ETRIONIC_CAPACITOR.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "oxygen_network_controller"));

        // Redstone Toggle Relay - 9 items
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.REDSTONE, ModBlocks.REDSTONE_TOGGLE_RELAY.get(), 1)
            .addIngredient(ModItems.STEEL_SHEET.get())
            .addIngredient(Items.REPEATER)
            .addIngredient(Items.COMPARATOR, 2)
                .addIngredient(Items.REPEATER)
                .addIngredient(ModItems.FAN.get(),2)
                .addIngredient(ModItems.STEEL_SHEET.get())
            .addIngredient(ModItems.STEEL_SHEET.get()).addIngredient(ModItems.STEEL_SHEET.get())
                .addIngredient(ModItems.STEEL_SHEET.get())
            .unlockedBy("has_fan", has(ModItems.FAN.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "redstone_toggle_relay"));

        // Oxygen Distributor - 9 items
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModBlocks.OXYGEN_DISTRIBUTOR.get(), 1)
            .addIngredient(ModItems.FAN.get())
            .addIngredient(ModItems.GAS_TANK.get()).addIngredient(ModItems.DESH_SHEET.get()).addIngredient(ModItems.DESH_SHEET.get())
            .addIngredient(ModItems.GAS_TANK.get()).addIngredient(ModItems.OXYGEN_GEAR.get())

            .addIngredient(ModItems.DESH_SHEET.get()).addIngredient(ModItems.FAN.get())
            .addIngredient(ModItems.FAN.get())
            .addIngredient(ModItems.FAN.get()).addIngredient(ModItems.FAN.get())
            .unlockedBy("has_oxygen_gear", has(ModItems.OXYGEN_GEAR.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "oxygen_distributor"));

        // Gravity Normalizer - 10 items (uses argon/etrium for gravity manipulation theme)
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.MISC, ModBlocks.GRAVITY_NORMALIZER.get(), 1)
            .addIngredient(ModItems.ETRIONIC_CAPACITOR.get())
            .addIngredient(ModItems.LARGE_GAS_TANK.get())
            .addIngredient(ModItems.OSTRUM_SHEET.get())
            .addIngredient(ModItems.OSTRUM_SHEET.get())
            .addIngredient(ModItems.LARGE_GAS_TANK.get())
            .addIngredient(ModItems.LARGE_GAS_TANK.get())
            .addIngredient(ModItems.OSTRUM_SHEET.get())
            .addIngredient(ModItems.FAN.get())
            .addIngredient(ModItems.FAN.get())
            .addIngredient(ModItems.FAN.get())
            .addIngredient(ModItems.FAN.get())
            .unlockedBy("has_etrionic_capacitor", has(ModItems.ETRIONIC_CAPACITOR.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "gravity_normalizer"));

        // ========== ROCKETS ==========

        // Tier 1 Rocket - Steel-based, basic lunar rocket (14 ingredient slots)
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.TRANSPORTATION, ModItems.TIER_1_ROCKET.get(), 1)
            .addIngredient(ModItems.ROCKET_NOSE_CONE.get())           // Slot 0: Nose cone (top)
            .addIngredient(ModBlocks.STEEL_SHEETBLOCK.get())          // Slot 1: Body left-top
            .addIngredient(ModBlocks.STEEL_SHEETBLOCK.get())          // Slot 2: Body right-top
            .addIngredient(ModBlocks.STEEL_SHEETBLOCK.get())          // Slot 3: Body left-middle
            .addIngredient(ModBlocks.STEEL_SHEETBLOCK.get())          // Slot 4: Body right-middle
            .addIngredient(ModBlocks.STEEL_SHEETBLOCK.get())          // Slot 5: Body left-bottom
            .addIngredient(ModBlocks.STEEL_SHEETBLOCK.get())          // Slot 6: Body right-bottom
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 7: Fin left
            .addIngredient(ModItems.STEEL_TANK.get())                 // Slot 8: Tank left
            .addIngredient(ModItems.STEEL_TANK.get())                 // Slot 9: Tank right
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 10: Fin right
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 11: Fin bottom-left
            .addIngredient(ModItems.STEEL_ENGINE.get())               // Slot 12: Engine bottom-center
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 13: Fin bottom-right
            .unlockedBy("has_steel_engine", has(ModItems.STEEL_ENGINE.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "tier_1_rocket"));

        // Tier 2 Rocket - Desh upgrade (14 ingredient slots)
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.TRANSPORTATION, ModItems.TIER_2_ROCKET.get(), 1)
            .addIngredient(ModItems.ROCKET_NOSE_CONE.get())           // Slot 0: Nose cone (top)
            .addIngredient(ModBlocks.DESH_SHEETBLOCK.get())           // Slot 1: Body left-top
            .addIngredient(ModBlocks.DESH_SHEETBLOCK.get())           // Slot 2: Body right-top
            .addIngredient(ModBlocks.DESH_SHEETBLOCK.get())           // Slot 3: Body left-middle
            .addIngredient(ModBlocks.DESH_SHEETBLOCK.get())           // Slot 4: Body right-middle
            .addIngredient(ModBlocks.DESH_SHEETBLOCK.get())           // Slot 5: Body left-bottom
            .addIngredient(ModBlocks.DESH_SHEETBLOCK.get())           // Slot 6: Body right-bottom
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 7: Fin left
            .addIngredient(ModItems.DESH_TANK.get())                  // Slot 8: Tank left
            .addIngredient(ModItems.DESH_TANK.get())                  // Slot 9: Tank right
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 10: Fin right
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 11: Fin bottom-left
            .addIngredient(ModItems.DESH_ENGINE.get())                // Slot 12: Engine bottom-center
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 13: Fin bottom-right
            .unlockedBy("has_desh_engine", has(ModItems.DESH_ENGINE.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "tier_2_rocket"));

        // Tier 3 Rocket - Ostrum upgrade (14 ingredient slots)
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.TRANSPORTATION, ModItems.TIER_3_ROCKET.get(), 1)
            .addIngredient(ModItems.ROCKET_NOSE_CONE.get())           // Slot 0: Nose cone (top)
            .addIngredient(ModBlocks.OSTRUM_SHEETBLOCK.get())         // Slot 1: Body left-top
            .addIngredient(ModBlocks.OSTRUM_SHEETBLOCK.get())         // Slot 2: Body right-top
            .addIngredient(ModBlocks.OSTRUM_SHEETBLOCK.get())         // Slot 3: Body left-middle
            .addIngredient(ModBlocks.OSTRUM_SHEETBLOCK.get())         // Slot 4: Body right-middle
            .addIngredient(ModBlocks.OSTRUM_SHEETBLOCK.get())         // Slot 5: Body left-bottom
            .addIngredient(ModBlocks.OSTRUM_SHEETBLOCK.get())         // Slot 6: Body right-bottom
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 7: Fin left
            .addIngredient(ModItems.OSTRUM_TANK.get())                // Slot 8: Tank left
            .addIngredient(ModItems.OSTRUM_TANK.get())                // Slot 9: Tank right
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 10: Fin right
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 11: Fin bottom-left
            .addIngredient(ModItems.OSTRUM_ENGINE.get())              // Slot 12: Engine bottom-center
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 13: Fin bottom-right
            .unlockedBy("has_ostrum_engine", has(ModItems.OSTRUM_ENGINE.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "tier_3_rocket"));

        // Tier 4 Rocket - Calorite upgrade with capacitor (14 ingredient slots)
        NasaWorkbenchRecipeBuilder.builder(RecipeCategory.TRANSPORTATION, ModItems.TIER_4_ROCKET.get(), 1)
            .addIngredient(ModItems.ROCKET_NOSE_CONE.get())           // Slot 0: Nose cone (top)
            .addIngredient(ModBlocks.CALORITE_SHEETBLOCK.get())       // Slot 1: Body left-top
            .addIngredient(ModBlocks.CALORITE_SHEETBLOCK.get())       // Slot 2: Body right-top
            .addIngredient(ModBlocks.CALORITE_SHEETBLOCK.get())       // Slot 3: Body left-middle
            .addIngredient(ModBlocks.CALORITE_SHEETBLOCK.get())       // Slot 4: Body right-middle
            .addIngredient(ModBlocks.CALORITE_SHEETBLOCK.get())       // Slot 5: Body left-bottom
            .addIngredient(ModBlocks.CALORITE_SHEETBLOCK.get())       // Slot 6: Body right-bottom
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 7: Fin left
            .addIngredient(ModItems.CALORITE_TANK.get())              // Slot 8: Tank left
            .addIngredient(ModItems.CALORITE_TANK.get())              // Slot 9: Tank right
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 10: Fin right
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 11: Fin bottom-left
            .addIngredient(ModItems.CALORITE_ENGINE.get())            // Slot 12: Engine bottom-center
            .addIngredient(ModItems.ROCKET_FIN.get())                 // Slot 13: Fin bottom-right
            .unlockedBy("has_calorite_engine", has(ModItems.CALORITE_ENGINE.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "tier_4_rocket"));

        // Launch Pad - Simple crafting recipe (also added to buildCraftingRecipes for standard crafting)
        AdAstraMekanized.LOGGER.info("NASA Workbench recipes complete - {} rockets added", 4);
    }

    private void buildCreateRecipes(RecipeOutput recipeOutput) {
        AdAstraMekanized.LOGGER.info("Building Create integration recipes...");

        // Steel sheet pressing (using Mekanism steel ingot)
        // We'll use a simple approach - generate the JSON directly
        // Note: These recipes will only work when Create is installed due to conditional loading

        // For now, we'll generate these manually in the resources folder
        // since Create recipe generation requires special handling
        // TODO: Add steel sheet pressing recipe
        // TODO: Add steel rod cutting recipe
    }

    private void buildCraftingRecipes(RecipeOutput recipeOutput) {
        AdAstraMekanized.LOGGER.info("Building standard crafting recipes...");

        // Sheet Block Compression (9 sheets -> 1 block)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.IRON_SHEETBLOCK.get())
            .requires(getIronSheet(), 9)
            .unlockedBy("has_iron_sheet", has(getIronSheet()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "iron_sheetblock"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_SHEETBLOCK.get())
            .requires(ModItems.STEEL_SHEET.get(), 9)
            .unlockedBy("has_steel_sheet", has(ModItems.STEEL_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "steel_sheetblock"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ETRIUM_SHEETBLOCK.get())
            .requires(ModItems.ETRIUM_SHEET.get(), 9)
            .unlockedBy("has_etrium_sheet", has(ModItems.ETRIUM_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "etrium_sheetblock"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DESH_SHEETBLOCK.get())
            .requires(ModItems.DESH_SHEET.get(), 9)
            .unlockedBy("has_desh_sheet", has(ModItems.DESH_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "desh_sheetblock"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.OSTRUM_SHEETBLOCK.get())
            .requires(ModItems.OSTRUM_SHEET.get(), 9)
            .unlockedBy("has_ostrum_sheet", has(ModItems.OSTRUM_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "ostrum_sheetblock"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CALORITE_SHEETBLOCK.get())
            .requires(ModItems.CALORITE_SHEET.get(), 9)
            .unlockedBy("has_calorite_sheet", has(ModItems.CALORITE_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "calorite_sheetblock"));

        // Sheet Block Decompression (1 block -> 9 sheets)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, getIronSheet(), 9)
            .requires(ModBlocks.IRON_SHEETBLOCK.get())
            .unlockedBy("has_iron_sheetblock", has(ModBlocks.IRON_SHEETBLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "iron_sheet_from_sheetblock"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.STEEL_SHEET.get(), 9)
            .requires(ModBlocks.STEEL_SHEETBLOCK.get())
            .unlockedBy("has_steel_sheetblock", has(ModBlocks.STEEL_SHEETBLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "steel_sheet_from_sheetblock"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ETRIUM_SHEET.get(), 9)
            .requires(ModBlocks.ETRIUM_SHEETBLOCK.get())
            .unlockedBy("has_etrium_sheetblock", has(ModBlocks.ETRIUM_SHEETBLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "etrium_sheet_from_sheetblock"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DESH_SHEET.get(), 9)
            .requires(ModBlocks.DESH_SHEETBLOCK.get())
            .unlockedBy("has_desh_sheetblock", has(ModBlocks.DESH_SHEETBLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "desh_sheet_from_sheetblock"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.OSTRUM_SHEET.get(), 9)
            .requires(ModBlocks.OSTRUM_SHEETBLOCK.get())
            .unlockedBy("has_ostrum_sheetblock", has(ModBlocks.OSTRUM_SHEETBLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "ostrum_sheet_from_sheetblock"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CALORITE_SHEET.get(), 9)
            .requires(ModBlocks.CALORITE_SHEETBLOCK.get())
            .unlockedBy("has_calorite_sheetblock", has(ModBlocks.CALORITE_SHEETBLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "calorite_sheet_from_sheetblock"));

        // ========== Ingot/Nugget/Block Compression/Decompression ==========

        // Etrium: 9 nuggets <-> 1 ingot, 9 ingots <-> 1 block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ETRIUM_INGOT.get())
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.ETRIUM_NUGGET.get())
            .unlockedBy("has_etrium_nugget", has(ModItems.ETRIUM_NUGGET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "etrium_ingot_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ETRIUM_NUGGET.get(), 9)
            .requires(ModItems.ETRIUM_INGOT.get())
            .unlockedBy("has_etrium_ingot", has(ModItems.ETRIUM_INGOT.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "etrium_nugget_from_ingot"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ETRIUM_BLOCK.get())
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.ETRIUM_INGOT.get())
            .unlockedBy("has_etrium_ingot", has(ModItems.ETRIUM_INGOT.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "etrium_block"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ETRIUM_INGOT.get(), 9)
            .requires(ModBlocks.ETRIUM_BLOCK.get())
            .unlockedBy("has_etrium_block", has(ModBlocks.ETRIUM_BLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "etrium_ingot_from_block"));

        // Desh: 9 nuggets <-> 1 ingot, 9 ingots <-> 1 block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DESH_INGOT.get())
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.DESH_NUGGET.get())
            .unlockedBy("has_desh_nugget", has(ModItems.DESH_NUGGET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "desh_ingot_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DESH_NUGGET.get(), 9)
            .requires(ModItems.DESH_INGOT.get())
            .unlockedBy("has_desh_ingot", has(ModItems.DESH_INGOT.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "desh_nugget_from_ingot"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DESH_BLOCK.get())
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.DESH_INGOT.get())
            .unlockedBy("has_desh_ingot", has(ModItems.DESH_INGOT.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "desh_block"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DESH_INGOT.get(), 9)
            .requires(ModBlocks.DESH_BLOCK.get())
            .unlockedBy("has_desh_block", has(ModBlocks.DESH_BLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "desh_ingot_from_block"));

        // Ostrum: 9 nuggets <-> 1 ingot, 9 ingots <-> 1 block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.OSTRUM_INGOT.get())
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.OSTRUM_NUGGET.get())
            .unlockedBy("has_ostrum_nugget", has(ModItems.OSTRUM_NUGGET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "ostrum_ingot_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.OSTRUM_NUGGET.get(), 9)
            .requires(ModItems.OSTRUM_INGOT.get())
            .unlockedBy("has_ostrum_ingot", has(ModItems.OSTRUM_INGOT.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "ostrum_nugget_from_ingot"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.OSTRUM_BLOCK.get())
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.OSTRUM_INGOT.get())
            .unlockedBy("has_ostrum_ingot", has(ModItems.OSTRUM_INGOT.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "ostrum_block"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.OSTRUM_INGOT.get(), 9)
            .requires(ModBlocks.OSTRUM_BLOCK.get())
            .unlockedBy("has_ostrum_block", has(ModBlocks.OSTRUM_BLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "ostrum_ingot_from_block"));

        // Calorite: 9 nuggets <-> 1 ingot, 9 ingots <-> 1 block
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CALORITE_INGOT.get())
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.CALORITE_NUGGET.get())
            .unlockedBy("has_calorite_nugget", has(ModItems.CALORITE_NUGGET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "calorite_ingot_from_nuggets"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CALORITE_NUGGET.get(), 9)
            .requires(ModItems.CALORITE_INGOT.get())
            .unlockedBy("has_calorite_ingot", has(ModItems.CALORITE_INGOT.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "calorite_nugget_from_ingot"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CALORITE_BLOCK.get())
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.CALORITE_INGOT.get())
            .unlockedBy("has_calorite_ingot", has(ModItems.CALORITE_INGOT.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "calorite_block"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CALORITE_INGOT.get(), 9)
            .requires(ModBlocks.CALORITE_BLOCK.get())
            .unlockedBy("has_calorite_block", has(ModBlocks.CALORITE_BLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "calorite_ingot_from_block"));

        // ========== Raw Ore Block Compression/Decompression ==========

        // Raw Desh: 9 raw <-> 1 raw block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RAW_DESH_BLOCK.get())
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.RAW_DESH.get())
            .unlockedBy("has_raw_desh", has(ModItems.RAW_DESH.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "raw_desh_block"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RAW_DESH.get(), 9)
            .requires(ModBlocks.RAW_DESH_BLOCK.get())
            .unlockedBy("has_raw_desh_block", has(ModBlocks.RAW_DESH_BLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "raw_desh_from_block"));

        // Raw Ostrum: 9 raw <-> 1 raw block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RAW_OSTRUM_BLOCK.get())
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.RAW_OSTRUM.get())
            .unlockedBy("has_raw_ostrum", has(ModItems.RAW_OSTRUM.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "raw_ostrum_block"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RAW_OSTRUM.get(), 9)
            .requires(ModBlocks.RAW_OSTRUM_BLOCK.get())
            .unlockedBy("has_raw_ostrum_block", has(ModBlocks.RAW_OSTRUM_BLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "raw_ostrum_from_block"));

        // Raw Calorite: 9 raw <-> 1 raw block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.RAW_CALORITE_BLOCK.get())
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.RAW_CALORITE.get())
            .unlockedBy("has_raw_calorite", has(ModItems.RAW_CALORITE.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "raw_calorite_block"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RAW_CALORITE.get(), 9)
            .requires(ModBlocks.RAW_CALORITE_BLOCK.get())
            .unlockedBy("has_raw_calorite_block", has(ModBlocks.RAW_CALORITE_BLOCK.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "raw_calorite_from_block"));

        // ========== Decorative Panels ==========

        // Iron Panel: 3 iron sheets + 5 iron ingots -> 1 panel
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.IRON_PANEL.get(), 1)
            .pattern("#I#")
            .pattern("III")
            .pattern("#I#")
            .define('#', getIronSheet())
            .define('I', Items.IRON_INGOT)
            .unlockedBy("has_iron_sheet", has(getIronSheet()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "iron_panel"));

        // Steel Panel: 3 steel sheets + 5 steel ingots -> 1 panel
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_PANEL.get(), 1)
            .pattern("#I#")
            .pattern("III")
            .pattern("#I#")
            .define('#', ModItems.STEEL_SHEET.get())
            .define('I', BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mekanism", "ingot_steel")))
            .unlockedBy("has_steel_sheet", has(ModItems.STEEL_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "steel_panel"));

        // Etrium Panel: 3 etrium sheets + 5 etrium ingots -> 1 panel
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ETRIUM_PANEL.get(), 1)
            .pattern("#I#")
            .pattern("III")
            .pattern("#I#")
            .define('#', ModItems.ETRIUM_SHEET.get())
            .define('I', ModItems.ETRIUM_INGOT.get())
            .unlockedBy("has_etrium_sheet", has(ModItems.ETRIUM_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "etrium_panel"));

        // Desh Panel: 3 desh sheets + 5 desh ingots -> 1 panel
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DESH_PANEL.get(), 1)
            .pattern("#I#")
            .pattern("III")
            .pattern("#I#")
            .define('#', ModItems.DESH_SHEET.get())
            .define('I', ModItems.DESH_INGOT.get())
            .unlockedBy("has_desh_sheet", has(ModItems.DESH_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "desh_panel"));

        // Ostrum Panel: 3 ostrum sheets + 5 ostrum ingots -> 1 panel
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.OSTRUM_PANEL.get(), 1)
            .pattern("#I#")
            .pattern("III")
            .pattern("#I#")
            .define('#', ModItems.OSTRUM_SHEET.get())
            .define('I', ModItems.OSTRUM_INGOT.get())
            .unlockedBy("has_ostrum_sheet", has(ModItems.OSTRUM_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "ostrum_panel"));

        // Calorite Panel: 3 calorite sheets + 5 calorite ingots -> 1 panel
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CALORITE_PANEL.get(), 1)
            .pattern("#I#")
            .pattern("III")
            .pattern("#I#")
            .define('#', ModItems.CALORITE_SHEET.get())
            .define('I', ModItems.CALORITE_INGOT.get())
            .unlockedBy("has_calorite_sheet", has(ModItems.CALORITE_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "calorite_panel"));

        // ========== Decorative Plating ==========

        // Iron Plating: 9 iron sheets -> 1 plating
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.IRON_PLATING.get(), 1)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', getIronSheet())
            .unlockedBy("has_iron_sheet", has(getIronSheet()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "iron_plating"));

        // Steel Plating: 9 steel sheets -> 1 plating
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_PLATING.get(), 1)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.STEEL_SHEET.get())
            .unlockedBy("has_steel_sheet", has(ModItems.STEEL_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "steel_plating"));

        // Desh Plating: 9 desh sheets -> 1 plating
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DESH_PLATING.get(), 1)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.DESH_SHEET.get())
            .unlockedBy("has_desh_sheet", has(ModItems.DESH_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "desh_plating"));

        // Ostrum Plating: 9 ostrum sheets -> 1 plating
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.OSTRUM_PLATING.get(), 1)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.OSTRUM_SHEET.get())
            .unlockedBy("has_ostrum_sheet", has(ModItems.OSTRUM_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "ostrum_plating"));

        // Calorite Plating: 9 calorite sheets -> 1 plating
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CALORITE_PLATING.get(), 1)
            .pattern("###")
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.CALORITE_SHEET.get())
            .unlockedBy("has_calorite_sheet", has(ModItems.CALORITE_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "calorite_plating"));

        // ========== Pillars ==========

        // Iron Pillar: 2 iron plating -> 2 pillars
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.IRON_PILLAR.get(), 2)
            .pattern("#")
            .pattern("#")
            .define('#', ModBlocks.IRON_PLATING.get())
            .unlockedBy("has_iron_plating", has(ModBlocks.IRON_PLATING.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "iron_pillar"));

        // Steel Pillar: 2 steel plating -> 2 pillars
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_PILLAR.get(), 2)
            .pattern("#")
            .pattern("#")
            .define('#', ModBlocks.STEEL_PLATING.get())
            .unlockedBy("has_steel_plating", has(ModBlocks.STEEL_PLATING.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "steel_pillar"));

        // Desh Pillar: 2 desh plating -> 2 pillars
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.DESH_PILLAR.get(), 2)
            .pattern("#")
            .pattern("#")
            .define('#', ModBlocks.DESH_PLATING.get())
            .unlockedBy("has_desh_plating", has(ModBlocks.DESH_PLATING.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "desh_pillar"));

        // Ostrum Pillar: 2 ostrum plating -> 2 pillars
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.OSTRUM_PILLAR.get(), 2)
            .pattern("#")
            .pattern("#")
            .define('#', ModBlocks.OSTRUM_PLATING.get())
            .unlockedBy("has_ostrum_plating", has(ModBlocks.OSTRUM_PLATING.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "ostrum_pillar"));

        // Calorite Pillar: 2 calorite plating -> 2 pillars
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.CALORITE_PILLAR.get(), 2)
            .pattern("#")
            .pattern("#")
            .define('#', ModBlocks.CALORITE_PLATING.get())
            .unlockedBy("has_calorite_plating", has(ModBlocks.CALORITE_PLATING.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "calorite_pillar"));

        // Marked Iron Pillar: iron pillar + yellow dye + black dye
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.MARKED_IRON_PILLAR.get())
            .requires(ModBlocks.IRON_PILLAR.get())
            .requires(Items.YELLOW_DYE)
            .requires(Items.BLACK_DYE)
            .unlockedBy("has_iron_pillar", has(ModBlocks.IRON_PILLAR.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "marked_iron_pillar"));

        // Glowing Pillars: pillar + glowstone dust
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GLOWING_IRON_PILLAR.get())
            .requires(ModBlocks.IRON_PILLAR.get())
            .requires(Items.GLOWSTONE_DUST)
            .unlockedBy("has_iron_pillar", has(ModBlocks.IRON_PILLAR.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "glowing_iron_pillar"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GLOWING_STEEL_PILLAR.get())
            .requires(ModBlocks.STEEL_PILLAR.get())
            .requires(Items.GLOWSTONE_DUST)
            .unlockedBy("has_steel_pillar", has(ModBlocks.STEEL_PILLAR.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "glowing_steel_pillar"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GLOWING_DESH_PILLAR.get())
            .requires(ModBlocks.DESH_PILLAR.get())
            .requires(Items.GLOWSTONE_DUST)
            .unlockedBy("has_desh_pillar", has(ModBlocks.DESH_PILLAR.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "glowing_desh_pillar"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GLOWING_OSTRUM_PILLAR.get())
            .requires(ModBlocks.OSTRUM_PILLAR.get())
            .requires(Items.GLOWSTONE_DUST)
            .unlockedBy("has_ostrum_pillar", has(ModBlocks.OSTRUM_PILLAR.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "glowing_ostrum_pillar"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GLOWING_CALORITE_PILLAR.get())
            .requires(ModBlocks.CALORITE_PILLAR.get())
            .requires(Items.GLOWSTONE_DUST)
            .unlockedBy("has_calorite_pillar", has(ModBlocks.CALORITE_PILLAR.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "glowing_calorite_pillar"));

        // ========== Factory Blocks ==========

        // Iron Factory Block: 8 iron sheets + 1 iron ingot -> 1 factory block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.IRON_FACTORY_BLOCK.get(), 1)
            .pattern("###")
            .pattern("#I#")
            .pattern("###")
            .define('#', getIronSheet())
            .define('I', Items.IRON_INGOT)
            .unlockedBy("has_iron_sheet", has(getIronSheet()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "iron_factory_block"));

        // Steel Factory Block: 8 steel sheets + 1 steel ingot -> 1 factory block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.STEEL_FACTORY_BLOCK.get(), 1)
            .pattern("###")
            .pattern("#S#")
            .pattern("###")
            .define('#', ModItems.STEEL_SHEET.get())
            .define('S', BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mekanism", "ingot_steel")))
            .unlockedBy("has_steel_sheet", has(ModItems.STEEL_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "steel_factory_block"));

        // ========== Encased Blocks ==========

        // Encased Iron Block: 6 iron sheets + 3 steel ingots -> 1 encased block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ENCASED_IRON_BLOCK.get(), 1)
            .pattern("III")
            .pattern("###")
            .pattern("III")
            .define('#', getIronSheet())
            .define('I', BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mekanism", "ingot_steel")))
            .unlockedBy("has_iron_sheet", has(getIronSheet()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "encased_iron_block"));

        // Encased Steel Block: 6 steel sheets + 3 steel ingots -> 1 encased block
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.ENCASED_STEEL_BLOCK.get(), 1)
            .pattern("III")
            .pattern("###")
            .pattern("III")
            .define('#', ModItems.STEEL_SHEET.get())
            .define('I', BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mekanism", "ingot_steel")))
            .unlockedBy("has_steel_sheet", has(ModItems.STEEL_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "encased_steel_block"));

        // ========== Steel Door and Trapdoor ==========

        // Steel Door: 6 steel sheets -> 3 doors
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.STEEL_DOOR.get(), 3)
            .pattern("##")
            .pattern("##")
            .pattern("##")
            .define('#', ModItems.STEEL_SHEET.get())
            .unlockedBy("has_steel_sheet", has(ModItems.STEEL_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "steel_door"));

        // Steel Trapdoor: 6 steel sheets -> 2 trapdoors
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ModBlocks.STEEL_TRAPDOOR.get(), 2)
            .pattern("###")
            .pattern("###")
            .define('#', ModItems.STEEL_SHEET.get())
            .unlockedBy("has_steel_sheet", has(ModItems.STEEL_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "steel_trapdoor"));

        // ========== NASA Workbench ==========

        // NASA Workbench: 3 steel sheets + 1 crafting table + 1 steel block + 2 iron rods + 1 redstone torch -> 1 workbench
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.NASA_WORKBENCH.get(), 1)
            .pattern("|#|")
            .pattern("TCT")
            .pattern("#B#")
            .define('#', ModItems.STEEL_SHEET.get())
            .define('C', Items.CRAFTING_TABLE)
            .define('B', BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mekanism", "block_steel")))
            .define('|', ModItems.IRON_ROD.get())
            .define('T', Items.REDSTONE_TORCH)
            .unlockedBy("has_steel_sheet", has(ModItems.STEEL_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "nasa_workbench"));

        // ========== ROCKET INFRASTRUCTURE ==========

        // Launch Pad: 9 steel sheets + 4 iron blocks + 4 steel ingots -> 1 launch pad
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, ModBlocks.LAUNCH_PAD.get(), 1)
            .pattern("###")
            .pattern("SIS")
            .pattern("I#I")
            .define('#', ModItems.STEEL_SHEET.get())
            .define('I', Items.IRON_BLOCK)
            .define('S', BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("mekanism", "ingot_steel")))
            .unlockedBy("has_steel_sheet", has(ModItems.STEEL_SHEET.get()))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "launch_pad"));

        // ========== Advanced Components ==========


    }

    private void buildSmeltingRecipes(RecipeOutput recipeOutput) {
        AdAstraMekanized.LOGGER.info("Building smelting recipes...");

        // Raw ore smelting
        oreSmelting(recipeOutput, "raw_desh", ModItems.RAW_DESH.get(), ModItems.DESH_INGOT.get(), 0.7f, 200);
        oreSmelting(recipeOutput, "raw_ostrum", ModItems.RAW_OSTRUM.get(), ModItems.OSTRUM_INGOT.get(), 0.7f, 200);
        oreSmelting(recipeOutput, "raw_calorite", ModItems.RAW_CALORITE.get(), ModItems.CALORITE_INGOT.get(), 0.7f, 200);

        // Moon ore smelting
        oreSmelting(recipeOutput, "moon_cheese", ModBlocks.MOON_CHEESE_ORE.get(), ModItems.CHEESE.get(), 0.1f, 200);
        oreSmelting(recipeOutput, "moon_desh", ModBlocks.MOON_DESH_ORE.get(), ModItems.DESH_INGOT.get(), 0.7f, 200);
        oreSmelting(recipeOutput, "moon_iron", ModBlocks.MOON_IRON_ORE.get(), Items.IRON_INGOT, 0.7f, 200);
        oreSmelting(recipeOutput, "moon_etrium_nugget", ModBlocks.MOON_ETRIUM_ORE.get(), ModItems.ETRIUM_NUGGET.get(), 0.2f, 200);

        // Mars ore smelting
        oreSmelting(recipeOutput, "mars_iron", ModBlocks.MARS_IRON_ORE.get(), Items.IRON_INGOT, 0.7f, 200);
        oreSmelting(recipeOutput, "mars_diamond", ModBlocks.MARS_DIAMOND_ORE.get(), Items.DIAMOND, 1.0f, 200);
        oreSmelting(recipeOutput, "mars_ostrum", ModBlocks.MARS_OSTRUM_ORE.get(), ModItems.OSTRUM_INGOT.get(), 0.7f, 200);
        oreSmelting(recipeOutput, "mars_etrium_nugget", ModBlocks.MARS_ETRIUM_ORE.get(), ModItems.ETRIUM_NUGGET.get(), 0.2f, 200);

        // Venus ore smelting
        oreSmelting(recipeOutput, "venus_coal", ModBlocks.VENUS_COAL_ORE.get(), Items.COAL, 0.1f, 200);
        oreSmelting(recipeOutput, "venus_gold", ModBlocks.VENUS_GOLD_ORE.get(), Items.GOLD_INGOT, 1.0f, 200);
        oreSmelting(recipeOutput, "venus_diamond", ModBlocks.VENUS_DIAMOND_ORE.get(), Items.DIAMOND, 1.0f, 200);
        oreSmelting(recipeOutput, "venus_calorite", ModBlocks.VENUS_CALORITE_ORE.get(), ModItems.CALORITE_INGOT.get(), 0.7f, 200);

        // Mercury ore smelting
        oreSmelting(recipeOutput, "mercury_iron", ModBlocks.MERCURY_IRON_ORE.get(), Items.IRON_INGOT, 0.7f, 200);

        // Glacio ore smelting
        oreSmelting(recipeOutput, "glacio_coal", ModBlocks.GLACIO_COAL_ORE.get(), Items.COAL, 0.1f, 200);
        oreSmelting(recipeOutput, "glacio_copper", ModBlocks.GLACIO_COPPER_ORE.get(), Items.COPPER_INGOT, 0.7f, 200);
        oreSmelting(recipeOutput, "glacio_iron", ModBlocks.GLACIO_IRON_ORE.get(), Items.IRON_INGOT, 0.7f, 200);
        oreSmelting(recipeOutput, "glacio_lapis", ModBlocks.GLACIO_LAPIS_ORE.get(), Items.LAPIS_LAZULI, 0.2f, 200);
        oreSmelting(recipeOutput, "glacio_etrium_nugget", ModBlocks.GLACIO_ETRIUM_ORE.get(), ModItems.ETRIUM_NUGGET.get(), 0.2f, 200);

        // Overworld deepslate ore smelting
        oreSmelting(recipeOutput, "deepslate_desh", ModBlocks.DEEPSLATE_DESH_ORE.get(), ModItems.DESH_INGOT.get(), 0.7f, 200);
        oreSmelting(recipeOutput, "deepslate_ostrum", ModBlocks.DEEPSLATE_OSTRUM_ORE.get(), ModItems.OSTRUM_INGOT.get(), 0.7f, 200);
        oreSmelting(recipeOutput, "deepslate_calorite", ModBlocks.DEEPSLATE_CALORITE_ORE.get(), ModItems.CALORITE_INGOT.get(), 0.7f, 200);
    }

    private void buildBlastingRecipes(RecipeOutput recipeOutput) {
        AdAstraMekanized.LOGGER.info("Building blasting recipes...");

        // Raw ore blasting
        oreBlasting(recipeOutput, "raw_desh", ModItems.RAW_DESH.get(), ModItems.DESH_INGOT.get(), 0.7f, 100);
        oreBlasting(recipeOutput, "raw_ostrum", ModItems.RAW_OSTRUM.get(), ModItems.OSTRUM_INGOT.get(), 0.7f, 100);
        oreBlasting(recipeOutput, "raw_calorite", ModItems.RAW_CALORITE.get(), ModItems.CALORITE_INGOT.get(), 0.7f, 100);

        // Moon ore blasting
        oreBlasting(recipeOutput, "moon_cheese", ModBlocks.MOON_CHEESE_ORE.get(), ModItems.CHEESE.get(), 0.1f, 100);
        oreBlasting(recipeOutput, "moon_desh", ModBlocks.MOON_DESH_ORE.get(), ModItems.DESH_INGOT.get(), 0.7f, 100);
        oreBlasting(recipeOutput, "moon_iron", ModBlocks.MOON_IRON_ORE.get(), Items.IRON_INGOT, 0.7f, 100);
        oreBlasting(recipeOutput, "moon_etrium_nugget", ModBlocks.MOON_ETRIUM_ORE.get(), ModItems.ETRIUM_NUGGET.get(), 0.2f, 100);

        // Mars ore blasting
        oreBlasting(recipeOutput, "mars_iron", ModBlocks.MARS_IRON_ORE.get(), Items.IRON_INGOT, 0.7f, 100);
        oreBlasting(recipeOutput, "mars_diamond", ModBlocks.MARS_DIAMOND_ORE.get(), Items.DIAMOND, 1.0f, 100);
        oreBlasting(recipeOutput, "mars_ostrum", ModBlocks.MARS_OSTRUM_ORE.get(), ModItems.OSTRUM_INGOT.get(), 0.7f, 100);
        oreBlasting(recipeOutput, "mars_etrium_nugget", ModBlocks.MARS_ETRIUM_ORE.get(), ModItems.ETRIUM_NUGGET.get(), 0.2f, 100);

        // Venus ore blasting
        oreBlasting(recipeOutput, "venus_coal", ModBlocks.VENUS_COAL_ORE.get(), Items.COAL, 0.1f, 100);
        oreBlasting(recipeOutput, "venus_gold", ModBlocks.VENUS_GOLD_ORE.get(), Items.GOLD_INGOT, 1.0f, 100);
        oreBlasting(recipeOutput, "venus_diamond", ModBlocks.VENUS_DIAMOND_ORE.get(), Items.DIAMOND, 1.0f, 100);
        oreBlasting(recipeOutput, "venus_calorite", ModBlocks.VENUS_CALORITE_ORE.get(), ModItems.CALORITE_INGOT.get(), 0.7f, 100);

        // Mercury ore blasting
        oreBlasting(recipeOutput, "mercury_iron", ModBlocks.MERCURY_IRON_ORE.get(), Items.IRON_INGOT, 0.7f, 100);

        // Glacio ore blasting
        oreBlasting(recipeOutput, "glacio_coal", ModBlocks.GLACIO_COAL_ORE.get(), Items.COAL, 0.1f, 100);
        oreBlasting(recipeOutput, "glacio_copper", ModBlocks.GLACIO_COPPER_ORE.get(), Items.COPPER_INGOT, 0.7f, 100);
        oreBlasting(recipeOutput, "glacio_iron", ModBlocks.GLACIO_IRON_ORE.get(), Items.IRON_INGOT, 0.7f, 100);
        oreBlasting(recipeOutput, "glacio_lapis", ModBlocks.GLACIO_LAPIS_ORE.get(), Items.LAPIS_LAZULI, 0.2f, 100);
        oreBlasting(recipeOutput, "glacio_etrium_nugget", ModBlocks.GLACIO_ETRIUM_ORE.get(), ModItems.ETRIUM_NUGGET.get(), 0.2f, 100);

        // Overworld deepslate ore blasting
        oreBlasting(recipeOutput, "deepslate_desh", ModBlocks.DEEPSLATE_DESH_ORE.get(), ModItems.DESH_INGOT.get(), 0.7f, 100);
        oreBlasting(recipeOutput, "deepslate_ostrum", ModBlocks.DEEPSLATE_OSTRUM_ORE.get(), ModItems.OSTRUM_INGOT.get(), 0.7f, 100);
        oreBlasting(recipeOutput, "deepslate_calorite", ModBlocks.DEEPSLATE_CALORITE_ORE.get(), ModItems.CALORITE_INGOT.get(), 0.7f, 100);
    }

    // Helper methods
    private void oreSmelting(RecipeOutput recipeOutput, String name, ItemLike input, ItemLike output,
                             float experience, int cookingTime) {
        SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.MISC, output,
                experience, cookingTime)
            .unlockedBy("has_" + name, has(input))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID,
                name + "_from_smelting"));
    }

    private void oreBlasting(RecipeOutput recipeOutput, String name, ItemLike input, ItemLike output,
                            float experience, int cookingTime) {
        SimpleCookingRecipeBuilder.blasting(Ingredient.of(input), RecipeCategory.MISC, output,
                experience, cookingTime)
            .unlockedBy("has_" + name, has(input))
            .save(recipeOutput, ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID,
                name + "_from_blasting"));
    }
}