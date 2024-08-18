package com.hasoook.hasoookmod.event.item;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.block.ModBlock;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = HasoookMod.MODID)
public class confusion_flower {
    @SubscribeEvent
    public static void RightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        InteractionHand hand = event.getHand();
        ItemStack itemStack = player.getItemInHand(hand); // 获取玩家手中的物品

        if (itemStack.getItem() == ModBlock.CONFUSION_FLOWER.asItem() && !player.level().isClientSide) {
            Snowball snowball = new Snowball(player.level(), player);
            snowball.setItem(itemStack);
            snowball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
            player.level().addFreshEntity(snowball);
            // 记录玩家使用物品的统计信息
            player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
            // 消耗一个物品
            itemStack.consume(1, player);
        }
    }
}
