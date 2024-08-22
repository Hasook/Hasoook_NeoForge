package com.hasoook.hasoookmod.event.effect;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.effect.ModEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.Objects;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class UnyieldingExpired {
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        // 效果被移除时
        handleEffectEvent(event);
    }

    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        // 效果结束时
        handleEffectEvent(event);
    }

    private static void handleEffectEvent(MobEffectEvent event) {
        MobEffect effect = Objects.requireNonNull(event.getEffectInstance()).getEffect().value();
        if (effect == ModEffects.UNYIELDING.get()) {
            LivingEntity entity = event.getEntity();
            entity.kill(); // 杀死实体
        }
    }
}
