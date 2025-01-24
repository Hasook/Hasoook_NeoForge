package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class Spotlight {
    @SubscribeEvent
    public static void Damage(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity(); // 获取实体
        Entity sourceEntity = event.getSource().getEntity(); // 获取攻击者

        // 确保攻击者是LivingEntity
        if (sourceEntity instanceof LivingEntity livingSource) {
            ItemStack sourceMainHandItem = livingSource.getMainHandItem(); // 获取攻击者的主手物品
            int sLvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.SPOTLIGHT, sourceMainHandItem);

            if (sLvl > 0 && livingSource.level() instanceof ServerLevel serverLevel) {
                BlockPos sourcePos = livingSource.blockPosition(); // 获取攻击者当前位置
                Vec3 targetPos = entity.position().add(0, entity.getBbHeight() / 2.0, 0); // 获取实体位置
                int count = 0; // 记录方块数量
                int range = Math.min(sLvl, 32); // 范围（为了照顾性能有最大限制）

                // 遍历范围内的方块
                for (int x = -range; x <= range; x++) {
                    for (int y = -range; y <= range; y++) {
                        for (int z = -range; z <= range; z++) {
                            // 计算方块的位置
                            BlockPos pos = sourcePos.offset(x, y, z);
                            // 获取方块的块状态
                            BlockState blockState = serverLevel.getBlockState(pos);
                            // 获取方块的亮度
                            int lightLevel = blockState.getLightEmission(serverLevel, pos);

                            // 如果亮度大于 15
                            if (lightLevel >= 15) {
                                count++;
                                // 计算从方块位置到被攻击者位置的方向
                                Vec3 blockPos = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5); // 方块的位置中心
                                Vec3 direction = targetPos.subtract(blockPos).normalize(); // 计算方向向量

                                // 计算方块到实体的距离
                                double distance = blockPos.distanceTo(targetPos);

                                // 通过距离来动态设置粒子数量和步长
                                int numParticles = (int) (distance / 0.5); // 粒子数量根据距离来设置，0.5 控制粒子间隔
                                if (numParticles < 10) numParticles = 10; // 最少生成 10 个粒子

                                // 生成粒子沿路径
                                for (int i = 0; i < numParticles; i++) {
                                    // 计算每个粒子的位置，按步长推进
                                    Vec3 particlePos = blockPos.add(direction.x * i * 0.5, direction.y * i * 0.5, direction.z * i * 0.5); // 步长 0.5

                                    // 在路径上生成粒子
                                    serverLevel.sendParticles(ParticleTypes.END_ROD, particlePos.x, particlePos.y, particlePos.z, 1, 0.1, 0.1, 0.1, 0.02);
                                }
                            }
                        }
                    }
                }
                // 修改伤害量
                float amount = event.getAmount();
                event.setAmount(amount + count);

                // 发送粒子效果
                serverLevel.sendParticles(ParticleTypes.END_ROD, targetPos.x, targetPos.y, targetPos.z, count * 5, 0.1, 0.1, 0.1, 0.1 + 0.001 * count);

                // 如果亮度大于20，添加火焰效果
                if (count > 20) {
                    entity.setRemainingFireTicks(Math.min(count * 5, 200)); // 设置火焰时间
                    serverLevel.sendParticles(ParticleTypes.FLAME, targetPos.x, targetPos.y, targetPos.z, count, 0.1, 0.1, 0.1, 0.1);
                }
            }
        }
    }
}
