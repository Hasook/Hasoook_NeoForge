package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class DamageEvent {
    @SubscribeEvent
    public static void swapAttack(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity(); // 获取实体
        Entity sourceEntity = event.getSource().getEntity(); // 获取攻击者

        if (sourceEntity instanceof LivingEntity attacker) {
            ItemStack targetMainHandItem = target.getMainHandItem(); // 获取实体的主手物品
            ItemStack attackerMainHandItem = attacker.getMainHandItem(); // 获取攻击者的主手物品
            int giveLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.GIVE, attackerMainHandItem);
            // 获取物品的给予等级
            int swapLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.SWAP, attackerMainHandItem);
            // 获取物品的交换等级

            if (giveLevel > 0 && targetMainHandItem.isEmpty()) {
                EnchantmentHelper.updateEnchantments(attackerMainHandItem, p_330066_ -> p_330066_.removeIf(p_344368_ -> p_344368_.is(ModEnchantments.GIVE)));
                // 移除附魔
                target.setItemInHand(InteractionHand.MAIN_HAND, attackerMainHandItem);
                attacker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                // 把攻击者的主手物品设置到实体的主手
            }

            if (swapLevel > 0 && !targetMainHandItem.isEmpty()) {
                attacker.setItemInHand(InteractionHand.MAIN_HAND, targetMainHandItem);
                target.setItemInHand(InteractionHand.MAIN_HAND, attackerMainHandItem);
                // 交换双方的主手物品
            }
        }
    }

    @SubscribeEvent
    public static void heartlessAttack(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity(); // 获取实体
        Entity sourceEntity = event.getSource().getEntity(); // 获取攻击者

        if (sourceEntity instanceof LivingEntity attacker && !sourceEntity.level().isClientSide) {
            ItemStack attackerMainHandItem = attacker.getMainHandItem(); // 获取攻击者的主手物品
            int heartlessLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.HEARTLESS, attackerMainHandItem);
            if (heartlessLevel > 0) {
                event.setCanceled(true); // 取消交互事件
                float amount = event.getAmount();
                float health = target.getHealth();
                target.setHealth(health - amount);
            }
        }
    }
}
