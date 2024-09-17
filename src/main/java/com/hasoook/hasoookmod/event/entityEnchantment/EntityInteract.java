package com.hasoook.hasoookmod.event.entityEnchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class EntityInteract {
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        Entity target = event.getTarget();
        int channeling = EntityEnchantmentHelper.getEnchantmentLevel(target,"minecraft:channeling");
        Level level = event.getLevel();
        if (channeling > 0 && !(target instanceof Creeper) && !event.getLevel().isClientSide ) {
            EntityEnchantmentHelper.removeEnchantment(target,"minecraft:channeling");
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
            if (lightning != null) {
                lightning.moveTo(event.getEntity().position()); // 设置位置
                level.addFreshEntity(lightning); // 生成闪电
            }
            event.setCanceled(true); // 取消交互事件
        }
    }
}
