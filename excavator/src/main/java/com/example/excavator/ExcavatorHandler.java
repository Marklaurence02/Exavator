package com.example.excavator;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ExcavatorHandler {

    /** Prevents our extra block breaks from re-triggering the effect. */
    private static boolean breaking = false;

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (breaking) return;
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!(event.getWorld() instanceof ServerLevel level)) return;
        if (player.isSpectator()) return;
        if (!player.isShiftKeyDown()) return; // only while sneaking

        ItemStack tool = player.getMainHandItem();
        int lvl = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.EXCAVATOR.get(), tool);
        if (lvl <= 0) return;

        int size = Math.min(ModNetwork.getSize(player.getUUID()), lvl + 1); // level N allows up to (N+1)x(N+1)

        BlockPos origin = event.getPos();

        Direction.Axis axis;
        Vec3 loc = null;
        HitResult hit = player.pick(player.getReachDistance() + 1.0D, 1.0F, false);
        if (hit instanceof BlockHitResult bhr && bhr.getBlockPos().equals(origin)) {
            axis = bhr.getDirection().getAxis();
            loc = bhr.getLocation();
        } else {
            Vec3 look = player.getLookAngle();
            axis = Direction.getNearest(look.x, look.y, look.z).getAxis();
        }

        List<BlockPos> targets = ExcavatorArea.compute(level, origin, axis, loc, tool, size);

        breaking = true;
        try {
            for (BlockPos pos : targets) {
                if (pos.equals(origin)) continue; // vanilla breaks the origin itself

                ItemStack held = player.getMainHandItem();
                if (held.isEmpty()) return; // tool broke
                if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.EXCAVATOR.get(), held) <= 0) return;
                if (!ExcavatorArea.canBreak(level, pos, held)) continue;

                // Handles drops, protection-mod events, XP and tool durability.
                player.gameMode.destroyBlock(pos);
            }
        } finally {
            breaking = false;
        }
    }
}
