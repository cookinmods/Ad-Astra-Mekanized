package com.hecookin.adastramekanized.common.menus;

import com.hecookin.adastramekanized.common.blockentities.machines.GravityNormalizerBlockEntity;
import com.hecookin.adastramekanized.common.registry.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.block.entity.BlockEntity;

public class GravityNormalizerMenu extends AbstractContainerMenu {

    private final GravityNormalizerBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    // Data slots for synchronization
    private int energyLow, energyHigh;
    private int argonLow, argonHigh;
    private int visibility = -1;
    private int colorIndex = -1;
    private int machineState = 1;
    private int targetGravityInt = 100; // Gravity * 100 for precision (100 = 1.0)

    // Usage tracking slots
    private int argonUsage = 0;
    private int energyUsage = 0;
    private int blockCount = 0;
    private int currentRadius = 0;

    // Client constructor
    public GravityNormalizerMenu(int containerId, Inventory playerInventory, FriendlyByteBuf buf) {
        this(containerId, playerInventory, playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    // Server constructor
    public GravityNormalizerMenu(int containerId, Inventory playerInventory, BlockEntity blockEntity) {
        super(ModMenuTypes.GRAVITY_NORMALIZER.get(), containerId);

        if (blockEntity instanceof GravityNormalizerBlockEntity normalizer) {
            this.blockEntity = normalizer;
            this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        } else {
            throw new IllegalArgumentException("Invalid block entity type");
        }

        // Add player inventory slots
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        // Add data slots for synchronization
        addDataSlots();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, blockEntity.getBlockState().getBlock());
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 162 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 220));
        }
    }

    private void addDataSlots() {
        // Energy low bits
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) (blockEntity.getEnergyStorage().getEnergyStored() & 0xFFFF);
            }

            @Override
            public void set(int value) {
                energyLow = value;
            }
        });

        // Energy high bits
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) ((blockEntity.getEnergyStorage().getEnergyStored() >> 16) & 0xFFFF);
            }

            @Override
            public void set(int value) {
                energyHigh = value;
            }
        });

        // Argon low bits
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) (blockEntity.getArgonTank().getStored() & 0xFFFF);
            }

            @Override
            public void set(int value) {
                argonLow = value;
            }
        });

        // Argon high bits
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) ((blockEntity.getArgonTank().getStored() >> 16) & 0xFFFF);
            }

            @Override
            public void set(int value) {
                argonHigh = value;
            }
        });

        // Visibility state
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getZoneVisibility() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                visibility = value;
            }
        });

        // Color index
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getZoneColor();
            }

            @Override
            public void set(int value) {
                colorIndex = value;
            }
        });

        // Machine state
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getMachineState();
            }

            @Override
            public void set(int value) {
                machineState = value;
            }
        });

        // Target gravity (as int * 100 for 2 decimal precision)
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) (blockEntity.getTargetGravity() * 100);
            }

            @Override
            public void set(int value) {
                targetGravityInt = value;
            }
        });

        // Argon usage (mB/tick * 100)
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) (blockEntity.getArgonUsage() * 100);
            }

            @Override
            public void set(int value) {
                argonUsage = value;
            }
        });

        // Energy usage (FE/tick * 100)
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return (int) (blockEntity.getEnergyUsage() * 100);
            }

            @Override
            public void set(int value) {
                energyUsage = value;
            }
        });

        // Block count
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getNormalizedBlockCount();
            }

            @Override
            public void set(int value) {
                blockCount = value;
            }
        });

        // Current radius
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getCurrentRadius();
            }

            @Override
            public void set(int value) {
                currentRadius = value;
            }
        });
    }

    public GravityNormalizerBlockEntity getBlockEntity() {
        return blockEntity;
    }

    // Data synchronization for energy
    public long getEnergy() {
        if (blockEntity.getLevel().isClientSide()) {
            return ((long) energyHigh << 16) | (energyLow & 0xFFFF);
        }
        return blockEntity.getEnergyStorage().getEnergyStored();
    }

    public long getMaxEnergy() {
        return blockEntity.getEnergyStorage().getMaxEnergyStored();
    }

    // Data synchronization for argon
    public long getChemicalAmount() {
        if (blockEntity.getLevel().isClientSide()) {
            return ((long) argonHigh << 16) | (argonLow & 0xFFFF);
        }
        return blockEntity.getArgonTank().getStored();
    }

    public long getChemicalCapacity() {
        return blockEntity.getArgonTank().getCapacity();
    }

    // Visibility state
    public boolean getVisibility() {
        if (blockEntity.getLevel().isClientSide()) {
            if (visibility == -1) {
                return blockEntity.getZoneVisibility();
            }
            return visibility != 0;
        }
        return blockEntity.getZoneVisibility();
    }

    // Color index
    public int getColorIndex() {
        if (blockEntity.getLevel().isClientSide()) {
            if (colorIndex == -1) {
                return blockEntity.getZoneColor();
            }
            return colorIndex;
        }
        return blockEntity.getZoneColor();
    }

    // Machine state
    public int getMachineState() {
        if (blockEntity.getLevel().isClientSide()) {
            return machineState;
        }
        return blockEntity.getMachineState();
    }

    // Target gravity
    public float getTargetGravity() {
        if (blockEntity.getLevel().isClientSide()) {
            return targetGravityInt / 100.0f;
        }
        return blockEntity.getTargetGravity();
    }

    // Usage data
    public float getArgonUsage() {
        return blockEntity.getLevel().isClientSide() ? argonUsage / 100.0f : blockEntity.getArgonUsage();
    }

    public float getEnergyUsage() {
        return blockEntity.getLevel().isClientSide() ? energyUsage / 100.0f : blockEntity.getEnergyUsage();
    }

    public int getBlockCount() {
        return blockEntity.getLevel().isClientSide() ? blockCount : blockEntity.getNormalizedBlockCount();
    }

    public int getCurrentRadius() {
        return blockEntity.getLevel().isClientSide() ? currentRadius : blockEntity.getCurrentRadius();
    }
}
