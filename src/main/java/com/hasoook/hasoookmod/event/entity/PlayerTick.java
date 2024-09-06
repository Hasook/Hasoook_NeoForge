package com.hasoook.hasoookmod.event.entity;

import com.hasoook.hasoookmod.HasoookMod;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class PlayerTick {
    @SubscribeEvent
    public static void PlayerTickEvent(PlayerTickEvent.Post event){
        Player player = event.getEntity();
        if (!player.level().isClientSide && !player.isSpectator() && !player.isCreative()) {
            Minecraft minecraft = Minecraft.getInstance();
            KeyMapping forwardKey = minecraft.options.keyUp; // 前进
            KeyMapping backKey = minecraft.options.keyDown; // 后退
            CompoundTag playerNbt = player.getPersistentData(); // 获取玩家NBT

            float speed = 1;
            float speed2 = player.getSpeed() * 10;
            if (player.isShiftKeyDown()) {
                speed = 0.2F;
            }
            if (player.isSprinting()) {
                speed = 1.5F;
            }

            if (forwardKey.isDown()) {
                float step = playerNbt.getFloat("forwardCount");
                playerNbt.putFloat("forwardCount", step + speed * speed2);
                player.displayClientMessage(Component.literal("前进" + speed * speed2), true);
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
                playerNbt.putFloat("forwardCount", step - speed * speed2);
                player.displayClientMessage(Component.literal("后退"), true);
                if (step <= -4 && player.getHealth() < player.getMaxHealth()) {
                    player.setHealth(player.getHealth() + 2);
                    playerNbt.putFloat("forwardCount", 0);
                }
            }

        }
    }

}
