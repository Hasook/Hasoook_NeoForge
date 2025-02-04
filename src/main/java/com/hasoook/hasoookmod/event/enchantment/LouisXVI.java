package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class LouisXVI {
    @SubscribeEvent
    public static void PlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        ItemStack itemStack = event.getItemStack();
        Player player = event.getEntity(); // 玩家
        Entity entity = event.getTarget(); // 实体
        boolean louisXvi = entity.getPersistentData().getBoolean("louis_xvi"); // 获取实体的nbt
        int enchantmentLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.LOUIS_XVI, itemStack); // 获取物品附魔等级

        if (!event.getLevel().isClientSide && !louisXvi && enchantmentLevel > 0) {
            entity.getPersistentData().putBoolean("louis_xvi", true);
            player.swing(event.getHand());
        }
    }

}
