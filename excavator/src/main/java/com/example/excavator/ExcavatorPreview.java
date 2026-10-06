package com.example.excavator;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Client-only: draws a pulsing glowing outline on every block that will be mined. */
@Mod.EventBusSubscriber(modid = ExcavatorMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ExcavatorPreview {

    @SubscribeEvent
    public static void onRender(RenderLevelLastEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;
        if (!player.isShiftKeyDown()) return;

        ItemStack tool = player.getMainHandItem();
        int lvl = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.EXCAVATOR.get(), tool);
        if (lvl <= 0) return;

        if (!(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) return;

        List<BlockPos> targets = ExcavatorArea.compute(
                mc.level, hit.getBlockPos(), hit.getDirection().getAxis(), hit.getLocation(), tool, lvl);
        if (targets.isEmpty()) return;

        PoseStack pose = event.getPoseStack();
        Vec3 cam = mc.gameRenderer.getMainCamera().getPosition();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer lines = buffers.getBuffer(RenderType.lines());

        // Soft pulsing glow
        float t = mc.level.getGameTime() + event.getPartialTick();
        float pulse = 0.6F + 0.4F * (float) Math.sin(t * 0.25F);

        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        for (BlockPos pos : targets) {
            AABB box = new AABB(pos).inflate(0.003D);
            // outer faint halo, then bright core
            LevelRenderer.renderLineBox(pose, lines, box.inflate(0.02D), 0.2F, 1.0F, 0.9F, 0.35F * pulse);
            LevelRenderer.renderLineBox(pose, lines, box, 0.4F, 1.0F, 1.0F, pulse);
        }
        pose.popPose();

        buffers.endBatch(RenderType.lines());
    }
}
