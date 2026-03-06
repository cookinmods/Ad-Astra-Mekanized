package com.hecookin.adastramekanized.common.entities.vehicles;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Lander entity for planetary descent.
 * Spawned when a rocket begins landing on a planet.
 * Contains the rocket in its inventory for recovery after landing.
 */
public class Lander extends Vehicle {

    private float speed;
    private float angle;
    public boolean startedRocketSound;

    public Lander(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        speed = compound.getFloat("Speed");
        angle = compound.getFloat("Angle");
        hasLanded = compound.getBoolean("HasLanded");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("Speed", speed);
        compound.putFloat("Angle", angle);
        compound.putBoolean("HasLanded", hasLanded);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float scale) {
        // Rider sits higher in the lander
        return new Vec3(0, 2.5f, 0);
    }

    @Override
    public boolean hideRider() {
        // Hide the rider inside the lander
        return true;
    }

    @Override
    public boolean zoomOutCameraInThirdPerson() {
        return true;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        // Dismount in front of the lander
        return super.getDismountLocationForPassenger(passenger)
            .add(passenger.getLookAngle().multiply(1, 0, 1)
                .normalize()
                .subtract(0, 2, 0));
    }

    @Override
    public boolean isSafeToDismount(Player player) {
        // Only allow dismounting when landed
        return onGround();
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        passenger.setYRot(getYRot());
        passenger.setYHeadRot(getYHeadRot());
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);

        // When player exits after landing, drop cargo and remove lander
        if (!level().isClientSide() && hasLanded) {
            handleSafeLanding();
        }
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        super.positionRider(passenger, callback);
        // Sync passenger rotation with lander angle
        passenger.setYRot(passenger.getYRot() + angle);
        passenger.setYHeadRot(passenger.getYHeadRot() + angle);
    }

    private boolean hasLanded = false;

    @Override
    public void tick() {
        super.tick();
        if (!onGround()) {
            flightTick();
        } else {
            // Reset angle when grounded
            angle = 0;
            hasLanded = true; // Mark as landed for safe dismount
        }
    }

    /**
     * Handle safe landing - drop rocket and cargo, remove lander
     * Called when player dismounts after successful landing
     */
    private void handleSafeLanding() {
        // Drop rocket item from slot 0
        ItemStack rocketStack = inventory().getItem(0);
        if (!rocketStack.isEmpty()) {
            spawnAtLocation(rocketStack);
        }

        // Drop cargo from slots 1-10
        for (int i = 1; i < 11; i++) {
            ItemStack stack = inventory().getItem(i);
            if (!stack.isEmpty()) {
                spawnAtLocation(stack);
            }
        }

        // Remove lander entity
        discard();
    }

    /**
     * Landing physics - controlled descent with thruster slowdown
     */
    private void flightTick() {
        Vec3 delta = getDeltaMovement();
        float xxa = -xxa(); // Left/right control

        // Rotation control
        if (xxa != 0) {
            angle += xxa * 1;
        } else {
            angle *= 0.9f; // Dampen rotation
        }

        // Vertical speed control with thrusters
        if (passengerHasSpaceDown() && delta.y() < -0.05) {
            // Player holding space - slow descent with thrusters
            speed += 0.01f;
            fallDistance *= 0.9f; // Reduce fall damage
            spawnLanderParticles();

            // Sound will be handled by client-side sound instance
            if (level().isClientSide() && !startedRocketSound) {
                startedRocketSound = true;
                com.hecookin.adastramekanized.client.sounds.SoundUtils.playLanderSound(this);
            }
        } else if (speed > -1.1) {
            // Free fall - accelerate downward (up to terminal velocity)
            speed -= 0.01f;
        }

        // Clamp turning angle
        angle = Mth.clamp(angle, -3, 3);

        // Apply rotation
        setYRot(getYRot() + angle);

        // Apply vertical movement
        setDeltaMovement(delta.x(), speed, delta.z());

        // Water landing - buoyancy
        if (isInWater()) {
            setDeltaMovement(delta.x(), Math.min(0.06, delta.y() + 0.15), delta.z());
            speed *= 0.9f;
        }
    }

    /**
     * Explode the lander (hard landing or damage)
     */
    public void explode() {
        if (level().isClientSide()) return;
        level().explode(
            this,
            getX(), getY(), getZ(),
            10, // Explosion power
            Level.ExplosionInteraction.TNT
        );
        discard();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource) {
        if (level().isClientSide()) return false;

        // Hard landing detection - >40 blocks = explosion
        if (fallDistance > 40 && onGround()) {
            explode();
            return true;
        }

        return false;
    }

    /**
     * Spawn thruster particles when slowing descent
     */
    public void spawnLanderParticles() {
        if (!level().isClientSide()) return;

        // Large flame particles from thrusters
        for (int i = 0; i < 10; i++) {
            level().addParticle(ParticleTypes.FLAME,
                getX(), getY() - 0.2, getZ(),
                Mth.nextDouble(level().random, -0.05, 0.05),
                Mth.nextDouble(level().random, -0.05, 0.05),
                Mth.nextDouble(level().random, -0.05, 0.05));
        }

        // Large smoke particles
        for (int i = 0; i < 10; i++) {
            level().addParticle(ParticleTypes.LARGE_SMOKE,
                getX(), getY() - 0.2, getZ(),
                Mth.nextDouble(level().random, -0.05, 0.05),
                Mth.nextDouble(level().random, -0.05, 0.05),
                Mth.nextDouble(level().random, -0.05, 0.05));
        }
    }

    @Override
    public ItemStack getDropStack() {
        // Lander doesn't drop itself - it contains the rocket in inventory
        return ItemStack.EMPTY;
    }

    @Override
    public int getInventorySize() {
        // 11 slots: 1 for rocket item + 10 for rocket's cargo
        return 11;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        // Lander has no GUI: it's purely a descent vehicle
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return null;
    }
}
