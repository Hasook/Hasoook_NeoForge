package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.*;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
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
    public static void betrayAttack(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity(); // 获取实体
        Entity sourceEntity = event.getSource().getEntity(); // 获取攻击者

        // 判断是不是直接伤害和生物
        if (event.getSource().isDirect() && sourceEntity instanceof LivingEntity attacker) {
            ItemStack targetMainHandItem = target.getMainHandItem(); // 获取实体的主手物品
            ItemStack attackerMainHandItem = attacker.getMainHandItem(); // 获取攻击者的主手物品
            int betrayLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.BETRAY, attackerMainHandItem);
            // 获取物品的背叛等级
            int loyaltyLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.LOYALTY, attackerMainHandItem);
            // 获取物品的忠诚等级

            Random random = new Random();
            int ran = random.nextInt(3 + loyaltyLevel);
            if (betrayLevel > ran) {
                if (sourceEntity instanceof ServerPlayer player) {
                    String name = attackerMainHandItem.getDisplayName().getString().replace("[", "").replace("]", "");
                    player.displayClientMessage(Component.translatable("hasoook.message.betray.attack", name), false);
                }
                if (target instanceof ServerPlayer player) {
                    String name = attackerMainHandItem.getDisplayName().getString().replace("[", "").replace("]", "");
                    player.displayClientMessage(Component.translatable("hasoook.message.betray.defense", name), false);
                }
                target.setItemInHand(InteractionHand.MAIN_HAND, attackerMainHandItem);
                attacker.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                if (!targetMainHandItem.isEmpty()) {
                    ItemEntity thrownItem = new ItemEntity(target.level(), target.getX(), target.getY() + 1.5, target.getZ(), targetMainHandItem.copy());
                    thrownItem.setPickUpDelay(10); // 设置拾取延迟
                    target.level().addFreshEntity(thrownItem); // 将物品添加到世界中
                }
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

    @SubscribeEvent
    public static void lootingWritableBook(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity(); // 获取实体
        Entity sourceEntity = event.getSource().getEntity(); // 获取攻击者

        if (sourceEntity instanceof LivingEntity attacker && !sourceEntity.level().isClientSide) {
            ItemStack attackerMainHandItem = attacker.getMainHandItem(); // 获取攻击者的主手物品
            int lootingLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, attackerMainHandItem);
            if (attackerMainHandItem.getItem() == Items.WRITABLE_BOOK && lootingLevel > 0) {
                // 获取目标的物品列表
                List<ItemStack> targetItems = new ArrayList<>();
                targetItems.add(target.getMainHandItem()); // 主手
                targetItems.add(target.getOffhandItem()); // 副手
                targetItems.add(target.getItemBySlot(EquipmentSlot.HEAD)); // 头盔
                targetItems.add(target.getItemBySlot(EquipmentSlot.CHEST)); // 胸甲
                targetItems.add(target.getItemBySlot(EquipmentSlot.LEGS)); // 裤子
                targetItems.add(target.getItemBySlot(EquipmentSlot.FEET)); // 鞋子

                // 移除空物品
                targetItems.removeIf(ItemStack::isEmpty);

                ItemStack newItemStack = new ItemStack(Items.ENCHANTED_BOOK); // 创建一个附魔书物品

                // 随机选择一个物品
                if (!targetItems.isEmpty()) {
                    Random rand = new Random();
                    ItemStack itemStack = targetItems.get(rand.nextInt(targetItems.size())); // 从列表中随机选择一个物品

                    // 获取该物品的所有附魔
                    ItemEnchantments itemEnchantments = itemStack.getTagEnchantments();

                    // 如果该物品有附魔
                    if (!itemEnchantments.isEmpty()) {
                        // 随机选择一个附魔
                        List<Holder<Enchantment>> enchantmentList = new ArrayList<>(itemEnchantments.keySet());
                        Holder<Enchantment> enchantment = enchantmentList.get(rand.nextInt(enchantmentList.size()));
                        int enchantmentLevel = itemEnchantments.getLevel(enchantment); // 获取该附魔的等级

                        // 移除物品上的指定附魔
                        EnchantmentHelper.updateEnchantments(itemStack, mutableEnchantments ->
                                mutableEnchantments.removeIf(enchantmentInstance -> enchantmentInstance.is(enchantment))
                        );

                        // 把指定附魔给到新物品，然后给到实体主手
                        newItemStack.enchant(enchantment,enchantmentLevel);
                        attacker.setItemInHand(InteractionHand.MAIN_HAND, newItemStack);
                    }
                }
            }
        }
    }
}
