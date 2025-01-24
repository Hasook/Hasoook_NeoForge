package com.hasoook.hasoookmod.event.entityEnchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class Damage {
    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        Entity entity = event.getEntity(); // 获取实体
        Entity sourceEntity = event.getSource().getEntity(); // 获取攻击者

        // 确保攻击者是 LivingEntity 类型
        if (!(sourceEntity instanceof LivingEntity attacker)) {
            return;  // 如果不是 LivingEntity 类型，直接返回，不进行后续操作
        }

        // 安全地将攻击者转换为 LivingEntity
        int fireAspectLevel = EntityEnchantmentHelper.getEnchantmentLevel(attacker, "minecraft:fire_aspect");
        int knockBackLevel = EntityEnchantmentHelper.getEnchantmentLevel(attacker, "minecraft:knockback");

        // 火焰附加效果
        if (fireAspectLevel > 0) {
            entity.setRemainingFireTicks(fireAspectLevel * 80); // 设置火焰持续时间
        }

        // 击退效果
        if (knockBackLevel > 0) {
            Vec3 direction = entity.position().subtract(sourceEntity.position()).normalize(); // 计算击退向量
            double knockbackStrength = 0.8 * knockBackLevel; // 设定击退的力量
            entity.push(direction.x * knockbackStrength, direction.y * knockbackStrength, direction.z * knockbackStrength); // 应用击退
        }
    }

    @SubscribeEvent
    public static void channelingAttack(LivingIncomingDamageEvent event) {
        Entity entity = event.getEntity(); // 获取实体
        Entity sourceEntity = event.getSource().getEntity(); // 获取攻击者
        int channeling = EntityEnchantmentHelper.getEnchantmentLevel(entity,"minecraft:channeling");
        if (channeling > 0) {
            EntityEnchantmentHelper.removeEnchantment(entity,"minecraft:channeling");
            // 在目标位置生成闪电
            Level level = event.getEntity().level();
            if (!level.isClientSide) {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                if (lightning != null && sourceEntity != null) {
                    lightning.moveTo((sourceEntity.position())); // 设置位置
                    level.addFreshEntity(lightning); // 生成闪电
                }
            }
        }
    }

}
