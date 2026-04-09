package cn.ussshenzhou.channel.audio;

import cn.ussshenzhou.channel.config.ChannelClientConfig;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author USS_Shenzhou
 */
public class UploadBitrateController {

    private static final AtomicInteger SERVER_LIMIT_BPS = new AtomicInteger(Integer.MAX_VALUE);
    private static final AtomicInteger USER_TARGET_BPS = new AtomicInteger(32000);

    public static int getServerLimitBps() {
        return SERVER_LIMIT_BPS.get();
    }

    public static void setServerLimitBps(int limitBps) {
        SERVER_LIMIT_BPS.set(Math.max(6000, limitBps));
    }

    public static int getUserTargetBitrateBps() {
        return USER_TARGET_BPS.get();
    }

    public static void setUserTargetBitrateBps(int bitrateBps) {
        int sanitized = Math.max(6000, bitrateBps);
        USER_TARGET_BPS.set(sanitized);
        ChannelClientConfig.write(c -> c.uploadBitrateBps = sanitized);
    }

    public static int getActualBitrateBps() {
        return Math.min(getUserTargetBitrateBps(), getServerLimitBps());
    }

    public static void initFromConfig() {
        int target = Math.max(6000, ChannelClientConfig.get().uploadBitrateBps);
        USER_TARGET_BPS.set(target);
    }

    public static boolean isServerLimitBounded() {
        return getServerLimitBps() < Integer.MAX_VALUE;
    }
}
