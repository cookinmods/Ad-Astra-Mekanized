package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.recipes.NasaWorkbenchRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipeTypes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, AdAstraMekanized.MOD_ID);

    public static final Supplier<RecipeType<NasaWorkbenchRecipe>> NASA_WORKBENCH =
            RECIPE_TYPES.register("nasa_workbench", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return AdAstraMekanized.MOD_ID + ":nasa_workbench";
                }
            });

    public static void register(IEventBus modEventBus) {
        AdAstraMekanized.LOGGER.info("Registering ModRecipeTypes...");
        RECIPE_TYPES.register(modEventBus);
        AdAstraMekanized.LOGGER.info("ModRecipeTypes registration complete");
    }
}
