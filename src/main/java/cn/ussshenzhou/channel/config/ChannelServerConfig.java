package cn.ussshenzhou.channel.config;

import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.config.TConfig;

import java.util.function.Consumer;

/**
 * @author USS_Shenzhou
 */
public class ChannelServerConfig implements TConfig {

    public int maxTotalBitrate = 8_000_000;
    public int maxClientBitrate = 64_000;
    public int bitrateGracePackets = 15;

    public static ChannelServerConfig get() {
        return ConfigHelper.getConfigRead(ChannelServerConfig.class);
    }

    public static void write(Consumer<ChannelServerConfig> writer) {
        ConfigHelper.getConfigWrite(ChannelServerConfig.class, writer);
    }
}
