package com.hasoook.hasoookmod.event.item;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

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
                        Math.min(2 + distance * 10, 400),
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
                        Math.min(1 + distance * 5, 200),
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
        ServerLevel serverLevel = (ServerLevel) entity.level();
        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET); // 获取脚部装备
        System.out.println(source);
        if (boots.is(ModItems.WATER_BOOTS) && !entity.level().isClientSide) {

            if (source.equals("onFire") || source.equals("inFire") || source.equals("hotFloor")) {
                event.setCanceled(true);
                boots.hurtAndBreak(1, entity, EquipmentSlot.FEET);
                entity.setRemainingFireTicks(0);
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5F, 1.0F
                );

                BlockPos entityPos = entity.blockPosition();
                BlockPos[] offsets = {
                        new BlockPos(0, 0, 0),
                        new BlockPos(1, 0, 0),
                        new BlockPos(-1, 0, 0),
                        new BlockPos(0, 0, 1),
                        new BlockPos(0, 0, -1)
                };
                for (BlockPos offset : offsets) {
                    BlockPos pos = entityPos.offset(offset);
                    if (entity.level().getBlockState(pos).getBlock() == Blocks.FIRE) {
                        entity.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                        boots.hurtAndBreak(1, entity, EquipmentSlot.FEET);
                    }
                }

                serverLevel.sendParticles(
                        ParticleTypes.LARGE_SMOKE,
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

            if (source.equals("lava")) {
                entity.setItemSlot(EquipmentSlot.FEET, new ItemStack(Blocks.STONE));
                entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F
                );
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
            }

            if (source.equals("freeze")) {
                entity.setItemSlot(EquipmentSlot.FEET, new ItemStack(Blocks.ICE)); // 替换为冰块
            }
        }
    }

    @SubscribeEvent
    public static void FarmlandTrampleEvent(BlockEvent.FarmlandTrampleEvent event) {
        LivingEntity entity = (LivingEntity) event.getEntity(); // 获取实体
        ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET); // 获取脚部装备
        if (boots.is(ModItems.WATER_BOOTS)) {
            event.setCanceled(true);
            BlockPos pos = event.getPos();
            // 遍历周围的3x3区域
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos targetPos = pos.offset(x, 0, z);
                    BlockState state = event.getLevel().getBlockState(targetPos);
                    if (state.getBlock() instanceof FarmBlock) {
                        ((ServerLevel) event.getLevel()).setBlockAndUpdate(targetPos, state.setValue(FarmBlock.MOISTURE, 7));
                    }
                }
            }
        }
    }

}
