package com.hecookin.adastramekanized.client.events;

import com.hecookin.adastramekanized.AdAstraMekanized;
import com.hecookin.adastramekanized.common.entities.vehicles.Lander;
import com.hecookin.adastramekanized.common.entities.vehicles.Rocket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = AdAstraMekanized.MOD_ID, value = Dist.CLIENT)
public class ClientRenderEvents {

    private record SavedItems(ItemStack mainHand, ItemStack offHand) {}
    private static final Map<UUID, SavedItems> savedItems = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (player.getVehicle() instanceof Lander) {
            event.setCanceled(true);
            return;
        }
        // Hide held items in 3rd person when riding a rocket
        if (player.getVehicle() instanceof Rocket) {
            savedItems.put(player.getUUID(), new SavedItems(
                player.getMainHandItem().copy(),
                player.getOffhandItem().copy()
            ));
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        }
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        Player player = event.getEntity();
        SavedItems saved = savedItems.remove(player.getUUID());
        if (saved != null) {
            player.setItemInHand(InteractionHand.MAIN_HAND, saved.mainHand());
            player.setItemInHand(InteractionHand.OFF_HAND, saved.offHand());
        }
    }
}
