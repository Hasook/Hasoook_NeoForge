package com.hasoook.hasoookmod;

import com.hasoook.hasoookmod.block.ModBlock;
import com.hasoook.hasoookmod.effect.ModEffects;
import com.hasoook.hasoookmod.effect.ModPotions;
import com.hasoook.hasoookmod.item.ModCreativeTab;
import com.hasoook.hasoookmod.item.ModItems;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(HasoookMod.MODID)
public class HasoookMod
{
    public static final String MODID = "hasoook";
    private static final Logger LOGGER = LogUtils.getLogger();

    public HasoookMod(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        ModItems.register(modEventBus);
        ModBlock.register(modEventBus);
        ModCreativeTab.register(modEventBus);
        ModEffects.register(modEventBus);
        ModPotions.register(modEventBus);

    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }
}
