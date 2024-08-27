package com.hasoook.hasoookmod.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class EnchantmentBrush extends Item {
    public EnchantmentBrush(Properties pProperties) {
        super(pProperties.stacksTo(1)
                .rarity(Rarity.RARE));
    }
}
