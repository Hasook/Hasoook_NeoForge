package com.hasoook.hasoookmod.item.custom;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class HundredThousandFansMedal extends Item {
    public HundredThousandFansMedal(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pEntityLiving) {
        Player player = pEntityLiving instanceof Player ? (Player)pEntityLiving : null;
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayer)player, pStack); // 触发使用物品的触发器
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this)); // 奖励使用物品的统计数据
            pStack.consume(1, player); // 消耗一个物品
        }

        if (player == null || !player.hasInfiniteMaterials()) {
            if (pStack.isEmpty()) {
                return new ItemStack(Items.BUCKET);
            }

            if (player != null) {
                player.getInventory().add(new ItemStack(Items.BUCKET)); // 将空瓶添加到玩家背包中
            }
        }

        pEntityLiving.gameEvent(GameEvent.DRINK); // 触发喝水事件
        return pStack;
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
        return 32; // 使用持续时间
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.DRINK; // 播放喝水动画
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        return ItemUtils.startUsingInstantly(pLevel, pPlayer, pHand); // 开始立即使用物品
    }

}
