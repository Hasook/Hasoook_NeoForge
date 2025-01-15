package com.hasoook.hasoookmod.event.block;

import com.hasoook.hasoookmod.HasoookMod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

@EventBusSubscriber(modid = HasoookMod.MOD_ID)
public class BlockDrops {
    @SubscribeEvent
    public static void BlockDropsEvent(BlockDropsEvent event) {
        Level level = event.getLevel();
        BlockEntity blockentity = event.getBlockEntity();
        if (!level.isClientSide && blockentity != null && !event.getDrops().isEmpty()) {
            // 获取掉落物
            ItemStack itemstack = event.getDrops().getFirst().getItem();
            // 将方块实体的组件给掉落物
            itemstack.applyComponents(blockentity.collectComponents());
        }
    }
}
