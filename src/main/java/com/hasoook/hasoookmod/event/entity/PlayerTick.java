package com.hasoook.hasoookmod.event.entity;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class PlayerTick {
    @SubscribeEvent
    public static void PlayerTickEvent(PlayerTickEvent.Post event){
        Player player = event.getEntity();
        ItemStack itemStack = player.getItemBySlot(EquipmentSlot.FEET);
        int sSSV = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.SEVEN_STEP_SNAKE_VENOM, itemStack);

        if (sSSV > 0 && !player.level().isClientSide && !player.isSpectator() && !player.isCreative()) {
            Minecraft minecraft = Minecraft.getInstance();
            KeyMapping forwardKey = minecraft.options.keyUp; // 前进
            KeyMapping backKey = minecraft.options.keyDown; // 后退
            CompoundTag playerNbt = player.getPersistentData(); // 获取玩家NBT

            float speed = 1; // 默认倍率
            float speed2 = player.getSpeed() * 10; // 速度倍率
            if (player.isShiftKeyDown()) {
                speed = 0.2F; // 潜行时的倍率
            }
            if (player.isSprinting()) {
                speed = 1.5F; // 疾跑时的倍率
            }

            if (forwardKey.isDown()) {
                float step = playerNbt.getFloat("forwardCount");
                playerNbt.putFloat("forwardCount", step + speed * speed2);
                // player.displayClientMessage(Component.literal("前进"), true);
                if (step >= 4) {
                    if (player.getHealth() > 2) {
                        player.setHealth(player.getHealth() - 2);
                        playerNbt.putFloat("forwardCount", 0);
                    } else {
                        player.kill();
                    }
                }
            }

            if (backKey.isDown()) {
                float step = playerNbt.getFloat("forwardCount");
                if (player.getHealth() <= player.getMaxHealth()) {
                    playerNbt.putFloat("forwardCount", step - speed * speed2);
                    // player.displayClientMessage(Component.literal("后退"), true);
                }
                if (step <= -4) {
                    player.setHealth(player.getHealth() + 2);
                    playerNbt.putFloat("forwardCount", 0);
                }
                if (player.getHealth() >= player.getMaxHealth()) {
                    playerNbt.putFloat("forwardCount", 0);
                }
            }

        }
    }

}
