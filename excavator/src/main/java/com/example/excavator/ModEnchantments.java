package com.example.excavator;

import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ExcavatorMod.MOD_ID);

    public static final RegistryObject<Enchantment> EXCAVATOR =
            ENCHANTMENTS.register("excavator", ExcavatorEnchantment::new);
}
