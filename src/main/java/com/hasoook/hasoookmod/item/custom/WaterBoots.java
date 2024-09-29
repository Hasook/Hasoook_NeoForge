package com.hasoook.hasoookmod.item.custom;

import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class WaterBoots extends ArmorItem {
    public WaterBoots(Holder<ArmorMaterial> pMaterial, Type pType, Properties pProperties) {
        super(pMaterial, pType, pProperties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // 只在物品在装备槽36时执行（脚部位置），并且确保不是客户端
        if (slotId == 36 && !level.isClientSide && entity instanceof LivingEntity livingEntity) {
            ServerLevel serverLevel = (ServerLevel) level;
            Random random = new Random();

            if (random.nextInt(20) == 0) {
                handleTemperatureEffects(stack, livingEntity, serverLevel);
                spawnWaterParticles(livingEntity, serverLevel);
            }

            // 如果在极热的维度中（地狱），移除靴子并生成烟雾
            if (level.dimensionType().ultraWarm()) {
                removeBootsAndSpawnSmoke(livingEntity, serverLevel);
            }
        }
    }

    // 处理温度影响
    private void handleTemperatureEffects(ItemStack stack, LivingEntity entity, ServerLevel serverLevel) {
        float temperature = serverLevel.getBiome(entity.blockPosition()).value().getBaseTemperature();
        int currentDamage = stack.getDamageValue(); // 当前耐久值
        int maxDamage = stack.getMaxDamage(); // 最大耐久值

        // 如果在寒冷群系
        if (temperature <= 0 && currentDamage < maxDamage - 1) {
            stack.hurtAndBreak(1, entity, EquipmentSlot.FEET);
        } else if (temperature <= 0 && currentDamage >= maxDamage - 1) {
            entity.setItemSlot(EquipmentSlot.FEET, new ItemStack(Blocks.ICE)); // 替换为冰块
        }
        // 如果在高温群系
        if (temperature >= 2) {
            stack.hurtAndBreak(1, entity, EquipmentSlot.FEET);
        }
    }

    // 生成水粒子
    private void spawnWaterParticles(LivingEntity entity, ServerLevel serverLevel) {
        serverLevel.sendParticles(
                ParticleTypes.FALLING_WATER,
                entity.getX(),
                entity.getY() + 0.2,
                entity.getZ(),
                1,
                entity.getBbWidth() / 2,
                0.2,
                entity.getBbWidth() / 2,
                0
        );
    }

    // 移除靴子并生成烟雾
    private void removeBootsAndSpawnSmoke(LivingEntity entity, ServerLevel serverLevel) {
        entity.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY); // 移除靴子
        serverLevel.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                entity.getX(),
                entity.getY() + 0.2,
                entity.getZ(),
                10,
                entity.getBbWidth() / 2,
                0.2,
                entity.getBbWidth() / 2,
                0
        );
        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F
        );
    }
}
