package com.hasoook.hasoookmod.effect;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.effect.custom.ConfusionEffect;
import com.hasoook.hasoookmod.effect.custom.NormalEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEffects {
    public static DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, HasoookMod.MODID);

    public static final DeferredHolder<MobEffect,MobEffect> CONFUSION =registerDeferredHolder("confusion",()->new ConfusionEffect(MobEffectCategory.HARMFUL,0xe55590)) ;

    public static DeferredHolder<MobEffect,MobEffect> registerDeferredHolder(String name, Supplier<MobEffect> supplier){
        return EFFECTS.register(name,supplier);
    }

    public static void register(IEventBus eventBus){
        EFFECTS.register(eventBus);
    }
}
