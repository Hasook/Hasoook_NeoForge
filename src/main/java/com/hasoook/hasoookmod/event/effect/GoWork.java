package com.hasoook.hasoookmod.event.effect;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.effect.ModEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.util.Objects;
import java.util.WeakHashMap;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class GoWork {
    // 效果被移除时
    @SubscribeEvent
    public static void onEffectRemoved(MobEffectEvent.Remove event) {
        if (Objects.requireNonNull(event.getEffectInstance()).is(ModEffects.GO_WORK)) {
            handleEffectEnd(event);
        }
    }

    // 效果过期时
    @SubscribeEvent
    public static void onEffectExpired(MobEffectEvent.Expired event) {
        if (Objects.requireNonNull(event.getEffectInstance()).is(ModEffects.GO_WORK)) {
            handleEffectEnd(event);
        }
    }

    private static void handleEffectEnd(MobEffectEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Mob mob && !entity.level().isClientSide) {
            mob.targetSelector.setControlFlag(Goal.Flag.TARGET, true);
            mob.targetSelector.tick();
            mob.setTarget(null);
        }
    }
}
