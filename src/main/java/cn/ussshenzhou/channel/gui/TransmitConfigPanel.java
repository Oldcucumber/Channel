package cn.ussshenzhou.channel.gui;

import cn.ussshenzhou.channel.audio.OpusManager;
import cn.ussshenzhou.channel.audio.UploadBitrateController;
import cn.ussshenzhou.channel.config.ChannelClientConfig;
import cn.ussshenzhou.channel.util.ModConstant;
import cn.ussshenzhou.t88.config.ConfigHelper;
import cn.ussshenzhou.t88.gui.advanced.TOptionsPanel;
import cn.ussshenzhou.t88.gui.widegt.TLabel;
import cn.ussshenzhou.t88.gui.widegt.TSlider;
import cn.ussshenzhou.t88.util.T88Config;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.text.DecimalFormat;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author USS_Shenzhou
 */
public class TransmitConfigPanel extends TOptionsPanel {
    private TLabel bitrate;
    private TLabel speed;
    private TLabel serverLimit;
    private TLabel actualBitrate;
    private TSlider bitrateSlider;
    private int life = 0;

    public TransmitConfigPanel() {
        var cfg = ChannelClientConfig.get();

        addOptionSplitter(Component.translatable("channel.config.net"));
        addOptionCycleButtonInit(
                Component.translatable("channel.config.net.length"),
                Stream.concat(ModConstant.USABLE_FRAME_LENGTH.stream(), Stream.of(cfg.frameLengthMs)).distinct().collect(Collectors.toList()),
                length -> _ -> ChannelClientConfig.write(c -> c.frameLengthMs = length),
                entry -> entry.getContent() == cfg.frameLengthMs
        ).getB().setTooltip(Tooltip.create(Component.translatable("channel.config.net.length.tooltip")));
        var netSampleRate = addOptionCycleButtonInit(
                Component.translatable("channel.config.net.samplerate"),
                ModConstant.USABLE_NETWORK_SAMPLE_RATE,
                f -> _ -> {
                    ChannelClientConfig.write(c -> c.networkSampleRate = f);
                },
                entry -> entry.getContent() == cfg.networkSampleRate
        ).getB();
        bitrateSlider = addOptionSlider(
                Component.translatable("channel.config.net.bitrate"),
                6,
                64,
                1,
                slider -> {
                    int kbps = (int) slider.getAbsValue();
                    UploadBitrateController.setUserTargetBitrateBps(kbps * 1000);
                    updateBitrateLabels();
                },
                (_, value) -> Component.literal((int) value + " kbps")
        ).getB();
        bitrateSlider.setAbsValue(UploadBitrateController.getUserTargetBitrateBps() / 1000f);
        bitrate = addOption(Component.translatable("channel.config.net.bitrate.user"), new TLabel(Component.empty())).getB();
        serverLimit = addOption(Component.translatable("channel.config.net.bitrate.server"), new TLabel(Component.empty())).getB();
        actualBitrate = addOption(Component.translatable("channel.config.net.bitrate.actual"), new TLabel(Component.empty())).getB();
        speed = addOption(Component.translatable("channel.config.net.flow"), new TLabel(Component.empty())).getB();
        netSampleRate.setTooltip(Tooltip.create(Component.translatable("channel.config.net.samplerate.tooltip")));
        updateBitrateLabels();
    }

    private void updateBitrateLabels() {
        bitrate.setText(Component.literal(getBitRate(UploadBitrateController.getUserTargetBitrateBps())));
        serverLimit.setText(Component.literal(getServerLimit()));
        actualBitrate.setText(Component.literal(getBitRate(UploadBitrateController.getActualBitrateBps())));
    }

    private String getServerLimit() {
        if (!UploadBitrateController.isServerLimitBounded()) {
            return Component.translatable("channel.config.net.bitrate.unlimited").getString();
        }
        return getBitRate(UploadBitrateController.getServerLimitBps());
    }

    private String getBitRate(int bps) {
        var df = new DecimalFormat("0.#");
        return df.format(bps / 1000f) + " kbps";
    }

    @Override
    public void tickT() {
        if (life % 10 == 0) {
            OpusManager.SEND_SPEED.update();
            speed.setText(Component.literal(getReadableSize(OpusManager.SEND_SPEED.averageIn1s())));
            updateBitrateLabels();
        }
        life++;
        super.tickT();
    }


    private String getReadableSize(double bytes) {
        var s = new StringBuilder();
        if (ConfigHelper.getConfigRead(T88Config.class).networkUnit == T88Config.NetworkUnit.BIT) {
            bytes *= 8;
            if (bytes < 1000) {
                s.append(bytes).append(" §7bps§r");
            } else if (bytes < 1000 * 1000) {
                s.append(String.format("%.1f §7Kbps§r", bytes / 1024));
            } else {
                s.append(String.format("%.2f §7Mbps§r", bytes / (1024 * 1024)));
            }
        } else {
            if (bytes < 1000) {
                s.append(bytes).append(" §7Bytes/S§r");
            } else if (bytes < 1000 * 1000) {
                s.append(String.format("%.1f §7KiB/S§r", bytes / 1024));
            } else {
                s.append(String.format("%.2f §7MiB/S§r", bytes / (1024 * 1024)));
            }
        }
        return s.toString();
    }
}
