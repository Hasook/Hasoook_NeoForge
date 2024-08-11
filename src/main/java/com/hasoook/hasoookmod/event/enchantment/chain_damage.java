package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.List;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class chain_damage {
    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity(); // 获取实体
        Entity sourceEntity = event.getSource().getEntity(); // 获取攻击者

        if (sourceEntity instanceof LivingEntity attacker) {
            ItemStack attackerMainHandItem = attacker.getMainHandItem(); // 获取攻击者的主手物品
            int swapLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.CHAIN_DAMAGE, attackerMainHandItem);

            if (swapLevel > 0) {
                float damage = event.getAmount();
                Level world = target.level(); // 获取当前世界对象
                double radius = 5.0; // 设置半径值

                // 获取周围与目标实体相同类型的实体
                AABB searchBox = target.getBoundingBox().inflate(radius);
                List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(LivingEntity.class, searchBox, e -> e.getClass().equals(target.getClass()));

                for (LivingEntity entity : nearbyEntities) {
                    if (entity != target) { // 不对目标自身造成伤害
                        entity.setHealth(0);
                    }
                }
            }
        }
    }
}
