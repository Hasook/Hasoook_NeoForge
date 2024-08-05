package com.hasoook.hasoookmod.item.custom;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

public class TotemOfSurrender extends Item {
    public TotemOfSurrender(Properties pProperties) {
        super(pProperties.stacksTo(1).rarity(Rarity.UNCOMMON));
    }
}
