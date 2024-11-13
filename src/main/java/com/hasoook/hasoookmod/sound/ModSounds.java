package com.hasoook.hasoookmod.sound;

import com.hasoook.hasoookmod.HasoookMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, HasoookMod.MOD_ID);

    public static final String SUBTITLES_DONG = "subtitles.item.huge_diamond_pickaxe.dong";
    public static final DeferredHolder<SoundEvent, SoundEvent> DONG = SOUNDS.register(
            "item.huge_diamond_pickaxe.dong",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(HasoookMod.MOD_ID, "item.huge_diamond_pickaxe.dong"))
    );
}
