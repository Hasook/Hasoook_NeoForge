package com.hasoook.hasoookmod.event.gamerule;

import com.hasoook.hasoookmod.HasoookMod;
import com.hasoook.hasoookmod.gamerule.ModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

import java.util.List;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class RandomBlockDrops {
    @SubscribeEvent
    public static void BlockDropsEvent(BlockDropsEvent event) {
        Level level = event.getLevel();
        GameRules.BooleanValue rule = level.getGameRules().getRule(ModGameRules.RANDOM_BLOCK_DROPS);

        if (!level.isClientSide && rule.get() && !event.getDrops().isEmpty()) {
            // 动态获取所有非空气物品
            List<Item> items = BuiltInRegistries.ITEM.stream()
                    .filter(item -> item != Items.AIR)
                    .toList();

            if (!items.isEmpty()) {
                event.getDrops().clear();

                // 随机选择物品
                Item randomItem = items.get(level.random.nextInt(items.size()));

                // 生成掉落物
                ItemEntity itemEntity = new ItemEntity(
                        level,
                        event.getPos().getX() + 0.5,
                        event.getPos().getY() + 0.5,
                        event.getPos().getZ() + 0.5,
                        new ItemStack(randomItem, 1)
                );
                event.getDrops().add(itemEntity);
            }
        }
    }
}
