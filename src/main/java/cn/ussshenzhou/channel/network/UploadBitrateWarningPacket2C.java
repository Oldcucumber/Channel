package cn.ussshenzhou.channel.network;

import cn.ussshenzhou.channel.util.ModConstant;
import cn.ussshenzhou.t88.gui.notification.TSimpleNotification;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = ModConstant.SHORT_ID, handleOnNetwork = true, id = "ubw")
public class UploadBitrateWarningPacket2C {

    public static final byte STATUS_WARN = 0;
    public static final byte STATUS_DROP = 1;

    private final byte status;

    public UploadBitrateWarningPacket2C(byte status) {
        this.status = status;
    }

    @Decoder
    public UploadBitrateWarningPacket2C(FriendlyByteBuf buf) {
        this.status = buf.readByte();
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeByte(this.status);
    }

    @ClientHandler
    public void clientHandler(IPayloadContext context) {
        if (status == STATUS_DROP) {
            TSimpleNotification.fire(Component.translatable("channel.notify.bitrate.drop"), 4, TSimpleNotification.Severity.ERROR);
        } else if (status == STATUS_WARN) {
            TSimpleNotification.fire(Component.translatable("channel.notify.bitrate.warn"), 4, TSimpleNotification.Severity.WARN);
        }
    }
}
