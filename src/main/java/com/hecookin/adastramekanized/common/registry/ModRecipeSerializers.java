package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.recipes.NasaWorkbenchRecipe;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, AdAstraMekanized.MOD_ID);

    public static final Supplier<RecipeSerializer<NasaWorkbenchRecipe>> NASA_WORKBENCH =
            RECIPE_SERIALIZERS.register("nasa_workbench",
                    () -> new RecipeSerializer<NasaWorkbenchRecipe>() {
                        @Override
                        public MapCodec<NasaWorkbenchRecipe> codec() {
                            return NasaWorkbenchRecipe.CODEC;
                        }

                        @Override
                        public StreamCodec<RegistryFriendlyByteBuf, NasaWorkbenchRecipe> streamCodec() {
                            return NasaWorkbenchRecipe.STREAM_CODEC;
                        }
                    });

    public static void register(IEventBus modEventBus) {
        AdAstraMekanized.LOGGER.info("Registering ModRecipeSerializers...");
        RECIPE_SERIALIZERS.register(modEventBus);
        AdAstraMekanized.LOGGER.info("ModRecipeSerializers registration complete");
    }
}
