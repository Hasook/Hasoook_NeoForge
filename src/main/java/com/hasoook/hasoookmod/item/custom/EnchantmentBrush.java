package com.hasoook.hasoookmod.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;

public class EnchantmentBrush extends Item {
    public EnchantmentBrush(Properties pProperties) {
        super(pProperties.stacksTo(1)
                .rarity(Rarity.RARE));
    }

    /**
     * 检查是否可以附魔
     * @param pStack 物品栈
     * @return 如果物品栈中物品的数量为 1，则返回 true，表示可以附魔；否则返回 false
     */
    @Override
    public boolean isEnchantable(ItemStack pStack) {
        return pStack.getCount() == 1;
    }

    /**
     * 返回物品的附魔值
     * @return 附魔值为 1
     */
    @Override
    public int getEnchantmentValue() {
        return 1;
    }
}

