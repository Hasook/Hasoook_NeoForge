package com.hasoook.hasoookmod.event.entity;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class ItemPickup {
    @SubscribeEvent
    public static void ItemPickupEvent(ItemEntityPickupEvent.Pre event) {
        Player player = event.getPlayer();
        ItemEntity itemEntity = event.getItemEntity();
        ItemStack itemStack = itemEntity.getItem();
        boolean pickupDelay = itemEntity.hasPickUpDelay();

        if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
            return; // 如果是潜影盒则不执行
        }

        // 判断能否拾起
        if (!pickupDelay) {
            itemEntity.kill();

            // 获取所有潜影盒
            List<ItemStack> shulkerBoxes = findShulkerBoxesInInventory(player);

            // 如果找到了潜影盒，并且有可修改的物品存储能力
            for (ItemStack shulkerBox : shulkerBoxes) {
                int remainingCount = 0;
                if (!shulkerBox.isEmpty() && shulkerBox.getCapability(Capabilities.ItemHandler.ITEM, null) instanceof IItemHandlerModifiable iItemHandlerModifiable) {
                    remainingCount = itemStack.getCount();
                    // 遍历潜影盒中的物品
                    for (int i = 0; i < iItemHandlerModifiable.getSlots(); i++) {
                        ItemStack slotStack = iItemHandlerModifiable.getStackInSlot(i);

                        // 如果潜影盒中的物品与拾取物品相同，则增加数量
                        if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, slotStack)) {
                            int availableSpace = slotStack.getMaxStackSize() - slotStack.getCount();
                            if (remainingCount <= availableSpace) {
                                // 可以堆叠，直接堆叠
                                slotStack.grow(remainingCount);
                                iItemHandlerModifiable.setStackInSlot(i, slotStack);
                                particlesAndSound(itemEntity);
                                itemStack.setCount(0);
                                return; // 成功放入潜影盒
                            } else {
                                // 设置为满堆叠数量
                                slotStack.setCount(slotStack.getMaxStackSize());
                                iItemHandlerModifiable.setStackInSlot(i, slotStack);
                                remainingCount -= availableSpace; // 减去已堆叠的数量
                            }
                        }
                    }

                    // 如果没有找到相同的物品，尝试将物品直接放入潜影盒的一个空槽中
                    for (int i = 0; i < iItemHandlerModifiable.getSlots(); i++) {
                        ItemStack slotStack = iItemHandlerModifiable.getStackInSlot(i);

                        if (slotStack.isEmpty()) {
                            // 如果槽是空的，直接放入物品
                            slotStack = itemStack.copy();
                            int countToAdd = Math.min(remainingCount, slotStack.getMaxStackSize());
                            slotStack.setCount(countToAdd);
                            iItemHandlerModifiable.setStackInSlot(i, slotStack);
                            particlesAndSound(itemEntity);
                            itemStack.setCount(0);
                            return; // 成功放入潜影盒
                        }
                    }
                }

                // 物品剩余数量小于0时跳出循环
                if (remainingCount <= 0) {
                    return;
                }
            }
        }
    }

    // 查找玩家背包中的所有潜影盒
    private static List<ItemStack> findShulkerBoxesInInventory(Player player) {
        Inventory inventory = player.getInventory();
        List<ItemStack> shulkerBoxes = new ArrayList<>();

        // 遍历所有潜影盒
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            int looting = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, stack);
            if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock && looting > 0) {
                shulkerBoxes.add(stack);
            }
        }
        return shulkerBoxes; // 返回潜影盒列表
    }

    // 粒子和音效
    private static void particlesAndSound(Entity entity) {
        ServerLevel serverlevel = (ServerLevel) entity.level();
        entity.playSound(SoundEvents.DECORATED_POT_INSERT, 0.5F, 1F);

        Random random = new Random();
        int ranCount = random.nextInt(5);
        serverlevel.sendParticles(
                ParticleTypes.DUST_PLUME,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                ranCount,
                0.0,
                0.0,
                0.0,
                0.0
        );
    }
}