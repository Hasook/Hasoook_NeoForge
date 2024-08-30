package com.hasoook.hasoookmod.event.entityEnchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentHelper;
import com.hasoook.hasoookmod.entityEnchantment.EntityEnchantmentInteract;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
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
        if (channeling > 0 && target instanceof Villager || target instanceof Pig || target instanceof Minecart || target instanceof Boat) {
            // 在目标位置生成闪电
            Level level = event.getLevel();
            if (!level.isClientSide) { // 确保只有在服务器端生成闪电
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
                if (lightning != null) {
                    lightning.moveTo(target.position());
                    level.addFreshEntity(lightning);
                }
            }
            if (target instanceof Villager) {
                event.setCanceled(true); // 取消交互事件
            }
        }
    }
}
