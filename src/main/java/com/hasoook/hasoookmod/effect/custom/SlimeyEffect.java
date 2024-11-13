package com.hasoook.hasoookmod.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

// Climbing Effect by SameDifferent: https://github.com/samedifferent/TrickOrTreat/blob/master/LICENSE
// Distributed under MIT
public class SlimeyEffect extends MobEffect {
    public SlimeyEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        // 如果实体与某个表面发生水平方向的碰撞
        if(livingEntity.horizontalCollision) {
            // 获取实体当前的速度向量
            Vec3 initialVec = livingEntity.getDeltaMovement();
            // 创建一个新的速度向量，保留水平方向的分量，设置竖直方向的速度为0.2D（使实体能够向上攀爬）
            Vec3 climbVec = new Vec3(initialVec.x, 0.2D, initialVec.z);
            // 设置实体的速度，`scale(0.96D)` 用于稍微降低实体的速度（模拟黏滑效果）
            livingEntity.setDeltaMovement(climbVec.scale(0.96D));
            return true;
        }

        // 如果没有发生水平方向的碰撞，则调用父类的 applyEffectTick 方法，应用默认的效果逻辑
        return super.applyEffectTick(livingEntity, amplifier);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true; // 总是应用效果
    }
}
