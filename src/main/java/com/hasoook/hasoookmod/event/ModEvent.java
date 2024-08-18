package com.hasoook.hasoookmod.event;

import com.hasoook.hasoookmod.event.enchantment.chain_damage;
import com.hasoook.hasoookmod.event.enchantment.swap;
import com.hasoook.hasoookmod.event.item.confusion_flower;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

public class ModEvent {
    public void HasoookMod(IEventBus modEventBus, ModContainer modContainer)
    {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(swap.class);
        NeoForge.EVENT_BUS.register(chain_damage.class);
        NeoForge.EVENT_BUS.register(confusion_flower.class);
    }
}
