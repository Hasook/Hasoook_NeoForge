package com.hasoook.hasoookmod.event.entity;

import com.hasoook.hasoookmod.Config;
import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class LlamaSpitDamage {
    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {

        Entity entity = event.getEntity(); // 获取实体
        Entity source = event.getSource().getEntity(); // 获取攻击者

        if (source instanceof Llama && entity instanceof Player player && !entity.level().isClientSide && Config.lamaGiveSpit) {
            ItemStack spit = new ItemStack(ModItems.SPIT.get(), 1);
            player.getInventory().add(spit); // 给玩家一个口水物品
        }
    }
}