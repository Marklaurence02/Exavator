package com.example.excavator;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExcavatorMod.MOD_ID)
public class ExcavatorMod {
    public static final String MOD_ID = "excavator";

    public ExcavatorMod() {
        ModEnchantments.ENCHANTMENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(new ExcavatorHandler());
    }
}
