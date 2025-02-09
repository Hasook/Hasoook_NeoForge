package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import com.hasoook.hasoookmod.net.LouisXVIS2CPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class LouisXVI {
    @SubscribeEvent
    public static void onPlayerInteractEntity(PlayerInteractEvent.EntityInteract event) {
        ItemStack itemStack = event.getItemStack();
        Player player = event.getEntity();
        Entity entity = event.getTarget();
        // 使用NBT存储
        CompoundTag persistentData = entity.getPersistentData();
        boolean louisXvi = entity.getPersistentData().getBoolean("louis_xvi");
        int enchantmentLevel = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.LOUIS_XVI, itemStack);

        if (!louisXvi && enchantmentLevel > 0 && entity instanceof LivingEntity) {
            // 服务端设置 NBT
            persistentData.putBoolean("louis_xvi", true);
            player.swing(event.getHand()); // 摇动手臂

            // 发送数据包给所有客户端
            PacketDistributor.sendToAllPlayers(new LouisXVIS2CPacket(entity.getId(), true));

            /*if (event.getLevel() instanceof ServerLevel serverLevel) {
                String id = entity.getEncodeId();
                assert id != null;
                String profileId;
                if (entity instanceof Player) {
                    profileId = id.split(":")[1];  // 只取冒号后的部分
                } else {
                    profileId = "MHF_" + id.split(":")[1];  // 非玩家加上“MHF_”前缀
                }
                serverLevel.getServer().getCommands().performPrefixedCommand(
                        new CommandSourceStack(CommandSource.NULL, new Vec3(entity.getX(), entity.getY(), entity.getZ()), Vec2.ZERO, serverLevel, 4, "", Component.literal(""), serverLevel.getServer(), player)
                                .withSuppressedOutput(),
                        "give @s minecraft:player_head[minecraft:profile=" + profileId + "]"
                );
            }*/

            ItemStack head = new ItemStack(Items.CARVED_PUMPKIN);
            ItemEntity itemEntity = new ItemEntity(entity.level(), entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(), head);

            itemEntity.setPickUpDelay(20);
            entity.level().addFreshEntity(itemEntity); // 将物品实体加入到世界中

            // 消耗一点耐久
            itemStack.hurtAndBreak(1, player, event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                    ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    : net.minecraft.world.entity.EquipmentSlot.OFFHAND);

            // 取消事件
            event.setCanceled(true);

        } else if (louisXvi && itemStack.is(Items.CARVED_PUMPKIN)) {
            persistentData.putBoolean("louis_xvi", false);
            player.swing(event.getHand());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void playerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player.level() instanceof ServerLevel serverLevel) {
            // 获取玩家周围range范围内的生物
            double range = 16.0;
            AABB area = player.getBoundingBox().inflate(range);
            List<LivingEntity> nearbyMobs = serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    area,
                    entity -> entity.getPersistentData().getBoolean("louis_xvi") && entity.isAlive()
            );

            // 网络发包
            for (LivingEntity mob : nearbyMobs) {
                if (player.hasLineOfSight(mob)) { // 检查视线可见
                    PacketDistributor.sendToPlayer(
                            (ServerPlayer) player,
                            new LouisXVIS2CPacket(mob.getId(), true)
                    );
                }
            }
        }
    }
}
