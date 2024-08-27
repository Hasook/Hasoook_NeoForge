package com.hasoook.hasoookmod.event.entityEnchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentHelper;
import net.minecraft.world.entity.Entity;
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
}
