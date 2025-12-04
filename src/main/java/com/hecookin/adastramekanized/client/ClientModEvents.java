package com.hecookin.adastramekanized.client;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.models.armor.SpaceSuitModel;
import com.hecookin.adastramekanized.client.models.entities.vehicles.RocketModel;
import com.hecookin.adastramekanized.client.renderers.blocks.GravityNormalizerBlockEntityRenderer;
import com.hecookin.adastramekanized.client.renderers.blocks.MekanismBasedOxygenDistributorRenderer;
import com.hecookin.adastramekanized.client.renderers.blocks.OxygenDistributorBlockEntityRenderer;
import com.hecookin.adastramekanized.client.renderers.blocks.SlidingDoorBlockEntityRenderer;
import com.hecookin.adastramekanized.client.renderers.entities.vehicles.LanderRenderer;
import com.hecookin.adastramekanized.client.renderers.entities.vehicles.RocketRenderer;
import com.hecookin.adastramekanized.client.gui.GuiOxygenDistributor;
import com.hecookin.adastramekanized.client.screens.GravityNormalizerScreen;
import com.hecookin.adastramekanized.client.screens.NasaWorkbenchScreen;
import com.hecookin.adastramekanized.client.screens.OxygenControllerScreen;
import com.hecookin.adastramekanized.client.screens.OxygenMonitorScreen;
import com.hecookin.adastramekanized.client.screens.PlanetsScreen;
import com.hecookin.adastramekanized.client.screens.RocketScreen;
import com.hecookin.adastramekanized.client.screens.WirelessPowerRelayScreen;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.registry.ModEntityTypes;
import com.hecookin.adastramekanized.common.registry.ModItems;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Register block entity renderers
            BlockEntityRenderers.register(ModBlockEntityTypes.OXYGEN_DISTRIBUTOR.get(), (context) -> new OxygenDistributorBlockEntityRenderer());
            BlockEntityRenderers.register(ModBlockEntityTypes.MEKANISM_OXYGEN_DISTRIBUTOR.get(), MekanismBasedOxygenDistributorRenderer::new);
            BlockEntityRenderers.register(ModBlockEntityTypes.SLIDING_DOOR.get(), SlidingDoorBlockEntityRenderer::new);
            BlockEntityRenderers.register(ModBlockEntityTypes.GRAVITY_NORMALIZER.get(), (context) -> new GravityNormalizerBlockEntityRenderer());

            // Register armor model renderers
            registerArmorRenderers();
        });
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Register armor model layers
        event.registerLayerDefinition(SpaceSuitModel.SPACE_SUIT_LAYER, SpaceSuitModel::createSpaceSuitLayer);
        event.registerLayerDefinition(SpaceSuitModel.NETHERITE_SPACE_SUIT_LAYER, SpaceSuitModel::createNetheriteSpaceSuitLayer);
        event.registerLayerDefinition(SpaceSuitModel.JET_SUIT_LAYER, SpaceSuitModel::createJetSuitLayer);

        // Register rocket model layers
        event.registerLayerDefinition(RocketModel.TIER_1_LAYER, RocketModel::createTier1Layer);
        event.registerLayerDefinition(RocketModel.TIER_2_LAYER, RocketModel::createTier2Layer);
        event.registerLayerDefinition(RocketModel.TIER_3_LAYER, RocketModel::createTier3Layer);
        event.registerLayerDefinition(RocketModel.TIER_4_LAYER, RocketModel::createTier4Layer);

        // Register lander model layer
        event.registerLayerDefinition(com.hecookin.adastramekanized.client.models.entities.vehicles.LanderModel.LAYER, com.hecookin.adastramekanized.client.models.entities.vehicles.LanderModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Register rocket entity renderers
        event.registerEntityRenderer(ModEntityTypes.TIER_1_ROCKET.get(), context ->
            new RocketRenderer(context, RocketModel.TIER_1_LAYER, RocketRenderer.TIER_1_TEXTURE));
        event.registerEntityRenderer(ModEntityTypes.TIER_2_ROCKET.get(), context ->
            new RocketRenderer(context, RocketModel.TIER_2_LAYER, RocketRenderer.TIER_2_TEXTURE));
        event.registerEntityRenderer(ModEntityTypes.TIER_3_ROCKET.get(), context ->
            new RocketRenderer(context, RocketModel.TIER_3_LAYER, RocketRenderer.TIER_3_TEXTURE));
        event.registerEntityRenderer(ModEntityTypes.TIER_4_ROCKET.get(), context ->
            new RocketRenderer(context, RocketModel.TIER_4_LAYER, RocketRenderer.TIER_4_TEXTURE));

        // Register lander renderer - placeholder until LanderModel is created
        event.registerEntityRenderer(ModEntityTypes.LANDER.get(), LanderRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // Register rocket item renderers and arm poses
        event.registerItem(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new RocketRenderer.ItemRenderer(RocketModel.TIER_1_LAYER, RocketRenderer.TIER_1_TEXTURE);
            }

            @Override
            public net.minecraft.client.model.HumanoidModel.ArmPose getArmPose(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.InteractionHand hand, net.minecraft.world.item.ItemStack stack) {
                return net.minecraft.client.model.HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        }, ModItems.TIER_1_ROCKET.get());

        // Tier 2
        event.registerItem(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new RocketRenderer.ItemRenderer(RocketModel.TIER_2_LAYER, RocketRenderer.TIER_2_TEXTURE);
            }

            @Override
            public net.minecraft.client.model.HumanoidModel.ArmPose getArmPose(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.InteractionHand hand, net.minecraft.world.item.ItemStack stack) {
                return net.minecraft.client.model.HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        }, ModItems.TIER_2_ROCKET.get());

        // Tier 3
        event.registerItem(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new RocketRenderer.ItemRenderer(RocketModel.TIER_3_LAYER, RocketRenderer.TIER_3_TEXTURE);
            }

            @Override
            public net.minecraft.client.model.HumanoidModel.ArmPose getArmPose(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.InteractionHand hand, net.minecraft.world.item.ItemStack stack) {
                return net.minecraft.client.model.HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        }, ModItems.TIER_3_ROCKET.get());

        // Tier 4
        event.registerItem(new IClientItemExtensions() {
            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return new RocketRenderer.ItemRenderer(RocketModel.TIER_4_LAYER, RocketRenderer.TIER_4_TEXTURE);
            }

            @Override
            public net.minecraft.client.model.HumanoidModel.ArmPose getArmPose(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.InteractionHand hand, net.minecraft.world.item.ItemStack stack) {
                return net.minecraft.client.model.HumanoidModel.ArmPose.BOW_AND_ARROW;
            }
        }, ModItems.TIER_4_ROCKET.get());
    }

    private static void registerArmorRenderers() {
        // Space suit pieces
        ArmorModelRegistry.register(ModItems.SPACE_HELMET.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.SPACE_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.SPACE_SUIT.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.SPACE_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.SPACE_PANTS.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.SPACE_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.SPACE_BOOTS.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.SPACE_SUIT_LAYER, SpaceSuitModel::new));

        // Netherite space suit pieces
        ArmorModelRegistry.register(ModItems.NETHERITE_SPACE_HELMET.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.NETHERITE_SPACE_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.NETHERITE_SPACE_SUIT.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.NETHERITE_SPACE_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.NETHERITE_SPACE_PANTS.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.NETHERITE_SPACE_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.NETHERITE_SPACE_BOOTS.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.NETHERITE_SPACE_SUIT_LAYER, SpaceSuitModel::new));

        // Jet suit pieces
        ArmorModelRegistry.register(ModItems.JET_SUIT_HELMET.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.JET_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.JET_SUIT.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.JET_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.JET_SUIT_PANTS.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.JET_SUIT_LAYER, SpaceSuitModel::new));
        ArmorModelRegistry.register(ModItems.JET_SUIT_BOOTS.get(),
            new ArmorModelRegistry.ArmorRenderer(SpaceSuitModel.JET_SUIT_LAYER, SpaceSuitModel::new));
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        // Register menu screens
        event.register(ModMenuTypes.OXYGEN_DISTRIBUTOR.get(), GuiOxygenDistributor::new);
        event.register(ModMenuTypes.OXYGEN_CONTROLLER.get(), OxygenControllerScreen::new);
        event.register(ModMenuTypes.WIRELESS_POWER_RELAY.get(), WirelessPowerRelayScreen::new);
        event.register(ModMenuTypes.OXYGEN_MONITOR.get(), OxygenMonitorScreen::new);
        event.register(ModMenuTypes.GRAVITY_NORMALIZER.get(), GravityNormalizerScreen::new);
        event.register(ModMenuTypes.NASA_WORKBENCH.get(), NasaWorkbenchScreen::new);
        event.register(ModMenuTypes.ROCKET.get(), RocketScreen::new);
        // TODO: Create LanderScreen - using RocketScreen as placeholder
        //event.register(ModMenuTypes.LANDER.get(), RocketScreen::new);
        event.register(ModMenuTypes.PLANETS.get(), PlanetsScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        // Register the oxygen distributor top model for the animated part
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/oxygen_distributor_top")));

        // Register the gravity normalizer top model for the animated part
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/gravity_normalizer_top")));

        // Register sliding door models
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/reinforced_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/reinforced_door_flipped")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/iron_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/steel_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/desh_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/ostrum_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/calorite_sliding_door")));

        // Register frame models too
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/sliding_door_frame_reinforced_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/sliding_door_frame_iron_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/sliding_door_frame_steel_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/sliding_door_frame_desh_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/sliding_door_frame_ostrum_sliding_door")));
        event.register(ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "block/sliding_door_frame_calorite_sliding_door")));
    }
}