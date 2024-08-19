package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class unyielding {
    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity(); // 获取实体
        ItemStack chestplate = entity.getItemBySlot(EquipmentSlot.CHEST); // 获取胸甲
        int unyieldingLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.UNYIELDING, chestplate);
        double amount = event.getAmount();
        double health = entity.getHealth();
        float absorptionAmount = entity.getAbsorptionAmount();

        if (unyieldingLevel > 0 && amount > health + absorptionAmount) {
            entity.setHealth(0.1F);
            entity.setAbsorptionAmount(0);
            event.setAmount(0);
            System.out.println(absorptionAmount);
        }

    }
}
