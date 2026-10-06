package com.example.excavator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-only: holds the chosen size and handles the cycle key. */
@Mod.EventBusSubscriber(modid = ExcavatorMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ExcavatorClient {
    /** Chosen size; the tool's level caps it. */
    public static int selected = 6;
    private static LocalPlayer lastPlayer = null;

    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            lastPlayer = null;
            return;
        }

        // Tell the server our choice whenever we join a world.
        if (player != lastPlayer) {
            lastPlayer = player;
            ModNetwork.CHANNEL.sendToServer(new SizePacket(selected));
        }

        while (ExcavatorKeys.CYCLE.consumeClick()) {
            int lvl = EnchantmentHelper.getItemEnchantmentLevel(
                    ModEnchantments.EXCAVATOR.get(), player.getMainHandItem());
            if (lvl <= 0) continue;

            int max = lvl + 1;
            int current = Math.min(selected, max);
            int next = current - 1;
            if (next < 2) next = max;

            selected = next;
            ModNetwork.CHANNEL.sendToServer(new SizePacket(next));
            player.displayClientMessage(new TextComponent("Excavator size: " + next + "x" + next), true);
        }
    }
}
