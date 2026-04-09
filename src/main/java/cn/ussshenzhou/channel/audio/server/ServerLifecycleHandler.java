package cn.ussshenzhou.channel.audio.server;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * @author USS_Shenzhou
 */
@EventBusSubscriber
public class ServerLifecycleHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        UploadBitrateCoordinator.syncLimit(event.getEntity().server.getPlayerList());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        RelayHandler.clearUploadStat(event.getEntity().getUUID());
        UploadBitrateCoordinator.syncLimit(event.getEntity().server.getPlayerList());
    }
}
