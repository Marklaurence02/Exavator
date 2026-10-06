package com.example.excavator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
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

        ItemStack tool = player.getMainHandItem();
        if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.EXCAVATOR.get(), tool) <= 0) return;

        BlockPos origin = event.getPos();
        if (!event.getState().is(BlockTags.MINEABLE_WITH_PICKAXE)) return;

        // Work out which face was mined and where on the block we hit.
        Direction.Axis axis;
        double fu = 0.5, fv = 0.5;
        int[] others;

        HitResult hit = player.pick(player.getReachDistance() + 1.0D, 1.0F, false);
        Vec3 loc = null;
        if (hit instanceof BlockHitResult bhr && bhr.getBlockPos().equals(origin)) {
            axis = bhr.getDirection().getAxis();
            loc = bhr.getLocation();
        } else {
            Vec3 look = player.getLookAngle();
            axis = Direction.getNearest(look.x, look.y, look.z).getAxis();
        }

        int a = axis.ordinal();
        others = new int[2];
        int n = 0;
        for (int i = 0; i < 3; i++) if (i != a) others[n++] = i;

        if (loc != null) {
            fu = frac(loc, origin, Direction.Axis.values()[others[0]]);
            fv = frac(loc, origin, Direction.Axis.values()[others[1]]);
        }

        // 4x4 grid: the mined block sits in the central 2x2, biased toward the point you hit.
        int uStart = fu < 0.5 ? -2 : -1;
        int vStart = fv < 0.5 ? -2 : -1;

        breaking = true;
        try {
            for (int du = uStart; du < uStart + 4; du++) {
                for (int dv = vStart; dv < vStart + 4; dv++) {
                    int[] off = new int[3];
                    off[others[0]] = du;
                    off[others[1]] = dv;
                    if (off[0] == 0 && off[1] == 0 && off[2] == 0) continue; // vanilla handles the origin

                    ItemStack held = player.getMainHandItem();
                    if (held.isEmpty()) return; // tool broke
                    if (EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.EXCAVATOR.get(), held) <= 0) return;

                    BlockPos pos = origin.offset(off[0], off[1], off[2]);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) continue;
                    if (state.getDestroySpeed(level, pos) < 0) continue;       // bedrock etc.
                    if (!state.getFluidState().isEmpty() && state.getBlock().asItem() == net.minecraft.world.item.Items.AIR) continue;
                    if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) continue;
                    if (!held.isCorrectToolForDrops(state)) continue;

                    // Handles drops, protection-mod events, XP, and tool durability.
                    player.gameMode.destroyBlock(pos);
                }
            }
        } finally {
            breaking = false;
        }
    }

    private static double frac(Vec3 loc, BlockPos pos, Direction.Axis axis) {
        return axis.choose(loc.x, loc.y, loc.z) - axis.choose(pos.getX(), pos.getY(), pos.getZ());
    }
}
