package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class swap {
    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity(); // 获取实体
        Entity sourceEntity = event.getSource().getEntity(); // 获取攻击者

        if (sourceEntity instanceof LivingEntity attacker) {
            ItemStack attackerMainHandItem = attacker.getMainHandItem(); // 获取攻击者的主手物品
            int swapLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.SWAP, attackerMainHandItem);

            if (swapLevel > 0) {
                ItemStack targetMainHandItem = target.getMainHandItem(); // 获取目标的主手物品

                attacker.setItemInHand(InteractionHand.MAIN_HAND, targetMainHandItem);
                target.setItemInHand(InteractionHand.MAIN_HAND, attackerMainHandItem);
                // 交换双方的主手物品
            }
        }
    }
}
