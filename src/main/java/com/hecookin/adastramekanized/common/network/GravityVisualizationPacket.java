package com.hecookin.adastramekanized.common.network;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.client.renderers.GravityZoneRenderer;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Packet to sync gravity zone visualization from server to client
 */
public record GravityVisualizationPacket(
    BlockPos normalizerPos,
    Set<BlockPos> gravityZones,
    boolean visible,
    int colorIndex,
    float targetGravity
) implements CustomPacketPayload {

    public static final Type<GravityVisualizationPacket> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(AdAstraMekanized.MOD_ID, "gravity_visualization")
    );

    // Codec for network serialization
    public static final StreamCodec<ByteBuf, GravityVisualizationPacket> CODEC = StreamCodec.of(
        GravityVisualizationPacket::encode,
        GravityVisualizationPacket::decode
    );

    private static void encode(ByteBuf buf, GravityVisualizationPacket packet) {
        // Write normalizer position
        buf.writeLong(packet.normalizerPos.asLong());

        // Write visibility flag
        buf.writeBoolean(packet.visible);

        // Write color index
        buf.writeByte(packet.colorIndex);

        // Write target gravity
        buf.writeFloat(packet.targetGravity);

        // Write zone count and positions
        buf.writeInt(packet.gravityZones.size());
        for (BlockPos pos : packet.gravityZones) {
            buf.writeLong(pos.asLong());
        }
    }

    private static GravityVisualizationPacket decode(ByteBuf buf) {
        // Read normalizer position
        BlockPos normalizerPos = BlockPos.of(buf.readLong());

        // Read visibility flag
        boolean visible = buf.readBoolean();

        // Read color index
        int colorIndex = buf.readByte();

        // Read target gravity
        float targetGravity = buf.readFloat();

        // Read zone positions
        int zoneCount = buf.readInt();
        Set<BlockPos> zones = new HashSet<>();
        for (int i = 0; i < zoneCount; i++) {
            zones.add(BlockPos.of(buf.readLong()));
        }

        return new GravityVisualizationPacket(normalizerPos, zones, visible, colorIndex, targetGravity);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /**
     * Handle the packet on the client side
     */
    public static void handle(GravityVisualizationPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Get the gravity zone renderer (similar to oxygen zone renderer)
            GravityZoneRenderer renderer = GravityZoneRenderer.getInstance();

            // Enable global rendering if any normalizer is visible
            renderer.setRenderingEnabled(true);

            if (packet.gravityZones != null && !packet.gravityZones.isEmpty()) {
                // We have zones - either update or hide them based on visibility
                renderer.updateNormalizerZones(packet.normalizerPos, packet.gravityZones, packet.colorIndex, packet.targetGravity);
                renderer.setNormalizerVisibility(packet.normalizerPos, packet.visible);
                AdAstraMekanized.LOGGER.debug("Client {} gravity visualization for normalizer at {}: {} zones, gravity {}x, color {}",
                    packet.visible ? "showing" : "hiding", packet.normalizerPos, packet.gravityZones.size(),
                    packet.targetGravity, packet.colorIndex);
            } else {
                // Empty zones means complete removal
                if (packet.normalizerPos.equals(BlockPos.ZERO)) {
                    // Legacy clear all
                    renderer.clearAllZones();
                    AdAstraMekanized.LOGGER.debug("Client cleared all gravity visualization");
                } else {
                    // Remove the normalizer completely - it's inactive or removed
                    renderer.removeNormalizer(packet.normalizerPos);
                    AdAstraMekanized.LOGGER.debug("Client REMOVED gravity visualization for normalizer at {}",
                        packet.normalizerPos);
                }
            }
        });
    }

}
