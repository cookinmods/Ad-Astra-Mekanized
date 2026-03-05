package com.hecookin.adastramekanized.common.blockentities;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.data.DistributorLinkData;
import com.hecookin.adastramekanized.common.items.OxygenNetworkController;
import com.hecookin.adastramekanized.common.menus.OxygenMonitorMenu;
import com.hecookin.adastramekanized.common.registry.ModBlockEntityTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the wall-mounted oxygen network monitor
 */
public class OxygenNetworkMonitorBlockEntity extends BlockEntity implements MenuProvider {
    private CompoundTag pairingData = new CompoundTag();
    private ItemStack pairedController = ItemStack.EMPTY;
    private long lastUpdateTime = 0;

    public OxygenNetworkMonitorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.OXYGEN_NETWORK_MONITOR.get(), pos, state);
    }

    /**
     * Pair this monitor with a controller
     */
    public void pairWithController(ItemStack controller, Player player) {
        if (!(controller.getItem() instanceof OxygenNetworkController)) {
            return;
        }

        // Generate a unique pairing ID
        long pairingId = System.currentTimeMillis();

        pairingData = new CompoundTag();
        pairingData.putBoolean("Paired", true);
        pairingData.putLong("PairingId", pairingId);

        // Store controller name if it has one
        if (controller.has(DataComponents.CUSTOM_NAME)) {
            pairingData.putString("ControllerName", controller.getHoverName().getString());
        } else {
            pairingData.putString("ControllerName", "Oxygen Controller");
        }

        // Save pairing data to the controller
        savePairingDataToItem(controller, pairingData);

        // Store a copy of the controller for reference
        this.pairedController = controller.copy();

        setChanged();

        // Sync to client
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }

        player.displayClientMessage(
            Component.literal("Monitor paired with controller")
                .withStyle(ChatFormatting.GREEN),
            true
        );

        AdAstraMekanized.LOGGER.info("Paired oxygen monitor at {} with controller (ID: {})",
            worldPosition, pairingId);
    }

    /**
     * Open the monitor menu
     */
    public void openMenu(ServerPlayer player) {
        if (!isPaired()) {
            return;
        }

        // Find the controller in player's inventory
        ItemStack controller = findPairedController(player);
        if (controller.isEmpty()) {
            player.displayClientMessage(
                Component.literal("Paired controller not found in inventory")
                    .withStyle(ChatFormatting.RED),
                true
            );
            return;
        }

        // Update distributor statuses
        DistributorLinkData linkData = OxygenNetworkController.getOrCreateLinkData(controller);
        OxygenNetworkController.updateDistributorStatuses(level, linkData);
        OxygenNetworkController.saveLinkData(controller, linkData);

        // Open the menu
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable("block.adastramekanized.oxygen_network_monitor");
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player p) {
                return new OxygenMonitorMenu(containerId, playerInventory,
                    OxygenNetworkMonitorBlockEntity.this.createMonitorItem(), controller);
            }
        }, buffer -> {
            ItemStack.STREAM_CODEC.encode(buffer, createMonitorItem());
            ItemStack.STREAM_CODEC.encode(buffer, controller);
            buffer.writeNbt(linkData.toNbt());
        });
    }

    /**
     * Create a temporary monitor item for menu purposes
     */
    private ItemStack createMonitorItem() {
        // Create a dummy item stack for menu handling
        ItemStack monitor = ItemStack.EMPTY;
        savePairingDataToItem(monitor, pairingData);
        return monitor;
    }

    /**
     * Find the paired controller in player's inventory
     */
    private ItemStack findPairedController(Player player) {
        if (!isPaired()) {
            return ItemStack.EMPTY;
        }

        long pairingId = pairingData.getLong("PairingId");

        // Check all inventory slots for a controller with matching pairing ID
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof OxygenNetworkController) {
                CompoundTag controllerPairing = getPairingDataFromItem(stack);
                if (controllerPairing != null &&
                    controllerPairing.getBoolean("Paired") &&
                    controllerPairing.getLong("PairingId") == pairingId) {
                    return stack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    public boolean isPaired() {
        return pairingData.getBoolean("Paired");
    }

    public void onRemoved() {
        // Clear pairing when block is broken
        pairingData = new CompoundTag();
        pairedController = ItemStack.EMPTY;
    }

    // Helper methods for item pairing data
    private void savePairingDataToItem(ItemStack stack, CompoundTag pairingData) {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.getUnsafe();
        if (tag == null) {
            tag = new CompoundTag();
        } else {
            tag = tag.copy();
        }
        tag.put("PairingData", pairingData);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    private CompoundTag getPairingDataFromItem(ItemStack stack) {
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.getUnsafe();
            if (tag != null && tag.contains("PairingData")) {
                return tag.getCompound("PairingData");
            }
        }
        return null;
    }

    // NBT Serialization
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.put("PairingData", pairingData);
        if (!pairedController.isEmpty()) {
            tag.put("PairedController", pairedController.save(provider));
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        pairingData = tag.getCompound("PairingData");
        if (tag.contains("PairedController")) {
            pairedController = ItemStack.parseOptional(provider, tag.getCompound("PairedController"));
        }
    }

    // Network synchronization
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, provider);
        return tag;
    }

    // MenuProvider implementation
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.adastramekanized.oxygen_network_monitor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        // Note: The primary code path uses openMenu(ServerPlayer) which has its own guards.
        // This fallback is safe — NeoForge handles null returns gracefully (no menu opens).
        ItemStack controller = findPairedController(player);
        if (!controller.isEmpty()) {
            return new OxygenMonitorMenu(containerId, playerInventory, createMonitorItem(), controller);
        }
        return null;
    }

}