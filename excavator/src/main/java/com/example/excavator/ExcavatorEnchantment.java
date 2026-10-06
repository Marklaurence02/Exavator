package com.example.excavator;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class ExcavatorEnchantment extends Enchantment {

    public ExcavatorEnchantment() {
        // RARE = weight 3 (same tier as Silk Touch is VERY_RARE, Fortune is RARE)
        super(Rarity.RARE, EnchantmentCategory.DIGGER, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public int getMinCost(int level) {
        return 10 + (level - 1) * 10; // I:10  II:20  III:30  IV:40  V:50
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 30;
    }

    /**
     * Restrict to pickaxes only. Enchanting table, anvil (book -> pickaxe)
     * and loot all go through this check.
     */
    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.getItem() instanceof PickaxeItem;
    }

    @Override
    public boolean isTreasureOnly() {
        return false; // obtainable from the enchanting table
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }
}
