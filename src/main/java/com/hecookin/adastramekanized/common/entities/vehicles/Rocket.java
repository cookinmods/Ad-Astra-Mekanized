package com.hecookin.adastramekanized.common.entities.vehicles;

import com.hecookin.adastramekanized.common.blocks.LaunchPadBlock;
import com.hecookin.adastramekanized.common.constants.RocketConstants;
import com.hecookin.adastramekanized.common.menus.PlanetsMenu;
import com.hecookin.adastramekanized.common.menus.PlanetsMenuProvider;
import com.hecookin.adastramekanized.common.registry.ModItems;
import com.hecookin.adastramekanized.common.registry.ModSounds;
import com.hecookin.adastramekanized.common.tags.ModFluidTags;
import com.hecookin.adastramekanized.common.utils.FluidUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Rocket entity with 4 tiers, launch sequence, flight physics, and fuel system.
 * Adapted from Ad Astra for Mekanized integration.
 */
public class Rocket extends Vehicle {

    // Entity data accessors
    public static final EntityDataAccessor<Boolean> IS_LAUNCHING = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> LAUNCH_TICKS = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> HAS_LAUNCHED = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_LANDING = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Long> FUEL = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.LONG);
    public static final EntityDataAccessor<String> FUEL_TYPE = SynchedEntityData.defineId(Rocket.class, EntityDataSerializers.STRING);

    private final FluidTank fluidContainer;
    private final RocketProperties properties;

    private boolean launchpadBound;
    private float speed = 0.05f;
    private float angle;
    public boolean startedRocketSound;
    private boolean showFuelMessage = true;

    public Rocket(EntityType<?> type, Level level) {
        this(type, level, RocketTier.getTierProperties(type));
    }

    public Rocket(EntityType<?> type, Level level, RocketProperties properties) {
        super(type, level);
        this.properties = properties;
        this.fluidContainer = new FluidTank(RocketConstants.FUEL_TANK_CAPACITY) {
            @Override
            public boolean isFluidValid(FluidStack stack) {
                return stack.getFluid().is(properties.fuelTag());
            }
        };
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_LAUNCHING, false);
        builder.define(LAUNCH_TICKS, -1);
        builder.define(HAS_LAUNCHED, false);
        builder.define(IS_LANDING, false);
        builder.define(FUEL, 0L);
        builder.define(FUEL_TYPE, "minecraft:empty");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        entityData.set(IS_LAUNCHING, compound.getBoolean("Launching"));
        entityData.set(LAUNCH_TICKS, compound.getInt("LaunchTicks"));
        entityData.set(HAS_LAUNCHED, compound.getBoolean("HasLaunched"));
        entityData.set(IS_LANDING, compound.getBoolean("Landing"));
        speed = compound.getFloat("Speed");
        angle = compound.getFloat("Angle");
        fluidContainer.readFromNBT(registryAccess(), compound.getCompound("FluidTank"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Launching", isLaunching());
        compound.putInt("LaunchTicks", launchTicks());
        compound.putBoolean("HasLaunched", hasLaunched());
        compound.putBoolean("Landing", isLanding());
        compound.putFloat("Speed", speed);
        compound.putFloat("Angle", angle);
        compound.put("FluidTank", fluidContainer.writeToNBT(registryAccess(), new CompoundTag()));
    }

    public IFluidHandler fluidContainer() {
        return fluidContainer;
    }

    @Override
    public ItemStack getDropStack() {
        ItemStack stack = properties.item().getDefaultInstance();

        // Save fuel data to item
        FluidStack fluid = fluidContainer.getFluid();
        if (!fluid.isEmpty()) {
            CompoundTag tag = new CompoundTag();
            tag.put("Fluid", fluid.save(registryAccess()));
            stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(tag));
        }

        return stack;
    }

    public int tier() {
        return properties.tier();
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(net.minecraft.world.entity.Entity entity, EntityDimensions dimensions, float scale) {
        return new Vec3(0, properties.ridingOffset() + 0.3f, 0);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        // Calculate rocket's forward direction based on yaw
        double yawRad = Math.toRadians(getYRot());
        Vec3 forward = new Vec3(-Math.sin(yawRad), 0, Math.cos(yawRad)).normalize();

        // Place player 2.5 blocks in front of rocket
        Vec3 location = super.getDismountLocationForPassenger(passenger)
            .add(forward.multiply(2.5, 2, 2.5));

        // Adjust Y position to find ground
        for (int i = 0; i < 6; i++) {
            if (level().getBlockState(BlockPos.containing(location)).isAir()) {
                location = location.subtract(0, 1, 0);
            } else {
                // Found solid ground - add 0.5 blocks up to avoid being placed inside the block
                location = location.add(0, 0.5, 0);
                break;
            }
        }

        return location;
    }

    @Override
    public boolean isSafeToDismount(Player player) {
        return !isLaunching() && !hasLaunched();
    }

    @Override
    public boolean shouldSit() {
        return false;
    }

    @Override
    public boolean zoomOutCameraInThirdPerson() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        launchPadTick();

        if (canLaunch()) {
            initiateLaunchSequence();
            showFuelMessage = false;
        } else if (showFuelMessage && !level().isClientSide() && passengerHasSpaceDown() && getControllingPassenger() instanceof Player player) {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("Not enough fuel!"), true);
        }

        if (isLaunching()) {
            entityData.set(LAUNCH_TICKS, launchTicks() - 1);
            if (launchTicks() <= 0) launch();
            spawnSmokeParticles();
        } else if (hasLaunched()) {
            if (level().isClientSide() && !startedRocketSound) {
                startedRocketSound = true;
                com.hecookin.adastramekanized.client.sounds.SoundUtils.playRocketSound(this);
            }
            flightTick();
        } else if (isLanding()) {
            landingTick();
        }

        if (!level().isClientSide()) {
            FluidUtils.moveItemToContainer(inventory, fluidContainer, 0, 1, 0);
            FluidUtils.moveContainerToItem(inventory, fluidContainer, 0, 1, 0);

            FluidStack fluidStack = fluidContainer.getFluid();
            entityData.set(FUEL, (long) fluidStack.getAmount());
            entityData.set(FUEL_TYPE, BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()).toString());
        }
    }

    private void launchPadTick() {
        if (level().isClientSide() || tickCount % 5 != 0) return;
        if (isLaunching() || hasLaunched()) return;

        var state = level().getBlockState(blockPosition());
        if (!state.hasProperty(LaunchPadBlock.PART)) {
            if (launchpadBound) {
                drop();
                playSound(SoundEvents.NETHERITE_BLOCK_BREAK);
                discard();
            }
        } else {
            launchpadBound = true;
            if (state.getValue(LaunchPadBlock.POWERED)) {
                if (hasEnoughFuel()) initiateLaunchSequence();
            }
        }
    }

    private void flightTick() {
        if (!level().isClientSide() && getY() >= RocketConstants.ATMOSPHERE_LEAVE_HEIGHT) {
            if (getControllingPassenger() instanceof ServerPlayer player) {
                // Only open menu if not already open
                if (!(player.containerMenu instanceof PlanetsMenu)) {
                    openPlanetsScreen(player);
                }
            } else {
                explode();
            }
            return;
        }

        float xxa = -xxa();
        if (xxa != 0) {
            angle += xxa * 1;
        } else {
            angle *= 0.9f;
        }

        if (speed < 1) {
            speed += 0.005f;
        }

        angle = Mth.clamp(angle, -3, 3);
        setYRot(getYRot() + angle);

        var delta = getDeltaMovement();
        setDeltaMovement(delta.x(), speed, delta.z());

        spawnRocketParticles();
        burnEntitiesUnderRocket();
        if (isObstructed()) explode();
    }

    private void landingTick() {
        // Check if landed
        if (onGround()) {
            setLanding(false);
            angle = 0;
            speed = 0.05f;
            return;
        }

        var delta = getDeltaMovement();
        float xxa = -xxa();  // Turning control

        if (xxa != 0) {
            angle += xxa * 1;
        } else {
            angle *= 0.9f;
        }

        // Slow descent with space key
        if (passengerHasSpaceDown() && delta.y() < -0.05) {
            speed += 0.01f;
            fallDistance *= 0.9f;
            spawnRocketParticles();
        } else if (speed > -1.1) {
            speed -= 0.01f;
        }

        angle = Mth.clamp(angle, -3, 3);
        setYRot(getYRot() + angle);

        setDeltaMovement(delta.x(), speed, delta.z());

        // Water landing
        if (isInWater()) {
            setDeltaMovement(delta.x(), Math.min(0.06, delta.y() + 0.15), delta.z());
            speed *= 0.9f;
        }
    }

    public boolean canLaunch() {
        if (isLaunching() || hasLaunched()) return false;
        if (!hasEnoughFuel()) return false;
        return passengerHasSpaceDown();
    }

    public void initiateLaunchSequence() {
        entityData.set(IS_LAUNCHING, true);
        entityData.set(LAUNCH_TICKS, RocketConstants.COUNTDOWN_LENGTH);
        level().playSound(null, blockPosition(), ModSounds.ROCKET_LAUNCH.get(), SoundSource.AMBIENT, 10, 1);
        consumeFuel(false);
    }

    public void launch() {
        entityData.set(HAS_LAUNCHED, true);
        entityData.set(IS_LAUNCHING, false);
        entityData.set(LAUNCH_TICKS, -1);

        // Save launch coordinates for the player's current dimension
        if (!level().isClientSide() && getControllingPassenger() instanceof net.minecraft.server.level.ServerPlayer player) {
            com.hecookin.adastramekanized.common.util.LaunchCoordinateTracker.saveLaunchCoordinates(player);
        }
    }

    public boolean isLaunching() {
        return this.entityData.get(IS_LAUNCHING);
    }

    public int launchTicks() {
        return this.entityData.get(LAUNCH_TICKS);
    }

    public boolean hasLaunched() {
        return this.entityData.get(HAS_LAUNCHED);
    }

    public boolean isLanding() {
        return this.entityData.get(IS_LANDING);
    }

    public void setLanding(boolean landing) {
        entityData.set(IS_LANDING, landing);
        if (landing) {
            // Reset states when starting landing
            entityData.set(HAS_LAUNCHED, false);
            entityData.set(IS_LAUNCHING, false);
            speed = 0;  // Start descent slowly
        }
    }

    public void spawnSmokeParticles() {
        if (!level().isClientSide()) return;
        for (int i = 0; i < 6; i++) {
            level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                getX(), getY(), getZ(),
                Mth.nextDouble(level().random, -0.05, 0.05),
                Mth.nextDouble(level().random, -0.05, 0.05),
                Mth.nextDouble(level().random, -0.05, 0.05));
        }
    }

    public void spawnRocketParticles() {
        if (!level().isClientSide()) return;
        for (int i = 0; i < 20; i++) {
            level().addParticle(ParticleTypes.FLAME,
                getX(), getY() - 0.75, getZ(),
                Mth.nextDouble(level().random, -0.05, 0.05),
                Mth.nextDouble(level().random, -0.05, 0.05),
                Mth.nextDouble(level().random, -0.05, 0.05));
        }

        for (int i = 0; i < 5; i++) {
            level().addParticle(ParticleTypes.LARGE_SMOKE,
                getX(), getY() - 0.75, getZ(),
                Mth.nextDouble(level().random, -0.05, 0.05),
                Mth.nextDouble(level().random, -0.05, 0.05),
                Mth.nextDouble(level().random, -0.05, 0.05));
        }
    }

    public void burnEntitiesUnderRocket() {
        if (level().isClientSide()) return;
        for (var entity : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox()
            .inflate(2, 30, 2)
            .move(0, -37, 0), e -> true)) {
            if (entity.equals(getControllingPassenger())) continue;
            entity.setRemainingFireTicks(200);
            entity.hurt(level().damageSources().inFire(), 10);
        }
    }

    public boolean isObstructed() {
        return false;
    }

    public void explode() {
        level().explode(this, getX(), getY(), getZ(), 7 + tier() * 2, Level.ExplosionInteraction.TNT);
        discard();
    }

    /**
     * Opens the planet selection screen for the player.
     * Rocket continues flying while menu is open.
     */
    private void openPlanetsScreen(ServerPlayer player) {
        player.openMenu(new PlanetsMenuProvider());
        // Note: Rocket stays alive and continues flying at altitude
    }

    public boolean consumeFuel(boolean simulate) {
        if (level().isClientSide()) return false;
        int millibuckets = fluidContainer.getFluid().getFluid().is(ModFluidTags.EFFICIENT_FUEL)
            ? RocketConstants.EFFICIENT_FUEL_COST
            : RocketConstants.FUEL_COST;
        FluidStack extracted = fluidContainer.drain(millibuckets,
            simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
        return extracted.getAmount() >= millibuckets;
    }

    public boolean hasEnoughFuel() {
        return consumeFuel(true);
    }

    @Override
    public int getInventorySize() {
        return 10; // 8 storage slots + 2 fuel slots
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new com.hecookin.adastramekanized.common.menus.RocketMenu(containerId, inventory, this);
    }

    public FluidStack fluid() {
        return new FluidStack(
            BuiltInRegistries.FLUID.get(ResourceLocation.parse(entityData.get(FUEL_TYPE))),
            entityData.get(FUEL).intValue());
    }

    /**
     * Rocket tier configuration - maps entity types to properties
     */
    public static class RocketTier {
        private static Map<EntityType<?>, RocketProperties> ROCKET_TO_PROPERTIES;

        public static void init(EntityType<?> tier1, EntityType<?> tier2, EntityType<?> tier3, EntityType<?> tier4) {
            ROCKET_TO_PROPERTIES = Map.of(
                tier1, new RocketProperties(1, ModItems.TIER_1_ROCKET.get(), 1.0f, ModFluidTags.TIER_1_ROCKET_FUEL),
                tier2, new RocketProperties(2, ModItems.TIER_2_ROCKET.get(), 1.0f, ModFluidTags.TIER_2_ROCKET_FUEL),
                tier3, new RocketProperties(3, ModItems.TIER_3_ROCKET.get(), 1.0f, ModFluidTags.TIER_3_ROCKET_FUEL),
                tier4, new RocketProperties(4, ModItems.TIER_4_ROCKET.get(), 1.7f, ModFluidTags.TIER_4_ROCKET_FUEL)
            );
        }

        public static RocketProperties getTierProperties(EntityType<?> type) {
            if (ROCKET_TO_PROPERTIES == null) {
                // Fallback for early initialization
                return new RocketProperties(1, ModItems.TIER_1_ROCKET.get(), 1.0f, ModFluidTags.TIER_1_ROCKET_FUEL);
            }
            return ROCKET_TO_PROPERTIES.getOrDefault(type,
                new RocketProperties(1, ModItems.TIER_1_ROCKET.get(), 1.0f, ModFluidTags.TIER_1_ROCKET_FUEL));
        }
    }
}
