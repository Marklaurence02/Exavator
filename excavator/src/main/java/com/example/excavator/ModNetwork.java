package com.example.excavator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class ModNetwork {
    private static final String PROTOCOL = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExcavatorMod.MOD_ID, "main"),
            () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    /** Each player's chosen size (server side). Default = largest allowed by the tool. */
    private static final Map<UUID, Integer> SELECTED = new ConcurrentHashMap<>();

    public static void register() {
        CHANNEL.registerMessage(0, SizePacket.class, SizePacket::encode, SizePacket::decode, SizePacket::handle);
    }

    public static void setSize(UUID id, int size) {
        SELECTED.put(id, Math.max(2, Math.min(6, size)));
    }

    public static int getSize(UUID id) {
        return SELECTED.getOrDefault(id, 6);
    }
}
