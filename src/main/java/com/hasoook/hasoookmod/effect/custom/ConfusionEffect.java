package com.hasoook.hasoookmod.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Random;

public class ConfusionEffect extends MobEffect {
    public ConfusionEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Mob) {
            Mob mob = (Mob) entity;
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
                    mob.setTarget(randomTarget);
                }
            }
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int tickCount, int amplifier) {
        return tickCount % 2 == 0; // 你可以根据需要替换此检查
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
        }
    }

}