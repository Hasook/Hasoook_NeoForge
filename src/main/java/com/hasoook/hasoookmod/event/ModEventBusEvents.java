package com.hasoook.hasoookmod.event;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.client.HideHeadHandler;
import com.hasoook.hasoookmod.entity.ModEntities;
import com.hasoook.hasoookmod.entity.client.TornadoModel;
import com.hasoook.hasoookmod.entity.custom.TornadoEntity;
import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentInteract;
import com.hasoook.hasoookmod.event.entityEnchantment.EnchantmentEntityTick;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = HasoookMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TornadoModel.LAYER_LOCATION, TornadoModel::createBodyLayer);

        NeoForge.EVENT_BUS.register(EntityEnchantmentInteract.class);
        NeoForge.EVENT_BUS.register(EnchantmentEntityTick.class);
        NeoForge.EVENT_BUS.register(HideHeadHandler.class);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TORNADO.get(), TornadoEntity.createAttributes().build());
    }
}
