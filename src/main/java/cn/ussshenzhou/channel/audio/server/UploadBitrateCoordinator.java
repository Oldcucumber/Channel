package cn.ussshenzhou.channel.audio.server;

import cn.ussshenzhou.channel.config.ChannelServerConfig;
import cn.ussshenzhou.channel.network.UploadBitratePacket2C;
import cn.ussshenzhou.t88.network.NetworkHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

/**
 * @author USS_Shenzhou
 */
public class UploadBitrateCoordinator {

    public static int calculateLimitBps(PlayerList playerList) {
        var cfg = ChannelServerConfig.get();
        int online = Math.max(1, playerList.getPlayerCount());
        int pooled = cfg.maxTotalBitrate / online;
        return Math.max(6000, Math.min(cfg.maxClientBitrate, pooled));
    }

    public static void syncLimit(PlayerList playerList) {
        int limit = calculateLimitBps(playerList);
        for (ServerPlayer player : playerList.getPlayers()) {
            NetworkHelper.sendToPlayer(player, new UploadBitratePacket2C(limit));
        }
    }

    public static void syncLimitFor(ServerPlayer player) {
        NetworkHelper.sendToPlayer(player, new UploadBitratePacket2C(calculateLimitBps(player.server.getPlayerList())));
    }
}
