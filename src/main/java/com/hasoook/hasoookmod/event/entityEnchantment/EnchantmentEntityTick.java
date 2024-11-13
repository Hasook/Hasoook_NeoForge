package com.hasoook.hasoookmod.event.entityEnchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class EnchantmentEntityTick {
    @SubscribeEvent
    public static void EntityTick(EntityTickEvent.Post event){
        Entity entity = event.getEntity();
        int size = EntityEnchantmentHelper.getEnchantmentSize(entity);
        // 获取附魔词条数
        int frostWalkerLevel = EntityEnchantmentHelper.getEnchantmentLevel(entity, "minecraft:frost_walker");
        // 获取冰霜行者附魔的等级

        // 当实体拥有附魔NBT时，生成粒子效果
        if (size > 0 && entity.level() instanceof ServerLevel serverLevel) {
            if (entity.getRandom().nextInt(8) == 0) {
                double x = entity.getX();
                double y = entity.getY() + entity.getBbHeight() / 2;
                double z = entity.getZ();
                double width = entity.getBbWidth() / 2.5;
                double height = entity.getBbHeight() / 2.5;
                serverLevel.sendParticles(ParticleTypes.WITCH, x, y, z, 1, width, height, width, 0.0);
            }
        }

        if (frostWalkerLevel > 0 && entity.onGround() && !entity.level().isClientSide) {

            BlockPos pos = entity.blockPosition();
            int range = Math.min(16, 2 + frostWalkerLevel);

            for (int x = -range; x <= range; x++) {
                for (int z = -range; z <= range; z++) {
                    double distance = Math.sqrt(x * x + z * z); // 计算当前点到中心点的距离
                    if (distance <= range - 0.2) { // 判断距离是否在范围内
                        BlockPos targetPos = pos.offset(x, -1, z);

                        // 检查目标位置的方块是否是水
                        if (entity.level().getBlockState(targetPos).getBlock() == Blocks.WATER && entity.level().getBlockState(targetPos.above()).getBlock() == Blocks.AIR) {
                            entity.level().setBlockAndUpdate(targetPos, Blocks.FROSTED_ICE.defaultBlockState());
                            // 替换方块为冰霜冰块
                        }
                    }
                }
            }
        }
    }
}
