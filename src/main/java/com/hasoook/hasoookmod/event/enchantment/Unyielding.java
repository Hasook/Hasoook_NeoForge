package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.effect.ModEffects;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class Unyielding {
    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity(); // 获取实体
        ItemStack chestplate = entity.getItemBySlot(EquipmentSlot.CHEST); // 获取胸甲
        boolean mainHand = entity.getMainHandItem().getItem() == Items.TOTEM_OF_UNDYING; // 检查主手物品
        boolean offHand  = entity.getOffhandItem().getItem() == Items.TOTEM_OF_UNDYING; // 检查副手物品
        int unyieldingLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.UNYIELDING, chestplate);
        double amount = event.getAmount(); // 获取伤害值
        double health = entity.getHealth() + entity.getAbsorptionAmount(); // 获取生命值+伤害吸收值
        String source = event.getSource().getMsgId(); // 获取伤害类型
        MobEffectInstance strengthEffect = entity.getEffect(ModEffects.UNYIELDING); // 获取药水效果
        int strengthLevel = (strengthEffect != null) ? strengthEffect.getAmplifier() + 1 : 0;

        if (unyieldingLevel > 0 && amount > health && strengthEffect == null && !source.equals("genericKill") && !mainHand && !offHand) {
            entity.setHealth(0.1F); // 将生命设置为0.1
            entity.setAbsorptionAmount(0); // 将生命吸收血量设置为0
            event.setAmount(0); // 将伤害设置为0
            MobEffectInstance effect = new MobEffectInstance(ModEffects.UNYIELDING, 100, 0);
            entity.addEffect(effect); // 给予不屈效果
        }

        if (strengthLevel > 0 && !source.equals("genericKill")) {
            /* CompoundTag nbt = entity.getPersistentData();
            double totalDamage = nbt.getDouble("LastDamageValue"); // 获取LastDamageValue的值
            totalDamage += amount; // 加上受到的伤害值
            nbt.putDouble("LastDamageValue", totalDamage); // 设置nbt值
            System.out.println(nbt.getDouble("LastDamageValue")); */

            entity.setHealth(0.1F);
            entity.setAbsorptionAmount(0);
            event.setAmount(0);
        }
    }

}
