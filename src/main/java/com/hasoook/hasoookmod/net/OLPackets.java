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
        registrar.playBidirectional(
                LouisXVIS2CPacket.TYPE,
                LouisXVIS2CPacket.STREAM_CODEC,
                new DirectionalPayloadHandler<>(LouisXVIS2CPacket::handle,null)
        );
        // 注册新的控制输入包（客户端->服务端）
        registrar.playToServer(
                ControlInputPacket.TYPE,
                ControlInputPacket.STREAM_CODEC,
                ControlInputPacket::handle
        );
    }
}

