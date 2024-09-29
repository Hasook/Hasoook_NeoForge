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
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class WaterBootsEvent {
    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity(); // 获取实体
        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET); // 获取脚部装备

        if (boots.is(ModItems.WATER_BOOTS) && !entity.level().isClientSide) {
            int distance = (int) event.getDistance();
            event.setDistance(0); // 将坠落距离设置为0

            if (entity.level() instanceof ServerLevel serverlevel) {
                if (distance > 3) {
                    // 播放落水音效
                    serverlevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                            SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.0F, 1.0F
                    );
                }

                // 粒子效果
                serverlevel.sendParticles(
                        ParticleTypes.SPLASH,
                        entity.getX(),
                        entity.getY() + 0.2,
                        entity.getZ(),
                        Math.min(4 + distance * 10, 200),
                        entity.getBbWidth() / 1.8,
                        entity.getBbHeight() / 4,
                        entity.getBbWidth() / 1.8,
                        1
                );
                serverlevel.sendParticles(
                        ParticleTypes.BUBBLE,
                        entity.getX(),
                        entity.getY() + 0.2,
                        entity.getZ(),
                        Math.min(4 + distance * 5, 200),
                        entity.getBbWidth() / 1.8,
                        entity.getBbHeight() / 4,
                        entity.getBbWidth() / 1.8,
                        1
                );
            }
        }
    }
    @SubscribeEvent
    public static void swapAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity(); // 获取实体
        String source = event.getSource().getMsgId();
        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET); // 获取脚部装备
        if (boots.is(ModItems.WATER_BOOTS) && !entity.level().isClientSide) {
            if (source.equals("onFire") || source.equals("inFire")) {
                event.setCanceled(true);
                boots.hurtAndBreak(1, entity, EquipmentSlot.FEET);
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F
                );
            }
            if (source.equals("lava")) {
                entity.setItemSlot(EquipmentSlot.FEET, new ItemStack(Blocks.STONE, 2));
            }
        }
    }
}
