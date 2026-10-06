package com.example.excavator;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Shared by the server (breaking) and the client (glow preview) so both always agree. */
public class ExcavatorArea {

    /**
     * Returns the blocks in the size x size area that would be mined. The origin is always first
     * (if it is pickaxe-mineable); an empty list means nothing should happen.
     *
     * @param hitLoc exact point hit on the block, or null to use a centered grid
     */
    public static List<BlockPos> compute(Level level, BlockPos origin, Direction.Axis axis, Vec3 hitLoc, ItemStack tool, int size) {
        List<BlockPos> result = new ArrayList<>();
        if (!level.getBlockState(origin).is(BlockTags.MINEABLE_WITH_PICKAXE)) return result;
        result.add(origin);

        int a = axis.ordinal();
        int[] others = new int[2];
        int n = 0;
        for (int i = 0; i < 3; i++) if (i != a) others[n++] = i;

        double fu = 0.5, fv = 0.5;
        if (hitLoc != null) {
            fu = frac(hitLoc, origin, Direction.Axis.values()[others[0]]);
            fv = frac(hitLoc, origin, Direction.Axis.values()[others[1]]);
        }

        // Odd sizes are centered on the mined block. Even sizes have no single center:
        // the mined block sits in the middle 2x2, biased toward where you are aiming.
        int half = size / 2;
        int uStart, vStart;
        if (size % 2 == 1) {
            uStart = -half;
            vStart = -half;
        } else {
            uStart = fu < 0.5 ? -half : -half + 1;
            vStart = fv < 0.5 ? -half : -half + 1;
        }

        for (int du = uStart; du < uStart + size; du++) {
            for (int dv = vStart; dv < vStart + size; dv++) {
                int[] off = new int[3];
                off[others[0]] = du;
                off[others[1]] = dv;
                if (off[0] == 0 && off[1] == 0 && off[2] == 0) continue;

                BlockPos pos = origin.offset(off[0], off[1], off[2]);
                if (canBreak(level, pos, tool)) result.add(pos);
            }
        }
        return result;
    }

    public static boolean canBreak(Level level, BlockPos pos, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) return false;
        if (state.getDestroySpeed(level, pos) < 0) return false; // bedrock etc.
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE)) return false;
        return tool.isCorrectToolForDrops(state);
    }

    private static double frac(Vec3 loc, BlockPos pos, Direction.Axis axis) {
        return axis.choose(loc.x, loc.y, loc.z) - axis.choose(pos.getX(), pos.getY(), pos.getZ());
    }
}
