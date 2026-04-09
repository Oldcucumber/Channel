package cn.ussshenzhou.channel.network;

import cn.ussshenzhou.channel.audio.UploadBitrateController;
import cn.ussshenzhou.channel.util.ModConstant;
import cn.ussshenzhou.t88.network.annotation.ClientHandler;
import cn.ussshenzhou.t88.network.annotation.Decoder;
import cn.ussshenzhou.t88.network.annotation.Encoder;
import cn.ussshenzhou.t88.network.annotation.NetPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * @author USS_Shenzhou
 */
@NetPacket(modid = ModConstant.SHORT_ID, handleOnNetwork = true, id = "ubc")
public class UploadBitratePacket2C {

    private final int limitBps;

    public UploadBitratePacket2C(int limitBps) {
        this.limitBps = limitBps;
    }

    @Decoder
    public UploadBitratePacket2C(FriendlyByteBuf buf) {
        this.limitBps = buf.readVarInt();
    }

    @Encoder
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(this.limitBps);
    }

    @ClientHandler
    public void clientHandler(IPayloadContext context) {
        UploadBitrateController.setServerLimitBps(limitBps);
    }
}
