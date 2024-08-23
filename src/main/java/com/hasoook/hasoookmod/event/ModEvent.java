package com.hasoook.hasoookmod.event;

import com.hasoook.hasoookmod.event.effect.UnyieldingExpired;
import com.hasoook.hasoookmod.event.enchantment.UnyieldingDamage;
import com.hasoook.hasoookmod.event.enchantment.ChainDamageAttack;
import com.hasoook.hasoookmod.event.enchantment.SwapAttack;
import com.hasoook.hasoookmod.event.item.confusion_flower;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

public class ModEvent {
    public void HasoookMod(IEventBus modEventBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(SwapAttack.class);
        NeoForge.EVENT_BUS.register(ChainDamageAttack.class);
        NeoForge.EVENT_BUS.register(confusion_flower.class);
        NeoForge.EVENT_BUS.register(UnyieldingDamage.class);
        NeoForge.EVENT_BUS.register(UnyieldingExpired.class);
    }
}
