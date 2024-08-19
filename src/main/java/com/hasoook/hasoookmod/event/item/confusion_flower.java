package com.hasoook.hasoookmod.event.item;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.block.ModBlock;
import com.hasoook.hasoookmod.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class confusion_flower {
    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        LivingEntity target = event.getEntity(); // 获取实体
        Entity sourceEntity = event.getSource().getEntity(); // 获取攻击者

        if (sourceEntity instanceof LivingEntity attacker && target instanceof LivingEntity) {
            ItemStack itemStack = attacker.getMainHandItem();
            if (itemStack.getItem() == ModBlock.CONFUSION_FLOWER.asItem() && !attacker.level().isClientSide) {
                MobEffectInstance poisonEffect = new MobEffectInstance(ModEffects.CONFUSION, 1200, 0);
                target.addEffect(poisonEffect);
                itemStack.consume(1, attacker);
            }
        }

    }
}
