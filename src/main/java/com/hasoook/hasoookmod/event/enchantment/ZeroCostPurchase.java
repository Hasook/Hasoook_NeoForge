package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class ZeroCostPurchase {
    @SubscribeEvent
    public static void PlayerTickEvent(PlayerTickEvent.Post event){
        Player player = event.getEntity();
        ItemStack itemStack = player.getMainHandItem();
        int ZCPLvl = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.ZERO_COST_PURCHASE, itemStack);
        if (ZCPLvl > 0 && !player.level().isClientSide) {
            // 获取玩家当前所在的世界
            Level level = player.level();

            // 获取玩家周围5格的所有实体
            List<Villager> villagers = level.getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(5), EntitySelector.NO_SPECTATORS);

            // 对所有村民进行处理
            for (Villager villager : villagers) {
                // 村民逃离玩家的逻辑
                double escapeDistance = 5.0; // 逃离的最小距离
                double dx = villager.getX() - player.getX();
                double dy = villager.getY() - player.getY();
                double dz = villager.getZ() - player.getZ();
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                if (distance < escapeDistance) {
                    // 让村民颤抖的效果
                    double shakeX = villager.getX() + (Math.random() - 0.5) * 0.2;
                    double shakeY = villager.getY() + (Math.random() - 0.5) * 0.2;
                    double shakeZ = villager.getZ() + (Math.random() - 0.5) * 0.2;

                    // 添加粒子效果，模拟颤抖
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, shakeX, shakeY + 1.5, shakeZ, 0, 0, 0);

                    // 村民逃离的逻辑
                    double escapeX = villager.getX() + dx / distance * escapeDistance;
                    double escapeY = villager.getY() + dy / distance * escapeDistance;
                    double escapeZ = villager.getZ() + dz / distance * escapeDistance;

                    Path path = villager.getNavigation().createPath(escapeX, escapeY, escapeZ, 1);
                    if (path != null) {
                        villager.getNavigation().moveTo(path, 1.0D);
                    }
                }
            }
        }
    }
}
