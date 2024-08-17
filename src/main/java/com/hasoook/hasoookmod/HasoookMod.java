package com.hasoook.hasoookmod;

import com.hasoook.hasoookmod.effect.ModEffects;
import com.hasoook.hasoookmod.event.enchantment.chain_damage;
import com.hasoook.hasoookmod.event.enchantment.swap;
import com.hasoook.hasoookmod.item.ModCreativeTab;
import com.hasoook.hasoookmod.item.ModItems;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
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
        ModCreativeTab.register(modEventBus);
        ModEffects.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(swap.class);
        NeoForge.EVENT_BUS.register(chain_damage.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }
}
