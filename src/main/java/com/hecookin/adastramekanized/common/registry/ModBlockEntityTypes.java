package com.hecookin.adastramekanized.common.registry;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.LaunchPadBlockEntity;
import com.hecookin.adastramekanized.common.blockentities.NasaWorkbenchBlockEntity;
import com.hecookin.adastramekanized.common.blockentities.machines.GravityNormalizerBlockEntity;
import com.hecookin.adastramekanized.common.blockentities.machines.OxygenDistributorBlockEntity;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import com.hecookin.adastramekanized.common.blockentities.machines.WirelessPowerRelayBlockEntity;
import com.hecookin.adastramekanized.common.blockentities.OxygenNetworkMonitorBlockEntity;
import com.hecookin.adastramekanized.common.blockentities.SlidingDoorBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntityTypes {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, AdAstraMekanized.MOD_ID);

    // Keeping old one temporarily for compatibility
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OxygenDistributorBlockEntity>> OXYGEN_DISTRIBUTOR =
        BLOCK_ENTITY_TYPES.register("oxygen_distributor",
            () -> createBlockEntityType(
                OxygenDistributorBlockEntity::new,
                ModBlocks.OXYGEN_DISTRIBUTOR.get()));

    // Improved oxygen distributor with Mekanism integration
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ImprovedOxygenDistributor>> MEKANISM_OXYGEN_DISTRIBUTOR =
        BLOCK_ENTITY_TYPES.register("mekanism_oxygen_distributor",
            () -> createBlockEntityType(
                ImprovedOxygenDistributor::new,
                ModBlocks.OXYGEN_DISTRIBUTOR.get()));

    // Wireless Power Relay block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WirelessPowerRelayBlockEntity>> WIRELESS_POWER_RELAY =
        BLOCK_ENTITY_TYPES.register("wireless_power_relay",
            () -> createBlockEntityType(
                WirelessPowerRelayBlockEntity::new,
                ModBlocks.WIRELESS_POWER_RELAY.get()));

    // Gravity Normalizer block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GravityNormalizerBlockEntity>> GRAVITY_NORMALIZER =
        BLOCK_ENTITY_TYPES.register("gravity_normalizer",
            () -> createBlockEntityType(
                GravityNormalizerBlockEntity::new,
                ModBlocks.GRAVITY_NORMALIZER.get()));

    // Oxygen Network Monitor block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<OxygenNetworkMonitorBlockEntity>> OXYGEN_NETWORK_MONITOR =
        BLOCK_ENTITY_TYPES.register("oxygen_network_monitor",
            () -> createBlockEntityType(
                OxygenNetworkMonitorBlockEntity::new,
                ModBlocks.OXYGEN_NETWORK_MONITOR.get()));

    // Sliding Door block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SlidingDoorBlockEntity>> SLIDING_DOOR =
        BLOCK_ENTITY_TYPES.register("sliding_door",
            () -> createBlockEntityType(
                SlidingDoorBlockEntity::new,
                ModBlocks.IRON_SLIDING_DOOR.get(),
                ModBlocks.REINFORCED_DOOR.get(),
                ModBlocks.STEEL_SLIDING_DOOR.get(),
                ModBlocks.DESH_SLIDING_DOOR.get(),
                ModBlocks.OSTRUM_SLIDING_DOOR.get(),
                ModBlocks.CALORITE_SLIDING_DOOR.get()));

    // NASA Workbench block entity
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<NasaWorkbenchBlockEntity>> NASA_WORKBENCH =
        BLOCK_ENTITY_TYPES.register("nasa_workbench",
            () -> createBlockEntityType(
                NasaWorkbenchBlockEntity::new,
                ModBlocks.NASA_WORKBENCH.get()));

    // Launch Pad block entity (for automatic fueling)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LaunchPadBlockEntity>> LAUNCH_PAD =
        BLOCK_ENTITY_TYPES.register("launch_pad",
            () -> createBlockEntityType(
                LaunchPadBlockEntity::new,
                ModBlocks.LAUNCH_PAD.get()));

    /**
     * Helper method to create a BlockEntityType
     */
    private static <T extends BlockEntity> BlockEntityType<T> createBlockEntityType(
            BlockEntityType.BlockEntitySupplier<T> supplier, Block... blocks) {
        return BlockEntityType.Builder.of(supplier, blocks).build(null);
    }
}