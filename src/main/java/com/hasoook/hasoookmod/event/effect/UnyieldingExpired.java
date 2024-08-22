package com.hasoook.hasoookmod.event.effect;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.effect.ModEffects;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.Objects;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class UnyieldingExpired {
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Remove  event) {

        MobEffect effect = Objects.requireNonNull(event.getEffectInstance()).getEffect().value();
        // 获取药水效果

        if (effect == ModEffects.UNYIELDING.get()) {
            DamageSource source = event.getEntity().getLastDamageSource();
            System.out.println(source);
            LivingEntity entity = event.getEntity();
            entity.kill(); // 杀死实体
        }
    }
}
