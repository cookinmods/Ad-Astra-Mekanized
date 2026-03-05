package com.hecookin.adastramekanized.common.capabilities;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.LaunchPadBlockEntity;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import com.hecookin.adastramekanized.common.blockentities.machines.WirelessPowerRelayBlockEntity;
import com.hecookin.adastramekanized.common.blocks.machines.OxygenDistributorBlock;
import com.hecookin.adastramekanized.common.items.armor.JetSuitItem;
import com.hecookin.adastramekanized.common.items.armor.NetheriteSpaceSuitItem;
import com.hecookin.adastramekanized.common.items.armor.SpaceSuitItem;
import com.hecookin.adastramekanized.common.registry.ModBlocks;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import com.hecookin.adastramekanized.common.registry.ModItems;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalHandler;
import mekanism.api.energy.IStrictEnergyHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * Simple capability provider for Mekanism integration.
 * The oxygen distributor block entity handles its own capabilities directly.
 */
public class MekanismCapabilityProvider {

    /**
     * Register capabilities during mod setup
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register capability providers for our block entities
        // This tells the system how to get capabilities from our block entities

        // Register Forge Energy capability for Wireless Power Relay
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlockEntityTypes.WIRELESS_POWER_RELAY.get(),
            (blockEntity, side) -> {
                if (blockEntity instanceof WirelessPowerRelayBlockEntity relay) {
                    return relay.getEnergyStorage();
                }
                return null;
            }
        );

        // Try to register Mekanism capabilities if available
        // Note: Forge EnergyStorage.BLOCK is registered in OxygenDistributorCapabilityProvider
        try {
            Class<?> mekCapabilities = Class.forName("mekanism.common.capabilities.Capabilities");
            java.lang.reflect.Field strictEnergyField = mekCapabilities.getField("STRICT_ENERGY");
            Object strictEnergyCapObject = strictEnergyField.get(null);
            java.lang.reflect.Method blockMethod = strictEnergyCapObject.getClass().getMethod("block");
            Object strictEnergyBlockCap = blockMethod.invoke(strictEnergyCapObject);

            if (strictEnergyBlockCap instanceof BlockCapability) {
                // Register Mekanism STRICT_ENERGY capability for Wireless Power Relay
                event.registerBlockEntity(
                    (BlockCapability) strictEnergyBlockCap,
                    ModBlockEntityTypes.WIRELESS_POWER_RELAY.get(),
                    (blockEntity, side) -> {
                        if (blockEntity instanceof WirelessPowerRelayBlockEntity relay) {
                            return relay.getEnergyStorage();  // EnergyStorage implements IStrictEnergyHandler
                        }
                        return null;
                    }
                );

                // Register Mekanism STRICT_ENERGY capability for Oxygen Distributor
                event.registerBlockEntity(
                    (BlockCapability) strictEnergyBlockCap,
                    ModBlockEntityTypes.OXYGEN_DISTRIBUTOR.get(),
                    (blockEntity, side) -> {
                        if (blockEntity instanceof ImprovedOxygenDistributor distributor) {
                            return distributor.getStrictEnergyHandler();
                        }
                        return null;
                    }
                );

                AdAstraMekanized.LOGGER.info("Registered Mekanism STRICT_ENERGY capabilities");
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.info("Mekanism not available, skipping STRICT_ENERGY capability registration");
        }

        AdAstraMekanized.LOGGER.info("Registered block entity capabilities for energy storage");

        // Register Fluid Handler capability for Launch Pad
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            ModBlockEntityTypes.LAUNCH_PAD.get(),
            (blockEntity, side) -> {
                if (blockEntity instanceof LaunchPadBlockEntity launchPad) {
                    return launchPad.getFluidHandler(side);
                }
                return null;
            }
        );

        AdAstraMekanized.LOGGER.info("Registered fluid handler capability for launch pad");

        // Register item capabilities for space suits
        registerSpaceSuitCapabilities(event);
    }

    private static void registerSpaceSuitCapabilities(RegisterCapabilitiesEvent event) {
        // Try to register Mekanism chemical capabilities for our space suits
        try {
            Class<?> mekCapabilities = Class.forName("mekanism.common.capabilities.Capabilities");
            java.lang.reflect.Field chemicalField = mekCapabilities.getField("CHEMICAL");
            Object chemicalCapObject = chemicalField.get(null);

            // Get the item() method to get ItemCapability
            java.lang.reflect.Method itemMethod = chemicalCapObject.getClass().getMethod("item");
            Object chemicalItemCap = itemMethod.invoke(chemicalCapObject);

            if (chemicalItemCap instanceof ItemCapability) {
                ItemCapability<IChemicalHandler, Void> capability = (ItemCapability<IChemicalHandler, Void>) chemicalItemCap;

                // Register for Space Suit (chest piece)
                event.registerItem(
                    capability,
                    (stack, context) -> {
                        // Always ensure the stack has chemical data
                        ensureChemicalData(stack, "oxygen");
                        return createChemicalHandler(stack, "oxygen");
                    },
                    ModItems.SPACE_SUIT.get()
                );

                // Register for Netherite Space Suit (chest piece)
                event.registerItem(
                    capability,
                    (stack, context) -> {
                        ensureChemicalData(stack, "oxygen");
                        return createChemicalHandler(stack, "oxygen");
                    },
                    ModItems.NETHERITE_SPACE_SUIT.get()
                );

                // Register for Jet Suit (handles both oxygen and nitrogen in separate tanks)
                event.registerItem(
                    capability,
                    (stack, context) -> {
                        // Jet suit needs special handling for dual tanks
                        if (stack.getItem() instanceof com.hecookin.adastramekanized.common.items.armor.JetSuitItem jetSuit) {
                            // Only initialize if not already initialized
                            CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                            CompoundTag tag = customData.copyTag();
                            if (!tag.contains("mekanism")) {
                                jetSuit.initializeDualChemicals(stack);
                            }
                            return createDualChemicalHandler(stack);
                        }
                        return null;
                    },
                    ModItems.JET_SUIT.get()
                );

                // Register for Gas Tank
                event.registerItem(
                    capability,
                    (stack, context) -> {
                        ensureChemicalData(stack, "oxygen", "nitrogen");
                        return createChemicalHandler(stack, "oxygen", "nitrogen");
                    },
                    ModItems.GAS_TANK.get()
                );

                // Register for Large Gas Tank
                event.registerItem(
                    capability,
                    (stack, context) -> {
                        ensureChemicalData(stack, "oxygen", "nitrogen");
                        return createChemicalHandler(stack, "oxygen", "nitrogen");
                    },
                    ModItems.LARGE_GAS_TANK.get()
                );

                AdAstraMekanized.LOGGER.info("Registered Mekanism chemical capabilities for space suits and gas tanks");
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.info("Mekanism not available, skipping chemical capability registration for items: {}", e.getMessage());
        }
    }

    /**
     * Ensures the ItemStack has chemical data in its NBT
     */
    private static void ensureChemicalData(ItemStack stack, String... acceptedChemicals) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (!tag.contains("mekanism")) {
            // Get the actual capacity from the armor item
            long capacity = getArmorCapacity(stack);

            // Initialize chemical data
            CompoundTag mekData = new CompoundTag();
            mekData.putLong("capacity", capacity);
            mekData.putString("chemicalType", String.join(",", acceptedChemicals));

            CompoundTag storedData = new CompoundTag();
            storedData.putLong("amount", 0L);
            storedData.putString("chemical", acceptedChemicals[0]);
            mekData.put("stored", storedData);

            tag.put("mekanism", mekData);
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    /**
     * Get the actual capacity from the armor item or gas tank
     */
    private static long getArmorCapacity(ItemStack stack) {
        if (stack.getItem() instanceof com.hecookin.adastramekanized.common.items.armor.base.ItemChemicalArmor armor) {
            // Access the capacity field from the armor
            return armor.getCapacity();
        }
        if (stack.getItem() instanceof com.hecookin.adastramekanized.common.items.GasTankItem gasTank) {
            // Get capacity from GasTankItem field
            try {
                java.lang.reflect.Field capacityField = com.hecookin.adastramekanized.common.items.GasTankItem.class.getDeclaredField("capacity");
                capacityField.setAccessible(true);
                return (long) capacityField.get(gasTank);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.debug("Failed to get capacity from GasTankItem: {}", e.getMessage());
            }
        }
        // Default fallback
        return 1000L;
    }

    /**
     * Creates a dual chemical handler for jet suit (oxygen in tank 0, nitrogen in tank 1)
     */
    private static IChemicalHandler createDualChemicalHandler(ItemStack stack) {
        try {
            // Get necessary classes
            Class<?> chemicalClass = Class.forName("mekanism.api.chemical.Chemical");
            Class<?> chemicalStackClass = Class.forName("mekanism.api.chemical.ChemicalStack");
            Class<?> mekanismChemicalsClass = Class.forName("mekanism.common.registries.MekanismChemicals");
            Class<?> holderClass = Class.forName("net.minecraft.core.Holder");
            Class<?> actionClass = Class.forName("mekanism.api.Action");

            // Get references to oxygen and nitrogen
            Object oxygenHolder = mekanismChemicalsClass.getField("OXYGEN").get(null);
            java.lang.reflect.Method getValue = holderClass.getMethod("value");
            Object oxygen = getValue.invoke(oxygenHolder);

            // Get nitrogen from ChemLibMekanized
            Class<?> chemLibChemicalsClass = Class.forName("com.hecookin.chemlibmekanized.registry.ChemlibMekanizedChemicals");
            Object nitrogenHolder = chemLibChemicalsClass.getField("NITROGEN").get(null);
            Object nitrogen = getValue.invoke(nitrogenHolder);

            // Get ChemicalStack.EMPTY and Action enum values
            Object chemicalStackEmpty = chemicalStackClass.getField("EMPTY").get(null);
            Object actionExecute = actionClass.getField("EXECUTE").get(null);
            Object actionSimulate = actionClass.getField("SIMULATE").get(null);

            final long oxygenCapacity = 8000L;
            final long nitrogenCapacity = 8000L;

            // Make final references for use in inner class
            final Object finalOxygen = oxygen;
            final Object finalNitrogen = nitrogen;

            // Create handler for dual tanks
            return new IChemicalHandler() {
                @Override
                public int getChemicalTanks() {
                    return 2; // Two tanks: 0 = oxygen, 1 = nitrogen
                }

                @Override
                public ChemicalStack getChemicalInTank(int tank) {
                    try {
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();
                        if (!tag.isEmpty() && tag.contains("mekanism")) {
                            CompoundTag mekData = tag.getCompound("mekanism");
                            String chemType = tank == 0 ? "oxygen" : "nitrogen";
                            if (mekData.contains(chemType)) {
                                CompoundTag chemData = mekData.getCompound(chemType);
                                long amount = chemData.getLong("amount");
                                if (amount > 0) {
                                    Object chemical = tank == 0 ? finalOxygen : finalNitrogen;
                                    // Use ChemicalStack constructor instead of static create method
                                    java.lang.reflect.Constructor<?> constructor = chemicalStackClass.getConstructor(chemicalClass, long.class);
                                    return (ChemicalStack) constructor.newInstance(chemical, amount);
                                }
                            }
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.debug("Failed to get chemical in tank {}: {}", tank, e.getMessage());
                    }
                    return (ChemicalStack) chemicalStackEmpty;
                }

                @Override
                public void setChemicalInTank(int tank, ChemicalStack chemStack) {
                    if (tank < 0 || tank > 1) return;
                    updateTankNBT(tank, chemStack);
                }

                @Override
                public long getChemicalTankCapacity(int tank) {
                    if (tank == 0) return oxygenCapacity;
                    if (tank == 1) return nitrogenCapacity;
                    return 0;
                }

                @Override
                public boolean isValid(int tank, ChemicalStack stack) {
                    if (stack == null) return false;
                    try {
                        java.lang.reflect.Method getChemicalMethod = stack.getClass().getMethod("getChemical");
                        Object chemical = getChemicalMethod.invoke(stack);

                        boolean isOxygen = chemical.equals(finalOxygen);
                        boolean isNitrogen = chemical.equals(finalNitrogen);

                        if (tank == 0) return isOxygen;
                        if (tank == 1) return isNitrogen;
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.debug("Failed to validate chemical: {}", e.getMessage());
                    }
                    return false;
                }

                @Override
                public ChemicalStack insertChemical(int tank, ChemicalStack chemStack, mekanism.api.Action action) {
                    if (!isValid(tank, chemStack)) {
                        return chemStack;
                    }

                    try {
                        // Read current amount from NBT
                        long currentAmount = 0;
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();
                        String chemType = tank == 0 ? "oxygen" : "nitrogen";

                        if (!tag.isEmpty() && tag.contains("mekanism")) {
                            CompoundTag mekData = tag.getCompound("mekanism");
                            if (mekData.contains(chemType)) {
                                currentAmount = mekData.getCompound(chemType).getLong("amount");
                            }
                        }

                        long capacity = tank == 0 ? oxygenCapacity : nitrogenCapacity;
                        java.lang.reflect.Method getAmountMethod = chemStack.getClass().getMethod("getAmount");
                        long toInsert = (long) getAmountMethod.invoke(chemStack);

                        // Slow down the filling rate
                        long maxInsertPerTick = 50L;
                        long canInsert = Math.min(Math.min(toInsert, maxInsertPerTick), capacity - currentAmount);

                        if (canInsert > 0 && action == actionExecute) {
                            // Update NBT
                            CompoundTag newTag = customData.copyTag();
                            CompoundTag mekData = newTag.contains("mekanism") ? newTag.getCompound("mekanism") : new CompoundTag();

                            // Ensure both tanks exist in the NBT structure
                            if (!mekData.contains("oxygen")) {
                                CompoundTag oxygenData = new CompoundTag();
                                oxygenData.putLong("amount", 0L);
                                oxygenData.putLong("capacity", oxygenCapacity);
                                mekData.put("oxygen", oxygenData);
                            }
                            if (!mekData.contains("nitrogen")) {
                                CompoundTag nitrogenData = new CompoundTag();
                                nitrogenData.putLong("amount", 0L);
                                nitrogenData.putLong("capacity", nitrogenCapacity);
                                mekData.put("nitrogen", nitrogenData);
                            }

                            CompoundTag chemData = mekData.getCompound(chemType);
                            chemData.putLong("amount", currentAmount + canInsert);
                            chemData.putLong("capacity", capacity);
                            mekData.put(chemType, chemData);
                            newTag.put("mekanism", mekData);
                            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(newTag));
                        }

                        if (canInsert == toInsert) {
                            return (ChemicalStack) chemicalStackEmpty;
                        } else {
                            java.lang.reflect.Method copyMethod = chemStack.getClass().getMethod("copyWithAmount", long.class);
                            return (ChemicalStack) copyMethod.invoke(chemStack, toInsert - canInsert);
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.error("Failed to insert chemical into tank {}: ", tank, e);
                    }
                    return chemStack;
                }

                @Override
                public ChemicalStack insertChemical(ChemicalStack chemStack, mekanism.api.Action action) {
                    // Try to insert into the appropriate tank based on chemical type
                    if (chemStack == null) return chemStack;
                    try {
                        java.lang.reflect.Method getChemicalMethod = chemStack.getClass().getMethod("getChemical");
                        Object chemical = getChemicalMethod.invoke(chemStack);

                        boolean isOxygen = chemical.equals(finalOxygen);
                        boolean isNitrogen = chemical.equals(finalNitrogen);

                        // Determine which tank based on chemical type
                        if (isOxygen) {
                            return insertChemical(0, chemStack, action);
                        } else if (isNitrogen) {
                            return insertChemical(1, chemStack, action);
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.error("JetSuit: Failed to insert chemical without tank index: ", e);
                    }
                    return chemStack;
                }

                @Override
                public ChemicalStack extractChemical(long amount, mekanism.api.Action action) {
                    // Try oxygen first, then nitrogen
                    ChemicalStack result = extractChemical(0, amount, action);
                    if (result != chemicalStackEmpty) return result;
                    return extractChemical(1, amount, action);
                }

                @Override
                public ChemicalStack extractChemical(int tank, long amount, mekanism.api.Action action) {
                    if (tank < 0 || tank > 1) return (ChemicalStack) chemicalStackEmpty;
                    try {
                        // Read current state from NBT
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();
                        String chemType = tank == 0 ? "oxygen" : "nitrogen";

                        if (!tag.isEmpty() && tag.contains("mekanism")) {
                            CompoundTag mekData = tag.getCompound("mekanism");
                            if (mekData.contains(chemType)) {
                                CompoundTag chemData = mekData.getCompound(chemType);
                                long currentAmount = chemData.getLong("amount");

                                long toExtract = Math.min(amount, currentAmount);
                                if (toExtract > 0) {
                                    Object chemical = tank == 0 ? finalOxygen : finalNitrogen;

                                    if (action == actionExecute) {
                                        // Update NBT with new amount
                                        CompoundTag newTag = customData.copyTag();
                                        CompoundTag newMekData = newTag.getCompound("mekanism");
                                        CompoundTag newChemData = newMekData.getCompound(chemType);
                                        newChemData.putLong("amount", currentAmount - toExtract);
                                        newMekData.put(chemType, newChemData);
                                        newTag.put("mekanism", newMekData);
                                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(newTag));
                                    }

                                    // Use ChemicalStack constructor instead of static create method
                                    java.lang.reflect.Constructor<?> constructor = chemicalStackClass.getConstructor(chemicalClass, long.class);
                                    return (ChemicalStack) constructor.newInstance(chemical, toExtract);
                                }
                            }
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.debug("Failed to extract chemical from tank {}: {}", tank, e.getMessage());
                    }
                    return (ChemicalStack) chemicalStackEmpty;
                }

                private void updateTankNBT(int tank, ChemicalStack chemStack) {
                    try {
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();
                        CompoundTag mekData = tag.contains("mekanism") ? tag.getCompound("mekanism") : new CompoundTag();
                        String chemType = tank == 0 ? "oxygen" : "nitrogen";
                        CompoundTag chemData = new CompoundTag();

                        if (chemStack != null && chemStack != chemicalStackEmpty) {
                            java.lang.reflect.Method getAmountMethod = chemStack.getClass().getMethod("getAmount");
                            long amount = (long) getAmountMethod.invoke(chemStack);
                            chemData.putLong("amount", amount);
                            chemData.putLong("capacity", tank == 0 ? oxygenCapacity : nitrogenCapacity);
                        } else {
                            chemData.putLong("amount", 0L);
                            chemData.putLong("capacity", tank == 0 ? oxygenCapacity : nitrogenCapacity);
                        }

                        mekData.put(chemType, chemData);
                        tag.put("mekanism", mekData);
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.debug("Failed to update tank {} NBT: {}", tank, e.getMessage());
                    }
                }
            };

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Failed to create dual chemical handler: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Creates a chemical handler for an ItemStack using reflection to avoid compile-time dependency
     */
    private static IChemicalHandler createChemicalHandler(ItemStack stack, String... acceptedChemicals) {
        try {
            // Get necessary classes
            Class<?> chemicalClass = Class.forName("mekanism.api.chemical.Chemical");
            Class<?> chemicalStackClass = Class.forName("mekanism.api.chemical.ChemicalStack");
            Class<?> mekanismChemicalsClass = Class.forName("mekanism.common.registries.MekanismChemicals");
            Class<?> holderClass = Class.forName("net.minecraft.core.Holder");
            Class<?> actionClass = Class.forName("mekanism.api.Action");

            // Get references to oxygen and hydrogen from Mekanism
            Object oxygenHolder = mekanismChemicalsClass.getField("OXYGEN").get(null);
            Object hydrogenHolder = mekanismChemicalsClass.getField("HYDROGEN").get(null);
            java.lang.reflect.Method getValue = holderClass.getMethod("value");
            Object oxygen = getValue.invoke(oxygenHolder);
            Object hydrogen = getValue.invoke(hydrogenHolder);

            // Get nitrogen from ChemLibMekanized
            Object nitrogen = null;
            try {
                Class<?> chemLibChemicalsClass = Class.forName("com.hecookin.chemlibmekanized.registry.ChemlibMekanizedChemicals");
                Object nitrogenHolder = chemLibChemicalsClass.getField("NITROGEN").get(null);
                nitrogen = getValue.invoke(nitrogenHolder);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.debug("ChemLibMekanized nitrogen not available: {}", e.getMessage());
            }

            final Object finalNitrogen = nitrogen;

            // Get ChemicalStack.EMPTY and Action enum values
            Object chemicalStackEmpty = chemicalStackClass.getField("EMPTY").get(null);
            Object actionExecute = actionClass.getField("EXECUTE").get(null);
            Object actionSimulate = actionClass.getField("SIMULATE").get(null);

            // The handler needs to always read/write to the actual ItemStack
            // since it gets recreated each time the capability is requested

            final long capacity = getArmorCapacity(stack);

            // Create the handler directly implementing IChemicalHandler
            return new IChemicalHandler() {
                @Override
                public int getChemicalTanks() {
                    return 1; // Single tank
                }

                @Override
                public ChemicalStack getChemicalInTank(int tank) {
                    if (tank != 0) return (ChemicalStack) chemicalStackEmpty;
                    try {
                        // Read current state from NBT
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();
                        if (!tag.isEmpty() && tag.contains("mekanism")) {
                            CompoundTag mekData = tag.getCompound("mekanism");
                            if (mekData.contains("stored")) {
                                CompoundTag storedData = mekData.getCompound("stored");
                                long amount = storedData.getLong("amount");
                                String chemicalType = storedData.getString("chemical");
                                if (amount > 0) {
                                    Object chemical;
                                    if ("hydrogen".equals(chemicalType)) {
                                        chemical = hydrogen;
                                    } else if ("nitrogen".equals(chemicalType) && finalNitrogen != null) {
                                        chemical = finalNitrogen;
                                    } else {
                                        chemical = oxygen;
                                    }
                                    // Use ChemicalStack constructor instead of static create method
                                    java.lang.reflect.Constructor<?> constructor = chemicalStackClass.getConstructor(chemicalClass, long.class);
                                    return (ChemicalStack) constructor.newInstance(chemical, amount);
                                }
                            }
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.debug("Failed to create chemical stack: {}", e.getMessage());
                    }
                    return (ChemicalStack) chemicalStackEmpty;
                }

                @Override
                public void setChemicalInTank(int tank, ChemicalStack stack) {
                    if (tank != 0) return;
                    updateNBT(stack);
                }

                @Override
                public long getChemicalTankCapacity(int tank) {
                    return tank == 0 ? capacity : 0;
                }

                @Override
                public boolean isValid(int tank, ChemicalStack stack) {
                    if (tank != 0 || stack == null) return false;
                    try {
                        java.lang.reflect.Method getChemicalMethod = stack.getClass().getMethod("getChemical");
                        Object chemical = getChemicalMethod.invoke(stack);
                        for (String accepted : acceptedChemicals) {
                            if ("oxygen".equalsIgnoreCase(accepted) && chemical.equals(oxygen)) return true;
                            if ("hydrogen".equalsIgnoreCase(accepted) && chemical.equals(hydrogen)) return true;
                            if ("nitrogen".equalsIgnoreCase(accepted) && finalNitrogen != null && chemical.equals(finalNitrogen)) return true;
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.debug("Failed to validate chemical: {}", e.getMessage());
                    }
                    return false;
                }

                @Override
                public ChemicalStack insertChemical(int tank, ChemicalStack chemStack, mekanism.api.Action action) {
                    if (tank != 0 || !isValid(tank, chemStack)) return chemStack;
                    try {
                        // Read current amount from NBT
                        long currentAmount = 0;
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();
                        if (!tag.isEmpty() && tag.contains("mekanism")) {
                            CompoundTag mekData = tag.getCompound("mekanism");
                            if (mekData.contains("stored")) {
                                currentAmount = mekData.getCompound("stored").getLong("amount");
                            }
                        }

                        java.lang.reflect.Method getAmountMethod = chemStack.getClass().getMethod("getAmount");
                        long toInsert = (long) getAmountMethod.invoke(chemStack);

                        // Slow down the filling rate - max 50 mB per operation (2% of a 2500 capacity tank)
                        long maxInsertPerTick = 50L;
                        long canInsert = Math.min(Math.min(toInsert, maxInsertPerTick), capacity - currentAmount);

                        if (canInsert > 0 && action == actionExecute) {
                            // Update NBT directly
                            java.lang.reflect.Method getChemicalMethod = chemStack.getClass().getMethod("getChemical");
                            Object chemical = getChemicalMethod.invoke(chemStack);

                            CompoundTag newTag = customData.copyTag();
                            CompoundTag mekData = newTag.contains("mekanism") ? newTag.getCompound("mekanism") : new CompoundTag();
                            CompoundTag storedData = new CompoundTag();

                            storedData.putLong("amount", currentAmount + canInsert);

                            // Determine chemical name
                            String chemicalName;
                            if (chemical.equals(hydrogen)) {
                                chemicalName = "hydrogen";
                            } else if (finalNitrogen != null && chemical.equals(finalNitrogen)) {
                                chemicalName = "nitrogen";
                            } else {
                                chemicalName = "oxygen";
                            }
                            storedData.putString("chemical", chemicalName);

                            mekData.put("stored", storedData);
                            mekData.putLong("capacity", capacity);
                            newTag.put("mekanism", mekData);
                            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(newTag));

                            AdAstraMekanized.LOGGER.debug("Inserted {} mB of {}, new amount: {}", canInsert, chemicalName, currentAmount + canInsert);
                        }

                        if (canInsert == toInsert) {
                            return (ChemicalStack) chemicalStackEmpty;
                        } else {
                            java.lang.reflect.Method copyMethod = chemStack.getClass().getMethod("copyWithAmount", long.class);
                            return (ChemicalStack) copyMethod.invoke(chemStack, toInsert - canInsert);
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.error("Failed to insert chemical: ", e);
                    }
                    return chemStack;
                }

                @Override
                public ChemicalStack extractChemical(int tank, long amount, mekanism.api.Action action) {
                    if (tank != 0) return (ChemicalStack) chemicalStackEmpty;
                    try {
                        // Read current state from NBT
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();
                        if (!tag.isEmpty() && tag.contains("mekanism")) {
                            CompoundTag mekData = tag.getCompound("mekanism");
                            if (mekData.contains("stored")) {
                                CompoundTag storedData = mekData.getCompound("stored");
                                long currentAmount = storedData.getLong("amount");
                                String chemicalType = storedData.getString("chemical");

                                long toExtract = Math.min(amount, currentAmount);
                                if (toExtract > 0) {
                                    Object chemical;
                                    if ("hydrogen".equals(chemicalType)) {
                                        chemical = hydrogen;
                                    } else if ("nitrogen".equals(chemicalType) && finalNitrogen != null) {
                                        chemical = finalNitrogen;
                                    } else {
                                        chemical = oxygen;
                                    }

                                    if (action == actionExecute) {
                                        // Update NBT with new amount
                                        CompoundTag newTag = customData.copyTag();
                                        CompoundTag newMekData = newTag.getCompound("mekanism");
                                        CompoundTag newStoredData = newMekData.getCompound("stored");
                                        newStoredData.putLong("amount", currentAmount - toExtract);
                                        newMekData.put("stored", newStoredData);
                                        newTag.put("mekanism", newMekData);
                                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(newTag));

                                        AdAstraMekanized.LOGGER.debug("Extracted {} mB of {}, new amount: {}", toExtract, chemicalType, currentAmount - toExtract);
                                    }

                                    // Use ChemicalStack constructor instead of static create method
                                    java.lang.reflect.Constructor<?> constructor = chemicalStackClass.getConstructor(chemicalClass, long.class);
                                    return (ChemicalStack) constructor.newInstance(chemical, toExtract);
                                }
                            }
                        }
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.debug("Failed to extract chemical: {}", e.getMessage());
                    }
                    return (ChemicalStack) chemicalStackEmpty;
                }

                private void updateNBT(ChemicalStack chemStack) {
                    try {
                        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                        CompoundTag tag = customData.copyTag();
                        CompoundTag mekData = tag.contains("mekanism") ? tag.getCompound("mekanism") : new CompoundTag();
                        CompoundTag storedData = new CompoundTag();

                        if (chemStack != null && chemStack != chemicalStackEmpty) {
                            java.lang.reflect.Method getAmountMethod = chemStack.getClass().getMethod("getAmount");
                            long amount = (long) getAmountMethod.invoke(chemStack);
                            java.lang.reflect.Method getChemicalMethod = chemStack.getClass().getMethod("getChemical");
                            Object chemical = getChemicalMethod.invoke(chemStack);

                            storedData.putLong("amount", amount);

                            if (chemical.equals(hydrogen)) {
                                storedData.putString("chemical", "hydrogen");
                            } else if (finalNitrogen != null && chemical.equals(finalNitrogen)) {
                                storedData.putString("chemical", "nitrogen");
                            } else {
                                storedData.putString("chemical", "oxygen");
                            }
                        } else {
                            storedData.putLong("amount", 0L);
                            storedData.putString("chemical", acceptedChemicals[0]);
                        }

                        mekData.put("stored", storedData);
                        mekData.putLong("capacity", capacity);
                        tag.put("mekanism", mekData);
                        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                    } catch (Exception e) {
                        AdAstraMekanized.LOGGER.debug("Failed to update NBT: {}", e.getMessage());
                    }
                }
            };

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Failed to create chemical handler: {}", e.getMessage());
            return null;
        }
    }

}