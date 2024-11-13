package com.hasoook.hasoookmod.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class ConfusionEffect extends MobEffect {
    public ConfusionEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        if (entity instanceof Mob mob) {
            LivingEntity target = mob.getTarget();

            if (target == null || !target.isAlive()) {
                double range = 16.0 + 8.0 * amplifier;

                List<LivingEntity> nearbyEntities = entity.level().getEntitiesOfClass(
                        LivingEntity.class,
                        new AABB(entity.position().subtract(range, range, range), entity.position().add(range, range, range))
                );

                List<LivingEntity> validTargets = nearbyEntities.stream()
                        .filter(nearbyEntity -> !nearbyEntity.equals(entity) && !(nearbyEntity instanceof Player))
                        .toList();

                if (!validTargets.isEmpty()) {
                    Random random = new Random();
                    LivingEntity randomTarget = validTargets.get(random.nextInt(validTargets.size()));
                    if (mob.getSensing().hasLineOfSight(randomTarget)) { // 是否能看见目标
                        mob.setTarget(randomTarget); // 设置目标
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return tickCount % 2 == 0;
    }

    @Override
    public void onEffectStarted(@NotNull LivingEntity entity, int amplifier) {
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
        }
    }

}
