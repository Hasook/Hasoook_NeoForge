package com.hasoook.hasoookmod.block.custom;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;

import java.util.Collections;
import java.util.List;

public class ConfusionFlower extends FlowerBlock {
    public ConfusionFlower(Holder<MobEffect> pEffect, float pSeconds, Properties pProperties) {
        super(pEffect, pSeconds, pProperties);
    }

    public List<ItemStack> getDrops(BlockState pState, LootContext.Builder pBuilder) {
        // 你可以在这里自定义掉落物品
        return Collections.singletonList(new ItemStack(Items.DIAMOND)); // 举例：掉落一个钻石
    }

}
