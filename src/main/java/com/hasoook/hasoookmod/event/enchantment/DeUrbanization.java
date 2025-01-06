package com.hasoook.hasoookmod.event.enchantment;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.enchantment.ModEnchantmentHelper;
import com.hasoook.hasoookmod.enchantment.ModEnchantments;
import com.hasoook.hasoookmod.util.ModTags;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
            BlockPos playerPos = player.blockPosition(); // 获取玩家的位置
            int range = 5; // 遍历玩家周围10格范围内的方块

            // 获取"城市化方块"标签中的方块
            TagKey<Block> urbanizationBlocksTag = ModTags.Blocks.URBANIZATION_BLOCKS;

            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        BlockPos targetPos = playerPos.offset(x, y, z);
                        BlockState blockState = player.level().getBlockState(targetPos);

                        if (blockState.is(urbanizationBlocksTag)) {
                            // 移除该方块
                            player.level().setBlockAndUpdate(targetPos, Blocks.AIR.defaultBlockState());

                            String name = I18n.get(blockState.getBlock().getDescriptionId());
                            player.displayClientMessage(Component.translatable("hasoook.message.de_urbanization.block", name), true);

                            if (Math.random() < 0.5) {
                                player.level().explode(null, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 0, Level.ExplosionInteraction.NONE);
                            }
                        }
                    }
                }
            }
        }
    }
}
