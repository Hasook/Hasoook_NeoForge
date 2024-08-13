package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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
            ItemStack targetMainHandItem = target.getMainHandItem(); // 获取实体的主手物品
            ItemStack attackerMainHandItem = attacker.getMainHandItem(); // 获取攻击者的主手物品
            int giveLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.GIVE, attackerMainHandItem);
            int swapLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.SWAP, attackerMainHandItem);

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
}
