package com.example.excavator;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExcavatorMod.MOD_ID)
public class ExcavatorMod {
    public static final String MOD_ID = "excavator";

    public ExcavatorMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEnchantments.ENCHANTMENTS.register(modBus);
        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(new ExcavatorHandler());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
