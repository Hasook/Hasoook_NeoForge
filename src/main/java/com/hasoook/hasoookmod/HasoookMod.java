package com.hasoook.hasoookmod;

import com.hasoook.hasoookmod.block.ModBlock;
import com.hasoook.hasoookmod.effect.ModEffects;
import com.hasoook.hasoookmod.effect.ModPotions;
import com.hasoook.hasoookmod.entity.ModEntities;
import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentInteract;
import com.hasoook.hasoookmod.event.entityEnchantment.EnchantmentEntityTick;
import com.hasoook.hasoookmod.item.ModArmorMaterials;
import com.hasoook.hasoookmod.item.ModCreativeTab;
import com.hasoook.hasoookmod.item.ModItems;
import com.hasoook.hasoookmod.sound.ModSounds;
import com.hasoook.hasoookmod.entity.client.TornadoRenderer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.checkerframework.checker.units.qual.C;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(HasoookMod.MOD_ID)
public class HasoookMod
{
    public static final String MOD_ID = "hasoook";
    private static final Logger LOGGER = LogUtils.getLogger();

    public HasoookMod(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        ModItems.register(modEventBus);
        ModBlock.register(modEventBus);
        ModCreativeTab.register(modEventBus);
        ModEffects.register(modEventBus);
        ModPotions.register(modEventBus);
        ModArmorMaterials.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);

        NeoForge.EVENT_BUS.register(EntityEnchantmentInteract.class);
        NeoForge.EVENT_BUS.register(EnchantmentEntityTick.class);

        ModEntities.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON,Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.TORNADO.get(), TornadoRenderer::new);
        }
    }
}
