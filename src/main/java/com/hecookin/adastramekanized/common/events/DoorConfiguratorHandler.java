package com.hecookin.adastramekanized.common.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blocks.SlidingDoorBlock;
import com.hecookin.adastramekanized.common.blocks.SteelDoorBlock;
import com.hecookin.adastramekanized.common.blocks.SteelTrapdoorBlock;
import com.hecookin.adastramekanized.common.blocks.properties.SlidingDoorPartProperty;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Handles sneak+right-click with the Oxygen Network Controller on doors
 * to toggle between hand-openable and redstone-only modes.
 */
@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID)
public class DoorConfiguratorHandler {

    private static final ResourceLocation MEKANISM_CONFIGURATOR = ResourceLocation.parse("mekanism:configurator");

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.isShiftKeyDown()) return;
        ItemStack heldItem = player.getItemInHand(event.getHand());
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(heldItem.getItem());
        if (!MEKANISM_CONFIGURATOR.equals(itemId)) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);

        if (state.getBlock() instanceof SteelDoorBlock) {
            if (!level.isClientSide) {
                toggleDoorRedstoneMode(level, pos, state, player);
            }
            event.setCanceled(true);
        } else if (state.getBlock() instanceof SteelTrapdoorBlock) {
            if (!level.isClientSide) {
                boolean newValue = !state.getValue(SteelTrapdoorBlock.REQUIRES_REDSTONE);
                level.setBlock(pos, state.setValue(SteelTrapdoorBlock.REQUIRES_REDSTONE, newValue), Block.UPDATE_ALL);
                sendToggleMessage(player, newValue);
            }
            event.setCanceled(true);
        } else if (state.getBlock() instanceof SlidingDoorBlock) {
            if (!level.isClientSide) {
                toggleSlidingDoorRedstoneMode(level, pos, state, player);
            }
            event.setCanceled(true);
        }
    }

    private static void toggleDoorRedstoneMode(Level level, BlockPos pos, BlockState state, Player player) {
        boolean newValue = !state.getValue(SteelDoorBlock.REQUIRES_REDSTONE);
        level.setBlock(pos, state.setValue(SteelDoorBlock.REQUIRES_REDSTONE, newValue), Block.UPDATE_ALL);
        // Update the other half of the door
        BlockPos otherHalf = state.getValue(net.minecraft.world.level.block.DoorBlock.HALF) ==
            net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER ? pos.above() : pos.below();
        BlockState otherState = level.getBlockState(otherHalf);
        if (otherState.getBlock() instanceof SteelDoorBlock) {
            level.setBlock(otherHalf, otherState.setValue(SteelDoorBlock.REQUIRES_REDSTONE, newValue), Block.UPDATE_ALL);
        }
        sendToggleMessage(player, newValue);
    }

    private static void toggleSlidingDoorRedstoneMode(Level level, BlockPos pos, BlockState state, Player player) {
        SlidingDoorPartProperty part = state.getValue(SlidingDoorBlock.PART);
        Direction direction = state.getValue(SlidingDoorBlock.FACING).getClockWise();
        BlockPos controllerPos = pos.relative(direction, -part.xOffset()).below(part.yOffset());
        BlockState controllerState = level.getBlockState(controllerPos);

        if (!(controllerState.getBlock() instanceof SlidingDoorBlock)) return;

        boolean newValue = !controllerState.getValue(SlidingDoorBlock.REQUIRES_REDSTONE);

        for (SlidingDoorPartProperty doorPart : SlidingDoorPartProperty.values()) {
            BlockPos partPos = controllerPos.relative(direction, doorPart.xOffset()).above(doorPart.yOffset());
            BlockState partState = level.getBlockState(partPos);
            if (partState.getBlock() instanceof SlidingDoorBlock) {
                level.setBlock(partPos, partState.setValue(SlidingDoorBlock.REQUIRES_REDSTONE, newValue), Block.UPDATE_ALL);
            }
        }

        sendToggleMessage(player, newValue);
    }

    private static void sendToggleMessage(Player player, boolean requiresRedstone) {
        player.displayClientMessage(
            Component.literal("Redstone required: ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(requiresRedstone ? "ON" : "OFF")
                    .withStyle(requiresRedstone ? ChatFormatting.GREEN : ChatFormatting.RED)),
            true
        );
    }
}
