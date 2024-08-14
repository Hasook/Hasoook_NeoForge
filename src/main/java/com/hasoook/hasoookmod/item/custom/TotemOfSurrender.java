package com.hasoook.hasoookmod.item.custom;

import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class TotemOfSurrender extends Item {
    public TotemOfSurrender(Properties pProperties) {
        super(pProperties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pHand);
        pPlayer.startUsingItem(pHand);
        return InteractionResultHolder.consume(itemStack);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
        int time = this.getUseDuration(pStack, pEntityLiving) - pTimeLeft;
        if (time >= 20 && !pLevel.isClientSide && pEntityLiving instanceof Player player) {

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

            // 把被选中的物品移除并且生成掉落物
            for (int slot : slotsToRemove) {
                ItemStack stack = player.getInventory().getItem(slot);
                int bindingLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.BINDING_CURSE, stack);
                // 获取物品的“绑定诅咒”等级
                int vanishingLevel = ModEnchantmentHelper.getEnchantmentLevel(Enchantments.VANISHING_CURSE, stack);
                // 获取物品的“消失诅咒”等级
                if (!stack.isEmpty() && bindingLevel == 0) {
                    if (vanishingLevel == 0) {
                        // 创建掉落物实体并添加到世界中
                        ItemEntity itemEntity = new ItemEntity(pLevel, player.getX(), player.getY(), player.getZ(), stack.copy());
                        pLevel.addFreshEntity(itemEntity);
                    }
                    player.getInventory().setItem(slot, ItemStack.EMPTY); // 清空槽位中的物品
                }
            }

            Minecraft.getInstance().gameRenderer.displayItemActivation(new ItemStack(ModItems.TOTEM_OF_SURRENDER.get()));

            // 传送玩家回到重生点
            if (player instanceof ServerPlayer serverPlayer) {
                Vec3 respawnPos = Vec3.atLowerCornerOf(Objects.requireNonNull(serverPlayer.getRespawnPosition()));
                serverPlayer.teleportTo(respawnPos.x + 0.5, respawnPos.y + 0.4, respawnPos.z + 0.5);
                serverPlayer.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 0, false, true));
            }


        }
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.BOW;
    }

}
