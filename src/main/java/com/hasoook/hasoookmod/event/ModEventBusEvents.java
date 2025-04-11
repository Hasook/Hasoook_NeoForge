package com.hasoook.hasoookmod.event;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.client.render.HideHeadHandler;
import com.hasoook.hasoookmod.entity.ModEntities;
import com.hasoook.hasoookmod.entity.client.MeteoriteModel;
import com.hasoook.hasoookmod.entity.client.TornadoModel;
import com.hasoook.hasoookmod.entity.custom.MeteoriteEntity;
import com.hasoook.hasoookmod.entity.custom.TornadoEntity;
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
        event.registerLayerDefinition(MeteoriteModel.LAYER_LOCATION, MeteoriteModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.TORNADO.get(), TornadoEntity.createAttributes().build());
        event.put(ModEntities.METEORITE.get(), MeteoriteEntity.createAttributes().build());
    }
}
