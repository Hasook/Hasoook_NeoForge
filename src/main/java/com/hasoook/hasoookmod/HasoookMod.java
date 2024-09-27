package com.hasoook.hasoookmod;

import com.hasoook.hasoookmod.block.ModBlock;
import com.hasoook.hasoookmod.effect.ModEffects;
import com.hasoook.hasoookmod.effect.ModPotions;
import com.hasoook.hasoookmod.entity.ModEntities;
import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentInteract;
import com.hasoook.hasoookmod.event.entityEnchantment.EnchantmentEntityTick;
import com.hasoook.hasoookmod.item.ModCreativeTab;
import com.hasoook.hasoookmod.item.ModItems;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

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
        ModEntities.register(modEventBus);

        NeoForge.EVENT_BUS.register(EntityEnchantmentInteract.class);
        NeoForge.EVENT_BUS.register(EnchantmentEntityTick.class);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
    }
}
