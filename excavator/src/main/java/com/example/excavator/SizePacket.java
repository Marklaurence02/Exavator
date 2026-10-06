package com.example.excavator;

import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/** Client -> server: the area size the player picked. */
public class SizePacket {
    public final int size;

    public SizePacket(int size) {
        this.size = size;
    }

    public static void encode(SizePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.size);
    }

    public static SizePacket decode(FriendlyByteBuf buf) {
        return new SizePacket(buf.readVarInt());
    }

    public static void handle(SizePacket msg, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context c = ctx.get();
        c.enqueueWork(() -> {
            ServerPlayer player = c.getSender();
            if (player != null) ModNetwork.setSize(player.getUUID(), msg.size);
        });
        c.setPacketHandled(true);
    }
}
