package com.hasoook.hasoookmod.event.entity;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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

        // 如果拾取的是潜影盒本身，直接返回
        if (itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof ShulkerBoxBlock) {
            return;
        }

        // 如果没有拾取延迟，开始处理物品
        if (!pickupDelay) {
            List<ItemStack> shulkerBoxes = findShulkerBoxesInInventory(player);

            // 尝试将物品放入找到的潜影盒
            for (ItemStack shulkerBox : shulkerBoxes) {
                if (insertItemIntoShulkerBox(itemStack, shulkerBox)) {
                    particlesAndSound(itemEntity); // 播放粒子和音效
                    itemStack.setCount(0); // 物品已成功放入，剩余数量为0
                    return;
                }
            }
        }
    }

    // 查找玩家背包中的所有潜影盒
    private static List<ItemStack> findShulkerBoxesInInventory(Player player) {
        Inventory inventory = player.getInventory();
        List<ItemStack> shulkerBoxes = new ArrayList<>();

        // 只查找潜影盒
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            int lootingLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.LOOTING, stack);
            if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock && lootingLevel > 0) {
                shulkerBoxes.add(stack);
            }
        }
        return shulkerBoxes;
    }

    // 将物品放入潜影盒
    private static boolean insertItemIntoShulkerBox(ItemStack itemStack, ItemStack shulkerBox) {
        if (shulkerBox.getCapability(Capabilities.ItemHandler.ITEM, null) instanceof IItemHandlerModifiable iItemHandlerModifiable) {
            int remainingCount = itemStack.getCount();

            // 尝试将物品堆叠到现有物品上
            for (int i = 0; i < iItemHandlerModifiable.getSlots(); i++) {
                ItemStack slotStack = iItemHandlerModifiable.getStackInSlot(i);

                // 如果潜影盒中有相同物品，尝试堆叠
                if (!slotStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, slotStack)) {
                    int availableSpace = slotStack.getMaxStackSize() - slotStack.getCount();
                    if (remainingCount <= availableSpace) {
                        slotStack.grow(remainingCount);
                        iItemHandlerModifiable.setStackInSlot(i, slotStack);
                        return true;
                    } else {
                        slotStack.setCount(slotStack.getMaxStackSize());
                        iItemHandlerModifiable.setStackInSlot(i, slotStack);
                        remainingCount -= availableSpace;
                    }
                }
            }

            // 如果没有找到相同物品，放入空槽
            for (int i = 0; i < iItemHandlerModifiable.getSlots(); i++) {
                ItemStack slotStack = iItemHandlerModifiable.getStackInSlot(i);

                if (slotStack.isEmpty()) {
                    slotStack = itemStack.copy();
                    int countToAdd = Math.min(remainingCount, slotStack.getMaxStackSize());
                    slotStack.setCount(countToAdd);
                    iItemHandlerModifiable.setStackInSlot(i, slotStack);
                    return true;
                }
            }
        }
        return false; // 未成功插入
    }

    // 粒子和音效
    private static void particlesAndSound(Entity entity) {
        ServerLevel serverlevel = (ServerLevel) entity.level();
        entity.playSound(SoundEvents.DECORATED_POT_INSERT, 0.5F, 1F);

        Random random = new Random();
        int ranCount = random.nextInt(4) + 1;
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