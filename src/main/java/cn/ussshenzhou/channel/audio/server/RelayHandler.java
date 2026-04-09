package cn.ussshenzhou.channel.audio.server;

import cn.ussshenzhou.channel.config.ChannelServerConfig;
import cn.ussshenzhou.channel.network.TalkPacket2C;
import cn.ussshenzhou.channel.network.UploadBitrateWarningPacket2C;
import cn.ussshenzhou.t88.network.NetworkHelper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author USS_Shenzhou
 */
public class RelayHandler {

    public static final ExecutorService SERVER_THREAD = Executors.newSingleThreadExecutor(new ThreadFactoryBuilder()
            .setNameFormat("Channel-Server-Thread-%d")
            .setDaemon(true)
            .build());
    private static final Map<UUID, UploadStat> UPLOAD_STATS = new HashMap<>();

    private record UploadStat(long windowStartMs, long bitsInWindow, int overLimitPackets, long lastWarnMs, boolean dropping) {
    }

    public static void process(ServerPlayer from, byte[] opusAudio, int sampleRate) {
        SERVER_THREAD.execute(() -> {
            if (!allowUpload(from, opusAudio.length)) {
                return;
            }
            normalTalking(from, opusAudio, sampleRate);
        });
    }

    private static boolean allowUpload(ServerPlayer from, int byteLength) {
        long now = System.currentTimeMillis();
        UUID id = from.getUUID();
        long packetBits = Math.max(0L, byteLength * 8L);
        int serverLimit = UploadBitrateCoordinator.calculateLimitBps(from.server.getPlayerList());
        int grace = Math.max(1, ChannelServerConfig.get().bitrateGracePackets);
        UploadStat previous = UPLOAD_STATS.get(id);

        if (previous == null || now - previous.windowStartMs >= 1000) {
            previous = new UploadStat(now, 0, 0, 0, false);
        }
        long nextBits = previous.bitsInWindow + packetBits;
        boolean overLimit = nextBits > serverLimit;

        int overLimitPackets = overLimit ? previous.overLimitPackets + 1 : 0;
        boolean dropping = overLimit && overLimitPackets > grace;
        long lastWarnMs = previous.lastWarnMs;
        if (overLimit && now - previous.lastWarnMs > 1000) {
            NetworkHelper.sendToPlayer(from, new UploadBitrateWarningPacket2C(dropping ? UploadBitrateWarningPacket2C.STATUS_DROP : UploadBitrateWarningPacket2C.STATUS_WARN));
            lastWarnMs = now;
        }

        UPLOAD_STATS.put(id, new UploadStat(previous.windowStartMs, nextBits, overLimitPackets, lastWarnMs, dropping));
        return !dropping;
    }

    public static void normalTalking(ServerPlayer from, byte[] opusAudio, int sampleRate) {
        from.level().players().stream().filter(to ->
                        (SharedConstants.IS_RUNNING_WITH_JDWP || to.getId() != from.getId()) &&
                                to.position().distanceTo(from.position()) < 64 &&
                                (!from.isSpectator() || to.isSpectator())
                )
                .forEach(to -> NetworkHelper.sendToPlayer(to, new TalkPacket2C(sampleRate, from.getUUID(), opusAudio)));
    }

    public static void clearUploadStat(UUID playerId) {
        SERVER_THREAD.execute(() -> UPLOAD_STATS.remove(playerId));
    }
}
