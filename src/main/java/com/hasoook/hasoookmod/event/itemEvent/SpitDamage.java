package com.hasoook.hasoookmod.event.itemEvent;

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

@EventBusSubscriber(modid = HasoookMod.MODID)
public class SpitDamage {
    @SubscribeEvent
    public static void onEntityAttack(LivingIncomingDamageEvent event) {
        Entity entity = event.getEntity(); // 获取实体
        Entity source = event.getSource().getEntity();
        if (source instanceof Llama && entity instanceof Player) { // 检查攻击者是否是羊驼
            if (!entity.level().isClientSide && Config.lamaGiveSpit) {
                Player player = (Player) entity; // 转换为玩家类型
                ItemStack spit = new ItemStack(ModItems.SPIT.get(), 1);
                player.getInventory().add(spit);
            }
        }
    }
}
