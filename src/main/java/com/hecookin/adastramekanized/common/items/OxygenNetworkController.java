package com.hecookin.adastramekanized.common.items;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.GravityNormalizerBlockEntity;
import com.hecookin.adastramekanized.common.blockentities.machines.ImprovedOxygenDistributor;
import com.hecookin.adastramekanized.common.blockentities.machines.WirelessPowerRelayBlockEntity;
import com.hecookin.adastramekanized.common.blocks.machines.WirelessPowerRelayBlock;
import com.hecookin.adastramekanized.common.data.DistributorLinkData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import com.hecookin.adastramekanized.common.blocks.RedstoneToggleRelay;
import com.hecookin.adastramekanized.common.blocks.OxygenNetworkMonitorBlock;
import com.hecookin.adastramekanized.common.data.ButtonControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Handheld controller for managing multiple oxygen distributors wirelessly
 */
public class OxygenNetworkController extends Item {

    public OxygenNetworkController() {
        super(new Item.Properties()
            .stacksTo(1)
            .fireResistant());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        InteractionHand hand = context.getHand();

        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockState state = level.getBlockState(pos);

        // Check if this is a monitor block - let the block handle it for now
        if (state.getBlock() instanceof OxygenNetworkMonitorBlock) {
            // Monitor still requires sneak+right-click for pairing
            if (player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }
            return InteractionResult.FAIL;
        }

        // Check if this is a redstone toggle relay - sneak+right-click to pair
        if (state.getBlock() instanceof RedstoneToggleRelay) {
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide) {
                    // Bind controller to relay
                    bindToRelay(level, pos, stack, player);
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }

        // Check if this is a wireless power relay - sneak+right-click to insert/swap
        if (state.getBlock() instanceof WirelessPowerRelayBlock) {
            if (player.isShiftKeyDown()) {
                if (!level.isClientSide) {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof WirelessPowerRelayBlockEntity relay) {
                        ItemStack controllerSlotItem = relay.getControllerSlot().getItem(0);

                        if (controllerSlotItem.isEmpty()) {
                            // Place controller in slot
                            relay.getControllerSlot().setItem(0, stack.copy());
                            stack.shrink(1);

                            player.displayClientMessage(
                                Component.literal("Controller inserted into power relay")
                                    .withStyle(ChatFormatting.GREEN),
                                true
                            );
                            AdAstraMekanized.LOGGER.info("Inserted controller into power relay at {}", pos);
                        } else {
                            // Swap controllers
                            ItemStack oldController = controllerSlotItem.copy();
                            relay.getControllerSlot().setItem(0, stack.copy());

                            // Give the old controller to the player
                            player.setItemInHand(hand, oldController);

                            player.displayClientMessage(
                                Component.literal("Swapped controllers in power relay")
                                    .withStyle(ChatFormatting.YELLOW),
                                true
                            );
                            AdAstraMekanized.LOGGER.info("Swapped controller in power relay at {}", pos);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        }

        // Check if this is a distributor (oxygen or gravity) - simple right-click to toggle link
        BlockEntity be = level.getBlockEntity(pos);
        boolean isOxygenDistributor = be instanceof ImprovedOxygenDistributor;
        boolean isGravityDistributor = be instanceof GravityNormalizerBlockEntity;

        if (isOxygenDistributor || isGravityDistributor) {
            if (!level.isClientSide) {
                String distributorType = isOxygenDistributor ? "oxygen distributor" : "gravity distributor";
                // Toggle link on simple right-click
                {
                    DistributorLinkData linkData = getOrCreateLinkData(stack);

                    if (linkData.isLinked(pos)) {
                        // Unlink
                        linkData.removeLink(pos);
                        saveLinkData(stack, linkData);
                        player.displayClientMessage(
                            Component.literal("Unlinked " + distributorType + " at ")
                                .withStyle(ChatFormatting.YELLOW)
                                .append(Component.literal(String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ()))
                                    .withStyle(ChatFormatting.WHITE)),
                            true
                        );
                        AdAstraMekanized.LOGGER.info("Unlinked {} at {} from controller", distributorType, pos);
                    } else {
                        // Link
                        if (linkData.addLink(pos)) {
                            saveLinkData(stack, linkData);
                            player.displayClientMessage(
                                Component.literal("Linked " + distributorType + " at ")
                                    .withStyle(ChatFormatting.GREEN)
                                    .append(Component.literal(String.format("%d, %d, %d", pos.getX(), pos.getY(), pos.getZ()))
                                        .withStyle(ChatFormatting.WHITE))
                                    .append(Component.literal(String.format(" (%d/%d)", linkData.getLinkCount(), 64))
                                        .withStyle(ChatFormatting.GRAY)),
                                true
                            );
                            AdAstraMekanized.LOGGER.info("Linked {} at {} to controller", distributorType, pos);
                        } else {
                            player.displayClientMessage(
                                Component.literal("Cannot link more distributors! Maximum 64 reached.")
                                    .withStyle(ChatFormatting.RED),
                                true
                            );
                        }
                    }
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Controller no longer has a GUI - only used for pairing
        // Show current link status to player
        if (!level.isClientSide) {
            DistributorLinkData linkData = getLinkData(stack);
            int count = linkData != null ? linkData.getLinkCount() : 0;

            player.displayClientMessage(
                Component.literal("Controller has ")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(count + " linked distributors")
                        .withStyle(count > 0 ? ChatFormatting.GREEN : ChatFormatting.RED)),
                true
            );
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);

        DistributorLinkData linkData = getLinkData(stack);
        if (linkData != null) {
            int count = linkData.getLinkCount();
            tooltip.add(Component.literal("Linked Distributors: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(count + "/64")
                    .withStyle(count > 0 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY)));

            if (flag.isAdvanced()) {
                tooltip.add(Component.literal("Right-click distributor to link/unlink")
                    .withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("Sneak+right-click relay/power relay to pair")
                    .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        DistributorLinkData linkData = getLinkData(stack);
        return linkData != null && linkData.getLinkCount() > 0;
    }

    // Data management methods

    public static DistributorLinkData getOrCreateLinkData(ItemStack stack) {
        if (!(stack.getItem() instanceof OxygenNetworkController)) {
            return new DistributorLinkData();
        }

        DistributorLinkData data = new DistributorLinkData();

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.getUnsafe();
            if (tag != null && tag.contains("LinkData")) {
                data.fromNbt(tag.getCompound("LinkData"));
            }
        }

        return data;
    }

    public static DistributorLinkData getLinkData(ItemStack stack) {
        if (!(stack.getItem() instanceof OxygenNetworkController)) {
            return null;
        }

        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData != null) {
            CompoundTag tag = customData.getUnsafe();
            if (tag != null && tag.contains("LinkData")) {
                DistributorLinkData data = new DistributorLinkData();
                data.fromNbt(tag.getCompound("LinkData"));
                return data;
            }
        }

        return null;
    }

    public static void saveLinkData(ItemStack stack, DistributorLinkData data) {
        if (!(stack.getItem() instanceof OxygenNetworkController)) {
            return;
        }

        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.getUnsafe();
        if (tag == null) {
            tag = new CompoundTag();
        } else {
            tag = tag.copy();
        }
        tag.put("LinkData", data.toNbt());
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Bind this controller to a redstone toggle relay block
     */
    private void bindToRelay(Level level, BlockPos pos, ItemStack stack, Player player) {
        // Check if another controller is already bound
        ButtonControllerManager.ControllerBinding existing = ButtonControllerManager.getBinding(level, pos);
        if (existing != null) {
            player.displayClientMessage(
                Component.literal("Replaced previous controller binding")
                    .withStyle(ChatFormatting.YELLOW),
                true
            );
        }

        // Store in level saved data
        ButtonControllerManager.bindController(level, pos, stack);

        DistributorLinkData linkData = getOrCreateLinkData(stack);
        player.displayClientMessage(
            Component.literal("Bound controller to relay with ")
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(linkData.getLinkCount() + " distributors")
                    .withStyle(ChatFormatting.WHITE)),
            true
        );

        AdAstraMekanized.LOGGER.info("Bound oxygen controller to relay at {}", pos);
    }

    /**
     * Update all linked distributors with current status from the level
     */
    public static void updateDistributorStatuses(Level level, DistributorLinkData linkData) {
        for (DistributorLinkData.LinkedDistributor link : linkData.getLinkedDistributors()) {
            BlockPos pos = link.getPos();
            BlockEntity be = level.getBlockEntity(pos);

            if (be instanceof ImprovedOxygenDistributor distributor) {
                int energy = distributor.getEnergyStorage().getEnergyStored();
                long oxygen = distributor.getOxygenTank().getStored();
                float efficiency = distributor.getEfficiency();
                boolean online = distributor.isActive();

                link.updateStatus(energy, oxygen, efficiency, online);
            } else if (be instanceof GravityNormalizerBlockEntity gravityDistributor) {
                int energy = gravityDistributor.getEnergyStorage().getEnergyStored();
                long argon = gravityDistributor.getArgonTank().getStored();
                float efficiency = gravityDistributor.getEfficiency();
                boolean online = gravityDistributor.isActive();

                link.updateStatus(energy, argon, efficiency, online);
            } else {
                // Distributor no longer exists or not loaded
                link.updateStatus(0, 0, 0, false);
            }
        }
    }
}