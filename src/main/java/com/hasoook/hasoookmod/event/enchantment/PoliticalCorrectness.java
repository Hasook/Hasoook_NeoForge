package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class PoliticalCorrectness {
    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        Entity sourceEntity = event.getSource().getEntity();
        int pcLvl = 0;

        if (sourceEntity instanceof LivingEntity livingSourceEntity) {
            pcLvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.POLITICAL_CORRECTNESS, livingSourceEntity.getMainHandItem());
        }

        if (pcLvl > 0 && !entity.level().isClientSide) {
            String entityType = entity.getType().toString();  // 获取实体类型名称
            String pcEntityType = sourceEntity.getPersistentData().getString("pcEntity");  // 获取保存的实体类型

            if (pcEntityType.equals(entityType)) {
                event.setCanceled(true);
                sourceEntity.sendSystemMessage(Component.nullToEmpty("你的攻击目标不够多元化！"));
            } else {
                sourceEntity.getPersistentData().putString("pcEntity", entityType);
            }

            System.out.println("Entity Type: " + entityType + ", Stored Entity Type: " + pcEntityType);
        }
    }
}
