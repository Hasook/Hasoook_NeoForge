package com.hasoook.hasoookmod.event.entityEnchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class Damage {
    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        Entity entity = event.getEntity(); // 获取实体
        Entity sourceEntity = event.getSource().getEntity(); // 获取攻击者
        int fire_aspect = 0; // 火焰附加
        int knockback = 0; // 击退

        if (sourceEntity != null) {
            fire_aspect = EntityEnchantmentHelper.getEnchantmentLevel(sourceEntity, "minecraft:fire_aspect");
            // 获取攻击者的火焰附加等级
            knockback = EntityEnchantmentHelper.getEnchantmentLevel(sourceEntity, "minecraft:knockback");
            // 获取攻击者的击退等级
        }

        if (fire_aspect > 0) {
            entity.setRemainingFireTicks(fire_aspect * 80);
        }

        if (knockback > 0) {
            // 计算击退向量
            Vec3 direction = entity.position().subtract(sourceEntity.position()).normalize(); // 获取从攻击者到受害者的单位向量
            double strength = 0.8 * knockback; // 设定击退的力量
            entity.push(direction.x * strength, direction.y * strength, direction.z * strength); // 应用击退效果
        }
    }

    @SubscribeEvent
    public static void fireImmune(LivingIncomingDamageEvent event) {
        Entity entity = event.getEntity(); // 获取实体
        int fireProtection = EntityEnchantmentHelper.getEnchantmentLevel(entity, "minecraft:fire_protection");
        if (fireProtection > 0) {
            String source = event.getSource().getMsgId();
            if (source.equals("onFire") || source.equals("inFire")) {
                event.setCanceled(true);
            }
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
