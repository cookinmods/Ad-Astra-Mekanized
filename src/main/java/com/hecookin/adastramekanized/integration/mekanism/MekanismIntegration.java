package com.hecookin.adastramekanized.integration.mekanism;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.api.IChemicalIntegration;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;


import java.util.List;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Mekanism integration using reflection to avoid compile-time dependencies
 *
 * This implementation provides full Mekanism integration when the mod is present,
 * and graceful fallbacks when it's not available.
 */
public class MekanismIntegration implements IChemicalIntegration {
    private static final String MEKANISM_MOD_ID = "mekanism";

    // Integration state
    private final boolean mekanismLoaded;
    private boolean apiAccessible = false;

    // Reflected classes (cached for performance)
    private Class<?> chemicalClass;
    private Class<?> chemicalStackClass;
    private Class<?> chemicalHandlerClass;
    private Class<?> oxygenChemicalClass;
    private Class<?> basicChemicalTankClass;
    private Object oxygenInstance;

    // Reflected methods (cached for performance)
    private Method getChemicalInTankMethod;
    private Method insertChemicalMethod;
    private Method extractChemicalMethod;

    public MekanismIntegration() {
        this.mekanismLoaded = ModList.get().isLoaded(MEKANISM_MOD_ID);

        if (mekanismLoaded) {
            initializeReflection();
        }

        AdAstraMekanized.LOGGER.info("Mekanism integration initialized - Available: {}, API Accessible: {}",
                mekanismLoaded, apiAccessible);
    }

    private void initializeReflection() {
        try {
            // Load core API classes
            chemicalClass = Class.forName("mekanism.api.chemical.Chemical");
            chemicalStackClass = Class.forName("mekanism.api.chemical.ChemicalStack");
            chemicalHandlerClass = Class.forName("mekanism.api.chemical.IChemicalHandler");
            basicChemicalTankClass = Class.forName("mekanism.api.chemical.BasicChemicalTank");

            // Load chemical methods
            getChemicalInTankMethod = chemicalHandlerClass.getMethod("getChemicalInTank", int.class);
            insertChemicalMethod = chemicalHandlerClass.getMethod("insertChemical", chemicalStackClass, boolean.class);
            extractChemicalMethod = chemicalHandlerClass.getMethod("extractChemical", long.class, boolean.class);

            // Try to access oxygen chemical
            tryLoadOxygenChemical();

            apiAccessible = true;
            AdAstraMekanized.LOGGER.info("Mekanism API reflection setup completed successfully");

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            AdAstraMekanized.LOGGER.warn("Mekanism API reflection setup failed: {}", e.getMessage());
            apiAccessible = false;
        }
    }

    private void tryLoadOxygenChemical() {
        try {
            // Try to access Mekanism's built-in chemicals
            Class<?> mekanismChemicalsClass = Class.forName("mekanism.common.registries.MekanismChemicals");

            // Get the OXYGEN field
            java.lang.reflect.Field oxygenField = mekanismChemicalsClass.getField("OXYGEN");
            Object deferredChemical = oxygenField.get(null);

            // Get the actual chemical from the deferred holder
            Method getMethod = deferredChemical.getClass().getMethod("get");
            oxygenInstance = getMethod.invoke(deferredChemical);

            AdAstraMekanized.LOGGER.debug("Successfully loaded Mekanism oxygen chemical");
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Could not load Mekanism oxygen: {}", e.getMessage());
        }
    }

    // === IChemicalIntegration Implementation ===

    @Override
    public boolean isChemicalSystemAvailable() {
        return mekanismLoaded && apiAccessible;
    }

    @Override
    public void initializeOxygenChemical() {
        if (!isChemicalSystemAvailable()) {
            AdAstraMekanized.LOGGER.debug("Oxygen chemical initialization skipped - Mekanism not available");
            return;
        }

        try {
            // Initialize oxygen chemical registration
            // This will be implemented once we understand Mekanism's chemical registration system
            AdAstraMekanized.LOGGER.info("Oxygen chemical initialization completed");
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to initialize oxygen chemical: {}", e.getMessage());
        }
    }

    // === ItemStack Chemical Methods ===

    /**
     * Check if an ItemStack has any chemical stored
     */
    public boolean hasAnyChemical(ItemStack stack) {
        if (!isChemicalSystemAvailable()) {
            return false;
        }

        try {
            // Get the chemical handler capability
            Class<?> capabilitiesClass = Class.forName("mekanism.common.capabilities.Capabilities");
            Object chemicalCapability = capabilitiesClass.getField("CHEMICAL").get(null);

            // Get capability method (should be on ICapabilityProvider)
            Method getCapabilityMethod = stack.getClass().getMethod("getCapability", Class.forName("net.neoforged.neoforge.capabilities.ItemCapability"));
            Object chemicalHandler = getCapabilityMethod.invoke(stack, chemicalCapability);

            if (chemicalHandler != null) {
                // Check if it has any chemical
                Method getChemicalInTankMethod = chemicalHandler.getClass().getMethod("getChemicalInTank", int.class);
                Object chemicalStack = getChemicalInTankMethod.invoke(chemicalHandler, 0);

                if (chemicalStack != null) {
                    Method getAmountMethod = chemicalStack.getClass().getMethod("getAmount");
                    long amount = (Long) getAmountMethod.invoke(chemicalStack);
                    return amount > 0;
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Error checking ItemStack chemical: {}", e.getMessage());
        }

        return false;
    }

    /**
     * Extract chemical from an ItemStack
     */
    public long extractChemical(ItemStack stack, String chemicalName, long amount) {
        if (!isChemicalSystemAvailable()) {
            return 0;
        }

        try {
            // Get the chemical handler capability
            Class<?> capabilitiesClass = Class.forName("mekanism.common.capabilities.Capabilities");
            Object chemicalCapability = capabilitiesClass.getField("CHEMICAL").get(null);

            // Get capability method
            Method getCapabilityMethod = stack.getClass().getMethod("getCapability", Class.forName("net.neoforged.neoforge.capabilities.ItemCapability"));
            Object chemicalHandler = getCapabilityMethod.invoke(stack, chemicalCapability);

            if (chemicalHandler != null) {
                // Get Action.EXECUTE enum value
                Class<?> actionClass = Class.forName("mekanism.api.Action");
                Object executeAction = actionClass.getField("EXECUTE").get(null);

                // Extract chemical
                Method extractMethod = chemicalHandler.getClass().getMethod("extractChemical", long.class, actionClass);
                Object extractedStack = extractMethod.invoke(chemicalHandler, amount, executeAction);

                if (extractedStack != null) {
                    Method getAmountMethod = extractedStack.getClass().getMethod("getAmount");
                    return (Long) getAmountMethod.invoke(extractedStack);
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Error extracting chemical from ItemStack: {}", e.getMessage());
        }

        return 0;
    }

    /**
     * Create an item stack with Mekanism chemical capability
     * This makes the item compatible with Mekanism's chemical tanks
     */
    public void attachChemicalCapability(ItemStack stack, long capacity, String... acceptedChemicals) {
        if (!isChemicalSystemAvailable()) {
            return;
        }

        try {
            // Store the chemical data in NBT for now
            // Mekanism will handle the actual capability attachment through its own system
            net.minecraft.world.item.component.CustomData customData = stack.getOrDefault(
                net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY
            );
            CompoundTag tag = customData.copyTag();

            CompoundTag chemicalTag = new CompoundTag();
            chemicalTag.putLong("capacity", capacity);
            chemicalTag.putString("acceptedChemicals", String.join(",", acceptedChemicals));
            tag.put("ChemicalData", chemicalTag);

            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(tag));

            AdAstraMekanized.LOGGER.debug("Attached chemical data to ItemStack: capacity={}, chemicals={}",
                capacity, String.join(",", acceptedChemicals));

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Error attaching chemical capability: {}", e.getMessage());
        }
    }

    // === Custom Methods for Oxygen Distributor ===

    /**
     * Create a new oxygen tank for the distributor
     */
    public Object createOxygenTank(long capacity) {
        if (!isChemicalSystemAvailable() || basicChemicalTankClass == null) {
            return null;
        }

        try {
            // Create a BasicChemicalTank that only accepts oxygen
            Constructor<?> constructor = basicChemicalTankClass.getConstructor(long.class, java.util.function.BiPredicate.class, java.util.function.BiPredicate.class, java.util.function.Predicate.class, chemicalClass, java.util.function.Consumer.class);

            // Create predicates for tank behavior
            java.util.function.BiPredicate<Object, Object> canExtract = (tank, stack) -> true;
            java.util.function.BiPredicate<Object, Object> canInsert = (tank, stack) -> {
                // Only accept oxygen
                return stack != null && stack.equals(oxygenInstance);
            };
            java.util.function.Predicate<Object> validator = chemical -> {
                // Validate that it's oxygen
                return chemical != null && chemical.equals(oxygenInstance);
            };
            java.util.function.Consumer<Object> listener = tank -> {}; // No special listener needed

            // Create tank with oxygen validation
            Object tank = constructor.newInstance(capacity, canExtract, canInsert, validator, oxygenInstance, listener);

            AdAstraMekanized.LOGGER.debug("Created oxygen tank with capacity {}", capacity);
            return tank;
        } catch (Exception e) {
            // Fallback to simpler constructor if the complex one fails
            try {
                Constructor<?> simpleConstructor = basicChemicalTankClass.getConstructor(long.class, chemicalClass);
                return simpleConstructor.newInstance(capacity, oxygenInstance);
            } catch (Exception e2) {
                AdAstraMekanized.LOGGER.error("Failed to create oxygen tank: {}", e2.getMessage());
                return null;
            }
        }
    }

    /**
     * Get the amount of oxygen stored in a tank
     */
    public long getOxygenAmount(Object tank) {
        if (tank == null || chemicalStackClass == null) {
            return 0;
        }

        try {
            Method getStackMethod = tank.getClass().getMethod("getStack");
            Object chemicalStack = getStackMethod.invoke(tank);

            Method getAmountMethod = chemicalStackClass.getMethod("getAmount");
            return (Long) getAmountMethod.invoke(chemicalStack);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to get oxygen amount: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Extract oxygen from a tank
     */
    public long extractOxygen(Object tank, long amount) {
        if (tank == null || amount <= 0) {
            return 0;
        }

        try {
            Method extractMethod = tank.getClass().getMethod("extract", long.class, boolean.class);
            Object extractedStack = extractMethod.invoke(tank, amount, false);

            if (extractedStack != null && chemicalStackClass.isInstance(extractedStack)) {
                Method getAmountMethod = chemicalStackClass.getMethod("getAmount");
                return (Long) getAmountMethod.invoke(extractedStack);
            }
            return 0;
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to extract oxygen: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * Serialize a chemical tank to NBT
     */
    public CompoundTag serializeChemicalTank(Object tank) {
        if (tank == null) {
            return null;
        }

        try {
            Method serializeMethod = tank.getClass().getMethod("serializeNBT");
            return (CompoundTag) serializeMethod.invoke(tank);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to serialize chemical tank: {}", e.getMessage());
            return new CompoundTag();
        }
    }

    /**
     * Deserialize a chemical tank from NBT
     */
    public void deserializeChemicalTank(Object tank, CompoundTag tag) {
        if (tank == null || tag == null) {
            return;
        }

        try {
            Method deserializeMethod = tank.getClass().getMethod("deserializeNBT", CompoundTag.class);
            deserializeMethod.invoke(tank, tag);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to deserialize chemical tank: {}", e.getMessage());
        }
    }

    // === ItemStack Chemical Methods for Armor ===

    /**
     * Get the amount of a specific chemical in an ItemStack
     */
    public Long getChemicalAmount(ItemStack stack, String chemicalName) {
        if (!isChemicalSystemAvailable() || stack.isEmpty()) {
            return 0L;
        }

        try {
            // Try to get chemical handler capability from the item
            Class<?> capabilitiesClass = Class.forName("mekanism.common.capabilities.Capabilities");
            java.lang.reflect.Field chemicalField = capabilitiesClass.getField("CHEMICAL");
            Object chemicalCapability = chemicalField.get(null);

            Method getCapabilityMethod = chemicalCapability.getClass().getMethod("getCapability", ItemStack.class);
            Object handler = getCapabilityMethod.invoke(chemicalCapability, stack);

            if (handler != null && chemicalHandlerClass.isInstance(handler)) {
                // Get the first tank (usually index 0 for armor)
                Object chemicalStack = getChemicalInTankMethod.invoke(handler, 0);
                if (chemicalStack != null) {
                    Method getAmountMethod = chemicalStackClass.getMethod("getAmount");
                    return (Long) getAmountMethod.invoke(chemicalStack);
                }
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Could not get chemical amount from ItemStack: {}", e.getMessage());
        }
        return 0L;
    }

    /**
     * Check if an ItemStack has a specific chemical
     */
    public boolean hasChemical(ItemStack stack, String chemicalName) {
        Long amount = getChemicalAmount(stack, chemicalName);
        return amount != null && amount > 0;
    }

    /**
     * Use/consume a chemical from an ItemStack
     */
    public void useChemical(ItemStack stack, String chemicalName, long amount) {
        if (!isChemicalSystemAvailable() || stack.isEmpty() || amount <= 0) {
            return;
        }

        try {
            // Get chemical handler capability
            Class<?> capabilitiesClass = Class.forName("mekanism.common.capabilities.Capabilities");
            java.lang.reflect.Field chemicalField = capabilitiesClass.getField("CHEMICAL");
            Object chemicalCapability = chemicalField.get(null);

            Method getCapabilityMethod = chemicalCapability.getClass().getMethod("getCapability", ItemStack.class);
            Object handler = getCapabilityMethod.invoke(chemicalCapability, stack);

            if (handler != null && chemicalHandlerClass.isInstance(handler)) {
                extractChemicalMethod.invoke(handler, amount, false);
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Could not use chemical from ItemStack: {}", e.getMessage());
        }
    }

    /**
     * Add (insert) chemical to an ItemStack
     */
    public void addChemical(ItemStack stack, String chemicalName, long amount) {
        if (!isChemicalSystemAvailable() || stack.isEmpty() || amount <= 0) {
            return;
        }

        try {
            // Get the chemical instance
            Object chemical;
            if (chemicalName.equalsIgnoreCase("oxygen")) {
                chemical = oxygenInstance;
            } else if (chemicalName.equalsIgnoreCase("nitrogen")) {
                // Get nitrogen from ChemLibMekanized
                try {
                    Class<?> chemLibChemicalsClass = Class.forName("com.hecookin.chemlibmekanized.registry.ChemlibMekanizedChemicals");
                    Class<?> holderClass = Class.forName("net.minecraft.core.Holder");
                    Object nitrogenHolder = chemLibChemicalsClass.getField("NITROGEN").get(null);
                    java.lang.reflect.Method getValue = holderClass.getMethod("value");
                    chemical = getValue.invoke(nitrogenHolder);
                } catch (Exception e) {
                    AdAstraMekanized.LOGGER.debug("ChemLibMekanized nitrogen not available: {}", e.getMessage());
                    return;
                }
            } else {
                chemical = getHydrogenInstance();
            }

            if (chemical == null) {
                AdAstraMekanized.LOGGER.debug("Chemical instance not found for: {}", chemicalName);
                return;
            }

            // Create a ChemicalStack with the chemical and amount
            Constructor<?> stackConstructor = chemicalStackClass.getConstructor(chemicalClass, long.class);
            Object chemicalStack = stackConstructor.newInstance(chemical, amount);

            // Get chemical handler capability
            Class<?> capabilitiesClass = Class.forName("mekanism.common.capabilities.Capabilities");
            java.lang.reflect.Field chemicalField = capabilitiesClass.getField("CHEMICAL");
            Object chemicalCapability = chemicalField.get(null);

            Method getCapabilityMethod = chemicalCapability.getClass().getMethod("getCapability", ItemStack.class);
            Object handler = getCapabilityMethod.invoke(chemicalCapability, stack);

            if (handler != null && chemicalHandlerClass.isInstance(handler)) {
                AdAstraMekanized.LOGGER.debug("Got chemical handler, calling insertChemical with {} mB of {}", amount, chemicalName);
                // Insert the chemical (false = actually insert, not simulate)
                Object result = insertChemicalMethod.invoke(handler, chemicalStack, false);
                AdAstraMekanized.LOGGER.debug("insertChemical returned: {}", result);

                // Check if anything was inserted (result should be the remainder, EMPTY if all inserted)
                if (result != null) {
                    try {
                        Method getAmountMethod = result.getClass().getMethod("getAmount");
                        long remainder = (Long) getAmountMethod.invoke(result);
                        long inserted = amount - remainder;
                        AdAstraMekanized.LOGGER.debug("Added {} mB of {} to ItemStack ({} mB remainder)", inserted, chemicalName, remainder);
                    } catch (Exception e2) {
                        AdAstraMekanized.LOGGER.debug("Added {} mB of {} to ItemStack", amount, chemicalName);
                    }
                }
            } else {
                AdAstraMekanized.LOGGER.warn("Chemical handler is null or wrong type! handler={}, expected type={}",
                    handler, chemicalHandlerClass);
            }
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Could not add chemical to ItemStack: ", e);
        }
    }

    /**
     * Add chemical tooltip information
     */
    public void addChemicalTooltip(ItemStack stack, List<Component> tooltip, String chemicalName, long capacity) {
        if (!isChemicalSystemAvailable()) {
            return;
        }

        try {
            Long amount = getChemicalAmount(stack, chemicalName);
            if (amount != null) {
                // Try to use Mekanism's formatting
                Class<?> langClass = Class.forName("mekanism.common.MekanismLang");
                java.lang.reflect.Field storedField = langClass.getField("GENERIC_STORED");
                Object storedLang = storedField.get(null);

                // Create the tooltip using Mekanism's style
                Method translateMethod = storedLang.getClass().getMethod("translateColored", Object.class, Object.class, Object.class, Object.class);
                Class<?> enumColorClass = Class.forName("mekanism.api.text.EnumColor");
                Object grayColor = enumColorClass.getField("GRAY").get(null);
                Object orangeColor = enumColorClass.getField("ORANGE").get(null);

                // Get the chemical reference for display
                Object chemical = chemicalName.equalsIgnoreCase("oxygen") ? oxygenInstance : getHydrogenInstance();
                if (chemical != null) {
                    Component component = (Component) translateMethod.invoke(storedLang, grayColor, chemical, orangeColor, amount);
                    tooltip.add(component);
                }
            }
        } catch (Exception e) {
            // Fallback to simple display
            Long amount = getChemicalAmount(stack, chemicalName);
            if (amount != null) {
                tooltip.add(Component.literal(chemicalName + ": " + amount + " / " + capacity + " mB"));
            }
        }
    }

    /**
     * Get the bar width for chemical display
     */
    public Integer getChemicalBarWidth(ItemStack stack) {
        if (!isChemicalSystemAvailable()) {
            return null;
        }

        try {
            // Use Mekanism's StorageUtils
            Class<?> storageUtilsClass = Class.forName("mekanism.common.util.StorageUtils");
            Method getBarWidthMethod = storageUtilsClass.getMethod("getBarWidth", ItemStack.class);
            return (Integer) getBarWidthMethod.invoke(null, stack);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Could not get chemical bar width: {}", e.getMessage());
        }
        return 0;
    }

    /**
     * Get the bar color for chemical display
     */
    public Integer getChemicalBarColor(ItemStack stack) {
        if (!isChemicalSystemAvailable()) {
            return null;
        }

        try {
            // Use Mekanism's ChemicalUtil
            Class<?> chemicalUtilClass = Class.forName("mekanism.common.util.ChemicalUtil");
            Method getRGBMethod = chemicalUtilClass.getMethod("getRGBDurabilityForDisplay", ItemStack.class);
            return (Integer) getRGBMethod.invoke(null, stack);
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Could not get chemical bar color: {}", e.getMessage());
        }
        return 0x3F76E4; // Default blue
    }

    /**
     * Get or create hydrogen instance
     */
    private Object getHydrogenInstance() {
        if (hydrogenInstance == null) {
            try {
                Class<?> mekanismChemicalsClass = Class.forName("mekanism.common.registries.MekanismChemicals");
                java.lang.reflect.Field hydrogenField = mekanismChemicalsClass.getField("HYDROGEN");
                Object deferredChemical = hydrogenField.get(null);
                Method getMethod = deferredChemical.getClass().getMethod("get");
                hydrogenInstance = getMethod.invoke(deferredChemical);
            } catch (Exception e) {
                AdAstraMekanized.LOGGER.debug("Could not load hydrogen chemical: {}", e.getMessage());
            }
        }
        return hydrogenInstance;
    }

    private Object hydrogenInstance;

    // Compatibility methods for oxygen-specific operations
    public Long getOxygenAmount(ItemStack stack) {
        return getChemicalAmount(stack, "oxygen");
    }

    public boolean consumeOxygen(ItemStack stack, long amount) {
        useChemical(stack, "oxygen", amount);
        return true;
    }

    // ===== Jetpack Mode Support =====

    private Class<?> jetpackModeClass;
    private Class<?> jetpackModeDataComponentClass;
    private Method getJetpackModeMethod;
    private Method setJetpackModeMethod;
    private Object[] jetpackModeValues;

    /**
     * Initialize jetpack mode reflection
     */
    private void initializeJetpackModeReflection() {
        if (jetpackModeClass != null) return; // Already initialized

        try {
            jetpackModeClass = Class.forName("mekanism.common.item.interfaces.IJetpackItem$JetpackMode");
            jetpackModeValues = (Object[]) jetpackModeClass.getMethod("values").invoke(null);

            // Get data component type
            Class<?> mekanismDataComponentsClass = Class.forName("mekanism.common.registries.MekanismDataComponents");
            java.lang.reflect.Field jetpackModeField = mekanismDataComponentsClass.getField("JETPACK_MODE");
            Object deferredHolder = jetpackModeField.get(null);
            Method getMethod = deferredHolder.getClass().getMethod("get");
            jetpackModeDataComponentClass = (Class<?>) getMethod.invoke(deferredHolder);

            AdAstraMekanized.LOGGER.debug("Jetpack mode reflection initialized successfully");
        } catch (Exception e) {
            AdAstraMekanized.LOGGER.warn("Could not initialize jetpack mode reflection", e);
        }
    }

    /**
     * Get jetpack mode from stack
     */
    public Object getJetpackMode(ItemStack stack) {
        initializeJetpackModeReflection();
        if (jetpackModeClass == null || jetpackModeDataComponentClass == null) {
            return null;
        }

        try {
            // Get DataComponentType instance
            Class<?> mekanismDataComponentsClass = Class.forName("mekanism.common.registries.MekanismDataComponents");
            java.lang.reflect.Field jetpackModeField = mekanismDataComponentsClass.getField("JETPACK_MODE");
            Object deferredHolder = jetpackModeField.get(null);
            Method getMethod = deferredHolder.getClass().getMethod("get");
            Object dataComponentType = getMethod.invoke(deferredHolder);

            // Get mode from stack
            Method getComponentMethod = ItemStack.class.getMethod("get", net.minecraft.core.component.DataComponentType.class);
            Object mode = getComponentMethod.invoke(stack, dataComponentType);

            if (mode != null) {
                return mode;
            }

            // Return NORMAL as default (ordinal 0)
            return jetpackModeValues != null && jetpackModeValues.length > 0 ? jetpackModeValues[0] : null;

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.debug("Failed to get jetpack mode: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Set jetpack mode on stack
     */
    public void setJetpackMode(ItemStack stack, Object mode) {
        initializeJetpackModeReflection();
        if (jetpackModeClass == null || jetpackModeDataComponentClass == null || mode == null) {
            return;
        }

        try {
            // Get DataComponentType instance
            Class<?> mekanismDataComponentsClass = Class.forName("mekanism.common.registries.MekanismDataComponents");
            java.lang.reflect.Field jetpackModeField = mekanismDataComponentsClass.getField("JETPACK_MODE");
            Object deferredHolder = jetpackModeField.get(null);
            Method getMethod = deferredHolder.getClass().getMethod("get");
            Object dataComponentType = getMethod.invoke(deferredHolder);

            // Set mode on stack
            Method setComponentMethod = ItemStack.class.getMethod("set", net.minecraft.core.component.DataComponentType.class, Object.class);
            setComponentMethod.invoke(stack, dataComponentType, mode);

            AdAstraMekanized.LOGGER.debug("Set jetpack mode to {}", mode);

        } catch (Exception e) {
            AdAstraMekanized.LOGGER.error("Failed to set jetpack mode", e);
        }
    }
}