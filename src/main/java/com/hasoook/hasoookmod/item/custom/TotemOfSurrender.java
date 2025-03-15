package com.hasoook.hasoookmod.item.custom;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class TotemOfSurrender extends Item {
    public TotemOfSurrender(Properties pProperties) {
        super(pProperties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level pLevel, Player pPlayer, @NotNull InteractionHand pHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void releaseUsing(@NotNull ItemStack pStack, @NotNull Level pLevel, @NotNull LivingEntity pEntityLiving, int pTimeLeft) {
        int time = this.getUseDuration(pStack, pEntityLiving) - pTimeLeft;
        if (time >= 20 && !pLevel.isClientSide && pEntityLiving instanceof ServerPlayer player) {

            pStack.shrink(1);
            // 获取玩家的所有有物品的背包槽位
            List<Integer> slotsWithItems = new ArrayList<>();
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (!stack.isEmpty()) {
                    slotsWithItems.add(i);
                }
            }

            // 随机选择一半的槽位
            Collections.shuffle(slotsWithItems, new Random());
            int numToRemove = slotsWithItems.size() / 2;
            List<Integer> slotsToRemove = slotsWithItems.subList(0, numToRemove);

            for (int slot : slotsToRemove) {
                // 将被选中的槽位里的物品移除并生成掉落物
                ItemStack stack = player.getInventory().getItem(slot);
                int bindingLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.BINDING_CURSE, stack);
                // 获取物品的“绑定诅咒”等级
                int vanishingLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.VANISHING_CURSE, stack);
                // 获取物品的“消失诅咒”等级
                if (bindingLevel == 0) {
                    if (vanishingLevel == 0) {
                        ItemEntity itemEntity = new ItemEntity(pLevel, player.getX(), player.getY(), player.getZ(), stack.copy());
                        pLevel.addFreshEntity(itemEntity);
                        // 创建掉落物实体并添加到世界中
                    }
                    player.getInventory().setItem(slot, ItemStack.EMPTY); // 将槽位设置为空
                }
            }

            Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(ModItems.TOTEM_OF_SURRENDER.get()));
            // 类似不死图腾的动画

            BlockPos bedPosition = player.getRespawnPosition();
            BlockPos spawnPosition = player.level().getSharedSpawnPos();
            if (bedPosition != null && player.level().getBlockState(bedPosition).getBlock() instanceof BedBlock) {
                player.teleportTo(bedPosition.getX() + 0.5, bedPosition.getY() + 1, bedPosition.getZ() + 0.5);// 传送玩家到重生点
            } else {
                player.teleportTo(spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ());// 传送玩家到重生点
            }
        }
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack, @NotNull LivingEntity pEntity) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack pStack) {
        return UseAnim.BOW;
    }

}
