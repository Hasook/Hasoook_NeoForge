package com.hasoook.hasoookmod.event.item;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.item.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class WaterBootsEvent {
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity(); // 获取实体
        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET); // 获取脚部装备
        if (boots.is(ModItems.WATER_BOOTS) && !entity.level().isClientSide) {
            int distance = (int) event.getDistance();
            event.setDistance(0); // 将坠落距离设置为0

            // 播放落水音效
            if (distance > 5) {
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 1.0F);
            }

            // 粒子效果
            if (entity.level() instanceof ServerLevel serverlevel) {
                serverlevel.sendParticles(
                        ParticleTypes.SPLASH,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        Math.min(distance * 2, 100),
                        entity.getBbWidth() / 1.5,
                        entity.getBbHeight() / 5,
                        entity.getBbWidth() / 1.5,
                        1
                );

                serverlevel.sendParticles(
                        ParticleTypes.BUBBLE,
                        entity.getX(),
                        entity.getY(),
                        entity.getZ(),
                        distance,
                        entity.getBbWidth() / 1.8,
                        entity.getBbHeight() / 5,
                        entity.getBbWidth() / 1.8,
                        1
                );
            }
        }
    }
}
