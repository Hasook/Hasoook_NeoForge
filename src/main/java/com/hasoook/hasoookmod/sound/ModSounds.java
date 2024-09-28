package com.hasoook.hasoookmod.sound;

import com.hasoook.hasoookmod.HasoookMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, HasoookMod.MODID);

    public static final String DONG = "subtitles.item.huge_diamond_pickaxe.dong";

    public static final DeferredHolder<SoundEvent, SoundEvent> BILI_COIN_THROW_SOUND = SOUNDS.register(
            "item.huge_diamond_pickaxe.dong",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(HasoookMod.MODID, "item.huge_diamond_pickaxe.dong"))
    );
}