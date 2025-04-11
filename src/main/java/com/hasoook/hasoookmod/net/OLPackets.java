package com.hasoook.hasoookmod.net;

import com.hasoook.hasoookmod.HasoookMod;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = HasoookMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class OLPackets {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event){
        final PayloadRegistrar registrar = event.registrar(HasoookMod.MOD_ID).executesOn(HandlerThread.NETWORK);
        registrar.playToClient(
                LouisXVIS2CPacket.TYPE,
                LouisXVIS2CPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(LouisXVIS2CPacket::handle,null)
        );
        registrar.playToServer(
                ControlInputPacket.TYPE,
                ControlInputPacket.STREAM_CODEC,
                ControlInputPacket::handle
        );
        registrar.playToServer(
                LeftClickAirPacket.TYPE,
                LeftClickAirPacket.STREAM_CODEC,
                LeftClickAirPacket::handle
        );
    }
}

