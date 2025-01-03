package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import com.hasoook.hasoookmod.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class DeUrbanization {
    @SubscribeEvent
    public static void PlayerTickEvent(PlayerTickEvent.Post event){
        Player player = event.getEntity();
        ItemStack itemStack = player.getMainHandItem();
        int du = ModEnchantmentHelper.getEnchantmentLevel(ModEnchantments.DE_URBANIZATION, itemStack);
        if (!player.level().isClientSide && du > 0) {
            // 获取玩家的位置
            BlockPos playerPos = player.blockPosition();
            // 遍历玩家周围10格范围内的区块
            int range = 10;

            // 获取REMOVABLE_BLOCKS标签中的方块
            TagKey<Block> removableBlocksTag = ModTags.Blocks.URBANIZATION_BLOCKS;

            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos targetPos = playerPos.offset(x, y, z);
                        BlockState blockState = player.level().getBlockState(targetPos);

                        // 检查该位置的方块是否在REMOVABLE_BLOCKS标签内
                        if (blockState.is(removableBlocksTag)) {
                            // 移除该方块
                            player.level().setBlockAndUpdate(targetPos, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }
    }
}
