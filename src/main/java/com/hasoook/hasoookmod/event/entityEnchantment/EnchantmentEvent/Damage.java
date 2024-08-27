package com.hasoook.hasoookmod.event.entityEnchantment.EnchantmentEvent;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.event.entityEnchantment.EntityEnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class Damage {
    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        Entity entity = event.getEntity(); // 获取实体
        Entity sourceEntity = event.getSource().getEntity(); // 获取攻击者
        int fire_aspect = 0; // 火焰附加等级

        if (sourceEntity != null) {
            fire_aspect = EntityEnchantmentHelper.getEnchantmentLevel(sourceEntity, "minecraft:fire_aspect");
            // 获取攻击者的火焰附加等级
        }

        if (fire_aspect > 0) {
            entity.setRemainingFireTicks(fire_aspect * 80);
        }
    }
}
