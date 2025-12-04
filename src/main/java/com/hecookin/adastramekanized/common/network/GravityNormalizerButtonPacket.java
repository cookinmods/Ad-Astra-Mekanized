package com.hecookin.adastramekanized.common.network;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.blockentities.machines.GravityNormalizerBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Packet to handle gravity normalizer GUI actions
 */
public record GravityNormalizerButtonPacket(BlockPos pos, Action action, float value) implements CustomPacketPayload {

    public enum Action {
        POWER,        // Toggle power on/off
        VISIBILITY,   // Toggle zone visibility
        COLOR,        // Change zone color
        SET_GRAVITY   // Set target gravity value
    }

    public static final Type<GravityNormalizerButtonPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "gravity_normalizer_button")
    );

    // Codec for network serialization
    public static final StreamCodec<ByteBuf, GravityNormalizerButtonPacket> CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC, GravityNormalizerButtonPacket::pos,
        ByteBufCodecs.idMapper(i -> Action.values()[i], Action::ordinal), GravityNormalizerButtonPacket::action,
        ByteBufCodecs.FLOAT, GravityNormalizerButtonPacket::value,
        GravityNormalizerButtonPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handle the packet on the server side
     */
    public static void handle(GravityNormalizerButtonPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                // Validate the player can interact with this position
                if (player.distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5) > 64) {
                    AdAstraMekanized.LOGGER.warn("Player {} tried to interact with gravity normalizer too far away", player.getName().getString());
                    return;
                }

                BlockEntity be = player.level().getBlockEntity(packet.pos);
                if (be instanceof GravityNormalizerBlockEntity normalizer) {
                    switch (packet.action) {
                        case POWER -> {
                            normalizer.setManuallyDisabled(packet.value == 0);  // value=0 means OFF (disabled)
                            AdAstraMekanized.LOGGER.debug("Player {} toggled gravity normalizer power to {}",
                                player.getName().getString(), packet.value != 0 ? "ON" : "OFF");
                        }
                        case VISIBILITY -> {
                            normalizer.setZoneVisibility(packet.value != 0);
                            AdAstraMekanized.LOGGER.debug("Player {} toggled gravity visibility to {}",
                                player.getName().getString(), packet.value != 0);
                        }
                        case COLOR -> {
                            normalizer.setZoneColor((int) packet.value);
                            AdAstraMekanized.LOGGER.debug("Player {} changed gravity zone color to index {}",
                                player.getName().getString(), (int) packet.value);
                        }
                        case SET_GRAVITY -> {
                            normalizer.setTargetGravity(packet.value);
                            AdAstraMekanized.LOGGER.debug("Player {} set gravity normalizer target to {}x",
                                player.getName().getString(), packet.value);
                        }
                    }
                }
            }
        });
    }
}
